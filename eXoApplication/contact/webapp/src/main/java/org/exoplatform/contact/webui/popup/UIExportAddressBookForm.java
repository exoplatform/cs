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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
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
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Author : Huu-Dung Kieu huu-dung.kieu@bull.be 16 oct. 07 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UIExportAddressBookForm.gtmpl",
    events = {
      @EventConfig(listeners = UIExportAddressBookForm.SaveActionListener.class),      
      @EventConfig(listeners = UIExportAddressBookForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIExportAddressBookForm extends UIForm implements UIPopupComponent{
  final static private String NAME = "fileName".intern() ;
  final static private String TYPE = "type".intern() ;
  
  private Map<String, String> privateGroupMap_ = new HashMap<String, String>() ;
  private Map<String, String> publicGroupMap_ = new HashMap <String, String>() ;
  private Map<String, SharedAddressBook> sharedGroupMap_ = new HashMap <String, SharedAddressBook>() ;

  public UIExportAddressBookForm() throws Exception { }
  public String getLabel(String id) throws Exception {
    try {
      return  super.getLabel(id) ;
    } catch (MissingResourceException mre) {
      return id ;
    }
  } 
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public Map<String, String> getContactGroups() throws Exception { return privateGroupMap_; }
  public void setContactGroups(Map<String, String> contactGroups) { privateGroupMap_ = contactGroups ; }

  public Map<String, SharedAddressBook> getSharedContactGroups() { return sharedGroupMap_; }
  public void setSharedContactGroups(Map<String, SharedAddressBook> contactGroups) { sharedGroupMap_ = contactGroups ; }
  
  public Map<String, String> getPublicContactGroup() { return publicGroupMap_ ; }//getSharedContactGroup
  public void setPublicContactGroup(Map<String, String> groups) { publicGroupMap_ = groups ; }

  public void updateList() throws Exception { 
    getChildren().clear() ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(String type : ContactUtils.getContactService().getImportExportType()) {
      options.add(new SelectItemOption<String>(type, type)) ;
    }  
    addUIFormInput(new UIFormStringInput(NAME, NAME, null).addValidator(MandatoryValidator.class)) ;
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options)) ;
    for (String group : privateGroupMap_.keySet()) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(group,privateGroupMap_.get(group), false));
    }
    for (String group : publicGroupMap_.keySet()) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(group, group, false));
    }
    for (String group : sharedGroupMap_.keySet()) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(group, sharedGroupMap_.get(group).getName(), false));
    }
  }

  public List<String> getCheckedGroups() throws Exception {
    List<String> checked = new ArrayList<String>();
    for (String group : privateGroupMap_.keySet()) {
      UIFormCheckBoxInput uiCheckBox = getChildById(group);
      if (uiCheckBox != null && uiCheckBox.isChecked()) {
        checked.add(group);
      }
    }
    for (String group : sharedGroupMap_.keySet()) {
      UIFormCheckBoxInput uiCheckBox = getChildById(group);
      if (uiCheckBox != null && uiCheckBox.isChecked()) {
        checked.add(group + JCRDataStorage.HYPHEN + sharedGroupMap_.get(group).getSharedUserId());
      }
    }
    for (String group : publicGroupMap_.keySet()) {
      UIFormCheckBoxInput uiCheckBox = getChildById(group);
      if (uiCheckBox != null && uiCheckBox.isChecked()) {
        checked.add(group);
      }
    }
    return checked;
  }

  static  public class SaveActionListener extends EventListener<UIExportAddressBookForm> {
    public void execute(Event<UIExportAddressBookForm> event) throws Exception {
      UIExportAddressBookForm uiForm = event.getSource() ;
      List<String> groupIds = uiForm.getCheckedGroups() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if (groupIds.size() < 1) {
        uiApp.addMessage(new ApplicationMessage("UIExportAddressBookForm.checkGroup-required", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;          
      } 
      String exportFormat = uiForm.getUIFormSelectBox(UIExportAddressBookForm.TYPE).getValue() ;
      String fileName = uiForm.getUIStringInput(UIExportAddressBookForm.NAME).getValue() ;
      /*if (ContactUtils.isEmpty(fileName)) {
        uiApp.addMessage(new ApplicationMessage("UIExportAddressBookForm.fileName-required", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }*/
      OutputStream out = null ;
      try {
        out = ContactUtils.getContactService().getContactImportExports(exportFormat).exportContact(
            SessionProviderFactory.createSystemProvider(), ContactUtils.getCurrentUser(), groupIds.toArray(new String[]{})) ;        
      } catch (ArrayIndexOutOfBoundsException e) {
        uiApp.addMessage(new ApplicationMessage("UIExportAddressBookForm.many-Contacts", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      if(out == null) {
         uiApp.addMessage(new ApplicationMessage("UIExportAddressBookForm.msg.there-is-not-contacts-exists", null,
           ApplicationMessage.WARNING)) ;
         event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
         return ;   
      }
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
      uiForm.getAncestorOfType(UIContactPortlet.class).cancelAction() ;    
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIExportAddressBookForm> {
    public void execute(Event<UIExportAddressBookForm> event) throws Exception {
      UIExportAddressBookForm uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIContactPortlet.class).cancelAction() ;
    }
  }  
  
}