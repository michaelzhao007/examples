import $ivy.`com.lihaoyi::castor:0.1.1`

implicit val ac = new castor.Context.Test()

object UploadBatchActor extends castor.BatchActor[String]{
  def runBatch(msgs: Seq[String]) = {
    val res = requests.post("https://httpbin.org/post", data=msgs.mkString)
    println("response " + res.statusCode)
  }
}

// Usage
println("sending hello")
UploadBatchActor.send("hello")

println("sending world")
UploadBatchActor.send("world")

println("sending !")
UploadBatchActor.send("!")

ac.waitForInactivity()