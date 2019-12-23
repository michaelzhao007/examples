// GenericMergeSort.sc
def mergeSort[T: Ordering](items: IndexedSeq[T]): IndexedSeq[T] = {
  if (items.length == 1) items
  else {
    val (left, right) = items.splitAt(items.length / 2)
    val sortedLeft = mergeSort(left)
    val sortedRight = mergeSort(right)
    var leftIndex = 0
    var rightIndex = 0
    val output = IndexedSeq.newBuilder[T]
    while(leftIndex < sortedLeft.length || rightIndex < sortedRight.length){
      val takeLeft = (leftIndex < sortedLeft.length, rightIndex < sortedRight.length) match{
        case (true, false) => true
        case (false, true) => false
        case (true, true) => Ordering[T].lt(sortedLeft(leftIndex), sortedRight(rightIndex))
      }
      if (takeLeft){
        output += sortedLeft(leftIndex)
        leftIndex += 1
      }else{
        output += sortedRight(rightIndex)
        rightIndex += 1
      }
    }
    output.result()
  }
}
// Usage
val input = Array("banana", "mandarin", "avocado", "apple", "mango", "cherry", "mangosteen")
pprint.log(input)
val output = mergeSort(input)
pprint.log(output)
