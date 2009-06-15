/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.calendar.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
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
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;

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
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage{

  final private static String CALENDARS = "calendars".intern() ;

  final private static String SHARED_CALENDAR = "sharedCalendars".intern() ;
  final private static String EVENTS = "events".intern() ;
  final private static String TASKS = "tasks".intern() ;
  final private static String CALENDAR_CATEGORIES = "categories".intern() ;
  final private static String FEED = "eXoCalendarFeed".intern() ;
  final private static String CALENDAR_GROUPS = "groups".intern() ;
  final private static String CALENDAR_EVENT = "events".intern() ;
  final private static String CALENDAR_SETTING = "calendarSetting".intern() ;
  final private static String EVENT_CATEGORIES = "eventCategories".intern() ;

  private final static String VALUE = "value".intern() ; 

  private NodeHierarchyCreator nodeHierarchyCreator_ ;

  private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);

  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ; 
  }  

  /**
   * @deprecated Use {@link #getPublicCalendarServiceHome()}
   */
  @Deprecated
  public Node getPublicCalendarServiceHome(SessionProvider sProvider) throws Exception {
    return getPublicCalendarServiceHome();
  }


  public Node getPublicCalendarServiceHome() throws Exception {
    SessionProvider sProvider = createSystemProvider();
    //try {
    Node publicApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider);
    try {
      return publicApp.getNode(Utils.CALENDAR_APP);
    } catch (Exception e) {
      Node calendarApp = publicApp.addNode(Utils.CALENDAR_APP, Utils.NT_UNSTRUCTURED);
      publicApp.getSession().save();
      return calendarApp;
    }
    /*} finally {
      closeSessionProvider(sProvider);
    }*/
  }


  /**
   * @deprecated Use {@link #getSharedCalendarHome()}
   */
  @Deprecated
  private Node getSharedCalendarHome(SessionProvider sProvider) throws Exception {
    return getSharedCalendarHome();
  }

  private Node getSharedCalendarHome() throws Exception {
    //TODO have to use system session 
    SessionProvider sProvider = createSessionProvider();
    //    /try {
    Node calendarServiceHome = getPublicCalendarServiceHome(sProvider);
    try {
      return calendarServiceHome.getNode(SHARED_CALENDAR);
    } catch (Exception e) {
      Node sharedCal = calendarServiceHome.addNode(SHARED_CALENDAR, Utils.NT_UNSTRUCTURED);
      calendarServiceHome.getSession().save();
      return sharedCal;
    }
    /*} finally {
      closeSessionProvider(sProvider);
    }*/
  }

  /**
   * @deprecated Use {@link #getPublicRoot()}
   */
  private Node getPublicRoot(SessionProvider sysProvider) throws Exception {
    return getPublicRoot();
  }

  private Node getPublicRoot() throws Exception {
    SessionProvider sProvider = createSystemProvider();
    //try {
    return nodeHierarchyCreator_.getPublicApplicationNode(sProvider);
    /*} finally {
      closeSessionProvider(sProvider);
    }*/
  }  



  /**
   * @deprecated Use {@link #getUserCalendarServiceHome(String)}
   */
  @Deprecated
  private Node getUserCalendarServiceHome(SessionProvider removeme, String username) throws Exception {
    return getUserCalendarServiceHome(username);
  }

  /**
   * Get the Calendar application user data storage root
   * @param username
   * @return the node that is on top of user data storage
   * @throws Exception
   */
  private Node getUserCalendarServiceHome(String username) throws Exception {
    // cs- 2356
    //SessionProvider sProvider = createSessionProvider();
    SessionProvider sProvider = createSystemProvider();
    //try {
    Node userApp = nodeHierarchyCreator_.getUserApplicationNode(sProvider, username);
    Node calendarRoot;
    try {
      return userApp.getNode(Utils.CALENDAR_APP);
    } catch (Exception e) {
      calendarRoot = userApp.addNode(Utils.CALENDAR_APP, Utils.NT_UNSTRUCTURED);
      if (!calendarRoot.hasNode(CALENDAR_SETTING)) {
        addCalendarSetting(calendarRoot, new CalendarSetting());
      }
      userApp.getSession().save();
      return calendarRoot;
    }
    /*} finally {
      closeSessionProvider(sProvider);
    }*/
  }

  /**
   * @deprecated User {@link #getPublicCalendarHome()}
   */
  @Deprecated
  private Node getPublicCalendarHome(SessionProvider sProvider) throws Exception {
    return getPublicCalendarHome();
  }


  private Node getPublicCalendarHome() throws Exception {
    SessionProvider sProvider = createSystemProvider();
    /*try {
    //sProvider = SessionProvider.createSystemProvider() ;
     */    
    Node calendarServiceHome = getPublicCalendarServiceHome(sProvider) ;
    try {
      return calendarServiceHome.getNode(CALENDARS) ;
    } catch (Exception e) {
      Node cal = calendarServiceHome.addNode(CALENDARS, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return cal ; 
    }
    /*}
    finally {
      closeSessionProvider(sProvider);
    }*/
  }


  private Node getUserCalendarHome(String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(username) ;
    try {
      return calendarServiceHome.getNode(CALENDARS) ;
    } catch (Exception e) {
      Node calendars = calendarServiceHome.addNode(CALENDARS, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return calendars ; 
    }
  }

  public Node getRssHome(String username) throws Exception {
    Node calendarServiceHome = getSharedCalendarHome() ;
    try {
      return calendarServiceHome.getNode(FEED) ;
    } catch (Exception e) {
      Node feed = calendarServiceHome.addNode(FEED, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return feed ;
    }
  }
  private Node getCalDavHome() throws Exception {
    Node calendarServiceHome = getPublicCalendarHome() ;
    try {
      return calendarServiceHome.getNode(FEED) ;
    } catch (Exception e) {
      Node feed = calendarServiceHome.addNode(FEED, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return feed ;
    }
  }
  protected Node getCalendarCategoryHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(username) ;
    try {
      return calendarServiceHome.getNode(CALENDAR_CATEGORIES) ;
    } catch (Exception e) {
      Node calCat = calendarServiceHome.addNode(CALENDAR_CATEGORIES, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return calCat;
    }
  }

  protected Node getEventCategoryHome(String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(username) ;
    try {
      return calendarServiceHome.getNode(EVENT_CATEGORIES) ;
    } catch (Exception e) {
      Node eventCat = calendarServiceHome.addNode(EVENT_CATEGORIES, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return eventCat ; 
    }
  }

  public Calendar getUserCalendar(String username, String calendarId) throws Exception {
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId) ;
    return getCalendar(new String[]{calendarId}, username, calendarNode, true) ;
  }
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception {
    NodeIterator iter = getUserCalendarHome(username).getNodes() ;
    List<Calendar> calList = new ArrayList<Calendar>() ;
    String[] defaultCalendars = null ;     
    if(getCalendarSetting(username) != null){
      defaultCalendars = getCalendarSetting( username).getFilterPrivateCalendars() ;
    }
    while(iter.hasNext()) {
      calList.add(getCalendar(defaultCalendars, username, iter.nextNode(), isShowAll)) ;
    }
    return calList ;
  }
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception {
    Node calendarHome = getUserCalendarHome(username) ;    
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + calendarHome.getPath() 
                                                + "//element(*,exo:calendar)[@exo:categoryId='").
                                                append(calendarCategoryId).
                                                append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<Calendar> calendares = new ArrayList<Calendar> () ;
    String[] defaultCalendars = null ;     
    if(getCalendarSetting(username) != null){
      defaultCalendars = getCalendarSetting(username).getFilterPrivateCalendars() ;
    }
    while(it.hasNext()){
      calendares.add(getCalendar(defaultCalendars, username, it.nextNode(), true)) ;
    }
    return calendares;
  }
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    Node calendarHome = getUserCalendarHome(username) ;
    Node calendarNode ;
    if(isNew) {
      try {
        calendarNode = calendarHome.getNode(calendar.getId()) ;
      } catch (Exception e) {
        calendarNode = calendarHome.addNode(calendar.getId(), Utils.EXO_CALENDAR) ;
        calendarNode.setProperty(Utils.EXO_ID, calendar.getId()) ;
        if(calendar.isDataInit()) reparePermissions(calendarNode, username) ;
      }
    }else {
      calendarNode = calendarHome.getNode(calendar.getId()) ;
    }    
    calendarNode.setProperty(Utils.EXO_NAME, calendar.getName()) ;
    calendarNode.setProperty(Utils.EXO_DESCRIPTION, calendar.getDescription()) ;
    calendarNode.setProperty(Utils.EXO_CATEGORY_ID, calendar.getCategoryId()) ;
    calendarNode.setProperty(Utils.EXO_VIEW_PERMISSIONS, calendar.getViewPermission()) ;
    calendarNode.setProperty(Utils.EXO_EDIT_PERMISSIONS, calendar.getEditPermission()) ;
    calendarNode.setProperty(Utils.EXO_GROUPS, calendar.getGroups()) ;
    calendarNode.setProperty(Utils.EXO_LOCALE, calendar.getLocale()) ;
    calendarNode.setProperty(Utils.EXO_TIMEZONE, calendar.getTimeZone()) ;
    calendarNode.setProperty(Utils.EXO_CALENDAR_COLOR, calendar.getCalendarColor()) ;
    calendarNode.setProperty(Utils.EXO_CALENDAR_OWNER, calendar.getCalendarOwner()) ;
    calendarHome.getSession().save() ;
  }


  public Calendar removeUserCalendar(String username, String calendarId) throws Exception {
    Node calendarHome = getUserCalendarHome(username) ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(new String[]{calendarId}, username, calNode, true) ;
      NodeIterator iter = calNode.getNodes() ;
      SessionProvider provider = SessionProvider.createSystemProvider() ;
      try {
        while(iter.hasNext()) {
          Node eventNode = iter.nextNode() ;
          Node eventFolder = getEventFolder(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()) ;
          syncRemoveEvent(eventFolder, eventNode.getName()) ;
          removeReminder(eventNode) ;
        }
        calNode.remove() ;
        calendarHome.save() ;
      } catch (Exception e) {
        e.printStackTrace() ;
      } finally {
        provider.close() ;
      }
      try {
        removeFeed(username, calendarId) ;        
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      return calendar ;
    }
    return null ;
  }

  public Calendar getGroupCalendar(String calendarId) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId) ;
    return getCalendar(new String[]{calendarId}, null, calendarNode, true) ;
  }

  public List<GroupCalendarData> getGroupCalendars(String[] groupIds, boolean isShowAll, String username) throws Exception {
    Node calendarHome = getPublicCalendarHome() ;
    List<Calendar> calendars ;
    QueryManager qm ;
    List<GroupCalendarData> groupCalendars = new ArrayList<GroupCalendarData>();
    String[] defaultCalendars = null ;
    if(username!= null && getCalendarSetting(username) != null) defaultCalendars = getCalendarSetting(username).getFilterPublicCalendars() ;
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
          Node calNode = it.nextNode() ;
          Calendar cal = getCalendar(defaultCalendars , null, calNode, isShowAll) ;
          if(cal!= null) calendars.add(cal) ;
        }
        groupCalendars.add(new GroupCalendarData(groupId, groupId, calendars)) ;
      }
    }

    return groupCalendars;
  }

  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception {
    Node calendarHome = getPublicCalendarHome() ;
    Node calendarNode ;
    if(isNew) {
      if(calendarHome.hasNode(calendar.getId())) throw new Exception("This calendar is already exists") ;
      calendarNode = calendarHome.addNode(calendar.getId(), Utils.EXO_CALENDAR) ;
      calendarNode.setProperty(Utils.EXO_ID, calendar.getId()) ;   
    }else {
      calendarNode = calendarHome.getNode(calendar.getId()) ;
    }    
    calendarNode.setProperty(Utils.EXO_NAME, calendar.getName()) ;
    calendarNode.setProperty(Utils.EXO_DESCRIPTION, calendar.getDescription()) ;
    calendarNode.setProperty(Utils.EXO_CATEGORY_ID, calendar.getCategoryId()) ;
    calendarNode.setProperty(Utils.EXO_VIEW_PERMISSIONS, calendar.getViewPermission()) ;
    calendarNode.setProperty(Utils.EXO_EDIT_PERMISSIONS, calendar.getEditPermission()) ;
    calendarNode.setProperty(Utils.EXO_LOCALE, calendar.getLocale()) ;
    calendarNode.setProperty(Utils.EXO_TIMEZONE, calendar.getTimeZone()) ;
    calendarNode.setProperty(Utils.EXO_CALENDAR_OWNER, calendar.getCalendarOwner()) ;
    calendarNode.setProperty(Utils.EXO_GROUPS, calendar.getGroups()) ;
    calendarNode.setProperty(Utils.EXO_CALENDAR_COLOR, calendar.getCalendarColor()) ;
    calendarHome.getSession().save() ;
  }  

  public Calendar removeGroupCalendar(String calendarId) throws Exception {
    SessionProvider sProvider = createSystemProvider(); 
    // TODO : system session ensure we can remove the calendar, but it is not safe! 
    // Anyone can remove the public calendar by calling the API
    try {
      Node calendarHome = getPublicCalendarHome(sProvider);
      if (calendarHome.hasNode(calendarId)) {
        Node calNode = calendarHome.getNode(calendarId);
        Calendar calendar = getCalendar(new String[] { calendarId }, null, calNode, true);
        NodeIterator iter = calNode.getNodes();
        while (iter.hasNext()) {
          Node eventNode = iter.nextNode();
          Node eventFolder = getEventFolder(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME)
                                            .getDate()
                                            .getTime());
          removeReminder(eventNode);
          syncRemoveEvent(eventFolder, eventNode.getName());
        }
        calNode.remove();
        // calendarHome.save() ;
        calendarHome.getSession().save();
        try {
          removeFeed(null, calendarId) ;        
        } catch (Exception e) {
          e.printStackTrace() ;
        }        
        return calendar;
      }
      return null;
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  private Calendar getCalendar(String[] defaultFilterCalendars, String username, Node calNode, boolean isShowAll) throws Exception {
    Calendar calendar = null ;
    if(isShowAll) {
      calendar = new Calendar() ;
      if(calNode.hasProperty(Utils.EXO_ID)) calendar.setId(calNode.getProperty(Utils.EXO_ID).getString()) ;
      if(calNode.hasProperty(Utils.EXO_NAME)) calendar.setName(calNode.getProperty(Utils.EXO_NAME).getString()) ;
      if(calNode.hasProperty(Utils.EXO_DESCRIPTION)) calendar.setDescription(calNode.getProperty(Utils.EXO_DESCRIPTION).getString()) ;
      if(calNode.hasProperty(Utils.EXO_CATEGORY_ID)) calendar.setCategoryId(calNode.getProperty(Utils.EXO_CATEGORY_ID).getString()) ;
      if(calNode.hasProperty(Utils.EXO_LOCALE)) calendar.setLocale(calNode.getProperty(Utils.EXO_LOCALE).getString()) ;
      if(calNode.hasProperty(Utils.EXO_TIMEZONE)) calendar.setTimeZone(calNode.getProperty(Utils.EXO_TIMEZONE).getString()) ;
      if(calNode.hasProperty(Utils.EXO_CALENDAR_COLOR)) calendar.setCalendarColor(calNode.getProperty(Utils.EXO_CALENDAR_COLOR).getString()) ;
      if(calNode.hasProperty(Utils.EXO_CALENDAR_OWNER)) calendar.setCalendarOwner(calNode.getProperty(Utils.EXO_CALENDAR_OWNER).getString()) ;
      if(!calendar.isPublic()) {
        if(calNode.hasProperty(Utils.EXO_GROUPS)){
          Value[] values = calNode.getProperty(Utils.EXO_GROUPS).getValues() ;
          List<String> groups = new ArrayList<String>() ;
          for(Value v : values) {
            groups.add(v.getString()) ;
          }
          calendar.setGroups(groups.toArray(new String[groups.size()])) ;
        }

        if(calNode.hasProperty(Utils.EXO_VIEW_PERMISSIONS)) {
          Value[] viewValues = calNode.getProperty(Utils.EXO_VIEW_PERMISSIONS).getValues() ;
          List<String> viewPerms = new ArrayList<String>() ;
          for(Value v : viewValues) {
            viewPerms.add(v.getString()) ;
          }
          calendar.setViewPermission(viewPerms.toArray(new String[viewPerms.size()])) ;
        }
        if(calNode.hasProperty(Utils.EXO_EDIT_PERMISSIONS)) {
          Value[] editValues = calNode.getProperty(Utils.EXO_EDIT_PERMISSIONS).getValues() ;
          List<String> editPerms = new ArrayList<String>() ;
          for(Value v : editValues) {
            editPerms.add(v.getString()) ;
          }
          calendar.setEditPermission(editPerms.toArray(new String[editPerms.size()])) ;
        }      
      }  
    } else {
      if(defaultFilterCalendars == null || !Arrays.asList(defaultFilterCalendars).contains(calNode.getName())) {
        calendar = new Calendar() ;
        if(calNode.hasProperty(Utils.EXO_ID)) calendar.setId(calNode.getProperty(Utils.EXO_ID).getString()) ;
        if(calNode.hasProperty(Utils.EXO_NAME)) calendar.setName(calNode.getProperty(Utils.EXO_NAME).getString()) ;
        if(calNode.hasProperty(Utils.EXO_DESCRIPTION)) calendar.setDescription(calNode.getProperty(Utils.EXO_DESCRIPTION).getString()) ;
        if(calNode.hasProperty(Utils.EXO_CATEGORY_ID)) calendar.setCategoryId(calNode.getProperty(Utils.EXO_CATEGORY_ID).getString()) ;
        if(calNode.hasProperty(Utils.EXO_LOCALE)) calendar.setLocale(calNode.getProperty(Utils.EXO_LOCALE).getString()) ;
        if(calNode.hasProperty(Utils.EXO_TIMEZONE)) calendar.setTimeZone(calNode.getProperty(Utils.EXO_TIMEZONE).getString()) ;
        if(calNode.hasProperty(Utils.EXO_SHARED_COLOR)) calendar.setCalendarColor(calNode.getProperty(Utils.EXO_SHARED_COLOR).getString()) ;
        if(calNode.hasProperty(Utils.EXO_CALENDAR_COLOR)) calendar.setCalendarColor(calNode.getProperty(Utils.EXO_CALENDAR_COLOR).getString()) ;
        if(calNode.hasProperty(Utils.EXO_CALENDAR_OWNER)) calendar.setCalendarOwner(calNode.getProperty(Utils.EXO_CALENDAR_OWNER).getString()) ;
        if(!calendar.isPublic()) {
          if(calNode.hasProperty(Utils.EXO_GROUPS)){
            Value[] values = calNode.getProperty(Utils.EXO_GROUPS).getValues() ;
            List<String> groups = new ArrayList<String>() ;
            for(Value v : values) {
              groups.add(v.getString()) ;
            }
            calendar.setGroups(groups.toArray(new String[groups.size()])) ;
          }
          if(calNode.hasProperty(Utils.EXO_VIEW_PERMISSIONS)) {
            Value[] viewValues = calNode.getProperty(Utils.EXO_VIEW_PERMISSIONS).getValues() ;
            List<String> viewPerms = new ArrayList<String>() ;
            for(Value v : viewValues) {
              viewPerms.add(v.getString()) ;
            }
            calendar.setViewPermission(viewPerms.toArray(new String[viewPerms.size()])) ;
          }
          if(calNode.hasProperty(Utils.EXO_EDIT_PERMISSIONS)) {
            Value[] editValues = calNode.getProperty(Utils.EXO_EDIT_PERMISSIONS).getValues() ;
            List<String> editPerms = new ArrayList<String>() ;
            for(Value v : editValues) {
              editPerms.add(v.getString()) ;
            }
            calendar.setEditPermission(editPerms.toArray(new String[editPerms.size()])) ;
          }      
        }  
      }
    }
    return calendar ;
  }  

  public List<GroupCalendarData> getCalendarCategories(String username, boolean isShowAll) throws Exception {
    SessionProvider sProvider = createSessionProvider();
    //try {
    Node calendarHome = getUserCalendarHome(username);
    NodeIterator iter = getCalendarCategoryHome(sProvider, username).getNodes();
    List<GroupCalendarData> calendarCategories = new ArrayList<GroupCalendarData>();
    List<Calendar> calendars;
    calendarHome.getSession().refresh(false);
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    String[] defaultCalendars = null;
    CalendarSetting calSetting = getCalendarSetting(username);
    if (calSetting != null) {
      defaultCalendars = calSetting.getFilterPrivateCalendars();
    }
    while (iter.hasNext()) {
      Node categoryNode = iter.nextNode();
      String categoryId = categoryNode.getProperty(Utils.EXO_ID).getString();
      StringBuffer queryString = new StringBuffer("/jcr:root" + calendarHome.getPath()
                                                  + "//element(*,exo:calendar)[@exo:categoryId='").append(categoryId).append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      calendars = new ArrayList<Calendar>();
      if (it.hasNext()) {
        while (it.hasNext()) {
          Calendar cal = getCalendar(defaultCalendars, username, it.nextNode(), isShowAll);
          if (cal != null)
            calendars.add(cal);
        }
      }
      calendarCategories.add(new GroupCalendarData(categoryId,
                                                   categoryNode.getProperty(Utils.EXO_NAME)
                                                   .getString(),
                                                   calendars));
    }
    return calendarCategories;
    /*} finally {
      closeSessionProvider(sProvider);
    }*/
  }

  public List<CalendarCategory> getCategories(String username) throws Exception {
    SessionProvider sProvider = createSessionProvider();
    try {
      Node calendarCategoryHome = getCalendarCategoryHome(sProvider, username) ;
      NodeIterator iter = calendarCategoryHome.getNodes() ;
      List<CalendarCategory> calendarCategories = new ArrayList<CalendarCategory> () ;
      while(iter.hasNext()) {
        calendarCategories.add(getCalendarCategory(iter.nextNode())) ;
      }
      return calendarCategories;
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception {
    Node calendarCategoryHome = getCalendarCategoryHome(null, username) ;
    return getCalendarCategory(calendarCategoryHome.getNode(calendarCategoryId)) ;
  }

  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    Node calCategoryHome = getCalendarCategoryHome(null, username) ;
    Node calCategoryNode ;
    if(isNew) {
      if(calCategoryHome.hasNode(calendarCategory.getId())) throw new Exception("This calendar category is already exists! ") ;
      calCategoryNode = calCategoryHome.addNode(calendarCategory.getId(),Utils.EXO_CALENDAR_CATEGORY) ;
      calCategoryNode.setProperty(Utils.EXO_ID, calendarCategory.getId()) ;
      if(calendarCategory.isDataInit()) reparePermissions(calCategoryNode, username) ;
    }else {
      calCategoryNode = calCategoryHome.getNode(calendarCategory.getId()) ;
    }
    calCategoryNode.setProperty(Utils.EXO_NAME, calendarCategory.getName()) ;
    calCategoryNode.setProperty(Utils.EXO_DESCRIPTION, calendarCategory.getDescription()) ;
    reparePermissions(calCategoryHome, username) ;
    calCategoryHome.getSession().save() ;
  }

  private void reparePermissions(Node node, String owner) throws Exception {
    /* ExtendedNode extNode = (ExtendedNode)node ;
    if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
    String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
    extNode.setPermission(owner, arrayPers) ;
    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;    
    for(AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
    } 
    extNode.removePermission("any") ;*/

  }

  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    Node calCategoryHome = getCalendarCategoryHome(null, username) ;
    Node calCategoryNode = calCategoryHome.getNode(calendarCategoryId) ; 
    CalendarCategory calCategory = getCalendarCategory(calCategoryNode) ;
    calCategoryNode.remove() ;
    for(Calendar cal : getUserCalendarsByCategory(username, calendarCategoryId)) {
      removeUserCalendar(username, cal.getId()) ;
    }
    calCategoryHome.save() ;
    //calendarHome.save() ;
    calCategoryHome.getSession().save() ;
    return calCategory ;
  }

  private CalendarCategory getCalendarCategory(Node calCategoryNode) throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    if(calCategoryNode.hasProperty(Utils.EXO_ID)) calCategory.setId(calCategoryNode.getProperty(Utils.EXO_ID).getString()) ;
    if(calCategoryNode.hasProperty(Utils.EXO_NAME)) calCategory.setName(calCategoryNode.getProperty(Utils.EXO_NAME).getString()) ;
    if(calCategoryNode.hasProperty(Utils.EXO_DESCRIPTION)) calCategory.setDescription(calCategoryNode.getProperty(Utils.EXO_DESCRIPTION).getString()) ;
    return calCategory ;
  }

  //Event Category APIs
  public List<EventCategory> getEventCategories(String username) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(username) ;
    NodeIterator iter = eventCategoryHome.getNodes() ;
    List<EventCategory> categories = new ArrayList<EventCategory> () ;
    while (iter.hasNext()) {
      categories.add(getEventCategory(iter.nextNode())) ;
    }
    return categories ;
  }

  public void saveEventCategory(String username, EventCategory eventCategory, String[] values, boolean isNew) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(username) ;
    Node eventCategoryNode = null ;
    if(isNew){
      NodeIterator iter = eventCategoryHome.getNodes() ;
      while(iter.hasNext()) {
        Node eCategiryNode = iter.nextNode() ;
        if(eventCategory.getName().trim().equalsIgnoreCase(eCategiryNode.getProperty(Utils.EXO_NAME).getString().trim())) {
          throw new ItemExistsException() ;
        }
      }
      //if(eventCategoryHome.hasNode(eventCategory.getName().toLowerCase())) throw new ItemExistsException() ;
      eventCategoryNode = eventCategoryHome.addNode(eventCategory.getId(), Utils.EXO_EVENT_CATEGORY) ;
      //eventCategoryNode = eventCategoryHome.addNode(eventCategory.getName().toLowerCase(), Utils.EXO_EVENT_CATEGORY) ;
      if(eventCategory.isDataInit()) reparePermissions(eventCategoryNode, username) ;
    }else {
      NodeIterator iter = eventCategoryHome.getNodes() ;
      while(iter.hasNext()) {
        Node eCategiryNode = iter.nextNode() ;
        if(!eCategiryNode.getName().equalsIgnoreCase(eventCategory.getId()) && eventCategory.getName().trim().equalsIgnoreCase(eCategiryNode.getProperty(Utils.EXO_NAME).getString().trim())) {
          throw new ItemExistsException() ;
        }
      }
      eventCategoryNode = eventCategoryHome.getNode(eventCategory.getId()) ;
      Node calendarHome = getUserCalendarHome(username) ;
      QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
      NodeIterator calIter = calendarHome.getNodes() ;
      Query query ;
      QueryResult result ;
      while (calIter.hasNext()) {
        StringBuffer queryString = new StringBuffer("/jcr:root" + calIter.nextNode().getPath() 
                                                    + "//element(*,exo:calendarEvent)[@exo:eventCategoryId='").
                                                    append(eventCategory.getId()).
                                                    append("']");
        query = qm.createQuery(queryString.toString(), Query.XPATH);
        result = query.execute();
        NodeIterator it = result.getNodes();
        while(it.hasNext()){
          Node eventNode = it.nextNode() ;
          eventNode.setProperty(Utils.EXO_EVENT_CATEGORY_NAME, eventCategory.getName()) ;
        }
      }

      // cs-2020      
      if(getSharedCalendarHome().hasNode(username)) {
        PropertyIterator iterPro = getSharedCalendarHome().getNode(username).getReferences() ;
        while(iterPro.hasNext()) {
          try{
            Node calendar = iterPro.nextProperty().getParent() ;
            NodeIterator it = calendar.getNodes();
            while(it.hasNext()){
              Node eventNode = it.nextNode() ;
              if (eventNode.getProperty(Utils.EXO_EVENT_CATEGORYID).getString().equals(eventCategory.getId()))
                eventNode.setProperty(Utils.EXO_EVENT_CATEGORY_NAME, eventCategory.getName()) ;
            }

          }catch (Exception e) {
            e.printStackTrace() ;
          }
        }
      }

      /*if(eventCategory.getName().equalsIgnoreCase(values[0])) {
        name = eventCategory.getName().toLowerCase() ;
        description = values[1] ;
      } else {
        if(eventCategoryHome.hasNode(values[0].toLowerCase()))throw new ItemExistsException() ; 
        else {
          eventCategoryNode.remove() ;
          eventCategoryHome.addNode(values[0].toLowerCase(), Utils.EXO_EVENT_CATEGORY) ;
          name = values[0].toLowerCase() ;
          description = values[1] ;
          Node calendarHome = getUserCalendarHome(sProvider, username) ;
          QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
          NodeIterator calIter = calendarHome.getNodes() ;
          Query query ;
          QueryResult result ;
          while (calIter.hasNext()) {
            StringBuffer queryString = new StringBuffer("/jcr:root" + calIter.nextNode().getPath() 
                + "//element(*,exo:calendarEvent)[@exo:eventCategoryId='").
                append(eventCategory.getName()).
                append("']");
            query = qm.createQuery(queryString.toString(), Query.XPATH);
            result = query.execute();
            NodeIterator it = result.getNodes();
            while(it.hasNext()){
              Node eventNode = it.nextNode() ;
              eventNode.setProperty(Utils.EXO_EVENT_CATEGORYID, name) ;
            }
          }
        }
      }
      eventCategoryNode = eventCategoryHome.getNode(name) ;*/
    }
    eventCategoryNode.setProperty(Utils.EXO_ID, eventCategory.getId()) ;
    eventCategoryNode.setProperty(Utils.EXO_NAME, eventCategory.getName()) ;
    eventCategoryNode.setProperty(Utils.EXO_DESCRIPTION, eventCategory.getDescription()) ;
    eventCategoryHome.getSession().save() ;
  } 

  public void removeEventCategory(String username, String eventCategoryName) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(username) ;
    if(eventCategoryHome.hasNode(eventCategoryName)) {
      Node eventCategoryNode = eventCategoryHome.getNode(eventCategoryName) ;
      for(CalendarEvent ce : getUserEventByCategory(username, eventCategoryName)) {
        removeUserEvent(username, ce.getCalendarId(), ce.getId()) ;
      }
      SessionProvider systemSession = SessionProvider.createSystemProvider() ;
      try {
        for(CalendarEvent ce : getSharedEventByCategory(username, eventCategoryName)) {
          removeSharedEvent(username, ce.getCalendarId(), ce.getId()) ;
        }
        for(CalendarEvent ce : getPublicEventByCategory(username, eventCategoryName)) {
          removePublicEvent(ce.getCalendarId(), ce.getId()) ;
        }
      } catch (Exception e) {
        e.printStackTrace() ;
      } finally {
        systemSession.close() ;
      }
      eventCategoryNode.remove() ;
      eventCategoryHome.save() ;
      eventCategoryHome.getSession().save() ;
    }
  }

  private EventCategory getEventCategory(Node eventCatNode) throws Exception {
    EventCategory eventCategory = new EventCategory() ;
    if(eventCatNode.hasProperty(Utils.EXO_ID)) eventCategory.setId(eventCatNode.getProperty(Utils.EXO_ID).getString()) ;
    if(eventCatNode.hasProperty(Utils.EXO_NAME)) eventCategory.setName(eventCatNode.getProperty(Utils.EXO_NAME).getString()) ;
    if(eventCatNode.hasProperty(Utils.EXO_DESCRIPTION)) eventCategory.setDescription(eventCatNode.getProperty(Utils.EXO_DESCRIPTION).getString()) ;
    return eventCategory ;
  }

  public EventCategory getEventCategory(String username, String eventCategoryName) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(username) ;
    return getEventCategory(eventCategoryHome.getNode(eventCategoryName)) ;
  }

  //Event APIs

  public CalendarEvent getUserEvent(String username, String calendarId, String eventId) throws Exception {
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId) ;
    return getEvent(calendarNode.getNode(eventId)) ;
  }

  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(String calendarId : calendarIds) {
      Node calendarNode = getUserCalendarHome(username).getNode(calendarId) ;
      NodeIterator it = calendarNode.getNodes();
      while(it.hasNext()) {
        events.add(getEvent(it.nextNode())) ;
      }
    }
    return events ;
  }
  private List<CalendarEvent> getPublicEventByCategory(String username, String eventCategoryId) throws Exception {
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    Node publicCalendarHome = getPublicCalendarHome(systemSession) ;
    QueryManager qm = publicCalendarHome.getSession().getWorkspace().getQueryManager();
    List<CalendarEvent> events = new ArrayList<CalendarEvent> () ;
    try {
      Query query ;
      QueryResult result ;
      NodeIterator calIter = publicCalendarHome.getNodes() ;
      while (calIter.hasNext()) {
        StringBuffer queryString = new StringBuffer("/jcr:root" + calIter.nextNode().getPath() 
                                                    + "//element(*,exo:calendarEvent)[@exo:eventCategoryId='").
                                                    append(eventCategoryId).
                                                    append("']");
        query = qm.createQuery(queryString.toString(), Query.XPATH);
        result = query.execute();
        NodeIterator it = result.getNodes();
        while(it.hasNext()){
          events.add(getEvent(it.nextNode())) ;
        }
      } 
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      systemSession.close() ;
    }
    return events ;
  }

  private List<CalendarEvent> getSharedEventByCategory(String username, String eventCategoryId) throws Exception {
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent> () ;    
    try {
      if(getSharedCalendarHome(systemSession).hasNode(username)) {
        PropertyIterator iterPro = getSharedCalendarHome(systemSession).getNode(username).getReferences() ;
        while(iterPro.hasNext()) {
          try {
            Node calendar = iterPro.nextProperty().getParent() ;
            NodeIterator it = calendar.getNodes();
            while(it.hasNext()){
              Node eventNode = it.nextNode() ;
              if (eventNode.getProperty(Utils.EXO_EVENT_CATEGORYID).getString().equals(eventCategoryId)) {
                events.add(getEvent(eventNode)) ;
              }
            }

          }catch (Exception e) {
            e.printStackTrace() ;
          }
        }
      }
      /*    
    try {
      while (calIter.hasNext()) {
        StringBuffer queryString = new StringBuffer("/jcr:root" + calIter.nextNode().getPath() 
                                                    + "//element(*,exo:calendarEvent)[@exo:eventCategoryId='").
                                                    append(eventCategoryId).
                                                    append("']");
        query = qm.createQuery(queryString.toString(), Query.XPATH);
        result = query.execute();
        NodeIterator it = result.getNodes();
        while(it.hasNext()){
          events.add(getEvent(systemSession, it.nextNode())) ;
        }
      }*/
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      systemSession.close() ;
    }
    return events ;
  }

  public List<CalendarEvent> getUserEventByCategory(String username, String eventCategoryId) throws Exception {
    Node calendarHome = getUserCalendarHome(username) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    List<CalendarEvent> events = new ArrayList<CalendarEvent> () ;
    Query query ;
    QueryResult result ;
    NodeIterator calIter = calendarHome.getNodes() ;
    while (calIter.hasNext()) {
      StringBuffer queryString = new StringBuffer("/jcr:root" + calIter.nextNode().getPath() 
                                                  + "//element(*,exo:calendarEvent)[@exo:eventCategoryId='").
                                                  append(eventCategoryId).
                                                  append("']");
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      NodeIterator it = result.getNodes();
      while(it.hasNext()){
        events.add(getEvent(it.nextNode())) ;
      }
    }
    return events;
  }

  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception {
    Node calendarHome = getUserCalendarHome(username) ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    eventQuery.setCalendarPath(calendarHome.getPath()) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager() ;
    Query query = qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType()) ;
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    CalendarEvent calEvent ;
    while(it.hasNext()) {
      calEvent = getEvent(it.nextNode()) ;
      calEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
      events.add(calEvent) ;
      if( eventQuery.getLimitedItems() == it.getPosition() ) break ;
    }
    return events ;
  }

  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
    if(event.getReminders() != null && event.getReminders().size() > 0) {
      //Need to use system session
      SessionProvider systemSession = SessionProvider.createSystemProvider();
      try {
        Node reminderFolder = getReminderFolder(event.getFromDateTime()) ;
        saveEvent(calendarNode, event, reminderFolder, isNew) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      } finally {
        systemSession.close() ;
      }
    }else {
      saveEvent(calendarNode, event, null, isNew) ;
    }
  }

  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception {
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
    if(calendarNode.hasNode(eventId)){
      Node eventNode = calendarNode.getNode(eventId) ;
      CalendarEvent event = getEvent(eventNode) ;
      //Need to use system session
      SessionProvider systemSession = SessionProvider.createSystemProvider() ;
      try {
        Node eventFolder = getEventFolder(systemSession, event.getFromDateTime()) ;
        syncRemoveEvent(eventFolder, event.getId()) ;
      } catch (Exception e) {
        e.printStackTrace() ;        
      } finally {
        systemSession.close() ;
      }
      removeReminder(eventNode) ;
      eventNode.remove() ;
      calendarNode.save() ;
      calendarNode.getSession().save() ;
      calendarNode.refresh(true) ;
      return event;
    }
    return null ;
  }

  private void removeReminder(Node eventNode)throws Exception {

    // Need to use system session
    if(eventNode.hasProperty(Utils.EXO_FROM_DATE_TIME)) {
      SessionProvider systemSession = createSystemProvider();
      try {
        Node reminders = getReminderFolder(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()) ;
        //System.out.println("path ------------" + reminders.getPath());
        //if(reminders.hasNode(eventNode.getName())) reminders.getNode(eventNode.getName()).remove() ;
        try{
          reminders.getNode(eventNode.getName()).remove() ;
          reminders.save();
        }catch (Exception e) {
          //e.printStackTrace() ;
        }
        Node events = reminders.getParent().getNode(Utils.CALENDAR_REMINDER) ;
        if(events != null && events.hasNode(eventNode.getName())) {
          if(events.hasNode(eventNode.getName())) {
            //System.out.println("=--------removed ?");
            events.getNode(eventNode.getName()).remove() ;
            if(!reminders.isNew())reminders.save() ;
            else reminders.getSession().save() ;
          }
        }
      }  catch (Exception e) {
        e.printStackTrace() ;
      }
      //TODO should not close session here, after remove reminder we have use session to sync event
      /* finally {
        closeSessionProvider(systemSession);
      }*/
    }
  } 

  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId) ;
    CalendarEvent calEvent = getEvent(calendarNode.getNode(eventId)) ;
    calEvent.setCalType(String.valueOf(Calendar.TYPE_PUBLIC)) ;
    return calEvent ;
  }

  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(String calendarId : calendarIds){
      Node calendarNode = getPublicCalendarHome().getNode(calendarId) ;    
      NodeIterator it = calendarNode.getNodes();
      while(it.hasNext()) {
        events.add(getEvent(it.nextNode())) ;
      }
    }
    return events ;
  }

  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception {
    Node calendarHome = getPublicCalendarHome() ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    eventQuery.setCalendarPath(calendarHome.getPath()) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager() ;
    Query query = qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType()) ;
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    CalendarEvent calEvent ;
    while(it.hasNext()) {
      calEvent = getEvent(it.nextNode()) ;
      calEvent.setCalType(String.valueOf(Calendar.TYPE_PUBLIC)) ;
      events.add(calEvent) ;
      if( eventQuery.getLimitedItems() == it.getPosition() ) break ;
    }
    return events ;
  }
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId) ;
    Node reminderFolder = getReminderFolder(event.getFromDateTime()) ;
    saveEvent(calendarNode, event, reminderFolder, isNew) ;
  }

  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId) ;
    if(calendarNode.hasNode(eventId)){
      Node eventNode = calendarNode.getNode(eventId) ;
      CalendarEvent event = getEvent(eventNode) ;
      removeReminder(eventNode) ;
      eventNode.remove() ;
      calendarNode.save() ;
      calendarNode.getSession().save() ;
      calendarNode.refresh(true) ;
      SessionProvider systemSession = SessionProvider.createSystemProvider() ;
      try {
        Node eventFolder = getEventFolder(systemSession, event.getFromDateTime()) ;
        syncRemoveEvent(eventFolder, eventId) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      } finally {
        systemSession.close() ;
      }
      return event;
    }
    return null ;
  }


  private CalendarEvent getEvent(Node eventNode) throws Exception {
    CalendarEvent event = new CalendarEvent() ;
    if(eventNode.hasProperty(Utils.EXO_ID)) event.setId(eventNode.getProperty(Utils.EXO_ID).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_CALENDAR_ID))event.setCalendarId(eventNode.getProperty(Utils.EXO_CALENDAR_ID).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_SUMMARY)) event.setSummary(eventNode.getProperty(Utils.EXO_SUMMARY).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_EVENT_CATEGORYID)) event.setEventCategoryId(eventNode.getProperty(Utils.EXO_EVENT_CATEGORYID).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_EVENT_CATEGORY_NAME)) event.setEventCategoryName(eventNode.getProperty(Utils.EXO_EVENT_CATEGORY_NAME).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_LOCATION)) event.setLocation(eventNode.getProperty(Utils.EXO_LOCATION).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_TASK_DELEGATOR)) event.setTaskDelegator(eventNode.getProperty(Utils.EXO_TASK_DELEGATOR).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_REPEAT)) event.setRepeatType(eventNode.getProperty(Utils.EXO_REPEAT).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_DESCRIPTION)) event.setDescription(eventNode.getProperty(Utils.EXO_DESCRIPTION).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_FROM_DATE_TIME)) event.setFromDateTime(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()) ;
    if(eventNode.hasProperty(Utils.EXO_TO_DATE_TIME)) event.setToDateTime(eventNode.getProperty(Utils.EXO_TO_DATE_TIME).getDate().getTime()) ;
    if(eventNode.hasProperty(Utils.EXO_EVENT_TYPE)) event.setEventType(eventNode.getProperty(Utils.EXO_EVENT_TYPE).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_PRIORITY)) event.setPriority(eventNode.getProperty(Utils.EXO_PRIORITY).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_IS_PRIVATE)) event.setPrivate(eventNode.getProperty(Utils.EXO_IS_PRIVATE).getBoolean()) ;
    if(eventNode.hasProperty(Utils.EXO_EVENT_STATE)) event.setEventState(eventNode.getProperty(Utils.EXO_EVENT_STATE).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_SEND_OPTION)) event.setSendOption(eventNode.getProperty(Utils.EXO_SEND_OPTION).getString()) ;
    if(eventNode.hasProperty(Utils.EXO_MESSAGE)) event.setMessage(eventNode.getProperty(Utils.EXO_MESSAGE).getString()) ;
    
    SessionProvider systemSession =  SessionProvider.createSystemProvider() ;
    try {
      event.setReminders(getReminders(eventNode)) ;
    }catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      systemSession.close() ;
    }
    event.setAttachment(getAttachments(eventNode)) ;
    if(eventNode.hasProperty(Utils.EXO_INVITATION)){
      Value[] values = eventNode.getProperty(Utils.EXO_INVITATION).getValues() ;
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
    if(eventNode.hasProperty(Utils.EXO_PARTICIPANT)){
      Value[] values = eventNode.getProperty(Utils.EXO_PARTICIPANT).getValues() ;
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
    if(eventNode.hasProperty(Utils.EXO_PARTICIPANT_STATUS)){
      Value[] values = eventNode.getProperty(Utils.EXO_PARTICIPANT_STATUS).getValues() ;
      if(values.length == 1 ){      
        event.setParticipantStatus(new String[]{values[0].getString()}) ;
      }else {
        String[] participantStatus = new String[values.length] ;
        for(int i = 0; i < values.length; i ++) {
          participantStatus[i] = values[i].getString() ;
        }
        event.setParticipantStatus(participantStatus) ;
      }
    }
    return event ;
  }

  private void saveEvent(Node calendarNode, CalendarEvent event, Node reminderFolder, boolean isNew) throws Exception {
    Node eventNode ;
    if(isNew) {
      eventNode = calendarNode.addNode(event.getId(), Utils.EXO_CALENDAR_EVENT) ;
      eventNode.setProperty(Utils.EXO_ID, event.getId()) ;
    }else {
      try {
        eventNode = calendarNode.getNode(event.getId()) ;
      } catch (Exception e) {
        //System.out.println("\n\n event added");
        eventNode = calendarNode.addNode(event.getId(), Utils.EXO_CALENDAR_EVENT) ;
        eventNode.setProperty(Utils.EXO_ID, event.getId()) ;
      }
      SessionProvider systemSession =  SessionProvider.createSystemProvider() ;
      try {
        removeReminder(eventNode) ; 
      }catch (Exception e) {
        e.printStackTrace() ;
      } finally {
        systemSession.close() ;
      }
    }
    eventNode.setProperty(Utils.EXO_SUMMARY, event.getSummary()) ;
    eventNode.setProperty(Utils.EXO_CALENDAR_ID, event.getCalendarId()) ;
    eventNode.setProperty(Utils.EXO_EVENT_CATEGORYID, event.getEventCategoryId()) ;
    eventNode.setProperty(Utils.EXO_EVENT_CATEGORY_NAME, event.getEventCategoryName()) ;
    eventNode.setProperty(Utils.EXO_DESCRIPTION, event.getDescription()) ;
    eventNode.setProperty(Utils.EXO_LOCATION, event.getLocation()) ;
    eventNode.setProperty(Utils.EXO_TASK_DELEGATOR, event.getTaskDelegator()) ;

    GregorianCalendar dateTime = Utils.getInstanceTempCalendar() ;
    dateTime.setTime(event.getFromDateTime()) ;
    eventNode.setProperty(Utils.EXO_FROM_DATE_TIME, dateTime) ;
    dateTime.setTime(event.getToDateTime()) ;
    eventNode.setProperty(Utils.EXO_TO_DATE_TIME, dateTime) ;
    eventNode.setProperty(Utils.EXO_EVENT_TYPE, event.getEventType()) ;
    eventNode.setProperty(Utils.EXO_REPEAT, event.getRepeatType()) ;
    eventNode.setProperty(Utils.EXO_PRIORITY, event.getPriority()) ;
    eventNode.setProperty(Utils.EXO_IS_PRIVATE, event.isPrivate()) ;
    eventNode.setProperty(Utils.EXO_EVENT_STATE, event.getEventState()) ;
    if(event.getInvitation() == null) event.setInvitation(new String[]{}) ; 
    eventNode.setProperty(Utils.EXO_INVITATION,  event.getInvitation()) ;
    if(event.getParticipant() == null) event.setParticipant(new String[]{}) ; 
    eventNode.setProperty(Utils.EXO_PARTICIPANT, event.getParticipant()) ;
    // add reminder child node
    List<Reminder> reminders = event.getReminders() ;
    if(reminders != null && !reminders.isEmpty()) {
      for(Reminder rm : reminders) {
        rm.setFromDateTime(event.getFromDateTime()) ;
        addReminder(eventNode, reminderFolder, rm) ;
      }
    }
    //  add attachment child node
    if(eventNode.hasNode(Utils.ATTACHMENT_NODE)) {
      while (eventNode.getNodes().hasNext()) {
        eventNode.getNodes().nextNode().remove() ;
      }
      eventNode.save() ;
    }
    List<Attachment> attachments = event.getAttachment() ;
    if(attachments != null) {
      for(Attachment att : attachments) {
        addAttachment(eventNode, att, isNew) ;
      }
    }
    //TODO cs-764
    eventNode.setProperty(Utils.EXO_MESSAGE, event.getMessage());
    eventNode.setProperty(Utils.EXO_SEND_OPTION, event.getSendOption()) ;
 
    if(event.getParticipantStatus() == null) event.setParticipantStatus(new String[]{}) ; 
    eventNode.setProperty(Utils.EXO_PARTICIPANT_STATUS, event.getParticipantStatus()) ;
 


    calendarNode.getSession().save() ;

    addEvent(event) ;

    //calendarNode.getSession().save()  ;
  }
  private void addReminder(Node eventNode, Node reminderFolder, Reminder reminder) throws Exception {
    Node reminderNode ;
    Node catNode ;
    try {
      catNode = reminderFolder.getNode(eventNode.getName()) ;
    } catch (Exception e) {
      catNode = reminderFolder.addNode(eventNode.getName(), Utils.NT_UNSTRUCTURED) ;
    }
    try {
      reminderNode = catNode.getNode(reminder.getId()) ;
    } catch (Exception e) {
      reminderNode = catNode.addNode(reminder.getId(), Utils.EXO_REMINDER) ;
    }
    reminderNode.setProperty(Utils.EXO_EVENT_ID, eventNode.getName()) ;
    reminderNode.setProperty(Utils.EXO_ALARM_BEFORE, reminder.getAlarmBefore()) ;
    reminderNode.setProperty(Utils.EXO_TIME_INTERVAL, reminder.getRepeatInterval()) ;
    reminderNode.setProperty(Utils.EXO_REMINDER_TYPE, reminder.getReminderType()) ;
    reminderNode.setProperty(Utils.EXO_EMAIL, reminder.getEmailAddress()) ;
    reminderNode.setProperty(Utils.EXO_IS_REPEAT, reminder.isRepeat()) ;
    reminderNode.setProperty(Utils.EXO_IS_OVER, false) ;
    if(reminder.getReminderType().equals(Reminder.TYPE_POPUP)) {
      reminderNode.setProperty(Utils.EXO_OWNER, reminder.getReminderOwner()) ;
    }
    java.util.Calendar cal = new GregorianCalendar() ;
    if(reminder.getFromDateTime() != null) {
      cal.setTime(reminder.getFromDateTime()) ;
      reminderNode.setProperty(Utils.EXO_FROM_DATE_TIME, cal) ;
      long time = reminder.getFromDateTime().getTime() - (reminder.getAlarmBefore() * 60 * 1000) ;
      cal = new GregorianCalendar() ;
      cal.setTimeInMillis(time) ;
      reminderNode.setProperty(Utils.EXO_REMINDER_DATE, cal) ;
    }
    StringBuffer summary = new StringBuffer("Type      : ") ;
    summary.append(eventNode.getProperty(Utils.EXO_EVENT_TYPE).getString()).append("<br>") ;
    summary.append("Summary: ") ;
    summary.append(eventNode.getProperty(Utils.EXO_SUMMARY).getString()).append("<br>") ;
    summary.append("Description: ") ;
    if(eventNode.hasProperty(Utils.EXO_DESCRIPTION))
      summary.append(eventNode.getProperty(Utils.EXO_DESCRIPTION).getString());    	
    summary.append("<br>")  ;
    summary.append("Location   : ") ; 
    if(eventNode.hasProperty(Utils.EXO_LOCATION)) 
      summary.append(eventNode.getProperty(Utils.EXO_LOCATION).getString()) ;
    summary.append("<br>") ;
    cal.setTime(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()) ;
    summary.append("From       : ").append(cal.get(java.util.Calendar.HOUR_OF_DAY)).append(":") ;
    summary.append(cal.get(java.util.Calendar.MINUTE)).append(" - ") ;
    summary.append(cal.get(java.util.Calendar.DATE)).append("/") ;
    summary.append(cal.get(java.util.Calendar.MONTH) + 1).append("/") ;
    summary.append(cal.get(java.util.Calendar.YEAR)).append("<br>") ;
    cal.setTime(eventNode.getProperty(Utils.EXO_TO_DATE_TIME).getDate().getTime()) ;
    summary.append("To         : ").append(cal.get(java.util.Calendar.HOUR_OF_DAY)).append(":") ;
    summary.append(cal.get(java.util.Calendar.MINUTE)).append(" - ") ;
    summary.append(cal.get(java.util.Calendar.DATE)).append("/") ;
    summary.append(cal.get(java.util.Calendar.MONTH) + 1).append("/") ;
    summary.append(cal.get(java.util.Calendar.YEAR)).append("<br>") ;
    reminderNode.setProperty(Utils.EXO_DESCRIPTION, summary.toString()) ;
    reminderNode.setProperty(Utils.EXO_SUMMARY, eventNode.getProperty(Utils.EXO_SUMMARY).getString()) ;
    if(!reminderFolder.isNew()) reminderFolder.save() ;
    else reminderFolder.getSession().save() ;
  }

  private void addEvent(CalendarEvent event) throws Exception {
    SessionProvider provider = SessionProvider.createSystemProvider() ;
    Node eventFolder = getEventFolder(provider, event.getFromDateTime()) ;
    Node publicEvent ;
    int fromDate ;
    int toDate ;
    syncRemoveEvent(eventFolder, event.getId()) ;
    CalendarEvent ev = new CalendarEvent() ;
    publicEvent = eventFolder.addNode(ev.getId(), Utils.EXO_CALENDAR_PUBLIC_EVENT) ;
    publicEvent.setProperty(Utils.EXO_ID, ev.getId()) ;
    publicEvent.setProperty(Utils.EXO_ROOT_EVENT_ID, event.getId()) ;
    publicEvent.setProperty(Utils.EXO_EVENT_TYPE, event.getEventType()) ;
    publicEvent.setProperty(Utils.EXO_CALENDAR_ID, event.getCalendarId()) ;
    java.util.Calendar dateTime = Utils.getInstanceTempCalendar() ;
    dateTime.setTime(event.getFromDateTime()) ;
    fromDate = dateTime.get(java.util.Calendar.DAY_OF_YEAR) ;
    publicEvent.setProperty(Utils.EXO_FROM_DATE_TIME, dateTime) ;
    publicEvent.setProperty(Utils.EXO_EVENT_STATE, event.getEventState()) ;
    dateTime.setTime(event.getToDateTime()) ;
    toDate = dateTime.get(java.util.Calendar.DAY_OF_YEAR) ;
    if(toDate > fromDate) {
      java.util.Calendar tmpTime = Utils.getInstanceTempCalendar() ;
      tmpTime.setTime(event.getFromDateTime()) ;
      tmpTime.set(java.util.Calendar.HOUR_OF_DAY, 0) ;
      tmpTime.set(java.util.Calendar.MINUTE, 0) ;
      tmpTime.set(java.util.Calendar.SECOND, 0) ;
      tmpTime.set(java.util.Calendar.MILLISECOND, 0) ;
      tmpTime.setTimeInMillis(tmpTime.getTimeInMillis() + (24 * 60 * 60 * 1000) - 1000) ;
      publicEvent.setProperty(Utils.EXO_TO_DATE_TIME, tmpTime) ;
    }else {
      publicEvent.setProperty(Utils.EXO_TO_DATE_TIME, dateTime) ;
    }
    publicEvent.setProperty(Utils.EXO_PARTICIPANT, event.getParticipant()) ;
    try{
      if(!eventFolder.isNew()) eventFolder.save() ;
      else eventFolder.getSession().save() ;
    }catch(Exception e) {
      eventFolder.getSession().refresh(true) ;
      eventFolder.getSession().save() ;
      e.printStackTrace() ;
    } finally {
      provider.close() ;
    }
    try {
      provider = SessionProvider.createSystemProvider() ;
      if(toDate > fromDate) {
        java.util.Calendar cal = Utils.getInstanceTempCalendar() ;
        cal.setTime(event.getFromDateTime()) ;
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0) ;
        cal.set(java.util.Calendar.MINUTE, 0) ;
        cal.set(java.util.Calendar.SECOND, 0) ;
        cal.set(java.util.Calendar.MILLISECOND, 0) ;
        for(int i = fromDate + 1; i <= toDate ; i++) {
          cal.roll(java.util.Calendar.DAY_OF_YEAR, true) ;
          Node dateFolder = getEventFolder(provider, cal.getTime()) ;
          ev = new CalendarEvent() ;
          eventFolder.getSession().getWorkspace().copy(publicEvent.getPath(), dateFolder.getPath() + Utils.SLASH + ev.getId()) ;
          dateFolder.getSession().save() ;
          if(i <= toDate) {
            Node newEvent = dateFolder.getNode(ev.getId()) ;
            newEvent.setProperty(Utils.EXO_ID, ev.getId()) ;
            newEvent.setProperty(Utils.EXO_FROM_DATE_TIME, cal) ;
            java.util.Calendar tmpCal = Utils.getInstanceTempCalendar() ;
            if(i == toDate) tmpCal.setTime(event.getToDateTime()) ;
            else tmpCal.setTimeInMillis(cal.getTimeInMillis() + (24 * 60 * 60 * 1000) - 1000) ;
            newEvent.setProperty(Utils.EXO_TO_DATE_TIME, tmpCal) ;
            newEvent.save() ;    			    			
          }    		
        }
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      provider.close() ;
    }
  }

  private void syncRemoveEvent(Node eventFolder, String rootEventId) throws Exception{
    QueryManager qm = eventFolder.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + eventFolder.getParent().getParent().getParent().getPath() 
                                                + "//element(*,exo:calendarPublicEvent)[@exo:rootEventId='").
                                                append(rootEventId).
                                                append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    while(it.hasNext()) {
      it.nextNode().remove() ;
    }
    eventFolder.getSession().save() ;
    eventFolder.refresh(true) ;
  }
  private Node getReminderFolder(Date fromDate)throws Exception {
    Node publicApp = getPublicCalendarServiceHome() ;
    Node dateFolder = getDateFolder(publicApp, fromDate) ;
    try {
      return dateFolder.getNode(Utils.CALENDAR_REMINDER) ;
    } catch (Exception e) {
      dateFolder.addNode(Utils.CALENDAR_REMINDER, Utils.NT_UNSTRUCTURED) ;
      //getPublicRoot(sysProvider).getSession().save() ;
      if(dateFolder.isNew())  dateFolder.getSession().save();
      else dateFolder.save() ;
      return dateFolder.getNode(Utils.CALENDAR_REMINDER) ;
    }
  }

  private Node getEventFolder(SessionProvider provider, Date fromDate)throws Exception {
    Node publicApp = getPublicCalendarServiceHome() ;
    Node dateFolder = getDateFolder(publicApp, fromDate) ;
    try {
      return dateFolder.getNode(CALENDAR_EVENT) ;
    } catch (Exception e) {
      dateFolder.addNode(CALENDAR_EVENT, Utils.NT_UNSTRUCTURED) ;
      getPublicRoot(provider).getSession().save() ;
      //e.printStackTrace() ;
      return dateFolder.getNode(CALENDAR_EVENT) ;

    }
  }

  private Node getEventFolder(Date fromDate)throws Exception {
    Node publicApp = getPublicCalendarServiceHome() ;
    Node dateFolder = getDateFolder(publicApp, fromDate) ;
    try {
      return dateFolder.getNode(CALENDAR_EVENT) ;
    } catch (Exception e) {
      dateFolder.addNode(CALENDAR_EVENT, Utils.NT_UNSTRUCTURED) ;
      getPublicRoot().getSession().save() ;
      //e.printStackTrace() ;
      return dateFolder.getNode(CALENDAR_EVENT) ;

    }
  }

  private Node getDateFolder(Node publicApp, Date date) throws Exception {
    java.util.Calendar fromCalendar = new GregorianCalendar() ;
    fromCalendar.setTime(date) ;
    Node yearNode;
    Node monthNode;
    String year = "Y" + String.valueOf(fromCalendar.get(java.util.Calendar.YEAR)) ;
    String month = "M" + String.valueOf(fromCalendar.get(java.util.Calendar.MONTH) + 1) ;
    String day = "D" + String.valueOf(fromCalendar.get(java.util.Calendar.DATE)) ;
    try {
      yearNode = publicApp.getNode(year) ;
    } catch (Exception e) {
      yearNode = publicApp.addNode(year, Utils.NT_UNSTRUCTURED) ;
    }
    try {
      monthNode = yearNode.getNode(month) ;
    } catch (Exception e) {
      monthNode = yearNode.addNode(month, Utils.NT_UNSTRUCTURED) ;
    }
    try {
      return monthNode.getNode(day) ;
    } catch (Exception e) {
      return monthNode.addNode(day, Utils.NT_UNSTRUCTURED) ;
    }
  }

  private List<Reminder> getReminders(Node eventNode) throws Exception {
    List<Reminder> reminders = new ArrayList<Reminder> () ;
    Date fromDate = eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime() ;
    Node reminderFolder = getReminderFolder(fromDate) ;
    if(reminderFolder.hasNode(eventNode.getName())) {
      NodeIterator iter = reminderFolder.getNode(eventNode.getName()).getNodes() ;
      while(iter.hasNext()) {
        Node reminderNode = iter.nextNode() ;
        if(reminderNode.isNodeType(Utils.EXO_REMINDER)) {
          Reminder reminder = new Reminder() ;
          reminder.setId(reminderNode.getName()) ;
          if(reminderNode.hasProperty(Utils.EXO_OWNER))reminder.setReminderOwner(reminderNode.getProperty(Utils.EXO_OWNER).getString()) ; 
          if(reminderNode.hasProperty(Utils.EXO_EVENT_ID)) reminder.setEventId(reminderNode.getProperty(Utils.EXO_EVENT_ID).getString()) ;
          if(reminderNode.hasProperty(Utils.EXO_REMINDER_TYPE)) reminder.setReminderType(reminderNode.getProperty(Utils.EXO_REMINDER_TYPE).getString()) ;
          if(reminderNode.hasProperty(Utils.EXO_ALARM_BEFORE))reminder.setAlarmBefore(reminderNode.getProperty(Utils.EXO_ALARM_BEFORE).getLong()) ;
          if(reminderNode.hasProperty(Utils.EXO_EMAIL)) reminder.setEmailAddress(reminderNode.getProperty(Utils.EXO_EMAIL).getString()) ;
          if(reminderNode.hasProperty(Utils.EXO_IS_REPEAT)) reminder.setRepeate(reminderNode.getProperty(Utils.EXO_IS_REPEAT).getBoolean()) ;
          if(reminderNode.hasProperty(Utils.EXO_TIME_INTERVAL)) reminder.setRepeatInterval(reminderNode.getProperty(Utils.EXO_TIME_INTERVAL).getLong()) ;
          if(reminderNode.hasProperty(Utils.EXO_DESCRIPTION)) reminder.setDescription(reminderNode.getProperty(Utils.EXO_DESCRIPTION).getString());
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
    //fix load image on IE6 UI
    ExtendedNode extNode = (ExtendedNode)eventNode ;
    if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
    String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
    extNode.setPermission(SystemIdentity.ANY, arrayPers) ;
    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;   
    for(AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
    } 
    try {
      attachHome = eventNode.getNode(Utils.ATTACHMENT_NODE) ;
    } catch (Exception e) {
      attachHome = eventNode.addNode(Utils.ATTACHMENT_NODE, Utils.NT_UNSTRUCTURED) ;
    }
    String name = attachment.getId().substring(attachment.getId().lastIndexOf(Utils.SLASH)+1) ; 
    try {
      attachNode = attachHome.getNode(name) ;
    } catch (Exception e) {
      attachNode = attachHome.addNode(name, Utils.EXO_EVEN_TATTACHMENT) ;
    }
    attachNode.setProperty(Utils.EXO_FILE_NAME, attachment.getName()) ;
    Node nodeContent = null;
    try {
      nodeContent = attachNode.getNode(Utils.JCR_CONTENT);
    } catch (Exception e) {
      nodeContent = attachNode.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
    }
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
          attachment.setId(attchmentNode.getPath()) ;
          if(attchmentNode.hasProperty(Utils.EXO_FILE_NAME)) attachment.setName(attchmentNode.getProperty(Utils.EXO_FILE_NAME).getString()) ;
          Node contentNode = attchmentNode.getNode(Utils.JCR_CONTENT) ; 
          if(contentNode != null) {
            if(contentNode.hasProperty(Utils.JCR_LASTMODIFIED)) attachment.setLastModified(contentNode.getProperty(Utils.JCR_LASTMODIFIED).getDate()) ;
            if(contentNode.hasProperty(Utils.JCR_MIMETYPE)) attachment.setMimeType(contentNode.getProperty(Utils.JCR_MIMETYPE).getString()) ;
            if(contentNode.hasProperty(Utils.JCR_DATA)) {
              InputStream  inputStream = contentNode.getProperty(Utils.JCR_DATA).getStream() ;
              attachment.setSize(inputStream.available()) ;
              attachment.setInputStream(inputStream) ;
            }
          }
          attachment.setWorkspace(attchmentNode.getSession().getWorkspace().getName()) ;
          attachments.add(attachment) ;
        }
      }
    }
    return attachments ;
  }
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception {
    Node calendarHome = getUserCalendarServiceHome(username) ;
    addCalendarSetting(calendarHome, setting) ;
    calendarHome.save() ;
  }

  private void addCalendarSetting(Node calendarHome, CalendarSetting setting) throws Exception {
    Node settingNode ;
    try {
      settingNode = calendarHome.getNode(CALENDAR_SETTING) ; 
    } catch (Exception e) {
      settingNode = calendarHome.addNode(CALENDAR_SETTING, Utils.EXO_CALENDAR_SETTING) ;
    }
    settingNode.setProperty(Utils.EXO_VIEW_TYPE, setting.getViewType()) ;
    settingNode.setProperty(Utils.EXO_TIME_INTERVAL, setting.getTimeInterval()) ;
    settingNode.setProperty(Utils.EXO_WEEK_START_ON, setting.getWeekStartOn()) ;
    settingNode.setProperty(Utils.EXO_DATE_FORMAT, setting.getDateFormat()) ;
    settingNode.setProperty(Utils.EXO_TIME_FORMAT, setting.getTimeFormat()) ;
    settingNode.setProperty(Utils.EXO_LOCATION, setting.getLocation()) ;
    settingNode.setProperty(Utils.EXO_TIMEZONE, setting.getTimeZone()) ;
    settingNode.setProperty(Utils.EXO_IS_SHOW_WORKING_TIME, setting.isShowWorkingTime()) ;
    if(setting.isShowWorkingTime()) {
      settingNode.setProperty(Utils.EXO_WORKING_BEGIN, setting.getWorkingTimeBegin()) ;
      settingNode.setProperty(Utils.EXO_WORKING_END, setting.getWorkingTimeEnd()) ;
    }
    settingNode.setProperty(Utils.EXO_BASE_URL, setting.getBaseURL()) ;
    settingNode.setProperty(Utils.EXO_PRIVATE_CALENDARS, setting.getFilterPrivateCalendars()) ;
    settingNode.setProperty(Utils.EXO_PUBLIC_CALENDARS, setting.getFilterPublicCalendars()) ;
    settingNode.setProperty(Utils.EXO_SHARED_CALENDARS, setting.getFilterSharedCalendars()) ;
    settingNode.setProperty(Utils.EXO_SHARED_CALENDAR_COLORS, setting.getSharedCalendarsColors()) ;
    settingNode.setProperty(Utils.EXO_SEND_OPTION, setting.getSendOption()) ;
  }
  public CalendarSetting getCalendarSetting(String username) throws Exception{
    Node calendarHome = getUserCalendarServiceHome(username) ;
    if(calendarHome.hasNode(CALENDAR_SETTING)){
      CalendarSetting calendarSetting = new CalendarSetting() ;
      Node settingNode = calendarHome.getNode(CALENDAR_SETTING) ;      
      calendarSetting.setViewType(settingNode.getProperty(Utils.EXO_VIEW_TYPE).getString()) ;
      calendarSetting.setTimeInterval(settingNode.getProperty(Utils.EXO_TIME_INTERVAL).getLong()) ;
      calendarSetting.setWeekStartOn(settingNode.getProperty(Utils.EXO_WEEK_START_ON).getString()) ;
      calendarSetting.setDateFormat(settingNode.getProperty(Utils.EXO_DATE_FORMAT).getString()) ;
      calendarSetting.setTimeFormat(settingNode.getProperty(Utils.EXO_TIME_FORMAT).getString()) ;
      if(settingNode.hasProperty(Utils.EXO_SEND_OPTION)) calendarSetting.setSendOption(settingNode.getProperty(Utils.EXO_SEND_OPTION).getString()) ;
      if(settingNode.hasProperty(Utils.EXO_BASE_URL)) calendarSetting.setBaseURL(settingNode.getProperty(Utils.EXO_BASE_URL).getString()) ;
      if(settingNode.hasProperty(Utils.EXO_LOCATION))
        calendarSetting.setLocation(settingNode.getProperty(Utils.EXO_LOCATION).getString()) ;
      if(settingNode.hasProperty(Utils.EXO_TIMEZONE)) calendarSetting.setTimeZone(settingNode.getProperty(Utils.EXO_TIMEZONE).getString())  ;
      if(settingNode.hasProperty(Utils.EXO_IS_SHOW_WORKING_TIME)) {
        calendarSetting.setShowWorkingTime(settingNode.getProperty(Utils.EXO_IS_SHOW_WORKING_TIME).getBoolean());
      }
      if(calendarSetting.isShowWorkingTime()) {
        if(settingNode.hasProperty(Utils.EXO_WORKING_BEGIN)) 
          calendarSetting.setWorkingTimeBegin(settingNode.getProperty(Utils.EXO_WORKING_BEGIN).getString()) ;
        if(settingNode.hasProperty(Utils.EXO_WORKING_END))
          calendarSetting.setWorkingTimeEnd(settingNode.getProperty(Utils.EXO_WORKING_END).getString()) ;
      }
      if(settingNode.hasProperty(Utils.EXO_PRIVATE_CALENDARS)){
        Value[] values = settingNode.getProperty(Utils.EXO_PRIVATE_CALENDARS).getValues() ;
        String[] calendars = new String[values.length] ;
        for(int i = 0; i < values.length; i++) {
          calendars[i] = values[i].getString() ;
        }
        calendarSetting.setFilterPrivateCalendars(calendars) ;
      }
      if(settingNode.hasProperty(Utils.EXO_PUBLIC_CALENDARS)){
        Value[] values = settingNode.getProperty(Utils.EXO_PUBLIC_CALENDARS).getValues() ;
        String[] calendars = new String[values.length] ;
        for(int i = 0; i < values.length; i++) {
          calendars[i] = values[i].getString() ;
        }
        calendarSetting.setFilterPublicCalendars(calendars) ;
      }

      if(settingNode.hasProperty(Utils.EXO_SHARED_CALENDARS)){
        Value[] values = settingNode.getProperty(Utils.EXO_SHARED_CALENDARS).getValues() ;
        String[] calendars = new String[values.length] ;
        for(int i = 0; i < values.length; i++) {
          calendars[i] = values[i].getString() ;
        }
        calendarSetting.setFilterSharedCalendars(calendars) ;
      }
      if(settingNode.hasProperty(Utils.EXO_SHARED_CALENDAR_COLORS)){
        Value[] values = settingNode.getProperty(Utils.EXO_SHARED_CALENDAR_COLORS).getValues() ;
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
  
  private void storeXML(String feedXML, Node rssHome, String rssNodeName, RssData rssData) throws Exception{
    Node rss ;
    if(rssHome.hasNode(rssNodeName)) rss = rssHome.getNode(rssNodeName);
    else rss = rssHome.addNode(rssNodeName, Utils.EXO_RSS_DATA);
    rss.setProperty(Utils.EXO_BASE_URL, rssData.getUrl()) ;
    rss.setProperty(Utils.EXO_TITLE, rssData.getTitle()) ;
    rss.setProperty(Utils.EXO_CONTENT, new ByteArrayInputStream(feedXML.getBytes()));
  }
  
  public int generateCalDav(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData, 
                            CalendarImportExport importExport) throws Exception {
    Node rssHomeNode = getRssHome(username) ;
    Node iCalHome = null ;
    try {
      iCalHome = rssHomeNode.getNode(Utils.CALDAV_NODE) ;
    } catch (Exception e) {
      iCalHome = rssHomeNode.addNode(Utils.CALDAV_NODE, Utils.NT_UNSTRUCTURED) ;
    }
    try {         
      SyndFeed feed = new SyndFeedImpl();      
      feed.setFeedType(rssData.getVersion());      
      feed.setTitle(rssData.getTitle());
      feed.setLink(rssData.getLink());
      feed.setDescription(rssData.getDescription());     
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      SyndEntry entry;
      SyndContent description;
      for(String calendarMap : calendars.keySet()) {
        String calendarId = calendarMap.split(Utils.SPLITTER)[0] ;
        String type = calendarMap.split(Utils.SPLITTER)[1] ;
        OutputStream out = importExport.exportCalendar(username, Arrays.asList(new String[]{calendarId}), type) ;
        if(out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          Node ical = null ;
          Node nodeContent = null ;
          try {
            ical = iCalHome.getNode(calendarMap + Utils.ICS_EXT) ;
            nodeContent = ical.getNode(Utils.JCR_CONTENT);
          } catch (Exception e) {
            ical = iCalHome.addNode(calendarMap + Utils.ICS_EXT, Utils.NT_FILE) ; 
            nodeContent = ical.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
          }
          nodeContent.setProperty(Utils.JCR_LASTMODIFIED, java.util.Calendar.getInstance().getTimeInMillis()) ;
          nodeContent.setProperty(Utils.JCR_MIMETYPE, Utils.MIMETYPE_ICALENDAR);
          nodeContent.setProperty(Utils.JCR_DATA, is);
          if(!iCalHome.isNew()) iCalHome.save() ;
          else iCalHome.getSession().save() ;
          String link = rssData.getLink() + ical.getPath() ;
          Calendar exoCal = calendars.get(calendarMap) ;
          entry = new SyndEntryImpl();
          entry.setTitle(exoCal.getName());                
          entry.setLink(link);     
          description = new SyndContentImpl();
          description.setType(Utils.MIMETYPE_TEXTPLAIN);
          description.setValue(exoCal.getDescription());
          entry.setDescription(description);
          entry.setAuthor(username) ;
          entries.add(entry);
          entry.getEnclosures() ;     
        }                   
      }      
      if(!entries.isEmpty()) {
        feed.setEntries(entries);      
        feed.setEncoding("UTF-8") ;     
        SyndFeedOutput output = new SyndFeedOutput();      
        String feedXML = output.outputString(feed);      
        feedXML = StringUtils.replace(feedXML,"&amp;","&");      
        storeXML(feedXML, rssHomeNode, rssData.getName(), rssData); 
        rssHomeNode.getSession().save() ;
      } else {
        System.out.println("No data to make caldav!");
        return -1 ;
      } 
      
       
    } catch (Exception e) {
      e.printStackTrace();
      return -1 ;
    }     
    return 1 ;
  }
  
  private void removeFeed(String username, String calendarId) throws Exception {
    Node rssHome = getRssHome(username) ;
    NodeIterator iter = rssHome.getNodes() ; 
    while(iter.hasNext()) {
      Node feedNode = iter.nextNode() ;
      if(feedNode.isNodeType(Utils.EXO_RSS_DATA)) {
        FeedData feedData = new FeedData() ;
        feedData.setTitle(feedNode.getProperty("exo:title").getString()) ;
        StringBuffer url = new StringBuffer(feedNode.getProperty(Utils.EXO_BASE_URL).getString()) ;  
        url.append("/").append(PortalContainer.getInstance().getPortalContainerInfo().getContainerName()) ;
        url.append("/").append(feedNode.getSession().getWorkspace().getName()) ;
        url.append("/").append(username)  ;
        url.append("/").append(feedNode.getName())  ;
        feedData.setUrl(url.toString()) ;
       
        URL feedUrl = new URL(feedData.getUrl());
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl)); 
        
        List entries = feed.getEntries();
        List<SyndEntry> listBefore = new ArrayList<SyndEntry>() ;
        listBefore.addAll(entries) ;

        for (int i = 0; i < listBefore.size(); i ++) {
          SyndEntry entry = (SyndEntry)feed.getEntries().get(i);
          String id = entry.getLink().substring(entry.getLink().lastIndexOf("/")+1) ;
          if (id.contains(calendarId)) {
            listBefore.remove(i) ; 
            //if (listBefore.size() == 0) {
            //  feedNode.remove() ;
            //  rssHome.getSession().save() ;
            //  break ;
            //} else {
              feed.setEntries(listBefore) ;
              SyndFeedOutput output = new SyndFeedOutput(); 
              String feedXML = output.outputString(feed);      
              feedXML = StringUtils.replace(feedXML,"&amp;","&");
              
              feedNode.setProperty(Utils.EXO_CONTENT, new ByteArrayInputStream(feedXML.getBytes()));
              feedNode.save() ;
            //}            
          }        
        }       
      }
    }  
  }

  public List<FeedData> getFeeds(String username) throws Exception {
    List<FeedData> feeds = new ArrayList<FeedData>() ;
    Node rssHome = getRssHome(username) ;
    NodeIterator iter = rssHome.getNodes() ;
    while(iter.hasNext()) {
      Node feedNode = iter.nextNode() ;
      if(feedNode.isNodeType(Utils.EXO_RSS_DATA)) {
        FeedData feed = new FeedData() ;
        feed.setTitle(feedNode.getProperty("exo:title").getString()) ;
        StringBuffer url = new StringBuffer(feedNode.getProperty(Utils.EXO_BASE_URL).getString()) ;  
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
  
  public int generateRss(String username, List<String> calendarIds, RssData rssData, 
                         CalendarImportExport importExport) throws Exception {

   // Node sharedNode = getSharedCalendarHome() ;
    Node rssHomeNode = getRssHome(username) ;
    

    Node iCalHome = null ;
    try {
      iCalHome = rssHomeNode.getNode(Utils.RSS_NODE) ;
    } catch (Exception e) {
      iCalHome = rssHomeNode.addNode(Utils.RSS_NODE, Utils.NT_UNSTRUCTURED) ;
    }
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
        OutputStream out = importExport.exportCalendar(username, Arrays.asList(new String[]{calendarId}), "0") ;
        if(out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          try {
            iCalHome.getNode(calendarId + Utils.ICS_EXT).setProperty(Utils.EXO_DATA, is) ;  
          } catch (Exception e) {
            Node ical = iCalHome.addNode(calendarId + Utils.ICS_EXT, Utils.EXO_ICAL_DATA) ;
            ical.setProperty(Utils.EXO_DATA, is) ;
          }
          StringBuffer path = new StringBuffer(Utils.SLASH) ;
          path.append(iCalHome.getName()).append(Utils.SLASH).append(iCalHome.getNode(calendarId + Utils.ICS_EXT).getName());        
          String url = getEntryUrl(portalName, rssHomeNode.getSession().getWorkspace().getName(), 
                                   username, path.toString(), rssData.getUrl()) ;
          Calendar exoCal = getUserCalendar(username, calendarId) ;
          entry = new SyndEntryImpl();
          entry.setTitle(exoCal.getName());                
          entry.setLink(url);        
          entry.setAuthor(username) ;
          description = new SyndContentImpl();
          description.setType(Utils.MIMETYPE_TEXTPLAIN);
          description.setValue(exoCal.getDescription());
          entry.setDescription(description);        
          entries.add(entry);
          entry.getEnclosures() ;     
        }                   
      }
      if(!entries.isEmpty()) {
        feed.setEntries(entries);      
        feed.setEncoding("UTF-8") ;     
        SyndFeedOutput output = new SyndFeedOutput();      
        String feedXML = output.outputString(feed);      
        feedXML = StringUtils.replace(feedXML,"&amp;","&");      
        storeXML(feedXML, rssHomeNode, rssData.getName(), rssData); 
        rssHomeNode.getSession().save() ;
      } else {
        System.out.println("No data to make rss!");
        return -1 ;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return -1 ;
    }  
    return 1 ;
  }

  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData, 
                         CalendarImportExport importExport) throws Exception {
   // Node sharedNode = getSharedCalendarHome() ;
    Node rssHomeNode = getRssHome(username) ;
    

    Node iCalHome = null ;
    try {
      iCalHome = rssHomeNode.getNode(Utils.RSS_NODE) ;
    } catch (Exception e) {
      iCalHome = rssHomeNode.addNode(Utils.RSS_NODE, Utils.NT_UNSTRUCTURED) ;
    }
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
      //List<String> ids = new ArrayList<String>();
      for(String calendarMap : calendars.keySet()) {
        String calendarId = calendarMap.split(Utils.SPLITTER)[0] ;
        String type = calendarMap.split(Utils.SPLITTER)[1] ;
        OutputStream out = importExport.exportCalendar(username, Arrays.asList(new String[]{calendarId}), type) ;
        if(out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          try {
            iCalHome.getNode(calendarMap + Utils.ICS_EXT).setProperty(Utils.EXO_DATA, is) ;  
          } catch (Exception e) {
            Node ical = iCalHome.addNode(calendarMap + Utils.ICS_EXT, Utils.EXO_ICAL_DATA) ;
            ical.setProperty(Utils.EXO_DATA, is) ;
          }
          StringBuffer path = new StringBuffer(Utils.SLASH) ;
          path.append(iCalHome.getName()).append(Utils.SLASH).append(iCalHome.getNode(calendarMap + Utils.ICS_EXT).getName());        
          String url = getEntryUrl(portalName, rssHomeNode.getSession().getWorkspace().getName(), 
                                   username, path.toString(), rssData.getUrl()) ;
          Calendar exoCal = calendars.get(calendarMap) ;
          entry = new SyndEntryImpl();
          entry.setTitle(exoCal.getName());                
          entry.setLink(url);        
          entry.setAuthor(username) ;
          description = new SyndContentImpl();
          description.setType(Utils.MIMETYPE_TEXTPLAIN);
          description.setValue(exoCal.getDescription());
          entry.setDescription(description);        
          entries.add(entry);
          entry.getEnclosures() ;     
        }                   
      }
      if(!entries.isEmpty()) {
        feed.setEntries(entries);      
        feed.setEncoding("UTF-8") ;     
        SyndFeedOutput output = new SyndFeedOutput();      
        String feedXML = output.outputString(feed);      
        feedXML = StringUtils.replace(feedXML,"&amp;","&");      
        storeXML(feedXML, rssHomeNode, rssData.getName(), rssData); 
        rssHomeNode.getSession().save() ;
      } else {
        System.out.println("No data to make rss!");
        return -1 ;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return -1 ;
    }  
    return 1 ;
  }
  public void updateRss(String username, String calendarId, CalendarImportExport imp) throws Exception{
    calendarId = calendarId.substring(0, calendarId.lastIndexOf(".")) ;   
    String id = calendarId.split(Utils.SPLITTER)[0] ;
    String type = calendarId.split(Utils.SPLITTER)[1] ;    
    Node rssHome = getRssHome(username) ;
    if(rssHome.hasNode(Utils.RSS_NODE)) {
      NodeIterator iter = rssHome.getNode(Utils.RSS_NODE).getNodes() ;
      while (iter.hasNext()) {
        Node rssCal = iter.nextNode() ;
        if (rssCal.getPath().contains(calendarId)) {
          OutputStream out = imp.exportCalendar(username, Arrays.asList(new String[]{id}), type) ;
          if(out != null) {
            ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
              rssCal.setProperty(Utils.EXO_DATA, is) ;
              rssCal.save() ;
          } else {
            rssCal.remove() ;
            rssHome.getSession().save() ;
          }
          break ;
        }  
      }
    }
  }
  public void updateRss(String username, String calendarId, CalendarImportExport imp, int number) throws Exception{
    calendarId = calendarId.substring(0, calendarId.lastIndexOf(".")) ;   
    String id = calendarId.split(Utils.SPLITTER)[0] ;
    String type = calendarId.split(Utils.SPLITTER)[1] ;    
    Node rssHome = getRssHome(username) ;
    if(rssHome.hasNode(Utils.RSS_NODE)) {
      NodeIterator iter = rssHome.getNode(Utils.RSS_NODE).getNodes() ;
      while (iter.hasNext()) {
        Node rssCal = iter.nextNode() ;
        if (rssCal.getPath().contains(calendarId)) {
          OutputStream out = imp.exportCalendar(username, Arrays.asList(new String[]{id}), type, number) ;
          if(out != null) {
            ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
              rssCal.setProperty(Utils.EXO_DATA, is) ;
              rssCal.save() ;
          } else {
            rssCal.remove() ;
            rssHome.getSession().save() ;
          }
          break ;
        }  
      }
    }
  }
  
  public void updateCalDav(String username, String calendarId, CalendarImportExport imp) throws Exception {
    calendarId = calendarId.substring(0, calendarId.lastIndexOf(".")) ;
    String id = calendarId.split(Utils.SPLITTER)[0] ;
    String type = calendarId.split(Utils.SPLITTER)[1] ;
    Node rssHome = getRssHome(username) ;
    if(rssHome.hasNode(Utils.CALDAV_NODE)) {
      NodeIterator iter = rssHome.getNode(Utils.CALDAV_NODE).getNodes() ;
      while (iter.hasNext()) {
        Node rssCal = iter.nextNode() ;
        Node nodeContent = rssCal.getNode(Utils.JCR_CONTENT) ;
        OutputStream out = imp.exportCalendar(username, Arrays.asList(new String[]{id}), type) ;
        if(out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          nodeContent.setProperty(Utils.JCR_DATA, is) ;  
        }
      }
      rssHome.getSession().save() ;
    }
  }

  public void updateCalDav(String username, String calendarId, CalendarImportExport imp, int number)throws Exception {
    calendarId = calendarId.substring(0, calendarId.lastIndexOf(".")) ;
    String id = calendarId.split(Utils.SPLITTER)[0] ;
    String type = calendarId.split(Utils.SPLITTER)[1] ;
    Node rssHome = getRssHome(username) ;
    if(rssHome.hasNode(Utils.CALDAV_NODE)) {
      NodeIterator iter = rssHome.getNode(Utils.CALDAV_NODE).getNodes() ;
      while (iter.hasNext()) {
        Node rssCal = iter.nextNode() ;
        Node nodeContent = rssCal.getNode(Utils.JCR_CONTENT) ;
        OutputStream out = imp.exportCalendar(username, Arrays.asList(new String[]{id}), type, number) ;
        if(out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          nodeContent.setProperty(Utils.JCR_DATA, is) ;  
        }
      }
      rssHome.getSession().save() ;
    }
  }
  public int generateCalDav(String username, List<String> calendarIds, RssData rssData, 
                            CalendarImportExport importExport) throws Exception {
    Node rssHomeNode = getRssHome(username) ;
    Node iCalHome = null ;
    try {
      iCalHome = rssHomeNode.getNode(Utils.CALDAV_NODE) ;
    } catch (Exception e) {
      iCalHome = rssHomeNode.addNode(Utils.CALDAV_NODE, Utils.NT_UNSTRUCTURED) ;
    }
    try {         
      SyndFeed feed = new SyndFeedImpl();      
      feed.setFeedType(rssData.getVersion());      
      feed.setTitle(rssData.getTitle());
      feed.setLink(rssData.getLink());
      feed.setDescription(rssData.getDescription());     
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      SyndEntry entry;
      SyndContent description;
      for(String calendarId : calendarIds) {        
        OutputStream out = importExport.exportCalendar(username, Arrays.asList(new String[]{calendarId}), "0") ;
        if(out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          Node ical = null ;
          Node nodeContent = null ;
          try {
            ical = iCalHome.getNode(calendarId + Utils.ICS_EXT) ;
            nodeContent = ical.getNode(Utils.JCR_CONTENT);
          } catch (Exception e) {
            ical = iCalHome.addNode(calendarId + Utils.ICS_EXT, Utils.NT_FILE) ; 
            nodeContent = ical.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
          }
          nodeContent.setProperty(Utils.JCR_LASTMODIFIED, java.util.Calendar.getInstance().getTimeInMillis()) ;
          nodeContent.setProperty(Utils.JCR_MIMETYPE, Utils.MIMETYPE_ICALENDAR);
          nodeContent.setProperty(Utils.JCR_DATA, is);
          if(!iCalHome.isNew()) iCalHome.save() ;
          else iCalHome.getSession().save() ;
          String link = rssData.getLink() + ical.getPath() ;
          Calendar exoCal = getUserCalendar(username, calendarId) ;
          entry = new SyndEntryImpl();
          entry.setTitle(exoCal.getName());                
          entry.setLink(link);     
          description = new SyndContentImpl();
          description.setType(Utils.MIMETYPE_TEXTPLAIN);
          description.setValue(exoCal.getDescription());
          entry.setDescription(description);
          entry.setAuthor(username) ;
          entries.add(entry);
          entry.getEnclosures() ;     
        }                   
      }      
      if(!entries.isEmpty()) {
        feed.setEntries(entries);      
        feed.setEncoding("UTF-8") ;     
        SyndFeedOutput output = new SyndFeedOutput();      
        String feedXML = output.outputString(feed);      
        feedXML = StringUtils.replace(feedXML,"&amp;","&");      
        storeXML(feedXML, rssHomeNode, rssData.getName(), rssData); 
        rssHomeNode.getSession().save() ;
      } else {
        System.out.println("No data to make caldav!");
        return -1 ;
      } 
      
       
    } catch (Exception e) {
      e.printStackTrace();
      return -1 ;
    }     
    return 1 ;
  }

  

 

  private String getEntryUrl(String portalName, String wsName, String username, String path, String baseUrl) throws Exception{
    StringBuilder url = new StringBuilder(baseUrl) ;
    url.append(Utils.SLASH).append(portalName)
    .append(Utils.SLASH).append(wsName) ;
    if(username != null) url.append(Utils.SLASH).append(username) ;
    url.append(path) ;
    return url.toString();
  }

  public EventPageList searchEvent(String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>(); 
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    try {
      if(eventQuery.getCalendarId() == null) {
        events.addAll(getUserEvents(username, eventQuery));
        if(publicCalendarIds != null && publicCalendarIds.length > 0) {
          eventQuery.setCalendarId(publicCalendarIds);
          events.addAll(getPublicEvents(eventQuery));
          eventQuery.setCalendarId(null) ;
        }
        events.addAll(getSharedEvents(username, eventQuery));
      } else {
        String calFullId = eventQuery.getCalendarId()[0] ;
        if(calFullId.split(Utils.COLON).length > 0) {
          String[] calId = new String[]{calFullId.split(Utils.COLON)[1]} ;
          int type = Integer.parseInt(calFullId.split(Utils.COLON)[0]) ;
          eventQuery.setCalendarId(calId) ;
          switch (type) {
          case Calendar.TYPE_PRIVATE:
            events.addAll(getUserEvents(username, eventQuery));
            return new EventPageList(events, 10);    
          case Calendar.TYPE_SHARED:
            events.addAll(getSharedEvents(username, eventQuery));
            return new EventPageList(events, 10);    
          case Calendar.TYPE_PUBLIC:
            events.addAll(getPublicEvents(eventQuery));
            return new EventPageList(events, 10);    
          default:
            break;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      systemSession.close() ;
    }
    return new EventPageList(events, 10);    
  }

  public Map<Integer, String > searchHightLightEvent(String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception {
    Map<Integer, String > mapData = new HashMap<Integer, String>() ;
    Query query ;
    QueryManager qm ;
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    try {
      CalendarSetting calSetting = getCalendarSetting(username)  ;
      // private events
      if(username != null && username.length() > 0) {
        Node calendarHome = getUserCalendarHome(username) ;
        eventQuery.setCalendarPath(calendarHome.getPath()) ;
        qm = calendarHome.getSession().getWorkspace().getQueryManager() ;
        query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
        NodeIterator it = query.execute().getNodes();   
        mapData = updateMap(mapData, it, eventQuery.getFromDate(), eventQuery.getToDate(), calSetting.getFilterPrivateCalendars()) ;
      }
      // shared events
      if(getSharedCalendarHome(systemSession).hasNode(username)) {
        PropertyIterator iter = getSharedCalendarHome(systemSession).getNode(username).getReferences() ;
        while(iter.hasNext()) {
          try{
            Node calendar = iter.nextProperty().getParent() ;
            eventQuery.setCalendarPath(calendar.getPath()) ;
            qm = calendar.getSession().getWorkspace().getQueryManager() ;
            query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
            NodeIterator it = query.execute().getNodes();
            mapData = updateMap(mapData, it, eventQuery.getFromDate(), eventQuery.getToDate(), calSetting.getFilterSharedCalendars()) ;
          }catch(Exception e){
            e.printStackTrace() ;
          }
        }
      }  
      // public events
      Node publicCalHome = getPublicCalendarHome(systemSession) ;
      eventQuery.setCalendarPath(publicCalHome.getPath()) ;
      qm = publicCalHome.getSession().getWorkspace().getQueryManager() ;
      eventQuery.setCalendarId(publicCalendarIds) ;
      query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
      NodeIterator it = query.execute().getNodes();
      mapData = updateMap(mapData, it, eventQuery.getFromDate(), eventQuery.getToDate(), calSetting.getFilterPublicCalendars()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      systemSession.close() ;
    }
    return mapData ;    
  }


  private Map<Integer, String> updateMap(Map<Integer, String> data, NodeIterator it, java.util.Calendar fromDate, java.util.Calendar toDate, String[] filterCalIds) throws Exception {
    int fromDayOfYear = fromDate.get(java.util.Calendar.DAY_OF_YEAR) ;
    int daysOfyer = fromDate.getMaximum(java.util.Calendar.DAY_OF_YEAR) ;
    int toDayOfYear = toDate.get(java.util.Calendar.DAY_OF_YEAR) ;
    if(toDate.get(java.util.Calendar.DAY_OF_YEAR ) > fromDate.get(java.util.Calendar.DAY_OF_YEAR)) {
      toDayOfYear = toDayOfYear + daysOfyer ;
    }
    boolean isVictory = false ;
    while(it.hasNext() && !isVictory) {
      Node eventNode = it.nextNode() ;
      if(filterCalIds == null || !Arrays.asList(filterCalIds).contains(eventNode.getProperty(Utils.EXO_CALENDAR_ID).getString())) {
        java.util.Calendar eventFormDate = eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate() ;
        java.util.Calendar eventToDate = eventNode.getProperty(Utils.EXO_TO_DATE_TIME).getDate() ;
        int eventFromDayOfYear = eventFormDate.get(java.util.Calendar.DAY_OF_YEAR) ;
        int eventToDayOfYear = eventToDate.get(java.util.Calendar.DAY_OF_YEAR) ;
        Integer begin = -1 ;
        Integer end = -1 ;
        if(fromDayOfYear >= eventFromDayOfYear) {
          begin = fromDayOfYear ;
          if(toDayOfYear <= eventToDayOfYear) {
            end = toDayOfYear ;
            isVictory = true ;
          } else {
            end = eventToDayOfYear ;
          }
        } else {
          begin = eventFromDayOfYear ;
          if(toDayOfYear <= eventToDayOfYear) {
            end = toDayOfYear ;
          } else {
            end = eventToDayOfYear ;
          }
        }
        if(begin > 0 && end > 0) for(Integer i = begin; i <= end; i++) data.put(i, VALUE);
      }
    }
    return data ;
  }

  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome() ;
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId) ;
    Value[] values = {};
    if (calendarNode.isNodeType(Utils.EXO_SHARED_MIXIN)) {     
      values = calendarNode.getProperty(Utils.EXO_SHARED_ID).getValues();
    } else {
      calendarNode.addMixin(Utils.EXO_SHARED_MIXIN);     
    }
    Session systemSession = sharedCalendarHome.getSession() ;
    Node userNode ;
    List<Value> valueList = new ArrayList<Value>() ;
    for(String user : receiverUsers) {
      CalendarSetting calSetting = getCalendarSetting(user) ;
      if(calSetting == null) calSetting = new CalendarSetting() ;
      Map<String, String> map = new HashMap<String, String> () ;
      for(String key : calSetting.getSharedCalendarsColors()) {
        map.put(key.split(":")[0], key.split(":")[1]) ;
      }
      if(map.get(calendarNode.getProperty(Utils.EXO_ID).getString()) == null)
        map.put(calendarNode.getProperty(Utils.EXO_ID).getString(), calendarNode.getProperty("exo:calendarColor").getString()) ;
      List<String> calColors = new ArrayList<String>() ;
      for(String key : map.keySet()) {
        calColors.add(key + ":" +map.get(key)) ;
      }
      //saveCalendarSetting(sProvider, username, calSetting) ;
      calSetting.setSharedCalendarsColors(calColors.toArray(new String[calColors.size()])) ;
      /*List<String> sharedCaeldnars = new ArrayList<String>() ;
      if(calSetting.getDefaultSharedCalendars() != null) {
        sharedCaeldnars.addAll(Arrays.asList(calSetting.getDefaultSharedCalendars())) ;
      }
      if(! sharedCaeldnars.contains(calendarId)) sharedCaeldnars.add(calendarId) ;
      calSetting.setDefaultSharedCalendars(sharedCaeldnars.toArray(new String[sharedCaeldnars.size()])) ;*/
      saveCalendarSetting(user, calSetting) ;
      try {
        userNode = sharedCalendarHome.getNode(user) ;
      } catch (Exception e) {
        userNode = sharedCalendarHome.addNode(user, Utils.NT_UNSTRUCTURED) ;
        if(userNode.canAddMixin(Utils.MIX_REFERENCEABLE)) {
          userNode.addMixin(Utils.MIX_REFERENCEABLE) ;
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
      calendarNode.setProperty(Utils.EXO_SHARED_ID, valueList.toArray( new Value[valueList.size()]));
      calendarNode.save() ;
      sharedCalendarHome.getSession().save() ;
      calendarNode.getSession().save();
      systemSession.logout() ;
    }
  }

  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception {
    if(getSharedCalendarHome().hasNode(username)) {
      Node sharedNode = getSharedCalendarHome().getNode(username) ;
      List<Calendar> calendars = new ArrayList<Calendar>() ;
      PropertyIterator iter = sharedNode.getReferences() ;
      String[] defaultFilterCalendars =  null  ;
      if(getCalendarSetting(username) != null) {
        defaultFilterCalendars = getCalendarSetting(username).getFilterSharedCalendars() ;
      }
      while(iter.hasNext()) {
        try{
          Calendar cal = getCalendar(defaultFilterCalendars, null, iter.nextProperty().getParent(), isShowAll) ;
          if(cal != null) {
            calendars.add(cal) ;
          }
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
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome() ;
    if(sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username) ;
      String uuid = userNode.getProperty("jcr:uuid").getString() ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendarNode ;
      List<Value> newValues = new ArrayList<Value>() ;
      while(iter.hasNext()) {
        calendarNode = iter.nextProperty().getParent() ;
        if(calendarNode.getProperty(Utils.EXO_ID).getString().equals(calendar.getId())) {
          Value[] values = calendarNode.getProperty(Utils.EXO_SHARED_ID).getValues() ;
          for(Value value : values){
            if(!value.getString().equals(uuid)) {
              newValues.add(value) ;
            }
          }
          calendarNode.setProperty(Utils.EXO_NAME, calendar.getName()) ;
          calendarNode.setProperty(Utils.EXO_DESCRIPTION, calendar.getDescription()) ;
          calendarNode.setProperty(Utils.EXO_CATEGORY_ID, calendar.getCategoryId()) ;
          calendarNode.setProperty(Utils.EXO_VIEW_PERMISSIONS, calendar.getViewPermission()) ;
          calendarNode.setProperty(Utils.EXO_EDIT_PERMISSIONS, calendar.getEditPermission()) ;
          calendarNode.setProperty(Utils.EXO_GROUPS, calendar.getGroups()) ;
          calendarNode.setProperty(Utils.EXO_LOCALE, calendar.getLocale()) ;
          calendarNode.setProperty(Utils.EXO_TIMEZONE, calendar.getTimeZone()) ;
          //calendarNode.setProperty("exo:calendarColor", calendar.getCalendarColor()) ;
          //calendarNode.setProperty(Utils.EXO_SHARED_COLOR, calendar.getCalendarColor()) ;
          CalendarSetting usCalSetting = getCalendarSetting(username) ;
          Map<String, String> map = new HashMap<String, String> () ;
          for(String key : usCalSetting.getSharedCalendarsColors()) {
            map.put(key.split(Utils.COLON)[0], key.split(Utils.COLON)[1]) ;
          }
          map.put(calendar.getId(), calendar.getCalendarColor()) ;
          List<String> calColors = new ArrayList<String>() ;
          for(String key : map.keySet()) {
            calColors.add(key + Utils.COLON +map.get(key)) ;
          }
          calColors.add(calendar.getId()+Utils.COLON+calendar.getCalendarColor());
          usCalSetting.setSharedCalendarsColors(calColors.toArray(new String[calColors.size()])) ;
          saveCalendarSetting(username, usCalSetting) ;
          calendarNode.save() ;
          break ;
        }
      }
    }      
  }

  public List<CalendarEvent> getSharedEvents(String username, EventQuery eventQuery) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    if(getSharedCalendarHome().hasNode(username)) {
      PropertyIterator iter = getSharedCalendarHome().getNode(username).getReferences() ;
      CalendarEvent calEvent ;
      while(iter.hasNext()) {
        try{
          Node calendar = iter.nextProperty().getParent() ;
          eventQuery.setCalendarPath(calendar.getPath()) ;
          QueryManager qm = calendar.getSession().getWorkspace().getQueryManager() ;
          Query query = qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType()) ;
          NodeIterator it = query.execute().getNodes();
          while(it.hasNext()){
            calEvent = getEvent(it.nextNode()) ;
            //if(eventQuery.getFilterCalendarIds()== null || !Arrays.asList(eventQuery.getFilterCalendarIds()).contains(calEvent.getCalendarId())) {
            calEvent.setCalType("1") ;
            events.add(calEvent) ;
            if( eventQuery.getLimitedItems() == it.getPosition() ) break ;
            //}
          }
        }catch (Exception e) {
          e.printStackTrace() ;
        }
      }
    }
    return events ;
  }

  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    if(getSharedCalendarHome().hasNode(username)) {
      PropertyIterator iter = getSharedCalendarHome().getNode(username).getReferences() ;
      while(iter.hasNext()) {
        try{
          Node calendar = iter.nextProperty().getParent() ;
          
          System.out.println("\n\n id :" + calendarIds.get(0) + "\n\n");
          if(calendarIds.contains(calendar.getProperty(Utils.EXO_ID).getString())) {
            NodeIterator it = calendar.getNodes();
            while(it.hasNext()){
              events.add(getEvent(it.nextNode())) ;
            }
          }
        }catch (Exception e) {
          e.printStackTrace() ;
        }
      }
    }
    return events ;
  }



  public void removeSharedCalendar(String username, String calendarId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome() ;
    // TODO use try catch
    if(sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username) ;
      String uuid = userNode.getProperty("jcr:uuid").getString() ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendar ;
      CalendarSetting calSetting = getCalendarSetting(username) ;
      Map<String, String> map = new HashMap<String, String>() ;
      for(String key : calSetting.getSharedCalendarsColors()) {
        map.put(key.split(":")[0], key.split(":")[1]) ;
      }
      List<Value> newValues = new ArrayList<Value>() ;
      while(iter.hasNext()) {
        calendar = iter.nextProperty().getParent() ;
        if(calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId)) {
          map.remove(calendarId) ;
          Value[] values = calendar.getProperty(Utils.EXO_SHARED_ID).getValues() ;
          for(Value value : values){
            if(!value.getString().equals(uuid)) {
              newValues.add(value) ;
            }
          }
          List<String> calColors = new ArrayList<String>() ;
          for(String key : map.keySet()) {
            calColors.add(key + ":" +map.get(key)) ;
          }
          calSetting.setSharedCalendarsColors(calColors.toArray(new String[calColors.size()])) ;
          saveCalendarSetting(username, calSetting) ;
          calendar.setProperty(Utils.EXO_SHARED_ID, newValues.toArray(new Value[newValues.size()])) ;
          List<String> viewPerms = new ArrayList<String>() ;
          if(calendar.hasProperty(Utils.EXO_VIEW_PERMISSIONS)) {
            Value[] viewValues = calendar.getProperty(Utils.EXO_VIEW_PERMISSIONS).getValues() ;
            for(Value v : viewValues) {
              if(v.getString() != null && !v.getString().equals(username)) viewPerms.add(v.getString()) ;
            }
          }
          List<String> editPerms = new ArrayList<String>() ;
          if(calendar.hasProperty(Utils.EXO_EDIT_PERMISSIONS)) {
            Value[] editValues = calendar.getProperty(Utils.EXO_EDIT_PERMISSIONS).getValues() ;
            for(Value v : editValues) {
              if(v.getString() != null && !v.getString().equals(username)) editPerms.add(v.getString()) ;
            }
          }   
          calendar.setProperty(Utils.EXO_VIEW_PERMISSIONS, viewPerms.toArray(new String[viewPerms.size()])) ;
          calendar.setProperty(Utils.EXO_EDIT_PERMISSIONS, editPerms.toArray(new String[editPerms.size()])) ;
          //calendar.save() ;
          calendar.getSession().save() ;
          calendar.refresh(true) ;
          break ;
        }
      }
      try {
        removeFeed(username, calendarId) ;        
      } catch (Exception e) {
        e.printStackTrace() ;
      }
    }
  }

  public void saveEventToSharedCalendar(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  {
    Node sharedCalendarHome = getSharedCalendarHome() ;
    //Node eventFolder = getEventFolder(SessionProvider.createSystemProvider(), event.getFromDateTime()) ;
    if(sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendar ;      
      while(iter.hasNext()) {
        calendar = iter.nextProperty().getParent() ;
        if(calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId)) {
          Node reminderFolder = getReminderFolder(event.getFromDateTime()) ;
          saveEvent(calendar, event, reminderFolder, isNew) ;
          calendar.save() ;
          break ;
        }
      }      
    }
  } 

  public List<CalendarEvent> getEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    List<String> filterList = new ArrayList<String>() ;
    CalendarSetting calSetting = getCalendarSetting(username) ;
    filterList.addAll(Arrays.asList(calSetting.getFilterPrivateCalendars())) ;
    filterList.addAll(Arrays.asList(calSetting.getFilterPublicCalendars())) ;
    filterList.addAll(Arrays.asList(calSetting.getFilterSharedCalendars())) ;
    eventQuery.setFilterCalendarIds(filterList.toArray(new String[]{})) ;
    events.addAll(getUserEvents(username, eventQuery)) ;
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    try {
      events.addAll(getSharedEvents(username, eventQuery)) ;
      if(publicCalendarIds != null && publicCalendarIds.length > 0) { 
        
        // add to fix bug CS-2728
        //String[] calendarBefore = eventQuery.getCalendarId() ;
        
        eventQuery.setCalendarId(publicCalendarIds) ;
        events.addAll(getPublicEvents(eventQuery)) ;
        //eventQuery.setCalendarId(calendarBefore) ;
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      systemSession.close() ;
    }
    return events ;
  }


  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception {
    Node eventFolder = getEventFolder(eventQuery.getFromDate().getTime()) ;
    Map<String, String> participantMap = new HashMap<String, String>() ;
    eventQuery.setCalendarPath(eventFolder.getPath()) ;
    eventQuery.setOrderBy(new String[]{Utils.EXO_FROM_DATE_TIME}) ;
    QueryManager qm = eventFolder.getSession().getWorkspace().getQueryManager();
    String[] pars = eventQuery.getParticipants() ;
    Query query ;
    Node event ;
    String from ;
    String to ;
    for(String par : pars) {
      eventQuery.setParticipants(new String[]{par}) ;
      query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      StringBuilder timeValues = new StringBuilder() ;
      while(it.hasNext()) {
        event = it.nextNode() ;
        if(event.hasProperty(Utils.EXO_EVENT_STATE) && !CalendarEvent.ST_AVAILABLE.equals(event.getProperty(Utils.EXO_EVENT_STATE).getValue().getString()))
        {
          java.util.Calendar fromCal = event.getProperty(Utils.EXO_FROM_DATE_TIME).getDate() ;
          java.util.Calendar toCal = event.getProperty(Utils.EXO_TO_DATE_TIME).getDate() ;
          if(fromCal.getTimeInMillis() < eventQuery.getFromDate().getTimeInMillis())
            from = String.valueOf(eventQuery.getFromDate().getTimeInMillis()) ;
          else 
            from = String.valueOf(fromCal.getTimeInMillis()) ;
          if(toCal.getTimeInMillis() > eventQuery.getToDate().getTimeInMillis()){
            GregorianCalendar cal = new GregorianCalendar() ;
            cal.setTimeInMillis(eventQuery.getToDate().getTimeInMillis() - 1000) ;
            to = String.valueOf(cal.getTimeInMillis()) ;
          } else to = String.valueOf(toCal.getTimeInMillis()) ;

          if(timeValues != null && timeValues.length() > 0) timeValues.append(",") ;
          timeValues.append(from).append(",").append(to) ;
          participantMap.put(par, timeValues.toString()) ;
        }
      }    
    }
    return participantMap ;
  }

  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome() ;
    if(sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendar ;
      while(iter.hasNext()) {
        calendar = iter.nextProperty().getParent() ;
        if(calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId)) {
          if(calendar.hasNode(eventId)) {
            Node event = calendar.getNode(eventId) ;
            Node eventFolder = getEventFolder(event.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()) ;
            syncRemoveEvent(eventFolder, eventId) ;
            removeReminder(event) ;
            event.remove() ;
          }
          calendar.save() ;
          calendar.refresh(true) ;
          break ;
        }
      }      
    }
  }

  public void moveEvent(String formCalendar, String toCalendar, String fromType, String toType, List<CalendarEvent> calEvents, String username) throws Exception {
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    try {
      switch (Integer.parseInt(fromType)) {
      case  Calendar.TYPE_PRIVATE :  
        if(getUserCalendarHome(username).hasNode(formCalendar)) {
          switch (Integer.parseInt(toType)) {
          case Calendar.TYPE_PRIVATE:
            //move events in side private calendars
            if(getUserCalendarHome(username).hasNode(toCalendar)){
              for(CalendarEvent calEvent : calEvents) {
                if(!formCalendar.equals(toCalendar)) {
                  removeUserEvent(username, formCalendar, calEvent.getId()) ;
                  calEvent.setCalendarId(toCalendar) ;
                  saveUserEvent(username, toCalendar, calEvent, getUserCalendarHome(username).getNode(toCalendar).hasNode(calEvent.getId())) ;
                } else {
                  saveUserEvent(username, toCalendar, calEvent,  false) ;
                }
              }
            }
            break;
          case Calendar.TYPE_SHARED:
            //move events form private to shared calendar
            if(getSharedCalendarHome().hasNode(username)){
              for(CalendarEvent calEvent : calEvents) {
                removeUserEvent(username, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                saveEventToSharedCalendar(username, toCalendar, calEvent, getSharedCalendarHome(systemSession).getNode(username).hasNode(calEvent.getId()));
              }
            }
            break;
          case Calendar.TYPE_PUBLIC:
            //move events form private to public calendar
            if(getPublicCalendarHome().hasNode(toCalendar)){
              for(CalendarEvent calEvent : calEvents) {
                removeUserEvent(username, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                savePublicEvent(toCalendar, calEvent, getPublicCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId())) ;
              }
            }
            break;
          default:
            break;
          }
        }
        break;
      case Calendar.TYPE_SHARED:
        if(getSharedCalendarHome(systemSession).hasNode(username)) {
          switch (Integer.parseInt(toType)) {
          case Calendar.TYPE_PRIVATE:
            //move events form share to private calendar
            if(getUserCalendarHome(username).hasNode(toCalendar)) {
              for(CalendarEvent calEvent : calEvents) {
                removeSharedEvent(username, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                saveUserEvent(username, toCalendar, calEvent, getUserCalendarHome(username).getNode(toCalendar).hasNode(calEvent.getId())) ;
              }
            }
            break;
          case Calendar.TYPE_SHARED:
            //   move events in side shared calendars
            if(getSharedCalendarHome().hasNode(username)){
              for(CalendarEvent calEvent : calEvents) {
                if(!formCalendar.equals(toCalendar)) {
                  removeSharedEvent(username, formCalendar, calEvent.getId()) ;
                  calEvent.setCalendarId(toCalendar) ;
                  saveEventToSharedCalendar(username, toCalendar, calEvent, getSharedCalendarHome().getNode(username).hasNode(calEvent.getId()));
                } else {
                  saveEventToSharedCalendar(username, toCalendar, calEvent, false);
                }
              }
            }
            break;
          case Calendar.TYPE_PUBLIC:
            //move events form share to public calendar
            if(getPublicCalendarHome(systemSession).hasNode(toCalendar)) {
              for(CalendarEvent calEvent : calEvents) {
                removeSharedEvent(username, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                savePublicEvent(toCalendar, calEvent, getPublicCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId())) ;
              }
            }
            break;
          default:
            break;
          }
        }
        break;
      case Calendar.TYPE_PUBLIC:
        if(getPublicCalendarHome().hasNode(formCalendar)) {
          switch (Integer.parseInt(toType)) {
          case Calendar.TYPE_PRIVATE:
            //move events from public to private calendar
            if(getUserCalendarHome(username).hasNode(toCalendar)) {
              for(CalendarEvent calEvent : calEvents) {
                removePublicEvent(formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                saveUserEvent(username, toCalendar, calEvent, getUserCalendarHome(username).getNode(toCalendar).hasNode(calEvent.getId())) ;
              }
            }
            break;
          case Calendar.TYPE_SHARED:
            //move events from public to shared calendar
            if(getSharedCalendarHome().hasNode(username)){
              for(CalendarEvent calEvent : calEvents) {
                removePublicEvent(formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                saveEventToSharedCalendar(username, toCalendar, calEvent, true);
              }
            }
            break;
          case Calendar.TYPE_PUBLIC:
            //move events in side public calendars
            if(getPublicCalendarHome(systemSession).hasNode(toCalendar)){
              for(CalendarEvent calEvent : calEvents) {
                if(!formCalendar.equals(toCalendar)) {
                  removePublicEvent(formCalendar, calEvent.getId()) ;
                  calEvent.setCalendarId(toCalendar) ;
                  savePublicEvent(toCalendar, calEvent, getPublicCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId())) ;
                } else {
                  savePublicEvent(toCalendar, calEvent,  false) ;
                }
              }
            }
            break;
          default:
            break;
          }
        }
        break;
      default:
        break;
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      systemSession.close() ;
    }
  }

  public void confirmInvitation(String fromUserId, String toUserId,int calType, String calendarId, String eventId, int answer) throws Exception{
    SessionProvider session = SessionProvider.createSystemProvider() ;
    try {
      Map<String, String> pars = new HashMap<String, String>() ;
      CalendarEvent event = null ;
      if( Calendar.TYPE_PRIVATE == calType) {
        event = getUserEvent(fromUserId, calendarId, eventId) ;
      } else  if(Calendar.TYPE_SHARED == calType)  {
        List<String> calendarIds = new ArrayList<String>() ;
        calendarIds.add(calendarId) ;
        for(CalendarEvent calEvent : getSharedEventByCalendars(fromUserId, calendarIds)) {
          if(calEvent.getId().equals(eventId)) {
            event = calEvent ;
            break ;
          }
        }
      } else  if(Calendar.TYPE_PUBLIC == calType)  {
        event = getGroupEvent(calendarId, eventId) ;
      }
      if(event != null) {
        if(event.getParticipant() != null) {
          for(String id : event.getParticipant()) {
            pars.put(id, id) ;
          }
        } 
        for(String s : toUserId.split(",")){
          if( Utils.DENY == answer) {

            pars.remove(s) ;
          } if (Utils.ACCEPT == answer || Utils.NOTSURE == answer) {
            pars.put(s, s) ;
          }
        }
        //TODO this make duplicate
        event.setParticipant(pars.values().toArray(new String[pars.values().size()]));
        if( Calendar.TYPE_PRIVATE == calType) {
          saveUserEvent(fromUserId, calendarId, event, false) ;
        } else  if(Calendar.TYPE_SHARED == calType)  {
          saveEventToSharedCalendar(fromUserId, calendarId, event, false) ;
        } else  if(Calendar.TYPE_PUBLIC == calType)  {
          savePublicEvent(calendarId, event, false) ;
        }
      }
    } catch (Exception e) {
      System.out.println("\n\n confirmInvitation " + e.getClass().toString()) ;
      //e.printStackTrace() ;
    } finally {
      session.close() ;
    }
  }

  public void confirmInvitation(String fromUserId, String confirmingEmail, String confirmingUser,int calType, String calendarId, String eventId, int answer) throws Exception{
    SessionProvider session = SessionProvider.createSystemProvider() ;
    try {
      Map<String, String> pars = new HashMap<String, String>() ;
      CalendarEvent event = null ;
      if( Calendar.TYPE_PRIVATE == calType) {
        event = getUserEvent(fromUserId, calendarId, eventId) ;
      } else  if(Calendar.TYPE_SHARED == calType)  {
        List<String> calendarIds = new ArrayList<String>() ;
        calendarIds.add(calendarId) ;
        for(CalendarEvent calEvent : getSharedEventByCalendars(fromUserId, calendarIds)) {
          if(calEvent.getId().equals(eventId)) {
            event = calEvent ;
            break ;
          }
        }
      } else  if(Calendar.TYPE_PUBLIC == calType)  {
        event = getGroupEvent(calendarId, eventId) ;
      }
      if(event != null) {
        if(event.getParticipantStatus() != null) {
          for(String parStatus : event.getParticipantStatus()) {
            String[] entry = parStatus.split(":");
            if(entry.length>1)
              pars.put(entry[0], entry[1]);
            else pars.put(entry[0], Utils.STATUS_EMPTY);
          }
        }
        String status = Utils.STATUS_EMPTY;
        switch (answer){
        case Utils.DENY:
          status = Utils.STATUS_NO;
          break;
        case Utils.ACCEPT:
          status = Utils.STATUS_YES;
          break;
        case Utils.NOTSURE:
          status = Utils.STATUS_PENDING;
          break;
        default :
          break;
        }
        
        if(pars.containsKey(confirmingUser)){
          pars.remove(confirmingUser);
          pars.put(confirmingUser, status);
        }
        if(pars.containsKey(confirmingEmail)){
          pars.remove(confirmingEmail);
          pars.put(confirmingEmail, status);
        }
        //TODO this make duplicate
        Map<String, String> participant = new HashMap<String, String>() ;
        for (Entry<String, String> par : pars.entrySet()) {
          participant.put(par.getKey()+":"+par.getValue(),"") ;
        }
        event.setParticipantStatus(participant.keySet().toArray(new String[participant.keySet().size()]));
        if( Calendar.TYPE_PRIVATE == calType) {
          saveUserEvent(fromUserId, calendarId, event, false) ;
        } else  if(Calendar.TYPE_SHARED == calType)  {
          saveEventToSharedCalendar(fromUserId, calendarId, event, false) ;
        } else  if(Calendar.TYPE_PUBLIC == calType)  {
          savePublicEvent(calendarId, event, false) ;
        }
      }

    } catch (Exception e) {
      System.out.println("\n\n confirmInvitation " + e.getClass().toString());
      throw new  Exception(e.getClass().toString(),e.fillInStackTrace());
      //e.printStackTrace() ;
    } finally {
      session.close() ;
    }
  }
  
  public int getTypeOfCalendar(String userName, String calendarId){
    try {
      Node calendarNode = getUserCalendarHome(userName).getNode(calendarId);
      return Utils.PRIVATE_TYPE;
    }catch(Exception e){
    }
    try {
      Node calendarNode = getPublicCalendarHome().getNode(calendarId) ;
      return Utils.PUBLIC_TYPE;
    }catch (Exception e){
    }
    try {
      Node sharedCalendarHome = getSharedCalendarHome() ;
      if(sharedCalendarHome.hasNode(userName)) {
        Node userNode = sharedCalendarHome.getNode(userName) ;
        PropertyIterator iter = userNode.getReferences() ;
        Node calendar ;
        while(iter.hasNext()) {
          calendar = iter.nextProperty().getParent() ;
          if(calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId))
            return Utils.SHARED_TYPE;
        }
      }
    }catch(Exception e){
    }
    return Utils.INVALID_TYPE;
  }
  
  /**
   * Create a session provider for current context. The method first try to get a normal session provider, 
   * then attempts to create a system provider if the first one was not available.
   * @return a SessionProvider initialized by current SessionProviderService
   * @see SessionProviderService#getSessionProvider(null)
   */
  private SessionProvider createSessionProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider provider = service.getSessionProvider(null);
    if (provider == null) {
      log.info("No user session provider was available, trying to use a system session provider");
      provider = service.getSystemSessionProvider(null);
    }
    return provider;
  }

  private SessionProvider createUserProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSessionProvider(null) ;    
  }  

  private SessionProvider createSystemProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSystemSessionProvider(null) ;    
  }


  /**
   * Safely closes JCR session provider. Call this method in finally to clean any provider initialized by createSessionProvider()
   * @param sessionProvider the sessionProvider to close
   * @see SessionProvider#close();
   */
  private void closeSessionProvider(SessionProvider sessionProvider) {
    if (sessionProvider != null) {
      sessionProvider.close();
    }
  }



}
