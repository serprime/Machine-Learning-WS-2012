#!/usr/bin/Rscript
require("RWeka")
require("rJava")
.jaddClassPath("../BayesianNetwork/target/machine_learning-0.0.1-SNAPSHOT.jar")

classifier <- make_Weka_classifier("at/ac/tuwien/machine_learning/SillyClassifier")
classifier 
data(iris)
model = classifier(Species ~., data=iris)
model

result = table(iris$Species, predict(model))
result

