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
public class CalendarAlias {
  
  private String id_ ;
  private String groupId_ ;
  private String calendarId_ ;
  private String label_ ;
  
  public CalendarAlias() {} 
  
  
  public void setId(String id_) {
    this.id_ = id_;
  }

  public String getId() {
    return id_;
  }
  
  public void setLabel(String label_) {
    this.label_ = label_;
  }

  public String getLabel() {
    return label_;
  }

  public void setCalendarId(String calendarId_) {
    this.calendarId_ = calendarId_;
  }

  public String getCalendarId() {
    return calendarId_;
  }


  public void setGroupId(String groupId_) {
    this.groupId_ = groupId_;
  }


  public String getGroupId() {
    return groupId_;
  }


  

    
}
