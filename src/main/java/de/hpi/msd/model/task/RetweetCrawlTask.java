package de.hpi.msd.model.task;

import java.util.Objects;

public class RetweetCrawlTask extends CrawlTask {
    private final long tweetId;
    private final long skip;
    private final int limit;

    public RetweetCrawlTask(long tweetId, long skip, int limit) {
        this.tweetId = tweetId;
        this.skip = skip;
        this.limit = limit;
    }

    public long getTweetId() {
        return tweetId;
    }

    public long getSkip() {
        return skip;
    }

    public int getLimit() {
        return limit;
    }

    public RetweetCrawlTask withSkip(long skip) {
        return new RetweetCrawlTask(this.tweetId, skip, this.limit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetweetCrawlTask that = (RetweetCrawlTask) o;
        return tweetId == that.tweetId &&
                skip == that.skip &&
                limit == that.limit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tweetId, skip, limit);
    }

    @Override
    public String toString() {
        return "RetweetCrawlTask{" +
                "tweetId='" + tweetId + '\'' +
                ", skip=" + skip +
                ", limit=" + limit +
                '}';
    }
}
