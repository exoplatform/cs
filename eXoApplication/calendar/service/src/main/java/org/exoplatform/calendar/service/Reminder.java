/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.Date;

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
  final public static String[] REMINDER_TYPES = {TYPE_EMAIL, TYPE_POPUP} ;
  
  private String id ;
  private String eventId ;
  private String reminderType = TYPE_EMAIL;
  private long alarmBefore = 0; 
  private String emailAddress ;
  private Date fromDateTime  ;
  private boolean isRepeat = false ;
  private long repeatInterval = 0 ;
  private String summary ;
  
  public Reminder() {
    id = "Reminder" + IdGenerator.generate() ;
  }
  
  public String getId() { return this.id ; }
  public void   setId(String id) { this.id = id ; }
  
  public String getEventId() { return this.eventId ; }
  public void   setEventId(String eventId) { this.eventId = eventId ; }
  
  public long getRepeatInterval() { return repeatInterval ; }
  public void   setRepeatInterval(long interval) { repeatInterval = interval; }
  
  public String getReminderType() { return reminderType; }
  public void   setReminderType(String reminderType) { this.reminderType = reminderType; }
  
  public long getAlarmBefore() { return alarmBefore; }
  public void setAlarmBefore(long alarmBefore) { this.alarmBefore = alarmBefore; }
  
  public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress ; }
  public String getEmailAddress() { return emailAddress; }
  
  public Date getFromDateTime() {return fromDateTime ; }
  public void setFromDateTime(Date d) { fromDateTime = d ; }
  
  public boolean isRepeat() { return isRepeat ; }
  public void setRepeate(boolean b) { isRepeat = b ; }
  
  public void setSummary(String sm) { this.summary = sm ; }
  public String getSummary() { return summary ; }
}
