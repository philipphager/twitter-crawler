package de.hpi.msd;

import de.hpi.msd.model.interaction.Interaction;
import de.hpi.msd.model.interaction.InteractionType;
import de.hpi.msd.model.task.CrawlTask;
import de.hpi.msd.model.task.RetweetCrawlTask;
import de.hpi.msd.model.task.TimelineCrawlTask;
import org.slf4j.LoggerFactory;
import twitter4j.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrawlerProcess implements Runnable {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger("de.hpi.msd.CrawlerProcess");
    private static final long MAX_RETWEETS = 500;
    private static final int MAX_TIMELINE_TWEETS = 25;
    private static final Paging TIMELINE_PAGING = new Paging(1, MAX_TIMELINE_TWEETS);
    private final Twitter twitter;
    private final Queue<CrawlTask> crawlerQueue;
    private final Set<CrawlTask> crawledTasks;
    private final Writer writer;
    private final String name;

    public CrawlerProcess(Twitter twitter,
                          Queue<CrawlTask> crawlerQueue,
                          Set<CrawlTask> crawledTasks,
                          File file,
                          String name) throws IOException {
        this.twitter = twitter;
        this.crawlerQueue = crawlerQueue;
        this.crawledTasks = crawledTasks;
        this.writer = new FileWriter(file);
        this.name = name;
    }

    @Override
    public void run() {
        log.info("{}: Creating new task thread", name);

        CrawlTask task;

        while (!Thread.currentThread().isInterrupted()) {
            if ((task = crawlerQueue.poll()) != null) {
                try {
                    if (task instanceof TimelineCrawlTask) {
                        TimelineCrawlTask timelineCrawlTask = (TimelineCrawlTask) task;
                        crawlUserTimeline(timelineCrawlTask);

                        log.debug("Crawled timeline of user: {} Queue: {}", timelineCrawlTask.getUserId(), crawlerQueue.size());
                    } else if (task instanceof RetweetCrawlTask) {
                        RetweetCrawlTask retweetCrawlTask = (RetweetCrawlTask) task;
                        crawlRetweets((RetweetCrawlTask) task);

                        log.debug("Crawled retweets of tweet: {} Skip: {} Limit: {} Queue: {}",
                                retweetCrawlTask.getTweetId(),
                                retweetCrawlTask.getSkip(),
                                retweetCrawlTask.getLimit(),
                                crawlerQueue.size());
                    }
                } catch (TwitterException e) {
                    crawlerQueue.add(task);

                    if (e.getRateLimitStatus() != null) {
                        waitForReset(e.getRateLimitStatus());
                    } else {
                        log.error("{}: TwitterException while crawling", name, e);
                    }
                } catch (IOException e) {
                    log.error("{}: IOException while crawling", name, e);
                }
            }
        }
    }

    private void waitForReset(RateLimitStatus rateLimitStatus) {
        if (rateLimitStatus.getRemaining() == 0) {
            try {
                int minutesUntilReset = rateLimitStatus.getSecondsUntilReset() / 60;
                log.debug("{}: Rate limit succeeded. Waiting for {} mins", name, minutesUntilReset);
                Thread.sleep(rateLimitStatus.getSecondsUntilReset() * 1000);
            } catch (InterruptedException e) {
                log.error("{}: Interrupted while waiting for API rate limit reset", name, e);
            }
        }
    }

    private void crawlUserTimeline(TimelineCrawlTask task) throws TwitterException, IOException {
        crawledTasks.add(task);

        final List<Status> statuses = twitter.getUserTimeline(task.getUserId(), TIMELINE_PAGING);
        final List<Interaction> interactions = getInteractions(statuses);
        writeToFile(interactions);

        crawlerQueue.addAll(getCrawlTasksFromStatus(statuses));
    }

    private void crawlRetweets(RetweetCrawlTask task) throws TwitterException, IOException {
        crawledTasks.add(task);

        final long[] retweeterIds = twitter.getRetweeterIds(task.getTweetId(), task.getLimit(), task.getSkip()).getIDs();
        final List<Interaction> interactions = getRetweetInteractions(task.getTweetId(), retweeterIds);
        writeToFile(interactions);

        crawlerQueue.addAll(getCrawlTasksFromRetweet(retweeterIds));

        if (retweeterIds.length == task.getLimit() && (task.getSkip() + task.getLimit()) < MAX_RETWEETS) {
            final RetweetCrawlTask retweetCrawlTask = task.withSkip(task.getSkip() + task.getLimit());

            if (!crawledTasks.contains(retweetCrawlTask)) {
                crawlerQueue.add(retweetCrawlTask);
            }
        }
    }

    private List<Interaction> getInteractions(List<Status> statuses) {
        return statuses.stream()
                .flatMap(status -> status.isRetweet() ? Stream.of(status, status.getRetweetedStatus()) : Stream.of(status))
                .map(status -> {
                    final long userId = status.getUser().getId();
                    final long tweetId = status.getId();
                    final InteractionType interactionType = status.isRetweet()
                            ? InteractionType.RETWEETS
                            : InteractionType.TWEETS;
                    return new Interaction(userId, tweetId, interactionType);
                }).collect(Collectors.toList());
    }

    private List<Interaction> getRetweetInteractions(long tweetId, long[] retweeterIds) {
        return Arrays.stream(retweeterIds)
                .mapToObj(userId -> new Interaction(userId, tweetId, InteractionType.RETWEETS))
                .collect(Collectors.toList());
    }

    private Set<? extends CrawlTask> getCrawlTasksFromStatus(List<Status> statuses) {
        return statuses.parallelStream()
                .flatMap(status -> status.isRetweet()
                        ? Stream.of(status, status.getRetweetedStatus())
                        : Stream.of(status))
                .flatMap(status -> Stream.of(
                        new TimelineCrawlTask(status.getUser().getId()),
                        new RetweetCrawlTask(status.getId(), 0, 100)))
                .filter(task -> !crawledTasks.contains(task))
                .collect(Collectors.toSet());
    }

    private Collection<? extends CrawlTask> getCrawlTasksFromRetweet(long[] retweeterIds) {
        return Arrays.stream(retweeterIds)
                .parallel()
                .mapToObj(TimelineCrawlTask::new)
                .filter(task -> !crawledTasks.contains(task))
                .collect(Collectors.toSet());

    }

    private void writeToFile(List<Interaction> interactions) throws IOException {
        for (Interaction interaction : interactions) {
            writer.append(String.valueOf(interaction.getUserId()))
                    .append(", ")
                    .append(String.valueOf(interaction.getTweetId()))
                    .append(", ")
                    .append(String.valueOf(interaction.getType().getEdgeType()))
                    .append("\n");
        }
        writer.flush();
    }
}
