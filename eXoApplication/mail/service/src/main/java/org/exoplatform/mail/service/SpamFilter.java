/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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

  public String[] getSenders() {
    return senderAddresses;
  }

  public void setSenders(String[] arr) {
    senderAddresses = arr;
  }

  public void reportSpam(Message msg) throws Exception {
    List<String> senderList = new ArrayList<String>(Arrays.asList(getSenders()));
    Map<String, String> senderMap = new HashMap<String, String>();
    for (String sender : senderList) {
      senderMap.put(sender, sender);
    }
    String sender = Utils.getAddresses(msg.getFrom())[0];
    senderMap.put(sender, sender);
    setSenders(senderMap.values().toArray(new String[] {}));
  }

  public void notSpam(Message msg) throws Exception {
    List<String> senderList = new ArrayList<String>(Arrays.asList(getSenders()));
    String sender = Utils.getAddresses(msg.getFrom())[0];
    senderList.remove(sender);
    setSenders(senderList.toArray(new String[] {}));
  }

  public boolean checkSpam(javax.mail.Message msg) throws Exception {
    // TODO : Need to improve this method to check spam more intelligent
    List<String> senderList = new ArrayList<String>(Arrays.asList(getSenders()));
    String sender = Utils.getAddresses(InternetAddress.toString(msg.getFrom()))[0];
    return senderList.contains(sender);
  }
}
