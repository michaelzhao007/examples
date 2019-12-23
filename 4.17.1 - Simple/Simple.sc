import $ivy.`com.lihaoyi::castor:0.1.1`

implicit val ac = new castor.Context.Test()

object UploadSimpleActor extends castor.SimpleActor[String]{
  def run(msg: String) = {
    val res = requests.post("https://httpbin.org/post", data=msg)
    println("response " + res.statusCode)
  }
}

// Usage
println("sending hello")
UploadSimpleActor.send("hello")

println("sending world")
UploadSimpleActor.send("world")

println("sending !")
UploadSimpleActor.send("!")

ac.waitForInactivity()