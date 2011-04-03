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

import javax.mail.Folder;
import javax.mail.Message;

import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.DataStorage;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MimeMessageParser;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class FetchMailContentThread implements Runnable {
  private Log log = ExoLogger.getLogger(this.getClass());
  private DataStorage                                     storage_;

  private String                                          username_;

  private String                                          accountId_;

  private LinkedHashMap<javax.mail.Message, List<String>> msgMap_;

  private javax.mail.Folder                               folder_;

  private int                                             numberMessage_ = 0;

  private MailService                                     mailService;

  public FetchMailContentThread(MailService mailService, DataStorage storage, LinkedHashMap<javax.mail.Message, List<String>> msgMap, int numberMessage, javax.mail.Folder folder, String username, String accountId) throws Exception {
    this.mailService = mailService;
    storage_ = storage;
    username_ = username;
    accountId_ = accountId;
    msgMap_ = msgMap;
    folder_ = folder;
    numberMessage_ = numberMessage;
  }

  public void run() {
    try {
      downloadMailContent();
    } catch (Exception e) {
      // e.printStackTrace();
    }
  }

  public void downloadMailContent() throws Exception {
    int j = 0;
    Message msg;
    List<javax.mail.Message> msgList = new ArrayList<javax.mail.Message>(msgMap_.keySet());

    if (!folder_.isOpen())
      folder_.open(Folder.READ_WRITE);
    while (j < numberMessage_) {
      CheckingInfo info = mailService.getCheckingInfo(username_, accountId_);
      msg = msgList.get(j);
      if (info.isRequestStop()) {
        log.info("stop update message at " + msg.getSubject());
        break;
      }
      log.info("save msg to database: " + msg.getSubject());
      storage_.saveTotalMessage(username_, accountId_, MimeMessageParser.getMessageId(msg), msg, null);
      j++;
    }
  }

  public void stop() {
    try {
      if (folder_.isOpen())
        folder_.close(true);
    } catch (Exception e) {
      // e.printStackTrace();
    }
  }
}
