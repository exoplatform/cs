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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webservice.cs.bean.EventData;
import org.exoplatform.webservice.cs.bean.SingleEvent;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 15, 2009  
 */



/**
 * Calendar web service is the implementation of RESTFul service to provide information 
 * to all needed request from calendar application system or out side, such as some gadgets or other
 * request to interact with calendar application data 
 * @anchor CSref.PublicRESTAPIs.CalendarApplication
 */
@Path("/cs/calendar")
public class CalendarWebservice implements ResourceContainer{
  /**
   * base private URL to limited the access of request
   * all request dedicated with PRIVAtE in URL mean the need to authentication  
   **/
  public final static String PRIVATE = "/private";
  /**
   * base URL for all rest services implement from collaboration suite (CS) and using for calendar application
   */
  public final static String BASE_URL = "/cs/calendar".intern();
  /**
   * base RSS URL, this use to retrieve the feed of calendar event 
   */
  public final static String BASE_RSS_URL = BASE_URL + "/feed".intern();
  /**
   * base RSS URL, this use to retrieve the detail of event or answer event invitation
   */
  public final static String BASE_EVENT_URL = BASE_URL + "/event".intern();
  /**
   * base URL to public a calendar to RSS type
   */
  final public static String BASE_URL_PUBLIC = "/cs/calendar/subscribe/".intern();
  /**
   * base slash symbol use to combine the full URL
   */
  final public static String BASE_URL_PRIVATE = BASE_URL + "/".intern();

  private Log log = ExoLogger.getExoLogger("calendar.webservice");

  static CacheControl cc = new CacheControl();
  static {
    cc.setNoCache(true);
    cc.setNoStore(true);
  }
  private CalendarService calendarService = null;
  private Object getCalendarService() {
    calendarService = (CalendarService)ExoContainerContext.getCurrentContainer()
        .getComponentInstanceOfType(CalendarService.class);
    if(calendarService == null){
      return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
    }
    return calendarService;
  }

  public CalendarWebservice() {}

  private boolean validateEventType(String type) {
    return type != null && (CalendarEvent.TYPE_EVENT.equals(type) || CalendarEvent.TYPE_TASK.equals(type));
  }

