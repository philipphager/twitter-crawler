
# Twitter Crawler
## Abstract
This repository contains code to crawl the Twitter API to create a dataset for the [kafka-salsa](https://github.com/torbsto/kafka-salsa) project. Kafka-salsa is an in-memory, graph-based tweet recommender system implemented on [Kafka-Streams](https://kafka.apache.org/documentation/streams/), which uses an interaction graph (e.g., likes, writes, retweets) between users and tweets to recommend new tweets to a given user. This repository contains the crawler we use to create a test dataset of user-tweet-interactions. Starting from a user (the seed user), we crawl interacted (e.g., liked) tweets and add other users that also engaged with those tweets to a queue to be crawled next. The result is a bipartite graph dataset in CSV form (`user_id, tweet_id, interaction`). Our implementation enables crawling with multiple user accounts in parallel, creating one thread per user account. Each thread produces an output file to avoid write conflicts during crawling. You need to merge the resulting CSV files after the crawler has finished (or was shutdown). Find the full documentation in the central repository [kafka-salsa](https://github.com/torbsto/kafka-salsa). We uploaded our evaluation dataset that we created using this crawler to [twitter-dataset](https://github.com/philipphager/twitter-dataset).

## Repository Overview
This repository is part of a larger project. Here is a list of all related repositories:
* [kafka-salsa](https://github.com/torbsto/kafka-salsa): Reference implementation and project documentation.
* [kafka-salsa-evaluation](https://github.com/philipphager/kafka-salsa-evaluation): Evaluation suite for [kafka-salsa](https://github.com/torbsto/kafka-salsa).
* [twitter-cralwer](https://github.com/philipphager/twitter-crawler): Twitter API crawler for user-tweet-interaction data.
* [twitter-dataset](https://github.com/philipphager/twitter-dataset): Crawled datasets of user-tweet-interactions used in evaluation.

## Installation
1. Clone the repository: `git clone git@github.com:philipphager/twitter-crawler.git`
2. Install [Apache Maven](https://maven.apache.org/install.html).
3. Navigate into the repository: `cd ./twitter-crawler/`
4. Get credentials for the [Twitter API](https://developer.twitter.com/).
5. Save the credentials as a Twitter4j `*.properties` file inside the `./configs/` directory. An example file can be found [here](http://twitter4j.org/en/configuration.html).
6. Build the project: `mvn package`
7. Run the crawler and specify a seed user: `java -jar target/twitter-crawler-1.0-SNAPSHOT.jar --seed neiltyson`
8. Stop the crawler anytime you want to. The resulting CSV files are inside the `./output/` directory and can be merged into one file e.g., using: `cat tweets-0.csv tweets-1.csv tweets-2.csv > tweets.csv` 

## Crawler Parameters
| Parameter        | Required | Description                        | Default |
| ---------------- | -------- | ---------------------------------- | ------- |
| --configDir, -c  | no       | Directory with twitter4j credential files. Multiple files will be used to crawl in parallel. | ./configs/ |
| --outputDir, -o  | no       | Directory to export the crawled tweet interactions as CSV files. One output file per config file is created. | ./output/ |
| --seed, -s       | no       | Name of user to start the crawl. | neiltyson (Neil deGrasse Tyson) |
