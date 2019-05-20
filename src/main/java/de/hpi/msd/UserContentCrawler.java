package de.hpi.msd;

import de.hpi.msd.model.Interaction;
import de.hpi.msd.model.InteractionType;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Crawls all available information for a given user:
 * Tweets, favorites and retweets...
 */
public class UserContentCrawler {
    private final Twitter twitter;
    private final Writer writer;

    public UserContentCrawler(Twitter twitter, Writer writer) {
        this.twitter = twitter;
        this.writer = writer;
    }

    public Set<Long> crawl(final long userId) throws IOException, TwitterException {
        final Set<Long> candidates = new HashSet<>();

        // Crawl all tweets of user and their interactions.
        ResponseList<Status> tweets = twitter.getUserTimeline(userId);
        candidates.addAll(parse(userId, tweets, InteractionType.TWEETS));

        // ResponseList<Status> retweets = twitter.getRetweets(userId);
        // parse(userId, retweets, InteractionType.RETWEETS);

        ResponseList<Status> favorites = twitter.getFavorites(userId);
        candidates.addAll(parse(userId, favorites, InteractionType.FAVORITES));

        // Other users from friend
        return candidates;
    }

    private Set<Long> parse(final long userId, final List<Status> statuses, final InteractionType type) throws IOException {
        List<Interaction> interactions = new ArrayList<>(statuses.size());
        Set<Long> candidates = new HashSet<>();

        for (Status status : statuses) {
            interactions.add(new Interaction(userId, status.getId(), type));
            candidates.add(status.getUser().getId());
        }

        write(interactions);
        return candidates;
    }

    private void write(List<Interaction> interactions) throws IOException {
        for (Interaction interaction : interactions) {
            writer.append(String.valueOf(interaction.getTweetId()))
                    .append(", ")
                    .append(String.valueOf(interaction.getUserId()))
                    .append(", ")
                    .append(String.valueOf(interaction.getType().getEdgeType()))
                    .append("\n");
        }
        writer.flush();
    }
}
