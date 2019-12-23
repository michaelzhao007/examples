import $ivy.`com.lihaoyi::castor:0.1.1`
// requires amm --class-based to run

implicit val ac = new castor.Context.Test()

sealed trait Msg
case class Text(s: String) extends Msg
case class Flush() extends Msg
object UploadStateMachineActor extends castor.StateMachineActor[Msg]{
  def initialState = new Idle()

  case class Idle() extends State({
    case Text(msg) => upload(msg)
  })

  case class Buffering(msgs: Vector[String]) extends State({
    case Text(s) => Buffering(msgs :+ s)
    case Flush() =>
      if (msgs.isEmpty) Idle()
      else upload(msgs.mkString)
  })

  def upload(data: String) = {
    println("Uploading " + data)
    val res = requests.post("https://httpbin.org/post", data=data)
    println("response " + res.statusCode)
    ac.scheduleMsg(this, Flush(), java.time.Duration.ofSeconds(10))
    Buffering(Vector.empty)
  }
}
// Usage
println("Sending...")
UploadStateMachineActor.send(Text("I am Cow"))
Thread.sleep(15000)

println("Sending...")
UploadStateMachineActor.send(Text("Hear me moo"))
println("Sending...")
UploadStateMachineActor.send(Text("I weigh twice as much as you"))
Thread.sleep(15000)

println("Sending...")
UploadStateMachineActor.send(Text("And I look good on the barbecue"))
ac.waitForInactivity()