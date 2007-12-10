/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Dec 7, 2007  
 */
public class SpamFilter {
  private String[] senderAddresses = new String[0];
  
  public String[] getSenders() { return senderAddresses ; }
  public void setSenders(String[] arr) { senderAddresses = arr ; }
  
  public void reportSpam(Message msg) throws Exception {
    List<String> senderList = new ArrayList<String>(Arrays.asList(getSenders()));
    Map<String, String> senderMap = new HashMap<String, String>();
    for (String sender : senderList) {
      senderMap.put(sender, sender) ;
    }
    String sender = Utils.getAddresses(msg.getFrom())[0];
    senderMap.put(sender, sender) ;
    setSenders(senderMap.values().toArray(new String[]{})) ;
  }
  
  public void notSpam(Message msg) throws Exception {
    List<String> senderList = new ArrayList<String>(Arrays.asList(getSenders()));
    String sender = Utils.getAddresses(msg.getFrom())[0];
    senderList.remove(sender);
    setSenders(senderList.toArray(new String[]{}));
  }
  
  public boolean checkSpam(javax.mail.Message msg) throws Exception {
    //TODO : Need to improve this method to check spam more intelligent
    List<String> senderList = new ArrayList<String>(Arrays.asList(getSenders()));
    String sender = Utils.getAddresses(InternetAddress.toString(msg.getFrom()))[0] ;
    return senderList.contains(sender) ;
  }
}
