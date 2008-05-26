/*
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
 */
package org.exoplatform.mail.webui.popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.webui.CalendarUtils;
import org.exoplatform.mail.webui.SelectItem;
import org.exoplatform.mail.webui.SelectItemOptionGroup;
import org.exoplatform.mail.webui.Selector;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
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
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTabPane;


/**
 * Created by The eXo Platform SARL
 * @author  Tuan Pham
 *          tuan.pham@exoplatform.com
 *          Nam Phung 
 *          phunghainam@gmail.com
 *          
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
    events = {
      @EventConfig(listeners = UIEventForm.SaveActionListener.class),
      @EventConfig(listeners = UIEventForm.AddCategoryActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.AddEmailAddressActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIEventForm extends UIFormTabPane implements UIPopupComponent, Selector{
  final public static String TAB_EVENTDETAIL = "eventDetail".intern() ;
  final public static String TAB_EVENTREMINDER = "eventReminder".intern() ;
  final public static String TAB_EVENTSHARE = "eventShare".intern() ;
  final public static String TAB_EVENTATTENDER = "eventAttender".intern() ;

  final public static String FIELD_SHARE = "shareEvent".intern() ;
  final public static String FIELD_STATUS = "status".intern() ;
  final public static String FIELD_MEETING = "meeting".intern() ;
  final public static String FIELD_PARTICIPANT = "participant".intern() ;

  final public static String ITEM_PUBLIC = "public".intern() ;
  final public static String ITEM_PRIVATE = "private".intern() ;
  final public static String ITEM_AVAILABLE = "available".intern() ;
  final public static String ITEM_BUSY = "busy".intern() ;

  final public static String ITEM_REPEAT = "true".intern() ;
  final public static String ITEM_UNREPEAT = "false".intern() ;

  final public static String ACT_REMOVE = "RemoveAttachment".intern() ;
  final public static String ACT_ADDEMAIL = "AddEmailAddress".intern() ;
  final public static String ACT_ADDCATEGORY = "AddCategory".intern() ;
  public boolean isAddNew_ = true ;
  protected String calType_ = "0" ;
  private String errorMsg_ = null ;

  public UIEventForm() throws Exception {
    super("UIEventForm");
    UIEventDetailTab eventDetailTab =  new UIEventDetailTab(TAB_EVENTDETAIL) ;
    addChild(eventDetailTab) ;
    setSelectedTab(eventDetailTab.getId()) ;
    UIEventReminderTab eventReminderTab =  new UIEventReminderTab(TAB_EVENTREMINDER) ;
    addChild(eventReminderTab) ;
    //setRenderedChild(TAB_EVENTDETAIL) ;
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
  }
  public void initForm(CalendarSetting calSetting, CalendarEvent eventCalendar) throws Exception {
    reset() ;
    UIEventDetailTab eventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    List<SelectItemOption<String>> fromTimes = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat()) ;
    List<SelectItemOption<String>> toTimes = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat()) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_FROM_TIME).setOptions(fromTimes) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_TO_TIME).setOptions(toTimes) ;
    if(eventCalendar != null) {
      isAddNew_ = false ;
      setEventSumary(eventCalendar.getSummary()) ;
      setEventDescription(eventCalendar.getDescription()) ;
      setEventAllDate(CalendarUtils.isAllDayEvent(eventCalendar)) ;
      setEventFromDate(eventCalendar.getFromDateTime()) ;
      setEventToDate(eventCalendar.getToDateTime()) ;
      if(eventCalendar.getCalendarId() != null) setSelectedCalendarId(eventCalendar.getCalendarId()) ;
      setSelectedCategory(eventCalendar.getEventCategoryId()) ;
      setEventPlace(eventCalendar.getLocation()) ;
      setEventRepeat(eventCalendar.getRepeatType()) ;
      setSelectedEventPriority(eventCalendar.getPriority()) ;
      setEventReminders(eventCalendar.getReminders()) ;
      //((UIEventDetailTab)getChildById(TAB_EVENTDETAIL)).getUIFormSelectBox(UIEventDetailTab.FIELD_CALENDAR).setEnable(false) ;
    } else {
      java.util.Calendar cal = GregorianCalendar.getInstance() ;
      int beginMinute = (cal.get(java.util.Calendar.MINUTE)/CalendarUtils.DEFAULT_TIMEITERVAL)*CalendarUtils.DEFAULT_TIMEITERVAL ;
      cal.set(java.util.Calendar.MINUTE, beginMinute) ;
      setEventFromDate(cal.getTime()) ;
      cal.add(java.util.Calendar.MINUTE, CalendarUtils.DEFAULT_TIMEITERVAL*2) ;
      setEventToDate(cal.getTime()) ;
    }
  }
  
  public void update(String calType, List<SelectItem> options) throws Exception{
    UIEventDetailTab uiEventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    if(options != null) {
      ((org.exoplatform.mail.webui.UIFormSelectBox)uiEventDetailTab.getChildById(UIEventDetailTab.FIELD_CALENDAR)).setOptions(options) ;
    }else {
      ((org.exoplatform.mail.webui.UIFormSelectBox)uiEventDetailTab.getChildById(UIEventDetailTab.FIELD_CALENDAR)).setOptions(getCalendarOption()) ;
    }
    calType_ = calType ;
  }
  
  public static List<SelectItem> getCalendarOption() throws Exception {
    List<SelectItem> options = new ArrayList<SelectItem>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    /*
     * Modified by Philippe (philippe.aristote@gmail.com)
     * Uses SelectItemOptionGroup to differienciate private, shared and public groups
     */

    // private calendars group
    SelectItemOptionGroup privGrp = new SelectItemOptionGroup("private-calendars");
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(SessionProviderFactory.createSessionProvider(), username, true) ;
    for(org.exoplatform.calendar.service.Calendar c : calendars) {
      privGrp.addOption(new org.exoplatform.mail.webui.SelectItemOption<String>(c.getName(), CalendarUtils.PRIVATE_TYPE + CalendarUtils.COLON + c.getId())) ;
    }
    options.add(privGrp);
    // shared calendars group
    GroupCalendarData gcd = calendarService.getSharedCalendars(SessionProviderFactory.createSystemProvider(), username, true);
    if(gcd != null) {
      SelectItemOptionGroup sharedGrp = new SelectItemOptionGroup("shared-calendars");
      for(org.exoplatform.calendar.service.Calendar c : gcd.getCalendars()) {
        if(CalendarUtils.canEdit(null, c.getEditPermission(), username)){
          String owner = "" ;
          if(c.getCalendarOwner() != null) owner = c.getCalendarOwner() + "- " ;
          sharedGrp.addOption(new org.exoplatform.mail.webui.SelectItemOption<String>(owner + c.getName(), CalendarUtils.SHARED_TYPE + CalendarUtils.COLON + c.getId())) ;
        }
      }
      options.add(sharedGrp);
    }
    // public calendars group
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(SessionProviderFactory.createSystemProvider(), CalendarUtils.getUserGroups(username), false, username) ;
    if(lgcd != null) {
      OrganizationService oService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
      SelectItemOptionGroup pubGrp = new SelectItemOptionGroup("public-calendars");
      for(GroupCalendarData g : lgcd) {
        for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
          if(CalendarUtils.canEdit(oService, c.getEditPermission(), username)){
            pubGrp.addOption(new org.exoplatform.mail.webui.SelectItemOption<String>(c.getName(), CalendarUtils.PUBLIC_TYPE + CalendarUtils.COLON + c.getId())) ;
          }
        }

      }
      options.add(pubGrp);
    }
    return options ;
  }

  public static List<SelectItemOption<String>> getCategory() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<EventCategory> eventCategories = calendarService.getEventCategories(SessionProviderFactory.createSystemProvider(), Util.getPortalRequestContext().getRemoteUser()) ;
    for(EventCategory category : eventCategories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getName())) ;
    }
    return options ;
  }

  protected void refreshCategory()throws Exception {
    UIFormInputWithActions eventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setOptions(getCategory()) ;
//    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setValue(selectedCategory) ;

  }

  public String[] getActions() { return new String[]{"Save", "Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public void updateValue(String selectField, String value) { }

  protected boolean isEventDetailValid(){
    if(CalendarUtils.isEmpty(getEventSumary())) {
      errorMsg_ = "UIEventForm.msg.event-summary-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getCalendarId())) {
      errorMsg_ = "UIEventForm.msg.event-calendar-required" ;
      return false ;
    } 
    if(CalendarUtils.isEmpty(getEventCategory())) {
      errorMsg_ = "UIEventForm.msg.event-category-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getEventFormDateValue())) {
      errorMsg_ = "UIEventForm.msg.event-fromdate-required" ;
      return false ;
    }
    try {
      getEventFromDate() ;
    } catch (Exception e) {
      e.printStackTrace() ;
      errorMsg_ = "UIEventForm.msg.event-fromdate-notvalid" ;
      return false ;
    }

    if(!getEventAllDate()) {
      if(CalendarUtils.isEmpty(getEventToDateValue())){
        errorMsg_ = "UIEventForm.msg.event-todate-required" ;
        return false ;
      } 
      try {
        getEventToDate() ;
      } catch (Exception e) {
        e.printStackTrace() ;
        errorMsg_ =  "UIEventForm.msg.event-todate-notvalid" ;
        return false ;
      }
      try {
        if(getEventFromDate().after(getEventToDate()) || getEventFromDate().equals(getEventToDate())){
          errorMsg_ = "UIEventForm.msg.event-date-time-logic" ;
          return false ;
        }
      } catch (Exception e) {
        e.printStackTrace() ;
        errorMsg_ = "UIEventForm.msg.event-date-time-getvalue" ;
        return false ;
      }      
    }
    if(getEmailReminder() && CalendarUtils.isEmpty(getEmailAddress())) {
      errorMsg_ = "UIEventForm.msg.event-email-required" ;
      return false ;
    } 
    errorMsg_ = null ;
    return true ;
  }
  protected String getEventSumary() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_EVENT).getValue() ;
  }
  protected void setEventSumary(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_EVENT).setValue(value) ;
  }
  protected String getEventDescription() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormTextAreaInput(UIEventDetailTab.FIELD_DESCRIPTION).getValue() ;
  }
  protected void setEventDescription(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormTextAreaInput(UIEventDetailTab.FIELD_DESCRIPTION).setValue(value) ;
  }
  protected String getCalendarId() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    //return eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CALENDAR).getValue() ;
    
    String value = ((org.exoplatform.mail.webui.UIFormSelectBox)eventDetailTab.getChildById(UIEventDetailTab.FIELD_CALENDAR)).getValue() ;
    if (!CalendarUtils.isEmpty(value) && value.split(CalendarUtils.COLON).length>0) {
      calType_ = value.split(CalendarUtils.COLON)[0] ; 
      return value.split(CalendarUtils.COLON)[1] ;      
    } 
    return value ;
  }
  
  public void setSelectedCalendarId(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    value = calType_ + CalendarUtils.COLON + value ;
    ((org.exoplatform.mail.webui.UIFormSelectBox)eventDetailTab.getChildById(UIEventDetailTab.FIELD_CALENDAR)).setValue(value) ;
  }

  protected String getEventCategory() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).getValue() ;
  }
  protected void setSelectedCategory(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setValue(value) ;
  }

  protected Date getEventFromDate() throws Exception {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
    UIFormSelectBox timeField = eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_FROM_TIME) ;
    if(getEventAllDate()) {
      DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
      return CalendarUtils.getBeginDay(df.parse(fromField.getValue())).getTime();
    } 
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    return df.parse(fromField.getValue() + " " + timeField.getValue()) ;
  }
  protected String getEventFormDateValue () {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
    return fromField.getValue() ;
  }
  protected void setEventFromDate(Date date) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
    UIFormSelectBox timeField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    fromField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(CalendarUtils.TIMEFORMAT) ;
    timeField.setValue(df.format(date)) ;
  }

  protected Date getEventToDate() throws Exception {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    UIFormSelectBox timeField = eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_TO_TIME) ;
    if(getEventAllDate()) {
      DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
      return CalendarUtils.getBeginDay(df.parse(toField.getValue())).getTime();
    } 
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    return df.parse(toField.getValue() + " " + timeField.getValue()) ;
  }
  protected void setEventToDate(Date date) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    UIFormSelectBox timeField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    toField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(CalendarUtils.TIMEFORMAT) ;
    timeField.setValue(df.format(date)) ;
  }

  protected String getEventToDateValue () {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    return toField.getValue() ;
  }
  protected boolean getEventAllDate() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormCheckBoxInput(UIEventDetailTab.FIELD_CHECKALL).isChecked() ;
  }
  protected void setEventAllDate(boolean isCheckAll) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormCheckBoxInput(UIEventDetailTab.FIELD_CHECKALL).setChecked(isCheckAll) ;
  }

  protected String getEventRepeat() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return  eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_REPEAT).getValue() ;
  }
  protected void setEventRepeat(String type) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_REPEAT).setValue(type) ;
  }
  protected String getEventPlace() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_PLACE).getValue();
  }
  protected void setEventPlace(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_PLACE).setValue(value) ;
  }

  protected boolean getEmailReminder() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).isChecked() ;
  }
  protected void setEmailReminder(boolean isChecked) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    eventDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).setChecked(isChecked) ;
  }
  protected String getEmailReminderTime() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventDetailTab.getUIStringInput(UIEventReminderTab.EMAIL_REMIND_BEFORE).getValue() ;
  }
  protected void setEmailReminderTime(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    eventDetailTab.getUIStringInput(UIEventReminderTab.EMAIL_REMIND_BEFORE).setValue(value) ;
  }

  protected String getEmailAddress() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventDetailTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).getValue() ;
  }

  protected void setEmailAddress(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    eventDetailTab.getUIFormTextAreaInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).setValue(value) ;
  }

  protected boolean getPopupReminder() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).isChecked() ;
  }
  protected void setPopupReminder(boolean isChecked) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    eventDetailTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).setChecked(isChecked) ;
  }
  protected String getPopupReminderTime() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventDetailTab.getUIStringInput(UIEventReminderTab.POPUP_REMIND_BEFORE).getValue() ;
  }

  protected void setPopupReminderTime(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTREMINDER) ;
    eventDetailTab.getUIStringInput(UIEventReminderTab.POPUP_REMIND_BEFORE).setValue(value) ;
  }
  
  protected void setEventReminders(List<Reminder> reminders){
    for(Reminder r : reminders) {
      if(Reminder.TYPE_EMAIL.equals(r.getReminderType())) {
        setEmailReminder(true) ;
        setEmailAddress(r.getEmailAddress()) ;
        setEmailReminderTime(String.valueOf(r.getAlarmBefore())) ; 
      }else if(Reminder.TYPE_POPUP.equals(r.getReminderType())) {
        setPopupReminder(true) ;
        setPopupReminderTime(String.valueOf(r.getAlarmBefore())) ;
        //setPopupReminderSnooze(r.getSnooze()) ;
      } else {
        System.out.println("\n\n reminder not supported");
      }
    }
  }
  protected String getEmailRemindBefore() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REMIND_BEFORE).getValue() ;
  }
  protected String isEmailRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_IS_REPEAT).getValue() ;
  }
  protected String getEmailRepeatInterVal() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).getValue() ;
  }
  protected String isPopupRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_IS_REPEAT).getValue() ;
  }
  protected String getPopupRepeatInterVal() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
  }
  protected List<Reminder>  getEventReminders(Date fromDateTime, List<Reminder> currentReminders) {
    List<Reminder> reminders = new ArrayList<Reminder>() ;
    if(getEmailReminder()) {
      Reminder email = new Reminder() ;
      if(currentReminders != null) {
        for(Reminder rm : currentReminders) {
          if(rm.getReminderType().equals(Reminder.TYPE_EMAIL)) {
            email = rm ;
            break ;
          }
        }
      }     
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
      if(currentReminders != null) {
        for(Reminder rm : currentReminders) {
          if(rm.getReminderType().equals(Reminder.TYPE_POPUP)) {
            popup = rm ;
            break ;
          }
        }
      } 
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
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_PRIORITY).getValue() ;
  }
  protected void setSelectedEventPriority(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_PRIORITY).setValue(value) ;
  }
  

  static  public class AddCategoryActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiForm.getParent() ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class, 470) ;
      UIEventCategoryForm uiEventCategoryForm = uiPopupContainer.createUIComponent(UIEventCategoryForm.class, null, null) ;
      uiPopupContainer.addChild(uiEventCategoryForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class AddEmailAddressActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      if(!uiForm.getEmailReminder()) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.email-reminder-required", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } else {
        UIPopupActionContainer uiActionContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class) ;    
        UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
        UIAddressForm uiAddressForm = uiChildPopup.activate(UIAddressForm.class, 650) ; 
        uiAddressForm.setContactList("") ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
      }
    }
  }

  static  public class SaveActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(uiForm.isEventDetailValid()) {
        String username = event.getRequestContext().getRemoteUser() ;
        String calendarId = uiForm.getCalendarId() ;
        CalendarEvent calendarEvent = new CalendarEvent() ;
       /* if(!uiForm.isAddNew_){
          calendarEvent = uiForm.calendarEvent_ ; 
        }*/
        calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
        calendarEvent.setSummary(uiForm.getEventSumary()) ;
        calendarEvent.setDescription(uiForm.getEventDescription()) ;
        Date from = uiForm.getEventFromDate() ;
        Date to = uiForm.getEventToDate() ;
        if(from.after(to)) {
          uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.event-date-time-logic", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } else if(from.equals(to)) {
          to = CalendarUtils.getEndDay(from).getTime() ;
        } 
        calendarEvent.setCalType(uiForm.calType_) ;
        calendarEvent.setFromDateTime(from) ;
        calendarEvent.setToDateTime(to);
        calendarEvent.setCalendarId(calendarId) ;
        calendarEvent.setEventCategoryId(uiForm.getEventCategory()) ;
        calendarEvent.setLocation(uiForm.getEventPlace()) ;
        calendarEvent.setRepeatType(uiForm.getEventRepeat()) ;
        calendarEvent.setPriority(uiForm.getEventPriority()) ; 
        calendarEvent.setReminders(uiForm.getEventReminders(from, calendarEvent.getReminders())) ;
        try {
          if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
            CalendarUtils.getCalendarService().saveUserEvent(SessionProviderFactory.createSystemProvider(), username, calendarId, calendarEvent, uiForm.isAddNew_) ;
          }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
            CalendarUtils.getCalendarService().saveEventToSharedCalendar(SessionProviderFactory.createSystemProvider(), username, calendarId, calendarEvent, uiForm.isAddNew_) ;
          }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
            CalendarUtils.getCalendarService().savePublicEvent(SessionProviderFactory.createSystemProvider(), calendarId, calendarEvent, uiForm.isAddNew_) ;          
          }
          uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
         /* if(uiForm.isAddNew_) {
            uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.add-event-successfully", null));
          } else {
            uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.update-event-successfully", null));
          }*/
          uiForm.reset() ;
          /*event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;*/
        }catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.add-event-error", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          e.printStackTrace() ;
        }
      } else {
        uiApp.addMessage(new ApplicationMessage(uiForm.errorMsg_, null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiForm.setRenderedChild(TAB_EVENTDETAIL) ;
      }
    }
  }
  static  public class CancelActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}
