---
layout: default
title: Assignment 2 - Classification
root: "../"
---

**Due: Thursday, October 1.  Written portion by 2pm, programming by noon**


## Introduction

For this homework, you will implement a naive Bayes classifier that estimates its parameters from training material in order to classify the unseen instances in test material. You will also extract features from data instances to improve classification accuracy.

To complete the homework, use the stub programs and data found in the class GitHub repository.

* Your written answers should be hand-written or printed and handed in before class. The problem descriptions clearly state where a written answer is expected.
* Programming portions should be turned in via GitHub by noon on the assignment due date.

There are 100 points total in this assignment. Point values for each problem/sub-problem are given below.


The used here classes will extend traits that are found version **0003** of the `nlpclass-fall2013` dependency.  In order to get these updates, you will need to edit your root `build.sbt` file and update the version of the dependency:

    libraryDependencies += "com.utcompling" % "nlpclass-fall2013_2.10" % "0003" changing()

If you use Eclipse, then after you modify the dependency you will once again have to run `sbt "eclipse with-source=true"` and refresh your project in Eclipse.


**If you have any questions or problems with any of the materials, don't hesitate to ask!**

**Tip:** Look over the entire homework before starting on it. Then read through each problem carefully, in its entirety, before answering questions and doing the implementation.

Finally: if possible, don't print this homework out! Just read it online, which ensures you'll be looking at the latest version of the homework (in case there are any corrections), you can easily cut-and-paste and follow links, and you won't waste paper.



## Problem 1 - A good day to play tennis? (10 pts)

Let’s start with a simple example problem from Tom Mitchell’s book Machine Learning. The problem is to predict whether it is a good day to play tennis given various factors and some initial data that provides information about whether previous days were good or bad days for tennis. The factors include (in the format "Attribute: List, of, Possible, Values"):

    Outlook: Sunny, Rain, Overcast
    Temperature: Hot, Mild, Cool
    Humidity: High, Normal
    Wind: String, Weak

Table 3.2 on page 59 of Mitchell’s book contains information for fourteen days; this data is encoded into a [training file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/train.txt). There is a separate [test file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/test.txt). As you might expect, you will learn model parameters using the training data and test the resulting model on the examples in the test data.

Each row in the data files corresponds to a single classification instance. For example, here is the training set:

    Outlook=Sunny,Temperature=Hot,Humidity=High,Wind=Weak,No
    Outlook=Sunny,Temperature=Hot,Humidity=High,Wind=Strong,No    
    Outlook=Overcast,Temperature=Hot,Humidity=High,Wind=Weak,Yes
    Outlook=Rain,Temperature=Mild,Humidity=High,Wind=Weak,Yes
    Outlook=Rain,Temperature=Cool,Humidity=Normal,Wind=Weak,Yes
    Outlook=Rain,Temperature=Cool,Humidity=Normal,Wind=Strong,No
    Outlook=Overcast,Temperature=Cool,Humidity=Normal,Wind=Strong,Yes
    Outlook=Sunny,Temperature=Mild,Humidity=High,Wind=Weak,No
    Outlook=Sunny,Temperature=Cool,Humidity=Normal,Wind=Weak,Yes
    Outlook=Rain,Temperature=Mild,Humidity=Normal,Wind=Weak,Yes
    Outlook=Sunny,Temperature=Mild,Humidity=Normal,Wind=Strong,Yes
    Outlook=Overcast,Temperature=Mild,Humidity=High,Wind=Strong,Yes
    Outlook=Overcast,Temperature=Hot,Humidity=Normal,Wind=Weak,Yes
    Outlook=Rain,Temperature=Mild,Humidity=High,Wind=Strong,No

