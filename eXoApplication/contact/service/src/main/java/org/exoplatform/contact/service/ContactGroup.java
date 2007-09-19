/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */
public class ContactGroup {
  private String id ;
  private String name ;
  private String description ;
  
  public ContactGroup() {
    id = "ContactGroup" + IdGenerator.generate() ;
  }
  
  public String getId()  { return id ; }
  public void   setId(String s) { id = s ; }
  
  public String getName()  { return name ; }
  public void   setName(String s) { name = s ; }
  
  public String getDescription() { return description ; }
  public void setDescription(String s) { description = s ;}
}
