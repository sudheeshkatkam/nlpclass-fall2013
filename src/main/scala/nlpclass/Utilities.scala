package nlpclass

import scala.collection.GenIterable
import scala.collection.GenIterableLike

object Utilities {

  implicit class Enriched_takeSub_Iterator[A, R <: GenIterable[A]](val self: Iterator[GenIterableLike[A, R]]) {
    /**
     * Take iterables from this collection until the total number of elements
     * in the taken items is about to exceed `n`.  The total number of
     * elements will be less than or equal to `n`.
     *
     * @param n	the maximum number of sub-elements to take
     * @return the new collection
     */
    def takeSub(n: Int): Iterator[R] = {
      if (self.isEmpty) {
        self.asInstanceOf[Iterator[R]]
      }
      else {
        new Iterator[R] {
          private var nextElement: R = self.next.asInstanceOf[R]
          private var total: Int = nextElement.size

          override def hasNext = total <= n

          override def next = {
            if (hasNext) {
              val x = nextElement
              if (self.hasNext) {
                nextElement = self.next.asInstanceOf[R]
                total += nextElement.size
              }
              else
                total = n + 1
              x
            }
            else
              throw new NoSuchElementException("next on empty iterator")
          }
        }
      }
    }
  }

}
