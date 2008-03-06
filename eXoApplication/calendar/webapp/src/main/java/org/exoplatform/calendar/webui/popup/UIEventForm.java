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

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.model.property.Categories;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.webui.CalendarView;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarView;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIFormComboBox;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
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
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Editor : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
    events = {
      @EventConfig(listeners = UIEventForm.SaveActionListener.class),
      @EventConfig(listeners = UIEventForm.AddCategoryActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.MoveNextActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.MovePreviousActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.DeleteUserActionListener.class, phase = Phase.DECODE),

      @EventConfig(listeners = UIEventForm.AddEmailAddressActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.AddAttachmentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.RemoveAttachmentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.AddParticipantActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.OnChangeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIEventForm extends UIFormTabPane implements UIPopupComponent, UISelector{
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
  private boolean isAddNew_ = true ;
  private CalendarEvent calendarEvent_ = null ;
  protected String calType_ = "0" ;
  private String errorMsg_ = null ;
  
  private String oldCalendarId_ = null ;
  private String newCalendarId_ = null ;
  private String newCategoryId_ = null ;
  //protected LinkedHashMap<String, String> participants_ = new LinkedHashMap<String, String>() ;

  public UIEventForm() throws Exception {
    super("UIEventForm");
    UIEventDetailTab eventDetailTab =  new UIEventDetailTab(TAB_EVENTDETAIL) ;
    addChild(eventDetailTab) ;
    UIEventReminderTab eventReminderTab =  new UIEventReminderTab(TAB_EVENTREMINDER) ;
    addChild(eventReminderTab) ;
    UIFormInputWithActions eventShareTab =  new UIFormInputWithActions(TAB_EVENTSHARE) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    eventShareTab.addUIFormInput(new UIFormSelectBox(FIELD_SHARE, FIELD_SHARE, getShareValue()) ) ;
    eventShareTab.addUIFormInput(new UIFormSelectBox(FIELD_STATUS, FIELD_STATUS, getStatusValue()) ) ;
    eventShareTab.addUIFormInput(new UIFormTextAreaInput(FIELD_PARTICIPANT, FIELD_PARTICIPANT, null)) ;
    eventShareTab.addUIFormInput(new UIFormTextAreaInput(FIELD_MEETING, FIELD_MEETING, null)) ;
    actions = new ArrayList<ActionData>() ;

    ActionData addUser = new ActionData() ;
    addUser.setActionListener("AddParticipant") ;
    addUser.setActionName("AddUser") ;
    addUser.setActionParameter(TAB_EVENTSHARE);
    addUser.setActionType(ActionData.TYPE_ICON) ;
    addUser.setCssIconClass("SelectUserIcon") ;
    actions.add(addUser) ;

    eventShareTab.setActionField(FIELD_PARTICIPANT, actions) ;
    addChild(eventShareTab) ;
    UIEventAttenderTab eventAttenderTab = new UIEventAttenderTab(TAB_EVENTATTENDER) ;
    addChild(eventAttenderTab) ;
    setSelectedTab(eventDetailTab.getId()) ;
  }
  public String getLabel(String id) {
    String label = id ;
    try {
      label = super.getLabel(id) ;
    } catch (Exception e) {
    }
    return label ;
  }

  public void initForm(CalendarSetting calSetting, CalendarEvent eventCalendar, String formTime) throws Exception {
    reset() ;
    UIEventDetailTab eventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    ((UIFormDateTimePicker)eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM)).setDateFormatStyle(calSetting.getDateFormat()) ;
    ((UIFormDateTimePicker)eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO)).setDateFormatStyle(calSetting.getDateFormat()) ;
    UIEventAttenderTab attenderTab = getChildById(TAB_EVENTATTENDER) ;
    List<SelectItemOption<String>> fromTimes 
    = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(),calSetting.getTimeFormat(), calSetting.getTimeInterval()) ;
    List<SelectItemOption<String>> toTimes 
    = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(),calSetting.getTimeFormat(), calSetting.getTimeInterval()) ;
    eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_FROM_TIME).setOptions(fromTimes) ;
    eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_TO_TIME).setOptions(toTimes) ;
    List<SelectItemOption<String>> fromOptions = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(),calSetting.getTimeFormat()) ;
    List<SelectItemOption<String>> toOptions = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(),calSetting.getTimeFormat()) ;
    attenderTab.getUIFormComboBox(UIEventAttenderTab.FIELD_FROM_TIME).setOptions(fromOptions) ;
    attenderTab.getUIFormComboBox(UIEventAttenderTab.FIELD_TO_TIME).setOptions(toOptions) ;
    if(eventCalendar != null) {
     // oldCalendarId_ = calType_ + CalendarUtils.COLON + eventCalendar.getCalendarId();
      isAddNew_ = false ;
      calendarEvent_ = eventCalendar ;
      setEventSumary(eventCalendar.getSummary()) ;
      setEventDescription(eventCalendar.getDescription()) ;
      setEventAllDate(CalendarUtils.isAllDayEvent(eventCalendar)) ;
      setEventFromDate(eventCalendar.getFromDateTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      setEventToDate(eventCalendar.getToDateTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      setSelectedCalendarId(eventCalendar.getCalendarId()) ;
      setSelectedCategory(eventCalendar.getEventCategoryId()) ;
      setEventPlace(eventCalendar.getLocation()) ;
      setEventRepeat(eventCalendar.getRepeatType()) ;
      setSelectedEventPriority(eventCalendar.getPriority()) ;
      setEventReminders(eventCalendar.getReminders()) ;
      setAttachments(eventCalendar.getAttachment()) ;
      if(eventCalendar.isPrivate()) {
        setSelectedShareType(UIEventForm.ITEM_PRIVATE) ;
      } else {
        setSelectedShareType(UIEventForm.ITEM_PUBLIC) ;
      }
      setSelectedEventState(eventCalendar.getEventState()) ;
      setMeetingInvitation(eventCalendar.getInvitation()) ;
      StringBuffer pars = new StringBuffer() ;
      //pars.append(Util.getPortalRequestContext().getRemoteUser()) ;
      if(eventCalendar.getParticipant() != null) {
        for(String par : eventCalendar.getParticipant()) {
          if(!CalendarUtils.isEmpty(pars.toString())) pars.append(",") ;
          pars.append(par) ;
        }
      }
      setParticipant(pars.toString()) ;
      attenderTab.updateParticipants(pars.toString());
      //eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CALENDAR).setEnable(true) ;
      if(CalendarUtils.SHARED_TYPE.equals(calType_) || CalendarUtils.PUBLIC_TYPE.equals(calType_)) {
        
        boolean isContains = false ;
        CalendarService calService = CalendarUtils.getCalendarService();
        List<EventCategory> listCategory = 
          calService.getEventCategories(SessionProviderFactory.createSessionProvider(), CalendarUtils.getCurrentUser());
        for(EventCategory eventCat : listCategory) {
          isContains = eventCat.getName().toLowerCase().equals(eventCalendar.getEventCategoryId().toLowerCase()) ;
          if(isContains) break ;
        }
        if(!isContains) {
          SelectItemOption<String> item = new SelectItemOption<String>(eventCalendar.getEventCategoryId(), eventCalendar.getEventCategoryId()) ;
          eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).getOptions().add(item) ;
          newCategoryId_ = eventCalendar.getEventCategoryId() ;
          eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setValue(eventCalendar.getEventCategoryId());
        }
        //eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).getOptions().add(arg0)
      }
      attenderTab.calendar_.setTime(eventCalendar.getFromDateTime()) ;
    } else {
      UIMiniCalendar miniCalendar = getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
      java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
      try {
        cal.setTimeInMillis(Long.parseLong(formTime)) ;
      } catch (Exception e)      {
        cal.setTime(miniCalendar.getCurrentCalendar().getTime()) ;
      }
      Long beginMinute = (cal.get(java.util.Calendar.MINUTE)/calSetting.getTimeInterval())*calSetting.getTimeInterval() ;
      cal.set(java.util.Calendar.MINUTE, beginMinute.intValue()) ;
      setEventFromDate(cal.getTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      cal.add(java.util.Calendar.MINUTE, (int)calSetting.getTimeInterval()*2) ;
      setEventToDate(cal.getTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      StringBuffer pars = new StringBuffer(CalendarUtils.getCurrentUser()) ;
      setParticipant(pars.toString()) ;
      attenderTab.updateParticipants(pars.toString());
    }
  }

  public void update(String calType, List<SelectItemOption<String>> options) throws Exception{
    UIEventDetailTab uiEventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    if(options != null) {
      uiEventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CALENDAR).setOptions(options) ;
    }else {
      uiEventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CALENDAR).setOptions(getCalendars()) ;
    }
    calType_ = calType ;
  }
  private List<SelectItemOption<String>> getCalendars() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    options.add(new SelectItemOption<String>(CalendarUtils.PRIVATE_CALENDARS, "")) ;
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(SessionProviderFactory.createSessionProvider(), username, true) ;
    for(org.exoplatform.calendar.service.Calendar c : calendars) {
      options.add(new SelectItemOption<String>(CalendarUtils.DOUBLESCORE  + c.getName(), CalendarUtils.PRIVATE_TYPE + ":" + c.getId())) ;
    }
    GroupCalendarData gcd = calendarService.getSharedCalendars(SessionProviderFactory.createSystemProvider(), username, true);
    if(gcd != null) {
      options.add(new SelectItemOption<String>(CalendarUtils.SHARED_CALENDARS, "")) ;
      for(org.exoplatform.calendar.service.Calendar c : gcd.getCalendars()) {
        if(Arrays.asList(c.getEditPermission()).contains(username)){
          options.add(new SelectItemOption<String>(CalendarUtils.DOUBLESCORE  + c.getName(), CalendarUtils.SHARED_TYPE + CalendarUtils.COLON + c.getId())) ;
        }
      }
    }
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(SessionProviderFactory.createSystemProvider(), CalendarUtils.getUserGroups(username), false, username) ;
    if(lgcd != null && lgcd.size() > 0) {
      options.add(new SelectItemOption<String>(CalendarUtils.PUBLIC_CALENDARS, "")) ;
      for(GroupCalendarData g : lgcd) {
        for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
          if(c != null && c.getEditPermission() != null && Arrays.asList(c.getEditPermission()).contains(username)){
            options.add(new SelectItemOption<String>(CalendarUtils.DOUBLESCORE + c.getName(), CalendarUtils.PUBLIC_TYPE + CalendarUtils.COLON + c.getId())) ;
          }
        }
      }
    }
    return options ;
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
    UIFormInputWithActions eventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setOptions(getCategory()) ;
  }

  private List<SelectItemOption<String>> getShareValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_PRIVATE, ITEM_PRIVATE)) ;
    options.add(new SelectItemOption<String>(ITEM_PUBLIC, ITEM_PUBLIC)) ;
    return options ;
  }
  private List<SelectItemOption<String>> getStatusValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_BUSY, ITEM_BUSY)) ;
    options.add(new SelectItemOption<String>(ITEM_AVAILABLE, ITEM_AVAILABLE)) ;
    return options ;
  }

  public String[] getActions() {
    return new String[]{"AddAttachment","Save", "Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void updateSelect(String selectField, String value) throws Exception {
  } 

  protected boolean isEventDetailValid(CalendarSetting calendarSetting){
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
      getEventFromDate(calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
      errorMsg_ = "UIEventForm.msg.event-fromdate-notvalid" ;
      return false ;
    }
    try {
      getEventToDate(calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    if(!getEventAllDate()) {
      if(CalendarUtils.isEmpty(getEventToDateValue())){
        errorMsg_ = "UIEventForm.msg.event-todate-required" ;
        return false ;
      } 
      try {
        getEventToDate(calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
        errorMsg_ =  "UIEventForm.msg.event-todate-notvalid" ;
        return false ;
      }
      try {
        if(getEventFromDate(calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()).after(getEventToDate(calendarSetting.getDateFormat(), calendarSetting.getTimeFormat())) || 
            getEventFromDate(calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()).equals(getEventToDate(calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()))){
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
    String value = eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CALENDAR).getValue() ;
    newCalendarId_ = value ;
    if (!CalendarUtils.isEmpty(value) && value.split(CalendarUtils.COLON).length>0) {
      calType_ = value.split(CalendarUtils.COLON)[0] ; 
      return value.split(CalendarUtils.COLON)[1] ;      
    } 
    return value ;
  }
  public void setSelectedCalendarId(String value) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    value = calType_ + CalendarUtils.COLON + value ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CALENDAR).setValue(value) ;
    oldCalendarId_ = value ;
  }

  protected String getEventCategory() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).getValue() ;
  }
  public void setSelectedCategory(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setValue(value) ;
  }

  protected Date getEventFromDate(String dateFormat,String timeFormat) throws Exception {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
    UIFormComboBox timeField = eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_FROM_TIME) ;
    if(getEventAllDate()) {
      DateFormat df = new SimpleDateFormat(dateFormat) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return CalendarUtils.getBeginDay(df.parse(fromField.getValue())).getTime();
    } 
    DateFormat df = new SimpleDateFormat(dateFormat + " " + timeFormat) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    return df.parse(fromField.getValue() + " " + timeField.getValue()) ;
  }
  protected String getEventFormDateValue () {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
    return fromField.getValue() ;
  }
  protected void setEventFromDate(Date date,String dateFormat, String timeFormat) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIEventAttenderTab eventAttenderTab = getChildById(TAB_EVENTATTENDER) ;
    UIFormDateTimePicker fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
    UIFormComboBox timeField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat(dateFormat) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    fromField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(timeFormat) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    timeField.setValue(df.format(date)) ;
    eventAttenderTab.setEventFromDate(date, timeFormat) ;
  }

  protected Date getEventToDate(String dateFormat, String timeFormat) throws Exception {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    UIFormComboBox timeField = eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_TO_TIME) ;
    if(getEventAllDate()) {
      DateFormat df = new SimpleDateFormat(dateFormat) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return CalendarUtils.getEndDay(df.parse(toField.getValue())).getTime();
    } 
    DateFormat df = new SimpleDateFormat(dateFormat + " " + timeFormat) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    return df.parse(toField.getValue() + " " + timeField.getValue()) ;
  }
  protected void setEventToDate(Date date,String dateFormat, String timeFormat) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIEventAttenderTab eventAttenderTab = getChildById(TAB_EVENTATTENDER) ;
    UIFormDateTimePicker toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    UIFormComboBox timeField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat(dateFormat) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    toField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(timeFormat) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    timeField.setValue(df.format(date)) ;
    eventAttenderTab.setEventToDate(date, timeFormat) ;
  }

  protected String getEventToDateValue () {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    return toField.getValue() ;
  }
  protected boolean getEventAllDate() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormCheckBoxInput(UIEventDetailTab.FIELD_CHECKALL).isChecked() ;
  }
  protected void setEventAllDate(boolean isCheckAll) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormCheckBoxInput(UIEventDetailTab.FIELD_CHECKALL).setChecked(isCheckAll) ;
  }

  protected String getEventRepeat() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return  eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_REPEAT).getValue() ;
  }
  protected void setEventRepeat(String type) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_REPEAT).setValue(type) ;
  }
  protected String getEventPlace() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_PLACE).getValue();
  }
  protected void setEventPlace(String value) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_PLACE).setValue(value) ;
  }

  protected boolean getEmailReminder() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).isChecked() ;
  }
  protected void setEmailReminder(boolean isChecked) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).setChecked(isChecked) ;
  }

  protected String getEmailRemindBefore() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REMIND_BEFORE).getValue() ;
  }
  protected String isEmailRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_IS_REPEAT).getValue() ;
  }
  protected void setEmailRepeat(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_IS_REPEAT).setValue(value) ;
  }
  protected String getEmailRepeatInterVal() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).getValue() ;
  }
  protected void setEmailRepeatInterVal(long value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
  }
  protected String isPopupRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_IS_REPEAT).getValue() ;
  }
  protected void setPopupRepeat(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_IS_REPEAT).setValue(value) ;
  }
  protected String getPopupRepeatInterVal() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
  }

  protected void setEmailRemindBefore(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REMIND_BEFORE).setValue(value) ;
  }

  protected String getEmailAddress() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormTextAreaInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).getValue() ;
  }

  protected void setEmailAddress(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormTextAreaInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).setValue(value) ;
  }

  protected boolean getPopupReminder() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).isChecked() ;
  }
  protected void setPopupReminder(boolean isChecked) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).setChecked(isChecked) ;
  }
  protected String getPopupReminderTime() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REMIND_BEFORE).getValue() ;
  }

  protected void setPopupRemindBefore(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REMIND_BEFORE).setValue(value) ;
  }
  protected long getPopupReminderSnooze() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    try {
      String time =  eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
      return Long.parseLong(time) ;
    } catch (Exception e){
      e.printStackTrace() ;
    }
    return 0 ;
  }
  protected void setPopupRepeatInterval(long value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
  }
  protected List<Attachment>  getAttachments(String eventId, boolean isAddNew) {
    UIEventDetailTab uiEventDetailTab = getChild(UIEventDetailTab.class) ;
    return uiEventDetailTab.getAttachments() ;
  }
  protected void setAttachments(List<Attachment> attachment) throws Exception {
    UIEventDetailTab uiEventDetailTab = getChild(UIEventDetailTab.class) ;
    uiEventDetailTab.setAttachments(attachment) ;
    uiEventDetailTab.refreshUploadFileList() ;
  }
  protected void setEventReminders(List<Reminder> reminders){
    for(Reminder rm : reminders) {
      if(Reminder.TYPE_EMAIL.equals(rm.getReminderType())) {
        setEmailReminder(true) ;
        setEmailAddress(rm.getEmailAddress()) ;
        setEmailRepeat(String.valueOf(rm.isRepeat())) ;
        setEmailRemindBefore(String.valueOf(rm.getAlarmBefore())) ;
        setEmailRepeatInterVal(rm.getRepeatInterval()) ;
      }else if(Reminder.TYPE_POPUP.equals(rm.getReminderType())) {
        setPopupReminder(true) ;  
        setPopupRepeat(String.valueOf(rm.isRepeat())) ;
        setPopupRemindBefore(String.valueOf(rm.getAlarmBefore()));
        setPopupRepeatInterval(rm.getRepeatInterval()) ;
      } else {
        System.out.println("\n\n reminder not supported");
      }
    }
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

  protected String getEventState() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return eventDetailTab.getUIFormSelectBox(FIELD_STATUS).getValue() ;
  }
  protected void setSelectedEventState(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormSelectBox(FIELD_STATUS).setValue(value) ;
  }

  protected String getShareType() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return  eventDetailTab.getUIFormSelectBox(FIELD_SHARE).getValue()  ;
  }
  protected void setSelectedShareType(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormSelectBox(FIELD_SHARE).setValue(value) ;
  }

  protected String[] getMeetingInvitation() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    String invitation = eventDetailTab.getUIFormTextAreaInput(FIELD_MEETING).getValue() ;
    if(CalendarUtils.isEmpty(invitation)) return null ;
    else return invitation.split(CalendarUtils.COMMA) ;
  } 
  protected void setMeetingInvitation(String[] values) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    StringBuffer sb = new StringBuffer() ;
    if(values != null) {
      for(String s : values) {
        sb.append(s).append(CalendarUtils.COMMA) ;
      }
    }
    eventDetailTab.getUIFormTextAreaInput(FIELD_MEETING).setValue(sb.toString()) ;
  }

  protected String[] getParticipant() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    String participant = eventDetailTab.getUIFormTextAreaInput(FIELD_PARTICIPANT).getValue() ;
    if(CalendarUtils.isEmpty(participant)) return null ;
    else {
      String[] pars = participant.split(CalendarUtils.COMMA) ;
      for(int i = 0; i < pars.length; i++) {
        pars[i] = pars[i].trim() ;
      }
      return pars ;
    }
  } 

  protected void setParticipant(String values) throws Exception{
    ((UIFormInputWithActions)getChildById(TAB_EVENTSHARE)).getUIFormTextAreaInput(FIELD_PARTICIPANT).setValue(values) ;
    //((UIEventAttenderTab)getChildById(TAB_EVENTATTENDER)).updateParticipants(values) ;
  }

  private void initParticipantCheckBox(String id) {
    UIEventAttenderTab eventAttenderTab = getChildById(TAB_EVENTATTENDER) ;
    UIFormCheckBoxInput<Boolean> input = eventAttenderTab.getChildById(id) ;
    if(input == null) eventAttenderTab.addUIFormInput(new UIFormCheckBoxInput<Boolean>(id,id, false)) ;
  }
  private SessionProvider getSession() {
    return SessionProviderFactory.createSessionProvider() ;
  }
  private SessionProvider getSystemSession() {
    return SessionProviderFactory.createSystemProvider() ;
  }
  static  public class AddCategoryActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      System.out.println("\n\n AddCategoryActionListener");
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIEventCategoryManager.class, 470) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class AddEmailAddressActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      System.out.println("\n\n AddEmailAddressActionListener");
      UIEventForm uiForm = event.getSource() ;
      if(!uiForm.getEmailReminder()) uiForm.setEmailReminder(true) ;
      /*UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.email-reminder-required", null));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;*/      
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiPopupAction  = uiPopupContainer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIAddressForm.class, 640) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;

    }
  }
  static  public class AddAttachmentActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      //UIPopupAction uiParentPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class RemoveAttachmentActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventDetailTab uiEventDetailTab = uiForm.getChild(UIEventDetailTab.class) ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      Attachment attachfile = new Attachment();
      for (Attachment att : uiEventDetailTab.attachments_) {
        if (att.getId().equals(attFileId)) {
          attachfile = (Attachment) att;
        }
      }
      uiEventDetailTab.removeFromUploadFileList(attachfile);
      uiEventDetailTab.refreshUploadFileList() ;
      uiForm.setSelectedTab(TAB_EVENTDETAIL) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class AddCalendarActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      System.out.println( "\n\n ==========> AddParticipantActionListener");
    }
  }

  static  public class AddParticipantActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      System.out.println( "\n\n ==========> AddParticipantActionListener");
      UIEventForm uiForm = event.getSource() ;
      String tabId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopupAction = uiContainer.getChild(UIPopupAction.class) ;
      UISelectUserForm uiSelectUserForm = uiChildPopupAction.activate(UISelectUserForm.class, 680) ;
      uiSelectUserForm.init(((UIEventAttenderTab)uiForm.getChildById(TAB_EVENTATTENDER)).parMap_.keySet()) ;
      uiSelectUserForm.tabId_ = tabId ;
      uiSelectUserForm.initSearchForm() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopupAction) ;      
    }
  }
  static  public class MoveNextActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      System.out.println( "\n\n ==========> MoveNextActionListener");
      UIEventForm uiForm = event.getSource() ;
      ((UIEventAttenderTab)uiForm.getChildById(TAB_EVENTATTENDER)).moveNextDay() ;
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTATTENDER)) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      System.out.println( "\n\n ==========> MovePreviousActionListener");
      UIEventForm uiForm = event.getSource() ;
      ((UIEventAttenderTab)uiForm.getChildById(TAB_EVENTATTENDER)).movePreviousDay() ;
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTATTENDER)) ;
    }
  }
  static  public class DeleteUserActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventAttenderTab tabAttender = uiForm.getChildById(TAB_EVENTATTENDER) ;
      String[] pars = tabAttender.getParticipants() ;
      StringBuffer newPars = new StringBuffer() ;
      for(String id : pars){
        UIFormCheckBoxInput input = uiForm.getUIFormCheckBoxInput(id) ;
        if(input != null && input.isChecked()) {
          tabAttender.parMap_.remove(id) ;
          uiForm.removeChildById(id) ;
        }else {
          if(newPars != null && newPars.length() > 0) newPars.append(",") ;
          newPars.append(id) ;
        }
      }
      uiForm.setParticipant(newPars.toString()) ;
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTATTENDER)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTSHARE)) ;
    }
  }
  /*static  public class SaveUserActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      System.out.println( "\n\n ==========> AddParticipantActionListener");
      UIEventForm uiForm = event.getSource() ;
      for(String id : uiForm.participants_.values()) {
        UIFormCheckBoxInput<Boolean> input = uiForm.getUIFormCheckBoxInput(id) ;
        if(input != null && input.isChecked()) {

        }
      }
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }*/

  static  public class SaveActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarSetting calSetting = calendarPortlet.getCalendarSetting() ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      //SessionProvider sessionProvider = SessionsUtils.getSystemProvider() ;
      if(uiForm.isEventDetailValid(calSetting)) {
        String username = event.getRequestContext().getRemoteUser() ;
        String calendarId = uiForm.getCalendarId() ;
        /* int count = 1;
        int interval = 60 ;
        long start_milisec = new Date().getTime() ;
        System.out.println("\n\n Starting add  event ...." ); */
        Date from = uiForm.getEventFromDate(calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
        System.out.println(from);
        Date to = uiForm.getEventToDate(calSetting.getDateFormat(),calSetting.getTimeFormat()) ;
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
        try {
          String[] pars = uiForm.getParticipant() ;
          String eventId = null ;
          /*Calendar temp =  CalendarUtils.getInstanceTempCalendar() ;
          temp.setTime(from) ;
          Calendar cal = CalendarUtils.getBeginDay(temp) ;
          Calendar calEnd = CalendarUtils.getEndDay(temp) ;
          int t = 1 ;
          while (cal.before(calEnd)) { */
          CalendarEvent calendarEvent = new CalendarEvent() ;
          if(!uiForm.isAddNew_){
            calendarEvent = uiForm.calendarEvent_ ; 
          }

          /*cal.add(Calendar.MINUTE, interval) ;
            from = cal.getTime() ;*/
          calendarEvent.setFromDateTime(from) ;
          /*cal.setTime(to) ;
            cal.add(Calendar.MINUTE, interval) ;
            to = cal.getTime() ; */
          calendarEvent.setToDateTime(to);
          if(pars != null && pars.length > 0) calendarEvent.setParticipant(pars) ;
          if(uiForm.getMeetingInvitation() != null) calendarEvent.setInvitation(uiForm.getMeetingInvitation()) ;
          calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
          calendarEvent.setSummary(uiForm.getEventSumary()) ;
          calendarEvent.setDescription(uiForm.getEventDescription()) ;
          calendarEvent.setCalType(uiForm.calType_) ;
          calendarEvent.setCalendarId(calendarId) ;
          calendarEvent.setEventCategoryId(uiForm.getEventCategory()) ;
          calendarEvent.setLocation(uiForm.getEventPlace()) ;
          calendarEvent.setRepeatType(uiForm.getEventRepeat()) ;
          calendarEvent.setPriority(uiForm.getEventPriority()) ; 
          calendarEvent.setPrivate(UIEventForm.ITEM_PRIVATE.equals(uiForm.getShareType())) ;
          calendarEvent.setEventState(uiForm.getEventState()) ;
          calendarEvent.setAttachment(uiForm.getAttachments(calendarEvent.getId(), uiForm.isAddNew_)) ;
          calendarEvent.setReminders(uiForm.getEventReminders(from, calendarEvent.getReminders())) ;
          eventId = calendarEvent.getId() ;
          if(uiForm.isAddNew_){
            if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
              calService.saveUserEvent(uiForm.getSession(), username, calendarId, calendarEvent, uiForm.isAddNew_) ;
            }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
              calService.saveEventToSharedCalendar(uiForm.getSystemSession() , username, calendarId, calendarEvent, uiForm.isAddNew_) ;
            }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
              calService.savePublicEvent(uiForm.getSystemSession() , calendarId, calendarEvent, uiForm.isAddNew_) ;          
            }
          } else  {
            String fromCal = uiForm.oldCalendarId_.split(CalendarUtils.COLON)[1].trim() ;
            String toCal = uiForm.newCalendarId_.split(CalendarUtils.COLON)[1].trim() ;
            String fromType = uiForm.oldCalendarId_.split(CalendarUtils.COLON)[0].trim() ;
            String toType = uiForm.newCalendarId_.split(CalendarUtils.COLON)[0].trim() ;
            if((uiForm.calType_.equals(CalendarUtils.SHARED_TYPE) || uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)) && uiForm.newCategoryId_ != null){
              EventCategory evc = new EventCategory() ;
              evc.setName(uiForm.newCategoryId_ ) ;
              calService.saveEventCategory(uiForm.getSession(), username, evc, null, true) ;
              uiViewContainer.updateCategory() ;
            }
            
            List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
            listEvent.add(calendarEvent) ;
            calService.moveEvent(uiForm.getSession(), fromCal, toCal, fromType, toType, listEvent, username) ;
          }
          
          /*System.out.println("\n\n added .  " + calendarEvent.getSummary() +" " +(new Date().getTime() - start_milisec) + " ss");
            t++ ;
          } 

          long end_milisec = new Date().getTime() ;
          long amount = end_milisec - start_milisec ;
          System.out.println("\n\n Finished.  " + amount + " ss");
           */ 
          CalendarView calendarView = (CalendarView)uiViewContainer.getRenderedChild() ;
          if(calendarView instanceof UIListContainer)((UIListContainer)calendarView).setDisplaySearchResult(false) ;
          uiViewContainer.refresh() ;
          calendarView.setLastUpdatedEventId(eventId) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
          UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
          uiMiniCalendar.updateMiniCal() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
          uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
        }catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.add-event-error", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          e.printStackTrace() ;
        }
      } else {
        uiApp.addMessage(new ApplicationMessage(uiForm.errorMsg_, null));
        uiForm.setSelectedTab(TAB_EVENTDETAIL) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }
  static  public class OnChangeActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventAttenderTab attendTab = uiForm.getChildById(TAB_EVENTATTENDER) ;
      boolean isCheckFreeTime = attendTab.getUIFormCheckBoxInput(attendTab.FIELD_CHECK_TIME).isChecked() ;
      if(isCheckFreeTime) {
        StringBuilder sb = new StringBuilder() ;
        for(String par : attendTab.getParticipants()) {
          if(sb != null && sb.length() > 0) sb.append(",") ;
          sb.append(par) ;
        }
        attendTab.updateParticipants(sb.toString()) ;
      }
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTATTENDER)) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      System.out.println("CancelActionListener ========>") ;
      UIEventForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}
