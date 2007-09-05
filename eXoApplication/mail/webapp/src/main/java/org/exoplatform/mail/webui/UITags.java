/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.webui.popup.UIMailSettings;
import org.exoplatform.portal.webui.application.UIPortlet;
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
    template =  "app:/templates/mail/webui/UITagContainer.gtmpl",
    events = {
        @EventConfig(listeners = UITags.ChangeTagActionListener.class)
    }
)

public class UITags extends UIComponent {
  public UITags() throws Exception {}
  
  private List<Tag> getTags() throws Exception {
    MailService mailService = (MailService)PortalContainer.getComponent(MailService.class) ;
    String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
    String accountId = getAncestorOfType(UINavigationContainer.class).
    getChild(UISelectAccount.class).getSelectedValue() ;
    return mailService.getTags(username, accountId) ;
  }
  
  static public class ChangeTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      String tagname = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITags uiTags = event.getSource();
      UIMailPortlet uiPortlet = uiTags.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      uiMessageList.setSelectedFolderId(null);
      uiMessageList.setSelectedTagName(tagname);
      uiMessageList.addCheckboxForMessages();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList);
    }
  }
}