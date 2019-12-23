// LoggingLongPipeline.sc
class Writer(log: os.Path, old: os.Path, rotateSize: Int)
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
class Uploader(url: String)(implicit ac: castor.Context) extends castor.SimpleActor[String]{
  def run(msg: String) = {
    val res = requests.post(url, data = msg)
    println("response " + res.statusCode)
  }
}
class Encoder(dests: Seq[castor.Actor[String]])
             (implicit ac: castor.Context) extends castor.SimpleActor[String]{
  def run(msg: String) = {
    val encoded = java.util.Base64.getEncoder.encodeToString(msg.getBytes)
    for(dest <- dests) dest.send(encoded)
  }
}
class Logger(dest: castor.Actor[String])
            (implicit ac: castor.Context) extends castor.SimpleActor[String]{
  def run(msg: String) = {
    dest.send(msg.replaceAll("([0-9]{4})[0-9]{8}([0-9]{4})", "<redacted>"))
  }
}
// Usage

logger.send("I am cow")
logger.send("hear me moo")
logger.send("I weight twice as much as you")
logger.send("And I look good on the barbecue")
logger.send("Yoghurt curds cream cheese and butter")
logger.send("Comes from liquids from my udder")
logger.send("I am cow, I am cow")
logger.send("Hear me moo, moooo")

ac.waitForInactivity()

assert(os.read(os.pwd / "log-old.txt") == "Q29tZXMgZnJvbSBsaXF1aWRzIGZyb20gbXkgdWRkZXI=\n")
assert(os.read(os.pwd / "log.txt") == "SSBhbSBjb3csIEkgYW0gY293\nSGVhciBtZSBtb28sIG1vb29v\n")

pprint.log(os.read(os.pwd / "log-old.txt"))
pprint.log(os.read(os.pwd / "log.txt"))

def decodeFile(p: os.Path) = {
  os.read.lines(p).map(s => new String(java.util.Base64.getDecoder.decode(s)))
}

assert(decodeFile(os.pwd / "log-old.txt") == Seq("Comes from liquids from my udder"))
assert(decodeFile(os.pwd / "log.txt") == Seq("I am cow, I am cow", "Hear me moo, moooo"))

pprint.log(decodeFile(os.pwd / "log-old.txt"))
pprint.log(decodeFile(os.pwd / "log.txt"))