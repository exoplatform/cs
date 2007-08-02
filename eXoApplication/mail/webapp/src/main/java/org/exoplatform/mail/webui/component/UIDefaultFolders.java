/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.component ;

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
    template =  "app:/templates/mail/webui/component/UIDefaultFolders.jstmpl",
    events = {
        @EventConfig(listeners = UIDefaultFolders.ChangeFolderActionListener.class),
        @EventConfig(listeners = UIDefaultFolders.RemoveAllMessagesActionListener.class)
    }
)

public class UIDefaultFolders extends UIComponent {
  public UIDefaultFolders() throws Exception {}
  
  static public class ChangeFolderActionListener extends EventListener<UIDefaultFolders> {
    public void execute(Event<UIDefaultFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class RemoveAllMessagesActionListener extends EventListener<UIDefaultFolders> {
    public void execute(Event<UIDefaultFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
}