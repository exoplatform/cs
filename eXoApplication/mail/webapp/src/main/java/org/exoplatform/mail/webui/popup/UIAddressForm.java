/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.contact.service.*;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormUploadInput;

import com.thoughtworks.xstream.core.ReferenceByIdMarshaller.IDGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Hai
 *          haiexo1002@gmail.com
 * Sep 25, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/UIAddressForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAddressForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAddressForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAddressForm extends UIForm implements UIPopupComponent { 
  public UIAddressForm() throws Exception {  
    setContactList("");
  }
  private Map<String, Contact> contactMap_ = new HashMap<String, Contact>(); 

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public List<Contact> getContacts() throws Exception { 
    return new ArrayList<Contact>(contactMap_.values());
  }
  
  
  public void setContactList() throws Exception {
    setContactList("");
  }

  public void setContactList(String groupId) throws Exception {
    List<Contact> contacts = new ArrayList<Contact>();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    String username = Util.getPortalRequestContext().getRemoteUser();   
    if (groupId == null || groupId == "") {
      contacts = contactSrv.getAllContact(username);
    } else {
      contacts = contactSrv.getContactPageListByGroup(username, groupId).getAll();
    }
    setContactList(contacts);
  }
  
  
  public void setContactList(List<Contact> contactList) throws Exception {
    getChildren().clear();
    contactMap_.clear();
    for (Contact contact : contactList) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false));
      contactMap_.put(contact.getId(), contact);
    }
  }
  
  static  public class SaveActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
    //  event.getSource().getAncestorOfType(UIMailPortlet.class). ;
     
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction() ;
    }
  }
}