  /**
   * this service to check permission of current logged user on any calendar by given calendar id
   * the input parameters will be in URL and listed bellow:
   * @param username : given user id, and can be current logged user
   * @param calendarId : given calendar id which we prefer to check permission on
   * @param type : calendar type private, public or share
   * @return JSon data value will return
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.checkPermission
   */
  @GET
  @RolesAllowed("users")
  @Path("/checkPermission/{username}/{calendarId}/{type}/")
  public Response checkPermission(@PathParam("username")
  String username, @PathParam("calendarId")
  String calendarId, @PathParam("type")
  String type) throws Exception {
    EventData eventData = new EventData();
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      Calendar cal = null ;
      eventData.setPermission(false);
      if(Utils.PRIVATE_TYPE == Integer.parseInt(type)) {
        if(calendarService.isRemoteCalendar(username, calendarId)) {
          eventData.setPermission(false);
        } else eventData.setPermission(true);
      } else if(Utils.PUBLIC_TYPE == Integer.parseInt(type)) {
        OrganizationService oService = (OrganizationService)ExoContainerContext
            .getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
        cal = calendarService.getGroupCalendar(calendarId) ;
        if(Utils.canEdit(oService, cal.getEditPermission(), username)) {
          eventData.setPermission(true);
        } 
      } else if(Utils.SHARED_TYPE == Integer.parseInt(type)) {
        if(calendarService.getSharedCalendars(username, true) != null) {
          cal = calendarService.getSharedCalendars(username, true).getCalendarById(calendarId) ;
          if(Utils.canEdit(null, Utils.getEditPerUsers(cal), username)) {
            eventData.setPermission(true);
          }  
        } 
      }  
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception when check permission", e);
      }
      eventData.setPermission(false);
    } 
    return Response.ok(eventData, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  /**
   * this service to provide one single event detail with given user name and given event id, the data return will be ics format
   * @param username : requested user name
   * @param eventFeedName : contains eventId and CalType
   * @return : RSS feeds in XML format
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.event
   */
  @GET
  @RolesAllowed("users")
  @Path("/event/{username}/{eventFeedName}/")
  public Response event(@PathParam("username")
  String username, @PathParam("eventFeedName")
  String eventFeedName) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      CalendarImportExport icalEx = calendarService.getCalendarImportExports(CalendarService.ICALENDAR);
      String eventId = eventFeedName.split(Utils.SPLITTER)[0];
      String type = eventFeedName.split(Utils.SPLITTER)[1].replace(Utils.ICS_EXT, "");
      CalendarEvent event = null;
      if (type.equals(Utils.PRIVATE_TYPE + "")) {
        event = calendarService.getEvent(username, eventId);
      } else if (type.equals(Utils.SHARED_TYPE + "")) {
        EventQuery eventQuery = new EventQuery();
        eventQuery.setText(eventId);
        event = calendarService.getEvents(username, eventQuery, null).get(0);        
      } else {
        EventQuery eventQuery = new EventQuery();
        eventQuery.setText(eventId);
        event = calendarService.getPublicEvents(eventQuery).get(0);
      }
      if (event == null) {
        return Response.status(HTTPStatus.NOT_FOUND).entity("Event " + eventId + "is removed").cacheControl(cacheControl).build();
      }      
      OutputStream out = icalEx.exportEventCalendar(event);
      InputStream in = new ByteArrayInputStream(out.toString().getBytes());
      return Response.ok(in, "text/calendar")
          .header("Cache-Control", "private max-age=600, s-maxage=120").
          header("Content-Disposition", "attachment;filename=\"" + eventId + Utils.ICS_EXT).cacheControl(cacheControl).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
      return Response.status(HTTPStatus.INTERNAL_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }


  /**
   * this service return the XML RSS feed data of one single given calendar and follow a given user id
   * @param username : requested user name
   * @param calendarId : calendar id from system
   * @param type : calendar type
   * @return : RSS feeds
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.feed
   */
  @SuppressWarnings("unchecked")
  @GET
  @RolesAllowed("users")
  @Path("/feed/{username}/{feedname}/{filename}/")
  public Response feed(@PathParam("username")
  String username, @PathParam("feedname")
  String feedname, @PathParam("filename")
  String filename, @Context UriInfo uri) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      FeedData feed = null;
      for (FeedData feedData : calendarService.getFeeds(username)) {
        if (feedData.getTitle().equals(feedname)) {
          feed = feedData;
          break;
        }        
      }
      SyndFeedInput input = new SyndFeedInput();
      SyndFeed syndFeed = input.build(new XmlReader(new ByteArrayInputStream(feed.getContent())));
      List<SyndEntry> entries = new ArrayList<SyndEntry>(syndFeed.getEntries());
      List<CalendarEvent> events = new ArrayList<CalendarEvent>();
      for (SyndEntry entry : entries) {
        String calendarId = entry.getLink().substring(entry.getLink().lastIndexOf("/")+1) ;
        List<String> calendarIds = new ArrayList<String>();
        calendarIds.add(calendarId);
        Calendar calendar = calendarService.getUserCalendar(username, calendarId) ;
        if (calendar != null) {
          events.addAll(calendarService.getUserEventByCalendar(username, calendarIds));
        } else {
          try {
            calendar = calendarService.getSharedCalendars(username, false).getCalendarById(calendarId);
          } catch (NullPointerException e) {
            calendar = null;
          }
          if (calendar != null) {
            events.addAll(calendarService.getSharedEventByCalendars(username, calendarIds));
          } else {
            calendar = calendarService.getGroupCalendar(calendarId);
            if (calendar != null) {
              EventQuery eventQuery = new EventQuery();
              eventQuery.setCalendarId(calendarIds.toArray(new String[]{}));
              events.addAll(calendarService.getPublicEvents(eventQuery));
            }
          }
        }        
      }
      if(events.size() == 0) {
        return Response.status(HTTPStatus.NOT_FOUND).entity("Feed " + feedname + "is removed").cacheControl(cacheControl).build();
      } 
      return Response.ok(makeFeed(username, events, feed, uri), MediaType.APPLICATION_XML).cacheControl(cacheControl).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
      return Response.status(HTTPStatus.INTERNAL_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }


  private String makeFeed(String author, List<CalendarEvent> events, FeedData feedData, UriInfo uri) throws Exception{
    URI baseUri = uri.getBaseUri();
    String baseURL = baseUri.getScheme() + "://" + baseUri.getHost() + ":" + Integer.toString(baseUri.getPort());
    String baseRestURL = baseUri.toString();
    SyndFeed feed = new SyndFeedImpl();      
    feed.setFeedType("rss_2.0");
    feed.setTitle(feedData.getTitle());
    feed.setLink(baseURL + feedData.getUrl());
    feed.setDescription(feedData.getTitle());     
    List<SyndEntry> entries = new ArrayList<SyndEntry>();
    SyndEntry entry;
    SyndContent description; 
    for(CalendarEvent event : events) {
      if (Utils.EVENT_NUMBER > 0 && Utils.EVENT_NUMBER <= entries.size()) break;
      entry = new SyndEntryImpl();
      entry.setTitle(event.getSummary());
      entry.setLink(baseRestURL + BASE_EVENT_URL + Utils.SLASH + author + Utils.SLASH + event.getId() 
                    + Utils.SPLITTER + event.getCalType() + Utils.ICS_EXT);    
      entry.setAuthor(author) ;
      description = new SyndContentImpl();
      description.setType(Utils.MIMETYPE_TEXTPLAIN);
      description.setValue(event.getDescription());
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
   * this service provide an end-point to subscribe calendar from exo calendar 
   * @param username : given user id prefer to get data 
   * @param calendarId : given calendar id
   * @param type : calendar type such as : personal, shared, public 
   * @return ICalendar data in text/calendar MimeType
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.publicProcess
   */
  @GET
  @Path("/subscribe/{username}/{calendarId}/{type}")
  public Response publicProcess(@PathParam("username")
  String username, @PathParam("calendarId")
  String calendarId, @PathParam("type")
  String type) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      Calendar calendar = null;
      if (type.equals(Utils.PRIVATE_TYPE + "")) {
        calendar = calendarService.getUserCalendar(username, calendarId);
      } else if (type.equals(Utils.SHARED_TYPE + "")) {
        calendar = calendarService.getSharedCalendars(username, false).getCalendarById(calendarId);       
      } else {
        calendar = calendarService.getGroupCalendar(calendarId);
      }
      if ((calendar == null) || Utils.isEmpty(calendar.getPublicUrl())) {
        return Response.status(HTTPStatus.LOCKED)
            .entity("Calendar " + calendarId + " is not public access").cacheControl(cacheControl).build();
      }

      CalendarImportExport icalEx = calendarService.getCalendarImportExports(CalendarService.ICALENDAR);
      OutputStream out = icalEx.exportCalendar(username, Arrays.asList(calendarId), type, -1);
      InputStream in = new ByteArrayInputStream(out.toString().getBytes());
      return Response.ok(in, "text/calendar")
          .header("Cache-Control", "private max-age=600, s-maxage=120").
          header("Content-Disposition", "attachment;filename=\"" + calendarId + ".ics").cacheControl(cacheControl).build();
    }catch (NullPointerException ne) {
      return Response.ok(null, "text/calendar")
          .header("Cache-Control", "private max-age=600, s-maxage=120").
          header("Content-Disposition", "attachment;filename=\"" + calendarId + ".ics").cacheControl(cacheControl).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
      return Response.status(HTTPStatus.INTERNAL_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  /**
   * this service generate the ICalendar data from given calendar id, the content of data will be all evnet in side
   * this service require authentication and permits only Users group
   * @param username : require user id to authentication and look up personal calendar
   * @param calendarId : given calendar id to look up
   * @param type : calendar type : private, shared, public
   * @return : text/calendar MimeType (ICalendar format) 
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.privateProcess
   */
  @GET
  @RolesAllowed("users")
  @Path("/{username}/{calendarId}/{type}")
  public Response privateProcess(@PathParam("username")
  String username, @PathParam("calendarId")
  String calendarId, @PathParam("type")
  String type) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      CalendarImportExport icalEx = calendarService.getCalendarImportExports(CalendarService.ICALENDAR);
      OutputStream out = icalEx.exportCalendar(username, Arrays.asList(calendarId), type, -1);
      InputStream in = new ByteArrayInputStream(out.toString().getBytes());
      return Response.ok(in, "text/calendar")
          .header("Cache-Control", "private max-age=600, s-maxage=120").
          header("Content-Disposition", "attachment;filename=\"" + calendarId + ".ics").cacheControl(cacheControl).build();
    }catch (NullPointerException ne) {
      return Response.ok(null, "text/calendar")
          .header("Cache-Control", "private max-age=600, s-maxage=120").
          header("Content-Disposition", "attachment;filename=\"" + calendarId + ".ics").cacheControl(cacheControl).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
      return Response.status(HTTPStatus.INTERNAL_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  /**
   * this service get list of personal events by their type, list of calendar IDs, from time, to time and the size limitation.
   * this service require authentication and permits only Users group
   * @param type : type of the events. The possible values are "Event" and "Task". 
   * @param calids : a string contains calendar IDs separated by commas (,). 
   * @param from : long value of the time which the events are started after. 
   * @param to : long value of the time which the events are started before. 
   * @param limit : the limitation number of returned events.
   * @return Response of a JSon object. The JSon object includes the list of events saved in "info" property.  
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.getEvents
   */
  @GET
  @RolesAllowed("users")
  @Path("/events/personal/{type}/{calids}/{from}/{to}/{limit}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEvents(@PathParam("type") String type, @PathParam("calids") String calids, @PathParam("from") long from, @PathParam("to") long to, @PathParam("limit") long limit) {
    if (!validateEventType(type)) {
      return Response.status(HTTPStatus.BAD_REQUEST).cacheControl(cc).build();
    }
    CalendarService calendarService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if(calendarService == null) {
      return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
    }
    List<String> calList = new LinkedList<String>();
    for (String s : calids.split(",")) {
      if (s.trim().length() > 0) 
        calList.add(s);
    }
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    EventQuery eventQuery = new EventQuery();
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.setTimeInMillis(from);
    eventQuery.setFromDate(calendar);
    calendar = java.util.Calendar.getInstance();
    calendar.setTimeInMillis(to);
    eventQuery.setToDate(calendar);
    eventQuery.setEventType(type);
    eventQuery.setLimitedItems(limit);
    eventQuery.setOrderBy(new String[]{Utils.EXO_FROM_DATE_TIME});
    if (calList.size() > 0)
      eventQuery.setCalendarId(calList.toArray(new String[calList.size()]));
    try {
      List<CalendarEvent> events = calendarService.getUserEvents(username, eventQuery);
      EventData data = new EventData();
      data.setInfo(events);
      return Response.ok(data, MediaType.APPLICATION_JSON_TYPE).cacheControl(cc).build();
    } catch (Exception e) {
      if (log.isWarnEnabled()) log.warn(String.format("Getting events for user %s from %s to %s failed", username, from, to), e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
  }

  /**
   * this service to listing up coming event or task given by current date time
   * this service require authentication and permits only Users group
   * @param username : current logged-in user
   * @param currentdatetime : current date time using ISO8601 format yyyyMMdd
   * @param type : event or task
   * @return page list of event or task
   * @throws Exception : HTTPStatus.INTERNAL_ERROR , HTTPStatus.UNAUTHORIZED , HTTPStatus.NO_CONTENT
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.upcomingEvent
   */
  @GET
  @RolesAllowed("users")
  @Path("/getissues/{currentdatetime}/{type}/{limit}")
  public Response upcomingEvent(@PathParam("currentdatetime")
  String currentdatetime, @PathParam("type")
  String type, @PathParam("limit")
  int limit) throws Exception {
    try {
      if (!validateEventType(type)) {
        return Response.status(HTTPStatus.BAD_REQUEST).cacheControl(cc).build();
      }
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      String username = ConversationState.getCurrent().getIdentity().getUserId();
      CalendarSetting calSetting = calendarService.getCalendarSetting(username);
      SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd") ;
      sf.setTimeZone(TimeZone.getTimeZone(calSetting.getTimeZone()));
      Date currentDate = sf.parse(currentdatetime);
      java.util.Calendar fromCal = calSetting.createCalendar(currentDate);
      java.util.Calendar toCal = calSetting.createCalendar(currentDate);
      toCal.add(java.util.Calendar.DAY_OF_YEAR, 1);
      toCal.add(java.util.Calendar.SECOND, -1); // To fix CS-5412 
      EventQuery eventQuery = new EventQuery();
      eventQuery.setFromDate(fromCal);
      eventQuery.setToDate(toCal);
      eventQuery.setLimitedItems(limit);
      eventQuery.setOrderBy(new String[]{Utils.EXO_FROM_DATE_TIME});
      eventQuery.setEventType(type);
      EventPageList data =  calendarService.searchEvent(username, eventQuery, null);
      String timezoneId = calSetting.getTimeZone();
      TimeZone userTimezone = TimeZone.getTimeZone(timezoneId);
      int timezoneOffset = userTimezone.getRawOffset() + userTimezone.getDSTSavings();
      if(data == null || data.getAll().isEmpty()) 
        return Response.status(HTTPStatus.NO_CONTENT).cacheControl(cc).build();
      EventData eventData = new EventData();
      eventData.setInfo(data.getAll());
      eventData.setUserTimezoneOffset(Integer.toString(timezoneOffset));
      return Response.ok(eventData, MediaType.APPLICATION_JSON).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }

  }

  /**
   * this service help user to update status of a task
   * this service require authentication and permits only Users group
   * @param taskid : given task id 
   * @param statusId id of the status. Possible values are: 1 - Need action, 2 - In Progress, 3 -Completed, 4 - Cancelled.
   * @return true/false
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.updateStatus
   */
  @GET
  @RolesAllowed("users")
  @Path("/updatestatus/{taskid}")
  public Response updateStatus(@PathParam("taskid") String taskid, @QueryParam("statusid") int statusId) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(String.format("Update status [%1$s] for task [%2$s] ..............", statusId, taskid));
    }
    try {
      statusId = statusId != 0 ? statusId : 3; // if the status is not given, it is understood as "Completed".
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      String username = ConversationState.getCurrent().getIdentity().getUserId();
      String status = CalendarEvent.TASK_STATUS[(statusId - 1) % 4];
      CalendarEvent task = calendarService.getEvent(username, taskid);
      if (status.equals(task.getEventState())) {
        return Response.ok(String.format("[%1$s] has been set for task %2$s before!", status, taskid), MediaType.APPLICATION_JSON).cacheControl(cc).build();
      } else {
        String calendarId = task.getCalendarId();
        task.setEventState(status);
        calendarService.saveUserEvent(username, calendarId, task, false);
        return Response.ok("true", MediaType.APPLICATION_JSON).cacheControl(cc).build();

      }
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Updating task status failed!", e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
  }

  /**
   * this service retrieve all data of private (personal) calendar of logged in user
   * this service require authentication and permits only Users group
   * @return : json object 
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.getCalendars
   */
  @GET
  @RolesAllowed("users")
  @Path("/getcalendars")
  public Response getCalendars() throws Exception{
    try{      
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      String username = ConversationState.getCurrent().getIdentity().getUserId();
      List<Calendar> calList = calendarService.getUserCalendars(username, true);
      EventData data = new EventData();
      data.setCalendars(calList);
      return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cc).build();
    }catch(Exception e){
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
  }

  private SingleEvent makeSingleEvent(CalendarSetting calSetting, CalendarEvent cEvent) {
    if (calSetting == null || cEvent == null) {
      throw new IllegalArgumentException("parameters must be not null");
    }
    SingleEvent event = new SingleEvent();
    event.setDescription(cEvent.getDescription());
    event.setEventState(cEvent.getEventState());
    event.setLocation(cEvent.getLocation());
    event.setPriority(cEvent.getPriority());
    event.setSummary(cEvent.getSummary());
    TimeZone timeZone = TimeZone.getTimeZone(calSetting.getTimeZone());
    event.setStartDateTime(cEvent.getFromDateTime().getTime());
    event.setStartTimeOffset(timeZone.getOffset(cEvent.getFromDateTime().getTime()));
    event.setEndDateTime(cEvent.getToDateTime().getTime());
    event.setEndTimeOffset(timeZone.getOffset(cEvent.getToDateTime().getTime()));
    event.setDateFormat(calSetting.getDateFormat());
    return event;
  }

  /**
   * this service to produce content of an event following given event id
   * this service require authentication and permits only Users group
   * @param eventid : id of event
   * @return JSon data type 
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.getEvent
   */
  @GET
  @RolesAllowed("users")
  @Path("/getevent/{eventid}")
  public Response getEvent(@PathParam("eventid") String eventid) throws Exception{
    try{      
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      String username = ConversationState.getCurrent().getIdentity().getUserId();
      CalendarEvent calEvent = calendarService.getEvent(username, eventid);
      CalendarSetting calSetting = calendarService.getCalendarSetting(username);
      if(!calEvent.getAttachment().isEmpty()) calEvent.setAttachment(null);
      SingleEvent data = makeSingleEvent(calSetting, calEvent);
      return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cc).build();
    }catch(Exception e){
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
  }

  /**
   * this service provide the end-point to answer or reply an invitation to join any given event id
   * @param calendarId : the calendar id of calendar that event belong to
   * @param calType : calendar type such as public, private, shared type
   * @param eventId : given event id to retrieve the data to update
   * @param inviter : the given user id of inviter ( owner of invitation )
   * @param invitee : the given user id of receiver ( participant )
   * @param eXoId   : given user id when user logged in 
   * @param answer  : the answer of invitation such as : accept, refuse, or may be will join
   * @return will be HTML in response
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.CalendarApplication.processInvitationReply
   */
  @GET
  @Path("/invitation/{calendarId}/{calType}/{eventId}/{inviter}/{invitee}/{eXoId}/{answer}")
  public Response processInvitationReply(@PathParam("calendarId") String calendarId,
                                         @PathParam("calType") String calType, 
                                         @PathParam("eventId") String eventId, 
                                         @PathParam("inviter") String inviter, 
                                         @PathParam("invitee") String invitee, 
                                         @PathParam("eXoId") String eXoId, 
                                         @PathParam("answer") String answer) throws Exception {
    try {
      if(getCalendarService() instanceof Response) {
        return (Response) getCalendarService();
      }
      String userId = eXoId.equals("null")?null:eXoId;
      calendarService.confirmInvitation(inviter, invitee, userId, Integer.parseInt(calType), calendarId, eventId, Integer.parseInt(answer));
      int ans = Integer.parseInt(answer);
      StringBuffer response = new StringBuffer();
      response.append("<html><head><title>Invitation Answer</title></head>");
      response.append("<body>");
      switch (ans) {
      case Utils.ACCEPT:
        response.append("You have accepted invitation from " + inviter);
        break;
      case Utils.DENY:
        response.append("You have refused invitation from " + inviter);
        break;
      case Utils.NOTSURE:
        response.append("You have answered invitation from " + inviter + " : Not sure!");
        break;
      }
      response.append("</body></html>");
      return Response.ok(response.toString(), MediaType.TEXT_HTML).cacheControl(cc).build();
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
  }                                    
}
