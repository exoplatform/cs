/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.jcr.RepositoryException;
import java.net.UnknownHostException;

import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.model.SelectItemOption;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Hoang Quang
 *          hung.hoang@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactUtils {
  private static String AKONG = "@" ;
  final public static String COMMA = ",".intern() ;
  public static final String HTTP = "http://" ; 
  public static String[] specialString = {"!", "#", "%", "&"
                                            , ":", ">", "<", "~", "`", "]", "'", "/", "-"} ;
//can't use String.replaceAll() ;
  public static String[] specialString2 = {"?", "[", "(", "|", ")", "*", "\\", "+", "}", "{", "^", "$", "\""
    ,"!", "#", "%", "&", ":", ">", "<", "~", "`", "]", "'", "/", "-"} ;
  
  public static String getDisplayAdddressShared(String sharedUserId, String addressName) {
    return sharedUserId + " - " + addressName ;
  }
  
  public static boolean isNameLong(String text) {
    if (text == null) return false ;
    if (text.length() > 40) return true ;
    return false ;
  }
  
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
  public static String filterString(String text, boolean isEmail) {
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
  }
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
      groupIds.add(((GroupImpl)object).getId()) ;
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
      return mailSvr.getAccounts(SessionProviderFactory.createSessionProvider(), getCurrentUser()) ;
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
  
  
  
  
}
