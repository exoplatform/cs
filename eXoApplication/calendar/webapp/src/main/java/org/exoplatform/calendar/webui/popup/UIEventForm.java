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

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.webui.CalendarView;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIFormComboBox;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
import org.exoplatform.calendar.webui.UIListContainer;
import org.exoplatform.calendar.webui.UIListView;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.calendar.webui.UIPreview;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Editor : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfigs ( {
  @ComponentConfig(
                   lifecycle = UIFormLifecycle.class,
                   template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
                   events = {
                     @EventConfig(listeners = UIEventForm.SaveActionListener.class),
                     @EventConfig(listeners = UIEventForm.AddCategoryActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.MoveNextActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.MovePreviousActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.DeleteUserActionListener.class, phase = Phase.DECODE),
                     //@EventConfig(listeners = UIEventForm.DeleteActionListener.class, confirm = "UIEventForm.msg.confirm-delete", phase = Phase.DECODE ),
                     @EventConfig(listeners = UIEventForm.AddEmailAddressActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.AddAttachmentActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.RemoveAttachmentActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.DownloadAttachmentActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.AddParticipantActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.AddUserActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.OnChangeActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.CancelActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.SelectTabActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.ConfirmOKActionListener.class, name = "ConfirmOK", phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.ConfirmCancelActionListener.class, name = "ConfirmCancel", phase = Phase.DECODE)
                   }
  )
  ,
    @ComponentConfig(
                   id = "UIPopupWindowAddUserEventForm",
                   type = UIPopupWindow.class,
                   template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
                   events = {
                     @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
                     @EventConfig(listeners = UIEventForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
                   }
  )
}
)
public class UIEventForm extends UIFormTabPane implements UIPopupComponent, UISelector{
  final public static String TAB_EVENTDETAIL = "eventDetail".intern() ;
  final public static String TAB_EVENTREMINDER = "eventReminder".intern() ;
  final public static String TAB_EVENTSHARE = "eventShare".intern() ;
  final public static String TAB_EVENTATTENDER = "eventAttender".intern() ;
 
 
  final public static String FIELD_MEETING = "participant".intern() ;
  
//TODO cs-839
  //final public static String FIELD_PARTICIPANT = "participant".intern() ;
  final public static String FIELD_ISSENDMAIL = "isSendMail".intern() ;
  //final public static String FIELD_INVITATION_NOTE = "invitationNote".intern() ;

  final public static String ITEM_PUBLIC = "public".intern() ;
  final public static String ITEM_PRIVATE = "private".intern() ;
  final public static String ITEM_AVAILABLE = "available".intern() ;
  final public static String ITEM_BUSY = "busy".intern() ;
  final public static String ITEM_OUTSIDE = "outside".intern() ;

  final public static String ITEM_REPEAT = "true".intern() ;
  final public static String ITEM_UNREPEAT = "false".intern() ;

  final public static String ITEM_ALWAYS = "always".intern();
  final public static String ITEM_NERVER = "never".intern();
  final public static String ITEM_ASK = "ask".intern();
  
  final public static String ACT_REMOVE = "RemoveAttachment".intern() ;
  final public static String ACT_DOWNLOAD = "DownloadAttachment".intern() ;
  final public static String ACT_ADDEMAIL = "AddEmailAddress".intern() ;
  final public static String ACT_ADDCATEGORY = "AddCategory".intern() ;
  final public static String STATUS_EMPTY = "".intern();
  final public static String STATUS_PENDING = "pending".intern();
  final public static String STATUS_YES = "yes".intern();
  final public static String STATUS_NO = "no".intern();
  private boolean isAddNew_ = true ;
  private boolean isChangedSignificantly = false;
  private CalendarEvent calendarEvent_ = null ;
  protected String calType_ = "0" ;
  protected String invitationMsg_ = "" ;
  protected String participantList_ = "" ;
  private String errorMsg_ = null ;
  private String errorValues = null ;
  protected Map<String, String> participants_ = new LinkedHashMap<String, String>() ;
  protected Map<String, String> participantStatus_ = new LinkedHashMap<String, String>() ;
  protected LinkedList<ParticipantStatus> participantStatusList_ = new LinkedList<ParticipantStatus>();
  private String oldCalendarId_ = null ;
  private String newCalendarId_ = null ;
  private String confirm_msg = "";
  //private String newCategoryId_ = null ;
  public UIEventForm() throws Exception {
    super("UIEventForm");
    this.setId("UIEventForm");
    confirm_msg = "confirm-msg" ;
    try{
      confirm_msg = getLabel("confirm-msg") ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    UIEventDetailTab eventDetailTab =  new UIEventDetailTab(TAB_EVENTDETAIL) ;
    addChild(eventDetailTab) ;
    UIEventReminderTab eventReminderTab =  new UIEventReminderTab(TAB_EVENTREMINDER) ;
    addChild(eventReminderTab) ;
    UIEventShareTab eventShareTab =  new UIEventShareTab(TAB_EVENTSHARE) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    
    eventShareTab.addUIFormInput(new UIFormRadioBoxInput(UIEventShareTab.FIELD_SHARE, UIEventShareTab.FIELD_SHARE, getShareValue()) ) ;
    eventShareTab.addUIFormInput(new UIFormRadioBoxInput(UIEventShareTab.FIELD_STATUS, UIEventShareTab.FIELD_STATUS, getStatusValue()) ) ;
    eventShareTab.addUIFormInput(new UIFormRadioBoxInput(UIEventShareTab.FIELD_SEND, UIEventShareTab.FIELD_SEND, CalendarUtils.getSendValue(null)) ) ;
    eventShareTab.addUIFormInput(new UIFormInputInfo(UIEventShareTab.FIELD_INFO, UIEventShareTab.FIELD_INFO, null) ) ;
    //eventShareTab.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ISSENDMAIL, FIELD_ISSENDMAIL, false)) ;
    // TODO cs-839
    //eventShareTab.addUIFormInput(new UIFormTextAreaInput(FIELD_PARTICIPANT, FIELD_PARTICIPANT, null)) ;
    eventShareTab.addUIFormInput(new UIFormTextAreaInput(FIELD_MEETING, FIELD_MEETING, null)) ;
    //eventShareTab.addUIFormInput(new UIFormTextAreaInput(FIELD_INVITATION_NOTE, FIELD_INVITATION_NOTE, null)) ;
    actions = new ArrayList<ActionData>() ;

    ActionData addUser = new ActionData() ;
    addUser.setActionListener("AddParticipant") ;
    addUser.setActionName("AddUser") ;
    addUser.setActionParameter(TAB_EVENTSHARE);
    addUser.setActionType(ActionData.TYPE_ICON) ;
    addUser.setCssIconClass("AddNewNodeIcon") ;
    actions.add(addUser) ;
    eventShareTab.setActionField(UIEventShareTab.FIELD_INFO, actions) ;
    addChild(eventShareTab) ;
    UIEventAttenderTab eventAttenderTab = new UIEventAttenderTab(TAB_EVENTATTENDER) ;
    addChild(eventAttenderTab) ;
    setSelectedTab(eventDetailTab.getId()) ;
  }
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      System.out.println("Can not find " + getId() + ".label." + id);
      //e.printStackTrace() ;
      return id ;
    }
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
      isAddNew_ = false ;
      calendarEvent_ = eventCalendar ;
      setEventSumary(eventCalendar.getSummary()) ;
      setEventDescription(eventCalendar.getDescription()) ;
      setEventAllDate(CalendarUtils.isAllDayEvent(eventCalendar)) ;
      setEventFromDate(eventCalendar.getFromDateTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      setEventCheckTime(eventCalendar.getFromDateTime()) ;
      setEventToDate(eventCalendar.getToDateTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      setSelectedCalendarId(eventCalendar.getCalendarId()) ;

      // cs-1790
      String eventCategoryId = eventCalendar.getEventCategoryId() ;
      if(!CalendarUtils.isEmpty(eventCategoryId)) {
        UIFormSelectBox selectBox = eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY) ;
        boolean hasEventCategory = false ;
        for (SelectItemOption<String> o : selectBox.getOptions()) {
          if (o.getValue().equals(eventCategoryId)) {
            hasEventCategory = true ;
            break ;
          }
        }
        if (!hasEventCategory){
          selectBox.getOptions().add(new SelectItemOption<String>(eventCalendar.getEventCategoryName(), eventCategoryId)) ;
        }
        setSelectedCategory(eventCategoryId) ;
      }
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
      //TODO cs-764
      setSendOption(eventCalendar.getSendOption());
      setMessage(eventCalendar.getMessage());
      setParticipantStatusValues(eventCalendar.getParticipantStatus());
      getChild(UIEventShareTab.class).setParticipantStatusList(participantStatusList_);
      
      setSelectedEventState(eventCalendar.getEventState()) ;
      setMeetingInvitation(eventCalendar.getInvitation()) ;
      StringBuffer pars = new StringBuffer() ;
      if(eventCalendar.getParticipant() != null) {
        for(String par : eventCalendar.getParticipant()) {
          if(!CalendarUtils.isEmpty(pars.toString())) pars.append(CalendarUtils.BREAK_LINE) ;
          pars.append(par) ;
        }
      }
//    TODO cs-839
      setParticipant(pars.toString()) ;
      //boolean isContains = false ;
      //CalendarService calService = CalendarUtils.getCalendarService();
      /*List<EventCategory> listCategory = 
        calService.getEventCategories(SessionProviderFactory.createSessionProvider(), CalendarUtils.getCurrentUser());
      for(EventCategory eventCat : listCategory) {
        isContains = (eventCalendar.getEventCategoryId() == null || eventCat.getName().equalsIgnoreCase(eventCalendar.getEventCategoryId())) ;
        if(isContains) break ;
      }*/
      if(eventCategoryId != null) {
        /*SelectItemOption<String> item = new SelectItemOption<String>(eventCalendar.getEventCategoryId(), eventCalendar.getEventCategoryId()) ;
        uiSelectBox.getOptions().add(item) ;
        newCategoryId_ = eventCalendar.getEventCategoryId() ;*/
        UIFormSelectBox uiSelectBox = eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY) ;
        if(!isAddNew_ && ! String.valueOf(Calendar.TYPE_PRIVATE).equalsIgnoreCase(calType_) ){
          SelectItemOption<String> item = new SelectItemOption<String>(eventCalendar.getEventCategoryName(), eventCalendar.getEventCategoryId()) ;
          uiSelectBox.getOptions().add(item) ;
          uiSelectBox.setValue(eventCalendar.getEventCategoryId());
          uiSelectBox.setDisabled(true) ;
          eventDetailTab.getUIFormSelectBoxGroup(UIEventDetailTab.FIELD_CALENDAR).setDisabled(true) ;
          eventDetailTab.setActionField(UIEventDetailTab.FIELD_CATEGORY, null) ;
        }
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
      setEventCheckTime(cal.getTime()) ;
      cal.add(java.util.Calendar.MINUTE, (int)calSetting.getTimeInterval()*2) ;
      setEventToDate(cal.getTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      StringBuffer pars = new StringBuffer(CalendarUtils.getCurrentUser()) ;
//    TODO cs-839
      setMeetingInvitation(new String[] { CalendarUtils.getOrganizationService().getUserHandler().findUserByName(pars.toString()).getEmail() }) ;
      setParticipant(pars.toString()) ;
      //TODO cs-764
      setSendOption(calSetting.getSendOption());
      getChild(UIEventShareTab.class).setParticipantStatusList(participantStatusList_);
      attenderTab.updateParticipants(pars.toString());
    }
  }

  private void setEventCheckTime(Date time) {
    UIEventAttenderTab uiAttenderTab = getChildById(TAB_EVENTATTENDER) ;
    uiAttenderTab.calendar_.setTime(time) ;
  }

  public void update(String calType, List<SelectItem> options) throws Exception{
    UIEventDetailTab uiEventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    if(options != null) {
      uiEventDetailTab.getUIFormSelectBoxGroup(UIEventDetailTab.FIELD_CALENDAR).setOptions(options) ;
    }else {
      uiEventDetailTab.getUIFormSelectBoxGroup(UIEventDetailTab.FIELD_CALENDAR).setOptions(getCalendars()) ;
    }
    calType_ = calType ;
  }
  private List<SelectItem> getCalendars() throws Exception {
    return CalendarUtils.getCalendarOption() ;
  }

  public static List<SelectItemOption<String>> getCategory() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<EventCategory> eventCategories = calendarService.getEventCategories(CalendarUtils.getCurrentUser()) ;
    for(EventCategory category : eventCategories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getId())) ;
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
    options.add(new SelectItemOption<String>(ITEM_OUTSIDE, ITEM_OUTSIDE)) ;
    return options ;
  }

  /*private List<SelectItemOption<String>> getSendValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_ALWAYS, ITEM_ALWAYS)) ;
    options.add(new SelectItemOption<String>(ITEM_NERVER, ITEM_NERVER)) ;
    options.add(new SelectItemOption<String>(ITEM_ASK, ITEM_ASK)) ;
    return options ;
  }*/
  
  public String[] getActions() {
    return new String[]{"Save", "Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void updateSelect(String selectField, String value) throws Exception {
  } 

  protected boolean isEventDetailValid(CalendarSetting calendarSetting) throws Exception{
    String dateFormat = calendarSetting.getDateFormat() ;
    String timeFormat = calendarSetting.getTimeFormat() ;
    Date from = null ;
    Date to = null ;

    if(CalendarUtils.isEmpty(getCalendarId())) {
      errorMsg_ = getId() +  ".msg.event-calendar-required" ;
      return false ;
    } 
    if(CalendarUtils.isEmpty(getEventCategory())) {
      errorMsg_ = getId() +  ".msg.event-category-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getEventFormDateValue())) {
      errorMsg_ = getId() +  ".msg.event-fromdate-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getEventToDateValue())){
      errorMsg_ = getId() +  ".msg.event-todate-required" ;
      return false ;
    }
    try {
      from = getEventFromDate(dateFormat, timeFormat) ;
    } catch (Exception e) {
      e.printStackTrace() ;
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    try {
      to = getEventToDate(dateFormat, timeFormat) ;
    } catch (Exception e) {
      e.printStackTrace() ;
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    //if(!getEventAllDate()) {
    /*if(CalendarUtils.isEmpty(getEventToDateValue())){
        errorMsg_ = "UIEventForm.msg.event-todate-required" ;
        return false ;
      } */
    /*try {
        getEventToDate(dateFormat, timeFormat) ;
      } catch (Exception e) {
        e.printStackTrace() ;
        errorMsg_ =  "UIEventForm.msg.event-todate-notvalid" ;
        return false ;
      }*/
    //try {
    if(from.after(to) || from.equals(to)){
      errorMsg_ = "UIEventForm.msg.event-date-time-logic" ;
      return false ;
    }
    /*} catch (Exception e) {
        e.printStackTrace() ;
        errorMsg_ = "UIEventForm.msg.event-date-time-getvalue" ;
        return false ;
      }    */  
    // }
    errorMsg_ = null ;
    return true ;
  }
  private boolean isReminderValid() throws Exception {
    if(getEmailReminder()) {
      if(CalendarUtils.isEmpty(getEmailAddress())) {
        errorMsg_ = "UIEventForm.msg.event-email-required" ;
        return false ;
      }
      else if(!CalendarUtils.isValidEmailAddresses(getEmailAddress())) {
        errorMsg_ = "UIEventForm.msg.event-email-invalid" ;
        errorValues = CalendarUtils.invalidEmailAddresses(getEmailAddress()) ;
        return false ;
      } 
    } 
    errorMsg_ = null ;
    return true ;
  }
//TODO cs-839
 private boolean isParticipantValid() throws Exception {
    if(isSendMail() && getMeetingInvitation() == null) {
      errorMsg_ = "UIEventForm.msg.error-particimant-email-required" ;
      return false ;
    }/*else if (isSendMail()) {
      errorValues = null ;
      //getParticipants() ;
      StringBuilder buider = new StringBuilder("") ;
      for (String par : getParticipantValues().split(CalendarUtils.COMMA)) {
        if (CalendarUtils.getOrganizationService().getUserHandler().findUserByName(par) == null) {
          if (buider.length() > 0) buider.append(", ") ; 
          buider.append(par) ;
        }
      }
      if (buider.length() > 0) {
        errorMsg_ = "UIEventForm.msg.invalid-username" ;
        errorValues = buider.toString() ;
        return false ;
      }
    }*/
    errorMsg_ = null ;
    return true ;
  }
  protected String getEventSumary() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_EVENT).getValue() ;
  }
  protected void setEventSumary(String value) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_EVENT).setValue(value) ;
  }
  protected String getEventDescription() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormTextAreaInput(UIEventDetailTab.FIELD_DESCRIPTION).getValue() ;
  }
  protected void setEventDescription(String value) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormTextAreaInput(UIEventDetailTab.FIELD_DESCRIPTION).setValue(value) ;
  }
  protected String getCalendarId() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    String value = eventDetailTab.getUIFormSelectBoxGroup(UIEventDetailTab.FIELD_CALENDAR).getValue() ;
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
    eventDetailTab.getUIFormSelectBoxGroup(UIEventDetailTab.FIELD_CALENDAR).setValue(value) ;
    oldCalendarId_ = value ;
  }

  protected String getEventCategory() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).getValue() ;
  }
  public void setSelectedCategory(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setValue(value);
  }

  protected Date getEventFromDate(String dateFormat,String timeFormat) throws Exception {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
    UIFormComboBox timeField = eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_FROM_TIME) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    if(getEventAllDate()) {
      DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return CalendarUtils.getBeginDay(df.parse(fromField.getValue())).getTime();
    } 
    DateFormat df = new SimpleDateFormat(dateFormat + " " + timeFormat, locale) ;
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
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    fromField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(timeFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    timeField.setValue(df.format(date)) ;
    eventAttenderTab.setEventFromDate(date, timeFormat) ;
  }

  protected Date getEventToDate(String dateFormat, String timeFormat) throws Exception {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    UIFormComboBox timeField = eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_TO_TIME) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    if(getEventAllDate()) {
      DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
      df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      return CalendarUtils.getEndDay(df.parse(toField.getValue())).getTime();
    } 
    DateFormat df = new SimpleDateFormat(dateFormat + " " + timeFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    return df.parse(toField.getValue() + " " + timeField.getValue()) ;
  }
  protected void setEventToDate(Date date,String dateFormat, String timeFormat) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIEventAttenderTab eventAttenderTab = getChildById(TAB_EVENTATTENDER) ;
    UIFormDateTimePicker toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    UIFormComboBox timeField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO_TIME) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceTempCalendar()) ;
    toField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(timeFormat, locale) ;
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
  public void setEmailReminder(boolean isChecked) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).setChecked(isChecked) ;
  }

  protected String getEmailRemindBefore() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REMIND_BEFORE).getValue() ;
  }
  protected boolean isEmailRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return Boolean.parseBoolean(eventReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.EMAIL_IS_REPEAT).getValue().toString()) ;
  }
  public void setEmailRepeat(Boolean value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.EMAIL_IS_REPEAT).setChecked(value) ;
  }
  protected String getEmailRepeatInterVal() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).getValue() ;
  }
  protected void setEmailRepeatInterVal(long value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
  }
  protected Boolean isPopupRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return Boolean.parseBoolean(eventReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.POPUP_IS_REPEAT).getValue().toString()) ;
  }
  protected void setPopupRepeat(Boolean value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormCheckBoxInput(UIEventReminderTab.POPUP_IS_REPEAT).setChecked(value) ;
  }
  protected String getPopupRepeatInterVal() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
  } 

  public void setEmailRemindBefore(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REMIND_BEFORE).setValue(value) ;
  }

  protected String getEmailAddress() throws Exception {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).getValue() ;
  }
  public void setEmailAddress(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).setValue(value) ;
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
  protected List<Attachment>  getAttachments(String eventId, boolean isAddNew) {
    UIEventDetailTab uiEventDetailTab = getChild(UIEventDetailTab.class) ;
    return uiEventDetailTab.getAttachments() ;
  }

  protected long getTotalAttachment() {
    UIEventDetailTab uiEventDetailTab = getChild(UIEventDetailTab.class) ;
    long attSize = 0 ; 
    for(Attachment att : uiEventDetailTab.getAttachments()) {
      attSize = attSize + att.getSize() ;
    }
    return attSize ;
  }

  protected void setAttachments(List<Attachment> attachment) throws Exception {
    UIEventDetailTab uiEventDetailTab = getChild(UIEventDetailTab.class) ;
    uiEventDetailTab.setAttachments(attachment) ;
    uiEventDetailTab.refreshUploadFileList() ;
  }
  protected void setPopupRepeatInterval(long value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
  } 
  protected void setEventReminders(List<Reminder> reminders){
    for(Reminder rm : reminders) {
      if(Reminder.TYPE_EMAIL.equals(rm.getReminderType())) {
        setEmailReminder(true) ;
        setEmailAddress(rm.getEmailAddress()) ;
        setEmailRepeat(rm.isRepeat()) ;
        setEmailRemindBefore(String.valueOf(rm.getAlarmBefore())) ;
        setEmailRepeatInterVal(rm.getRepeatInterval()) ;
      } else if(Reminder.TYPE_POPUP.equals(rm.getReminderType())) {
        setPopupReminder(true) ;  
        setPopupRepeat(rm.isRepeat()) ;
        setPopupRemindBefore(String.valueOf(rm.getAlarmBefore()));
        setPopupRepeatInterval(rm.getRepeatInterval()) ;
      }  
    }
  }
  protected List<Reminder>  getEventReminders(Date fromDateTime, List<Reminder> currentReminders) throws Exception {
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
      StringBuffer sbAddress = new StringBuffer() ;
      for(String s : getEmailAddress().replaceAll(CalendarUtils.SEMICOLON, CalendarUtils.COMMA).split(CalendarUtils.COMMA)) {
        s = s.trim() ;
        if(CalendarUtils.isEmailValid(s)) {
          if(sbAddress.indexOf(s) < 0) {
            if(sbAddress.length() > 0) sbAddress.append(CalendarUtils.COMMA) ;
            sbAddress.append(s) ;
          }  
        }  
      }
      email.setEmailAddress(sbAddress.toString()) ;
      email.setRepeate(isEmailRepeat()) ;
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
      StringBuffer sb = new StringBuffer() ;
      boolean isExist = false ;
//    TODO cs-839
 /*     if(getParticipants() != null) {
        for(String s : getParticipants()) {
          if(s.equals(CalendarUtils.getCurrentUser())) isExist = true ;
          break ;
        }
        for(String s : getParticipants()) {
          if(sb.length() >0) sb.append(CalendarUtils.COMMA);
          sb.append(s);
        }
      }
*/
      if(!isExist) {
        if(sb.length() >0) sb.append(CalendarUtils.COMMA);
        sb.append(CalendarUtils.getCurrentUser());
      }
      popup.setReminderOwner(sb.toString()) ;
      popup.setReminderType(Reminder.TYPE_POPUP) ;
      popup.setAlarmBefore(Long.parseLong(getPopupReminderTime()));
      popup.setRepeate(isPopupRepeat()) ;
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
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_STATUS).getValue() ;
  }
  public void setSelectedEventState(String value) {
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_STATUS).setValue(value) ;
  }

  protected String getShareType() {
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return  eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_SHARE).getValue()  ;
  }
  
  protected String getSendOption(){
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return  eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_SEND).getValue()  ;
  }
  
  protected void setSendOption(String value){
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_SEND).setValue(value)  ;
  }
  
  public String getMessage() {
    return invitationMsg_;
  }
  
  public void setMessage(String invitationMsg) {
    this.invitationMsg_ = invitationMsg;
  }
  
  protected void setSelectedShareType(String value) {
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_SHARE).setValue(value) ;
  }

  protected String[] getMeetingInvitation() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    String invitation = eventDetailTab.getUIFormTextAreaInput(FIELD_MEETING).getValue() ;
    if(CalendarUtils.isEmpty(invitation)) return null ;
    else return invitation.replace(CalendarUtils.SEMICOLON, CalendarUtils.COMMA).split(CalendarUtils.COMMA) ;
  } 

  protected String getInvitationEmail() {
    //UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
   // String invitation = eventDetailTab.getUIFormTextAreaInput(FIELD_MEETING).getValue() ;
    /* if(CalendarUtils.isEmpty(invitation)) return null ;
    else return invitation.split(CalendarUtils.COMMA) ;*/
    //TODO cs-764
    StringBuilder buider = new StringBuilder("") ;
    for (Entry<String, String> par : participantStatus_.entrySet()) {
      if (buider.length() > 0 && par.getKey().contains("@")) buider.append(CalendarUtils.COMMA) ;
      if(par.getKey().contains("@")) buider.append(par.getKey()) ;
    }
    return buider.toString() ;
    //return invitation ;
  } 
  protected void setMeetingInvitation(String[] values) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    StringBuffer sb = new StringBuffer() ;
    if(values != null) {
      for(String s : values) {
        if(sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
        sb.append(s) ;
      }
    }
    eventDetailTab.getUIFormTextAreaInput(FIELD_MEETING).setValue(sb.toString()) ;
  }
