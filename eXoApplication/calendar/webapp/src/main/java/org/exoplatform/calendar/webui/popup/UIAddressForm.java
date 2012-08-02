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
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template =  "app:/templates/calendar/webui/UIPopup/UIAddressForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UIAddressForm.AddActionListener.class), 
                   @EventConfig(listeners = UIAddressForm.ReplaceActionListener.class, phase = Phase.DECODE),
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
  protected String[] actions_ = new String[]{"Add", "Replace", "Cancel"}; 
  
  // CS- 3130
  // public LinkedHashMap<String, Contact> checkedList_ = new LinkedHashMap<String, Contact>() ;
  public List<String> checkedList_ = new ArrayList<String>();
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
    options.add(new SelectItemOption<String>("all", org.exoplatform.calendar.service.Utils.EMPTY_STR)) ;
    for( AddressBook cg : contactService.getGroups(CalendarUtils.getCurrentUser())) {
      options.add(new SelectItemOption<String>(cg.getName(), cg.getId())) ;
    }
    List<SharedAddressBook> addressList = contactService
    .getSharedAddressBooks(CalendarUtils.getCurrentUser()) ;
    for(SharedAddressBook sa : addressList) {
      String name = org.exoplatform.calendar.service.Utils.EMPTY_STR ;
      if(!CalendarUtils.isEmpty(sa.getSharedUserId())) name = sa.getSharedUserId() + "-" ;
      options.add(new SelectItemOption<String>(name + sa.getName(), sa.getId())) ;
    }
    
    List<String> publicAddressBookIds = contactService.getAllsPublicAddressBookIds(null);
    if (!publicAddressBookIds.isEmpty()) {
      for (String publicCg : publicAddressBookIds) {
        options.add(new SelectItemOption<String>(CalendarUtils.getOrganizationService()
                                                              .getGroupHandler()
                                                              .findGroupById(publicCg)
                                                              .getGroupName(), publicCg));
      }
    }
    
    return options;
  }

  public String[] getActions() { return actions_ ; }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {
    actions_ = new String[]{"Add", "Replace", "Cancel"}; 
  } 
  @SuppressWarnings("unchecked")
  public List<ContactData> getContacts() throws Exception {
    for(String id : checkedList_) {
      UIFormCheckBoxInput uiInput = getUIFormCheckBoxInput(id) ;
      if(uiInput != null) uiInput.setChecked(true) ;
    }
    return new ArrayList<ContactData>(uiPageIterator_.getCurrentPageData());
  }
  @SuppressWarnings("unchecked")
  public void setContactList(String groupId) throws Exception {
    ContactFilter filter = new ContactFilter() ;
    if(!CalendarUtils.isEmpty(groupId)) {
      filter.setCategories(new String[]{groupId}) ;
    }  
    setContactList(filter);
  }
  @SuppressWarnings("unchecked")
  public void setContactList(ContactFilter filter) throws Exception {
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    Map<String, String> resultMap = contactSrv.searchEmails(CalendarUtils.getCurrentUser(), filter) ;
    List<ContactData> data = new ArrayList<ContactData>() ;
    for(String ct : resultMap.keySet()) {
      String id  = ct ;
      String value = resultMap.get(id) ; 
      if(resultMap.get(id) != null && resultMap.get(id).trim().length() > 0) {
        if(value.lastIndexOf(Utils.SPLIT) > 0) {
          String fullName = value.substring(0,value.lastIndexOf(Utils.SPLIT)) ;
          String email = value.substring(value.lastIndexOf(Utils.SPLIT) + Utils.SPLIT.length()) ;
          if(!CalendarUtils.isEmpty(email)) data.add(new ContactData(id, fullName, email)) ;
        }
      }
    }
    setContactList(data);
  }
  
  @SuppressWarnings({ "unchecked", "deprecation" })
  public List<ContactData> getContactList() {
    try {
      return (List<ContactData>)uiPageIterator_.getPageList().getAll() ;
    } catch (Exception e) {
      return new ArrayList<ContactData>() ;
    }
  }
  @SuppressWarnings({ "deprecation", "unchecked" })
  public void setContactList(List<ContactData> contactList) throws Exception {
    getUIFormSelectBox(FIELD_GROUP).setOptions(getGroups()) ;
    //org.exoplatform.commons.utils.ObjectPageList objPageList = new org.exoplatform.commons.utils.ObjectPageList(contactList, 10) ;
    LazyPageList<ContactData> pageList = new LazyPageList<ContactData>(new ListAccessImpl<ContactData>(ContactData.class, contactList), 10);
    uiPageIterator_.setPageList(pageList) ;
    for (ContactData contact : contactList) {
      UIFormCheckBoxInput uiCheckbox = getUIFormCheckBoxInput(contact.getId()) ;
      if(uiCheckbox == null) {
        uiCheckbox = new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false) ;
        addUIFormInput(uiCheckbox);
      } 
    }
  }
  @SuppressWarnings("unchecked")
  public List<ContactData> getCheckedContact() throws Exception {
    List<ContactData> contactList = new ArrayList<ContactData>();  
    for (ContactData contact : new ArrayList<ContactData>(uiPageIterator_.getCurrentPageData())) {
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
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required",null)) ;
        return ;
      }
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UITaskForm uiTaskForm = uiContainer.findFirstComponentOfType(UITaskForm.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      UIInvitationForm uiInvitationForm =uiContainer.findFirstComponentOfType(UIInvitationForm.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
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
      String info = null ;
      for(ContactData c : uiForm.getCheckedContact()) {
        if(!uiForm.checkedList_.contains(c.getId())){
          Contact con = new Contact() ;
          con.setId(c.getId()) ;
          con.setEmailAddress(c.getEmail()) ;
          con.setFullName(c.getFullName()) ;
          if (!uiForm.checkedList_.contains(c.getId())) uiForm.checkedList_.add(c.getId()) ;
        }
        if(uiInvitationForm != null) {
          info = c.getFullName() + org.exoplatform.calendar.service.Utils.SPACE + 
            CalendarUtils.OPEN_PARENTHESIS + c.getEmail() + CalendarUtils.CLOSE_PARENTHESIS;
        } else {
          info = c.getEmail();
        }
        if(!CalendarUtils.isEmpty(info) && !listMail.contains(info)) {
          if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
          if(info != null) sb.append(info.replace(";", ",")) ;
        }
      }
      if(uiTaskForm != null) {
        uiTaskForm.setSelectedTab(UITaskForm.TAB_TASKREMINDER) ;
        uiTaskForm.setEmailAddress(sb.toString()) ;
        chilPopup.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskForm) ;
      }else if(uiEventForm != null) {
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTREMINDER) ;
        uiEventForm.setEmailAddress(sb.toString()) ;
        chilPopup.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
      } else if(uiInvitationForm != null) {
        String value =  uiInvitationForm.appendValue(uiInvitationForm.getParticipantValue(),  sb.toString()) ;
        uiInvitationForm.getUIFormTextAreaInput(UIInvitationForm.FIELD_PARTICIPANT).setValue(value) ;
        chilPopup.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiInvitationForm) ;
     }
    }
  }

  static  public class ReplaceActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception { 
      UIAddressForm uiForm = event.getSource();
      if(uiForm.getCheckedContact().size() <= 0) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required",null)) ;
        return ;
      }
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UITaskForm uiTaskForm = uiContainer.findFirstComponentOfType(UITaskForm.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      UIInvitationForm uiInvitationForm = uiContainer.findFirstComponentOfType(UIInvitationForm.class) ;
      StringBuilder sb = new StringBuilder() ;
      for(ContactData c : uiForm.getCheckedContact()) {
        if(!CalendarUtils.isEmpty(c.getEmail())) {
          if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
          for (String email : c.getEmail().replace(";", ",").split(","))
            if (sb.indexOf(email.trim()) == -1) sb.append(email.trim()) ;
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
      if(uiInvitationForm != null) {
         String value =  uiInvitationForm.appendValue(uiInvitationForm.getParticipantValue(),  sb.toString()) ;
         uiInvitationForm.getUIFormTextAreaInput(UIInvitationForm.FIELD_PARTICIPANT).setValue(value) ;
      }

      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
    }  
  } 
  static  public class SearchActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource();  
      String text = uiForm.getUIStringInput(UIAddressForm.FIELD_KEYWORD).getValue() ;
      String category = uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).getValue() ;
      if(category.equals(NewUserListener.DEFAULTGROUP)) category = category + CalendarUtils.getCurrentUser() ;
      uiForm.selectedAddressId_ = category ;
      try {
        ContactFilter filter = new ContactFilter() ;
        if(!CalendarUtils.isEmpty(uiForm.selectedAddressId_)) {
          filter.setCategories(new String[]{uiForm.selectedAddressId_}) ;
        } 
        if(!CalendarUtils.isEmpty(text)) filter.setText(CalendarUtils.encodeJCRText(text)) ;
        uiForm.setContactList(filter) ;
        uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).setValue(category) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
      } catch (Exception e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddressForm.msg.keyword-error", null)) ;
        }
    }
  }
  static  public class ChangeGroupActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource();  
      String category = uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).getValue() ;
      if(category.equals(NewUserListener.DEFAULTGROUP)) category = category + CalendarUtils.getCurrentUser() ;
      uiForm.selectedAddressId_ = category ;
      uiForm.setContactList(category) ;
      uiForm.getUIStringInput(UIAddressForm.FIELD_KEYWORD).setValue(null) ;
      uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).setValue(uiForm.selectedAddressId_) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm.getAncestorOfType(UIPopupAction.class));           
    }
  }
  public class ContactData {
    private String id ;
    private String fullName ;
    private String email ;

    public ContactData(String id,String fullName,String email){
      this.id = id ;
      this.fullName = fullName;
      this.email = email ;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    public String getFullName() {
      return fullName;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getEmail() {
      return email;
    }

  }
}
