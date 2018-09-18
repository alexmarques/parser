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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    public static final BlockedIpsRepository repository = new BlockedIpsRepository();
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");

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
        LocalDateTime endLocalDateTime = startLocalDateTime.plusHours(1);

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

        Map<String, List<BlockedIpDTO>> requests = new HashMap<>();

        while(reader.ready()) {

            String line = reader.readLine();

            BlockedIpDTO blockedIpDTO = parseLine(line);

            if(blockedIpDTO.getRequestTime().isEqual(startLocalDateTime) || blockedIpDTO.getRequestTime().isAfter(startLocalDateTime)) {
                if(blockedIpDTO.getRequestTime().isEqual(endLocalDateTime) || blockedIpDTO.getRequestTime().isBefore(endLocalDateTime)) {
                    List<BlockedIpDTO> blockedIpList = requests.getOrDefault(blockedIpDTO.getIp(), new ArrayList<>());
                    blockedIpList.add(blockedIpDTO);
                    requests.put(blockedIpDTO.getIp(), blockedIpList);
                }
            }
        }

        LocalDateTime finalEndLocalDateTime = endLocalDateTime;
        requests.forEach((k, v) -> {
            if(v.size() > Integer.parseInt(thresholdParam)) {
                v.forEach(blockedIpDTO -> repository.save(blockedIpDTO));
                String reason = String.format("IP: %s has %s or more requests between %s and %s", k, thresholdParam, startLocalDateTime.toString(), finalEndLocalDateTime.toString());
                //System.out.println("IP: " + k + " made " + v.toString() + " requests.");
                System.out.println(reason);
            }
        });

        reader.close();
        is.close();
        Database.releaseConnection();
    }

    private static BlockedIpDTO parseLine(String line) {
        String split[] = line.split("\\|");

        BlockedIpDTO dto = new BlockedIpDTO();
        dto.setRequestTime(parseRequestedTime(split[0]));
        dto.setIp(split[1]);

        return dto;
    }

    private static LocalDateTime parseRequestedTime(String requestedTime) {
        String date = requestedTime.replace(" ", ".");
        String substringDate = date.substring(0, date.lastIndexOf("."));
        return LocalDateTime.from(formatter.parse(substringDate));
    }

    private static String parseHttpMethod(String httpMethod) {
        return null;
    }
}