//TODO cs-839
/*
  protected String[] getParticipants() throws Exception {
    String participants = getParticipantValues() ;
    if(CalendarUtils.isEmpty(participants)) return null ;
    else {
      OrganizationService orgService = CalendarUtils.getOrganizationService() ;
      Map<String, String> parMap = new HashMap<String, String> () ;
      for(String user : participants.split(CalendarUtils.COMMA))  {
        if(orgService.getUserHandler().findUserByName(user.trim()) != null) {
          parMap.put(user.trim(), user.trim()) ;
        } else if(CalendarUtils.isValidEmailAddresses(user.trim())) {
          //parMap.put(user.trim(), user.trim()) ;
        }
      }
      return parMap.values().toArray(new String[parMap.values().size()]) ;
    }
  } */
//TODO cs-839
  protected String  getParticipantValues() {
    StringBuilder buider = new StringBuilder("") ;
    for (String par : participants_.keySet()) {
      if (buider.length() > 0) buider.append(CalendarUtils.BREAK_LINE) ;
      buider.append(par) ;
    }
    return buider.toString() ;
  } 
  
  //TODO cs-764
  protected String  getParticipantStatusValues() {
    StringBuilder buider = new StringBuilder("") ;
    for (Entry<String, String> par : participantStatus_.entrySet()) {
      if (buider.length() > 0) buider.append(CalendarUtils.BREAK_LINE) ;
      buider.append(par.getKey()+":"+par.getValue()) ;
    }
    return buider.toString() ;
  }
  
  protected void  setParticipantStatusValues(String[] values) {
    participantStatus_.clear();
    participantStatusList_.clear();
    StringBuilder buider = new StringBuilder("") ;
    for (String par : values) {
      String[] entry = par.split(":");
      if(entry.length>1){
        participantStatus_.put(entry[0], entry[1]);
        participantStatusList_.add(new ParticipantStatus(entry[0],entry[1]));
      }
      else if(entry.length == 1){
        participantStatus_.put(entry[0], STATUS_EMPTY);
        participantStatusList_.add(new ParticipantStatus(entry[0],STATUS_EMPTY));
      }
    }
  }
  
