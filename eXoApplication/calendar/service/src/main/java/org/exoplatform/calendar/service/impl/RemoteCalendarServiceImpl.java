/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.calendar.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.util.CompatibilityHints;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.RemoteCalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 10, 2011  
 */
public class RemoteCalendarServiceImpl implements RemoteCalendarService {
  
  private static final Namespace CALDAV_NAMESPACE = Namespace.getNamespace("C", "urn:ietf:params:xml:ns:caldav");
  private static final String CALDAV_XML_CALENDAR_MULTIGET = "calendar-multiget";
  private static final String CALDAV_XML_CALENDAR_QUERY = "calendar-query";
  private static final String CALDAV_XML_CALENDAR_DATA = "calendar-data";
  private static final String CALDAV_XML_FILTER = "filter";
  private static final String CALDAV_XML_COMP_FILTER = "comp-filter";
  private static final String CALDAV_XML_TIME_RANGE = "time-range";
  private static final String CALDAV_XML_START = "start";
  private static final String CALDAV_XML_END = "end";
  private static final String CALDAV_XML_COMP_FILTER_NAME = "name";
  private static final String CALDAV_XML_TIMEZONE = "timezone";
  
  private static final Log logger = ExoLogger.getLogger("cs.calendar.service.remote");
  
  private JCRDataStorage storage_;
  
  public RemoteCalendarServiceImpl(JCRDataStorage storage) {
    this.storage_ = storage;
  }
  
