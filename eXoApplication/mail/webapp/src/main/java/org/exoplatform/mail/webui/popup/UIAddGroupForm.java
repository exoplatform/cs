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
package org.exoplatform.mail.webui.popup;

import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 26, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAddGroupForm.AddActionListener.class), 
      @EventConfig(listeners = UIAddGroupForm.CancelActionListener.class, phase = Phase.DECODE)
    }  
)
public class UIAddGroupForm extends UIForm implements UIPopupComponent{
  public final static String GROUP_NAME = "group-name".intern() ;
  public final static String GROUP_DESCRIPTION = "group-description".intern() ;
  
  public UIAddGroupForm() {
    addUIFormInput(new UIFormStringInput(GROUP_NAME, GROUP_NAME, null)) ;
    addUIFormInput(new UIFormTextAreaInput(GROUP_DESCRIPTION, GROUP_DESCRIPTION, null)) ;
  }
  
  public String[] getActions() {return (new String[]{"Add", "Cancel"}) ; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class AddActionListener extends EventListener<UIAddGroupForm> {
    public void execute(Event<UIAddGroupForm> event) throws Exception {
      UIAddGroupForm uiAddGroupForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiAddGroupForm.getAncestorOfType(UIMailPortlet.class) ;
      ContactService contactSrv = uiAddGroupForm.getApplicationComponent(ContactService.class) ;
      UIApplication uiApp = uiAddGroupForm.getAncestorOfType(UIApplication.class) ;
      String groupName = uiAddGroupForm.getUIStringInput(GROUP_NAME).getValue() ;
      String groupDescription = uiAddGroupForm.getUIFormTextAreaInput(GROUP_DESCRIPTION).getValue() ;
      String username = MailUtils.getCurrentUser() ;
      if (groupName == null || groupName.trim().equals("")) {
        uiApp.addMessage(new ApplicationMessage("UIAddGroupForm.msg.group-name-required", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      } else {
        for (AddressBook group : contactSrv.getGroups(username))
          if (group.getName().equalsIgnoreCase(groupName.trim())) {
            uiApp.addMessage(new ApplicationMessage("UIAddGroupForm.msg.group-name-exist", null,
                ApplicationMessage.WARNING)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
        AddressBook group = new AddressBook();
        group.setName(groupName);
        group.setDescription(groupDescription);
        contactSrv.saveAddressBook(username, group, true);
        UIAddContactForm uiAddContact = uiPortlet.findFirstComponentOfType(UIAddContactForm.class);
        UIAddressBookForm uiAddressBook = uiPortlet.findFirstComponentOfType(UIAddressBookForm.class);
        if (uiAddContact != null) {
          uiAddContact.refreshGroupList() ;
          uiAddContact.setAddedNewGroup(true);
          //cs-2170
          ((UIFormSelectBoxWithGroups)(uiAddContact.getChildById(UIAddContactForm.SELECT_GROUP))).setValue(group.getId()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddContact.getParent()) ;
        } else if (uiAddressBook != null) {
          uiAddressBook.updateGroup(group.getId()) ;
          uiAddressBook.refrestContactList(group.getId()) ; 
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent()) ;
        }
      }
      UIPopupAction uiPopupAction = uiAddGroupForm.getAncestorOfType(UIPopupAction.class) ; 
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAddGroupForm> {
    public void execute(Event<UIAddGroupForm> event) throws Exception {
      UIAddGroupForm uiAddGroup = event.getSource();
      UIPopupAction uiPopupAction = uiAddGroup.getAncestorOfType(UIPopupAction.class) ; 
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

}
