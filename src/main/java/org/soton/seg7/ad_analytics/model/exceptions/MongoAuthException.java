package org.soton.seg7.ad_analytics.model.exceptions;

/**
 * Created by bogdanbuduroiu on 25/02/2017.
 */
public class MongoAuthException extends Exception {

    private static final String MESSAGE = "Error while trying to reach the database. See below for stack trace: ";

    public MongoAuthException() {
        super(MESSAGE);
    }
}
