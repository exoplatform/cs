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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

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
      @EventConfig(listeners = UISharedForm.SaveActionListener.class),    
      @EventConfig(listeners = UISharedForm.SelectPermissionActionListener.class, phase = Phase.DECODE),  
      @EventConfig(listeners = UISharedForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UISharedForm extends UIForm implements UIPopupComponent, UISelector{
  final static public String FIELD_NAME = "calendarName".intern() ;
  final static public String FIELD_USER = "username".intern() ;
  final static public String FIELD_EDIT = "canEdit".intern() ;
  //final static public String USER_NAME = "username".intern() ;
  private Map<String, String> permission_ = new HashMap<String, String>() ;
  private String calendarId_ ;
  private boolean isAddNew_ = true ;
  public UISharedForm() throws Exception{
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    inputset.addChild(new UIFormInputInfo(FIELD_NAME, FIELD_NAME, null)) ;
    inputset.addUIFormInput(new UIFormStringInput(FIELD_USER, FIELD_USER, null)) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    /* ActionData selectGroupAction = new ActionData() ;
    selectGroupAction.setActionListener("SelectPermission") ;
    selectGroupAction.setActionName("SelectGroup") ;
    selectGroupAction.setActionType(ActionData.TYPE_ICON) ;
    selectGroupAction.setActionParameter(UISelectComponent.TYPE_GROUP) ;
    actions.add(selectGroupAction) ;*/
    ActionData selectUserAction = new ActionData() ;
    selectUserAction.setActionListener("SelectPermission") ;
    selectUserAction.setActionName("SelectUser") ;
    selectUserAction.setCssIconClass("SelectUserIcon") ;
    selectUserAction.setActionType(ActionData.TYPE_ICON) ;
    selectUserAction.setActionParameter(UISelectComponent.TYPE_USER) ;
    actions.add(selectUserAction) ;

    /*ActionData selectMemberAction = new ActionData() ;
    selectMemberAction.setActionListener("SelectPermission") ;
    selectMemberAction.setActionName("SelectMemberShip") ;
    selectMemberAction.setActionType(ActionData.TYPE_ICON) ;
    selectMemberAction.setCssIconClass("SelectMemberIcon") ;
    selectMemberAction.setActionParameter(UISelectComponent.TYPE_MEMBERSHIP) ;
    actions.add(selectMemberAction) ;*/
    inputset.setActionField(FIELD_USER, actions) ;
    inputset.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_EDIT, FIELD_EDIT, null)) ;
    addChild(inputset) ;
  }

  public void init(String username, Calendar cal, boolean isAddNew) {
    isAddNew_ = isAddNew ;
    calendarId_ = cal.getId() ;
    setCalendarName(cal.getName()) ;
    boolean canEdit = false ;
    if(cal.getEditPermission() != null) {
      for(String editPerm : cal.getEditPermission() ) {
        if(editPerm !=null && editPerm.equals(username)) {
          canEdit = true ;
          break ;
        }
      }
    }
    setCanEdit(canEdit) ;
  }
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  public void setSelectedCalendarId(String id) { calendarId_ = id ;}
  public void setCalendarName(String value) {
    getUIFormInputInfo(FIELD_NAME).setValue(value) ;
  }
  protected void setCanEdit(boolean canEdit) {
    getUIFormCheckBoxInput(FIELD_EDIT).setChecked(canEdit) ;
  }
  protected boolean canEdit() {
    return getUIFormCheckBoxInput(FIELD_EDIT).isChecked() ;
  }
  protected void setSharedUser(String value) {
    getUIStringInput(FIELD_NAME).setValue(value) ;
  }
  public String[] getActions() {
    return new String[] {"Save","Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField) ;
    permission_.put(value, value) ;
    StringBuilder sb = new StringBuilder() ;
    for(String s : permission_.values()) {
      if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
      sb.append(s) ;
    }
    fieldInput.setValue(sb.toString()) ;
  }  
  static  public class SaveActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      String names = uiForm.getUIStringInput(FIELD_USER).getValue() ;
      if(names == null || names.length() < 1) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.invalid-username", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }      
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      SessionProvider sProvider = SessionsUtils.getSystemProvider() ;
      String username = CalendarUtils.getCurrentUser() ;
      List<String> receiverUsers  = Arrays.asList(names.split(CalendarUtils.COMMA)) ;  
      if(uiForm.canEdit()) {
        Calendar cal = calendarService.getUserCalendar(SessionsUtils.getSessionProvider(), CalendarUtils.getCurrentUser(), uiForm.calendarId_) ;
        cal.setEditPermission(receiverUsers.toArray(new String[receiverUsers.size()])) ;
        calendarService.saveUserCalendar(sProvider, username, cal, false) ;
      }
      calendarService.shareCalendar(sProvider, username, uiForm.calendarId_, receiverUsers) ;
      for(String receiver : receiverUsers) {
        CalendarSetting calSetting = calendarService.getCalendarSetting(sProvider, receiver) ;
        if(calSetting == null) calSetting = new CalendarSetting() ;
        Set<String> sharedCaeldnars = new HashSet<String>() ;
        if(calSetting.getDefaultSharedCalendars() != null) {
          for(String id : calSetting.getDefaultPrivateCalendars()) {
            sharedCaeldnars.add(id) ;
          }
        }
        if(!sharedCaeldnars.contains(uiForm.calendarId_)) sharedCaeldnars.add(uiForm.calendarId_) ;
        calSetting.setDefaultSharedCalendars(sharedCaeldnars.toArray(new String[sharedCaeldnars.size()])) ;
        calendarService.saveCalendarSetting(sProvider, receiver, calSetting) ;
      }
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ;
      calendarPortlet.cancelAction() ;       
    }
  }
  static  public class SelectPermissionActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupAction childPopup = uiForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
      UIGroupSelector uiGroupSelector = childPopup.activate(UIGroupSelector.class, 500) ;
      uiGroupSelector.setType(permType) ;
      uiGroupSelector.setSelectedGroups(null) ;
      uiGroupSelector.setComponent(uiForm, new String[]{UISharedForm.FIELD_USER}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(childPopup) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }

}
