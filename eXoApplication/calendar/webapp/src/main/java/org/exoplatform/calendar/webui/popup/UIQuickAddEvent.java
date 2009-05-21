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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.webui.CalendarView;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIFormComboBox;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
import org.exoplatform.calendar.webui.UIListContainer;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

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
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null).addValidator(MandatoryValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormDateTimePicker(FIELD_FROM, FIELD_FROM, new Date(), false).addValidator(MandatoryValidator.class));
    addUIFormInput(new UIFormDateTimePicker(FIELD_TO, FIELD_TO, new Date(), false).addValidator(MandatoryValidator.class));
    addUIFormInput(new UIFormComboBox(FIELD_FROM_TIME, FIELD_FROM_TIME, options));
    addUIFormInput(new UIFormComboBox(FIELD_TO_TIME, FIELD_TO_TIME, options));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ALLDAY, FIELD_ALLDAY, false));
    addUIFormInput(new UIFormSelectBoxWithGroups(FIELD_CALENDAR, FIELD_CALENDAR, null)) ;
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
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat ,locale) ;
    fromField.setValue(df.format(value)) ;
    DateFormat tf = new SimpleDateFormat(timeFormat, locale) ;
    timeFile.setValue(tf.format(value)) ;
  }
  private Date getEventFromDate(String dateFormat, String timeFormat) throws Exception {
    try {
      UIFormDateTimePicker fromField = getChildById(FIELD_FROM) ;
      UIFormComboBox timeFile = getChildById(FIELD_FROM_TIME) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      Locale locale = context.getParentAppRequestContext().getLocale() ;
      if(getIsAllDay()) {
        DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
        df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
        return CalendarUtils.getBeginDay(df.parse(fromField.getValue())).getTime();
      } 
      DateFormat df = new SimpleDateFormat(dateFormat + " "  + timeFormat, locale) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return df.parse(fromField.getValue() + " " + timeFile.getValue()) ;
    }
    catch (Exception e) {
      e.printStackTrace() ;
      return null ;
    }
  }

  private void setEventToDate(Date value,String dateFormat,  String timeFormat) {
    UIFormDateTimePicker toField =  getChildById(FIELD_TO) ;
    UIFormComboBox timeField =  getChildById(FIELD_TO_TIME) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    toField.setValue(df.format(value)) ;
    DateFormat tf = new SimpleDateFormat(timeFormat, locale) ;
    timeField.setValue(tf.format(value)) ;
  }
  private Date getEventToDate(String dateFormat, String timeFormat) throws Exception {
    try {
      UIFormDateTimePicker toField = getChildById(FIELD_TO) ;
      UIFormComboBox timeFile = getChildById(FIELD_TO_TIME) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      Locale locale = context.getParentAppRequestContext().getLocale() ;
      if(getIsAllDay()) {
        DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
        df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
        return CalendarUtils.getEndDay(df.parse(toField.getValue())).getTime();
      } 
      DateFormat df = new SimpleDateFormat(dateFormat + " " + timeFormat, locale) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return df.parse(toField.getValue() + " " + timeFile.getValue() ) ;
    } catch (Exception e) {
      e.printStackTrace() ;
      return null ;
    }
  }

  public List<SelectItem> getCalendars() throws Exception {
    return CalendarUtils.getCalendarOption() ;
  }

  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  private String getEventSummary() {
    return getUIStringInput(FIELD_EVENT).getValue() ;
  }
  private String getEventDescription() {return getUIFormTextAreaInput(FIELD_DESCRIPTION).getValue() ;}


  private boolean getIsAllDay() {
    return getUIFormCheckBoxInput(FIELD_ALLDAY).isChecked() ;
  }
  public void setIsAllday(boolean isChecked) {
    getUIFormCheckBoxInput(FIELD_ALLDAY).setChecked(isChecked) ;
  }
  private String getEventCalendar() {
    String values = getUIFormSelectBoxGroup(FIELD_CALENDAR).getValue() ;
    if(values != null && values.trim().length() > 0 && values.split(CalendarUtils.COLON).length > 0) {
      calType_ = values.split(CalendarUtils.COLON)[0] ;
      return values.split(CalendarUtils.COLON)[1] ;
    }
    return null ;

  }
  public void setSelectedCalendar(String value) {
    value = calType_ + CalendarUtils.COLON + value ;
    getUIFormSelectBoxGroup(FIELD_CALENDAR).setValue(value) ;
  }
  public UIFormSelectBoxWithGroups getUIFormSelectBoxGroup(String id) {
    return findComponentById(id) ;
  }
  public void setSelectedCategory(String value) {getUIFormSelectBox(FIELD_CATEGORY).setValue(value) ;}
  private String getEventCategory() {return getUIFormSelectBox(FIELD_CATEGORY).getValue() ;}
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  public void setEvent(boolean isEvent) { isEvent_ = isEvent ; }
  public boolean isEvent() { return isEvent_ ; }

  public void update(String calType, List<SelectItem> options) throws Exception{
    if(options != null) {
      getUIFormSelectBoxGroup(FIELD_CALENDAR).setOptions(options) ;
    }else {
      getUIFormSelectBoxGroup(FIELD_CALENDAR).setOptions(getCalendars()) ;
    } 
    calType_ = calType ;
  }
  static  public class SaveActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      UIQuickAddEvent uiForm = event.getSource() ;
      UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String summary = uiForm.getEventSummary().trim() ;
      if(!CalendarUtils.isNameValid(summary, CalendarUtils.SIMPLECHARACTER)){
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.summary-invalid", CalendarUtils.SIMPLECHARACTER, ApplicationMessage.WARNING) ) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String description = uiForm.getEventDescription() ;
      if(!CalendarUtils.isEmpty(description)) description = description.replaceAll(CalendarUtils.GREATER_THAN, "").replaceAll(CalendarUtils.SMALLER_THAN,"") ;
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
      if(from.after(to) || from.equals(to)) {
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.logic-required", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }  
      if(uiForm.getIsAllDay()) {
        java.util.Calendar tempCal = CalendarUtils.getInstanceTempCalendar() ;
        tempCal.setTime(to) ;
        tempCal.add(java.util.Calendar.MILLISECOND, -1) ;
        to = tempCal.getTime() ;
      }
      try {
        CalendarEvent calEvent = new CalendarEvent() ;
        calEvent.setSummary(summary) ;
        calEvent.setDescription(description) ;
        calEvent.setCalendarId(uiForm.getEventCalendar());
        String username = CalendarUtils.getCurrentUser() ;
        if(uiForm.isEvent_){ 
          calEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
          calEvent.setEventState(UIEventForm.ITEM_BUSY) ;
          calEvent.setParticipant(new String[]{username}) ;
          String emailAddress = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username).getEmail() ;
          if(CalendarUtils.isEmailValid(emailAddress)) {
            List<Reminder> reminders = new ArrayList<Reminder>() ;
            Reminder email = new Reminder(Reminder.TYPE_EMAIL) ;
            email.setReminderType(Reminder.TYPE_EMAIL) ;
            email.setAlarmBefore(5) ;
            email.setEmailAddress(emailAddress) ;
            email.setRepeate(Boolean.FALSE) ;
            email.setRepeatInterval(0) ;
            email.setFromDateTime(from) ;      
            reminders.add(email) ;
            calEvent.setReminders(reminders) ;
          }
          calEvent.setRepeatType(CalendarEvent.RP_NOREPEAT) ;
        } else {
          calEvent.setEventType(CalendarEvent.TYPE_TASK) ;
          calEvent.setEventState(CalendarEvent.NEEDS_ACTION) ;
        }
        calEvent.setEventCategoryId(uiForm.getEventCategory());
        //String eventCategoryName = CalendarUtils.getCalendarService().getEventCategory(SessionProviderFactory.createSessionProvider(), username, uiForm.getEventCategory()).getName() ;
        //calEvent.setEventCategoryName(eventCategoryName) ;
        UIFormSelectBox selectBox = (UIFormSelectBox)uiForm.getChildById(FIELD_CATEGORY) ;
        for (SelectItemOption<String> o : selectBox.getOptions()) {
          if (o.getValue().equals(selectBox.getValue())) {
            calEvent.setEventCategoryName(o.getLabel()) ;
            break ;
          }
        }
        
        calEvent.setFromDateTime(from);
        calEvent.setToDateTime(to) ;
        calEvent.setCalType(uiForm.calType_) ;
        if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
          CalendarUtils.getCalendarService().saveUserEvent(username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
          CalendarUtils.getCalendarService().saveEventToSharedCalendar(username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
          CalendarUtils.getCalendarService().savePublicEvent(calEvent.getCalendarId(), calEvent, true) ;          
        }
        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
        uiPopupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        UICalendarViewContainer uiContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class);
        UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
        CalendarView calendarView = (CalendarView)uiContainer.getRenderedChild() ;
       
        // hung.hoang
        /* 
        if (calendarView instanceof UIListContainer) {
          ((UIListContainer)calendarView).setDisplaySearchResult(false) ;
        }
        */
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
        uiEventForm.update(uiForm.calType_, uiForm.getUIFormSelectBoxGroup(FIELD_CALENDAR).getOptions()) ;
        uiEventForm.initForm(calendarSetting, null, null) ;
        uiEventForm.setEventSumary(uiForm.getEventSummary()) ;
        uiEventForm.setEventDescription(uiForm.getEventDescription()) ;
        uiEventForm.setEventFromDate(uiForm.getEventFromDate(dateFormat, timeFormat),dateFormat, timeFormat) ;
        Date to = uiForm.getEventToDate(dateFormat, timeFormat) ;
        if(uiForm.getIsAllDay()) {
          java.util.Calendar tempCal = CalendarUtils.getInstanceTempCalendar() ;
          tempCal.setTime(to) ;
          tempCal.add(java.util.Calendar.MILLISECOND, -1) ;
          to = tempCal.getTime() ;
        }
        uiEventForm.setEventToDate(to,dateFormat, timeFormat) ;
        uiEventForm.setEventAllDate(uiForm.getIsAllDay()) ;
        uiEventForm.setSelectedCategory(uiForm.getEventCategory()) ;
        String username = CalendarUtils.getCurrentUser() ;
        uiEventForm.setSelectedEventState(UIEventForm.ITEM_BUSY) ;
//      TODO cs-839
        uiEventForm.setParticipant(username) ;
        uiEventForm.setParticipantStatus(username) ;
        uiEventForm.getChild(UIEventShareTab.class).setParticipantStatusList(uiEventForm.getParticipantStatusList());
        uiEventForm.setEmailAddress(CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username).getEmail()) ;
        uiEventForm.setEmailRemindBefore(String.valueOf(5));
        uiEventForm.setEmailReminder(true) ;
        uiEventForm.setEmailRepeat(false) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      } else {
        uiPopupAction.deActivate() ;
        UIPopupContainer uiPouContainer  = uiPopupAction.activate(UIPopupContainer.class, 700) ;
        uiPouContainer.setId(UIPopupContainer.UITASKPOPUP) ;
        UITaskForm uiTaskForm = uiPouContainer.addChild(UITaskForm.class, null, null) ;
        uiTaskForm.update(uiForm.calType_, uiForm.getUIFormSelectBoxGroup(FIELD_CALENDAR).getOptions()) ;
        uiTaskForm.initForm(calendarSetting, null, null) ;
        Date to = uiForm.getEventToDate(dateFormat, timeFormat) ;
        if(uiForm.getIsAllDay()) {
          java.util.Calendar tempCal = CalendarUtils.getInstanceTempCalendar() ;
          tempCal.setTime(to) ;
          tempCal.add(java.util.Calendar.MILLISECOND, -1) ;
          to = tempCal.getTime() ;
        }
        uiTaskForm.setEventSumary(uiForm.getEventSummary()) ;
        uiTaskForm.setEventDescription(uiForm.getEventDescription()) ;
        uiTaskForm.setEventFromDate(uiForm.getEventFromDate(dateFormat, timeFormat),dateFormat, timeFormat) ;
        uiTaskForm.setEventToDate(to, dateFormat, timeFormat) ;
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
