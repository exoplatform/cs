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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.Colors;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.calendar.webui.UIFormColorPicker;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
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
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "app:/templates/calendar/webui/UIPopup/UIImportForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UIImportForm.SaveActionListener.class),  
                   @EventConfig(listeners = UIImportForm.ImportActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIImportForm.AddActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIImportForm.CancelActionListener.class, phase = Phase.DECODE)
                 }
)
public class UIImportForm extends UIForm implements UIPopupComponent{

  final public static String DISPLAY_NAME = "displayName" ;
  final public static String DESCRIPTION = "description" ;
  final public static String CATEGORY = "category" ;
  final public static String SELECT_COLOR = "selectColor" ;
  final public static String TIMEZONE = "timeZone" ;
  final public static String LOCALE = "locale" ;

  final static public String TYPE = "type".intern() ;
  final static public String FIELD_UPLOAD = "upload".intern() ;
  final static public String FIELD_TO_CALENDAR = "impotTo".intern() ;
  final static public String ONCHANGE = "OnChange".intern() ;
  final static public int UPDATE_EXIST = 1 ;
  final static public int ADD_NEW = 0 ;
  protected int flag_ = -1 ;

  public UIImportForm() throws Exception {
    this.setMultiPart(true) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    for(String type : calendarService.getExportImportType()) {
      options.add(new SelectItemOption<String>(type, type)) ;
    }
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options)) ;
    addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD));
    UIFormSelectBox privateCal = new UIFormSelectBox(FIELD_TO_CALENDAR, FIELD_TO_CALENDAR, getPrivateCalendars()) ; 
    addUIFormInput(privateCal);
    addUIFormInput(new UIFormStringInput(DISPLAY_NAME, DISPLAY_NAME, null).addValidator(MandatoryValidator.class));
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null));
    addUIFormInput(new UIFormSelectBox(CATEGORY, CATEGORY, getCategory()));
    //cs-2163
    CalendarSetting calendarSetting = CalendarUtils.getCalendarService()
      .getCalendarSetting(CalendarUtils.getCurrentUser()) ;
    UIFormSelectBox locale = new UIFormSelectBox(LOCALE, LOCALE, getLocales()) ;
    locale.setValue(calendarSetting.getLocation()) ;
    addUIFormInput(locale);    
    UIFormSelectBox timeZones = new UIFormSelectBox(TIMEZONE, TIMEZONE, getTimeZones()) ;
    timeZones.setValue(calendarSetting.getTimeZone()) ;
    addUIFormInput(timeZones);
    addUIFormInput(new UIFormColorPicker(SELECT_COLOR, SELECT_COLOR, Colors.COLORS));
  }

  public void init(String calId, String calType) {
    if(!CalendarUtils.isEmpty(calId) && String.valueOf(Calendar.TYPE_PRIVATE).equals(calType)) {
      UIFormSelectBox selectBox = getUIFormSelectBox(FIELD_TO_CALENDAR) ;
      if(selectBox.getOptions()!= null && !selectBox.getOptions().isEmpty()) {
        switchMode(UPDATE_EXIST);
        selectBox.setValue(calId) ;
      } else {
        switchMode(ADD_NEW);
      }
    } else {
      switchMode(ADD_NEW);
    } 
  }
  private  List<SelectItemOption<String>> getCategory() throws Exception {
    String username = CalendarUtils.getCurrentUser() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<CalendarCategory> categories = calendarService.getCategories(username) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(CalendarCategory category : categories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getId())) ;
    }
    return options ;
  }
  private SessionProvider getSession()  {
    return SessionProviderFactory.createSessionProvider() ;
  }

  private SessionProvider getSystemSession()  {
    return SessionProviderFactory.createSystemProvider() ;
  }
  private List<SelectItemOption<String>> getTimeZones() {
    return CalendarUtils.getTimeZoneSelectBoxOptions(TimeZone.getAvailableIDs()) ;
  } 
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  private List<SelectItemOption<String>> getLocales() {
    return CalendarUtils.getLocaleSelectBoxOptions(java.util.Calendar.getAvailableLocales()) ;
  }
  public String[] getActions(){
    return new String[]{"Save", "Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  public List<SelectItemOption<String>> getPrivateCalendars() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    try {
      for(Calendar c : CalendarUtils.getCalendarService().getUserCalendars(CalendarUtils.getCurrentUser(), true)){
        options.add(new SelectItemOption<String>(c.getName(), c.getId())) ;
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    return options ;
  }
  public boolean isNew() {
    return flag_ == ADD_NEW ;
  }
  protected String getSelectedGroup() {
    return getUIFormSelectBox(CATEGORY).getValue() ;
  }
  protected String getDescription() {
    return getUIFormTextAreaInput(DESCRIPTION).getValue() ;
  }

  protected String getSelectedColor() {
    return getChild(UIFormColorPicker.class).getValue() ;
  }

  protected String getTimeZone() {
    return getUIFormSelectBox(TIMEZONE).getValue() ;
  }

  protected String getLocale() {
    return getUIFormSelectBox(LOCALE).getValue() ;
  }
  public void switchMode(int flag) {
    flag_ = flag ;
    if(flag == UPDATE_EXIST) {
      getUIFormSelectBox(FIELD_TO_CALENDAR).setRendered(true);
      getUIStringInput(DISPLAY_NAME).setRendered(false);
      getUIFormTextAreaInput(DESCRIPTION).setRendered(false);
      getUIFormSelectBox(CATEGORY).setRendered(false);
      getUIFormSelectBox(TIMEZONE).setRendered(false);
      getUIFormSelectBox(LOCALE).setRendered(false);
      getChild(UIFormColorPicker.class).setRendered(false);
    } else if(flag == ADD_NEW) {
      getUIFormSelectBox(FIELD_TO_CALENDAR).setRendered(false);
      getUIStringInput(DISPLAY_NAME).setRendered(true);
      getUIFormTextAreaInput(DESCRIPTION).setRendered(true);
      getUIFormSelectBox(CATEGORY).setRendered(true);
      getUIFormSelectBox(TIMEZONE).setRendered(true);
      getUIFormSelectBox(LOCALE).setRendered(true);
      getChild(UIFormColorPicker.class).setRendered(true);
    } else {
      System.out.println("Wrong flag(" +flag+ ") only UPDATE_EXIST(1) or ADD_NEW(0) accept ");
    }
  }

  static  public class SaveActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      String username = CalendarUtils.getCurrentUser() ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      UIImportForm uiForm = event.getSource() ;
      UIFormUploadInput input = uiForm.getUIInput(FIELD_UPLOAD) ;
      String importFormat = uiForm.getUIFormSelectBox(UIImportForm.TYPE).getValue() ;
      String calendarName = uiForm.getUIStringInput(UIImportForm.DISPLAY_NAME).getValue() ;
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UploadResource resource = uploadService.getUploadResource(input.getUploadId()) ;
      if(resource == null) {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.file-name-error", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      SessionProvider userSession = SessionProviderFactory.createSessionProvider() ;
      try {
        if(calendarService.getCalendarImportExports(importFormat).isValidate(input.getUploadDataAsStream())) {
          if(uiForm.isNew()) {
            if(CalendarUtils.isEmpty(calendarName)) {
              calendarName = resource.getFileName() ;
            } 
            if(!CalendarUtils.isNameValid(calendarName, CalendarUtils.SPECIALCHARACTER)) {
              uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.file-name-invalid", null));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
            List<Calendar> pCals = calendarService.getUserCalendars(username, true) ;
            for(Calendar cal : pCals) {
              if(cal.getName().trim().equalsIgnoreCase(calendarName)) {
                uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{calendarName}, ApplicationMessage.WARNING)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                return ;
              }
            }
            Calendar calendar = new Calendar() ;
            calendar.setName(calendarName) ;
            calendar.setDescription(uiForm.getDescription()) ;
            calendar.setLocale(uiForm.getLocale()) ;
            calendar.setTimeZone(uiForm.getTimeZone()) ;
            calendar.setCalendarColor(uiForm.getSelectedColor()) ;
            calendar.setCalendarOwner(username) ;
            calendar.setPublic(false) ;
            calendar.setCategoryId(uiForm.getSelectedGroup()) ;
            calendarService.saveUserCalendar(username, calendar, true) ;
            calendarService.getCalendarImportExports(importFormat).importToCalendar(userSession, username, input.getUploadDataAsStream(), calendar.getId()) ;
          } else {
            String calendarId = uiForm.getUIFormSelectBox(FIELD_TO_CALENDAR).getValue() ;
            calendarService.getCalendarImportExports(importFormat).importToCalendar(userSession, username, input.getUploadDataAsStream(), calendarId) ;
          }
          UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
          UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
          UICalendarViewContainer uiCalendarViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
          uiCalendarViewContainer.refresh() ;
          calendarPortlet.setCalendarSetting(null) ;
          uploadService.removeUpload(input.getUploadId()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarViewContainer) ;
          calendarPortlet.cancelAction() ;
        } else {
          uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.file-type-error", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        } 
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.file-type-error", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
      }
    }
  }
  static  public class ImportActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      if(uiForm.getPrivateCalendars().isEmpty()) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar",null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;  
      } else {
        uiForm.switchMode(UPDATE_EXIST) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
      }
    }
  }  
  static  public class AddActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      uiForm.switchMode(ADD_NEW) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  } 
  static  public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      UIFormUploadInput input = uiForm.getUIInput(FIELD_UPLOAD) ;
      uploadService.removeUpload(input.getUploadId()) ;
      calendarPortlet.cancelAction() ;
    }
  }
}
