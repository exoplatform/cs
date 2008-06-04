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
import java.util.MissingResourceException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Author : Huu-Dung Kieu huu-dung.kieu@bull.be 16 oct. 07 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UIExportForm.gtmpl",
    events = {
      @EventConfig(listeners = UIExportForm.SaveActionListener.class),      
      @EventConfig(listeners = UIExportForm.CancelActionListener.class)
    }
)
public class UIExportForm extends UIForm implements UIPopupComponent{
  final static private String NAME = "name".intern() ;
  final static private String TYPE = "type".intern() ;
  public static String fullName = "fullName".intern() ;
  
  public boolean                         viewContactsList = true;
  private String                         selectedTag_     = null;
  private LinkedHashMap<String, Contact> contactMap       = new LinkedHashMap<String, Contact>();
  private String                         selectedGroup    = null;
  private String                         sortedBy_        = null;
  private boolean                        isAscending_     = true;
  private String                         viewQuery_       = null;
  private Contact[]                      contacts_        = null;
  
  
  public UIExportForm() throws Exception {
    setId("UIExportForm") ;
    sortedBy_ = fullName ;
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

  public void setAscending(boolean isAsc) { isAscending_ = isAsc;}
  public boolean isAscending() { return isAscending_; }

  public void setSortedBy(String s) { sortedBy_ = s;}
  public String getSortedBy() { return sortedBy_; }

  public String getViewQuery() { return viewQuery_; }
  public void setViewQuery(String view) { viewQuery_ = view; }

  public void setContacts(Contact[] contacts) throws Exception { contacts_ = contacts; }
  public Contact[] getContacts() throws Exception {
    return contactMap.values().toArray(new Contact[] {});
  }

  public void setSelectedGroup(String s) throws Exception { selectedGroup = s; }
  public String getSelectedGroup() { return selectedGroup; }

  public void setViewContactsList(boolean list) { viewContactsList = list; }
  public boolean getViewContactsList() { return viewContactsList; }

  public void updateList() throws Exception { 
    getChildren().clear() ;
    contactMap.clear();
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    ContactService contactService = ContactUtils.getContactService();
    for(String type : contactService.getImportExportType()) {
      options.add(new SelectItemOption<String>(type, type)) ;
    }
    addUIFormInput(new UIFormStringInput(NAME, NAME, null)) ;
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options)) ;    
    for (Contact contact : contacts_) {
      UIFormCheckBoxInput<Boolean> checkbox 
        = new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), true);
      addUIFormInput(checkbox);
      contactMap.put(contact.getId(), contact);
    }
  }

  public List<String> getCheckedContacts() throws Exception {
    List<String> checkedContacts = new ArrayList<String>();
    for (Contact contact : getContacts()) {
      UIFormCheckBoxInput uiCheckBox = getChildById(contact.getId());
      if (uiCheckBox != null && uiCheckBox.isChecked()) {
        checkedContacts.add(contact.getId());
      }
    }
    return checkedContacts;
  }

  public String getSelectedTag() { return selectedTag_; }
  public void setSelectedTag(String tagName) { selectedTag_ = tagName; }
  
  static  public class SaveActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class);
      List<String> contactIds = uiForm.getCheckedContacts() ;
      
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if (contactIds.size() == 0) {  
        uiApp.addMessage(new ApplicationMessage("UIExportForm.msg.check-contact-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      String username = ContactUtils.getCurrentUser() ;
      ContactService contactService = ContactUtils.getContactService() ;
      
      String exportFormat = uiForm.getUIFormSelectBox(UIExportForm.TYPE).getValue() ;
      String fileName = uiForm.getUIStringInput(UIExportForm.NAME).getValue() ;
      if (ContactUtils.isEmpty(fileName)) {  
        uiApp.addMessage(new ApplicationMessage("UIExportForm.msg.filename-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      List<Contact> contacts = new ArrayList<Contact>() ;
      for(String contactId : contactIds) {
      	contacts.add(uiForm.contactMap.get(contactId)) ;
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

}