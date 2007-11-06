/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.popup.UIExportForm;
import org.exoplatform.contact.webui.popup.UIEditTagForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
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
        @EventConfig(listeners = UITags.ExportAddressActionListener.class),
        @EventConfig(listeners = UITags.DeleteTagActionListener.class,
            confirm = "UITags.msg.confirm-delete")        
    }
)
public class UITags extends UIComponent {
  
  public UITags() throws Exception { }
  private String selectedTag_ = null ;
  
  public List<Tag> getTags() throws Exception {
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ;    
    return contactService.getTags(username);
  }
  
  public void setSelectedTag(String id) { selectedTag_ = id ; }
  public String getSelectedTag() { return selectedTag_ ; }
  
  static  public class SelectTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.setSelectedTag(tagId) ;
      UIWorkingContainer uiWorkingContainer = uiForm.getAncestorOfType(UIWorkingContainer.class) ;
      uiWorkingContainer.findFirstComponentOfType(UIAddressBooks.class).setSelectedGroup(null) ;
      
      String username = ContactUtils.getCurrentUser() ;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.setContacts(ContactUtils.getContactService().getContactPageListByTag(username, tagId)) ;
      uiContacts.setSelectedGroup(null) ;
      uiContacts.setSelectedTag(tagId) ;
      uiContacts.setDisplaySearchResult(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }
  
  static  public class EditTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UIEditTagForm uiEditTagForm = popupAction.createUIComponent(UIEditTagForm.class, null, "UIEditTagForm") ;
      uiEditTagForm.setValues(tagId) ;
      popupAction.activate(uiEditTagForm, 500, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class ExportAddressActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      
      UIExportForm uiExportForm = popupAction.createUIComponent(UIExportForm.class, null,
          "ExportForm");
      uiExportForm.setSelectedTag(tagName);
      uiExportForm.updateList();

      popupAction.activate(uiExportForm, 500, 0, true);
      
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }  
  
  static  public class DeleteTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiTags = event.getSource() ;
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      ContactUtils.getContactService().removeTag(ContactUtils.getCurrentUser(), tagId) ;
      UIWorkingContainer uiWorkingContainer = uiTags.getAncestorOfType(UIWorkingContainer.class) ;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
      if (tagId.equals(uiTags.getSelectedTag())) {
        uiTags.setSelectedTag(null) ;
        uiContacts.setContacts(null) ;
        uiWorkingContainer.findFirstComponentOfType(UIContactPreview.class).setContact(null) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTags) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
      }  
      
    }
  }
  
}
