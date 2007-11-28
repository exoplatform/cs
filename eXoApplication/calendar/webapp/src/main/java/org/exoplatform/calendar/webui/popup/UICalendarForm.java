/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
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
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
    events = {
      @EventConfig(listeners = UICalendarForm.SaveActionListener.class),
      @EventConfig(listeners = UICalendarForm.AddCategoryActionListener.class,  phase=Phase.DECODE),
      @EventConfig(listeners = UICalendarForm.SelectPermissionActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UICalendarForm.ResetActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UICalendarForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UICalendarForm extends UIFormTabPane implements UIPopupComponent, UISelector{
  final public static String DISPLAY_NAME = "displayName" ;
  final public static String DESCRIPTION = "description" ;
  final public static String CATEGORY = "category" ;
  final public static String SHARED_GROUPS = "sharedGroups" ;
  final public static String EDIT_PERMISSION = "editPermission" ;
  final public static String SELECT_COLOR = "selectColor" ;
  final public static String SELECT_GROUPS = "selectGroups" ;
  final public static String INPUT_CALENDAR = "calendarDetail".intern() ;
  final public static String INPUT_SHARE = "public".intern() ;
  final public static String TIMEZONE = "timeZone" ;
  final public static String LOCALE = "locale" ;

  private Map<String, String> permission_ = new HashMap<String, String>() ;
  private boolean isAddNew_ = true ;
  public String calendarId_ = null ;
  public UICalendarForm() throws Exception{
    super("UICalendarForm", false);

    UIFormInputWithActions calendarDetail = new UIFormInputWithActions(INPUT_CALENDAR) ;
    calendarDetail.addUIFormInput(new UIFormStringInput(DISPLAY_NAME, DISPLAY_NAME, null).addValidator(EmptyFieldValidator.class)) ;
    calendarDetail.addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
    calendarDetail.addUIFormInput(new UIFormSelectBox(CATEGORY, CATEGORY, getCategory())) ;
    calendarDetail.addUIFormInput(new UIFormSelectBox(LOCALE, LOCALE, getLocales())) ;
    calendarDetail.addUIFormInput(new UIFormSelectBox(TIMEZONE, TIMEZONE, getTimeZones())) ;
    calendarDetail.addUIFormInput(new UIFormSelectBox(SELECT_COLOR, SELECT_COLOR, getColors())) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData addCategory = new ActionData() ;
    addCategory.setActionListener("AddCategory") ;
    addCategory.setActionType(ActionData.TYPE_ICON) ;
    addCategory.setActionName("AddCategory") ;
    actions.add(addCategory) ;
    calendarDetail.setActionField(CATEGORY, actions) ;
    addChild(calendarDetail) ;

    UIFormInputWithActions sharing = new UIFormInputWithActions(INPUT_SHARE) ;
    sharing.addUIFormInput(new UIFormInputInfo(SELECT_GROUPS, SELECT_GROUPS, null)) ;
    for(Object groupObj : getPublicGroups()) {
      String group = ((Group)groupObj).getId() ;
      if(sharing.getUIFormCheckBoxInput(group) != null)sharing.getUIFormCheckBoxInput(group).setChecked(false) ;
      else sharing.addUIFormInput(new UIFormCheckBoxInput<Boolean>(group, group, false)) ;
    }
    actions = new ArrayList<ActionData> () ;
    sharing.addUIFormInput(new UIFormStringInput(EDIT_PERMISSION, null, null)) ;
    ActionData editPermission = new ActionData() ;
    editPermission.setActionListener("SelectPermission") ;
    editPermission.setActionName("SelectUser") ;
    editPermission.setActionParameter(UISelectComponent.TYPE_USER) ;
    editPermission.setActionType(ActionData.TYPE_ICON) ;
    //editPermission.setCssIconClass("") ;
    actions.add(editPermission) ;
    ActionData membershipPerm = new ActionData() ;
    membershipPerm.setActionListener("SelectPermission") ;
    membershipPerm.setActionName("SelectMemberShip") ;
    membershipPerm.setActionParameter(UISelectComponent.TYPE_MEMBERSHIP) ;
    membershipPerm.setActionType(ActionData.TYPE_ICON) ;
    //editPermission.setCssIconClass("") ;
    actions.add(membershipPerm) ;

    sharing.setActionField(EDIT_PERMISSION, actions) ;
    sharing.setRendered(false) ;
    addChild(sharing) ;
  }

  private List<SelectItemOption<String>> getColors() {
    List<SelectItemOption<String>> colors = new ArrayList<SelectItemOption<String>>() ;
    for(String color : Calendar.COLORS) {
      colors.add(new SelectItemOption<String>(color, color)) ;
    }
    return colors;
  }

  public String[] getActions(){
    return new String[]{"Save", "Reset", "Cancel"} ;
  }
  private  List<SelectItemOption<String>> getCategory() throws Exception {
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
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
  protected void lockCheckBoxFields(boolean isLock) throws Exception {
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
  public void reset() {
    /*   if(isAddNew_) {

    }
    calendarId_ = null ;
    isAddNew_ = true ;*/
  }
  public void init(Calendar calendar) throws Exception {
    reset() ;
    isAddNew_ = false ;
    calendarId_ = calendar.getId() ;
    setDisplayName(calendar.getName()) ;
    setDescription(calendar.getDescription()) ;
    setSelectedGroup(calendar.getCategoryId()) ;
    setLocale(calendar.getLocale()) ;
    setTimeZone(calendar.getTimeZone()) ;
    setSelectedColor(calendar.getCalendarColor()) ;
    lockCheckBoxFields(true) ;
    UIFormInputWithActions sharing = getChildById(INPUT_SHARE) ;
    sharing.getUIStringInput(EDIT_PERMISSION).setEnable(false) ;
    sharing.setActionField(EDIT_PERMISSION, null) ;
  }

  protected String getDisplayName() {
    return getUIStringInput(DISPLAY_NAME).getValue() ;
  }
  protected void setDisplayName(String value) {
    getUIStringInput(DISPLAY_NAME).setValue(value) ;
  }

  protected String getDescription() {
    return getUIFormTextAreaInput(DESCRIPTION).getValue() ;
  }
  protected void setDescription(String value) {
    getUIFormTextAreaInput(DESCRIPTION).setValue(value) ;
  }
  protected String getSelectedGroup() {
    return getUIFormSelectBox(CATEGORY).getValue() ;
  }
  public void setSelectedGroup(String value) {
    getUIFormSelectBox(CATEGORY).setValue(value) ;
  }
  protected String getSelectedColor() {
    return getUIFormSelectBox(SELECT_COLOR).getValue() ;
  }
  protected void setSelectedColor(String value) {
    getUIFormSelectBox(SELECT_COLOR).setValue(value) ;
  }
  protected String getLocale() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIFormSelectBox(LOCALE).getValue() ;
  }
  protected void setLocale(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIFormSelectBox(LOCALE).setValue(value) ;
  }
  protected String getTimeZone() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIFormSelectBox(TIMEZONE).getValue() ;
  }

  protected void setTimeZone(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIFormSelectBox(TIMEZONE).setValue(value) ;
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
  protected boolean isPublic() throws Exception{
    UIFormInputWithActions sharing = getChildById(INPUT_SHARE) ;
    for(Object groupObj : getPublicGroups()) {
      String group = ((Group)groupObj).getId() ;
      UIFormCheckBoxInput checkBox = sharing.getUIFormCheckBoxInput(group) ;
      if( checkBox != null) {
        if(checkBox.isChecked()) {
          return true ;
        }
      }
    }
    return false ;
  }
  private Object[] getPublicGroups() throws Exception {
    OrganizationService organization = getApplicationComponent(OrganizationService.class) ;
    String currentUser = Util.getPortalRequestContext().getRemoteUser() ;
    return organization.getGroupHandler().findGroupsOfUser(currentUser).toArray() ;
  }

  @SuppressWarnings("unchecked")
  private List getSelectedGroups() throws Exception {
    UIFormInputWithActions sharing = getChildById(INPUT_SHARE) ;
    List groups = new ArrayList() ;
    for(Object o : getPublicGroups()) {
      String groupId = ((Group)o).getId() ;
      UIFormCheckBoxInput<Boolean> input =  sharing.getUIFormCheckBoxInput(groupId) ;
      if(input != null && input.isChecked()) {
        groups.add(o) ;
      } 
    }
    return groups  ;
  }
  private List<SelectItemOption<String>> getTimeZones() {
    return CalendarUtils.getTimeZoneSelectBoxOptions(TimeZone.getAvailableIDs()) ;
  } 

  private List<SelectItemOption<String>> getLocales() {
    return CalendarUtils.getLocaleSelectBoxOptions(java.util.Calendar.getAvailableLocales()) ;
  }

  static  public class AddCategoryActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      uiForm.setRenderedChild(INPUT_CALENDAR) ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class);
      uiChildPopup.activate(UICalendarCategoryManager.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }

  static  public class SelectPermissionActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.setRenderedChild(INPUT_SHARE) ;
      if(!uiForm.isPublic()) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UICalendarForm.msg.checkbox-public-notchecked", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null);
      uiGroupSelector.setType(permType) ;
      uiGroupSelector.setSelectedGroups(uiForm.getSelectedGroups()) ;
      uiGroupSelector.setComponent(uiForm, new String[] {EDIT_PERMISSION});
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(uiGroupSelector, 500, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class ResetActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      System.out.println("\n\n ResetActionListener");
      UICalendarForm uiForm = event.getSource() ;
      uiForm.reset() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class SaveActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      Calendar calendar = new Calendar() ;
      if(!uiForm.isAddNew_) calendar.setId(uiForm.calendarId_) ;
      calendar.setName(uiForm.getUIStringInput(DISPLAY_NAME).getValue()) ;
      calendar.setDescription(uiForm.getUIFormTextAreaInput(DESCRIPTION).getValue()) ;
      calendar.setCategoryId(uiForm.getUIFormSelectBox(CATEGORY).getValue()) ;
      boolean isPublic = uiForm.isPublic() ;
      calendar.setPublic(isPublic) ;
      calendar.setLocale(uiForm.getLocale()) ;
      calendar.setTimeZone(uiForm.getTimeZone()) ;
      calendar.setCalendarColor(uiForm.getSelectedColor()) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(isPublic) {
        Object[] groupList = uiForm.getPublicGroups() ;
        List<String> selected = new ArrayList<String>() ;
        for(Object groupObj : groupList) {
          String groupId = ((Group)groupObj).getId() ;
          if(uiForm.getUIFormCheckBoxInput(groupId)!= null && uiForm.getUIFormCheckBoxInput(groupId).isChecked()) { 
            selected.add(groupId) ;
          }
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
        calendarService.saveGroupCalendar(calendar, uiForm.isAddNew_) ;
      }else {
        if(CalendarUtils.isEmpty(uiForm.getUIFormSelectBox(CATEGORY).getValue())) {
          uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.category-empty", null, ApplicationMessage.WARNING) ) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } 
        calendarService.saveUserCalendar(username, calendar, uiForm.isAddNew_) ;        
      }
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
      UICalendarWorkingContainer uiWorkingContainer = calendarPortlet.getChild(UICalendarWorkingContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ; ;
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