//TODO cs-839
  public void setParticipant(String values) throws Exception{
	  //participants_.clear() ;
	    OrganizationService orgService = CalendarUtils.getOrganizationService() ;
	    StringBuffer sb = new StringBuffer() ;
	    for(String s : values.split(CalendarUtils.BREAK_LINE)) {
	      User user = orgService.getUserHandler().findUserByName(s) ; 
	      if(user != null) {
	        participants_.put(s.trim(), user.getEmail()) ;
	        if(!CalendarUtils.isEmpty(sb.toString())) sb.append(CalendarUtils.BREAK_LINE) ;
	        sb.append(s.trim()) ;
	      }
	    }
	    ((UIEventAttenderTab)getChildById(TAB_EVENTATTENDER)).updateParticipants(getParticipantValues()) ;
  }
  
  public String  getParticipantStatus() {
    StringBuilder buider = new StringBuilder("") ;
    for (String par : participantStatus_.keySet()) {
      if (buider.length() > 0) buider.append(CalendarUtils.BREAK_LINE) ;
      buider.append(par) ;
    }
    return buider.toString() ;
  } 
  
  public void setParticipantStatus(String values) throws Exception{
    //participantStatus_.clear();
      for(String s : values.split(CalendarUtils.BREAK_LINE)) {
        if(s.trim().length()>0)
          if(participantStatus_.put(s.trim(), STATUS_EMPTY) == null)
            participantStatusList_.add(new ParticipantStatus(s.trim(),STATUS_EMPTY));
        }
  }
  /*public void setParticipant(String[] values) throws Exception{
    StringBuffer sb = new StringBuffer() ;
    if(values != null) {
      for(String s : values) {
        if(sb.length() > 0) sb.append(CalendarUtils.BREAK_LINE) ;
        sb.append(s) ;
      }
      participantList_=sb.toString();
      setParticipant(participantList_);
    }
  }*/

  protected SessionProvider getSession() {
    return SessionProviderFactory.createSessionProvider() ;
  }
  protected SessionProvider getSystemSession() {
    return SessionProviderFactory.createSystemProvider() ;
  }

  protected boolean isSendMail() {
    UIFormInputWithActions uiShareTab = getChildById(TAB_EVENTSHARE) ;
    return false ; //uiShareTab.getUIFormCheckBoxInput(FIELD_ISSENDMAIL).isChecked() ;
  }
  /*protected String getInvitationNote() {
    UIFormInputWithActions uiShareTab = getChildById(TAB_EVENTSHARE) ;
    return uiShareTab.getUIFormTextAreaInput(FIELD_INVITATION_NOTE).getValue() ;
  }*/

  protected void sendMail(MailService svr, OrganizationService orSvr,CalendarSetting setting, Account acc, String fromId,  String toId, CalendarEvent event) throws Exception {
    List<Attachment> atts = getAttachments(null, false);
    DateFormat df = new SimpleDateFormat(setting.getDateFormat() + " " + setting.getTimeFormat()) ;
    User invitor = orSvr.getUserHandler().findUserByName(CalendarUtils.getCurrentUser()) ;
    StringBuffer sbSubject = new StringBuffer("["+getLabel("invitation")+"] ") ;
    sbSubject.append(event.getSummary()) ;
    sbSubject.append(" ") ;
    sbSubject.append(df.format(event.getFromDateTime())) ;

    StringBuffer sbBody = new StringBuffer() ;
    sbBody.append("<div style=\"margin: 20px auto; padding: 8px; background: rgb(224, 236, 255) none repeat scroll 0%; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; width: 400px;\">") ;
    sbBody.append("<table style=\"margin: 0px; padding: 0px; border-collapse: collapse; border-spacing: 0px; width: 100%; line-height: 16px;\">") ;
    sbBody.append("<tbody>") ;
    sbBody.append("<tr>") ;
    //Da bo width=60px;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap; \">"+getLabel("fromWho")+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\"> " + invitor.getUserName() +"("+invitor.getEmail()+")" + " </td>") ;
    sbBody.append("</tr>") ;
    //
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_MESSAGE)+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\">" + event.getMessage()+ "</td>") ;
    sbBody.append("</tr>") ;
    
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_EVENT)+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\">" + event.getSummary()+ "</td>") ;
    sbBody.append("</tr>") ;
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_DESCRIPTION)+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\">" + (event.getDescription() != null && event.getDescription().trim().length() > 0 ? event.getDescription() : " ") + "</td>") ;
    sbBody.append("</tr>") ;
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel("when")+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\"> <div>"+getLabel(UIEventDetailTab.FIELD_FROM)+": " +df.format(event.getFromDateTime())+"</div>");
    sbBody.append("<div>"+getLabel(UIEventDetailTab.FIELD_TO)+": "+df.format(event.getToDateTime())+"</div></td>") ;
    sbBody.append("</tr>") ;
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_PLACE)+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\">" + (event.getLocation() != null && event.getLocation().trim().length() > 0 ? event.getLocation(): " ") + "</td>") ;
    sbBody.append("</tr>") ;
    sbBody.append("<tr>") ;
