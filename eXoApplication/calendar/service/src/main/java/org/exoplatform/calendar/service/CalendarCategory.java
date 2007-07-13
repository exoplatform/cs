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
public class CalendarCategory {
  private String id_ ;
  private String name_ ;
  private String description_ ;
  private String[] calendars_ ;
  private List<CalendarAlias> publicCalendar_ ;
  
  public CalendarCategory() {}
  
  public void setId(String id_) {
    this.id_ = id_;
  }
  public String getId() {
    return id_;
  }

  public void setName(String name_) {
    this.name_ = name_;
  }

  public String getName() {
    return name_;
  }

  public void setDescription(String description_) {
    this.description_ = description_;
  }

  public String getDescription() {
    return description_;
  }

  public void setCalendars(String[] calendars_) {
    this.calendars_ = calendars_;
  }

  public String[] getCalendars() {
    return calendars_;
  }

  public void setPublicCalendar(List<CalendarAlias> publicCalendar_) {
    this.publicCalendar_ = publicCalendar_;
  }

  public List<CalendarAlias> getPublicCalendar() {
    return publicCalendar_;
  }

}
