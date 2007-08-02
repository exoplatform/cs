/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class Message extends MessageHeader {
  private String from;
  private String to ;
  private String cc ;
  private String bcc ;
  private String body ;
  private String subject ;
  private Date sendDate ;
  private Date receivedDate ;
  private boolean isUnread = true ;
  
  private String[] folders ;
  private String[] tags ;
  
  private List<Attachment> attachments ;
  
  public String getMessageTo() { return to ; }
  public void setMessageTo(String s) { to = s ; }
  
  public String getMessageCc() { return cc ; }
  public void setMessageCc(String s) { cc = s ; }
  
  public String getMessageBcc() { return bcc ; }
  public void setMessageBcc(String s) { bcc = s ; }
  
  public String getSubject() { return subject ; }
  public void setSubject(String s) { subject = s ; }
  
  public String getMessageBody() { return body ; }
  public void   setMessageBody(String s) { body =  s ; }
  
  public void setUnread(boolean b) { isUnread = b ; }
  public boolean isUnread() { return isUnread ; }
  
  public Date getSendDate() { return sendDate ; }
  public void setSendDate(Date d) { sendDate = d ; }
  
  public Date getReceivedDate() { return receivedDate ; }
  public void setReceivedDate(Date d) { receivedDate = d ; }
  
  public String[] getFolders() { return folders ; }
  public void setFolders(String[] folders) { this.folders = folders ; }
  
  public String[] getTags() { return tags ; }
  public void setTags(String[] tags) { this.tags = tags ; }
  
  public List<Attachment> getAttachments() { return attachments ; }
  public void setAttachements(List<Attachment> attachments) { this.attachments = attachments ; }
  
  public Message cloneMessage() { return null ; }
  
  public String getFrom() { return from ; }
  public void setFrom(String from) { this.from = from ; } 
}
