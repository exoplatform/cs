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
package org.exoplatform.mail.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.exoplatform.mail.service.DataStorage;
import org.exoplatform.mail.service.MailImportExport;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen
 * hung.nguyen@exoplatform.com Jul 2, 2007
 */

public class EMLImportExport implements MailImportExport {
  private DataStorage jcrDataStorage_;

  public EMLImportExport(DataStorage jcrDataStorage) throws Exception {
    jcrDataStorage_ = jcrDataStorage;
  }

  public OutputStream exportMessage(String username, String accountId, Message message) throws Exception {
    Properties props = System.getProperties();
    Session session = Session.getDefaultInstance(props, null);
    MimeMessage mimeMessage = new MimeMessage(session);
    mimeMessage = Utils.mergeToMimeMessage(message, mimeMessage);
    OutputStream outputStream = new ByteArrayOutputStream();
    mimeMessage.writeTo(outputStream);
    return outputStream;
  }

  public boolean importMessage(String username, String accountId, String folderId, MimeMessage mimeMessage, long[] msgUID) throws Exception {
    return jcrDataStorage_.saveMessage(username, accountId, msgUID, mimeMessage, new String[] { folderId }, null, null, null, null, true, username);
  }
}
