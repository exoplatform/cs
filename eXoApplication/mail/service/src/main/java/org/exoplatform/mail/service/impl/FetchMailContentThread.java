/**
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
 **/
package org.exoplatform.mail.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.mail.Message;

import org.exoplatform.mail.service.MimeMessageParser;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

import com.sun.mail.imap.IMAPFolder;

public class FetchMailContentThread implements Runnable {

  private JCRDataStorage storage_;
  private SessionProvider sProvider_;
  private String username_;
  private String accountId_;
  private LinkedHashMap<javax.mail.Message, List<String>> msgMap_;
  private javax.mail.Folder folder_;
  
  public FetchMailContentThread(SessionProvider sProvider, JCRDataStorage storage, LinkedHashMap<javax.mail.Message, List<String>> msgMap, javax.mail.Folder folder, String username, String accountId) throws Exception {
    sProvider_ = sProvider; 
    storage_ = storage;
    username_ = username;
    accountId_ = accountId;
    msgMap_ = msgMap;
    folder_ = folder;
  }

  public void run() {
    try {
      downloadMailContent();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public void downloadMailContent() throws Exception {
    int j = 0;
    Message msg;
    int totalNew = msgMap_.size();
    List<javax.mail.Message> msgList = new ArrayList<javax.mail.Message>(msgMap_.keySet()) ;
    while (j < totalNew) {
      msg = msgList.get(j);
      storage_.saveTotalMessage(sProvider_, username_, accountId_, MimeMessageParser.getMessageId(msg), msg);
      j++;
    } 
  }
  
  public void stop() {
    try {
      folder_.close(true);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
