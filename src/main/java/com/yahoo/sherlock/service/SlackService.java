/*
 * Copyright 2017, Yahoo Holdings Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.sherlock.service;

import com.google.common.base.Strings;
import com.slack.api.Slack;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import com.yahoo.sherlock.enums.Triggers;
import com.yahoo.sherlock.model.AnomalyReport;
import com.yahoo.sherlock.model.JobMetadata;
import com.yahoo.sherlock.model.SlackMetaData;
import com.yahoo.sherlock.settings.CLISettings;
import com.yahoo.sherlock.settings.Constants;
import com.yahoo.sherlock.store.AnomalyReportAccessor;
import com.yahoo.sherlock.store.SlackMetadataAccessor;
import com.yahoo.sherlock.store.Store;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.UrlValidator;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;

/**
 * Slack class for slack service.
 */
@Slf4j
public class SlackService {

    /**
     * SlackMetadataAccessor object.
     */
    protected SlackMetadataAccessor slackMetadataAccessor;

    /**
     * AnomalyReportAccessor object.
     */
    protected AnomalyReportAccessor anomalyReportAccessor;

    /**
     * Constructor.
     */
    public SlackService() {
        slackMetadataAccessor = Store.getSlackMetadataAccessor();
        anomalyReportAccessor = Store.getAnomalyReportAccessor();
    }

    /**
     * Sends the consolidated alerts over the specified trigger period.
     *
     * @param zonedDateTime current date as ZonedDateTime object
     * @param trigger       trigger name value
     */
    public void sendConsolidatedSlack(ZonedDateTime zonedDateTime, String trigger) throws IOException {
        List<SlackMetaData> slacks = slackMetadataAccessor.getAllSlackMetadataByTrigger(trigger);
        log.info("Found {} slacks for {} trigger index", slacks.size(), trigger);
        for (SlackMetaData slackMetaData : slacks) {
            int hour = Integer.valueOf(slackMetaData.getSendOutHour());
            int minute = Integer.valueOf(slackMetaData.getSendOutMinute());
            boolean isTimeToSend = trigger.equalsIgnoreCase(Triggers.HOUR.toString()) ?
                    zonedDateTime.getMinute() == minute :
                    zonedDateTime.getHour() == hour && zonedDateTime.getMinute() == minute;
            if (isTimeToSend) {
                List<AnomalyReport> anomalyReports = anomalyReportAccessor.getAnomalyReportsForSlackId(slackMetaData.getSlackId());
                List<AnomalyReport> filteredReports = anomalyReports.stream()
                        .filter(a -> !a.getStatus().equalsIgnoreCase(Constants.SUCCESS))
                        .collect(Collectors.toList());
                log.info("Sending {} anomaly reports to {}", filteredReports.size(), slackMetaData.getSlackId());
                if (filteredReports.size() > 0) {
                    if (!sendSlack(Constants.SHERLOCK, Arrays.asList(slackMetaData), filteredReports)) {
                        log.error("Error while sending slack: {}, trigger: {}!", slackMetaData.getSlackId(), trigger);
                    }
                }
            }
        }
    }

    /**
     * Method to send slack messages to given slack channels.
     *
     * @param job            job for which the anomalies are found
     * @param slackMetaDatas list of SlackMetaDatas
     * @param anomalyReports list of anomaly reports
     */
    public void processSlackReports(JobMetadata job, List<SlackMetaData> slackMetaDatas, List<AnomalyReport> anomalyReports) {
        anomalyReports = anomalyReports.stream()
                .filter(a -> !a.getStatus().equalsIgnoreCase(Constants.SUCCESS))
                .collect(Collectors.toList());
        if (CLISettings.ENABLE_SLACK) {
            if (isErrorCase(anomalyReports)) {
                SlackMetaData slackMetaData = SlackMetaData.builder()
                        .slackWebhookUrl(CLISettings.FAILURE_SLACK_WEBHOOK_URL)
                        .slackUsername(CLISettings.FAILURE_SLACK_USERNAME)
                        .slackIconEmoji(CLISettings.FAILURE_SLACK_ICON_EMOJI)
                        .slackMention(CLISettings.FAILURE_SLACK_MENTION)
                        .build();
                if (!sendSlack(CLISettings.FAILURE_SLACK_WEBHOOK_URL, Arrays.asList(slackMetaData), anomalyReports)) {
                    log.error("Error while sending failure slack!");
                }
            } else if (isNoDataCase(anomalyReports)) {
                if (job.getNotificationOnNoData()) {
                    if (!sendSlack(job.getOwner(), slackMetaDatas, anomalyReports)) {
                        log.error("Error while sending Nodata slack!");
                    }
                }
            } else {
                if (!sendSlack(job.getOwner(), slackMetaDatas, anomalyReports)) {
                    log.error("Error while sending anomaly report slack!");
                }
            }
        }
    }

