package com.green.chord;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by wsgreen on 11/12/15.
 */
public class Message {
  public static final String FIND_SUCCESSOR = "FIND_SUCCESSOR";
  public static final String RET_SUCCESSOR = "RET_SUCCESSOR";

  private String fromIp;
  private String regardingId;
  private String type;
  private String value;

  public Message(String msg) {
    String[] tokens = msg.split(":");
    fromIp = tokens[0];
    regardingId = tokens[1];
    type = tokens[2];
    value = tokens[3];
  }

  public Message(String fromId, String regardingId, String type, String value) {
    this.fromIp = fromId;
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
      out.write(msg.toString());
      out.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public boolean myMessage(String type, String regardingId) {
    return (type.compareTo(this.type) == 0 && regardingId.compareTo(this.regardingId) == 0);
  }

  public String getFromIp() {
    return fromIp;
  }

  public String getRegardingId() {
    return regardingId;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    String out = "";
    out += fromIp + ":";
    out += regardingId + ":";
    out += type + ":";
    out += value + ":";

    return out;
  }
}
