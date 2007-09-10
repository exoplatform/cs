/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import org.exoplatform.mail.service.Message;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/mail/webui/UIMessagePreview.gtmpl",
    events = {
        @EventConfig(listeners = UIMessagePreview.DownloadAttachmentActionListener.class)
    }
)

public class UIMessagePreview extends UIComponent {
  private Message selectedMessage_ = null;
  
  public UIMessagePreview() throws Exception {}
  
  public Message getMessage() throws Exception { return selectedMessage_; }
  
  public void setMessage(Message msg) throws Exception { selectedMessage_ = msg; }
  
  public static class DownloadAttachmentActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      String attId = event.getRequestContext().getRequestParameter(OBJECTID);
      System.out.println("===================>>>> " + attId);
    }
  }
}