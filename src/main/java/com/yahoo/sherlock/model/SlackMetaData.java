package com.yahoo.sherlock.model;

import com.yahoo.sherlock.enums.Triggers;
import com.yahoo.sherlock.store.Attribute;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Data storer for slack id metadata.
 */
@Data
@Builder(builderClassName = "Builder")
public class SlackMetaData implements Serializable {

    /**
     * Serialization id for uniformity across platform.
     */
    private static final long serialVersionUID = 32L;

    /**
     * SlackId value.
     */
    @Attribute
    private String slackId;

    /**
     * SlackWebhookUrl value.
     */
    @Attribute
    private String slackWebhookUrl;

    /**
     * SlackUsername value.
     */
    @Attribute
    private String slackUsername;

    /**
     * slackIconEmoji value.
     */
    @Attribute
    private String slackIconEmoji;

    /**
     * slackMention value.
     */
    @Attribute
    private String slackMention;

    /**
     * Hours value(0-23) to send out slack.
     */
    @Attribute
    private String sendOutHour = "12";

    /**
     * Minutes value(0-59) to send out slack.
     */
    @Attribute
    private String sendOutMinute = "00";

    /**
     * Slack sendout repeat interval.
     * It can have six values: {@link Triggers}
     */
    @Attribute
    private String repeatInterval = Triggers.INSTANT.toString();

    /**
     * Default constructor.
     */
    public SlackMetaData() {

    }

    /**
     * Constructor to intialize default object.
     *
     * @param slackId slackId to add
     * @param slackWebhookUrl slackWebhookUrl to add
     * @param slackUsername slackUsername to add
     * @param slackIconEmoji slackIconEmoji to add
     * @param slackMention slackMention to add
     */
    public SlackMetaData(String slackId, String slackWebhookUrl, String slackUsername, String slackIconEmoji, String slackMention) {

        this.slackId = slackId;
        this.slackWebhookUrl = slackWebhookUrl;
        this.slackUsername = slackUsername;
        this.slackIconEmoji = slackIconEmoji;
        this.slackMention = slackMention;
    }


    /**
     * Constructor with params.
     *
     * @param slackId slackId to add
     * @param slackWebhookUrl slackWebhookUrl to add
     * @param slackUsername slackUsername to add
     * @param slackIconEmoji slackIconEmoji to add
     * @param slackMention slackMention to add
     * @param sendOutHour    send out hour value(0-23)
     * @param sendOutMinute  send out hour value(0-23)
     * @param repeatInterval slack send out interval value: possible values {@link Triggers}
     */
    public SlackMetaData(String slackId, String slackWebhookUrl, String slackUsername, String slackIconEmoji, String slackMention, String sendOutHour, String sendOutMinute, String repeatInterval) {
        this.slackId = slackId;
        this.slackWebhookUrl = slackWebhookUrl;
        this.slackUsername = slackUsername;
        this.slackIconEmoji = slackIconEmoji;
        this.slackMention = slackMention;
        this.sendOutHour = sendOutHour;
        this.sendOutMinute = sendOutMinute;
        this.repeatInterval = repeatInterval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SlackMetaData)) {
            return false;
        }

        SlackMetaData that = (SlackMetaData) o;

        return slackId.equals(that.slackId);
    }

    @Override
    public int hashCode() {
        return slackId.hashCode();
    }
}
