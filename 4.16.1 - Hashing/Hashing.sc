import $ivy.`org.springframework.security:spring-security-crypto:5.1.6.RELEASE`
import org.springframework.security.crypto.bcrypt.BCrypt
import scala.concurrent._, ExecutionContext.Implicits.global, duration.Duration

// Sequential
val (hashes1, duration1) = time{
  val base64 = java.util.Base64.getEncoder()
  for(p <- os.walk(os.pwd / "post") if os.isFile(p)) yield {
    println(p)
    BCrypt.hashpw(base64.encodeToString(os.read.bytes(p)), BCrypt.gensalt())
  }
}

pprint.log(hashes1)
pprint.log(duration1)

// Parallel
val (hashes2, duration2) = time{
  val base64 = java.util.Base64.getEncoder()
  val futures = for(p <- os.walk(os.pwd / "post") if os.isFile(p)) yield Future{
    println(p)
    BCrypt.hashpw(base64.encodeToString(os.read.bytes(p)), BCrypt.gensalt())
  }
  futures.map(Await.result(_, Duration.Inf))
}

pprint.log(hashes2)
pprint.log(duration2)

@main def main(args: String*) = {
  pprint.log(args)
}
