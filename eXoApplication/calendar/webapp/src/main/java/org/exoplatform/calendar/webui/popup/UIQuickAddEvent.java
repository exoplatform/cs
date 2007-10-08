/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIPopup/UIQuickAddEvent.gtmpl",
    events = {
      @EventConfig(listeners = UIQuickAddEvent.SaveActionListener.class),
      @EventConfig(listeners = UIQuickAddEvent.MoreDetailActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIQuickAddEvent.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIQuickAddEvent extends UIForm implements UIPopupComponent{

  final public static String FIELD_EVENT = "eventName".intern() ;
  final public static String FIELD_CALENDAR = "calendar".intern() ;
  final public static String FIELD_CATEGORY = "category".intern() ;
  final public static String FIELD_FROM = "from".intern() ;
  final public static String FIELD_TO = "to".intern() ;
  final public static String FIELD_FROM_TIME = "fromTime".intern() ;
  final public static String FIELD_TO_TIME = "toTime".intern() ;
  final public static String FIELD_ALLDAY = "allDay".intern() ;
  final public static String FIELD_DESCRIPTION = "description".intern() ;
  final public static String TIME_PATTERNS_12 ="hh:mm a" ;
  final public static String TIME_PATTERNS_24 ="HH:mm" ;

  final public static String UIQUICKADDTASK = "UIQuickAddTask".intern() ;
  
  private int timeInterval_ = 15 ;
  private String timeFormat_ = TIME_PATTERNS_12 ;
  private boolean isEvent_ = true ;

  public UIQuickAddEvent() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null).addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormDateTimeInput(FIELD_FROM, FIELD_FROM, new Date(), false).addValidator(EmptyFieldValidator.class));
    addUIFormInput(new UIFormDateTimeInput(FIELD_TO, FIELD_TO, new Date(), false).addValidator(EmptyFieldValidator.class));
    addUIFormInput(new UIFormSelectBox(FIELD_FROM_TIME, FIELD_FROM_TIME, getTimes()));
    addUIFormInput(new UIFormSelectBox(FIELD_TO_TIME, FIELD_TO_TIME, getTimes()));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ALLDAY, FIELD_ALLDAY, false));
    addUIFormInput(new UIFormSelectBox(FIELD_CALENDAR, FIELD_CALENDAR, getCalendar())) ;
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, UIEventForm.getCategory())) ;
    init() ;
  }

  public void init() {
    java.util.Calendar cal = GregorianCalendar.getInstance() ;
    int beginMinute = (cal.get(java.util.Calendar.MINUTE)/timeInterval_)* timeInterval_ ;
    cal.set(java.util.Calendar.MINUTE, beginMinute) ;
    setEventFromDate(cal.getTime()) ;
    cal.add(java.util.Calendar.MINUTE, timeInterval_) ;
    setEventToDate(cal.getTime()) ;
  }
  public void init(String startTime, String endTime) throws Exception {
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm") ;
    try {
      setEventFromDate(df.parse(startTime)) ;
      setEventToDate(df.parse(endTime)) ;
    } catch (Exception e) {
      init() ;
      e.printStackTrace() ;
    }
  }

  private List<SelectItemOption<String>> getCalendar() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<Calendar> calendars = calendarService.getUserCalendars(username) ;
    for(Calendar c : calendars) {
      options.add(new SelectItemOption<String>(c.getName(), c.getId())) ;
    }
    return options ;
  }

  public String getLabel(String id) {
    String label = id ;
    try {
      label = super.getLabel(id) ;
    } catch (Exception e) {
    }
    return label ;
  }
  private List<SelectItemOption<String>> getTimes() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    GregorianCalendar cal = new GregorianCalendar(Locale.US) ;
    cal.set(java.util.Calendar.HOUR, 0) ;
    cal.set(java.util.Calendar.MINUTE, 0) ;
    DateFormat df = new SimpleDateFormat(timeFormat_) ;
    int time = 0 ;
    while (time ++ < 24*60/(timeInterval_)) {
      options.add(new SelectItemOption<String>(df.format(cal.getTime()), df.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, timeInterval_) ;
    }
    return options ;
  }
  private String getEventSummary() {
    return getUIStringInput(FIELD_EVENT).getValue() ;
  }
  private void setEventSummay(String value) {
    getUIStringInput(FIELD_EVENT).setValue(value) ;
  }
  private String getEventDescription() {return getUIFormTextAreaInput(FIELD_DESCRIPTION).getValue() ;}
  private void setEventDescription(String value) { getUIFormTextAreaInput(FIELD_DESCRIPTION).setValue(value) ;}

  private Date getEventFromDate() {
    DateFormat df = SimpleDateFormat.getInstance() ;
    String dateString = getUIFormDateTimeInput(FIELD_FROM).getValue() + " " + getUIFormSelectBox(FIELD_FROM_TIME).getValue() ;
    try {
      return df.parse(dateString) ;
    } catch (Exception e) {
      return null ;
    }
  }
  private void setEventFromDate(Date value) {
    UIFormDateTimeInput fromField = getChildById(FIELD_FROM) ;
    UIFormSelectBox timeFile = getChildById(FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy") ;
    fromField.setValue(df.format(value)) ;
    df = new SimpleDateFormat(timeFormat_) ;
    timeFile.setValue(df.format(value)) ;
  }

  private Date getEventToDate() {
    DateFormat df = SimpleDateFormat.getInstance() ;
    String dateString = getUIFormDateTimeInput(FIELD_TO).getValue() + " " + getUIFormSelectBox(FIELD_TO_TIME).getValue() ;
    try {
      return df.parse(dateString) ;
    } catch (Exception e) {
      return null ;
    }
  }
  private void setEventToDate(Date value) {
    UIFormDateTimeInput toField =  getChildById(FIELD_TO) ;
    UIFormSelectBox timeField =  getChildById(FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy") ;
    toField.setValue(df.format(value)) ;
    df = new SimpleDateFormat(timeFormat_) ;
    timeField.setValue(df.format(value)) ;

  }

  private boolean getIsAllDay() {
    return getUIFormCheckBoxInput(FIELD_ALLDAY).isChecked() ;
  }
  private void setIsAllDay(boolean value) {getUIFormCheckBoxInput(FIELD_ALLDAY).setChecked(value);} 

  private String getEventCalendar() {return getUIFormSelectBox(FIELD_CALENDAR).getValue() ;}
  private void setSelectedCalendar(String value) {getUIFormSelectBox(FIELD_CALENDAR).setValue(value) ;}
  private String getEventCategory() {return getUIFormSelectBox(FIELD_CATEGORY).getValue() ;}
  private void setSelectedEventCategory(String value) {getUIFormSelectBox(FIELD_CATEGORY).setValue(value) ;}

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
  }
  public void setEvent(boolean isEvent) {
    isEvent_ = isEvent;
  }

  public boolean isEvent() {
    return isEvent_;
  }
  static  public class SaveActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      System.out.println("SaveActionListener");
      UIQuickAddEvent uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(CalendarUtils.isEmpty(uiForm.getEventCalendar())) {
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.calendar-field-required", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(CalendarUtils.isEmpty(uiForm.getEventCategory())) {
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.category-field-required", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      java.util.Calendar cal = GregorianCalendar.getInstance() ;
      Date fromDate = uiForm.getEventFromDate() ;
      Date toDate = uiForm.getEventToDate() ;
      if(uiForm.getIsAllDay()) {
        cal.setTime(fromDate) ;
        cal.set(java.util.Calendar.HOUR, cal.getActualMinimum(java.util.Calendar.HOUR_OF_DAY)) ;
        cal.set(java.util.Calendar.MINUTE, cal.getActualMinimum(java.util.Calendar.MINUTE)) ;
        fromDate = cal.getTime() ;
        cal.add(java.util.Calendar.DATE, 1) ;
        toDate = cal.getTime() ;
      }
      if(fromDate.equals(toDate) || fromDate.after(toDate)) {
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.logic-required", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      try {
        CalendarService calService = uiForm.getApplicationComponent(CalendarService.class) ;
        String username = event.getRequestContext().getRemoteUser() ;
        String calendarId = uiForm.getEventCalendar() ;
        CalendarEvent calEvent = new CalendarEvent() ;
        calEvent.setSummary(uiForm.getEventSummary()) ;
        calEvent.setDescription(uiForm.getEventDescription()) ;
        calEvent.setCalendarId(calendarId);
        if(uiForm.isEvent_){ 
          calEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
        } else {
          calEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        }
        calEvent.setEventCategoryId(uiForm.getEventCategory());
        calEvent.setFromDateTime(fromDate);
        calEvent.setToDateTime(toDate) ;
        calService.saveUserEvent(username, calendarId, calEvent, true) ;

        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
        uiPopupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        UICalendarViewContainer uiContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class);
        uiContainer.refresh() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-successfully", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-unsuccessfully", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }
  static  public class MoreDetailActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      UIQuickAddEvent uiForm = event.getSource() ;
      if(uiForm.isEvent()) {
        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
        uiPopupAction.deActivate() ;
        UIPopupContainer uiPouContainer  = uiPopupAction.activate(UIPopupContainer.class, 700) ;
        UIEventForm uiEventForm = uiPouContainer.addChild(UIEventForm.class, null, null) ;
        uiEventForm.setEventSumary(uiForm.getEventSummary()) ;
        uiEventForm.setEventDescription(uiForm.getEventDescription()) ;
        uiEventForm.setEventFromDate(uiForm.getEventFromDate()) ;
        uiEventForm.setEventToDate(uiForm.getEventToDate()) ;
        uiEventForm.setEventAllDate(uiForm.getIsAllDay()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      } else {
        System.out.println("\n\n add detail task here");
      }
    }
  }
  static  public class CancelActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      event.getSource().getAncestorOfType(UICalendarPortlet.class).cancelAction() ;
    }
  }


}
