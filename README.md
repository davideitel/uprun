# Stock Up-run Finder

This project contains a simple command line tool to find the longest "up-run" of a given stock
for a provided date range. An up-run is defined as a set of continuous business days on which
a stock either increases in opening price or remains the same as the previous day.

    usage: uprun -t [stock symbol] -s [MM-dd-YYYY] -e [MM-dd-YYYY]
    Options:
        -t <arg>   Ticker symbol of a stock (e.g. MSFT).
        -s <arg>   Start date (inclusive).
        -e <arg>   End date (inclusive).
        -help      Print this message.

Example:

    uprun -t MSFT -s "12-3-2013" -e "11-12-2014"

## Requirements

 - Java VM >= 1.7.0
 - Gradle  2.2.1 (or use the gradlew included in the project)

## Build

    gradlew standalone

The generated jar can be found in:

    build/libs/uprun.jar 

## Tests

Use the following command to run the tests:
    
    gradlew test
 
## License

Apache License, Version 2.0 (current)
http://www.apache.org/licenses/LICENSE-2.0
