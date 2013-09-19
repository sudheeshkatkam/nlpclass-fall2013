package nlpclass

import scala.collection.JavaConverters._
import dhg.util.CollectionUtil._

/**
 * Lemmatize
 */
object Lemmatize extends (String => String) {

  /**
   * Turn raw text into a Vector of tokenized sentences
   */
  override def apply(word: String): String = {
    val Vector(Vector(Token(_, lemma))) = Tokenize(word)
    lemma
  }

}
