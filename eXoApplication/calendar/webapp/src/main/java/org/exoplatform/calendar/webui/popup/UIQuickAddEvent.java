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
package org.exoplatform.calendar.webui.popup;

import java.security.acl.Group;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.CalendarView;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIFormComboBox;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
import org.exoplatform.calendar.webui.UIListContainer;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
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
import org.hsqldb.lib.ArrayCounter;

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
  final public static String UIQUICKADDTASK = "UIQuickAddTask".intern() ;

  private String calType_ = "0".intern() ;
  private boolean isEvent_ = true ;
  public UIQuickAddEvent() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null).addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormDateTimePicker(FIELD_FROM, FIELD_FROM, new Date(), false).addValidator(EmptyFieldValidator.class));
    addUIFormInput(new UIFormDateTimePicker(FIELD_TO, FIELD_TO, new Date(), false).addValidator(EmptyFieldValidator.class));
    addUIFormInput(new UIFormComboBox(FIELD_FROM_TIME, FIELD_FROM_TIME, options));
    addUIFormInput(new UIFormComboBox(FIELD_TO_TIME, FIELD_TO_TIME, options));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ALLDAY, FIELD_ALLDAY, false));
    addUIFormInput(new UIFormSelectBox(FIELD_CALENDAR, FIELD_CALENDAR, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, UIEventForm.getCategory())) ;
  }

  public UIFormComboBox getUIFormCombobox(String name) {
    return  findComponentById(name) ;
  }

  public void init(CalendarSetting  calendarSetting, String startTime, String endTime) throws Exception {
    List<SelectItemOption<String>> fromOptions 
    = CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat(),calendarSetting.getTimeFormat(), calendarSetting.getTimeInterval()) ;
    List<SelectItemOption<String>> toOptions 
    = CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat(),calendarSetting.getTimeFormat(),  calendarSetting.getTimeInterval()) ;
    UIFormDateTimePicker fromField = getChildById(FIELD_FROM) ;
    fromField.setDateFormatStyle(calendarSetting.getDateFormat()) ;
    UIFormDateTimePicker toField = getChildById(FIELD_TO) ;
    toField.setDateFormatStyle(calendarSetting.getDateFormat()) ;
    getUIFormCombobox(FIELD_FROM_TIME).setOptions(fromOptions) ;
    getUIFormCombobox(FIELD_TO_TIME).setOptions(toOptions) ;
    UIMiniCalendar miniCalendar = getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
    java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
    cal.setTime(miniCalendar.getCurrentCalendar().getTime());
    if(startTime != null) {
      cal.setTimeInMillis(Long.parseLong(startTime)) ;
    } 
    Long begingMinute = (cal.get(java.util.Calendar.MINUTE)/calendarSetting.getTimeInterval())*calendarSetting.getTimeInterval() ;
    cal.set(java.util.Calendar.MINUTE, begingMinute.intValue()) ;
    setEventFromDate(cal.getTime(),calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()) ;
    if(endTime != null ) cal.setTimeInMillis(Long.parseLong(endTime)) ; 
    else {
      cal.add(java.util.Calendar.MINUTE, (int)calendarSetting.getTimeInterval()*2) ;
    }
    setEventToDate(cal.getTime(),calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()) ;
  }
  private void setEventFromDate(Date value, String dateFormat, String timeFormat) {
    UIFormDateTimePicker fromField = getChildById(FIELD_FROM) ;
    UIFormComboBox timeFile = getChildById(FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat(dateFormat) ;
    fromField.setValue(df.format(value)) ;
    DateFormat tf = new SimpleDateFormat(timeFormat) ;
    timeFile.setValue(tf.format(value)) ;
  }
  private Date getEventFromDate(String dateFormat, String timeFormat) throws Exception {
    try {
      UIFormDateTimePicker fromField = getChildById(FIELD_FROM) ;
      UIFormComboBox timeFile = getChildById(FIELD_FROM_TIME) ;
      if(getIsAllDay()) {
        DateFormat df = new SimpleDateFormat(dateFormat) ;
        df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
        return CalendarUtils.getBeginDay(df.parse(fromField.getValue())).getTime();
      } 
      DateFormat df = new SimpleDateFormat(dateFormat + " "  + timeFormat) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return df.parse(fromField.getValue() + " " + timeFile.getValue() ) ;
    }
    catch (Exception e) {
      e.printStackTrace() ;
      return null ;
    }
  }

  private void setEventToDate(Date value,String dateFormat,  String timeFormat) {
    UIFormDateTimePicker toField =  getChildById(FIELD_TO) ;
    UIFormComboBox timeField =  getChildById(FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat(dateFormat) ;
    toField.setValue(df.format(value)) ;
    DateFormat tf = new SimpleDateFormat(timeFormat) ;
    timeField.setValue(tf.format(value)) ;
  }
  private Date getEventToDate(String dateFormat, String timeFormat) throws Exception {
    try {
      UIFormDateTimePicker toField = getChildById(FIELD_TO) ;
      UIFormComboBox timeFile = getChildById(FIELD_TO_TIME) ;
      if(getIsAllDay()) {
        DateFormat df = new SimpleDateFormat(dateFormat) ;
        df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
        df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
        return CalendarUtils.getEndDay(df.parse(toField.getValue())).getTime();
      } 
      DateFormat df = new SimpleDateFormat(dateFormat + " " + timeFormat) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return df.parse(toField.getValue() + " " + timeFile.getValue() ) ;
    } catch (Exception e) {
      e.printStackTrace() ;
      return null ;
    }
  }

  public List<SelectItemOption<String>> getCalendars() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    options.add(new SelectItemOption<String>(CalendarUtils.PRIVATE_CALENDARS, "")) ;
    List<Calendar> calendars = calendarService.getUserCalendars(SessionProviderFactory.createSessionProvider(), username, true) ;
    for(Calendar c : calendars) {
      options.add(new SelectItemOption<String>(CalendarUtils.DOUBLESCORE  + c.getName(), CalendarUtils.PRIVATE_TYPE + ":" + c.getId())) ;
    }

    GroupCalendarData gcd = calendarService.getSharedCalendars(SessionProviderFactory.createSystemProvider(), username, true);
    if(gcd != null) {
      options.add(new SelectItemOption<String>(CalendarUtils.SHARED_CALENDARS, "")) ;
      for(Calendar c : gcd.getCalendars()) {
        if(Arrays.asList(c.getEditPermission()).contains(username)){
          options.add(new SelectItemOption<String>(CalendarUtils.DOUBLESCORE  + c.getName(), CalendarUtils.SHARED_TYPE + CalendarUtils.COLON + c.getId())) ;
        }
      }
    }

    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(SessionProviderFactory.createSystemProvider(), CalendarUtils.getUserGroups(username), false, username) ;
    if(lgcd != null) {
      options.add(new SelectItemOption<String>(CalendarUtils.PUBLIC_CALENDARS, "")) ;
      for(GroupCalendarData g : lgcd) {
        for(Calendar c : g.getCalendars()){
          if(c != null && c.getEditPermission() != null && Arrays.asList(c.getEditPermission()).contains(username)){
            options.add(new SelectItemOption<String>(CalendarUtils.DOUBLESCORE + c.getName(), CalendarUtils.PUBLIC_TYPE + CalendarUtils.COLON + c.getId())) ;
          }
        }

      }
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
  private String getEventSummary() {
    return getUIStringInput(FIELD_EVENT).getValue() ;
  }
  private String getEventDescription() {return getUIFormTextAreaInput(FIELD_DESCRIPTION).getValue() ;}


  private boolean getIsAllDay() {
    return getUIFormCheckBoxInput(FIELD_ALLDAY).isChecked() ;
  }
  private String getEventCalendar() {
    String values = getUIFormSelectBox(FIELD_CALENDAR).getValue() ;
    if(values != null && values.trim().length() > 0 && values.split(CalendarUtils.COLON).length > 0) {
      calType_ = values.split(CalendarUtils.COLON)[0] ;
      return values.split(CalendarUtils.COLON)[1] ;
    }
    return null ;

  }
  public void setSelectedCalendar(String value) {
    value = calType_ + CalendarUtils.COLON + value ;
    getUIFormSelectBox(FIELD_CALENDAR).setValue(value) ;
  }
  public void setSelectedCategory(String value) {getUIFormSelectBox(FIELD_CATEGORY).setValue(value) ;}
  private String getEventCategory() {return getUIFormSelectBox(FIELD_CATEGORY).getValue() ;}
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  public void setEvent(boolean isEvent) { isEvent_ = isEvent ; }
  public boolean isEvent() { return isEvent_ ; }

  public void update(String calType, List<SelectItemOption<String>> options) throws Exception{
    if(options != null) {
      getUIFormSelectBox(FIELD_CALENDAR).setOptions(options) ;
    }else {
      getUIFormSelectBox(FIELD_CALENDAR).setOptions(getCalendars()) ;
    } 
    calType_ = calType ;
  }
  static  public class SaveActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      UIQuickAddEvent uiForm = event.getSource() ;
      UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
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
      Date from = uiForm.getEventFromDate(uiPortlet.getCalendarSetting().getDateFormat() ,uiPortlet.getCalendarSetting().getTimeFormat()) ;
      Date to = uiForm.getEventToDate(uiPortlet.getCalendarSetting().getDateFormat(), uiPortlet.getCalendarSetting().getTimeFormat()) ;
      if(from == null) {
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.fromDate-format", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(to == null) {
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.toDate-format", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(from.after(to)) {
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.logic-required", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if(from.equals(to)) {
        to = CalendarUtils.getEndDay(from).getTime() ;
      } 
      if(uiForm.getIsAllDay()) {
        java.util.Calendar tempCal = CalendarUtils.getInstanceTempCalendar() ;
        tempCal.setTime(to) ;
        tempCal.add(java.util.Calendar.MILLISECOND, -1) ;
        to = tempCal.getTime() ;
      }
      try {
        CalendarEvent calEvent = new CalendarEvent() ;
        calEvent.setSummary(uiForm.getEventSummary()) ;
        calEvent.setDescription(uiForm.getEventDescription()) ;
        calEvent.setCalendarId(uiForm.getEventCalendar());
        if(uiForm.isEvent_){ 
          calEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
        } else {
          calEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        }
        calEvent.setEventCategoryId(uiForm.getEventCategory());
        calEvent.setFromDateTime(from);
        calEvent.setToDateTime(to) ;
        calEvent.setCalType(uiForm.calType_) ;
        String username = CalendarUtils.getCurrentUser() ;
        if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
          CalendarUtils.getCalendarService().saveUserEvent(SessionProviderFactory.createSessionProvider(), username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
          CalendarUtils.getCalendarService().saveEventToSharedCalendar(SessionProviderFactory.createSystemProvider(), username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
          CalendarUtils.getCalendarService().savePublicEvent(SessionProviderFactory.createSystemProvider(), calEvent.getCalendarId(), calEvent, true) ;          
        }
        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
        uiPopupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        UICalendarViewContainer uiContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class);
        UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
        uiMiniCalendar.updateMiniCal() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
        CalendarView calendarView = (CalendarView)uiContainer.getRenderedChild() ;
        if (calendarView instanceof UIListContainer) {
          ((UIListContainer)calendarView).setDisplaySearchResult(false) ;
        }
        calendarView.setLastUpdatedEventId(calEvent.getId()) ;
        uiContainer.refresh() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
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
      CalendarSetting calendarSetting = 
        uiForm.getAncestorOfType(UICalendarPortlet.class).getCalendarSetting() ;
      String dateFormat = calendarSetting.getDateFormat() ;
      String timeFormat = calendarSetting.getTimeFormat() ;
      
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      if(uiForm.isEvent()) {
        uiPopupAction.deActivate() ;
        UIPopupContainer uiPouContainer = uiPopupAction.activate(UIPopupContainer.class, 700) ;
        uiPouContainer.setId(UIPopupContainer.UIEVENTPOPUP) ;
        UIEventForm uiEventForm = uiPouContainer.addChild(UIEventForm.class, null, null) ;
        uiEventForm.update(uiForm.calType_, uiForm.getUIFormSelectBox(FIELD_CALENDAR).getOptions()) ;
        uiEventForm.initForm(calendarSetting, null, null) ;
        uiEventForm.setEventSumary(uiForm.getEventSummary()) ;
        uiEventForm.setEventDescription(uiForm.getEventDescription()) ;
        uiEventForm.setEventFromDate(uiForm.getEventFromDate(dateFormat, timeFormat),dateFormat, timeFormat) ;
        uiEventForm.setEventToDate(uiForm.getEventToDate(dateFormat, timeFormat),dateFormat, timeFormat) ;
        uiEventForm.setEventAllDate(uiForm.getIsAllDay()) ;
        uiEventForm.setSelectedCategory(uiForm.getEventCategory()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      } else {
        uiPopupAction.deActivate() ;
        UIPopupContainer uiPouContainer  = uiPopupAction.activate(UIPopupContainer.class, 700) ;
        uiPouContainer.setId(UIPopupContainer.UITASKPOPUP) ;
        UITaskForm uiTaskForm = uiPouContainer.addChild(UITaskForm.class, null, null) ;
        uiTaskForm.update(uiForm.calType_, uiForm.getUIFormSelectBox(FIELD_CALENDAR).getOptions()) ;
        uiTaskForm.initForm(calendarSetting, null, null) ;
        uiTaskForm.setEventSumary(uiForm.getEventSummary()) ;
        uiTaskForm.setEventDescription(uiForm.getEventDescription()) ;
        uiTaskForm.setEventFromDate(uiForm.getEventFromDate(dateFormat, timeFormat),dateFormat, timeFormat) ;
        uiTaskForm.setEventToDate(uiForm.getEventToDate(dateFormat, timeFormat), dateFormat, timeFormat) ;
        uiTaskForm.setEventAllDate(uiForm.getIsAllDay()) ;
        uiTaskForm.setSelectedCategory(uiForm.getEventCategory()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }
  }
  static  public class CancelActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {    	
      event.getSource().getAncestorOfType(UICalendarPortlet.class).cancelAction() ;
    }
  }


}
