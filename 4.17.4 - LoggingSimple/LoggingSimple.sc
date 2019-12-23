// LoggingSimple.sc
import $ivy.`com.lihaoyi::castor:0.1.1`

class Logger(log: os.Path, old: os.Path, rotateSize: Int)
            (implicit ac: castor.Context) extends castor.SimpleActor[String]{
  def run(s: String) = {
    val newLogSize = logSize + s.length + 1
    if (newLogSize <= rotateSize) logSize = newLogSize
    else {
      logSize = s.length
      os.move(log, old, replaceExisting = true)
    }
    os.write.append(log, s + "\n", createFolders = true)
  }
  private var logSize = 0
}

implicit val ac = new castor.Context.Test()

val logger = new Logger(os.pwd / "log.txt", os.pwd / "log-old.txt", rotateSize = 50)

// Usage
logger.send("I am cow")
logger.send("hear me moo")
logger.send("I weight twice as much as you")
logger.send("And I look good on the barbecue")
logger.send("Yoghurt curds cream cheese and butter")
logger.send("Comes from liquids from my udder")
logger.send("I am cow, I am cow")
logger.send("Hear me moo, moooo")

// Logger hasn't finished yet, running in the background
ac.waitForInactivity()
// Now logger has finished

assert(os.read.lines(os.pwd / "log-old.txt") == Seq("Comes from liquids from my udder"))
assert(os.read.lines(os.pwd / "log.txt") == Seq("I am cow, I am cow", "Hear me moo, moooo"))

pprint.log(os.read.lines(os.pwd / "log-old.txt"))
pprint.log(os.read.lines(os.pwd / "log.txt"))