/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.model.SelectItemOption;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Hoang Quang
 *          hung.hoang@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactUtils {
  //private static String AKONG = "@" ;
  final public static String COMMA = ",".intern() ;
  final public static String SEMI_COMMA = ";".intern() ;
  public static final String HTTP = "http://" ; 
  public static String[] specialString = {"!", "#", "%", "&"
                                            , ":", ">", "<", "~", "`", "]", "'", "/", "-"} ;
//can't use String.replaceAll() ;
  public static String[] specialString2 = {"?", "[", "(", "|", ")", "*", "\\", "+", "}", "{", "^", "$", "\""
    ,"!", "#", "%", "&", ":", ">", "<", "~", "`", "]", "'", "/", "-"} ;
  
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
    OrganizationService organizationService = 
      (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objGroupIds = organizationService.getGroupHandler().findGroupsOfUser(getCurrentUser()).toArray() ;
    List<String> groupIds = new ArrayList<String>() ;
    for (Object object : objGroupIds) {
      groupIds.add(((Group)object).getId()) ;
    }
    return groupIds ;
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
      e.printStackTrace() ;
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
    if (!isEmpty(contact.getWorkPhone1())) phones.put("workPhone1", contact.getWorkPhone1());
    if (!isEmpty(contact.getWorkPhone2())) phones.put("workPhone2", contact.getWorkPhone2());
    if (!isEmpty(contact.getHomePhone1())) phones.put("homePhone1", contact.getHomePhone1());
    if (!isEmpty(contact.getHomePhone2())) phones.put("homePhone2", contact.getHomePhone2());
    if (!isEmpty(contact.getMobilePhone())) phones.put("mobilePhone", contact.getMobilePhone());
    return phones;
  }
}
