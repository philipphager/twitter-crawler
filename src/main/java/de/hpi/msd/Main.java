package de.hpi.msd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;

public class Main {
    @Parameter(names = {"--seed", "-s"},
            description = "Name of twitter user to start the crawl.")
    String seed = "neiltyson";

    @Parameter(names = {"--configDir", "-c"},
            description = "Configuration directory containing twitter4j.property files.",
            converter = FileConverter.class)
    File configDir = new File("./configs");

    @Parameter(names = {"--outDir", "-o"},
            description = "Output directory for crawled tweets.",
            converter = FileConverter.class)
    File outDir = new File("./output");

    @Parameter(names = "--help", help = true)
    private boolean help;

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        ConfigurationManager configurationManager = new ConfigurationManager(main.configDir);
        Crawler crawler = new Crawler(configurationManager);
        crawler.crawl(main.seed, main.outDir);
    }
}
