/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.Date;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Event {
  private String id_ ;
  private String name_ ;
  private String description_ ;
  private String categoryId_ ;
  private String calendarId_ ;
  private Date fromDateTime_ ;
  private Date toDateTime_ ;
  private String repeat_ ;
  private String location_ ;
  private String reminder_ ;
  private String alarmBefore_ ;  
  private boolean isPrivate_ = true ;
  private String eventState_ ;
  private String[] invitation_ ;
  
  public Event() {}
  
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

  public void setPrivate(boolean isPrivate_) {
    this.isPrivate_ = isPrivate_;
  }

  public boolean isPrivate() {
    return isPrivate_;
  }

  public void setCategoryId(String categoryId_) {
    this.categoryId_ = categoryId_;
  }

  public String getCategoryId() {
    return categoryId_;
  }

  public void setCalendarId(String calendarId_) {
    this.calendarId_ = calendarId_;
  }

  public String getCalendarId() {
    return calendarId_;
  }

  public void setFromDateTime(Date fromDateTime_) {
    this.fromDateTime_ = fromDateTime_;
  }

  public Date getFromDateTime() {
    return fromDateTime_;
  }

  public void setToDateTime(Date toDateTime_) {
    this.toDateTime_ = toDateTime_;
  }

  public Date getToDateTime() {
    return toDateTime_;
  }

  public void setRepeat(String repeat_) {
    this.repeat_ = repeat_;
  }

  public String getRepeat() {
    return repeat_;
  }

  public void setLocation(String location_) {
    this.location_ = location_;
  }

  public String getLocation() {
    return location_;
  }

  public void setReminder(String reminder_) {
    this.reminder_ = reminder_;
  }

  public String getReminder() {
    return reminder_;
  }

  public void setAlarmBefore(String alarmBefore_) {
    this.alarmBefore_ = alarmBefore_;
  }

  public String getAlarmBefore() {
    return alarmBefore_;
  }

  public void setEventState(String eventState_) {
    this.eventState_ = eventState_;
  }

  public String getEventState() {
    return eventState_;
  }

  public void setInvitation(String[] invitation_) {
    this.invitation_ = invitation_;
  }

  public String[] getInvitation() {
    return invitation_;
  }  
  
}
