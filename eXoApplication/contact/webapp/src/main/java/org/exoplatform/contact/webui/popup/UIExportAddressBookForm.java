/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.MissingResourceException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
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
    template = "app:/templates/contact/webui/popup/UIExportAddressBookForm.gtmpl",
    events = {
      @EventConfig(listeners = UIExportAddressBookForm.SaveActionListener.class),      
      @EventConfig(listeners = UIExportAddressBookForm.CancelActionListener.class)
    }
)
public class UIExportAddressBookForm extends UIForm implements UIPopupComponent{
  final static private String NAME = "name".intern() ;
  final static private String TYPE = "type".intern() ;
  
  private ContactGroup[]                 contactGroups_   = null;
  private List<String>                   sharedContactGroup_ = null ;
  
  public UIExportAddressBookForm() throws Exception {
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
  
  public List<String> getSharedContactGroup() { return sharedContactGroup_ ; }
  public void setSharedContactGroup(List<String> groups) { sharedContactGroup_ = groups ; }
  
  public ContactGroup[] getContactGroups() {
    return contactGroups_;
  }

  public void setContactGroups(ContactGroup[] contactGroups_) {
    this.contactGroups_ = contactGroups_;
  }

  public void updateList() throws Exception { 
    getChildren().clear() ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    ContactService contactService = ContactUtils.getContactService();
    
    for(String type : contactService.getImportExportType()) {
      options.add(new SelectItemOption<String>(type, type)) ;
    }
    
    addUIFormInput(new UIFormStringInput(NAME, NAME, null)) ;
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options)) ;
    for (ContactGroup group : contactGroups_) {
      UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(group.getId(),
          group.getName(), false);
      addUIFormInput(checkbox);
    }
    for (String group : sharedContactGroup_) {
      UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(group,
          group, false);
      addUIFormInput(checkbox);
    }
  }

  public List<String> getCheckedGroups() throws Exception {
    List<String> checked = new ArrayList<String>();
    for (ContactGroup group : getContactGroups()) {
      UIFormCheckBoxInput uiCheckBox = getChildById(group.getId());
      if (uiCheckBox != null && uiCheckBox.isChecked()) {
        checked.add(group.getId());
      }
    }
    return checked;
  }

  static  public class SaveActionListener extends EventListener<UIExportAddressBookForm> {
    public void execute(Event<UIExportAddressBookForm> event) throws Exception {
      UIExportAddressBookForm uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class);
      
      String username = ContactUtils.getCurrentUser() ;
      ContactService contactService = ContactUtils.getContactService() ;
      
      List<String> groupIds = uiForm.getCheckedGroups() ;
      int size = groupIds.size();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if (size == 0) {
        uiApp.addMessage(new ApplicationMessage("UIExportAddressBookForm.checkGroup-required", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;          
      } 
      LinkedHashMap<String, String> contactMap = new LinkedHashMap<String, String> () ;
      for (int i=0; i<size; i++) {
        List<Contact> contacts = contactService.getContactPageListByGroup(username, groupIds.get(i)).getAll();
        int count = contacts.size();
        for (int j=0; j<count; j++) {
          String contactId = contacts.get(j).getId();
          contactMap.put(contactId, contactId) ;
        }
          
      }
      
      List<String> sharedGroups = uiForm.sharedContactGroup_ ;
      for (String group : sharedGroups) {
        List<Contact> sharedContacts = contactService.getSharedContactsByGroup(group).getAll() ;
        for (Contact contact : sharedContacts) {
          String contactId = contact.getId() ;
          contactMap.put(contactId, contactId) ;
        }
      }
     // String[] contactIds = contactMap.values().toArray(new String[] {}) ;
      List<String> listContactIds = new ArrayList<String>() ;
      listContactIds.addAll(contactMap.values()) ;
      
      String exportFormat = uiForm.getUIFormSelectBox(UIExportAddressBookForm.TYPE).getValue() ;
      String fileName = uiForm.getUIStringInput(UIExportAddressBookForm.NAME).getValue() ;
      if (ContactUtils.isEmpty(fileName)) {
        uiApp.addMessage(new ApplicationMessage("UIExportAddressBookForm.fileName-required", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      OutputStream out = contactService.getContactImportExports(exportFormat).exportContact(username, listContactIds) ;

      String contentType = null;
      String extension = null;
      if (exportFormat.equals("x-vcard")) {
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
      
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');") ;
      
      uiContactPortlet.cancelAction() ;
      
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIExportAddressBookForm> {
    public void execute(Event<UIExportAddressBookForm> event) throws Exception {
      UIExportAddressBookForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;
    }
  }  
  
}