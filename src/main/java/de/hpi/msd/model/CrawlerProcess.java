package de.hpi.msd.model;

import twitter4j.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrawlerProcess implements Runnable {
    private final static int PAGE_SIZE = 1000;
    private final Twitter twitter;
    private final Queue<CrawlTask> crawlerQueue;
    private final Set<Long> crawledUsers;
    private final Writer writer;
    private final String name;

    public CrawlerProcess(Twitter twitter, Queue<CrawlTask> crawlerQueue, Set<Long> crawledUsers, File file, String name) throws IOException {
        this.twitter = twitter;
        this.crawlerQueue = crawlerQueue;
        this.crawledUsers = crawledUsers;
        this.writer = new FileWriter(file);
        this.name = name;
    }

    @Override
    public void run() {
        System.out.printf("%s: Creating new crawler thread.\n", name);
        CrawlTask task;

        while (!Thread.currentThread().isInterrupted()) {
            if ((task = crawlerQueue.poll()) != null) {
                try {
                    final List<Status> statuses = getStatuses(task);
                    final List<Interaction> interactions = getInteractions(task, statuses);
                    writeToFile(interactions);

                    final Set<CrawlTask> crawlTasks = getCrawlTasks(statuses);
                    crawlerQueue.addAll(crawlTasks);
                } catch (TwitterException e) {
                    // Re-add task to queue
                    crawlerQueue.add(task);
                    // Rate limit exceeded
                    RateLimitStatus rateLimitStatus = e.getRateLimitStatus();
                    if (rateLimitStatus != null) {
                        waitForReset(rateLimitStatus);
                    } else {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void waitForReset(RateLimitStatus rateLimitStatus) {
        if (rateLimitStatus.getRemaining() == 0) {
            try {
                System.out.printf("%s: Rate limit succeeded. Waiting for %d mins.%n\n", name, rateLimitStatus.getSecondsUntilReset() / 60);
                Thread.sleep(rateLimitStatus.getSecondsUntilReset() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Status> getStatuses(CrawlTask task) throws TwitterException {
        Paging paging = new Paging(1, PAGE_SIZE);
        List<Status> statuses;

        switch (task.getType()) {
            case TWEETS:
                statuses = twitter.getUserTimeline(task.getUserId(), paging);
                break;
            case RETWEETS:
                statuses = twitter.getRetweets(task.getUserId());
                break;
            case FAVORITES:
                statuses = twitter.getFavorites(task.getUserId(), paging);
                break;
            default:
                throw new IllegalArgumentException("Tried to crawl unknown interaction type" + task.getType());
        }

        crawledUsers.add(task.getUserId());
        return statuses;
    }

    private List<Interaction> getInteractions(CrawlTask task, List<Status> statuses) {
        return statuses.stream()
                .map(status -> {
                    final long userId = task.getUserId();
                    final long tweetId = status.getId();
                    final InteractionType type = task.getType();
                    return new Interaction(userId, tweetId, type);
                }).collect(Collectors.toList());
    }

    private Set<CrawlTask> getCrawlTasks(List<Status> statuses) {
        return statuses.stream()
                .map(status -> status.getUser().getId())
                .filter(userId -> !crawledUsers.contains(userId))
                .flatMap(userId -> Stream.of(
                        new CrawlTask(userId, InteractionType.TWEETS),
                        new CrawlTask(userId, InteractionType.FAVORITES)
                ))
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
