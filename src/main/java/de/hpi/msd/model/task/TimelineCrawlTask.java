package de.hpi.msd.model.task;

import java.util.Objects;

public class TimelineCrawlTask extends CrawlTask {
    private final long userId;

    public TimelineCrawlTask(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimelineCrawlTask that = (TimelineCrawlTask) o;
        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "TimelineCrawlTask{" +
                "userId=" + userId +
                '}';
    }
}
