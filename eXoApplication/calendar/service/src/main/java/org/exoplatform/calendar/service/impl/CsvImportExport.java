/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Apr 1, 2008  
 */
public class CsvImportExport implements CalendarImportExport {
  public static final String CSV_PATTERN = "\"([^\"]+?)\",?|([^,]+),?|,";
  private static Pattern csvRE;
  public static String EV_SUMMARY = "Subject".intern() ;
  public static String EV_STARTDATE = "Start Date".intern() ;
  public static String EV_STARTTIME = "Start Time".intern() ;
  public static String EV_ENDDATE = "End Date".intern() ;
  public static String EV_ENDTIME = "End Time".intern() ;
  public static String EV_ALLDAY = "All day event".intern() ;
  public static String EV_HASREMINDER = "Reminder on/off".intern() ;
  public static String EV_REMINDERDATE = "Reminder Date".intern() ;
  public static String EV_REMINDERTIME = "Reminder Time".intern() ;
  public static String EV_MEETINGORGANIZER = "Meeting Organizer".intern() ;
  public static String EV_ATTENDEES = "Required Attendees".intern() ;
  public static String EV_INVITATION = "Optional Attendees".intern() ;
  public static String EV_ATTACTMENT = "Meeting Resources".intern() ;
  public static String EV_BILLINGINFO = "Billing Information".intern();
  public static String EV_CATEGORIES = "Categories".intern();
  public static String EV_DESCRIPTION = "Description".intern() ;
  public static String EV_LOCATION = "Location".intern() ;
  public static String EV_MILEAGE = "Mileage".intern() ;
  public static String EV_PRIORITY = "Priority".intern() ;
  public static String EV_PRIVATE = "Private".intern() ;
  public static String EV_SENSITIVITY = "Sensitivity".intern() ;
  public static String EV_STATUS = "Show time as".intern() ;

  private static LinkedHashMap<String, Integer> dataMap = new LinkedHashMap<String, Integer>() ;
  private static String[] keys = new String[]{EV_SUMMARY, EV_STARTDATE, EV_STARTTIME, 
    EV_ENDDATE, EV_ENDTIME, EV_ALLDAY, EV_HASREMINDER, EV_REMINDERDATE, EV_REMINDERTIME, 
    EV_MEETINGORGANIZER, EV_ATTENDEES, EV_INVITATION, EV_ATTACTMENT, EV_BILLINGINFO, EV_CATEGORIES, 
    EV_DESCRIPTION, EV_LOCATION,EV_MILEAGE, EV_PRIORITY, EV_PRIVATE, EV_SENSITIVITY, EV_STATUS} ;

  private static final String PRIVATE_TYPE = "0".intern() ;
  private static final String SHARED_TYPE = "1".intern() ;
  private static final String PUBLIC_TYPE = "2".intern() ;
  private JCRDataStorage storage_ ;

  /* public static void main(String[] argv) throws IOException {
    System.out.println(CSV_PATTERN);
    new CSVRE().process(new BufferedReader(new InputStreamReader(System.in)));
  }*/

  /** Construct a regex-based CSV parser. */

