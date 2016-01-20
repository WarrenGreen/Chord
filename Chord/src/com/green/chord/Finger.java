package com.green.chord;

/**
 * Created by wsgreen on 11/12/15.
 */
public class Finger {
  private String ip;
  private int port;
  private String id;

  public Finger(String finger) {
    String[] split = finger.split(":");
    ip = split[0].trim();
    port = Integer.parseInt(split[1]);
    id = split[2].trim();
  }

  public Finger(String ip, int port, String Id) {
    this.ip = ip;
    this.port = port;
    this.id = Id;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return ip+":"+port+":"+id;
  }
}
