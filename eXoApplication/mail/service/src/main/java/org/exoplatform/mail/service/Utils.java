/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 20, 2007  
 */
public class Utils {
  
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
  
  public static final String KEY_FOLDERS = "Folders".intern() ;
  public static final String KEY_MESSAGE = "Messages".intern() ;
  public static final String KEY_TAGS = "Tags".intern() ;
  public static final String KEY_FILTER = "Filter".intern() ;
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
  public static final String EXO_CONTENT_TYPE = "exo:contentType".intern() ;
  public static final String EXO_REPLYTO = "exo:replyto".intern();
  public static final String EXO_CC = "exo:cc".intern() ;
  public static final String EXO_BCC= "exo:bcc".intern() ;
  public static final String EXO_BODY = "exo:body".intern() ;
  public static final String EXO_SIZE = "exo:size".intern();
  public static final String EXO_STAR = "exo:star".intern() ;
  public static final String EXO_PRIORITY = "exo:priority".intern() ;
  public static final String EXO_HASATTACH = "exo:hasAttach".intern();
  public static final String EXO_TAGS = "exo:tags".intern() ;
  public static final String EXO_FOLDERS = "exo:folders".intern() ;
  public static final String EXO_PERSONAL = "exo:personal".intern() ;
  public static final String EXO_RECEIVEDDATE = "exo:receivedDate".intern() ; 
  public static final String EXO_SENDDATE = "exo:sendDate".intern() ;
  public static final String EXO_SERVERPROPERTIES = "exo:serverProperties".intern() ;
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
  public static final String EXO_ISROOT = "exo:isRoot".intern();
  public static final String EXO_ADDRESSES = "exo:addresses".intern();
  public static final String EXO_MESSAGEIDS = "exo:messageIds".intern();
  public static final String EXO_ROOT = "exo:root".intern();
  

  public static final String EXO_MAIL_SETTING = "exo:mailSetting".intern();
  public static final String EXO_NUMBER_OF_CONVERSATION = "exo:showNumberOfConversation".intern();
  public static final String EXO_FORMAT_WHEN_REPLYFORWARD = "exo:formatWhenReplyForward".intern();
  public static final String EXO_REPLY_MESSAGE_WITH = "exo:replyMessageWith".intern();
  public static final String EXO_FORWARD_MESSAGE_WITH = "exo:forwardMessageWith".intern();
  public static final String EXO_PREFIX_MESSAGE_WITH = "exo:prefixMessageWith".intern();
  public static final String EXO_PERIOD_CHECKMAIL_AUTO = "exo:periodCheckMailAuto".intern();
  public static final String EXO_DEFAULT_ACCOUNT = "exo:defaultAccount".intern();
  public static final String EXO_EDITOR = "exo:editor".intern();
  public static final String EXO_SAVE_SENT_MESSAGE = "exo:saveMessageInSent".intern();
  public static final String EXO_FILTER = "exo:filter".intern() ;
  
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
  
  public static String getPersonal(InternetAddress address) throws Exception {
    String personal = "";
    if (address.getPersonal() != null && address.getPersonal() != "") {
      personal = address.getPersonal();
    } else { 
      personal = address.getAddress(); 
    }
    return personal;
  }
  
  public static String[] getAddresses(String addressList)  throws Exception { 
    InternetAddress[] internetAddresses = getInternetAddress(addressList);
    String[] strs = new String[internetAddresses.length];
    for (int i = 0; i < internetAddresses.length; i++ ) {
      strs[i] = internetAddresses[i].getAddress();
    }
    return strs;
  }
  
  public static InternetAddress[] getInternetAddress(String addressList) throws Exception {
    if (addressList == null || addressList == "") 
      return new InternetAddress[1];
    return InternetAddress.parse(addressList);
  }
  
  public static String createFolderId(String accountId, String folderName, boolean isPersonal) {
    String folderId = accountId + "DefaultFolder" + folderName;
    if (isPersonal) folderId = accountId + "UserFolder" + folderName;
    return folderId;
  }
  
