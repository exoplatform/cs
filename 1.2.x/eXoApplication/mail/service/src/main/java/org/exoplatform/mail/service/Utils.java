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

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.services.scheduler.JobSchedulerService;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 20, 2007  
 */
public class Utils {
  
  private static final Log logger = LogFactory.getLog(Utils.class);
  
  public static MailService mailService_;
  public static JobSchedulerService schedulerService_;
  
  public static final String SVR_SMTP = "smtp" ;
  public static final String SVR_SMTP_HOST = "mail.smtp.host".intern() ;
  public static final String SVR_SMTP_PORT = "mail.smtp.port".intern() ;  
  public static final String SVR_SMTP_USER = "mail.smtp.user".intern() ;
  public static final String SVR_SMTP_PASSWORD = "mail.smtp.user".intern() ;
  public static final String SVR_SMTP_AUTH = "mail.smtp.auth".intern() ;
  public static final String SVR_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback".intern() ;
  public static final String SVR_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port".intern() ;
  public static final String SVR_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class".intern() ;
  public static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory".intern();
  
  public static final String SVR_SSL_CLASSNAME = "javax.net.ssl.SSLSocketFactory".intern() ;
  public static final String SVR_MAIL_SMTP_DEBUG = "mail.smtp.debug".intern() ;
  public static final String SVR_MAIL_DEBUG = "mail.debug".intern() ;
  public static final String SVR_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable".intern() ;
  public static final String SVR_PROTOCOL = "protocol".intern() ;
  public static final String SVR_INCOMING_HOST = "incoming.host".intern() ;
  public static final String SVR_INCOMING_PORT = "incoming.port".intern() ;
  public static final String SVR_INCOMING_FOLDER = "folder".intern() ;
  public static final String SVR_INCOMING_USERNAME = "incoming.username".intern() ;
  public static final String SVR_INCOMING_PASSWORD = "incoming.password".intern() ;
  
  public static final String SVR_OUTGOING_HOST = "outgoing.host".intern() ;
  public static final String SVR_OUTGOING_PORT = "outgoing.port".intern() ;
   
  public static final String SVR_INCOMING_SSL = "ssl".intern() ;
  public static final String SVR_OUTGOING_SSL = "outgoing.ssl".intern() ;
  
  public static final String SVR_POP_LEAVE_ON_SERVER = "leave.on.server".intern() ;
  public static final String SVR_POP_SKIP_OVER_SIZE = "skip.over.size".intern() ;
  
  public static final String SVR_IMAP_MARK_AS_DELETE = "mark.as.delete".intern() ;
  
  public static final String KEY_FOLDERS = "Folders".intern() ;
  public static final String KEY_MESSAGE = "Messages".intern() ;
  public static final String KEY_TAGS = "Tags".intern() ;
  public static final String KEY_FILTER = "Filter".intern() ;
  public static final String KEY_MAIL_SETTING = "MailSetting".intern();
  public static final String KEY_SPAM_FILTER = "SpamFilter".intern() ;
  public static final String KEY_ACCOUNT = "account".intern() ;
  public static final String KEY_HEADER = "mailHeader".intern() ;
  public static final String KEY_CONVERSATION = "Conversation".intern() ;
  public static final String KEY_ATTACHMENT = "attachment".intern() ;
  
