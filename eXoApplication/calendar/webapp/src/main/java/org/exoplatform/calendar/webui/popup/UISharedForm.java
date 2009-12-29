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
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.NameValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfigs({
	@ComponentConfig(
	    lifecycle = UIFormLifecycle.class,
	    template = "system:/groovy/webui/form/UIForm.gtmpl",
	    events = {
	      @EventConfig(listeners = UISharedForm.SaveActionListener.class),    
	      @EventConfig(listeners = UISharedForm.SelectPermissionActionListener.class, phase = Phase.DECODE),  
	      @EventConfig(listeners = UISharedForm.CancelActionListener.class)
	    }
	),
	@ComponentConfig(
		id = "UIPopupWindowUserSelect",
        type = UIPopupWindow.class,
        template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
        events = {
          @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
          @EventConfig(listeners = UISharedForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
          @EventConfig(listeners = UISharedForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
        }
	)
})
public class UISharedForm extends UIForm implements UIPopupComponent, UISelector{
  final public static String SPECIALCHARACTER[] = {CalendarUtils.SEMICOLON,CalendarUtils.SLASH,CalendarUtils.BACKSLASH,"'","|",">","<","\"", "?", "!", "@", "#", "$", "%","^","&","*"} ;
  final public static String SHARED_TAB = "UIInputUserSelect".intern() ;
  private Map<String, String> permission_ = new HashMap<String, String>() ;
  private String calendarId_ ;
  protected boolean isAddNew_ = true ;
  public UISharedForm() throws Exception{
    UISharedTab inputset = new UISharedTab(SHARED_TAB) ;
    inputset.addChild(new UIFormInputInfo(UISharedTab.FIELD_NAME, UISharedTab.FIELD_NAME, null)) ;
    inputset.addUIFormInput(new UIFormStringInput(UISharedTab.FIELD_USER, UISharedTab.FIELD_USER, null).addValidator(NameValidator.class)) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData selectUserAction = new ActionData() ;
    selectUserAction.setActionListener("SelectPermission") ;
    selectUserAction.setActionName("SelectUser") ;
    selectUserAction.setCssIconClass("SelectUserIcon") ;
    selectUserAction.setActionType(ActionData.TYPE_ICON) ;
    selectUserAction.setActionParameter(UISelectComponent.TYPE_USER) ;
    actions.add(selectUserAction) ;
    inputset.setActionField(UISharedTab.FIELD_USER, actions) ;
    inputset.addChild(new UIFormCheckBoxInput<Boolean>(UISharedTab.FIELD_EDIT, UISharedTab.FIELD_EDIT, null)) ;
    addChild(inputset) ;
  }

  public void init(String username, Calendar cal, boolean isAddNew) {
    isAddNew_ = isAddNew ;
    calendarId_ = cal.getId() ;
    setCalendarName(cal.getName()) ;
    boolean canEdit = false ;
    if(cal.getEditPermission() != null) {
      canEdit = Arrays.asList(cal.getEditPermission()).contains(username) ;
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
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    inputset.calendarName_ = value ;
    if(!CalendarUtils.isEmpty(value) && value.trim().length() > 30) value = value.substring(0, 30)+"..." ; 
    inputset.getUIFormInputInfo(UISharedTab.FIELD_NAME).setValue(value) ;
  }
  protected void setCanEdit(boolean canEdit) {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    inputset.getUIFormCheckBoxInput(UISharedTab.FIELD_EDIT).setChecked(canEdit) ;
  }
  protected boolean canEdit() {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    return inputset.getUIFormCheckBoxInput(UISharedTab.FIELD_EDIT).isChecked() ;
  }
  protected void setSharedUser(String value) {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    inputset.getUIStringInput(UISharedTab.FIELD_USER).setValue(value) ;
  }
  protected String getSharedUser() {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    return inputset.getUIStringInput(UISharedTab.FIELD_USER).getValue() ;
  }
  public String[] getActions() {
    return new String[] {"Save","Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

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

public void updateSelect(String selectField, String value) throws Exception {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    UIFormStringInput fieldInput = inputset.getUIStringInput(selectField) ;
    permission_.put(value.substring(value.lastIndexOf(":/") + 2), value.substring(value.lastIndexOf(":/") + 2)) ;
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
      String names = uiForm.getUIStringInput(UISharedTab.FIELD_USER).getValue() ;
      if(CalendarUtils.isEmpty(names)) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.invalid-username", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      /*if(!CalendarUtils.isNameValid(names, SPECIALCHARACTER)) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.invalid-username", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }*/
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      OrganizationService oService = CalendarUtils.getOrganizationService() ;
      String username = CalendarUtils.getCurrentUser() ;
      List<String> receiverUsers  = new ArrayList<String>() ;
      StringBuffer sb = new StringBuffer() ;
      for(String name : Arrays.asList(names.split(CalendarUtils.COMMA))) {
        name = name.trim();
        if( oService.getUserHandler().findUserByName(name) != null) { 
          receiverUsers.add(name) ;
        }
        else{
          if(sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
          sb.append(name) ;
        }
      }
      if(sb.length() > 0) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.not-found-user", new Object[]{sb.toString()}, 1)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(receiverUsers.contains(username)) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.found-user", new Object[]{username}, 1)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }

      Calendar cal = calendarService.getUserCalendar(username, uiForm.calendarId_) ;
      Map<String, String> perms = new HashMap<String, String>() ;
      if(cal.getViewPermission() != null) {
        for(String v : cal.getViewPermission()) {
          perms.put(v,String.valueOf(cal.getEditPermission()!= null && Arrays.asList(cal.getEditPermission()).contains(v))) ;
        }
      }
      List<String> newUsers = new ArrayList<String>() ;
      for(String u : receiverUsers) {
        if(perms.get(u) == null) newUsers.add(u) ; 
        perms.put(u, String.valueOf(uiForm.canEdit())) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
      cal.setViewPermission(perms.keySet().toArray(new String[perms.keySet().size()])) ;
      List<String> tempList = new ArrayList<String>() ;
      for(String v : perms.keySet()) {
        if(Boolean.parseBoolean(perms.get(v))) tempList.add(v) ;
      }
      cal.setEditPermission(tempList.toArray(new String[tempList.size()])) ;
      calendarService.saveUserCalendar(username, cal, false) ;
      calendarService.shareCalendar(username, uiForm.calendarId_, newUsers) ;
      UIAddEditPermission uiAddEdit = uiForm.getParent() ;
      uiAddEdit.updateGrid(cal, uiAddEdit.getCurrentPage());
      uiForm.setCanEdit(false) ;
      uiForm.setSharedUser(null) ;
      uiForm.permission_.clear() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UICalendars.class)) ;
    }
  }
  static  public class SelectPermissionActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      String currentValue = uiForm.getSharedUser() ;
      uiForm.permission_.clear() ;
      if(!CalendarUtils.isEmpty(currentValue)) {
        OrganizationService orgService = CalendarUtils.getOrganizationService() ;
        for(String s :currentValue.split(CalendarUtils.COMMA)) {
          s = s.trim() ;
          if(orgService.getUserHandler().findUserByName(s) != null) {
            uiForm.permission_.put(s, s) ;
          }
        }
      }
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupWindow uiPopupWindow = uiContainer.getChild(UIPopupWindow.class) ;
      if(uiPopupWindow == null) uiPopupWindow = uiContainer.addChild(UIPopupWindow.class, "UIPopupWindowUserSelect", "UIPopupWindowUserSelect") ;
      UIUserSelector uiUserSelector = uiContainer.createUIComponent(UIUserSelector.class, null, null) ;
      uiUserSelector.setShowSearch(true);
      uiUserSelector.setShowSearchUser(true) ;
      uiUserSelector.setShowSearchGroup(true);
      uiPopupWindow.setUIComponent(uiUserSelector);
      uiPopupWindow.setShow(true);
      uiPopupWindow.setWindowSize(740, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;	
//      String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
//      UIPopupAction childPopup = uiForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
//      UIGroupSelector uiGroupSelector = childPopup.activate(UIGroupSelector.class, 500) ;
//      uiGroupSelector.setType(permType) ;
//      uiGroupSelector.setSelectedGroups(null) ;
//      uiGroupSelector.setComponent(uiForm, new String[]{UISharedTab.FIELD_USER}) ;
//      event.getRequestContext().addUIComponentToUpdateByAjax(childPopup) ;
    }
  }
  static  public class AddActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UISharedForm uiShareForm = uiContainer.findFirstComponentOfType(UISharedForm.class);
      UISharedTab uiSharedTab = uiShareForm.getChild(UISharedTab.class);
      UIFormStringInput uiInput = uiSharedTab.getUIStringInput(UISharedTab.FIELD_USER);
      String currentValues = uiInput.getValue();
      String values = uiUserSelector.getSelectedUsers();
      System.out.println(values + "-" + currentValues);
      if(!CalendarUtils.isEmpty(currentValues) && currentValues != "null") values += ","+ currentValues; 
      values = uiShareForm.cleanValue(values);
      uiInput.setValue(values);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
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
  
  static  public class CancelActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }

}
