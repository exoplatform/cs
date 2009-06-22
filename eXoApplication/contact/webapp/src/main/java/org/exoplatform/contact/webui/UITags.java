/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.UIContacts.FullNameComparator;
import org.exoplatform.contact.webui.popup.UIExportForm;
import org.exoplatform.contact.webui.popup.UIEditTagForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIExportForm.ContactData;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
        @EventConfig(listeners = UITags.AddTagActionListener.class),
        @EventConfig(listeners = UITags.EditTagActionListener.class),
        @EventConfig(listeners = UITags.ExportAddressActionListener.class),
        @EventConfig(listeners = UITags.PrintActionListener.class),
        @EventConfig(listeners = UITags.DeleteTagActionListener.class, confirm = "UITags.msg.confirm-delete")        
    }
)
public class UITags extends UIComponent {
  
  public UITags() throws Exception { }
  private String selectedTag_ = null ;
  private Map<String, Tag> tagMap_ = new LinkedHashMap<String, Tag>() ;
  
  public List<Tag> getTags() throws Exception {
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ;
    List<Tag> tags = contactService.getTags(username) ;
    tagMap_.clear() ;
    for(Tag tag : tags) { tagMap_.put(tag.getId(), tag) ; }
    return tags;
  }
  public Map<String, Tag> getTagMap() { return tagMap_ ; }
  public void setSelectedTag(String id) { selectedTag_ = id ; }
  public String getSelectedTag() { return selectedTag_ ; }
  
  static  public class SelectTagActionListener extends EventListener<UITags> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.setSelectedTag(tagId) ;
      UIWorkingContainer uiWorkingContainer = uiForm.getAncestorOfType(UIWorkingContainer.class) ;
      uiWorkingContainer.findFirstComponentOfType(UIAddressBooks.class).setSelectedGroup(null) ;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
      DataPageList pageList =ContactUtils.getContactService().getContactPageListByTag(
          ContactUtils.getCurrentUser(), tagId) ;
      FullNameComparator.isAsc = true ;
      uiContacts.setSortedBy(UIContacts.fullName) ;
      uiContacts.setContacts(pageList) ;
      uiContacts.setSelectedGroup(null) ;
      uiContacts.setSelectedTag(tagId) ;
      uiContacts.setDisplaySearchResult(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }
  
  static  public class AddTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UIEditTagForm uiEditTagForm = popupAction.activate(UIEditTagForm.class, 500) ;
      uiEditTagForm.setNew(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class EditTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UIEditTagForm uiEditTagForm = popupAction.activate(UIEditTagForm.class, 500) ;
      uiEditTagForm.setValues(uiForm.tagMap_.get(tagId)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class ExportAddressActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiForm = event.getSource() ;
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;  
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ; 
      List<Contact> contacts = null ;
      if (!ContactUtils.isEmpty(uiContacts.getSelectedTag()) && uiContacts.getSelectedTag().equals(tagId)) {
        contacts = uiContacts.getContactPageList().getAll() ;
      } else {
        contacts = ContactUtils.getContactService().getContactPageListByTag(
            ContactUtils.getCurrentUser(), tagId).getAll();
      }
      if (contacts == null || contacts.size() == 0) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UITag.msg.noContactToExport", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;  
      }
      UIExportForm uiExportForm = popupAction.activate(UIExportForm.class, 600) ;
      uiExportForm.setId("ExportForm");
      uiExportForm.setSelectedTag(uiForm.tagMap_.get(tagId).getName()) ; 
      List<ContactData> data = new ArrayList<ContactData>() ;
      for(Contact contact : contacts) {
        ContactData contactData = uiExportForm.new ContactData(contact.getId(), contact.getFullName(), contact.getEmailAddress()) ;
        data.add(contactData) ;
      }
      Map<String, Contact> contactMap = new HashMap<String, Contact>() ;
      for (Contact contact : contacts) contactMap.put(contact.getId(), contact) ;
      uiExportForm.setContacts(contactMap) ;
      uiExportForm.setContactList(data);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  } 
  
  static  public class DeleteTagActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiTags = event.getSource() ;
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      ContactUtils.getContactService()
        .removeTag(ContactUtils.getCurrentUser(), tagId) ;
      UIWorkingContainer uiWorkingContainer = uiTags.getAncestorOfType(UIWorkingContainer.class) ;
      uiWorkingContainer.getAncestorOfType(UIContactPortlet.class).cancelAction() ;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
      if (tagId.equals(uiTags.getSelectedTag())) {
        uiTags.setSelectedTag(null) ;
        uiContacts.setContacts(null) ;
        uiWorkingContainer.findFirstComponentOfType(UIContactPreview.class).setContact(null) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }
  
  static  public class PrintActionListener extends EventListener<UITags> {
    public void execute(Event<UITags> event) throws Exception {
      UITags uiTags = event.getSource() ;
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID); 
      UIWorkingContainer workingContainer = uiTags.getAncestorOfType(UIWorkingContainer.class) ;
      UIContacts uiContacts = workingContainer.findFirstComponentOfType(UIContacts.class) ;      
      UIContactPreview uiContactPreview = workingContainer.findFirstComponentOfType(UIContactPreview.class) ;
      uiContactPreview.setRendered(false) ;
      uiContacts.setListBeforePrint(Arrays.asList(uiContacts.getContacts())) ;
      uiContacts.setViewListBeforePrint(uiContacts.getViewContactsList()) ;
      uiContacts.setViewContactsList(false) ;
      uiContacts.setPrintForm(true) ;
      //uiContacts.setSelectedGroup(null) ;

      List<Contact> contacts = ContactUtils.getContactService().getContactPageListByTag(
          ContactUtils.getCurrentUser(), tagId).getAll() ;
      LinkedHashMap<String, Contact> contactMap = new LinkedHashMap<String, Contact>() ;
      for (Contact contact : contacts) contactMap.put(contact.getId(), contact) ;
      uiContacts.setContactMap(contactMap) ;   
      event.getRequestContext().addUIComponentToUpdateByAjax(workingContainer) ;  
    }
  }
  
}
