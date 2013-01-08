#!/usr/bin/Rscript
require("RWeka")
require("rJava")

.jaddClassPath("../BayesianNetwork/target/machine_learning-0.0.1-SNAPSHOT.jar")

bayesNet <- make_Weka_classifier("weka/classifiers/bayes/BayesNet")

adult =  read.arff("adult_preprocessed.arff")

hcAdultModel = bayesNet(`income`~., data=adult, control = Weka_control(D="",  Q = "weka.classifiers.bayes.net.search.global.HillClimber", "--", P=1, S="BAYES", E="weka.classifiers.bayes.net.estimate.SimpleEstimator", "--", A=0.5))
extendedAdultModel = bayesNet(`income`~., data=adult, control = Weka_control(D="",  Q = "weka.classifiers.bayes.net.search.global.IteratedLocalSearch", "--", T=5, P=1, S="BAYES", E="weka.classifiers.bayes.net.estimate.SimpleEstimator", "--", A=0.5))

"ADULT DATA SET"
hcAdultEvaluation = evaluate_Weka_classifier(hcAdultModel)
"Original Hill Climber classifier"
hcAdultEvaluation$details
extendedAdultEvaluation = evaluate_Weka_classifier(extendedAdultModel)
"New iterated local search classifier"
extendedAdultEvaluation$details
print("Difference of details")
extendedAdultEvaluation$details - hcAdultEvaluation$details 
print("Difference of confusion matrices")
extendedAdultEvaluation$confusionMatrix- hcAdultEvaluation$confusionMatrix


