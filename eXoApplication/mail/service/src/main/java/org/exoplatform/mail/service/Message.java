/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class Message extends MessageHeader {
  private String to_ ;
  private String cc_ ;
  private String bcc_ ;
  private String body_ ;
  private String subject_ ;
  private boolean isUnread_ = false ;
  
  private List<Attachment> attachments_ ;
  
  public String getMessageTo() { return to_ ; }
  public void setMessageTo(String s) { to_ = s ; }
  
  public String getMessageCc() { return cc_ ; }
  public void setMessageCc(String s) { cc_ = s ; }
  
  public String getMessageBcc() { return bcc_ ; }
  public void setMessageBcc(String s) { bcc_ = s ; }
  
  public String getSubject() { return subject_ ; }
  public void setSubject(String s) { subject_ = s ; }
  
  public String getMessageBody() { return body_ ; }
  public void   setMessageBody(String s) { body_ =  s ; }
  
  public void setUnread(boolean b) { isUnread_ = b ; }
  public boolean isUnread() { return isUnread_ ; }
  
  public List<Attachment> getAttachments() { return attachments_ ; }
  public void setAttachements(List<Attachment> attachments) { attachments_ = attachments ; }
  
  public Message cloneMessage() { return null ; } 
}
