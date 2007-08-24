/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class EventCategory {
  private String name ;
  private String description ;
  
  public String getName() { return name ; }
  public void   setName(String name) { this.name = name ; }

  public String getDescription() { return description ; }
  public void   setDescription(String description) { this.description = description ; }

}