//  TODO cs-839
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(FIELD_MEETING)+"</td>") ;
    //cs-2407
    //cs-764
    toId = toId.replace(CalendarUtils.BREAK_LINE, CalendarUtils.COMMA);
    if (CalendarUtils.isEmpty(getInvitationEmail())) {
      sbBody.append("<td style=\"padding: 4px;\">" +toId + "</td>") ;
    } else {
      String newInvi = getInvitationEmail().replace(",", ", ") ;
      sbBody.append("<td style=\"padding: 4px;\">" +toId + ", " + newInvi + "</td>") ;
    }    
    sbBody.append("</tr>");
    if(!atts.isEmpty()){
      sbBody.append("<tr>");
      sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_ATTACHMENTS)+":</td>");  
      StringBuffer sbf = new StringBuffer();
      for(Attachment att : atts) {
        if(sbf.length() > 0) sbf.append(",") ;
        sbf.append(att.getName());
      }
      sbBody.append("<td style=\"padding: 4px;\"> ("+atts.size()+") " +sbf.toString()+" </td>");
      sbBody.append("</tr>");
    }
    sbBody.append("</tbody>");
    sbBody.append("</table>");
    sbBody.append("</div>") ;    

    StringBuffer sbAddress = new StringBuffer() ;
//  TODO cs-839
   /* for(String s : toId.split(CalendarUtils.COMMA)) {
      s = s.trim() ;
      User reciver = orSvr.getUserHandler().findUserByName(s.trim()) ;
      if(reciver.getEmail() != null)
        if(!CalendarUtils.isEmpty(sbAddress.toString())) sbAddress.append(CalendarUtils.COMMA) ;
      sbAddress.append(reciver.getEmail()) ;
    }*/
    if(event.getInvitation()!= null) {
      for(String s : event.getInvitation()) {
        s = s.trim() ; 
        if(sbAddress.length() > 0) sbAddress.append(",") ;
        sbAddress.append(s) ;
      }
    }    

    //TODO cs-764
    OrganizationService orgService = CalendarUtils.getOrganizationService() ;
    StringBuffer sb = new StringBuffer() ;
    for(String s : toId.split(CalendarUtils.COMMA)) {
      User user = orgService.getUserHandler().findUserByName(s) ; 
      if(user != null) {
        if(!CalendarUtils.isEmpty(sb.toString())) sb.append(CalendarUtils.COMMA) ;
        sb.append(user.getEmail()) ;
      }
    }
    if(sbAddress.length() > 0 && sb.toString().trim().length() > 0 ) sbAddress.append(",") ;
    sbAddress.append(sb.toString().trim()) ;
    
    StringBuffer values = new StringBuffer(fromId) ; 
    User user = orSvr.getUserHandler().findUserByName(fromId) ;

    values.append(CalendarUtils.SEMICOLON + " ") ; 
    values.append(toId) ;
    values.append(CalendarUtils.SEMICOLON + " ") ;
    values.append(event.getCalType()) ;
    values.append(CalendarUtils.SEMICOLON + " ") ;
    values.append(event.getCalendarId()) ;
    values.append(CalendarUtils.SEMICOLON + " ") ;
    values.append(event.getId()) ;
    if (acc != null) { // use cs-mail service
      Message message = new Message() ;
      message.setSubject(sbSubject.toString()) ;
      message.setMessageBody(sbBody.toString()) ;
      message.setMessageTo(sbAddress.toString()) ;
      message.setContentType(Utils.MIMETYPE_TEXTHTML) ;
      message.setFrom(user.getEmail()) ;
      message.setHeader(CalendarUtils.EXO_INVITATION , values.toString()) ;
      message.setSendDate(new Date()) ;
      List<org.exoplatform.mail.service.Attachment> attachments = new ArrayList<org.exoplatform.mail.service.Attachment>() ;
      try {
        CalendarService calService = CalendarUtils.getCalendarService() ;
        OutputStream out = calService.getCalendarImportExports(CalendarServiceImpl.ICALENDAR).exportEventCalendar(getSystemSession(), fromId, event.getCalendarId(), event.getCalType(), event.getId()) ;
        ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
        BufferAttachment bf = new BufferAttachment() ;
        bf.setInputStream(is) ;
        bf.setName("icalendar.ics");
        bf.setMimeType("text/calendar") ;

        attachments.add(bf) ;
        for(Attachment att : atts) {
          bf = new BufferAttachment() ;
          bf.setInputStream(att.getInputStream()) ;
          bf.setName(att.getName());
          bf.setMimeType(att.getMimeType()) ;
          attachments.add(bf) ;
        }
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      message.setAttachements(attachments) ;      
      svr.sendMessage(user.getUserName(), acc.getId(), message) ;
      
      // TODO cs-1141
      ContactService contactService = (ContactService)PortalContainer.getComponent(ContactService.class) ;
      contactService.saveAddress(CalendarUtils.getCurrentUser(), sbAddress.toString()) ;
    } else { // use kernel service
      org.exoplatform.services.mail.Message  message = new org.exoplatform.services.mail.Message(); 
      message.setSubject(sbSubject.toString()) ;
      message.setBody(sbBody.toString()) ;
      message.setTo(sbAddress.toString()) ;
      message.setMimeType(Utils.MIMETYPE_TEXTHTML) ;
      message.setFrom(user.getEmail()) ;
      org.exoplatform.services.mail.Attachment attachmentCal = new org.exoplatform.services.mail.Attachment() ;
      try {
        CalendarService calService = CalendarUtils.getCalendarService() ;
        OutputStream out = calService.getCalendarImportExports(CalendarServiceImpl.ICALENDAR).exportEventCalendar(getSystemSession(), fromId, event.getCalendarId(), event.getCalType(), event.getId()) ;
        ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
        attachmentCal.setInputStream(is) ;
        attachmentCal.setMimeType("text/calendar") ;
        message.addAttachment(attachmentCal) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      if(!atts.isEmpty()){
        for(Attachment att : atts) {
          org.exoplatform.services.mail.Attachment attachment = new org.exoplatform.services.mail.Attachment() ;
          attachment.setInputStream(att.getInputStream()) ;
          attachment.setMimeType(att.getMimeType()) ;
          message.addAttachment(attachment) ;
        }
      }
      org.exoplatform.services.mail.MailService mService = getApplicationComponent(org.exoplatform.services.mail.impl.MailServiceImpl.class) ;
      mService.sendMessage(message) ;
//    TODO cs-1141
      ContactService contactService = (ContactService)PortalContainer.getComponent(ContactService.class) ;
      contactService.saveAddress(CalendarUtils.getCurrentUser(), sbAddress.toString()) ;
    }
  }


  public String cleanValue(String values) throws Exception{
	  String[] tmpArr = values.split(",");
      List<String> list = Arrays.asList(tmpArr);
      java.util.Set<String> set = new java.util.HashSet<String>(list);
      String[] result = new String[set.size()];
      set.toArray(result);
      String data = "";
      for (String s : result) {
          data += "," + s;
      }
      data = data.substring(1);
	  return data;
  }
public Attachment getAttachment(String attId) {
    UIEventDetailTab uiDetailTab = getChildById(TAB_EVENTDETAIL) ;
    for (Attachment att : uiDetailTab.getAttachments()) {
      if(att.getId().equals(attId)) {
        return att ;
      }
    }
    return null;
  }

  public List<ParticipantStatus> getParticipantStatusList() {
    
  return participantStatusList_;
}

  
  public void SaveAndNoAsk(Event<UIEventForm> event, boolean isSend)throws Exception {
    UIEventForm uiForm = event.getSource() ;
    UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
    UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
    UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
    UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
    CalendarSetting calSetting = calendarPortlet.getCalendarSetting() ;
    CalendarService calService = CalendarUtils.getCalendarService() ;
    String summary = uiForm.getEventSumary().trim() ;
    if(!CalendarUtils.isNameValid(summary, CalendarUtils.SIMPLECHARACTER)){
      uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.summary-invalid", CalendarUtils.SIMPLECHARACTER, ApplicationMessage.WARNING) ) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;
    }
    String location = uiForm.getEventPlace() ;
    if(!CalendarUtils.isEmpty(location)) location = location.replaceAll(CalendarUtils.GREATER_THAN, "").replaceAll(CalendarUtils.SMALLER_THAN,"") ;
    String description = uiForm.getEventDescription() ;
    if(!CalendarUtils.isEmpty(description)) description = description.replaceAll(CalendarUtils.GREATER_THAN, "").replaceAll(CalendarUtils.SMALLER_THAN,"") ;
    if(!uiForm.isEventDetailValid(calSetting)) {
      uiApp.addMessage(new ApplicationMessage(uiForm.errorMsg_, null));
      uiForm.setSelectedTab(TAB_EVENTDETAIL) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;
    } else {
      if(!uiForm.isReminderValid()) {
        uiApp.addMessage(new ApplicationMessage(uiForm.errorMsg_, new String[] {uiForm.errorValues} ));
        uiForm.setSelectedTab(TAB_EVENTREMINDER) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else {
//      TODO cs-839
        if(!uiForm.isParticipantValid()) {
          uiApp.addMessage(new ApplicationMessage(uiForm.errorMsg_, new String[] { uiForm.errorValues }));
          uiForm.setSelectedTab(TAB_EVENTSHARE) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }else {
          String username = CalendarUtils.getCurrentUser() ;
          String calendarId = uiForm.getCalendarId() ;
          Date from = uiForm.getEventFromDate(calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
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

          Calendar currentCalendar = null ;
          if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
            currentCalendar = calService.getUserCalendar(username, calendarId) ; 
          } else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)) {
            GroupCalendarData gCalendarData = calService.getSharedCalendars(username, true) ;
            if( gCalendarData!= null && gCalendarData.getCalendarById(calendarId) != null) currentCalendar = gCalendarData.getCalendarById(calendarId) ;
          } else  if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)) {
            currentCalendar = calService.getGroupCalendar(calendarId) ;
          }
          if(currentCalendar == null) {
            uiPopupAction.deActivate() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
            //event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet) ;
            uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          } else {
            boolean canEdit = false ;
            if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)) {
              canEdit = CalendarUtils.canEdit(null, currentCalendar.getEditPermission(), username) ;
            } else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)) {
              canEdit = CalendarUtils.canEdit(CalendarUtils.getOrganizationService(), currentCalendar.getEditPermission(), username) ;
            }
            if(!canEdit && !uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE) ) {
              uiPopupAction.deActivate() ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
              uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null,1));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
          }
          CalendarEvent calendarEvent  = null ; 
          CalendarEvent oldCalendarEvent = null;
