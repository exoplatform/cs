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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.chain.web.WebContext;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 15, 2009  
 */



@Path("/cs/calendar")
public class CalendarWebservice implements ResourceContainer{
  public final static String BASE_URL = "/portal/rest".intern();
  public final static String BASE_RSS_URL = "/cs/calendar/rss".intern();
  
  public CalendarWebservice() {}

  protected void start() {
    PortalContainer manager = PortalContainer.getInstance() ;
    OrganizationService oService = (OrganizationService)ExoContainerContext
    .getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    ((ComponentRequestLifecycle)oService).startRequest(manager);
  }
  protected void stop() {
    PortalContainer manager = PortalContainer.getInstance() ;
    OrganizationService oService = (OrganizationService)ExoContainerContext
    .getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    ((ComponentRequestLifecycle)oService).endRequest(manager);
  }
  
  /**
   * 
   * @param username : user id
   * @param calendarId : given calendar id
   * @param type : calendar type private, public or share
   * @return json data value
   * @throws Exception
   */
  @GET
  @Path("/checkPermission/{username}/{calendarId}/{type}/")
  public Response checkPermission(@PathParam("username")
                                  String username, @PathParam("calendarId")
                                  String calendarId, @PathParam("type")
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
        buffer = new StringBuffer("{canEdit:1}");
      } else if(Utils.PUBLIC_TYPE == Integer.parseInt(type)) {
        start();
        OrganizationService oService = (OrganizationService)ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
        cal = calService.getGroupCalendar(calendarId) ;
        if(Utils.canEdit(oService, cal.getEditPermission(), username)) {
          buffer = new StringBuffer("{canEdit:1}");
        } 
        stop();
      } else if(Utils.SHARED_TYPE == Integer.parseInt(type)) {
        if(calService.getSharedCalendars(username, true) != null) {
          cal = calService.getSharedCalendars(username, true).getCalendarById(calendarId) ;
          if(Utils.canEdit(null, cal.getEditPermission(), username)) {
            buffer = new StringBuffer("{canEdit:1}");
          }  
        } 
      }  
    } catch (Exception e) {
      //e.printStackTrace() ;
      buffer = new StringBuffer("{ERROR:500 " + e + "}") ;
    } 
    return Response.ok(buffer.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }


  /**
   * 
   * @param username : requested user name
   * @param calendarId : calendar id from system
   * @param type : calendar type
   * @return : Rss feeds
   * @throws Exception
   */
  @GET
  @Path("/rss/{username}/{calendarId}/{type}/")
  public Response rss(@PathParam("username")
                      String username, @PathParam("calendarId")
                      String calendarId, @PathParam("type")
                      String type) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {

      CalendarService calService = (CalendarService)ExoContainerContext
      .getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
      Calendar cal = null ;
      switch (Integer.parseInt(type)) {
      case Utils.PRIVATE_TYPE: cal = calService.getUserCalendar(username, calendarId);
      break;
      case Utils.PUBLIC_TYPE: cal = calService.getGroupCalendar(calendarId);
      break;
      case Utils.SHARED_TYPE: cal = calService.getSharedCalendars(username, true).getCalendarById(calendarId);
      default:break;
      }

      if(cal == null) {
        return Response.status(HTTPStatus.NOT_FOUND).entity("Calendar " + calendarId + "is removed").cacheControl(cacheControl).build();
      } 
      if(cal.getPublicUrl() == null || cal.getPublicUrl().length()==0) {
        return Response.status(HTTPStatus.NO_CONTENT).entity("Calendar " + calendarId + "is not public rss").cacheControl(cacheControl).build();
      }

      List<CalendarEvent> events = calService.getUserEventByCalendar(username, Arrays.asList(calendarId));
      return Response.ok(makeRss(username, cal, events), MediaType.APPLICATION_XML).cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  /**
   * 
   * @param auhtor : the feed create 
   * @param cal : calendar object data
   * @param events : list of event from data
   * @return
   * @throws Exception
   */
  private String makeRss(String auhtor, Calendar cal, List<CalendarEvent> events) throws Exception{

    SyndFeed feed = new SyndFeedImpl();      
    feed.setFeedType("rss_2.0");      
    feed.setTitle(cal.getName());
    feed.setLink(cal.getPublicUrl());
    if(cal.getDescription() == null) cal.setDescription("This is description");
    feed.setDescription(cal.getDescription());     
    List<SyndEntry> entries = new ArrayList<SyndEntry>();
    SyndEntry entry;
    SyndContent description; 
    for(CalendarEvent e : events) {
      entry = new SyndEntryImpl();
      entry.setTitle(e.getSummary());                
      entry.setLink(cal.getPublicUrl().replaceFirst("rss","subscribe") + Utils.SLASH + e.getId());    
      entry.setAuthor(auhtor) ;
      description = new SyndContentImpl();
      description.setType(Utils.MIMETYPE_TEXTPLAIN);
      description.setValue(e.getDescription());
      entry.setDescription(description);        
      entries.add(entry);
      entry.getEnclosures() ;  
    }
    feed.setEntries(entries);      
    feed.setEncoding("UTF-8") ;     
    SyndFeedOutput output = new SyndFeedOutput();      
    String feedXML = output.outputString(feed);      
    feedXML = StringUtils.replace(feedXML,"&amp;","&");  
    return feedXML;
  }

  /**
   * 
   * @param username : 
   * @param calendarId
   * @param type
   * @param eventId
   * @return Icalendar data
   * @throws Exception
   */
  @GET
  //@Produces("text/calendar")
  @Path("/subscribe/{username}/{calendarId}/{type}")
  public Response publicProcess(@PathParam("username")
                              String username, @PathParam("calendarId")
                              String calendarId, @PathParam("type")
                              String type) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {

      CalendarService calService = (CalendarService)ExoContainerContext
      .getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
      CalendarImportExport icalEx = calService.getCalendarImportExports(CalendarService.ICALENDAR);
      OutputStream out = icalEx.exportCalendar(username, Arrays.asList(calendarId), type);
      InputStream in = new ByteArrayInputStream(out.toString().getBytes());
      return Response.ok(in, "text/calendar")
      .header("Cache-Control", "private max-age=600, s-maxage=120").
      header("Content-Disposition", "attachment;filename=\"" + calendarId + ".ics").cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }
  
  /**
   * 
   * @param username : 
   * @param calendarId
   * @param type
   * @param eventId
   * @return Icalendar data
   * @throws Exception
   */
  @GET
  //@Produces("text/calendar")
  @Path("private/{username}/{calendarId}/{type}")
  public Response privateProcess(@PathParam("username")
                              String username, @PathParam("calendarId")
                              String calendarId, @PathParam("type")
                              String type) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {

      CalendarService calService = (CalendarService)ExoContainerContext
      .getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
      CalendarImportExport icalEx = calService.getCalendarImportExports(CalendarService.ICALENDAR);
      OutputStream out = icalEx.exportCalendar(username, Arrays.asList(calendarId), type);
      InputStream in = new ByteArrayInputStream(out.toString().getBytes());
      return Response.ok(in, "text/calendar")
      .header("Cache-Control", "private max-age=600, s-maxage=120").
      header("Content-Disposition", "attachment;filename=\"" + calendarId + ".ics").cacheControl(cacheControl).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }
}
