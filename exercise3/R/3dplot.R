#!/usr/bin/Rscript

# plotting library
library(rgl)

# library to wait for user input
library(tcltk)

Nth.delete<-function(dataframe, n)dataframe[(seq(n,to=nrow(dataframe),by=n)),]
m <- read.csv(file="Skin_NonSkin.csv", sep = "\t", head=FALSE)
# We choose every 1000 row as an instance for our sample set
sample <- Nth.delete(m,1000)
# Renaming of columns
names(sample)[names(sample)=="V1"] <- "B"
names(sample)[names(sample)=="V2"] <- "G"
names(sample)[names(sample)=="V3"] <- "R"
names(sample)[names(sample)=="V4"] <- "Class"


plot3d(sample$R,sample$G,sample$B, col=sample$Class, size=2, type='s')

tk_messageBox(message = "Press a key to exit")
