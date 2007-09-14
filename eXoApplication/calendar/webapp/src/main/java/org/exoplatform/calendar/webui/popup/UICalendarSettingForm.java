/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
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
  final private static String BASE_URL = "baseURL".intern() ;
  final private static String DEFAULT_CALENDARS = "defaultCalendars".intern() ;
  
  private List<Calendar> calendars_ ;
  public UICalendarSettingForm() throws Exception{
    super("UICalendarSettingForm", false) ;
    CalendarService cservice = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    CalendarSetting calendarSetting = cservice.getCalendarSetting(username) ;
    
    UIFormInputWithActions setting = new UIFormInputWithActions("setting").setRendered(true) ;
    
    List<SelectItemOption<String>> viewTypes = new ArrayList<SelectItemOption<String>>() ;
    viewTypes.add(new SelectItemOption<String>("Day view", CalendarSetting.DAY_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Week view", CalendarSetting.WEEK_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Month view", CalendarSetting.MONTH_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Year view", CalendarSetting.YEAR_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Schedule view", CalendarSetting.SCHEDULE_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Events view", CalendarSetting.EVENTS_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Tasks view", CalendarSetting.TASKS_VIEW)) ;
    setting.addUIFormInput(new UIFormSelectBox(VIEW_TYPE, VIEW_TYPE, viewTypes)) ;
    
    List<SelectItemOption<String>> timeInterval = new ArrayList<SelectItemOption<String>>() ;
    int i = 5 ;
    while(i < 121) {
      timeInterval.add(new SelectItemOption<String>(i + " minutes", String.valueOf(i))) ;
      i += 5;
    }
    setting.addUIFormInput(new UIFormSelectBox(TIME_INTERVAL, TIME_INTERVAL, timeInterval)) ;
    
    List<SelectItemOption<String>> weekStartOn = new ArrayList<SelectItemOption<String>>() ;
    weekStartOn.add(new SelectItemOption<String>("Monday", CalendarSetting.MONDAY)) ;
    weekStartOn.add(new SelectItemOption<String>("Tuesday", CalendarSetting.TUESDAY)) ;
    weekStartOn.add(new SelectItemOption<String>("Wendnesday", CalendarSetting.WENDNESDAY)) ;
    weekStartOn.add(new SelectItemOption<String>("Thursday", CalendarSetting.THURSDAY)) ;
    weekStartOn.add(new SelectItemOption<String>("Friday", CalendarSetting.FRIDAY)) ;
    weekStartOn.add(new SelectItemOption<String>("Saturday", CalendarSetting.SATURDAY)) ;
    weekStartOn.add(new SelectItemOption<String>("Sunday", CalendarSetting.SUNDAY)) ;    
    setting.addUIFormInput(new UIFormSelectBox(WEEK_START_ON, WEEK_START_ON, weekStartOn)) ;
    
    List<SelectItemOption<String>> dateFormat = new ArrayList<SelectItemOption<String>>() ;
    dateFormat.add(new SelectItemOption<String>("dd/mm/yyyy", "dd/MM/yyyy")) ;
    dateFormat.add(new SelectItemOption<String>("dd-mm-yyyy", "dd-MM-yyyy")) ;
    dateFormat.add(new SelectItemOption<String>("mm/dd/yyyy", "mm/dd/yyyy")) ;
    dateFormat.add(new SelectItemOption<String>("mm-dd-yyyy", "mm-dd-yyyy")) ;
    setting.addUIFormInput(new UIFormSelectBox(DATE_FORMAT, DATE_FORMAT, dateFormat)) ;
    
    List<SelectItemOption<String>> timeFormat = new ArrayList<SelectItemOption<String>>() ;
    timeFormat.add(new SelectItemOption<String>("24 Hours", " HH:mm:ss")) ;
    timeFormat.add(new SelectItemOption<String>("AM/PM", "  HH:mm aaa")) ;
    
    setting.addUIFormInput(new UIFormSelectBox(TIME_FORMAT, TIME_FORMAT, timeFormat)) ;
    setting.addUIFormInput(new UIFormStringInput(LOCATION, LOCATION, null)) ;
    setting.addUIFormInput(new UIFormStringInput(BASE_URL, BASE_URL, null)) ;
    addUIFormInput(setting) ;
    UIFormInputWithActions defaultCalendars = new UIFormInputWithActions("defaultCalendars") ;    
    calendars_ = cservice.getUserCalendars(username) ;
    List<String> settedCalendars = new ArrayList<String>() ;
    if(calendarSetting != null && calendarSetting.getDefaultPrivateCalendars() != null) {
      settedCalendars = new ArrayList<String>(Arrays.asList(calendarSetting.getDefaultPrivateCalendars())) ;
    }
    for(Calendar calendar : calendars_) {
      UIFormCheckBoxInput checkBox = new UIFormCheckBoxInput(calendar.getName(), calendar.getId(), false) ;
      for(String calendarId : settedCalendars) {
        if(calendar.getId().equals(calendarId)) checkBox.setChecked(true) ;        
      }
      defaultCalendars.addUIFormInput(checkBox) ;
    }
    defaultCalendars.setRendered(false) ;
    addUIFormInput(defaultCalendars) ;
    
    if(calendarSetting != null) {
      setting.getUIFormSelectBox(VIEW_TYPE).setValue(calendarSetting.getViewType()) ;
      setting.getUIFormSelectBox(TIME_INTERVAL).setValue(String.valueOf(calendarSetting.getTimeInterval())) ;
      setting.getUIFormSelectBox(WEEK_START_ON).setValue(calendarSetting.getWeekStartOn()) ;
      setting.getUIFormSelectBox(DATE_FORMAT).setValue(calendarSetting.getDateFormat()) ;
      setting.getUIFormSelectBox(TIME_FORMAT).setValue(calendarSetting.getTimeFormat()) ;
      setting.getUIStringInput(LOCATION).setValue(calendarSetting.getLocation()) ;      
      setting.getUIStringInput(BASE_URL).setValue(calendarSetting.getBaseURL()) ;
    }
  }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  static  public class SaveActionListener extends EventListener<UICalendarSettingForm> {
    public void execute(Event<UICalendarSettingForm> event) throws Exception {
      UICalendarSettingForm uiForm = event.getSource() ;      
      CalendarSetting calendarSetting = new CalendarSetting() ;
      calendarSetting.setViewType(uiForm.getUIFormSelectBox(uiForm.VIEW_TYPE).getValue()) ;
      calendarSetting.setTimeInterval(Long.parseLong(uiForm.getUIFormSelectBox(uiForm.TIME_INTERVAL).getValue())) ;
      calendarSetting.setWeekStartOn(uiForm.getUIFormSelectBox(uiForm.WEEK_START_ON).getValue()) ;
      calendarSetting.setDateFormat(uiForm.getUIFormSelectBox(uiForm.DATE_FORMAT).getValue()) ;
      calendarSetting.setTimeFormat(uiForm.getUIFormSelectBox(uiForm.TIME_FORMAT).getValue()) ;
      calendarSetting.setLocation(uiForm.getUIStringInput(uiForm.LOCATION).getValue()) ;
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
      cservice.saveCalendarSetting(Util.getPortalRequestContext().getRemoteUser(), calendarSetting) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
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
