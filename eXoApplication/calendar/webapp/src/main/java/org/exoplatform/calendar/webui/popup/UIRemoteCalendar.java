/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
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
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.ext.UIFormColorPicker;
import org.exoplatform.webui.form.validator.MandatoryValidator;



/**
 * Created by The eXo Platform SAS
 * Author : khiem.dohoang
 *          khiem.dohoang@exoplatform.com
 * Jan 5, 2011  
 */

@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "app:/templates/calendar/webui/UIPopup/UIRemoteCalendar.gtmpl",
                 events = {
                   @EventConfig(listeners = UIRemoteCalendar.BackActionListener.class),
                   @EventConfig(listeners = UIRemoteCalendar.FinishActionListener.class),
                   @EventConfig(listeners = UIRemoteCalendar.CancelActionListener.class, phase = Phase.DECODE)
                 }
             )
public class UIRemoteCalendar extends UIForm implements UIPopupComponent {

  private static final String URL = "url".intern();
  private static final String NAME = "name".intern();
  private static final String DESCRIPTION = "description".intern();
  private static final String USE_AUTHENTICATION = "useAuthentication";
  private static final String USERNAME = "username".intern();
  private static final String PASSWORD = "password".intern();
  private static final String COLOR = "color".intern();
  private static final String AUTO_REFRESH = "autoRefresh".intern();
  private static final String LAST_UPDATED = "lastUpdated".intern();
  
  private String remoteType;
  private boolean isAddNew_ = true; 
  private String calendarId_ = null;
  
