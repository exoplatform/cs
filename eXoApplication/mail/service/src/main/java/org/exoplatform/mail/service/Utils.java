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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.jcr.Node;
import javax.jcr.Value;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.gatein.common.util.ParameterValidation;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 20, 2007  
 */
public class Utils {

  private static final Log          logger                                = ExoLogger.getLogger("cs.mail.service");

  public static MailService         mailService_;

  public static JobSchedulerService schedulerService_;
  
  /**
   * {@literal
   * Javamail uses property names are always in form mail.<protocol>.<property>, 
   * therefore this string is used as prefix part of mail property names.
   * }
   */
  public static final String        SVR_MAIL                              = "mail";

  public static final String        SVR_IMAP                              = "imap";

  public static final String        SVR_IMAPS                             = "imaps";

  public static final String        IMAP_SSL_FACTORY                      = "mail.imap.ssl.socketFactory".intern();

  public static final String        SVR_IMAP_STARTTLS_REQUIRED            = "mail.imap.starttls.required".intern();

  public static final String        MAIL_IMAP_SSL_ENABLE                  = "mail.imap.ssl.enable".intern();

  public static final String        IMAP_CONECT_TIMEOUT                   = "mail.imap.connectiontimeout".intern();

  public static final String        IMAP_SSL_STARTTLS_ENABLE              = "mail.imap.starttls.enable".intern();

  public static final String        IMAP_SASL_MECHS                       = "mail.imap.sasl.mechanisms".intern();

  public static final String        POP3_SSL_STARTTLS_ENABLE              = "mail.pop3.starttls.enable".intern();

  public static final String        SVR_POP3S                             = "pop3s";

  public static final String        POP3_SSL_FACTORY                      = "mail.pop3.ssl.socketFactory".intern();

  public static final String        SVR_POP3_STARTTLS_REQUIRED            = "mail.pop3.starttls.required".intern();

  public static final String        MAIL_POP3_SSL_ENABLE                  = "mail.pop3.ssl.enable".intern();

  public static final String        POP3_CONECT_TIMEOUT                   = "mail.pop3.connectiontimeout".intern();

  // in MS Exchange, a big message maybe insufficience of bytes when received. set it is false to get rid this problem
  // But neet to certain that Mail server do not implement Imap Partial FETCH
  public static final String        IMAP_MSX_PARTIAL_FETCH                = "mail.imap.partialfetch".intern();
  
  public static final String        SVR_SMTP                              = "smtp";

  public static final String        SVR_SMTPS                             = "smtps";
  
  public static final String        SVR_TRANSPORT_PROTOCOL                = "mail.transport.protocol";

  public static final String        SVR_SMTP_HOST                         = "host".intern();

  public static final String        SVR_SMTP_PORT                         = "port".intern();

  public static final String        SVR_SMTP_USER                         = "user".intern();

  public static final String        SVR_SMTP_PASSWORD                     = "user".intern();

  public static final String        SVR_SMTP_AUTH                         = "auth".intern();

  public static final String        SVR_SMTP_SOCKET_FACTORY_FALLBACK      = "socketFactory.fallback".intern();

  public static final String        SVR_SMTP_SOCKET_FACTORY_PORT          = "socketFactory.port".intern();

  public static final String        SVR_SMTP_SOCKET_FACTORY_CLASS         = "socketFactory.class".intern();

  public static final String        SVR_SMTP_SSL_SOCKET_FACTORY_CLASS     = "ssl.socketFactory.class";

  public static final String        SVR_SMTP_SSL_SOCKET_FACTORY_PORT      = "ssl.socketFactory.port";

  public static final String        SVR_SMTP_STARTTLS_REQUIRED            = "starttls.required".intern();

  public static final String        SMTP_SSL_FACTORY                      = "ssl.socketFactory".intern();

  public static final String        SVR_SMTP_SSL_ENABLE                  = "ssl.enable".intern();

  public static final String        SMTP_QUIT_WAIT                        = "quitwait".intern();
  
  public static final String        SMTP_SSL_PROTOCOLS                    = "ssl.protocols".intern();

  public static final String        SMTP_CONECT_TIMEOUT                   = "connectiontimeout".intern();

  public static final String        SVR_MAIL_SMTP_DEBUG                   = "debug".intern();

  public static final String        SMTP_ISAUTHENTICATION                 = "smtp.isauthentication".intern();

  public static final String        SMTP_USEINCOMINGSETTING               = "smtp.useincomingsetting".intern();

  public static final String        SMTP_DNS_NOTIFY                       = "dsn.notify".intern();

  public static final String        SMTP_DNS_RET                          = "dsn.ret".intern();

  public static final String        SMTP_TIMEOUT                          = "timeout";

  public static final String        SMATP_SSL_STARTTLS_ENABLE             = "starttls.enable".intern();

  public static final String        SMTP_AUTH_MECHS                       = "auth.mechanisms".intern();

  public static final String        SSL_FACTORY                           = "javax.net.ssl.SSLSocketFactory".intern();

  public static final String        SVR_SSL_CLASSNAME                     = "javax.net.ssl.SSLSocketFactory".intern();

  public static final String        SOCKET_FACTORY                        = "javax.net.SocketFactory".intern();

  public static final String        SVR_MAIL_DEBUG                        = "mail.debug".intern();

  public static final String        SVR_SMTP_STARTTLS_ENABLE              = "starttls.enable".intern();

