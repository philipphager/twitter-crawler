package de.hpi.msd.mocks;

import twitter4j.IDs;
import twitter4j.RateLimitStatus;
import twitter4j.Status;

import java.util.List;

public class MockIDs implements IDs {
    private final List<Status> statuses;

    public static IDs asUserIds(List<Status> statuses) {
        return new MockIDs(statuses);
    }

    private MockIDs(List<Status> statuses) {
        this.statuses = statuses;
    }

    @Override
    public long[] getIDs() {
        return statuses.stream().mapToLong(l -> l.getUser().getId()).toArray();
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public long getPreviousCursor() {
        return 0;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public long getNextCursor() {
        return 0;
    }

    @Override
    public RateLimitStatus getRateLimitStatus() {
        return null;
    }

    @Override
    public int getAccessLevel() {
        return 0;
    }
}
