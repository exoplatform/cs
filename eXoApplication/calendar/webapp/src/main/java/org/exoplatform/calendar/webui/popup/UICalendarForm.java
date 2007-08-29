/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarContainer;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
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
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    //template = "app:/templates/calendar/webui/UICalendarForm.gtmpl",
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
    events = {
      @EventConfig(listeners = UICalendarForm.SelectPublicActionListener.class,  phase=Phase.DECODE),
      @EventConfig(listeners = UICalendarForm.AddCategoryActionListener.class,  phase=Phase.DECODE),
      @EventConfig(listeners = UICalendarForm.SelectPermissionActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UICalendarForm.SaveActionListener.class),
      @EventConfig(listeners = UICalendarForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UICalendarForm extends UIFormTabPane implements UIPopupComponent, UISelector{
  final public static String DISPLAY_NAME = "displayName" ;
  final public static String DESCRIPTION = "description" ;
  final public static String CATEGORY = "category" ;
  final public static String ISPUBLIC = "isPublic" ;
  final public static String SHARED_GROUPS = "sharedGroups" ;
  final public static String EDIT_PERMISSION = "editPermission" ;
  final public static String SELECT_GROUPS = "selectGroups" ;
  final public static String INPUT_CALENDAR = "calendarDetail".intern() ;
  final public static String INPUT_SHARE = "public".intern() ;

  private Map<String, String> permission_ = new HashMap<String, String>() ;
  
  public UICalendarForm() throws Exception{
    super("UICalendarForm", false);

    UIFormInputWithActions calendarDetail = new UIFormInputWithActions(INPUT_CALENDAR) ;
    calendarDetail.addUIFormInput(new UIFormStringInput(DISPLAY_NAME, DISPLAY_NAME, null).addValidator(EmptyFieldValidator.class)) ;
    calendarDetail.addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
    calendarDetail.addUIFormInput(new UIFormSelectBox(CATEGORY, CATEGORY, getCategory())) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData addCategory = new ActionData() ;
    addCategory.setActionListener("AddCategory") ;
    addCategory.setActionType(ActionData.TYPE_ICON) ;
    addCategory.setActionName("AddCategory") ;
    actions.add(addCategory) ;
    calendarDetail.setActionField(CATEGORY, actions) ;
    addChild(calendarDetail) ;

    UIFormInputWithActions sharing = new UIFormInputWithActions(INPUT_SHARE) ;
    sharing.addUIFormInput(new UIFormCheckBoxInput<Boolean>(ISPUBLIC, ISPUBLIC, null)) ;
    UIFormCheckBoxInput uiCheckbox = sharing.getUIFormCheckBoxInput(ISPUBLIC) ;
    uiCheckbox.setOnChange("SelectPublic") ;
    sharing.addUIFormInput(new UIFormInputInfo(SELECT_GROUPS, SELECT_GROUPS, null)) ;
    String[] groups = CalendarUtils.getAllGroups() ;
    for(String group : groups) {
      if(sharing.getUIFormCheckBoxInput(group) != null)sharing.getUIFormCheckBoxInput(group).setChecked(false) ;
      else sharing.addUIFormInput(new UIFormCheckBoxInput<Boolean>(group, group, false)) ;
    }
    /*sharing.addUIFormInput(new UIFormTextAreaInput(SHARED_GROUPS, SHARED_GROUPS, null)) ;

    ActionData sharedGroups = new ActionData() ;
    sharedGroups.setActionListener("SelectGroup") ;
    sharedGroups.setActionName("SharedGroups") ;
    sharedGroups.setActionType(ActionData.TYPE_ICON) ;
    sharedGroups.setCssIconClass("AddIcon16x16 SelectMemberIcon") ;    
    actions.add(sharedGroups) ;
    sharing.setActionField(SHARED_GROUPS, actions) ;
     */
    actions = new ArrayList<ActionData> () ;
    sharing.addUIFormInput(new UIFormStringInput(EDIT_PERMISSION, null, null)) ;
    ActionData editPermissions = new ActionData() ;
    editPermissions.setActionListener("SelectPermission") ;
    editPermissions.setActionName("EditPermission") ;
    editPermissions.setActionType(ActionData.TYPE_ICON) ;
    editPermissions.setCssIconClass("AddIcon16x16 SelectUserIcon") ;    
    actions.add(editPermissions) ;
    sharing.setActionField(EDIT_PERMISSION, actions) ;
    sharing.setRendered(false) ;
    addChild(sharing) ;
    
    lockCheckBoxFields(!uiCheckbox.isChecked());
  }

  public String[] getActions(){
    return new String[]{"Save", "Cancel"} ;
  }
  private  List<SelectItemOption<String>> getCategory() throws Exception {
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
    List<CalendarCategory> categories = calendarService.getCategories(username) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(CalendarCategory category : categories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getId())) ;
    }
    return options ;
  }
  public void reloadCategory() throws Exception {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIFormSelectBox(CATEGORY).setOptions(getCategory()) ;
  }
  private void lockCheckBoxFields(boolean isLock) throws Exception {
    UIFormInputWithActions shareTab = getChildById(INPUT_SHARE) ;
    for(String group : CalendarUtils.getAllGroups()) {
      UIFormCheckBoxInput uiInput = shareTab.getUIFormCheckBoxInput(group) ;
      if(uiInput != null) uiInput.setEnable(!isLock) ;
    }
    shareTab.getUIStringInput(EDIT_PERMISSION).setEditable(!isLock) ;
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }
  public void updateSelect(String selectField, String value) throws Exception {
    UIFormInputWithActions shareTab = getChildById(INPUT_SHARE) ;
    UIFormStringInput fieldInput = shareTab.getUIStringInput(selectField) ;
    permission_.put(value, value) ;
    StringBuilder sb = new StringBuilder() ;
    for(String s : permission_.values()) {
      sb.append(s).append(CalendarUtils.COLON) ;
    }
    fieldInput.setValue(sb.toString()) ;
  }
  static  public class SelectPublicActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      System.out.println(" ============= > SelectPublicActionListener");
      UIFormInputWithActions shareTab = uiForm.getChildById(INPUT_SHARE) ;
      uiForm.setRenderedChild(INPUT_SHARE) ;
      if(shareTab.getUIFormCheckBoxInput(ISPUBLIC).isChecked()) {
        uiForm.lockCheckBoxFields(false) ;
      } else { 
        uiForm.lockCheckBoxFields(true) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  static  public class AddCategoryActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      System.out.println(" ============= > AddCategoryActionListener");
      uiForm.setRenderedChild(INPUT_CALENDAR) ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class);
      uiChildPopup.activate(UICalendarCategoryForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }

  static  public class SelectPermissionActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      System.out.println(" ============= > SelectPermissionActionListener");
      UICalendarForm uiForm = event.getSource() ;
      uiForm.setRenderedChild(INPUT_SHARE) ;
      if(!uiForm.getUIFormCheckBoxInput(ISPUBLIC).isChecked()) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UICalendarForm.msg.checkbox-public-notchecked", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null);
      uiGroupSelector.setSelectUser(true);
      uiGroupSelector.setComponent(uiForm, new String[] {EDIT_PERMISSION});
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(uiGroupSelector, 500, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }

  static  public class SaveActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
      Calendar calendar = new Calendar() ;
      calendar.setName(uiForm.getUIStringInput(DISPLAY_NAME).getValue()) ;
      calendar.setDescription(uiForm.getUIFormTextAreaInput(DESCRIPTION).getValue()) ;
      calendar.setCategoryId(uiForm.getUIFormSelectBox(CATEGORY).getValue()) ;
      boolean isPublic = uiForm.getUIFormCheckBoxInput(ISPUBLIC).isChecked() ;
      calendar.setPublic(isPublic) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(isPublic) {
        String[] groupList = CalendarUtils.getAllGroups() ;
        List<String> selected = new ArrayList<String>() ;
        for(String groupId : groupList) {
          if(uiForm.getUIFormCheckBoxInput(groupId).isChecked()) selected.add(groupId) ;
        }
        if(selected.size() < 1){
          uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.group-empty", null, ApplicationMessage.WARNING) ) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        calendar.setGroups(selected.toArray(new String[]{})) ;
        String editPermission = uiForm.getUIStringInput(EDIT_PERMISSION).getValue() ;
        if(!CalendarUtils.isEmpty(editPermission)) {
          calendar.setEditPermission(editPermission.split(CalendarUtils.COLON)) ;
        }
        calendarService.saveGroupCalendar(calendar, true) ;
      }else {
        if(CalendarUtils.isEmpty(uiForm.getUIFormSelectBox(CATEGORY).getValue())) {
          uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.category-empty", null, ApplicationMessage.WARNING) ) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } else {
          calendarService.saveUserCalendar(username, calendar, true) ;
        }
      }
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
      UICalendars uiCalendars = calendarPortlet.getChild(UICalendarWorkingContainer.class).getChild(UICalendarContainer.class).getChild(UICalendars.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ; ;
    }
  }
  static  public class CancelActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }

}
