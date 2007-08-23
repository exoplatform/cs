/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarUtils {
  static public String[] getUserGroups(String username) throws Exception {
    OrganizationService organization = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objs = organization.getGroupHandler().findGroupsOfUser(username).toArray() ;
    String[] groups = new String[objs.length] ;
    for(int i = 0; i < objs.length ; i ++) {
      groups[i] = ((GroupImpl)objs[i]).getId() ;
    }
    return groups ;
  }
}
