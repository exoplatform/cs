/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 20, 2007  
 */
public class Utils {
  
   
  
  public static final String SVR_SMTP_AUTH = "mail.smtp.auth".intern() ;
  public static final String SVR_SMTP_SOCKETFACTORY_FALLBACK = "mail.smtp.socketFactory.fallback".intern() ;
  public static final String SVR_SMTP_SOCKETFACTORY_PORT = "mail.smtp.socketFactory.port".intern() ;
  public static final String SVR_SMTP_SOCKETFACTORY_CLASS = "mail.smtp.socketFactory.class".intern() ;
  public static final String SVR_SSL_CLASSNAME = "javax.net.ssl.SSLSocketFactory".intern() ;
  public static final String SVR_MAIL_SMTP_DEBUG = "mail.smtp.debug".intern() ;
  public static final String SVR_MAIL_DEBUG = "mail.debug".intern() ;
  public static final String SVR_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable".intern() ;
  public static final String SVR_SMTP = "smtp" ;
  public static final String SVR_PROTOCOL = "protocol".intern() ;
  public static final String SVR_SMTP_HOST = "mail.smtp.host".intern() ;
  public static final String SVR_SMTP_PORT = "mail.smtp.port".intern() ;  
  public static final String SVR_SMTP_USER = "mail.smtp.user".intern() ;
  public static final String SVR_SMTP_PASSWORD = "mail.smtp.user".intern() ;
  public static final String SVR_POP_HOST = "host".intern() ;
  public static final String SVR_POP_PORT = "port".intern() ;
  public static final String SVR_FOLDER = "folder".intern() ;
  public static final String SVR_USERNAME = "username".intern() ;
  public static final String SVR_PASSWORD = "password".intern() ; 
  public static final String SVR_SSL = "ssl".intern() ;
  
  public static final String KEY_FOLDERS = "Folders".intern() ;
  public static final String KEY_MESSAGE = "Messages".intern() ;
  public static final String KEY_TAGS = "Tags".intern() ;
  public static final String KEY_ACCOUNT = "account".intern() ;
  public static final String KEY_HEADER = "mailHeader".intern() ;
  
  public static final String EXO_ACCOUNT = "exo:account".intern() ;
  public static final String EXO_ID = "exo:id".intern() ;
  public static final String EXO_LABEL = "exo:label".intern() ;
  public static final String EXO_USERDISPLAYNAME = "exo:userDisplayName".intern() ;
  public static final String EXO_EMAILADDRESS = "exo:emailAddress".intern() ;
  public static final String EXO_REPLYEMAIL = "exo:emailReplyAddress".intern() ;
  public static final String EXO_SIGNATURE = "exo:signature".intern() ;
  public static final String EXO_DESCRIPTION = "exo:description".intern() ;
  public static final String EXO_FROM = "exo:from".intern();
  public static final String EXO_TO = "exo:to".intern() ;
  public static final String EXO_SUBJECT = "exo:subject".intern() ;
  public static final String EXO_REPLYTO = "exo:replyto".intern();
  public static final String EXO_CC = "exo:cc".intern() ;
  public static final String EXO_BCC= "exo:bcc".intern() ;
  public static final String EXO_BODY = "exo:body".intern() ;
  public static final String EXO_SIZE = "exo:size".intern();
  public static final String EXO_TAGS = "exo:tags".intern() ;
  public static final String EXO_FOLDERS = "exo:folders".intern() ;
  public static final String EXO_PERSONAL = "exo:personal".intern() ;
  public static final String EXO_RECEIVEDDATE = "exo:receivedDate".intern() ; 
  public static final String EXO_SENDDATE = "exo:sendDate".intern() ;
  public static final String EXO_SERVERPROPERTIES = "exo:serverProperties".intern() ;
  public static final String EXO_MESSAGE = "exo:message".intern() ;
  public static final String EXO_ISUNREAD = "exo:isUnread".intern() ;
  public static final String EXO_FOLDER = "exo:folder".intern() ;
  public static final String EXO_UNREADMESSAGES = "exo:unreadMessages".intern() ;
  public static final String EXO_NAME = "exo:name".intern() ;
  public static final String EXO_MAILTAG = "exo:mailtag".intern();
  
  public static final String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  public static final String NT_FILE = "nt:file".intern() ;
  public static final String NT_RESOURCE = "nt:resource".intern() ;
  
  public static final String JCR_LASTMODIFIED = "jcr:lastModified".intern() ;
  public static final String JCR_CONTENT = "jcr:content".intern() ;
  public static final String JCR_MIMETYPE = "jcr:mimeType".intern() ;
  public static final String JCR_DATA = "jcr:data".intern() ;
  
  public static final String MIMETYPE_TEXTPLAIN = "text/plain".intern() ;
  public static final String ATTACHMENT = "ATTACHMENT".intern();
  public static final String INLINE = "INLINE".intern();
  
  public static final String POP3 = "pop3".intern() ;
  public static final String IMAP = "imap".intern() ;
  
  final static public String FD_INBOX = "Inbox".intern();
  final static public String FD_DRAFTS = "Drafts".intern() ;
  final static public String FD_SENT = "Sent".intern() ;
  final static public String FD_SPAM = "Spam".intern() ;
  final static public String FD_TRASH = "Trash".intern() ;

  public static boolean isEmptyField(String value) {
    return value == null || value.trim().length() == 0 ;
  }
  
  public static boolean isNumber(String number) {
    try {
      Long.parseLong(number.trim()) ;
    } catch(NumberFormatException nfe) {
      return false;
    }
    return true ;
  }
  
  public static String formatDate(String format, Date date) {
    Format formatter = new SimpleDateFormat(format);
    return formatter.format(date);
  }
}
