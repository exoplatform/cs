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

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
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
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIImportForm.SaveActionListener.class),      
      @EventConfig(listeners = UIImportForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIImportForm extends UIForm implements UIPopupComponent{
  final static public String FIELD_UPLOAD = "upload".intern() ;
  final static public String TYPE = "type".intern() ;
  final static private String NAME = "name".intern() ;
  public UIImportForm() throws Exception {
    this.setMultiPart(true) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    for(String type : calendarService.getExportImportType()) {
      options.add(new SelectItemOption<String>(type, type)) ;
    }
    addUIFormInput(new UIFormStringInput(NAME, NAME, null)) ;
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options)) ;
    addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD)) ;
  }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  static  public class SaveActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      UIFormUploadInput input = uiForm.getUIInput(FIELD_UPLOAD) ;
      String importFormat = uiForm.getUIFormSelectBox(UIImportForm.TYPE).getValue() ;
      String calendarName = uiForm.getUIStringInput(UIImportForm.NAME).getValue() ;
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UploadResource resource = uploadService.getUploadResource(input.getUploadId()) ;
      if(resource == null) {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.file-name-error", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(CalendarUtils.isEmpty(calendarName)) {
        calendarName = resource.getFileName() ;
      } else {
        if(!CalendarUtils.isNameValid(calendarName, CalendarUtils.SPECIALCHARACTER)) {
          uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.file-name-invalid", null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      try {
        String username = CalendarUtils.getCurrentUser() ;
        CalendarService calendarService = CalendarUtils.getCalendarService() ;
        List<Calendar> pCals = calendarService.getUserCalendars(SessionProviderFactory.createSessionProvider(), username, true) ;
        for(Calendar cal : pCals) {
            if(cal.getName().trim().toLowerCase().equals(calendarName.toLowerCase())) {
              uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{calendarName}, ApplicationMessage.WARNING)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
        }
        calendarService.getCalendarImportExports(importFormat).importCalendar(SessionProviderFactory.createSessionProvider(), username, input.getUploadDataAsStream(), calendarName) ;
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
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.file-type-error", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        //e.printStackTrace() ;
        return ; 
      }
    }
  }

  static  public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }  
}
