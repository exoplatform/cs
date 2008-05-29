/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 * 28-05-2008  
 */
public class ReminderBean {
  private String id ;
  private List<Reminder> reminderlist ;
  
  public ReminderBean(String id, List<Reminder> inputList) {
    this.id = id ;
    this.reminderlist = inputList ;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getId() {
    return id;
  }
  public void setReminderlist(List<Reminder> reminderlist) {
    this.reminderlist = reminderlist;
  }
  public List<Reminder> getReminderlist() {
    return reminderlist;
  }
  
  
}
