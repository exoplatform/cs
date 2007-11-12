package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContactPreview;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UITags;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIAdvancedSearchForm.SearchActionListener.class),      
      @EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class)
    }
)

public class UIAdvancedSearchForm extends UIForm implements UIPopupComponent {
  public final static String FIELD_TEXT_INPUT = "text" ;
  public static final String FIELD_FULLNAME_INPUT = "fullName";
  public static final String FIELD_FIRSTNAME_INPUT = "firstName";
  public static final String FIELD_MIDDLENAME_INPUT = "middleName";
  public static final String FIELD_LASTNAME_INPUT = "lastName";
  public static final String FIELD_NICKNAME_INPUT = "nickName";
  public static final String FIELD_BIRTHDAY_DATETIME = "birthday" ;
  public static final String FIELD_JOBTITLE_INPUT = "jobTitle";
  public static final String FIELD_EMAIL_INPUT = "preferredEmail" ;
  
  public final static String FIELD_GENDER_BOX = "gender" ;
  public static final String MALE = "Male".intern() ;
  public static final String FEMALE = "Female".intern() ;
  
  public UIAdvancedSearchForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_TEXT_INPUT, FIELD_TEXT_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_FULLNAME_INPUT, FIELD_FULLNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_FIRSTNAME_INPUT, FIELD_FIRSTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_MIDDLENAME_INPUT, FIELD_MIDDLENAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_LASTNAME_INPUT, FIELD_LASTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_NICKNAME_INPUT, FIELD_NICKNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_JOBTITLE_INPUT, FIELD_JOBTITLE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_EMAIL_INPUT, FIELD_EMAIL_INPUT, null)
      .addValidator(EmailAddressValidator.class));
    
    List<SelectItemOption<String>> genders = new ArrayList<SelectItemOption<String>>() ;
    genders.add(new SelectItemOption<String>("", "")) ;
    genders.add(new SelectItemOption<String>(MALE, MALE)) ;
    genders.add(new SelectItemOption<String>(FEMALE, FEMALE)) ;
    addChild(new UIFormSelectBox(FIELD_GENDER_BOX, FIELD_GENDER_BOX, genders)) ;
  }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  static  public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;
      ContactFilter filter = new ContactFilter() ;
      filter.setText(uiAdvancedSearchForm.getUIStringInput(FIELD_TEXT_INPUT).getValue()) ;
      filter.setFullName(uiAdvancedSearchForm.getUIStringInput(FIELD_FULLNAME_INPUT).getValue()) ;
      filter.setFirstName(uiAdvancedSearchForm.getUIStringInput(FIELD_FIRSTNAME_INPUT).getValue()) ;
      filter.setMiddleName(uiAdvancedSearchForm.getUIStringInput(FIELD_MIDDLENAME_INPUT).getValue()) ;
      filter.setLastName(uiAdvancedSearchForm.getUIStringInput(FIELD_LASTNAME_INPUT).getValue()) ;
      filter.setNickName(uiAdvancedSearchForm.getUIStringInput(FIELD_NICKNAME_INPUT).getValue()) ;
      filter.setJobTitle(uiAdvancedSearchForm.getUIStringInput(FIELD_JOBTITLE_INPUT).getValue()) ;
      filter.setEmailAddress(uiAdvancedSearchForm.getUIStringInput(FIELD_EMAIL_INPUT).getValue()) ;
      filter.setGender(uiAdvancedSearchForm.getUIFormSelectBox(FIELD_GENDER_BOX).getValue()) ;
      
      DataPageList resultPageList = 
        ContactUtils.getContactService().searchContact(ContactUtils.getCurrentUser(), filter) ;
      UIContactPortlet uiContactPortlet = uiAdvancedSearchForm.getAncestorOfType(UIContactPortlet.class) ;
      uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class).setSelectedGroup(null) ;
      uiContactPortlet.findFirstComponentOfType(UITags.class).setSelectedTag(null) ;      
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.setContacts(resultPageList) ; 
      uiContacts.setViewContactsList(true) ;
      uiContacts.setDisplaySearchResult(true) ;
      uiContacts.setSelectedContact(null) ; /// ?
      uiContactPortlet.findFirstComponentOfType(UIContactPreview.class).setContact(null) ;
      event.getRequestContext()
        .addUIComponentToUpdateByAjax(uiContactPortlet.getChild(UIWorkingContainer.class)) ;
      uiContactPortlet.cancelAction() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiAdvancedSearchForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
    } 
  }
  
}