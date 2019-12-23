// Lookup.sc
val jsonString = os.read(os.pwd / "ammonite-releases.json")

val data = ujson.read(jsonString)

pprint.log(data(0), height = 20)

pprint.log(data(0)("url"))

pprint.log(data(0)("author")("id"))

pprint.log(data(0).obj, height = 20)

pprint.log(data(0).obj.keys, height = 20)

pprint.log(data(0).obj.size)

pprint.log(data(0)("url").str)

pprint.log(data(0)("author")("id").num)

pprint.log(data(0)("author")("id").num.toInt)