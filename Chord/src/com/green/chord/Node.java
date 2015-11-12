package com.green.chord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wsgreen
 * @ip IP Address of node
 * @port Port of node
 * @Id SHA-1 hash of (ip, port)
 * @leader first node in the ring
 * @fingerCount number of finger table entries to store. Default is 10 unless specified. Maximum is 265 with SHA-1.
 * @ringIp an ip of a node in the desired ring
 * @ringPort an ip corresponding to the ringIp
 */
public class Node {
  //Potentially convert these to generic Param type inside map as parameters grow
  private String ip = null;
  private int port = -1;
  private String Id;
  private boolean leader;
  private int fingerCount = 10;
  private String ringIp;
  private int ringPort;

  private AtomicBoolean running = new AtomicBoolean(true);

  private ServerSocket serverSocket = null;

  private BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

  private Finger[] fingerTable;

  public Node(String configFile) {
    readConfig(configFile);
    fingerTable = new Finger[fingerCount];

    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      e.printStackTrace();
    }

    Thread listener = new Thread(getListener());
    listener.start();

    Thread stabilizer = new Thread(getStabilizer());
    stabilizer.start();
  }

  private String findSuccessor(String hash) {
    return null;
  }

  private void readConfig(String configFile) {
    List<String> configParams = null;
    try {
      configParams = Files.readAllLines(Paths.get(configFile));
    } catch (IOException e) {
      System.err.println("Config file was not found or was unable to be read.");
      System.exit(1);
    }

    for(String param: configParams) {
      int colon = param.indexOf(':');
      String title = param.substring(0, colon).trim();
      String value = param.substring(colon+1).trim();

      switch (title) {
        case Params.IP_ADDRESS:
          ip = value;
          break;
        case Params.PORT:
          port = Integer.valueOf(value);
          break;
        case Params.LEADER:
          leader = Boolean.valueOf(value);
          break;
        case Params.RING_IP:
          ringIp = value;
          break;
        case Params.RING_PORT:
          ringPort = Integer.valueOf(value);
          break;
        case Params.FINGER_TABLE_LENGTH:
          fingerCount = Integer.valueOf(value);
      }
    }

    assert(ip != null);
    assert(port > 0);
    Id = Params.toSHA1(ip, port);
  }

  private Runnable getListener() {
    return  () -> {
      Socket clientSocket = null;
      BufferedReader in = null;

      while(running.get()) {
        try {
          clientSocket = serverSocket.accept();
          in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
          e.printStackTrace();
        }

        try {
          queue.add(new Message(in.readLine()));
        } catch (IOException e) {
          e.printStackTrace();
        }

      }

    };
  }

  private Runnable getStabilizer() {
    //TODO
    return null;
  }
}
