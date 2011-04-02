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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageListQuery;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.webui.popup.UIConfirmForm;
import org.exoplatform.calendar.webui.popup.UIEventCategoryManager;
import org.exoplatform.calendar.webui.popup.UIEventForm;
import org.exoplatform.calendar.webui.popup.UIEventShareTab;
import org.exoplatform.calendar.webui.popup.UIExportForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIPopupContainer;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.calendar.webui.popup.UITaskForm;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
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
  private Log log = ExoLogger.getLogger(this.getClass());
  final public static int TYPE_NONE = -1 ;
  final public static int TYPE_DAY = 0 ;
  final public static int TYPE_WEEK = 1 ;
  final public static int TYPE_MONTH = 2 ;
  final public static int TYPE_YEAR = 3 ;
  final public static String ACT_NEXT = "MoveNext".intern() ;
  final public static String ACT_PREVIOUS  = "MovePrevious".intern() ;
  public final static String ACT_ADDNEW_EVENT = "QuickAddNewEvent".intern() ;
  public final static String ACT_ADDNEW_TASK = "QuickAddNewTask".intern() ;
  public final static String[] CONTEXT_MENU = {ACT_ADDNEW_EVENT, ACT_ADDNEW_TASK}  ;
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
  final public static String ISOCCUR = "isOccur".intern();
  final public static String RECURID = "recurId".intern();

  final public static String TYPE_EVENT = CalendarEvent.TYPE_EVENT ;
  final public static String TYPE_TASK = CalendarEvent.TYPE_TASK ;
  final public static String TYPE_BOTH = "Both".intern() ;

  private String viewType_ = TYPE_BOTH ;
  private String [] views = {TYPE_BOTH, TYPE_EVENT, TYPE_TASK} ;

  protected Calendar calendar_ = null ;
  protected List<String> displayTimes_ = null ;
  protected Map<String, String> timeSteps_ = null ;
  public boolean isShowEvent_ = true;
  private String editedEventId_ = null ;
  private int timeInterval_ = 30 ;
  protected CalendarSetting calendarSetting_ ;

  private String dateTimeFormat_  ;
  private DateFormat dfFormat = null;
  protected List<String> privateCalendarIds = new ArrayList<String>() ;
  protected List<String> publicCalendarIds = new ArrayList<String>() ;
  protected Calendar instanceTempCalendar_ = null ;
  final public static Map<Integer, String> monthsName_ = new HashMap<Integer, String>() ;
  private Map<Integer, String> daysMap_ = new LinkedHashMap<Integer, String>() ;
  private Map<Integer, String> monthsMap_ = new LinkedHashMap<Integer, String>() ;
  private Map<String, String> priorityMap_ = new HashMap<String, String>() ;
  abstract LinkedHashMap<String, CalendarEvent> getDataMap() ;
  protected DateFormatSymbols dfs_  ;
  protected Map<String,Map<String,CalendarEvent>> recurrenceEventsMap = new LinkedHashMap<String, Map<String,CalendarEvent>>();

  private CalendarEvent currentOccurrence; 
  
  private boolean allDelete_ = true ;

  public UICalendarView() throws Exception{
    try {
    initCategories() ;
    applySeting() ;
    calendar_ = CalendarUtils.getInstanceOfCurrentCalendar();
    } catch (Exception e) { 
      log.warn(e);
    }
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    dfs_ = new DateFormatSymbols(locale) ;
    for(int i = 0; i< dfs_.getMonths().length; i++) {
      monthsMap_.put(i, dfs_.getMonths()[i]) ;
    }
    for(int i = 1; i < dfs_.getWeekdays().length ; i ++) {
      daysMap_.put(i, dfs_.getWeekdays()[i]) ;
    }
    for(int i = 0 ; i < CalendarEvent.PRIORITY.length ; i++ ) {
      priorityMap_.put(String.valueOf(i), CalendarEvent.PRIORITY[i]) ;
    }
  }
  
  protected String renderDayViewInTitleBar(String monthOpenTag, String monthCloseTag, 
                                        String yearOpenTag, String yearCloseTag) {
    String formatPattern = "";
    String dateFormat = this.getDateFormat();
    if (dateFormat.equalsIgnoreCase(CalendarUtils.FORMATPATTERN1)) { //dd/MM/yyyy
      formatPattern = "%1$td / %2$s%1$tm%3$s / %4$s%1$tY%5$s";// day/<string>month<string>/<string>year<string>
    } else if (dateFormat.equalsIgnoreCase(CalendarUtils.FORMATPATTERN2)) {// dd-MM-yyyy
      formatPattern = "%1$td - %2$s%1$tm%3$s - %4$s%1$tY%5$s";// day-<string>month<string>-<string>year<string>
    } else if (dateFormat.equalsIgnoreCase(CalendarUtils.FORMATPATTERN3)) { //MM/dd/yyyy
      formatPattern = "%2$s%1$tm%3$s / %1$td / %4$s%1$tY%5$s";// <string>month<string>/day/<string>year<string>
    } else if (dateFormat.equalsIgnoreCase(CalendarUtils.FORMATPATTERN4)) { //MM-dd-yyyy
      formatPattern = "%2$s%1$tm%3$s - %1$td - %4$s%1$tY%5$s";// <string>month<string>/day/<string>year<string>      
    }
    return String.format(formatPattern, getCurrentCalendar(), monthOpenTag, monthCloseTag, 
                         yearOpenTag, yearCloseTag);
  }
  /**
   * 
   * @return an instance of GregorianCalendar with time zone as of calendar setting.
   */
  protected Calendar getInstanceTempCalendar() { 
    return  CalendarUtils.getInstanceOfCurrentCalendar();
  } 
  public void applySeting() throws Exception {
    displayTimes_ = null ;
    timeSteps_ = null ;
    instanceTempCalendar_ = null;
    try {
      calendarSetting_ = getAncestorOfType(UICalendarPortlet.class).getCalendarSetting() ;
    } catch (Exception e) {
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser() ;
      calendarSetting_ = calService.getCalendarSetting(username) ;
    } 
    dateTimeFormat_ = getDateFormat() + " " + getTimeFormat() ;
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
      for(GroupCalendarData group : calendarService.getGroupCalendars(groups, false, CalendarUtils.getCurrentUser())) {
        for(org.exoplatform.calendar.service.Calendar calendar : group.getCalendars()) {
          map.put(calendar.getId(), calendar.getId()) ;          
        }
      }
      return map.values().toArray(new String[]{}) ;
    }    
  }
  public  List<String> getPrivateCalendars() throws Exception{
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<String> list = new ArrayList<String>() ;
    for(org.exoplatform.calendar.service.Calendar c : calendarService.getUserCalendars(CalendarUtils.getCurrentUser() , true)) {
      if (c.getId().equals(Utils.getDefaultCalendarId(CalendarUtils.getCurrentUser())) && c.getName().equals(NewUserListener.DEFAULT_CALENDAR_NAME)) {
        String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.DEFAULT_CALENDAR_ID);
        c.setName(newName);
      }
      list.add(c.getId()) ;
    }
    return list ;
  }

  public List<String> getSharedCalendars() throws Exception{
    List<String> list = new ArrayList<String>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ; 
    GroupCalendarData gcd =  calendarService.getSharedCalendars(CalendarUtils.getCurrentUser() , true) ;
    if(gcd != null)
      for(org.exoplatform.calendar.service.Calendar cal : gcd.getCalendars()) {
        list.add(cal.getId()) ;
      }
    return list ;
  }
  public String[] getFilterCalendarIds() throws Exception {
    List<String> filterList = new ArrayList<String>() ;
    filterList.addAll(Arrays.asList(getCalendarSetting().getFilterPrivateCalendars())) ;
    filterList.addAll(Arrays.asList(getCalendarSetting().getFilterPublicCalendars())) ;
    filterList.addAll(Arrays.asList(getCalendarSetting().getFilterSharedCalendars())) ;
    List<String> ids = new ArrayList<String>() ;
    ids.addAll(getPrivateCalendars()) ;
    ids.addAll(getSharedCalendars()) ;
    ids.addAll(Arrays.asList(getPublicCalendars())) ;
    List<String> results = new ArrayList<String>() ;
    for(String id : ids) {
      if(!filterList.contains(id))  results.add(id) ;
    }
    return filterList.toArray(new String[]{}) ;
  }
  protected List<GroupCalendarData> getPublicCalendars(String username) throws Exception{
    String[] groups = CalendarUtils.getUserGroups(username) ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<GroupCalendarData> groupCalendars = calendarService.getGroupCalendars(groups, false, username) ;
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

  
  protected String renderDateTimeString(Date date) {
    if (dfFormat == null) {
      dfFormat = new SimpleDateFormat(dateTimeFormat_);
      Calendar cal = (Calendar) calendar_.clone();
      dfFormat.setCalendar(cal);
    }
    return dfFormat.format(date);
  }
  
  private DateFormat dayFormat = null;
  protected String renderDayString(Date date) {
    if (dayFormat == null) {
      
      dayFormat = new SimpleDateFormat(calendarSetting_.getDateFormat());
      dayFormat.setCalendar(calendar_);
    }
    
    return dayFormat.format(date);
  }
  
  private DateFormat timeFormat = null;
  protected String renderTimeString(Date date) {
    if (timeFormat == null) {
      timeFormat = new SimpleDateFormat(calendarSetting_.getTimeFormat());
      timeFormat.setCalendar(calendar_);
    }
    
    return timeFormat.format(date);
  }
  
  
  public void initCategories() throws Exception {
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser();
    List<EventCategory> eventCategories = calendarService.getEventCategories(username) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(EventCategory category : eventCategories) {
      if (category.getId().contains("defaultEventCategoryId") && category.getName().contains("defaultEventCategoryName")) {
        String newName = CalendarUtils.getResourceBundle("UICalendarView.label." + category.getId());
        options.add(new SelectItemOption<String>(newName, category.getId())) ;
        category.setName(newName);
      } else {
        options.add(new SelectItemOption<String>(category.getName(), category.getId())) ;        
      }      
    }
    UIFormSelectBox categoryInput =   new UIFormSelectBox(EVENT_CATEGORIES, EVENT_CATEGORIES, options) ;
    addUIFormInput(categoryInput) ;
  }
  protected String getSelectedCategory() {
    return getUIFormSelectBox(EVENT_CATEGORIES).getValue() ;
  }
  public void setSelectedCategory(String id) {
    getUIFormSelectBox(EVENT_CATEGORIES).setValue(id) ;
  }

  protected String[] getMonthsName() { 
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    dfs_ = new DateFormatSymbols(locale) ;
    for(int i = 0; i< dfs_.getMonths().length; i++) {
      monthsMap_.put(i, dfs_.getMonths()[i]) ;
    }
    return monthsMap_.values().toArray(new String[]{})  ;
  }
  protected String[] getDaysName() { 
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    dfs_ = new DateFormatSymbols(locale) ;
    for(int i = 1; i < dfs_.getWeekdays().length ; i ++) {
      daysMap_.put(i, dfs_.getWeekdays()[i]) ;
    }
    return daysMap_.values().toArray(new String[]{})  ;
  }
  protected Calendar getDateByValue(int year, int month, int day, int type, int value) {
    Calendar cl = new GregorianCalendar(year, month, day) ;
    switch (type){
    case TYPE_DAY : cl.add(Calendar.DATE, value) ;break;
    case TYPE_WEEK : cl.add(Calendar.WEEK_OF_YEAR, value) ;break;
    case TYPE_MONTH : cl.add(Calendar.MONTH, value) ;break;
    case TYPE_YEAR : cl.add(Calendar.YEAR, value) ;break;
    default:break;
    }
    return cl ;
  }

  protected int getDaysInMonth() {
    return calendar_.getActualMaximum(Calendar.DAY_OF_MONTH) ;
  }
  protected int getDaysInMonth(int month, int year) {
    Calendar cal = new GregorianCalendar(year, month, 1) ;
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH) ;
  }
  protected int getDayOfWeek(int year, int month, int day) {
    GregorianCalendar gc = new GregorianCalendar(year, month, day) ;
    gc.setFirstDayOfWeek(Integer.parseInt(calendarSetting_.getWeekStartOn()));
    return gc.get(java.util.Calendar.DAY_OF_WEEK) ;
  }
  protected  String getMonthName(int month) {
    getMonthsName() ;
    return monthsMap_.get(month).toString() ;

  } ;
  protected  String getDayName(int day) {
    getDaysName() ;
    return daysMap_.get(day).toString() ;} ;

    protected String keyGen(int day, int month, int year) {
      return String.valueOf(day) + CalendarUtils.UNDERSCORE +  String.valueOf(month) +  CalendarUtils.UNDERSCORE + String.valueOf(year); 
    }
    public void update() throws Exception {
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser();
      List<EventCategory> eventCategories = calendarService.getEventCategories(username) ;
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      for(EventCategory category : eventCategories) {
        if (category.getId().contains("defaultEventCategoryId") && category.getName().contains("defaultEventCategoryName")) {
          String newName = CalendarUtils.getResourceBundle("UICalendarView.label." + category.getId());
          options.add(new SelectItemOption<String>(newName, category.getId())) ;
        } else {
          options.add(new SelectItemOption<String>(category.getName(), category.getId())) ;        
        }
      }
      getUIFormSelectBox(EVENT_CATEGORIES).setOptions(options) ;
      getUIFormSelectBox(EVENT_CATEGORIES).setValue(null) ;
    }

    public List<CalendarEvent> getList() throws Exception {
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
      if(privateCalendarIds.size() > 0) {
        events = calendarService.getUserEventByCalendar(CalendarUtils.getCurrentUser(), privateCalendarIds)  ;
      }
      if(publicCalendarIds.size() > 0) {
        if(events.size() > 0) {
          List<CalendarEvent> publicEvents = 
            calendarService.getGroupEventByCalendar(publicCalendarIds) ;
          for(CalendarEvent event : publicEvents) {
            events.add(event) ;
          }
        }else {
          events = calendarService.getGroupEventByCalendar(publicCalendarIds)  ;
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
      Calendar currentCal = CalendarUtils.getInstanceOfCurrentCalendar() ;
      boolean isCurrentDay = (currentCal.get(Calendar.DATE) == day) ;
      boolean isCurrentMonth = (currentCal.get(Calendar.MONTH) == month) ;
      boolean isCurrentYear = (currentCal.get(Calendar.YEAR) == year) ;
      return (isCurrentDay && isCurrentMonth && isCurrentYear ) ;
    }
    protected boolean isCurrentWeek(int week, int month, int year) {
      Calendar currentCal = CalendarUtils.getInstanceOfCurrentCalendar() ;
      boolean isCurrentWeek = currentCal.get(Calendar.WEEK_OF_YEAR) == week ;
      boolean isCurrentMonth = currentCal.get(Calendar.MONTH) == month ;
      boolean isCurrentYear = currentCal.get(Calendar.YEAR) == year ;
      return (isCurrentWeek && isCurrentMonth && isCurrentYear ) ;
    }
    protected boolean isCurrentMonth(int month, int year) {
      Calendar currentCal = CalendarUtils.getInstanceOfCurrentCalendar() ;
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
    public Calendar getCurrentCalendar() {return calendar_ ;}
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

    protected void moveCalendarTo(int field, int amount) throws Exception {
      calendar_.add(field, amount) ;
    }
    protected void removeEvents(List<CalendarEvent> events) throws Exception {
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser() ;
      OrganizationService orService = CalendarUtils.getOrganizationService() ;
      for (CalendarEvent ce : events) {
        org.exoplatform.calendar.service.Calendar cal = null ;
        if(CalendarUtils.PUBLIC_TYPE.equals(ce.getCalType())){
          cal = calService.getGroupCalendar(ce.getCalendarId());
          // cs-4429: fix for group calendar permission
          if(CalendarUtils.canEdit(orService,cal.getEditPermission(), username)) {
            calService.removePublicEvent(ce.getCalendarId(), ce.getId()) ;
          } else {
            allDelete_ = false ;
          }
        } else if(CalendarUtils.PRIVATE_TYPE.equals(ce.getCalType())) {
          calService.removeUserEvent(username, ce.getCalendarId(), ce.getId()) ;
        } else if(CalendarUtils.SHARED_TYPE.equals(ce.getCalType())){
          cal = calService.getSharedCalendars(username, true).getCalendarById(ce.getCalendarId());
          if(CalendarUtils.canEdit(null, Utils.getEditPerUsers(cal), username)) {
            calService.removeSharedEvent(username, ce.getCalendarId(), ce.getId()) ;
          } else {
            allDelete_ = false ;
          }
        }
      }
    }
    
    protected void moveEvents(List<CalendarEvent> events, String toCalendarId, String toType)throws Exception{
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser() ;
      for (CalendarEvent ce : events) {
        List<CalendarEvent> list = new ArrayList<CalendarEvent>() ;
        list.add(ce) ;
        // for occurrence event
        if (!CalendarEvent.RP_NOREPEAT.equals(ce.getRepeatType()) && !CalendarUtils.isEmpty(ce.getRecurrenceId())) {
          calService.updateOccurrenceEvent(ce.getCalendarId(), ce.getCalendarId(), ce.getCalType(), ce.getCalType(), list, username);
        } else {
          calService.moveEvent(ce.getCalendarId(), ce.getCalendarId(), ce.getCalType(), ce.getCalType(), list, username) ;
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
      
        List<String> displayTimes_ =   new ArrayList<String>() ;
        Calendar cal = CalendarUtils.getInstanceOfCurrentCalendar() ;
        cal.set(Calendar.HOUR_OF_DAY, 0) ;
        cal.set(Calendar.MINUTE, 0) ;
        cal.set(Calendar.MILLISECOND, 0) ;
        DateFormat df = new SimpleDateFormat(timeFormat) ;
        df.setCalendar(cal) ;
        for(int i = 0; i < 24*(60/timeInterval); i++) {
          displayTimes_.add(df.format(cal.getTime())) ;
          cal.add(java.util.Calendar.MINUTE, timeInterval) ;
        }
      
      return displayTimes_ ;
    }
    protected List<String> getDisplayTimes(String timeFormat, int timeInterval, Locale locale) {
      List<String> displayTimes =   new ArrayList<String>() ;
      //Calendar cal = CalendarUtils.getInstanceOfCurrentCalendar() ;
      Calendar cal = GregorianCalendar.getInstance(locale);
      cal.set(Calendar.HOUR_OF_DAY, 0) ;
      cal.set(Calendar.MINUTE, 0) ;
      cal.set(Calendar.MILLISECOND, 0) ;
      DateFormat valuedf = new SimpleDateFormat(CalendarUtils.TIMEFORMAT, locale) ;
      DateFormat df = new SimpleDateFormat(timeFormat, locale) ;
//      df.setCalendar(cal) ;
      for(int i = 0; i < 24*(60/timeInterval); i++) {
        displayTimes.add(valuedf.format(cal.getTime()) +"_"+ df.format(cal.getTime())) ;
        cal.add(java.util.Calendar.MINUTE, timeInterval) ;
      }
      return displayTimes ;
    }
    protected Map<String, String> getTimeSteps(String timeFormat, int timeInterval) {
      if(timeSteps_ == null) {
        timeSteps_ = new LinkedHashMap<String, String>() ;
        Calendar cal = CalendarUtils.getInstanceOfCurrentCalendar() ;
        cal.setTime(getCurrentDate()) ;
        cal.set(Calendar.HOUR_OF_DAY, 0) ;
        cal.set(Calendar.MINUTE, 0) ;
        cal.set(Calendar.MILLISECOND, 0) ;
        DateFormat df = new SimpleDateFormat(timeFormat) ;
        df.setCalendar(cal) ;
        for(int i = 0; i < 24*(60/timeInterval); i++) {
          timeSteps_.put(String.valueOf(cal.getTimeInMillis()), df.format(cal.getTime())) ;
          cal.add(java.util.Calendar.MINUTE, timeInterval) ;
        }
      }
      return timeSteps_ ;
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

    public String getLabel(String arg) {
      if (CalendarUtils.isEmpty(arg)) return "" ;
      try {
        return super.getLabel(arg) ;
      } catch (Exception e) {
        e.printStackTrace() ;
        return arg ;
      }
    }  
    @Override
    public void processRender(WebuiRequestContext arg0) throws Exception {
      if(this instanceof UIListView) {} else refresh() ;
      super.processRender(arg0);
    }
    public List<CalendarEvent>  getSelectedEvents(String eventIds) throws Exception {
      String[] list = eventIds.split(",");
      //CalendarService calService = CalendarUtils.getCalendarService() ;
        List<CalendarEvent> dataList = new ArrayList<CalendarEvent>() ;
      for(int i = 0; i < list.length; i++){
        CalendarEvent evt = getDataMap().get(list[i]) ;
        dataList.add(evt);
      }
    return dataList;
  }
    
    /**
     * Get data about recurrence events <br/>
     * Each item of the map has the key is the eventid of recurrence event.
     * The value is the map contains all occurrence events with the key is the recurrence-id
     * @return the Map contains recurrence events data
     */
    public Map<String,Map<String,CalendarEvent>> getRecurrenceMap() {
      return recurrenceEventsMap;
    }
    
    
    public void setCurrentOccurrence(CalendarEvent currentOccurrence) {
      this.currentOccurrence = currentOccurrence;
    }

    public CalendarEvent getcurrentOccurrence() {
      return currentOccurrence;
    }


    static  public class AddEventActionListener extends EventListener<UICalendarView> {
      public void execute(Event<UICalendarView> event) throws Exception {
        UICalendarView uiForm = event.getSource() ;
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        String username = CalendarUtils.getCurrentUser() ;
        if(CalendarUtils.getCalendarOption().isEmpty()) {
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.calendar-list-empty", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }  
        List<EventCategory> eventCategories = CalendarUtils.getCalendarService().getEventCategories(username) ;
        if(eventCategories.isEmpty()) {
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.event-category-list-empty", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }  
        String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
        String formTime = CalendarUtils.getCurrentTime(uiForm) ;
        //String formTime = event.getRequestContext().getRequestParameter(CURRENTTIME) ;
        String value = uiForm.getUIFormSelectBox(EVENT_CATEGORIES).getValue() ;
        UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        UIPopupAction uiParenPopup = uiPortlet.getChild(UIPopupAction.class) ;
        UIPopupContainer uiPopupContainer = uiParenPopup.activate(UIPopupContainer.class, 700) ;
        if(CalendarEvent.TYPE_TASK.equals(type)) {
          uiPopupContainer.setId(UIPopupContainer.UITASKPOPUP) ;
          UITaskForm uiTaskForm = uiPopupContainer.addChild(UITaskForm.class, null, null) ;
          uiTaskForm.initForm(uiPortlet.getCalendarSetting(), null, formTime) ;
          uiTaskForm.setEmailAddress(CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username).getEmail()) ;
          uiTaskForm.update(CalendarUtils.PRIVATE_TYPE,  CalendarUtils.getCalendarOption()) ;
          if(CalendarUtils.isEmpty(value)) uiTaskForm.setSelectedCategory("meeting") ;
          else uiTaskForm.setSelectedCategory(value) ;  
        } else {
          uiPopupContainer.setId(UIPopupContainer.UIEVENTPOPUP) ;
          UIEventForm uiEventForm =  uiPopupContainer.addChild(UIEventForm.class, null, null) ;
          uiEventForm.initForm(uiPortlet.getCalendarSetting(), null, formTime) ;
          uiEventForm.update(CalendarUtils.PRIVATE_TYPE, CalendarUtils.getCalendarOption()) ;
          uiEventForm.setSelectedEventState(UIEventForm.ITEM_BUSY) ;
          uiEventForm.setParticipant(username) ;
          uiEventForm.setParticipantStatus(username);
          uiEventForm.getChild(UIEventShareTab.class).setParticipantStatusList(uiEventForm.getParticipantStatusList());
          uiEventForm.setEmailAddress(CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username).getEmail()) ;
          uiEventForm.setEmailRemindBefore(String.valueOf(5));
          uiEventForm.setEmailReminder(true) ;
          uiEventForm.setEmailRepeat(false) ;
          if(CalendarUtils.isEmpty(value)) uiEventForm.setSelectedCategory("Meeting") ;  
          else  uiEventForm.setSelectedCategory(value) ;
        }
        //event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiParenPopup) ;     
      }
    }

    static  public class DeleteEventActionListener extends EventListener<UICalendarView> {
      public void execute(Event<UICalendarView> event) throws Exception {
        UICalendarView uiCalendarView = event.getSource() ;
        UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
        uiCalendarView.allDelete_ = true ;
        UICalendarPortlet calPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
        calPortlet.cancelAction() ;
        if(uiCalendarView instanceof UIMonthView ) {
          List<CalendarEvent> list = ((UIMonthView)uiCalendarView).getSelectedEvents() ;
          if(list.isEmpty()) {
            uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.check-box-required", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          } else {
            try {
              uiCalendarView.removeEvents(list) ;
              ((UIMonthView)uiCalendarView).refresh() ;
              if(uiCalendarView.allDelete_) {
                uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.delete-event-successfully", null)) ;
              } else {
                uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.can-not-delete-all-event",null)) ;
              }
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            } catch (Exception e) {
              e.printStackTrace() ;
              uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.delete-event-error", null, ApplicationMessage.WARNING)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
          }
        } else if(uiCalendarView instanceof UIListView) {
          UIListView uiListView = (UIListView)uiCalendarView ;
          List<CalendarEvent> list = ((UIListView)uiCalendarView).getSelectedEvents();
          if(uiListView.getSelectedEvents().isEmpty()) {
            uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.check-box-required", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          try {
            UIListContainer  uiListContainer = uiCalendarView.getParent() ;
            long currentPage = uiListView.getCurrentPage() ;
            uiCalendarView.removeEvents(list) ;
            uiListView.setSelectedEvent(null) ;
            uiListView.setLastUpdatedEventId(null) ;
            uiListContainer.refresh() ;
            if(currentPage <= uiListView.getAvailablePage()) uiListView.updateCurrentPage(currentPage) ;
            if(uiCalendarView.allDelete_) {
              uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.delete-event-successfully", null)) ;
            } else {
              uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.can-not-delete-all-event",null)) ;
            }
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;

          } catch (Exception e) {
            e.printStackTrace() ;
            uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.delete-event-error", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
        } else {
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.function-not-supported", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        UIMiniCalendar uiMiniCalendar = uiCalendarView.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
        uiCalendarView.setLastUpdatedEventId(null) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView.getParent()) ;
      }
    }
    static  public class ChangeCategoryActionListener extends EventListener<UICalendarView> {
      public void execute(Event<UICalendarView> event) throws Exception {
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
        UIEventCategoryManager uiCalendarViewMan = popupAction.activate(UIEventCategoryManager.class, 470) ;
        uiCalendarViewMan.categoryId_ = listView.getSelectedCategory() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
    }


    static  public class ViewActionListener extends EventListener<UICalendarView> {
      public void execute(Event<UICalendarView> event) throws Exception {
        UICalendarView uiCalendarView = event.getSource() ;
        UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
        uiPortlet.cancelAction() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        CalendarEvent eventCalendar = null ;
        if(uiCalendarView instanceof UIListView) {
          UIListView uiListView = (UIListView)uiCalendarView ;
          long pageNum = uiListView.getCurrentPage() ;
          if(!uiListView.isDisplaySearchResult()) uiCalendarView.refresh() ;
          uiListView.updateCurrentPage(pageNum) ; 
        } else {
          uiCalendarView.refresh() ;
        }  
        String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
        Boolean isOccur = false;
        if (!Utils.isEmpty(event.getRequestContext().getRequestParameter(ISOCCUR))) {
          isOccur = Boolean.parseBoolean(event.getRequestContext().getRequestParameter(ISOCCUR));
        }
        // need to get recurrence-id
        String recurId = null;
        if (isOccur) recurId = event.getRequestContext().getRequestParameter(RECURID);
        
        if(uiCalendarView.getDataMap() != null) {
          eventCalendar = uiCalendarView.getDataMap().get(eventId) ;
          
          if (isOccur && !Utils.isEmpty(recurId)) {
            eventCalendar = uiCalendarView.recurrenceEventsMap.get(eventId).get(recurId);
          }
        }
        if(eventCalendar != null) {
          if(uiCalendarView instanceof UIListView) {
            UIListView uiListView = (UIListView)uiCalendarView ;
            UIListContainer uiListContainer = uiListView.getParent() ;
            uiListView.setLastUpdatedEventId(eventId) ;
            uiListView.setSelectedEvent(eventCalendar.getId()) ;
            UIPreview uiPreview = uiListContainer.getChild(UIPreview.class);
            uiPreview.setEvent(eventCalendar);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiListContainer);
          } else {
            UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
            UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class, 700) ;
            uiPopupContainer.setId("UIEventPreview");
            UIPreview uiPreview = uiPopupContainer.addChild(UIPreview.class, null, null) ;
            uiPreview.setEvent(eventCalendar) ;
            uiPreview.setId("UIPreviewPopup") ;
            uiPreview.setShowPopup(true) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
          }
        } else {
          UICalendarWorkingContainer uiWorkingContainer = uiCalendarView.getAncestorOfType(UICalendarWorkingContainer.class) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
          UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.event-not-found", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }
      }
    }

    static  public class EditActionListener extends EventListener<UICalendarView> {
      public void execute(Event<UICalendarView> event) throws Exception {
        UICalendarView uiCalendarView = event.getSource() ;
        UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
        UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
        UIPopupContainer uiPopupContainer = uiPortlet.createUIComponent(UIPopupContainer.class, null, null) ;
        CalendarEvent eventCalendar = null ;
        String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
        Boolean isOccur = false;
        if (!Utils.isEmpty(event.getRequestContext().getRequestParameter(ISOCCUR))) {
          isOccur = Boolean.parseBoolean(event.getRequestContext().getRequestParameter(ISOCCUR));
        }
        // need to get recurrence-id
        String recurId = null;
        if (isOccur) recurId = event.getRequestContext().getRequestParameter(RECURID);
        
        // cs-1825
        //event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView.getParent()) ;
        if(uiCalendarView instanceof UIListView ) {
          UIListContainer listContainer = uiCalendarView.getAncestorOfType(UIListContainer.class) ;
          UIListView uiListView = listContainer.findFirstComponentOfType(UIListView.class) ;
          long pageNum = uiListView.getCurrentPage() ;
          if (!listContainer.isDisplaySearchResult()) {
            listContainer.refresh() ;
            uiListView.updateCurrentPage(pageNum) ; 
          }          
          uiListView.setSelectedEvent(eventId) ;
        } else if( uiCalendarView instanceof UIPreview) {
          UIPreview uiPreview = (UIPreview)uiCalendarView ;
          UIListContainer listContainer = uiCalendarView.getAncestorOfType(UIListContainer.class) ;
          UIListView uiListView = listContainer.findFirstComponentOfType(UIListView.class) ;
          long pageNum = uiListView.getCurrentPage() ;
          if (!listContainer.isDisplaySearchResult()) {
            listContainer.refresh() ;
            uiListView.updateCurrentPage(pageNum) ;             
          }          
          uiListView.setSelectedEvent(eventId) ;
          eventCalendar = uiListView.getDataMap().get(eventId) ;
          uiPreview.setEvent(eventCalendar) ;
          if (!listContainer.isDisplaySearchResult()) uiPreview.refresh() ;
        } else uiCalendarView.refresh() ;
        String username = CalendarUtils.getCurrentUser() ;
        String calendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
        String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
        if(uiCalendarView.getDataMap() != null && uiCalendarView.getDataMap().get(eventId) != null) {
          if (eventCalendar == null) {
            eventCalendar = uiCalendarView.getDataMap().get(eventId) ;
            
            if (isOccur && !Utils.isEmpty(recurId)) {
              eventCalendar = uiCalendarView.getRecurrenceMap().get(eventId).get(recurId);
            }
          }
          CalendarService calendarService = CalendarUtils.getCalendarService() ;
          boolean canEdit = false ;
          if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
            canEdit = true ;
          } else if (CalendarUtils.SHARED_TYPE.equals(calType)) {
            GroupCalendarData calendarData = calendarService.getSharedCalendars(CalendarUtils.getCurrentUser(), true)  ;
            if(calendarData != null && calendarData.getCalendarById(calendarId) != null)
              canEdit = CalendarUtils.canEdit(null, Utils.getEditPerUsers(calendarData.getCalendarById(calendarId)), username) ;
          } else if (CalendarUtils.PUBLIC_TYPE.equals(calType)) {
            OrganizationService oSevices = uiCalendarView.getApplicationComponent(OrganizationService.class) ;
            List<GroupCalendarData> publicData = uiCalendarView.getPublicCalendars(username) ;
            for (GroupCalendarData calendarData : publicData) {
              if(calendarData.getCalendarById(calendarId) != null) {
                // cs-4429: fix for group calendar permission
                canEdit = CalendarUtils.canEdit(oSevices, (calendarData.getCalendarById(calendarId)).getEditPermission(), username) ;
                break ;
              }
            }
          }
          if(canEdit) {
            if(CalendarEvent.TYPE_EVENT.equals(eventCalendar.getEventType())) {
              if (isOccur && !Utils.isEmpty(recurId)) {
                uiCalendarView.setCurrentOccurrence(eventCalendar);
              }
              
              uiPopupContainer.setId(UIPopupContainer.UIEVENTPOPUP) ;
              UIEventForm uiEventForm = uiPopupContainer.createUIComponent(UIEventForm.class, null, null) ;
              uiEventForm.update(calType, CalendarUtils.getCalendarOption()) ;
              uiEventForm.initForm(uiPortlet.getCalendarSetting(), eventCalendar, null) ;
              uiEventForm.setSelectedCalendarId(calendarId) ;
              uiPopupContainer.addChild(uiEventForm) ;
              uiPopupAction.activate(uiPopupContainer, 700, 0) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
            } else if(CalendarEvent.TYPE_TASK.equals(eventCalendar.getEventType())) {
              uiPopupContainer.setId(UIPopupContainer.UITASKPOPUP) ;
              UITaskForm uiTaskForm = uiPopupContainer.createUIComponent(UITaskForm.class, null, null) ;
              uiTaskForm.update(calType, CalendarUtils.getCalendarOption()) ;
              uiTaskForm.initForm(uiPortlet.getCalendarSetting(), eventCalendar, null) ;
              uiTaskForm.setSelectedCalendarId(calendarId) ;
              uiPopupContainer.addChild(uiTaskForm) ;
              uiPopupAction.activate(uiPopupContainer, 700, 0) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
            }
          } else {
            UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
            uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.have-no-edit-permission", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
        } else {
          UICalendarWorkingContainer uiWorkingContainer = uiCalendarView.getAncestorOfType(UICalendarWorkingContainer.class) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
          UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.event-not-found", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }
      }
    }
    static  public class DeleteActionListener extends EventListener<UICalendarView> {
      public void execute(Event<UICalendarView> event) throws Exception {
        UICalendarView uiCalendarView = event.getSource() ;
        String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
        String calendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
        String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
        String username = CalendarUtils.getCurrentUser() ;
        Boolean isOccur = false;
        if (!Utils.isEmpty(event.getRequestContext().getRequestParameter(ISOCCUR))) {
          isOccur = Boolean.parseBoolean(event.getRequestContext().getRequestParameter(ISOCCUR));
        }
        // need to get recurrence-id
        String recurId = null;
        if (isOccur) recurId = event.getRequestContext().getRequestParameter(RECURID);
        
        CalendarService calendarService = CalendarUtils.getCalendarService() ;
        UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
        UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
        uiPortlet.cancelAction() ;
        org.exoplatform.calendar.service.Calendar calendar = null ;
        
        try {
          // if event is occurrence event (not exception)
          if (isOccur && !Utils.isEmpty(recurId)) {
            CalendarEvent currentOccurrence = uiCalendarView.getRecurrenceMap().get(eventId).get(recurId);
            uiCalendarView.setCurrentOccurrence(currentOccurrence);
            UIConfirmForm confirmForm = uiPopupAction.activate(UIConfirmForm.class, 600);
            confirmForm.setConfirmMessage(uiCalendarView.getLabel("delete-recurrence-event-confirm-msg"));
            confirmForm.setConfig_id(uiCalendarView.getId());
            String[] actions = new String[] {"ConfirmDeleteOnlyInstance", "ConfirmDeleteAllSeries", "ConfirmDeleteCancel"};
            confirmForm.setActions(actions);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
            return;
          }
          
          if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
            calendar = calendarService.getUserCalendar(username, calendarId) ;
          } else if (CalendarUtils.SHARED_TYPE.equals(calType)) {
            GroupCalendarData calendarData = calendarService.getSharedCalendars(CalendarUtils.getCurrentUser(), true)  ;
            if(calendarData != null) calendar = calendarData.getCalendarById(calendarId) ;
          } else if (CalendarUtils.PUBLIC_TYPE.equals(calType)) {
            calendar = calendarService.getGroupCalendar(calendarId) ;
          }
          if(calendar == null) {
            UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
            uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          } else {
            // if calendar is remote calendar
            if(calendarService.isRemoteCalendar(CalendarUtils.getCurrentUser(), calendarId)) {
              UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
              uiApp.addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return;
            }
            
            // cs-4429: fix for group calendar permission
            if((CalendarUtils.SHARED_TYPE.equals(calType) && !CalendarUtils.canEdit(uiCalendarView.getApplicationComponent(
                      OrganizationService.class), Utils.getEditPerUsers(calendar), CalendarUtils.getCurrentUser())) ||
               (CalendarUtils.PUBLIC_TYPE.equals(calType) && !CalendarUtils.canEdit(uiCalendarView.getApplicationComponent(
                      OrganizationService.class), calendar.getEditPermission(), CalendarUtils.getCurrentUser()))) 
            {
              UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
              uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit-event", null, 1)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              uiCalendarView.refresh() ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView.getParent()) ;
              return ;
            }
            
            if(CalendarUtils.PUBLIC_TYPE.equals(calType)){
              calendarService.removePublicEvent(calendarId, eventId) ;
            } else if(CalendarUtils.PRIVATE_TYPE.equals(calType)){
              calendarService.removeUserEvent(username, calendarId, eventId) ;
            } else if(CalendarUtils.SHARED_TYPE.equals(calType)) {
              calendarService.removeSharedEvent(username, calendarId, eventId) ;
            }
            UICalendarViewContainer uiContainer = uiCalendarView.getAncestorOfType(UICalendarViewContainer.class) ;
            UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
            uiCalendarView.setLastUpdatedEventId(null) ;
            // hung.hoang
            
            if(uiContainer.getRenderedChild() instanceof UIListContainer) {
              UIListView uiListView = ((UIListContainer)uiContainer.getRenderedChild()).getChild(UIListView.class) ;
              if (uiListView.isDisplaySearchResult()) {
                if (uiListView.getDataMap().containsKey(eventId)){
                  uiListView.getDataMap().remove(eventId) ;
                  try {
                    UIPreview preview = ((UIListContainer)uiContainer.getRenderedChild()).findFirstComponentOfType(UIPreview.class) ;
                    if (preview.getEvent() != null && preview.getEvent().getId().equals(eventId)) {
                      preview.setEvent(null) ;
                    }
                  } catch (Exception ex) { }  
                }
                event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
                return ;
              }              
            } 
            
            
            if(event.getSource() instanceof UIListView) {
              UIListView listView = (UIListView) event.getSource() ;
              long currentPage = listView.getCurrentPage() ;
              uiContainer.refresh() ;
              if(currentPage <= listView.getAvailablePage()) listView.updateCurrentPage(currentPage) ;
            } else if(event.getSource() instanceof UIPreview) {
              UIPreview preview = (UIPreview) event.getSource() ;
              UIListContainer listContainer = preview.getAncestorOfType(UIListContainer.class) ;
              UIListView listView = listContainer.findFirstComponentOfType(UIListView.class) ;
              long currentPage = listView.getCurrentPage() ;
              uiContainer.refresh() ;
              if(currentPage <= listView.getAvailablePage()) listView.updateCurrentPage(currentPage) ;
            } else {
              uiContainer.refresh() ;
            }
            event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
          }
        } catch (PathNotFoundException e) {
          e.printStackTrace() ;
          UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }
        UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        CalendarSetting setting = calendarService.getCalendarSetting(username) ;
        uiViewContainer.refresh() ;
        uiPortlet.setCalendarSetting(setting) ;
        
        //cs-1825
        event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
      }
    }
    static public class TaskViewActionListener extends EventListener<UICalendarView> {
      public void execute(Event<UICalendarView> event) throws Exception {
        UICalendarView uiCalendarView = event.getSource() ;     
        String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
        UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
        UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        UIListView uiListView = uiViewContainer.findFirstComponentOfType(UIListView.class) ;
        String username = CalendarUtils.getCurrentUser() ;
        EventQuery eventQuery = new EventQuery() ;
        java.util.Calendar fromcalendar  = uiListView.getBeginDay(new GregorianCalendar(uiListView.getCurrentYear(), uiListView.getCurrentMonth(), uiListView.getCurrentDay())) ;
        eventQuery.setFromDate(fromcalendar) ;
        java.util.Calendar tocalendar = uiListView.getEndDay(new GregorianCalendar(uiListView.getCurrentYear(), uiListView.getCurrentMonth(), uiListView.getCurrentDay())) ;
        eventQuery.setToDate(tocalendar) ;
        
        UICalendars uiCalendars = uiPortlet.findFirstComponentOfType(UICalendars.class);
        List<String> checkedCals = uiCalendars.getCheckedCalendars() ;        
        List<String> calendarIds = new ArrayList<String>() ; 
        for (GroupCalendarData groupCalendarData : uiCalendars.getPrivateCalendars())
          for (org.exoplatform.calendar.service.Calendar cal : groupCalendarData.getCalendars())
            if (checkedCals.contains(cal.getId())) calendarIds.add(cal.getId());
        for (GroupCalendarData calendarData : uiCalendars.getPublicCalendars())
          for (org.exoplatform.calendar.service.Calendar  calendar : calendarData.getCalendars())
            if (checkedCals.contains(calendar.getId())) calendarIds.add(calendar.getId());
        GroupCalendarData shareClas = uiCalendars.getSharedCalendars();
        if (shareClas != null)
          for (org.exoplatform.calendar.service.Calendar cal : shareClas.getCalendars())
            if (checkedCals.contains(cal.getId())) {
              calendarIds.add(cal.getId());
            }
        if (calendarIds.size() > 0)
          eventQuery.setCalendarId(calendarIds.toArray(new String[] {}));
        else {
          eventQuery.setCalendarId(new String[] {"null"});
        }
        eventQuery.setOrderBy(new String[] {Utils.EXO_SUMMARY});
        uiListView.setEventQuery(eventQuery);
        uiListView.update(new EventPageListQuery(username, eventQuery.getQueryStatement(), 10)) ;
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
          String currentTime = event.getRequestContext().getRequestParameter("currentTime") ;
          UICalendarPortlet portlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
          UICalendarViewContainer uiContainer = portlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
          UIMiniCalendar uiMiniCalendar = portlet.findFirstComponentOfType(UIMiniCalendar.class) ;
          Calendar cal = calendarview.getInstanceTempCalendar() ;
          cal.setTimeInMillis(Long.parseLong(currentTime)) ;
          int type = Integer.parseInt(viewType) ;
          
          if (type == TYPE_NONE) {
            String viewTypeStr =  uiContainer.getCurrentViewType();
            for (int i = 0; i < UICalendarViewContainer.TYPES.length; i++) {
              String t = UICalendarViewContainer.TYPES[i];
              if (t.equals(viewTypeStr)) { 
                type = i;
                break;
              }
            }
          }
          ((UICalendarView) uiContainer.getRenderedChild()).setCurrentCalendar(cal);
          uiContainer.initView(UICalendarViewContainer.TYPES[type]) ;
          switch (type){
          case TYPE_DAY : {
            if(uiContainer.getRenderedChild() instanceof UIDayView) {
              UIDayView uiView = uiContainer.getChild(UIDayView.class) ;
              uiView.setCurrentCalendar(cal) ;
              uiView.refresh() ;
            } else if(uiContainer.getRenderedChild() instanceof UIListContainer) {
              UIListContainer uiView = uiContainer.getChild(UIListContainer.class) ;
              UIListView uiListView = uiView.getChild(UIListView.class) ;
              if(!uiListView.isDisplaySearchResult()){
                uiView.setCurrentCalendar(cal) ;
                uiView.setSelectedCategory(calendarview.getSelectedCategory()) ;
                uiView.refresh() ;
              }
              uiContainer.setRenderedChild(UIListContainer.class) ;
            } else {
              uiContainer.setRenderedChild(UIDayView.class) ;
              UIDayView uiView = uiContainer.getChild(UIDayView.class) ;
              uiView.setCurrentCalendar(cal) ;
              uiView.refresh() ;
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
            uiView.setCategoryId(calendarview.getSelectedCategory()) ;
            uiView.refresh() ;
            uiContainer.setRenderedChild(UIYearView.class) ;
          }break;
          default:break;
          }
          uiMiniCalendar.setCurrentCalendar(cal) ;
          UIActionBar uiActionBar = portlet.findFirstComponentOfType(UIActionBar.class) ;
          uiActionBar.setCurrentView(uiContainer.getRenderedChild().getId()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
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
        if(uiView instanceof UIListView) {
          UIListView uiListView = (UIListView)uiView ;
          uiListView.setCurrentPage(1) ;
          
        }
        uiView.refresh() ;
        UIListContainer uiListContainer = uiView.getAncestorOfType(UIListContainer.class) ;
        if(uiView instanceof UIListView) {
          UIListView uiListView = (UIListView)uiView ;
          uiListView.setSelectedEvent(null) ;
        }        
        if(uiListContainer != null) {
          uiListContainer.setLastUpdatedEventId(null) ;
          uiListContainer.getChild(UIPreview.class).setEvent(null) ; 
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiView.getParent());
      }
    }
    static  public class QuickAddActionListener extends EventListener<UICalendarView> {
      public void execute(Event<UICalendarView> event) throws Exception {
        UICalendarView uiForm = event.getSource() ;
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        if(CalendarUtils.getCalendarOption().isEmpty()) {
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.calendar-list-empty", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        List<EventCategory> eventCategories = CalendarUtils.getCalendarService().getEventCategories(CalendarUtils.getCurrentUser()) ;
        if(eventCategories.isEmpty()) {
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.event-category-list-empty", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }  
        String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
        String startTime = event.getRequestContext().getRequestParameter("startTime") ;
        String finishTime = event.getRequestContext().getRequestParameter("finishTime") ;
        String selectedCategory = uiForm.getUIFormSelectBox(EVENT_CATEGORIES).getValue() ;
        UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
        UIQuickAddEvent uiQuickAddEvent = uiPopupAction.activate(UIQuickAddEvent.class, 600) ;
        if(!CalendarUtils.isEmpty(selectedCategory)) 
          uiQuickAddEvent.setSelectedCategory(selectedCategory) ;
        else uiQuickAddEvent.setSelectedCategory("Meeting") ; 
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
      }
    }
    static  public class MoveNextActionListener extends EventListener<UIMonthView> {
      public void execute(Event<UIMonthView> event) throws Exception {
        UICalendarView calendarview = event.getSource() ;
        try {
          String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
          int field = Integer.parseInt(type) ;
          calendarview.moveCalendarTo(field, 1) ;
          calendarview.refresh() ;
          UICalendarPortlet uiClendarPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
          UIMiniCalendar uiMiniCalendar = uiClendarPortlet.findFirstComponentOfType(UIMiniCalendar.class);
          uiMiniCalendar.setCurrentCalendar(calendarview.getCurrentCalendar()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
        } catch (Exception e) {
          e.printStackTrace() ;
          return ;
        }
      }
    }
    static  public class MovePreviousActionListener extends EventListener<UIMonthView> {
      public void execute(Event<UIMonthView> event) throws Exception {
        UICalendarView calendarview = event.getSource() ;
        try {
          String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
          int field = Integer.parseInt(type) ;
          calendarview.moveCalendarTo(field, -1) ;
          calendarview.refresh() ;
          UICalendarPortlet uiClendarPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
          UIMiniCalendar uiMiniCalendar = uiClendarPortlet.findFirstComponentOfType(UIMiniCalendar.class);
          uiMiniCalendar.setCurrentCalendar(calendarview.getCurrentCalendar()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
        } catch (Exception e) {
          e.printStackTrace() ;
          return ;
        }
      }
    }
   static public class ExportEventActionListener extends EventListener<UICalendarView>{
     public void execute(Event<UICalendarView> event) throws Exception{
       UICalendarView uiComponent = event.getSource() ;
       UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
          String currentUser = CalendarUtils.getCurrentUser() ;
          CalendarService calService = CalendarUtils.getCalendarService() ;
          String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
          String selectedCalendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
          String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
          UIApplication uiApp = uiComponent.getAncestorOfType(UIApplication.class) ;
          org.exoplatform.calendar.service.Calendar calendar = null;
          CalendarEvent instanceEvent = new CalendarEvent();
          instanceEvent.setId(eventId);
          if(instanceEvent.getEventType().equalsIgnoreCase("Task")){
            uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar" + "aaa", null, 1)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
          }
          if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
            calendar = calService.getUserCalendar(currentUser, selectedCalendarId) ;
            if (calendar.getId().equals(Utils.getDefaultCalendarId(currentUser)) && calendar.getName().equals(NewUserListener.DEFAULT_CALENDAR_NAME)) {
              String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.DEFAULT_CALENDAR_ID);
              calendar.setName(newName);
            }
          } else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
            GroupCalendarData gCalendarData = calService.getSharedCalendars(currentUser, true) ;
            if(gCalendarData != null) { 
              calendar = gCalendarData.getCalendarById(selectedCalendarId) ;
              if (calendar.getId().equals(Utils.getDefaultCalendarId(calendar.getCalendarOwner())) && calendar.getName().equals(NewUserListener.DEFAULT_CALENDAR_NAME)) {
                String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.DEFAULT_CALENDAR_ID);
                calendar.setName(newName);
              }
              if(calendar != null && !CalendarUtils.isEmpty(calendar.getCalendarOwner())) calendar.setName(calendar.getCalendarOwner() + "-" + calendar.getName()) ;
            }
          } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
            try {
              calendar = calService.getGroupCalendar(selectedCalendarId) ;
            } catch (PathNotFoundException e) {
              System.out.println("\n\n calendar has been removed !");
            }
          }  
          if(calendar == null) {
            uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
          } else {
            boolean canEdit = false ;
            if(calType.equals(CalendarUtils.SHARED_TYPE)) {
              canEdit = CalendarUtils.canEdit(null, Utils.getEditPerUsers(calendar), currentUser) ;
            } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
              // cs-4429: fix for group calendar permission
              canEdit = CalendarUtils.canEdit(CalendarUtils.getOrganizationService(), calendar.getEditPermission(), currentUser) ;
            }
            if(!calType.equals(CalendarUtils.PRIVATE_TYPE) && !canEdit) {
              uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
            List<org.exoplatform.calendar.service.Calendar> list = new ArrayList<org.exoplatform.calendar.service.Calendar>() ;
            list.add(calendar) ;
            UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
            popupAction.deActivate() ;
            
            UIExportForm exportForm = popupAction.activate(UIExportForm.class, 500) ;
            exportForm.eventId = eventId ;
            exportForm.update(calType, list, selectedCalendarId) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
          }
       
     }
   }
   
   static public class MoveEventActionListener extends EventListener<UICalendarView>{
     public void execute(Event<UICalendarView> event) throws Exception{
       UICalendarView uiComponent = event.getSource() ;
       String eventIds = event.getRequestContext().getRequestParameter("objectId") ;
       String selectedCalendarId = event.getRequestContext().getRequestParameter("calendarId") ;
       String toType = event.getRequestContext().getRequestParameter("caltype") ;
       String currentUser = CalendarUtils.getCurrentUser() ;
       CalendarService calService = CalendarUtils.getCalendarService() ;
       List<CalendarEvent> eventList = uiComponent.getSelectedEvents(eventIds);
       for(CalendarEvent ce : eventList){
         calService.moveEvent(ce.getCalendarId(), selectedCalendarId, ce.getCalType(), toType, eventList, currentUser) ;     
       }       
     }
   }
   
   public static class ConfirmDeleteOnlyInstance extends EventListener<UICalendarView> {
     public void execute(Event<UICalendarView> event) throws Exception {
       // delete the only selected event
       
       UICalendarView uiCalendarView = event.getSource() ;
       UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
       UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
       
       try {
         CalendarEvent occurrence = uiCalendarView.getcurrentOccurrence();
         String calendarId = occurrence.getCalendarId();
         String calType = occurrence.getCalType();
         String username = CalendarUtils.getCurrentUser() ;
         CalendarService calService = CalendarUtils.getCalendarService() ;
         
         org.exoplatform.calendar.service.Calendar calendar = null ;
         if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
           calendar = calService.getUserCalendar(username, calendarId) ;
         } else if (CalendarUtils.SHARED_TYPE.equals(calType)) {
           GroupCalendarData calendarData = calService.getSharedCalendars(CalendarUtils.getCurrentUser(), true)  ;
           if(calendarData != null) calendar = calendarData.getCalendarById(calendarId) ;
         } else if (CalendarUtils.PUBLIC_TYPE.equals(calType)) {
           calendar = calService.getGroupCalendar(calendarId) ;
         }
         
         // cs-4429: fix for group calendar permission
         if((CalendarUtils.SHARED_TYPE.equals(calType) && !CalendarUtils.canEdit(uiCalendarView.getApplicationComponent(
                   OrganizationService.class), Utils.getEditPerUsers(calendar), CalendarUtils.getCurrentUser())) ||
            (CalendarUtils.PUBLIC_TYPE.equals(calType) && !CalendarUtils.canEdit(uiCalendarView.getApplicationComponent(
                   OrganizationService.class), calendar.getEditPermission(), CalendarUtils.getCurrentUser()))) 
         {
           UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
           uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit-event", null, 1)) ;
           event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
           uiCalendarView.refresh() ;
           event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView.getParent()) ;
           return ;
         }
         
         calService.removeOccurrenceInstance(username, occurrence);
         // update UI
         uiPopupAction.deActivate();
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
   }
   
   public static class ConfirmDeleteAllSeries extends EventListener<UICalendarView> {
     public void execute(Event<UICalendarView> event) throws Exception {
       UICalendarView uiCalendarView = event.getSource() ;
       UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
       UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
       
       try {
         CalendarEvent occurrence = uiCalendarView.getcurrentOccurrence();
         String eventId = occurrence.getId();
         String username = CalendarUtils.getCurrentUser() ;
         CalendarService calService = CalendarUtils.getCalendarService() ;
         // get the original recurrence node
         String calType = occurrence.getCalType();
         String calendarId = occurrence.getCalendarId();
         CalendarEvent originalEvent = null;
         
         org.exoplatform.calendar.service.Calendar calendar = null ;
         if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
           calendar = calService.getUserCalendar(username, calendarId) ;
         } else if (CalendarUtils.SHARED_TYPE.equals(calType)) {
           GroupCalendarData calendarData = calService.getSharedCalendars(CalendarUtils.getCurrentUser(), true)  ;
           if(calendarData != null) calendar = calendarData.getCalendarById(calendarId) ;
         } else if (CalendarUtils.PUBLIC_TYPE.equals(calType)) {
           calendar = calService.getGroupCalendar(calendarId) ;
         }
         
         // cs-4429: fix for group calendar permission
         if((CalendarUtils.SHARED_TYPE.equals(calType) && !CalendarUtils.canEdit(uiCalendarView.getApplicationComponent(
                   OrganizationService.class), Utils.getEditPerUsers(calendar), CalendarUtils.getCurrentUser())) ||
            (CalendarUtils.PUBLIC_TYPE.equals(calType) && !CalendarUtils.canEdit(uiCalendarView.getApplicationComponent(
                   OrganizationService.class), calendar.getEditPermission(), CalendarUtils.getCurrentUser()))) 
         {
           UIApplication uiApp = uiCalendarView.getAncestorOfType(UIApplication.class) ;
           uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit-event", null, 1)) ;
           event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
           uiCalendarView.refresh() ;
           event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarView.getParent()) ;
           return ;
         }
         

         if (calType.equals(CalendarUtils.PRIVATE_TYPE)) {
           originalEvent = calService.getEvent(username, eventId) ;
         }
         
         if (calType.equals(CalendarUtils.PUBLIC_TYPE)) {
           originalEvent = calService.getGroupEvent(occurrence.getCalendarId(), eventId) ;
         }
         if (calType.equals(CalendarUtils.SHARED_TYPE)) {
           originalEvent = calService.getSharedEvent(username, calendarId, eventId);
         }
         
         calService.removeRecurrenceSeries(username, originalEvent);
         
         uiPopupAction.deActivate();
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
   }
   
   public static class ConfirmDeleteCancel extends EventListener<UICalendarView> {
     public void execute(Event<UICalendarView> event) throws Exception {
       UICalendarView uiCalendarView = event.getSource() ;
       // cancel
       uiCalendarView.setCurrentOccurrence(null);
       UICalendarPortlet uiPortlet = uiCalendarView.getAncestorOfType(UICalendarPortlet.class) ;
       UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
       uiPortlet.cancelAction();
       uiPopupAction.deActivate();
     }
   }
}
