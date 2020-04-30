package com.yahoo.sherlock.exception;

/**
 * Exception thrown when no {@code SlackMetadata} can be
 * found with a specified slackId.
 */
public class SlackNotFoundException extends Exception {

    /** Exception message. */
    public static final String EXEPTION_MSG = "Slack is not available";

    /**
     * Default constructor.
     */
    public SlackNotFoundException() {
        super();
    }

    /**
     * Constructor with a message.
     *
     * @param message exception message
     */
    public SlackNotFoundException(String message) {
        super(EXEPTION_MSG);
    }

    /**
     * Constructor with a message and a throwable.
     *
     * @param message exception message
     * @param cause   cause of exception
     */
    public SlackNotFoundException(String message, Throwable cause) {
        super(EXEPTION_MSG, cause);
    }

}