  public static final String EXO_ACCOUNT = "exo:account".intern() ;
  public static final String EXO_ID = "exo:id".intern() ;
  public static final String EXO_IN_REPLY_TO_HEADER = "exo:inReplyToHeader".intern();
  public static final String EXO_LABEL = "exo:label".intern() ;
  public static final String EXO_USERDISPLAYNAME = "exo:userDisplayName".intern() ;
  public static final String EXO_EMAILADDRESS = "exo:emailAddress".intern() ;
  public static final String EXO_REPLYEMAIL = "exo:emailReplyAddress".intern() ;
  public static final String EXO_SIGNATURE = "exo:signature".intern() ;
  public static final String EXO_DESCRIPTION = "exo:description".intern() ;
  public static final String EXO_FROM = "exo:from".intern();
  public static final String EXO_FROMS = "exo:froms".intern();
  public static final String EXO_TO = "exo:to".intern() ;
  public static final String EXO_SUBJECT = "exo:subject".intern() ;
  public static final String EXO_CONTENT_TYPE = "exo:contentType".intern() ;
  public static final String EXO_REPLYTO = "exo:replyto".intern();
  public static final String EXO_CC = "exo:cc".intern() ;
  public static final String EXO_BCC= "exo:bcc".intern() ;
  public static final String EXO_BODY = "exo:body".intern() ;
  public static final String EXO_SIZE = "exo:size".intern();
  public static final String EXO_STAR = "exo:star".intern() ;
  public static final String EXO_PRIORITY = "exo:priority".intern() ;
  public static final String EXO_HASATTACH = "exo:hasAttach".intern();
  public static final String ATT_IS_LOADED_PROPERLY = "exo:isLoadedProperly".intern();
  public static final String EXO_TAGS = "exo:tags".intern() ;
  public static final String EXO_FOLDERS = "exo:folders".intern() ;
  public static final String EXO_HEADERS = "exo:headers".intern() ;
  public static final String EXO_PERSONAL = "exo:personal".intern() ;
  public static final String EXO_RECEIVEDDATE = "exo:receivedDate".intern() ; 
  public static final String EXO_SENDDATE = "exo:sendDate".intern() ;
  public static final String EXO_SERVERPROPERTIES = "exo:serverProperties".intern() ;
  public static final String EXO_POPSERVERPROPERTIES = "exo:popServerProperties".intern() ;
  public static final String EXO_IMAPSERVERPROPERTIES = "exo:imapServerProperties".intern() ;
  public static final String EXO_MESSAGE = "exo:message".intern() ;
  public static final String EXO_ISUNREAD = "exo:isUnread".intern() ;
  public static final String EXO_TOTALMESSAGE = "exo:totalMessages".intern();
  public static final String EXO_FOLDER = "exo:folder".intern() ;
  public static final String EXO_UNREADMESSAGES = "exo:unreadMessages".intern() ;
  public static final String EXO_NAME = "exo:name".intern() ;
  public static final String EXO_MAILTAG = "exo:mailtag".intern();
  public static final String EXO_COLOR = "exo:color".intern();
  public static final String EXO_CHECKMAILAUTO = "exo:checkMailAuto".intern();
  public static final String EXO_EMPTYTRASH = "exo:emptyTrash".intern();
  public static final String EXO_PLACESIGNATURE = "exo:placeSignature".intern();
  public static final String EXO_SPAM_FILTER = "exo:spamFilter".intern();
  public static final String EXO_CONVERSATION = "exo:conversation".intern();
  public static final String EXO_CONVERSATIONID = "exo:conversationId".intern();
  public static final String EXO_LAST_START_CHECKING_TIME = "exo:lastStartCheckingTime".intern();
  public static final String EXO_LAST_CHECKED_TIME = "exo:lastCheckedTime".intern() ;
  public static final String EXO_CHECK_ALL = "exo:checkAll".intern();
  public static final String EXO_CHECK_FROM_DATE = "exo:checkFromDate".intern();
  public static final String EXO_IS_SAVE_PASSWORD  = "exo:isSavePassword".intern() ;
  public static final String EXO_MAIL_ATTACHMENT = "exo:mailAttachment".intern() ;
  public static final String EXO_ATT_NAME = "exo:fileName".intern() ;
  public static final String EXO_IS_ROOT = "exo:isRoot".intern();

  public static final String EXO_MAIL_SETTING = "exo:mailSetting".intern();
  public static final String EXO_NUMBER_MSG_PER_PAGE = "exo:numberMsgPerPage".intern();
  public static final String EXO_FORMAT_AS_ORIGINAL = "exo:formatAsOriginal".intern();
  public static final String EXO_REPLY_WITH_ATTACH = "exo:replyWithAttach".intern();
  public static final String EXO_FORWARD_WITH_ATTACH = "exo:forwardWithAttach".intern();
  public static final String EXO_PREFIX_MESSAGE_WITH = "exo:prefixMsgWith".intern();
  public static final String EXO_PERIOD_CHECKMAIL_AUTO = "exo:periodCheckAuto".intern();
  public static final String EXO_DEFAULT_ACCOUNT = "exo:defaultAccount".intern();
  public static final String EXO_USE_WYSIWYG = "exo:useWysiwyg".intern();
  public static final String EXO_SAVE_SENT_MESSAGE = "exo:saveMsgInSent".intern();
  public static final String EXO_FILTER = "exo:filter".intern() ;
  public static final String EXO_PATH = "exo:path".intern();
  