  public static final String        SVR_PROTOCOL                          = "protocol".intern();

  public static final String        SVR_INCOMING_HOST                     = "incoming.host".intern();

  public static final String        SVR_INCOMING_PORT                     = "incoming.port".intern();

  public static final String        SVR_INCOMING_FOLDER                   = "folder".intern();

  public static final String        SVR_IS_CUSTOM_INBOX                   = "isCustomInbox".intern();

  public static final String        SVR_INCOMING_USERNAME                 = "incoming.username".intern();

  public static final String        SVR_INCOMING_PASSWORD                 = "incoming.password".intern();

  public static final String        SVR_OUTGOING_HOST                     = "outgoing.host".intern();

  public static final String        SVR_OUTGOING_PORT                     = "outgoing.port".intern();

  public static final String        SVR_OUTGOING_USERNAME                 = "outgoing.username".intern();

  public static final String        SVR_OUTGOING_PASSWORD                 = "outgoing.password".intern();

  public static final String        SVR_INCOMING_SSL                      = "ssl".intern();

  public static final String        SVR_OUTGOING_SSL                      = "outgoing.ssl".intern();

  public static final String        SVR_LEAVE_ON_SERVER                   = "leave.on.server".intern();

  public static final String        SVR_POP_SKIP_OVER_SIZE                = "skip.over.size".intern();

  public static final String        KEY_FOLDERS                           = "Folders".intern();

  public static final String        KEY_MESSAGE                           = "Messages".intern();

  public static final String        KEY_TAGS                              = "Tags".intern();

  public static final String        KEY_FILTER                            = "Filter".intern();

  public static final String        KEY_MAIL_SETTING                      = "MailSetting".intern();

  public static final String        KEY_SPAM_FILTER                       = "SpamFilter".intern();

  public static final String        KEY_ACCOUNT                           = "account".intern();

  public static final String        KEY_HEADER                            = "mailHeader".intern();

  public static final String        KEY_CONVERSATION                      = "Conversation".intern();

  public static final String        KEY_ATTACHMENT                        = "attachment".intern();

  public static final String        EXO_ACCOUNT                           = "exo:account".intern();

  public static final String        EXO_ID                                = "exo:id".intern();

  public static final String        EXO_UID                               = "exo:uid".intern();

  public static final String        EXO_IN_REPLY_TO_HEADER                = "exo:inReplyToHeader".intern();

  public static final String        EXO_LABEL                             = "exo:label".intern();

  public static final String        EXO_USERDISPLAYNAME                   = "exo:userDisplayName".intern();

  public static final String        EXO_EMAILADDRESS                      = "exo:emailAddress".intern();

  public static final String        EXO_REPLYEMAIL                        = "exo:emailReplyAddress".intern();

  public static final String        EXO_SIGNATURE                         = "exo:signature".intern();

  public static final String        EXO_DESCRIPTION                       = "exo:description".intern();

  public static final String        EXO_FROM                              = "exo:from".intern();

  public static final String        EXO_FROMS                             = "exo:froms".intern();

  public static final String        EXO_TO                                = "exo:to".intern();

  public static final String        EXO_SUBJECT                           = "exo:subject".intern();

  public static final String        EXO_CONTENT_TYPE                      = "exo:contentType".intern();

  public static final String        EXO_REPLYTO                           = "exo:replyto".intern();

  public static final String        EXO_CC                                = "exo:cc".intern();

  public static final String        EXO_BCC                               = "exo:bcc".intern();

  public static final String        EXO_BODY                              = "exo:body".intern();

  public static final String        EXO_SIZE                              = "exo:size".intern();

  public static final String        EXO_STAR                              = "exo:star".intern();

  public static final String        EXO_PRIORITY                          = "exo:priority".intern();

  public static final String        EXO_HASATTACH                         = "exo:hasAttach".intern();

  public static final String        IS_LOADED                             = "exo:isLoaded".intern();

  public static final String        ATT_IS_LOADED_PROPERLY                = "exo:isLoadedProperly".intern();

  public static final String        ATT_IS_SHOWN_IN_BODY                  = "exo:isShownInBody".intern();

  public static final String        IS_RETURN_RECEIPT                     = "exo:isReturnReceipt".intern();

  public static final String        EXO_TAGS                              = "exo:tags".intern();

  public static final String        MSG_FOLDERS                           = "exo:folders".intern();

  public static final String        MSG_HEADERS                           = "exo:headers".intern();

  public static final String        EXO_PERSONAL                          = "exo:personal".intern();

  public static final String        EXO_RECEIVEDDATE                      = "exo:receivedDate".intern();

  public static final String        EXO_SENDDATE                          = "exo:sendDate".intern();

  public static final String        EXO_SERVERPROPERTIES                  = "exo:serverProperties".intern();

  public static final String        EXO_SMTPSERVERPROPERTIES              = "exo:smtpServerProperties".intern();

  public static final String        EXO_MESSAGE                           = "exo:message".intern();

  public static final String        EXO_ISUNREAD                          = "exo:isUnread".intern();

  public static final String        EXO_TOTALMESSAGE                      = "exo:totalMessages".intern();

  public static final String        EXO_FOLDER                            = "exo:folder".intern();

  public static final String        EXO_UNREADMESSAGES                    = "exo:unreadMessages".intern();

  public static final String        EXO_FOLDERTYPE                        = "exo:folderType".intern();

