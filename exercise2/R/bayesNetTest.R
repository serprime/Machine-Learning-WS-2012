#!/usr/bin/Rscript
require("RWeka")
require("rJava")

.jaddClassPath("../BayesianNetwork/target/machine_learning-0.0.1-SNAPSHOT.jar")

bayesNet <- make_Weka_classifier("weka/classifiers/bayes/BayesNet")

res = bayesNet(Species~., data=iris, control = Weka_control(D="",  Q = "at.ac.tuwien.machine_learning.UltraSearcher", "--", P=1, S="BAYES", E="weka.classifiers.bayes.net.estimate.SimpleEstimator", "--", A=0.5))
predict(res)



result = table(iris$Species, predict(res))
result


