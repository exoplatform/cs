/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UIMonthView;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.mail.Attachment;
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
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.NumberFormatValidator;

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
      //@EventConfig(listeners = UIEventForm.AddAttachmentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.AddParticipantActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventForm.CancelActionListener.class)
    }
)
public class UIEventForm extends UIFormTabPane implements UIPopupComponent, UISelector{
  final public static String TAB_EVENTDETAIL = "eventDetail".intern() ;
  final public static String TAB_EVENTSHARE = "eventShare".intern() ;
  final public static String TAB_EVENTATTENDER = "eventAttender".intern() ;

  final public static String FIELD_EVENT = "eventName".intern() ;
  final public static String FIELD_CALENDAR = "calendar".intern() ;
  final public static String FIELD_CATEGORY = "category".intern() ;
  final public static String FIELD_FROM = "from".intern() ;
  final public static String FIELD_TO = "to".intern() ;
  final public static String FIELD_CHECKALL = "allDay".intern() ;
  final public static String FIELD_REPEAT = "repeat".intern() ;
  final public static String FIELD_PLACE = "place".intern() ;
  final public static String FIELD_REMINDER = "reminder".intern() ;
  final public static String FIELD_TIMEREMINDER = "timeReminder".intern() ;
  final public static String FIELD_PRIORITY = "priority".intern() ; 
  //final public static String FIELD_ATTACHMENT = "attachment".intern() ;
  final public static String FIELD_DESCRIPTION = "description".intern() ;

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

  private List<Attachment> attachments_ = new ArrayList<Attachment>() ;

