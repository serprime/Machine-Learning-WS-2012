#!/usr/bin/Rscript

# Weka bridge
library(RWeka)

# plotting library
library(rgl)

# library to wait for user input
library(tcltk)

Nth.rows<-function(dataframe, n)dataframe[(seq(n,to=nrow(dataframe),by=n)),]


skin_arff <- read.arff("../exercise1/samples/skin\ color/Skin_NonSkin.arff")
# We choose every 1000 row as an instance for our sample set
sample <-Nth.rows(skin_arff,100)
splits <- split(sample, c("trainset", "testset"))

training <- splits$trainset
testing <- splits$testset
nrow(training)
nrow(testing)

classifier <-IBk(Y~., training, control = Weka_control(K = 20))
predictedData <- testing
predictedData$P <- predict(classifier, testing)
predictedData$Color <- (predictedData$Y == predictedData$P)
str(predictedData)
plot3d(predictedData$R, predictedData$G, predictedData$B, col=predictedData$Color, size=2, type='s')
tk_messageBox(message = "Press a key to exit")

#evaluation <- evaluate_Weka_classifier(classifier)
#evaluation$confusionMatrix



