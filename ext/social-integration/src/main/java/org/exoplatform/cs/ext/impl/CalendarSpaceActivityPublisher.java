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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.impl.CalendarEventListener;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 30, 2010  
 */
public class CalendarSpaceActivityPublisher extends CalendarEventListener {
  private Log LOG = ExoLogger.getLogger(CalendarSpaceActivityPublisher.class);
  
  
  public static final String CALENDAR_APP_ID = "cs-calendar:spaces";
  public static final String EVENT_ADDED = "EventAdded".intern();
  public static final String EVENT_UPDATED = "EventUpdated".intern();
  public static final String EVENT_ID_KEY = "EventID".intern();
  public static final String CALENDAR_ID_KEY = "CalendarID".intern();
  public static final String TASK_ADDED = "TaskAdded".intern();
  public static final String TASK_UPDATED = "TaskUpdated".intern();
  public static final String EVENT_TYPE_KEY = "EventType".intern();
  public static final String EVENT_SUMMARY_KEY = "EventSummary".intern();
  public static final String EVENT_TITLE_KEY = "EventTitle".intern();
  public static final String EVENT_DESCRIPTION_KEY = "EventDescription".intern();
  public static final String EVENT_LOCALE_KEY = "EventLocale".intern();
  public static final String EVENT_STARTTIME_KEY = "EventStartTime".intern();
  public static final String EVENT_ENDTIME_KEY = "EventEndTime".intern();
  
  public void savePublicEvent(CalendarEvent event, String calendarId) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager") ;
      
      if (calendarId == null || calendarId.indexOf(CalendarDataInitialize.CALENDAR_ID_PREFIX) < 0) {
        return;
      }
      
//      String msg = "A new event has been added : " + event.getSummary();
//      String body = event.getDescription();
      Activity activity = new Activity();
      
      String eventType = event.getEventType().equalsIgnoreCase(CalendarEvent.TYPE_EVENT) ? EVENT_ADDED
                                                                                        : TASK_ADDED;
      activity.setTitle(event.getSummary());
      activity.setBody(event.getDescription());
      Map<String, String> params = new HashMap<String, String>();
//      params.put(EVENT_TITLE_KEY, );
      params.put(EVENT_TYPE_KEY, eventType);
      params.put(EVENT_ID_KEY, event.getId());
      params.put(CALENDAR_ID_KEY, calendarId);
      params.put(EVENT_SUMMARY_KEY, event.getSummary());
      params.put(EVENT_LOCALE_KEY, event.getLocation() != null ? event.getLocation() : "");
      params.put(EVENT_DESCRIPTION_KEY, event.getDescription() != null ? event.getDescription() : "");
      params.put(EVENT_STARTTIME_KEY, String.valueOf(event.getFromDateTime().getTime()));
      params.put(EVENT_ENDTIME_KEY, String.valueOf(event.getToDateTime().getTime()));
      activity.setType(CALENDAR_APP_ID);
      activity.setTemplateParams(params);
      
      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class); 
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
      String spaceId = calendarId.split(CalendarDataInitialize.CALENDAR_ID_PREFIX)[1]; 
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceId, false);
      activityM.recordActivity(spaceIdentity, activity);
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when event added " +e.getMessage());
    }
    
  }

  public void updatePublicEvent(CalendarEvent event, String calendarId) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
      if (calendarId == null || calendarId.indexOf(CalendarDataInitialize.CALENDAR_ID_PREFIX) < 0) {
        return;
      }

//      String msg = "The following event has been updated: " + event.getSummary();
//      String body = event.getDescription();
      Activity activity = new Activity();
      activity.setTitle(event.getSummary());
      activity.setBody(event.getDescription());
      Map<String, String> params = new HashMap<String, String>();
      
      String eventType = event.getEventType().equalsIgnoreCase(CalendarEvent.TYPE_EVENT) ? EVENT_UPDATED
                                                                                         : TASK_UPDATED;
      // params.put(EVENT_TITLE_KEY, );
      params.put(EVENT_TYPE_KEY, eventType);
      params.put(EVENT_ID_KEY, event.getId());
      params.put(CALENDAR_ID_KEY, calendarId);
      params.put(EVENT_SUMMARY_KEY, event.getSummary());
      params.put(EVENT_LOCALE_KEY, event.getLocation() != null ? event.getLocation() : "");
      params.put(EVENT_DESCRIPTION_KEY, event.getDescription() != null ? event.getDescription() : "");
      params.put(EVENT_STARTTIME_KEY, String.valueOf(event.getFromDateTime().getTime()));
      params.put(EVENT_ENDTIME_KEY, String.valueOf(event.getToDateTime().getTime()));
      activity.setType(CALENDAR_APP_ID);
      activity.setTemplateParams(params);

      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance()
                                                                    .getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                   .getComponentInstanceOfType(ActivityManager.class);
      String spaceId = calendarId.split(CalendarDataInitialize.CALENDAR_ID_PREFIX)[1];
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                              spaceId,
                                                              false);
      activityM.recordActivity(spaceIdentity, activity);
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when event updated " + e.getMessage());
    }

  }

  
}
