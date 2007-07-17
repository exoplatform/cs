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
import org.exoplatform.calendar.service.Task;
import org.exoplatform.commons.utils.PageList;
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
  final private static String CATEGORIES = "categories".intern() ;
  
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
  
  private Session getJCRSession() throws Exception {
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
  }

  public Calendar getCalendar(String username, String calendarId) throws Exception {
    Node calendarNode = getCalendarHome(username).getNode(calendarId) ;
    Calendar calendar = new Calendar() ;
    calendar.setId(calendarNode.getProperty("exo:id").getString()) ;
    calendar.setName(calendarNode.getProperty("exo:name").getString()) ;
    calendar.setDescription(calendarNode.getProperty("exo:description").getString()) ;
    calendar.setCategoryId(calendarNode.getProperty("exo:categoryId").getString()) ;
    return calendar ;
  }
  
  public Calendar getCalendar(String calendarId) throws Exception {
    Node calendarNode = getCalendarHome().getNode(calendarId) ;
    Calendar calendar = new Calendar() ;
    calendar.setId(calendarNode.getProperty("exo:id").getString()) ;
    calendar.setName(calendarNode.getProperty("exo:name").getString()) ;
    calendar.setDescription(calendarNode.getProperty("exo:description").getString()) ;
    Value[] values = calendarNode.getProperty("exo:groups").getValues() ;
    if(values.length == 1 ){      
      calendar.setGroups(new String[]{values[0].getString()}) ;
    }else {
      String[] groups = new String[values.length] ;
      for(int i = 0; i < values.length - 1; i ++) {
        groups[i] = values[i].getString() ;
      }
      calendar.setGroups(groups) ;
    }
    Value[] viewValues = calendarNode.getProperty("exo:viewPermissions").getValues() ;
    if(viewValues.length == 1 ){      
      calendar.setGroups(new String[]{viewValues[0].getString()}) ;
    }else {
      String[] views = new String[viewValues.length] ;
      for(int i = 0; i < viewValues.length - 1; i ++) {
        views[i] = viewValues[i].getString() ;
      }
      calendar.setGroups(views) ;
    }
    Value[] editValues = calendarNode.getProperty("exo:editPermissions").getValues() ;
    if(editValues.length == 1 ){      
      calendar.setGroups(new String[]{editValues[0].getString()}) ;
    }else {
      String[] edits = new String[editValues.length] ;
      for(int i = 0; i < editValues.length - 1; i ++) {
        edits[i] = editValues[i].getString() ;
      }
      calendar.setGroups(edits) ;
    }
    calendar.setCategoryId(calendarNode.getProperty("exo:categoryId").getString()) ;
    return calendar ;
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
    Calendar cal  ;
    while(it.hasNext()){
      Node calNode = it.nextNode() ;
      cal = new Calendar() ;
      cal.setId(calNode.getProperty("exo:id").getString()) ;
      cal.setName(calNode.getProperty("exo:name").getString()) ;
      cal.setDescription(calNode.getProperty("exo:description").getString()) ;
      cal.setCategoryId(calNode.getProperty("exo:categoryId").getString()) ;
      calendares.add(cal) ;
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
    Calendar cal  ;
    while(it.hasNext()){
      Node calNode = it.nextNode() ;
      cal = new Calendar() ;
      cal.setId(calNode.getProperty("exo:id").getString()) ;
      cal.setName(calNode.getProperty("exo:name").getString()) ;
      cal.setDescription(calNode.getProperty("exo:description").getString()) ;
      Value[] values = calNode.getProperty("exo:groups").getValues() ;
      if(values.length == 1 ){      
        cal.setGroups(new String[]{values[0].getString()}) ;
      }else {
        String[] groups = new String[values.length] ;
        for(int i = 0; i < values.length - 1; i ++) {
          groups[i] = values[i].getString() ;
        }
        cal.setGroups(groups) ;
      }
      Value[] viewValues = calNode.getProperty("exo:viewPermissions").getValues() ;
      if(viewValues.length == 1 ){      
        cal.setGroups(new String[]{viewValues[0].getString()}) ;
      }else {
        String[] views = new String[viewValues.length] ;
        for(int i = 0; i < viewValues.length - 1; i ++) {
          views[i] = viewValues[i].getString() ;
        }
        cal.setGroups(views) ;
      }
      Value[] editValues = calNode.getProperty("exo:editPermissions").getValues() ;
      if(editValues.length == 1 ){      
        cal.setGroups(new String[]{editValues[0].getString()}) ;
      }else {
        String[] edits = new String[editValues.length] ;
        for(int i = 0; i < editValues.length - 1; i ++) {
          edits[i] = editValues[i].getString() ;
        }
        cal.setGroups(edits) ;
      }
      cal.setCategoryId(calNode.getProperty("exo:categoryId").getString()) ;
      calendares.add(cal) ;
    }
    return calendares;
  }
  
  public void saveCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    Node calendarHome  ;
    if(calendar.isPrivate()) {
      calendarHome = getCalendarHome(username) ;
    }else {
      calendarHome = getCalendarHome() ;
    }
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
    calendar.setId(calNode.getProperty("exo:id").getString()) ;
    calendar.setName(calNode.getProperty("exo:name").getString()) ;
    calendar.setDescription(calNode.getProperty("exo:description").getString()) ;
    calendar.setCategoryId(calNode.getProperty("exo:categoryId").getString()) ;
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
    Value[] viewValues = calNode.getProperty("exo:viewPermissions").getValues() ;
    if(viewValues.length == 1 ){      
      calendar.setGroups(new String[]{viewValues[0].getString()}) ;
    }else {
      String[] views = new String[viewValues.length] ;
      for(int i = 0; i < viewValues.length - 1; i ++) {
        views[i] = viewValues[i].getString() ;
      }
      calendar.setGroups(views) ;
    }
    Value[] editValues = calNode.getProperty("exo:editPermissions").getValues() ;
    if(editValues.length == 1 ){      
      calendar.setGroups(new String[]{editValues[0].getString()}) ;
    }else {
      String[] edits = new String[editValues.length] ;
      for(int i = 0; i < editValues.length - 1; i ++) {
        edits[i] = editValues[i].getString() ;
      }
      calendar.setGroups(edits) ;
    }    
    return calendar ;
  }
  public void createCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void createEvent(String username, String calendarId, String eventCategoryId, Event event, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void createEvent(String calendarId, String eventCategoryId, Event event, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public List<CalendarCategory> getCalendarCategories(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Event getEvent(String username, String calendarId, String eventCategoryId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Event getEvent(String calendarId, String eventCategoryId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Event> getEventByCalendar(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Event> getEventByCalendar(String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<EventCategory> getEventCategories(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public EventCategory getEventCategory(String username, String calendarId, String eventCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Event removeEvent(String username, String calendarId, String eventCategoryId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeEvent(String calendarId, String eventCategoryId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public EventCategory removeEventCategory(String username, String calendarId, String eventCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void saveEventCategory(String username, String calendarId, EventCategory eventCategory, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  
  
  
}
