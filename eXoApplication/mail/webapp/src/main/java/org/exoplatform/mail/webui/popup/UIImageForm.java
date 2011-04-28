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
package org.exoplatform.mail.webui.popup;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIImageForm.SaveActionListener.class, phase = Phase.DECODE),      
      @EventConfig(listeners = UIImageForm.CancelActionListener.class)
    }
)
public class UIImageForm extends UIForm implements UIPopupComponent{
  public static final String FIELD_UPLOAD = "upload".intern() ;
  public static final String[] imageTypes = { ".gif", ".jpg", ".jpeg", ".tiff", ".bmp", ".png" } ;
   
  public UIImageForm() throws Exception {
    this.setMultiPart(true) ;
    int sizeLimit = MailUtils.getLimitUploadSize();
    if (sizeLimit == MailUtils.DEFAULT_VALUE_UPLOAD_PORTAL) {
      addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD, true));
    } else {
      addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD, sizeLimit, true));
    }
  }
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public String getDonwloadLink(String fileName, ByteArrayInputStream inputStream) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource = new InputStreamDownloadResource(inputStream, "image") ;
    dresource.setDownloadName(fileName) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }
  
  static  public class SaveActionListener extends EventListener<UIImageForm> {
    public void execute(Event<UIImageForm> event) throws Exception {
      UIImageForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD);
      UploadResource uploadResource = input.getUploadResource() ;
      if(uploadResource == null) {
        uiApp.addMessage(new ApplicationMessage("UIImageForm.msg.selectFile-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String mimeType = uploadResource.getMimeType() ;
      String fileName = uploadResource.getFileName() ;
      boolean isImage = false ;
      for(String imageType : imageTypes)
        if (fileName.toLowerCase().endsWith(imageType)) isImage = true ;
      if(Utils.isEmptyField(fileName)) {
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.fileName-error", null, 
            ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }else if((!isImage)){
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.unformat-imagefile", null, 
                                                ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getUploadData()) ;
      UIPopupActionContainer uiPopupActionContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class) ;
      UIAddContactForm uiContactForm =  uiPopupActionContainer.findFirstComponentOfType(UIAddContactForm.class) ;
      uiContactForm.setImage(inputStream) ;
      uiContactForm.setMimeType(mimeType) ;
      uiContactForm.setFileName(fileName) ;
      
      Contact contact = new Contact() ;
      contact.setId(org.exoplatform.contact.service.Utils.contactTempId) ;
      ContactAttachment attachment = new ContactAttachment() ;
      attachment.setInputStream(new ByteArrayInputStream(input.getUploadData())) ;
      attachment.setFileName(fileName) ;
      attachment.setMimeType(mimeType) ;
      contact.setAttachment(attachment) ; 
      // remove the file upload in component upload input.
      UploadService uploadSv = uiForm.getApplicationComponent(UploadService.class);
      uploadSv.removeUploadResource(input.getUploadId());
      ContactService contactSrv = uiForm.getApplicationComponent(ContactService.class);
      String username = MailUtils.getCurrentUser() ;
      List<String> tempContact = new ArrayList<String>() ;
      tempContact.add(org.exoplatform.contact.service.Utils.contactTempId) ;
      try {
        contactSrv.removeContacts(username, tempContact) ;
      } catch (PathNotFoundException e) {}
      contactSrv.saveContact(username, contact, true) ;
      uiContactForm.setTempContact(contactSrv
        .getContact(username, contact.getId())) ;      
      UIPopupAction popupAction = uiPopupActionContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactForm) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UIImageForm> {
    public void execute(Event<UIImageForm> event) throws Exception {
      UIImageForm uiForm = event.getSource() ;
      UIPopupActionContainer uiPopupActionContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class) ;
      UIAddContactForm uiContactForm =  uiPopupActionContainer.findFirstComponentOfType(UIAddContactForm.class) ;
      UIPopupAction popupAction = uiPopupActionContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactForm) ;
     }
  }  
}
