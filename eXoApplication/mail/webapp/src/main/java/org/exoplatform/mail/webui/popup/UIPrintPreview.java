/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.webui.application.WebuiRequestContext;
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
 * Nov 7, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UIPrintPreview.gtmpl",
    events = {  
      @EventConfig(listeners = UIPrintPreview.PrintActionListener.class),
      @EventConfig(listeners = UIPrintPreview.CancelActionListener.class)
    }
)
public class UIPrintPreview extends UIForm implements UIPopupComponent {
  private String printMessage_ ;
  
  public UIPrintPreview() { }
  
  public String getPrintMessage() throws Exception {
    return printMessage_;
  }
  
  public void setPrintMessage(String msgId) throws Exception {
    printMessage_ = msgId ;
  }
  
  public String[] getAction() { return new String[] {"print", "cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class PrintActionListener extends EventListener<UIPrintPreview> {
    public void execute(Event<UIPrintPreview> event) throws Exception {
      
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIPrintPreview> {
    public void execute(Event<UIPrintPreview> event) throws Exception {
      UIPrintPreview uiPrintPreview = event.getSource();
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.getJavascriptManager().importJavascript("eXo.mail.UIMailPortlet","/mail/javascript/");
      context.getJavascriptManager().addJavascript("eXo.mail.UIMailPortlet.closePrint()");
      uiPrintPreview.getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}