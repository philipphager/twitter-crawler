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

public class Crawler {
    private final ConfigurationManager configurationManager;

    public Crawler(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void crawl(String seedUserName, File outputPath) throws IOException, TwitterException, InterruptedException {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final List<Configuration> twitterConfigurations = configurationManager.loadConfigurations();
        final ConcurrentLinkedQueue<CrawlTask> taskQueue = new ConcurrentLinkedQueue<>();
        final Set<CrawlTask> crawledTasks = new HashSet<>();

        outputPath.mkdirs();

        for (int i = 0; i < twitterConfigurations.size(); i++) {
            final Twitter twitter = new TwitterFactory(twitterConfigurations.get(i)).getInstance();

            if (i == 0) {
                final User user = twitter.showUser(seedUserName);
                taskQueue.add(new TimelineCrawlTask(user.getId()));
            }

            final String name = "task-" + i;
            final File file = new File(outputPath, String.format("tweets-%d.csv", i));
            final CrawlerProcess crawlerProcess = new CrawlerProcess(twitter, taskQueue, crawledTasks, file, name);
            executorService.execute(crawlerProcess);
        }

        // All threads have finished or time has run out.
        executorService.awaitTermination(7, TimeUnit.DAYS);
    }
}
