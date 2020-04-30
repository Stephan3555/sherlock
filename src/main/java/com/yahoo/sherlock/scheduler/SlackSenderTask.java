package com.yahoo.sherlock.scheduler;

import com.yahoo.sherlock.enums.Triggers;
import com.yahoo.sherlock.service.SlackService;
import com.yahoo.sherlock.utils.TimeUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * Class for slack sender runnable task.
 */
@NoArgsConstructor
@Slf4j
public class SlackSenderTask implements Runnable {

    /**
     * Thread name prefix.
     */
    private static final String THREAD_NAME_PREFIX = "SlackSenderTask-";

    /**
     * Slack Service obj to send slack messages.
     */
    private SlackService slackService = new SlackService();

    @Override
    public void run() {
        try {
            String name = THREAD_NAME_PREFIX + Thread.currentThread().getName();
            log.info("Running thread {}", name);
            runSlackSender(TimeUtils.getTimestampMinutes());
        } catch (IOException e) {
            log.error("Error while running slack sender task!", e);
        }
    }

    /**
     * Method to send slack messages if required at this time.
     *
     * @param timestampMinutes input current timestamp in minutes
     * @throws IOException if an error sending email
     */
    public void runSlackSender(long timestampMinutes) throws IOException {
        ZonedDateTime date = TimeUtils.zonedDateTimeFromMinutes(timestampMinutes);
        slackService.sendConsolidatedSlack(date, Triggers.DAY.toString());
        slackService.sendConsolidatedSlack(date, Triggers.HOUR.toString());
        if (date.getDayOfMonth() == 1) {
            slackService.sendConsolidatedSlack(date, Triggers.MONTH.toString());
        } else if (date.getDayOfWeek().getValue() == 1) {
            slackService.sendConsolidatedSlack(date, Triggers.WEEK.toString());
        }
    }
}
