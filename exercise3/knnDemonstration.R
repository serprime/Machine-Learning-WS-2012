#!/usr/bin/Rscript

# Weka bridge
library(RWeka)

# plotting library
library(rgl)

# library to wait for user input
library(tcltk)

Nth.delete<-function(dataframe, n)dataframe[(seq(n,to=nrow(dataframe),by=n)),]

# splitdf function will return a list of training and testing sets
splitdf <- function(dataframe, seed=NULL) {
    if (!is.null(seed)) set.seed(seed)
    index <- 1:nrow(dataframe)
    trainindex <- sample(index, trunc(length(index)/2))
    trainset <- dataframe[trainindex, ]
    testset <- dataframe[-trainindex, ]
    list(trainset=trainset,testset=testset)
}

skin_arff <- read.arff("../exercise1/samples/skin\ color/Skin_NonSkin.arff")
# We choose every 1000 row as an instance for our sample set
sample <- Nth.delete(skin_arff,100)
splits <- splitdf(sample, seed=12345)
training <- splits$trainset
testing <- splits$testset

classifier <-IBk(Y~., training, control = Weka_control(K = 20))
predictedData <- testing
predictedData$P <- predict(classifier, testing)
predictedData$Color <- (predictedData$Y == predictedData$P)
str(predictedData)
plot3d(predictedData$R, predictedData$G, predictedData$B, col=predictedData$Color, size=2, type='s')
tk_messageBox(message = "Press a key to exit")

#evaluation <- evaluate_Weka_classifier(classifier)
#evaluation$confusionMatrix



