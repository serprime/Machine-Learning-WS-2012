#!/usr/bin/Rscript

# library for option parsing
library("optparse")

option_list <- list(
                    make_option(c("-t", "--train"), help="load training data", metavar="ARFF_FILE"),
                    make_option(c("-T", "--test"), help="load test data, if ommitted, training data is splitted into parts", metavar="ARFF_FILE"),
                    make_option(c("-k", "--kneighbors"), type="integer", help="k parameter for nearest neighbor classifier", default=as.integer(5), metavar="K"),
                    make_option(c("--extract"), type="integer", help="reduce input data set by extracting every nth instance", metavar="N")
                    )
parser <- OptionParser(usage="knnDemonstration [option] trainingfile", option_list=option_list)
arguments <- parse_args(parser, positional_arguments = TRUE)
opt <- arguments$options

# handle training data
if(is.null(opt$train)){
    cat("Please provide some training data.. \n")
    stop()
}
if(file.access(opt$train) == -1){
    stop(sprintf("Specified file ( %s ) does not exist", opt$input))
}

# handle test data
if( !is.null(opt$test) && (file.access(opt$test) == -1)){
    stop(sprintf("Specified file ( %s ) does not exist", opt$test))
}

# Weka bridge
library(RWeka)

# plotting library
library(rgl)

# library for the text box
library(rpanel)

# helper function to extract a fraction amount of the training data
Nth.rows<-function(dataframe, n)dataframe[(seq(n,to=nrow(dataframe),by=n)),]


inputArff <- read.arff(opt$train)
# If the user specified --extract, we choose every nth row as an instance for our sample set
if (!(is.null(opt$extract))){
    sample <-Nth.rows(inputArff, opt$extract)
} else {
    sample <- inputArff
}

# if the user didn't specify a test data set, the training data is splitted
if (is.null(opt$test)){
    splits <- split(sample, c("trainset", "testset"))
    training <- splits$trainset
    testing <- splits$testset
} else {
    training <- sample
    testing <- read.arff(opt$test)
}

if( ncol(training) < 3 || 4 < ncol(training)){
    stop(sprintf("The provided training has got %s columns, whith the last column considered as class attribute, but we can only plot 2dimensional or 3dimensional data!",  ncol(training)))
}

if( ncol(testing) != ncol(training)){
    stop(sprintf("The number of columns for the provided data sets is not the same: %s columns for trainingdata and %s columns for testdata.",  ncol(training), ncol(testing)))
}


interface.draw <- function(panel) {
    classifier <-IBk(Y~., training, control = Weka_control(K = as.numeric(panel$k)))
    predictedData <- testing
    predictedData$P <- predict(classifier, testing)
    predictedData$Color <- (predictedData$Y == predictedData$P)
    # TODO: maybe output some evaluation data? e.g. percentage of misclassified instances
    plot3d(predictedData$R, predictedData$G, predictedData$B, xlab="B", ylab="G", zlab="R", col=predictedData$Color, size=1, type='s')
    panel
}
interface.panel <- rp.control("K nearest neighbor", k=opt$k)
rp.textentry(panel=interface.panel, var=k, action=interface.draw, names="K nearest neighbor", labels="K=")
# run it!
rp.do(interface.panel, interface.draw)
# user must close the panel to exit the program
rp.block(interface.panel)
