package com.oracle.psr.workerClasses;

import java.util.logging.Logger;

import weblogic.logging.LoggingHelper;

public class GetLogger {
public static Logger getLogger(){   
    Logger logger = null ;
	logger = LoggingHelper.getServerLogger();
    return logger ;
}
}