// Phrases.sc
sealed trait Phrase
case class Word(s: String) extends Phrase
case class Pair(lhs: Phrase, rhs: Phrase) extends Phrase

import fastparse._, NoWhitespace._
def prefix[_: P] = P( "hello" | "goodbye" ).!.map(Word)
def suffix[_: P] = P( "world" | "seattle" ).!.map(Word)
def ws[_: P] = P( " ".rep(1) )
def parened[_: P] = P( "(" ~ parser ~ ")" )
def parser[_: P]: P[Phrase] = P( (parened | prefix) ~ ws ~ (parened | suffix) ).map{
  case (lhs, rhs) => Pair(lhs, rhs)
}
// Usage
pprint.log(fastparse.parse("hello seattle", parser(_)))
pprint.log(fastparse.parse("hello (goodbye seattle)", parser(_)))
pprint.log(fastparse.parse("(hello  world)   (goodbye seattle)", parser(_)))
pprint.log(fastparse.parse("(hello  world)   ((goodbye seattle) world)", parser(_)))