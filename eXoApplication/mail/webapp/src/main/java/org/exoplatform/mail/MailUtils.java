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
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
  
  
  static public MailService getMailService() throws Exception {
    return (MailService)PortalContainer.getComponent(MailService.class) ;
  }
  
  static public String getCurrentUser() throws Exception { 
    return Util.getPortalRequestContext().getRemoteUser() ; 
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
    String str = "";
    DecimalFormat df = new DecimalFormat("0.00");
    if (size > 1024 * 1024) str += df.format(((double) size)/(1024 * 1024)) + " MB" ;
    else if (size > 1024) str += df.format(((double) size)/(1024)) + " KB" ;
    else str += size + " B" ;
    return str ;
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
    if (s == null || s.length() == 0) return true ;
    return false ;    
  }
  
  public static boolean isChecking(String username, String accountId) throws Exception {
  	ExoContainer container = ExoContainerContext.getCurrentContainer();
		JobSchedulerService schedulerService = 
			(JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
		List allJobs = schedulerService.getAllJobs() ;
		for(Object obj : allJobs) {
			if(((JobDetail)obj).getName().equals(username + ":" + accountId)) return true ; 
		}
  	return false ;
  }
  
  public static String formatDate(String format, Date date) {
    Format formatter = new SimpleDateFormat(format);
    return formatter.format(date);
  }
  
  public static String formatDate(Date date) {
    Calendar systemDate =  new GregorianCalendar() ;
    Calendar cal =  new GregorianCalendar() ;
    cal.setTime(date);
    boolean isSameYear = (systemDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) ;
    boolean isSameMonth = (systemDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH)) ;
    boolean isSameWeek = (isSameMonth && (systemDate.get(Calendar.WEEK_OF_MONTH) == cal.get(Calendar.WEEK_OF_MONTH)));
    boolean isSameDate = (isSameWeek && (systemDate.get(Calendar.DAY_OF_WEEK) == cal.get(Calendar.DAY_OF_WEEK)));
    
    if (isSameYear) 
      if (isSameDate) 
        return (new SimpleDateFormat("HH:mm aaa")).format(date);
      else if (isSameWeek)
        return (new SimpleDateFormat("EEEE")).format(date);
      else if (isSameMonth)
        return (new SimpleDateFormat("EEEE, dd")).format(date);
      else 
        return (new SimpleDateFormat("MMM dd")).format(date);
    else 
      return (new SimpleDateFormat("MMM dd, yyyy")).format(date);
  }
  
  public static String encodeHTML(String htmlContent) throws Exception {
    return htmlContent.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
    .replaceAll("<", "&lt;").replaceAll(">", "&gt;") ;
  }
  
  public static boolean isInvitation(Message msg) throws Exception {
    String inviteHeader = msg.getHeader("X-Exo-Invitation") ;
    if (inviteHeader != null) return true ;
    else return false ;
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
}
