/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
      @EventConfig(listeners = UISelectUserForm.SaveActionListener.class), 
      @EventConfig(listeners = UISelectUserForm.SearchActionListener.class), 
      @EventConfig(listeners = UISelectUserForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UISelectUserForm extends UIForm implements UIPopupComponent { 
  final public static String FIELD_KEYWORD = "keyWord".intern() ;
  final public static String FIELD_GROUP = "group".intern() ;
  

  private List<User> data_  = new ArrayList<User>() ;
  private boolean isShowSearch_ = false ;
  protected String tabId_ = null ;
  public List<User> getData() {
    return data_ ;
  }
  public void update(List<User> list) {
    data_ = list ;
  }
  public UISelectUserForm() throws Exception {  
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    PageList pl = service.getUserHandler().getUserPageList(0) ;
    for(Object o : pl.getAll()){
      User user =  (User)o ;
      data_.add(user) ;
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(user.getUserName(),user.getUserName(), false)) ;
    }
    initSearchForm() ;

  }
  public void  initSearchForm() throws Exception{
    addUIFormInput(new UIFormStringInput(FIELD_KEYWORD, FIELD_KEYWORD, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_GROUP, FIELD_GROUP, getGroups())) ;
  }
  private List<SelectItemOption<String>> getGroups() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    ContactService contactService = getApplicationComponent(ContactService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    options.add(new SelectItemOption<String>("all", "")) ;
    for( ContactGroup cg : contactService.getGroups(SessionsUtils.getSessionProvider(), username)) {
      options.add(new SelectItemOption<String>(cg.getName(), cg.getId())) ;
    }
    return options;
  }

  public String[] getActions() { return new String[]{"Save", "Cancel"}; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {} 
  public void setShowSearch(boolean isShowSearch) {
    this.isShowSearch_ = isShowSearch;
  }
  public boolean isShowSearch() {
    return isShowSearch_;
  }

  static  public class SaveActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception { 
      System.out.println("======== >>>UISelectUserForm.SaveActionListener");
      UISelectUserForm uiForm = event.getSource();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      if(uiEventForm != null) {
        StringBuilder sb = new StringBuilder() ;
        for(User u : uiForm.getData()) {
          UIFormCheckBoxInput input = uiForm.getUIFormCheckBoxInput(u.getUserName()) ;
          if(input != null && input.isChecked()) {
            if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COLON) ;
          	sb.append(u.getUserName()) ;
          }
        }
        uiEventForm.setSelectedTab(uiForm.tabId_) ;
        uiEventForm.setParticipant(sb.toString()) ;
        ((UIEventAttenderTab)uiEventForm.getChildById(uiEventForm.TAB_EVENTATTENDER)).updateParticipants(sb.toString()) ;
      } 
      //UIPopupAction parentPopup = uiContainer.getAncestorOfType(UIPopupAction.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm.getChildById(uiEventForm.TAB_EVENTATTENDER)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm.getChildById(uiEventForm.TAB_EVENTSHARE)) ;
    }  
  } 
  static  public class SearchActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
    }
  }
  static  public class CancelActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiAddressForm = event.getSource();  
      UIPopupContainer uiContainer = uiAddressForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction parentPopup = uiContainer.getAncestorOfType(UIPopupAction.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(parentPopup) ;
    }
  }
}
