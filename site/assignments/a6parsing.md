---
layout: default
title: Assignment 6 - Parsing
root: "../"
---

**Due: Tuesday, December 3.  Programming at noon.  Written portions at 2pm.**

* Written portions are found throughout the assignment, and are clearly marked.
* Coding portions must be turned in via GitHub using the tag `a5`.


## Introduction

This homework is designed to guide you in constructing a syntactic parser built from a probabilistic context-free grammar (PCFG).

To complete the homework, use the code and interfaces found in the class GitHub repository.

* Your written answers should be hand-written or printed and handed in before class. The problem descriptions clearly state where a written answer is expected.
* Programming portions should be turned in via GitHub by noon on the assignment due date.

There are 100 points total in this assignment. Point values for each problem/sub-problem are given below.

The classes used here will extend traits that are found in the `nlpclass-fall2013` dependency.  In order to get these updates, you will need to edit your root `build.sbt` file and update the version of the dependency:

    libraryDependencies += "com.utcompling" % "nlpclass-fall2013_2.10" % "0009" changing()

If you use Eclipse, then after you modify the dependency you will once again have to run `sbt "eclipse with-source=true"` and refresh your project in Eclipse.


**If you have any questions or problems with any of the materials, don't hesitate to ask!**

**Tip:** Look over the entire homework before starting on it. Then read through each problem carefully, in its entirety, before answering questions and doing the implementation.

Finally: if possible, don't print this homework out! Just read it online, which ensures you'll be looking at the latest version of the homework (in case there are any corrections), you can easily cut-and-paste and follow links, and you won't waste paper.



## Part 1: Trees

With a context-free grammar, the syntax of a sentence is represented as a tree.  Thus, your code will have to represent trees.  I have provided a some tools to get your started.

First, there is an trait `nlpclass.Tree` that will encompass all tree structures.  The trait requires only that a tree implementation have a `label`, and a vector of `children`:

{% highlight scala %}
trait Tree {
  def label: String
  def children: Vector[Tree]
}
{% endhighlight %}

Since you will have to read in data from files, it will be necessary to be able to convert a string representation of a tree into a `Tree` object.  The `nlpclass.Tree.fromString` method does exactly this.

{% highlight scala %}
import nlpclass._
val s = "(S (NP (D the) (N man)) (VP (V walks) (NP (D the) (N dog))))"
val t = Tree.fromString(s)
{% endhighlight %}

The `Tree.fromString` method returns an object that implements the `Tree` trait.  Specifically, it returns an instance of `nlpclass.TreeNode`, a very basic kind of `Tree`.  A `TreeNode` simply stores the label and children that are required by the `Tree` trait (and the `children` field defaults to an empty vector):

{% highlight scala %}
case class TreeNode(label: String, children: Vector[Tree] = Vector()) extends Tree
{% endhighlight %}

The `Tree` trait also provides a `toString` implementation and method called `pretty` that can be used to view the tree in different forms.

    scala> println(t)
    (S (NP (D the) (N man)) (VP (V walks) (NP (D the) (N dog))))
    scala> println(t.pretty)
    S
      NP
        D the
        N man
      VP
        V walks
        NP
          D the
          N dog

Further, I have provided a method `nlpclass.TreeViz.drawTree` that produces an image of the tree

{% highlight scala %}
scala> TreeViz.drawTree(t)
{% endhighlight %}


## Part 2: Chomsky Normal Form 

### To CNF

Our goal is to implement the CKY algorithm for parsing.  Since CKY requires that all trees be in Chomsky Normal Form (CNF), we will first write a function for transforming a tree into CNF.  However, we will not strictly be using CNF; we want to allow for unary productions since this makes it easier to covert back from CNF.

A tree is in CNF (with unary rules) if each of its productions fall into one of three categories:

* A non-terminal yielding exactly two non-terminals: A → B C
* A non-terminal yielding exactly one non-terminals: A → B
* A non-terminal yielding exactly one terminal: A → w

It is provable that any CFG can be converted to a CFG in CNF.  To transform any tree into a CNF tree, you should trace through the tree, making the following change:

* If the node has more than two children, take the last two children X and Y and group them into a new node {X+Y} that has X and Y as children.  Repeat until there are exactly two children.

    (NP (D a) (A big) (A brown) (N dog))   
    → (NP (D a) (A big) ({A+N} (A brown) (N dog)))  
    → (NP (D a) ({A+{A+N}} (A big) ({A+N} (A brown) (N dog))))  

    Note: While it would be possible to group in a left-branching way (eg, "(NP ({D+A} N))" instead of "(NP (D {A+N}))"), I find it more visually appealing to branch right since English is generally a right-branching language.

We do not need to worry about cases where terminals are found in non-unary productions since all of our terminals are words, which cannot have children, and all words are produced by part-of-speech tags, which do not have non-terminal children.

