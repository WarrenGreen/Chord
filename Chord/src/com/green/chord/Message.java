package com.green.chord;

/**
 * Created by wsgreen on 11/12/15.
 */
public class Message {
  public static final String FIND_SUCCESSOR = "FIND_SUCCESSOR";
  public static final String RET_SUCCESSOR = "RET_SUCCESSOR";

  private String regardingId;
  private String type;
  private String value;

  public Message(String msg) {

  }

  public static void send(String toAddress, String fromAddress, String type) {

  }

  public static void send(String toAddress, String fromAddress, String type, String value) {

  }

  public boolean myMessage(String type, String regardingId) {
    return (type.compareTo(this.type) == 0 && regardingId.compareTo(this.regardingId) == 0);
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
}
