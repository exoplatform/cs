/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.exoplatform.mail.service.MailImportExport;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */

public class EMLImportExport implements MailImportExport{
	private JCRDataStorage jcrDataStorage_ ;
	public EMLImportExport(JCRDataStorage jcrDataStorage) throws Exception {
		jcrDataStorage_ = jcrDataStorage ;
	}
	public OutputStream exportMessage(SessionProvider sProvider, String username, String accountId, String messageId) throws Exception {
		Properties props = System.getProperties();
    Session session = Session.getDefaultInstance(props, null);
    Message message = jcrDataStorage_.getMessageById(sProvider, username, accountId, messageId);
    MimeMessage mimeMessage = new MimeMessage(session);
    mimeMessage = Utils.mergeToMimeMessage(message, mimeMessage);
    OutputStream outputStream = new ByteArrayOutputStream();
    mimeMessage.writeTo(outputStream);
		return outputStream;
	}

	public void importMessage(SessionProvider sProvider, String username, String accountId, String folderId, InputStream inputStream, String type) throws Exception {
		Properties props = System.getProperties();
    Session session = Session.getDefaultInstance(props, null);
    MimeMessage mimeMessage = new MimeMessage(session, inputStream);
    Message message = new Message();
    message.setAccountId(accountId);
    message = Utils.mergeFromMimeMessage(message, mimeMessage);
    message.setFolders(new String[] {folderId});
    jcrDataStorage_.saveMessage(sProvider, username, accountId, message, true);
	}  
	
}
