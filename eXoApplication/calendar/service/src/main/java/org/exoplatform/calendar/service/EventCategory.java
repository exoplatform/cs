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
  private String id ;
  private String name ;
  private String description ;
  
  public EventCategory() {}
  
  public void setName(String name_) {
    this.name = name_;
  }

  public String getName() {
    return name;
  }

  public void setDescription(String description_) {
    this.description = description_;
  }

  public String getDescription() {
    return description;
  }
  
  
  
}
