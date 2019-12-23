// Lookup.sc
val output = ujson.Arr(
  ujson.Obj("hello" -> "world", "answer" -> 42),
  true
)

println(output)

output(0)("hello") = "goodbye"

output(0)("tags") = ujson.Arr("awesome", "yay", "wonderful")

println(output)

output(0).obj.remove("hello")

println(output)

output.arr.append(123)

println(output)

output.arr.clear()

println(output)