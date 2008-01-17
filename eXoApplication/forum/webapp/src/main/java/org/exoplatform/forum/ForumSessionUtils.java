/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;

public class ForumSessionUtils {
  public static String SYSTEM_SUFFIX = ":/" + SystemIdentity.SYSTEM ;
  public static String ANONIM_SUFFIX = ":/" + SystemIdentity.ANONIM ;

  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }
  
  public static boolean isAnonim() throws Exception {
    String userId = getCurrentUser() ;
    if(userId == null) return true ;   
    return false ;
  }
  
  public static SessionProvider getSystemProvider() {   
    String key = Util.getPortalRequestContext().getSessionId() + SYSTEM_SUFFIX;
    return getJcrSessionProvider(key) ;
  }    

  public static SessionProvider getSessionProvider() {    
    String key = Util.getPortalRequestContext().getSessionId();
    return getJcrSessionProvider(key) ;
  }
  
  public static SessionProvider getAnonimProvider() {
    String key = Util.getPortalRequestContext().getSessionId() + ANONIM_SUFFIX ;
    return getJcrSessionProvider(key) ;
  } 

  private static SessionProvider getJcrSessionProvider(String key) {    
    SessionProviderService service = 
      (SessionProviderService)PortalContainer.getComponent(SessionProviderService.class) ;    
    SessionProvider sessionProvider = null ;    
    try{
      sessionProvider = service.getSessionProvider(key) ;
      return sessionProvider ;
    }catch (NullPointerException e) {
      if(key.indexOf(SYSTEM_SUFFIX)>0) {
        sessionProvider = SessionProvider.createSystemProvider() ;
        service.setSessionProvider(key,sessionProvider) ;
        return sessionProvider ;
      }else if(key.indexOf(ANONIM_SUFFIX)>0) {
        sessionProvider = SessionProvider.createAnonimProvider() ;
        service.setSessionProvider(key,sessionProvider) ;
        return sessionProvider ;
      }else {
        sessionProvider = new SessionProvider(null) ;
        service.setSessionProvider(key,sessionProvider) ;
        return sessionProvider ;
      }
    }   
  }
  
  public static String getFileSource(ForumAttachment attachment, DownloadService dservice) throws Exception {    
    if (attachment != null) {
      try {
        InputStream input = attachment.getInputStream() ;
        byte[] imageBytes = null ;
        if (input != null) {
          imageBytes = new byte[input.available()] ;
          input.read(imageBytes) ;
          ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
          InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image") ;
          dresource.setDownloadName(attachment.getName()) ;
          return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;        
        }
      } catch (PathNotFoundException ex) {
        return null ;
      }
    }
    return null ;
  }
  
  public static String[] getUserGroups() throws Exception{
    OrganizationService organizationService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objGroupIds = organizationService.getGroupHandler().findGroupsOfUser(getCurrentUser()).toArray() ;
    String[] groupIds = new String[objGroupIds.length];
    for (int i = 0; i < groupIds.length; i++) {
      groupIds[i] = ((GroupImpl)objGroupIds[i]).getId() ;
    }
    return groupIds ;
  }
}
