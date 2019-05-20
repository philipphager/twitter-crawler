package de.hpi.msd;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class ConfigurationManager {
    private final File path;

    public ConfigurationManager(File path) {
        this.path = path;
    }

    public List<Configuration> loadConfigurations() throws IOException {
        final List<Configuration> configurations = new ArrayList<>();
        final File[] files = path.listFiles((dir, name) -> name.endsWith(".properties"));
        Objects.requireNonNull(files, "No configuration files found at: " + path);

        for (File file : files) {
            try (InputStream input = new FileInputStream(file)) {
                final Properties properties = new Properties();
                properties.load(input);

                final String consumerKey = properties.getProperty("oauth.consumerKey");
                final String consumerSecret = properties.getProperty("oauth.consumerSecret");
                final String accessToken = properties.getProperty("oauth.accessToken");
                final String accessTokenSecret = properties.getProperty("oauth.accessTokenSecret");

                final Configuration configuration = new ConfigurationBuilder()
                        .setDebugEnabled(true)
                        .setOAuthConsumerKey(consumerKey)
                        .setOAuthConsumerSecret(consumerSecret)
                        .setOAuthAccessToken(accessToken)
                        .setOAuthAccessTokenSecret(accessTokenSecret)
                        .build();
                configurations.add(configuration);
            }
        }

        return configurations;
    }
}
