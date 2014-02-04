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
package org.exoplatform.calendar.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Encoding;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.mortbay.log.Log;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public class ICalendarImportExport implements CalendarImportExport{
  private static final String PRIVATE_TYPE = String.valueOf(Calendar.TYPE_PRIVATE) ;
  private static final String SHARED_TYPE = String.valueOf(Calendar.TYPE_SHARED) ;
  private static final String PUBLIC_TYPE = String.valueOf(Calendar.TYPE_PUBLIC) ;
  private JCRDataStorage storage_ ;

  public ICalendarImportExport(JCRDataStorage storage) throws Exception {
    storage_ = storage ;
  }

  private void setPriorityCalEvent(Object event, CalendarEvent exoEvent) {
    if (exoEvent.getPriority() != null) {
      int priority = 0;
      for (int i = 0; i < CalendarEvent.PRIORITY.length; i++) {
        if (exoEvent.getPriority().equalsIgnoreCase(CalendarEvent.PRIORITY[i])) {
          if (i == CalendarEvent.PRI_MEDIUM) {
            priority = 5;
          } else if (i == CalendarEvent.PRI_LOW) {
            priority = 6;
          } else {
            priority = i;
          }
          if(event instanceof VToDo) {
            ((VToDo)event).getProperties().add(new Priority(priority));
            ((VToDo)event).getProperties().getProperty(Property.PRIORITY).getParameters()
            .add(net.fortuna.ical4j.model.parameter.Value.INTEGER);
          } else {
            ((VEvent)event).getProperties().add(new Priority(priority));
            ((VEvent)event).getProperties().getProperty(Property.PRIORITY).getParameters()
            .add(net.fortuna.ical4j.model.parameter.Value.INTEGER);
          }
          break;
        }
      }
    }
  }

  private void setPriorityExoEvent(Object event, CalendarEvent exoEvent) {
    String value = null;
    if (event instanceof VToDo) {
      VToDo vToDo = (VToDo) event;
      if (vToDo.getPriority() != null)
        value = vToDo.getPriority().getValue();
    } else {
      VEvent vEvent = (VEvent) event;
      if (vEvent.getPriority() != null)
        value = vEvent.getPriority().getValue();
    }
    if (value != null) {
      int priority = 0;
      try {
        priority = Integer.parseInt(value);
        if (1 < priority && priority <= 4) {
          priority = CalendarEvent.PRI_HIGH;
        } else if (priority == 5) {
          priority = CalendarEvent.PRI_MEDIUM;
        } else if (priority > 5) {
          priority = CalendarEvent.PRI_LOW;
        }
        exoEvent.setPriority(CalendarEvent.PRIORITY[priority]);
      } catch (Exception e) {
        Log.warn("Can not set the priority of this event");
      }
    }
  }

  private net.fortuna.ical4j.model.Calendar getVEvent(net.fortuna.ical4j.model.Calendar calendar, CalendarEvent exoEvent) throws Exception {
    Uid id = new Uid(exoEvent.getId()) ; 
    long start = exoEvent.getFromDateTime().getTime() ;
    long end = exoEvent.getToDateTime().getTime() ;
    String summary = exoEvent.getSummary() ;
    VEvent event ;
    if(end > 0) {
      event = new VEvent(new DateTime(start), new DateTime(end), summary);
      event.getProperties().getProperty(Property.DTEND).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
    }else {
      event = new VEvent(new DateTime(start), summary);            
    }
    event.getProperties().getProperty(Property.DTSTART).getParameters()
    .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME); 

    event.getProperties().add(new Description(exoEvent.getDescription()));
    event.getProperties().getProperty(Property.DESCRIPTION).getParameters()
    .add(net.fortuna.ical4j.model.parameter.Value.TEXT);

    event.getProperties().add(new Location(exoEvent.getLocation()));
    event.getProperties().getProperty(Property.LOCATION).getParameters()
    .add(net.fortuna.ical4j.model.parameter.Value.TEXT);

    if(exoEvent.getEventCategoryName() != null){
      event.getProperties().add(new Categories(exoEvent.getEventCategoryName())) ;
      event.getProperties().getProperty(Property.CATEGORIES).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    }
    setPriorityCalEvent(event, exoEvent);

    /*   if(exoEvent.getEventType().equals(CalendarEvent.TYPE_TASK)) {
      long completed = exoEvent.getCompletedDateTime().getTime() ;
      event.getProperties().add(new Completed(new DateTime(completed)));
      event.getProperties().getProperty(Property.COMPLETED).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);

      event.getProperties().add(new Due(new DateTime(end)));
      event.getProperties().getProperty(Property.DUE).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);

      event.getProperties().add(new Status(exoEvent.getStatus()));
      event.getProperties().getProperty(Property.STATUS).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    }*/
    if(exoEvent.getAttachment()!= null)
    if(!exoEvent.getAttachment().isEmpty()) {
      for(Attachment att : exoEvent.getAttachment()) {
        byte bytes[] = new byte[att.getInputStream().available()] ; 
        att.getInputStream().read(bytes) ;
        ParameterList plist = new ParameterList() ;
        plist.add(new XParameter(Parameter.CN, att.getName()));
        plist.add(new XParameter(Parameter.FMTTYPE, att.getMimeType()));
        plist.add(Encoding.BASE64) ;
        plist.add(Value.BINARY) ; 
        Attach attach = new Attach(plist, bytes);
        event.getProperties().add(attach) ;
      }
    } 
    if(exoEvent.getReminders() != null )
    if(!exoEvent.getReminders().isEmpty()) {
      for(Reminder r : exoEvent.getReminders()){
        VAlarm reminder = new VAlarm(new DateTime(r.getFromDateTime())) ;
        Long times = new Long(1) ;
        if(r.isRepeat()) times = (r.getAlarmBefore() / r.getRepeatInterval()) ; 
        reminder.getProperties().add(new Repeat(times.intValue()));
        reminder.getProperties().add(new Duration(new Dur(new Long(r.getAlarmBefore()).intValue())));
        if(Reminder.TYPE_POPUP.equals(r.getReminderType())) {
          for(String n : r.getReminderOwner().split(Utils.COMMA)) {
            Attendee a = new Attendee(n) ;
            reminder.getProperties().add(a) ;
          }
          reminder.getProperties().add(Action.DISPLAY);
        }else {
          for(String m : r.getEmailAddress().split(Utils.COMMA)) {
            Attendee a = new Attendee(m) ;
            reminder.getProperties().add(a) ;
          }
          reminder.getProperties().add(Action.EMAIL);
        }
        reminder.getProperties().add(new Summary(exoEvent.getSummary()));
        reminder.getProperties().add(new Description(r.getDescription()));
        reminder.getProperties().add(id) ; 
        calendar.getComponents().add(reminder) ;
      }
    }
    if(exoEvent.isPrivate()) event.getProperties().add(new Clazz(Clazz.PRIVATE.getValue())) ;
    else event.getProperties().add(new Clazz(Clazz.PUBLIC.getValue())) ;
    event.getProperties().getProperty(Property.CLASS).getParameters().add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    String[] attendees = exoEvent.getInvitation() ;
    if(attendees != null && attendees.length > 0) {
      for(int i = 0; i < attendees.length; i++ ) {
        if(attendees[i] != null) {
          event.getProperties().add(new Attendee(attendees[i]));          
        }
      }
      event.getProperties().getProperty(Property.ATTENDEE).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    } 
    if(!Utils.isEmpty(exoEvent.getRepeatType())) {
      Recur rc = null ;
      if(CalendarEvent.RP_NOREPEAT.equalsIgnoreCase(exoEvent.getRepeatType())){
      } else if(CalendarEvent.RP_WEEKEND.equalsIgnoreCase(exoEvent.getRepeatType())){
        rc = new Recur(Recur.WEEKLY, 1) ;
        rc.getDayList().add(WeekDay.SU);
        rc.getDayList().add(WeekDay.SA);
        rc.setInterval(1);
      } else if(CalendarEvent.RP_WORKINGDAYS.equalsIgnoreCase(exoEvent.getRepeatType())){
        rc = new Recur(Recur.WEEKLY, 1) ;
        rc.getDayList().add(WeekDay.MO);
        rc.getDayList().add(WeekDay.TU);
        rc.getDayList().add(WeekDay.WE);
        rc.getDayList().add(WeekDay.TH);
        rc.getDayList().add(WeekDay.FR);
        rc.setInterval(1);
      } else if(CalendarEvent.RP_WEEKLY.equalsIgnoreCase(exoEvent.getRepeatType())){
        rc = new Recur(Recur.WEEKLY, 1) ;
        rc.getDayList().add(WeekDay.SU);
        rc.getDayList().add(WeekDay.MO);
        rc.getDayList().add(WeekDay.TU);
        rc.getDayList().add(WeekDay.WE);
        rc.getDayList().add(WeekDay.TH);
        rc.getDayList().add(WeekDay.FR);
        rc.getDayList().add(WeekDay.SA);
        rc.setInterval(1);
      } else {
        rc = new Recur(exoEvent.getRepeatType().toUpperCase(), 1) ;
        rc.setInterval(1);
      }
      if(rc != null) {
        rc.setWeekStartDay(WeekDay.SU.getDay()) ;
        RRule r = new RRule(rc) ;
        event.getProperties().add(r);
      }
    }
    if(!Utils.isEmpty(exoEvent.getEventState())) {
      XProperty xProperty = new XProperty(Utils.X_STATUS, exoEvent.getEventState()) ;
      event.getProperties().add(xProperty) ;
    }
    event.getProperties().add(id) ;
    calendar.getComponents().add(event);
    return calendar ;
  }

  private net.fortuna.ical4j.model.Calendar getVTask(net.fortuna.ical4j.model.Calendar calendar, CalendarEvent exoEvent) throws Exception {
    Uid id = new Uid(exoEvent.getId()) ; 
    long start = exoEvent.getFromDateTime().getTime() ;
    long end = exoEvent.getToDateTime().getTime() ;
    String summary = exoEvent.getSummary() ;
    VToDo event ;
    if(end > 0) {
      event = new VToDo(new DateTime(start), new DateTime(end), summary);
      if (event.getProperties().getProperty(Property.DTEND) != null) {
        event.getProperties().getProperty(Property.DTEND).getParameters()
        .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
      }
    }else {
      event = new VToDo(new DateTime(start), summary);            
    }
    event.getProperties().getProperty(Property.DTSTART).getParameters()
    .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME); 

    event.getProperties().add(new Description(exoEvent.getDescription()));
    event.getProperties().getProperty(Property.DESCRIPTION).getParameters()
    .add(net.fortuna.ical4j.model.parameter.Value.TEXT);

    event.getProperties().add(new Location(exoEvent.getLocation()));
    event.getProperties().getProperty(Property.LOCATION).getParameters()
    .add(net.fortuna.ical4j.model.parameter.Value.TEXT);

    if(exoEvent.getEventCategoryName() != null){
      event.getProperties().add(new Categories(exoEvent.getEventCategoryName())) ;
      event.getProperties().getProperty(Property.CATEGORIES).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    }
    setPriorityCalEvent(event, exoEvent);
    if (exoEvent.getCompletedDateTime() != null) {
      long completed = exoEvent.getCompletedDateTime().getTime() ;
      event.getProperties().add(new Completed(new DateTime(completed)));
      event.getProperties().getProperty(Property.COMPLETED).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
    }
    event.getProperties().add(new Due(new DateTime(end)));
    event.getProperties().getProperty(Property.DUE).getParameters()
    .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
    if (!Utils.isEmpty(exoEvent.getStatus())) {
      event.getProperties().add(new Status(exoEvent.getStatus()));
      event.getProperties().getProperty(Property.STATUS).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    }
    if(!exoEvent.getAttachment().isEmpty()) {
      for(Attachment att : exoEvent.getAttachment()) {
        byte bytes[] = new byte[att.getInputStream().available()] ; 
        att.getInputStream().read(bytes) ;
        ParameterList plist = new ParameterList() ;
        plist.add(new XParameter(Parameter.CN, att.getName()));
        plist.add(new XParameter(Parameter.FMTTYPE, att.getMimeType()));
        plist.add(Encoding.BASE64) ;
        plist.add(Value.BINARY) ; 
        Attach attach = new Attach(plist, bytes);
        event.getProperties().add(attach) ;
      }
    } 
    if(!exoEvent.getReminders().isEmpty()) {
      for(Reminder r : exoEvent.getReminders()){
        VAlarm reminder = new VAlarm(new DateTime(r.getFromDateTime())) ;
        Long times = new Long(1) ;
        if(r.isRepeat()) times = (r.getAlarmBefore() / r.getRepeatInterval()) ; 
        reminder.getProperties().add(new Repeat(times.intValue()));
        reminder.getProperties().add(new Duration(new Dur(new Long(r.getAlarmBefore()).intValue())));
        if(Reminder.TYPE_POPUP.equals(r.getReminderType())) {
          for(String n : r.getReminderOwner().split(Utils.COMMA)) {
            Attendee a = new Attendee(n) ;
            reminder.getProperties().add(a) ;
          }
          reminder.getProperties().add(Action.DISPLAY);
        }else {
          for(String m : r.getEmailAddress().split(Utils.COMMA)) {
            Attendee a = new Attendee(m) ;
            reminder.getProperties().add(a) ;
          }
          reminder.getProperties().add(Action.EMAIL);
        }
        reminder.getProperties().add(new Summary(exoEvent.getSummary()));
        reminder.getProperties().add(new Description(r.getDescription()));
        reminder.getProperties().add(id) ; 
        calendar.getComponents().add(reminder) ;
      }
    }
    if(exoEvent.isPrivate()) event.getProperties().add(new Clazz(Clazz.PRIVATE.getValue())) ;
    else event.getProperties().add(new Clazz(Clazz.PUBLIC.getValue())) ;
    event.getProperties().getProperty(Property.CLASS).getParameters().add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    String[] attendees = exoEvent.getInvitation() ;
    if(attendees != null && attendees.length > 0) {
      for(int i = 0; i < attendees.length; i++ ) {
        if(attendees[i] != null) {
          event.getProperties().add(new Attendee(attendees[i]));          
        }
      }
      event.getProperties().getProperty(Property.ATTENDEE).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    } 
    if(!Utils.isEmpty(exoEvent.getRepeatType())) {
      Recur rc = null ;
      if(CalendarEvent.RP_NOREPEAT.equalsIgnoreCase(exoEvent.getRepeatType())){
      } else if(CalendarEvent.RP_WEEKEND.equalsIgnoreCase(exoEvent.getRepeatType())){
        rc = new Recur(Recur.WEEKLY, 1) ;
        rc.getDayList().add(WeekDay.SU);
        rc.getDayList().add(WeekDay.SA);
        rc.setInterval(1);
      } else if(CalendarEvent.RP_WORKINGDAYS.equalsIgnoreCase(exoEvent.getRepeatType())){
        rc = new Recur(Recur.WEEKLY, 1) ;
        rc.getDayList().add(WeekDay.MO);
        rc.getDayList().add(WeekDay.TU);
        rc.getDayList().add(WeekDay.WE);
        rc.getDayList().add(WeekDay.TH);
        rc.getDayList().add(WeekDay.FR);
        rc.setInterval(1);
      } else if(CalendarEvent.RP_WEEKLY.equalsIgnoreCase(exoEvent.getRepeatType())){
        rc = new Recur(Recur.WEEKLY, 1) ;
        rc.getDayList().add(WeekDay.SU);
        rc.getDayList().add(WeekDay.MO);
        rc.getDayList().add(WeekDay.TU);
        rc.getDayList().add(WeekDay.WE);
        rc.getDayList().add(WeekDay.TH);
        rc.getDayList().add(WeekDay.FR);
        rc.getDayList().add(WeekDay.SA);
        rc.setInterval(1);
      } else {
        rc = new Recur(exoEvent.getRepeatType().toUpperCase(), 1) ;
        rc.setInterval(1);
      }
      if(rc != null) {
        rc.setWeekStartDay(WeekDay.SU.getDay()) ;
        RRule r = new RRule(rc) ;
        event.getProperties().add(r);
      }
    }
    if(!Utils.isEmpty(exoEvent.getEventState())) {
      XProperty xProperty = new XProperty(Utils.X_STATUS, exoEvent.getEventState()) ;
      event.getProperties().add(xProperty) ;
    }
    event.getProperties().add(id) ;
    calendar.getComponents().add(event);
    return calendar ;
  }


  public OutputStream exportCalendar(String username, List<String> calendarIds, String type) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    if(type.equals(PRIVATE_TYPE)) {
      events = storage_.getUserEventByCalendar(username, calendarIds) ;
    }else if(type.equals(SHARED_TYPE)) {
      events = storage_.getSharedEventByCalendars(username, calendarIds) ;
    }else if(type.equals(PUBLIC_TYPE)){
      events = storage_.getGroupEventByCalendar(calendarIds) ;
    }
    if(events.isEmpty()) return null ;
    net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    calendar.getProperties().add(Method.REQUEST);
    for(CalendarEvent exoEvent : events) {
      if(exoEvent.getEventType().equals(CalendarEvent.TYPE_EVENT)){
        calendar = getVEvent(calendar, exoEvent) ;        
      } else { // task
        calendar = getVTask(calendar, exoEvent) ;
      }
    }    
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    CalendarOutputter output = new CalendarOutputter();
    try {
      output.output(calendar, bout) ;
    }catch(ValidationException e) {
      e.printStackTrace() ;
      return null ;
    }  
    return bout;
  }

  public OutputStream exportCalendar(String username, List<String> calendarIds, String type, int limited) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    if(type.equals(PRIVATE_TYPE)) {
      EventQuery eventQuery = new EventQuery() ;
      eventQuery.setCalendarId(calendarIds.toArray(new String[]{})) ;
      eventQuery.setOrderBy(new String[]{"jcr:lastModified"}) ;
      eventQuery.setLimitedItems(limited) ;
      events = storage_.getEvents(username, eventQuery, null) ;
    }else if(type.equals(SHARED_TYPE)) {
      events = storage_.getSharedEventByCalendars(username, calendarIds) ;
    }else if(type.equals(PUBLIC_TYPE)){
      events = storage_.getGroupEventByCalendar(calendarIds) ;
    }
    if(events.isEmpty()) return null ;
    net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    calendar.getProperties().add(Method.REQUEST);
    for(CalendarEvent exoEvent : events) {
      if(exoEvent.getEventType().equals(CalendarEvent.TYPE_EVENT)){
        calendar = getVEvent(calendar, exoEvent) ;        
      } else { // task
        calendar = getVTask(calendar, exoEvent) ;
      }
    }    
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    CalendarOutputter output = new CalendarOutputter();
    try {
      output.output(calendar, bout) ;
    }catch(ValidationException e) {
      e.printStackTrace() ;
      return null ;
    } 
    return bout;
  }

  public OutputStream exportEventCalendar(String username, String calendarId, String type, String eventId) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    List<String> calendarIds = Arrays.asList(new String[]{calendarId}) ;
    /* if(type.equals(PRIVATE_TYPE)) {
      events.add(storage_.getUserEvent(sProvider, username, calendarId, eventId))  ;
    } */
    if(type.equals(PRIVATE_TYPE)) {
      events = storage_.getUserEventByCalendar(username, calendarIds) ;
    }else if(type.equals(SHARED_TYPE)) {
      events = storage_.getSharedEventByCalendars(username, calendarIds) ;
    }else if(type.equals(PUBLIC_TYPE)){
      events = storage_.getGroupEventByCalendar(calendarIds) ;
    }
    net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    calendar.getProperties().add(Method.REQUEST);
    for(CalendarEvent exoEvent : events) {
      if(exoEvent.getId().equals(eventId)) {
        if(exoEvent.getEventType().equals(CalendarEvent.TYPE_EVENT)){
          calendar = getVEvent(calendar, exoEvent) ;
        } else {
          calendar = getVTask(calendar, exoEvent) ;
        }
        break ;
      }
    }
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    CalendarOutputter output = new CalendarOutputter();
    try {
      output.output(calendar, bout) ;
    }catch(ValidationException e) {
      e.printStackTrace() ;
      return null ;
    }    
    return bout;
  }

  public void importCalendar(String username, InputStream icalInputStream, String calendarName) throws Exception {
    CalendarBuilder calendarBuilder = new CalendarBuilder() ;
    net.fortuna.ical4j.model.Calendar iCalendar = calendarBuilder.build(icalInputStream) ;
    NodeIterator iter = storage_.getCalendarCategoryHome(username).getNodes() ;
    CalendarService  calService =    
    (CalendarService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if(calService == null) calService = (CalendarService) ExoContainerContext.getContainerByName(PortalContainer.getCurrentPortalContainerName()).getComponentInstanceOfType(CalendarService.class);
        
    Node cat = null;
    String categoryId ;
    //Map<String, VEvent> vEventData = new HashMap<String, VEvent>() ;
    Map<String, VFreeBusy> vFreeBusyData = new HashMap<String, VFreeBusy>() ;
    Map<String, VAlarm> vAlarmData = new HashMap<String, VAlarm>() ;
    boolean isExists = false ;
    while(iter.hasNext()) {
      cat = iter.nextNode() ;
      if(cat.getProperty(Utils.EXO_NAME).getString().equals("Imported")) {
        isExists = true ;
        break ;
      }
    }
    if(!isExists) {
      CalendarCategory calendarCate = new CalendarCategory() ;
      calendarCate.setDescription("Imported icalendar category") ;
      calendarCate.setName("Imported") ;
      categoryId = calendarCate.getId() ;
      calService.saveCalendarCategory(username, calendarCate, true) ;
    }else {
      categoryId = cat.getProperty(Utils.EXO_ID).getString() ;
    }
    Calendar exoCalendar = new Calendar() ;
    exoCalendar.setName(calendarName) ;
    exoCalendar.setCalendarColor(org.exoplatform.calendar.service.Calendar.COLORS[new Random().nextInt(org.exoplatform.calendar.service.Calendar.COLORS.length -1)]) ;
    exoCalendar.setDescription(iCalendar.getProductId().getValue()) ;
    exoCalendar.setCategoryId(categoryId) ;
    exoCalendar.setPublic(false) ;
    exoCalendar.setCalendarOwner(username) ;
    calService.saveUserCalendar(username, exoCalendar, true) ;   
    ComponentList componentList = iCalendar.getComponents() ;
    CalendarEvent exoEvent ;
    for(Object obj : componentList) {
      if(obj instanceof VEvent) {
        VEvent v = (VEvent)obj ;
        //vEventData.put(v.getUid().getValue(), v) ;
        if(!v.getAlarms().isEmpty()) {
          for (Object o : v.getAlarms()) {
            if (o instanceof VAlarm) {
              VAlarm va = (VAlarm)o;
              vAlarmData.put(v.getUid().getValue()+":"+ va.getProperty(Property.ACTION).getName(), va) ;
            }
          }
        }

      }
      if(obj instanceof VFreeBusy) vFreeBusyData.put(((VFreeBusy)obj).getUid().getValue(), (VFreeBusy)obj) ;
    }
    for(Object obj : componentList) {
      if(obj instanceof VEvent){
        VEvent event = (VEvent)obj ;
        exoEvent = new CalendarEvent() ;
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try{
            calService.saveEventCategory(username, evCate, true) ;
          }catch(ItemExistsException e){ 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          } catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryId(evCate.getId()) ;
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(exoCalendar.getId()) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_EVENT) ;

        String sValue = "" ;
        String eValue = "" ;
        if(event.getStartDate() != null) {
          sValue = event.getStartDate().getValue() ;
          exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        }
        if(event.getEndDate() != null) {
          eValue = event.getEndDate().getValue() ;
          exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        }
        if (sValue.length() == 8 && eValue.length() == 8 ) {
          //exoEvent.setAllday(true) ;
          exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
        }
        if (sValue.length() > 8 && eValue.length() > 8 ) {         
          if("0000".equals(sValue.substring(9,13)) && "0000".equals(eValue.substring(9,13)) ) {
            //exoEvent.setAllday(true);
            exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
          }
        }
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;

        setPriorityExoEvent(event, exoEvent);

        /*if(vFreeBusyData.get(event.getUid().getValue()) != null) {
          exoEvent.setStatus(CalendarEvent.ST_BUSY) ;
        }*/
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        //List<Reminder> list = null ;
        /*if(!event.getAlarms().isEmpty()){
          list = new ArrayList<Reminder>() ;
          for(Object o : event.getAlarms()){
            VAlarm reminder = (VAlarm)o ;
            Reminder r = null ;
            if( reminder.getAction().equals(Action.EMAIL)) {
              r = new Reminder(Reminder.TYPE_EMAIL) ;
            } else if( reminder.getAction().equals(Action.DISPLAY))  {
              r = new Reminder(Reminder.TYPE_POPUP) ;
            }
            r.setFromDateTime(exoEvent.getFromDateTime()) ;
            //r.setAlarmBefore(reminder.getDuration().getDuration().)
            list.add(r) ;
          }
          if(!list.isEmpty()) {
            exoEvent.setReminders(list) ;
          }

        }*/
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        calService.saveUserEvent(username, exoCalendar.getId(), exoEvent, true) ;
      } else if (obj instanceof VToDo) {
        VToDo event = (VToDo)obj ;
        exoEvent = new CalendarEvent() ;
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try{
            calService.saveEventCategory(username, evCate, true) ;
          }catch(ItemExistsException e){ 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          }catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(exoCalendar.getId()) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        if(event.getStartDate() != null) exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        if(event.getDue() != null) exoEvent.setToDateTime(event.getDue().getDate()) ;
        //if(event.getEndDate() != null) exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        
        setPriorityExoEvent(event, exoEvent);
        
        if(vFreeBusyData.get(event.getUid().getValue()) != null) {
          exoEvent.setStatus(CalendarEvent.ST_BUSY) ;
        }
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        //List<Reminder> list = null ;
        /*if(!event.getAlarms().isEmpty()){
          list = new ArrayList<Reminder>() ;
          for(Object o : event.getAlarms()){
            VAlarm reminder = (VAlarm)o ;
            Reminder r = null ;
            if( reminder.getAction().equals(Action.EMAIL)) {
              r = new Reminder(Reminder.TYPE_EMAIL) ;
            } else if( reminder.getAction().equals(Action.DISPLAY))  {
              r = new Reminder(Reminder.TYPE_POPUP) ;
            }
            r.setFromDateTime(exoEvent.getFromDateTime()) ;
            //r.setAlarmBefore(reminder.getDuration().getDuration().)
            list.add(r) ;
          }
          if(!list.isEmpty()) {
            exoEvent.setReminders(list) ;
          }

        }*/
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        calService.saveUserEvent(username, exoCalendar.getId(), exoEvent, true) ;
      }
    }
  }

  public List<CalendarEvent> getEventObjects(InputStream icalInputStream) throws Exception {
    CalendarBuilder calendarBuilder = new CalendarBuilder() ;
    net.fortuna.ical4j.model.Calendar iCalendar = calendarBuilder.build(icalInputStream) ;
    ComponentList componentList = iCalendar.getComponents() ;
    List<CalendarEvent> eventList = new ArrayList<CalendarEvent>() ;
    VEvent event ;
    for(Object obj : componentList) {
      if(obj instanceof VEvent){
        CalendarEvent exoEvent = new CalendarEvent() ;
        event = (VEvent)obj ;
        if(event.getProperty(Property.UID) != null) {
          exoEvent.setId(event.getProperty(Property.UID).getValue()) ;
        }
        if(event.getProperty(Property.CATEGORIES) != null) {
          exoEvent.setEventCategoryName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
        }
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
        if(event.getStartDate() != null) exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        if(event.getEndDate() != null) exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;

        setPriorityExoEvent(event, exoEvent);

        try {
          RRule r = (RRule)event.getProperty(Property.RRULE) ;
          if(r != null &&  r.getRecur() != null) {
            Recur rc = r.getRecur() ;
            rc.getFrequency();
            if(Recur.WEEKLY.equalsIgnoreCase(rc.getFrequency())) {
              if(rc.getDayList().size() == 2) {
                exoEvent.setRepeatType(CalendarEvent.RP_WEEKEND) ;
              } else if(rc.getDayList().size() == 5) {
                exoEvent.setRepeatType(CalendarEvent.RP_WORKINGDAYS) ;
              } if(rc.getDayList().size() == 7) {
                exoEvent.setRepeatType(CalendarEvent.RP_WEEKLY) ;
              }
            } else {
              exoEvent.setRepeatType(rc.getFrequency().toLowerCase()) ;
            }
          }
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        exoEvent.setPrivate(true) ;
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(attendees.size() < 1) {
          exoEvent.setInvitation(new String[]{}) ;
        }else {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        eventList.add(exoEvent) ; 
      }
    }
    return eventList;
  }

  public boolean isValidate(InputStream icalInputStream) throws Exception {
    try {
      CalendarBuilder calendarBuilder = new CalendarBuilder() ;
      calendarBuilder.build(icalInputStream) ;
      return true ;
    } catch (Exception e) {
      e.printStackTrace() ;
      return false ;
    }
  }

  public void importToCalendar(String username, InputStream icalInputStream, String calendarId) throws Exception {
    CalendarBuilder calendarBuilder = new CalendarBuilder() ;
    net.fortuna.ical4j.model.Calendar iCalendar = calendarBuilder.build(icalInputStream) ;
    //Map<String, VEvent> vEventData = new HashMap<String, VEvent>() ;
    Map<String, VFreeBusy> vFreeBusyData = new HashMap<String, VFreeBusy>() ;
    Map<String, VAlarm> vAlarmData = new HashMap<String, VAlarm>() ;
    CalendarService  calService = (CalendarService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class) ;
    ComponentList componentList = iCalendar.getComponents() ;
    CalendarEvent exoEvent ;
    for(Object obj : componentList) {
      if(obj instanceof VEvent) {
        VEvent v = (VEvent)obj ;
        //vEventData.put(v.getUid().getValue(), v) ;
        if(!v.getAlarms().isEmpty()) {
          for (Object o : v.getAlarms()) {
            if (o instanceof VAlarm) {
              VAlarm va = (VAlarm)o;
              vAlarmData.put(v.getUid().getValue()+":"+ va.getProperty(Property.ACTION).getName(), va) ;
            }
          }
        }
      }
      if(obj instanceof VFreeBusy) vFreeBusyData.put(((VFreeBusy)obj).getUid().getValue(), (VFreeBusy)obj) ;
    }
    for(Object obj : componentList) {
      if(obj instanceof VEvent){
        VEvent event = (VEvent)obj ;
        exoEvent = new CalendarEvent() ;
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try{
            calService.saveEventCategory(username, evCate, true) ;
          }catch(ItemExistsException e){ 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          } catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryId(evCate.getId()) ;
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(calendarId) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_EVENT) ;

        String sValue = "" ;
        String eValue = "" ;
        if(event.getStartDate() != null) {
          sValue = event.getStartDate().getValue() ;
          exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        }
        if(event.getEndDate() != null) {
          eValue = event.getEndDate().getValue() ;
          exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        }
        if (sValue.length() == 8 && eValue.length() == 8 ) {
          //exoEvent.setAllday(true) ;
          exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
        }
        if (sValue.length() > 8 && eValue.length() > 8 ) {         
          if("0000".equals(sValue.substring(9,13)) && "0000".equals(eValue.substring(9,13)) ) {
            //exoEvent.setAllday(true);
            exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
          }
        }
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        setPriorityExoEvent(event, exoEvent);
        if(vFreeBusyData.get(event.getUid().getValue()) != null) {
          exoEvent.setEventState(CalendarEvent.ST_BUSY) ;
        }
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        //List<Reminder> list = null ;
        /*if(!event.getAlarms().isEmpty()){
            list = new ArrayList<Reminder>() ;
            for(Object o : event.getAlarms()){
              VAlarm reminder = (VAlarm)o ;
              Reminder r = null ;
              if( reminder.getAction().equals(Action.EMAIL)) {
                r = new Reminder(Reminder.TYPE_EMAIL) ;
              } else if( reminder.getAction().equals(Action.DISPLAY))  {
                r = new Reminder(Reminder.TYPE_POPUP) ;
              }
              r.setFromDateTime(exoEvent.getFromDateTime()) ;
              //r.setAlarmBefore(reminder.getDuration().getDuration().)
              list.add(r) ;
            }
            if(!list.isEmpty()) {
              exoEvent.setReminders(list) ;
            }

          }*/
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          RRule r = (RRule)event.getProperty(Property.RRULE) ;
          if(r != null &&  r.getRecur() != null) {
            Recur rc = r.getRecur() ;
            rc.getFrequency();
            if(Recur.WEEKLY.equalsIgnoreCase(rc.getFrequency())) {
              if(rc.getDayList().size() == 2) {
                exoEvent.setRepeatType(CalendarEvent.RP_WEEKEND) ;
              } else if(rc.getDayList().size() == 5) {
                exoEvent.setRepeatType(CalendarEvent.RP_WORKINGDAYS) ;
              } if(rc.getDayList().size() == 7) {
                exoEvent.setRepeatType(CalendarEvent.RP_WEEKLY) ;
              }
            } else {
              exoEvent.setRepeatType(rc.getFrequency().toLowerCase()) ;
            }
          }
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        switch (storage_.getTypeOfCalendar(username, calendarId)){
        case Utils.PRIVATE_TYPE:
          calService.saveUserEvent(username, calendarId, exoEvent, true) ;
          break;
        case Utils.SHARED_TYPE:
          calService.saveEventToSharedCalendar(username, calendarId, exoEvent, true);
          break;
        case Utils.PUBLIC_TYPE:
          calService.savePublicEvent(calendarId, exoEvent, true);
          break;
        }
      }else if(obj instanceof VToDo){ 
        VToDo event = (VToDo)obj ;
        exoEvent = new CalendarEvent() ;
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try{
            calService.saveEventCategory(username, evCate, true) ;
          }catch(ItemExistsException e){ 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          } catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryId(evCate.getId()) ;
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(calendarId) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        if(event.getStartDate() != null) exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        if (event.getDue() != null) exoEvent.setToDateTime(event.getDue().getDate()) ;
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        
        setPriorityExoEvent(event, exoEvent);
        
        if(vFreeBusyData.get(event.getUid().getValue()) != null) {
          exoEvent.setStatus(CalendarEvent.ST_BUSY) ;
        }
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        //List<Reminder> list = null ;
        /*if(!event.getAlarms().isEmpty()){
            list = new ArrayList<Reminder>() ;
            for(Object o : event.getAlarms()){
              VAlarm reminder = (VAlarm)o ;
              Reminder r = null ;
              if( reminder.getAction().equals(Action.EMAIL)) {
                r = new Reminder(Reminder.TYPE_EMAIL) ;
              } else if( reminder.getAction().equals(Action.DISPLAY))  {
                r = new Reminder(Reminder.TYPE_POPUP) ;
              }
              r.setFromDateTime(exoEvent.getFromDateTime()) ;
              //r.setAlarmBefore(reminder.getDuration().getDuration().)
              list.add(r) ;
            }
            if(!list.isEmpty()) {
              exoEvent.setReminders(list) ;
            }

          }*/
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          RRule r = (RRule)event.getProperty(Property.RRULE) ;
          if(r != null &&  r.getRecur() != null) {
            Recur rc = r.getRecur() ;
            rc.getFrequency();
            if(Recur.WEEKLY.equalsIgnoreCase(rc.getFrequency())) {
              if(rc.getDayList().size() == 2) {
                exoEvent.setRepeatType(CalendarEvent.RP_WEEKEND) ;
              } else if(rc.getDayList().size() == 5) {
                exoEvent.setRepeatType(CalendarEvent.RP_WORKINGDAYS) ;
              } if(rc.getDayList().size() == 7) {
                exoEvent.setRepeatType(CalendarEvent.RP_WEEKLY) ;
              }
            } else {
              exoEvent.setRepeatType(rc.getFrequency().toLowerCase()) ;
            }
          }
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }

        switch (storage_.getTypeOfCalendar(username, calendarId)){
        case Utils.PRIVATE_TYPE:
          calService.saveUserEvent(username, calendarId, exoEvent, true) ;
          break;
        case Utils.SHARED_TYPE:
          calService.saveEventToSharedCalendar(username, calendarId, exoEvent, true);
          break;
        case Utils.PUBLIC_TYPE:
          calService.savePublicEvent(calendarId, exoEvent, true);
          break;
        }
      }      
    }
  }

  @Override
  public ByteArrayOutputStream exportEventCalendar(CalendarEvent exoEvent) throws Exception {
    net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    calendar.getProperties().add(Method.REQUEST);
        if(exoEvent.getEventType().equals(CalendarEvent.TYPE_EVENT)){
          calendar = getVEvent(calendar, exoEvent) ;
        } else {
          calendar = getVTask(calendar, exoEvent) ;
        }
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    CalendarOutputter output = new CalendarOutputter();
    try {
      output.output(calendar, bout) ;
    }catch(ValidationException e) {
      e.printStackTrace() ;
      return null ;
    }    
    return bout;
  }
}