Each instance consists of a list of attribute values, separated by commas, and the last element is the classification value. The value is "Yes" if it is a good day to play tennis based on the conditions, and "No" if it is not.  This is the same format of the data used in [Assignment 0, Part 6](http://utcompling.github.io/nlpclass-fall2013/assignments/a0programming.html#part_6_reading_a_data_file).

What we are interested in for this toy example is to determine whether the probability of playing tennis is higher than the probability of not playing tennis. We can represent the probability of playing tennis as: 

* p(Label=yes | Outlook=o, Temperator=t, Humidity=h, Wind=w)

Note that *Label*, *Outlook*, *Temperature*, *Humidity*, and *Wind* are all random variables, *yes* is a value, and *o*, *t*, *h*, and *w* are variables for values. In order to reduce clutter, we'll write expression without explicit random variables, so the above will be written just as:

* p( **yes** | o, t, h, w)

So, we want to find out whether:

* p( **yes** | o,t,h,w) > p( **no** | o,t,h,w)

Another way of stating this is that for each instance (with values for *o*, *t*, *h*, and *w*), we seek to find the label *x* with maximum probability:

`\[
    \begin{align}
    \hat{x} 
      &= \stackrel{\arg\!\max}{\tiny{x\hspace{-1mm}\in\hspace{-1mm}\text{yes},\text{no}}} p(x \mid o,t,h,w) \\
      &= \stackrel{\arg\!\max}{\tiny{x\hspace{-1mm}\in\hspace{-1mm}\text{yes},\text{no}}} p(x)~p(o \mid x)~p(t \mid x)~p(h \mid x)~p(w \mid x)
    \end{align}
\]`

**Part (a) [4 pts].** Written answer. Show explicitly how the last line above is derived from the first line using Bayes rule, the chain rule, independence assumptions, and from the fact we are finding the argmax.


So, if we have a new instance that we wish to classify, like:

    Outlook=Sunny,Temperature=Cool,Humidity=High,Wind=Strong

what we seek is:

`\[
    \begin{align}
    \hat{x} 
      &= \stackrel{\arg\!\max}{\tiny{x\hspace{-1mm}\in\hspace{-1mm}\text{yes},\text{no}}} p(x \mid \text{sunny}, \text{cool}, \text{high}, \text{strong})\\
      &= \stackrel{\arg\!\max}{\tiny{x\hspace{-1mm}\in\hspace{-1mm}\text{yes},\text{no}}} p(x)~p(\text{sunny} \mid x)~p(\text{cool} \mid x)~p(\text{high} \mid x)~p(\text{strong} \mid x)
    \end{align}
\]`


This simply means we need to compute the two values:

`\[
    \begin{align}    
      & p(\text{yes})~p(\text{sunny} \mid \text{yes})~p(\text{cool} \mid \text{yes})~p(\text{high} \mid \text{yes})~p(\text{strong} \mid \text{yes}) \\
      & p(\text{no})~p(\text{sunny} \mid \text{no})~p(\text{cool} \mid \text{no})~p(\text{high} \mid \text{no})~p(\text{strong} \mid \text{no})
    \end{align}
\]`

And pick the label that produced the higher value. 

Terms like p(yes) and p(sunny|no) are just parameters that we can estimate from a corpus, like the training corpus above. We'll start by doing maximum likelihood estimation, which means that the values assigned to the parameters are those which maximize the probability of the training corpus. We'll return to what this means precisely later in the course; for now, it just means that you do exactly what you'd think: count the number of times (frequency) each possibility happened and divide it by the number of times it could have happened. Here are some examples:

`\[
    p(\text{yes}) 
      = \frac{freq(\text{yes})}{\sum_x freq(D=x)} 
      = \frac{freq(\text{yes})}{freq(\text{yes}) + freq(\text{no})} 
      = \frac{9}{9 + 5} = 0.643
\]`
&nbsp; 
`\[
    \begin{align}
    p(\text{sunny} \mid \text{yes}) 
      &= \frac{freq(\text{yes},\text{sunny})}{\sum_x freq(\text{yes},O=x)} \\
      &= \frac{freq(\text{yes},\text{sunny})}{freq(\text{yes},\text{sunny}) + freq(\text{yes},\text{rain}) + freq(\text{yes},\text{overcast})} \\
      &= \frac{2}{2 + 3 + 4} = 0.222
    \end{align}
\]`
&nbsp; 
`\[
    \begin{align}
    p(\text{sunny} \mid \text{no}) 
      &= \frac{freq(\text{no},\text{sunny})}{\sum_x freq(\text{no},O=x)} \\
      &= \frac{freq(\text{no},\text{sunny})}{freq(\text{no},\text{sunny}) + freq(\text{no},\text{rain}) + freq(\text{no},\text{overcast})} \\
      &= \frac{2}{3 + 2 + 0} = 0.6
    \end{align}
\]`

Easy! 

*Note:* you might have noticed that *freq*(**yes**, **sunny**) + *freq*(**yes**, **rain**) + *freq*(**yes**, **overcast**) = *freq*(**yes**). This is true for this example because each attribute only occurs exactly once per instance. Later on, with sentiment analysis, we'll need the extra flexibility of being able to see the same attribute multiple times per instance, such as multiple words.

The data includes a test set for the tennis task as well, provided in full here:

    Outlook=Sunny,Temperature=Cool,Humidity=High,Wind=Strong,No
    Outlook=Overcast,Temperature=Cool,Humidity=Normal,Wind=Weak,Yes
    Outlook=Sunny,Temperature=Hot,Humidity=Normal,Wind=Weak,Yes
    Outlook=Rain,Temperature=Hot,Humidity=High,Wind=Strong,No
    Outlook=Sunny,Temperature=Cool,Humidity=Normal,Wind=Weak,Yes
    Outlook=Overcast,Temperature=Hot,Humidity=High,Wind=Strong,No
    Outlook=Sunny,Temperature=Mild,Humidity=High,Wind=Weak,Yes
    Outlook=Overcast,Temperature=Mild,Humidity=Normal,Wind=Strong,Yes
    Outlook=Rain,Temperature=Cool,Humidity=Normal,Wind=Strong,No
    Outlook=Overcast,Temperature=Cool,Humidity=Normal,Wind=Strong,Yes
    Outlook=Rain,Temperature=Hot,Humidity=Normal,Wind=Weak,Yes
    Outlook=Sunny,Temperature=Cool,Humidity=High,Wind=Weak,Yes
    Outlook=Rain,Temperature=Hot,Humidity=Normal,Wind=Strong,No

Like the training set, this provides a list of instances, and for each instance, the values for each of the attributes and the classification outcome.

**Part (b) [2 pts].** Written answer. Using the training set to determine the relevant parameters, what is the most probable label for:

    Outlook=Sunny,Temperature=Hot,Humidity=Normal,Wind=Weak

Make sure to show your work, including the values you obtained for each label. Does it match the label given for the third instance in the test set above?

**Part (c) [3 pts].** Written answer. Derive the general formula for calculating p(x|o,t,h,w) and calculate p(yes|overcast,cool,normal,weak) based on parameters estimated from the training set.

**Part (d) [1 pt].** Written answer. Provide a set of attribute values o, t, h, and w for which the probability of either yes or no is zero.



## Problem 2 - Implement basic naive Bayes (25 pts)

This problem will walk you through the implementation and training of a naive Bayes model.


### NaiveBayesModel

Implement a class 

    nlp.a2.NaiveBayesModel[Label, Feature, Value]

that extends

    nlpclass.NaiveBayesModelToImplement[Label, Feature, Value]

The `NaiveBayesModel` class will contain your naive Bayes implementation.  It requires a method:

    def predict(features: Vector[(Feature, Value)]): Label

The `predict` method takes in a Vector of (feature,value) pairs and outputs the most likely label given the features.

The structure in which you store the underlying data within the `NaiveBayesModel` class is ultimately up to you, but I highly encourage you to make use of the `ProbabilityDistribution` and `ConditionalProbabilityDistribution` classes that you wrote for [Assignment 1, Problem 5](http://utcompling.github.io/nlpclass-fall2013/assignments/a1prob.html#problem_5_30_points).  In fact, you'll probably want something like this, which should look familiar:

{% highlight scala %}
class NaiveBayesModel[Label, Feature, Value](
  labels: Set[Label],
  pLabel: ProbabilityDistribution[Label],
  pValue: Map[Feature, ConditionalProbabilityDistribution[Label, Value]])
  extends NaiveBayesModelToImplement[Label, Feature, Value]
{% endhighlight %}

When trained on the tennis [training file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/train.txt), you should get the following behavior:

{% highlight scala %}
scala> val nbm = new NaiveBayesModel[String,String,String](...)
scala> nbm.predict(Vector("Outlook"->"Sunny", "Temperature"->"Cool", "Humidity"->"High", "Wind"->"Strong"))
res0: String = No
scala> nbm.predict(Vector("Outlook"->"Overcast", "Temperature"->"Cool", "Humidity"->"Normal", "Wind"->"Weak"))
res1: String = Yes
{% endhighlight %}


### UnsmoothedNaiveBayesModelTrainer

In order to maintain good modularity in your code, you will not actually calculate the probability distributions from within the `NaiveBayesModel` class; you will only *use* the distributions to predict a label.

Instead, you will use a distinct *trainer* class to turn the raw data into a `NaiveBayesModel`, calculating the probability distributions along the way.

So you will implement a class 

    nlp.a2.UnsmoothedNaiveBayesTrainer[Label, Feature, Value]

that extends

    nlpclass.NaiveBayesTrainerToImplement[Label, Feature, Value]

The `UnsmoothedNaiveBayesTrainer` class requires a method:

    def train(instances: Vector[(Label, Vector[(Feature, Value)])]): NaiveBayesModelToImplement[Label, Feature, Value]

The `train` method takes a Vector of labeled instances, uses those instances to calculate probability distributions (without smoothing, of course), and then instantiates a `NaiveBayesModel` to be returned.  

When trained on the tennis [training file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/train.txt), you should get the following behavior:

{% highlight scala %}
scala> val instances = ... from tennis training file ...
scala> val nbt = new UnsmoothedNaiveBayesTrainer(...)
scala> val nbm = nbt.train(instances)
scala> nbm.predict(Vector("Outlook"->"Sunny", "Temperature"->"Cool", "Humidity"->"High", "Wind"->"Strong"))
res0: String = No
scala> nbm.predict(Vector("Outlook"->"Overcast", "Temperature"->"Cool", "Humidity"->"Normal", "Wind"->"Weak"))
res1: String = Yes
{% endhighlight %}


*Note:*  This separation of concerns is nice because it means that the `NaiveBayesModel` needs only to be concerned with using the parameters to predict labels; it does not care where those parameters come from, meaning that it can be reused under all sorts of parameter-estimating scenarios.  Thus, the trainer's only job is to estimate the parameters from raw data and produce a model.  This means that we can have many kinds of trainers that all have the same interface (train from data to make a model), but vary in their parameter estimation techniques.



### NaiveBayesScorer

In order to evaluate your model, you will need to implement an object that runs labeled test instances through your classifier and checks the results.  

So you will implement an object:

    nlp.a2.NaiveBayesScorer

that extends

    nlpclass.NaiveBayesScorerToImplement

The `NaiveBayesScorer` class requires a method:

    def score[Label, Feature, Value](
        naiveBayesModel: NaiveBayesModelToImplement[Label, Feature, Value],
        testInstances: Vector[(Label, Vector[(Feature, Value)])],
        positveLabel: Label)

The `score` method takes a three arguments.  

1. `naiveBayesModel`: A trained `NaiveBayesModel` instance
2. `testInstances`: A Vector of test instances and their *correct* labels
3. `positveLabel`: The label to be treated as "positive" for precision and recall calculations

Notice that the `score` method does not return anything.  Instead, it should simply print out the information:

* Accuracy
* Precision (based on the `positiveLabel`)
* Recall (based on the `positiveLabel`)
* F1 (based on the `positiveLabel`)

When trained on the tennis [training file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/train.txt) and tested on the tennis [testing file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/test.txt), you should get the following behavior:

{% highlight scala %}
scala> val trainInstances = ... from tennis training file ...
scala> val nbt = new UnsmoothedNaiveBayesTrainer(...)
scala> val nbm = nbt.train(trainInstances)
scala> val testInstances = ... from tennis test file ...
scala> NaiveBayesScorer.score(nbm, testInstances, "Yes")
accuracy = 61.54
precision (Yes) = 66.67
recall (Yes) = 75.00
f1 = 70.59
scala> NaiveBayesScorer.score(nbm, testInstances, "No")
accuracy = 61.54
precision (No) = 50.00
recall (No) = 40.00
f1 = 44.44
{% endhighlight %}




### Naive Bayes from the command-line

In order for us to test your code, you will need to create an object `nlp.a2.NaiveBayes` so that we can train and test a model from the command line.

    $ sbt "run-main nlp.a2.NaiveBayes --train tennis/train.txt --test tennis/test.txt --poslab Yes"
    accuracy = 61.54
    precision (Yes) = 66.67
    recall (Yes) = 75.00
    f1 = 70.59
    $ sbt "run-main nlp.a2.NaiveBayes --train tennis/train.txt --test tennis/test.txt --poslab No"
    accuracy = 61.54
    precision (No) = 50.00
    recall (No) = 40.00
    f1 = 44.44


### Logging

A logging framework is one that lets you print information to the screen (or to a file) from your program in a clean way.  Whereas 

You will have to take a few steps to make this work.

First, you will have to extend the trait `com.typesafe.scalalogging.log4j.Logging` from any class or object that you want to be able to log.  If your class or object is already extending something, then you should used the `with` keyword to indicate a second trait to extend:

{% highlight scala %}
import com.typesafe.scalalogging.log4j.Logging

class NaiveBayesModel[Label, Feature, Value](...)
  extends NaiveBayesModelToImplement[Label, Feature, Value]
  with Logging
{% endhighlight %}

Then you will have to add logging statements to print out relevant information.  There are 6 levels of logging statements: trace, debug, info, warn, error, and fatal.  Trace is the lowest, fatal is the highest.  You can log statements using the syntax `logger.debug`, `logger.info`, etc.  (`logger` is a field on the `Logging` trait, which is why it exists even though you aren't explicitly declaring it.)

{% highlight scala %}
  override def predict(features: Vector[(Feature, Value)]): Label = {
    [...]
    logger.debug("something at the debug level")
    [...]
    logger.info("something at the info level")
    [...]
  }
{% endhighlight %}

When `predict` is run, it will print two statement at different logging levels.  

To see the logging statements when you run your program, you should pass a VM argument indicating the log level that you want to show:

    sbt -Dorg.apache.logging.log4j.level=INFO "run-main nlp.a2.NaiveBayes ..."

The way the logging framework works, you will get all the logging statements at the level you specify and above.  So, if you specify INFO, you will get info, warn, error, and fatal statements.  If you specify DEBUG, you will get debug, info, warn, error, and fatal statements.

For this assignment, you should add log statements to the `predict` method of `NaiveBayesModel` so that, for each test instance, it logs posterior probabilities for each label, given the features.







sbt -Dorg.apache.logging.log4j.level=INFO "run-main nlp.a2.NaiveBayes --train ppa/train.txt --test ppa/dev.txt --poslab N"





TODO !!!!

outputs the probabilities for each label in reverse sorted order, using the format:

    Label_1 Probability_1 Label_2 Probability_2 ... Label_n Probability_n

For example, here's what the output should look like:

    No 0.795417348609 Yes 0.204582651391
    Yes 1.0 No 0.0
    Yes 0.672947510094 No 0.327052489906
    No 0.8382923674 Yes 0.1617076326
    ...






## Problem 3 - Prepositional Phrase Attachment and smoothing (25 pts)

Prepositional phrase attachment is the well-known task of resolving a common ambiguity in English syntax regarding whether a prepositional phrase attaches to the verb or the noun in sentences with the pattern 

> Verb Noun_Phrase Prepositional_Phrase 

An example is I saw the man with the telescope. If the prepositional phrase attaches to the verb, then the seeing was done with the telescope; if it attaches to the noun, it indicates that the man had the telescope in his possession. A clear difference can be seen with the following related examples:

* Attach to the noun: He ate the spaghetti with meatballs.
* Attach to the verb: He ate the spaghetti with chopsticks.

We can deal with this decision just like any simple labeling problem: each sentence receives a label *V* or *N* indicating the attachment decision, and there is no benefit to be gained from using previous attachment decisions.

For this problem, you will use a conveniently formatted [data set](https://github.com/utcompling/nlpclass-fall2013/tree/master/data/classify/ppa) for prepositional phrase attachment which has been made available by Adwait Ratnaparkhi. There are three files which you will use for this problem: `train.txt`, `dev.txt`, and `test.txt`. Look at the contents of training:

    verb=join,noun=board,prep=as,prep_obj=director,V
    verb=is,noun=chairman,prep=of,prep_obj=N.V.,N
    verb=named,noun=director,prep=of,prep_obj=conglomerate,N
    verb=caused,noun=percentage,prep=of,prep_obj=deaths,N
    verb=using,noun=crocidolite,prep=in,prep_obj=filters,V
    ...

Each row lists an abbreviated form of a prepositional phrase attachment. The four words correspond to the head verb, head noun, preposition, and head noun object of the preposition, in that order. The final element indicates whether the attachment was to the verb (V) or to the noun (N).

For this exercise, you will train a classifier that learns a model from the data in training and use it to classify new instances. You will develop your model using the material in `dev.txt`. You must not personally inspect the contents of the test data — you will run your classifier on `test.txt` only once, when you are done developing.

The first thing you should do is train your unsmoothed naive Bayes classifier on the ppa data:

    $ sbt "run-main nlp.a2.NaiveBayes --train ppa/train.txt --test ppa/dev.txt --poslab N"
    Accuracy: 67.39

Ratnaparkhi et al (1994) obtain accuracies of around 80%, so we clearly should be able to do much better.  One obvious problem shows up if you look at the actual output:

    $ ./tennis_cat.py -t out/ppa.basic.train -p out/ppa.basic.dev | more
    V 0.832766613303 N 0.167233386697
    V 1.0 N 0.0
    V 1.0 N 0.0
    V 1.0 N 0.0
    V 1.0 N 0.0
    N 0.5 V 0.5
    V 1.0 N 0.0
    V 1.0 N 0.0
    N 0.5 V 0.5
    V 1.0 N 0.0
    V 0.999803237929 N 0.000196762070993
    V 0.999826549526 N 0.000173450474426
    V 0.999763228 N 0.000236771999789
    V 1.0 N 0.0
    V 1.0 N 0.0

There are many items that have zero probability for N, V, or both (those are the ones with uniform .5 probability for both labels). The problem is that we haven't done any smoothing, so there are many parameters that we assign zero to, and then the overall probability for the class becomes zero. For example, the tenth     line in `out/ppa.basic.dev` is:

    verb=was,noun=performer,prep=among,prep_obj=groups,N

The output gives zero probability to N because the only training instance that has noun=performer is with the V label:

    verb=juxtapose,noun=performer,prep=with,prep_obj=tracks,V

Thus, the value of p(Noun=performer | Label=V) is zero, making p(Label=V | Verb=juxtapose, Noun=performer, Prep=with, PrepObj=tracks) also zero, regardless of how much the rest of the information looks like a V attachment.

We can fix this by using add-λ smoothing. For example, we can smooth the prior probabilities of the labels as follows:



Here, L is the set of labels, like {V, N} or {yes, no}, and |L| is the size of that set. Quite simply, we've added an extra λ count to both labels, so we've added λ|L| hallucinated counts. We ensure it still is a probability distribution by adding λ|L| to the denominator.

**Part (a) [5 pts].** Written answer. Provide the general formula for a similar smoothed estimate for p(Attribute=x|Label=y) in terms of the relevant frequencies of x and y and the set ValuesAttribute consisting of the values associated with the attribute. (For example, ValuesOutlook from the tennis example is {sunny,rainy,overcast}.) If it helps, first write it down as the estimate for a specific parameter, like p(Outlook=sunny|Label=yes), and then do the more general formula.

The values associated with each attribute in the tennis dataset are small, fixed sets. However, for the ppa data, the values are words, so we are not likely to observe every value in the training set. That means that we need to "save" some probability for the unknown value for every distribution of the form p(Attribute=x|Label=y). To do this, we just need to add unknown to the set of values. In an implementation, this is done implicitly by setting the size of the value set to be one more than the number of elements in the set. You should make sure to do this in your implementation. However, you should not do this when smoothing p(Label=x) because we assume the set of labels to be fixed.

**Part (b) [20 pts].** Implementation. Copy tennis_cat.py to ppa_cat.py, then modify ppa_cat.py to use add-λ smoothed estimates of the parameters. You have less explicit code guidance on this, so you need to create and populate the appropriate data structures for storing the set of possible values associated with each attribute. 

Note: the --lambda option has been set up for specifying the lambda value on the command line. The value is accessible as options.lambda_value. It has a default of 0, so if you do not specify a value for --lambda, the program should behave the same way that tennis_cat.py does (i.e, it should obtain the same accuracy).

Use λ=1 while implementing and debugging this. You should obtain an accuracy of around 80% on the devset when you do the following:

    $ ./ppa_cat.py -t out/ppa.basic.train -p out/ppa.basic.dev -l 1 | ./score.py -g out/ppa.basic.dev 

Find a good λ for improving the accuracy on the development set. By exploring other values for λ, you may be able to improve the results. 

Written answer. Report the best λ you found and what accuracy you obtained.

Tip. You can easily check many possible values for λ using the Unix command line. For example,

    $ for i in {0..10}; do echo $i; ./ppa_cat.py -t out/ppa.basic.train -p out/ppa.basic.dev -l $i | ./score.py -g out/ppa.basic.dev; done

You should see multiple accuracy values go by, each one associated with the λ value printed above it. (If this doesn't work, then you might not be using the bash shell.) You can also specify ranges such as for i in .5 1 1.5 2 2.5 for drilling down further.

Coding check. If you have implemented things correctly, you should get the following output for the tennis dataset:

    $ for i in {0..4}; do echo $i; ./ppa_cat.py -t data/tennis/train -p data/tennis/test -l $i | ./score.py -g data/tennis/test; done
    0
    Accuracy: 61.54
    1
    Accuracy: 61.54
    2
    Accuracy: 69.23
    3
    Accuracy: 76.92
    4
    Accuracy: 76.92


## Problem 4 - Computing with logarithms (15 pts)

When you calculated the values to determine the most probable label for problems 1 and 2, you (probably) followed the equation directly and used multiplication to combine the various probabilities. Doing so works fine on small examples like those in problems 1 and 2, but ior problems 4 and 5 you will be using a much wider set of attributes with even more values than those used so far. This means that you will be combining a much larger group of much smaller probabilities, so you might easily end up exceeding the floating point precision when many more probabilties are to be combined. A straightforward way of getting around this is to convert the probabilities to logarithms and use addition of log probabilities instead of multiplication of probabilities.

First, here's a reminder of the basic property of interest:



Try it out in Python:

    >>> import math
    >>> 6 * 7
    42
    >>> math.exp(math.log(6) + math.log(7))
    41.999999999999986

More generally:



Thus, when determining the most probable label, we can do the following:



**Part (a) [3 pts].** Written answer. Provide the formula for calculating p(yes|overcast,cool,normal,weak) when using log values of the parameters, such as logp(yes)  and logp(no|yes). Note: you need to determine the probability, not the argmax. This is simple, but writing this out explicitly will help you for part (b).

**Part (b) [12 pts].** Implementation. Copy ppa_cat.py to log_cat.py, then modify log_cat.py so that the calculations are done using logarithms. Make sure that your modified version produces the same results on the tennis data and the ppa data as the original did, including when λ≠0.  Keep in mind that:
the parameters, including those for unseen items, must be log values
the computation of the score should use addition rather than multiplication
you must exponentiate the log scores for each label before you normalize to get the probabilities
Note. You can do logs in various bases; which base you use doesn’t matter, as long as you use it consistently. The easiest thing to do would be to use math.log(number), which gives you base e. You can get other bases by providing an extra argument. For example, math.log(8,2) returns log28, the value 3.0.
Note. Since working in log space involves addition and since the log of zero is undefined, unseen events don’t directly produce a probability of zero. You can nonetheless simulate this by having unseen features contribute a large negative amount, such as -50, to the calculation. 


## Problem 5 - Extending the feature set (20 pts)

The simple set of features used for the ppa data above can definitely be improved upon. For example, you could
have features that:
are combinations of the head noun and verb, e.g. verb+noun=join+board
are a stemmed version of the verb, e.g. verb_stem=caus from cause, causes, caused, and causing
identify all numbers, e.g. noun_form=number for 42, 1.5, 100,000, etc.
identify capitalized forms, e.g. noun_form=Xx for Texas and noun_form=XX for USA.
use word clusters derived from a large corpus (see the discussion of bitstrings in Ratnaparkhi et al (1994))
Implementation. Create extended ppa features by modifying ppa_features.py, following instructions in the code. Look at the suggestions above, at some examples of extended features already in ppa_features.py, and also look at the training file to see if you can think of other features. You should come up with at least five new features, but are encouraged to do much more. Be creative! Look at the directions in the ppa_features.py so that you can define features that will be output when the --extended_features option is given to ppa_features.py.

Written answer. Describe five of the features you added, and why you think they would be useful.

Tip. Some resources have been imported for you:
The Porter stemmer is available via the stemmer object -- to get the stem of a word, you can call, for example, stemmer.stem_token("walked")
The bitstrings supplied with the data set capture word clusters. They can be imported by using the --bitstrings option, which gives you a dictionary from words to BitVector objects. There is an example in the code of how you can use them. You might find the BitVector documentation useful.
As you enable new features, you can try them out by generating new feature files and running the classifier:

    $ ./ppa_features.py -i data/ppa/training -e -b data/ppa/bitstrings > out/ppa.extended.train
    $ ./ppa_features.py -i data/ppa/devset -e -b data/ppa/bitstrings > out/ppa.extended.dev
    $ ./log_cat.py -t out/ppa.extended.train -p out/ppa.extended.dev -l 1 | ./score.py -g out/ppa.extended.dev

Notice that the above commands use the shortened versions of the options.

As before with the basic features, find an optimal λ on the dev set with the extended features. (Note that this may change as you add more features.)  When you are satisfied that you cannot improve the performance any further, you can finally try out the test set! (Once you've done this, there is no going back to change the features any more.) 

Written answer. What is the performance you obtain when using the basic features and the extended features, each with their best λ?

Additional notes based on office hours and questions from students


### Note 1

Keep in mind that it is possible to have the same attribute occur more than once in a given instance, so your code should handle this. What this means is that to get p(a=v|l), you do not divide by the frequency of l, but by the overall frequency of a and l occurring together. This will give you the same value for the tennis data and the ppa data *before* you add your own features. 

In general, we can have lots instances of the same attribute with text classification -- for example, with twitter sentiment analysis, you will have tweets like:

Love the angelic music behind Luke Russert #HCR reporting.

With a positive sentiment. This will turn into a set of attribute-values + label like:

word=love,word=angelic,word=music,word=behind,word=luke,word=russert,hashtag=hcr,word=reporting,positive

So, your code should be set up to handle that!


### Note 2

Values can be shared across multiple random variables, which means that you can't just use a tuple containing the value and the label -- you have to make sure to include the attribute. For example, if you have ppa instances like:

verb=join,noun=board,prep=as,prep_obj=director,V
verb=saw,noun=director,prep=of,prep_obj=company,N

You do not want the prep_obj=director and noun=director to be mixed in the same counts!


### Note 3

For smoothing the p(attr=val | label) distributions, you should not use the length of the al_freq map (the keys of that map) in the denominator. Remember that you are smoothing so that you can get a non-zero value for *every* setting of attributes with the values for *every* label. So, what you want is: for each attribute, collect the set of *all* values that have occurred with it (*regardless* of the label), plus one for the "unknown" value.