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
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.impl.GroupImpl;
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
      @EventConfig(listeners = UIAddressForm.ShowPageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAddressForm.ChangeGroupActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAddressForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAddressForm extends UIForm implements UIPopupComponent { 
  final public static String FIELD_KEYWORD = "keyWord".intern() ;
  final public static String FIELD_GROUP = "group".intern() ;
  private String recipientsType = "";
  protected String selectedAddressId_ = "" ;
  private UIPageIterator uiPageIterator_ ;
  public void setRecipientsType(String type)  {
    recipientsType=type;
  }
  public String getRecipientType(){
    return recipientsType;
  }

  public UIAddressForm() throws Exception {  
    addUIFormInput(new UIFormStringInput(FIELD_KEYWORD, FIELD_KEYWORD, null)) ;
    UIFormSelectBox fieldGroup = new UIFormSelectBox(FIELD_GROUP, FIELD_GROUP, getGroups()) ;
    fieldGroup.setOnChange("ChangeGroup") ;
    addUIFormInput(fieldGroup) ;
    uiPageIterator_ = new UIPageIterator() ;
    uiPageIterator_.setId("UICalendarAddressPage") ;
  }
  private List<SelectItemOption<String>> getGroups() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    ContactService contactService = getApplicationComponent(ContactService.class) ;
    options.add(new SelectItemOption<String>("all", "")) ;
    SessionProvider sessionPro = SessionProviderFactory.createSessionProvider() ;
    for( ContactGroup cg : contactService.getGroups(sessionPro, CalendarUtils.getCurrentUser())) {
      options.add(new SelectItemOption<String>(cg.getName(), cg.getId())) ;
    }
    List<SharedAddressBook> addressList = contactService
    .getSharedAddressBooks(SessionProviderFactory.createSystemProvider(), CalendarUtils.getCurrentUser()) ;
    for(SharedAddressBook sa : addressList) {
      options.add(new SelectItemOption<String>(sa.getName(), sa.getId())) ;
    }
    Object[] objGroups = 
      CalendarUtils.getOrganizationService().getGroupHandler().findGroupsOfUser(CalendarUtils.getCurrentUser()).toArray() ;
    for (Object object : objGroups) {
      if(object != null) {
        GroupImpl g = (GroupImpl)object ;
        options.add(new SelectItemOption<String>(g.getGroupName(), g.getId())) ;
      }
    }
    return options;
  }

  public String[] getActions() { return new String[]{"Add", "Replace", "Cancel"}; }

  //private Map<String, Contact> contactMap_ = new HashMap<String, Contact>(); 

  public void activate() throws Exception {}
  public void deActivate() throws Exception {} 
  @SuppressWarnings("unchecked")
  public List<Contact> getContacts() throws Exception { 
    return new ArrayList<Contact>(uiPageIterator_.getCurrentPageData());
  }
  public void setContactList(String groupId) throws Exception {
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    ContactFilter filter = new ContactFilter() ;
    if(!CalendarUtils.isEmpty(groupId)) {
      filter.setCategories(new String[]{groupId}) ;
    }  
    DataPageList resultPageList = 
      contactSrv.searchContact(SessionProviderFactory.createSystemProvider(), CalendarUtils.getCurrentUser(), filter) ;
    setContactList(resultPageList.getAll());
  }

  public void setContactList(List<Contact> contactList) throws Exception {
    getUIFormSelectBox(FIELD_GROUP).setOptions(getGroups()) ;
    ObjectPageList objPageList = new ObjectPageList(contactList, 10) ;
    uiPageIterator_.setPageList(objPageList) ;
    for (Contact contact : contactList) {
      UIFormCheckBoxInput uiCheckbox = getUIFormCheckBoxInput(contact.getId()) ;
      if(uiCheckbox == null) {
        uiCheckbox = new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false) ;
        addUIFormInput(uiCheckbox);
      } 
    }
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
  public UIPageIterator  getUIPageIterator() {  return uiPageIterator_ ; }
  public long getAvailablePage(){ return uiPageIterator_.getAvailablePage() ;}
  public long getCurrentPage() { return uiPageIterator_.getCurrentPage();}
  protected void updateCurrentPage(int page) throws Exception{
    uiPageIterator_.setCurrentPage(page) ;
  }

  static public class AddActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
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
        if(!CalendarUtils.isEmpty(email) && !listMail.contains(email)) {
          if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
          if(email != null) sb.append(email) ;
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
        if(!CalendarUtils.isEmpty(c.getEmailAddress())) {
          if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
          sb.append(c.getEmailAddress()) ;
        }
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
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
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
      try {
        ContactFilter filter = new ContactFilter() ;
        if(!CalendarUtils.isEmpty(uiForm.selectedAddressId_)) {
          filter.setCategories(new String[]{uiForm.selectedAddressId_}) ;
        } 
        if(!CalendarUtils.isEmpty(text))
          filter.setText(CalendarUtils.encodeJCRText(text)) ;
        DataPageList resultPageList = 
          contactService.searchContact(SessionProviderFactory.createSystemProvider(), event.getRequestContext().getRemoteUser(), filter) ;
        uiForm.setContactList(resultPageList.getAll()) ;
        uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).setValue(category) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
      } catch (Exception e) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.keyword-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
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
      DataPageList resultPageList = 
        contactService.searchContact(SessionProviderFactory.createSystemProvider(), event.getRequestContext().getRemoteUser(), filter) ;
      uiForm.setContactList(resultPageList.getAll()) ;
      uiForm.getUIStringInput(UIAddressForm.FIELD_KEYWORD).setValue(null) ;
      uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).setValue(uiForm.selectedAddressId_) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();  
      UIPopupContainer uiContainer = uiAddressForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
    }
  }

  static  public class ShowPageActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiAddressForm.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);           
    }
  }
}
