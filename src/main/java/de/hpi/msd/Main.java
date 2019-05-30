package de.hpi.msd;

import de.hpi.msd.model.task.CrawlTask;
import de.hpi.msd.model.task.TimelineCrawlTask;
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
    // TODO: Add proper logger
    // Exponential growth?

    public static void main(String[] args) throws TwitterException, InterruptedException, IOException {
        assert args.length == 1 : "Please specify seed twitter user name to start crawling";

        final File configurationPath = new File("./configs");
        final File outputPath = new File("./output");
        final ConfigurationManager configurationManager = new ConfigurationManager(configurationPath);
        final List<Configuration> configurations = configurationManager.loadConfigurations();

        final Set<CrawlTask> crawledUsers = new HashSet<>();
        final ConcurrentLinkedQueue<CrawlTask> crawlQueue = new ConcurrentLinkedQueue<>();
        final ExecutorService executorService = Executors.newCachedThreadPool();

        outputPath.mkdirs();

        for (int i = 0; i < configurations.size(); i++) {
            final Twitter twitter = new TwitterFactory(configurations.get(i)).getInstance();

            if (i == 0) {
                final User user = twitter.showUser(args[0]);
                crawlQueue.add(new TimelineCrawlTask(user.getId()));
            }

            final String name = "task-" + i;
            final File file = new File(outputPath, String.format("tweets-%d.csv", i));
            final CrawlerProcess crawlerProcess = new CrawlerProcess(twitter, crawlQueue, crawledUsers, file, name);
            executorService.execute(crawlerProcess);
        }

        // All threads have finished or time has run out.
        executorService.awaitTermination(10, TimeUnit.HOURS);
    }
}
