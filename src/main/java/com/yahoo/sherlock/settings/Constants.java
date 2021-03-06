/*
 * Copyright 2017, Yahoo Holdings Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.sherlock.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

/**
 * Constants for the project.
 */
@Slf4j
public final class Constants {

    /**
     * Constructor.
     */
    private Constants() { }

    /**
     * Constant for help url.
     */
    public static final String HELP_URL = "";

    /**
     * Constant for Trigger name minute.
     */
    public static final String MINUTE = "MINUTE";

    /**
     * Constant for Trigger name hour.
     */
    public static final String HOUR = "HOUR";

    /**
     * Constant for Trigger name day.
     */
    public static final String DAY = "DAY";

    /**
     * Constant for Trigger name week.
     */
    public static final String WEEK = "WEEK";

    /**
     * Constant for Trigger name month.
     */
    public static final String MONTH = "MONTH";

    /**
     * Timestamp format.
     */
    public static final String TIMESTAMP_FORMAT = "EEE dd MMM yyyy HH:mm:ss z";

    /**
     * Timestamp format without seconds.
     */
    public static final String TIMESTAMP_FORMAT_NO_SECONDS = "EEE dd MMM yyyy HH:mm z";

    /**
     * Constant for csrf authorizer to use for security.
     */
    public static final String CSRF = "csrf";

    /**
     * Constant for xss protection authorizer to use for security.
     */
    public static final String XSS_PROTECTION = "xssprotection";

    /**
     * JobId key in job datamap.
     */
    public static final String JOB_ID = "jobId";

    /**
     * Frequency key in job datamap.
     */
    public static final String FREQUENCY = "frequency";

    /**
     * Constant for success response.
     */
    public static final String SUCCESS = "success";

    /**
     * Granularities key for granularity values list.
     */
    public static final String GRANULARITIES = "granularities";

    /**
     * Frequencies key for frequency values list.
     */
    public static final String FREQUENCIES = "frequencies";

    /**
     * Constant for 'deletedJobsView' key in UI params.
     */
    public static final String DELETEDJOBSVIEW = "deletedJobsView";

    /**
     * Constant for 'instantView' key in UI params.
     */
    public static final String INSTANTVIEW = "instantView";

    /**
     * Constant for 'error' key in UI params.
     */
    public static final String ERROR = "error";

    /**
     * Constant for 'version' key in UI params.
     */
    public static final String VERSION = "version";

    /**
     * Constant for 'title' key in UI params.
     */
    public static final String TITLE = "title";

    /**
     * Constant for 'project' key in UI params.
     */
    public static final String PROJECT = "project";

    /**
     * Constant for Sherlock.
     */
    public static final String SHERLOCK = "Sherlock";

    /**
     * Constant for ':id' in request.
     */
    public static final String ID = ":id";

    /**
     * Constant for ':ids' in request.
     */
    public static final String IDS = ":ids";

    /**
     * Constant for ':frequency' in request.
     */
    public static final String FREQUENCY_PARAM = ":frequency";

    /**
     * Constant for 'warning'.
     */
    public static final String WARNING = "warning";

    /**
     * Constant for 'emailHtml'.
     */
    public static final String EMAIL_HTML = "emailHtml";

    /**
     * Constant for 'slackText'.
     */
    public static final String SLACK_TEXT = "slackText";

    /**
     * Constant for 'selectedDate'.
     */
    public static final String SELECTED_DATE = "selectedDate";

    /**
     * Constant for 'timelineDots'.
     */
    public static final String TIMELINE_POINTS = "timelinePoints";

    /**
     * Constant for 'hoursOfLag'.
     */
    public static final String HOURS_OF_LAG = "hoursOfLag";

    /**
     * Constant for 'druidCluster', when passing a list of clusters to the UI.
     */
    public static final String DRUID_CLUSTERS = "druidClusters";

    /**
     * Constant for 'slackChannels', when passing a list of channels to the UI.
     */
    public static final String SLACK_CHANNELS = "slackChannels";

    /**
     * Constant for 'emailError'.
     */
    public static final String EMAIL_ERROR = "emailError";

    /**
     * Constant for 'slackError'.
     */
    public static final String SLACK_ERROR = "slackError";

