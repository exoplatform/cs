/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;

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

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.Event;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
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
  final private static String CALENDAR_GROUPS = "groups".intern() ;
  
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
  
  protected Node getCalendarCategoryHome(String username) throws Exception {
    Node calendarServiceHome = getCalendarServiceHome(username) ;
    if(calendarServiceHome.hasNode(CALENDAR_CATEGORIES)) return calendarServiceHome.getNode(CALENDAR_CATEGORIES) ;
    return calendarServiceHome.addNode(CALENDAR_CATEGORIES) ;
  }
  
  private Node getCalendarGroupHome() throws Exception {
    Node calendarServiceHome = getCalendarServiceHome() ;
    if(calendarServiceHome.hasNode(CALENDAR_GROUPS)) return calendarServiceHome.getNode(CALENDAR_GROUPS) ;
    return calendarServiceHome.addNode(CALENDAR_GROUPS) ;
  }
  
  private Session getJCRSession() throws Exception {
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
  }

  public Calendar getUserCalendar(String username, String calendarId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;
    return getCalendar(username, calendarNode) ;
  }
  public List<Calendar> getUserCalendars(String username) throws Exception {
    NodeIterator iter = getCalendarHome(username).getNodes() ;
    List<Calendar> calList = new ArrayList<Calendar>() ;
    while(iter.hasNext()) {
      calList.add(getCalendar(username, iter.nextNode())) ;
    }
    return calList ;
  }
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception {
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
      calendares.add(getCalendar(username, it.nextNode())) ;
    }
    return calendares;
  }
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
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
    
    //Check to save category
    if(calendar.getCategoryId() != null && calendar.getCategoryId().length() > 0) {
      Node calendarCategory = getCalendarCategoryHome(username).getNode(calendar.getCategoryId()) ;
      checkToSave(calendarCategory, calendar.getId()) ;
      calendarCategory.save() ;
    }
    calendarHome.getSession().save() ;
  }
  
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception {
    Node calendarHome = getCalendarHome(username) ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(username, calNode) ;
      calNode.remove() ;
      calendarHome.save() ;
      calendarHome.getSession().save() ;
      return calendar ;
    }
    return null ;
  }
  
  
  
  public Calendar getGroupCalendar(String calendarId) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendarId) ;
    return getCalendar(null, calendarNode) ;
  }
  
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds) throws Exception {
    Node calendarHome = getCalendarHome() ;
    List<Calendar> calendars ;
    QueryManager qm ;
    List<GroupCalendarData> groupCalendars = new ArrayList<GroupCalendarData>();
    for(String groupId : groupIds) {
      qm = calendarHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + calendarHome.getPath() 
                                                  + "//element(*,exo:calendar)[@exo:groups='").
                                    append(groupId).
                                    append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      if(it.hasNext()) {
        calendars = new ArrayList<Calendar> () ;
        while(it.hasNext()){
          calendars.add(getCalendar(null, it.nextNode())) ;
        }
        groupCalendars.add(new GroupCalendarData(groupId, calendars)) ;
      }
    }
    
    return groupCalendars;
  }
  
  public void saveGroupCalendar(Calendar calendar, boolean isNew) throws Exception {
    Node calendarHome = getCalendarHome() ;
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
    
    //  Check to save group
    Node calendarGroups = getCalendarGroupHome() ;
    Node groupNode ;
    for(String group : calendar.getGroups()) {
      if(calendarGroups.hasNode(group)) {
        groupNode = calendarGroups.getNode(group) ;
        if(groupNode.hasProperty("exo:calendarIds")) {
          checkToSave(groupNode, calendar.getId()) ;
          groupNode.save() ;
        }          
      }else {
        groupNode = calendarGroups.addNode(group, "exo:calendarCategory") ;
        groupNode.setProperty("exo:id", group) ;
        groupNode.setProperty("exo:name", group) ;
        groupNode.setProperty("exo:calendarIds", new String[] {calendar.getId()}) ;
        //calendarGroups.getSession().save() ;
      }
    }
    calendarHome.getSession().save() ;
    
  }  
  
  public Calendar removeGroupCalendar(String calendarId) throws Exception {
    Node calendarHome = getCalendarHome() ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(null, calNode) ;
      calNode.remove() ;
      calendarHome.save() ;
      calendarHome.getSession().save() ;
      return calendar ;
    }
    return null ;
  }
  
  private void checkToSave(Node groupNode, String calendarId) throws Exception{
    if(groupNode.hasProperty("exo:calendarIds")) {
      Value[] vls = groupNode.getProperty("exo:calendarIds").getValues() ;
      boolean hasValue = false ;
      for(Value vl : vls) {
        if (vl.getString().equals(calendarId)) hasValue = true ;
      }
      if(!hasValue) {
        String[] values = new String[vls.length + 1] ;
        for(int i = 0; i < vls.length; i ++) {
          values[i] = vls[i].getString() ;
        }
        values[vls.length] = calendarId ;
        groupNode.setProperty("exo:calendarIds", values) ;      
      }
    }else {
      groupNode.setProperty("exo:calendarIds", new String[] {calendarId}) ;
    }
    
  }
  private Calendar getCalendar(String username, Node calNode) throws Exception {
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
    
    calendar.setEventCategories(getEventCategories(username, calNode.getProperty("exo:id").getString())) ;
    
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
    Node calendarHome = getCalendarHome(username) ;
    CalendarCategory calCategory = getCalendarCategory(calCategoryNode) ;
    if(calCategory.getCalendars() != null){
      for(String calendarId : calCategory.getCalendars()) {
        if(calendarHome.hasNode(calendarId)) {
          Node calendar = calendarHome.getNode(calendarId) ;
          if(calendar.hasProperty("exo:categoryId")){
            if(calendar.getProperty("exo:categoryId").getString().equals(calendarCategoryId))
              calendar.remove() ;
          }        
        }
      }
    }
    calCategoryNode.remove() ;
    calCategoryHome.save() ;
    calendarHome.save() ;
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
  
  //Event Category APIs
  public void saveUserEventCategory(String username, Calendar calendar, EventCategory eventCategory, boolean isNew) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendar.getId()) ;
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
  
  public EventCategory removeUserEventCategory(String username, String calendarId, String eventCategoryId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;
    Node eventCategoryNode = calendarNode.getNode(eventCategoryId) ;
    EventCategory eventCategory = getEventCategory(eventCategoryNode) ;
    eventCategoryNode.remove() ;
    calendarNode.save() ;
    calendarNode.getSession().save() ;
    return eventCategory ;
  }
  
  public void saveGroupEventCategory(Calendar calendar, EventCategory eventCategory, boolean isNew) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendar.getId()) ;
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
  public EventCategory removeGroupEventCategory(String calendarId, String eventCategoryId) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendarId) ;
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
  
  private List<EventCategory> getEventCategories(String username, String calendarId) throws Exception {
    Node calendarNode ; 
    if(username != null) calendarNode = getCalendarHome(username).getNode(calendarId) ;
    else calendarNode = getCalendarHome().getNode(calendarId) ;
    NodeIterator iter = calendarNode.getNodes() ;
    List<EventCategory> categories = new ArrayList<EventCategory> ();
    while(iter.hasNext()) {
      Node eventCat = iter.nextNode() ;
      if(eventCat.isNodeType("exo:eventCategory")) categories.add(getEventCategory(eventCat)) ;
    }
    return categories ;
  }
  // Event APIs
  
  public Event getUserEvent(String username, String calendarId, String eventCategoryId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;
    if(eventCategoryId != null) {
      return getEvent(calendarNode.getNode(eventCategoryId).getNode(eventId)) ;
    }
    return getEvent(calendarNode.getNode(eventId)) ;
  }
  
  public List<Event> getUserEventByCalendar(String username, String calendarId) throws Exception {
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
  
  public List<Event> getEvents(EventQuery eventQuery) throws Exception {
    Session session = getJCRSession() ;
    QueryManager qm = session.getWorkspace().getQueryManager() ;
    Query query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<Event> events = new ArrayList<Event>() ;
    while(it.hasNext()) {
      events.add(getEvent(it.nextNode())) ;
    }
    return events ;
  }
  
  public void saveUserEvent(String username, String calendarId, Event event, boolean isNew) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId);
    saveEvent(calendarNode, event, isNew) ;
  }
  
  public Event removeUserEvent(String username, String calendarId, String eventCategoryId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId);
    Node eventNode ;
    if(eventCategoryId != null) eventNode = calendarNode.getNode(eventCategoryId).getNode(eventId) ;
    else eventNode = calendarNode.getNode(eventId) ;
    Event event = getEvent(eventNode) ;
    eventNode.remove() ;
    calendarNode.save() ;
    calendarNode.getSession().save() ;
    return event;
  }
  
  public Event getGroupEvent(String calendarId, String eventCategoryId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendarId) ;
    if(eventCategoryId != null) {
      return getEvent(calendarNode.getNode(eventCategoryId).getNode(eventId)) ;
    }
    return getEvent(calendarNode.getNode(eventId)) ;
  }
  
  public List<Event> getGroupEventByCalendar(String calendarId) throws Exception {
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
  
  public void saveGroupEvent(String calendarId, Event event, boolean isNew) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendarId) ;
    saveEvent(calendarNode, event, isNew) ;
  }
  
  public Event removeGroupEvent(String calendarId, String eventCategoryId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendarId) ;
    Node eventNode ;
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
  
  private void saveEvent(Node calendarNode, Event event, boolean isNew) throws Exception {
    Node eventNode ;
    if(isNew) {
      if(event.getEventCategoryId() != null && event.getEventCategoryId().length() > 0) {
        eventNode = calendarNode.getNode(event.getEventCategoryId()).addNode(event.getId(), "exo:calendarEvent") ;
      }else {
        eventNode = calendarNode.addNode(event.getId(), "exo:calendarEvent") ;
      }      
      eventNode.setProperty("exo:id", event.getId()) ;
    }else {
      if(event.getEventCategoryId() != null && event.getEventCategoryId().length() > 0) {
        eventNode = calendarNode.getNode(event.getEventCategoryId()).getNode(event.getId()) ;
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
  
  public String getEventCategoryId(String username, String calendarId, String eventCategoryName) throws Exception {
    List<EventCategory> eventCatList = getEventCategories(username, calendarId) ;
    if(eventCatList.size() < 1) return null ;
    for(EventCategory evCat : eventCatList) {
      if(evCat.getName().equals(eventCategoryName)) return evCat.getId() ;
    }
    return null ;
  }  
}
