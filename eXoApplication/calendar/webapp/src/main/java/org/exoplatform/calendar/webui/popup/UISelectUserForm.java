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

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@gmail.com
 * Dec 11, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/calendar/webui/UIPopup/UISelectUserForm.gtmpl",
    events = {
      @EventConfig(listeners = UISelectUserForm.ReplaceActionListener.class), 
      @EventConfig(listeners = UISelectUserForm.AddActionListener.class, phase = Phase.DECODE), 
      @EventConfig(listeners = UISelectUserForm.SearchActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.ChangeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.ShowPageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.CloseActionListener.class, phase = Phase.DECODE)
    }
)

public class UISelectUserForm extends UIForm implements UIPopupComponent { 
  final public static String FIELD_KEYWORD = "keyWord".intern() ;
  final public static String FIELD_GROUP = "group".intern() ;


  private Map<String, User> userData_ = new HashMap<String, User>() ;
  private boolean isShowSearch_ = false ;
  protected String tabId_ = null ;
  protected String groupId_ = null ;
  protected Collection<String> pars_ ;
  public UIPageIterator uiIterator_ ;

  public List<User> getData() throws Exception {
    List<User> users = new ArrayList<User>() ;
    OrganizationService orService = CalendarUtils.getOrganizationService() ;
    for(Object obj : uiIterator_.getCurrentPageData()){
      User user = (User)obj ;
      if(CalendarUtils.isEmpty(groupId_)) {
        users.add(user) ;
      } else {
        for(Object gObj : orService.getGroupHandler().findGroupsOfUser(user.getUserName())){
          Group g = (Group)gObj ;
          if(groupId_.equals(g.getId())) users.add(user) ;
        }
      }
      if(getUIFormCheckBoxInput(user.getUserName()) == null)
        addUIFormInput(new UIFormCheckBoxInput<Boolean>(user.getUserName(),user.getUserName(), false)) ;
    } 
    for(String currentUsers : pars_) {
      if(getUIFormCheckBoxInput(currentUsers) != null) 
        getUIFormCheckBoxInput(currentUsers).setChecked(true) ;
    }
    return  users;
  }
  public UISelectUserForm() throws Exception {  
    uiIterator_ = new UIPageIterator() ;
    uiIterator_.setId("UISelectUserPage") ;
  }
  public UIPageIterator  getUIPageIterator() {  return uiIterator_ ; }
  public long getAvailablePage(){ return uiIterator_.getAvailablePage() ;}
  public long getCurrentPage() { return uiIterator_.getCurrentPage();}

