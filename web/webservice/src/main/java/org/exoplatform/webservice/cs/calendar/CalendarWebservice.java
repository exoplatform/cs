/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.webservice.cs.calendar;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;



/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 15, 2009  
 */
public class CalendarWebservice implements ResourceContainer{

  public final static String JSON_CONTENT_TYPE    = "application/json";
  public final static String XML_CONTENT_TYPE    = "text/xml";
  public final static String SERVICE_BASED_URL = "/portal/rest/" ;

  public CalendarWebservice() {}

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/cs/calendar/checkPermission/{username}/{calendarId}/{type}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response checkPermission(@URIParam("username")
                                  String username, @URIParam("calendarId")
                                  String calendarId, @URIParam("type")
                                  String type) throws Exception {
    StringBuffer buffer = new StringBuffer();
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try { 
      CalendarService calService = (CalendarService)ExoContainerContext
      .getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
      Calendar cal = null ;
      buffer.append("{canEdit:0}");
      if(Utils.PRIVATE_TYPE == Integer.parseInt(type)) {
        buffer.append("{canEdit:1}");
      } else if(Utils.PUBLIC_TYPE == Integer.parseInt(type)) {
        OrganizationService oService = (OrganizationService)ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
        cal = calService.getGroupCalendar(calendarId) ;
        if(Utils.canEdit(oService, cal.getEditPermission(), username)) {
          buffer.append("{canEdit:1}");
        } 
      } else if(Utils.SHARED_TYPE == Integer.parseInt(type)) {
        if(calService.getSharedCalendars(username, true) != null) {
          cal = calService.getSharedCalendars(username, true).getCalendarById(calendarId) ;
          if(Utils.canEdit(null, cal.getEditPermission(), username)) {
            buffer.append("{canEdit:1}");
          }  
        } 
      }  
    } catch (Exception e) {
      //e.printStackTrace() ;
      buffer = new StringBuffer("{ERROR:500 " + e + "}") ;
      
    } 
    return Response.Builder.ok(buffer.toString(), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }
}
