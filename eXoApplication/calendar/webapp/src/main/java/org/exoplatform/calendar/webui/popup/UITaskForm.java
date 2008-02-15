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

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.webui.CalendarView;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIFormComboBox;
import org.exoplatform.calendar.webui.UIListContainer;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormTabPane;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
    events = {
      @EventConfig(listeners = UITaskForm.SaveActionListener.class),
      @EventConfig(listeners = UITaskForm.AddCategoryActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.AddEmailAddressActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.AddAttachmentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.RemoveAttachmentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.SelectUserActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UITaskForm extends UIFormTabPane implements UIPopupComponent, UISelector{
  final public static String TAB_TASKDETAIL = "eventDetail".intern() ;
  final public static String TAB_TASKREMINDER = "eventReminder".intern() ;
  final public static String ITEM_PUBLIC = "public".intern() ;
  final public static String ITEM_PRIVATE = "private".intern() ;
  final public static String ITEM_AVAILABLE = "available".intern() ;
  final public static String ITEM_BUSY = "busy".intern() ;
  final public static String ITEM_REPEAT = "true".intern() ;
  final public static String ITEM_UNREPEAT = "false".intern() ;
  final public static String ACT_REMOVE = "RemoveAttachment".intern() ;
  final public static String ACT_ADDEMAIL = "AddEmailAddress".intern() ;
  final public static String ACT_ADDCATEGORY = "AddCategory".intern() ;
  final public static String ACT_SELECTUSER = "SelectUser".intern() ;

  private boolean isAddNew_ = true ;
  private CalendarEvent calendarEvent_ = null ;
  private String errorMsg_ = null ;
  private String calType_ = "0" ;
  public UITaskForm() throws Exception {
    super("UIEventForm");
    UITaskDetailTab uiTaskDetailTab =  new UITaskDetailTab(TAB_TASKDETAIL) ;
    addChild(uiTaskDetailTab) ;
    UIEventReminderTab eventReminderTab =  new UIEventReminderTab(TAB_TASKREMINDER) ;
    addChild(eventReminderTab) ;
    setSelectedTab(uiTaskDetailTab.getId()) ;
  }
  public String getLabel(String id) {
    String label = id ;
    try {
      label = super.getLabel(id) ;
    } catch (Exception e) {
    }
    return label ;
  }
  public void reset() {
    super.reset() ;
    calendarEvent_ = null;
  }
  private SessionProvider getSession() {
    return SessionProviderFactory.createSessionProvider() ;
  }
  private SessionProvider getSystemSession() {
    return SessionProviderFactory.createSystemProvider() ;
  }
  public void initForm(CalendarSetting calSetting, CalendarEvent eventCalendar) throws Exception {
    reset() ;
    UITaskDetailTab taskDetailTab = getChildById(TAB_TASKDETAIL) ;
    List<SelectItemOption<String>> fromTimes 
      = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(), calSetting.getTimeFormat(), calSetting.getTimeInterval()) ;
    List<SelectItemOption<String>> toTimes 
      = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(), calSetting.getTimeFormat(), calSetting.getTimeInterval()) ;
    taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_FROM_TIME).setOptions(fromTimes) ;
    taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_TO_TIME).setOptions(toTimes) ;
    if(eventCalendar != null) {
      isAddNew_ = false ;
      calendarEvent_ = eventCalendar ;
      setEventSumary(eventCalendar.getSummary()) ;
      setEventDescription(eventCalendar.getDescription()) ;
      setEventAllDate(CalendarUtils.isAllDayEvent(eventCalendar)) ;
      setEventFromDate(eventCalendar.getFromDateTime(), calSetting.getTimeFormat()) ;
      setEventToDate(eventCalendar.getToDateTime(), calSetting.getTimeFormat()) ;
      setSelectedCalendarId(eventCalendar.getCalendarId()) ;
      setSelectedCategory(eventCalendar.getEventCategoryId()) ;
      setEventDelegation(eventCalendar.getTaskDelegator()) ;
      setSelectedEventPriority(eventCalendar.getPriority()) ;
      setEventReminders(eventCalendar.getReminders()) ;
      setAttachments(eventCalendar.getAttachment()) ;
      taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CALENDAR).setEnable(false) ;
      if(CalendarUtils.SHARED_TYPE.equals(calType_)) {
        taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setRendered(false) ;
      }
    } else {
      UIMiniCalendar miniCalendar = getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
      java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
      cal.setTime(miniCalendar.getCurrentCalendar().getTime()) ;
      int beginMinute = (cal.get(java.util.Calendar.MINUTE)/CalendarUtils.DEFAULT_TIMEITERVAL)*CalendarUtils.DEFAULT_TIMEITERVAL ;
      cal.set(java.util.Calendar.MINUTE, beginMinute) ;
      setEventFromDate(cal.getTime(), calSetting.getTimeFormat()) ;
      cal.add(java.util.Calendar.MINUTE, CalendarUtils.DEFAULT_TIMEITERVAL*2) ;
      setEventToDate(cal.getTime(), calSetting.getTimeFormat()) ;
    }
  }

  public static List<SelectItemOption<String>> getCategory() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<EventCategory> eventCategories = calendarService.getEventCategories(SessionProviderFactory.createSessionProvider(), Util.getPortalRequestContext().getRemoteUser()) ;
    for(EventCategory category : eventCategories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getName())) ;
    }
    return options ;
  }

  protected void refreshCategory()throws Exception {
    UIFormInputWithActions taskDetailTab = getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setOptions(getCategory()) ;
  }
  protected String getStatus() {
    UITaskDetailTab uiTaskDetailTab = getChildById(TAB_TASKDETAIL) ;
    return uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_STATUS).getValue() ;
  }
  protected void setStatus(String value) {
    UITaskDetailTab uiTaskDetailTab = getChildById(TAB_TASKDETAIL) ;
    uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_STATUS).setValue(value) ;
  }
  public String[] getActions() {
    return new String[]{"AddAttachment","Save", "Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void updateSelect(String selectField, String value) throws Exception {
    getUIStringInput(selectField).setValue(value) ;
  }

  protected boolean isEventDetailValid(CalendarSetting calendarSetting){
    if(CalendarUtils.isEmpty(getEventSumary())) {
      errorMsg_ = getId() + ".msg.event-summary-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getCalendarId())) {
      errorMsg_ = getId() + ".msg.event-calendar-required" ;
      return false ;
    } 
    if(CalendarUtils.isEmpty(getEventCategory())) {
      errorMsg_ = getId() + ".msg.event-category-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getEventFormDateValue())) {
      errorMsg_ = getId() + ".msg.event-fromdate-required" ;
      return false ;
    } 
    if(!getEventAllDate()) {
      if(CalendarUtils.isEmpty(getEventToDateValue())){
        errorMsg_ = getId() + ".msg.event-todate-required" ;
        return false ;
      } 
    }
    try {
      getEventFromDate(calendarSetting.getTimeFormat()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    try {
      getEventToDate(calendarSetting.getTimeFormat()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    if(getEmailReminder() && CalendarUtils.isEmpty(getEmailAddress())) {
      errorMsg_ = getId() + ".msg.event-email-required" ;
      return false ;
    } 
    errorMsg_ = null ;
    return true ;
  }
  protected String getEventSumary() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_EVENT).getValue() ;
  }
  protected void setEventSumary(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_EVENT).setValue(value) ;
  }
  protected String getEventDescription() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormTextAreaInput(UITaskDetailTab.FIELD_DESCRIPTION).getValue() ;
  }
  protected void setEventDescription(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormTextAreaInput(UITaskDetailTab.FIELD_DESCRIPTION).setValue(value) ;
  }
  protected String getCalendarId() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CALENDAR).getValue() ;
  }
  public void setSelectedCalendarId(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CALENDAR).setValue(value) ;
  }

  protected String getEventCategory() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).getValue() ;
  }
  public void setSelectedCategory(String value) {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setValue(value) ;
  }

  protected Date getEventFromDate(String timeFormat) throws Exception {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormComboBox timeField = taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_FROM_TIME) ;
    UIFormDateTimeInput fromField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM) ;
    if(getEventAllDate()) {
      DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return CalendarUtils.getBeginDay(df.parse(fromField.getValue())).getTime();
    } 
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT + " "  + timeFormat) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    return df.parse(fromField.getValue() + " " + timeField.getValue()) ;
  }
  protected String getEventFormDateValue () {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormDateTimeInput fromField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM) ;
    return fromField.getValue() ;
  }
  protected void setEventFromDate(Date date, String timeFormat) throws Exception{
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    ((UIFormDateTimeInput)taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM))
    .setValue(CalendarUtils.parse(date, CalendarUtils.DATEFORMAT)) ;
    taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_FROM_TIME)
    .setValue(CalendarUtils.parse(date,timeFormat)) ;    

  }

  protected Date getEventToDate(String timeFormat) throws Exception {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormComboBox timeField = taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_TO_TIME) ;
    UIFormDateTimeInput toField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO) ;
    if(getEventAllDate()) {
      DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return CalendarUtils.getEndDay(df.parse(toField.getValue())).getTime();
    } 
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT + " " + timeFormat) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    return df.parse(toField.getValue() + " " + timeField.getValue()) ;
  }
  protected String getEventToDateValue () {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormDateTimeInput toField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO) ;
    return toField.getValue() ;
  }
  protected void setEventToDate(Date date, String timeFormat) throws Exception{
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    ((UIFormDateTimeInput)taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO))
    .setValue(CalendarUtils.parse(date, CalendarUtils.DATEFORMAT)) ;
    taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_TO_TIME)
    .setValue(CalendarUtils.parse(date, timeFormat)) ; 
  }

  protected boolean getEventAllDate() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormCheckBoxInput(UITaskDetailTab.FIELD_CHECKALL).isChecked() ;
  }
  protected void setEventAllDate(boolean isCheckAll) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormCheckBoxInput(UITaskDetailTab.FIELD_CHECKALL).setChecked(isCheckAll) ;
  }
  protected String getEventDelegation() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_DELEGATION).getValue();
  }
  protected void setEventDelegation(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_DELEGATION).setValue(value) ;
  }

  protected boolean getEmailReminder() {
    UIEventReminderTab taskReminderTab =  getChildById(TAB_TASKREMINDER) ;
    return taskReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).isChecked() ;
  }
  protected void setEmailReminder(boolean isChecked) {
    UIEventReminderTab taskReminderTab =  getChildById(TAB_TASKREMINDER) ;
    taskReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).setChecked(isChecked) ;
  }
  protected String getEmailRemindBefore() {
    UIEventReminderTab taskReminderTab =  getChildById(TAB_TASKREMINDER) ;
    return taskReminderTab.getUIStringInput(UIEventReminderTab.EMAIL_REMIND_BEFORE).getValue() ;
  }
  protected String isEmailRepeat() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return eventDetailTab.getUIStringInput(UIEventReminderTab.EMAIL_IS_REPEAT).getValue() ;
  }
  protected String getEmailRepeatInterVal() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return eventDetailTab.getUIStringInput(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).getValue() ;
  }
  protected void setEmailReminderBefore(String value) {
    UIEventReminderTab taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUIStringInput(UIEventReminderTab.EMAIL_REMIND_BEFORE).setValue(value) ;
  }

  protected String getEmailAddress() {
    UIEventReminderTab taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return taskDetailTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).getValue() ;
  }

  protected void setEmailAddress(String value) {
    UIEventReminderTab taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUIFormTextAreaInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).setValue(value) ;
  }

  protected boolean getPopupReminder() {
    UIEventReminderTab taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return taskDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).isChecked() ;
  }
  protected void setPopupReminder(boolean isChecked) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).setChecked(isChecked) ;
  }
  protected String getPopupReminderTime() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return taskDetailTab.getUIStringInput(UIEventReminderTab.POPUP_REMIND_BEFORE).getValue() ;
  }
  protected String isPopupRepeat() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return eventDetailTab.getUIStringInput(UIEventReminderTab.POPUP_IS_REPEAT).getValue() ;
  }
  protected String getPopupRepeatInterVal() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return eventDetailTab.getUIStringInput(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
  }
  protected void setPopupReminderTime(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUIStringInput(UIEventReminderTab.POPUP_REMIND_BEFORE).setValue(value) ;
  }
  protected long getPopupReminderSnooze() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    try {
      String time =  taskDetailTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
      return Long.parseLong(time) ;
    } catch (Exception e){
      e.printStackTrace() ;
    }
    return 0 ;
  }
  protected void setPopupReminderSnooze(long value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
  }
  protected List<Attachment>  getAttachments(String eventId, boolean isAddNew) {
    UITaskDetailTab taskDetailTab = getChild(UITaskDetailTab.class) ;
    return taskDetailTab.getAttachments() ;
  }
  protected void setAttachments(List<Attachment> attachment) throws Exception {
    UITaskDetailTab taskDetailTab = getChild(UITaskDetailTab.class) ;
    taskDetailTab.setAttachments(attachment) ;
    taskDetailTab.refreshUploadFileList() ;
  }
  protected void setEventReminders(List<Reminder> reminders){
    UIEventReminderTab taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    for(Reminder r : reminders) {
      if(Reminder.TYPE_EMAIL.equals(r.getReminderType())) {
        setEmailReminder(true) ;
        setEmailAddress(r.getEmailAddress()) ;
        setEmailReminderBefore(String.valueOf(r.getAlarmBefore())) ;
        taskDetailTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_IS_REPEAT).setValue(String.valueOf(r.isRepeat())) ;
        taskDetailTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).setValue(String.valueOf(r.getRepeatInterval())) ;
      }else if(Reminder.TYPE_POPUP.equals(r.getReminderType())) {
        setPopupReminder(true) ;
        taskDetailTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REMIND_BEFORE).setValue(String.valueOf(r.getAlarmBefore())) ;
        taskDetailTab.getUIFormSelectBox(UIEventReminderTab.POPUP_IS_REPEAT).setValue(String.valueOf(r.isRepeat())) ;
        taskDetailTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).setValue(String.valueOf(r.getRepeatInterval())) ;
      } else {
        System.out.println("\n\n reminder not supported");
      }      
    }
  }

  protected List<Reminder>  getEventReminders(Date fromDateTime) {
    List<Reminder> reminders = new ArrayList<Reminder>() ;
    if(getEmailReminder()) { 
      Reminder email = new Reminder() ;
      email.setReminderType(Reminder.TYPE_EMAIL) ;
      email.setAlarmBefore(Long.parseLong(getEmailRemindBefore())) ;
      email.setEmailAddress(getEmailAddress()) ;
      email.setRepeate(Boolean.parseBoolean(isEmailRepeat())) ;
      email.setRepeatInterval(Long.parseLong(getEmailRepeatInterVal())) ;
      email.setFromDateTime(fromDateTime) ;
      reminders.add(email) ;
    }
    if(getPopupReminder()) {
      Reminder popup = new Reminder() ;
      popup.setReminderType(Reminder.TYPE_POPUP) ;
      popup.setAlarmBefore(Long.parseLong(getPopupReminderTime())) ;
      popup.setRepeate(Boolean.parseBoolean(isPopupRepeat())) ;
      popup.setRepeatInterval(Long.parseLong(getPopupRepeatInterVal())) ;
      popup.setFromDateTime(fromDateTime) ;
      reminders.add(popup) ;
    }
    return reminders ;
  }

  protected String getEventPriority() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_PRIORITY).getValue() ;
  }
  protected void setSelectedEventPriority(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_PRIORITY).setValue(value) ;
  }

  public void update(String calType, List<SelectItemOption<String>> options) throws Exception{
    UITaskDetailTab uiTaskDetailTab = getChildById(TAB_TASKDETAIL) ;
    if(options != null) {
      uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CALENDAR).setOptions(options) ;
    }else {
      uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CALENDAR).setOptions(getCalendar()) ;
    }
    calType_ = calType ;
  }

  private List<SelectItemOption<String>> getCalendar() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(getSession(), username, true) ;
    for(org.exoplatform.calendar.service.Calendar c : calendars) {
      options.add(new SelectItemOption<String>(c.getName(), c.getId())) ;
    }
    return options ;
  }

  static  public class AddCategoryActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      System.out.println("\n\n AddCategoryActionListener");
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIEventCategoryManager.class, 470) ;
      uiForm.setSelectedTab(TAB_TASKDETAIL) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class AddEmailAddressActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      System.out.println("\n\n AddEmailAddressActionListener");
      UITaskForm uiForm = event.getSource() ;
      uiForm.setSelectedTab(TAB_TASKREMINDER) ;
      if(!uiForm.getEmailReminder()) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UITaskForm.msg.email-reminder-required", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } else {
        UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
        UIPopupAction uiPopupAction  = uiPopupContainer.getChild(UIPopupAction.class) ;
        uiPopupAction.activate(UIAddressForm.class, 640) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }
  }
  static  public class AddAttachmentActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      //UIPopupAction uiParentPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class RemoveAttachmentActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UITaskDetailTab uiTaskDetailTab = uiForm.getChild(UITaskDetailTab.class) ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      Attachment attachfile = new Attachment();
      for (Attachment att : uiTaskDetailTab.attachments_) {
        if (att.getId().equals(attFileId)) {
          attachfile = (Attachment) att;
        }
      }
      uiTaskDetailTab.removeFromUploadFileList(attachfile);
      uiTaskDetailTab.refreshUploadFileList() ;
      uiForm.setSelectedTab(TAB_TASKDETAIL) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class AddCalendarActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      //UITaskForm uiForm = event.getSource() ;
      System.out.println( "\n\n ==========> AddCalendarActionListener");
    }
  }

  static  public class SelectUserActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      System.out.println( "\n\n ==========> AddParticipantActionListener");
      UITaskForm uiForm = event.getSource() ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class) ;
      UIGroupSelector uiGroupSelector = uiPopupAction.activate(UIGroupSelector.class,500) ;
      uiGroupSelector.setType(UISelectComponent.TYPE_USER) ;
      uiGroupSelector.setSelectedGroups(null) ;
      uiGroupSelector.setComponent(uiForm,new String[]{UITaskDetailTab.FIELD_DELEGATION}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static  public class SaveActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      if(uiForm.isEventDetailValid(calendarPortlet.getCalendarSetting())) {
        String username = event.getRequestContext().getRemoteUser() ;
        String calendarId = uiForm.getCalendarId() ;
        CalendarEvent calendarEvent = new CalendarEvent() ;
        if(!uiForm.isAddNew_){
          calendarEvent = uiForm.calendarEvent_ ; 
        }
        calendarEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        calendarEvent.setSummary(uiForm.getEventSumary()) ;
        calendarEvent.setDescription(uiForm.getEventDescription()) ;
        calendarEvent.setTaskDelegator(uiForm.getEventDelegation()) ;
        Date from = uiForm.getEventFromDate(calendarPortlet.getCalendarSetting().getTimeFormat()) ;
        Date to = uiForm.getEventToDate(calendarPortlet.getCalendarSetting().getTimeFormat()) ;
        if(from.after(to)) {
          uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.event-date-time-logic", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } else if(from.equals(to)) {
          to = CalendarUtils.getEndDay(from).getTime() ;
        } 
        if(uiForm.getEventAllDate()) {
          java.util.Calendar tempCal = CalendarUtils.getInstanceTempCalendar() ;
          tempCal.setTime(to) ;
          tempCal.add(java.util.Calendar.MILLISECOND, -1) ;
          to = tempCal.getTime() ;
        }
        calendarEvent.setCalType(uiForm.calType_) ;
        calendarEvent.setFromDateTime(from) ;
        calendarEvent.setToDateTime(to);
        calendarEvent.setCalendarId(calendarId) ;
        calendarEvent.setEventCategoryId(uiForm.getEventCategory()) ;
        calendarEvent.setEventState(uiForm.getStatus()) ;
        calendarEvent.setLocation(uiForm.getEventDelegation()) ;
        calendarEvent.setPriority(uiForm.getEventPriority()) ; 
        calendarEvent.setAttachment(uiForm.getAttachments(calendarEvent.getId(), uiForm.isAddNew_)) ;
        calendarEvent.setReminders(uiForm.getEventReminders(from)) ;
        try {
          if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
            CalendarUtils.getCalendarService().saveUserEvent(uiForm.getSession(), username, calendarId, calendarEvent, uiForm.isAddNew_) ;
          }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
            CalendarUtils.getCalendarService().saveEventToSharedCalendar(uiForm.getSystemSession(), username, calendarId, calendarEvent, uiForm.isAddNew_) ;
          }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
            CalendarUtils.getCalendarService().savePublicEvent(uiForm.getSystemSession(), calendarId, calendarEvent, uiForm.isAddNew_) ;          
          }
          CalendarView calendarView = (CalendarView)uiViewContainer.getRenderedChild() ;
          if (calendarView instanceof UIListContainer)((UIListContainer)calendarView).setDisplaySearchResult(false) ;
          uiViewContainer.refresh() ;
          calendarView.setLastUpdatedEventId(calendarEvent.getId()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
          UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
          uiMiniCalendar.updateMiniCal() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
          uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
        }catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-event-error", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          e.printStackTrace() ;
        }
      } else {
        uiApp.addMessage(new ApplicationMessage(uiForm.errorMsg_, null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiForm.setSelectedTab(TAB_TASKDETAIL) ;
      }
    }
  }
  static  public class CancelActionListener extends EventListener<UITaskForm> {
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}

