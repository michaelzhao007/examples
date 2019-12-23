// Lookup.sc
val jsonString = os.read(os.pwd / "ammonite-releases.json")

val data = ujson.read(jsonString)

pprint.log(ujson.write(data, indent = 4), height = 20)

def traverse(v: ujson.Value): Iterable[String] = v match{
  case a: ujson.Arr => a.arr.flatMap(traverse)
  case o: ujson.Obj => o.obj.values.flatMap(traverse)
  case s: ujson.Str => Seq(s.str)
  case _ => Nil
}

pprint.log(traverse(data), height = 20)

def traverse2(v: ujson.Value): Boolean = v match{
  case a: ujson.Arr =>
    a.arr.foreach(traverse2)
    true
  case o: ujson.Obj =>
    o.obj.filterInPlace{case (k, v) => traverse2(v)}
    true
  case s: ujson.Str => !s.str.startsWith("https://")
  case _ => true
}
traverse2(data)
pprint.log(ujson.write(data, indent = 4), height = 20)