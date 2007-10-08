/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jul 16, 2007  
 */
public class Reminder {
  
  final public static String REPEAT = "1".intern() ;
  final public static String UNREPEAT = "0".intern() ;
  final public static String TYPE_EMAIL = "email".intern() ;
  final public static String TYPE_POPUP = "popup".intern() ;
  final public static String TYPE_BOTH = "both".intern() ;
  final public static String[] REMINDER_TYPES = {TYPE_EMAIL, TYPE_POPUP, TYPE_BOTH} ;
  
  private String id ;
  private String eventId ;
  private long snooze = 0;
  private String reminder = "";
  private String alarmBefore = "0"; 
  private String emailAddress  ;
  public Reminder() {
    //id = ID() ;
  }
  public Reminder(String type) {
   // id = ID() ;
    reminder = type ;
  }
  public Reminder(String id, String type) {
    this.id = id ;
    reminder = type ;
  }
 // public static String ID(){return "Reminder" + IdGenerator.generate() ; }
  public String getId() { return this.id ; }
  public void   setId(String id) { this.id = id ; }
  
  public String getEventId() { return this.eventId ; }
  public void   setEventId(String eventId) { this.eventId = eventId ; }
  
  public long getSnooze() { return snooze; }
  public void   setSnooze(long minute) { snooze = minute; }
  
  public String getReminder() { return reminder; }
  public void   setReminder(String reminder) { this.reminder = reminder; }
  
  public String getAlarmBefore() { return alarmBefore; }
  public void   setAlarmBefore(String alarmBefore) { this.alarmBefore = alarmBefore; }
  
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }
  public String getEmailAddress() {
    return emailAddress;
  }
  
}
