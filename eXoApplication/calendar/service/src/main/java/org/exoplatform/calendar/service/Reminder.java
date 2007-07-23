/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jul 16, 2007  
 */
public class Reminder {
  private String id ;
  private String eventId ;
  private String repeat = "1";
  private String reminder = "Via email";
  private String alarmBefore = "5"; 
  
  public String getId() { return this.id ; }
  public void   setId(String id) { this.id = id ; }
  
  public String getEventId() { return this.eventId ; }
  public void   setEventId(String eventId) { this.eventId = eventId ; }
  
  public String getRepeat() { return repeat; }
  public void   setRepeat(String repeat) { this.repeat = repeat; }
  
  public String getReminder() { return reminder; }
  public void   setReminder(String reminder) { this.reminder = reminder; }
  
  public String getAlarmBefore() { return alarmBefore; }
  public void   setAlarmBefore(String alarmBefore) { this.alarmBefore = alarmBefore; }
  
}