  public UIRemoteCalendar() throws Exception {
    UIFormStringInput remoteUrl = new UIFormStringInput(URL, URL, null);
    remoteUrl.addValidator(MandatoryValidator.class);
    //remoteUrl.setEditable(false);
    addUIFormInput(remoteUrl);
    addUIFormInput(new UIFormStringInput(NAME, NAME, null).addValidator(MandatoryValidator.class));
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(USE_AUTHENTICATION, USE_AUTHENTICATION, null));
    addUIFormInput(new UIFormStringInput(USERNAME, USERNAME, null));
    UIFormStringInput password = new UIFormStringInput(PASSWORD, PASSWORD, null);
    password.setType(UIFormStringInput.PASSWORD_TYPE);
    addUIFormInput(password);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    for (String s : Utils.SYNC_PERIOD) {
      options.add(new SelectItemOption<String>(s, s));
    }
    addUIFormInput(new UIFormSelectBox(AUTO_REFRESH, AUTO_REFRESH, options));  
    addUIFormInput(new UIFormColorPicker(COLOR, COLOR)); 
  }
  
  public void init(String url, String remoteType) {
    isAddNew_ = true;
    this.remoteType = remoteType;
    setUrl(url);
    this.getUIStringInput(URL).setEditable(false);
    setSyncPeriod(Utils.SYNC_AUTO);
    setSelectColor(Calendar.COLORS[new  Random().nextInt(Calendar.COLORS.length)]);
    setUseAuthentication(true);
  }
  
  public void init(Calendar calendar) throws Exception {
    if (calendar != null) {
      isAddNew_ = false;
      calendarId_ = calendar.getId();
    } else return;
    CalendarService calService = CalendarUtils.getCalendarService();
    String username = CalendarUtils.getCurrentUser();
    
    if (!calService.isRemoteCalendar(username, calendarId_)) {
      return;
    }
    remoteType = calService.getRemoteCalendarType(username, calendarId_);
    setUrl(calService.getRemoteCalendarUrl(username, calendarId_));
    this.getUIStringInput(URL).setEditable(true);
    setCalendarName(calService.getUserCalendar(username, calendarId_).getName());
    setDescription(calendar.getDescription());
    setSelectColor(calendar.getCalendarColor());
    setUseAuthentication(calService.getRemoteCalendarUsername(username, calendarId_) != null);
    setRemoteUser(calService.getRemoteCalendarUsername(username, calendarId_));
    setRemotePassword(calService.getRemoteCalendarPassword(username, calendarId_));
  }
  
  @Override
  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }
  
  protected String getUrl() {
    return this.getUIStringInput(URL).getValue();
  }
  
  protected void setUrl(String url) {
    this.getUIStringInput(URL).setValue(url);
  }
  
  protected void setCalendarName(String name) {
    this.getUIStringInput(NAME).setValue(name);
  }
  
  protected String getCalendarName() {
    return this.getUIStringInput(NAME).getValue();
  }
  
  protected void setDescription(String description) {
    this.getUIFormTextAreaInput(DESCRIPTION).setValue(description);
  }
  
  protected String getDescription() {
    return this.getUIFormTextAreaInput(DESCRIPTION).getValue();
  }
  
  protected String getSyncPeriod() {
    return this.getUIFormSelectBox(AUTO_REFRESH).getValue();
  }
  
  protected void setSyncPeriod(String value) {
    this.getUIFormSelectBox(AUTO_REFRESH).setValue(value);
  }
  
  protected String getSelectColor() {
    return this.getChild(UIFormColorPicker.class).getValue();
  }
  
  protected void setSelectColor(String value) {
    this.getChild(UIFormColorPicker.class).setValue(value);
  }
  
  protected void setRemoteUser(String remoteUser) {
    this.getUIStringInput(USERNAME).setValue(remoteUser);
  }
  
  protected String getRemoteUser() {
    return this.getUIStringInput(USERNAME).getValue();
  }
  
  protected void setRemotePassword(String password) {
    this.getUIStringInput(PASSWORD).setValue(password);
  }
  
  protected String getRemotePassword() {
    return this.getUIStringInput(PASSWORD).getValue();
  }
  
  protected void setUseAuthentication(Boolean checked) {
    this.getUIFormCheckBoxInput(USE_AUTHENTICATION).setChecked(checked);
  }
  
  protected Boolean getUseAuthentication() {
    return this.getUIFormCheckBoxInput(USE_AUTHENTICATION).isChecked();
  }
  
  public static class FinishActionListener extends EventListener<UIRemoteCalendar> {

    @Override
    public void execute(Event<UIRemoteCalendar> event) throws Exception {
      // TODO Auto-generated method stub
      UIRemoteCalendar uiform = event.getSource();
      UICalendarPortlet calendarPortlet = uiform.getAncestorOfType(UICalendarPortlet.class);
      UIApplication uiApp = uiform.getAncestorOfType(UIApplication.class);
      CalendarService calService = CalendarUtils.getCalendarService();
      String remoteType = uiform.remoteType;
      String username = CalendarUtils.getCurrentUser();
      String url = uiform.getUrl();
      String calendarName = uiform.getCalendarName();
      String description = uiform.getDescription();
      String syncPeriod = uiform.getSyncPeriod();
      String remoteUser = "";
      String remotePassword = "";
      Calendar eXoCalendar = null;
      Credentials credentials = null;
      try {       
        if (calService.isPublicAccessRemoteUrl(url)) {
          // check valid url
          if(!calService.isValidRemoteUrl(url, remoteType)) {
            // pop-up error message: invalid ics url
            uiApp.addMessage(new ApplicationMessage("UIRemoteCalendar.msg.url-is-invalid", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
          credentials = null;          
        } else {
          // check useAuthentication
          if(!uiform.getUseAuthentication()) {
            // pop-up error message: need authentication
            uiApp.addMessage(new ApplicationMessage("UIRemoteCalendar.msg.authentication-required", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }          
          remoteUser = uiform.getRemoteUser();
          remotePassword = uiform.getRemotePassword();          
          if(CalendarUtils.isEmpty(remoteUser)) {
            // pop-up error message: require remote username
            uiApp.addMessage(new ApplicationMessage("UIRemoteCalendar.msg.remote-user-name-required", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }          
          //check valid url
          if(!calService.isValidRemoteUrl(url, remoteType, remoteUser, remotePassword)) {
            // pop-up error message: invalid caldav url
            uiApp.addMessage(new ApplicationMessage("UIRemoteCalendar.msg.url-is-invalid-or-wrong-authentication", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }          
          credentials = new UsernamePasswordCredentials(remoteUser, remotePassword);    
        }

        if (uiform.isAddNew_) {
          // access to remote calendar
          if(remoteType.equals(CalendarService.ICALENDAR)) {
            eXoCalendar = calService.importRemoteIcs(username, url, calendarName, uiform.getSyncPeriod(), credentials);
          } else {
            if(remoteType.equals(CalendarService.CALDAV)) {
              eXoCalendar = calService.importCalDavCalendar(username, url, calendarName, uiform.getSyncPeriod(), credentials);
            }
          }
        } else {
          // update remote calendar info
          eXoCalendar = calService.updateRemoteCalendarInfo(username, uiform.calendarId_, url, calendarName, description, syncPeriod, remoteUser, remotePassword);
          // refresh calendar
          eXoCalendar = calService.refreshRemoteCalendar(username, uiform.calendarId_);
        }
        
        eXoCalendar.setCalendarColor(uiform.getSelectColor());
        eXoCalendar.setDescription(uiform.getDescription());
        calService.saveUserCalendar(username, eXoCalendar, false) ;
      } catch (Exception e) {
        e.printStackTrace();
        uiApp.addMessage(new ApplicationMessage("UIRemoteCalendar.msg.cant-import-remote-calendar", null, ApplicationMessage.ERROR));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      calendarPortlet.cancelAction() ;
      UICalendarWorkingContainer uiWorkingContainer = calendarPortlet.getChild(UICalendarWorkingContainer.class) ;
      uiApp.addMessage(new ApplicationMessage("UIRemoteCalendar.msg-import-succesfully", null, ApplicationMessage.INFO));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
  }
  
  public static class BackActionListener extends EventListener<UIRemoteCalendar> {

    @Override
    public void execute(Event<UIRemoteCalendar> event) throws Exception {
      // TODO Auto-generated method stub
      // back to UISubscribeForm
      UIRemoteCalendar uiform = event.getSource();
      UICalendarPortlet uiCalendarPortlet = uiform.getAncestorOfType(UICalendarPortlet.class);
      UIPopupAction uiPopupAction = uiCalendarPortlet.getChild(UIPopupAction.class);
      uiPopupAction.deActivate();
      UISubscribeForm uiSubscribe = uiPopupAction.activate(UISubscribeForm.class, 600);
      uiSubscribe.init(uiform.remoteType, uiform.getUrl());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }
  
  public static class CancelActionListener extends EventListener<UIRemoteCalendar> {

    @Override
    public void execute(Event<UIRemoteCalendar> event) throws Exception {
      // TODO Auto-generated method stub
      UIRemoteCalendar uiform = event.getSource();
      UICalendarPortlet calendarPortlet = uiform.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction();
    }
  }

}