/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class MessageHeader {
  private String id ;
  private String accountId;
  private long priority;
  
  public MessageHeader() {
    id = "MessageHeader" + IdGenerator.generate() ;
  }
  public String getId() { return id ; }
  public void setId(String id) { this.id = id; }
  
  public String getAccountId() { return accountId ; }
  public void setAccountId(String accountId) { this.accountId = accountId ; }

  public long getPriority() { return priority; }
  public void setPriority(long priority) { this.priority = priority; }
}