//        TODO cs-839
          String[] pars = uiForm.getParticipantValues().split(CalendarUtils.BREAK_LINE) ;
          String eventId = null ;
          if(uiForm.isAddNew_){
            calendarEvent = new CalendarEvent() ;
          } else {
            calendarEvent = uiForm.calendarEvent_ ;
            oldCalendarEvent = new CalendarEvent();
            oldCalendarEvent.setSummary(calendarEvent.getSummary());
            oldCalendarEvent.setDescription(calendarEvent.getDescription());
            oldCalendarEvent.setLocation(calendarEvent.getLocation());
            oldCalendarEvent.setFromDateTime(calendarEvent.getFromDateTime());
            oldCalendarEvent.setToDateTime(calendarEvent.getToDateTime());
          }
          calendarEvent.setFromDateTime(from) ;
          calendarEvent.setToDateTime(to);
          
          //TODO cs-764
          calendarEvent.setSendOption(uiForm.getSendOption());
          calendarEvent.setMessage(uiForm.getMessage());
          String[] parStatus = uiForm.getParticipantStatusValues().split(CalendarUtils.BREAK_LINE) ;
          calendarEvent.setParticipantStatus(parStatus);
          
//        TODO cs-839
          calendarEvent.setParticipant(pars) ;
          if(CalendarUtils.isEmpty(uiForm.getInvitationEmail())) calendarEvent.setInvitation(null) ;
          else 
            if(CalendarUtils.isValidEmailAddresses(uiForm.getInvitationEmail())) {
              String addressList = uiForm.getInvitationEmail().replaceAll(CalendarUtils.SEMICOLON,CalendarUtils.COMMA) ;
              Map<String, String> emails = new LinkedHashMap<String, String>() ;
              for(String email : addressList.split(CalendarUtils.COMMA)) {
                String address = email.trim() ;
                if (!emails.containsKey(address)) emails.put(address, address) ;
              }
              if(!emails.isEmpty()) calendarEvent.setInvitation(emails.keySet().toArray(new String[emails.size()])) ;
            } else {
              uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.event-email-invalid"
                , new String[] { CalendarUtils.invalidEmailAddresses(uiForm.getInvitationEmail())}));
              uiForm.setSelectedTab(TAB_EVENTSHARE) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
          calendarEvent.setCalendarId(uiForm.getCalendarId()) ;
          calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
          calendarEvent.setSummary(summary) ;
          calendarEvent.setDescription(description) ;
          calendarEvent.setCalType(uiForm.calType_) ;
          calendarEvent.setCalendarId(calendarId) ;
          calendarEvent.setEventCategoryId(uiForm.getEventCategory()) ;
          //if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {c
          //  String eventCategoryName = calService.getEventCategory(uiForm.getSession(), username, uiForm.getEventCategory()).getName() ;
          //  calendarEvent.setEventCategoryName(eventCategoryName) ;              
          UIFormSelectBox selectBox = ((UIFormInputWithActions)uiForm.getChildById(TAB_EVENTDETAIL))
          .getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY) ;
          for (SelectItemOption<String> o : selectBox.getOptions()) {
            if (o.getValue().equals(selectBox.getValue())) {
              calendarEvent.setEventCategoryName(o.getLabel()) ;
              break ;
            }
          }              
          //}
          calendarEvent.setLocation(location) ;
          calendarEvent.setRepeatType(uiForm.getEventRepeat()) ;
          calendarEvent.setPriority(uiForm.getEventPriority()) ; 
          calendarEvent.setPrivate(UIEventForm.ITEM_PRIVATE.equals(uiForm.getShareType())) ;
          calendarEvent.setEventState(uiForm.getEventState()) ;
          calendarEvent.setAttachment(uiForm.getAttachments(calendarEvent.getId(), uiForm.isAddNew_)) ;
          calendarEvent.setReminders(uiForm.getEventReminders(from, calendarEvent.getReminders())) ;
          eventId = calendarEvent.getId() ;
          CalendarView calendarView = (CalendarView)uiViewContainer.getRenderedChild() ;
          try {
            if(uiForm.isAddNew_){
              if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
                calService.saveUserEvent(username, calendarId, calendarEvent, uiForm.isAddNew_) ;
              }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
                calService.saveEventToSharedCalendar(username , calendarId, calendarEvent, uiForm.isAddNew_) ;
              }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
                calService.savePublicEvent(calendarId, calendarEvent, uiForm.isAddNew_) ;          
              }
            } else  {
              String fromCal = uiForm.oldCalendarId_.split(CalendarUtils.COLON)[1].trim() ;
              String toCal = uiForm.newCalendarId_.split(CalendarUtils.COLON)[1].trim() ;
              String fromType = uiForm.oldCalendarId_.split(CalendarUtils.COLON)[0].trim() ;
              String toType = uiForm.newCalendarId_.split(CalendarUtils.COLON)[0].trim() ;
              /*if(uiForm.newCategoryId_ != null) {
                EventCategory evc = new EventCategory() ;
                evc.setName(uiForm.newCategoryId_ ) ;
                calService.saveEventCategory(uiForm.getSession(), username, evc, null, true) ;
                uiViewContainer.updateCategory() ;
              }*/
              List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
              listEvent.add(calendarEvent) ;
              calService.moveEvent(fromCal, toCal, fromType, toType, listEvent, username) ;

              // hung.hoang
              if(calendarView instanceof UIListContainer) {
                UIListContainer uiListContainer = (UIListContainer)calendarView ;
                if (uiListContainer.isDisplaySearchResult() && calendarEvent.getAttachment() != null) {
                  UIPreview uiPreview = uiListContainer.getChild(UIPreview.class) ;
                  EventQuery eventQuery = new EventQuery() ;
                  eventQuery.setCalendarId(new String[] {calendarEvent.getCalendarId()}) ;
                  eventQuery.setEventType(calendarEvent.getEventType()) ;
                  eventQuery.setCategoryId(new String[] {calendarEvent.getEventCategoryId()}) ;

                  UIListView listView = uiListContainer.getChild(UIListView.class) ;
                  List<CalendarEvent> list = calService. getEvents(
                                                                   username, eventQuery, listView.getPublicCalendars()) ;
                  for (CalendarEvent ev : list) {
                    if (ev.getId().equals(calendarEvent.getId())) {
                      if (listView.getDataMap().containsKey(ev.getId())) {
                        listView.getDataMap().put(ev.getId(), ev) ;
                        if (uiPreview.getEvent().getId().equals(ev.getId())) {
                          uiPreview.setEvent(ev) ; 
                        }
                      }                        
                      break ;
                    }
                  }                    
                }
              }
            }

            if(calendarView instanceof UIListContainer) {
              UIListContainer uiListContainer = (UIListContainer)calendarView ;
              if (!uiListContainer.isDisplaySearchResult()) {
                uiViewContainer.refresh() ;
              }
            } else {
              uiViewContainer.refresh() ;
            }  
            calendarView.setLastUpdatedEventId(eventId) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
            UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
            uiPopupAction.deActivate() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
          }catch (Exception e) {
            uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.add-event-error", null));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            e.printStackTrace() ;
          }
          if(calendarEvent != null && uiForm.isSendMail()) {
            Account acc = CalendarUtils.getMailService().getDefaultAccount(username);
            //if(acc != null) {
            try {
          /*    StringBuffer recive = new StringBuffer() ; 
              
//            TODO cs-839
              for(String rc : uiForm.getParticipantValues().split(CalendarUtils.COMMA)) {
                rc = rc.trim() ;                                 
                if(recive.length() > 0) recive.append(CalendarUtils.COMMA) ;
                recive.append(rc) ;
              }*/
              uiForm.sendMail(CalendarUtils.getMailService(), CalendarUtils.getOrganizationService(), calSetting, acc, username, uiForm.getParticipantValues(), calendarEvent) ;
            } catch (Exception e) {
              uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.error-send-email", null));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              e.printStackTrace() ;
            }
            /* } else {
              uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.cant-send-email", null));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            }*/
          }
          //TODO cs-764
          else {
            if(calendarEvent != null && isSend){
              Account acc = CalendarUtils.getMailService().getDefaultAccount(username);
                if(oldCalendarEvent!=null){
                if(oldCalendarEvent.getSummary()!=null && !oldCalendarEvent.getSummary().equalsIgnoreCase(calendarEvent.getSummary())||calendarEvent.getSummary()!=null && !calendarEvent.getSummary().equalsIgnoreCase(oldCalendarEvent.getSummary()))
                  uiForm.isChangedSignificantly = true;
                if(oldCalendarEvent.getDescription()!=null && !oldCalendarEvent.getDescription().equalsIgnoreCase(calendarEvent.getDescription())||calendarEvent.getDescription()!=null && !calendarEvent.getDescription().equalsIgnoreCase(oldCalendarEvent.getDescription()))
                  uiForm.isChangedSignificantly = true;
                if(oldCalendarEvent.getLocation()!=null && !oldCalendarEvent.getLocation().equalsIgnoreCase(calendarEvent.getLocation())||calendarEvent.getLocation()!=null && !calendarEvent.getLocation().equalsIgnoreCase(oldCalendarEvent.getLocation()))
                  uiForm.isChangedSignificantly = true;
                if(!oldCalendarEvent.getFromDateTime().equals(calendarEvent.getFromDateTime()))
                    uiForm.isChangedSignificantly = true;
                if(!oldCalendarEvent.getToDateTime().equals(calendarEvent.getToDateTime()))
                  uiForm.isChangedSignificantly = true;
                }                
                if(uiForm.isAddNew_||uiForm.isChangedSignificantly){
                  try {
                    uiForm.sendMail(CalendarUtils.getMailService(), CalendarUtils.getOrganizationService(), calSetting, acc, username, uiForm.getParticipantValues(), calendarEvent) ;
                  }catch (Exception e) {
                    uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.error-send-email", null));
                    event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                    e.printStackTrace() ;
                  }                  
                  Map<String, String> parsUpdated = new HashMap<String, String>() ;
                  for(String parSt : calendarEvent.getParticipantStatus()) {
                    String[] entry = parSt.split(":");
                    parsUpdated.put(entry[0],STATUS_PENDING);
                  }
                  Map<String, String> participant = new HashMap<String, String>() ;
                  for (Entry<String, String> par : parsUpdated.entrySet()) {
                    participant.put(par.getKey()+":"+par.getValue(),"") ;
                  }
                  calendarEvent.setParticipantStatus(participant.keySet().toArray(new String[participant.keySet().size()]));
                  if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
                    calService.saveUserEvent(username, calendarId, calendarEvent, false) ;
                  }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
                    calService.saveEventToSharedCalendar(username , calendarId, calendarEvent, false) ;
                  }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
                    calService.savePublicEvent(calendarId, calendarEvent, false) ;          
                  }

                }
                else {
                  //select new Invitation email
                  Map<String, String> invitations = new LinkedHashMap<String, String>() ;
                  for(String s: calendarEvent.getInvitation())
                    invitations.put(s, s);
                  for(String parSt : calendarEvent.getParticipantStatus()) {
                    String[] entry = parSt.split(":");
                    //is old
                    if(entry.length > 1 && entry[0].contains("@"))
                      invitations.remove(entry[0]);
                  }
                  calendarEvent.setInvitation(invitations.keySet().toArray(new String[invitations.size()])) ;
                  //select new User
                  StringBuilder buider = new StringBuilder("") ;
                  for(String parSt : calendarEvent.getParticipantStatus()) {
                    String[] entry = parSt.split(":");
                    //is new
                    if((entry.length == 1) && (!entry[0].contains("@"))){
                      if (buider.length() > 0) buider.append(CalendarUtils.BREAK_LINE) ;
                      buider.append(entry[0]) ;
                    }
                  }
                  
                  if(buider.toString().trim().length()>0 || invitations.size() > 0){
                    try {
                    uiForm.sendMail(CalendarUtils.getMailService(), CalendarUtils.getOrganizationService(), calSetting, acc, username, buider.toString(), calendarEvent) ;
                    }catch (Exception e) {
                      uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.error-send-email", null));
                      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                      e.printStackTrace() ;
                    }                    
                    Map<String, String> parsUpdated = new HashMap<String, String>() ;
                    for(String parSt : calendarEvent.getParticipantStatus()) {
                      String[] entry = parSt.split(":");
                      if(entry.length > 1)
                        parsUpdated.put(entry[0],entry[1]);
                      else
                        parsUpdated.put(entry[0], STATUS_PENDING);
                    }
                    Map<String, String> participant = new HashMap<String, String>() ;
                    for (Entry<String, String> par : parsUpdated.entrySet()) {
                      participant.put(par.getKey()+":"+par.getValue(),"") ;
                    }
                    calendarEvent.setParticipantStatus(participant.keySet().toArray(new String[participant.keySet().size()]));
                    if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
                      calService.saveUserEvent(username, calendarId, calendarEvent, false) ;
                    }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
                      calService.saveEventToSharedCalendar(username , calendarId, calendarEvent, false) ;
                    }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
                      calService.savePublicEvent(calendarId, calendarEvent, false) ;          
                    }
                 }
                }
              
            }
          }
        }
      }
    }
    UIEventDetailTab uiDetailTab = uiForm.getChildById(TAB_EVENTDETAIL) ;
    for (Attachment att : uiDetailTab.getAttachments()) {
      UIAttachFileForm.removeUploadTemp(uiForm.getApplicationComponent(UploadService.class), att.getResourceId()) ;
    }
  }
  
  static  public class AddCategoryActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      UIEventCategoryManager categoryMan =  uiChildPopup.activate(UIEventCategoryManager.class, 470) ;
      categoryMan.categoryId_ = uiForm.getEventCategory() ;
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
        UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
        UIPopupAction uiPopupAction  = uiPopupContainer.getChild(UIPopupAction.class) ;
        UIAddressForm uiAddressForm = uiPopupAction.activate(UIAddressForm.class, 640) ;
        uiAddressForm.setContactList("") ;
        String oldAddress = uiForm.getEmailAddress() ;
        List<Contact> allContact = new ArrayList<Contact>() ;
        ContactService contactService = uiAddressForm.getApplicationComponent(ContactService.class) ;
        String username = CalendarUtils.getCurrentUser() ;
        DataPageList dataList = contactService.searchContact(username, new ContactFilter()) ;
        allContact = dataList.getAll() ;
        if(!allContact.isEmpty()) {
          if(!CalendarUtils.isEmpty(oldAddress)) {
            for(String address : oldAddress.split(",")) {
              for(Contact c : allContact){
                if(!CalendarUtils.isEmpty(c.getEmailAddress())) {
                  if(Arrays.asList(c.getEmailAddress().split(";")).contains(address.trim())) {
                    uiAddressForm.checkedList_.put(c.getId(), c) ;
                  }
                }
              }
            }
          }
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }
  }
  static  public class AddAttachmentActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      UIAttachFileForm uiAttachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      uiAttachFileForm.setAttSize(uiForm.getTotalAttachment()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class RemoveAttachmentActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      if(uiContainer != null) uiContainer.deActivate() ;
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
  static  public class DownloadAttachmentActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      String attId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Attachment attach = uiForm.getAttachment(attId) ;
      if(attach != null) {
        String mimeType = attach.getMimeType().substring(attach.getMimeType().indexOf("/")+1) ;
        DownloadResource dresource = new InputStreamDownloadResource(attach.getInputStream(), mimeType);
        DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class);
        dresource.setDownloadName(attach.getName());
        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTDETAIL)) ;
      }
    }
  }

  static  public class AddParticipantActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventAttenderTab tabAttender = uiForm.getChildById(TAB_EVENTATTENDER) ;