  public static final String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  public static final String NT_FILE = "nt:file".intern() ;
  public static final String NT_RESOURCE = "nt:resource".intern() ;
  
  public static final String JCR_LASTMODIFIED = "jcr:lastModified".intern() ;
  public static final String JCR_CONTENT = "jcr:content".intern() ;
  public static final String JCR_MIMETYPE = "jcr:mimeType".intern() ;
  public static final String JCR_DATA = "jcr:data".intern() ;
  
  public static final String MIMETYPE_TEXTPLAIN = "text/plain".intern() ;
  public static final String MIMETYPE_TEXTHTML = "text/html".intern() ;
  public static final String ATTACHMENT = "ATTACHMENT".intern();
  public static final String INLINE = "INLINE".intern();
  
  public static final String EXO_FROM_CONDITION = "exo:fromCondition".intern();
  public static final String EXO_TO_CONDITION = "exo:toCondition".intern();
  public static final String EXO_SUBJECT_CONDITION = "exo:subjectCondition".intern();
  public static final String EXO_BODY_CONDITION = "exo:bodyCondition".intern();
  public static final String EXO_APPLY_TAG = "exo:applyTag".intern();
  public static final String EXO_APPLY_FOLDER = "exo:applyFolder".intern();
  public static final String EXO_KEEP_IN_INBOX = "exo:keepInbox".intern();
  public static final String EXO_APPLY_FOR_ALL = "exo:applyForAll".intern();
  
  public static final int CONDITION_CONTAIN = 0 ;
  public static final int CONDITION_NOT_CONTAIN = 1 ;
  public static final int CONDITION_IS = 2 ;
  public static final int CONDITION_NOT_IS = 3 ;
  public static final int CONDITION_STARTS_WITH = 4 ;
  public static final int CONDITION_ENDS_WITH = 5 ;
  
  public static final String POP3 = "pop3".intern() ;
  public static final String IMAP = "imap".intern() ;
  
  public static final String FD_INBOX = "Inbox".intern();
  public static final String FD_DRAFTS = "Drafts".intern() ;
  public static final String FD_SENT = "Sent".intern() ;
  public static final String FD_SPAM = "Spam".intern() ;
  public static final String FD_TRASH = "Trash".intern() ;
  
  public static final String P_HEAD = "Head".intern() ;
  public static final String P_FOOT = "Foot".intern() ;
  
  public static final String HEADER_IN_REPLY_TO = "In-Reply-To".intern() ;
  public static final String HEADER_REFERENCES = "References".intern() ;
  public static final long PRIORITY_HIGH = 1 ;
  public static final long PRIORITY_NORMAL = 3 ;
  public static final long PRIORITY_LOW = 5 ;
  
  public static final String TAG_RED = "Red".intern() ;
  public static final String TAG_BLUE = "Blue".intern() ;
  public static final String TAG_GREEN = "Green".intern() ;
  public static final String TAG_BROWN = "Brown".intern() ;
  public static final String TAG_ORANGE = "Orange".intern() ;
  public static final String TAG_PING = "Ping".intern() ;
  public static final String TAG_PING_VIOLET = "PingViolet".intern() ;
  public static final String TAG_VIOLET = "Violet".intern() ;
  public static final String TAG_YELLOW = "Yellow".intern() ;
  public static final String[] TAG_COLOR = {TAG_RED, TAG_BLUE, TAG_GREEN, TAG_BROWN, TAG_ORANGE, TAG_PING, TAG_YELLOW, TAG_VIOLET};
  public static final String[] MIME_MAIL_TYPES = {"eml"};
  
  // TODO
  public static final byte NO_MAIL_DUPLICATE = 0;
  public static final byte MAIL_DUPLICATE_IN_SAME_FOLDER = 1;
  public static final byte MAIL_DUPLICATE_IN_OTHER_FOLDER = 2;
  
  public static final boolean SHOWCONVERSATION = true ;
  
  public static boolean isEmptyField(String value) {
    return value == null || value.trim().length() == 0 ;
  }
  