For this part of the assigment, you will create an object called `nlp.a6.Cnf` with a method `convertTree` that takes a tree as a parameter and returns a new tree that is in CNF:
{% highlight scala %}
object Cnf {
  def convertTree(t: Tree): ? = {
    // your code here
  }
}
{% endhighlight %}

Note the question mark: your result tree **does not need to use the TreeNode class**.  You are welcome to implement your own CNF tree class or CNF tree class hierarchy if you find it useful.  Whatever you decide to use as your CNF tree representation will be the return type from `apply`.  If you want the tree printing and TreeViz functionality, then have your classes extend `nlpclass.Tree`.

Once this method is implemented, you should get behavior similar to this:

{% highlight scala %}
scala> import nlpclass._
scala> val s = "(S (NP (D the) (A big) (N dog)) (VP (V walks)))"
scala> val t = Tree.fromString(s)

scala> t.pretty
S
  NP
    D the
    A big
    N dog
  VP V walks

scala> import nlp.a6._
scala> val c = Cnf.convertTree(t)
(S (NP (D the) ({A+N} (A big) (N dog))) (VP (V walks)))

scala> c.pretty
S
  NP
    D the
    {A+N}
      A big
      N dog
  VP V walks
{% endhighlight %}


### From CNF

Since we untimately want our parser to be able to give us trees in the form of our training data (which may not be in CNF), we will need a function for reversing the CNF conversion.

You will write a function `Cnf.undo` that takes a tree in CNF and returns a `Tree` in the original format.

{% highlight scala %}
scala> val u = Cnf.undo(c)
(S (NP (D the) (A big) (N dog)) (VP (V walks)))

scala> u.pretty
S
  NP
    D the
    A big
    N dog
  VP V walks
{% endhighlight %}


### Written Exercises

Assume the following three-sentence dataset:
    
    (V walk)
    (S (NP (D the) (A big) (N dogs)) (VP (V walk)))
    (S (NP (D the) (A tall) (N men)) (VP (V walk) (NP (D the) (N dogs))))

> **Written Answer (a):** Give the PCFG that would be generated by this dataset using the Maximum Likelihood Estimate.

> **Written Answer (b):** Transform the PCFG from above into its CNF (with unary productions) equivalent.



## Part 3: Likelihood of a Parsed Sentence

You will need to create a class `nlp.a6.PcfgParser` that extends the trait `nlpclass.Parser`.  For this part, you should implement the `likelihood` method (you can use `???` for the other methods to prevent compiler errors):

{% highlight scala %}
class PcfgParser(...) extends Parser {

  def likelihood(t: Tree): Double = {
    // your code here
  }

  def parse(tokens: Vector[String]): Option[Tree] = ???
  def generate(): Tree = ???
}
{% endhighlight %}

This method should take a `Tree` as input and return the likelihood of that tree.  The result should be returned as a **logarithm** (and computed that way as well).

In order to calculate the likelihood, you will need two probability distributions:

* The conditional probability distribution `\( p(\beta \mid A) \)` for non-terminal A and production `\( \beta \)`
* The probability distribution over possible root tree nodes `\( p(\sigma) \)` for production `\( \sigma \)`

Since we will need the grammar to be in CNF for parsing, you should assume (or ensure?) that these distributions are in CNF.  In other words, `\( \beta \)` and `\( \sigma \)` can only have three forms: a pair of nonterminals (B C), a single nonterminal (B), or a single terminal (*w*).

The likelihood of a parsed sentence is computed as the product of all productions in the tree.  Additionally, you must take into consideration the likelihood of the tree's root.

Since you will be storing your probability distributions for the grammar in CNF, you will need to convert the incoming tree into CNF before looking up the productions in the distributions.


### Written Exercises

For the following parsed sentence:

    (S (NP (D the) (A tall) (N dogs)) (VP (V walk)))

> **Written Answer (a):** Calculate the likelihood given the PCFG from exercise 2a.

> **Written Answer (a):** Convert the parsed sentence to CNF (with unary rules) and calculate the likelihood given the PCFG from exercise 2b.  Confirm that this is the same value as you found in 3a.




## Part 4: Unsmoothed PCFG Parser Trainer

To initialize the probability distributions for the PcfgParser from the Maximum Likelihood Estimate (MLE), you will implement a class `nlp.a6.UnsmoothedPcfgParserTrainer` that extends `nlpclass.ParserTrainer` and implements a method called `train`:

{% highlight scala %}
class UnsmoothedPcfgParserTrainer(...) extends ParserTrainer {
  def train(trees: Vector[Tree]): PcfgParser = {
    // your code here
  }
}
{% endhighlight %}

The `train` method will count up productions across all given trees to compute the MLE.  Since the `PcfgParser` will be expecting probability distributions over productions that conform to CNF, you should convert all the given trees to CNF before computing the MLE.

To check your implementation, assume the following dataset (`trees2`):

    (S (NP (D the) (N dog)) (VP (V barks)))
    (S (NP (D the) (N dog)) (VP (V walks)))
    (S (NP (D the) (N man)) (VP (V walks) (NP (D the) (N dog))))

