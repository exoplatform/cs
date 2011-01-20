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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;

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
import org.exoplatform.calendar.service.RemoteCalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 10, 2011  
 */
public class RemoteCalendarServiceImpl implements RemoteCalendarService {
  
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
      throw new IOException("Remote URL is invalid. Maybe no legal protocol or URl could not be parsed");
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
    
    InputStream icalInputStream = connectToRemoteServer(remoteUrl, remoteType, remoteUser, remotePassword);
    Calendar eXoCalendar = storage_.createRemoteCalendar(username, calendarName, remoteUrl, remoteType, syncPeriod, remoteUser, remotePassword);
    importRemoteCalendar(username, eXoCalendar.getId(), icalInputStream);
    storage_.setRemoteCalendarLastUpdated(username, eXoCalendar.getId(), Utils.getGreenwichMeanTime());
    icalInputStream.close();
    return eXoCalendar;
  }

  // TODO: improve this function follow CalDav synchronization operations specification to update faster (avoid fully reload)
  @Override
  public Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception {
    if (!storage_.isRemoteCalendar(username, remoteCalendarId)) {
      return null;
    }
    
    Node calendarNode = storage_.getUserCalendarHome(username).getNode(remoteCalendarId);
    String remoteUrl = calendarNode.getProperty(Utils.EXO_REMOTE_URL).getString();
    String remoteUser = calendarNode.getProperty(Utils.EXO_REMOTE_USERNAME).getString();
    String remotePassword = calendarNode.getProperty(Utils.EXO_REMOTE_PASSWORD).getString();
    String remoteType = calendarNode.getProperty(Utils.EXO_REMOTE_TYPE).getString();
    
    InputStream icalInputStream = null;
    
    icalInputStream = connectToRemoteServer(remoteUrl, remoteType, remoteUser, remotePassword);
    
    /*if (!isValidate(icalInputStream)) {
      logger.debug("Error when parsing input stream. The input stream is not in iCalendar format.");
      throw new IOException("Can't parsing iCalendar input stream");
    }*/
    
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
  
  public boolean isValidate(InputStream icalInputStream) throws Exception {
    try {
      CalendarBuilder calendarBuilder = new CalendarBuilder() ;
      calendarBuilder.build(icalInputStream) ;
      return true ;
    } catch (Exception e) {
      e.printStackTrace() ;
      return false ;
    }
  }

}
