package com.green.chord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
  private String id;
  private boolean leader;
  private int fingerCount = 256;
  private String ringIp;
  private int ringPort;

  private static ServerSocket serverSocket = null;

  private AtomicBoolean running = new AtomicBoolean(true);
  private BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

  private Finger[] fingerTable;
  private Finger predecessor;

  public Node(String configFile) {
    readConfig(configFile);
    fingerTable = new Finger[fingerCount];

    if(leader)
      initRing();

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
  }

  private String findSuccessor(String id) {
    if((id.compareTo(this.id) > 0) && (id.compareTo(fingerTable[0].getId()) <= 0)){
      return fingerTable[0].toString();
    } else {
      String n0 = closestPrecedingNode(id);
      Message.send(n0, new Message(ip+":"+port, id, Message.FIND_SUCCESSOR, ""));

      Message m = queue.poll();
      while( !m.myMessage(Message.RET_SUCCESSOR, id)) { //Not sure about this...
        queue.add(m);
        m = queue.poll();
      }
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

      while(running.get()) {
        try {
          clientSocket = serverSocket.accept();
          in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
          e.printStackTrace();
        }

        Message msg = null;
        try {
          msg = new Message(in.readLine());
        } catch (IOException e) {
          e.printStackTrace();
        }

        switch (msg.getType()) {
          case Message.FIND_SUCCESSOR:
            Message.send(msg.getFromIp(), new Message(ip+":"+port, msg.getRegardingId(),
                    Message.RET_SUCCESSOR, findSuccessor(id)));
            break;
          default:
            queue.add(msg);
        }

      }

    };
  }

  private Runnable getStabilizer() {
    //TODO
    return null;
  }

  @Override
  public String toString() {
    return ip+":"+port;
  }
}
