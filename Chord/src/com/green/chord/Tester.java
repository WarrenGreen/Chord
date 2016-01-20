package com.green.chord;

/**
 * Created by wsgreen on 11/15/15.
 */
public class Tester {
  private static final String leaderConfig = "./config.txt";
  private static final String slaveConfig = "./config-slave.txt";

  public static void main(String[] args) {
    Node leader = new Node(leaderConfig);

  }
}
