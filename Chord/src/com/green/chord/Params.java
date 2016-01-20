package com.green.chord;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by wsgreen on 11/12/15.
 */
public class Params {
  public static final String IP_ADDRESS = "IP";
  public static final String PORT = "PORT";
  public static final String LEADER = "LEADER";
  public static final String RING_IP = "RING_IP";
  public static final String RING_PORT = "RING_PORT";
  public static final String FINGER_TABLE_LENGTH = "FINGER_TABLE_LENGTH";

  public static String toSHA1(String value) {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("SHA-1");
    }
    catch(NoSuchAlgorithmException e) {
      e.printStackTrace();
      System.err.println("SHA-1 algorithm was not found.");
      System.exit(2);
    }

    return HexBin.encode(md.digest(value.getBytes()));
  }

  public static String toSHA1(String ip, int port) {
    return toSHA1(ip+":"+port);
  }
}