//    TODO cs-839
      String values = uiForm.getParticipantValues() ;
      tabAttender.updateParticipants(values) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(tabAttender) ;       
      
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      //uiContainer.addChild(UIInvitationForm.class) ;
      UIPopupAction uiPopupAction = uiContainer.getChild(UIPopupAction.class);
      UIPopupContainer uiInvitationContainer= uiPopupAction.createUIComponent(UIPopupContainer.class, null, "UIInvitationContainer");
      uiInvitationContainer.getChild(UIPopupAction.class).setId("UIPopupAction3") ;
      uiInvitationContainer.getChild(UIPopupAction.class).getChild(UIPopupWindow.class).setId("UIPopupWindow");
      UIInvitationForm uiInvitationForm = uiInvitationContainer.addChild(UIInvitationForm.class, null, null);
      uiInvitationForm.setInvitationMsg(uiForm.invitationMsg_) ;
      uiForm.participantList_ = new String("");
      uiInvitationForm.setParticipantValue(uiForm.participantList_) ;
      uiPopupAction.activate(uiInvitationContainer, 500, 0, true);
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiInvitationContainer);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      
      
     /* UIPopupContainer uiContainer2 = uiContainer.getChild(UIPopupAction.class).addChild(UIPopupContainer.class, null, null)  ;
      uiContainer2.getChild(UIPopupAction.class).setId("newId") ;
      uiContainer2.getChild(UIPopupAction.class).getChild(UIPopupWindow.class).setId("newIdWindow") ;*/
      
      /*UIPopupWindow uiPopupWindow = uiContainer2.getChild(UIPopupWindow.class) ;
      if(uiPopupWindow == null)uiPopupWindow = uiContainer.addChild(UIPopupWindow.class, "UIPopupWindowUserSelectEventForm", "UIPopupWindowUserSelectEventForm") ;
      UIUserSelector uiUserSelector = uiContainer2.createUIComponent(UIUserSelector.class, null, null) ;
      uiUserSelector.setShowSearch(true);
      uiUserSelector.setShowSearchUser(true) ;
      uiUserSelector.setShowSearchGroup(true);
      uiPopupWindow.setUIComponent(uiUserSelector);
      uiPopupWindow.setShow(true);
      uiPopupWindow.setWindowSize(740, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer2) ;      */
    }
  }
  static  public class AddActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UIEventForm uiEventForm = uiContainer.getChild(UIEventForm.class);
      UIEventShareTab uiEventShareTab =  uiEventForm.getChild(UIEventShareTab.class);
      Long currentPage = uiEventShareTab.getCurrentPage();