  public static boolean checkConnection(Account acc) throws Exception {
    try {
      String protocol = acc.getProtocol();
      Properties props = System.getProperties();
      String socketFactoryClass = "javax.net.SocketFactory";
      if (acc.isIncomingSsl()) socketFactoryClass = Utils.SSL_FACTORY;

      if(protocol.equals(Utils.POP3)) {
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        props.setProperty( "mail.pop3.socketFactory.class", socketFactoryClass);
      } else if (protocol.equals(Utils.IMAP)) {
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.socketFactory.class", socketFactoryClass);
      }
      Session session = Session.getInstance(props, null) ;
      URLName url = new URLName(acc.getProtocol(), acc.getIncomingHost(), Integer.valueOf(acc.getIncomingPort()), null, acc.getIncomingUser(), acc.getIncomingPassword());
      Store store = session.getStore(url) ;
      store.connect();
    } catch(Exception e) {
      return false;
    }
    return true ;
  }
  
  public static boolean isNumber(String number) {
    try {
      Long.parseLong(number.trim()) ;
    } catch(Exception nfe) {
      return false;
    }
    return true ;
  }
  
  public static String formatDate(String format, Date date) {
    Format formatter = new SimpleDateFormat(format);
    return formatter.format(date);
  }
  
  public static String getPersonal(InternetAddress address) throws Exception {
    String personal = "";
    if (address != null) {
      if (address.getPersonal() != null && address.getPersonal() != "") {
        personal = address.getPersonal();
      } else { 
        personal = address.getAddress(); 
      }
    }
    return personal;
  }
  
  public static String[] getAddresses(String addressList) throws Exception {
    String[] strs = new String[1];
    try {
      InternetAddress[] internetAddresses = getInternetAddress(addressList);
      strs = new String[internetAddresses.length];
      for (int i = 0; i < internetAddresses.length; i++ ) {
        strs[i] = internetAddresses[i].getAddress();
      }
    } catch(Exception e) {
      strs[0] = "";
    }
    return strs;
  }
  
  public static String[] getAllRecipients(javax.mail.Message msg) throws Exception {
    return getAddresses(InternetAddress.toString(msg.getFrom()) + "," + InternetAddress.toString(msg.getAllRecipients()));
  }
  
  public static Map<String, String> getAddressMap(String addressList) throws Exception { 
    InternetAddress[] internetAddresses = getInternetAddress(addressList);
    Map<String, String> addressMap = new HashMap<String, String>() ;
    for (int i = 0; i < internetAddresses.length; i++ ) {
      String address = internetAddresses[i].getAddress();
      addressMap.put(address, address);
    }
    return addressMap;
  }
  
  public static InternetAddress[] getInternetAddress(String addressList) throws Exception {
    if (addressList == null || addressList == "") 
      return new InternetAddress[1];
    try {
      return InternetAddress.parse(addressList);
    } catch (Exception e) {
      return new InternetAddress[1];
    }
  }
  
  public static String createFolderId(String accountId, String folderName, boolean isPersonal) {
    String folderId = accountId + "DefaultFolder" + folderName;
    if (isPersonal) folderId = accountId + "UserFolder" + folderName;
    return folderId;
  }
  
