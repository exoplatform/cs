/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;

import net.wimpi.pim.util.versitio.versitException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Author : Huu-Dung Kieu huu-dung.kieu@bull.be 16 oct. 07 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIImportForm.SaveActionListener.class),      
      @EventConfig(listeners = UIImportForm.CancelActionListener.class)
    }
)
public class UIImportForm extends UIForm implements UIPopupComponent{
  final static public String FIELD_UPLOAD = "upload".intern() ;
  final static public String TYPE = "type".intern() ;
  public final static String FIELD_CATEGORY_BOX = "category" ;
  public static String[] Types = null ;
  
  public UIImportForm() throws Exception {
    this.setMultiPart(true) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    ContactService contactService = ContactUtils.getContactService();
    Types = contactService.getImportExportType() ;
    for(String type : Types) {
      options.add(new SelectItemOption<String>(type, type)) ;
    }
    List<ContactGroup> groupList = contactService.getGroups(ContactUtils.getCurrentUser());
    List<SelectItemOption<String>> groupOptions = new ArrayList<SelectItemOption<String>>() ;
    for (ContactGroup group : groupList) {
      groupOptions.add(new SelectItemOption<String>(group.getName(), group.getId())) ;
    }
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY_BOX, FIELD_CATEGORY_BOX, groupOptions)) ;    
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options)) ;
    addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD)) ;
  }
  
  public void setValues(String group) {
    getUIFormSelectBox(FIELD_CATEGORY_BOX).setValue(group) ;
  }  

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  static  public class SaveActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      String category = uiForm.getUIFormSelectBox(FIELD_CATEGORY_BOX).getValue() ;
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      UIFormUploadInput input = uiForm.getUIInput(FIELD_UPLOAD) ;
      UploadResource uploadResource = input.getUploadResource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if (uploadResource == null) {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.uploadResource-empty", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }       
      boolean canImport = false ;
      String[] array = uploadResource.getMimeType().split("/") ;
      String extend = array[array.length - 1] ;
      for(String type : Types) {
        if (extend.equalsIgnoreCase(type)) canImport = true ;
      }
      if(!canImport) {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.fileName-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }  
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      String importFormat = uiForm.getUIFormSelectBox(UIImportForm.TYPE).getValue() ;
      try {
        ContactUtils.getContactService().getContactImportExports(importFormat)
          .importContact(ContactUtils.getCurrentUser(), input.getUploadDataAsStream(), category) ;
        UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
        uploadService.removeUpload(input.getUploadId()) ;
        uiContacts.updateList() ;
      } catch (Exception ex) {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.invalid-format", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      uiContactPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet.getChild(UIWorkingContainer.class)) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;
     }
  }  
}
