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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.Colors;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
import org.exoplatform.calendar.webui.UIFormColorPicker;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
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
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;

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
      @EventConfig(listeners = UICalendarForm.CancelActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UICalendarForm.SelectTabActionListener.class, phase=Phase.DECODE)
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
  final public static String PERMISSION_SUB = "_permission".intern() ;

  public Map<String, String> permission_ = new HashMap<String, String>() ;
  public Map<String, Map<String, String>> perms_ = new HashMap<String, Map<String, String>>() ;
  //public String calendarId_ = null ;
  public Calendar calendar_ = null ;
  public String calType_ =  CalendarUtils.PRIVATE_TYPE ;
  private boolean isAddNew_ = true ;
  public String groupCalId_ = null ;
  public UICalendarForm() throws Exception{
    super("UICalendarForm");
    UIFormInputWithActions calendarDetail = new UIFormInputWithActions(INPUT_CALENDAR) ;
    calendarDetail.addUIFormInput(new UIFormStringInput(DISPLAY_NAME, DISPLAY_NAME, null).addValidator(MandatoryValidator.class).addValidator(NameValidator.class)) ;
    calendarDetail.addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
    calendarDetail.addUIFormInput(new UIFormSelectBox(CATEGORY, CATEGORY, getCategory())) ;
    calendarDetail.addUIFormInput(new UIFormSelectBox(LOCALE, LOCALE, getLocales())) ;
    calendarDetail.addUIFormInput(new UIFormSelectBox(TIMEZONE, TIMEZONE, getTimeZones())) ;
    calendarDetail.addUIFormInput(new UIFormColorPicker(SELECT_COLOR, SELECT_COLOR, Colors.COLORS)) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData addCategory = new ActionData() ;
    addCategory.setActionListener("AddCategory") ;
    addCategory.setActionType(ActionData.TYPE_ICON) ;
    addCategory.setActionName("AddCategory") ;
    actions.add(addCategory) ;
    calendarDetail.setActionField(CATEGORY, actions) ;
    setSelectedTab(calendarDetail.getId()) ;
    addChild(calendarDetail) ;
    UIGroupCalendarTab sharing = new UIGroupCalendarTab(INPUT_SHARE) ;
    sharing.addUIFormInput(new UIFormInputInfo(SELECT_GROUPS, SELECT_GROUPS, null)) ;
    sharing.addUIFormInput(new UIFormStringInput(EDIT_PERMISSION, null, null)) ;
    for(Object groupObj : getPublicGroups()) {
      String group = ((Group)groupObj).getId() ;
      if(sharing.getUIFormCheckBoxInput(group) != null)sharing.getUIFormCheckBoxInput(group).setChecked(false) ;
      else sharing.addUIFormInput(new UIFormCheckBoxInput<Boolean>(group, group, false)) ;
      if(sharing.getUIFormInputInfo(group+PERMISSION_SUB) == null) {
        sharing.addUIFormInput(new UIFormStringInput(group+PERMISSION_SUB ,group+PERMISSION_SUB, null)) ;
        actions = new ArrayList<ActionData> () ;
        ActionData editPermission = new ActionData() ;
        editPermission.setActionListener("SelectPermission") ;
        editPermission.setActionName("SelectUser") ;
        editPermission.setActionParameter(UISelectComponent.TYPE_USER + ":" + group+PERMISSION_SUB) ;
        editPermission.setActionType(ActionData.TYPE_ICON) ;
        editPermission.setCssIconClass("SelectUserIcon") ;
        actions.add(editPermission) ;
        ActionData membershipPerm = new ActionData() ;
        membershipPerm.setActionListener("SelectPermission") ;
        membershipPerm.setActionName("SelectMemberShip") ;
        membershipPerm.setActionParameter(UISelectComponent.TYPE_MEMBERSHIP + ":" + group+PERMISSION_SUB) ;
        membershipPerm.setActionType(ActionData.TYPE_ICON) ;
        membershipPerm.setCssIconClass("SelectMemberIcon") ;
        actions.add(membershipPerm) ;
        sharing.setActionField(group+PERMISSION_SUB, actions) ;
      }
    }
    addChild(sharing) ;
  }

  public String[] getActions(){
    return new String[]{"Save", "Reset", "Cancel"} ;
  }
 /* private SessionProvider getSession()  {
    return SessionProviderFactory.createSessionProvider() ;
  }

  private SessionProvider getSystemSession()  {
    return SessionProviderFactory.createSystemProvider() ;
  }*/
  private  List<SelectItemOption<String>> getCategory() throws Exception {
    String username = CalendarUtils.getCurrentUser() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<CalendarCategory> categories = calendarService.getCategories(username) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(CalendarCategory category : categories) {
      if (category.getId().equals(NewUserListener.DEFAULT_CALENDAR_CATEGORYID) && category.getName().equals(NewUserListener.DEFAULT_CALENDAR_CATEGORYNAME)) {
        String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.DEFAULT_CALENDAR_CATEGORYID);
        category.setName(newName);
      }
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
      UIFormStringInput uiPermInput = shareTab.getUIStringInput(group + PERMISSION_SUB) ;
      if(uiInput != null) uiInput.setEnable(!isLock) ;
      if(uiPermInput != null) uiPermInput.setEditable(!isLock) ;
      if(isLock) shareTab.setActionField(group + PERMISSION_SUB, null) ;
    }
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  public void resetField() throws Exception {
    permission_.clear() ;
    perms_.clear() ;
    UIGroupCalendarTab sharing = getChildById(INPUT_SHARE) ;
    for(Object obj : getPublicGroups()) {
      String groupId = ((Group)obj).getId() ;
      UIFormCheckBoxInput checkbox = sharing.getUIFormCheckBoxInput(((Group)obj).getId()) ;
      if(checkbox != null) checkbox.setChecked(false) ;
      UIFormStringInput uiInputIfo = sharing.getUIStringInput(groupId + PERMISSION_SUB);
      if(uiInputIfo != null) uiInputIfo.setValue(null) ;
    }
    if(isAddNew_) {
      calendar_ = null ;
      calType_ = CalendarUtils.PRIVATE_TYPE ;
      setDisplayName(null) ;
      setDescription(null) ;
      setSelectedGroup(null) ;
      setLocale(null) ;
      setTimeZone(null) ;
      setSelectedColor(null) ;
      lockCheckBoxFields(false) ;
    } else {
      /*  //Calendar calendar = null ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      if(CalendarUtils.PRIVATE_TYPE.equals(calType_)) {
        calendar = calService.getUserCalendar(getSession(), username, calendarId_) ;
      } else if(CalendarUtils.SHARED_TYPE.equals(calType_)) {
        Iterator iter = calService.getSharedCalendars(getSystemSession(), username, true).getCalendars().iterator() ;
        while (iter.hasNext()) {
          Calendar cal = ((Calendar)iter.next()) ;
          if(cal.getId().equals(calendarId_)) {
            calendar = cal ;
            break ;
          }
        }
      } else if(CalendarUtils.PUBLIC_TYPE.equals(calType_)) {
        calendar = calService.getGroupCalendar(getSystemSession(), calendarId_) ;
      }
      if(calendar != null) */
      init(calendar_) ;
    }

  }
  public boolean isAddNew() { return isAddNew_ ; }
  public void init(Calendar calendar) throws Exception {
    isAddNew_ = false ;
    calendar_ = calendar ;
    //calendarId_ = calendar.getId() ;
    setDisplayName(calendar.getName()) ;
    setDescription(calendar.getDescription()) ;
    UIFormInputWithActions sharing = getChildById(INPUT_SHARE) ;
    sharing.setRendered(true) ;
    if(CalendarUtils.PUBLIC_TYPE.equals(calType_)) {
      UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
      calendarDetail.removeChildById(CATEGORY) ;
      calendarDetail.setActionField(CATEGORY, null) ;
      for(String groupId : calendar.getGroups()) {
        UIFormCheckBoxInput checkbox = sharing.getUIFormCheckBoxInput(groupId) ;
        UIFormStringInput uiInput = sharing.getUIStringInput(groupId + PERMISSION_SUB) ;
        if(checkbox != null) { 
          checkbox.setChecked(true) ;
          StringBuffer sb = new StringBuffer() ;
          List<String> checkList = new ArrayList<String>() ;
          if(calendar.getEditPermission() != null) {
            for(String s : calendar.getEditPermission()) {
              if(s.lastIndexOf(CalendarUtils.SLASH_COLON) > -1) {
                String id = s.split(CalendarUtils.SLASH_COLON)[0].trim() ;
                String perm = s.split(CalendarUtils.SLASH_COLON)[1].trim() ;
                if(groupId.equals(id)) {
                  if(!checkList.contains(s.split(CalendarUtils.SLASH_COLON)[1])) {
                    checkList.add(perm) ;
                    if(sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
                    sb.append(perm) ;
                  }
                }
              }
              /*if(!CalendarUtils.isEmpty(sb.toString())) sb.append(CalendarUtils.COMMA) ;
              if(s.startsWith("*")) sb.append(UIGroupSelector.TYPE_MEMBERSHIP + ":/"+ groupId + ":/"+ s) ;
              else sb.append(UIGroupSelector.TYPE_USER + ":/"+ groupId + ":/"+ s) ;*/
              //updateSelect(groupId + PERMISSION_SUB, sb.toString())  ;
            }
          }
          uiInput.setValue(sb.toString()) ;
        }
      }
    }
    if(CalendarUtils.PRIVATE_TYPE.equals(calType_))
    {
      setSelectedGroup(calendar.getCategoryId()) ;
      lockCheckBoxFields(true) ;
      sharing.setRendered(false) ; 
    }
    setLocale(calendar.getLocale()) ;
    setTimeZone(calendar.getTimeZone()) ;
    setSelectedColor(calendar.getCalendarColor()) ;

  }

  protected String getDisplayName() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIStringInput(DISPLAY_NAME).getValue() ;
  }
  protected void setDisplayName(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIStringInput(DISPLAY_NAME).setValue(value) ;
  }

  protected String getDescription() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIFormTextAreaInput(DESCRIPTION).getValue() ;
  }
  protected void setDescription(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIFormTextAreaInput(DESCRIPTION).setValue(value) ;
  }
  protected String getSelectedGroup() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    if(calendarDetail.getUIFormSelectBox(CATEGORY) != null) return calendarDetail.getUIFormSelectBox(CATEGORY).getValue() ;
    else return null ;
  }
  public void setSelectedGroup(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIFormSelectBox(CATEGORY).setValue(value) ;
  }
  protected String getSelectedColor() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getChild(UIFormColorPicker.class).getValue() ;
  }
  protected void setSelectedColor(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getChild(UIFormColorPicker.class).setValue(value) ;
  }
  protected String getLocale() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIFormSelectBox(LOCALE).getValue() ;
  }
  public void setLocale(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIFormSelectBox(LOCALE).setValue(value) ;
  }
  protected String getTimeZone() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIFormSelectBox(TIMEZONE).getValue() ;
  }

  public void setTimeZone(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIFormSelectBox(TIMEZONE).setValue(value) ;
  }
  @SuppressWarnings("unchecked")
  public void updateSelect(String selectField, String value) throws Exception {
    UIGroupCalendarTab shareTab = getChildById(INPUT_SHARE) ;
    UIFormStringInput fieldInput = shareTab.getUIStringInput(selectField) ;
    StringBuilder sb = new StringBuilder() ;
    Map<String, String> temp = new HashMap<String, String>() ;
    String key = value.substring(0, selectField.lastIndexOf(PERMISSION_SUB));
    String tempS = value.substring(value.lastIndexOf(CalendarUtils.COLON_SLASH) + 2) ;
    if(perms_.get(selectField) == null) {
      temp.put(key, tempS) ;
    } else {
      temp = perms_.get(selectField) ;
      if(temp.get(key) != null && !tempS.equals(temp.get(key))) tempS = temp.get(key) + CalendarUtils.COMMA +  tempS ;
      temp.put(key, tempS) ;
    }
    perms_.put(selectField, temp) ;
    Map<String, String> tempMap = new HashMap<String, String>() ;
    for(String s : temp.values()) {
      for(String t : s.split(CalendarUtils.COMMA)) {
        tempMap.put(t, t) ;
      }
    }
    for(String s : tempMap.values()) {
      if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
      sb.append(s) ;
    }
    fieldInput.setValue(sb.toString()) ;
    setSelectedTab(shareTab.getId()) ;
  }
  protected boolean isPublic() throws Exception{
    UIGroupCalendarTab sharing = getChildById(INPUT_SHARE) ;
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
    String currentUser = CalendarUtils.getCurrentUser() ;
    return organization.getGroupHandler().findGroupsOfUser(currentUser).toArray() ;
  }

  @SuppressWarnings("unchecked")
  private List getSelectedGroups(String groupId) throws Exception {
    UIGroupCalendarTab sharing = getChildById(INPUT_SHARE) ;
    List groups = new ArrayList() ;
    Group g = (Group)getApplicationComponent(OrganizationService.class).getGroupHandler().findGroupById(groupId) ;
    UIFormCheckBoxInput<Boolean> input =  sharing.getUIFormCheckBoxInput(groupId) ;
    if(input != null && input.isChecked()) {
      groups.add(g) ;
    } 
    return groups  ;
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

  static  public class AddCategoryActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      uiForm.setSelectedTab(INPUT_CALENDAR) ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class);
      uiChildPopup.activate(UICalendarCategoryManager.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }

  static  public class SelectPermissionActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      uiForm.setSelectedTab(INPUT_SHARE) ;
      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String permType = value.split(CalendarUtils.COLON)[0] ;
      String fieldId = value.split(CalendarUtils.COLON)[1] ;
      String checkBoxId = fieldId.split(PERMISSION_SUB)[0] ;
      UIGroupCalendarTab shareTab = uiForm.getChildById(INPUT_SHARE) ;
      UIFormCheckBoxInput checkBox = shareTab.getUIFormCheckBoxInput(checkBoxId) ;
      if(!checkBox.isChecked()) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UICalendarForm.msg.checkbox-notchecked", new String[]{checkBoxId}, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      if(!uiForm.isPublic()) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UICalendarForm.msg.checkbox-public-notchecked", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      String currentUsers = shareTab.getUIStringInput(fieldId).getValue() ;
      if(uiForm.perms_.get(fieldId) != null) uiForm.perms_.get(fieldId).clear() ;
      if(!CalendarUtils.isEmpty(currentUsers)) {
        for(String user : currentUsers.split(CalendarUtils.COMMA)) {
          user = user.trim() ;
          String fullKey = permType + CalendarUtils.COLON_SLASH + fieldId +  CalendarUtils.COLON_SLASH + user ;
          uiForm.updateSelect(fieldId, fullKey) ;
        }
      }
      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null);
      uiGroupSelector.setType(permType) ;
      String groupId = value.split(CalendarUtils.COLON)[1].split(PERMISSION_SUB)[0] ;
      uiGroupSelector.setSelectedGroups(uiForm.getSelectedGroups(groupId)) ;
      uiGroupSelector.changeGroup(groupId) ;
      uiGroupSelector.setComponent(uiForm, new String[] {value.split(CalendarUtils.COLON)[1]});
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(uiGroupSelector, 500, 0, true) ;
      uiGroupSelector.setFilter(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class ResetActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      uiForm.resetField() ;
      if(uiForm.isAddNew_) {
        UICalendarPortlet uiCalendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        uiForm.setSelectedGroup(uiForm.groupCalId_) ;
        uiForm.setTimeZone(uiCalendarPortlet.getCalendarSetting().getTimeZone()) ;
        uiForm.setLocale(uiCalendarPortlet.getCalendarSetting().getLocation()) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class SaveActionListener extends EventListener<UICalendarForm> {
    @SuppressWarnings({ "unchecked", "deprecation" })
    public void execute(Event<UICalendarForm> event) throws Exception {
      try {
        UICalendarForm uiForm = event.getSource() ;
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        String displayName = uiForm.getUIStringInput(DISPLAY_NAME).getValue() ;
//      CS-3009
        displayName = CalendarUtils.reduceSpace(displayName) ;
        /*if(!CalendarUtils.isNameValid(displayName, CalendarUtils.SPECIALCHARACTER)){
          uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.name-invalid", null, ApplicationMessage.WARNING) ) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }*/
        displayName = displayName.trim() ;
        CalendarService calendarService = CalendarUtils.getCalendarService() ;
        String username = CalendarUtils.getCurrentUser() ;
        String calendarCategoryId = uiForm.getSelectedGroup() ;
        boolean isPublic = uiForm.isPublic() ;
        if(isPublic) uiForm.calType_ = CalendarUtils.PUBLIC_TYPE ;
        Calendar calendar = new Calendar() ;
        if(!uiForm.isAddNew_) calendar = uiForm.calendar_ ;
        calendar.setName(displayName) ;
        calendar.setDescription(uiForm.getDescription()) ;
        calendar.setLocale(uiForm.getLocale()) ;
        calendar.setTimeZone(uiForm.getTimeZone()) ;
        calendar.setCalendarColor(uiForm.getSelectedColor()) ;
        calendar.setCalendarOwner(username) ;
        if(CalendarUtils.PRIVATE_TYPE.equals(uiForm.calType_)) {
          if(CalendarUtils.isEmpty(calendarCategoryId)) {
            uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.category-empty", null, ApplicationMessage.WARNING) ) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          calendar.setCategoryId(calendarCategoryId) ;
          List<Calendar> pCals = calendarService.getUserCalendars(username, true) ;
          for(Calendar cal : pCals) {
            if (cal.getId().equals(NewUserListener.DEFAULT_CALENDAR_ID) && cal.getName().equals(NewUserListener.DEFAULT_CALENDAR_NAME)) {
              String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.DEFAULT_CALENDAR_ID);
              cal.setName(newName);
            }
            if(uiForm.isAddNew_) {
              if(cal.getName().trim().equalsIgnoreCase(displayName.trim())) {
                uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{displayName}, ApplicationMessage.WARNING)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                return ;
              }
            } else {
              if(cal.getName().trim().equalsIgnoreCase(displayName.trim()) && !cal.getId().equals(calendar.getId())) {
                uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{displayName}, ApplicationMessage.WARNING)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                return ;
              }
            }
          }
          calendarService.saveUserCalendar(username, calendar, uiForm.isAddNew_) ;    
        } else if(CalendarUtils.SHARED_TYPE.equals(uiForm.calType_)) {
          calendarService.saveSharedCalendar(username, calendar) ;
        }else if (CalendarUtils.PUBLIC_TYPE.equals(uiForm.calType_)) {
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
          
          // CS-3607
          List<GroupCalendarData> groupCalendars = calendarService.getGroupCalendars(selected.toArray(new String[] {}), false, username) ;
          for (GroupCalendarData groupCalendarData : groupCalendars) {
            for (Calendar calendar2 : groupCalendarData.getCalendars()) {
              if (uiForm.isAddNew_) {
                if(calendar2.getName().equalsIgnoreCase(displayName.trim())) {
                  uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{displayName}, ApplicationMessage.WARNING)) ;
                  event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                  return ;
                }                
              } else {
                if(calendar2.getName().trim().equalsIgnoreCase(displayName.trim()) && !calendar2.getId().equals(calendar.getId())) {
                  uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{displayName}, ApplicationMessage.WARNING)) ;
                  event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                  return ;
                }
              }
            }
          }
          
          calendar.setPublic(isPublic) ;
          calendar.setGroups(selected.toArray((new String[]{})));
          List<String> listPermission = new ArrayList<String>() ;
          OrganizationService orgService = CalendarUtils.getOrganizationService() ;
          for(String groupIdSelected : selected) {
            String groupKey = groupIdSelected + CalendarUtils.SLASH_COLON ;
            UIFormInputWithActions sharedTab = uiForm.getChildById(UICalendarForm.INPUT_SHARE) ;
            String typedPerms = sharedTab.getUIStringInput(groupIdSelected + PERMISSION_SUB).getValue();
            if(!CalendarUtils.isEmpty(typedPerms)) {
              for(String s : typedPerms.split(CalendarUtils.COMMA)){
                s = s.trim() ;
                if(!CalendarUtils.isEmpty(s)) {
                  List<User> users = orgService.getUserHandler().findUsersByGroup(groupIdSelected).getAll() ;  
                  boolean isExisted = false ;
                  for(User u : users) {
                    if(u.getUserName().equals(s)) {
                      isExisted = true ;
                      break ;
                    }
                  }
                  if(isExisted) {             
                    listPermission.add(groupKey + s) ;
                  } else {
                    if(s.equals(CalendarUtils.ANY)) listPermission.add(groupKey + s) ; 
                    else if(s.indexOf(CalendarUtils.ANY_OF) > -1) {
                      String typeName = s.substring(s.lastIndexOf(CalendarUtils.DOT)+ 1, s.length()) ;
                      if(orgService.getMembershipTypeHandler().findMembershipType(typeName) != null) {
                        listPermission.add(groupKey + s) ;
                      } else {
                        uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.name-not-on-group", new Object[]{s,groupKey}, ApplicationMessage.WARNING)) ;
                        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                        return ;
                      } 
                    } else {
                      uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.name-not-on-group", new Object[]{s,groupKey}, ApplicationMessage.WARNING)) ;
                      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                      return ;
                    }
                  }
                }
              }
            }
            Collection<Membership> mbsh = CalendarUtils.getOrganizationService().getMembershipHandler().findMembershipsByUser(username) ;
            if(!listPermission.contains(groupKey + CalendarUtils.getCurrentUser()) 
                && !CalendarUtils.isMemberShipType(mbsh, typedPerms))
            { 
              listPermission.add(groupKey + CalendarUtils.getCurrentUser()) ;
            }
          }        
          calendar.setEditPermission(listPermission.toArray(new String[listPermission.size()])) ;
          calendarService.savePublicCalendar(calendar, uiForm.isAddNew_, username) ;
        } 
        UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        calendarPortlet.setCalendarSetting(null) ;
        calendarPortlet.cancelAction() ;
        UICalendarWorkingContainer uiWorkingContainer = calendarPortlet.getChild(UICalendarWorkingContainer.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ; ;
      }catch (Exception e) { 
        e.printStackTrace() ;
      }
    }
  }
  static  public class CancelActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }
  
  static public class SelectTabActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource()) ;      
    }
  }
}
