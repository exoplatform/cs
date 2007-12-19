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

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.portal.webui.util.Util;


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
  
  static public String getAccountId() throws Exception { 
  	return getMailService().getCurrentAccount(SessionsUtils.getSessionProvider(), getCurrentUser()) ;
  }
  
  static public void setAccountId(String accId) throws Exception { 
  	getMailService().updateCurrentAccount(SessionsUtils.getSessionProvider(), getCurrentUser(), accId) ;
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
  
  public static boolean isFieldEmpty(String s) {
    if (s == null || s.length() == 0) return true ;
    return false ;    
  }
}