    /**
     * The number of milliseconds in a minute.
     */
    public static final int MILLISECONDS_IN_MINUTE = 60000;

    /**
     * The number of minutes in an hour.
     */
    public static final int MINUTES_IN_HOUR = 60;

    /**
     * The number of seconds in a minute.
     */
    public static final int SECONDS_IN_MINUTE = 60;

    /**
     * The number of hours in a day.
     */
    public static final int HOURS_IN_DAY = 24;

    /**
     * Constant for 'nodata' key in UI params.
     */
    public static final String NODATA = "nodata";

    /**
     * Delimiter constant for comma.
     */
    public static final String COMMA_DELIMITER = ",";

    /**
     * Delimiter constant for pipe.
     */
    public static final String PIPE_DELIMITER = "|";

    /**
     * Delimiter constant for colon.
     */
    public static final String COLON_DELIMITER = ":";

    /**
     * Delimiter constant for semicolon.
     */
    public static final String SEMICOLON_DELIMITER = ";";

    /**
     * Delimiter constant for '@'.
     */
    public static final String AT_DELIMITER = "@";

    /**
     * Delimiter constant for html line break.
     */
    public static final String HTML_LINEBREAK_DELIMITER = "<br/>";

    /**
     * Delimiter constant for new line.
     */
    public static final String NEWLINE_DELIMITER = "\n";

    /**
     * Retention time in weeks(unit is days) for redis keys.
     */
    public static final int REDIS_RETENTION_WEEKS_IN_DAYS = 14;

    /**
     * Retention time in years(unit is days) for redis keys.
     */
    public static final int REDIS_RETENTION_YEARS_IN_DAYS = 366;

    /**
     * Retention time (unit is days) for redis keys.
     */
    public static final int REDIS_RETENTION_ONE_DAY = 1;

    /**
     * Regex constant for whitespace.
     */
    public static final String WHITESPACE_REGEX = "\\s+";

    /**
     * Constant for _cloned.
     */
    public static final String CLONED = "_cloned";

    /**
     * Maximum for minute in an hour value.
     */
    public static final int MAX_MINUTE = 60;

    /**
     * Maximum for hour in a day value.
     */
    public static final int MAX_HOUR = 24;

    /**
     * Maximum for day in a month value.
     */
    public static final int MAX_DAY = 7;

    /**
     * Maximum for week in a year value.
     */
    public static final int MAX_WEEK = 4;

    /**
     * Maximum for month in a year value.
     */
    public static final int MAX_MONTH = 12;

    /**
     * Number of seconds in a day.
     */
    public static final long SECONDS_IN_DAY = Constants.HOURS_IN_DAY * Constants.MINUTES_IN_HOUR * Constants.SECONDS_IN_MINUTE;

    /**
     * Constant for 'anomalyDetectionModels'.
     */
    public static final String ANOMALY_DETECTION_MODELS = "anomalyDetectionModels";

    /**
     * Constant for 'timeseriesModels'.
     */
    public static final String TIMESERIES_MODELS = "timeseriesModels";

    /**
     * Constant for 'http'.
     */
    public static final String HTTP = "http";

    /**
     * Constant for 'path'.
     */
    public static final String PATH = "path";

    /**
     * Constant set of allowed druid brokers.
     */
    public static final Set<String> VALID_DRUID_BROKERS;

    static {
        List<String> list;
        // read allowed druid brokers from the file
        if (CLISettings.DRUID_BROKERS_LIST_FILE != null) {
            try (Stream<String> stream = Files.lines(Paths.get(CLISettings.DRUID_BROKERS_LIST_FILE))) {
                list = stream
                    .map(l -> l.replaceAll(WHITESPACE_REGEX, ""))
                    .map(s -> Arrays.asList(s.split(COMMA_DELIMITER)))
                    .flatMap(List::stream)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            } catch (IOException e) {
                log.error("Exception while reading druid brokers list file {}", CLISettings.DRUID_BROKERS_LIST_FILE, e);
                list = new ArrayList<>();
            }
            VALID_DRUID_BROKERS = new HashSet<>(list);
        } else {
            VALID_DRUID_BROKERS = null;
        }
    }
}
