/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Oct 25, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIImportForm.ImportActionListener.class), 
      @EventConfig(listeners = UIImportForm.CancelActionListener.class)
    }
)
public class UIImportForm extends UIForm implements UIPopupComponent {

  public UIImportForm() {}
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  static public class ImportActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      System.out.println(" === >>> Import Listener");
    }
  }
  
  static public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}