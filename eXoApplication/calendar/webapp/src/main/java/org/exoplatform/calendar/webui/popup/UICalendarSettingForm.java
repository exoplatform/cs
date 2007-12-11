/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarView;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UICalendarSettingForm.SaveActionListener.class),
      @EventConfig(listeners = UICalendarSettingForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UICalendarSettingForm extends UIFormTabPane implements UIPopupComponent{
  final private static String VIEW_TYPE = "viewType".intern() ;
  final private static String TIME_INTERVAL = "timeInterval".intern() ;
  final private static String WEEK_START_ON = "weekStartOn".intern() ;
  final private static String DATE_FORMAT = "dateFormat".intern() ;
  final private static String TIME_FORMAT = "timeFormat".intern() ;
  final private static String LOCATION = "location".intern() ;
  final private static String TIMEZONE = "timeZone".intern() ;
  final private static String ISSHOWWORKINGTIME = "showWorkingTime".intern() ;
  final private static String WORKINGTIME_BEGIN = "beginTime".intern() ;
  final private static String WORKINGTIME_END = "endTime".intern() ;
  final private static String BASE_URL = "baseURL".intern() ;
  final private static String DEFAULT_CALENDARS = "defaultCalendars".intern() ;

  private List<Calendar> calendars_ ;
  public UICalendarSettingForm() throws Exception{
    super("UICalendarSettingForm") ;
    UIFormInputWithActions setting = new UIFormInputWithActions("setting") ;//.setRendered(true) ;
    
    List<SelectItemOption<String>> viewTypes = new ArrayList<SelectItemOption<String>>() ;
    viewTypes.add(new SelectItemOption<String>("Day view", CalendarSetting.DAY_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Week view", CalendarSetting.WEEK_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Month view", CalendarSetting.MONTH_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Year view", CalendarSetting.YEAR_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("List view", CalendarSetting.LIST_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Schedule view", CalendarSetting.SCHEDULE_VIEW)) ;
    setting.addUIFormInput(new UIFormSelectBox(VIEW_TYPE, VIEW_TYPE, viewTypes)) ;

    List<SelectItemOption<String>> timeInterval = new ArrayList<SelectItemOption<String>>() ;
    int i = 5 ;
    while(i < 121) {
      timeInterval.add(new SelectItemOption<String>(i + " minutes", String.valueOf(i))) ;
      i += 5;
    }
    setting.addUIFormInput(new UIFormSelectBox(TIME_INTERVAL, TIME_INTERVAL, timeInterval)) ;

    List<SelectItemOption<String>> weekStartOn = new ArrayList<SelectItemOption<String>>() ;
    weekStartOn.add(new SelectItemOption<String>(UICalendarView.MONDAY, String.valueOf(java.util.Calendar.MONDAY))) ;
    weekStartOn.add(new SelectItemOption<String>(UICalendarView.TUESDAY, String.valueOf(java.util.Calendar.TUESDAY))) ;
    weekStartOn.add(new SelectItemOption<String>(UICalendarView.WEDNESDAY, String.valueOf(java.util.Calendar.WEDNESDAY))) ;
    weekStartOn.add(new SelectItemOption<String>(UICalendarView.THURSDAY, String.valueOf(java.util.Calendar.THURSDAY))) ;
    weekStartOn.add(new SelectItemOption<String>(UICalendarView.FRIDAY, String.valueOf(java.util.Calendar.FRIDAY))) ;
    weekStartOn.add(new SelectItemOption<String>(UICalendarView.SATURDAY, String.valueOf(java.util.Calendar.SATURDAY))) ;
    weekStartOn.add(new SelectItemOption<String>(UICalendarView.SUNDAY, String.valueOf(java.util.Calendar.SUNDAY))) ;    
    setting.addUIFormInput(new UIFormSelectBox(WEEK_START_ON, WEEK_START_ON, weekStartOn)) ;

    List<SelectItemOption<String>> dateFormat = new ArrayList<SelectItemOption<String>>() ;
    dateFormat.add(new SelectItemOption<String>("dd/mm/yyyy", "dd/MM/yyyy")) ;
    dateFormat.add(new SelectItemOption<String>("dd-mm-yyyy", "dd-MM-yyyy")) ;
    dateFormat.add(new SelectItemOption<String>("mm/dd/yyyy", "MM/dd/yyyy")) ;
    dateFormat.add(new SelectItemOption<String>("mm-dd-yyyy", "MM-dd-yyyy")) ;
    setting.addUIFormInput(new UIFormSelectBox(DATE_FORMAT, DATE_FORMAT, dateFormat)) ;

    List<SelectItemOption<String>> timeFormat = new ArrayList<SelectItemOption<String>>() ;
    timeFormat.add(new SelectItemOption<String>("AM/PM", "hh:mm a")) ;
    timeFormat.add(new SelectItemOption<String>("24 Hours", "HH:mm")) ;

    setting.addUIFormInput(new UIFormSelectBox(TIME_FORMAT, TIME_FORMAT, timeFormat)) ;
    setting.addUIFormInput(new UIFormSelectBox(LOCATION, LOCATION, getLocales())) ;
    setting.addUIFormInput(new UIFormSelectBox(TIMEZONE, TIMEZONE, getTimeZones())) ;
    setting.addUIFormInput(new UIFormCheckBoxInput<Boolean>(ISSHOWWORKINGTIME, ISSHOWWORKINGTIME, false)) ;
    List<SelectItemOption<String>> startTimes = new ArrayList<SelectItemOption<String>>() ;
    List<SelectItemOption<String>> endTimes = CalendarUtils.getTimesSelectBoxOptions(CalendarUtils.TIMEFORMAT, 30) ;
    setting.addUIFormInput(new UIFormSelectBox(WORKINGTIME_BEGIN, WORKINGTIME_BEGIN, startTimes)) ;
    setting.addUIFormInput(new UIFormSelectBox(WORKINGTIME_END, WORKINGTIME_END, endTimes)) ;

    setting.addUIFormInput(new UIFormStringInput(BASE_URL, BASE_URL, null)) ;
    addUIFormInput(setting) ;
    setSelectedTab(setting.getId()) ;
    UIFormInputWithActions defaultCalendars = new UIFormInputWithActions("defaultCalendars") ;    
    addUIFormInput(defaultCalendars) ;
  }

  private List<SelectItemOption<String>> getTimeZones() {
    return CalendarUtils.getTimeZoneSelectBoxOptions(TimeZone.getAvailableIDs()) ;
  }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void init(CalendarSetting calendarSetting ) throws Exception{
    CalendarService cservice = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    if(calendarSetting != null) {

      setViewType(calendarSetting.getViewType()) ;
      setTimeInterval(String.valueOf(calendarSetting.getTimeInterval())) ;
      setWeekStartOn(calendarSetting.getWeekStartOn()) ;
      setDateFormat(calendarSetting.getDateFormat()) ;
      setTimeFormat(calendarSetting.getTimeFormat()) ;
      getUIFormSelectBox(WORKINGTIME_BEGIN).setOptions(CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat(), 30)) ;
      getUIFormSelectBox(WORKINGTIME_END).setOptions(CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat(), 30)) ;
      if(calendarSetting.getLocation() == null) {
        calendarSetting.setLocation(Util.getPortalRequestContext().getLocale().getISO3Country()) ;
      }
      setLocale(calendarSetting.getLocation()) ;     
      setTimeZone(calendarSetting.getTimeZone()) ;
      setShowWorkingTimes(calendarSetting.isShowWorkingTime()) ;
      if(calendarSetting.isShowWorkingTime()) {
        setWorkingBegin(calendarSetting.getWorkingTimeBegin(), CalendarUtils.DATEFORMAT + " " + calendarSetting.getTimeFormat()) ;
        setWorkingEnd(calendarSetting.getWorkingTimeEnd(), CalendarUtils.DATEFORMAT + " " + calendarSetting.getTimeFormat()) ;
      }
      setBaseUrl(calendarSetting.getBaseURL()) ;
    }
    calendars_ = cservice.getUserCalendars(SessionsUtils.getSessionProvider(), username) ;
    List<String> settedCalendars = new ArrayList<String>() ;
    if(calendarSetting != null && calendarSetting.getDefaultPrivateCalendars() != null) {
      settedCalendars = new ArrayList<String>(Arrays.asList(calendarSetting.getDefaultPrivateCalendars())) ;
    }
    UIFormInputWithActions defaultCalendars = getChildById("defaultCalendars") ;    
    defaultCalendars.addChild(new UIFormInputInfo(DEFAULT_CALENDARS, DEFAULT_CALENDARS, DEFAULT_CALENDARS)) ;
    for(Calendar calendar : calendars_) {
      UIFormCheckBoxInput checkBox = new UIFormCheckBoxInput<Boolean>(calendar.getName(), calendar.getId(), false) ;
      for(String calendarId : settedCalendars) {
        if(calendar.getId().equals(calendarId)) checkBox.setChecked(true) ;        
      }
      defaultCalendars.addUIFormInput(checkBox) ;
    }
  }
  private List<SelectItemOption<String>> getLocales() {
   return CalendarUtils.getLocaleSelectBoxOptions(java.util.Calendar.getAvailableLocales()) ;
  }

  protected String getViewType() {
    return getUIFormSelectBox(VIEW_TYPE).getValue() ;
  }
  protected void setViewType(String value) {
    getUIFormSelectBox(VIEW_TYPE).setValue(value) ;
  }
  protected String getTimeInterval() {
    return getUIFormSelectBox(TIME_INTERVAL).getValue() ;
  }
  protected void setTimeInterval(String value) {
    getUIFormSelectBox(TIME_INTERVAL).setValue(value) ;
  }
  protected String getWeekStartOn() {
    return getUIFormSelectBox(WEEK_START_ON).getValue() ;
  }
  protected void setWeekStartOn(String value) {
    getUIFormSelectBox(WEEK_START_ON).setValue(value) ;
  }
  protected String getDateFormat() {
    return getUIFormSelectBox(DATE_FORMAT).getValue() ;
  }
  protected void setDateFormat(String value) {
    getUIFormSelectBox(DATE_FORMAT).setValue(value) ;
  }
  protected String getTimeFormat() {
    return getUIFormSelectBox(TIME_FORMAT).getValue() ;
  }
  protected void setTimeFormat(String value) {
    getUIFormSelectBox(TIME_FORMAT).setValue(value) ;
  }
  protected String getLocale() {
    return getUIFormSelectBox(LOCATION).getValue() ;
  }
  protected void setLocale(String value) {
    getUIFormSelectBox(LOCATION).setValue(value) ;
  }
  protected String getTimeZone() {
    return getUIFormSelectBox(TIMEZONE).getValue() ;
  }
  protected void setTimeZone(String value) {
    getUIFormSelectBox(TIMEZONE).setValue(value) ;
  }
  protected boolean getShowWorkingTimes() {
    return getUIFormCheckBoxInput(ISSHOWWORKINGTIME).isChecked() ;
  }
  protected void setShowWorkingTimes(boolean value) {
    getUIFormCheckBoxInput(ISSHOWWORKINGTIME).setChecked(value) ;
  }
  protected String getWorkingBegin() throws Exception {
    java.util.Calendar cal = GregorianCalendar.getInstance() ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat timeFormat = new SimpleDateFormat(getTimeFormat()) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_BEGIN).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return timeFormat.format(cal.getTime()) ;
  }

  protected Date getWorkingBeginTime() throws Exception {
    java.util.Calendar cal = CalendarUtils.getBeginDay(CalendarUtils.getInstanceTempCalendar()) ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_BEGIN).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return  cal.getTime()  ;
  }
  protected void setWorkingBegin(String value, String format) throws Exception {
    java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat timeFormat = new SimpleDateFormat(CalendarUtils.TIMEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(format) ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    getUIFormSelectBox(WORKINGTIME_BEGIN).setValue(timeFormat.format(cal.getTime())) ;
  }
  protected String getWorkingEnd() throws Exception{
    java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat timeFormat = new SimpleDateFormat(getTimeFormat()) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_END).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return timeFormat.format(cal.getTime()) ;
  }

  protected Date getWorkingEndTime() throws Exception{
    java.util.Calendar cal = CalendarUtils.getBeginDay(CalendarUtils.getInstanceTempCalendar()) ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_END).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return  cal.getTime();
  }
  protected void setWorkingEnd(String value, String format) throws Exception {
    java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat timeFormat = new SimpleDateFormat(CalendarUtils.TIMEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(format) ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    getUIFormSelectBox(WORKINGTIME_END).setValue(timeFormat.format(cal.getTime())) ;
  }
  protected String getBaseUrl() {
    return getUIStringInput(BASE_URL).getValue() ;
  }
  protected void setBaseUrl(String value) {
    getUIStringInput(BASE_URL).setValue(value) ;
  }


  static  public class SaveActionListener extends EventListener<UICalendarSettingForm> {
    public void execute(Event<UICalendarSettingForm> event) throws Exception {
      UICalendarSettingForm uiForm = event.getSource() ;      
      CalendarSetting calendarSetting = new CalendarSetting() ;
      calendarSetting.setViewType(uiForm.getViewType()) ;
      calendarSetting.setTimeInterval(Long.parseLong(uiForm.getTimeInterval())) ;
      calendarSetting.setWeekStartOn(uiForm.getWeekStartOn()) ;
      calendarSetting.setDateFormat(uiForm.getDateFormat()) ;
      calendarSetting.setTimeFormat(uiForm.getTimeFormat()) ;
      calendarSetting.setLocation(uiForm.getLocale()) ;
      calendarSetting.setTimeZone(uiForm.getTimeZone()) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(uiForm.getShowWorkingTimes()) {
        if(uiForm.getWorkingBeginTime().after(uiForm.getWorkingEndTime())) {
          uiApp.addMessage(new ApplicationMessage("UICalendarSettingForm.msg.working-time-logic", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        calendarSetting.setShowWorkingTime(uiForm.getShowWorkingTimes()) ;
        calendarSetting.setWorkingTimeBegin(uiForm.getWorkingBegin()) ;
        calendarSetting.setWorkingTimeEnd(uiForm.getWorkingEnd()) ;
      }
      List<String> defaultCalendars = new ArrayList<String>() ;
      List<UIComponent> children = ((UIFormInputWithActions)uiForm.getChildById("defaultCalendars")).getChildren() ;//UIFormInputWithActions("defaultCalendars"
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            defaultCalendars.add(((UIFormCheckBoxInput)child).getBindingField()) ;
          }
        }
      } 
      if(defaultCalendars.size() > 0)calendarSetting.setDefaultPrivateCalendars(defaultCalendars.toArray(new String[] {})) ;
      CalendarService cservice = CalendarUtils.getCalendarService() ;
      cservice.saveCalendarSetting(SessionsUtils.getSessionProvider(), Util.getPortalRequestContext().getRemoteUser(), calendarSetting) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.setCalendarSetting(calendarSetting) ;
      calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class).refresh() ;
      calendarPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UICalendarSettingForm> {
    public void execute(Event<UICalendarSettingForm> event) throws Exception {
      UICalendarSettingForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }
}
