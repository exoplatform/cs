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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.services.jcr.util.IdGenerator;
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
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 24, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/UIAttachFileForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAttachFileForm.AddMoreActionListener.class), 
      @EventConfig(listeners = UIAttachFileForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAttachFileForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAttachFileForm extends UIForm implements UIPopupComponent {

  public static final String FIELD_UPLOAD = "upload" ;  
  public int numberFile = 5 ;

  public UIAttachFileForm() throws Exception {
    setMultiPart(true) ;
    for (int i = 0; i < 5; i++ ) {
      UIFormUploadInput uiInput = new UIFormUploadInput(FIELD_UPLOAD + String.valueOf(i+1), FIELD_UPLOAD + String.valueOf(i+1)) ;
      addUIFormInput(uiInput) ;
    }
  }
  
  public void setNumberFile(int nb) { numberFile = nb; }
  
  public int  getNumberFile() { return numberFile; }

  public String[] getActions() { return new String[]{ "Save", "Cancel" } ;} 
  
  public void activate() throws Exception {}
  
  public void deActivate() throws Exception {}

  static  public class SaveActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      List<BufferAttachment> fileAttachList = new ArrayList<BufferAttachment>();
      try {
        for (int i = 1; i <= uiForm.getNumberFile(); i++) {        
          UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD + String.valueOf(i));
          UploadResource uploadResource = input.getUploadResource() ;
          if (uploadResource != null) {
            BufferAttachment attachFile = new BufferAttachment() ;
            attachFile.setId("Attachment" + IdGenerator.generate());
            attachFile.setName(uploadResource.getFileName()) ;
            attachFile.setInputStream(input.getUploadDataAsStream()) ;
            attachFile.setMimeType(uploadResource.getMimeType()) ;
            attachFile.setSize((long)uploadResource.getUploadedSize());
            fileAttachList.add(attachFile);
          }
        }
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.upload-error", null, 
            ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      }     
      
      if (fileAttachList.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.file-empty-error", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else {
        UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
        UIComposeForm uiComposeForm = uiPortlet.findFirstComponentOfType(UIComposeForm.class);
        for (BufferAttachment att : fileAttachList) {
          uiComposeForm.addToUploadFileList(att) ;
        }
        uiComposeForm.refreshUploadFileList() ;
      } 
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupActionContainer.class)) ;
    }
  }

  static  public class AddMoreActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiAttach = event.getSource();
      int numberAttachFile = uiAttach.getNumberFile() + 1;
      if (numberAttachFile <= 10) { 
        UIFormUploadInput uiInput = new UIFormUploadInput(FIELD_UPLOAD + String.valueOf(numberAttachFile), FIELD_UPLOAD + String.valueOf(numberAttachFile)) ;
        uiAttach.addUIFormInput(uiInput) ;
        uiAttach.setNumberFile(numberAttachFile);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAttach);
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiAttach = event.getSource();
      UIPopupAction uiPopupAction = uiAttach.getAncestorOfType(UIPopupAction.class) ; 
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}
