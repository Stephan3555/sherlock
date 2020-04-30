package com.yahoo.sherlock.store.redis;

import com.yahoo.sherlock.enums.Triggers;
import com.yahoo.sherlock.exception.SlackNotFoundException;
import com.yahoo.sherlock.model.SlackMetaData;
import com.yahoo.sherlock.settings.DatabaseConstants;
import com.yahoo.sherlock.store.SlackMetadataAccessor;
import com.yahoo.sherlock.store.StoreParams;
import com.yahoo.sherlock.store.core.AsyncCommands;
import com.yahoo.sherlock.store.core.RedisConnection;
import io.lettuce.core.RedisFuture;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Slack metadata accessor implemented for clusters with lettuce.
 */
@Slf4j
public class LettuceSlackMetadataAccessor extends AbstractLettuceAccessor implements SlackMetadataAccessor {

    private final String slackIdIndex;
    private final String slackTriggerIndex;
    private final String slackJobIndex;

    /**
     * @param params store parameters
     */
    public LettuceSlackMetadataAccessor(StoreParams params) {
        super(params);
        this.slackIdIndex = params.get(DatabaseConstants.INDEX_SLACK_ID);
        this.slackTriggerIndex = params.get(DatabaseConstants.INDEX_SLACKID_TRIGGER);
        this.slackJobIndex = params.get(DatabaseConstants.INDEX_SLACKID_JOBID);
    }

    @Override
    public void putSlackMetadata(SlackMetaData slackMetaData) {
        log.info("Putting Slack metadata with ID [{}]", slackMetaData.getSlackId());
        String slackId = slackMetaData.getSlackId();
        try (RedisConnection<String> conn = connect()) {
            if (slackId != null && !slackId.isEmpty()) {
                AsyncCommands<String> cmd = conn.async();
                cmd.setAutoFlushCommands(false);
                RedisFuture[] futures = {
                        cmd.hmset(key(slackMetaData.getSlackId()), map(slackMetaData)),
                        cmd.sadd(index(slackIdIndex, DatabaseConstants.SLACKS), slackId)
                };
                cmd.flushCommands();
                await(futures);
                RedisFuture<Long> futureIndex;
                String interval = slackMetaData.getRepeatInterval();
                futureIndex = cmd.sadd(index(slackTriggerIndex, interval), slackId);
                cmd.flushCommands();
                await(futureIndex);
                log.info("Added slackId:{} to {} index", slackId, interval);
                log.info("Successfully added new Slack ID [{}]", slackId);
            } else {
                log.error("Slack ID can not be null or empty! value: {}!", slackId);
            }
        }
    }

