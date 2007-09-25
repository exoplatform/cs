/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.io.ByteArrayInputStream;

import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.upload.UploadResource;
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
      ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getUploadData()) ;
      UIPopupContainer uiPopupActionContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIContactForm uiContactForm =  uiPopupActionContainer.findFirstComponentOfType(UIContactForm.class) ;
      UIProfileInputSet uiProfileInputSet = uiContactForm.getChild(UIProfileInputSet.class) ;
      uiProfileInputSet.setImage(inputStream) ;
      uiProfileInputSet.setFileName(fileName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupActionContainer) ;
    }
  }
  
  
  static  public class CancelActionListener extends EventListener<UIImageForm> {
    public void execute(Event<UIImageForm> event) throws Exception {
      UIImageForm uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      uiContactPortlet.cancelAction() ;
     }
  }  
}
