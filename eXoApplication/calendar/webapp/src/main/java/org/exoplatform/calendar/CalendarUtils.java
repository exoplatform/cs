/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UIMonthView;
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
  final public static String SEMICOLON = ";".intern() ;
  final public static String COLON = ",".intern() ;
  
  static public String[] getUserGroups(String username) throws Exception {
    OrganizationService organization = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objs = organization.getGroupHandler().findGroupsOfUser(username).toArray() ;
    String[] groups = new String[objs.length] ;
    for(int i = 0; i < objs.length ; i ++) {
      groups[i] = ((GroupImpl)objs[i]).getId() ;
    }
    return groups ;
  }
  static public String[] getAllGroups() throws Exception {
    OrganizationService organization = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objs = organization.getGroupHandler().getAllGroups().toArray() ;
    String[] groups = new String[objs.length] ;
    for(int i = 0; i < objs.length ; i ++) {
      groups[i] = ((GroupImpl)objs[i]).getId() ;
    }
    return groups ;
  }
  public static boolean isEmpty(String value) {
    return (value == null || value.trim().length() == 0) ;
  }
  
  static public CalendarService getCalendarService() throws Exception {
    return (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
  }
}
