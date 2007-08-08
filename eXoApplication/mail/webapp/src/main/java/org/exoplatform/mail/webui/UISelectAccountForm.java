/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.webui.popup.UIPopupComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    template = "app:/templates/mail/webui/UISelectAccountForm.gtmpl",
    events = {
      @EventConfig( listeners = UISelectAccountForm.SaveAccountActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UISelectAccountForm.CancelAccountActionListener.class)
    }
)
public class UISelectAccountForm extends UIForm implements UIPopupComponent{
  final static public String FIELD_ACCOUNT = "accountSelect" ;
  
  private  List<SelectItemOption<String>> accountOptions = new ArrayList<SelectItemOption<String>>() ;
  
  public UISelectAccountForm() {
    addUIFormInput(new UIFormSelectBox(FIELD_ACCOUNT, FIELD_ACCOUNT, accountOptions)) ;
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  static  public class SaveAccountActionListener extends EventListener<UISelectAccountForm> {
    public void execute(Event<UISelectAccountForm> event) throws Exception {
      UISelectAccountForm uiForm = event.getSource() ;
      System.out.println("========> AddAccountActionListener") ;
    }
  }
  static  public class CancelAccountActionListener extends EventListener<UISelectAccountForm> {
    public void execute(Event<UISelectAccountForm> event) throws Exception {
      
      
    }
  }  
}
