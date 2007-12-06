/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage implements DataStorage{

  final private static String CALENDARS = "calendars".intern() ;
  
  final private static String SHARED_CALENDAR = "sharedCalendars".intern() ;
  final private static String EVENTS = "events".intern() ;
  final private static String TASKS = "tasks".intern() ;
  final private static String CALENDAR_CATEGORIES = "categories".intern() ;
  final private static String FEED = "eXoCalendarFeed".intern() ;
  final private static String CALENDAR_GROUPS = "groups".intern() ;
  final private static String CALENDAR_REMINDER = "reminders".intern() ;
  final private static String CALENDAR_SETTING = "calendarSetting".intern() ;
  final private static String EVENT_CATEGORIES = "eventCategories".intern() ;
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final private static String CALENDAR_APP = "CalendarApplication".intern() ;
  private static final String SHARED_MIXIN = "exo:calendarShared".intern();
  private static final String SHARED_PROP = "exo:sharedId".intern();
  private final static String VALUE = "value".intern() ; 

  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  //private JCRRegistryService jcrRegistryService_ ;

  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ; 
    //jcrRegistryService_ = jcrRegistryService ;
  }  

  private Node getPublicCalendarServiceHome(SessionProvider sProvider) throws Exception {
    /*ServiceRegistry serviceRegistry = new ServiceRegistry("CalendarService") ;
    Session session = getJCRSession() ;
    jcrRegistryService_.createServiceRegistry(serviceRegistry, false) ;
    Node node = jcrRegistryService_.getServiceRegistryNode(session, serviceRegistry.getName()) ;
    session.logout() ;*/
  	Node calendarSer ;
  	Node publicApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider)  ;
  	if(publicApp.hasNode(CALENDAR_APP)) calendarSer = publicApp.getNode(CALENDAR_APP) ;
  	else calendarSer = publicApp.addNode(CALENDAR_APP, NT_UNSTRUCTURED) ;
  	return calendarSer ;
  }

  private Node getSharedCalendarHome(SessionProvider sProvider) throws Exception {
    Node calendarServiceHome = getPublicCalendarServiceHome(sProvider) ;
    if(calendarServiceHome.hasNode(SHARED_CALENDAR)) return calendarServiceHome.getNode(SHARED_CALENDAR) ;
    return calendarServiceHome.addNode(SHARED_CALENDAR, NT_UNSTRUCTURED) ;
  }
  
  private Node getUserCalendarServiceHome(SessionProvider sProvider, String username) throws Exception {
    /*ServiceRegistry serviceRegistry = new ServiceRegistry("CalendarService") ;
    Session session = getJCRSession() ;
    if(jcrRegistryService_.getUserNode(session, username) == null)
      jcrRegistryService_.createUserHome(username, false) ;
    jcrRegistryService_.createServiceRegistry(username, serviceRegistry, false) ;
    Node calServiceNode = jcrRegistryService_.getServiceRegistryNode(session, username, serviceRegistry.getName()) ;
    if(!calServiceNode.hasNode(CALENDAR_SETTING)) {
      addCalendarSetting(calServiceNode, new CalendarSetting()) ;
      calServiceNode.getSession().save() ;
    }
    session.logout() ;*/
  	Node userApp = nodeHierarchyCreator_.getUserApplicationNode(sProvider, username)  ;
  	Node calendarRoot ; 
  	if(userApp.hasNode(CALENDAR_APP)) calendarRoot = userApp.getNode(CALENDAR_APP) ;
  	else calendarRoot = userApp.addNode(CALENDAR_APP, NT_UNSTRUCTURED) ;
  	if(!calendarRoot.hasNode(CALENDAR_SETTING)) {
      addCalendarSetting(calendarRoot, new CalendarSetting()) ;
      calendarRoot.getSession().save() ;
    }
  	return calendarRoot ;
  }
  
  private Node getCalendarHome(SessionProvider sProvider) throws Exception {
    Node calendarServiceHome = getPublicCalendarServiceHome(sProvider) ;
    if(calendarServiceHome.hasNode(CALENDARS)) return calendarServiceHome.getNode(CALENDARS) ;
    return calendarServiceHome.addNode(CALENDARS, NT_UNSTRUCTURED) ;
  }
  
  private Node getCalendarHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    if(calendarServiceHome.hasNode(CALENDARS)) return calendarServiceHome.getNode(CALENDARS) ;
    return calendarServiceHome.addNode(CALENDARS, NT_UNSTRUCTURED) ;
  }
  
  public Node getRssHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    if(calendarServiceHome.hasNode(FEED)) return calendarServiceHome.getNode(FEED) ;
    return calendarServiceHome.addNode(FEED, NT_UNSTRUCTURED) ;
  }

  protected Node getCalendarCategoryHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    if(calendarServiceHome.hasNode(CALENDAR_CATEGORIES)) return calendarServiceHome.getNode(CALENDAR_CATEGORIES) ;
    return calendarServiceHome.addNode(CALENDAR_CATEGORIES, NT_UNSTRUCTURED) ;
  }

  protected Node getEventCategoryHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    if(calendarServiceHome.hasNode(EVENT_CATEGORIES)) return calendarServiceHome.getNode(EVENT_CATEGORIES) ;
    return calendarServiceHome.addNode(EVENT_CATEGORIES, NT_UNSTRUCTURED) ;
  }

  private Node getCalendarGroupHome(SessionProvider sProvider) throws Exception {
    Node calendarServiceHome = getPublicCalendarServiceHome(sProvider) ;
    if(calendarServiceHome.hasNode(CALENDAR_GROUPS)) return calendarServiceHome.getNode(CALENDAR_GROUPS) ;
    return calendarServiceHome.addNode(CALENDAR_GROUPS, NT_UNSTRUCTURED) ;
  }

  

  public Calendar getUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    Node calendarNode = getCalendarHome(sProvider, username).getNode(calendarId) ;
    return getCalendar(username, calendarNode) ;
  }
  public List<Calendar> getUserCalendars(SessionProvider sProvider, String username) throws Exception {
    NodeIterator iter = getCalendarHome(sProvider, username).getNodes() ;
    List<Calendar> calList = new ArrayList<Calendar>() ;
    while(iter.hasNext()) {
      calList.add(getCalendar(username, iter.nextNode())) ;
    }
    return calList ;
  }
  public List<Calendar> getUserCalendarsByCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception {
    Node calendarHome = getCalendarHome(sProvider, username) ;    
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
  public void saveUserCalendar(SessionProvider sProvider, String username, Calendar calendar, boolean isNew) throws Exception {
    Node calendarHome = getCalendarHome(sProvider, username) ;
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
    calendarNode.setProperty("exo:locale", calendar.getLocale()) ;
    calendarNode.setProperty("exo:timeZone", calendar.getTimeZone()) ;
    calendarNode.setProperty("exo:calendarColor", calendar.getCalendarColor()) ;
    //Check to save category
    /*if(calendar.getCategoryId() != null && calendar.getCategoryId().length() > 0) {
      Node calendarCategory = getCalendarCategoryHome(sProvider, username).getNode(calendar.getCategoryId()) ;
      checkToSave(calendarCategory, calendar.getId()) ;
      calendarCategory.save() ;
    }*/
    calendarHome.getSession().save() ;
  }

  public Calendar removeUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    Node calendarHome = getCalendarHome(sProvider, username) ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(username, calNode) ;
      NodeIterator iter = calNode.getNodes() ;
      while(iter.hasNext()) {
      	//Need to use system session
      	removeReminder(sProvider, iter.nextNode()) ;
      }
      calNode.remove() ;
      calendarHome.save() ;
      calendarHome.getSession().save() ;
      return calendar ;
    }
    return null ;
  }

  public Calendar getGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception {
    Node calendarNode = getCalendarHome(sProvider).getNode(calendarId) ;
    return getCalendar(null, calendarNode) ;
  }

  public List<GroupCalendarData> getGroupCalendars(SessionProvider sProvider, String[] groupIds) throws Exception {
    Node calendarHome = getCalendarHome(sProvider) ;
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
        groupCalendars.add(new GroupCalendarData(groupId, groupId, calendars)) ;
      }
    }

    return groupCalendars;
  }

  public void saveGroupCalendar(SessionProvider sProvider, Calendar calendar, boolean isNew) throws Exception {
    Node calendarHome = getCalendarHome(sProvider) ;
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
    calendarNode.setProperty("exo:calendarColor", calendar.getCalendarColor()) ;
    //  Check to save group
    /*Node calendarGroups = getCalendarGroupHome() ;
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
    }*/
    calendarHome.getSession().save() ;

  }  

  public Calendar removeGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception {
    Node calendarHome = getCalendarHome(sProvider) ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(null, calNode) ;
      NodeIterator iter = calNode.getNodes() ;
      while(iter.hasNext()) {
      	removeReminder(sProvider, iter.nextNode()) ;
      }
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
    if(calNode.hasProperty("exo:locale")) calendar.setLocale(calNode.getProperty("exo:locale").getString()) ;
    if(calNode.hasProperty("exo:timeZone")) calendar.setTimeZone(calNode.getProperty("exo:timeZone").getString()) ;
    if(calNode.hasProperty("exo:calendarColor")) calendar.setCalendarColor(calNode.getProperty("exo:calendarColor").getString()) ;
    if(!calendar.isPublic()) {
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

  public List<GroupCalendarData> getCalendarCategories(SessionProvider sProvider, String username) throws Exception {
    Node calendarHome = getCalendarHome(sProvider, username) ;
    NodeIterator iter = getCalendarCategoryHome(sProvider, username).getNodes() ;
    List<GroupCalendarData> calendarCategories = new ArrayList<GroupCalendarData> () ;
    List<Calendar> calendars ;
    calendarHome.getSession().refresh(false) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    while(iter.hasNext()){      
      Node categoryNode =  iter.nextNode() ;
      String categoryId = categoryNode.getProperty("exo:id").getString() ;     
      StringBuffer queryString = 
        new StringBuffer("/jcr:root" + calendarHome.getPath()  
            + "//element(*,exo:calendar)[@exo:categoryId='").append(categoryId).append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      calendars = new ArrayList<Calendar> () ;
      if(it.hasNext()) {
        while(it.hasNext()){
          calendars.add(getCalendar(username, it.nextNode())) ;
        }        
      }
      calendarCategories.add(new GroupCalendarData(categoryId, categoryNode.getProperty("exo:name").getString(), calendars)) ;
    }
    return calendarCategories;
  }

  public List<CalendarCategory> getCategories(SessionProvider sProvider, String username) throws Exception {
    Node calendarCategoryHome = getCalendarCategoryHome(sProvider, username) ;
    NodeIterator iter = calendarCategoryHome.getNodes() ;
    List<CalendarCategory> calendarCategories = new ArrayList<CalendarCategory> () ;
    while(iter.hasNext()) {
      calendarCategories.add(getCalendarCategory(iter.nextNode())) ;
    }
    return calendarCategories;
  }

  public CalendarCategory getCalendarCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception {
    Node calendarCategoryHome = getCalendarCategoryHome(sProvider, username) ;
    return getCalendarCategory(calendarCategoryHome.getNode(calendarCategoryId)) ;
  }

  public void saveCalendarCategory(SessionProvider sProvider, String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    Node calCategoryHome = getCalendarCategoryHome(sProvider, username) ;
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
    //calCategoryNode.setProperty("exo:calendarIds", calendarCategory.getCalendars()) ;
    calCategoryHome.getSession().save() ;
  }

  public CalendarCategory removeCalendarCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception {
    Node calCategoryHome = getCalendarCategoryHome(sProvider, username) ;
    Node calCategoryNode = calCategoryHome.getNode(calendarCategoryId) ; 
    //Node calendarHome = getCalendarHome(username) ;
    CalendarCategory calCategory = getCalendarCategory(calCategoryNode) ;
    /*if(calCategory.getCalendars() != null){
      for(String calendarId : calCategory.getCalendars()) {
        if(calendarHome.hasNode(calendarId)) {
          Node calendar = calendarHome.getNode(calendarId) ;
          if(calendar.hasProperty("exo:categoryId")){
            if(calendar.getProperty("exo:categoryId").getString().equals(calendarCategoryId))
              calendar.remove() ;
          }        
        }
      }
    }*/
    calCategoryNode.remove() ;
    for(Calendar cal : getUserCalendarsByCategory(sProvider, username, calendarCategoryId)) {
      removeUserCalendar(sProvider, username, cal.getId()) ;
    }
    calCategoryHome.save() ;
    //calendarHome.save() ;
    calCategoryHome.getSession().save() ;
    return calCategory ;
  }

  private CalendarCategory getCalendarCategory(Node calCategoryNode) throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    if(calCategoryNode.hasProperty("exo:id")) calCategory.setId(calCategoryNode.getProperty("exo:id").getString()) ;
    if(calCategoryNode.hasProperty("exo:name")) calCategory.setName(calCategoryNode.getProperty("exo:name").getString()) ;
    if(calCategoryNode.hasProperty("exo:description")) calCategory.setDescription(calCategoryNode.getProperty("exo:description").getString()) ;
    /*if(calCategoryNode.hasProperty("exo:calendarIds")) {
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
    }   */
    return calCategory ;
  }

  //Event Category APIs
  public List<EventCategory> getEventCategories(SessionProvider sProvider, String username) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(sProvider, username) ;
    NodeIterator iter = eventCategoryHome.getNodes() ;
    List<EventCategory> categories = new ArrayList<EventCategory> () ;
    while (iter.hasNext()) {
      categories.add(getEventCategory(iter.nextNode())) ;
    }
    return categories ;
  }

  public void saveEventCategory(SessionProvider sProvider, String username, EventCategory eventCategory, EventCategory newEventCategory, boolean isNew) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(sProvider, username) ;
    Node eventCategoryNode = null ;
    String name = null ;
    String description = null ;
    if(isNew){
      if(eventCategoryHome.hasNode(eventCategory.getName())) throw new Exception("This event category is already exists!") ;
      eventCategoryNode = eventCategoryHome.addNode(eventCategory.getName(), "exo:eventCategory") ;
      name = eventCategory.getName() ;
      description = eventCategory.getDescription() ;
    }else {
      eventCategoryNode = eventCategoryHome.getNode(eventCategory.getName()) ;
      if(eventCategory.getName().equals(newEventCategory.getName())) {
        name = eventCategory.getName() ;
        description = newEventCategory.getDescription() ;
      } else {
        if(eventCategoryHome.hasNode(newEventCategory.getName())){
          throw new Exception("This event category is already exists!") ;
        } else {
          eventCategoryNode.remove() ;
          eventCategoryHome.addNode(newEventCategory.getName()) ;
          name = newEventCategory.getName() ;
          description = newEventCategory.getDescription() ;
          for(CalendarEvent ce : getUserEventByCategory(sProvider, username, eventCategory.getName())) {
            ce.setEventCategoryId(newEventCategory.getName()) ;
            saveUserEvent(sProvider, username,  ce.getCalendarId(), ce, false) ;
          }
        }
      }
      eventCategoryNode = eventCategoryHome.getNode(newEventCategory.getName()) ;
    }
    eventCategoryNode.setProperty("exo:name", name) ;
    eventCategoryNode.setProperty("exo:description", description) ;
    eventCategoryHome.getSession().save() ;
  } 

  public void removeEventCategory(SessionProvider sProvider, String username, String eventCategoryName) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(sProvider, username) ;
    if(eventCategoryHome.hasNode(eventCategoryName)) {
      Node eventCategoryNode = eventCategoryHome.getNode(eventCategoryName) ;
      for(CalendarEvent ce : getUserEventByCategory(sProvider, username, eventCategoryName)) {
        removeUserEvent(sProvider, username, ce.getCalendarId(), ce.getId()) ;
      }
      eventCategoryNode.remove() ;
      eventCategoryHome.save() ;
      eventCategoryHome.getSession().save() ;
    }
  }

  private EventCategory getEventCategory(Node eventCatNode) throws Exception {
    EventCategory eventCategory = new EventCategory() ;
    if(eventCatNode.hasProperty("exo:name")) eventCategory.setName(eventCatNode.getProperty("exo:name").getString()) ;
    if(eventCatNode.hasProperty("exo:description")) eventCategory.setDescription(eventCatNode.getProperty("exo:description").getString()) ;
    return eventCategory ;
  }

  public EventCategory getEventCategory(SessionProvider sProvider, String username, String eventCategoryName) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(sProvider, username) ;
    return getEventCategory(eventCategoryHome.getNode(eventCategoryName)) ;
  }

  /*private List<EventCategory> getEventCategories(SessionProvider sProvider, String username, String calendarId) throws Exception {
    Node calendarNode ; 
    if(username != null) calendarNode = getCalendarHome(sProvider, username).getNode(calendarId) ;
    else calendarNode = getCalendarHome(sProvider).getNode(calendarId) ;
    NodeIterator iter = calendarNode.getNodes() ;
    List<EventCategory> categories = new ArrayList<EventCategory> ();
    while(iter.hasNext()) {
      Node eventCat = iter.nextNode() ;
      if(eventCat.isNodeType("exo:eventCategory")) categories.add(getEventCategory(eventCat)) ;
    }
    return categories ;
  }*/
  // Event APIs

  public CalendarEvent getUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome(sProvider, username).getNode(calendarId) ;
    return getEvent(sProvider, calendarNode.getNode(eventId)) ;
  }

  public List<CalendarEvent> getUserEventByCalendar(SessionProvider sProvider, String username, List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(String calendarId : calendarIds) {
      Node calendarNode = getCalendarHome(sProvider, username).getNode(calendarId) ;
      NodeIterator it = calendarNode.getNodes();
      while(it.hasNext()) {
        events.add(getEvent(sProvider, it.nextNode())) ;
      }
    }
    return events ;
  }
  public List<CalendarEvent> getUserEventByCategory(SessionProvider sProvider, String username, String eventCategoryId) throws Exception {
    Node calendarHome = getCalendarHome(sProvider, username) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    List<CalendarEvent> events = new ArrayList<CalendarEvent> () ;
    NodeIterator calIter = calendarHome.getNodes() ;
    Query query ;
    QueryResult result ;
    while (calIter.hasNext()) {
      StringBuffer queryString = new StringBuffer("/jcr:root" + calIter.nextNode().getPath() 
          + "//element(*,exo:calendarEvent)[@exo:eventCategoryId='").
          append(eventCategoryId).
          append("']");
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      NodeIterator it = result.getNodes();
      while(it.hasNext()){
        events.add(getEvent(sProvider, it.nextNode())) ;
      }
    }
    return events;
  }
  public List<CalendarEvent> getUserEvents(SessionProvider sProvider, String username, EventQuery eventQuery) throws Exception {
    Node calendarHome = getCalendarHome(sProvider, username) ;
    eventQuery.setCalendarPath(calendarHome.getPath()) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager() ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    Query query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    CalendarEvent calEvent ;
    while(it.hasNext()) {
      calEvent = getEvent(sProvider, it.nextNode()) ;
      calEvent.setCalType("0") ;
      events.add(calEvent) ;
    }
    return events ;
  }

  public void saveUserEvent(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getCalendarHome(sProvider, username).getNode(calendarId);
    if(event.getReminders() != null && event.getReminders().size() > 0) {
    	//Need to use system session
    	Node reminderFolder = getReminderFolder(sProvider, event.getFromDateTime()) ;
    	saveEvent(calendarNode, event, reminderFolder, isNew) ;
    }else {
    	saveEvent(calendarNode, event, null, isNew) ;
    }
    
  }

  public CalendarEvent removeUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome(sProvider, username).getNode(calendarId);
    if(calendarNode.hasNode(eventId)){
      Node eventNode = calendarNode.getNode(eventId) ;
      CalendarEvent event = getEvent(sProvider, eventNode) ;
      //Need to use system session
      removeReminder(sProvider, eventNode) ;
      eventNode.remove() ;
      calendarNode.save() ;
      calendarNode.getSession().save() ;
      return event;
    }
    return null ;
  }
  
  private void removeReminder(SessionProvider sProvider, Node eventNode)throws Exception {
  	// Need to use system session
  	Node node = getReminderFolder(sProvider, eventNode.getProperty("exo:fromDateTime").getDate().getTime()) ;
  	if(node.hasNode(eventNode.getName())) node.getNode(eventNode.getName()).remove() ;
  	//node.save() ;  	
  } 
  
  public CalendarEvent getGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome(sProvider).getNode(calendarId) ;
    CalendarEvent calEvent = getEvent(sProvider, calendarNode.getNode(eventId)) ;
    calEvent.setCalType("2") ;
    return calEvent ;
  }

  public List<CalendarEvent> getGroupEventByCalendar(SessionProvider sProvider, List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(String calendarId : calendarIds){
      Node calendarNode = getCalendarHome(sProvider).getNode(calendarId) ;    
      NodeIterator it = calendarNode.getNodes();
      while(it.hasNext()) {
        events.add(getEvent(sProvider, it.nextNode())) ;
      }
    }
    return events ;
  }

  public List<CalendarEvent> getPublicEvents(SessionProvider sProvider, EventQuery eventQuery) throws Exception {
    Node calendarHome = getCalendarHome(sProvider) ;
    eventQuery.setCalendarPath(calendarHome.getPath()) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager() ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    Query query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    CalendarEvent calEvent ;
    while(it.hasNext()) {
      calEvent = getEvent(sProvider, it.nextNode()) ;
      calEvent.setCalType("2") ;
      events.add(calEvent) ;
    }
    return events ;
  }
  public void saveGroupEvent(SessionProvider sProvider, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getCalendarHome(sProvider).getNode(calendarId) ;
    Node reminderFolder = getReminderFolder(sProvider, event.getFromDateTime()) ;
    saveEvent(calendarNode, event, reminderFolder, isNew) ;
  }

  public CalendarEvent removeGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception {
    Node calendarNode = getCalendarHome(sProvider).getNode(calendarId) ;
    if(calendarNode.hasNode(eventId)){
      Node eventNode = calendarNode.getNode(eventId) ;
      CalendarEvent event = getEvent(sProvider, eventNode) ;
      removeReminder(sProvider, eventNode) ;
      eventNode.remove() ;
      calendarNode.save() ;
      calendarNode.getSession().save() ;
      return event;
    }
    return null ;
  }


  private CalendarEvent getEvent(SessionProvider sProvider, Node eventNode) throws Exception {
    CalendarEvent event = new CalendarEvent() ;
    if(eventNode.hasProperty("exo:id")) event.setId(eventNode.getProperty("exo:id").getString()) ;
    if(eventNode.hasProperty("exo:calendarId"))event.setCalendarId(eventNode.getProperty("exo:calendarId").getString()) ;
    if(eventNode.hasProperty("exo:summary")) event.setSummary(eventNode.getProperty("exo:summary").getString()) ;
    if(eventNode.hasProperty("exo:eventCategoryId")) event.setEventCategoryId(eventNode.getProperty("exo:eventCategoryId").getString()) ;
    if(eventNode.hasProperty("exo:location")) event.setLocation(eventNode.getProperty("exo:location").getString()) ;
    if(eventNode.hasProperty("exo:taskDelegator")) event.setTaskDelegator(eventNode.getProperty("exo:taskDelegator").getString()) ;
    if(eventNode.hasProperty("exo:repeat")) event.setRepeatType(eventNode.getProperty("exo:repeat").getString()) ;
    if(eventNode.hasProperty("exo:description")) event.setDescription(eventNode.getProperty("exo:description").getString()) ;
    if(eventNode.hasProperty("exo:fromDateTime")) event.setFromDateTime(eventNode.getProperty("exo:fromDateTime").getDate().getTime()) ;
    if(eventNode.hasProperty("exo:toDateTime")) event.setToDateTime(eventNode.getProperty("exo:toDateTime").getDate().getTime()) ;
    if(eventNode.hasProperty("exo:eventType")) event.setEventType(eventNode.getProperty("exo:eventType").getString()) ;
    if(eventNode.hasProperty("exo:priority")) event.setPriority(eventNode.getProperty("exo:priority").getString()) ;
    if(eventNode.hasProperty("exo:isPrivate")) event.setPrivate(eventNode.getProperty("exo:isPrivate").getBoolean()) ;
    if(eventNode.hasProperty("exo:eventState")) event.setEventState(eventNode.getProperty("exo:eventState").getString()) ;

    event.setReminders(getReminders(sProvider, eventNode)) ;
    event.setAttachment(getAttachments(eventNode)) ;
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
    if(eventNode.hasProperty("exo:participant")){
      Value[] values = eventNode.getProperty("exo:participant").getValues() ;
      if(values.length == 1 ){      
        event.setParticipant(new String[]{values[0].getString()}) ;
      }else {
        String[] participant = new String[values.length] ;
        for(int i = 0; i < values.length; i ++) {
          participant[i] = values[i].getString() ;
        }
        event.setParticipant(participant) ;
      }
    }
    return event ;
  }

  private void saveEvent(Node calendarNode, CalendarEvent event, Node reminderFolder, boolean isNew) throws Exception {
    Node eventNode ;
    if(isNew) {
      eventNode = calendarNode.addNode(event.getId(), "exo:calendarEvent") ;
      eventNode.setProperty("exo:id", event.getId()) ;
    }else {
      if(calendarNode.hasNode(event.getId())) {
        eventNode = calendarNode.getNode(event.getId()) ;
      }else {
        CalendarEvent tempEvent = new CalendarEvent() ;
        eventNode = calendarNode.addNode(tempEvent.getId(), "exo:calendarEvent") ;
        eventNode.setProperty("exo:id", tempEvent.getId()) ;
      }
    }
    eventNode.setProperty("exo:summary", event.getSummary()) ;
    eventNode.setProperty("exo:calendarId", event.getCalendarId()) ;
    eventNode.setProperty("exo:eventCategoryId", event.getEventCategoryId()) ;
    eventNode.setProperty("exo:description", event.getDescription()) ;
    eventNode.setProperty("exo:location", event.getLocation()) ;
    eventNode.setProperty("exo:taskDelegator", event.getTaskDelegator()) ;

    GregorianCalendar dateTime = Utils.getInstanceTempCalendar() ;
    dateTime.setTime(event.getFromDateTime()) ;
    eventNode.setProperty("exo:fromDateTime", dateTime) ;
    dateTime.setTime(event.getToDateTime()) ;
    eventNode.setProperty("exo:toDateTime", dateTime) ;
    eventNode.setProperty("exo:eventType", event.getEventType()) ;
    eventNode.setProperty("exo:repeat", event.getRepeatType()) ;
    eventNode.setProperty("exo:priority", event.getPriority()) ;
    eventNode.setProperty("exo:isPrivate", event.isPrivate()) ;
    eventNode.setProperty("exo:eventState", event.getEventState()) ;
    eventNode.setProperty("exo:invitation",  event.getInvitation()) ;
    eventNode.setProperty("exo:participant", event.getParticipant()) ;
    // add reminder child node
    /*if(eventNode.hasNode(Utils.REMINDERS_NODE)) {
      while (eventNode.getNodes().hasNext()) {
        eventNode.getNodes().nextNode().remove() ;
      }
    }*/
    List<Reminder> reminders = event.getReminders() ;
    if(reminders != null && !reminders.isEmpty()) {
      for(Reminder rm : reminders) {
        addReminder(eventNode, reminderFolder, rm) ;
      }
    }
    //  add attachment child node
    if(eventNode.hasNode(Utils.ATTACHMENT_NODE)) {
      while (eventNode.getNodes().hasNext()) {
        eventNode.getNodes().nextNode().remove() ;
      }
    }
    List<Attachment> attachments = event.getAttachment() ;
    if(attachments != null) {
      for(Attachment att : attachments) {
        addAttachment(eventNode, att, isNew) ;
      }
    }
    calendarNode.save() ;
    calendarNode.getSession().save()  ;
  }
  private void addReminder(Node eventNode, Node reminderFolder, Reminder reminder) throws Exception {
    Node reminderNode ;
    Node catNode ;
    /*if(eventNode.hasNode(Utils.REMINDERS_NODE)) {
      reminders = eventNode.getNode(Utils.REMINDERS_NODE) ;
    } else {
      reminders = eventNode.addNode(Utils.REMINDERS_NODE, Utils.NT_UNSTRUCTURED) ;
    }*/
    if(reminderFolder.hasNode(eventNode.getName())) {
    	catNode = reminderFolder.getNode(eventNode.getName()) ;
    }else {
    	catNode = reminderFolder.addNode(eventNode.getName(), Utils.NT_UNSTRUCTURED) ;
    }
    if(catNode.hasNode(reminder.getId())){
      reminderNode = catNode.getNode(reminder.getId()) ;
    } else {
      reminderNode = catNode.addNode(reminder.getId(), "exo:reminder") ;
    }
    reminderNode.setProperty("exo:eventId", eventNode.getName()) ;
    reminderNode.setProperty("exo:alarmBefore", reminder.getAlarmBefore()) ;
    reminderNode.setProperty("exo:repeatInterval", reminder.getRepeatInterval()) ;
    reminderNode.setProperty("exo:reminderType", reminder.getReminderType()) ;
    reminderNode.setProperty("exo:email", reminder.getEmailAddress()) ;
    reminderNode.setProperty("exo:isRepeat", reminder.isRepeat()) ;
    java.util.Calendar cal = new GregorianCalendar() ;
    if(reminder.getFromDateTime() != null) {
    	long time = reminder.getFromDateTime().getTime() - (reminder.getAlarmBefore() * 60 * 1000) ;
    	cal.setTimeInMillis(time) ;
      reminderNode.setProperty("exo:fromDateTime", cal) ;
    }
    StringBuffer summary = new StringBuffer("Event: ") ;
    summary.append(eventNode.getProperty("exo:summary").getString()).append("<br>") ;
    cal.setTime(eventNode.getProperty("exo:fromDateTime").getDate().getTime()) ;
    summary.append("From : ").append(cal.get(java.util.Calendar.HOUR_OF_DAY)).append(":") ;
    summary.append(cal.get(java.util.Calendar.MINUTE)).append(" - ") ;
    summary.append(cal.get(java.util.Calendar.DATE)).append("/") ;
    summary.append(cal.get(java.util.Calendar.MONTH)).append("/") ;
    summary.append(cal.get(java.util.Calendar.YEAR)).append("<br>") ;
    cal.setTime(eventNode.getProperty("exo:toDateTime").getDate().getTime()) ;
    summary.append("To   : ").append(cal.get(java.util.Calendar.HOUR_OF_DAY)).append(":") ;
    summary.append(cal.get(java.util.Calendar.MINUTE)).append(" - ") ;
    summary.append(cal.get(java.util.Calendar.DATE)).append("/") ;
    summary.append(cal.get(java.util.Calendar.MONTH)).append("/") ;
    summary.append(cal.get(java.util.Calendar.YEAR)).append("<br>") ;
    reminderNode.setProperty("exo:eventSummary", summary.toString()) ;
    reminderFolder.getSession().save() ;
  }
  
  private Node getReminderFolder(SessionProvider sProvider, Date fromDate)throws Exception {
  	java.util.Calendar fromCalendar = new GregorianCalendar() ;
  	fromCalendar.setTime(fromDate) ;
  	Node reminderHome = getReminderHome(sProvider) ;
  	Node yearNode;
  	Node monthNode;
  	String year = String.valueOf(fromCalendar.get(java.util.Calendar.YEAR)) ;
  	String month = String.valueOf(fromCalendar.get(java.util.Calendar.MONTH) + 1) ;
  	String day = String.valueOf(fromCalendar.get(java.util.Calendar.DATE)) ;
  	if(reminderHome.hasNode(year)) yearNode = reminderHome.getNode(year) ;
  	else yearNode = reminderHome.addNode(year, NT_UNSTRUCTURED) ;
  	if(yearNode.hasNode(month)) monthNode = yearNode.getNode(month) ;
  	else monthNode = yearNode.addNode(month, NT_UNSTRUCTURED) ;
  	if(monthNode.hasNode(day)) return monthNode.getNode(day) ;
  	else return monthNode.addNode(day, NT_UNSTRUCTURED) ;  	
  }
  
  private Node getReminderHome(SessionProvider sProvider) throws Exception {
    Node calendarServiceHome  = getPublicCalendarServiceHome(sProvider) ;    
    if(calendarServiceHome.hasNode(CALENDAR_REMINDER)) return calendarServiceHome.getNode(CALENDAR_REMINDER) ;
    return calendarServiceHome.addNode(CALENDAR_REMINDER, NT_UNSTRUCTURED) ;
  }
  
  private List<Reminder> getReminders(SessionProvider sProvider, Node eventNode) throws Exception {
    List<Reminder> reminders = new ArrayList<Reminder> () ;
    Date fromDate = eventNode.getProperty("exo:fromDateTime").getDate().getTime() ;
    Node reminderFolder = getReminderFolder(sProvider, fromDate) ;
    if(reminderFolder.hasNode(eventNode.getName())) {
    	NodeIterator iter = reminderFolder.getNode(eventNode.getName()).getNodes() ;
      while(iter.hasNext()) {
        Node reminderNode = iter.nextNode() ;
        if(reminderNode.isNodeType("exo:reminder")) {
          Reminder reminder = new Reminder() ;
          reminder.setId(reminderNode.getName()) ;
          if(reminderNode.hasProperty("exo:eventId")) reminder.setEventId(reminderNode.getProperty("exo:eventId").getString()) ;
          if(reminderNode.hasProperty("exo:reminderType")) reminder.setReminderType(reminderNode.getProperty("exo:reminderType").getString()) ;
          if(reminderNode.hasProperty("exo:alarmBefore"))reminder.setAlarmBefore(reminderNode.getProperty("exo:alarmBefore").getLong()) ;
          if(reminderNode.hasProperty("exo:email")) reminder.setEmailAddress(reminderNode.getProperty("exo:email").getString()) ;
          if(reminderNode.hasProperty("exo:isRepeat")) reminder.setRepeate(reminderNode.getProperty("exo:isRepeat").getBoolean()) ;
          if(reminderNode.hasProperty("exo:repeatInterval")) reminder.setRepeatInterval(reminderNode.getProperty("exo:repeatInterval").getLong()) ;
          reminder.setFromDateTime(fromDate) ;
          reminders.add(reminder) ;
        }
      }
    }
    return reminders ;
  }

  private void addAttachment(Node eventNode, Attachment attachment, boolean isNew) throws Exception {
    Node attachHome ;
    Node attachNode ;
    if(eventNode.hasNode(Utils.ATTACHMENT_NODE)) {
      attachHome = eventNode.getNode(Utils.ATTACHMENT_NODE) ;
    } else {
      attachHome = eventNode.addNode(Utils.ATTACHMENT_NODE, Utils.NT_UNSTRUCTURED) ;
    }
    if(attachHome.hasNode(attachment.getId())) {
      attachNode = attachHome.getNode(attachment.getId()) ;
    } else {
      attachNode = attachHome.addNode(attachment.getId(), Utils.EXO_EVEN_TATTACHMENT) ;
    }
    attachNode.setProperty(Utils.EXO_FILE_NAME, attachment.getName()) ;
    Node nodeContent = null;
    if (!attachNode.hasNode(Utils.JCR_CONTENT)) nodeContent = attachNode.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
    else nodeContent = attachNode.getNode(Utils.JCR_CONTENT);
    nodeContent.setProperty(Utils.JCR_LASTMODIFIED, java.util.Calendar.getInstance().getTimeInMillis()) ;
    nodeContent.setProperty(Utils.JCR_MIMETYPE, attachment.getMimeType());
    nodeContent.setProperty(Utils.JCR_DATA, attachment.getInputStream());
  }

  private List<Attachment> getAttachments(Node eventNode) throws Exception {
    List<Attachment> attachments = new ArrayList<Attachment> () ;
    if(eventNode.hasNode(Utils.ATTACHMENT_NODE)) {
      Node attachHome = eventNode.getNode(Utils.ATTACHMENT_NODE) ;
      NodeIterator iter = attachHome.getNodes() ;
      while(iter.hasNext()) {
        Node attchmentNode = iter.nextNode() ;
        if(attchmentNode.isNodeType(Utils.EXO_EVEN_TATTACHMENT)) {
          Attachment attachment = new Attachment() ;
          attachment.setId(attchmentNode.getName()) ;
          if(attchmentNode.hasProperty(Utils.EXO_FILE_NAME)) attachment.setName(attchmentNode.getProperty(Utils.EXO_FILE_NAME).getString()) ;
          //attachment.setName(attchmentNode.getName()) ;
          Node contentNode = attchmentNode.getNode(Utils.JCR_CONTENT) ; 
          if(contentNode != null) {
            if(contentNode.hasProperty(Utils.JCR_LASTMODIFIED)) attachment.setLastModified(contentNode.getProperty(Utils.JCR_LASTMODIFIED).getDate()) ;
            if(contentNode.hasProperty(Utils.JCR_MIMETYPE)) attachment.setMimeType(contentNode.getProperty(Utils.JCR_MIMETYPE).getString()) ;
            if(contentNode.hasProperty(Utils.JCR_DATA)) {
              InputStream  inputStream = contentNode.getProperty(Utils.JCR_DATA).getStream() ;
              attachment.setInputStream(inputStream) ;
              attachment.setSize(inputStream.available()) ;
            }
          }
          attachments.add(attachment) ;
        }
      }
    }
    return attachments ;
  }
  public void saveCalendarSetting(SessionProvider sProvider, String username, CalendarSetting setting) throws Exception {
    Node calendarHome = getUserCalendarServiceHome(sProvider, username) ;
    addCalendarSetting(calendarHome, setting) ;
    calendarHome.getSession().save() ;
  }

  private void addCalendarSetting(Node calendarHome, CalendarSetting setting) throws Exception {
    Node settingNode ;
    if(calendarHome.hasNode(CALENDAR_SETTING)) settingNode = calendarHome.getNode(CALENDAR_SETTING) ;
    else settingNode = calendarHome.addNode(CALENDAR_SETTING, "exo:calendarSetting") ;
    settingNode.setProperty("exo:viewType", setting.getViewType()) ;
    settingNode.setProperty("exo:timeInterval", setting.getTimeInterval()) ;
    settingNode.setProperty("exo:weekStartOn", setting.getWeekStartOn()) ;
    settingNode.setProperty("exo:dateFormat", setting.getDateFormat()) ;
    settingNode.setProperty("exo:timeFormat", setting.getTimeFormat()) ;
    settingNode.setProperty("exo:location", setting.getLocation()) ;
    settingNode.setProperty("exo:timeZone", setting.getTimeZone()) ;
    settingNode.setProperty("exo:showWorkingTime", setting.isShowWorkingTime()) ;
    if(setting.isShowWorkingTime()) {
      settingNode.setProperty("exo:workingTimeBegin", setting.getWorkingTimeBegin()) ;
      settingNode.setProperty("exo:workingTimeEnd", setting.getWorkingTimeEnd()) ;
    }
    settingNode.setProperty("exo:baseURL", setting.getBaseURL()) ;
    settingNode.setProperty("exo:defaultPrivateCalendars", setting.getDefaultPrivateCalendars()) ;
    settingNode.setProperty("exo:defaultPublicCalendars", setting.getDefaultPublicCalendars()) ;
    settingNode.setProperty("exo:sharedCalendarsColors", setting.getSharedCalendarsColors()) ;
  }
  public CalendarSetting getCalendarSetting(SessionProvider sProvider ,String username) throws Exception{
    Node calendarHome = getUserCalendarServiceHome(sProvider, username) ;
    if(calendarHome.hasNode(CALENDAR_SETTING)){
      CalendarSetting calendarSetting = new CalendarSetting() ;
      Node settingNode = calendarHome.getNode(CALENDAR_SETTING) ;      
      calendarSetting.setViewType(settingNode.getProperty("exo:viewType").getString()) ;
      calendarSetting.setTimeInterval(settingNode.getProperty("exo:timeInterval").getLong()) ;
      calendarSetting.setWeekStartOn(settingNode.getProperty("exo:weekStartOn").getString()) ;
      calendarSetting.setDateFormat(settingNode.getProperty("exo:dateFormat").getString()) ;
      calendarSetting.setTimeFormat(settingNode.getProperty("exo:timeFormat").getString()) ;
      if(settingNode.hasProperty("exo:location"))
        calendarSetting.setLocation(settingNode.getProperty("exo:location").getString()) ;
      if(settingNode.hasProperty("exo:timeZone")) calendarSetting.setTimeZone(settingNode.getProperty("exo:timeZone").getString())  ;
      if(settingNode.hasProperty("exo:showWorkingTime")) {
        calendarSetting.setShowWorkingTime(settingNode.getProperty("exo:showWorkingTime").getBoolean());
      }
      if(calendarSetting.isShowWorkingTime()) {
        if(settingNode.hasProperty("exo:workingTimeBegin")) 
          calendarSetting.setWorkingTimeBegin(settingNode.getProperty("exo:workingTimeBegin").getString()) ;
        if(settingNode.hasProperty("exo:workingTimeEnd"))
          calendarSetting.setWorkingTimeEnd(settingNode.getProperty("exo:workingTimeEnd").getString()) ;
      }
      if(settingNode.hasProperty("exo:defaultCalendars")){
        Value[] values = settingNode.getProperty("exo:defaultCalendars").getValues() ;
        String[] calendars = new String[values.length] ;
        for(int i = 0; i < values.length; i++) {
          calendars[i] = values[i].getString() ;
        }
        calendarSetting.setDefaultPrivateCalendars(calendars) ;
      }
      if(settingNode.hasProperty("exo:sharedCalendarsColors")){
        Value[] values = settingNode.getProperty("exo:sharedCalendarsColors").getValues() ;
        String[] calendarsColors = new String[values.length] ;
        for(int i = 0; i < values.length; i++) {
          calendarsColors[i] = values[i].getString() ;
        }
        calendarSetting.setSharedCalendarsColors(calendarsColors) ;
      }
      return calendarSetting ;
    }
    return null ;
  }

  public List<FeedData> getFeeds(SessionProvider sProvider ,String username) throws Exception {
    List<FeedData> feeds = new ArrayList<FeedData>() ;
    Node rssHome = getRssHome(sProvider, username) ;
    NodeIterator iter = rssHome.getNodes() ;
    while(iter.hasNext()) {
      Node feedNode = iter.nextNode() ;
      if(feedNode.isNodeType("exo:rssData")) {
        FeedData feed = new FeedData() ;
        feed.setTitle(feedNode.getProperty("exo:title").getString()) ;
        StringBuffer url = new StringBuffer(feedNode.getProperty("exo:baseUrl").getString()) ;  
        url.append("/").append(PortalContainer.getInstance().getPortalContainerInfo().getContainerName()) ;
        url.append("/").append(feedNode.getSession().getWorkspace().getName()) ;
        url.append("/").append(username)  ;
        url.append("/").append(feedNode.getName())  ;
        feed.setUrl(url.toString()) ;
        feeds.add(feed) ;
      }
    }
    return feeds ;
  }

  public void generateRss(SessionProvider sProvider ,String username, List<String> calendarIds, RssData rssData, 
      CalendarImportExport importExport) throws Exception {
    Node rssHomeNode = getRssHome(sProvider, username) ;
    Node iCalHome = null ;
    if(rssHomeNode.hasNode("iCalendars")) iCalHome = rssHomeNode.getNode("iCalendars") ;
    else iCalHome = rssHomeNode.addNode("iCalendars", NT_UNSTRUCTURED) ;
    try {         
      SyndFeed feed = new SyndFeedImpl();      
      feed.setFeedType(rssData.getVersion());      
      feed.setTitle(rssData.getTitle());
      feed.setLink(rssData.getLink());
      feed.setDescription(rssData.getDescription());     
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      SyndEntry entry;
      SyndContent description;
      ExoContainer container = ExoContainerContext.getCurrentContainer() ;
      PortalContainerInfo containerInfo = 
        (PortalContainerInfo)container.getComponentInstanceOfType(PortalContainerInfo.class) ;      
      String portalName = containerInfo.getContainerName() ; 
      List<String> ids = new ArrayList<String>();
      for(String calendarId : calendarIds) {        
        ids.clear() ;
        ids.add(calendarId) ;
        OutputStream out = importExport.exportCalendar(sProvider, username, ids, "0") ;
        if(out != null) {
        	ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          if(iCalHome.hasNode(calendarId + ".ics")){
            iCalHome.getNode(calendarId + ".ics").setProperty("exo:data", is) ;          
          }else {
            Node ical = iCalHome.addNode(calendarId + ".ics", "exo:iCalData") ;
            ical.setProperty("exo:data", is) ;
          }
          StringBuffer path = new StringBuffer("/") ;
          path.append(iCalHome.getName()).append("/").append(iCalHome.getNode(calendarId + ".ics").getName());        
          String url = getEntryUrl(portalName, rssHomeNode.getSession().getWorkspace().getName(), 
              username, path.toString(), rssData.getUrl()) ;
          Calendar exoCal = getUserCalendar(sProvider, username, calendarId) ;
          entry = new SyndEntryImpl();
          entry.setTitle(exoCal.getName());                
          entry.setLink(url);        
          description = new SyndContentImpl();
          description.setType("text/plain");
          description.setValue(exoCal.getDescription());
          entry.setDescription(description);        
          entries.add(entry);
          entry.getEnclosures() ;     
        }                   
      }      
      feed.setEntries(entries);      
      feed.setEncoding("UTF-8") ;     
      SyndFeedOutput output = new SyndFeedOutput();      
      String feedXML = output.outputString(feed);      
      feedXML = StringUtils.replace(feedXML,"&amp;","&");      
      storeXML(feedXML, rssHomeNode, rssData.getName(), rssData); 
      rssHomeNode.getSession().save() ;
    } catch (Exception e) {
      e.printStackTrace();
    }     
  }

  private void storeXML(String feedXML, Node rssHome, String rssNodeName, RssData rssData) throws Exception{
    Node rss ;
    if(rssHome.hasNode(rssNodeName)) rss = rssHome.getNode(rssNodeName);
    else rss = rssHome.addNode(rssNodeName, "exo:rssData");
    rss.setProperty("exo:baseUrl", rssData.getUrl()) ;
    rss.setProperty("exo:title", rssData.getTitle()) ;
    rss.setProperty("exo:content", new ByteArrayInputStream(feedXML.getBytes()));
  }

  private String getEntryUrl(String portalName, String wsName, String username, String path, String baseUrl) throws Exception{
    StringBuilder url = new StringBuilder(baseUrl) ;
    url.append("/").append(portalName)
    .append("/").append(wsName)
    .append("/").append(username)
    .append(path) ;
    return url.toString();
  }

  public EventPageList searchEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception {
    List<CalendarEvent> events = getEvent(sProvider, username, eventQuery, publicCalendarIds) ;
    /*Query query ;
    QueryManager qm ;
    if(username != null && username.length() > 0) {
      Node calendarHome = getCalendarHome(username) ;
      eventQuery.setCalendarPath(calendarHome.getPath()) ;
      qm = calendarHome.getSession().getWorkspace().getQueryManager() ;
      query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
      NodeIterator it = query.execute().getNodes();
      while(it.hasNext()) {
        events.add(getEvent(it.nextNode())) ;        
      }
    }
    Node publicCalHome = getCalendarHome() ;
    eventQuery.setCalendarPath(publicCalHome.getPath()) ;
    qm = publicCalHome.getSession().getWorkspace().getQueryManager() ;
    query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
    NodeIterator it = query.execute().getNodes();
    while(it.hasNext()) {
      events.add(getEvent(it.nextNode())) ;
    }    */
    return new EventPageList(events, 10) ;    
  }

  public Map<Integer, String > searchHightLightEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception {
    Map<Integer, String > mapData = new HashMap<Integer, String>() ;
    Query query ;
    QueryManager qm ;
    // private events
    if(username != null && username.length() > 0) {
      Node calendarHome = getCalendarHome(sProvider, username) ;
      eventQuery.setCalendarPath(calendarHome.getPath()) ;
      qm = calendarHome.getSession().getWorkspace().getQueryManager() ;
      query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
      NodeIterator it = query.execute().getNodes();   
      mapData = updateMap(mapData, it, eventQuery.getFromDate(), eventQuery.getToDate()) ;
    }
    // shared event
    if(getSharedCalendarHome(sProvider).hasNode(username)) {
      PropertyIterator iter = getSharedCalendarHome(sProvider).getNode(username).getReferences() ;
      while(iter.hasNext()) {
        try{
          Node calendar = iter.nextProperty().getParent() ;
          eventQuery.setCalendarPath(calendar.getPath()) ;
          qm = calendar.getSession().getWorkspace().getQueryManager() ;
          query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
          NodeIterator it = query.execute().getNodes();
          mapData = updateMap(mapData, it, eventQuery.getFromDate(), eventQuery.getToDate()) ;
        }catch(Exception e){
          e.printStackTrace() ;
        }
      }
    }  
    // public events
    Node publicCalHome = getCalendarHome(sProvider) ;
    eventQuery.setCalendarPath(publicCalHome.getPath()) ;
    qm = publicCalHome.getSession().getWorkspace().getQueryManager() ;
    eventQuery.setCalendarId(publicCalendarIds) ;
    query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
    NodeIterator it = query.execute().getNodes();
    mapData = updateMap(mapData, it, eventQuery.getFromDate(), eventQuery.getToDate()) ;  
    return mapData ;    
  }

  private Map<Integer, String> updateMap(Map<Integer, String> data, NodeIterator it, java.util.Calendar fromDate, java.util.Calendar toDate) throws Exception {
    Long start = new Long(1);
    Long end ;
    boolean isVictory = false ;
    long milisOfDay = 24 * 60 * 60 * 1000 ;
    long beginDay = fromDate.getTimeInMillis() / milisOfDay ;
    long endDay = toDate.getTimeInMillis() / milisOfDay ;
    while(it.hasNext()) {
      Node eventNode = it.nextNode() ;
      start = new Long(1) ;
      long fromDay = eventNode.getProperty("exo:fromDateTime").getDate().getTimeInMillis() / milisOfDay  + 1;
      long toDay = eventNode.getProperty("exo:toDateTime").getDate().getTimeInMillis() / milisOfDay + 1;
      if(fromDay < beginDay) {
        if(toDay < endDay ) {
          end = toDay - beginDay ;          
        }else {
          end = endDay - beginDay ;
          isVictory = true ;
        }
      }else {
        if(fromDay == beginDay) {
          if( toDay < endDay) {
            end = toDay - beginDay ;            
          }else {
            end = endDay - beginDay ;
            isVictory = true ;
          }
        }else {
          start = fromDay - beginDay ;
          if(toDay < endDay) {
            end = start + (toDay - fromDay) ;            
          }else {
            end = start + (endDay - fromDay) ;
          }
        }
      }
      for (int i = start.intValue(); i <= end.intValue(); i ++) {
        data.put(i, VALUE) ;            
      }
      if (isVictory) break ;
    }
    return data ;
  }
  
  public void shareCalendar(SessionProvider sProvider, String username, String calendarId, List<String> receiverUsers) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome(sProvider) ;
    Node calendarNode = getCalendarHome(sProvider, username).getNode(calendarId) ;
    Value[] values = {};
    if (calendarNode.isNodeType(SHARED_MIXIN)) {     
      values = calendarNode.getProperty(SHARED_PROP).getValues();
    } else {
      calendarNode.addMixin(SHARED_MIXIN);     
    }
    Session systemSession = sharedCalendarHome.getSession() ;
    Node userNode ;
    List<Value> valueList = new ArrayList<Value>() ;
    for(String user : receiverUsers) {
      if(sharedCalendarHome.hasNode(user)) {
        userNode = sharedCalendarHome.getNode(user) ;
      } else {
        userNode = sharedCalendarHome.addNode(user, NT_UNSTRUCTURED) ;
        if(userNode.canAddMixin("mix:referenceable")) {
          userNode.addMixin("mix:referenceable") ;
        } 
      }
      boolean isExist = false ; 
      for (int i = 0; i < values.length; i++) {
        Value value = values[i];
        String uuid = value.getString();
        Node refNode = systemSession.getNodeByUUID(uuid);
        if(refNode.getPath().equals(userNode.getPath())) {
          isExist = true ; 
          break ;
        }
        valueList.add(value) ;
      }
      if(!isExist) {
        Value value2add = calendarNode.getSession().getValueFactory().createValue(userNode);
        valueList.add(value2add) ;
      }      
    }
    if(valueList.size() > 0) {
      calendarNode.setProperty(SHARED_PROP, valueList.toArray( new Value[valueList.size()]));
      calendarNode.save() ;
      sharedCalendarHome.getSession().save() ;
      calendarNode.getSession().save();
      systemSession.logout() ;
    }
  }
  
  public GroupCalendarData getSharedCalendars(SessionProvider sProvider, String username) throws Exception {
    if(getSharedCalendarHome(sProvider).hasNode(username)) {
      Node sharedNode = getSharedCalendarHome(sProvider).getNode(username) ;
      List<Calendar> calendars = new ArrayList<Calendar>() ;
      PropertyIterator iter = sharedNode.getReferences() ;
      while(iter.hasNext()) {
        try{
          calendars.add(getCalendar(null, iter.nextProperty().getParent())) ;
        }catch(Exception e){
          e.printStackTrace() ;
        }
      }
      if(calendars.size() > 0) {
        return new GroupCalendarData("Shared", "Shared", calendars) ;
      }
    }
    return null ;
  }
  public List<CalendarEvent> getSharedEvent(SessionProvider sProvider, String username, EventQuery eventQuery) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    if(getSharedCalendarHome(sProvider).hasNode(username)) {
      PropertyIterator iter = getSharedCalendarHome(sProvider).getNode(username).getReferences() ;
      CalendarEvent calEvent ;
      while(iter.hasNext()) {
        try{
          Node calendar = iter.nextProperty().getParent() ;
          eventQuery.setCalendarPath(calendar.getPath()) ;
          QueryManager qm = calendar.getSession().getWorkspace().getQueryManager() ;
          Query query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
          NodeIterator it = query.execute().getNodes();
          
          while(it.hasNext()){
            calEvent = getEvent(sProvider, it.nextNode()) ;
            calEvent.setCalType("1") ;
            events.add(calEvent) ;
          }
        }catch (Exception e) {
          e.printStackTrace() ;
        }
      }
    }
    return events ;
  }
  
  public List<CalendarEvent> getSharedEventByCalendars(SessionProvider sProvider, String username, List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    if(getSharedCalendarHome(sProvider).hasNode(username)) {
      PropertyIterator iter = getSharedCalendarHome(sProvider).getNode(username).getReferences() ;
      while(iter.hasNext()) {
        try{
          Node calendar = iter.nextProperty().getParent() ;          
          if(calendarIds.contains(calendar.getProperty("exo:id").getString())) {
            NodeIterator it = calendar.getNodes();
            while(it.hasNext()){
              events.add(getEvent(sProvider, it.nextNode())) ;
            }
          }
        }catch (Exception e) {
          e.printStackTrace() ;
        }
      }
    }
    return events ;
  }
  
  public List<CalendarEvent> getEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    //user session
    events.addAll(getUserEvents(sProvider, username, eventQuery)) ;
    //system session
    events.addAll(getSharedEvent(sProvider, username, eventQuery)) ;
    eventQuery.setCalendarId(publicCalendarIds) ;
    events.addAll(getPublicEvents(sProvider, eventQuery)) ;
    return events ;
  }
  
  public void removeSharedCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome(sProvider) ;
    if(sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username) ;
      String uuid = userNode.getProperty("jcr:uuid").getString() ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendar ;
      List<Value> newValues = new ArrayList<Value>() ;
      while(iter.hasNext()) {
        calendar = iter.nextProperty().getParent() ;
        if(calendar.getProperty("exo:id").getString().equals(calendarId)) {
          Value[] values = calendar.getProperty(SHARED_PROP).getValues() ;
          for(Value value : values){
            if(!value.getString().equals(uuid)) {
              newValues.add(value) ;
            }
          }
          calendar.setProperty(SHARED_PROP, newValues.toArray(new Value[newValues.size()])) ;
          calendar.save() ;
          break ;
        }
      }      
    }
  }
  
  public void saveEventToSharedCalendar(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  {
    Node sharedCalendarHome = getSharedCalendarHome(sProvider) ;
    if(sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendar ;      
      while(iter.hasNext()) {
        calendar = iter.nextProperty().getParent() ;
        if(calendar.getProperty("exo:id").getString().equals(calendarId)) {
        	Node reminderFolder = getReminderFolder(sProvider, event.getFromDateTime()) ;
          saveEvent(calendar, event, reminderFolder, isNew) ;
          calendar.save() ;
          break ;
        }
      }      
    }
  }
}


