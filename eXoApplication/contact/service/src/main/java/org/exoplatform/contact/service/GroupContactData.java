/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class GroupContactData {
  private String name ;
  private List<Contact> contacts ;
  
  public GroupContactData(String name, List<Contact> contacts) throws Exception {
    this.name = name ;
    this.contacts = contacts ;
  }
  public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }

  public List<Contact> getContacts() { return contacts ; }
  public void setContacts(List<Contact> contacts) { this.contacts = contacts ; }

}
