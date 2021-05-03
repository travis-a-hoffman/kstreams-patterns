package io.firkin.kstreams;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Main {

  static final Map<String, Consumer<String[]>> cmds = new HashMap<>();
  static final Map<String, String> descs = new HashMap<>();

  static {
    cmds.put("hello", io.firkin.kstreams.v28.HelloKStreams::main);
    cmds.put("help", Main::help);
    cmds.put("no-topic", io.firkin.kstreams.v28.HelloKStreams::main);
    cmds.put("word-count", io.firkin.kstreams.v28.WordCount::main);

    descs.put("hello", "Hello World!");
    descs.put("help", "Print this usage and help messsage");
    descs.put("no-topic", "Example: Failed to opening a non-existent topic");
    descs.put("word-count", "Example: Counts words as they come");
  }

  public static void main(String[] argv) {
    if (argv.length == 0) {
      help(argv);
    } else if (!cmds.containsKey(argv[0])) {
      System.out.println("Unknown Subcommand: "+argv[0]+"\n");
      help(argv);
    } else {
      cmds.get(argv[0]).accept(Arrays.copyOfRange(argv, 1, argv.length));
    }
  }

  static void help(String[] argv) {
    System.out.println("Usage: java io.firkin.kstreams.Main <subcommand>");
    System.out.println("SubCommands:");
    cmds.keySet().stream().sorted()
        .forEach(s -> System.out.printf("  %-12s  %s\n", s, descs.get(s)));
  }
}
