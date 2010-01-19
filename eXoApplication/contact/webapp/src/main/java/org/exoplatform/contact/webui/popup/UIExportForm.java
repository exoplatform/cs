/*
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
 */
package org.exoplatform.contact.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactContainer;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
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
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Author : Huu-Dung Kieu huu-dung.kieu@bull.be 16 oct. 07 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UIExportForm.gtmpl",
    events = {
      @EventConfig(listeners = UIExportForm.SaveActionListener.class),  
      @EventConfig(listeners = UIExportForm.ShowPageActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIExportForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIExportForm extends UIForm implements UIPopupComponent{
  final static private String NAME = "name".intern() ;
  final static private String TYPE = "type".intern() ;
  private String selectedTag_     = null;
  private String selectedGroup = null;
  private UIPageIterator uiPageIterator_ ;
  private Map<String, String> checkedContacts = new LinkedHashMap<String, String>() ;
  private Map<String, Contact> contacts = null ;

  public UIExportForm() throws Exception {
    setId("UIExportForm") ;
    uiPageIterator_ = new UIPageIterator() ;
    uiPageIterator_.setId("UIContactPage") ;
  }  

  public String getLabel(String id) throws Exception {
    try {
      return  super.getLabel(id) ;
    } catch (MissingResourceException mre) {
      return id ;
    }
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void setContacts(Map<String, Contact> contacts) { this.contacts = contacts; }
  public void setContactList(List<ContactData> contactList) throws Exception {
    getChildren().clear() ;
    ObjectPageList objPageList = new ObjectPageList(contactList, 10) ;
    uiPageIterator_.setPageList(objPageList) ;
    for (ContactData contact : contactList) {
      UIFormCheckBoxInput uiCheckbox = getUIFormCheckBoxInput(contact.getId()) ;
      if(uiCheckbox == null) {
        uiCheckbox = new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false) ;
        addUIFormInput(uiCheckbox);
      } 
    }
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(String type : ContactUtils.getContactService().getImportExportType()) {
      options.add(new SelectItemOption<String>(type, type)) ;
    }
    addUIFormInput(new UIFormStringInput(NAME, NAME, null).addValidator(MandatoryValidator.class)) ;
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options)) ;
  }
  @SuppressWarnings("unchecked")
  public List<ContactData> getContacts() throws Exception { 
    return new ArrayList<ContactData>(uiPageIterator_.getCurrentPageData());
  }
  public void setSelectedGroup(String address) throws Exception { selectedGroup = address ; }
  public String getSelectedGroup() {
    try {
      return selectedGroup.split(Utils.SPLIT)[2];       
    } catch (NullPointerException e) {
      return null ;
    }
    
  }
  
  public List<String> getCheckedCurrentPage() throws Exception {
    List<String> checkedContacts = new ArrayList<String>();
    for (ContactData contact : getContacts()) {
      UIFormCheckBoxInput uiCheckBox = getChildById(contact.getId());
      if (uiCheckBox != null && uiCheckBox.isChecked()) {
        checkedContacts.add(contact.getId());
      }
    }
    return checkedContacts;
  }

  public String getSelectedTag() { return selectedTag_; }
  public void setSelectedTag(String tagName) { selectedTag_ = tagName; }
  
  public UIPageIterator  getUIPageIterator() {  return uiPageIterator_ ; }
  public long getAvailablePage(){ return uiPageIterator_.getAvailablePage() ;}
  public long getCurrentPage() { return uiPageIterator_.getCurrentPage();}
  protected void updateCurrentPage(int page) throws Exception{
    uiPageIterator_.setCurrentPage(page) ;
    for (ContactData contactData : getContacts())
      if (checkedContacts.containsKey(contactData.getId())) {
        UIFormCheckBoxInput uiCheckBox = getChildById(contactData.getId());
        uiCheckBox.setChecked(true) ;
      }
  }
  
  static  public class SaveActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiForm = event.getSource() ;
      boolean isExportAll = event.getRequestContext().getRequestParameter(OBJECTID).equals("all");
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class);
      Map<String, String> contactIds = uiForm.checkedContacts ;
      for (ContactData contact : uiForm.getContacts()) {
        UIFormCheckBoxInput uiCheckBox = uiForm.getChildById(contact.getId());
        if (uiCheckBox.isChecked()) {
          uiForm.checkedContacts.put(contact.getId(), contact.getId());
        } else {
          uiForm.checkedContacts.remove(contact.getId()) ;
        }
      }      
      for (String contactId : uiForm.getCheckedCurrentPage()) contactIds.put(contactId, contactId) ;
      
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if (!isExportAll && contactIds.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIExportForm.msg.check-contact-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String username = ContactUtils.getCurrentUser() ;
      ContactService contactService = ContactUtils.getContactService() ;
      
      String exportFormat = uiForm.getUIFormSelectBox(UIExportForm.TYPE).getValue() ;
      String fileName = uiForm.getUIStringInput(UIExportForm.NAME).getValue() ;
    /*  if (ContactUtils.isEmpty(fileName)) {
        uiApp.addMessage(new ApplicationMessage("UIExportForm.msg.filename-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }*/
      List<Contact> contacts = new ArrayList<Contact>() ;
      if (!ContactUtils.isEmpty(uiForm.getSelectedGroup())) {
        String[] address = uiForm.selectedGroup.split(Utils.SPLIT) ;
        if (isExportAll) {
          ContactPageList pageList = null ;          
          if (address[0].equals(DataStorage.PERSONAL)) {
            pageList = contactService.getPersonalContactsByAddressBook(
                username, address[1]) ;  
          } else if (address[0].equals(DataStorage.SHARED)) {
            SharedAddressBook sharedAddress = uiContactPortlet.findFirstComponentOfType(
                UIAddressBooks.class).getSharedGroups().get(address[1]) ;
            pageList = contactService.getSharedContactsByAddressBook(
                username, sharedAddress) ;
          } else {
            pageList = contactService.getPublicContactsByAddressBook(address[1]) ;
          }
          if (pageList == null) {
            uiApp.addMessage(new ApplicationMessage("UIExportForm.msg.deletedPer", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(
              uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class)) ;
            return ;
          }          
          if (pageList.getAvailable() > Utils.limitExport) {
            uiApp.addMessage(new ApplicationMessage("UIExportForm.msg.manyContacts", new Object[]{Utils.limitExport + ""}, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          contacts.addAll(pageList.getAll()) ;          
        } else {
          if (contactIds.size() > Utils.limitExport) {
            uiApp.addMessage(new ApplicationMessage("UIExportForm.msg.manyContacts", new Object[]{Utils.limitExport + ""}, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }          
          if (address[0].equals(DataStorage.PERSONAL)) {
            for(String contactId : contactIds.keySet()) {
              contacts.add(contactService.getContact(username, contactId)) ;              
            }            
          } else if (address[0].equals(DataStorage.SHARED)) {
            for(String contactId : contactIds.keySet()) {
              //cs-2326
              Contact contact = contactService.getSharedContactAddressBook(username, contactId) ;
              if (contact != null)
                contacts.add(contactService.getSharedContactAddressBook(username, contactId)) ;
            }
          } else {
            for(String contactId : contactIds.keySet()) {
              contacts.add(contactService.getPublicContact(contactId)) ;
            }
          }          
        }
      } else {
        if (isExportAll) {
          contacts.addAll(uiForm.contacts.values()) ;
        } else {
          for(String contactId : contactIds.keySet()) {
            contacts.add(uiForm.contacts.get(contactId)) ;
          }          
        }
      }      
      if (contacts.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIExportForm.msg.deletedPer", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(
          uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(
          uiContactPortlet.findFirstComponentOfType(UIContactContainer.class)) ;
        return ;
      }
      
      OutputStream out = contactService.getContactImportExports(exportFormat).exportContact(username, contacts) ;
      String contentType = null;
      String extension = null;
      if(exportFormat.equals("x-vcard")){
        contentType = "text/x-vcard";
        extension = ".vcf";
      }
      ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
      DownloadResource dresource = new InputStreamDownloadResource(is, contentType) ;
      DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class) ;
      if(fileName != null && fileName.length() > 0) {
        if(fileName.length() > 4 && fileName.endsWith(extension) )
          dresource.setDownloadName(fileName);
        else 
          dresource.setDownloadName(fileName + extension);
      }else {
        dresource.setDownloadName("eXoExported.vcf");
      }
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
      event.getRequestContext().getJavascriptManager()
        .addJavascript("ajaxRedirect('" + downloadLink + "');") ;
      uiContactPortlet.cancelAction() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;
    }
  }

  static  public class ShowPageActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiExportForm = event.getSource() ;
      for (ContactData contact : uiExportForm.getContacts()) {
        String contactId = contact.getId() ;
        UIFormCheckBoxInput uiCheckBox = uiExportForm.getChildById(contactId);
        if (uiCheckBox.isChecked()) {
          uiExportForm.checkedContacts.put(contactId, contactId);
        } else {
          uiExportForm.checkedContacts.remove(contactId) ;
        }
      }
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiExportForm.updateCurrentPage(page) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiExportForm.getAncestorOfType(UIPopupAction.class));           
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