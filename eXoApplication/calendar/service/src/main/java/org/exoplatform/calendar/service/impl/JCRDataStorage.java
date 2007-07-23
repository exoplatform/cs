/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.CalendarParserImpl;
import net.fortuna.ical4j.data.ContentHandler;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.Event;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage implements DataStorage{
  final private static String CALENDARS = "calendars".intern() ;
  final private static String EVENTS = "events".intern() ;
  final private static String TASKS = "tasks".intern() ;
  final private static String CALENDAR_CATEGORIES = "categories".intern() ;
  
  private RepositoryService  repositoryService_ ; 
  private JCRRegistryService jcrRegistryService_ ;
  
  public JCRDataStorage(RepositoryService  repositoryService, 
                        JCRRegistryService jcrRegistryService)throws Exception {
    repositoryService_ = repositoryService ;
    jcrRegistryService_ = jcrRegistryService ;
  }  
  
  private Node getCalendarServiceHome() throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("CalendarService") ;
    Session session = getJCRSession() ;
    jcrRegistryService_.createServiceRegistry(serviceRegistry, false) ;    
    return jcrRegistryService_.getServiceRegistryNode(session, serviceRegistry.getName()) ;
  }
  
  private Node getCalendarHome() throws Exception {
    Node calendarServiceHome = getCalendarServiceHome() ;
    if(calendarServiceHome.hasNode(CALENDARS)) return calendarServiceHome.getNode(CALENDARS) ;
    return calendarServiceHome.addNode(CALENDARS) ;
  }
  
  private Node getCalendarServiceHome(String username) throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("CalendarService") ;
    Session session = getJCRSession() ;
    if(jcrRegistryService_.getUserNode(session, username) == null)
      jcrRegistryService_.createUserHome(username, false) ;
    jcrRegistryService_.createServiceRegistry(username, serviceRegistry, false) ;    
    return jcrRegistryService_.getServiceRegistryNode(session, username, serviceRegistry.getName()) ;
  }
  
  private Node getCalendarHome(String username) throws Exception {
    Node calendarServiceHome = getCalendarServiceHome(username) ;
    if(calendarServiceHome.hasNode(CALENDARS)) return calendarServiceHome.getNode(CALENDARS) ;
    return calendarServiceHome.addNode(CALENDARS) ;
  }
  
  private Node getCalendarCategoryHome(String username) throws Exception {
    Node calendarServiceHome = getCalendarServiceHome(username) ;
    if(calendarServiceHome.hasNode(CALENDAR_CATEGORIES)) return calendarServiceHome.getNode(CALENDAR_CATEGORIES) ;
    return calendarServiceHome.addNode(CALENDAR_CATEGORIES) ;
  }
  
  private Session getJCRSession() throws Exception {
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
  }

  public Calendar getCalendar(String username, String calendarId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;
    return getCalendar(calendarNode) ;
  }
  
  public Calendar getCalendar(String calendarId) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendarId) ;
    return getCalendar(calendarNode) ;
  }
  
  public List<Calendar> getAllCalendars(String username) throws Exception {
    NodeIterator iter = getCalendarHome(username).getNodes() ;
    List<Calendar> calList = new ArrayList<Calendar>() ;
    while(iter.hasNext()) {
      calList.add(getCalendar(iter.nextNode())) ;
    }
    return calList ;
  }
  
  public List<Calendar> getCalendarsByCategory(String username, String calendarCategoryId) throws Exception {
    Node calendarHome = getCalendarHome(username) ;    
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + calendarHome.getPath() 
                                                + "//element(*,exo:calendar)[@exo:categoryId='").
                                  append(calendarCategoryId).
                                  append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<Calendar> calendares = new ArrayList<Calendar> () ;
    while(it.hasNext()){
      calendares.add(getCalendar(it.nextNode())) ;
    }
    return calendares;
  }

  public List<Calendar> getCalendarsByGroup(String groupName) throws Exception {
    Node calendarHome = getCalendarHome() ;    
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + calendarHome.getPath() 
                                                + "//element(*,exo:calendar)[@exo:groups='").
                                  append(groupName).
                                  append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<Calendar> calendares = new ArrayList<Calendar> () ;
    while(it.hasNext()){
      calendares.add(getCalendar(it.nextNode())) ;
    }
    return calendares;
  }
  
  public void saveCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    Node calendarHome  ;
    if(calendar.isPrivate()) calendarHome = getCalendarHome(username) ;
    else calendarHome = getCalendarHome() ;
    Node calendarNode ;
    if(isNew) {
      if(calendarHome.hasNode(calendar.getId())) throw new Exception("This calendar is already exists") ;
      calendarNode = calendarHome.addNode(calendar.getId(), "exo:calendar") ;
      calendarNode.setProperty("exo:id", calendar.getId()) ;
    }else {
      calendarNode = calendarHome.getNode(calendar.getId()) ;
    }    
    calendarNode.setProperty("exo:name", calendar.getName()) ;
    calendarNode.setProperty("exo:description", calendar.getDescription()) ;
    calendarNode.setProperty("exo:categoryId", calendar.getCategoryId()) ;
    calendarNode.setProperty("exo:viewPermissions", calendar.getViewPermission()) ;
    calendarNode.setProperty("exo:editPermissions", calendar.getEditPermission()) ;
    calendarNode.setProperty("exo:groups", calendar.getGroups()) ;
    calendarHome.getSession().save() ;   
  }

  public Calendar removeCalendar(String username, String calendarId) throws Exception {
    Node calendarHome = getCalendarHome(username) ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(calNode) ;
      calNode.remove() ;
      calendarHome.save() ;
      calendarHome.getSession().save() ;
      return calendar ;
    }
    return null ;
  }
  
  public Calendar removeCalendar(String calendarId) throws Exception {
    Node calendarHome = getCalendarHome() ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(calNode) ;
      calNode.remove() ;
      calendarHome.save() ;
      calendarHome.getSession().save() ;
      return calendar ;
    }
    return null ;
  }
  
  private Calendar getCalendar(Node calNode) throws Exception {
    Calendar calendar = new Calendar() ;
    if(calNode.hasProperty("exo:id")) calendar.setId(calNode.getProperty("exo:id").getString()) ;
    if(calNode.hasProperty("exo:name")) calendar.setName(calNode.getProperty("exo:name").getString()) ;
    if(calNode.hasProperty("exo:description")) calendar.setDescription(calNode.getProperty("exo:description").getString()) ;
    if(calNode.hasProperty("exo:categoryId")) calendar.setCategoryId(calNode.getProperty("exo:categoryId").getString()) ;
    if(!calendar.isPrivate()) {
      if(calNode.hasProperty("exo:groups")){
        Value[] values = calNode.getProperty("exo:groups").getValues() ;
        if(values.length == 1 ){      
          calendar.setGroups(new String[]{values[0].getString()}) ;
        }else {
          String[] groups = new String[values.length] ;
          for(int i = 0; i < values.length - 1; i ++) {
            groups[i] = values[i].getString() ;
          }
          calendar.setGroups(groups) ;
        }
      }
      
      if(calNode.hasProperty("exo:viewPermissions")) {
        Value[] viewValues = calNode.getProperty("exo:viewPermissions").getValues() ;
        if(viewValues.length == 1 ){      
          calendar.setViewPermission(new String[]{viewValues[0].getString()}) ;
        }else {
          String[] views = new String[viewValues.length] ;
          for(int i = 0; i < viewValues.length - 1; i ++) {
            views[i] = viewValues[i].getString() ;
          }
          calendar.setViewPermission(views) ;
        }
      }
      
      if(calNode.hasProperty("exo:editPermissions")) {
        Value[] editValues = calNode.getProperty("exo:editPermissions").getValues() ;
        if(editValues.length == 1 ){      
          calendar.setEditPermission(new String[]{editValues[0].getString()}) ;
        }else {
          String[] edits = new String[editValues.length] ;
          for(int i = 0; i < editValues.length - 1; i ++) {
            edits[i] = editValues[i].getString() ;
          }
          calendar.setEditPermission(edits) ;
        }
      }      
    }
        
    return calendar ;
  }  
  
  public List<CalendarCategory> getCalendarCategories(String username) throws Exception {
    Node calendarCategoryHome = getCalendarCategoryHome(username) ;
    NodeIterator iter = calendarCategoryHome.getNodes() ;
    List<CalendarCategory> calendarCategories = new ArrayList<CalendarCategory> () ;
    while(iter.hasNext()) {
      calendarCategories.add(getCalendarCategory(iter.nextNode())) ;
    }
    return calendarCategories;
  }
  
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception {
    Node calendarCategoryHome = getCalendarCategoryHome(username) ;
    return getCalendarCategory(calendarCategoryHome.getNode(calendarCategoryId)) ;
  }
  
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    Node calCategoryHome = getCalendarCategoryHome(username) ;
    Node calCategoryNode ;
    if(isNew) {
      if(calCategoryHome.hasNode(calendarCategory.getId())) throw new Exception("This calendar category is already exists! ") ;
      calCategoryNode = calCategoryHome.addNode(calendarCategory.getId(),"exo:calendarCategory") ;
      calCategoryNode.setProperty("exo:id", calendarCategory.getId()) ;
    }else {
      calCategoryNode = calCategoryHome.getNode(calendarCategory.getId()) ;
    }
    calCategoryNode.setProperty("exo:name", calendarCategory.getName()) ;
    calCategoryNode.setProperty("exo:description", calendarCategory.getDescription()) ;
    calCategoryNode.setProperty("exo:calendarIds", calendarCategory.getCalendars()) ;
    calCategoryHome.getSession().save() ;
  }
  
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    Node calCategoryHome = getCalendarCategoryHome(username) ;
    Node calCategoryNode = calCategoryHome.getNode(calendarCategoryId) ; 
    CalendarCategory calCategory = getCalendarCategory(calCategoryNode) ;
    calCategoryNode.remove() ;
    calCategoryHome.save() ;
    calCategoryHome.getSession().save() ;
    return calCategory ;
  }
  
  private CalendarCategory getCalendarCategory(Node calCategoryNode) throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    if(calCategoryNode.hasProperty("exo:id")) calCategory.setId(calCategoryNode.getProperty("exo:id").getString()) ;
    if(calCategoryNode.hasProperty("exo:name")) calCategory.setName(calCategoryNode.getProperty("exo:name").getString()) ;
    if(calCategoryNode.hasProperty("exo:description")) calCategory.setDescription(calCategoryNode.getProperty("exo:description").getString()) ;
    if(calCategoryNode.hasProperty("exo:calendarIds")) {
      Value[] calendarValues = calCategoryNode.getProperty("exo:calendarIds").getValues() ;
      if(calendarValues.length == 1 ){      
        calCategory.setCalendars(new String[]{calendarValues[0].getString()}) ;
      }else {
        String[] calendars = new String[calendarValues.length] ;
        for(int i = 0; i < calendarValues.length - 1; i ++) {
          calendars[i] = calendarValues[i].getString() ;
        }
        calCategory.setCalendars(calendars) ;
      }
    }   
    return calCategory ;
  }
  
  public EventCategory getEventCategory(String username, String calendarId, String eventCategoryId) throws Exception {
    Node calendarNode ;
    if(username != null) calendarNode = getCalendarHome(username).getNode(calendarId) ;
    else calendarNode = getCalendarHome().getNode(calendarId) ;
    return getEventCategory(calendarNode.getNode(eventCategoryId));
  }
  
  public List<EventCategory> getEventCategories(String username, String calendarId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;
    NodeIterator iter = calendarNode.getNodes() ;
    List<EventCategory> categories = new ArrayList<EventCategory> ();
    while(iter.hasNext()) {
      Node eventCat = iter.nextNode() ;
      if(eventCat.isNodeType("exo:eventCategory")) categories.add(getEventCategory(eventCat)) ;
    }
    return categories ;
  }
  
  public void saveEventCategory(String username, String calendarId, EventCategory eventCategory, boolean isNew) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;
    Node eventCategoryNode ;
    if(isNew){
      if(calendarNode.hasNode(eventCategory.getId())) throw new Exception("This event category is already exists!") ;
      eventCategoryNode = calendarNode.addNode(eventCategory.getId(), "exo:eventCategory") ;
      eventCategoryNode.setProperty("exo:id", eventCategory.getId()) ;
    }else {
      eventCategoryNode = calendarNode.getNode(eventCategory.getId()) ;
    }
    eventCategoryNode.setProperty("exo:name", eventCategory.getName()) ;
    eventCategoryNode.setProperty("exo:description", eventCategory.getDescription()) ;
    calendarNode.save() ;
    calendarNode.getSession().save() ;
  }
  
  public EventCategory removeEventCategory(String username, String calendarId, String eventCategoryId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;
    Node eventCategoryNode = calendarNode.getNode(eventCategoryId) ;
    EventCategory eventCategory = getEventCategory(eventCategoryNode) ;
    eventCategoryNode.remove() ;
    calendarNode.save() ;
    calendarNode.getSession().save() ;
    return eventCategory ;
  }
  
  private EventCategory getEventCategory(Node eventCatNode) throws Exception {
    EventCategory eventCategory = new EventCategory() ;
    if(eventCatNode.hasProperty("exo:id")) eventCategory.setId(eventCatNode.getProperty("exo:id").getString()) ;
    if(eventCatNode.hasProperty("exo:name")) eventCategory.setName(eventCatNode.getProperty("exo:name").getString()) ;
    if(eventCatNode.hasProperty("exo:description")) eventCategory.setDescription(eventCatNode.getProperty("exo:description").getString()) ;
    return eventCategory ;
  }
  
  public Event getEvent(String username, String calendarId, String eventCategoryId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;
    if(eventCategoryId != null) {
      return getEvent(calendarNode.getNode(eventCategoryId).getNode(eventId)) ;
    }
    return getEvent(calendarNode.getNode(eventId)) ;
  }

  public Event getEvent(String calendarId, String eventCategoryId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendarId) ;
    if(eventCategoryId != null) {
      return getEvent(calendarNode.getNode(eventCategoryId).getNode(eventId)) ;
    }
    return getEvent(calendarNode.getNode(eventId)) ;
  }
  
  public List<Event> getEventByCalendar(String username, String calendarId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;    
    QueryManager qm = calendarNode.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + calendarNode.getPath() 
                                                + "//element(*,exo:calendarEvent)") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<Event> events = new ArrayList<Event>() ;
    while(it.hasNext()) {
      events.add(getEvent(it.nextNode())) ;
    }
    return events ;
  }

  public List<Event> getEventByCalendar(String calendarId) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendarId) ;    
    QueryManager qm = calendarNode.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + calendarNode.getPath() 
                                                + "//element(*,exo:calendarEvent)") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<Event> events = new ArrayList<Event>() ;
    while(it.hasNext()) {
      events.add(getEvent(it.nextNode())) ;
    }
    return events ;
  }
  
  public void saveEvent(String username, String calendarId, String eventCategoryId, Event event, boolean isNew, boolean isPublicCalendar) throws Exception {
    Node calendarNode ;
    Node eventNode ;
    if(isPublicCalendar) calendarNode = getCalendarHome().getNode(calendarId) ;
    else calendarNode = getCalendarHome(username).getNode(calendarId) ;
    if(isNew) {
      if(eventCategoryId != null) {
        eventNode = calendarNode.getNode(eventCategoryId).addNode(event.getId(), "exo:calendarEvent") ;
      }else {
        eventNode = calendarNode.addNode(event.getId(), "exo:calendarEvent") ;
      }      
      eventNode.setProperty("exo:id", event.getId()) ;
    }else {
      if(eventCategoryId != null) {
        eventNode = calendarNode.getNode(eventCategoryId).getNode(event.getId()) ;
      }else {
        eventNode = calendarNode.getNode(event.getId()) ;
      }
    }
    eventNode.setProperty("exo:summary", event.getSummary()) ;
    eventNode.setProperty("exo:calendarId", event.getCalendarId()) ;
    eventNode.setProperty("exo:eventCategoryId", event.getEventCategoryId()) ;
    eventNode.setProperty("exo:location", event.getLocation()) ;
    eventNode.setProperty("exo:description", event.getDescription()) ;
    GregorianCalendar dateTime = new GregorianCalendar() ;
    dateTime.setTime(event.getFromDateTime()) ;
    eventNode.setProperty("exo:fromDateTime", dateTime.getInstance()) ;
    dateTime.setTime(event.getToDateTime()) ;
    eventNode.setProperty("exo:toDateTime", dateTime.getInstance()) ;
    eventNode.setProperty("exo:eventType", event.getEventType()) ;
    eventNode.setProperty("exo:priority", event.getPriority()) ;
    eventNode.setProperty("exo:isPrivate", event.isPrivate()) ;
    eventNode.setProperty("exo:eventState", event.getEventState()) ;
    eventNode.setProperty("exo:invitation", event.getInvitation()) ;
    // add reminder child node
    List<Reminder> reminders = event.getReminders() ;
    for(Reminder rm : reminders) {
      addReminder(eventNode, rm, isNew) ;
    }
    calendarNode.save() ;
    calendarNode.getSession().save() ;
  }
  
  public Event removeEvent(String username, String calendarId, String eventCategoryId, String eventId, boolean isPublicCalendar) throws Exception {
    Node calendarNode ;
    Node eventNode ;
    if(isPublicCalendar) calendarNode = getCalendarHome().getNode(calendarId) ;
    else calendarNode = getCalendarHome(username).getNode(calendarId) ;
    if(eventCategoryId != null) eventNode = calendarNode.getNode(eventCategoryId).getNode(eventId) ;
    else eventNode = calendarNode.getNode(eventId) ;
    Event event = getEvent(eventNode) ;
    eventNode.remove() ;
    calendarNode.save() ;
    calendarNode.getSession().save() ;
    return event;
  }
  
  private void addReminder(Node eventNode, Reminder reminder, boolean isNew) throws Exception {
    Node reminderNode ;
    if(isNew) {
      reminderNode = eventNode.addNode(reminder.getId(), "exo:reminder") ;
      reminderNode.setProperty("exo:id", reminder.getId()) ;
    }else {
      reminderNode = eventNode.getNode(reminder.getId()) ;
    }
    reminderNode.setProperty("exo:eventId", reminder.getEventId()) ;
    reminderNode.setProperty("exo:alarmBefore", reminder.getAlarmBefore()) ;
    reminderNode.setProperty("exo:repeat", reminder.getRepeat()) ;
    reminderNode.setProperty("exo:reminder", reminder.getReminder()) ;
  }
  
  private Event getEvent(Node eventNode) throws Exception {
    Event event = new Event() ;
    if(eventNode.hasProperty("exo:id")) event.setId(eventNode.getProperty("exo:id").getString()) ;
    if(eventNode.hasProperty("exo:calendarId"))event.setCalendarId(eventNode.getProperty("exo:calendarId").getString()) ;
    if(eventNode.hasProperty("exo:summary")) event.setSummary(eventNode.getProperty("exo:summary").getString()) ;
    if(eventNode.hasProperty("exo:eventCategoryId")) event.setEventCategoryId(eventNode.getProperty("exo:eventCategoryId").getString()) ;
    if(eventNode.hasProperty("exo:location")) event.setLocation(eventNode.getProperty("exo:location").getString()) ;
    if(eventNode.hasProperty("exo:description")) event.setDescription(eventNode.getProperty("exo:description").getString()) ;
    if(eventNode.hasProperty("exo:fromDateTime")) event.setFromDateTime(eventNode.getProperty("exo:fromDateTime").getDate().getTime()) ;
    if(eventNode.hasProperty("exo:toDateTime")) event.setToDateTime(eventNode.getProperty("exo:toDateTime").getDate().getTime()) ;
    if(eventNode.hasProperty("exo:eventType")) event.setEventType(eventNode.getProperty("exo:eventType").getString()) ;
    if(eventNode.hasProperty("exo:priority")) event.setPriority(eventNode.getProperty("exo:priority").getString()) ;
    if(eventNode.hasProperty("exo:isPrivate")) event.setPrivate(eventNode.getProperty("exo:isPrivate").getBoolean()) ;
    if(eventNode.hasProperty("exo:eventState")) event.setEventState(eventNode.getProperty("exo:eventState").getString()) ;
    event.setReminders(getReminders(eventNode)) ;
    if(eventNode.hasProperty("exo:invitation")){
      Value[] values = eventNode.getProperty("exo:invitation").getValues() ;
      if(values.length == 1 ){      
        event.setInvitation(new String[]{values[0].getString()}) ;
      }else {
        String[] invites = new String[values.length] ;
        for(int i = 0; i < values.length; i ++) {
          invites[i] = values[i].getString() ;
        }
        event.setInvitation(invites) ;
      }
    }    
    return event ;
  }
  
  private List<Reminder> getReminders(Node eventNode) throws Exception {
    NodeIterator iter = eventNode.getNodes() ;
    List<Reminder> reminders = new ArrayList<Reminder> () ;
    while(iter.hasNext()) {
      Node reminderNode = iter.nextNode() ;
      if(reminderNode.isNodeType("exo:reminder")) {
        Reminder reminder = new Reminder() ;
        if(reminderNode.hasProperty("exo:id")) reminder.setId(reminderNode.getProperty("exo:id").getString()) ;
        if(reminderNode.hasProperty("exo:eventId")) reminder.setEventId(reminderNode.getProperty("exo:eventId").getString()) ;
        if(reminderNode.hasProperty("exo:reminder")) reminder.setReminder(reminderNode.getProperty("exo:reminder").getString()) ;
        if(reminderNode.hasProperty("exo:alarmBefore"))reminder.setAlarmBefore(reminderNode.getProperty("exo:alarmBefore").getString()) ;
        if(reminderNode.hasProperty("exo:repeat")) reminder.setRepeat(reminderNode.getProperty("exo:repeat").getString()) ;
        reminders.add(reminder) ;
      }
    }
    return reminders ;
  }
  
  private String getEventCategoryId(String username, String calendarId, String eventCategoryName) throws Exception {
    List<EventCategory> eventCatList = getEventCategories(username, calendarId) ;
    if(eventCatList.size() < 1) return null ;
    for(EventCategory evCat : eventCatList) {
      if(evCat.getName().equals(eventCategoryName)) return evCat.getId() ;
    }
    return null ;
  }
  public void importICalendar(String username, InputStream icalInputStream) throws Exception {
    CalendarBuilder calendarBuilder = new CalendarBuilder() ;
    net.fortuna.ical4j.model.Calendar iCalendar = calendarBuilder.build(icalInputStream) ;
    GregorianCalendar currentDateTime = new GregorianCalendar() ;
    NodeIterator iter = getCalendarCategoryHome(username).getNodes() ;
    Node cat = null;
    String categoryId ;
    boolean isExists = false ;
    while(iter.hasNext()) {
      cat = iter.nextNode() ;
      if(cat.getProperty("exo:name").getString().equals("Imported")) {
        isExists = true ;
      }
    }
    if(!isExists) {
      CalendarCategory calendarCate = new CalendarCategory() ;
      currentDateTime = new GregorianCalendar() ;
      calendarCate.setId(String.valueOf(currentDateTime.getTimeInMillis())) ;
      calendarCate.setDescription("Imported icalendar category") ;
      calendarCate.setName("Imported") ;
      categoryId = calendarCate.getId() ;
      saveCalendarCategory(username, calendarCate, true) ;
    }else {
      categoryId = cat.getProperty("exo:id").getString() ;
    }
    Calendar exoCalendar = new Calendar() ;
    exoCalendar.setId(String.valueOf(currentDateTime.getTimeInMillis())) ;
    exoCalendar.setName(iCalendar.getProductId().getValue()) ;
    exoCalendar.setDescription(iCalendar.getProductId().getValue()) ;
    exoCalendar.setCategoryId(categoryId) ;
    exoCalendar.setPrivate(true) ;
    saveCalendar(username, exoCalendar, true) ;   
    
    ComponentList componentList = iCalendar.getComponents() ;
    VEvent event ;
    Event exoEvent ;
    for(Object obj : componentList) {
      if(obj instanceof VEvent){
        event = (VEvent)obj ;
        String eventCategoryId = null ;
        if(event.getProperty(Property.CATEGORIES) != null) {
          eventCategoryId = getEventCategoryId(username, exoCalendar.getId(), 
                                         event.getProperty(Property.CATEGORIES).getValue()) ;
          if(eventCategoryId == null){
            currentDateTime = new GregorianCalendar() ;
            EventCategory evCate = new EventCategory() ;
            evCate.setId(String.valueOf(currentDateTime.getTimeInMillis())) ;
            evCate.setName(event.getProperty(Property.CATEGORIES).getValue()) ;
            evCate.setDescription(event.getProperty(Property.CATEGORIES).getValue()) ;
            saveEventCategory(username, exoCalendar.getId(), evCate, true) ;
            eventCategoryId = evCate.getId() ;
          }          
        }
        exoEvent = new Event() ;
        currentDateTime = new GregorianCalendar() ;
        exoEvent.setId(String.valueOf(currentDateTime.getTimeInMillis())) ;
        exoEvent.setCalendarId(exoCalendar.getId()) ;
        exoEvent.setEventCategoryId(eventCategoryId) ;
        if(event.getSummary() != null) exoEvent.setSummary(event.getSummary().getValue()) ;
        if(event.getDescription() != null) exoEvent.setDescription(event.getDescription().getValue()) ;
        if(event.getStatus() != null) exoEvent.setStatus(event.getStatus().getValue()) ;
        exoEvent.setEventType("event") ;
        if(event.getStartDate() != null) exoEvent.setFromDateTime(event.getStartDate().getDate()) ;
        if(event.getEndDate() != null) exoEvent.setToDateTime(event.getEndDate().getDate()) ;
        if(event.getLocation() != null) exoEvent.setLocation(event.getLocation().getValue()) ;
        if(event.getPriority() != null) exoEvent.setPriority(event.getPriority().getValue()) ;
        exoEvent.setPrivate(true) ;
        Reminder reminder = new Reminder() ;
        currentDateTime = new GregorianCalendar() ;
        reminder.setId(String.valueOf(currentDateTime.getTimeInMillis())) ;
        reminder.setEventId(exoEvent.getId()) ;
        List<Reminder> reminders = new ArrayList<Reminder>() ;
        reminders.add(reminder) ;
        exoEvent.setReminders(reminders) ;
        PropertyList attendees = event.getProperties(Property.ATTENDEE) ;
        if(attendees.size() < 1) {
          exoEvent.setInvitation(new String[]{}) ;
        }else {
          String[] invitation = new String[attendees.size()] ;
          for(int i = 0; i < attendees.size(); i ++) {
            invitation[i] = ((Attendee)attendees.get(i)).getValue() ;
          }
          exoEvent.setInvitation(invitation) ;
        }
        saveEvent(username, exoCalendar.getId(),eventCategoryId, exoEvent, true, false) ;
      }
    }
  }
  
  public String exportICalendar(String username, String calendarId) throws Exception {
    List<Event> events ;
    if(username != null) events = getEventByCalendar(username, calendarId) ;
    else events = getEventByCalendar(calendarId) ;
    
    net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    
    for(Event exoEvent : events) {
      long start = exoEvent.getFromDateTime().getTime() ;
      long end = exoEvent.getToDateTime().getTime() ;
      String summary = exoEvent.getSummary() ;
      VEvent event ;
      if(end > 0) {
        event = new VEvent(new DateTime(start), new DateTime(end), summary);
        event.getProperties().getProperty(Property.DTEND).getParameters()
        .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
      }else {
        event = new VEvent(new DateTime(start), summary);            
      }
      event.getProperties().getProperty(Property.DTSTART).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME); 
      
      event.getProperties().add(new Description(exoEvent.getDescription()));
      event.getProperties().getProperty(Property.DESCRIPTION).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
      
      event.getProperties().add(new Location(exoEvent.getLocation()));
      event.getProperties().getProperty(Property.LOCATION).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
      
      if(exoEvent.getEventCategoryId() != null){
        EventCategory category = getEventCategory(username, calendarId, exoEvent.getEventCategoryId()) ;  
        event.getProperties().add(new Categories(category.getName())) ;
        event.getProperties().getProperty(Property.CATEGORIES).getParameters()
        .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
      }
      event.getProperties().add(new Priority(Integer.parseInt(exoEvent.getPriority())));
      event.getProperties().getProperty(Property.PRIORITY).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.INTEGER);  
      
      if(exoEvent.getEventType().equals("task")) {
        long completed = exoEvent.getCompletedDateTime().getTime() ;
        event.getProperties().add(new Completed(new DateTime(completed)));
        event.getProperties().getProperty(Property.COMPLETED).getParameters()
        .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
        
        event.getProperties().add(new Due(new DateTime(end)));
        event.getProperties().getProperty(Property.DUE).getParameters()
        .add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
        
        event.getProperties().add(new Status(exoEvent.getStatus()));
        event.getProperties().getProperty(Property.STATUS).getParameters()
        .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
      }
      String[] attendees = exoEvent.getInvitation() ; 
      for(int i = 0; i < attendees.length; i++ ) {
        if(attendees[i] != null) {
          event.getProperties().add(new Attendee(attendees[i]));          
        }
      }
      event.getProperties().getProperty(Property.ATTENDEE).getParameters()
      .add(net.fortuna.ical4j.model.parameter.Value.TEXT);
      Uid id = new Uid(exoEvent.getId()) ; 
      event.getProperties().add(id) ; 
      calendar.getComponents().add(event);
    }
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    CalendarOutputter output = new CalendarOutputter();
    output.output(calendar, bout) ;
    return bout.toString() ;
  }
}
