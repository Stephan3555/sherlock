package com.yahoo.sherlock.store;

import com.yahoo.sherlock.exception.SlackNotFoundException;
import com.yahoo.sherlock.model.SlackMetaData;

import java.io.IOException;
import java.util.List;

/**
 * The {@code SlackMetadataAccessor} defines an interface for
 * communicating with a persistence layer, be it SQL, Redis, etc.,
 * to retrieve and store {@code SlackMetadata} objects.
 */
public interface SlackMetadataAccessor {

    /**
     * Put the new default {@code SlackMetadata} object if the slackId not present.
     *
     * @param slackId             slack ID
     * @param ownerSlackWebhook   Slacks webhook Url
     * @param ownerSlackUsername  Slacks Username
     * @param ownerSlackMention   Slacks Mention
     * @param ownerSlackIconEmoji Slacks Icon Emoji
     * @param jobId               jobId related to the slack
     * @throws IOException if there is an error with the persistence layer
     */
    void putSlackMetadataIfNotExist(String slackId, String ownerSlackWebhook, String ownerSlackUsername, String ownerSlackMention, String ownerSlackIconEmoji, String jobId) throws IOException;

    /**
     * Put a {@code SlackMetaData} object in the store.
     * SlackMetaData with existing ID should be overridden.
     * The method will overwrite existing objects.
     *
     * @param slackMetaData the slack ID to store
     * @throws IOException if there is an error with the persistence layer
     */
    void putSlackMetadata(SlackMetaData slackMetaData) throws IOException;

    /**
     * Get the list of all {@code SlackMetadata} objects.
     *
     * @return the slack metadata object list
     * @throws IOException if there is an error with the persistence layer
     */
    List<SlackMetaData> getAllSlackMetadata() throws IOException;

    /**
     * Get the {@code SlackMetadata} object with the specifFied slackId.
     *
     * @param slackId the slackId for which to retrieve the slack metadata
     * @return the slack metadata with the specified slackId
     * @throws IOException            if there is an error with the persistence layer
     * @throws SlackNotFoundException if no slack can be found with the specified ID
     */
    SlackMetaData getSlackMetadata(String slackId) throws IOException, SlackNotFoundException;

    /**
     * Get the list of all input slacks that are present in instant Slack index.
     *
     * @param slacks list of input slacks
     * @return list of all input slacks that are present in instant Slack index
     * @throws IOException if there is an error with the persistence layer
     */
    List<String> checkSlacksInInstantIndex(List<String> slacks) throws IOException;

    /**
     * Remove the slackid from the index of given trigger.
     *
     * @param slackId slackId
     * @param trigger trigger name
     * @throws IOException if there is an error with the persistence layer
     */
    void removeFromTriggerIndex(String slackId, String trigger) throws IOException;

    /**
     * Get the list of all {@code SlackMetaData} objects in a given trigger index.
     *
     * @param trigger trigger name
     * @return the slack metadata object list
     * @throws IOException if there is an error with the persistence layer
     */
    List<SlackMetaData> getAllSlackMetadataByTrigger(String trigger) throws IOException;

    /**
     * Method to delete the slack metadata object from all index and database.
     *
     * @param slackMetadata slack metadata object to delete
     */
    void deleteSlackMetadata(SlackMetaData slackMetadata) throws IOException;


}
