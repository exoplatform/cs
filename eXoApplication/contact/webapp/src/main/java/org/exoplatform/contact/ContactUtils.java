/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.webui.core.model.SelectItemOption;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactUtils {
  private static String AKONG = "@" ;
  
  public static String[] specialString = {"!", "#", "$", "%", "^", "&"
                                            , ":", ">", "<", "~", "`", "]", "'", "/"} ;
  public static String filterString(String text, boolean isEmail) {

    for (String str : specialString) {
      text = text.replaceAll(str, "") ;
    }
    if (!isEmail) text = text.replaceAll(AKONG, "") ;
    int i = 0 ;
    while (i < text.length()) {
      if (text.charAt(i) == '?' || text.charAt(i) == '[' || text.charAt(i) == '(' || text.charAt(i) == '|'
        || text.charAt(i) == ')' || text.charAt(i) == '*' || text.charAt(i) == '\\' || text.charAt(i) == '+'
        || text.charAt(i) == '}' || text.charAt(i) == '{' || text.charAt(i) == '^' || text.charAt(i) == '$'
        || text.charAt(i) == '"'  ) {
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
  
  public static String getImageSource(Contact contact, DownloadService dservice) throws Exception {    
    ContactAttachment contactAttachment = contact.getAttachment();
    if (contactAttachment != null) {
      try {
        InputStream input = contactAttachment.getInputStream() ;
        byte[] imageBytes = null ;
        if (input != null) {
          imageBytes = new byte[input.available()] ;
          input.read(imageBytes) ;
          ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
          InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image") ;
          dresource.setDownloadName(contactAttachment.getFileName()) ;
          return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;        
        }
      } catch (PathNotFoundException ex) {
        return null ;
      }
    }
    return null ;
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
      return mailSvr.getAccounts(SessionsUtils.getSessionProvider(), getCurrentUser()) ;
    } catch (RepositoryException e) {
      return null ;
    } catch (IndexOutOfBoundsException ex) {
      return null ;
    }
   }
  
}
