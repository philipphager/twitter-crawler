## twitter-crawler
This project is a crawler used to create a dataset for tweet interactions (e.g., user -retweets-> tweet). The crawler takes multiple twitter4j credential files to crawl with multiple processes in parallel. The crawler will automatically pause when the rate limit is exceeded and resume when it is allowed to crawl the Twitter API again. All crawled interactions will be saved to CSV files in the output directory. Each crawl process produces its own output file to avoid write conflicts during crawling. Therefore, you have to merge all crawled CSV files after stopping the crawl, e.g., using:

```bash
cd output/
cat tweets-0.csv tweets-1.csv tweets-2.csv > tweets.csv
``` 

### Build & Run Project
The project can be built with Maven. To create an executable fat JAR (containing all dependencies), run:
```bash
mvn package
```

Execute the JAR:
```bash
java -jar target/twitter-crawler-1.0-SNAPSHOT.jar --help
```

### Parameters
| Parameter        | Required | Description                        | Default |
| ---------------- | -------- | ---------------------------------- | ------- |
| --configDir, -c  | no       | Directory with twitter4j credential files. Multiple files will be used to crawl in parallel. | ./configs/ |
| --outputDir, -o  | no       | Directory to export the crawled tweet interactions as CSV files. One file per config file is created. | ./output/ |
| --seed, -s       | no       | Name of user to start the crawl. | neiltyson (Neil deGrasse Tyson) |