  public static final String        EXO_NAME                              = "exo:name".intern();

  public static final String        EXO_MAILTAG                           = "exo:mailtag".intern();

  public static final String        EXO_COLOR                             = "exo:color".intern();

  public static final String        EXO_CHECKMAILAUTO                     = "exo:checkMailAuto".intern();

  public static final String        EXO_EMPTYTRASH                        = "exo:emptyTrash".intern();

  public static final String        EXO_PLACESIGNATURE                    = "exo:placeSignature".intern();

  public static final String        EXO_SPAM_FILTER                       = "exo:spamFilter".intern();

  public static final String        EXO_CONVERSATION                      = "exo:conversation".intern();

  public static final String        EXO_CONVERSATIONID                    = "exo:conversationId".intern();

  public static final String        EXO_LAST_START_CHECKING_TIME          = "exo:lastStartCheckingTime".intern();

  public static final String        EXO_LAST_CHECKED_TIME                 = "exo:lastCheckedTime".intern();

  public static final String        EXO_CHECK_ALL                         = "exo:checkAll".intern();

  public static final String        EXO_CHECK_FROM_DATE                   = "exo:checkFromDate".intern();

  public static final String        EXO_IS_SAVE_PASSWORD                  = "exo:isSavePassword".intern();

  public static final String        EXO_MAIL_ATTACHMENT                   = "exo:mailAttachment".intern();

  public static final String        EXO_ATT_NAME                          = "exo:fileName".intern();

  public static final String        EXO_IS_ROOT                           = "exo:isRoot".intern();

  public static final String        EXO_LAST_UPDATE_TIME                  = "exo:lastUpdateTime".intern();

  public static final String        EXO_SECURE_AUTHS_INCOMING             = "exo:secureAuthsIncoming".intern();

  public static final String        EXO_SECURE_AUTHS_OUTGOING             = "exo:secureAuthsOutgoing".intern();

  public static final String        EXO_AUTH_MECHS_INCOMING               = "exo:authMechsIncoming".intern();

  public static final String        EXO_AUTH_MECHS_OUTGOING               = "exo:authMechsOutgoing".intern();

  public static final String        EXO_MAIL_SETTING                      = "exo:mailSetting".intern();

  public static final String        EXO_NUMBER_MSG_PER_PAGE               = "exo:numberMsgPerPage".intern();

  public static final String        EXO_FORMAT_AS_ORIGINAL                = "exo:formatAsOriginal".intern();

  public static final String        EXO_REPLY_WITH_ATTACH                 = "exo:replyWithAttach".intern();

  public static final String        EXO_FORWARD_WITH_ATTACH               = "exo:forwardWithAttach".intern();

  public static final String        EXO_PREFIX_MESSAGE_WITH               = "exo:prefixMsgWith".intern();

  public static final String        EXO_PERIOD_CHECKMAIL_AUTO             = "exo:periodCheckAuto".intern();

  public static final String        EXO_DEFAULT_ACCOUNT                   = "exo:defaultAccount".intern();

  public static final String        EXO_USE_WYSIWYG                       = "exo:useWysiwyg".intern();

  public static final String        EXO_SAVE_SENT_MESSAGE                 = "exo:saveMsgInSent".intern();

  public static final String        EXO_FILTER                            = "exo:filter".intern();

  public static final String        EXO_PATH                              = "exo:path".intern();

  public static final String        EXO_LAYOUT                            = "exo:layout".intern();

  public static final String        EXO_RETURN_RECEIPT                    = "exo:returnReceipt".intern();

  public static final String        NT_UNSTRUCTURED                       = "nt:unstructured".intern();

  public static final String        NT_FILE                               = "nt:file".intern();

  public static final String        NT_RESOURCE                           = "nt:resource".intern();

  public static final String        JCR_LASTMODIFIED                      = "jcr:lastModified".intern();

  public static final String        JCR_CONTENT                           = "jcr:content".intern();

  public static final String        JCR_MIMETYPE                          = "jcr:mimeType".intern();

  public static final String        JCR_DATA                              = "jcr:data".intern();

  public static final String        MIMETYPE_TEXTPLAIN                    = "text/plain".intern();

  public static final String        MIMETYPE_TEXTHTML                     = "text/html".intern();

  public static final String        ATTACHMENT                            = "ATTACHMENT".intern();

  public static final String        INLINE                                = "INLINE".intern();

  public static final String        EXO_FROM_CONDITION                    = "exo:fromCondition".intern();

  public static final String        EXO_TO_CONDITION                      = "exo:toCondition".intern();

  public static final String        EXO_SUBJECT_CONDITION                 = "exo:subjectCondition".intern();

  public static final String        EXO_BODY_CONDITION                    = "exo:bodyCondition".intern();

  public static final String        EXO_APPLY_TAG                         = "exo:applyTag".intern();

  public static final String        EXO_APPLY_FOLDER                      = "exo:applyFolder".intern();

  public static final String        EXO_KEEP_IN_INBOX                     = "exo:keepInbox".intern();

  public static final String        EXO_APPLY_FOR_ALL                     = "exo:applyForAll".intern();

  public static final int           CONDITION_CONTAIN                     = 0;

  public static final int           CONDITION_NOT_CONTAIN                 = 1;

  public static final int           CONDITION_IS                          = 2;

  public static final int           CONDITION_NOT_IS                      = 3;

