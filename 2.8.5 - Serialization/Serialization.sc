// Lookup.sc
val jsonString = os.read(os.pwd / "ammonite-releases.json")

val data = ujson.read(jsonString)

println(ujson.write(data(0)("author"), indent=4))

case class Author(login: String, id: Int, site_admin: Boolean)

implicit val authorRW = upickle.default.macroRW[Author]

val author = upickle.default.read[Author](data(0)("author"))

pprint.log(author)

pprint.log(author.login)
pprint.log(author.id)
pprint.log(author.site_admin)

case class Author2(login: String,
                   id: Int,
                   @upickle.implicits.key("site_admin") siteAdmin: Boolean)

implicit val author2RW = upickle.default.macroRW[Author2]

val author2 = upickle.default.read[Author](data(0)("author"))

pprint.log(author2)

pprint.log(upickle.default.write(author))
pprint.log(upickle.default.write(author2))

pprint.log(ujson.write(data(0)("assets"), indent = 4), height = 20)

case class Asset(id: Int, name: String)

implicit val assetRW = upickle.default.macroRW[Asset]

val assets = upickle.default.read[Seq[Asset]](data(0)("assets"))

pprint.log(assets)

case class Uploader(id: Int, login: String, `type`: String)

case class Asset2(id: Int, name: String, uploader: Uploader)

implicit val uploaderRW = upickle.default.macroRW[Uploader]

implicit val asset2RW = upickle.default.macroRW[Asset2]

val assets2 = upickle.default.read[Seq[Asset2]](data(0)("assets"))

pprint.log(assets2)

case class Asset3(id: Int, name: String, uploader: ujson.Value)

implicit val asset3RW = upickle.default.macroRW[Asset3]

val assets3 = upickle.default.read[Seq[Asset3]](data(0)("assets"))

pprint.log(assets3)

pprint.log(upickle.default.write(assets, indent = 4), height = 20)
pprint.log(upickle.default.write(assets2, indent = 4), height = 20)
pprint.log(upickle.default.write(assets3, indent = 4), height = 20)

val blob = upickle.default.writeBinary(Author("haoyi", 31337, true))

pprint.log(blob, height = 20)

pprint.log(upickle.default.readBinary[Author](blob))

val mapListAuthors = Map(
  1 -> Nil,
  2 -> List(Author("haoyi", 1337, true), Author("lihaoyi", 31337, true))
)

val blob2 = upickle.default.writeBinary(mapListAuthors)

pprint.log(upickle.default.readBinary[Map[Int, List[Author]]](blob2))

pprint.log(upack.read(blob), height = 20)

pprint.log(upack.read(blob2), height = 20)

val msg = upack.Obj(
  upack.Str("login") -> upack.Str("haoyi"),
  upack.Str("id") -> upack.Int32(31337),
  upack.Str("site_admin") -> upack.True
)

val blob3 = upack.write(msg)
pprint.log(blob3, height = 20)

val deserialized = upickle.default.readBinary[Author](blob3)

pprint.log(deserialized)