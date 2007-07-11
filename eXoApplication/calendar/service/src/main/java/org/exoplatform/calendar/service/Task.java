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
public class Task {
  private String id_ ;
  private String name_ ;
  private String description_ ;
  private String calendarId_ ;
  private String status_ ;
  private String priority_ ;
  private String[] delegation_ ;
  private Date startDateTime_ ;
  private Date dueDateTime_ ;
  private String reminder_ ;
  private String note_ ;
  
  public Task() {}
  
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

  public void setCalendarId(String calendarId_) {
    this.calendarId_ = calendarId_;
  }

  public String getCalendarId() {
    return calendarId_;
  }

  public void setStatus(String status_) {
    this.status_ = status_;
  }

  public String getStatus() {
    return status_;
  }

  public void setPriority(String priority_) {
    this.priority_ = priority_;
  }

  public String getPriority() {
    return priority_;
  }

  public void setDelegation(String[] delegation_) {
    this.delegation_ = delegation_;
  }

  public String[] getDelegation() {
    return delegation_;
  }

  public void setStartDateTime(Date startDateTime_) {
    this.startDateTime_ = startDateTime_;
  }

  public Date getStartDateTime() {
    return startDateTime_;
  }

  public void setDueDateTime(Date dueDateTime_) {
    this.dueDateTime_ = dueDateTime_;
  }

  public Date getDueDateTime() {
    return dueDateTime_;
  }

  public void setReminder(String reminder_) {
    this.reminder_ = reminder_;
  }

  public String getReminder() {
    return reminder_;
  }

  public void setNote(String note_) {
    this.note_ = note_;
  }

  public String getNot() {
    return note_;
  }  
  
  
}
