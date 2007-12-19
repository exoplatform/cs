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
package org.exoplatform.calendar.webui;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.popup.UIEventCategoryManager;
import org.exoplatform.calendar.webui.popup.UIEventForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIPopupContainer;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.calendar.webui.popup.UITaskForm;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

public abstract class UICalendarView extends UIForm  implements CalendarView {
  final static protected String EVENT_CATEGORIES = "eventCategories".intern() ;

  final public static String JANUARY = "January".intern() ;
  final public static String FEBRUARY = "February".intern() ;
  final public static String MARCH = "March".intern() ;
  final public static String APRIL = "April".intern() ;
  final public static String MAY = "May".intern() ;
  final public static String JUNE = "June".intern() ;
  final public static String JULY = "July".intern() ;
  final public static String AUGUST = "August".intern() ;
  final public static String SEPTEMBER = "September".intern() ;
  final public static String OCTOBER = "October".intern() ;
  final public static String NOVEMBER = "November".intern() ;
  final public static String DECEMBER = "December".intern() ;
  final public static String[] MONTHS = {JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER} ;

  final public static String MONDAY = "Monday".intern() ;
  final public static String TUESDAY = "Tuesday".intern() ;
  final public static String WEDNESDAY = "Wednesday".intern() ;
  final public static String THURSDAY = "Thursday".intern() ;
  final public static String FRIDAY = "Friday".intern() ;
  final public static String SATURDAY = "Saturday".intern() ;
  final public static String SUNDAY = "Sunday".intern() ;
  final public static String[] DAYS = {SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY} ;
  final public static int TYPE_DATE = 1 ;
  final public static int TYPE_WEEK = 2 ;
  final public static int TYPE_MONTH = 3 ;
  final public static int TYPE_YEAR = 4 ;

  final public static String ACT_NEXT = "MoveNext".intern() ;
  final public static String ACT_PREVIOUS  = "MovePrevious".intern() ;


  public final static String ACT_ADDNEW_EVENT = "QuickAddNewEvent".intern() ;
  public final static String ACT_ADDNEW_TASK = "QuickAddNewTask".intern() ;
  public final static String[] CONTEXT_MENU = {ACT_ADDNEW_EVENT, ACT_ADDNEW_TASK}  ;
  //public final static String ACT_GOTO_DATE = "GotoDate".intern() ;
  public final static String ACT_VIEW = "View".intern() ;
  public final static String ACT_EDIT = "Edit".intern() ;
  public final static String ACT_DELETE = "Delete".intern() ;
  public final static String[] QUICKEDIT_MENU = {ACT_VIEW, ACT_EDIT, ACT_DELETE} ; 
  final public static String CALNAME = "calName".intern() ;
  final public static String CALENDARID = "calendarId".intern() ;
  final public static String CALTYPE = "calType".intern() ;
  final public static String EVENTID = "eventId".intern() ;
  final public static String DAY = "day".intern() ;
  final public static String MONTH = "month".intern() ;
  final public static String YEAR = "year".intern() ;

  final public static String TYPE_EVENT = CalendarEvent.TYPE_EVENT ;
  final public static String TYPE_TASK = CalendarEvent.TYPE_TASK ;
  final public static String TYPE_BOTH = "Both".intern() ;

  private String viewType_ = TYPE_BOTH ;
  private String [] views = {TYPE_BOTH, TYPE_EVENT, TYPE_TASK} ;

  protected Calendar calendar_ = null ;
  public boolean isShowEvent_ = true;
  private String editedEventId_ = null ;
  private int timeInterval_ = 30 ;
  private CalendarSetting calendarSetting_ ;

  private String dateTimeFormat_  ;
  protected List<String> privateCalendarIds = new ArrayList<String>() ;
  protected List<String> publicCalendarIds = new ArrayList<String>() ;

