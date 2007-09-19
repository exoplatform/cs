/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
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
      @EventConfig(listeners = UIImageForm.SaveActionListener.class),      
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
  
  static  public class SaveActionListener extends EventListener<UIImageForm> {
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
  
  static  public class CancelActionListener extends EventListener<UIImageForm> {
    public void execute(Event<UIImageForm> event) throws Exception {
      UIImageForm uiForm = event.getSource() ;
      UIContactPortlet calendarPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      calendarPortlet.cancelAction() ;
     }
  }  
}
