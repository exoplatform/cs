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
package org.exoplatform.mail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.JobDetail;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class MailUtils { 
  final public static String SEMICOLON = ";".intern() ;
  final public static String COMMA = ",".intern() ;
  final public static String SLASH = "/".intern() ;
  final public static String BACKSLASH = "\\".intern() ;
  public static final String GREATER_THAN = ">".intern() ;
  public static final String SMALLER_THAN = "<".intern() ;
  final public static String SPECIALCHARACTER[] = {SEMICOLON,COMMA,SLASH,BACKSLASH,"'","|",">","<","\"", "?", "!", "@", "#", "$", "%","^","&","*"} ;
  final public static String SUPPORTED_VIEW_TYPE_ATTACH[] = {"gif", "png", "jpg", "jpec", "bmp"} ;
  final public static String SIMPLECHARACTER[] = {GREATER_THAN, SMALLER_THAN, "'", "\""};
  
  static public MailService getMailService() throws Exception {
    return (MailService)PortalContainer.getComponent(MailService.class) ;
  }
  
  static public String getCurrentUser() throws Exception { 
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }
  
  public static boolean isNameValid(String name, String[] regex) {
    for(String c : regex){ if(name.contains(c)) return false ;}
    return true ;
  }
  
  public static String getImageSource(Contact contact, DownloadService dservice) throws Exception {    
    ContactAttachment contactAttachment = contact.getAttachment();
    if (contactAttachment != null) {
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
    }
    return null ;
  }
  
  static public OrganizationService getOrganizationService() throws Exception {
    return (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
  }
  
  public static String encodeJCRText(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").
    replaceAll("'", "&apos;").replaceAll("\"", "&quot;") ;
  }
  
  public static String convertSize(long size) throws Exception {
    return Utils.convertSize(size);
  }
  
  public static String getImageSource(Attachment attach, DownloadService dservice) throws Exception {      
    if (attach != null) {
      InputStream input = attach.getInputStream() ;
      byte[] imageBytes = null ;
      if (input != null) {
        imageBytes = new byte[input.available()] ;
        input.read(imageBytes) ;
        ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
        InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image") ;
        dresource.setDownloadName(attach.getName()) ;
        return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;        
      }
    }
    return null ;
  }
  
  public static boolean isFieldEmpty(String s) {
    return (s == null || s.trim().length() == 0);    
  }
  
//  public static boolean isChecking(String username, String accountId) throws Exception {
//    try {
//      ExoContainer container = ExoContainerContext.getCurrentContainer();
//      JobSchedulerService schedulerService = 
//        (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
//      List allJobs = schedulerService.getAllJobs() ;
//      for(Object obj : allJobs) {
//        if(((JobDetail)obj).getName().equals(username + ":" + accountId)) return true ; 
//      }
//    } catch(Exception e) { }
//    
//    return false ;
//  }
  
  public static String formatDate(String format, Date date, Locale locale) {
    Format formatter = new SimpleDateFormat(format, locale);
    return formatter.format(date);
  }
  
  public static String formatDate(Date date, Locale locale) {
    Calendar systemDate =  new GregorianCalendar() ;
    Calendar cal =  new GregorianCalendar() ;
    cal.setTime(date);
    boolean isSameYear = (systemDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) ;
    boolean isSameMonth = (systemDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH)) ;
    boolean isSameWeek = (isSameMonth && (systemDate.get(Calendar.WEEK_OF_MONTH) == cal.get(Calendar.WEEK_OF_MONTH)));
    boolean isSameDate = (isSameWeek && (systemDate.get(Calendar.DAY_OF_WEEK) == cal.get(Calendar.DAY_OF_WEEK)));
    if (isSameYear) 
      if (isSameDate) 
        return (new SimpleDateFormat("HH:mm aaa", locale)).format(date);
      else if (isSameWeek)
        return (new SimpleDateFormat("EEEE", locale)).format(date);
      else if (isSameMonth)
        return (new SimpleDateFormat("EEEE, dd", locale)).format(date);
      else 
        return (new SimpleDateFormat("MMM dd", locale)).format(date);
    else 
      return (new SimpleDateFormat("MMM dd, yyyy", locale)).format(date);
  }
  
  public static String encodeHTML(String htmlContent) throws Exception {
    return (!isFieldEmpty(htmlContent)) ? htmlContent.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
        .replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&#39;") : "" ;
  }
  
  public static String decodeHTML(String htmlContent) throws Exception {
    return (!isFieldEmpty(htmlContent)) ? htmlContent.replaceAll("&amp;", "&").replaceAll( "&quot;", "\"")
        .replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("&#39;", "'") : "" ;
  }
  
  public static boolean isInvitation(Message msg) throws Exception {
    return (msg.getHeader("X-Exo-Invitation") != null) ;
  }
  
  public static String getEventFrom(Message msg) throws Exception {
    String from = null;
    if (isInvitation(msg)) {
      from = msg.getHeader("X-Exo-Invitation").split(";")[0].trim() ;
    }
    return from ;
  }
  
  public static String getEventTo(Message msg) throws Exception {
    String to = null;
    if (isInvitation(msg)) {
      to = msg.getHeader("X-Exo-Invitation").split(";")[1].trim() ;
    }
    return to ;
  }
  
  public static String getEventType(Message msg) throws Exception {
    String eventType = null;
    if (isInvitation(msg)) {
      eventType = msg.getHeader("X-Exo-Invitation").split(";")[2].trim() ;
    }
    return eventType ;
  }
  
  public static String getCalendarId(Message msg) throws Exception {
    String calId = null;
    if (isInvitation(msg)) {
      calId = msg.getHeader("X-Exo-Invitation").split(";")[3].trim() ;
    }
    return calId ;
  }
  
  public static String getCalendarEventId(Message msg) throws Exception {
    String calEvenId = null;
    if (isInvitation(msg)) {
      calEvenId = msg.getHeader("X-Exo-Invitation").split(";")[4].trim() ;
    }
    return calEvenId ;
  }
  
  public static boolean isValidEmailAddresses(String addressList) throws Exception {
    if (isFieldEmpty(addressList))  return true ;
    boolean isInvalid = true ;
    try {
      InternetAddress[] iAdds = InternetAddress.parse(addressList, true);
      
      String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}" ;
      for (int i = 0 ; i < iAdds.length; i ++) {
        if(!iAdds[i].getAddress().toString().matches(emailRegex)) isInvalid = false;
      }
    } catch(AddressException e) {
      return false ;
    }
    return isInvalid ;
  }
  
  public static String html2string(String str) throws Exception {
    if (str != null) {
      str = str.replaceAll("<[^>]*>", "");
      str = str.replaceAll("&nbsp;", "");
      str = str.replaceAll("&quot;", "\"");
      str = str.replaceAll("\n", "");
    } else {
      str = "" ;
    }
    return str;
  }
  
  //TODO : need to improve later
  public static String html2text(String str) throws Exception {
    if (str != null) {
      str = str.replaceAll("<br*/?>", "\n");
      str = str.replaceAll("<[^>]*>", "");
      str = str.replaceAll("&nbsp;", "");
      str = str.replaceAll("&quot;", "\"");
    } else {
      str = "" ;
    }
    return str;
  }
  
  //TODO : need to improve later
  public static String text2html(String str) throws Exception {
    if (str != null) {
      str = str.replaceAll("\n", "<br />");
    } else {
      str = "" ;
    }
    return str;
  }
  
  public static String convertTextToHtmlLink(String s) throws Exception {
    if (isFieldEmpty(s)) return "" ;
    s = decodeHTML(s);
    // for external link with form http:// , https://, ftp://
    s = s.replaceAll("(\r?\n?)(https?|ftp)", "<br /> $2");
    s = s.replaceAll("([^((href|src)=\")])(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "<a target=\"_blank\" href=\"$0\"> $0 </a>") ;
    // for email 
    s = s.replaceAll("(\\s)([_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5})", "$1<a target=\"_blank\" href=\"mailto:$2\"> $2 </a>") ;
    return s ;
  }
  
  public static String insertTargetToHtmlLink(String s) throws Exception {
    if (isFieldEmpty(s)) return "" ;
    s = decodeHTML(s);
    // for a tag we insert "target=_blank" to open in other window or tab
    s = s.replaceAll("<(A|a)(.*?)>(.*?)</(A|a)>", "<a $2 target=\"_blank\"> $3 </a>") ;
    return s ;
  }
}
