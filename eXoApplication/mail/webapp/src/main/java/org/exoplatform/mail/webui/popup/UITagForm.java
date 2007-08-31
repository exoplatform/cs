/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITagForm.SaveActionListener.class), 
      @EventConfig(listeners = UITagForm.CancelActionListener.class, phase = Phase.DECODE)
    }  
)
public class UITagForm extends UIForm implements UIPopupComponent{
  public static final String SELECT_AVAIABLE_TAG = "New Tag";
  
  public UITagForm() { 
    addChild(new UIFormStringInput(SELECT_AVAIABLE_TAG, null, null));
  }
  public void createCheckBoxTagList(List<Tag> listTags) {
    removeChild(UIFormCheckBoxInput.class);
    for(Tag tag : listTags) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(tag.getName(), tag.getName(), null)) ;
    }
  }
  
  public String getLabel(String id) { return id ;}
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  static  public class SaveActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiForm = event.getSource() ;
    }
  }
  static  public class CancelActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiForm = event.getSource() ;
    }
  }
   
}