//    TODO cs-839
      //String currentValues = uiEventForm.getParticipantValues();
      String values = uiUserSelector.getSelectedUsers();
      List<String> currentEmails = new ArrayList<String>() ;
      String [] invitors = uiEventForm.getMeetingInvitation() ;
      if (invitors != null) currentEmails.addAll(Arrays.asList(invitors)) ;
      for (String value : values.split(CalendarUtils.COMMA)) {
        String email = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(value).getEmail() ;
        if (!currentEmails.contains(email)) currentEmails.add(email) ;
        if (!uiEventForm.participants_.keySet().contains(value)) {
          uiEventForm.participants_.put(value, email) ;
        }
      }
      for(Entry<String,String> entry : uiEventForm.participants_.entrySet()){
        if(!uiEventForm.participantStatus_.keySet().contains(entry.getKey())){
          if(uiEventForm.participantStatus_.put(entry.getKey(),STATUS_EMPTY)==null)
            uiEventForm.participantStatusList_.add(uiEventForm.new ParticipantStatus(entry.getKey(),STATUS_EMPTY));
        }
      }
      uiEventShareTab.setParticipantStatusList(uiEventForm.getParticipantStatusList());
      uiEventShareTab.updateCurrentPage(currentPage.intValue());
      ((UIEventAttenderTab)uiEventForm.getChildById(TAB_EVENTATTENDER)).updateParticipants(uiEventForm.getParticipantValues()) ; 
      uiEventForm.setMeetingInvitation(currentEmails.toArray(new String[currentEmails.size()])) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }
  static  public class AddUserActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupContainer uiPopupContainer = uiForm.getParent();  
      uiPopupContainer.deActivate();
      UIPopupWindow uiPopupWindow = uiPopupContainer.getChildById("UIPopupWindowAddUserEventForm") ;
      /*if(uiPopupWindow != null) {
        uiPopupWindow.setShow(false) ;
        uiPopupWindow.setUIComponent(null) ;
      }*/
      if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, "UIPopupWindowAddUserEventForm", "UIPopupWindowAddUserEventForm") ;
      UIUserSelector uiUserSelector = uiPopupContainer.createUIComponent(UIUserSelector.class, null, null) ;
      uiUserSelector.setShowSearch(true);
      uiUserSelector.setShowSearchUser(true) ;
      uiUserSelector.setShowSearchGroup(true);
      uiPopupWindow.setUIComponent(uiUserSelector);
      uiPopupWindow.setShow(true);
      uiPopupWindow.setWindowSize(740, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUseSelector = event.getSource() ;
      UIPopupWindow uiPoupPopupWindow = uiUseSelector.getParent() ;
      UIPopupContainer uiContainer = uiPoupPopupWindow.getAncestorOfType(UIPopupContainer.class) ;
      uiPoupPopupWindow.setUIComponent(null) ;
      uiPoupPopupWindow.setShow(false) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;  
    }
  }
  static  public class MoveNextActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventDetailTab uiEventDetailTab = uiForm.getChildById(TAB_EVENTDETAIL) ;
      UIEventAttenderTab uiEventAttenderTab =  uiForm.getChildById(TAB_EVENTATTENDER) ;
      uiEventAttenderTab.moveNextDay() ;
      if(uiEventAttenderTab.isCheckFreeTime()) {
        uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_FROM).setCalendar(uiEventAttenderTab.calendar_) ;
        uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_TO).setCalendar(uiEventAttenderTab.calendar_) ;
      }
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventDetailTab uiEventDetailTab = uiForm.getChildById(TAB_EVENTDETAIL) ;
      UIEventAttenderTab uiEventAttenderTab =  uiForm.getChildById(TAB_EVENTATTENDER) ;
      uiEventAttenderTab.movePreviousDay() ;
      if(uiEventAttenderTab.isCheckFreeTime()) {
        uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_FROM).setCalendar(uiEventAttenderTab.calendar_) ;
        uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_TO).setCalendar(uiEventAttenderTab.calendar_) ;
      }
      /*uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_FROM).setCalendar(uiEventAttenderTab.calendar_) ;
      uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_TO).setCalendar(uiEventAttenderTab.calendar_) ;*/
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class DeleteUserActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventAttenderTab tabAttender = uiForm.getChildById(TAB_EVENTATTENDER) ;
//    TODO cs-839
      String values = uiForm.getParticipantValues() ;
      tabAttender.updateParticipants(values) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(tabAttender) ;
      StringBuffer newPars = new StringBuffer() ;
      for(String id : tabAttender.getParticipants()){
        UIFormCheckBoxInput input = tabAttender.getUIFormCheckBoxInput(id) ;
        if(input != null) {
          if( input.isChecked()) {
            tabAttender.parMap_.remove(id) ;
            //TODO cs-764
            uiForm.participantStatus_.remove(id);
            for(Iterator<ParticipantStatus> i = uiForm.participantStatusList_.iterator(); i.hasNext();){
              ParticipantStatus participantStatus = i.next();
              if(id.equalsIgnoreCase(participantStatus.getParticipant()))
                i.remove();
            }
            uiForm.participants_.remove(id);
            uiForm.removeChildById(id) ; 
            
            List<String> currentEmails = new ArrayList<String>() ;
            String [] invitors = uiForm.getMeetingInvitation() ;
            if (invitors != null) {
              currentEmails.addAll(Arrays.asList(invitors)) ;
              currentEmails.remove(CalendarUtils.getOrganizationService().getUserHandler().findUserByName(id.trim()).getEmail()) ;
              uiForm.setMeetingInvitation(currentEmails.toArray(new String[currentEmails.size()])) ;
            }
          }else {
            if(!CalendarUtils.isEmpty(newPars.toString())) newPars.append(CalendarUtils.BREAK_LINE) ;
            newPars.append(id) ;
          }
        }
      }
      uiForm.getChild(UIEventShareTab.class).setParticipantStatusList(uiForm.getParticipantStatusList());