  public static final int           CONDITION_STARTS_WITH                 = 4;

  public static final int           CONDITION_ENDS_WITH                   = 5;

  public static final String        POP3                                  = "pop3".intern();

  public static final String        IMAP                                  = "imap".intern();

  public static final String        FD_INBOX                              = "Inbox".intern();

  public static final String        FD_DRAFTS                             = "Drafts".intern();

  public static final String        FD_SENT                               = "Sent".intern();

  public static final String        FD_SPAM                               = "Spam".intern();

  public static final String        FD_TRASH                              = "Trash".intern();

  public static final String[]      DEFAULT_FOLDERS                       = new String[] { FD_INBOX, FD_DRAFTS, FD_SENT, FD_SPAM, FD_TRASH };

  public static final String        P_HEAD                                = "Head".intern();

  public static final String        P_FOOT                                = "Foot".intern();

  public static final String        HEADER_IN_REPLY_TO                    = "In-Reply-To".intern();

  public static final String        HEADER_REFERENCES                     = "References".intern();

  public static final long          PRIORITY_HIGH                         = 1;

  public static final long          PRIORITY_NORMAL                       = 3;

  public static final long          PRIORITY_LOW                          = 5;

  public static final String        TAG_RED                               = "Red".intern();

  public static final String        TAG_BLUE                              = "Blue".intern();

  public static final String        TAG_GREEN                             = "Green".intern();

  public static final String        TAG_BROWN                             = "Brown".intern();

  public static final String        TAG_ORANGE                            = "Orange".intern();

  public static final String        TAG_PING                              = "Ping".intern();

  public static final String        TAG_PING_VIOLET                       = "PingViolet".intern();

  public static final String        TAG_VIOLET                            = "Violet".intern();

  public static final String        TAG_YELLOW                            = "Yellow".intern();

  public static final String[]      TAG_COLOR                             = { TAG_RED, TAG_BLUE, TAG_GREEN, TAG_BROWN, TAG_ORANGE, TAG_PING, TAG_YELLOW, TAG_VIOLET };

  public static final String[]      MIME_MAIL_TYPES                       = { "eml" };

  public static final String[]      NOT_SUPPORTED_CHARSETS                = { "koi8-r" };

  // TODO
  public static final byte          NO_MAIL_DUPLICATE                     = 0;

  public static final byte          MAIL_DUPLICATE_IN_SAME_FOLDER         = 1;

  public static final byte          MAIL_DUPLICATE_IN_OTHER_FOLDER        = 2;

  public static final boolean       SHOWCONVERSATION                      = true;

  public static final String        TLS_SSL                               = "ssl/tls";                                                                                 // is SSL

  public static final String        STARTTLS                              = "starttls";

  public static final String        NTLM                                  = "ntlm";

  public static final String        PLAIN                                 = "plain";

  public static final String        LOGIN                                 = "login";

  public static final String        DIGIT_MD5                             = "digest-md5";

  public static final String        CRAM_MD5                              = "cram-md5";

  public static final String        KERBEROS_GSSAPI                       = "kerberos/gssapi";                                                                         // kerberos v5

  public static final String[]      MECHANISM                             = { NTLM, PLAIN, LOGIN, DIGIT_MD5, KERBEROS_GSSAPI, CRAM_MD5 };                              // only support common those type

  public static final String        EXO_PERMISSIONS                       = "exo:permissions".intern();

  public static final String        READ_ONLY                             = "read".intern();

  public static final String        SEND_RECIEVE                          = "send".intern();

  private static final String       SERVICE_SETTINGS_NAME                 = "cs.mail.service.settings".intern();

  private static final String       LEAVE_ON_SEVER                        = "leaveOnServer".intern();

  private static final String       INCOMING_SERVER                       = "incomingServer".intern();

  private static final String       OUTGOING_SEVER                        = "outgoingServer".intern();

  private static final String       INCOMING_SERVER_PORT                  = "incomingPort".intern();

  private static final String       OUTGOING_SEVER_PORT                   = "outgoingPort".intern();

  private static final String       ACCEPT_INCOMING_SECURE_AUTHENTICATION = "acceptIncomingSecureAuthentication".intern();

  private static final String       INCOMING_SECURE_AUTHENTICATION        = "incomingSecureAuthentication".intern();

  private static final String       INCOMING_AUTHENTICATION_MECHANISM     = "incomingAuthenticationMechanism".intern();

  private static final String       ACCEPT_OUTGOING_SECURE_AUTHENTICATION = "acceptOutgoingSecureAuthentication".intern();

  private static final String       OUTGOING_SECURE_AUTHENTICATION        = "outgoingSecureAuthentication".intern();

  private static final String       OUTGOING_AUTHENTICATION_MECHANISM     = "outgoingAuthenticationMechanism".intern();

  private static String             PLUS_ENCODE                           = "PLUS_ENCODE_043";

  private static String             EQUAL_ENCODE                          = "EQUAL_ENCODE_061";

  public static boolean isEmptyField(String value) {
    return value == null || value.trim().length() == 0;
  }

