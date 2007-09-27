/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact;

import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactUtils {
  
  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }
  
  static public ContactService getContactService() throws Exception {
    return (ContactService)PortalContainer.getComponent(ContactService.class) ;
  }
  
  public static boolean IsEmpty(String s) {
    return (s == null) || s.equalsIgnoreCase("null") || s.equalsIgnoreCase("");     
  }
}
