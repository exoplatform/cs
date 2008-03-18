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

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
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
//import org.exoplatform.contact.webui.*;
/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@gmail.com
 * Nov 06, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/calendar/webui/UIPopup/UIAddressForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAddressForm.AddActionListener.class), 
      @EventConfig(listeners = UIAddressForm.ReplaceActionListener.class),
      @EventConfig(listeners = UIAddressForm.SearchActionListener.class), 
      @EventConfig(listeners = UIAddressForm.ChangeGroupActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAddressForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAddressForm extends UIForm implements UIPopupComponent { 
  final public static String FIELD_KEYWORD = "keyWord".intern() ;
  final public static String FIELD_GROUP = "group".intern() ;

  private List<Contact> alreadyCheckedContact = new ArrayList<Contact>();

  private String recipientsType = "";
  protected String selectedAddressId_ = "" ;
  public void setRecipientsType(String type)  {
    recipientsType=type;
  }
  public String getRecipientType(){
    return recipientsType;
  }

  public UIAddressForm() throws Exception {  
    setContactList();
  }
  public void  initSearchForm() throws Exception{
    addUIFormInput(new UIFormStringInput(FIELD_KEYWORD, FIELD_KEYWORD, null)) ;
    UIFormSelectBox fieldGroup = new UIFormSelectBox(FIELD_GROUP, FIELD_GROUP, getGroups()) ;
    fieldGroup.setOnChange("ChangeGroup") ;
    addUIFormInput(fieldGroup) ;

  }
  private List<SelectItemOption<String>> getGroups() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    ContactService contactService = getApplicationComponent(ContactService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    options.add(new SelectItemOption<String>("all", "")) ;
    SessionProvider sessionPro = SessionProviderFactory.createSessionProvider() ;
    for( ContactGroup cg : contactService.getGroups(sessionPro, username)) {
      options.add(new SelectItemOption<String>(cg.getName(), cg.getId())) ;
    }
    
    OrganizationService organizationService = 
      (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objGroupIds = 
      organizationService.getGroupHandler().findGroupsOfUser(CalendarUtils.getCurrentUser()).toArray() ;
    List<String> groupIds = new ArrayList<String>() ;
    List<String> groupNames = new ArrayList<String>() ;
    for (Object object : objGroupIds) {
      if(object != null) {
        groupIds.add(((GroupImpl)object).getId()) ;
        groupNames.add(((GroupImpl)object).getGroupName()) ;
      }
    }
    
    for(int i = 0 ; i < groupIds.size() ; i ++ )  {
        options.add(new SelectItemOption<String>(groupNames.get(i), groupIds.get(i))) ;
    }
    return options;
  }

  public String[] getActions() { return new String[]{"Add", "Replace", "Cancel"}; }

  private Map<String, Contact> contactMap_ = new HashMap<String, Contact>(); 

  public void activate() throws Exception {}
  public void deActivate() throws Exception {} 
  public List<Contact> getContacts() throws Exception { 
    return new ArrayList<Contact>(contactMap_.values());
  }


  public void setContactList() throws Exception {
    setContactList("");
  }

  public void setContactList(String groupId) throws Exception {
    List<Contact> contacts = new ArrayList<Contact>();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    String username = Util.getPortalRequestContext().getRemoteUser();
    if(CalendarUtils.isEmpty(groupId)) {
      ContactFilter filter = new ContactFilter() ;
      DataPageList resultPageList = 
        contactSrv.searchContact(SessionProviderFactory.createSystemProvider(), CalendarUtils.getCurrentUser(), filter) ;
      contacts = resultPageList.getAll() ;
    } else {
      contacts = contactSrv.getContactPageListByGroup(SessionProviderFactory.createSessionProvider(), username, groupId).getAll();
    }
    setContactList(contacts);
  }

  public void setContactList(List<Contact> contactList) throws Exception {
    getChildren().clear();
    initSearchForm() ;
    contactMap_.clear();
    for (Contact contact : contactList) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false);
      addUIFormInput(uiCheckbox);
      for (Contact ct : getAlreadyCheckedContact()) {
        if(ct.getId().equals(contact.getId()))
        {
          uiCheckbox.setChecked(true);
        }
      }
      contactMap_.put(contact.getId(), contact);
    }
  }

  public void setAlreadyCheckedContact(List<Contact> alreadyCheckedContact) throws Exception {
    if(alreadyCheckedContact!=null)
    {    
      this.alreadyCheckedContact = alreadyCheckedContact;
    }
  }

  public List<Contact> getAlreadyCheckedContact() {
    return alreadyCheckedContact;
  }


  public List<Contact> getCheckedContact() throws Exception {
    List<Contact> contactList = new ArrayList<Contact>();  
    for (Contact contact : getContacts()) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = getChildById(contact.getId());
      if (uiCheckbox!=null && uiCheckbox.isChecked()) {
        contactList.add(contact);
      }
    }
    return contactList;
  }
  
  static public class AddActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      System.out.println("======== >>>>>>UIAddressForm.AddActionListener") ;
      UIAddressForm uiForm = event.getSource() ;
      
      if(uiForm.getCheckedContact().size() <= 0) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required",null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UITaskForm uiTaskForm = uiContainer.findFirstComponentOfType(UITaskForm.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      StringBuffer sb = new StringBuffer() ;
      
      if(uiTaskForm != null) {
        if(uiTaskForm.getEmailAddress() != null && uiTaskForm.getEmailAddress().trim().length() > 0) {
          sb.append(uiTaskForm.getEmailAddress()) ;
        }
      } else if (uiEventForm != null) {
        if(uiEventForm.getEmailAddress() != null && uiEventForm.getEmailAddress().trim().length() > 0) {
          sb.append(uiEventForm.getEmailAddress()) ;
        }
      }
      
      List<String> listMail = Arrays.asList( sb.toString().split(CalendarUtils.COMMA)) ; 
      String email = null ;
      for(Contact c : uiForm.getCheckedContact()) {
        email = c.getEmailAddress() ;
        if(!listMail.contains(email)) {
          if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
          sb.append(email) ;
        }
      }
      
      if(uiTaskForm != null) {
        uiTaskForm.setSelectedTab(UITaskForm.TAB_TASKREMINDER) ;
        uiTaskForm.setEmailAddress(sb.toString()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskForm) ;
      }else if(uiEventForm != null) {
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTREMINDER) ;
        uiEventForm.setEmailAddress(sb.toString()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm) ;
      }
    }
  }
  
  static  public class ReplaceActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception { 
      System.out.println("======== >>>UIAddressForm.ReplaceActionListener");
      UIAddressForm uiForm = event.getSource();
      if(uiForm.getCheckedContact().size() <= 0) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required",null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UITaskForm uiTaskForm = uiContainer.findFirstComponentOfType(UITaskForm.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      StringBuilder sb = new StringBuilder() ;
      for(Contact c : uiForm.getCheckedContact()) {
        if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
        sb.append(c.getEmailAddress()) ;
      }
      if(uiTaskForm != null) {
        uiTaskForm.setSelectedTab(UITaskForm.TAB_TASKREMINDER) ;
        uiTaskForm.setEmailAddress(sb.toString()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskForm) ;
      } 
      if(uiEventForm != null) {
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTREMINDER) ;
        uiEventForm.setEmailAddress(sb.toString()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm) ;
      } 
      //UIPopupAction parentPopup = uiContainer.getAncestorOfType(UIPopupAction.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }  
  } 
  static  public class SearchActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource();  
      ContactService contactService = uiForm.getApplicationComponent(ContactService.class) ;
      String text = uiForm.getUIStringInput(UIAddressForm.FIELD_KEYWORD).getValue() ;
      String category = uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).getValue() ;
      if(category.equals(NewUserListener.DEFAULTGROUP)) category = category + CalendarUtils.getCurrentUser() ;
      uiForm.selectedAddressId_ = category ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(CalendarUtils.isEmpty(text)) {
        uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.keyword-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      ContactFilter filter = new ContactFilter() ;
      if(!CalendarUtils.isEmpty(uiForm.selectedAddressId_)) {
        filter.setCategories(new String[]{uiForm.selectedAddressId_}) ;
      } 
      filter.setText(text) ;
      DataPageList resultPageList = 
        contactService.searchContact(SessionProviderFactory.createSystemProvider(), event.getRequestContext().getRemoteUser(), filter) ;
      uiForm.setContactList(resultPageList.getAll()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  static  public class ChangeGroupActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource();  
      ContactService contactService = uiForm.getApplicationComponent(ContactService.class) ;
      String category = uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).getValue() ;
      if(category.equals(NewUserListener.DEFAULTGROUP)) category = category + CalendarUtils.getCurrentUser() ;
      uiForm.selectedAddressId_ = category ;
      ContactFilter filter = new ContactFilter() ;
      if(!CalendarUtils.isEmpty(uiForm.selectedAddressId_)) {
        filter.setCategories(new String[]{uiForm.selectedAddressId_}) ;
      }
      DataPageList resultPageList = null ;
      if(category.equals("shared_" + CalendarUtils.getCurrentUser())) {
        resultPageList = contactService.getSharedContacts(CalendarUtils.getCurrentUser()) ;
      } else {
        resultPageList = 
          contactService.searchContact(SessionProviderFactory.createSystemProvider(), event.getRequestContext().getRemoteUser(), filter) ;
      }
      uiForm.setContactList(resultPageList.getAll()) ;
      uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).setValue(uiForm.selectedAddressId_) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();  
      UIPopupContainer uiContainer = uiAddressForm.getAncestorOfType(UIPopupContainer.class) ;
      //UIPopupAction parentPopup = uiContainer.getAncestorOfType(UIPopupAction.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