  final public static Map<Integer, String> monthsName_ = new HashMap<Integer, String>() ;
  private Map<Integer, String> daysMap_ = new LinkedHashMap<Integer, String>() ;
  private Map<Integer, String> monthsMap_ = new LinkedHashMap<Integer, String>() ;
  private Map<String, String> priorityMap_ = new HashMap<String, String>() ;
  abstract LinkedHashMap<String, CalendarEvent> getDataMap() ;
  protected DateFormatSymbols dfs_  ;
  public UICalendarView() throws Exception{
    initCategories() ;

    calendar_ = GregorianCalendar.getInstance() ;
    calendar_.setLenient(false) ;
    int gmtoffset = calendar_.get(Calendar.DST_OFFSET) + calendar_.get(Calendar.ZONE_OFFSET);
    calendar_.setTimeInMillis(System.currentTimeMillis() - gmtoffset) ;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy k:m:s z");
    dfs_ = new DateFormatSymbols() ;
    System.out.println("\n\n GMT Time " + simpleDateFormat.format(calendar_.getTime()));
    int i = 0 ; 
    for(String month : dfs_.getMonths()) {
      monthsMap_.put(i, month) ;
      i++ ;
    }
    int j = 1 ;
    for(String month : dfs_.getWeekdays()) {
      daysMap_.put(j, month) ;
      j++ ;
    }
    int p = 1 ;
    for(String s : CalendarEvent.PRIORITY) {
      priorityMap_.put(String.valueOf(p), s) ;
      p ++ ;
    }
    applySeting() ;
  }
  public void applySeting() throws Exception {
    try {
      calendarSetting_ = getAncestorOfType(UICalendarPortlet.class).getCalendarSetting() ;
    } catch (Exception e) {
      CalendarService calService = getApplicationComponent(CalendarService.class) ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      calendarSetting_ = calService.getCalendarSetting(SessionsUtils.getSessionProvider(), username) ;
    }
    dateTimeFormat_ = getDateFormat() + " " + getTimeFormat() ;
    TimeZone settingTimeZone = TimeZone.getTimeZone(calendarSetting_.getTimeZone()) ;
    // calendar_.set(Calendar.ZONE_OFFSET, settingTimeZone.getRawOffset()) ;
  }
  public void setViewType(String viewType) { this.viewType_ = viewType ; }
  public String getViewType() { return viewType_ ; }
  protected String[] getViews() {return views ; }
  public void setLastUpdatedEventId(String eventId) {editedEventId_ = eventId;}
  public String getLastUpdatedEventId() {return editedEventId_;}

  public String[] getPublicCalendars() throws Exception{
    try{
      return getAncestorOfType(UICalendarWorkingContainer.class)
      .findFirstComponentOfType(UICalendars.class).getPublicCalendarIds() ;
    }catch(Exception e) {
      String[] groups = CalendarUtils.getUserGroups(CalendarUtils.getCurrentUser()) ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      Map<String, String> map = new HashMap<String, String> () ;    
      for(GroupCalendarData group : calendarService.getGroupCalendars(SessionsUtils.getSystemProvider(), groups)) {
        for(org.exoplatform.calendar.service.Calendar calendar : group.getCalendars()) {
          map.put(calendar.getId(), calendar.getId()) ;          
        }
      }
      return map.values().toArray(new String[]{}) ;
    }    
  }
  protected List<GroupCalendarData> getPublicCalendars(String username) throws Exception{
    String[] groups = CalendarUtils.getUserGroups(username) ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<GroupCalendarData> groupCalendars = calendarService.getGroupCalendars(SessionsUtils.getSystemProvider(), groups) ;
    return groupCalendars ;
  }

