/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.util.IdGenerator;
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
  final static public String FIELD_UPLOAD = "upload".intern() ;
  public UIImageForm() throws Exception {
    this.setMultiPart(true) ;
    addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD)) ;
  }
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  static  public class SaveActionListene extends EventListener<UIImageForm> {
    public void execute(Event<UIImageForm> event) throws Exception {
      UIImageForm uiImageForm = event.getSource() ;
      UIFormUploadInput input = uiImageForm.getUIInput(FIELD_UPLOAD) ;
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      UploadResource resource = uploadService.getUploadResource(input.getUploadId()) ;
      UIPopupContainer uiPopupContainer = uiImageForm.getAncestorOfType(UIPopupContainer.class) ;
      UIProfileInputSet uiProfileInputSet = uiPopupContainer.findFirstComponentOfType(UIProfileInputSet.class) ; 
      uiProfileInputSet.setImageSource(resource.getStoreLocation()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;
    }
  }

  static  public class SaveActionListener extends EventListener<UIImageForm> {
    public void execute(Event<UIImageForm> event) throws Exception {
      UIImageForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD);
      UploadResource uploadResource = input.getUploadResource() ;
      if(uploadResource == null) {
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.fileName-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String fileName = uploadResource.getFileName() ;
      if(fileName == null || fileName.equals("")) {
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.fileName-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupContainer uiPopupActionContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
//      try {
//        UIProfileInputSet uiProfileInputSet = uiPopupActionContainer.getChild(UIProfileInputSet.class) ;
//        BufferAttachment attachfile = new BufferAttachment() ;
//         attachfile.setId("Attachment" + IdGenerator.generate());
//         attachfile.setName(uploadResource.getFileName()) ;
//         attachfile.setInputStream(input.getUploadDataAsStream()) ;
//         attachfile.setMimeType(uploadResource.getMimeType()) ;
//         attachfile.setSize((long)uploadResource.getUploadedSize());
//         uiProfileInputSet.addToUploadFileList(attachfile) ;
//         uiProfileInputSet.refreshUploadFileList() ;
//         UploadService uploadService = uiForm.getApplicationComponent(UploadService.class) ;
//         uploadService.removeUpload(input.getUploadId()) ;
//      } catch(Exception e) {
//        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.upload-error", null, 
//            ApplicationMessage.WARNING));
//        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
//        e.printStackTrace() ;
//        return ;
//      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupActionContainer) ;
    }
  }
  
  
  static  public class CancelActionListener extends EventListener<UIImageForm> {
    public void execute(Event<UIImageForm> event) throws Exception {
      UIImageForm uiForm = event.getSource() ;
      UIContactPortlet calendarPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      calendarPortlet.cancelAction() ;
     }
  }  
}
