package org.command;
import org.apache.commons.cli.*;

public class CmdClientParser {
    public static String login(String[] line){
        Options opt = new Options();
        opt.addOption("u", "username", true, "имя файла");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(opt, line);
            if (cmd.hasOption("u")) {
                return cmd.getOptionValue("u").trim();
            }

        } catch (ParseException e) {
            if(!(e instanceof UnrecognizedOptionException || e instanceof MissingArgumentException)){
                e.printStackTrace();
            }
        }
        return null;
    }
    public static String createTopic(String[] line){
        Options opt = new Options();
        opt.addOption("n", "name", true, "Название раздела для голосования.");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(opt, line);
            if (cmd.hasOption("n")) {
                return cmd.getOptionValue("n").trim();
            }


        } catch (ParseException e) {
            if(!(e instanceof UnrecognizedOptionException || e instanceof MissingArgumentException)){
                e.printStackTrace();
            }
        }
        return null;
    }
    public static String createVote(String[] line){
        Options opt = new Options();
        opt.addOption("t", "topic", true, "Раздел, в котором создаётся голосование.");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(opt, line);
            if (cmd.hasOption("t")) {
                return cmd.getOptionValue("t").trim();
            }
        } catch (ParseException e) {
            if(!(e instanceof UnrecognizedOptionException || e instanceof MissingArgumentException)){
                e.printStackTrace();
            }
        }
        return null;
    }
    public static String[] view(String[] line){
        Options opt = new Options();
        opt.addOption("t", "topic", true, "Тема голосования.");
        opt.addOption("v", "vote", true, "Информация по конкретному варианту.");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(opt, line);
            if (cmd.hasOption("t") && !cmd.hasOption("v")){
                return new String[]{cmd.getOptionValue("t").trim()};
            }
            if (cmd.hasOption("t") && cmd.hasOption("v")) {
                return new String[]{cmd.getOptionValue("t").trim(),cmd.getOptionValue("v").trim()};
            }
        } catch (ParseException e) {
            if(!(e instanceof UnrecognizedOptionException || e instanceof MissingArgumentException)){
                e.printStackTrace();
            }
        }
        return null;
    }
    public static String[] vote(String[] line){
        Options opt = new Options();
        opt.addOption("t", "topic", true, "Тема голосования.");
        opt.addOption("v", "vote", true, "Информация по конкретному варианту.");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(opt, line);
            if (cmd.hasOption("t") && cmd.hasOption("v")) {
                return new String[]{cmd.getOptionValue("t").trim(),cmd.getOptionValue("v").trim()};
            }

        } catch (ParseException e) {
            if(!(e instanceof UnrecognizedOptionException || e instanceof MissingArgumentException)){
                e.printStackTrace();
            }
        }
        return null;
    }
    public static String[] delete(String[] line){
        Options opt = new Options();
        opt.addOption("t", "topic", true, "Тема голосования.");
        opt.addOption("v", "vote", true, "Информация по конкретному варианту.");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(opt, line);
            if (cmd.hasOption("t") && cmd.hasOption("v")) {
                return new String[]{cmd.getOptionValue("t").trim(),cmd.getOptionValue("v").trim()};
            }

        } catch (ParseException e) {
            if(!(e instanceof UnrecognizedOptionException || e instanceof MissingArgumentException)){
                e.printStackTrace();
            }
        }
        return null;
    }
}
