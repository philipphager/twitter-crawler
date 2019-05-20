package de.hpi.msd;

import de.hpi.msd.model.CrawlTask;
import de.hpi.msd.model.CrawlerProcess;
import de.hpi.msd.model.InteractionType;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    // TODO: Pagination
    // TODO: Crawl tweet users

    public static void main(String[] args) throws TwitterException, InterruptedException, IOException {
        final File configurationPath = new File("./configs");
        final ConfigurationManager configurationManager = new ConfigurationManager(configurationPath);
        final List<Configuration> configurations = configurationManager.loadConfigurations();

        final Set<Long> crawledUsers = new HashSet<>();
        final ConcurrentLinkedQueue<CrawlTask> crawlQueue = new ConcurrentLinkedQueue<>();
        final ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < configurations.size(); i++) {
            final Twitter twitter = new TwitterFactory(configurations.get(i)).getInstance();

            if (i == 0) {
                // FIXME: Add a seed user
                final User user = twitter.showUser("realdonaldtrump");
                final long seedUser = user.getId();
                crawlQueue.add(new CrawlTask(seedUser, InteractionType.TWEETS));
                crawlQueue.add(new CrawlTask(seedUser, InteractionType.FAVORITES));
            }

            final File file = new File(String.format("tweets-%d.csv", i));
            final CrawlerProcess crawlerProcess = new CrawlerProcess(twitter, crawlQueue, crawledUsers, file, "crawler-" + i);
            executorService.execute(crawlerProcess);
        }

        // All threads have finished or time has run out.
        executorService.awaitTermination(10, TimeUnit.HOURS);
    }
}
