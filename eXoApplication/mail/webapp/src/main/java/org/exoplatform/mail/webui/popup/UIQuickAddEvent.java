/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 8, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UIQuickAddEvent.gtmpl",
    events = {
      @EventConfig(listeners = UIQuickAddEvent.SaveActionListener.class),
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


  public String calType_ = "0" ;
  private boolean isEvent_ = true ;
  public UIQuickAddEvent() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null).addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormDateTimeInput(FIELD_FROM, FIELD_FROM, new Date(), false).addValidator(EmptyFieldValidator.class));
    addUIFormInput(new UIFormDateTimeInput(FIELD_TO, FIELD_TO, new Date(), false).addValidator(EmptyFieldValidator.class));
    addUIFormInput(new UIFormSelectBox(FIELD_FROM_TIME, FIELD_FROM_TIME, options));
    addUIFormInput(new UIFormSelectBox(FIELD_TO_TIME, FIELD_TO_TIME, options));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ALLDAY, FIELD_ALLDAY, false));
    addUIFormInput(new UIFormSelectBox(FIELD_CALENDAR, FIELD_CALENDAR, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, options)) ;
  }


  public void init(CalendarSetting  calendarSetting, String startTime, String endTime) throws Exception {
    List<SelectItemOption<String>> options = MailUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat()) ;
    getUIFormSelectBox(FIELD_FROM_TIME).setOptions(options) ;
    getUIFormSelectBox(FIELD_TO_TIME).setOptions(options) ;
    java.util.Calendar cal = GregorianCalendar.getInstance() ;
    if(startTime != null) cal.setTimeInMillis(Long.parseLong(startTime)) ;
    else {
      cal.set(java.util.Calendar.MINUTE, (cal.get(java.util.Calendar.MINUTE)/15)*15) ;
    }
    setEventFromDate(cal.getTime()) ;
    if(endTime != null )cal.setTimeInMillis(Long.parseLong(endTime)) ; 
    else {
      cal.add(java.util.Calendar.MINUTE, 15) ;
    }
    setEventToDate(cal.getTime()) ;
  }

  private void setEventFromDate(Date value) {
    UIFormDateTimeInput fromField = getChildById(FIELD_FROM) ;
    UIFormSelectBox timeFile = getChildById(FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat(MailUtils.DATEFORMAT) ;
    fromField.setValue(df.format(value)) ;
    df = new SimpleDateFormat(MailUtils.TIMEFORMAT) ;
    timeFile.setValue(df.format(value)) ;
  }

  private Date getEventFromDate() throws Exception {
    UIFormDateTimeInput fromField = getChildById(FIELD_FROM) ;
    UIFormSelectBox timeFile = getChildById(FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat(MailUtils.DATETIMEFORMAT) ;
    return df.parse(fromField.getValue() + " " + timeFile.getValue() ) ;
  }
  
  private Date getEventToDate() throws Exception {
    UIFormDateTimeInput fromField = getChildById(FIELD_TO) ;
    UIFormSelectBox timeFile = getChildById(FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat(MailUtils.DATETIMEFORMAT) ;
    return df.parse(fromField.getValue() + " " + timeFile.getValue() ) ;
  }
  
  private void setEventToDate(Date value) {
    UIFormDateTimeInput toField =  getChildById(FIELD_TO) ;
    UIFormSelectBox timeField =  getChildById(FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat(MailUtils.DATEFORMAT) ;
    toField.setValue(df.format(value)) ;
    df = new SimpleDateFormat(MailUtils.TIMEFORMAT) ;
    timeField.setValue(df.format(value)) ;
  }

  private List<SelectItemOption<String>> getCalendar() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
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
    } catch (Exception e) { }
    return label ;
  }

  public void setSelectedCalendar(String value) {getUIFormSelectBox(FIELD_CALENDAR).setValue(value) ;}

  public void activate() throws Exception {}
  
  public void deActivate() throws Exception {}
  
  public void setEvent(boolean isEvent) { isEvent_ = isEvent ; }
  
  public boolean isEvent() { return isEvent_ ; }
  
  public void update(String calType, List<SelectItemOption<String>> options) throws Exception{
    if (options != null) {
      getUIFormSelectBox(FIELD_CALENDAR).setOptions(options) ;
    } else {
      getUIFormSelectBox(FIELD_CALENDAR).setOptions(getCalendar()) ;
    }
    calType_ = calType ;
  }
  
  static  public class SaveActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      
    }
  }

  static  public class CancelActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction() ;
    }
  }
}