  public static boolean checkConnection(Account acc) throws Exception {
    try {
      String protocol = acc.getProtocol();
      Properties props = System.getProperties();
      String socketFactoryClass = "javax.net.SocketFactory";
      if (acc.isIncomingSsl())
        socketFactoryClass = Utils.SSL_FACTORY;

      if (protocol.equals(Utils.POP3)) {
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        props.setProperty("mail.pop3.socketFactory.class", socketFactoryClass);
      } else if (protocol.equals(Utils.IMAP)) {
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.socketFactory.class", socketFactoryClass);
      }
      Session session = Session.getInstance(props, null);
      URLName url = new URLName(acc.getProtocol(), acc.getIncomingHost(), Integer.valueOf(acc.getIncomingPort()), null, acc.getIncomingUser(), acc.getIncomingPassword());
      Store store = session.getStore(url);
      store.connect();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public static boolean isNumber(String number) {
    try {
      Long.parseLong(number.trim());
    } catch (Exception nfe) {
      return false;
    }
    return true;
  }

  public static String formatDate(String format, Date date) {
    Format formatter = new SimpleDateFormat(format);
    return formatter.format(date);
  }

  public static String getPersonal(InternetAddress address) throws Exception {
    String personal = "";
    if (address != null) {
      if (!isEmptyField(address.getPersonal())) {
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
      for (int i = 0; i < internetAddresses.length; i++) {
        strs[i] = internetAddresses[i].getAddress();
      }
    } catch (Exception e) {
      strs[0] = "";
    }
    return strs;
  }

  public static String[] getAllRecipients(javax.mail.Message msg) throws Exception {
    return getAddresses(InternetAddress.toString(msg.getFrom()) + "," + InternetAddress.toString(msg.getAllRecipients()));
  }

  public static Map<String, String> getAddressMap(String addressList) throws Exception {
    InternetAddress[] internetAddresses = getInternetAddress(addressList);
    Map<String, String> addressMap = new HashMap<String, String>();
    for (int i = 0; i < internetAddresses.length; i++) {
      String address = internetAddresses[i].getAddress();
      addressMap.put(address, address);
    }
    return addressMap;
  }

  public static InternetAddress[] getInternetAddress(String addressList) throws Exception {
    if (isEmptyField(addressList))
      return new InternetAddress[1];
    try {
      return InternetAddress.parse(addressList);
    } catch (Exception e) {
      return new InternetAddress[1];
    }
  }

  public static String generateFID(String accountId, String folderName, boolean isPersonal) {
    if (isPersonal) {
      return (accountId + "UserFolder" + folderName);
    } else {
      for (int i = 0; i < DEFAULT_FOLDERS.length; i++) {
        if (folderName.equalsIgnoreCase(DEFAULT_FOLDERS[i])) {
          return (accountId + "DefaultFolder" + DEFAULT_FOLDERS[i]);
        }
      }
      return (accountId + "DefaultFolder" + folderName);
    }
  }

  public static String getFolderNameFromFolderId(String folderId) {
    int index = -1;
    if (folderId.indexOf("UserFolder") > -1)
      index = folderId.indexOf("UserFolder") + 10;
    else if (folderId.indexOf("DefaultFolder") > -1)
      index = folderId.indexOf("DefaultFolder") + 13;
    if (index > 0)
      return folderId.substring(index, folderId.length());
    else
      return "";
  }

  public static MimeMessage setHeader(MimeMessage mm, Message message) throws Exception {
    mm.setHeader("X-Priority", String.valueOf(message.getPriority()));
    String priority = "Normal";
    if (message.getPriority() == Utils.PRIORITY_HIGH) {
      priority = "High";
    } else if (message.getPriority() == Utils.PRIORITY_LOW) {
      priority = "Low";
    }
    if (message.getPriority() != 0)
      mm.setHeader("Importance", priority);
    Iterator<String> iter = message.getHeaders().keySet().iterator();
    while (iter.hasNext()) {
      String key = iter.next().toString();
      mm.setHeader(key, message.getHeaders().get(key));
    }

    return mm;
  }

  public static javax.mail.internet.MimeMessage mergeToMimeMessage(Message message, javax.mail.internet.MimeMessage mimeMessage) throws Exception {
    InternetAddress addressFrom = null;
    if (message.getFrom() != null) {
      addressFrom = new InternetAddress(message.getFrom());
    }
    mimeMessage.setFrom(addressFrom);

    if (message.getMessageTo() != null) {
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message.getMessageTo()));
    }
    if (message.getMessageCc() != null) {
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(message.getMessageCc(), true));
    }
    if (message.getMessageBcc() != null) {
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.BCC, InternetAddress.parse(message.getMessageBcc(), false));
    }
    if (message.getReplyTo() != null) {
      mimeMessage.setReplyTo(Utils.getInternetAddress(message.getReplyTo()));
    }
    String subject = message.getSubject();
    if (subject == null)
      subject = "";
    mimeMessage.setSubject(subject);
    mimeMessage.setSentDate(message.getSendDate());
    mimeMessage = setHeader(mimeMessage, message);

    MimeMultipart mixedPart = new MimeMultipart("mixed");
    MimeMultipart alternativePart = new MimeMultipart("alternative");

    String contentType = "text/plain";
    if (message.getContentType() != null && message.getContentType().toLowerCase().indexOf("text/html") > -1)
      contentType = "text/html";
    List<Attachment> attachList = message.getAttachments();
    if (attachList != null && attachList.size() > 0) {
      MimeBodyPart contentPartRoot = new MimeBodyPart();
      MimeBodyPart textBodyPart = new MimeBodyPart();
      textBodyPart.setContent(html2text(decodeHTML(message.getMessageBody())), "text/plain");
      alternativePart.addBodyPart(textBodyPart);

      MimeBodyPart htmlBodyPart = new MimeBodyPart();
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

    return mimeMessage;
  }

