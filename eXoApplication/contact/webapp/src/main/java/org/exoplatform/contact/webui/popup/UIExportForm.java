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
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.webui.util.Util;
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
      @EventConfig(listeners = UIExportForm.CancelActionListener.class),
      @EventConfig(listeners = UIExportForm.SortActionListener.class)
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
  

  public String getSelectedGroupName() throws Exception {
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ; 
    ContactGroup group = contactService.getGroup(username, selectedGroup);
    if (group != null) return group.getName() ;
    else return selectedGroup ;
  }
  
  public void setAscending(boolean isAsc) {
    isAscending_ = isAsc;
  }

  public boolean isAscending() {
    return isAscending_;
  }

  public void setSortedBy(String s) {
    sortedBy_ = s;
  }

  public String getSortedBy() {
    return sortedBy_;
  }

  public String getViewQuery() {
    return viewQuery_;
  }

  public void setViewQuery(String view) {
    viewQuery_ = view;
  }

  public void setContacts(Contact[] contacts) throws Exception {
    contacts_ = contacts;
  }

  public Contact[] getContacts() throws Exception {
    return contactMap.values().toArray(new Contact[] {});
  }

  public void setSelectedGroup(String s) throws Exception {
    selectedGroup = s;
  }

  public String getSelectedGroup() {
    return selectedGroup;
  }

  public void setViewContactsList(boolean list) {
    viewContactsList = list;
  }

  public boolean getViewContactsList() {
    return viewContactsList;
  }

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
        = new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false);
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

  public String getSelectedTag() {
    return selectedTag_;
  }
  public String getSelectedTagName() throws Exception {
    return ContactUtils.getContactService()
      .getTag(ContactUtils.getCurrentUser(), selectedTag_).getName() ;
  }
  
  public void setSelectedTag(String tagId) {
    selectedTag_ = tagId;
  }
  
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
      OutputStream out = contactService.getContactImportExports(exportFormat).exportContact(username, contactIds) ;
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
      
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');") ;
      
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
  
  static public class SortActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiForm = event.getSource();
      String sortedBy = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser();
      uiForm.setAscending(!uiForm.isAscending_);
      uiForm.setSortedBy(sortedBy);
      if (uiForm.selectedGroup != null) {
        ContactFilter filter = new ContactFilter();
        filter.setAscending(uiForm.isAscending_);
        filter.setOrderBy(uiForm.getSortedBy());
        filter.setViewQuery(uiForm.getViewQuery());
        filter.setCategories(new String[] {uiForm.getSelectedGroup()});

        uiForm.setContacts(contactService.getContactPageListByGroup(username,filter, false).getAll().toArray(new Contact[] {}));
      } else if (uiForm.getSelectedTag() != null) {
        ContactFilter filter = new ContactFilter();
        filter.setAscending(uiForm.isAscending_);
        filter.setOrderBy(uiForm.getSortedBy());
        filter.setViewQuery(uiForm.getViewQuery());
        filter.setTag(new String[] {uiForm.getSelectedTag()});
        uiForm.setContacts(contactService.getContactPageListByTag(username, filter).getAll().toArray(new Contact[] {}));
      }
      uiForm.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

}