  public static javax.mail.internet.MimeMessage mergeToMimeMessage(Message message, javax.mail.internet.MimeMessage mimeMessage) throws Exception {
    InternetAddress addressFrom = new InternetAddress(message.getFrom());
    mimeMessage.setFrom(addressFrom);
    mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message.getMessageTo()));
    if(message.getMessageCc() != null) {
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(message.getMessageCc(), true));
    }
    if(message.getMessageBcc() != null) {   
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.BCC, InternetAddress.parse(message.getMessageBcc(), false));
    }
    mimeMessage.setSubject(message.getSubject());
    mimeMessage.setSentDate(message.getSendDate());
    
    MimeMultipart  multipPartRoot = new MimeMultipart("mixed");
    
    MimeMultipart  multipPartContent = new MimeMultipart("alternative");
    
    MimeBodyPart contentPartRoot = new MimeBodyPart();
    contentPartRoot.setContent(multipPartContent);

    MimeBodyPart  mimeBodyPart1 = new MimeBodyPart();
    mimeBodyPart1.setContent(message.getMessageBody(), "text/html");
    multipPartContent.addBodyPart(mimeBodyPart1);
    
    multipPartRoot.addBodyPart(contentPartRoot);
    
    List<Attachment> attachList = message.getAttachments();
    if (attachList != null) {
      for (Attachment att : attachList) {
        JCRMessageAttachment attach = (JCRMessageAttachment) att;
        InputStream is = attach.getInputStream();

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(is, att.getMimeType());
        mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

        mimeBodyPart.setDisposition(Part.ATTACHMENT);
        mimeBodyPart.setFileName(attach.getName());
        multipPartRoot.addBodyPart(mimeBodyPart);
      }        
    }
    mimeMessage.setHeader("X-Priority", String.valueOf(message.getPriority()));
    String priority = "Normal";
    if (message.getPriority() == Utils.PRIORITY_HIGH) {
      priority = "High";
    } else if (message.getPriority() == Utils.PRIORITY_LOW) {
      priority = "Low";
    }     
    if (message.getPriority() != 0 ) mimeMessage.setHeader("Importance", priority);
    
    mimeMessage.setContent(multipPartRoot);
    
    return mimeMessage;
  }
  
  public static Message mergeFromMimeMessage(Message message, javax.mail.Message mimeMessage) throws Exception {
    Calendar gc = GregorianCalendar.getInstance();
    Date receivedDate = gc.getTime();
    message.setMessageTo(InternetAddress.toString(mimeMessage.getRecipients(javax.mail.Message.RecipientType.TO)));
    message.setMessageCc(InternetAddress.toString(mimeMessage.getRecipients(javax.mail.Message.RecipientType.CC)));
    message.setMessageBcc(InternetAddress.toString(mimeMessage.getRecipients(javax.mail.Message.RecipientType.BCC)));
    message.setSubject(mimeMessage.getSubject());
    message.setContentType(mimeMessage.getContentType());
    message.setFrom(InternetAddress.toString(mimeMessage.getFrom()));
    message.setReplyTo(InternetAddress.toString(mimeMessage.getReplyTo()));
    message.setReceivedDate(receivedDate);
    message.setSendDate(mimeMessage.getSentDate());
    message.setSize(mimeMessage.getSize());
    message.setUnread(true);
    message.setHasStar(false);       
    message.setPriority(Utils.PRIORITY_NORMAL);
    String[] xPriority = mimeMessage.getHeader("X-Priority");
    String[] importance = mimeMessage.getHeader("Importance");
    
    if (xPriority != null && xPriority.length > 0) {
      for (int j = 0 ; j < xPriority.length; j++) {
        message.setPriority(Long.valueOf(mimeMessage.getHeader("X-Priority")[j].substring(0,1)));
      }          
    }
    
    if (importance != null && importance.length > 0) {
      for (int j = 0 ; j < importance.length; j++) {
        if (importance[j].equalsIgnoreCase("Low")) {
          message.setPriority(Utils.PRIORITY_LOW);
        } else if (importance[j].equalsIgnoreCase("high")) {
          message.setPriority(Utils.PRIORITY_HIGH);
        } 
      }
    }
    
    message.setAttachements(new ArrayList<Attachment>());
    
    Object obj = mimeMessage.getContent() ;
    if (obj instanceof Multipart) {
      setMultiPart((Multipart)obj, message);
    } else {
      setPart(mimeMessage, message);
    }
    return message;
  }
  
  private static void setMultiPart(Multipart multipart, Message newMail) {
    try {
      int i = 0 ;
      int n = multipart.getCount() ;
      while( i < n) {
        setPart(multipart.getBodyPart(i), newMail);
        i++ ;
      }     
    }catch(Exception e) {
      e.printStackTrace() ;
    }   
  }

  private static void setPart(Part part, Message newMail){
    try {
      String disposition = part.getDisposition();
      String contentType = part.getContentType();
      if (disposition == null) {
        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
          newMail.setMessageBody((String)part.getContent());
        } else {
          MimeMultipart mimeMultiPart = (MimeMultipart)part.getContent() ;
          for (int i=0; i<mimeMultiPart.getCount();i++) {
            // for each part, set the body content
            setPart(mimeMultiPart.getBodyPart(i), newMail);
          }
        }
      } else {
        if (disposition.equalsIgnoreCase(Part.INLINE)) {
          /* this must be presented INLINE, hence inside the body of the message */
          if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
            newMail.setMessageBody((String)part.getContent());
          }
        } else {
          /* this part must be presented as an attachment, hence we add it to the attached files */
          BufferAttachment file = new BufferAttachment();
          file.setId("Attachment" + IdGenerator.generate());
          file.setName(part.getFileName());
          InputStream is = part.getInputStream();
          file.setInputStream(is);
          file.setSize(is.available());
          if (contentType.indexOf(";") > 0) {
            String[] type = contentType.split(";") ;
            file.setMimeType(type[0]);
          } else {
            file.setMimeType(contentType) ;
          }
          newMail.getAttachments().add(file);
        }
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
}
