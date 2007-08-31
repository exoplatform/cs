/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UITagForm;
import org.exoplatform.web.command.handler.GetApplicationHandler;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    template =  "app:/templates/mail/webui/UIMessageActionBar.gtmpl",
    events = {
        @EventConfig(listeners = UIMessageActionBar.ReplyActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.ReplyAllActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.ForwardActionListener.class), 
        @EventConfig(listeners = UIMessageActionBar.MarkAsReadActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.MarkAsUnReadActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.AddStarActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.RemoveStarActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.AddTagActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.MoveMessagesActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.ImportActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.ExportActionListener.class)
    }
)

public class UIMessageActionBar extends UIContainer {
  
  public UIMessageActionBar()throws Exception {}
  
  static  public class ReplyActionListener extends EventListener<UIMessageActionBar> {    
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }

  static public class ReplyAllActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  } 
     
  static public class ForwardActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }  
  static public class MarkAsReadActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class MarkAsUnReadActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class AddStarActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class RemoveStarActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class AddTagActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ; 
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      UITagForm uiTagForm = uiActionBar.createUIComponent(UITagForm.class, null, null) ;
      String username = uiPortlet.getCurrentUser();
      MailService mailService = uiActionBar.getApplicationComponent(MailService.class);
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      List<Tag> listTags = mailService.getTags(username, accId);
      uiTagForm.createCheckBoxTagList(listTags) ;
      uiPopupAction.activate(uiTagForm, 600, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }
  static public class MoveMessagesActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class ImportActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class ExportActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
}