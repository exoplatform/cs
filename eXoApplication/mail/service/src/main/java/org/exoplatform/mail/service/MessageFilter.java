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
public class MessageFilter {
  private String name_ ;
  private String accountId_ ;
  private String[] folder_ ;
  private String[] tag_ ;
  private String subject_ ;
  private String body_ ;

  public MessageFilter(String name) {
    name_ = name ;
  }
  
  public String getName() { return name_ ; }
  
  public String getAccountId() { return accountId_ ; }
  public void setAccountId(String id) { accountId_ =  id ; }
  
  public String[] getFolder() { return folder_ ; }
  public void setFolder(String[] folder) { folder_ = folder ; }
  
  public String[] getTag() { return tag_ ; }
  public void setTag(String[] tag) { tag_ = tag ; }
  
  public String getSubject() { return subject_ ; }
  public void setSubject(String subject) { subject_ = subject ; }
  
  public String getBody() { return body_ ; }
  public void setBody(String body) { body_ = body ; }
}
