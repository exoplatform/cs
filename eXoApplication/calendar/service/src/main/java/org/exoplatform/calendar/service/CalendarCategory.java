/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarCategory {
  private String id ;
  private String name ;
  private String description ;
  //private String[] calendars ;
  
  public CalendarCategory() {
    id = "CalendarCategory" + IdGenerator.generate() ;
  }
  
  public String getId() { return id ; }
  public void setId(String id) { this.id = id ; }
  
  public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }

  public String getDescription() { return description ; }
  public void setDescription(String description) { this.description = description ; }

  //public String[] getCalendars() { return calendars ; }
  //public void setCalendars(String[] calendars) { this.calendars = calendars ; }

}
