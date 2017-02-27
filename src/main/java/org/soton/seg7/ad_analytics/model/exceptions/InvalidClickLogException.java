package org.soton.seg7.ad_analytics.model.exceptions;

/**
 * Created by Tom on 27/02/17.
 */
public class InvalidClickLogException extends Exception {

    private static final String MESSAGE = "Error loading click log, file is in wrong format for an click log";

    public InvalidClickLogException() { super(MESSAGE); }

}
