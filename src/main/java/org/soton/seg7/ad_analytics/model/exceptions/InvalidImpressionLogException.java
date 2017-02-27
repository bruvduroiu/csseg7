package org.soton.seg7.ad_analytics.model.exceptions;

/**
 * Created by Tom on 27/02/17.
 */
public class InvalidImpressionLogException extends Exception {

    private static final String MESSAGE = "Error loading impression log, file is in wrong format for an impression log";

    public InvalidImpressionLogException() { super(MESSAGE); }

}