  public UIEventForm() throws Exception {
    super("UIEventForm", false);
    UIFormInputWithActions eventDetailTab =  new UIFormInputWithActions(TAB_EVENTDETAIL) ;
    UIFormInputWithActions eventShareTab =  new UIFormInputWithActions(TAB_EVENTSHARE) ;
    UIEventAttenderTab eventAttenderTab = new UIEventAttenderTab(TAB_EVENTATTENDER) ;

    eventDetailTab.addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null)) ;
    eventDetailTab.addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    eventDetailTab.addUIFormInput(new UIFormSelectBox(FIELD_CALENDAR, FIELD_CALENDAR, getCalendar())) ;
    eventDetailTab.addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, getCategory())) ;
    ActionData addCategoryAction = new ActionData() ;
    addCategoryAction.setActionType(ActionData.TYPE_ICON) ;
    addCategoryAction.setActionName("AddCategory") ;
    addCategoryAction.setActionListener("AddCategory") ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    actions.add(addCategoryAction) ;
    eventDetailTab.setActionField(FIELD_CATEGORY,actions) ;
    eventDetailTab.addUIFormInput(new UIFormDateTimeInput(FIELD_FROM, FIELD_FROM, new Date()));
    eventDetailTab.addUIFormInput(new UIFormDateTimeInput(FIELD_TO, FIELD_TO, new Date()));
    eventDetailTab.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_CHECKALL, FIELD_CHECKALL, null));
    eventDetailTab.addUIFormInput(new UIFormStringInput(FIELD_PLACE, FIELD_PLACE, null));
    eventDetailTab.addUIFormInput(new UIFormSelectBox(FIELD_REPEAT, FIELD_REPEAT, getRepeater())) ;
    eventDetailTab.addUIFormInput(new UIFormSelectBox(FIELD_REMINDER, FIELD_REMINDER, getReminder())) ;
    eventDetailTab.addUIFormInput(new UIFormStringInput(FIELD_TIMEREMINDER, FIELD_TIMEREMINDER, null).addValidator(NumberFormatValidator.class));
    eventDetailTab.addUIFormInput(new UIFormSelectBox(FIELD_PRIORITY, FIELD_PRIORITY, getPriority())) ;
    /*eventDetailTab.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENT, FIELD_ATTACHMENT, null)) ;
    actions = new ArrayList<ActionData>() ;
    ActionData addAttachment = new ActionData() ;
    addAttachment.setActionListener("AddAttachment") ;
    addAttachment.setActionName("AddAttachment") ;
    addAttachment.setActionType(ActionData.TYPE_ICON) ;
    actions.add(addAttachment) ;
    eventDetailTab.setActionField(FIELD_ATTACHMENT, actions) ;*/
    addChild(eventDetailTab) ;

    eventShareTab.addUIFormInput(new UIFormSelectBox(FIELD_SHARE, FIELD_SHARE, getShareValue()) ) ;
    eventShareTab.addUIFormInput(new UIFormSelectBox(FIELD_STATUS, FIELD_STATUS, getStatusValue()) ) ;
    eventShareTab.addUIFormInput(new UIFormTextAreaInput(FIELD_MEETING, FIELD_MEETING, null)) ;
    eventShareTab.addUIFormInput(new UIFormTextAreaInput(FIELD_PARTICIPANT, FIELD_PARTICIPANT, null)) ;
    actions = new ArrayList<ActionData>() ;
    ActionData addParticipant = new ActionData() ;
    addParticipant.setActionListener("AddParticipant") ;
    addParticipant.setActionName("AddParticipant") ;
    addParticipant.setActionType(ActionData.TYPE_ICON) ;
    actions.add(addParticipant) ;
    eventShareTab.setActionField(FIELD_PARTICIPANT, actions) ;
    addChild(eventShareTab) ;

    addChild(eventAttenderTab) ;
    setRenderedChild(TAB_EVENTDETAIL) ;

  }

  public void initForm(String calendarId) {

  }
  private List<SelectItemOption<String>> getCalendar() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<Calendar> calendars = calendarService.getUserCalendars(username) ;
    for(Calendar c : calendars) {
      options.add(new SelectItemOption<String>(c.getName(), c.getId())) ;
    }
    return options ;
  }
  private List<SelectItemOption<String>> getCategory() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
    List<EventCategory> eventCategories = calendarService.getEventCategories(Util.getPortalRequestContext().getRemoteUser()) ;
    for(EventCategory category : eventCategories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getName())) ;
    }
    return options ;
  }

  private List<SelectItemOption<String>> getPriority() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("hight", "1")) ;
    options.add(new SelectItemOption<String>("normal", "2")) ;
    options.add(new SelectItemOption<String>("low", "3")) ;
    return options ;
  }
  protected void refreshCategory()throws Exception {
    UIFormInputWithActions eventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(FIELD_CATEGORY).setOptions(getCategory()) ;
  }

  private List<SelectItemOption<String>> getRepeater() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_REPEAT, ITEM_REPEAT)) ;
    options.add(new SelectItemOption<String>(ITEM_UNREPEAT, ITEM_UNREPEAT)) ;
    return options ;
  }
  private List<SelectItemOption<String>> getReminder() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(String rmdType : Reminder.REMINDER_TYPES) {
      options.add(new SelectItemOption<String>(rmdType, rmdType)) ;
    }
    return options ;
  }
  private List<SelectItemOption<String>> getShareValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_PUBLIC, ITEM_PUBLIC)) ;
    options.add(new SelectItemOption<String>(ITEM_PRIVATE, ITEM_PRIVATE)) ;
    return options ;
  }
  private List<SelectItemOption<String>> getStatusValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_AVAILABLE, ITEM_AVAILABLE)) ;
    options.add(new SelectItemOption<String>(ITEM_BUSY, ITEM_BUSY)) ;
    return options ;
  }

  public String[] getActions() {
    return new String[]{"Save", "Cancel"} ;
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void updateSelect(String selectField, String value) throws Exception {
    // TODO Auto-generated method stub

  }
  protected boolean isEventDetailValid(){
    return !(CalendarUtils.isEmpty(getEventSumary()) ||
        CalendarUtils.isEmpty(getEventCategory()) ||
        CalendarUtils.isEmpty(getCalendarId()) ||
        CalendarUtils.isEmpty(getEventFromDate().toString()));

  }
  protected String getEventSumary() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIStringInput(FIELD_EVENT).getValue() ;
  }
  protected void setEventSumary(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIStringInput(FIELD_EVENT).setValue(value) ;
  }
  protected String getEventDescription() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormTextAreaInput(FIELD_DESCRIPTION).getValue() ;
  }
  protected void setEventDescription(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormTextAreaInput(FIELD_DESCRIPTION).setValue(value) ;
  }
  protected String getCalendarId() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(FIELD_CALENDAR).getValue() ;
  }
  protected void setSelectedCalendarId(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(FIELD_CALENDAR).setValue(value) ;
  }

  protected String getEventCategory() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(FIELD_CATEGORY).getValue() ;
  }
  protected void setSelectedCategory(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(FIELD_CATEGORY).setValue(value) ;
  }

  protected Date getEventFromDate() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput fromField = eventDetailTab.getChildById(FIELD_FROM) ;
    return fromField.getCalendar().getTime() ;
  }
  protected void setEventFromDate(java.util.Calendar calendar) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput fromField = eventDetailTab.getChildById(FIELD_FROM) ;
    fromField.setCalendar(calendar) ;
  }

  protected Date getEventToDate() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput fromField = eventDetailTab.getChildById(FIELD_TO) ;
    return fromField.getCalendar().getTime() ;
  }
  protected void setEventToDate(java.util.Calendar calendar) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimeInput fromField = eventDetailTab.getChildById(FIELD_TO) ;
    fromField.setCalendar(calendar) ;
  }

  protected boolean getEventAllDate() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormCheckBoxInput(FIELD_CHECKALL).isChecked() ;
  }
  protected void setEventAllDate(boolean isCheckAll) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormCheckBoxInput(FIELD_CHECKALL).setChecked(isCheckAll) ;
  }

  protected boolean getEventRepeat() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return ITEM_REPEAT.equals(eventDetailTab.getUIFormSelectBox(FIELD_REPEAT).getValue()) ;
  }
  protected void setEventRepeat(boolean isRepeat) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    if(isRepeat) {
      eventDetailTab.getUIFormSelectBox(FIELD_REPEAT).setValue(ITEM_REPEAT) ;
    } else {
      eventDetailTab.getUIFormSelectBox(FIELD_REPEAT).setValue(ITEM_UNREPEAT) ;
    }
  }
  protected String getEventPlace() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIStringInput(FIELD_PLACE).getValue();
  }
  protected void setEventPlace(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIStringInput(FIELD_PLACE).setValue(value) ;
  }

  protected List<Reminder>  getEventReminder(String eventId) {
    List<Reminder> reminders = new ArrayList<Reminder>() ;
    String reminder = getEventReminderType() ;
    Reminder rmdByMail = new Reminder() ;
    Reminder rmdByPopup = new Reminder() ;
    rmdByMail.setEventId(eventId) ;
    rmdByPopup.setEventId(eventId) ;
    rmdByMail.setReminder(Reminder.TYPE_EMAIL) ;
    rmdByPopup.setReminder(Reminder.TYPE_POPUP) ;
    rmdByMail.setAlarmBefore(getEventReminderTime()) ;
    rmdByPopup.setAlarmBefore(getEventReminderTime());
    if(getEventRepeat()) {
      rmdByMail.setRepeat(Reminder.REPEAT) ;
      rmdByPopup.setRepeat(Reminder.REPEAT) ;
    } else { 
      rmdByMail.setRepeat(Reminder.UNREPEAT) ;
      rmdByPopup.setRepeat(Reminder.UNREPEAT) ;
    }
    if(Reminder.TYPE_BOTH.equals(reminder)) {
      reminders.add(rmdByMail) ;
      reminders.add(rmdByPopup) ;
    } else if(Reminder.TYPE_EMAIL.equals(reminder)) {
      reminders.add(rmdByMail) ;
    } else {
      reminders.add(rmdByPopup) ;
    }
    return reminders ;
  }
  protected String  getEventReminderType() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(FIELD_REMINDER).getValue() ;
  }
  protected void setSelectedReminder(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(FIELD_REMINDER).setValue(value) ;
  }

  protected String getEventReminderTime() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIStringInput(FIELD_TIMEREMINDER).getValue();
  }
  protected void setEventReminderTime(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIStringInput(FIELD_TIMEREMINDER).setValue(value) ;
  }
  protected String getEventPriority() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(FIELD_PRIORITY).getValue() ;
  }
  protected void setSelectedEventPriority(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(FIELD_PRIORITY).setValue(value) ;
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

  protected String getMeetingInvitation() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return  eventDetailTab.getUIFormTextAreaInput(FIELD_MEETING).getValue()  ;
  } 
  protected void setMeetingInvitation(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormTextAreaInput(FIELD_MEETING).setValue(value) ;
  }

  protected String getParticipant() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return  eventDetailTab.getUIFormTextAreaInput(FIELD_PARTICIPANT).getValue()  ;
  } 
  protected void setParticipant(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormTextAreaInput(FIELD_PARTICIPANT).setValue(value) ;
  }

  static  public class AddCategoryActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupAction uiParentPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIEventCategoryForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }

  /*static  public class AddAttachmentActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupAction uiParentPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIAddEmailAddress.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }*/

  static  public class AddCalendarActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      System.out.println( "\n\n ==========> AddParticipantActionListener");
    }
  }

  static  public class AddParticipantActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      System.out.println( "\n\n ==========> AddParticipantActionListener");
    }
  }

  static  public class SaveActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(uiForm.isEventDetailValid()) {
        CalendarService calendarService = uiForm.getApplicationComponent(CalendarService.class) ;
        String username = event.getRequestContext().getRemoteUser() ;
        String calendarId = uiForm.getCalendarId() ;
        CalendarEvent calendarEvent = new CalendarEvent() ;
        calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
        calendarEvent.setSummary(uiForm.getEventSumary()) ;
        calendarEvent.setDescription(uiForm.getEventDescription()) ;
        calendarEvent.setCalendarId(calendarId) ;
        calendarEvent.setEventCategoryId(uiForm.getEventCategory()) ;
        calendarEvent.setFromDateTime(uiForm.getEventFromDate()) ;
        calendarEvent.setLocation(uiForm.getEventPlace()) ;
        calendarEvent.setToDateTime(uiForm.getEventToDate());
        calendarEvent.setPrivate(UIEventForm.ITEM_PRIVATE.equals(uiForm.getShareType())) ;
        calendarEvent.setReminders(uiForm.getEventReminder(calendarEvent.getId())) ;
        calendarEvent.setPriority(uiForm.getEventPriority()) ; 
        try {
          calendarService.saveUserEvent(username, calendarId, calendarEvent, true) ;
          UIMonthView uiMonthView = uiForm.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMonthView.class) ;
          if(uiMonthView != null) { 
            uiMonthView.refresh() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiMonthView) ;
          }
          uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
          uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.add-event-successfully", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.add-event-error", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          e.printStackTrace() ;
        }
      } else {
        uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.fields-required", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiForm.setRenderedChild(TAB_EVENTDETAIL) ;
      }
    }
  }
  static  public class CancelActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax( uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
}
