package com.ef;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    public static final BlockedIpsRepository repository = new BlockedIpsRepository();
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");

    public static void main(String[] args) throws IOException {

        Option file = Option.builder()
                .longOpt("accesslog")
                .desc("log file")
                .required()
                .hasArg()
                .valueSeparator()
                .build();

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
        options.addOption(file);
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

        Path path = null;

        try {
            path = Paths.get(cmd.getOptionValue("accesslog"));
            if(Files.notExists(path)) {
                throw new Exception("log file not found");
            }
        } catch (Exception e) {
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

        List<String> lines = Files.readAllLines(path);

        Map<String, BlockedIpDTO> requests = new HashMap<>();

        for(String line : lines) {

            BlockedIpDTO blockedIpDTO = parseLine(line);
            blockedIpDTO.setStartDateParam(startLocalDateTime);
            blockedIpDTO.setDurationParam(durationParam);
            blockedIpDTO.setThresholdParam(Integer.valueOf(thresholdParam));

            if(blockedIpDTO.getRequestTime().isEqual(startLocalDateTime) || blockedIpDTO.getRequestTime().isAfter(startLocalDateTime)) {
                if(blockedIpDTO.getRequestTime().isEqual(endLocalDateTime) || blockedIpDTO.getRequestTime().isBefore(endLocalDateTime)) {
                    BlockedIpDTO blockedIp = requests.getOrDefault(blockedIpDTO.getIp(), blockedIpDTO);
                    blockedIp.incrementCount();
                    requests.put(blockedIpDTO.getIp(), blockedIp);
                }
            }
        }

        LocalDateTime finalEndLocalDateTime = endLocalDateTime;
        requests.forEach((k, v) -> {
            if(v.getCount() > Integer.parseInt(thresholdParam)) {
                String reason = String.format("IP: %s has %s or more requests between %s and %s",
                        k, thresholdParam, startLocalDateTime.format(Parser.formatter), finalEndLocalDateTime.format(Parser.formatter));
                v.setReason(reason);
                repository.save(v);
                System.out.println(reason);
            }
        });

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
}