  public CsvImportExport(JCRDataStorage dataStore) {
    csvRE = Pattern.compile(CSV_PATTERN);
    storage_ = dataStore ;
    int count = 0 ;
    for(String k : keys) {
      dataMap.put(k, count) ;
      count++ ;
    }
  }
  /** Process one file. Delegates to parse() a line at a time */
  public List<CalendarEvent> process(BufferedReader in) throws IOException {
    String line;
    // For each line...
    int lineCount = 0 ;
    List<CalendarEvent> eventList = new ArrayList<CalendarEvent>() ;
    while ((line = in.readLine()) != null) {
      if(lineCount > 0) {
        List<String> l = parse(line);
        if(!Utils.isEmpty(l.get(dataMap.get(EV_SUMMARY)))) {
          boolean isValid = true ;
          CalendarEvent eventObj = new CalendarEvent() ;
          eventObj.setEventType(CalendarEvent.TYPE_EVENT) ;
          eventObj.setCalType(PRIVATE_TYPE) ;
          DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a") ;
          // System.out.println("df " + df.getTimeInstance().getCalendar().getTime());
          //Event Summnary
          if(!Utils.isEmpty(l.get(dataMap.get(EV_SUMMARY)))) eventObj.setSummary(l.get(dataMap.get(EV_SUMMARY))) ;
          //Event fromdate
          if(!Utils.isEmpty(l.get(dataMap.get(EV_STARTDATE)))) {
            if(!Utils.isEmpty(l.get(dataMap.get(EV_STARTTIME)))) {
              Calendar cal = GregorianCalendar.getInstance() ;  
              try {
                cal.setTime(df.parse(l.get(dataMap.get(EV_STARTDATE)) + " " + l.get(dataMap.get(EV_STARTTIME)))) ;
              } catch (Exception e) {
                e.printStackTrace() ;
                isValid = false ;
                //break ;
              }
              if(!Utils.isEmpty(l.get(dataMap.get(EV_ALLDAY))) && isValid){
                if(Boolean.parseBoolean(l.get(dataMap.get(EV_ALLDAY)))){ 
                  cal.set(Calendar.HOUR_OF_DAY, 0) ;
                  cal.set(Calendar.MINUTE, 0) ;
                  cal.set(Calendar.MILLISECOND, 0) ;
                } 
              }
              if(isValid) eventObj.setFromDateTime(cal.getTime()) ;
            }
          } 
          //Event todate
          if(!Utils.isEmpty(l.get(dataMap.get(EV_ENDDATE)))) {
            if(!Utils.isEmpty(l.get(dataMap.get(EV_ENDTIME)))) {
              Calendar cal = GregorianCalendar.getInstance() ;  
              try {
                cal.setTime(df.parse(l.get(dataMap.get(EV_ENDDATE)) + " " + l.get(dataMap.get(EV_ENDTIME)))) ;
              } catch (Exception e) {
                e.printStackTrace() ;
                isValid = false ;
                //break ;
              }
              if(!Utils.isEmpty(l.get(dataMap.get(EV_ALLDAY))) && isValid){
                if(Boolean.parseBoolean(l.get(dataMap.get(EV_ALLDAY)))){ 
                  cal.set(Calendar.HOUR_OF_DAY, 23) ;
                  cal.set(Calendar.MINUTE, 59) ;
                  cal.set(Calendar.MILLISECOND, 999) ;
                } 
              }
              if(isValid) eventObj.setToDateTime(cal.getTime()) ;
            }
            //TODO check againt for reminders
            //Event reminders
            /*if(!Utils.isEmpty(l.get(dataMap.get(EV_HASREMINDER)))) {
            if(Boolean.parseBoolean(l.get(dataMap.get(EV_HASREMINDER)))) {
              if(!Utils.isEmpty(l.get(dataMap.get(EV_REMINDERDATE))) && !Utils.isEmpty(l.get(dataMap.get(EV_REMINDERTIME)))) {
                try {
                  List<Reminder> rmList = new ArrayList<Reminder>() ;
                  Calendar cal = GregorianCalendar.getInstance() ;  
                  cal.setTime(df.parse(l.get(dataMap.get(EV_REMINDERDATE)) + " " + l.get(dataMap.get(EV_REMINDERTIME)))) ;
                  Reminder rm = new Reminder() ;
                  rm.setReminderType(Reminder.TYPE_POPUP) ;
                  rm.setEventId(eventObj.getId()) ;
                  rm.setFromDateTime(eventObj.getFromDateTime()) ;
                  rm.setRepeate(false);
                  rm.setAlarmBefore(cal.getTimeInMillis()) ;
                  rmList.add(rm) ;
                  eventObj.setReminders(rmList) ;
                } catch (Exception e) {
                  e.printStackTrace() ;
                }
              }
            }
          }*/
            //Event oner 9
            //Event Participants 10
            if(!Utils.isEmpty(l.get(dataMap.get(EV_ATTENDEES)))) {
              eventObj.setParticipant(l.get(dataMap.get(EV_ATTENDEES)).split(";")) ;
            }
            //Event Invitation 11
            if(!Utils.isEmpty(l.get(dataMap.get(EV_INVITATION)))) {
              eventObj.setInvitation(l.get(dataMap.get(EV_INVITATION)).split(";")) ;
            }
            //Event categories 14
            if(!Utils.isEmpty(l.get(dataMap.get(EV_CATEGORIES)))) {
              eventObj.setEventCategoryId(l.get(dataMap.get(EV_CATEGORIES)))  ;
            } else {
              eventObj.setEventCategoryId("csvImported") ;
            }
            //Event Place
            if(!Utils.isEmpty(l.get(dataMap.get(EV_LOCATION)))) {
              eventObj.setLocation(l.get(dataMap.get(EV_LOCATION)))  ;
            }
            if(!Utils.isEmpty(l.get(dataMap.get(EV_DESCRIPTION)))) {
              eventObj.setDescription(l.get(dataMap.get(EV_DESCRIPTION)))  ;
            }
            if(!Utils.isEmpty(l.get(dataMap.get(EV_STATUS)))) {
              eventObj.setRepeatType(l.get(dataMap.get(EV_STATUS)))  ;
            }
            if(!Utils.isEmpty(l.get(dataMap.get(EV_PRIORITY)))) {
              eventObj.setPriority(l.get(dataMap.get(EV_PRIORITY)))  ;
            }
          } 
          /* System.out.println("Found " + l.size() + " items.");
        for (int i = 0; i < l.size(); i++) {
          System.out.print(l.get(i) + ",");
        }*/
         if(isValid) eventList.add(eventObj) ;
        }
      }
      lineCount ++ ;
    }
    return eventList ;
  }

