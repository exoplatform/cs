/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.cs.ext.impl;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.webservice.cs.calendar.CalendarWebservice;
import org.exoplatform.webui.application.portlet.PortletRequestContext;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 7, 2010  
 */
public class CalendarDataInitialize extends SpaceListenerPlugin {

  private static final Log log = ExoLogger.getLogger(CalendarDataInitialize.class);
  public static final String ANY = "*.*".intern();
  public static final String SLASH_COLON = "/:".intern() ;
  public static final String SLASH = "/".intern();
  public static final String COLON = ":".intern() ;
  public static final String SPLITER = "://".intern();
  public static final String PUBLIC_TYPE = "2".intern();
  
  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    try {
      Space space = event.getSpace();
      
      PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
      String baseUrl = portletRequestContext.getRequest().getScheme() + SPLITER + 
      portletRequestContext.getRequest().getServerName() + COLON +
      String.format("%s",portletRequestContext.getRequest().getServerPort()) 
      + SLASH ;
      CalendarService calService = (CalendarService) PortalContainer.getInstance().getComponentInstanceOfType(CalendarService.class);
      String username = Util.getPortalRequestContext().getRemoteUser() ; 
      Calendar calendar = new Calendar() ;
      calendar.setPublic(false) ;
      calendar.setGroups((new String[]{space.getGroupId()}));
      calendar.setName(space.getName()) ;
      String url =  baseUrl + PortalContainer.getCurrentPortalContainerName() +SLASH+ 
      PortalContainer.getCurrentRestContextName() + CalendarWebservice.BASE_URL_PRIVATE + username +SLASH+
      calendar.getId() +SLASH+ PUBLIC_TYPE;
      calendar.setPrivateUrl(url);
      calendar.setEditPermission(new String[]{space.getGroupId()+ SLASH_COLON + ANY}) ;
      //calendar.setCalendarColor() ;
      calendar.setCalendarOwner(username) ;
      calService.savePublicCalendar(calendar, true, username);
      
    }catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

}
