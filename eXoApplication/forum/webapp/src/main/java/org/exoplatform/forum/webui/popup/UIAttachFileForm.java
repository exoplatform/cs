/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;


import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.webui.UIForumPortlet;
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
  private boolean isTopicForm = true ;

  public UIAttachFileForm() throws Exception {
    setMultiPart(true) ;
    UIFormUploadInput uiInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD) ;
    addUIFormInput(uiInput) ;
  }

	public void updateIsTopicForm(boolean isTopicForm) throws Exception {
	  this.isTopicForm = isTopicForm ;
	}
	
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  static  public class SaveActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD);
      UploadResource uploadResource = input.getUploadResource() ;
      if(uploadResource == null) {
      	System.out.println("\n\n  error 1\n\n");
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.fileName-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String fileName = uploadResource.getFileName() ;
      if(fileName == null || fileName.equals("")) {
      	System.out.println("\n\n  error 2\n\n");
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.fileName-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      UITopicForm topicForm = forumPortlet.findFirstComponentOfType(UITopicForm.class);
      UIPostForm postForm = forumPortlet.findFirstComponentOfType(UIPostForm.class);
			
      try {
        BufferAttachment attachfile = new BufferAttachment() ;
        attachfile.setId("Attachment" + IdGenerator.generate());
        attachfile.setName(uploadResource.getFileName()) ;
        attachfile.setInputStream(input.getUploadDataAsStream()) ;
        attachfile.setMimeType(uploadResource.getMimeType()) ;
        attachfile.setSize((long)uploadResource.getUploadedSize());
				if(uiForm.isTopicForm) {
					topicForm.addToUploadFileList(attachfile) ;
          topicForm.refreshUploadFileList() ;
				} else {
					postForm.addToUploadFileList(attachfile) ;
          postForm.refreshUploadFileList() ;
				}
         UploadService uploadService = uiForm.getApplicationComponent(UploadService.class) ;
         uploadService.removeUpload(input.getUploadId()) ;
      } catch(Exception e) {
      	System.out.println("\n\n  error 3\n\n");
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.upload-error", null, 
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      }
      UIPopupAction popupAction = forumPortlet.findComponentById("UIChildPopupAction") ;
      popupAction.setRendered(false)  ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      if(uiForm.isTopicForm) {
        event.getRequestContext().addUIComponentToUpdateByAjax(topicForm) ;
			} else {
        event.getRequestContext().addUIComponentToUpdateByAjax(postForm) ;
			}
    }
  }

  static  public class CancelActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.findComponentById("UIChildPopupAction");
      popupAction.setRendered(false)  ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

}
