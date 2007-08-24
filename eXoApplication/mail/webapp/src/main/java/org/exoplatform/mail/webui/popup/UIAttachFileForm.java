/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.mail.webui.UIMailPortlet;
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
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 24, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAttachFileForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAttachFileForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAttachFileForm extends UIForm implements UIPopupComponent {

  final static public String FIELD_UPLOAD = "upload" ;  
  

  public UIAttachFileForm() throws Exception {
    setMultiPart(true) ;
    //UIFormMultiValueInputSet multiUpload = new UIFormMultiValueInputSet(FIELD_UPLOAD, FIELD_UPLOAD) ;
    //multiUpload.setType(UIFormUploadInput.class) ;
    //addUIFormInput(multiUpload) ;
    UIFormUploadInput uiInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD) ;
    uiInput.setEditable(false);
    addUIFormInput(uiInput) ;
  }


  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  static  public class SaveActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD);
      if(input.getUploadResource() == null) {
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.fileName-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String fileName = input.getUploadResource().getFileName() ;
      if(fileName == null || fileName.equals("")) {
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.fileName-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupActionContainer uiPopupActionContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class) ;
      try {
        byte[] content = input.getUploadData() ;
        MimeTypeResolver mimeTypeSolver = new MimeTypeResolver() ;
        String mimeType = mimeTypeSolver.getMimeType(fileName) ;
        String fileSize = String.valueOf((((float)(content.length/100))/10)) +" Kb";     
        String[] arrValues = {fileName, fileSize +" Kb", mimeType} ;
        UploadService uploadService = uiForm.getApplicationComponent(UploadService.class) ;
        UIFormUploadInput uiChild = uiForm.getChild(UIFormUploadInput.class) ;
        uploadService.removeUpload(uiChild.getUploadId()) ;
        UIComposeForm uiComposeForm = uiPopupActionContainer.getChild(UIComposeForm.class) ;
        uiComposeForm.setUploadFileList(fileName, fileSize, mimeType) ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.upload-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupActionContainer) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction() ;
    }
  }
}
