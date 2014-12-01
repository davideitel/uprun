package com.eitel;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.eitel.UpRunFinder.UpRunResult;
import com.google.common.collect.Lists;

public class UpRunFinderTest {

    private UpRunFinder upRunFinder;

    @Before
    public void before() {
        this.upRunFinder = new UpRunFinder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSameDatesProvided() {
        DateTime dateTime = DateTime.now();
        this.upRunFinder.findUpRun("MSFT", dateTime, dateTime);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongDateOrder() {
        DateTime start = DateTime.now();
        DateTime end = DateTime.now().minusDays(2);
        this.upRunFinder.findUpRun("MSFT", start, end);
    }

    @Test
    public void testNoUpRuns() {
        List<StockData> data = Lists.newArrayList();
        data.add(new StockData("01-12-2014", "20.00"));
        data.add(new StockData("01-11-2014", "21.00"));
        data.add(new StockData("01-10-2014", "22.00"));

        UpRunResult result = this.upRunFinder.processStockData(data);
        assertEquals(1, result.duration);
        assertEquals("01-10-2014", result.startDate);
        assertEquals("01-10-2014", result.endDate);
        assertEquals(0, result.percentChange, 0.1);
    }

    @Test
    public void testEntireRangeIsUpRun() {
        List<StockData> data = Lists.newArrayList();
        data.add(new StockData("01-12-2014", "30.00"));
        data.add(new StockData("01-11-2014", "25.00"));
        data.add(new StockData("01-10-2014", "20.00"));

        UpRunResult result = this.upRunFinder.processStockData(data);
        assertEquals(3, result.duration);
        assertEquals("01-10-2014", result.startDate);
        assertEquals("01-12-2014", result.endDate);
        assertEquals(50.0, result.percentChange, 0.1);
    }

    @Test
    public void testStartAtUpRun() {
        List<StockData> data = Lists.newArrayList();
        data.add(new StockData("01-13-2014", "5.00"));
        data.add(new StockData("01-12-2014", "30.00"));
        data.add(new StockData("01-11-2014", "25.00"));
        data.add(new StockData("01-10-2014", "20.00"));

        UpRunResult result = this.upRunFinder.processStockData(data);
        assertEquals(3, result.duration);
        assertEquals("01-10-2014", result.startDate);
        assertEquals("01-12-2014", result.endDate);
        assertEquals(50.0, result.percentChange, 0.1);
    }

    @Test
    public void testEndAtUpRun() {
        List<StockData> data = Lists.newArrayList();
        data.add(new StockData("01-13-2014", "40.00"));
        data.add(new StockData("01-12-2014", "30.00"));
        data.add(new StockData("01-11-2014", "20.00"));
        data.add(new StockData("01-10-2014", "50.00"));

        UpRunResult result = this.upRunFinder.processStockData(data);
        assertEquals(3, result.duration);
        assertEquals("01-11-2014", result.startDate);
        assertEquals("01-13-2014", result.endDate);
        assertEquals(100.0, result.percentChange, 0.1);
    }

    /**
     * Expect the last "longest" up-run to be outputted.
     */
    @Test
    public void testMultipleUpRunsOfSameDuration() {
        List<StockData> data = Lists.newArrayList();
        data.add(new StockData("01-16-2014", "40.00"));
        data.add(new StockData("01-15-2014", "30.00"));
        data.add(new StockData("01-14-2014", "20.00"));
        data.add(new StockData("01-13-2014", "40.00"));
        data.add(new StockData("01-12-2014", "30.00"));
        data.add(new StockData("01-11-2014", "20.00"));
        data.add(new StockData("01-10-2014", "50.00"));

        UpRunResult result = this.upRunFinder.processStockData(data);
        assertEquals(3, result.duration);
        assertEquals("01-14-2014", result.startDate);
        assertEquals("01-16-2014", result.endDate);
        assertEquals(100.0, result.percentChange, 0.1);
    }

    @Test
    public void testMultipleUpRunsOfDifferentDurations() {
        List<StockData> data = Lists.newArrayList();
        data.add(new StockData("01-16-2014", "40.00"));
        data.add(new StockData("01-15-2014", "30.00"));
        data.add(new StockData("01-14-2014", "20.00"));
        data.add(new StockData("01-13-2014", "40.00"));
        data.add(new StockData("01-12-2014", "30.00"));
        data.add(new StockData("01-11-2014", "20.00"));
        data.add(new StockData("01-10-2014", "10.00"));

        UpRunResult result = this.upRunFinder.processStockData(data);
        assertEquals(4, result.duration);
        assertEquals("01-10-2014", result.startDate);
        assertEquals("01-13-2014", result.endDate);
        assertEquals(300.0, result.percentChange, 0.1);
    }

    @Test
    public void testDateSkipping() {
        List<StockData> data = Lists.newArrayList();
        data.add(new StockData("01-17-2014", "35.00"));
        data.add(new StockData("01-16-2014", "40.00"));
        data.add(new StockData("01-11-2014", "20.00"));
        data.add(new StockData("01-10-2014", "50.00"));

        UpRunResult result = this.upRunFinder.processStockData(data);
        assertEquals(2, result.duration);
        assertEquals("01-11-2014", result.startDate);
        assertEquals("01-16-2014", result.endDate);
        assertEquals(100.0, result.percentChange, 0.1);
    }

    @Test
    public void testDaysWithSamePrices() {
        List<StockData> data = Lists.newArrayList();
        data.add(new StockData("01-14-2014", "35.00"));
        data.add(new StockData("01-13-2014", "40.00"));
        data.add(new StockData("01-12-2014", "20.00"));
        data.add(new StockData("01-11-2014", "20.00"));
        data.add(new StockData("01-10-2014", "50.00"));

        UpRunResult result = this.upRunFinder.processStockData(data);
        assertEquals(3, result.duration);
        assertEquals("01-11-2014", result.startDate);
        assertEquals("01-13-2014", result.endDate);
        assertEquals(100.0, result.percentChange, 0.1);
    }

    @Test
    public void testOnlyDaysWithSamePrices() {
        List<StockData> data = Lists.newArrayList();
        data.add(new StockData("01-15-2014", "5.00"));
        data.add(new StockData("01-14-2014", "20.00"));
        data.add(new StockData("01-13-2014", "20.00"));
        data.add(new StockData("01-12-2014", "20.00"));
        data.add(new StockData("01-11-2014", "20.00"));
        data.add(new StockData("01-10-2014", "50.00"));

        UpRunResult result = this.upRunFinder.processStockData(data);
        assertEquals(4, result.duration);
        assertEquals("01-11-2014", result.startDate);
        assertEquals("01-14-2014", result.endDate);
        assertEquals(0, result.percentChange, 0.1);
    }
}
