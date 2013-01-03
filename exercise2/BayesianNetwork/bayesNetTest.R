#!/usr/bin/Rscript
require("RWeka")

bayesNet <- make_Weka_classifier("weka/classifiers/bayes/BayesNet")

res = bayesNet(Species~., data=iris, control = Weka_control(D="",  Q = "weka.classifiers.bayes.net.search.local.HillClimber", "--", P=1, S="BAYES", E="weka.classifiers.bayes.net.estimate.SimpleEstimator", "--", A=0.5))
predict(res)



