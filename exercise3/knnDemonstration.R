#!/usr/bin/Rscript

# library for option parsing
library("optparse")

option_list <- list(
                    make_option(c("-t", "--train"), help="load training data", metavar="ARFF_FILE"),
                    make_option(c("-T", "--test"), help="load test data, if ommitted, training data is splitted into parts", metavar="ARFF_FILE"),
                    make_option(c("-k", "--kneighbors"), type="integer", help="k parameter for nearest neighbor classifier", default=as.integer(5), metavar="K"),
                    make_option(c("-c", "--color"), type="character", default="Prediction", help="color the points based on the predicted value or based on the prediction correctness", metavar="Prediction|Correctness"),
                    make_option(c("-e","--extract"), type="integer", help="reduce input data set by extracting every nth instance", metavar="N")
                    )
parser <- OptionParser(usage="knnDemonstration [option] trainingfile", option_list=option_list)
arguments <- parse_args(parser, positional_arguments = TRUE)
opt <- arguments$options

# handle training data
if(is.null(opt$train)){
    stop("Please provide some training data.. ")
}
if(file.access(opt$train) == -1){
    stop(sprintf("Specified file ( %s ) does not exist", opt$train))
}

# handle test data
if( !is.null(opt$test) && (file.access(opt$test) == -1)){
    stop(sprintf("Specified file ( %s ) does not exist", opt$test))
}

# only values 'Prediction' or 'Correctness' as color values are allowed
if (!opt$color %in% c("Prediction","Correctness")){
    stop(sprintf("Expected 'Prediction' or 'Correctness' as 'color' parameter, but given %s", opt$color ))
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

if ( ncol(training) == 3 ){
    modus = "2D"
} else if ( ncol(training) == 4 ){
    modus = "3D"
} else {
    stop(sprintf("The provided training has got %s columns, whith the last column considered as class attribute, but we can only plot 2dimensional or 3dimensional data!",  ncol(training)))
}

if( ncol(testing) != ncol(training)){
    stop(sprintf("The number of columns for the provided data sets is not the same: %s columns for trainingdata and %s columns for testdata.",  ncol(training), ncol(testing)))
}

if (!all(names(testing) == names(training))){
    stop(sprintf("Please ensure that both training set and test data set have the same schema.\nThe training set looks like: %s \nAnd the test data looks like %s", paste(names(training),"",collapse=""),paste(names(testing),"",collapse="")))
}

if (modus == "2D"){
    # for convenience, rename class column to "Class"
    names(training)[3] <- "Class"
    names(testing)[3] <- "Class"
    # change default behaviour, plot to pdf, in plot to window
    willFail <- tryCatch({
        x11() # fails on windows
        windows() # fails on linux
        } , error = function(err){
        err # Do nothing, just return the error
        }
    )
} else if (modus == "3D"){
    # for convenience, rename class column to "Class"
    names(training)[4] <- "Class"
    names(testing)[4] <- "Class"
}

    interface.draw <- function(panel) {
        classifier <-IBk(Class ~ ., training, control = Weka_control(K = as.numeric(panel$k)))
        testing <- testing
        testing$P <- predict(classifier, testing)
        if (opt$color == "Prediction"){
            testing$Color <- testing$P
        } else if (opt$color == "Correctness"){
            testing$Color <- (testing$Class == testing$P)
        } # TODO: does another switch makes sense? Maybe for 'Class', the true Class Attribute without prediction?
        # TODO: maybe output some evaluation data? e.g. percentage of misclassified instances
        if (modus == "3D"){
            plot3d(testing[[1]], testing[[2]], testing[[3]], xlab=names(testing)[1], ylab=names(testing)[2], zlab=names(testing)[3], col=testing$Color, size=1, type='s')
        } else if (modus == "2D"){
            plot(testing[[1]], testing[[2]],xlab=names(testing)[1], ylab=names(testing)[2], col=testing$Color)
        }
        panel
    }
    interface.panel <- rp.control("K nearest neighbor", k=opt$k)
    rp.textentry(panel=interface.panel, var=k, action=interface.draw, names="K nearest neighbor", labels="K=")
    # run it!
    rp.do(interface.panel, interface.draw)
    # user must close the panel to exit the program
    rp.block(interface.panel)
