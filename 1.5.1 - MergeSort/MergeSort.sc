// MergeSort.sc
def mergeSort(items: Array[Int]): Array[Int] = {
  if (items.length == 1) items
  else {
    val (left, right) = items.splitAt(items.length / 2)
    val sortedLeft = mergeSort(left)
    val sortedRight = mergeSort(right)
    var leftIndex = 0
    var rightIndex = 0
    val output = Array.newBuilder[Int]
    while(leftIndex < sortedLeft.length || rightIndex < sortedRight.length){
      val takeLeft = (leftIndex < sortedLeft.length, rightIndex < sortedRight.length) match{
        case (true, false) => true
        case (false, true) => false
        case (true, true) => sortedLeft(leftIndex) < sortedRight(rightIndex)
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
val input = Array(8, 3, 5, 4, 6, 1, 0, 2, 7, 9)
pprint.log(input)
val output = mergeSort(input)
pprint.log(output)
