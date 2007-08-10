/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UICalendarPrintPreview.gtmpl",
    events = {
      @EventConfig(listeners = UICalendarPrintPreview.SaveActionListener.class),
      @EventConfig(listeners = UICalendarPrintPreview.CancelActionListener.class)
    }
)
public class UICalendarPrintPreview extends UIComponent {
  
  public UICalendarPrintPreview() {
  }
  
  static  public class SaveActionListener extends EventListener<UICalendarPrintPreview> {
    public void execute(Event<UICalendarPrintPreview> event) throws Exception {
      UICalendarPrintPreview uiForm = event.getSource() ;
    }
  }
  static  public class CancelActionListener extends EventListener<UICalendarPrintPreview> {
    public void execute(Event<UICalendarPrintPreview> event) throws Exception {
      UICalendarPrintPreview uiForm = event.getSource() ;
    }
  }
}
