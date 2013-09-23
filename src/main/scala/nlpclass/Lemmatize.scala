package nlpclass

import dhg.util.CollectionUtil._

/**
 * Lemmatize
 */
object Lemmatize extends (String => String) {

  /**
   * Turn a raw word into its lemma
   */
  override def apply(word: String): String = {
    Tokenize(word).flatten.map(_.lemma).mkString(" ")
  }

}