  @Override
  public InputStream connectToRemoteServer(String remoteUrl, String remoteType, String remoteUser, String remotePassword) throws IOException {
    HostConfiguration hostConfig = new HostConfiguration();
    String host = new URL(remoteUrl).getHost();
    if (StringUtils.isEmpty(host)) host = remoteUrl;
    hostConfig.setHost(host);
    HttpClient client = new HttpClient();
    client.setHostConfiguration(hostConfig);
    client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
    client.getHttpConnectionManager().getParams().setSoTimeout(10000);
    // basic authentication
    if (!StringUtils.isEmpty(remoteUser)) {
      Credentials credentials = new UsernamePasswordCredentials(remoteUser, remotePassword);  
      client.getState().setCredentials(new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
    }
    GetMethod get = new GetMethod(remoteUrl);
    try {
      client.executeMethod(get);
      InputStream icalInputStream = get.getResponseBodyAsStream();
      return icalInputStream;
    }
    catch (IOException e) {
      logger.debug(e.getMessage());
      throw new IOException(e.getMessage());
    }
  }


  @Override
  public boolean isValidRemoteUrl(String url, String type, String remoteUser, String remotePassword) throws IOException,UnsupportedOperationException {
    try {
      HttpClient client = new HttpClient();
      HostConfiguration hostConfig = new HostConfiguration();
      String host = new URL(url).getHost();
      if (StringUtils.isEmpty(host)) host = url;
      hostConfig.setHost(host);
      client.setHostConfiguration(hostConfig);
      Credentials credentials = null;
      client.setHostConfiguration(hostConfig);
      if (!StringUtils.isEmpty(remoteUser)) {
        credentials = new UsernamePasswordCredentials(remoteUser, remotePassword);  
        client.getState().setCredentials(new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
      }
      
      if (type.equals(CalendarService.ICALENDAR)) {
        GetMethod get = new GetMethod(url);
        client.executeMethod(get);
        int statusCode = get.getStatusCode();
        get.releaseConnection();
        return (statusCode == HttpURLConnection.HTTP_OK);
      }
      else {
        if (type.equals(CalendarService.CALDAV)) {
          OptionsMethod options = new OptionsMethod(url);
          client.executeMethod(options);
          Header header = options.getResponseHeader("DAV");
          options.releaseConnection();
          if (header == null) {
            logger.debug("Cannot connect to remoter server or not support WebDav access");
            return false;
          }
          Boolean support = header.toString().contains("calendar-access");
          options.releaseConnection();
          if (!support) { 
            logger.debug("Remote server does not support CalDav access");
            throw new UnsupportedOperationException ("Remote server does not support CalDav access");
          }
          return support;
        }
        return false;
      }
    }
    catch (MalformedURLException e) {
      logger.debug(e.getMessage());
      throw new IOException("URL is invalid. Maybe no legal protocol or URl could not be parsed");
    }
    catch (IOException e) {
      logger.debug(e.getMessage());
      throw new IOException("Error occurs when connecting to remote server");
    }
  }

  @Override
  public Calendar importRemoteCalendar(String username,
                                       String calendarId,
                                       InputStream icalInputStream) throws Exception {
    CalendarBuilder calendarBuilder = new CalendarBuilder() ;
    // Enable relaxed-unfolding to allow ical4j parses "folding" line follows iCalendar spec
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
    net.fortuna.ical4j.model.Calendar iCalendar;
    try {
      iCalendar = calendarBuilder.build(icalInputStream) ;
    }
    catch (ParserException e) {
      logger.debug(e.getMessage());
      throw new ParserException("Cannot parsed the input stream. The input stream format must be iCalendar", e.getLineNo());
    }
    catch (IOException e) {
      logger.debug(e.getMessage());
      throw new IOException("I/O error when parsing input stream");
    }
    
    CalendarService  calService = (CalendarService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if(calService == null) {
      calService = (CalendarService) ExoContainerContext.getContainerByName(PortalContainer.getCurrentPortalContainerName()).getComponentInstanceOfType(CalendarService.class);
    }
    
    Map<String, VFreeBusy> vFreeBusyData = new HashMap<String, VFreeBusy>() ;
    Map<String, VAlarm> vAlarmData = new HashMap<String, VAlarm>() ;
    
    Calendar eXoCalendar = calService.getUserCalendar(username, calendarId);
    
    // import calendar components
    ComponentList componentList = iCalendar.getComponents() ;
    CalendarEvent exoEvent ;
    for(Object obj : componentList) {
      if(obj instanceof VEvent) {
        VEvent v = (VEvent)obj ;
        if(!v.getAlarms().isEmpty()) {
          for (Object o : v.getAlarms()) {
            if (o instanceof VAlarm) {
              VAlarm va = (VAlarm)o;
              vAlarmData.put(v.getUid().getValue()+":"+ va.getProperty(Property.ACTION).getName(), va) ;
            }
          }
        }

      }
      if(obj instanceof VFreeBusy) vFreeBusyData.put(((VFreeBusy)obj).getUid().getValue(), (VFreeBusy)obj) ;
    }
    for(Object obj : componentList) {
      if(obj instanceof VEvent){
        VEvent event = (VEvent)obj ;
        exoEvent = new CalendarEvent() ;
        //exoEvent.setId(event.getUid().toString());
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try {
            calService.saveEventCategory(username, evCate, true) ;
          } catch (ItemExistsException e) { 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          } catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryId(evCate.getId()) ;
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(calendarId) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_EVENT) ;

        String sValue = "" ;
        String eValue = "" ;
        if(event.getStartDate() != null) {
          sValue = event.getStartDate().getValue() ;
          exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        }
        if(event.getEndDate() != null) {
          eValue = event.getEndDate().getValue() ;
          exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        }
        if (sValue.length() == 8 && eValue.length() == 8 ) {
          //exoEvent.setAllday(true) ;
          exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
        }
        if (sValue.length() > 8 && eValue.length() > 8 ) {         
          if("0000".equals(sValue.substring(9,13)) && "0000".equals(eValue.substring(9,13)) ) {
            //exoEvent.setAllday(true);
            exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
          }
        }
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        if(event.getPriority() != null) exoEvent.setPriority(CalendarEvent.PRIORITY[Integer.parseInt(event.getPriority().getValue())] ) ;
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        calService.saveUserEvent(username, calendarId, exoEvent, true) ;
      } else if (obj instanceof VToDo) {
        VToDo event = (VToDo)obj ;
        exoEvent = new CalendarEvent() ;
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try{
            calService.saveEventCategory(username, evCate, true) ;
          } catch(ItemExistsException e){ 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          } catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(calendarId) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        if(event.getStartDate() != null) exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        if(event.getDue() != null) exoEvent.setToDateTime(event.getDue().getDate()) ;
        //if(event.getEndDate() != null) exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        if(event.getPriority() != null) exoEvent.setPriority(CalendarEvent.PRIORITY[Integer.parseInt(event.getPriority().getValue())] ) ;
        if(vFreeBusyData.get(event.getUid().getValue()) != null) {
          exoEvent.setStatus(CalendarEvent.ST_BUSY) ;
        }
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        calService.saveUserEvent(username, calendarId, exoEvent, true) ;
      }
    }
    return eXoCalendar;
  }
  
  @Override
  public Calendar importRemoteCalendar(String username, String remoteUrl, String remoteType, String calendarName, String syncPeriod, Credentials credentials) throws Exception {
    String remoteUser = null;
    String remotePassword = null;
    if (credentials != null) {
      remoteUser = ((UsernamePasswordCredentials) credentials).getUserName();
      remotePassword = ((UsernamePasswordCredentials) credentials).getPassword();
    }
    
    if (remoteType.equals(CalendarService.ICALENDAR)) {
      InputStream icalInputStream = connectToRemoteServer(remoteUrl, remoteType, remoteUser, remotePassword);
      Calendar eXoCalendar = storage_.createRemoteCalendar(username, calendarName, remoteUrl, remoteType, syncPeriod, remoteUser, remotePassword);
      importRemoteCalendar(username, eXoCalendar.getId(), icalInputStream);
      storage_.setRemoteCalendarLastUpdated(username, eXoCalendar.getId(), Utils.getGreenwichMeanTime());
      icalInputStream.close();
      return eXoCalendar;
    }
    else {
      if (remoteType.equals(CalendarService.CALDAV)) {
        MultiStatus multiStatus = connectToCalDavServer(remoteUrl, remoteUser, remotePassword);
        Calendar eXoCalendar = storage_.createRemoteCalendar(username, calendarName, remoteUrl, remoteType, syncPeriod, remoteUser, remotePassword);
        String href;
        CalendarBuilder builder = new CalendarBuilder();
        // Enable relaxed-unfolding to allow ical4j parses "folding" line follows iCalendar spec
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
        
        for (int i = 0; i < multiStatus.getResponses().length; i++) {
          MultiStatusResponse multiRes = multiStatus.getResponses()[i];
          href = multiRes.getHref();
          DavPropertySet propSet =  multiRes.getProperties(DavServletResponse.SC_OK);
          DavProperty calendarData = propSet.get("calendar-data", CALDAV_NAMESPACE);
          DavProperty etag = propSet.get(DavPropertyName.GETETAG.getName(), DavConstants.NAMESPACE);
          try {
            net.fortuna.ical4j.model.Calendar iCalEvent = builder.build(new StringReader(calendarData.getValue().toString()));
            importRemoteCalendarEvent(username, eXoCalendar.getId(), iCalEvent, href, etag.getValue().toString());
            storage_.setRemoteCalendarLastUpdated(username, eXoCalendar.getId(), Utils.getGreenwichMeanTime());
          }
          catch (Exception e) {
            logger.debug("Exception occurs when import calendar component " + href + ". Skip this component.");
            continue;
          }
        }
        
        return eXoCalendar;
      }
      return null;
    }
  }

  // TODO: improve this function follow CalDav synchronization operations specification to update faster (avoid fully reload)
  @Override
  public Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception {
    if (!storage_.isRemoteCalendar(username, remoteCalendarId)) {
      logger.debug("This calendar is not remote calendar.");
      return null;
    }
    Node calendarNode = storage_.getUserCalendarHome(username).getNode(remoteCalendarId);
    String remoteType = calendarNode.getProperty(Utils.EXO_REMOTE_TYPE).getString();
    if (CalendarService.ICALENDAR.equals(remoteType)) {
      String remoteUrl = calendarNode.getProperty(Utils.EXO_REMOTE_URL).getString();
      String remoteUser = calendarNode.getProperty(Utils.EXO_REMOTE_USERNAME).getString();
      String remotePassword = calendarNode.getProperty(Utils.EXO_REMOTE_PASSWORD).getString();
      
      InputStream icalInputStream = connectToRemoteServer(remoteUrl, remoteType, remoteUser, remotePassword);
      
      // remove all components in local calendar
      List<String> calendarIds = new ArrayList<String>();
      calendarIds.add(remoteCalendarId);
      List<CalendarEvent> events = storage_.getUserEventByCalendar(username, calendarIds);
      for (CalendarEvent event : events) {
        storage_.removeUserEvent(username, remoteCalendarId, event.getId());
      }
      
      Calendar eXoCalendar = importRemoteCalendar(username, remoteCalendarId, icalInputStream);
      storage_.setRemoteCalendarLastUpdated(username, eXoCalendar.getId(), Utils.getGreenwichMeanTime());
      icalInputStream.close();
      return eXoCalendar;
    }
    
    if (CalendarService.CALDAV.equals(remoteType)) {
      java.util.Calendar from = java.util.Calendar.getInstance();
      from.add(java.util.Calendar.YEAR, -1);
      java.util.Calendar to = java.util.Calendar.getInstance();
      to.add(java.util.Calendar.YEAR, 1);
      Calendar eXoCalendar = synchronizeWithCalDavServer(username, remoteCalendarId, from, to);
      storage_.setRemoteCalendarLastUpdated(username, eXoCalendar.getId(), Utils.getGreenwichMeanTime());
      return eXoCalendar;
    }
    
    return null;
  }
  
  public boolean isValidate(InputStream icalInputStream) throws Exception {
    try {
      CalendarBuilder calendarBuilder = new CalendarBuilder() ;
      // Enable relaxed-unfolding to allow ical4j parses "folding" line follows iCalendar spec
      CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
      calendarBuilder.build(icalInputStream) ;
      return true ;
    } catch (Exception e) {
      e.printStackTrace() ;
      return false ;
    }
  }
  
   
  /**
   * First time connect to CalDav server to get data
   * @param url
   * @param username
   * @param password
   * @return
   * @throws Exception
   */
  public MultiStatus connectToCalDavServer(String url, String username, String password) throws Exception {
    HostConfiguration hostConfig = new HostConfiguration();
    String host = new URL(url).getHost();
    if (StringUtils.isEmpty(host)) host = url;
    hostConfig.setHost(host);
    HttpClient client = new HttpClient();
    client.setHostConfiguration(hostConfig);
    client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
    client.getHttpConnectionManager().getParams().setSoTimeout(10000);
    // basic authentication
    if (!StringUtils.isEmpty(username)) {
      Credentials credentials = new UsernamePasswordCredentials(username, password);  
      client.getState().setCredentials(new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
    }
    return doCalendarQuery(client, url);
  }
  
  /**
   * Get a map of pairs (href,etag) from caldav server
   * This calendar query doesn't include calendar-data element to get data faster
   * @param url
   * @param remoteUser
   * @param remotePassword
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  public Map<String,String> getEntityTags(HttpClient client, String uri, java.util.Calendar from, java.util.Calendar to) throws Exception {
    
    Map<String,String> etags = new HashMap<String, String>();
    ReportMethod report = null;
    
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();
      
      // root element
      Element calendarQuery = DomUtil.createElement(doc, CALDAV_XML_CALENDAR_QUERY, CALDAV_NAMESPACE);
      calendarQuery.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(),
                                      Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(),
                                      DavConstants.NAMESPACE.getURI());
      
      ReportInfo reportInfo = new ReportInfo(calendarQuery, DavConstants.DEPTH_0);
      DavPropertyNameSet propNameSet = reportInfo.getPropertyNameSet();
      propNameSet.add(DavPropertyName.GETETAG);
      
      // filter element
      Element filter = DomUtil.createElement(doc, CALDAV_XML_FILTER, CALDAV_NAMESPACE);
      
      Element calendarComp = DomUtil.createElement(doc, CALDAV_XML_COMP_FILTER, CALDAV_NAMESPACE);
      calendarComp.setAttribute(CALDAV_XML_COMP_FILTER_NAME, net.fortuna.ical4j.model.Calendar.VCALENDAR);
      
      Element eventComp = DomUtil.createElement(doc, CALDAV_XML_COMP_FILTER, CALDAV_NAMESPACE);
      eventComp.setAttribute(CALDAV_XML_COMP_FILTER_NAME, net.fortuna.ical4j.model.component.VEvent.VEVENT);
      
      Element todoComp = DomUtil.createElement(doc, CALDAV_XML_COMP_FILTER, CALDAV_NAMESPACE);
      todoComp.setAttribute(CALDAV_XML_COMP_FILTER_NAME, net.fortuna.ical4j.model.component.VEvent.VTODO);
      
      Element timeRange = DomUtil.createElement(doc, CALDAV_XML_TIME_RANGE, CALDAV_NAMESPACE);
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
      timeRange.setAttribute(CALDAV_XML_START, format.format(from.getTime()));
      timeRange.setAttribute(CALDAV_XML_END, format.format(to.getTime()));
     
      eventComp.appendChild(timeRange);
      todoComp.appendChild(timeRange);
      calendarComp.appendChild(eventComp);
      calendarComp.appendChild(todoComp);
      filter.appendChild(calendarComp);
     
      reportInfo.setContentElement(filter);
     
      report = new ReportMethod(uri, reportInfo);
      client.executeMethod(report);
      MultiStatus multiStatus = report.getResponseBodyAsMultiStatus();
      
      String href;
      for (int i = 0; i < multiStatus.getResponses().length; i++) {
        MultiStatusResponse multiRes = multiStatus.getResponses()[i];
        href = multiRes.getHref();
        DavPropertySet propSet =  multiRes.getProperties(DavServletResponse.SC_OK);
        DavProperty etag = propSet.get(DavPropertyName.GETETAG.getName(), DavConstants.NAMESPACE);
        etags.put(href, etag.getValue().toString());
      }
      
      return etags;
    } finally {
      if (report != null) report.releaseConnection();
    }
  }
  
  /**
   * Do reload data from CalDav server for remote calendar with a time-range condition.
   * This function first gets entity tag map from server, then compare with data from local
   * to determines which events/task (or other components) need to be update, create or delete
   * @param username
   * @param remoteCalendarId
   * @param from
   * @param to
   * @throws Exception
   */
  
  public Calendar synchronizeWithCalDavServer(String username, String remoteCalendarId, java.util.Calendar from, java.util.Calendar to) throws Exception {
    if (!storage_.isRemoteCalendar(username, remoteCalendarId)) {
      return null;
    }
    
    CalendarService  calService = (CalendarService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if(calService == null) {
      calService = (CalendarService) ExoContainerContext.getContainerByName(PortalContainer.getCurrentPortalContainerName()).getComponentInstanceOfType(CalendarService.class);
    }
    
    CalendarBuilder calendarBuilder = new CalendarBuilder(); 
    // Enable relaxed-unfolding to allow ical4j parses "folding" line follows iCalendar spec
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
    
    if (!CalendarService.CALDAV.equals(calService.getRemoteCalendarType(username, remoteCalendarId))) {
      throw new UnsupportedOperationException("Not support");
    }
    
    // get infomation about remote calendar
    String url = calService.getRemoteCalendarUrl(username, remoteCalendarId);
    String remoteUser = calService.getRemoteCalendarUsername(username, remoteCalendarId);
    String remotePassword = calService.getRemoteCalendarPassword(username, remoteCalendarId);

    HttpClient client = new HttpClient();
    HostConfiguration hostConfig = new HostConfiguration();
    String host = new URL(url).getHost();
    if (StringUtils.isEmpty(host)) host = url;
    hostConfig.setHost(host);
    client.setHostConfiguration(hostConfig);
    Credentials credentials = null;
    client.setHostConfiguration(hostConfig);
    if (!StringUtils.isEmpty(remoteUser)) {
      credentials = new UsernamePasswordCredentials(remoteUser, remotePassword);  
      client.getState().setCredentials(new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
    }
    
    // cache a List (or Map) of {href, etag} pairs from MultiStatus responses
    Map<String, String> entityTags = getEntityTags(client, url, from, to);
    
    // get List of event from local calendar in specific time-range
    EventQuery eventQuery = new EventQuery();
    eventQuery.setCalendarId(new String[] {remoteCalendarId});
    eventQuery.setFromDate(from);
    eventQuery.setToDate(to);
    
    List<CalendarEvent> eXoEvents = calService.getUserEvents(username, eventQuery);
    Iterator<CalendarEvent> it = eXoEvents.iterator();
    // events map contains set of (href, CalendarEvent) pairs in the local calendar
    Map<String, String> events = new HashMap<String, String>();
    while (it.hasNext()) {
      CalendarEvent event = it.next();
      //events.put(calService.getCalDavResourceHref(username, remoteCalendarId, event.getId()), calService.getCalDavResourceEtag(username, remoteCalendarId, event.getId()));
      events.put(calService.getCalDavResourceHref(username, remoteCalendarId, event.getId()), event.getId());
    }
    
    // list of href of new events on the server
    List<String> created = new ArrayList<String>(); 
    
    // map of out-of-date event/task, the key is the href of event/task, the value is the id of event on local calendar
    Map<String,String> updated = new HashMap<String, String>();
    
    // list of event id need to delete
    List<String> deleted = new ArrayList<String>();
    
    // for each event on entity tags list, find this event in local calendar by href then use etag value to get:
    Iterator<Entry<String, String>> iter = entityTags.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, String> pairs = iter.next();
      String href = pairs.getKey();
      String etag = pairs.getValue();
      // new events
      if (!events.containsKey(href)) {
        created.add(href);
      } else {
        // check need-to-update events
        String eventId = events.get(href);
        String calendarId = calService.getEvent(username, eventId).getCalendarId();
        String localEtag = calService.getCalDavResourceEtag(username, calendarId, eventId);
        if (!localEtag.equals(etag)) {
          updated.put(href, eventId);
        }
      }
    }
        
    // for each event on local calendar, find this event in responses list to get list of need-to-delete event
    iter = events.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, String> pairs = iter.next();
      String href = pairs.getKey();
      if (!entityTags.containsKey(href)) {
        deleted.add(pairs.getValue());
      }
    }
    
    // from three lists, do update on local calendar
    // do a multi-get report request to server to get list of new events
    MultiStatus multiStatus = doCalendarMultiGet(client, url, created.toArray(new String[0]));
    String href;
    if (multiStatus != null) {
      for (int i = 0; i < multiStatus.getResponses().length; i++) {
        MultiStatusResponse multiRes = multiStatus.getResponses()[i];
        href = multiRes.getHref();
        DavPropertySet propSet =  multiRes.getProperties(DavServletResponse.SC_OK);
        DavProperty calendarData = propSet.get(CALDAV_XML_CALENDAR_DATA, CALDAV_NAMESPACE);
        DavProperty etag = propSet.get(DavPropertyName.GETETAG.getName(), DavConstants.NAMESPACE);
        try {
          net.fortuna.ical4j.model.Calendar iCalEvent = calendarBuilder.build(new StringReader(calendarData.getValue().toString()));
          // add new event
          importRemoteCalendarEvent(username, remoteCalendarId, iCalEvent, href, etag.getValue().toString());
        } 
        catch (Exception e) {
          logger.debug("Exception occurs when import calendar component " + href + ". Skip this component.");
          continue;
        }
      }
    }
    
    multiStatus = doCalendarMultiGet(client, url, updated.keySet().toArray(new String[0]));
    if (multiStatus != null) {
      for (int i = 0; i < multiStatus.getResponses().length; i++) {
        MultiStatusResponse multiRes = multiStatus.getResponses()[i];
        href = multiRes.getHref();
        DavPropertySet propSet =  multiRes.getProperties(DavServletResponse.SC_OK);
        DavProperty calendarData = propSet.get(CALDAV_XML_CALENDAR_DATA, CALDAV_NAMESPACE);
        DavProperty etag = propSet.get(DavPropertyName.GETETAG.getName(), DavConstants.NAMESPACE);
        String eventId = updated.get(href);
        try {
          net.fortuna.ical4j.model.Calendar iCalEvent = calendarBuilder.build(new StringReader(calendarData.getValue().toString()));
          // update event 
          updateRemoteCalendarEvent(username, remoteCalendarId, eventId, iCalEvent, etag.getValue().toString());
        }
        catch (Exception e) {
          logger.debug("Exception occurs when import calendar component " + href + ". Skip this component.");
          continue;
        }
      }
    }
    
    // delete no-longer exists events
    Iterator<String> iterator = deleted.iterator();
    while (iterator.hasNext()) {
      String eventId = iterator.next();
      calService.removeUserEvent(username, remoteCalendarId, eventId);
    }
    return calService.getUserCalendar(username, remoteCalendarId);
  }
  
  
  public MultiStatus doCalendarMultiGet(HttpClient client, String uri, String[] hrefs) throws Exception {
    
    if (hrefs.length == 0) return null;
    
    ReportMethod report = null;
    
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();
      
      // root element
      Element calendarMultiGet = DomUtil.createElement(doc, CALDAV_XML_CALENDAR_MULTIGET, CALDAV_NAMESPACE);
      calendarMultiGet.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(),
                                      Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(),
                                      DavConstants.NAMESPACE.getURI());
      
      ReportInfo reportInfo = new ReportInfo(calendarMultiGet, DavConstants.DEPTH_0);
      DavPropertyNameSet propNameSet = reportInfo.getPropertyNameSet();
      propNameSet.add(DavPropertyName.GETETAG);
      DavPropertyName calendarData = DavPropertyName.create(CALDAV_XML_CALENDAR_DATA, CALDAV_NAMESPACE);
      propNameSet.add(calendarData);
      
      Element href;
      for (int i = 0; i < hrefs.length; i++) {
        href = DomUtil.createElement(doc, DavConstants.XML_HREF, DavConstants.NAMESPACE, hrefs[i]);
        reportInfo.setContentElement(href);
      }
      
      report = new ReportMethod(uri, reportInfo);
      client.executeMethod(report);
      MultiStatus multiStatus = report.getResponseBodyAsMultiStatus();
      return multiStatus;
    } finally {
      if (report != null) report.releaseConnection();
    }
  }
      
    /**
     * Send a calendar-query REPORT request, full time-range
     * @param client
     * @param uri
     * @return
     * @throws Exception
     */
  public MultiStatus doCalendarQuery(HttpClient client, String uri) throws Exception {
    ReportMethod report = null;
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();
      
      // root element
      Element calendarQuery = DomUtil.createElement(doc, CALDAV_XML_CALENDAR_QUERY, CALDAV_NAMESPACE);
      calendarQuery.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(),
                                      Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(),
                                      DavConstants.NAMESPACE.getURI());
      
      ReportInfo reportInfo = new ReportInfo(calendarQuery, DavConstants.DEPTH_0);
      DavPropertyNameSet propNameSet = reportInfo.getPropertyNameSet();
      propNameSet.add(DavPropertyName.GETETAG);
      DavPropertyName calendarData = DavPropertyName.create(CALDAV_XML_CALENDAR_DATA, CALDAV_NAMESPACE);
      propNameSet.add(calendarData);
      
      // filter element
      Element filter = DomUtil.createElement(doc, CALDAV_XML_FILTER, CALDAV_NAMESPACE);
      
      Element calendarComp = DomUtil.createElement(doc, CALDAV_XML_COMP_FILTER, CALDAV_NAMESPACE);
      calendarComp.setAttribute(CALDAV_XML_COMP_FILTER_NAME, net.fortuna.ical4j.model.Calendar.VCALENDAR);
      
      Element eventComp = DomUtil.createElement(doc, CALDAV_XML_COMP_FILTER, CALDAV_NAMESPACE);
      eventComp.setAttribute(CALDAV_XML_COMP_FILTER_NAME, net.fortuna.ical4j.model.component.VEvent.VEVENT);
      
      Element todoComp = DomUtil.createElement(doc, CALDAV_XML_COMP_FILTER, CALDAV_NAMESPACE);
      todoComp.setAttribute(CALDAV_XML_COMP_FILTER_NAME, net.fortuna.ical4j.model.component.VEvent.VTODO);
      
      /*Element timeRange = DomUtil.createElement(doc, CALDAV_XML_TIME_RANGE, CALDAV_NAMESPACE);
      java.util.Calendar start = java.util.Calendar.getInstance();
      start.add(java.util.Calendar.YEAR, -1);
      java.util.Calendar end = java.util.Calendar.getInstance();
      end.add(java.util.Calendar.YEAR, 1);
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
      timeRange.setAttribute(CALDAV_XML_START, format.format(start.getTime()));
      timeRange.setAttribute(CALDAV_XML_END, format.format(end.getTime()));
     
      eventComp.appendChild(timeRange);
      todoComp.appendChild(timeRange);*/
      calendarComp.appendChild(eventComp);
      calendarComp.appendChild(todoComp);
      filter.appendChild(calendarComp);
     
      reportInfo.setContentElement(filter);
     
      report = new ReportMethod(uri, reportInfo);
      client.executeMethod(report);
      MultiStatus multiStatus = report.getResponseBodyAsMultiStatus();
      return multiStatus;
    } finally {
      if (report != null) report.releaseConnection();
    }  
  }
  
  /**
   * Import to local calendar event from iCalendar object with href and etag
   * @param username
   * @param calendarId
   * @param iCalendar
   * @param href
   * @param etag
   * @throws Exception
   */
  public void importRemoteCalendarEvent(String username, String calendarId, net.fortuna.ical4j.model.Calendar iCalendar, String href, String etag) throws Exception {
    CalendarService  calService = (CalendarService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if(calService == null) {
      calService = (CalendarService) ExoContainerContext.getContainerByName(PortalContainer.getCurrentPortalContainerName()).getComponentInstanceOfType(CalendarService.class);
    }
    
    Map<String, VFreeBusy> vFreeBusyData = new HashMap<String, VFreeBusy>() ;
    Map<String, VAlarm> vAlarmData = new HashMap<String, VAlarm>() ;
    
    Calendar eXoCalendar = calService.getUserCalendar(username, calendarId);
    
    // import calendar components
    ComponentList componentList = iCalendar.getComponents() ;
    CalendarEvent exoEvent ;
    for(Object obj : componentList) {
      if(obj instanceof VEvent) {
        VEvent v = (VEvent)obj ;
        if(!v.getAlarms().isEmpty()) {
          for (Object o : v.getAlarms()) {
            if (o instanceof VAlarm) {
              VAlarm va = (VAlarm)o;
              vAlarmData.put(v.getUid().getValue()+":"+ va.getProperty(Property.ACTION).getName(), va) ;
            }
          }
        }

      }
      if(obj instanceof VFreeBusy) vFreeBusyData.put(((VFreeBusy)obj).getUid().getValue(), (VFreeBusy)obj) ;
    }
    for(Object obj : componentList) {
      if(obj instanceof VEvent){
        VEvent event = (VEvent)obj ;
        exoEvent = new CalendarEvent() ;
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try {
            calService.saveEventCategory(username, evCate, true) ;
          } catch (ItemExistsException e) { 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          } catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryId(evCate.getId()) ;
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(calendarId) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_EVENT) ;

        String sValue = "" ;
        String eValue = "" ;
        if(event.getStartDate() != null) {
          sValue = event.getStartDate().getValue() ;
          exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        }
        if(event.getEndDate() != null) {
          eValue = event.getEndDate().getValue() ;
          exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        }
        if (sValue.length() == 8 && eValue.length() == 8 ) {
          //exoEvent.setAllday(true) ;
          exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
        }
        if (sValue.length() > 8 && eValue.length() > 8 ) {         
          if("0000".equals(sValue.substring(9,13)) && "0000".equals(eValue.substring(9,13)) ) {
            //exoEvent.setAllday(true);
            exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
          }
        }
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        if(event.getPriority() != null) exoEvent.setPriority(CalendarEvent.PRIORITY[Integer.parseInt(event.getPriority().getValue())] ) ;
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        calService.saveUserEvent(username, calendarId, exoEvent, true) ;
        storage_.setRemoteEvent(username, calendarId, exoEvent.getId(), href, etag);
      } else if (obj instanceof VToDo) {
        VToDo event = (VToDo)obj ;
        exoEvent = new CalendarEvent() ;
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try{
            calService.saveEventCategory(username, evCate, true) ;
          } catch(ItemExistsException e){ 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          } catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(calendarId) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        if(event.getStartDate() != null) exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        if(event.getDue() != null) exoEvent.setToDateTime(event.getDue().getDate()) ;
        //if(event.getEndDate() != null) exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        if(event.getPriority() != null) exoEvent.setPriority(CalendarEvent.PRIORITY[Integer.parseInt(event.getPriority().getValue())] ) ;
        if(vFreeBusyData.get(event.getUid().getValue()) != null) {
          exoEvent.setStatus(CalendarEvent.ST_BUSY) ;
        }
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        calService.saveUserEvent(username, calendarId, exoEvent, true) ;
        storage_.setRemoteEvent(username, calendarId, exoEvent.getId(), href, etag);
      }
    }
  }
  
  /**
   * Update local event from iCalendar object
   * @param username
   * @param calendarId
   * @param eventId
   * @param iCalendar
   * @param etag
   */
  public void updateRemoteCalendarEvent(String username, String calendarId, String eventId, net.fortuna.ical4j.model.Calendar iCalendar, String etag) throws Exception {
    CalendarService  calService = (CalendarService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if(calService == null) {
      calService = (CalendarService) ExoContainerContext.getContainerByName(PortalContainer.getCurrentPortalContainerName()).getComponentInstanceOfType(CalendarService.class);
    }
    
    Map<String, VFreeBusy> vFreeBusyData = new HashMap<String, VFreeBusy>() ;
    Map<String, VAlarm> vAlarmData = new HashMap<String, VAlarm>() ;
    
    Calendar eXoCalendar = calService.getUserCalendar(username, calendarId);
    
    // import calendar components
    ComponentList componentList = iCalendar.getComponents() ;
    CalendarEvent exoEvent ;
    for(Object obj : componentList) {
      if(obj instanceof VEvent) {
        VEvent v = (VEvent)obj ;
        if(!v.getAlarms().isEmpty()) {
          for (Object o : v.getAlarms()) {
            if (o instanceof VAlarm) {
              VAlarm va = (VAlarm)o;
              vAlarmData.put(v.getUid().getValue()+":"+ va.getProperty(Property.ACTION).getName(), va) ;
            }
          }
        }

      }
      if(obj instanceof VFreeBusy) vFreeBusyData.put(((VFreeBusy)obj).getUid().getValue(), (VFreeBusy)obj) ;
    }
    for(Object obj : componentList) {
      if(obj instanceof VEvent){
        VEvent event = (VEvent)obj ;
        //exoEvent = new CalendarEvent() ;
        exoEvent = calService.getEvent(username, eventId);
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try {
            calService.saveEventCategory(username, evCate, true) ;
          } catch (ItemExistsException e) { 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          } catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryId(evCate.getId()) ;
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(calendarId) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_EVENT) ;

        String sValue = "" ;
        String eValue = "" ;
        if(event.getStartDate() != null) {
          sValue = event.getStartDate().getValue() ;
          exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        }
        if(event.getEndDate() != null) {
          eValue = event.getEndDate().getValue() ;
          exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        }
        if (sValue.length() == 8 && eValue.length() == 8 ) {
          //exoEvent.setAllday(true) ;
          exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
        }
        if (sValue.length() > 8 && eValue.length() > 8 ) {         
          if("0000".equals(sValue.substring(9,13)) && "0000".equals(eValue.substring(9,13)) ) {
            //exoEvent.setAllday(true);
            exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() -1)) ;
          }
        }
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        if(event.getPriority() != null) exoEvent.setPriority(CalendarEvent.PRIORITY[Integer.parseInt(event.getPriority().getValue())] ) ;
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        calService.saveUserEvent(username, calendarId, exoEvent, false) ;
        storage_.setRemoteEvent(username, calendarId, exoEvent.getId(), null, etag);
      } else if (obj instanceof VToDo) {
        VToDo event = (VToDo)obj ;
        exoEvent = new CalendarEvent() ;
        if(event.getProperty(Property.CATEGORIES) != null) {
          EventCategory evCate = new EventCategory() ;
          evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim()) ;
          try{
            calService.saveEventCategory(username, evCate, true) ;
          } catch(ItemExistsException e){ 
            evCate = calService.getEventCategoryByName(username, evCate.getName());
          } catch (Exception e) {
            e.printStackTrace();
          }
          exoEvent.setEventCategoryName(evCate.getName()) ;
        } 
        exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
        exoEvent.setCalendarId(calendarId) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        if(event.getStartDate() != null) exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        if(event.getDue() != null) exoEvent.setToDateTime(event.getDue().getDate()) ;
        //if(event.getEndDate() != null) exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        if(event.getPriority() != null) exoEvent.setPriority(CalendarEvent.PRIORITY[Integer.parseInt(event.getPriority().getValue())] ) ;
        if(vFreeBusyData.get(event.getUid().getValue()) != null) {
          exoEvent.setStatus(CalendarEvent.ST_BUSY) ;
        }
        if(event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue()) ;
        }
        if(event.getClassification() != null) exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue())) ;
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(!attendees.isEmpty()) {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        try {
          PropertyList dataList = event.getProperties(Property.ATTACH) ;
          List<Attachment> attachments = new ArrayList<Attachment>() ;
          for(Object o : dataList) {
            Attach a = (Attach)o ;
            Attachment att = new Attachment() ;
            att.setName(a.getParameter(Parameter.CN).getValue())  ;
            att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue()) ;
            InputStream in = new ByteArrayInputStream(a.getBinary()) ;
            att.setSize(in.available());
            att.setInputStream(in) ;
            attachments.add(att) ;
          }
          if(!attachments.isEmpty()) exoEvent.setAttachment(attachments) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        calService.saveUserEvent(username, calendarId, exoEvent, false) ;
        storage_.setRemoteEvent(username, calendarId, exoEvent.getId(), null, etag);
      }
    }
  }
  
  
}