  public LinkedHashMap<String, String> getColors() {
    try{
      return getAncestorOfType(UICalendarPortlet.class)
      .findFirstComponentOfType(UICalendars.class).getColorMap() ;
    }catch(Exception e) {
      e.printStackTrace() ;
      return new LinkedHashMap<String, String>() ;
    }    
  }
  public void refresh() throws Exception {} ;
  public void initCategories() throws Exception {
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<EventCategory> eventCategories = calendarService.getEventCategories(SessionsUtils.getSessionProvider(), Util.getPortalRequestContext().getRemoteUser()) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("all", "")) ;
    for(EventCategory category : eventCategories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getName())) ;
    }
    addUIFormInput(new UIFormSelectBox(EVENT_CATEGORIES, EVENT_CATEGORIES, options)) ;
  }

  protected String[] getMonthsName() { 
    return MONTHS ;
  }
  protected String[] getDaysName() { 
    return DAYS ;
  }

  protected Calendar getDateByValue(int year, int month, int day, int type, int value) {
    Calendar cl = new GregorianCalendar(year, month, day) ;
    switch (type){
    case TYPE_DATE : cl.add(Calendar.DATE, value) ;break;
    case TYPE_WEEK : cl.add(Calendar.WEEK_OF_YEAR, value) ;break;
    case TYPE_MONTH : cl.add(Calendar.MONTH, value) ;break;
    case TYPE_YEAR : cl.add(Calendar.YEAR, value) ;break;
    default: System.out.println("Invalid type.");break;
    }
    return cl ;
  }

  protected int getDaysInMonth() {
    //Calendar cal = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay()) ;
    return calendar_.getActualMaximum(Calendar.DAY_OF_MONTH) ;
  }
  protected int getDaysInMonth(int month, int year) {
    Calendar cal = new GregorianCalendar(year, month, 1) ;
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH) ;
  }
  protected int getDayOfWeek(int year, int month, int day) {
    GregorianCalendar gc = new GregorianCalendar(year, month, day) ;
    return gc.get(java.util.Calendar.DAY_OF_WEEK) ;
  }
  protected  String getMonthName(int month) {return monthsMap_.get(month).toString() ;} ;
  protected  String getDayName(int day) {return daysMap_.get(day).toString() ;} ;

  protected String keyGen(int day, int month, int year) {
    return String.valueOf(day) + CalendarUtils.UNDERSCORE +  String.valueOf(month) +  CalendarUtils.UNDERSCORE + String.valueOf(year); 
  }
  public void update() throws Exception {
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<EventCategory> eventCategories = calendarService.getEventCategories(SessionsUtils.getSessionProvider(), Util.getPortalRequestContext().getRemoteUser()) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("all", "")) ;
    for(EventCategory category : eventCategories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getName())) ;
    }
    getUIFormSelectBox(EVENT_CATEGORIES).setOptions(options) ;
  }

  public List<CalendarEvent> getList() throws Exception {
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    if(privateCalendarIds.size() > 0) {
      events = calendarService.getUserEventByCalendar(SessionsUtils.getSessionProvider(), Util.getPortalRequestContext().getRemoteUser(), privateCalendarIds)  ;
    }
    if(publicCalendarIds.size() > 0) {
      if(events.size() > 0) {
        List<CalendarEvent> publicEvents = 
          calendarService.getGroupEventByCalendar(SessionsUtils.getSystemProvider(), publicCalendarIds) ;
        for(CalendarEvent event : publicEvents) {
          events.add(event) ;
        }
      }else {
        events = calendarService.getGroupEventByCalendar(SessionsUtils.getSystemProvider(), publicCalendarIds)  ;
      }
    }
    return events ;
  }

  protected void gotoDate(int day, int month, int year) {
    setCurrentDay(day) ;
    setCurrentMonth(month) ;
    setCurrentYear(year) ;
  }
  protected boolean isCurrentDay(int day, int month, int year) {
    Calendar currentCal = CalendarUtils.getInstanceTempCalendar() ;
    boolean isCurrentDay = currentCal.get(Calendar.DATE) == day ;
    boolean isCurrentMonth = currentCal.get(Calendar.MONTH) == month ;
    boolean isCurrentYear = currentCal.get(Calendar.YEAR) == year ;
    return (isCurrentDay && isCurrentMonth && isCurrentYear ) ;
  }
  protected boolean isCurrentWeek(int week, int month, int year) {
    Calendar currentCal = CalendarUtils.getInstanceTempCalendar() ;
    boolean isCurrentWeek = currentCal.get(Calendar.WEEK_OF_YEAR) == week ;
    boolean isCurrentMonth = currentCal.get(Calendar.MONTH) == month ;
    boolean isCurrentYear = currentCal.get(Calendar.YEAR) == year ;
    return (isCurrentWeek && isCurrentMonth && isCurrentYear ) ;
  }
  protected boolean isCurrentMonth(int month, int year) {
    Calendar currentCal = CalendarUtils.getInstanceTempCalendar() ;
    boolean isCurrentMonth = currentCal.get(Calendar.MONTH) == month ;
    boolean isCurrentYear = currentCal.get(Calendar.YEAR) == year ;
    return (isCurrentMonth && isCurrentYear ) ;
  }

  protected boolean isSameDate(java.util.Calendar date1, java.util.Calendar date2) {
    return CalendarUtils.isSameDate(date1, date2) ;
  }
  protected boolean isSameDate(Date value1, Date value2) {
    return CalendarUtils.isSameDate(value1, value2) ;
  }
  public void setCurrentCalendar(Calendar value) {calendar_ = value ;}
  protected Calendar getCurrentCalendar() {return calendar_ ;}
  protected Date getCurrentDate() {return calendar_.getTime() ;} 
  protected void setCurrentDate(Date value) {calendar_.setTime(value) ;} 

  protected int getCurrentDay() {return calendar_.get(Calendar.DATE) ;}
  protected void setCurrentDay(int day) {calendar_.set(Calendar.DATE, day) ;}

  protected int getCurrentWeek() {return calendar_.get(Calendar.WEEK_OF_YEAR) ;}
  protected void setCurrentWeek(int week) {calendar_.set(Calendar.WEEK_OF_YEAR, week) ;}

  protected int getCurrentMonth() {return calendar_.get(Calendar.MONTH) ;}
  protected void setCurrentMonth(int month) {calendar_.set(Calendar.MONTH, month) ;}

  protected int getCurrentYear() {return calendar_.get(Calendar.YEAR) ;}
  protected void setCurrentYear(int year) {calendar_.set(Calendar.YEAR, year) ;}

  protected void removeEvents(List<CalendarEvent> events) throws Exception {
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    for (CalendarEvent ce : events) {
      if(CalendarUtils.PUBLIC_TYPE.equals(ce.getCalType())){
        calService.removeGroupEvent(SessionsUtils.getSystemProvider(), ce.getCalendarId(), ce.getId()) ;
      } else {
        calService.removeUserEvent(SessionsUtils.getSessionProvider(), username, ce.getCalendarId(), ce.getId()) ;
      }
    }
  }
  protected Calendar getBeginDay(Calendar cal) {
    return CalendarUtils.getBeginDay(cal) ;
  }
  protected Calendar getEndDay(Calendar cal)  {
    return CalendarUtils.getEndDay(cal) ;
  }
  protected String[] getContextMenu() {
    return  CONTEXT_MENU ;
  }
  protected String[] getQuickEditMenu() {
    return  QUICKEDIT_MENU ;
  }
  protected List<String> getDisplayTimes(String timeFormat, int timeInterval) {
    List<String> times = new ArrayList<String>() ;
    Calendar cal = getBeginDay(GregorianCalendar.getInstance()) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    for(int i = 0; i < 24*(60/timeInterval); i++) {
      times.add(df.format(cal.getTime())) ;
      cal.add(java.util.Calendar.MINUTE, timeInterval) ;
    }
    return times ;
  }
  protected Map<String, String> getTimeSteps(String timeFormat, int timeInterval) {
    Map<String, String> times = new LinkedHashMap<String, String>() ;
    Calendar cal = getBeginDay(GregorianCalendar.getInstance())  ;
    cal.setTime(getCurrentDate()) ;
    cal.set(Calendar.HOUR_OF_DAY, 0) ;
    cal.set(Calendar.MINUTE, 0) ;
    cal.set(Calendar.MILLISECOND, 0) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    for(int i = 0; i < 24*(60/timeInterval); i++) {
      times.put(String.valueOf(cal.getTimeInMillis()), df.format(cal.getTime())) ;
      cal.add(java.util.Calendar.MINUTE, timeInterval) ;
    }
    return times ;
  }
  protected String getDateFormat() {
    return calendarSetting_.getDateFormat();
  }

  protected String getDateTimeFormat() {
    return dateTimeFormat_;
  }

  protected int getTimeInterval() {
    return timeInterval_;
  }
  protected int getDefaultTimeInterval() {
    return CalendarUtils.DEFAULT_TIMEITERVAL ;
  }
  protected String getTimeFormat() {
    return calendarSetting_.getTimeFormat();
  }
  public void setCalendarSetting(CalendarSetting calendarSetting_) {
    this.calendarSetting_ = calendarSetting_;
  }
  public CalendarSetting getCalendarSetting() {
    return calendarSetting_;
  }
  public boolean isShowWorkingTime() {
    return calendarSetting_.isShowWorkingTime() ;
  }
  public String getStartTime() {
    if(calendarSetting_.isShowWorkingTime()) {
      return calendarSetting_.getWorkingTimeBegin() ;
    }
    return "" ;
  }
  public String getEndTime() {
    if(calendarSetting_.isShowWorkingTime()) {
      return calendarSetting_.getWorkingTimeEnd() ;
    }
    return "" ;
  }
  public String getPriority(String key) {
    return priorityMap_.get(key) ;
  }
  static  public class AddEventActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      UICalendarView uiForm = event.getSource() ;
      System.out.println(" ===========> AddEventActionListener") ;
      CalendarService calendarService =  CalendarUtils.getCalendarService() ; 
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      List<org.exoplatform.calendar.service.Calendar> privateCalendars = 
        calendarService.getUserCalendars(SessionsUtils.getSessionProvider(), username) ;
      if(privateCalendars.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.calendar-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } else {
        String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
        String value = uiForm.getUIFormSelectBox(EVENT_CATEGORIES).getValue() ;
        UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        UIPopupAction uiParenPopup = uiPortlet.getChild(UIPopupAction.class) ;
        UIPopupContainer uiPopupContainer = uiParenPopup.activate(UIPopupContainer.class, 700) ;
        if(CalendarEvent.TYPE_TASK.equals(type)) {
          uiPopupContainer.setId(UIPopupContainer.UITASKPOPUP) ;
          UITaskForm uiTaskForm = uiPopupContainer.addChild(UITaskForm.class, null, null) ;
          uiTaskForm.initForm(uiPortlet.getCalendarSetting(), null) ;
          uiTaskForm.update(CalendarUtils.PRIVATE_TYPE, null) ;
          uiTaskForm.setSelectedCategory(value) ;
        } else {
          uiPopupContainer.setId(UIPopupContainer.UIEVENTPOPUP) ;
          UIEventForm uiEventForm =  uiPopupContainer.addChild(UIEventForm.class, null, null) ;
          uiEventForm.initForm(uiPortlet.getCalendarSetting(), null) ;
          uiEventForm.update(CalendarUtils.PRIVATE_TYPE, null) ;
          uiEventForm.setSelectedCategory(value) ;
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiParenPopup) ;     
      }
    }
  }
  static  public class DeleteEventActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      UICalendarView uiCalendarView = event.getSource() ;
      System.out.println(" ===========> DeleteEventActionListener") ;
      UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
      if(uiCalendarView instanceof UIMonthView ) {
        List<CalendarEvent> list = ((UIMonthView)uiCalendarView).getSelectedEvents() ;
        if(list.isEmpty()) {
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.check-box-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        } else {
          try {
            uiCalendarView.removeEvents(((UIMonthView)uiCalendarView).getSelectedEvents()) ;
            ((UIMonthView)uiCalendarView).refreshEvents() ;
            uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.delete-event-successfully", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          } catch (Exception e) {
            e.printStackTrace() ;
            uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.delete-event-error", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          }
        }
      } else if(uiCalendarView instanceof UIListView) {
        List<CalendarEvent> list = ((UIListView)uiCalendarView).getSelectedEvents() ;
        if(list.isEmpty()) {
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.check-box-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        } else {
          try {
            uiCalendarView.removeEvents(((UIListView)uiCalendarView).getSelectedEvents()) ;
            ((UIListView)uiCalendarView).refresh() ;
            UIListContainer  uiListContainer = uiCalendarView.getParent() ;
            uiListContainer.refresh() ;
            uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.delete-event-successfully", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          } catch (Exception e) {
            e.printStackTrace() ;
            uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.delete-event-error", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          }
        }
      } else {
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.function-not-supported", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
      UIMiniCalendar uiMiniCalendar = uiCalendarView.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
      uiMiniCalendar.updateMiniCal() ;
      uiCalendarView.setLastUpdatedEventId(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView.getParent()) ;
    }
  }
  static  public class ChangeCategoryActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      //UICalendarView uiForm = event.getSource() ;
      System.out.println(" ===========> ChangeCategoryActionListener") ;
    }
  }
  static  public class EventSelectActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
    }
  }
  static  public class AddCategoryActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      UICalendarView listView = event.getSource() ;
      UICalendarPortlet calendarPortlet = listView.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UIEventCategoryManager.class, 470) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }


  static  public class ViewActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      System.out.println("ViewActionListener");
      UICalendarView uiCalendarView = event.getSource() ;
      UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class, 700) ;
      uiPopupContainer.setId("UIEventPreview");
      CalendarEvent eventCalendar = null ;
      String username = event.getRequestContext().getRemoteUser() ;
      String calendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      CalendarService calService = uiCalendarView.getApplicationComponent(CalendarService.class) ;
      if(uiCalendarView.getDataMap() != null) {
        eventCalendar = uiCalendarView.getDataMap().get(eventId) ;
      }
      /* if(CalendarUtils.PUBLIC_TYPE.equals(calType)) {
        eventCalendar = calService.getGroupEvent(calendarId, eventId) ;
      } else if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
        eventCalendar = calService.getUserEvent(username, calendarId, eventId) ;
      } else if(CalendarUtils.SHARED_TYPE.equals(calType)) {
        eventCalendar = calService.get
        System.out.println("\n\n shared event not edit");
      }*/
      if(eventCalendar != null) {
        UIPreview uiPreview = uiPopupContainer.addChild(UIPreview.class, null, null) ;
        uiPreview.setEvent(eventCalendar) ;
        uiPreview.setShowPopup(true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView.getParent()) ;
      }
    }
  }

  static  public class EditActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      System.out.println("EditEventActionListener");
      UICalendarView uiCalendarView = event.getSource() ;
      UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class, 700) ;
      CalendarEvent eventCalendar = null ;
      String username = event.getRequestContext().getRemoteUser() ;
      String calendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      if(uiCalendarView.getDataMap()  != null) {
        eventCalendar = uiCalendarView.getDataMap().get(eventId) ;
      }
      if(eventCalendar != null) {
        if(CalendarEvent.TYPE_EVENT.equals(eventCalendar.getEventType())) {
          uiPopupContainer.setId(UIPopupContainer.UIEVENTPOPUP) ;
          UIEventForm uiEventForm = uiPopupContainer.createUIComponent(UIEventForm.class, null, null) ;
          List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
          if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
            options = null ;
          } else if(CalendarUtils.SHARED_TYPE.equals(calType)) {
            GroupCalendarData calendarData = calendarService.getSharedCalendars(SessionsUtils.getSystemProvider(), CalendarUtils.getCurrentUser())  ;
            for(org.exoplatform.calendar.service.Calendar cal : calendarData.getCalendars()) {
              options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
            }
          } else if(CalendarUtils.PUBLIC_TYPE.equals(calType)) {
            for (GroupCalendarData calendarData : uiCalendarView.getPublicCalendars(username)) {
              for(org.exoplatform.calendar.service.Calendar cal : calendarData.getCalendars()) {
                options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
              }
            }
          }    
          uiEventForm.update(calType, options) ;
          uiEventForm.initForm(uiPortlet.getCalendarSetting(), eventCalendar) ;
          uiEventForm.setSelectedCalendarId(calendarId) ;
          uiPopupContainer.addChild(uiEventForm) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView.getParent()) ;

        } else if(CalendarEvent.TYPE_TASK.equals(eventCalendar.getEventType())) {
          uiPopupContainer.setId(UIPopupContainer.UITASKPOPUP) ;
          UITaskForm uiTaskForm = uiPopupContainer.createUIComponent(UITaskForm.class, null, null) ;
          List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
          if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
            options = null ;
          } else if(CalendarUtils.SHARED_TYPE.equals(calType)) {
            GroupCalendarData calendarData = calendarService.getSharedCalendars(SessionsUtils.getSystemProvider(), CalendarUtils.getCurrentUser())  ;
            for(org.exoplatform.calendar.service.Calendar cal : calendarData.getCalendars()) {
              options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
            }
          } else if(CalendarUtils.PUBLIC_TYPE.equals(calType)) {
            for (GroupCalendarData calendarData : uiCalendarView.getPublicCalendars(username)) {
              for(org.exoplatform.calendar.service.Calendar cal : calendarData.getCalendars()) {
                options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
              }
            }
          }    
          uiTaskForm.update(calType, options) ;
          uiTaskForm.initForm(uiPortlet.getCalendarSetting(),eventCalendar) ;
          uiTaskForm.setSelectedCalendarId(calendarId) ;
          uiPopupContainer.addChild(uiTaskForm) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView.getParent()) ;
        } else {
          System.out.println("\n\n event type is not supported ! ");
        }
      } else {
        System.out.println("\n\n event not found !");
      }
    }
  }
  static  public class DeleteActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      UICalendarView calendarview = event.getSource() ;
      System.out.println("\n\n QuickDeleteEventActionListener");
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      UICalendarViewContainer uiContainer = calendarview.getAncestorOfType(UICalendarViewContainer.class) ;
      UICalendarPortlet uiPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      try {
        CalendarService calService = calendarview.getApplicationComponent(CalendarService.class) ;
        String username = event.getRequestContext().getRemoteUser() ;
        if(CalendarUtils.PUBLIC_TYPE.equals(calType)){
          calService.removeGroupEvent(SessionsUtils.getSystemProvider(), calendarId, eventId) ;
        } else if(CalendarUtils.PRIVATE_TYPE.equals(calType)){
          calService.removeUserEvent(SessionsUtils.getSessionProvider(), username, calendarId, eventId) ;
        } else if(CalendarUtils.SHARED_TYPE.equals(calType)) {
          // need to implement
        }
        uiMiniCalendar.updateMiniCal() ;
        calendarview.setLastUpdatedEventId(null) ;
        uiContainer.refresh() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
    }
  }
  static public class TaskViewActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      UICalendarView uiCalendarView = event.getSource() ;     
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      UIListView uiListView = uiViewContainer.findFirstComponentOfType(UIListView.class) ;
      CalendarService calendarService =  CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser() ;
      EventQuery eventQuery = new EventQuery() ;
      java.util.Calendar fromcalendar  = uiListView.getBeginDay(new GregorianCalendar(uiListView.getCurrentYear(), uiListView.getCurrentMonth(), uiListView.getCurrentDay())) ;
      eventQuery.setFromDate(fromcalendar) ;
      java.util.Calendar tocalendar = uiListView.getEndDay(new GregorianCalendar(uiListView.getCurrentYear(), uiListView.getCurrentMonth(), uiListView.getCurrentDay())) ;
      eventQuery.setToDate(tocalendar) ;
      uiListView.update(calendarService.searchEvent(SessionsUtils.getSystemProvider(), username, eventQuery, uiCalendarView.getPublicCalendars())) ; 
      uiListView.setShowEventAndTask(false) ;
      uiListView.setDisplaySearchResult(false) ;
      uiListView.isShowEvent_ = false ;
      uiViewContainer.setRenderedChild(viewType);      
      UIActionBar uiActionbar = uiPortlet.findFirstComponentOfType(UIActionBar.class) ;
      uiActionbar.setCurrentView(viewType) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }  

  static  public class GotoDateActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      try {
        UICalendarView calendarview = event.getSource() ;
        String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
        String year = event.getRequestContext().getRequestParameter(YEAR) ;
        String month = event.getRequestContext().getRequestParameter(MONTH) ;
        String day = event.getRequestContext().getRequestParameter(DAY) ;
        UICalendarPortlet portlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
        UICalendarViewContainer uiContainer = portlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
        cal.set(Calendar.DATE, Integer.parseInt(day)) ;
        cal.set(Calendar.MONTH, Integer.parseInt(month)) ;
        cal.set(Calendar.YEAR, Integer.parseInt(year)) ;
        int type = Integer.parseInt(viewType) ; 
        switch (type){
        case TYPE_DATE : {
          if(uiContainer.getRenderedChild() instanceof UIDayView) {
            UIDayView uiView = uiContainer.getChild(UIDayView.class) ;
            uiView.setCurrentCalendar(cal) ;
            uiView.refresh() ;
          } else if(uiContainer.getRenderedChild() instanceof UIListContainer) {
            UIListContainer uiView = uiContainer.getChild(UIListContainer.class) ;
            UIListView uiListView = uiView.getChild(UIListView.class) ;
            if(!uiListView.isDisplaySearchResult()){
              uiView.setCurrentCalendar(cal) ;
              uiView.refresh() ;
            }
          } else {
            uiContainer.setRenderedChild(UIDayView.class) ;
            UIDayView uiView = uiContainer.getChild(UIDayView.class) ;
            uiView.setCurrentCalendar(cal) ;
            uiView.refresh() ;
          }
          if(calendarview instanceof UIMiniCalendar) {
            ((UIMiniCalendar)calendarview).setCurrentCalendar(cal) ;
            ((UIMiniCalendar)calendarview).updateMiniCal() ;
          }
        }break;
        case TYPE_WEEK : {
          UIWeekView uiView = uiContainer.getChild(UIWeekView.class) ;
          uiView.setCurrentCalendar(cal) ;
          uiView.refresh() ;
          uiContainer.setRenderedChild(UIWeekView.class) ;
        }break;
        case TYPE_MONTH : {
          UIMonthView uiView = uiContainer.getChild(UIMonthView.class) ;
          uiView.setCurrentCalendar(cal) ;
          uiView.refresh() ;
          uiContainer.setRenderedChild(UIMonthView.class) ;
        }break;
        case TYPE_YEAR :{
          UIYearView uiView = uiContainer.getChild(UIYearView.class) ;
          uiView.setCurrentCalendar(cal) ;
          uiView.refresh() ;
          uiContainer.setRenderedChild(UIYearView.class) ;
        }break;
        default: System.out.println("Invalid type.");break;
        }
        UIActionBar uiActionBar = portlet.findFirstComponentOfType(UIActionBar.class) ;
        uiActionBar.setCurrentView(uiContainer.getRenderedChild().getId()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
    }
  }
  static public class SwitchViewActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      UICalendarView uiView = event.getSource();
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID);
      uiView.setViewType(viewType) ;
      uiView.refresh() ;
      UIListContainer uiListContainer = uiView.getAncestorOfType(UIListContainer.class) ;
      if(uiListContainer != null) { 
        uiListContainer.getChild(UIPreview.class).setEvent(null) ; 
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiView.getParent());
    }
  }
  static  public class QuickAddActionListener extends EventListener<UICalendarView> {
    public void execute(Event<UICalendarView> event) throws Exception {
      System.out.println("QuickAddActionListener");
      UICalendarView calendarview = event.getSource() ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
      List<org.exoplatform.calendar.service.Calendar> privateCalendars = 
        calendarService.getUserCalendars(SessionsUtils.getSessionProvider(), CalendarUtils.getCurrentUser()) ;
      if(privateCalendars.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.calendar-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String startTime = event.getRequestContext().getRequestParameter("startTime") ;
      String finishTime = event.getRequestContext().getRequestParameter("finishTime") ;
      String selectedCategory = calendarview.getUIFormSelectBox(EVENT_CATEGORIES).getValue() ;
      UICalendarPortlet uiPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIQuickAddEvent uiQuickAddEvent = uiPopupAction.activate(UIQuickAddEvent.class, 600) ;
      uiQuickAddEvent.setSelectedCategory(selectedCategory) ;
      if(CalendarEvent.TYPE_TASK.equals(type)) {
        uiQuickAddEvent.setEvent(false) ;
        uiQuickAddEvent.setId("UIQuickAddTask") ;
      } else {
        uiQuickAddEvent.setEvent(true) ;
        uiQuickAddEvent.setId("UIQuickAddEvent") ;
      }
      try {
        Long.parseLong(startTime) ;
      }catch (Exception e) {
        startTime = null ;
      }
      try {
        Long.parseLong(finishTime) ;
      }catch (Exception e) {
        finishTime = null ;
      }
      uiQuickAddEvent.init(uiPortlet.getCalendarSetting(), startTime, finishTime) ;
      uiQuickAddEvent.update(CalendarUtils.PRIVATE_TYPE, null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}
