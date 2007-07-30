/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class GroupCalendarData {
  private String name ;
  private List<Calendar> calendars ;
  
  public GroupCalendarData(String name, List<Calendar> calendars) throws Exception {
    this.name = name ;
    this.calendars = calendars ;
  }
  public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }

  public List<Calendar> getCalendars() { return calendars ; }
  public void setCalendars(List<Calendar> calendars) { this.calendars = calendars ; }

}
