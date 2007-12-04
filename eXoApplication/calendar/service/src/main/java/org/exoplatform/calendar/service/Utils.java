/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Sep 28, 2007  
 */
public class Utils {
  
  
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
  public static final String EXO_CHECKMAILAUTO = "exo:checkMailAuto".intern();
  public static final String EXO_EMPTYTRASH = "exo:emptyTrash".intern();
  public static final String EXO_PLACESIGNATURE = "exo:placeSignature".intern();

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
  
  public static final String EXO_EVEN_TATTACHMENT = "exo:eventAttachment".intern() ;
  public static final String EXO_FILE_NAME = "exo:fileName".intern() ;
  
  //public static final String ATTACHMENT_ID = "exo:attachmentId".intern() ; ;
  public static final String ATTACHMENT_NODE = "attachment".intern() ; ;
  public static final String REMINDERS_NODE = "reminders".intern() ; ;
  
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
  
  public static GregorianCalendar getInstanceTempCalendar() { 
	  GregorianCalendar  calendar = new GregorianCalendar() ;
		int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset) ;
		return  calendar;
	}
}