    @Override
    public void putSlackMetadataIfNotExist(String slackId, String ownerSlackWebhook, String ownerSlackUsername, String ownerSlackIconEmoji, String ownerSlackMention, String jobId) throws IOException {

        log.info("Putting Slack with ID [{}] if not already exist", slackId);
        try (RedisConnection<String> conn = connect()) {
            if (slackId != null) {
                AsyncCommands<String> cmd = conn.async();
                cmd.setAutoFlushCommands(false);
                RedisFuture<Long> checkIndex = cmd.sadd(index(slackIdIndex, DatabaseConstants.SLACKS), slackId);
                RedisFuture<Long> jobSlackIndex = cmd.sadd(index(slackJobIndex, slackId), jobId);
                cmd.flushCommands();
                await(checkIndex, jobSlackIndex);
                if (checkIndex.get() != 0L) {
                    putSlackMetadata(new SlackMetaData(slackId, ownerSlackWebhook, ownerSlackUsername, ownerSlackIconEmoji, ownerSlackMention));
                } else {
                    log.info("[{}] already exist", slackId);
                }
            } else {
                log.error("Slack ID can not be null!");
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while deleting job!", e);
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public List<SlackMetaData> getAllSlackMetadata() throws IOException {
        log.info("Getting metadata for all slacks");
        try (RedisConnection<String> conn = connect()) {
            AsyncCommands<String> cmd = conn.async();
            cmd.setAutoFlushCommands(false);
            RedisFuture<Set<String>> slackFutureList = cmd.smembers(index(slackIdIndex, DatabaseConstants.SLACKS));
            cmd.flushCommands();
            await(slackFutureList);
            Set<String> slackList = slackFutureList.get();
            List<RedisFuture<Map<String, String>>> values = new ArrayList<>(slackList.size());
            log.info("Fetching metadata for {} slacks", slackList.size());
            for (String slackId : slackList) {
                values.add(cmd.hgetall(key(slackId)));
            }
            cmd.flushCommands();
            await(values);
            List<SlackMetaData> slackMetaDataList = new ArrayList<>(values.size());
            for (RedisFuture<Map<String, String>> value : values) {
                slackMetaDataList.add(unmap(SlackMetaData.class, value.get()));
            }
            return slackMetaDataList;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while getting slacks metadata!", e);
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public SlackMetaData getSlackMetadata(String slackId) throws IOException, SlackNotFoundException {
        log.info("Getting metadata for slack {}", slackId);
        try (RedisConnection<String> conn = connect()) {
            AsyncCommands<String> cmd = conn.async();
            cmd.setAutoFlushCommands(false);
            RedisFuture<Map<String, String>> value = cmd.hgetall(key(slackId));
            cmd.flushCommands();
            await(value);
            if (value.get() != null && value.get().isEmpty()) {
                throw new SlackNotFoundException(slackId);
            }
            return unmap(SlackMetaData.class, value.get());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while getting slacks metadata!", e);
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> checkSlacksInInstantIndex(List<String> slacks) throws IOException {
        log.info("Checking for slacks in Instant index");
        try (RedisConnection<String> conn = connect()) {
            AsyncCommands<String> cmd = conn.async();
            cmd.setAutoFlushCommands(false);
            RedisFuture<Set<String>> future = cmd.smembers(index(slackTriggerIndex, Triggers.INSTANT.toString()));
            cmd.flushCommands();
            await(future);
            List<String> result = new ArrayList<>();
            Set<String> instantList = future.get();
            for (String slack : slacks) {
                if (instantList.contains(slack)) {
                    result.add(slack);
                }
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while getting instant slack index!", e);
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void removeFromTriggerIndex(String slackId, String trigger) throws IOException {
        log.info("Removing {} from slack trigger {} index", slackId, trigger);
        try (RedisConnection<String> conn = connect()) {
            AsyncCommands<String> cmd = conn.async();
            cmd.setAutoFlushCommands(false);
            RedisFuture<Long> future = cmd.srem(index(slackTriggerIndex, trigger), slackId);
            cmd.flushCommands();
            await(future);
            log.info("Removed {} from index. redis response: {}", slackId, future.get().toString());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while getting instant slack index!", e);
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public List<SlackMetaData> getAllSlackMetadataByTrigger(String trigger) throws IOException {
        log.info("Getting metadata for all slacks for trigger {}", trigger);
        try (RedisConnection<String> conn = connect()) {
            AsyncCommands<String> cmd = conn.async();
            cmd.setAutoFlushCommands(false);
            RedisFuture<Set<String>> slackFutureList = cmd.smembers(index(slackTriggerIndex, trigger));
            cmd.flushCommands();
            await(slackFutureList);
            Set<String> slackList = slackFutureList.get();
            List<RedisFuture<Map<String, String>>> values = new ArrayList<>(slackList.size());
            log.info("Fetching metadata for {} slacks", slackList.size());
            for (String slackId : slackList) {
                values.add(cmd.hgetall(key(slackId)));
            }
            cmd.flushCommands();
            await(values);
            List<SlackMetaData> slackMetaDataList = new ArrayList<>(values.size());
            for (RedisFuture<Map<String, String>> value : values) {
                slackMetaDataList.add(unmap(SlackMetaData.class, value.get()));
            }
            return slackMetaDataList;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while getting slacks metadata!", e);
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteSlackMetadata(SlackMetaData slackMetadata) throws IOException {
        String slackId = slackMetadata.getSlackId();
        String trigger = slackMetadata.getRepeatInterval();
        log.info("Deleting slack {}", slackId);
        try (RedisConnection<String> conn = connect()) {
            AsyncCommands<String> cmd = conn.async();
            cmd.setAutoFlushCommands(false);
            RedisFuture[] slackIndexFutureList = new RedisFuture[]{
                    cmd.srem(index(slackTriggerIndex, trigger), slackId),
                    cmd.srem(index(slackIdIndex, DatabaseConstants.SLACKS), slackId),
                    cmd.del(index(DatabaseConstants.INDEX_SLACKID_REPORT, slackId)),
                    cmd.del(key(slackId))
            };
            cmd.flushCommands();
            await(slackIndexFutureList);
            log.info("Succesfully deleted slack {}", slackId);
        } catch (Exception e) {
            log.error("Error occurred while deleting slacks metadata!", e);
            throw new IOException(e.getMessage(), e);
        }
    }


}
