/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */
public class Tag {
  
  private String name ;
  private String description ;
  
  public void setName(String name) { this.name = name ; }
  public String getName() { return name ; }
  
  public void setDescription(String description) { this.description = description ; }
  public String getDescription() { return description ; }
    
}
