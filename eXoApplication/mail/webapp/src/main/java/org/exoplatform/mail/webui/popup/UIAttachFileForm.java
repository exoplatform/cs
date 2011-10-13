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

import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
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
                 template =  "app:/templates/mail/webui/popup/UIAttachFileForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UIAttachFileForm.AddMoreActionListener.class), 
                   @EventConfig(listeners = UIAttachFileForm.SaveActionListener.class), 
                   @EventConfig(listeners = UIAttachFileForm.CancelActionListener.class, phase = Phase.DECODE)
                 }
)

public class UIAttachFileForm extends UIForm implements UIPopupComponent {
  private static final Log log = ExoLogger.getExoLogger(UIAttachFileForm.class);

  public static final String FIELD_UPLOAD = "upload" ;  
  public int numberFile = 5 ;
  private long attSize = 0;

  public UIAttachFileForm() throws Exception {
    setMultiPart(true) ;
    int sizeLimit = MailUtils.getLimitUploadSize();
    for (int i = 0; i < numberFile; i++) {
      if (sizeLimit == MailUtils.DEFAULT_VALUE_UPLOAD_PORTAL) {
        addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD + String.valueOf(i + 1),FIELD_UPLOAD + String.valueOf(i + 1), true));
      } else {
        addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD + String.valueOf(i + 1), FIELD_UPLOAD + String.valueOf(i + 1), sizeLimit, true));
      }
    }
  }

  public void setNumberFile(int nb) { numberFile = nb; }

  public int  getNumberFile() { return numberFile; }

  public String[] getActions() { return new String[]{ "Save", "Cancel" } ;} 

  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
  public long getAttSize() {return attSize ;}
  public void setAttSize(long value) { attSize = value ;}


  public static void removeUploadTemp(UploadService uservice, String uploadId) {
    try {
      uservice.removeUploadResource(uploadId) ;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method removeUploadTemp", e);
      }
    }
  }

  static  public class SaveActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiForm = event.getSource();      
      UIPopupActionContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class) ;
      List<BufferAttachment> attachList = new ArrayList<BufferAttachment>();
      long attSize = 0;
      try {
        for (int i = 1; i <= uiForm.getNumberFile(); i++) {  
          UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD + String.valueOf(i));
          UploadResource uploadResource = input.getUploadResource() ;
          if (uploadResource != null) {
            attSize = attSize + ((long)uploadResource.getUploadedSize()) ;
            if(attSize > 10*1024*1024) {
              event.getRequestContext()
                   .getUIApplication()
                   .addMessage(new ApplicationMessage("UIAttachFileForm.msg.size-attachs-must-be-smaller-than-10M",
                                                      null,
                                                      ApplicationMessage.WARNING));              
              return ;
            }
            BufferAttachment attachFile = new BufferAttachment() ;
            attachFile.setId("Attachment" + IdGenerator.generate());
            attachFile.setName(uploadResource.getFileName()) ;
            attachFile.setInputStream(input.getUploadDataAsStream()) ;
            attachFile.setMimeType(uploadResource.getMimeType()) ;
            attachFile.setSize((long)uploadResource.getUploadedSize());
            attachFile.setResoureId(input.getUploadId()) ;
            attachFile.setPath(uploadResource.getStoreLocation());
            attachList.add(attachFile);            
          }
        }
      } catch(Exception e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAttachFileForm.msg.upload-error",
                                                                                       null,
                                                                                       ApplicationMessage.INFO));        
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class SaveActionListener", e);
        }
        return ;
      }     
      if (attachList.isEmpty()) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAttachFileForm.msg.file-empty-error",
                                                                                       null,
                                                                                       ApplicationMessage.INFO));        
        return ;
      } else {
        UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
        UIComposeForm uiComposeForm = uiPortlet.findFirstComponentOfType(UIComposeForm.class);
        UIEventForm uiEventForm =  uiPortlet.findFirstComponentOfType(UIEventForm.class);
        if(uiComposeForm != null) {
          for (Attachment att : attachList) {
            uiComposeForm.addToUploadFileList(att) ;
          }
          uiComposeForm.refreshUploadFileList() ;
          UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
          uiPopupAction.cancelPopupAction();
          event.getRequestContext().addUIComponentToUpdateByAjax(uiComposeForm.getChildById(UIComposeForm.FIELD_TO_SET)) ;
        } else if (uiEventForm != null) {
          uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTDETAIL) ;
          UIEventDetailTab uiEventDetailTab = uiEventForm.getChild(UIEventDetailTab.class) ;
          for(Attachment att :  attachList){
            org.exoplatform.calendar.service.Attachment a = new org.exoplatform.calendar.service.Attachment() ;
            a.setInputStream(att.getInputStream());
            a.setMimeType(att.getMimeType()) ;
            a.setName(att.getName());
            a.setSize(att.getSize());
            a.setResourceId(att.getResoureId()) ;
            uiEventDetailTab.addToUploadFileList(a) ;
          }
          uiEventDetailTab.refreshUploadFileList() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiEventDetailTab) ;
          UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class) ;
          uiPopupAction.cancelPopupAction();
        }
      } 
    }
  }

  static  public class AddMoreActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiAttach = event.getSource();
      int numberAttachFile = uiAttach.getNumberFile() + 1;
      if (numberAttachFile <= 50) { 
        UIFormUploadInput uiInput = new UIFormUploadInput(FIELD_UPLOAD + String.valueOf(numberAttachFile), FIELD_UPLOAD + String.valueOf(numberAttachFile)) ;
        uiAttach.addUIFormInput(uiInput) ;
        uiAttach.setNumberFile(numberAttachFile);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAttach);
    }
  }

  static  public class CancelActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiForm = event.getSource();
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ; 
      for (int i = 1; i <= uiForm.getNumberFile(); i++) {  
        UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD + String.valueOf(i));
        UploadResource uploadResource = input.getUploadResource() ;
        if(uploadResource != null) 
          UIAttachFileForm.removeUploadTemp(uiForm.getApplicationComponent(UploadService.class), uploadResource.getUploadId()) ;
      }
      uiPopupAction.cancelPopupAction();
    }
  }
}
