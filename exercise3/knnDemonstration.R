#!/usr/bin/Rscript

# library for option parsing
library("optparse")

option_list <- list(
                    make_option(c("-t", "--train"), help="load training data", metavar="ARFF_FILE"),
                    make_option(c("-T", "--test"), help="NOT IMPLEMENTED YET", metavar="ARFF_FILE"),
                    make_option(c("-k", "--kneighbors"), type="integer", help="k parameter for nearest neighbor classifier", default=as.integer(5), metavar="K"),
                    make_option(c("--extract"), type="integer", help="reduce input data set by extracting every nth instance", metavar="N")
                    )
parser <- OptionParser(usage="knnDemonstration [option] trainingfile", option_list=option_list)
arguments <- parse_args(parser, positional_arguments = TRUE)
opt <- arguments$options

if(is.null(opt$train)){
    cat("Please provide some training data.. \n")
    stop()
}
if(file.access(opt$train) == -1){
    stop(sprintf("Specified file ( $s ) does not exist", opt$input))
}


# Weka bridge
library(RWeka)

# plotting library
library(rgl)

# library to wait for user input
library(tcltk)


Nth.rows<-function(dataframe, n)dataframe[(seq(n,to=nrow(dataframe),by=n)),]


skin_arff <- read.arff(opt$train)
# If the user specified --extract, we choose every nth row as an instance for our sample set
if (!(is.null(opt$extract))){
    sample <-Nth.rows(skin_arff,opt$extract)
} else {
    sample <- skin_arff
}
splits <- split(sample, c("trainset", "testset"))

training <- splits$trainset
# TODO: provide logic for test data via command line
testing <- splits$testset

classifier <-IBk(Y~., training, control = Weka_control(K = opt$k))
predictedData <- testing
predictedData$P <- predict(classifier, testing)
predictedData$Color <- (predictedData$Y == predictedData$P)
plot3d(predictedData$R, predictedData$G, predictedData$B, col=predictedData$Color, size=2, type='s')
tk_messageBox(message = "Press a key to exit")

#evaluation <- evaluate_Weka_classifier(classifier)
#evaluation$confusionMatrix



