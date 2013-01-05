#!/usr/bin/Rscript
require("RWeka")
require("rJava")

.jaddClassPath("../BayesianNetwork/target/machine_learning-0.0.1-SNAPSHOT.jar")

bayesNet <- make_Weka_classifier("weka/classifiers/bayes/BayesNet")

model = bayesNet(Species~., data=iris, control = Weka_control(D="",  Q = "weka.classifiers.bayes.net.search.global.IteratedLocalSearch", "--", P=1, S="BAYES", E="weka.classifiers.bayes.net.estimate.SimpleEstimator", "--", A=0.5))
predict(model)



result = table(iris$Species, predict(model))

evaluate_Weka_classifier(model)
