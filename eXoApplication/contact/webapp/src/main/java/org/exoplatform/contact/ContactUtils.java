/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.RepositoryException;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.popup.PermissionData;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.contact.webui.popup.UISelectComponent;
import org.exoplatform.contact.webui.popup.UISelectPermissionsForm;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.organization.account.UIGroupSelector;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Hoang Quang
 *          hung.hoang@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactUtils {
  private static final Log log = ExoLogger.getExoLogger(ContactUtils.class);
  
  //private static String AKONG = "@" ;
  final public static String COMMA = ",".intern() ;
  final public static String SEMI_COMMA = ";".intern() ;
  public static final String HTTP = "http://" ; 
  public static String[] specialString = {"!", "#", "%", "&"
                                            , ":", ">", "<", "~", "`", "]", "'", "/", "-"} ;
//can't use String.replaceAll() ;
  public static String[] specialString2 = {"?", "[", "(", "|", ")", "*", "\\", "+", "}", "{", "^", "$", "\""
    ,"!", "#", "%", "&", ":", ">", "<", "~", "`", "]", "'", "/", "-"} ;
  final static public String FIELD_USER = "user".intern() ;
  final static public String FIELD_GROUP = "group".intern() ;  
  final static public String FIELD_EDIT_PERMISSION = "canEdit".intern() ;
  final static public String NAME = "fileName".intern() ;
  final static public String TYPE = "type".intern() ;
  public static final int DEFAULT_VALUE_UPLOAD_PORTAL = -1;
  
  public static String getDisplayAdddressShared(String sharedUserId, String addressName) {
    return sharedUserId + " - " + addressName ;
  }
  /*
  public static boolean isNameLong(String text) {
    if (text == null) return false ;
    if (text.length() > 40) return true ;
    return false ;
  }
  */
  public static String encodeJCRText(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").
    replaceAll("'", "&apos;").replaceAll("\"", "&quot;") ;
  }
  
  public static boolean isNameValid(String name, String[] regex) {
    if (isEmpty(name)) return true ;
    for(String c : regex){ if(name.contains(c)) return false ;}
    return true ;
  }
  
  // add
  public static String encodeHTML(String str) {
    if (str == null) return "" ;
    return str.replaceAll("<", "&lt;").replaceAll(">", "&gt;") ;
    
    /*return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").
      replaceAll("'", "&apos;").replaceAll("\"", "&quot;") ;*/
  }
//not use
  /*public static String filterString(String text, boolean isEmail) {
    if (text == null || text.trim().length() == 0) return "" ;
    for (String str : specialString) {
      text = text.replaceAll(str, "") ;
    }
    if (!isEmail) text = text.replaceAll(AKONG, "") ;
    int i = 0 ;
    while (i < text.length()) {
      if (Arrays.asList(specialString2).contains(text.charAt(i) + "")) {
        text = text.replace((text.charAt(i)) + "", "") ;
      } else {
        i ++ ;
      }
    }
    return text ;
  }*/
  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }
  
  static public ContactService getContactService() throws Exception {
    return (ContactService)PortalContainer.getComponent(ContactService.class) ;
  }
  
  public static boolean isEmpty(String s) {
    if (s == null || s.trim().length() == 0) return true ;
    return false ;    
  }
  
  public static List<String> getUserGroups() throws Exception {
    Identity identity = ConversationState.getCurrent().getIdentity();
    List<String> groupIds = new ArrayList<String>(identity.getGroups());
    return groupIds;
  }
  
  public static String getPublicGroupName(String groupId) throws Exception {
    OrganizationService organizationService = 
      (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    return organizationService.getGroupHandler().findGroupById(groupId).getGroupName() ;
  }
  
  public static boolean isPublicGroup(String groupId) throws Exception {
    if (getUserGroups().contains(groupId)) return true ;
    return false ;
  }
  
  public static String formatDate(String format, Date date) {
    Format formatter = new SimpleDateFormat(format);
    return formatter.format(date);
  }
  
  static public class SelectComparator implements Comparator{
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((SelectItemOption) o1).getLabel() ;
      String name2 = ((SelectItemOption) o2).getLabel() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }
  
  public static List<Account> getAccounts() throws Exception {
    MailService mailSvr = (MailService)PortalContainer.getComponent(MailService.class) ;
    try {
      return mailSvr.getAccounts(getCurrentUser()) ;
    } catch (RepositoryException e) {
      return null ;
    } catch (IndexOutOfBoundsException ex) {
      return null ;
    }
  }
  
  public static String emptyName() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
    try {
        return  res.getString("ContactUtils.label.emptyName");
    } catch (MissingResourceException e) {      
      if (log.isDebugEnabled()) {
        log.debug("MissingResourceException in method emptyName", e);
      }
      return "(empty name)" ;
    }
  }  
 
  static public String getEmailUser(String userName) throws Exception {
    OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
    User user = organizationService.getUserHandler().findUserByName(userName) ;
    String email = user.getEmail() ;
    return email;
  }

  static public String getFullName(String userName) throws Exception {
    OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
    User user = organizationService.getUserHandler().findUserByName(userName) ;
    String fullName = user.getFullName() ;
    return fullName ;
  }
  
  public static void sendMessage(Message message) throws Exception {
    org.exoplatform.services.mail.MailService mService = (org.exoplatform.services.mail.MailService)PortalContainer.getComponent(org.exoplatform.services.mail.MailService.class) ;
    mService.sendMessage(message) ;
  }
  
  public static boolean isCollectedAddressBook(String addressId) {
    return addressId.contains(NewUserListener.ADDRESSESGROUP) ;
  }
  
  public static String reduceSpace(String s) {
    if (isEmpty(s)) return "" ;
    String[] words = s.split(" ") ;
    StringBuilder builder = new StringBuilder() ;
    for (String word : words) {
      if (builder.length() > 0 && word.trim().length() > 0) builder.append(" ") ;
      builder.append(word.trim()) ;
    }
    return builder.toString() ;
  }
  
  public static String listToString(List<String> list) {
    if (list == null || list.size() == 0) return ""; 
    StringBuilder builder = new StringBuilder();
    for (String str : list) {
      if (builder.length() > 0) builder.append("; " + str);
      else builder.append(str);
    }
    return builder.toString();
  }
  
  public static int getAge(Contact contact) {
    if (contact == null) return 0;
    Calendar birthday = new GregorianCalendar() ;
    birthday.setTime(contact.getBirthday());
    Calendar now = new GregorianCalendar() ;
    return now.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
  }
  
  public static Map<String, String> getIMs(Contact contact) {
    Map<String, String> ims = new LinkedHashMap<String, String>();
    if (!isEmpty(contact.getExoId())) ims.put("exoChat", contact.getExoId());
    if (!isEmpty(contact.getGoogleId())) ims.put("google", contact.getGoogleId());
    if (!isEmpty(contact.getMsnId())) ims.put("msn", contact.getMsnId());
    if (!isEmpty(contact.getAolId())) ims.put("aol-aim", contact.getAolId());
    if (!isEmpty(contact.getYahooId())) ims.put("yahoo", contact.getYahooId());
    if (!isEmpty(contact.getIcrId())) ims.put("icr", contact.getIcrId());
    if (!isEmpty(contact.getSkypeId())) ims.put("skype", contact.getSkypeId());
    if (!isEmpty(contact.getIcqId())) ims.put("icq", contact.getIcqId());
    return ims;
  }
  
  public static Map<String, String> getPhones(Contact contact) {
    Map<String, String> phones = new LinkedHashMap<String, String>();
    if (!isEmpty(contact.getWorkPhone1())) phones.put(contact.getWorkPhone1(), "work");
    if (!isEmpty(contact.getWorkPhone2())) phones.put(contact.getWorkPhone2(), "work");
    if (!isEmpty(contact.getHomePhone1())) phones.put(contact.getHomePhone1(), "home");
    if (!isEmpty(contact.getHomePhone2())) phones.put(contact.getHomePhone2(), "home");
    if (!isEmpty(contact.getMobilePhone())) phones.put(contact.getMobilePhone(),"home");
    return phones;
  }
  
  public static boolean isNullArray(String[] array){
    if(array.length < 1) return true;
    return false;
  }
  
  public static UIFormInputWithActions initSelectPermissions(UIFormInputWithActions inputset) throws Exception {
    inputset.addUIFormInput(new UIFormStringInput(FIELD_USER, FIELD_USER, null)) ;
    List<ActionData> actionUser = new ArrayList<ActionData>() ;
    actionUser = new ArrayList<ActionData>() ;
    ActionData selectUserAction = new ActionData() ;
    selectUserAction.setActionListener("SelectPermission") ;
    selectUserAction.setActionName("SelectUser") ;    
    selectUserAction.setActionType(ActionData.TYPE_ICON) ;
    selectUserAction.setCssIconClass("SelectUserIcon") ;
    selectUserAction.setActionParameter(UISelectComponent.TYPE_USER) ;
    actionUser.add(selectUserAction) ;
    inputset.setActionField(FIELD_USER, actionUser) ;
    
    UIFormStringInput groupField = new UIFormStringInput(FIELD_GROUP, FIELD_GROUP, null) ;
    inputset.addUIFormInput(groupField) ;
    List<ActionData> actionGroup = new ArrayList<ActionData>() ;
    ActionData selectGroupAction = new ActionData() ;
    selectGroupAction.setActionListener("SelectPermission") ;
    selectGroupAction.setActionName("SelectGroup") ;    
    selectGroupAction.setActionType(ActionData.TYPE_ICON) ;  
    selectGroupAction.setCssIconClass("SelectGroupIcon") ;
    selectGroupAction.setActionParameter(UISelectComponent.TYPE_GROUP) ;
    actionGroup.add(selectGroupAction) ;
    inputset.setActionField(FIELD_GROUP, actionGroup) ;    
    inputset.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_EDIT_PERMISSION, FIELD_EDIT_PERMISSION, null)) ;
    return inputset;
  }
  
  public static void updateSelect(UIFormStringInput fieldInput, String selectField, String value) throws Exception {
    StringBuilder sb = new StringBuilder("") ;
    if (!ContactUtils.isEmpty(fieldInput.getValue())) sb.append(fieldInput.getValue()) ;
    if (sb.indexOf(value) == -1) {
      if (sb.length() == 0) sb.append(value) ;
      else sb.append("," + value) ;
      fieldInput.setValue(sb.toString()) ;
    }
  }
  
  public static AddressBook setViewPermissionAddress(AddressBook contactGroup, Map<String, String> receiveUsers, Map<String, String> receiveGroups) {
    String[] viewPerUsers = contactGroup.getViewPermissionUsers() ;
    Map<String, String> viewMapUsers = new LinkedHashMap<String, String>() ; 
    if (viewPerUsers != null)
      for (String view : viewPerUsers) viewMapUsers.put(view, view) ; 
    for (String user : receiveUsers.keySet()) viewMapUsers.put(user, user) ;
    contactGroup.setViewPermissionUsers(viewMapUsers.keySet().toArray(new String[] {})) ;
    
    String[] viewPerGroups = contactGroup.getViewPermissionGroups() ;
    Map<String, String> viewMapGroups = new LinkedHashMap<String, String>() ; 
    if (viewPerGroups != null)
      for (String view : viewPerGroups) viewMapGroups.put(view, view) ; 
    for (String user : receiveGroups.keySet()) viewMapGroups.put(user, user) ;
    contactGroup.setViewPermissionGroups(viewMapGroups.keySet().toArray(new String[] {})) ;
    return contactGroup;
  }
  
  public static AddressBook removeEditPermissionAddress(AddressBook contactGroup, Map<String, String> receiveUsers, Map<String, String> receiveGroups) {
    List<String> newPerUsers = new ArrayList<String>() ; 
    if (contactGroup.getEditPermissionUsers() != null)
      for (String edit : contactGroup.getEditPermissionUsers())
        if(!receiveUsers.keySet().contains(edit)) {
          newPerUsers.add(edit) ;
        }
    contactGroup.setEditPermissionUsers(newPerUsers.toArray(new String[newPerUsers.size()])) ;
    
    List<String> newPerGroups = new ArrayList<String>() ; 
    if (contactGroup.getEditPermissionGroups() != null)
      for (String edit : contactGroup.getEditPermissionGroups())
        if(!receiveGroups.keySet().contains(edit)) {
          newPerGroups.add(edit) ;
        }
    contactGroup.setEditPermissionGroups(newPerGroups.toArray(new String[newPerGroups.size()])) ; 
    return contactGroup;
  }
  
  public static AddressBook setEditPermissionAddress(AddressBook contactGroup, Map<String, String> receiveUsers, Map<String, String> receiveGroups) {
    String[] editPerUsers = contactGroup.getEditPermissionUsers() ;
    Map<String, String> editMapUsers = new LinkedHashMap<String, String>() ; 
    if (editPerUsers != null)
      for (String edit : editPerUsers) editMapUsers.put(edit, edit) ;
    for (String user : receiveUsers.keySet()) editMapUsers.put(user, user) ;
    contactGroup.setEditPermissionUsers(editMapUsers.keySet().toArray(new String[] {})) ;
    
    String[] editPerGroups = contactGroup.getEditPermissionGroups() ;
    Map<String, String> editMapGroups = new LinkedHashMap<String, String>() ; 
    if (editPerGroups != null)
      for (String edit : editPerGroups) editMapGroups.put(edit, edit) ;
    for (String group : receiveGroups.keySet()) editMapGroups.put(group, group) ;
    contactGroup.setEditPermissionGroups(editMapGroups.keySet().toArray(new String[] {})) ;
    return contactGroup;
  }
  
  public static void selectPermissions(UIForm uiForm, Event<?> event) throws Exception {
    String permType = event.getRequestContext().getRequestParameter(UIForm.OBJECTID) ;
    UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
    uiContainer.removeChild(UIPopupWindow.class) ;
    uiForm.removeChild(UIPopupWindow.class) ;
    if (permType.equals(UISelectComponent.TYPE_USER)) {
      UIPopupWindow uiPopupWindow = uiContainer.getChild(UIPopupWindow.class) ;        
      if (uiPopupWindow == null) {
        uiPopupWindow = uiContainer.addChild(UIPopupWindow.class, "UIPopupWindowUserSelect", "UIPopupWindowUserSelect") ;
      }
      UIUserSelector uiUserSelector = uiContainer.createUIComponent(UIUserSelector.class, null, null) ;
      uiUserSelector.setShowSearch(true);
      uiUserSelector.setShowSearchUser(true) ;
      uiUserSelector.setShowSearchGroup(true);

      uiPopupWindow.setUIComponent(uiUserSelector);
      uiPopupWindow.setShow(true);
      uiPopupWindow.setWindowSize(740, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;        
    } else {
      UIPopupWindow uiPopup = uiForm.addChild(UIPopupWindow.class, null, "UIPopupGroupSelector");
      uiPopup.setWindowSize(540, 0);
      UIGroupSelector uiGroup = uiForm.createUIComponent(UIGroupSelector.class, null, null);
      uiPopup.setUIComponent(uiGroup);
      uiGroup.setId("UIGroupSelector");
      uiGroup.getChild(UITree.class).setId("TreeGroupSelector");
      uiGroup.getChild(UIBreadcumbs.class).setId("BreadcumbsGroupSelector");
      uiForm.getChild(UIPopupWindow.class).setShow(true);  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ; 
    }
  }
 
  public static void updateGroupGrid(UIContainer uiContainer, AddressBook group) throws Exception {
    List<PermissionData> dataRow = new ArrayList<PermissionData>() ;
    if(group.getViewPermissionUsers() != null) {
      for(String username : group.getViewPermissionUsers() ) {
        dataRow.add(new PermissionData(username, (group.getEditPermissionUsers()!= null && Arrays.asList(group.getEditPermissionUsers()).contains(username)))) ;
      }
    }
    if(group.getViewPermissionGroups() != null) {
      for(String groupId : group.getViewPermissionGroups() ) {
        dataRow.add(new PermissionData(groupId, (group.getEditPermissionGroups()!= null && Arrays.asList(group.getEditPermissionGroups()).contains(groupId)))) ;
      }
    }
    UIGrid permissionList = uiContainer.getChild(UIGrid.class) ;
    int currentPage = 1 ;
    try {
      currentPage = permissionList.getUIPageIterator().getPageList().getCurrentPage() ;
    } catch (NullPointerException e) { }
    LazyPageList<PermissionData> pageList = new LazyPageList<PermissionData>(new ListAccessImpl<PermissionData>(PermissionData.class, dataRow), 10);
    permissionList.getUIPageIterator().setPageList(pageList) ;
    if (currentPage > 1 && currentPage <= permissionList.getUIPageIterator().getAvailablePage()) {
      permissionList.getUIPageIterator().setCurrentPage(currentPage) ;
    }
    UISelectPermissionsForm uiSelectPermissionsForm = uiContainer.getChild(UISelectPermissionsForm.class);
    if (uiSelectPermissionsForm!= null) uiSelectPermissionsForm.setGroup(group) ;
  }
  
  public static void exportData(UIForm uiForm, Event<?> event,OutputStream out) throws Exception {
    String exportFormat = uiForm.getUIFormSelectBox(ContactUtils.TYPE).getValue() ;
    String fileName = uiForm.getUIStringInput(ContactUtils.NAME).getValue() ;
    String contentType = null;
    String extension = null;
    if (exportFormat.equals("x-vcard")) {
      contentType = "text/x-vcard";
      extension = ".vcf";
    }

    ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
    DownloadResource dresource = new InputStreamDownloadResource(is, contentType) ;
    DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class) ;
    if(fileName != null && fileName.length() > 0) {
      if(fileName.length() > 4 && fileName.endsWith(extension) )
        dresource.setDownloadName(fileName);
      else 
        dresource.setDownloadName(fileName + extension);
    }else {
      dresource.setDownloadName("eXoExported.vcf");
    }
    String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;      
    event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');") ;
    uiForm.getAncestorOfType(UIContactPortlet.class).cancelAction() ;
  }
  
  public static int getLimitUploadSize() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    int limitMB = DEFAULT_VALUE_UPLOAD_PORTAL;
    try {
      limitMB = Integer.parseInt(portletPref.getValue("uploadFileSizeLimitMB", "").trim());
    } catch (Exception e) {}
    return limitMB;
  }
  
}

