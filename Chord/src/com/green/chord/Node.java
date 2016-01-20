package com.green.chord;

import com.sun.istack.internal.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

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

  private static final Logger LOGGER = Logger.getLogger(Node.class);

  private static final String FIND = "find";
  private static final String INSERT = "insert";
  private static final String HELP = "Please enter a valid command.";

  //Potentially convert these to generic Param type inside map as parameters grow
  private String ip = null;
  private int port = -1;
  private String id;
  private boolean leader;
  private int fingerCount = 256;
  private String ringIp;
  private int ringPort;

  private ServerSocket serverSocket = null;

  private AtomicBoolean running = new AtomicBoolean(true);

  private Finger[] fingerTable;
  private Finger predecessor;

  public Node(String configFile) {
    readConfig(configFile);
    fingerTable = new Finger[fingerCount];

    LOGGER.info(String.format("%s: init", this.id));

    if(leader)
      initRing();
    else {
      fingerTable[0] = null;
      joinRing();
    }

    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Server socket failure.");
      System.exit(1);
    }

    Thread listener = new Thread(getListener());
    listener.start();

    Thread stabilizer = new Thread(getStabilizer());
    stabilizer.start();

    Thread client = new Thread(getClient());
    client.start();
  }

  private String findSuccessor(String id) {
    LOGGER.log(Level.INFO, String.format("%s: find %s", this.id, id));
     if( fingerTable[0] != null &&
             this.id.compareTo(fingerTable[0].getId()) ==0 ||
            ((id.compareTo(this.id) > 0) && (id.compareTo(fingerTable[0].getId()) <= 0)) ||
            ((id.compareTo(this.id) > 0) && (id.compareTo(fingerTable[0].getId()) > 0) && this.id.compareTo(fingerTable[0].getId()) >= 0) ){
      return fingerTable[0].toString();
    } else {
      String n0 = closestPrecedingNode(id);
      Message m = Message.sendWithResponse(n0, new Message(ip + ":" + port, id, Message.FIND_SUCCESSOR, ""));

      return m.getValue();
    }
  }

  private String closestPrecedingNode(String id) {
    for(int i=fingerTable.length-1;i>=0;i++) {
      String finger = fingerTable[i].getId();
      if((finger.compareTo(this.id) > 0) && (finger.compareTo(id) < 0)) { // finger[i] in (n, id)
        return fingerTable[i].toString();
      }
    }

    return this.toString();
  }

  private void initRing() {
    for(int i=0;i<fingerTable.length;i++) {
      fingerTable[i] = new Finger(ip, port, id);
    }

    predecessor = null;
  }

  private void joinRing() {
    Message m = Message.sendWithResponse(ringIp+":"+ringPort, new Message(ip + ":" + port, id, Message.FIND_SUCCESSOR, ""));

    fingerTable[0] = new Finger(m.getValue());
    for(int i=1;i<fingerTable.length;i++) {
      fingerTable[i] = new Finger(findSuccessor(fingerTable[i-1].getId()));
    }

    predecessor = null;
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
    id = Params.toSHA1(ip, port);
  }

  private Runnable getListener() {
    return  () -> {
      Socket clientSocket = null;
      BufferedReader in = null;
      PrintWriter out = null;
      Message msg = null;

      while(running.get()) {
        try {
          clientSocket = serverSocket.accept();
          in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          out = new PrintWriter(clientSocket.getOutputStream(), true);
          msg = new Message(in.readLine());
          LOGGER.info(String.format("%s: receive %s", this.id, msg.getFromIp()));

          switch (msg.getType()) {
            case Message.FIND_SUCCESSOR:
              Message resp = null;
              if(msg.getRegardingId().compareTo(this.id)==0 ) {
                resp = new Message(ip + ":" + port, msg.getRegardingId(),
                        Message.RET_SUCCESSOR, this.id);
              } else {
                 resp = new Message(ip + ":" + port, msg.getRegardingId(),
                        Message.RET_SUCCESSOR, findSuccessor(msg.getRegardingId()));
              }
              out.println(resp.toString());
              break;
          }

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

  private Runnable getClient() {
    return () -> {
      Scanner in = new Scanner(System.in);
      String input = null;

      while( (input = in.nextLine()).compareToIgnoreCase("end") != 0) {
        switch (input) {
          default:
            System.out.println(Node.HELP);
        }
      }

      running.set(false);
      try {
        serverSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    };
  }

  @Override
  public String toString() {
    return ip+":"+port;
  }
}
