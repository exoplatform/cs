/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.mail.service.bench;

import java.io.IOException;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.exoplatform.mail.service.MailService;
import org.picocontainer.Startable;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

/**
 * The service is used to create and handle a simple mail server for testing purpose.
 * GreenMail package is used in this service to provide the mail server. 
 * Created by The eXo Platform SAS
 * @author <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Aug 8, 2011  
 */
public class SimpleMailServerInitializer implements Startable {
  
  private GreenMail greenMail;
  
  private String host = "localhost";
  
  private MimeMessage newMimeMessage(String to, String from, String subject, String body, final byte[] attachment, final String contentType, final String filename, String description) throws MessagingException {
    Address[] froms = new InternetAddress[]{new InternetAddress(from)};
    MimeMessage mimeMessage = new MimeMessage(getSmtpSession());
    mimeMessage.setSubject(subject);
    mimeMessage.setFrom(froms[0]);
    mimeMessage.setSentDate(new Date());
    MimeMultipart multipPartContent = new MimeMultipart();
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(body, "text/html; charset=utf-8");
    multipPartContent.addBodyPart(mimeBodyPart);

 // Part two is attachment
    mimeBodyPart = new MimeBodyPart();
    DataSource source = new ByteArrayDataSource(attachment, contentType);
    mimeBodyPart.setDataHandler(new DataHandler(source));
    mimeBodyPart.setDisposition(Part.ATTACHMENT);
    mimeBodyPart.setFileName(filename);
    multipPartContent.addBodyPart(mimeBodyPart);
    mimeMessage.setContent(multipPartContent);
    return mimeMessage;
  }
  
  public SimpleMailServerInitializer(MailService mailService) {
    greenMail = new GreenMail();
  }
 
  public Session getSmtpSession() {
    return GreenMailUtil.getSession(ServerSetupTest.SMTP);
  }
  
  public Session getImapSession() {
    return GreenMailUtil.getSession(ServerSetupTest.IMAP);
  }
  
  public Session getPop3Session() {
    return GreenMailUtil.getSession(ServerSetupTest.POP3);
  }
  
  public void addUser(String email, String password) {
    greenMail.setUser(email, password);
  }
  
  public void addUser(String email, String username, String password) {
    greenMail.setUser(email, username, password);
  }
  
  public void sendMailMessage(String to, String from, String subject, String body, byte[] attachment, String filename, String description) throws MessagingException, IOException {
    MimeMessage message = newMimeMessage(to, from, subject, body, attachment, "text/html; charset=utf-8", filename, description);
    Address[] tos = new InternetAddress[]{new InternetAddress(to)};
    Transport.send(message, tos);
  }
  
  public String getImapPort() {
    return String.valueOf(ServerSetupTest.IMAP.getPort());
  }
  
  public String getImapProtocol() {
    return ServerSetupTest.IMAP.getProtocol();
  }
  
  public String getPop3Port() {
    return String.valueOf(ServerSetupTest.POP3.getPort());
  }
  
  public String getPop3Protocol() {
    return ServerSetupTest.POP3.getProtocol();
  }
  
  public String getSmtpPort() {
    return String.valueOf(ServerSetupTest.SMTP.getPort());
  }
  
  public String getSmtpProtocol() {
    return ServerSetupTest.SMTP.getProtocol();
  }
  
  public String getHost() {
    return host;
  }
  
  @Override
  public void start() {
    greenMail.start();
  }

  @Override
  public void stop() {
    greenMail.stop();
  }

}
