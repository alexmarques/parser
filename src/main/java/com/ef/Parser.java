package com.ef;

import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;

public class Parser {

    public static void main(String[] args) throws ParseException, IOException {

        Option duration = Option.builder()
                .longOpt("duration")
                .desc("hourly, daily")
                .numberOfArgs(2)
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        Option startDate = Option.builder()
                .longOpt("startDate")
                .desc("yyyy-MM-dd.HH:mm:ss format")
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        Option threshold = Option.builder()
                .longOpt("threshold")
                .required()
                .desc("minimal requests number")
                .hasArg()
                .valueSeparator()
                .build();

        Options options = new Options();
        options.addOption(startDate);
        options.addOption(duration);
        options.addOption(threshold);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        HelpFormatter formatter = new HelpFormatter();

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("java Parser", "", options, e.getMessage(), true);
            System.exit(-1);
        }

        String durationParam = cmd.getOptionValue("duration");

        if(!"hourly".equals(durationParam) && !"daily".equals(durationParam)) {
            formatter.printHelp("java Parser", "", options, "duration argument must be hourly or daily", true);
            System.exit(-1);
        }

        String startDateParam = cmd.getOptionValue("startDate");

        try {
            LocalDateTime.parse(startDateParam, DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss"));
        } catch (DateTimeException e) {
            formatter.printHelp("java Parser", "", options, "startDate must be in format: yyyy-MM-dd.HH:mm:ss", true);
            System.exit(-1);
        }

        TemporalAccessor startDateFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss").parse(startDateParam);
        LocalDateTime startLocalDateTime = LocalDateTime.from(startDateFormatted);
        LocalDateTime endLocalDateTime = null;

        switch (durationParam) {
            case "hourly":
                endLocalDateTime = startLocalDateTime.plusHours(1);
                break;
            case "daily":
                endLocalDateTime = startLocalDateTime.plusDays(1);
                break;
        }

        String thresholdParam = cmd.getOptionValue("threshold");

        try {
            Integer.parseInt(thresholdParam);
        } catch (NumberFormatException e) {
            formatter.printHelp("java Parser", "", options, "threshold must be an integer value", true);
            System.exit(-1);
        }

        InputStream is = Parser.class.getClassLoader().getResourceAsStream("access.log");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Map<String, Integer> requests = new HashMap<>();

        while(reader.ready()) {

            String line = reader.readLine();

            String split[] = line.split("\\|");

            String date = split[0].replace(" ", ".");
            int x = date.lastIndexOf(".");
            String substringDate = date.substring(0, x);
            String ipAddress = split[1];

            TemporalAccessor dateFromFile = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss").parse(substringDate);
            LocalDateTime localDateTimeFromFile = LocalDateTime.from(dateFromFile);


            if(localDateTimeFromFile.isEqual(startLocalDateTime) || localDateTimeFromFile.isAfter(startLocalDateTime)) {
                if(localDateTimeFromFile.isEqual(endLocalDateTime) || localDateTimeFromFile.isBefore(endLocalDateTime)) {
                    int count = requests.getOrDefault(ipAddress, 0);
                    requests.put(ipAddress, ++count);

                }

            }
        }

        requests.forEach((k, v) -> {
            if(v.intValue() > Integer.parseInt(thresholdParam)) {
                System.out.println("IP: " + k + " made " + v.toString() + " requests.");
            }
        });

        reader.close();
        is.close();

    }

    /*
    2017-01-01 00:00:11.763|192.168.234.82|"GET / HTTP/1.1"|200|"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0"
    2017-01-01 00:00:21.164|192.168.234.82|"GET / HTTP/1.1"|200|"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0"
    2017-01-01 00:00:23.003|192.168.169.194|"GET / HTTP/1.1"|200|"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393"
    2017-01-01 00:00:40.554|192.168.234.82|"GET / HTTP/1.1"|200|"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0"
    2017-01-01 00:00:54.583|192.168.169.194|"GET / HTTP/1.1"|200|"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393"
    2017-01-01 00:00:54.639|192.168.234.82|"GET / HTTP/1.1"|200|"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0"
    2017-01-01 00:00:59.410|192.168.169.194|"GET / HTTP/1.1"|200|"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393"
    2017-01-01 00:01:02.113|192.168.247.138|"GET / HTTP/1.1"|200|"Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0"
    2017-01-01 00:01:04.033|192.168.54.139|"GET / HTTP/1.1"|200|"Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.78 Safari/537.36"
    2017-01-01 00:01:04.678|192.168.162.248|"GET / HTTP/1.1"|200|"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0"
     */
}
