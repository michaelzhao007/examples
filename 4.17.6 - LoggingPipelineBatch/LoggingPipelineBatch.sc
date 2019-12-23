// LoggingPipelineBatch.sc
import $ivy.`com.lihaoyi::castor:0.1.1`
sealed trait Msg
case class Text(value: String) extends Msg
case class Rotate() extends Msg
class Writer(log: os.Path, old: os.Path)
            (implicit ac: castor.Context) extends castor.BatchActor[Msg]{
  def runBatch(msgs: Seq[Msg]): Unit = {
    msgs.lastIndexOf(Rotate()) match{
      case -1 => os.write.append(log, groupMsgs(msgs), createFolders = true)
      case rotateIndex =>
        val prevRotateIndex = msgs.lastIndexOf(Rotate(), rotateIndex - 1)
        if (prevRotateIndex != -1) os.remove.all(log)
        os.write.append(log, groupMsgs(msgs.slice(prevRotateIndex, rotateIndex)), createFolders = true)
        os.move(log, old, replaceExisting = true)
        os.write.over(log, groupMsgs(msgs.drop(rotateIndex)), createFolders = true)
    }
  }
  def groupMsgs(msgs: Seq[Msg]) = msgs.collect{case Text(value) => value}.mkString("\n") + "\n"
}

class Logger(dest: castor.Actor[Msg], rotateSize: Int)
            (implicit ac: castor.Context) extends castor.SimpleActor[String]{
  def run(s: String) = {
    val newLogSize = logSize + s.length + 1
    if (newLogSize <= rotateSize) logSize = newLogSize
    else {
      logSize = s.length
      dest.send(Rotate())
    }
    dest.send(Text(s))
  }
  private var logSize = 0
}

implicit val ac = new castor.Context.Test()

val writer = new Writer(os.pwd / "log.txt", os.pwd / "log-old.txt")
val logger = new Logger(writer, rotateSize = 50)

logger.send("I am cow")
logger.send("hear me moo")
logger.send("I weight twice as much as you")
logger.send("And I look good on the barbecue")
logger.send("Yoghurt curds cream cheese and butter")
logger.send("Comes from liquids from my udder")
logger.send("I am cow, I am cow")
logger.send("Hear me moo, moooo")

ac.waitForInactivity()

assert(os.read.lines(os.pwd / "log-old.txt") == Seq("Comes from liquids from my udder"))
assert(os.read.lines(os.pwd / "log.txt") == Seq("I am cow, I am cow", "Hear me moo, moooo"))
pprint.log(os.read.lines(os.pwd / "log-old.txt"))
pprint.log(os.read.lines(os.pwd / "log.txt"))