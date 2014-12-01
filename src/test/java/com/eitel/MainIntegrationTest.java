package com.eitel;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class MainIntegrationTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private final ByteArrayOutputStream standardOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream standardError = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(standardOut));
        System.setErr(new PrintStream(standardError));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }

    @Test
    public void testWithProperInputs() {
        String[] args = {"-t", "MSFT", "-s", "12-3-1996", "-e", "11-12-2014"};
        Main.main(args);
        assertTrue(standardOut.toString().contains("Start: 2009-06-03"));
        assertTrue(standardOut.toString().contains("End: 2009-06-19"));
        assertTrue(standardOut.toString().contains("Duration: 13 business days"));
        assertTrue(standardOut.toString().contains("Percent Gain: 13.0%"));
        assertTrue(standardError.toString().isEmpty());
    }

    @Test
    public void testWithInvalidDateFormat() {
        exit.expectSystemExitWithStatus(1);
        String[] args = {"-t", "MSFT", "-s", "12/3/1996", "-e", "11/12/2014"};
        Main.main(args);
        assertTrue(standardError.toString().contains("Incorrectly formatted input provided."));
        assertTrue(standardOut.toString().isEmpty());
    }

    @Test
    public void testWithInvalidStock() {
        exit.expectSystemExitWithStatus(1);
        String[] args = {"-t", "MSFT1234", "-s", "12-3-1996", "-e", "11-12-2014"};
        Main.main(args);
        assertTrue(standardError.toString().contains("Invalid stock symbol provided: MSFT1234"));
        assertTrue(standardOut.toString().isEmpty());
    }

    @Test
    public void testMissingStockSymbol() {
        exit.expectSystemExitWithStatus(1);
        String[] args = {"-s", "12-3-1996", "-e", "11-12-2014"};
        Main.main(args);
        assertTrue(standardError.toString().contains("Missing ticker symbol"));
        assertTrue(standardOut.toString().isEmpty());
    }

    @Test
    public void testMissingStartDate() {
        exit.expectSystemExitWithStatus(1);
        String[] args = {"-t", "MSFT", "-e", "11-12-2014"};
        Main.main(args);
        assertTrue(standardError.toString().contains("Missing start date"));
        assertTrue(standardOut.toString().isEmpty());
    }

    @Test
    public void testMissingEndDate() {
        exit.expectSystemExitWithStatus(1);
        String[] args = {"-t", "MSFT", "-s", "12-3-1996"};
        Main.main(args);
        assertTrue(standardError.toString().contains("Missing end date"));
        assertTrue(standardOut.toString().isEmpty());
    }

    @Test
    public void testHelp() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {"-h"};
        Main.main(args);
        assertTrue(standardOut.toString().contains("usage: uprun"));
        assertTrue(standardError.toString().isEmpty());
    }

    @Test
    public void testHelpWhenNoArgumentsAreProvided() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {};
        Main.main(args);
        assertTrue(standardOut.toString().contains("usage: uprun"));
        assertTrue(standardError.toString().isEmpty());
    }

}
