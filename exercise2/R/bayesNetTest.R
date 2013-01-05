#!/usr/bin/Rscript
require("RWeka")
require("rJava")

.jaddClassPath("../BayesianNetwork/target/machine_learning-0.0.1-SNAPSHOT.jar")

bayesNet <- make_Weka_classifier("weka/classifiers/bayes/BayesNet")


hcModel = bayesNet(Species~., data=iris, control = Weka_control(D="",  Q = "weka.classifiers.bayes.net.search.global.HillClimber", "--", P=1, S="BAYES", E="weka.classifiers.bayes.net.estimate.SimpleEstimator", "--", A=0.5))
extendedModel = bayesNet(Species~., data=iris, control = Weka_control(D="",  Q = "weka.classifiers.bayes.net.search.global.IteratedLocalSearch", "--", P=1, S="BAYES", E="weka.classifiers.bayes.net.estimate.SimpleEstimator", "--", A=0.5))



hcEvaluation = evaluate_Weka_classifier(hcModel)
"Original Hill Climber Classifier"
hcEvaluation$details



extendedEvaluation = evaluate_Weka_classifier(extendedModel)
"New Iteradet Local Search Classifier"
extendedEvaluation$details

print("Difference of details")
extendedEvaluation$details - hcEvaluation$details 
print("Difference of confusion matrices")
extendedEvaluation$confusionMatrix- hcEvaluation$confusionMatrix
