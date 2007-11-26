/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.core.model.SelectItemOption;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class MailUtils {
 
  final public static String SEMICOLON = ";".intern() ;
  final public static String COLON = ",".intern() ;
  final public static String UNDERSCORE = "_".intern() ;
  final public static String TIMEFORMAT  = "HH:mm".intern() ;
  final public static String DATEFORMAT = "MM/dd/yyyy".intern() ;
  final public static String DATETIMEFORMAT = DATEFORMAT + " " +TIMEFORMAT ;   
  
  private static String selectedAccountId_;
  
  static public MailService getMailService() throws Exception {
    return (MailService)PortalContainer.getComponent(MailService.class) ;
  }
  
  static public String getCurrentUser() throws Exception { 
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }
  
  static public String getAccountId() throws Exception { 
    return selectedAccountId_ ;
  }
  
  static public void setAccountId(String accId) throws Exception { 
    selectedAccountId_ = accId ;
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
  
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    GregorianCalendar cal = new GregorianCalendar(Locale.US) ;
    cal.set(java.util.Calendar.AM_PM, java.util.Calendar.AM) ;
    cal.set(java.util.Calendar.HOUR, 0) ;
    cal.set(java.util.Calendar.MINUTE, 0) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    DateFormat df2 = new SimpleDateFormat(TIMEFORMAT) ;
    int time = 0 ;
    while (time ++ < 24*60/(15)) {
      options.add(new SelectItemOption<String>(df.format(cal.getTime()), df2.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, 15) ;
    }
    return options ;
  }
  
  public static boolean isFieldEmpty(String s) {
    if (s == null || s.length() == 0) return true ;
    return false ;    
  }
}