And you should be able to do this:

{% highlight scala %}
val trainer = new UnsmoothedPcfgParserTrainer(...)
val pcfg = trainer.train(trees2)
val s1 = "(S (NP (D the) (N dog)) (VP (V walks) (NP (D the) (N man)))))"
pcfg.likelihood(Tree.fromString(s1)) // -3.1780538303479453
val s2 = "(S (NP (D a) (N cat)) (VP (V runs)))"
pcfg.likelihood(Tree.fromString(s2)) // -Infinity
{% endhighlight %}


## Part 5: Parsing with P-CKY

For this part you will implement the `parse` method on `PcfgParser`:

{% highlight scala %}
def parse(tokens: Vector[String]): Option[Tree]
{% endhighlight %}

This method takes a sentence (as a sequence of tokens), runs the Probabilitic CKY algorithm, and returns a `Tree` representing the most likely parse of that sentence, if there is one, and returns `None` if there is no valid parse of the sentence.  Be sure to convert the tree back from CNF before returning it.

Using this dataset (`trees3`):

    (S (NP (D the) (A big) (N dog)) (VP (V barks)))
    (S (NP (D the) (N dog)) (VP (V walks)))
    (S (NP (D the) (A tall) (N man)) (VP (V walks) (NP (D the) (N dog))))
    (S (NP (D the) (N man)) (VP (V saw) (NP (D the) (N dog) (PP (P in) (NP (D a) (N house))))))
    (S (NP (D the) (N man)) (VP (V saw) (NP (D the) (N dog)) (PP (P with) (NP (D a) (N telescope)))))

you should see this behavior without smoothing:

{% highlight scala %}
scala> val trainer = new UnsmoothedPcfgParserTrainer(...)
scala> val pcfg = trainer.train(trees3)

scala> val s1 = "the dog walks the man".split(" ").toVector
scala> pcfg.parse(s1).fold("None")(_.pretty)
S
  NP
    D the
    N dog
  VP
    V walks
    NP
      D the
      N man

scala> val s2 = "a man in the telescope barks the dog with the house with a telescope".split(" ").toVector
scala> pcfg.parse(s2).fold("None")(_.pretty)
S
  NP
    D a
    N man
    PP
      P in
      NP
        D the
        N telescope
  VP
    V barks
    NP
      D the
      N dog
    PP
      P with
      NP
        D the
        N house
        PP
          P with
          NP
            D a
            N telescope

scala> val s3 = "a cat walks".split(" ").toVector
scala> pcfg.parse(s3).fold("None")(_.pretty)
None
{% endhighlight %}



## Part 6: Generating Text

Since a PCFG is a generative model, we can use it to generate sentences.  For this part, you will implement the `generate` method on `PcfgParser`.  The method should return a `Tree` object 

{% highlight scala %}
def generate(): Tree
{% endhighlight %}

Your method should first sample some non-terminal A from the distribution `\( p(\sigma) \)` over possible start non-terminals.  Then, it should sample some `\( \beta \)` from `\( p(\beta \mid A) \)`.  For each non-terminal in `\( \beta \)`, you should recursively sample a next production until all paths result in terminal nodes (words).

Be sure to convert your generated tree back from CNF before returning it.

Using `trees3` from Part 5 above, you should get something like this:

{% highlight scala %}
scala> val trainer = new UnsmoothedPcfgParserTrainer()
scala> val pcfg = trainer.train(trees3)
scala> pcfg.generate().sentence.mkString(" ")
"the dog with the man saw the dog"
{% endhighlight %}

### Agreement features

Using this data:

    (S (NP (D all) (N dogs)) (VP (V bark)))
    (S (NP (D a) (N man)) (VP (V walks) (NP (D a) (N dog))))

you might see trees like this:

{% highlight scala %}
"a dogs bark"
"all man bark all man"
{% endhighlight %}

A large number of possible sentences can be generated from this grammar, but not all of them would generally be considered grammatical.

To ensure number agreement, we can add *features* to the same data:

{% highlight scala %}
(S (NPpl (Dpl all) (Npl dogs)) (VPpl (Vpl bark)))
(S (NPsg (Dsg a) (Nsg man)) (VPsg (Vsg walks) (NPpl (Dpl all) (Npl dogs))))
{% endhighlight %}

and, again, generate trees.  But with this grammar, all trees will have number agreement.

> **Written Answer (a):** How many distinct trees can be generated from the above grammar with features?  What is the probability of each tree?




## Part 7: Add-λ Smoothed PCFG

We would ultimately like for our parser to be able to return a "best guess" tree for *any* sentence that it is given.

Ensure that all compound non-terminals created during CNF converion are left unsmoothed.  After all, "{A+N}" should never produce anything other than "A N".  Also ensure that no compund non-terminal is given a non-zero probability in the start symbol distribution since a compound non-terminal must always have a parent.



## Part 8: The Penn Treebank



