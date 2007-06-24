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
  private String body_ ;
  private List<Attachment> attachments_ ;
  
  public String getMessageBody() { return body_ ; }
  public void   setMessageBody(String s) { body_ =  s ; }
  
  public List<Attachment> getAttachments() { return attachments_ ; }
  public void setAttachements(List<Attachment> attachments) { attachments_ = attachments ; }
  
  public Message cloneMessage() { return null ; } 
}