    /**
     * Helper to identify Error case in anomaly reports.
     *
     * @param anomalyReports input list of anomaly reports
     * @return true if error case else false
     */
    private boolean isErrorCase(List<AnomalyReport> anomalyReports) {
        return anomalyReports.size() == 1 && anomalyReports.get(0).getStatus().equals(Constants.ERROR);
    }

    /**
     * Helper to identify Nodata case in anomaly reports.
     *
     * @param anomalyReports input list of anomaly reports
     * @return true if no data case else false
     */
    private boolean isNoDataCase(List<AnomalyReport> anomalyReports) {
        return anomalyReports.size() == 1 && anomalyReports.get(0).getStatus().equals(Constants.NODATA);
    }

    /**
     * Method for slack service.
     *
     * @param owner          owner name
     * @param slacks         Slack Metadata list
     * @param anomalyReports anomaly report as a list of anomalies
     * @return status of slack message: true for success, false for error
     */
    public boolean sendSlack(String owner, List<SlackMetaData> slacks, List<AnomalyReport> anomalyReports) {

        Payload payload = Payload.builder().build();
        try {
            if (!slacks.isEmpty()) {

                if (anomalyReports.size() > 0) {
                    for (SlackMetaData slack : slacks) {
                        LinkedList<LayoutBlock> layoutBlocks = new LinkedList<>();
                        // add SlackMedaData
                        layoutBlocks.addAll(generateSlackMetadataBlock(slack));

                        if (!(isNoDataCase(anomalyReports) || isErrorCase(anomalyReports))) {
                            layoutBlocks.addFirst(section(section -> section.text(markdownText(
                                    "*Anomaly Report:*"))));
                            layoutBlocks.add(divider());
                            layoutBlocks.addAll(renderPayloadForAnomalyReport(anomalyReports));
                            layoutBlocks.add(divider());

                            payload = Payload.builder()
                                    .text("Sherlock Anomaly Report for " + slack.getSlackId())
                                    .blocks(layoutBlocks).build();

                            log.info("Slack rendered sunccessfully.");

                            // send slack message
                            log.info("Sending slack to " + owner + " on slack channel: " + slacks);
                            return sendFormattedSlack(slack, payload);
                        } else {
                            layoutBlocks.addFirst(section(section -> section.text(markdownText(
                                    "*Anomaly Report ERROR:*"))));
                            layoutBlocks.add(divider());
                            layoutBlocks.addAll(renderPayloadForAnomalyReportError(anomalyReports));
                            layoutBlocks.add(divider());

                            payload = Payload.builder()
                                    .text("Sherlock Anomaly Report for " + slack.getSlackId())
                                    .blocks(layoutBlocks).build();

                            log.info("Slack Error rendered sunccessfully.");

                            // send slack message
                            log.info("Sending slack ERROR to " + owner + " on slack channel: " + slacks);
                            return sendFormattedSlack(slack, payload);
                        }

                    }
                } else {
                    return true;
                }


            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("Exception processing slack body!", e);
            return false;
        }

        return false;

    }

    /**
     * Generates the payload for an Slack Error Anomaly Report.
     *
     * @param anomalyReports anomalyreports to generate the payload for
     * @return a list of layoutblocks containing the payload
     */
    private Collection<? extends LayoutBlock> renderPayloadForAnomalyReportError(List<AnomalyReport> anomalyReports) {
        LinkedList<LayoutBlock> layoutBlocks = new LinkedList<>();


        // render the slack message
        for (AnomalyReport anomalyReport : anomalyReports) {
            LayoutBlock layoutblock = section(section -> section.text(markdownText(
                    "*Anomaly Test Name:* `" + anomalyReport.getTestName() + "`\n" +
                            "*Report Time(Latest missing datapoint):* `" + anomalyReport.getFormattedReportGenerationTime() + "`\n" +
                            "*Job Status:* `" + anomalyReport.getStatus() + "`\n" +
                            "*Visualization Link:* `" + anomalyReport.getQueryURL() + "`\n")));

            layoutBlocks.add(layoutblock);
            layoutBlocks.add(divider());
        }
        return layoutBlocks;
    }

    /**
     * Generates the payload for an Slack Anomaly Report.
     *
     * @param anomalyReports anomalyreports to generate the payload for
     * @return a list of layoutblocks containing the payload
     */
    private Collection<? extends LayoutBlock> renderPayloadForAnomalyReport(List<AnomalyReport> anomalyReports) {
        LinkedList<LayoutBlock> layoutBlocks = new LinkedList<>();

        // render the slack message
        for (AnomalyReport anomalyReport : anomalyReports) {
            LayoutBlock layoutblock = section(section -> section.text(markdownText(
                    "*Metric:* `" + anomalyReport.getMetricInfo() + "`\n" +
                            "*Group By Dimensions:* `" + anomalyReport.getGroupByFilters() + "`\n" +
                            "*Anomaly Info:* `" + anomalyReport.getFormattedAnomalyTimestamps() + "`\n" +
                            "*Metric Deviation:* `" + anomalyReport.getFormattedDeviation() + "`\n" +
                            "*Job Status:* `" + anomalyReport.getStatus() + "`\n" +
                            "*Model Info:* `" + anomalyReport.getModelInfo() + "`\n" +
                            "*Visualization Link:* `" + anomalyReport.getQueryURL() + "`\n")));

            layoutBlocks.add(layoutblock);
            layoutBlocks.add(divider());
        }
        return layoutBlocks;
    }

    /**
     * Generates a Message Block with Slack Metadata at the beginning of the message.
     *
     * @param slack SlackMetadata to create the message
     * @return List of LayoutBlocks containing the metadata
     */
    private List<LayoutBlock> generateSlackMetadataBlock(SlackMetaData slack) {
        List<LayoutBlock> layoutBlocks = new LinkedList<>();
        layoutBlocks.add(section(section -> section.text(markdownText(
                slack.getSlackIconEmoji() + " " + slack.getSlackUsername() + " " + transformMentionToMarkdown(slack.getSlackMention())

        ))));
        return layoutBlocks;
    }

    /**
     * Transform the Slack Mention in an Markdown Element, so Slack can understand it.
     *
     * @param mention The mention to transform
     * @return returns the corresponding slack markdown element or the mention itself if its not known
     */
    private String transformMentionToMarkdown(String mention) {
        switch (mention) {
            case "@Here":
            case "@here":
                return "<!here|here>";
            case "@Channel":
            case "@channel":
                return "<!channel|channel>";
            case "@Everyone":
            case "@everyone":
                return "<!everyone|everyone>";
            default:
                return "<" + mention + ">";
        }

    }

    /**
     * Method to send final formatted Slack message.
     *
     * @param slackMetaData where to send the slack message
     * @param payload       the payload for the message
     * @return true or false: success or failure to send slack message
     */
    protected boolean sendFormattedSlack(SlackMetaData slackMetaData, Payload payload) {

        try (Slack slack = Slack.getInstance()) {

            WebhookResponse response = slack.send(slackMetaData.getSlackWebhookUrl(), payload);

            if (response.getCode() == 200) {
                log.info("Slack sent successfully!");
                return true;
            } else {
                log.error("Problem while sending Slack Message!");
                log.error("SLack ReturnCode: " + response.getCode());
                log.error("SLack Returnmessage : " + response.getMessage());
                log.error("SLack Returnbody : " + response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.error("Exception in sending slack message!", e);
            return false;
        }


    }

    /**
     * Validates if the Slack message is valid. To be valid the values for slackId and webhookUrl must be set.
     * Furthermore every single field will be tested, if they are filled out correctly.
     *
     * @param slackId    slackId for the channel
     * @param webhookUrl webhook to send the slack message
     * @param username   optional username of the message
     * @param iconEmoji  optional icon to display in the message
     * @param mention    optional mention in the message
     * @return true if slack is valid, otherwise false
     */
    public boolean validateSlack(String slackId, String webhookUrl, String username, String iconEmoji, String mention) {

        if (Strings.isNullOrEmpty(slackId) && Strings.isNullOrEmpty(webhookUrl)) {
            return false;
        }

        UrlValidator defaultValidator = new UrlValidator();
        boolean resultWebHookUrl = defaultValidator.isValid(webhookUrl);

        boolean resultUsername = !Strings.isNullOrEmpty(username);

        boolean resultIconEmoji = true;
        if (!Strings.isNullOrEmpty(iconEmoji)) {
            resultIconEmoji = iconEmoji.startsWith(":") && iconEmoji.endsWith(":");
        }

        boolean resultMention = true;
        if (!Strings.isNullOrEmpty(mention)) {
            resultMention = mention.startsWith("@");
        }


        return resultWebHookUrl && resultUsername && resultIconEmoji && resultMention;
    }

}
