package de.hpi.msd.mocks;

import twitter4j.Status;
import twitter4j.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MockStatusResponses {
    public static final User MOCK_USER_TRUMP = new MockUser(25073877);
    public static final User MOCK_USER_TYSON = new MockUser(19725644);
    public static final User MOCK_USER_NYE = new MockUser(37710752);

    public static final List<Status> EMPTY_STATUS = Collections.emptyList();

    public static final List<Status> SINGLE_TRUMP_TWEET = Collections.singletonList(
            new MockStatus(10001230, MOCK_USER_TRUMP));

    public static final List<Status> SINGLE_TYSON_RETWEET = Collections.singletonList(
            new MockStatus(10001230, MOCK_USER_TYSON, new MockStatus(10005670, MOCK_USER_TRUMP)));

    public static final List<Status> NYE_TWEET_AND_RETWEET = Arrays.asList(
            new MockStatus(1000978, MOCK_USER_NYE),
            new MockStatus(1000765, MOCK_USER_NYE, new MockStatus(10001230, MOCK_USER_TYSON)));

    public static final List<Status> TRUMP_RETWEETS = Arrays.asList(
            new MockStatus(10001230, MOCK_USER_TYSON, new MockStatus(10001230, MOCK_USER_TRUMP)),
            new MockStatus(10008593, MOCK_USER_NYE, new MockStatus(10001230, MOCK_USER_TRUMP)));

    public static final List<Status> HUNDRED_TRUMP_RETWEETS = Collections.nCopies(100,
            new MockStatus(10008593, MOCK_USER_NYE, new MockStatus(10001230, MOCK_USER_TRUMP)));
}
