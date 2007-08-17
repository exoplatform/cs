/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UIAddNewContact.gtmpl",
    //template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl" ,
    events = {
      @EventConfig(listeners = UIContactForm.AddCategoryActionListener.class),
      @EventConfig(listeners = UIContactForm.SaveActionListener.class),      
      @EventConfig(listeners = UIContactForm.CancelActionListener.class)
    }
)
public class UIContactForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_CATEGORY_SELECTBOX = "category";
  public static final String FIELD_FULLNAME_INPUT = "fullName";
  public static final String FIELD_FIRSTNAME_INPUT = "firstName";
  public static final String FIELD_MIDDLENAME_INPUT = "middleName";
  public static final String FIELD_LASTNAME_INPUT = "lastName";
  public static final String FIELD_NICKNAME_INPUT = "nickName";
  public static final String FIELD_PERSONALTITLE_INPUT = "personalTitle";
  public static final String FIELD_EMAIL_MULTIVALUE = "preferredEmail" ;
  
  public static final String FIELD_EXOCHAT_INPUT = "exoChat";
  public static final String FIELD_GOOGLE_INPUT = "google";
  public static final String FIELD_MSN_INPUT = "msn";
  public static final String FIELD_AOLAIM_INPUT = "aolAim";
  public static final String FIELD_YAHOO_INPUT = "yahoo";
  public static final String FIELD_ICR_INPUT = "icr";
  public static final String FIELD_SKYPE_INPUT = "skype";
  public static final String FIELD_ICQ_INPUT = "icq";
  
  public static final String FIELD_STREET_INPUT = "street";
  public static final String FIELD_CITY_INPUT = "city";
  public static final String FIELD_STATE_INPUT = "state";
  public static final String FIELD_POSTALCODE_INPUT = "postalCode";
  public static final String FIELD_COUNTRY_INPUT = "country";
  public static final String FIELD_HOMEPHONE1_INPUT = "homePhone1";
  public static final String FIELD_HOMEPHONE2_INPUT = "homePhone2";
  public static final String FIELD_HOMEFAX_INPUT = "homeFax";
  public static final String FIELD_MOBILEPHONE_INPUT = "mobilePhone";
  public static final String FIELD_WEBPAGE_INPUT = "webPage";
  
  public static final String FIELD_WORKSTREET_INPUT = "workStreet";
  public static final String FIELD_WORKCITY_INPUT = "workCity";
  public static final String FIELD_WORKSTATE_INPUT = "workState";
  public static final String FIELD_WORKPOSTALCODE_INPUT = "workPostalCode";
  public static final String FIELD_WORKCOUNTRY_INPUT = "workCountry";
  public static final String FIELD_WORKPHONE1_INPUT = "workPhone1";
  public static final String FIELD_WORKPHONE2_INPUT = "workPhone2";
  public static final String FIELD_WORKFAX_INPUT = "workFax";
  public static final String FIELD_WORKMOBILEPHONE_INPUT = "workMobilePhone";
  public static final String FIELD_WORKWEBPAGE_INPUT = "workWebPage";
  
  public static final String FIELD_NOTE_INPUT = "note";
  
  public UIContactForm() throws Exception {

    ContactService contactService = 
      (ContactService)PortalContainer.getInstance().getComponentInstanceOfType(ContactService.class) ;
    
    List<ContactGroup> contactGroups =  contactService.getGroups("exo");
    System.out.println("\n\n\n size: " + contactGroups.size() + "\n\n\n");
    List<SelectItemOption<String>> Categories = new ArrayList<SelectItemOption<String>>() ; 
    /*for (int i = 0; i < contactGroups.size(); i++) {
      Categories.add(new SelectItemOption<String>(contactGroups.get(i).getName(), contactGroups.get(i).getId())) ;
    }*/
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY_SELECTBOX, FIELD_CATEGORY_SELECTBOX, Categories)) ;
    
    addUIFormInput(new UIFormStringInput(FIELD_FULLNAME_INPUT, FIELD_FULLNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_FIRSTNAME_INPUT, FIELD_FIRSTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_MIDDLENAME_INPUT, FIELD_MIDDLENAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_LASTNAME_INPUT, FIELD_LASTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_NICKNAME_INPUT, FIELD_NICKNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_PERSONALTITLE_INPUT, FIELD_PERSONALTITLE_INPUT, null));
    UIFormMultiValueInputSet emailMultiValue = new UIFormMultiValueInputSet(FIELD_EMAIL_MULTIVALUE, FIELD_EMAIL_MULTIVALUE); 
    emailMultiValue.setType(UIFormStringInput.class) ;
    addUIFormInput(emailMultiValue);
    
    addUIFormInput(new UIFormStringInput(FIELD_EXOCHAT_INPUT, FIELD_EXOCHAT_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_GOOGLE_INPUT, FIELD_GOOGLE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_MSN_INPUT, FIELD_MSN_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_AOLAIM_INPUT, FIELD_AOLAIM_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_YAHOO_INPUT, FIELD_YAHOO_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_ICR_INPUT, FIELD_ICR_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_SKYPE_INPUT, FIELD_SKYPE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_ICQ_INPUT, FIELD_ICQ_INPUT, null));
    
    addUIFormInput(new UIFormStringInput(FIELD_STREET_INPUT, FIELD_STREET_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_CITY_INPUT, FIELD_CITY_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_STATE_INPUT, FIELD_STATE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_POSTALCODE_INPUT, FIELD_POSTALCODE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_COUNTRY_INPUT, FIELD_COUNTRY_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_HOMEPHONE1_INPUT, FIELD_HOMEPHONE1_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_HOMEPHONE2_INPUT, FIELD_HOMEPHONE2_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_HOMEFAX_INPUT, FIELD_HOMEFAX_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_MOBILEPHONE_INPUT, FIELD_MOBILEPHONE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WEBPAGE_INPUT, FIELD_WEBPAGE_INPUT, null));
    
    addUIFormInput(new UIFormStringInput(FIELD_WORKSTREET_INPUT, FIELD_WORKSTREET_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WORKCITY_INPUT, FIELD_WORKCITY_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WORKSTATE_INPUT, FIELD_WORKSTATE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WORKPOSTALCODE_INPUT, FIELD_WORKPOSTALCODE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WORKCOUNTRY_INPUT, FIELD_WORKCOUNTRY_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WORKPHONE1_INPUT, FIELD_WORKPHONE1_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WORKPHONE2_INPUT, FIELD_WORKPHONE2_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WORKFAX_INPUT, FIELD_WORKFAX_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WORKMOBILEPHONE_INPUT, FIELD_WORKMOBILEPHONE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_WORKWEBPAGE_INPUT, FIELD_WORKWEBPAGE_INPUT, null));
    
    addUIFormInput(new UIFormTextAreaInput(FIELD_NOTE_INPUT, null,""));
    
  }
  
  public String[] getActions() { return new String[] {"Save", "Canel"} ; }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub 
  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub  
  }
  
  static  public class SaveActionListener extends EventListener<UIContactForm> {
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiForm = event.getSource() ;
      
      String selectedCategoryId = uiForm.getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).getValue();
      
      String fullName = uiForm.getUIStringInput(FIELD_FULLNAME_INPUT).getValue();
      String firstName = uiForm.getUIStringInput(FIELD_FIRSTNAME_INPUT).getValue();
      String middleName = uiForm.getUIStringInput(FIELD_MIDDLENAME_INPUT).getValue();
      String lastName = uiForm.getUIStringInput(FIELD_LASTNAME_INPUT).getValue();
      String nickName = uiForm.getUIStringInput(FIELD_NICKNAME_INPUT).getValue();
      String personalTitle = uiForm.getUIStringInput(FIELD_PERSONALTITLE_INPUT).getValue();      
      UIFormMultiValueInputSet emailMultiValueInputSet = uiForm.getChild(UIFormMultiValueInputSet.class);
      List<String> emailList = (List<String>) emailMultiValueInputSet.getValue();
      
      System.out.println("\n\n\n=================>>Id selected: " + selectedCategoryId);
      System.out.println("=================>> Full name : " + fullName);
      for(int i = 0; i < emailList.size(); i++) {       
        System.out.println("=================>> Value at position "+ i + ":" + emailList.get(i));
      }
    
      String exoChat = uiForm.getUIStringInput(FIELD_EXOCHAT_INPUT).getValue();
      String google = uiForm.getUIStringInput(FIELD_GOOGLE_INPUT).getValue();
      String msn = uiForm.getUIStringInput(FIELD_MSN_INPUT).getValue();
      String aolAim = uiForm.getUIStringInput(FIELD_AOLAIM_INPUT).getValue();
      String yahoo = uiForm.getUIStringInput(FIELD_YAHOO_INPUT).getValue();
      String icr = uiForm.getUIStringInput(FIELD_ICR_INPUT).getValue(); 
      String skype = uiForm.getUIStringInput(FIELD_SKYPE_INPUT).getValue();
      String icq = uiForm.getUIStringInput(FIELD_ICQ_INPUT).getValue();
      
      String street = uiForm.getUIStringInput(FIELD_STREET_INPUT).getValue();
      String city = uiForm.getUIStringInput(FIELD_CITY_INPUT).getValue();
      String state = uiForm.getUIStringInput(FIELD_STATE_INPUT).getValue();
      String postalCode = uiForm.getUIStringInput(FIELD_POSTALCODE_INPUT).getValue();
      String country = uiForm.getUIStringInput(FIELD_COUNTRY_INPUT).getValue();
      String homePhone1 = uiForm.getUIStringInput(FIELD_HOMEPHONE1_INPUT).getValue(); 
      String homePhone2 = uiForm.getUIStringInput(FIELD_HOMEPHONE2_INPUT).getValue();
      String homeFax = uiForm.getUIStringInput(FIELD_HOMEFAX_INPUT).getValue();
      String mobilePhone = uiForm.getUIStringInput(FIELD_MOBILEPHONE_INPUT).getValue();
      String webPage = uiForm.getUIStringInput(FIELD_WEBPAGE_INPUT).getValue();
      
      String workStreet = uiForm.getUIStringInput(FIELD_WORKSTREET_INPUT).getValue();
      String workCity = uiForm.getUIStringInput(FIELD_WORKCITY_INPUT).getValue();
      String workState = uiForm.getUIStringInput(FIELD_WORKSTATE_INPUT).getValue();
      String workPostalCode = uiForm.getUIStringInput(FIELD_WORKPOSTALCODE_INPUT).getValue();
      String workCountry = uiForm.getUIStringInput(FIELD_WORKCOUNTRY_INPUT).getValue();
      String workPhone1 = uiForm.getUIStringInput(FIELD_WORKPHONE1_INPUT).getValue(); 
      String workPhone2 = uiForm.getUIStringInput(FIELD_WORKPHONE2_INPUT).getValue();
      String workFax = uiForm.getUIStringInput(FIELD_WORKFAX_INPUT).getValue();
      String workMobilePhone = uiForm.getUIStringInput(FIELD_WORKMOBILEPHONE_INPUT).getValue();
      String workWebPage = uiForm.getUIStringInput(FIELD_WORKWEBPAGE_INPUT).getValue();
      
      String note = uiForm.getChild(UIFormTextAreaInput.class).getValue() ;
      System.out.println("===========> note :" + note + "\n\n\n");
      
      // Begin save contact
      Contact contact = new Contact();
      contact.setId("contact id");
      contact.setLastName(lastName);
      contact.setFirstName(firstName);
      contact.setEmailAddress(emailList.get(0));
      contact.setHomePhone(homePhone1);
      contact.setWorkPhone(workPhone1);
      contact.setHomeAddress(street + "-" + city + "-" + state);
      contact.setCountry(country);
      contact.setPostalCode(postalCode);
      contact.setPersonalSite(webPage);
      contact.setOrganization(workWebPage);
      contact.setJobTitle(personalTitle);
      contact.setCompanyAddress(workStreet + "-" + workCity + "-" + workState);
      contact.setCompanySite(workWebPage);
      contact.setCategories(new String[] { selectedCategoryId });
      
      ContactService contactService = 
        (ContactService)PortalContainer.getInstance().getComponentInstanceOfType(ContactService.class) ;
      contactService.saveContact("exo", contact, true);
      
      System.out.println(contactService.getContact("exo", contact.getId()).getFirstName());
      
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;      
    }
  }
  
  
  static  public class AddCategoryActionListener extends EventListener<UIContactForm> {
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiForm = event.getSource() ;
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.activate(UICategoryForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;      
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIContactForm> {
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;      
    }
  }  
}
