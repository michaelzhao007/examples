// Create.sc
val output = ujson.Arr(
  ujson.Obj("hello" -> ujson.Str("world"), "answer" -> ujson.Num(42)),
  ujson.Bool(true)
)

pprint.log(output)

val output2 = ujson.Arr(
  ujson.Obj("hello" -> "world", "answer" -> 42),
  true
)

pprint.log(output2)

pprint.log(ujson.write(output))

println(ujson.write(output))

println(ujson.write(output, indent = 4))