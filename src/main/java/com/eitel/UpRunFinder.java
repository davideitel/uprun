package com.eitel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

class UpRunFinder {

    @VisibleForTesting
    static final String HEADER_DATE = "Date";
    @VisibleForTesting
    static final String HEADER_PRICE = "Open"; // Use the opening price

    public UpRunFinder() {
    }

    public UpRunResult findUpRun(String stockSymbol, DateTime start, DateTime end) {
        if (start.isAfter(end) || start.equals(end)) {
            throw new IllegalArgumentException();
        }

        try (Reader reader = retrieveStockData(stockSymbol, start, end)) {
            Iterable<StockData> stockData = parseStockData(reader);
            return processStockData(stockData);
        } catch (MalformedURLException e) {
            Throwables.propagate(e);
        } catch (FileNotFoundException e) {
            // This is only encountered when the stock symbol is invalid/unknown.
            throw new InvalidStockSymbolException(e);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        // Should never be reached.
        return null;
    }


    private Iterable<StockData> parseStockData(Reader reader) throws IOException {
        final Iterator<CSVRecord> iterator = CSVFormat.DEFAULT.withHeader().parse(reader).iterator();
        // A custom iterator is created to adapt the data to help with testing and hide the underlying CSV processing.
        return new Iterable<StockData>() {
            @Override
            public Iterator<StockData> iterator() {
                return new Iterator<StockData>() {

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public StockData next() {
                        CSVRecord record = iterator.next();
                        return new StockData(record.get(HEADER_DATE), new BigDecimal(record.get(HEADER_PRICE)));
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    /**
     * Retrieve stock data from Yahoo's historical stock price API in reverse chronological order (there does not seem to be a good way to reverse this).
     * @param stockSymbol
     * @param startDate
     * @param endDate
     */
    private Reader retrieveStockData(String stockSymbol, DateTime startDate, DateTime endDate) throws MalformedURLException, IOException {
        /*
         * We are using Yahoo's historical stock price API which uses the following URL structure:
         *
         * http://ichart.finance.yahoo.com/table.csv?
         *
         * The following options are added to the URL to retrieve data for a given stock:
         *
         * s: stock symbol
         * a: start month (January is 0)
         * b: start day (First day of the month is 1)
         * c: start year
         * d: end month (January is 0)
         * e: end day (First day of the month is 1)
         * f: end year
         *
         * Note that the data is retrieved in reverse chronological order.
         * There does NOT appear to be a away to reverse this.
         */
        String url = String.format("http://ichart.finance.yahoo.com/table.csv?s=%s&a=%s&b=%s&c=%s&d=%s&e=%s&f=%s&g=d&ignore=.csv",
            stockSymbol,
            startDate.getMonthOfYear() - 1,
            startDate.getDayOfMonth(),
            startDate.getYear(),
            endDate.getMonthOfYear() - 1,
            endDate.getDayOfMonth(),
            endDate.getYear()
        );
        return new InputStreamReader(new URL(url).openStream());
    }

    @VisibleForTesting
    UpRunResult processStockData(Iterable<StockData> stockData) {
        String startDate = null;
        BigDecimal startPrice = null;

        String bestUpRunStart = null;
        String bestUpRunEnd = null;

        double percentChange = 0;

        int duration = 1; // num business days
        int maxDuration = 1;

        BigDecimal previousPrice = null;
        String previousDate = null;

        for (StockData record : stockData) {

            String date = record.getDate();
            BigDecimal currentPrice = record.getPrice();

            if (startDate == null) {
                // Only happens for first record
                startDate = date;
                startPrice = currentPrice;

            } else {

                if (currentPrice.compareTo(previousPrice) <= 0) {
                    // Continuing up-run
                    duration++;
                } else {
                    // Up-run ended
                    if (duration > maxDuration) {
                        maxDuration = duration;
                        bestUpRunStart = startDate;
                        bestUpRunEnd = previousDate;
                        percentChange = computePercentChange(startPrice, previousPrice);
                    }
                    duration = 1;
                    startDate = date;
                    startPrice = currentPrice;
                }
            }
            previousPrice = currentPrice;
            previousDate = date;
        }

        /*
         * Handle the case when the entire time range is an up-run
         * and when the last "run" is the longest.
         */
        if (bestUpRunStart == null || duration > maxDuration) {
            bestUpRunStart = startDate;
            bestUpRunEnd= previousDate;
            maxDuration = duration;
            percentChange = computePercentChange(startPrice, previousPrice);
        }

        UpRunResult result = new UpRunResult();
        result.duration = maxDuration;
        // Note that start/end are reversed because the data is retrieved in reversed order.
        result.startDate = bestUpRunEnd;
        result.endDate = bestUpRunStart;
        result.percentChange = percentChange;
        return result;
    }

    private double computePercentChange(BigDecimal startPrice, BigDecimal endPrice) {
        return startPrice.subtract(endPrice).divide(endPrice, 2, RoundingMode.HALF_UP).abs().doubleValue() * 100.0;
    }

    class UpRunResult {
        protected String startDate;
        protected String endDate;
        protected int duration;
        protected double percentChange;
    }

}
