/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.portal.webui.util.Util;
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
    template =  "app:/templates/contact/webui/UITags.gtmpl",
    events = {
        @EventConfig(listeners = UITags.SelectTagActionListener.class)      
    }
)
public class UITags extends UIComponent  {
  public UITags() throws Exception {
    
  }
  
  public List<Tag> getTags() throws Exception {
    ContactService contactService = this.getApplicationComponent(ContactService.class);
    String username = Util.getPortalRequestContext().getRemoteUser() ;    
    return contactService.getTags(username);
  }
  
  static  public class SelectTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIWorkingContainer uiWorkingContainer = uiForm.getAncestorOfType(UIWorkingContainer.class) ;
      
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.setSelectTag(true);
      uiContacts.setTagName(tagName);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
      
      UIContactPreview uiContactPreview = uiWorkingContainer.findFirstComponentOfType(UIContactPreview.class);
      ContactService contactService = uiForm.getApplicationComponent(ContactService.class) ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      Contact contact = null ;
      if (contactService.getContactsByTag(username, tagName).size() > 0) 
        contact = contactService.getContactsByTag(username, tagName).get(0);
      uiContactPreview.setContact(contact);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPreview) ;
    }
  }
  
}
