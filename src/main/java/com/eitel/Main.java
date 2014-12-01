package com.eitel;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.eitel.UpRunFinder.UpRunResult;
import com.google.common.annotations.VisibleForTesting;

public class Main {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("MM-dd-YYYY");

    private static Options options = new Options();
    static {
        options.addOption("h", "help", false, "Prints this help message");
        options.addOption("t", "stock-symbol", true, "Stock Ticker Symbol");
        options.addOption("s", "start-date", true, "Start date in the format MM-dd-YYYY");
        options.addOption("e", "end-date", true, "End date in the format MM-dd-YYYY");
    }

    private static final CommandLineParser PARSER = new BasicParser();

    public static void main(String[] args) {

        try {
            CommandLine commandLine = PARSER.parse(options, args);
            String stockSymbol = commandLine.getOptionValue("t");
            String startDateStr = commandLine.getOptionValue("s");
            String endDateStr = commandLine.getOptionValue("e");

            if (commandLine.hasOption('h') || commandLine.getOptions().length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("uprun", options);
                System.exit(0);
            }

            if (stockSymbol == null) {
                System.err.println("Missing ticker symbol");
                System.exit(1);
            } else if (startDateStr == null) {
                System.err.println("Missing start date");
                System.exit(1);
            } else if (endDateStr == null) {
                System.err.println("Missing end date");
                System.exit(1);
            }

            DateTime startDate = parseDate(startDateStr);
            DateTime endDate = parseDate(endDateStr);

            UpRunFinder finder = new UpRunFinder();
            try {
                UpRunResult result = finder.findUpRun(stockSymbol, startDate, endDate);
                System.out.println(String.format("Start: %s", result.startDate));
                System.out.println(String.format("End: %s", result.endDate));
                System.out.println(String.format("Duration: %s business days", result.duration));
                System.out.println(String.format("Percent Gain: %s%%", result.percentChange));

            } catch (InvalidStockSymbolException e) {
                System.err.println(String.format("Invalid stock symbol provided: %s", stockSymbol));
                System.exit(1);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid arguments provided.");
                System.exit(1);
            }

        } catch (ParseException|IllegalArgumentException e) {
            System.err.println("Incorrectly formatted input provided.");
            System.exit(1);
        }
    }

    @VisibleForTesting
    static DateTime parseDate(String dateStr) {
        return DATE_FORMAT.parseDateTime(dateStr);
    }
}
