/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
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
        @EventConfig(listeners = UITags.SelectTagActionListener.class),
        @EventConfig(listeners = UITags.EditTagActionListener.class),
        @EventConfig(listeners = UITags.DeleteTagActionListener.class)        
    }
)
public class UITags extends UIComponent  {
  
  public UITags() throws Exception { }
  private String selectedTag_ = null ;
  
  public List<Tag> getTags() throws Exception {
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ;    
    return contactService.getTags(username);
  }
  
  public void setSelectedTag(String name) { selectedTag_ = name ; }
  public String getSelectedTag() { return selectedTag_ ; }
  
  static  public class SelectTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.setSelectedTag(tagName) ;
      UIWorkingContainer uiWorkingContainer = uiForm.getAncestorOfType(UIWorkingContainer.class) ;
      uiWorkingContainer.setSelectedGroup(null) ;
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.setContacts(contactService.getContactsByTag(username, tagName)) ;
      UIContactPreview uiContactPreview = uiWorkingContainer.findFirstComponentOfType(UIContactPreview.class);
      uiContactPreview.updateContact() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }
  
  static  public class EditTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      
      
    }
  }
  
  static  public class DeleteTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagName = event.getRequestContext().getRequestParameter(OBJECTID) ;
    
      
    }
  }
  
}