  public static String encodeJCRTextSearch(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;");
  }

  public static String encodeHTML(String htmlContent) throws Exception {
    return (!isEmptyField(htmlContent)) ? htmlContent.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&#39;") : "";
  }

  public static String decodeHTML(String htmlContent) throws Exception {
    return (!isEmptyField(htmlContent)) ? htmlContent.replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("&#39;", "'") : "";
  }

  public static String decodeText(String str) throws Exception {
    if (isEmptyField(str))
      return str;
    // TODO : khdung
    try {
      String ret = MimeUtility.decodeText(str);
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
      str = "";
    }
    return str;
  }

  public static String text2html(String str) throws Exception {
    if (str != null) {
      str = str.replaceAll("\n", "<br>");
    } else {
      str = "";
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
    if (size > 1024 * 1024)
      str += df.format(((double) size) / (1024 * 1024)) + " MB";
    else if (size > 1024)
      str += df.format(((double) size) / (1024)) + " KB";
    else
      str += size + " B";
    return str;
  }

  public static ContinuationService getContinuationService() throws Exception {
    ContinuationService continuation = (ContinuationService) PortalContainer.getInstance().getComponentInstanceOfType(ContinuationService.class);
    return continuation;
  }

  public static String escapeIllegalJcrChars(String name) {
    StringBuffer buffer = new StringBuffer(name.length() * 2);
    for (int i = 0; i < name.length(); i++) {
      char ch = name.charAt(i);
      if (ch == '%' || ch == '/' || ch == ':' || ch == '[' || ch == ']' || ch == '*' || ch == '\'' || ch == '"' || ch == '|' || (ch == '.' && name.length() < 3) || (ch == ' ' && (i == 0 || i == name.length() - 1)) || ch == '\t' || ch == '\r' || ch == '\n') {
        buffer.append('%');
        buffer.append(Character.toUpperCase(Character.forDigit(ch / 16, 16)));
        buffer.append(Character.toUpperCase(Character.forDigit(ch % 16, 16)));
      } else {
        buffer.append(ch);
      }
    }
    return buffer.toString();
  }

  /**
   * Unescapes previously escaped jcr chars. <p/> Please note, that this does not exactly the same
   * as the url related {@link #unescape(String)}, since it handles the byte-encoding differently.
   * 
   * @param name
   *          the name to unescape
   * @return the unescaped name
   */
  public static String unescapeIllegalJcrChars(String name) {
    StringBuffer buffer = new StringBuffer(name.length());
    int i = name.indexOf('%');
    while (i > -1 && i + 2 < name.length()) {
      buffer.append(name.toCharArray(), 0, i);
      int a = Character.digit(name.charAt(i + 1), 16);
      int b = Character.digit(name.charAt(i + 2), 16);
      if (a > -1 && b > -1) {
        buffer.append((char) (a * 16 + b));
        name = name.substring(i + 3);
      } else {
        buffer.append('%');
        name = name.substring(i + 1);
      }
      i = name.indexOf('%');
    }
    buffer.append(name);
    return buffer.toString();
  }

  public static boolean isGmailAccount(String emailaddr) {
    if (emailaddr != null && emailaddr.length() > 0 && emailaddr.contains("@")) {
      String suffixEmail = emailaddr.split("@")[1];
      if (suffixEmail.equalsIgnoreCase("gmail.com") || suffixEmail.equalsIgnoreCase("google.com"))
        return true;
    }
    return false;
  }

  public static long getNumberOfUnreadMessageReally(List<Message> msgList) {
    long numberOfUnread = 0;
    if (msgList != null && msgList.size() > 0) {
      for (Message msg : msgList) {
        if (msg.isUnread())
          numberOfUnread += 1;
      }
    }
    return numberOfUnread;
  }

  public static boolean isDelegatedAccount(Account acc, String recieve) {
    return (acc != null && acc.getDelegateFrom() != null && recieve != null && !recieve.equalsIgnoreCase(acc.getDelegateFrom()));
  }

  public static Message getMessage(Node messageNode) throws Exception {
    Message msg = new Message();
    if (messageNode.hasProperty(Utils.EXO_ID))
      msg.setId(messageNode.getProperty(Utils.EXO_ID).getString());
    if (messageNode.hasProperty(Utils.EXO_UID))
      msg.setUID(messageNode.getProperty(Utils.EXO_UID).getString());
    msg.setPath(messageNode.getPath());
    if (messageNode.hasProperty(Utils.EXO_IN_REPLY_TO_HEADER))
      msg.setInReplyToHeader(messageNode.getProperty(Utils.EXO_IN_REPLY_TO_HEADER).getString());
    if (messageNode.hasProperty(Utils.EXO_ACCOUNT))
      msg.setAccountId(messageNode.getProperty(Utils.EXO_ACCOUNT).getString());
    if (messageNode.hasProperty(Utils.EXO_FROM))
      msg.setFrom(messageNode.getProperty(Utils.EXO_FROM).getString());
    if (messageNode.hasProperty(Utils.EXO_TO))
      msg.setMessageTo(messageNode.getProperty(Utils.EXO_TO).getString());
    if (messageNode.hasProperty(Utils.EXO_SUBJECT))
      msg.setSubject(messageNode.getProperty(Utils.EXO_SUBJECT).getString());
    if (messageNode.hasProperty(Utils.EXO_CC))
      msg.setMessageCc(messageNode.getProperty(Utils.EXO_CC).getString());
    if (messageNode.hasProperty(Utils.EXO_BCC))
      msg.setMessageBcc(messageNode.getProperty(Utils.EXO_BCC).getString());
    if (messageNode.hasProperty(Utils.EXO_REPLYTO))
      msg.setReplyTo(messageNode.getProperty(Utils.EXO_REPLYTO).getString());
    if (messageNode.hasProperty(Utils.EXO_CONTENT_TYPE))
      msg.setContentType(messageNode.getProperty(Utils.EXO_CONTENT_TYPE).getString());
    if (messageNode.hasProperty(Utils.EXO_BODY))
      msg.setMessageBody(messageNode.getProperty(Utils.EXO_BODY).getString());
    if (messageNode.hasProperty(Utils.EXO_SIZE))
      msg.setSize(messageNode.getProperty(Utils.EXO_SIZE).getLong());
    if (messageNode.hasProperty(Utils.EXO_HASATTACH))
      msg.setHasAttachment(messageNode.getProperty(Utils.EXO_HASATTACH).getBoolean());
    if (messageNode.hasProperty(Utils.ATT_IS_SHOWN_IN_BODY))
      msg.setHasAttachment(messageNode.getProperty(Utils.ATT_IS_SHOWN_IN_BODY).getBoolean());
    if (messageNode.hasProperty(Utils.EXO_STAR))
      msg.setHasStar(messageNode.getProperty(Utils.EXO_STAR).getBoolean());
    if (messageNode.hasProperty(Utils.EXO_PRIORITY))
      msg.setPriority(messageNode.getProperty(Utils.EXO_PRIORITY).getLong());
    if (messageNode.hasProperty(Utils.EXO_ISUNREAD))
      msg.setUnread(messageNode.getProperty(Utils.EXO_ISUNREAD).getBoolean());
    if (messageNode.hasProperty(Utils.MSG_FOLDERS)) {
      Value[] propFolders = messageNode.getProperty(Utils.MSG_FOLDERS).getValues();
      String[] folders = new String[propFolders.length];
      for (int i = 0; i < propFolders.length; i++) {
        folders[i] = propFolders[i].getString();
      }
      msg.setFolders(folders);
    }
    if (messageNode.hasProperty(Utils.EXO_TAGS)) {
      Value[] propTags = messageNode.getProperty(Utils.EXO_TAGS).getValues();
      String[] tags = new String[propTags.length];
      for (int i = 0; i < propTags.length; i++) {
        tags[i] = propTags[i].getString();
      }
      msg.setTags(tags);
    }
    if (messageNode.hasProperty(Utils.MSG_HEADERS)) {
      Value[] properties = messageNode.getProperty(Utils.MSG_HEADERS).getValues();
      for (int i = 0; i < properties.length; i++) {
        String property = properties[i].getString();
        int index = property.indexOf('=');
        if (index != -1)
          msg.setHeader(property.substring(0, index), property.substring(index + 1));
      }
    }
    GregorianCalendar cal = new GregorianCalendar();
    if (messageNode.hasProperty(Utils.EXO_RECEIVEDDATE))
      cal.setTimeInMillis(messageNode.getProperty(Utils.EXO_RECEIVEDDATE).getLong());
    msg.setReceivedDate(cal.getTime());
    if (messageNode.hasProperty(Utils.EXO_SENDDATE))
      cal.setTimeInMillis(messageNode.getProperty(Utils.EXO_SENDDATE).getLong());
    msg.setSendDate(cal.getTime());
    if (messageNode.hasProperty(Utils.EXO_LAST_CHECKED_TIME))
      cal.setTimeInMillis(messageNode.getProperty(Utils.EXO_LAST_UPDATE_TIME).getLong());
    msg.setLastUpdateDate(cal.getTime());
    if (messageNode.hasProperty(Utils.IS_LOADED))
      msg.setIsLoaded(messageNode.getProperty(Utils.IS_LOADED).getBoolean());
    if (messageNode.hasProperty(Utils.ATT_IS_LOADED_PROPERLY))
      msg.setAttIsLoadedProperly(messageNode.getProperty(Utils.ATT_IS_LOADED_PROPERLY).getBoolean());
    if (messageNode.hasProperty(Utils.IS_RETURN_RECEIPT))
      msg.setIsReturnReceipt(messageNode.getProperty(Utils.IS_RETURN_RECEIPT).getBoolean());

    return msg;
  }

  public static JCRMessageAttachment getJCRMessageAttachment(Node attactNode) throws Exception {
    JCRMessageAttachment file = new JCRMessageAttachment();
    file.setId(attactNode.getPath());
    file.setMimeType(attactNode.getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_MIMETYPE).getString());
    file.setName(attactNode.getProperty(Utils.EXO_ATT_NAME).getString());
    if (attactNode.hasNode(Utils.ATT_IS_LOADED_PROPERLY))
      file.setIsLoadedProperly(attactNode.getProperty(Utils.ATT_IS_LOADED_PROPERLY).getBoolean());
    file.setIsShowInBody(attactNode.getProperty(Utils.ATT_IS_SHOWN_IN_BODY).getBoolean());
    file.setWorkspace(attactNode.getSession().getWorkspace().getName());
    file.setSize(attactNode.getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_DATA).getLength());
    file.setPath("/" + file.getWorkspace() + attactNode.getPath());
    return file;
  }

  private static Map<String, MailSettingConfig> getSettingConfig() {
    MailService mailService = (MailService) PortalContainer.getInstance().getComponentInstanceOfType(MailService.class);
    return mailService.getSettingConfig().get(SERVICE_SETTINGS_NAME).getMailSettingConfig();
  }

  public static boolean isUserAllowedLeaveOnServer() throws Exception {
    return getSettingConfig().get(LEAVE_ON_SEVER).getUserAllowed();
  }

  public static String getLeaveOnServer() {
    return getSettingConfig().get(LEAVE_ON_SEVER).getDefaultValue();
  }

  public static boolean isUserAllowedInconmingServer() throws Exception {
    return getSettingConfig().get(INCOMING_SERVER).getUserAllowed();
  }

  public static String getIncomingServer() {
    return getSettingConfig().get(INCOMING_SERVER).getDefaultValue();
  }

  public static boolean isUserAllowedIncomingPort() throws Exception {
    return getSettingConfig().get(INCOMING_SERVER_PORT).getUserAllowed();
  }

  public static String getIncomingPort() {
    return getSettingConfig().get(INCOMING_SERVER_PORT).getDefaultValue();
  }

  public static boolean isUserAllowedOutgoingServer() throws Exception {
    return getSettingConfig().get(OUTGOING_SEVER).getUserAllowed();
  }

  public static String getOutgoingServer() {
    return getSettingConfig().get(OUTGOING_SEVER).getDefaultValue();
  }

  public static boolean isUserAllowedOutgoingPort() throws Exception {
    return getSettingConfig().get(OUTGOING_SEVER_PORT).getUserAllowed();
  }

  public static String getOutgoingPort() {
    return getSettingConfig().get(OUTGOING_SEVER_PORT).getDefaultValue();
  }

  public static boolean isUserAllowedAcceptIncomingSecureAuthentication() throws Exception {
    return getSettingConfig().get(ACCEPT_INCOMING_SECURE_AUTHENTICATION).getUserAllowed();
  }

  public static String getAcceptIncomingSecureAuthentication() {
    return getSettingConfig().get(ACCEPT_INCOMING_SECURE_AUTHENTICATION).getDefaultValue();
  }

  public static boolean isUserAllowedIncomingSecureAuthentication() throws Exception {
    return getSettingConfig().get(INCOMING_SECURE_AUTHENTICATION).getUserAllowed();
  }

  public static String getIncomingSecureAuthentication() {
    return getSettingConfig().get(INCOMING_SECURE_AUTHENTICATION).getDefaultValue();
  }

  public static boolean isUserAllowedIncomingAuthenticationMechanism() throws Exception {
    return getSettingConfig().get(INCOMING_AUTHENTICATION_MECHANISM).getUserAllowed();
  }

  public static String getIncomingAuthenticationMechanism() {
    return getSettingConfig().get(INCOMING_AUTHENTICATION_MECHANISM).getDefaultValue();
  }

  public static boolean isUserAllowedAcceptOutgoingSecureAuthentication() throws Exception {
    return getSettingConfig().get(ACCEPT_OUTGOING_SECURE_AUTHENTICATION).getUserAllowed();
  }

  public static String getAcceptOutgoingSecureAuthentication() {
    return getSettingConfig().get(ACCEPT_OUTGOING_SECURE_AUTHENTICATION).getDefaultValue();
  }

  public static boolean isUserAllowedOutgoingSecureAuthentication() throws Exception {
    return getSettingConfig().get(OUTGOING_SECURE_AUTHENTICATION).getUserAllowed();
  }

  public static String getOutgoingSecureAuthentication() {
    return getSettingConfig().get(OUTGOING_SECURE_AUTHENTICATION).getDefaultValue();
  }

  public static boolean isUserAllowedOutgoingAuthenticationMechanism() throws Exception {
    return getSettingConfig().get(OUTGOING_AUTHENTICATION_MECHANISM).getUserAllowed();
  }

  public static String getOutgoingAuthenticationMechanism() {
    return getSettingConfig().get(OUTGOING_AUTHENTICATION_MECHANISM).getDefaultValue();
  }
  
  /**
   * <p>
   * This function is used to get absolute property name from protocol and property.
   * </p>
   * <p>
   * {@literal
   * Note: As documentation of Javamail 1.4.4, the library is always using property names of the form mail.<protocol>.<property> 
   * }
   * </p>
   * @param protocol - mail protocol such as imap, imaps, smtp, smtps, ...
   * @param property - such as host, port, ... 
   * @return
   */
  public static String getMailConfigPropertyName(String protocol, String property) {
    ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(protocol, "protocol", null);
    ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(property, "property", null);
    return Utils.SVR_MAIL + "." + protocol + "." + property;
  }
  
  public static String encodeMailId(String id) {
    if (id == null) return "";
    return id.replaceAll("\\+", PLUS_ENCODE).replaceAll("=", EQUAL_ENCODE);
  }

  public static String decodeMailId(String id) {
    if (id == null) return "";
    return id.replaceAll(PLUS_ENCODE, "+").replaceAll(EQUAL_ENCODE, "=");
  }
}
