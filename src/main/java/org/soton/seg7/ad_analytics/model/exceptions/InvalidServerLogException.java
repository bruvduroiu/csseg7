package org.soton.seg7.ad_analytics.model.exceptions;

public class InvalidServerLogException extends Exception {

    private static final String MESSAGE = "Error loading server log, file is in wrong format for an server log";

    public InvalidServerLogException() { super(MESSAGE); }

}