  public static javax.mail.internet.MimeMessage mergeToMimeMessage(Message message, javax.mail.internet.MimeMessage mimeMessage) throws Exception {
    InternetAddress addressFrom = null;
    if (message.getFrom() != null) { 
      addressFrom = new InternetAddress(message.getFrom());
    }
    mimeMessage.setFrom(addressFrom);
    
    if(message.getMessageTo() != null) { 
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message.getMessageTo()));
    }
    if(message.getMessageCc() != null) { 
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(message.getMessageCc(), true));
    }
    if(message.getMessageBcc() != null) {    
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.BCC, InternetAddress.parse(message.getMessageBcc(), false));
    }
    if(message.getReplyTo() != null) {    
      mimeMessage.setReplyTo(Utils.getInternetAddress(message.getReplyTo()));
    }
    String subject = message.getSubject();
    if (subject == null ) subject = "";
    mimeMessage.setSubject(subject);
    mimeMessage.setSentDate(message.getSendDate());
    mimeMessage.setHeader("X-Priority", String.valueOf(message.getPriority()));
    String priority = "Normal";
    if (message.getPriority() == Utils.PRIORITY_HIGH) {
      priority = "High";
    } else if (message.getPriority() == Utils.PRIORITY_LOW) {
      priority = "Low";
    }     
    if (message.getPriority() != 0 ) mimeMessage.setHeader("Importance", priority);    
    Iterator iter = message.getHeaders().keySet().iterator() ;
    while (iter.hasNext()) {
      String key = iter.next().toString() ;
      mimeMessage.setHeader(key, message.getHeaders().get(key)) ;
    }
    
    MimeMultipart  mixedPart = new MimeMultipart("mixed");
    MimeMultipart  alternativePart = new MimeMultipart("alternative");
    
    String contentType = "text/plain" ;
    if (message.getContentType().toLowerCase().indexOf("text/html") > -1) contentType = "text/html" ;
    List<Attachment> attachList = message.getAttachments();
    if (attachList != null && attachList.size() > 0) {
      MimeBodyPart contentPartRoot = new MimeBodyPart();
      MimeBodyPart textBodyPart = new MimeBodyPart();
      textBodyPart.setContent(html2text(decodeHTML(message.getMessageBody())), "text/plain");
      alternativePart.addBodyPart(textBodyPart);
      
      MimeBodyPart  htmlBodyPart = new MimeBodyPart();
      htmlBodyPart.setContent(text2html(decodeHTML(message.getMessageBody())), "text/html");
      alternativePart.addBodyPart(htmlBodyPart);
      
      contentPartRoot.setContent(alternativePart);
      mixedPart.addBodyPart(contentPartRoot);
      
      for (Attachment att : attachList) {
        InputStream is = att.getInputStream();
        MimeBodyPart attachPart = new MimeBodyPart();
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(is, att.getMimeType());
        attachPart.setDataHandler(new DataHandler(byteArrayDataSource));

        attachPart.setDisposition(Part.ATTACHMENT);
        attachPart.setFileName(att.getName());
        mixedPart.addBodyPart(attachPart);
      }
      mimeMessage.setContent(mixedPart);
    } else if (message.getContentType() != null && contentType.equals("text/plain")) {
      mimeMessage.setText(html2text(decodeHTML(message.getMessageBody())));
    } else {
      mimeMessage.setContent(message.getMessageBody(), "text/html");
    }
    
    return mimeMessage ;
  }
  
  public static String encodeJCRTextSearch(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").
    replaceAll("'", "&apos;").replaceAll("\"", "&quot;") ;
  }
  
  public static String encodeHTML(String htmlContent) throws Exception {
    return (!isEmptyField(htmlContent)) ? htmlContent.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
        .replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&#39;") : "" ;
  }
  
  public static String decodeHTML(String htmlContent) throws Exception {
    return (!isEmptyField(htmlContent)) ? htmlContent.replaceAll("&amp;", "&").replaceAll( "&quot;", "\"")
        .replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("&#39;", "'") : "" ;
  }
  
  public static String decodeText(String str) throws Exception {
    if (isEmptyField(str)) 
      return str ;
    //TODO : khdung
    try {
      String ret = MimeUtility.decodeText(str) ;
      return ret;
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      StringBuffer sb = sw.getBuffer();
      logger.error(sb.toString());
    }
    return str;  
  } 
  
  public static String html2text(String str) throws Exception {
    if (str != null) {
      str = str.replaceAll("<[^>]*>", "");
      str = str.replaceAll("&nbsp;", "");
      str = str.replaceAll("&quot;", "\"");
    } else {
      str = "" ;
    }
    return str;
  }
  
  public static String text2html(String str) throws Exception {
    if (str != null) {
      str = str.replaceAll("\n", "<br>");
    } else {
      str = "" ;
    }
    return str;
  }
  
  // TODO : khdung
  
  public static void setMailService(MailService mailService) {
    mailService_ = mailService;
  }
  public static void setScheduleService(JobSchedulerService schedulerService) {
    schedulerService_ = schedulerService;
  }
  public static MailService getMailService() {
    return mailService_;
  }
  public static JobSchedulerService getJobSchedulerService() {
    return schedulerService_;
  }

  public static String convertSize(long size) throws Exception {
    String str = "";
    DecimalFormat df = new DecimalFormat("0.00");
    if (size > 1024 * 1024) str += df.format(((double) size)/(1024 * 1024)) + " MB" ;
    else if (size > 1024) str += df.format(((double) size)/(1024)) + " KB" ;
    else str += size + " B" ;
    return str ;
  } 
}
