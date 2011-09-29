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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.ContactImportExport;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Author : Huu-Dung Kieu huu-dung.kieu@bull.be 16 oct. 07 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIImportForm.SaveActionListener.class),      
      @EventConfig(listeners = UIImportForm.CancelActionListener.class),
      @EventConfig(listeners = UIImportForm.AddCategoryActionListener.class,  phase=Phase.DECODE)
    }
)
public class UIImportForm extends UIForm {
  final static public String FIELD_UPLOAD = "upload".intern() ;
  final static public String FIELD_TYPE = "type".intern() ;
  public static final String INPUT_CATEGORY = "categoryInput";
  public static final String FIELD_CATEGORY = "category";
  
  //public static String[] Types = null ;
  private String[] Types = null ;
  private Map<String, String> groups_ = new LinkedHashMap<String, String>() ;

  public UIImportForm() { this.setMultiPart(true) ; }
  public void addConponent() throws Exception {
    UIFormInputWithActions input = new UIFormInputWithActions(INPUT_CATEGORY) ;
    input.addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, getCategoryList())) ; 
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData addAction = new ActionData() ;
    addAction.setActionType(ActionData.TYPE_ICON) ;
    addAction.setActionListener("AddCategory") ;
    addAction.setActionName("AddCategory") ;
    actions.add(addAction) ;
    input.setActionField(FIELD_CATEGORY, actions) ;
    addUIFormInput(input) ;

    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    ContactService contactService = ContactUtils.getContactService();
    Types = contactService.getImportExportType() ;
    for(String type : Types) {
      options.add(new SelectItemOption<String>(type, type)) ;
    }
    addUIFormInput(new UIFormSelectBox(FIELD_TYPE, FIELD_TYPE, options)) ;
    
    UIFormUploadInput formUploadInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD, true) ;
    addUIFormInput(formUploadInput) ;    
  }
  
  public String getLabel(String id) throws Exception {
    try {
      return  super.getLabel(id) ;
    } catch (MissingResourceException mre) {
      return null ;
    }
  }
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }

  public List<SelectItemOption<String>> getCategoryList() throws Exception {
    List<SelectItemOption<String>> categories = new ArrayList<SelectItemOption<String>>() ;
    for(String group : groups_.keySet())
      categories.add(new SelectItemOption<String>(ContactUtils.encodeHTML(groups_.get(group)),group)) ;
    return categories ;
  }
  public void setGroup(Map<String, String> groups) { groups_ = groups ; }
  
  public void setCategoryList(List<SelectItemOption<String>> options ) {
    UIFormInputWithActions iput = getChildById(INPUT_CATEGORY) ;
     iput.getUIFormSelectBox(FIELD_CATEGORY).setOptions(options) ;
//   cs- 1628
     groups_.clear() ;
     for (SelectItemOption<String> option : options) {
       groups_.put(option.getValue(), option.getLabel()) ;
     }
  }

  public void setValues(String group) {
    getUIFormSelectBox(FIELD_CATEGORY).setValue(group) ;
  }  
  static  public class AddCategoryActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UICategoryForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }  
  
  static  public class SaveActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      String category = uiForm.getUIFormSelectBox(FIELD_CATEGORY).getValue() ;
      
      if (ContactUtils.isEmpty(category)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIImportForm.msg.addGroup-required",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
        
        return ;        
      }
      UIFormUploadInput uiformInput = uiForm.getUIInput(FIELD_UPLOAD) ;  
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      UploadResource uploadResource = uploadService.getUploadResource(uiformInput.getUploadId()) ;

      String uploadId = uiformInput.getUploadId() ;
      if (uploadResource == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIImportForm.msg.uploadResource-empty", null, 
            ApplicationMessage.WARNING)) ;
        
        return ;
      }
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      String importFormat = uiForm.getUIFormSelectBox(UIImportForm.FIELD_TYPE).getValue() ;

      ContactImportExport service = ContactUtils.getContactService().getContactImportExports(importFormat) ;
      try {
        UIAddressBooks uiAddressBooks = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
        if (uiAddressBooks.getSharedGroups().containsKey(category)) {
          if (!uiAddressBooks.havePermission(category)) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddressBooks.msg.removedPer",
                                                                                           null,
                                                                                           ApplicationMessage.WARNING));
            return ;            
          }
          service.importContact(ContactUtils.getCurrentUser(), uiformInput.getUploadDataAsStream(), category + DataStorage.HYPHEN) ;            
        } else if (uiAddressBooks.getPrivateGroupMap().containsKey(category)){
          service.importContact(ContactUtils.getCurrentUser(), uiformInput.getUploadDataAsStream(), category) ;
        } else {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIImportForm.msg.address-deleted",
                                                                                         null,
                                                                                         ApplicationMessage.WARNING));
          return ;
        }
        UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
        uploadService.removeUpload(uploadId) ;
        uiContacts.updateList() ; 
      } catch (IndexOutOfBoundsException e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIImportForm.msg.too-many-contact",
                                                                                       new Object[] { Utils.limitExport + "" },
                                                                                       ApplicationMessage.WARNING));
          return ;
      } catch (Exception ex) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIImportForm.msg.invalid-format",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
        return ;        
      }
      uiContactPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet.getChild(UIWorkingContainer.class)) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      UIFormUploadInput uiformInput = uiForm.getUIInput(FIELD_UPLOAD) ;  
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      uploadService.removeUpload(uiformInput.getUploadId()) ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;
     }
  }  
}
