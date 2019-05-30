package de.hpi.msd;

import de.hpi.msd.model.interaction.InteractionType;
import de.hpi.msd.model.task.CrawlTask;
import de.hpi.msd.model.task.RetweetCrawlTask;
import de.hpi.msd.model.task.TimelineCrawlTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import twitter4j.*;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static de.hpi.msd.mocks.MockIDs.asUserIds;
import static de.hpi.msd.mocks.MockResponseList.asResponse;
import static de.hpi.msd.mocks.MockStatusResponses.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawlerProcessTest {

    @Mock
    Twitter twitter;

    String seedUser;
    Queue<CrawlTask> crawlTasks;
    Set<CrawlTask> crawledTasks;
    CrawlerProcess crawlerProcess;
    BufferedReader reader;
    File outputFile;

    @Before
    public void setUp() throws Exception {
        outputFile = new File("./tmp.csv");
        seedUser = "neilTyson";
        crawlTasks = new ConcurrentLinkedQueue<>();
        crawledTasks = new HashSet<>();

        boolean ignored = outputFile.createNewFile();
        reader = new BufferedReader(new FileReader(outputFile));

        crawlerProcess = new CrawlerProcess(twitter, crawlTasks, crawledTasks, new FileWriter(outputFile), "crawler-1");
    }

    @After
    public void tearDown() throws Exception {
        boolean ignored = outputFile.delete();
    }

    @Test
    public void crawlEmptyUserTimeline() throws Exception {
        givenUserTweets(EMPTY_STATUS);

        crawlTasks.add(new TimelineCrawlTask(MOCK_USER_TRUMP.getId()));

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(crawlerProcess);

        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);

        Mockito.verify(twitter).getUserTimeline(anyLong(), any(Paging.class));
    }

    @Test
    public void crawlUserTimelineAndSaveInteraction() throws Exception {
        givenUserTweets(SINGLE_TRUMP_TWEET);
        givenRetweeters(EMPTY_STATUS);

        crawlTasks.add(new TimelineCrawlTask(MOCK_USER_TRUMP.getId()));

        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(crawlerProcess);

        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);

        verifyFileWrites(MOCK_USER_TRUMP, SINGLE_TRUMP_TWEET.get(0), InteractionType.TWEETS);
    }

    @Test
    public void crawlRetweetFromTimelineAndSaveInteractions() throws Exception {
        givenUserTweets(SINGLE_TYSON_RETWEET);
        givenRetweeters(EMPTY_STATUS);
        final Status trumpTweet = SINGLE_TYSON_RETWEET.get(0).getRetweetedStatus();

        crawlTasks.add(new TimelineCrawlTask(MOCK_USER_TYSON.getId()));

        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(crawlerProcess);

        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);

        verifyFileWrites(MOCK_USER_TYSON, trumpTweet, InteractionType.RETWEETS);
        verifyFileWrites(MOCK_USER_TRUMP, trumpTweet, InteractionType.TWEETS);
    }

    @Test
    public void crawlTweetAndRetweetFromUserTimelineAndSaveInteractions() throws Exception {
        givenUserTweets(NYE_TWEET_AND_RETWEET);
        givenRetweeters(EMPTY_STATUS);

        crawlTasks.add(new TimelineCrawlTask(MOCK_USER_NYE.getId()));

        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(crawlerProcess);

        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);

        verifyFileWrites(MOCK_USER_NYE, NYE_TWEET_AND_RETWEET.get(0), InteractionType.TWEETS);
        verifyFileWrites(MOCK_USER_NYE, NYE_TWEET_AND_RETWEET.get(1).getRetweetedStatus(), InteractionType.RETWEETS);
        verifyFileWrites(MOCK_USER_TYSON, NYE_TWEET_AND_RETWEET.get(1).getRetweetedStatus(), InteractionType.TWEETS);
    }

    @Test
    public void crawlRetweetersAndSaveInteractions() throws Exception {
        givenUserTweets(SINGLE_TRUMP_TWEET);
        givenRetweeters(TRUMP_RETWEETS);
        final Status trumpTweet = SINGLE_TRUMP_TWEET.get(0);

        crawlTasks.add(new RetweetCrawlTask(trumpTweet.getId(), 0, 100));

        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(crawlerProcess);

        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);

        verifyFileWrites(MOCK_USER_TYSON, trumpTweet, InteractionType.RETWEETS);
        verifyFileWrites(MOCK_USER_NYE, trumpTweet, InteractionType.RETWEETS);
    }

    private void givenUserTweets(List<Status> statuses) throws TwitterException {
        when(twitter.getUserTimeline(anyLong(), any(Paging.class))).thenReturn(asResponse(statuses));
    }

    private void givenRetweeters(List<Status> statuses) throws TwitterException {
        when(twitter.getRetweeterIds(anyLong(), anyInt(), anyLong())).thenReturn(asUserIds(statuses));
    }

    private void verifyFileWrites(User user, Status status, InteractionType type) throws IOException {
        String[] line = reader.readLine().split(", ");
        assertThat(line).containsExactly(
                String.valueOf(user.getId()),
                String.valueOf(status.getId()),
                String.valueOf(type.getEdgeType()));
    }
}
