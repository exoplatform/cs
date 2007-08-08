/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

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
    template =  "app:/templates/mail/webui/UICustomizeFolders.gtmpl",
    events = {
        @EventConfig(listeners = UICustomizeFolders.ChangeFolderActionListener.class),
        @EventConfig(listeners = UICustomizeFolders.AddFolderActionListener.class),
        @EventConfig(listeners = UICustomizeFolders.RemoveFolderActionListener.class),
        @EventConfig(listeners = UICustomizeFolders.RenameFolderActionListener.class), 
        @EventConfig(listeners = UICustomizeFolders.RemoveAllMessagesFolderActionListener.class)
    }
)

public class UICustomizeFolders extends UIComponent {
  public UICustomizeFolders() throws Exception {}
  
  static public class ChangeFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class AddFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class RemoveFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class RenameFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class RemoveAllMessagesFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
}