package com.green.chord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by wsgreen on 11/12/15.
 */
public class Message {
  private static final com.sun.istack.internal.logging.Logger LOGGER = com.sun.istack.internal.logging.Logger.getLogger(Node.class);

  public static final int FIND_SUCCESSOR = 1;
  public static final int RET_SUCCESSOR = 2;

  private String fromIp;
  private String regardingId;
  private int type;
  private String value = null;

  public Message(String msg) {
    String[] tokens = msg.split("~");
    fromIp = tokens[0];
    regardingId = tokens[1];
    type = Integer.parseInt(tokens[2]);
    if(tokens.length > 3)
      value = tokens[3];
  }

  public Message(String fromIp, String regardingId, int type, String value) {
    this.fromIp = fromIp;
    this.regardingId = regardingId;
    this.type = type;
    this.value = value;
  }

  public static void send(String toAddress, Message msg) {
    Socket socket = null;
    PrintWriter out = null;

    int colon = toAddress.indexOf(':');
    String toIp = toAddress.substring(0, colon).trim();
    int toPort = Integer.parseInt(toAddress.substring(colon + 1));

    try {
      socket = new Socket(toIp, toPort);
      out = new PrintWriter(socket.getOutputStream(), true);
      out.println(msg.toString());
      out.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static Message sendWithResponse(String toAddress, Message msg) {
    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;
    Message response = null;

    int colon = toAddress.indexOf(':');
    String toIp = toAddress.substring(0, colon).trim();
    int toPort = Integer.parseInt(toAddress.substring(colon + 1));

    try {
      LOGGER.info(String.format("%s: sendwR %s", msg.fromIp, toAddress));
      socket = new Socket(toIp, toPort);
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out.println(msg.toString());

      response =  new Message(in.readLine());
      out.close();
      in.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return response;

  }

  public boolean myMessage(int type, String regardingId) {
    return (this.type == type && regardingId.compareTo(this.regardingId) == 0);
  }

  public String getFromIp() {
    return fromIp;
  }

  public String getRegardingId() {
    return regardingId;
  }

  public int getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    String out = "";
    out += fromIp + "~";
    out += regardingId + "~";
    out += type + "~";
    out += value + "~";

    return out;
  }
}
