/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public class ForumUtils {
  public static String SYSTEM_SUFFIX = ":/" + SystemIdentity.SYSTEM ;
  public static String ANONIM_SUFFIX = ":/" + SystemIdentity.ANONIM ;

  public static boolean isAnonim() {
    String userId = Util.getPortalRequestContext().getRemoteUser() ;
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

}
