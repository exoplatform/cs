/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class MessageHeader {
  private String id_ ;
  private String accountId_;
  
  public String getId() { return id_ ; }
  public void setId(String id) { id_ = id; }
  
  public String getAccountId() {
    return accountId_;
  }
  public void setAccountId(String accountId) {
    this.accountId_ = accountId;
  }

}
