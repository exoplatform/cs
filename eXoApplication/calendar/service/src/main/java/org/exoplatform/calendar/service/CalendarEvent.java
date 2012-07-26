/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.calendar.service;

import java.util.Date;
import java.util.List;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarEvent {

  final public static String   TYPE_EVENT      = "Event".intern();

  final public static String   TYPE_TASK       = "Task".intern();

  final public static String   TYPE_JOURNAL    = "Journal".intern();

  final public static String   TENTATIVE       = "tentative".intern();

  final public static String   CONFIRMED       = "confirmed".intern();

  final public static String   CANCELLED       = "canceled".intern();

  final public static String   NEEDS_ACTION    = "needs-action".intern();

  final public static String   COMPLETED       = "completed".intern();

  final public static String   IN_PROCESS      = "in-process".intern();

  final public static String   PRIORITY_NONE   = "none".intern();

  final public static String   PRIORITY_LOW    = "low".intern();

  final public static String   PRIORITY_NORMAL = "normal".intern();

  final public static String   PRIORITY_HIGH   = "high".intern();

  final public static int      PRI_UNDEFINED   = 0;

  final public static int      PRI_HIGH        = 1;

  final public static int      PRI_MEDIUM      = 2;

  final public static int      PRI_LOW         = 3;

  final public static String[] PRIORITY        = { PRIORITY_NONE, PRIORITY_HIGH, PRIORITY_NORMAL, PRIORITY_LOW };

  final public static String   DRAFT           = "draft".intern();

  final public static String   FINAL           = "final".intern();

  final public static String[] EVENT_STATUS    = { TENTATIVE, CONFIRMED, CANCELLED };

  final public static String[] TASK_STATUS     = { NEEDS_ACTION, IN_PROCESS, COMPLETED, CANCELLED };

  final public static String[] JOURNAL_STATUS  = { DRAFT, FINAL, CANCELLED };

  final public static String   IS_PUBLIC       = "public".intern();

  final public static String   IS_PRIVATE      = "private".intern();

  final public static String   ST_AVAILABLE    = "available".intern();

  final public static String   ST_BUSY         = "busy".intern();

  final public static String   ST_OUTSIDE      = "outside".intern();

  final public static String   RP_NOREPEAT     = "norepeat".intern();

  final public static String   RP_DAILY        = "daily".intern();

  final public static String   RP_WEEKLY       = "weekly".intern();

  final public static String   RP_MONTHLY      = "monthly".intern();

  final public static String   RP_YEARLY       = "yearly".intern();

  final public static String   RP_WEEKEND      = "weekend".intern();

  final public static String   RP_WORKINGDAYS  = "workingdays".intern();

  final public static String[] RP_WEEKLY_BYDAY = { "MO", "TU", "WE", "TH", "FR", "SA", "SU" };

  final public static String[] REPEATTYPES     = { RP_NOREPEAT, RP_DAILY, RP_WEEKLY, RP_MONTHLY, RP_YEARLY };

  private String               id;

  private String               summary;

  private String               location;

  private String               description;

  private String               eventCategoryId;

  private String               eventCategoryName;

  private String               calendarId;

  private String               repeatType;

  private Date                 fromDateTime;

  private Date                 toDateTime;

  private Date                 completedDateTime;

  private String               taskDelegator;

  private String               sendOption      = CalendarSetting.ACTION_BYSETTING;

  private String               message;

  private String[]             participantStatus;

  private Date                 lastUpdatedTime;

  // properties for exo:repeatCalendarEvent mixin type
  private String               recurrenceId;

  private Boolean              isExceptionOccurrence;

  private String[]             excludeId;

  private String               originalReference;

  private Date                 repeatUntilDate;

  private long                 repeatCount;

  private long                 repeatInterval;

  private long[]               repeatBySecond;

  private long[]               repeatByMinute;

  private long[]               repeatByHour;

  private String[]             repeatByDay;

  private long[]               repeatByMonthDay;

  private long[]               repeatByYearDay;

  private long[]               repeatByWeekNo;

  private long[]               repeatByMonth;

  private long[]               repeatBySetPos;

  private String               repeatWkst;

  /**
   * Types: TYPE_EVENT, TYPE_TASK, TYPE_JOURNAL
   */
  private String               eventType       = TYPE_EVENT;

  /**
   * Values: LOW, NORMAL, HIGHT
   */
  private String               priority;

  private boolean              isPrivate       = true;

  private String               eventState      = ST_BUSY;

  /**
   *Status:
   *-for event: TENTATIVE, CONFIRMED, CANCELLED. 
   *-for task:  NEEDS-ACTION, COMPLETED, IN-PROCESS, CANCELLED
   *-for journal: DRAFT, FINAL, CANCELLED
   */
  private String               status          = "";

  private String               calType         = "0";

  private String[]             invitation;

  private String[]             participant;

  private List<Reminder>       reminders;

  private List<Attachment>     attachment;

  public CalendarEvent() {
    id = "Event" + IdGenerator.generate();
  }

  // copy constructor
  public CalendarEvent(CalendarEvent event) {
    this.id = event.id;
    this.summary = event.summary;
    this.description = event.description;
    this.fromDateTime = event.fromDateTime;
    this.toDateTime = event.toDateTime;
    this.eventCategoryId = event.eventCategoryId;
    this.eventCategoryName = event.eventCategoryName;
    this.message = event.message;
    this.location = event.location;
    this.repeatType = event.repeatType;
    this.calendarId = event.calendarId;
    this.calType = event.calType;
    this.sendOption = event.sendOption;
    this.status = event.status;
    List<Attachment> attachments = event.getAttachment();
    setAttachment(attachments);
    this.setInvitation(event.getInvitation());
    this.setParticipant(event.getParticipant());
    this.setParticipantStatus(event.getParticipantStatus());
    this.setReminders(event.getReminders());
    this.setPriority(event.getPriority());
    // this.setLastUpdatedTime(event.getLastUpdatedTime());
    this.setRepeatUntilDate(event.getRepeatUntilDate());
    this.setRepeatCount(event.getRepeatCount());
    this.setRepeatInterval(event.getRepeatInterval());
    this.setRepeatByDay(event.getRepeatByDay());
    this.setRepeatByMonthDay(event.getRepeatByMonthDay());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String sum) {
    this.summary = sum;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isPrivate() {
    return isPrivate;
  }

  public void setPrivate(boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  public String getEventCategoryId() {
    return eventCategoryId;
  }

  public void setEventCategoryId(String eventCategoryId) {
    this.eventCategoryId = eventCategoryId;
  }

  public String getCalendarId() {
    return calendarId;
  }

  public void setCalendarId(String calendarId) {
    this.calendarId = calendarId;
  }

  public Date getFromDateTime() {
    return fromDateTime;
  }

  public void setFromDateTime(Date fromDateTime) {
    this.fromDateTime = fromDateTime;
  }

  public Date getToDateTime() {
    return toDateTime;
  }

  public void setToDateTime(Date toDateTime) {
    this.toDateTime = toDateTime;
  }

  public Date getCompletedDateTime() {
    return completedDateTime;
  }

  public void setCompletedDateTime(Date completedDateTime) {
    this.completedDateTime = completedDateTime;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getEventState() {
    return eventState;
  }

  public void setEventState(String eventState) {
    this.eventState = eventState;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String[] getInvitation() {
    return invitation;
  }

  public void setInvitation(String[] invitation) {
    this.invitation = invitation;
  }

  public List<Reminder> getReminders() {
    return reminders;
  }

  public void setReminders(List<Reminder> rm) {
    this.reminders = rm;
  }

  public void setRepeatType(String repeatType) {
    this.repeatType = repeatType;
  }

  public String getRepeatType() {
    return repeatType;
  }

  public List<Attachment> getAttachment() {
    return attachment;
  }

  public void setAttachment(List<Attachment> list) {
    attachment = list;
  }

  public void setParticipant(String[] participant) {
    this.participant = participant;
  }

  public String[] getParticipant() {
    return participant;
  }

  public String[] getParticipantStatus() {
    return participantStatus;
  }

  public void setParticipantStatus(String[] participantStatus) {
    this.participantStatus = participantStatus;
  }

  public void setTaskDelegator(String taskDelegator) {
    this.taskDelegator = taskDelegator;
  }

  public String getTaskDelegator() {
    return taskDelegator;
  }

  public void setCalType(String calType) {
    this.calType = calType;
  }

  public String getCalType() {
    return calType;
  }

  public void setEventCategoryName(String eventCategoryName) {
    this.eventCategoryName = eventCategoryName;
  }

  public String getEventCategoryName() {
    return eventCategoryName;
  }

  public void setSendOption(String sendOption) {
    this.sendOption = sendOption;
  }

  public String getSendOption() {
    return sendOption;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    if (message != null)
      this.message = message;
    else
      this.message = new String();
  }

  public Date getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  public void setLastUpdatedTime(Date lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  /**
   * @param recurrenceIndex the recurrenceIndex to set
   */
  public void setRecurrenceId(String recurrenceId) {
    this.recurrenceId = recurrenceId;
  }

  /**
   * @return the recurrenceIndex
   */
  public String getRecurrenceId() {
    return recurrenceId;
  }

  /**
   * @param isExceptionOccurrence the isExceptionOccurrence to set
   */
  public void setIsExceptionOccurrence(Boolean isExceptionOccurrence) {
    this.isExceptionOccurrence = isExceptionOccurrence;
  }

  /**
   * @return the isExceptionOccurrence
   */
  public Boolean getIsExceptionOccurrence() {
    return isExceptionOccurrence;
  }

  /**
   * @param excludeId the excludeId to set
   */
  public void setExcludeId(String[] excludeId) {
    this.excludeId = excludeId;
  }

  /**
   * @return the excludeId
   */
  public String[] getExcludeId() {
    return excludeId;
  }

  /**
   * @param repeatUntilDate the repeatUntilDate to set
   */
  public void setRepeatUntilDate(Date repeatUntilDate) {
    this.repeatUntilDate = repeatUntilDate;
  }

  /**
   * @return the repeatUntilDate
   */
  public Date getRepeatUntilDate() {
    return repeatUntilDate;
  }

  /**
   * @param originalReference the originalReference to set
   */
  public void setOriginalReference(String originalReference) {
    this.originalReference = originalReference;
  }

  /**
   * @return the originalReference
   */
  public String getOriginalReference() {
    return originalReference;
  }

  /**
   * @param repeatCount the repeatCount to set
   */
  public void setRepeatCount(long repeatCount) {
    this.repeatCount = repeatCount;
  }

  /**
   * @return the repeatCount
   */
  public long getRepeatCount() {
    return repeatCount;
  }

  /**
   * @param repeatInterval the repeatInterval to set
   */
  public void setRepeatInterval(long repeatInterval) {
    this.repeatInterval = repeatInterval;
  }

  /**
   * @return the repeatInterval
   */
  public long getRepeatInterval() {
    return repeatInterval;
  }

  /**
   * @param repeatBySecond the repeatBySecond to set
   */
  public void setRepeatBySecond(long[] repeatBySecond) {
    this.repeatBySecond = repeatBySecond;
  }

  /**
   * @return the repeatBySecond
   */
  public long[] getRepeatBySecond() {
    return repeatBySecond;
  }

  /**
   * @return the repeatByMinute
   */
  public long[] getRepeatByMinute() {
    return repeatByMinute;
  }

  /**
   * @param repeatByMinute the repeatByMinute to set
   */
  public void setRepeatByMinute(long[] repeatByMinute) {
    this.repeatByMinute = repeatByMinute;
  }

  /**
   * @return the repeatByMonthDay
   */
  public long[] getRepeatByMonthDay() {
    return repeatByMonthDay;
  }

  /**
   * @param repeatByMonthDay the repeatByMonthDay to set
   */
  public void setRepeatByMonthDay(long[] repeatByMonthDay) {
    this.repeatByMonthDay = repeatByMonthDay;
  }

  /**
   * @return the repeatByYearDay
   */
  public long[] getRepeatByYearDay() {
    return repeatByYearDay;
  }

  /**
   * @param repeatByYearDay the repeatByYearDay to set
   */
  public void setRepeatByYearDay(long[] repeatByYearDay) {
    this.repeatByYearDay = repeatByYearDay;
  }

  /**
   * @return the repeatByWeekNo
   */
  public long[] getRepeatByWeekNo() {
    return repeatByWeekNo;
  }

  /**
   * @param repeatByWeekNo the repeatByWeekNo to set
   */
  public void setRepeatByWeekNo(long[] repeatByWeekNo) {
    this.repeatByWeekNo = repeatByWeekNo;
  }

  /**
   * @return the repeatByMonth
   */
  public long[] getRepeatByMonth() {
    return repeatByMonth;
  }

  /**
   * @param repeatByMonth the repeatByMonth to set
   */
  public void setRepeatByMonth(long[] repeatByMonth) {
    this.repeatByMonth = repeatByMonth;
  }

  /**
   * @return the repeatBySetPos
   */
  public long[] getRepeatBySetPos() {
    return repeatBySetPos;
  }

  /**
   * @param repeatBySetPos the repeatBySetPos to set
   */
  public void setRepeatBySetPos(long[] repeatBySetPos) {
    this.repeatBySetPos = repeatBySetPos;
  }

  /**
   * @return the repeatWkst
   */
  public String getRepeatWkst() {
    return repeatWkst;
  }

  /**
   * @param repeatWkst the repeatWkst to set
   */
  public void setRepeatWkst(String repeatWkst) {
    this.repeatWkst = repeatWkst;
  }

  /**
   * @param repeatByHour the repeatByHour to set
   */
  public void setRepeatByHour(long[] repeatByHour) {
    this.repeatByHour = repeatByHour;
  }

  /**
   * @return the repeatByHour
   */
  public long[] getRepeatByHour() {
    return repeatByHour;
  }

  /**
   * @param repeatByDay the repeatByDay to set
   */
  public void setRepeatByDay(String[] repeatByDay) {
    this.repeatByDay = repeatByDay;
  }

  /**
   * @return the repeatByDay
   */
  public String[] getRepeatByDay() {
    return repeatByDay;
  }

}