  /** Parse one line.
   * @return List of Strings, minus their double quotes
   */
  public List parse(String line) {
    List<String> list = new ArrayList<String>();
    Matcher m = csvRE.matcher(line);
    // For each field
    while (m.find()) {
      String match = m.group();
      if (match == null)
        break;
      if (match.endsWith(",")) {  // trim trailing ,
        match = match.substring(0, match.length() - 1);
      }
      if (match.startsWith("\"")) { // assume also ends with
        match = match.substring(1, match.length() - 1);
      }
      if (match.length() == 0)
        match = null;
      list.add(match);
    }
    return list;
  }

  public OutputStream exportCalendar(SessionProvider sProvider, String username, List<String> calendarIds, String type) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void importCalendar(SessionProvider sProvider, String username, InputStream csvInputStream, String calendarName) throws Exception {
    List<CalendarEvent> data = process(new BufferedReader(new InputStreamReader(csvInputStream))) ;
    if(data.size() > 0) {
      GregorianCalendar currentDateTime = new GregorianCalendar() ;
      NodeIterator iter = storage_.getCalendarCategoryHome(sProvider, username).getNodes() ;
      Node cat = null;
      String categoryId ;
      boolean isExists = false ;
      while(iter.hasNext()) {
        cat = iter.nextNode() ;
        if(cat.getProperty("exo:name").getString().equals("Imported")) {
          isExists = true ;
          break ;
        }
      }
      if(!isExists) {
        CalendarCategory calendarCate = new CalendarCategory() ;
        currentDateTime = new GregorianCalendar() ;
        calendarCate.setDescription("Imported icalendar category") ;
        calendarCate.setName("Imported") ;
        categoryId = calendarCate.getId() ;
        storage_.saveCalendarCategory(sProvider, username, calendarCate, true) ;
      }else {
        categoryId = cat.getProperty("exo:id").getString() ;
      }
      org.exoplatform.calendar.service.Calendar exoCalendar = new org.exoplatform.calendar.service.Calendar() ;
      exoCalendar.setName(calendarName) ;
      exoCalendar.setCalendarColor(exoCalendar.COLORS[new Random().nextInt(exoCalendar.COLORS.length -1)]) ;
      exoCalendar.setDescription("") ;
      exoCalendar.setCategoryId(categoryId) ;
      exoCalendar.setPublic(true) ;
      storage_.saveUserCalendar(sProvider, username, exoCalendar, true) ;   
      for(CalendarEvent exoEvent : data) {
        exoEvent.setCalendarId(exoCalendar.getId()) ;
        storage_.saveUserEvent(sProvider, username, exoCalendar.getId(), exoEvent, true) ;
      }
    }
  }
}
