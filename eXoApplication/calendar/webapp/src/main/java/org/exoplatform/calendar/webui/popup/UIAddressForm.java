/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
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
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

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
      @EventConfig(listeners = UIAddressForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAddressForm.SearchActionListener.class), 
      @EventConfig(listeners = UIAddressForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAddressForm extends UIForm implements UIPopupComponent { 
  final public static String FIELD_KEYWORD = "keyWord".intern() ;
  final public static String FIELD_GROUP = "group".intern() ;
  
  private List<Contact> alreadyCheckedContact = new ArrayList<Contact>();

  private String recipientsType = "";
  
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
    if (groupId == null || groupId == "") {
      contacts = contactSrv.getAllContact(SessionsUtils.getSessionProvider(), username);
    } else {
      contacts = contactSrv.getContactPageListByGroup(SessionsUtils.getSessionProvider(), username, groupId).getAll();
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
  
  static  public class SaveActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception { 
      System.out.println("======== >>>UIAddressForm.SaveActionListener");
      UIAddressForm uiForm = event.getSource();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UITaskForm uiTaskForm = uiContainer.findFirstComponentOfType(UITaskForm.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      StringBuilder sb = new StringBuilder() ;
      for(Contact c : uiForm.getCheckedContact()) {
        sb.append(c.getEmailAddress()).append("\n") ;
      }
      if(uiTaskForm != null) {
        uiTaskForm.setRenderedChild(UITaskForm.TAB_TASKREMINDER) ;
        uiTaskForm.setEmailAddress(sb.toString()) ;
      } 
      if(uiEventForm != null) {
        uiEventForm.setRenderedChild(UIEventForm.TAB_EVENTREMINDER) ;
        uiEventForm.setEmailAddress(sb.toString()) ;
      } 
      UIPopupAction parentPopup = uiContainer.getAncestorOfType(UIPopupAction.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(parentPopup) ;
    }  
  } 
  static  public class SearchActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource();  
      ContactService contactService = uiForm.getApplicationComponent(ContactService.class) ;
      String text = uiForm.getUIStringInput(UIAddressForm.FIELD_KEYWORD).getValue() ;
      String category = uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).getValue() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(CalendarUtils.isEmpty(text)) {
        uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.keyword-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      ContactFilter filter = new ContactFilter() ;
      if(!CalendarUtils.isEmpty(category)) {
        filter.setCategories(new String[]{category}) ;
      }
      filter.setText(text) ;
      DataPageList resultPageList = 
        contactService.searchContact(SessionsUtils.getSystemProvider(), event.getRequestContext().getRemoteUser(), filter) ;
      uiForm.setContactList(resultPageList.getAll()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();  
      UIPopupContainer uiContainer = uiAddressForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction parentPopup = uiContainer.getAncestorOfType(UIPopupAction.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(parentPopup) ;
    }
  }
}