  public void init(Collection<String> pars) throws Exception{
    if(getChildren()!= null) getChildren().clear() ;
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    ObjectPageList objPageList = new ObjectPageList(service.getUserHandler().getUserPageList(0).getAll(), 10) ;
    uiIterator_.setPageList(objPageList) ;
    for(String s : pars) {
      if(getUIFormCheckBoxInput(s) != null) getUIFormCheckBoxInput(s).setChecked(true) ;
    }
    pars_ = pars ;
  }
  public void  initSearchForm() throws Exception{
    addUIFormInput(new UIFormStringInput(FIELD_KEYWORD, FIELD_KEYWORD, null)) ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(FIELD_GROUP, FIELD_GROUP, getGroups()) ;
    addUIFormInput(uiSelectBox) ;
    uiSelectBox.setOnChange("Change") ;
    isShowSearch_ = true ;
  }
  private List<SelectItemOption<String>> getGroups() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    OrganizationService orgService = CalendarUtils.getOrganizationService() ;
    options.add(new SelectItemOption<String>("all", "")) ;
    for( Object g : orgService.getGroupHandler().getAllGroups()) { 
      Group  cg = (Group)g ;
      options.add(new SelectItemOption<String>(cg.getGroupName(), cg.getId())) ;
    }
    return options;
  }

  public String[] getActions() { return new String[]{"Add", "Replace", "Close"}; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {} 
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  public void setShowSearch(boolean isShowSearch) {
    this.isShowSearch_ = isShowSearch;
  }
  public boolean isShowSearch() {
    return isShowSearch_;
  }
  public String getSelectedGroup() {

    return getUIFormSelectBox(FIELD_GROUP).getValue() ;
  }
  public void setSelectedGroup(String selectedGroup) {
    getUIFormSelectBox(FIELD_GROUP).setValue(selectedGroup) ;
    groupId_ = selectedGroup ;
  }
  static  public class AddActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception { 
      System.out.println("======== >>>UISelectUserForm.SaveActionListener");
      UISelectUserForm uiForm = event.getSource();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      if(uiEventForm != null) {

        StringBuilder sb = new StringBuilder() ;
        for(Object o : uiForm.uiIterator_.getCurrentPageData()) {
          User u = (User)o ;
          UIFormCheckBoxInput input = uiForm.getUIFormCheckBoxInput(u.getUserName()) ;
          if(input != null && input.isChecked()) {
            if(!uiForm.pars_.contains(u.getUserName())) {
              if(!CalendarUtils.isEmpty(sb.toString())) sb.append(CalendarUtils.COMMA) ;
              sb.append(u.getUserName()) ;
            }
          }
        }
        if(CalendarUtils.isEmpty(sb.toString())) {
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UISelectUserForm.msg.user-required",null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        for(String s : uiForm.pars_) {
          if(!CalendarUtils.isEmpty(sb.toString())) sb.append(CalendarUtils.COMMA) ;
          sb.append(s) ;
        }
        uiEventForm.setSelectedTab(uiForm.tabId_) ;
        uiEventForm.setParticipant(sb.toString()) ;
        ((UIEventAttenderTab)uiEventForm.getChildById(uiEventForm.TAB_EVENTATTENDER)).updateParticipants(sb.toString()) ;
      } 
    }  
  } 

  protected void updateCurrentPage(int page) throws Exception{
    uiIterator_.setCurrentPage(page) ;
  }
  public void setKeyword(String value) {
    getUIStringInput(FIELD_KEYWORD).setValue(value) ;
  }
  static  public class ReplaceActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception { 
      System.out.println("======== >>>UISelectUserForm.SaveActionListener");
      UISelectUserForm uiForm = event.getSource();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      if(uiEventForm != null) {
        StringBuilder sb = new StringBuilder() ;
        for(Object o : uiForm.uiIterator_.getCurrentPageData()) {
          User u = (User)o ;
          UIFormCheckBoxInput input = uiForm.getUIFormCheckBoxInput(u.getUserName()) ;
          if(input != null && input.isChecked()) {
            if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
            sb.append(u.getUserName()) ;
          }
        }
        if(CalendarUtils.isEmpty(sb.toString())) {
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UISelectUserForm.msg.user-required",null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        uiEventForm.setSelectedTab(uiForm.tabId_) ;
        uiEventForm.setParticipant(sb.toString()) ;
        ((UIEventAttenderTab)uiEventForm.getChildById(uiEventForm.TAB_EVENTATTENDER)).updateParticipants(sb.toString()) ;
      } 
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm.getChildById(uiEventForm.TAB_EVENTATTENDER)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm.getChildById(uiEventForm.TAB_EVENTSHARE)) ;
    }  
  } 
  static  public class SearchActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiForm = event.getSource() ;
      OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class) ;
      String keyword = uiForm.getUIStringInput(UISelectUserForm.FIELD_KEYWORD).getValue();
      uiForm.groupId_ = uiForm.getSelectedGroup() ;
      uiForm.setSelectedGroup(uiForm.getSelectedGroup()) ;
      if(keyword == null || keyword.trim().length() <= 0) keyword = "*" ;
      keyword = "*" + keyword.toLowerCase() + "*" ;
      Query q = new Query() ;
      q.setUserName(keyword) ;
      List results = new ArrayList() ;
      results.addAll(service.getUserHandler().findUsers(q).getAll()) ;
      q = new Query() ;
      q.setEmail(keyword) ;
      results.addAll(service.getUserHandler().findUsers(q).getAll()) ;
      q = new Query() ;
      q.setFirstName(keyword) ;
      results.addAll(service.getUserHandler().findUsers(q).getAll()) ;
      q = new Query() ;
      q.setLastName(keyword) ;
      results.addAll(service.getUserHandler().findUsers(q).getAll()) ;
      uiForm.groupId_ = null ;
      ObjectPageList objPageList = new ObjectPageList(results, 10) ;
      uiForm.uiIterator_.setPageList(objPageList);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  static  public class ChangeActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiForm = event.getSource() ;
      uiForm.setSelectedGroup(uiForm.getSelectedGroup()) ;
      uiForm.setKeyword(null) ;
      OrganizationService service = CalendarUtils.getOrganizationService() ;
      if(!CalendarUtils.isEmpty(uiForm.getSelectedGroup())) {
        uiForm.uiIterator_.setPageList(service.getUserHandler().findUsersByGroup(uiForm.getSelectedGroup()));
      } else {
        uiForm.uiIterator_.setPageList(service.getUserHandler().getUserPageList(0));
      }
      for(String s : uiForm.pars_) {
        if(uiForm.getUIFormCheckBoxInput(s) != null) uiForm.getUIFormCheckBoxInput(s).setChecked(true) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  static  public class CloseActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiAddressForm = event.getSource();  
      UIPopupContainer uiContainer = uiAddressForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
    }
  }
  static  public class ShowPageActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiSelectUserForm = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiSelectUserForm.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectUserForm);           
    }
  }
}
