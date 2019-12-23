// LoggingDebounced.sc
import $ivy.`com.lihaoyi::castor:0.1.1`
sealed trait Msg
case class Flush() extends Msg
case class Text(value: String) extends Msg

class Logger(log: os.Path, debounceTime: java.time.Duration)
            (implicit ac: castor.Context) extends castor.StateMachineActor[Msg]{
  def initialState = Idle()
  case class Idle() extends State({
    case Text(value) =>
      ac.scheduleMsg(this, Flush(), debounceTime)
      Buffering(Vector(value))
  })
  case class Buffering(buffer: Vector[String]) extends State({
    case Text(value) => Buffering(buffer :+ value)
    case Flush() =>
      os.write.append(log, buffer.mkString(" ") + "\n", createFolders = true)
      Idle()
  })
}

implicit val ac = new castor.Context.Test()

val logPath = os.pwd / "out" / "scratch" / "log.txt"

val logger = new Logger(logPath, java.time.Duration.ofMillis(50))

// Usage
logger.send(Text("I am cow"))
logger.send(Text("hear me moo"))
Thread.sleep(100)
logger.send(Text("I weight twice as much as you"))
logger.send(Text("And I look good on the barbecue"))
Thread.sleep(100)
logger.send(Text("Yoghurt curds cream cheese and butter"))
logger.send(Text("Comes from liquids from my udder"))
logger.send(Text("I am cow, I am cow"))
logger.send(Text("Hear me moo, moooo"))

ac.waitForInactivity()

assert(os.read.lines(logPath) == Seq(
  "I am cow hear me moo",
  "I weight twice as much as you And I look good on the barbecue",
  "Yoghurt curds cream cheese and butter Comes from liquids from my udder I am cow, I am cow Hear me moo, moooo",
))
pprint.log(os.read.lines(logPath))