//    TODO cs-839
      //uiForm.setParticipant(newPars.toString()) ;
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTATTENDER)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTSHARE)) ;
    }
  }

  static  public class SaveActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarSetting calSetting = calendarPortlet.getCalendarSetting() ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      //TODO cs-764
      if(!uiForm.isReminderValid()) {
        uiApp.addMessage(new ApplicationMessage(uiForm.errorMsg_, new String[] {uiForm.errorValues} ));
        uiForm.setSelectedTab(TAB_EVENTREMINDER) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      else {
      String sendOption = uiForm.getSendOption();
      /*if(sendOption.equalsIgnoreCase(CalendarSetting.ACTION_BYSETTING))
        sendOption = calSetting.getSendOption(); */
      if(CalendarSetting.ACTION_ASK.equalsIgnoreCase(sendOption)){
          // Show Confirm
        UIPopupAction pAction = uiPopupContainer.getChild(UIPopupAction.class) ;
        UIConfirmForm confirmForm =  pAction.activate(UIConfirmForm.class, 300);
        confirmForm.setConfirmMessage(uiForm.confirm_msg);
        confirmForm.setConfig_id(uiForm.getId()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(pAction) ;
      }
      else {
        if(CalendarSetting.ACTION_ALWAYS.equalsIgnoreCase(sendOption))
          uiForm.SaveAndNoAsk(event, true);
        else
          uiForm.SaveAndNoAsk(event, false);
      }
     }
    }
  }
  static  public class OnChangeActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
//    TODO cs-839
      UIEventForm uiForm = event.getSource() ;
      UIEventAttenderTab attendTab = uiForm.getChildById(TAB_EVENTATTENDER) ;
      UIFormInputWithActions eventShareTab = uiForm.getChildById(TAB_EVENTSHARE) ;
      String values = uiForm.getParticipantValues() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      boolean isCheckFreeTime = attendTab.getUIFormCheckBoxInput(UIEventAttenderTab.FIELD_CHECK_TIME).isChecked() ;
      if(CalendarUtils.isEmpty(values)) {
        if(isCheckFreeTime) attendTab.getUIFormCheckBoxInput(UIEventAttenderTab.FIELD_CHECK_TIME).setChecked(false) ;
        uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
        attendTab.updateParticipants(values) ;
        
        uiForm.setParticipant(values) ;
        uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.participant-required", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(eventShareTab) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(attendTab) ;
      } else {
        StringBuilder sb1 = new StringBuilder() ;
        StringBuilder sb2 = new StringBuilder() ;
        for(String uName : values.split(CalendarUtils.BREAK_LINE)) {
          User user = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(uName.trim()) ;
          if(user != null) {
            if(sb1 != null && sb1.length() > 0) sb1.append(CalendarUtils.BREAK_LINE) ;
            sb1.append(uName.trim()) ;
          } else {
            if(sb2 != null && sb2.length() > 0) sb2.append(CalendarUtils.BREAK_LINE) ;
            sb2.append(uName.trim()) ;
          }
        }
        attendTab.updateParticipants(sb1.toString());
        uiForm.setParticipant(values) ;
        if(sb2.length() > 0) {
          if(isCheckFreeTime) attendTab.getUIFormCheckBoxInput(UIEventAttenderTab.FIELD_CHECK_TIME).setChecked(false) ;
          uiApp.addMessage(new ApplicationMessage("UIEventForm.msg.name-not-correct", new Object[]{sb2.toString()}));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(eventShareTab) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(attendTab) ;
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      UIEventDetailTab uiDetailTab = uiForm.getChildById(TAB_EVENTDETAIL) ;
      for (Attachment att : uiDetailTab.getAttachments()) {
        UIAttachFileForm.removeUploadTemp(uiForm.getApplicationComponent(UploadService.class), att.getResourceId()) ;
      }
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class SelectTabActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource()) ;      
    }
  }
  
  static public class ConfirmOKActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiEventForm = event.getSource();
      UIPopupContainer uiPopupContainer = uiEventForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class);
      uiPopupAction.deActivate();
      uiEventForm.SaveAndNoAsk(event, true);
      
    }
  }
  
  static public class  ConfirmCancelActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiEventForm = event.getSource();
      UIPopupContainer uiPopupContainer = uiEventForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class);
      uiPopupAction.deActivate();
      uiEventForm.SaveAndNoAsk(event, false);
    }
  }
  
  
  public class ParticipantStatus {
    private String participant ;
    private String status ;

    public ParticipantStatus(String participant,String status){
      this.participant = participant;
      this.status = status ;
    }

    public String getParticipant() {
      return participant;
    }

    public void setParticipant(String participant) {
      this.participant = participant;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

  }
}
