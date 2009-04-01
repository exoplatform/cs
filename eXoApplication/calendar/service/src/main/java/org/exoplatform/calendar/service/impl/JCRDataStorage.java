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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ; 
  }  

  public Node getPublicCalendarServiceHome(SessionProvider sProvider) throws Exception {
    Node publicApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider)  ;
    try {
      return publicApp.getNode(Utils.CALENDAR_APP) ;
    } catch (Exception e) {
      Node calendarApp = publicApp.addNode(Utils.CALENDAR_APP, Utils.NT_UNSTRUCTURED) ;
      publicApp.getSession().save() ;
      return calendarApp ;
    }
  }

  private Node getSharedCalendarHome(SessionProvider sProvider) throws Exception {
    Node calendarServiceHome = getPublicCalendarServiceHome(sProvider) ;
    try {
      return calendarServiceHome.getNode(SHARED_CALENDAR) ;
    } catch (Exception e) {
      Node sharedCal = calendarServiceHome.addNode(SHARED_CALENDAR, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ; 
      return sharedCal ; 
    }
  }

  private Node getPublicRoot(SessionProvider sysProvider) throws Exception {
    // sysProvider = SessionProvider.createSystemProvider() ;
    return nodeHierarchyCreator_.getPublicApplicationNode(sysProvider) ;
  }

  private Node getUserCalendarServiceHome(SessionProvider sProvider, String username) throws Exception {
    Node userApp = nodeHierarchyCreator_.getUserApplicationNode(sProvider, username)  ;
    Node calendarRoot ; 
    try {
      return userApp.getNode(Utils.CALENDAR_APP) ;
    } catch (Exception e) {
      calendarRoot = userApp.addNode(Utils.CALENDAR_APP, Utils.NT_UNSTRUCTURED) ;
      if(!calendarRoot.hasNode(CALENDAR_SETTING)) {
        addCalendarSetting(calendarRoot, new CalendarSetting()) ;
      }
      userApp.getSession().save();
      return calendarRoot ;
    }
  }

  private Node getPublicCalendarHome(SessionProvider sProvider) throws Exception {
    //sProvider = SessionProvider.createSystemProvider() ;
    Node calendarServiceHome = getPublicCalendarServiceHome(sProvider) ;
    try {
      return calendarServiceHome.getNode(CALENDARS) ;
    } catch (Exception e) {
      Node cal = calendarServiceHome.addNode(CALENDARS, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return cal ; 
    }
  }

  private Node getUserCalendarHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    try {
      return calendarServiceHome.getNode(CALENDARS) ;
    } catch (Exception e) {
      Node calendars = calendarServiceHome.addNode(CALENDARS, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return calendars ; 
    }
  }

  public Node getRssHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    try {
      return calendarServiceHome.getNode(FEED) ;
    } catch (Exception e) {
      Node feed = calendarServiceHome.addNode(FEED, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return feed ;
    }
  }
  public Node getCalDavHome(SessionProvider sProvider) throws Exception {
    Node calendarServiceHome = getPublicCalendarHome(sProvider) ;
    try {
      return calendarServiceHome.getNode(FEED) ;
    } catch (Exception e) {
      Node feed = calendarServiceHome.addNode(FEED, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return feed ;
    }
  }
  protected Node getCalendarCategoryHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    try {
      return calendarServiceHome.getNode(CALENDAR_CATEGORIES) ;
    } catch (Exception e) {
      Node calCat = calendarServiceHome.addNode(CALENDAR_CATEGORIES, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return calCat;
    }
  }

  protected Node getEventCategoryHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    try {
      return calendarServiceHome.getNode(EVENT_CATEGORIES) ;
    } catch (Exception e) {
      Node eventCat = calendarServiceHome.addNode(EVENT_CATEGORIES, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return eventCat ; 
    }
  }

  public Calendar getUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    Node calendarNode = getUserCalendarHome(sProvider, username).getNode(calendarId) ;
    return getCalendar(new String[]{calendarId}, username, calendarNode, true) ;
  }
  public List<Calendar> getUserCalendars(SessionProvider sProvider, String username, boolean isShowAll) throws Exception {
    NodeIterator iter = getUserCalendarHome(sProvider, username).getNodes() ;
    List<Calendar> calList = new ArrayList<Calendar>() ;
    String[] defaultCalendars = null ;     
    if(getCalendarSetting(sProvider, username) != null){
      defaultCalendars = getCalendarSetting(sProvider, username).getFilterPrivateCalendars() ;
    }
    while(iter.hasNext()) {
      calList.add(getCalendar(defaultCalendars, username, iter.nextNode(), isShowAll)) ;
    }
    return calList ;
  }
  public List<Calendar> getUserCalendarsByCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception {
    Node calendarHome = getUserCalendarHome(sProvider, username) ;    
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
    if(getCalendarSetting(sProvider, username) != null){
      defaultCalendars = getCalendarSetting(sProvider, username).getFilterPrivateCalendars() ;
    }
    while(it.hasNext()){
      calendares.add(getCalendar(defaultCalendars, username, it.nextNode(), true)) ;
    }
    return calendares;
  }
  public void saveUserCalendar(SessionProvider sProvider, String username, Calendar calendar, boolean isNew) throws Exception {
    Node calendarHome = getUserCalendarHome(sProvider, username) ;
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

  @SuppressWarnings("deprecation")
  public Calendar removeUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    Node calendarHome = getUserCalendarHome(sProvider, username) ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(new String[]{calendarId}, username, calNode, true) ;
      NodeIterator iter = calNode.getNodes() ;
      SessionProvider provider = SessionProvider.createSystemProvider() ;
      try {
        while(iter.hasNext()) {
          Node eventNode = iter.nextNode() ;
          Node eventFolder = getEventFolder(provider, eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()) ;
          syncRemoveEvent(eventFolder, eventNode.getName()) ;
          removeReminder(sProvider, eventNode) ;
        }
        calNode.remove() ;
        calendarHome.save() ;
      } catch (Exception e) {
        e.printStackTrace() ;
      } finally {
        provider.close() ;
      }
      return calendar ;
    }
    return null ;
  }

  public Calendar getGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception {
    Node calendarNode = getPublicCalendarHome(sProvider).getNode(calendarId) ;
    return getCalendar(new String[]{calendarId}, null, calendarNode, true) ;
  }

  public List<GroupCalendarData> getGroupCalendars(SessionProvider sProvider, String[] groupIds, boolean isShowAll, String username) throws Exception {
    Node calendarHome = getPublicCalendarHome(sProvider) ;
    List<Calendar> calendars ;
    QueryManager qm ;
    List<GroupCalendarData> groupCalendars = new ArrayList<GroupCalendarData>();
    String[] defaultCalendars = null ;
    if(username!= null && getCalendarSetting(sProvider, username) != null) defaultCalendars = getCalendarSetting(sProvider, username).getFilterPublicCalendars() ;
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

  public void savePublicCalendar(SessionProvider sProvider, Calendar calendar, boolean isNew, String username) throws Exception {
    Node calendarHome = getPublicCalendarHome(sProvider) ;
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

  public Calendar removeGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception {
    Node calendarHome = getPublicCalendarHome(sProvider) ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(new String[]{calendarId}, null, calNode, true) ;
      NodeIterator iter = calNode.getNodes() ;
      while(iter.hasNext()) {
        Node eventNode = iter.nextNode() ;
        Node eventFolder = getEventFolder(sProvider, eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()) ;
        removeReminder(sProvider, eventNode) ;
        syncRemoveEvent(eventFolder, eventNode.getName()) ;
      }
      calNode.remove() ;
      //calendarHome.save() ;
      calendarHome.getSession().save() ;
      return calendar ;
    }
    return null ;
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

  public List<GroupCalendarData> getCalendarCategories(SessionProvider sProvider, String username, boolean isShowAll) throws Exception {
    Node calendarHome = getUserCalendarHome(sProvider, username) ;
    NodeIterator iter = getCalendarCategoryHome(sProvider, username).getNodes() ;
    List<GroupCalendarData> calendarCategories = new ArrayList<GroupCalendarData> () ;
    List<Calendar> calendars ;
    calendarHome.getSession().refresh(false) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    String[] defaultCalendars = null ;
    CalendarSetting calSetting = getCalendarSetting(sProvider, username) ;
    if(calSetting != null) {
      defaultCalendars = calSetting.getFilterPrivateCalendars() ;
    }
    while(iter.hasNext()){      
      Node categoryNode =  iter.nextNode() ;
      String categoryId = categoryNode.getProperty(Utils.EXO_ID).getString() ;     
      StringBuffer queryString = 
        new StringBuffer("/jcr:root" + calendarHome.getPath()  
                         + "//element(*,exo:calendar)[@exo:categoryId='").append(categoryId).append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      calendars = new ArrayList<Calendar> () ;
      if(it.hasNext()) {
        while(it.hasNext()){
          Calendar cal = getCalendar(defaultCalendars, username, it.nextNode(), isShowAll) ;
          if(cal != null) calendars.add(cal) ;
        }        
      }
      calendarCategories.add(new GroupCalendarData(categoryId, categoryNode.getProperty(Utils.EXO_NAME).getString(), calendars)) ;
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

  public CalendarCategory removeCalendarCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception {
    Node calCategoryHome = getCalendarCategoryHome(sProvider, username) ;
    Node calCategoryNode = calCategoryHome.getNode(calendarCategoryId) ; 
    CalendarCategory calCategory = getCalendarCategory(calCategoryNode) ;
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
    if(calCategoryNode.hasProperty(Utils.EXO_ID)) calCategory.setId(calCategoryNode.getProperty(Utils.EXO_ID).getString()) ;
    if(calCategoryNode.hasProperty(Utils.EXO_NAME)) calCategory.setName(calCategoryNode.getProperty(Utils.EXO_NAME).getString()) ;
    if(calCategoryNode.hasProperty(Utils.EXO_DESCRIPTION)) calCategory.setDescription(calCategoryNode.getProperty(Utils.EXO_DESCRIPTION).getString()) ;
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

  public void saveEventCategory(SessionProvider sProvider, String username, EventCategory eventCategory, String[] values, boolean isNew) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(sProvider, username) ;
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
      Node calendarHome = getUserCalendarHome(sProvider, username) ;
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
      SessionProvider systemSession = SessionProvider.createSystemProvider() ; 
      try {
      if(getSharedCalendarHome(systemSession).hasNode(username)) {
        PropertyIterator iterPro = getSharedCalendarHome(systemSession).getNode(username).getReferences() ;
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
      } finally {
        systemSession.close() ;
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

  public void removeEventCategory(SessionProvider sProvider, String username, String eventCategoryName) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(sProvider, username) ;
    if(eventCategoryHome.hasNode(eventCategoryName)) {
      Node eventCategoryNode = eventCategoryHome.getNode(eventCategoryName) ;
      for(CalendarEvent ce : getUserEventByCategory(sProvider, username, eventCategoryName)) {
        removeUserEvent(sProvider, username, ce.getCalendarId(), ce.getId()) ;
      }
      SessionProvider systemSession = SessionProvider.createSystemProvider() ;
      try {
        for(CalendarEvent ce : getSharedEventByCategory(username, eventCategoryName)) {
          removeSharedEvent(systemSession, username, ce.getCalendarId(), ce.getId()) ;
        }
        for(CalendarEvent ce : getPublicEventByCategory(username, eventCategoryName)) {
          removePublicEvent(systemSession,ce.getCalendarId(), ce.getId()) ;
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

  public EventCategory getEventCategory(SessionProvider sProvider, String username, String eventCategoryName) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(sProvider, username) ;
    return getEventCategory(eventCategoryHome.getNode(eventCategoryName)) ;
  }

  //Event APIs

  public CalendarEvent getUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception {
    Node calendarNode = getUserCalendarHome(sProvider, username).getNode(calendarId) ;
    return getEvent(sProvider, calendarNode.getNode(eventId)) ;
  }

  public List<CalendarEvent> getUserEventByCalendar(SessionProvider sProvider, String username, List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(String calendarId : calendarIds) {
      Node calendarNode = getUserCalendarHome(sProvider, username).getNode(calendarId) ;
      NodeIterator it = calendarNode.getNodes();
      while(it.hasNext()) {
        events.add(getEvent(sProvider, it.nextNode())) ;
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
          events.add(getEvent(systemSession, it.nextNode())) ;
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
                events.add(getEvent(systemSession, eventNode)) ;
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

  public List<CalendarEvent> getUserEventByCategory(SessionProvider sProvider, String username, String eventCategoryId) throws Exception {
    Node calendarHome = getUserCalendarHome(sProvider, username) ;
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
        events.add(getEvent(sProvider, it.nextNode())) ;
      }
    }
    return events;
  }

  public List<CalendarEvent> getUserEvents(SessionProvider sProvider, String username, EventQuery eventQuery) throws Exception {
    Node calendarHome = getUserCalendarHome(sProvider, username) ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    eventQuery.setCalendarPath(calendarHome.getPath()) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager() ;
    Query query = qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType()) ;
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    CalendarEvent calEvent ;
    while(it.hasNext()) {
      calEvent = getEvent(sProvider, it.nextNode()) ;
      calEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE)) ;
      events.add(calEvent) ;
    }
    return events ;
  }

  public void saveUserEvent(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getUserCalendarHome(sProvider, username).getNode(calendarId);
    if(event.getReminders() != null && event.getReminders().size() > 0) {
      //Need to use system session
      SessionProvider systemSession = SessionProvider.createSystemProvider();
      try {
        Node reminderFolder = getReminderFolder(systemSession, event.getFromDateTime()) ;
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

  public CalendarEvent removeUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception {
    Node calendarNode = getUserCalendarHome(sProvider, username).getNode(calendarId);
    if(calendarNode.hasNode(eventId)){
      Node eventNode = calendarNode.getNode(eventId) ;
      CalendarEvent event = getEvent(sProvider, eventNode) ;
      //Need to use system session
      SessionProvider systemSession = SessionProvider.createSystemProvider() ;
      try {
        Node eventFolder = getEventFolder(systemSession, event.getFromDateTime()) ;
        syncRemoveEvent(eventFolder, event.getId()) ;
      } catch (Exception e) {
        e.printStackTrace() ;        
      }
      removeReminder(systemSession, eventNode) ;
      systemSession.close() ;
      eventNode.remove() ;
      calendarNode.save() ;
      calendarNode.getSession().save() ;
      calendarNode.refresh(true) ;
      return event;
    }
    return null ;
  }

  private void removeReminder(SessionProvider systemSession, Node eventNode)throws Exception {
    // Need to use system session
    if(eventNode.hasProperty(Utils.EXO_FROM_DATE_TIME)) {
      //SessionProvider systemSession = SessionProvider.createSystemProvider() ;
      try {
        Node reminders = getReminderFolder(systemSession, eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()) ;
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

  public CalendarEvent getGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception {
    Node calendarNode = getPublicCalendarHome(sProvider).getNode(calendarId) ;
    CalendarEvent calEvent = getEvent(sProvider, calendarNode.getNode(eventId)) ;
    calEvent.setCalType(String.valueOf(Calendar.TYPE_PUBLIC)) ;
    return calEvent ;
  }

  public List<CalendarEvent> getGroupEventByCalendar(SessionProvider sProvider, List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(String calendarId : calendarIds){
      Node calendarNode = getPublicCalendarHome(sProvider).getNode(calendarId) ;    
      NodeIterator it = calendarNode.getNodes();
      while(it.hasNext()) {
        events.add(getEvent(sProvider, it.nextNode())) ;
      }
    }
    return events ;
  }

  public List<CalendarEvent> getPublicEvents(SessionProvider sProvider, EventQuery eventQuery) throws Exception {
    Node calendarHome = getPublicCalendarHome(sProvider) ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    eventQuery.setCalendarPath(calendarHome.getPath()) ;
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager() ;
    Query query = qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType()) ;
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    CalendarEvent calEvent ;
    while(it.hasNext()) {
      calEvent = getEvent(sProvider, it.nextNode()) ;
      calEvent.setCalType(String.valueOf(Calendar.TYPE_PUBLIC)) ;
      events.add(calEvent) ;
    }
    return events ;
  }
  public void savePublicEvent(SessionProvider sProvider, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getPublicCalendarHome(sProvider).getNode(calendarId) ;
    Node reminderFolder = getReminderFolder(sProvider, event.getFromDateTime()) ;
    saveEvent(calendarNode, event, reminderFolder, isNew) ;
  }

  public CalendarEvent removePublicEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception {
    Node calendarNode = getPublicCalendarHome(sProvider).getNode(calendarId) ;
    if(calendarNode.hasNode(eventId)){
      Node eventNode = calendarNode.getNode(eventId) ;
      CalendarEvent event = getEvent(sProvider, eventNode) ;
      removeReminder(sProvider, eventNode) ;
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


  private CalendarEvent getEvent(SessionProvider sProvider, Node eventNode) throws Exception {
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
    SessionProvider systemSession =  SessionProvider.createSystemProvider() ;
    try {
      event.setReminders(getReminders(systemSession, eventNode)) ;
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
        removeReminder(systemSession, eventNode) ; 
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
  private Node getReminderFolder(SessionProvider sysProvider, Date fromDate)throws Exception {
    Node publicApp = getPublicCalendarServiceHome(sysProvider) ;
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

  private Node getEventFolder(SessionProvider sysProvider, Date fromDate)throws Exception {
    Node publicApp = getPublicCalendarServiceHome(sysProvider) ;
    Node dateFolder = getDateFolder(publicApp, fromDate) ;
    try {
      return dateFolder.getNode(CALENDAR_EVENT) ;
    } catch (Exception e) {
      dateFolder.addNode(CALENDAR_EVENT, Utils.NT_UNSTRUCTURED) ;
      getPublicRoot(sysProvider).getSession().save() ;
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

  private List<Reminder> getReminders(SessionProvider sProvider, Node eventNode) throws Exception {
    List<Reminder> reminders = new ArrayList<Reminder> () ;
    Date fromDate = eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime() ;
    Node reminderFolder = getReminderFolder(sProvider, fromDate) ;
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
  public void saveCalendarSetting(SessionProvider sProvider, String username, CalendarSetting setting) throws Exception {
    Node calendarHome = getUserCalendarServiceHome(sProvider, username) ;
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
  }
  public CalendarSetting getCalendarSetting(SessionProvider sProvider ,String username) throws Exception{
    Node calendarHome = getUserCalendarServiceHome(sProvider, username) ;
    if(calendarHome.hasNode(CALENDAR_SETTING)){
      CalendarSetting calendarSetting = new CalendarSetting() ;
      Node settingNode = calendarHome.getNode(CALENDAR_SETTING) ;      
      calendarSetting.setViewType(settingNode.getProperty(Utils.EXO_VIEW_TYPE).getString()) ;
      calendarSetting.setTimeInterval(settingNode.getProperty(Utils.EXO_TIME_INTERVAL).getLong()) ;
      calendarSetting.setWeekStartOn(settingNode.getProperty(Utils.EXO_WEEK_START_ON).getString()) ;
      calendarSetting.setDateFormat(settingNode.getProperty(Utils.EXO_DATE_FORMAT).getString()) ;
      calendarSetting.setTimeFormat(settingNode.getProperty(Utils.EXO_TIME_FORMAT).getString()) ;
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

  public List<FeedData> getFeeds(SessionProvider sProvider ,String username) throws Exception {
    List<FeedData> feeds = new ArrayList<FeedData>() ;
    Node rssHome = getRssHome(sProvider, username) ;
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

  public int generateRss(SessionProvider sProvider ,String username, List<String> calendarIds, RssData rssData, 
                         CalendarImportExport importExport) throws Exception {
    Node rssHomeNode = getRssHome(sProvider, username) ;
    Node iCalHome = null ;
    try {
      iCalHome = rssHomeNode.getNode("iCalendars") ;
    } catch (Exception e) {
      iCalHome = rssHomeNode.addNode("iCalendars", Utils.NT_UNSTRUCTURED) ;
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
        OutputStream out = importExport.exportCalendar(sProvider, username, Arrays.asList(new String[]{calendarId}), "0") ;
        if(out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          try {
            iCalHome.getNode(calendarId + ".ics").setProperty("exo:data", is) ;  
          } catch (Exception e) {
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


  public int generateCalDav(SessionProvider sProvider ,String username, List<String> calendarIds, RssData rssData, 
                            CalendarImportExport importExport) throws Exception {
    Node rssHomeNode = getRssHome(sProvider, username) ;
    Node WebDaveiCalHome = null ;
    try {
      WebDaveiCalHome = rssHomeNode.getNode("WebDavCalendars") ;
    } catch (Exception e) {
      WebDaveiCalHome = rssHomeNode.addNode("WebDavCalendars", Utils.NT_UNSTRUCTURED) ;
      rssHomeNode.save();
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
        OutputStream out = importExport.exportCalendar(sProvider, username, Arrays.asList(new String[]{calendarId}), "0") ;
        if(out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          Node ical = null ;
          Node nodeContent = null ;
          try {
            ical = WebDaveiCalHome.getNode(calendarId + ".ics") ;
            nodeContent = ical.getNode(Utils.JCR_CONTENT);
          } catch (Exception e) {
            ical = WebDaveiCalHome.addNode(calendarId + ".ics", Utils.NT_FILE) ; 
            nodeContent = ical.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
          }
          nodeContent.setProperty(Utils.JCR_LASTMODIFIED, java.util.Calendar.getInstance().getTimeInMillis()) ;
          nodeContent.setProperty(Utils.JCR_MIMETYPE, Utils.MIMETYPE_ICALENDAR);
          nodeContent.setProperty(Utils.JCR_DATA, is);
          WebDaveiCalHome.save() ;
          String link = rssData.getLink() + ical.getPath() ;
          Calendar exoCal = getUserCalendar(sProvider, username, calendarId) ;
          entry = new SyndEntryImpl();
          entry.setTitle(exoCal.getName());                
          entry.setLink(link);     
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
        System.out.println("No data to make caldav!");
        return -1 ;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return -1 ;
    }     
    return 1 ;
  }

  private void storeXML(String feedXML, Node rssHome, String rssNodeName, RssData rssData) throws Exception{
    Node rss ;
    if(rssHome.hasNode(rssNodeName)) rss = rssHome.getNode(rssNodeName);
    else rss = rssHome.addNode(rssNodeName, Utils.EXO_RSS_DATA);
    rss.setProperty(Utils.EXO_BASE_URL, rssData.getUrl()) ;
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
    List<CalendarEvent> events = new ArrayList<CalendarEvent>(); 
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    try {
      if(eventQuery.getCalendarId() == null) {
        events.addAll(getUserEvents(sProvider, username, eventQuery));
        if(publicCalendarIds != null && publicCalendarIds.length > 0) {
          eventQuery.setCalendarId(publicCalendarIds);
          events.addAll(getPublicEvents(systemSession, eventQuery));
          eventQuery.setCalendarId(null) ;
        }
        events.addAll(getSharedEvents(systemSession, username, eventQuery));
      } else {
        String calFullId = eventQuery.getCalendarId()[0] ;
        if(calFullId.split(Utils.COLON).length > 0) {
          String[] calId = new String[]{calFullId.split(Utils.COLON)[1]} ;
          int type = Integer.parseInt(calFullId.split(Utils.COLON)[0]) ;
          eventQuery.setCalendarId(calId) ;
          switch (type) {
          case Calendar.TYPE_PRIVATE:
            events.addAll(getUserEvents(sProvider, username, eventQuery));
            return new EventPageList(events, 10);    
          case Calendar.TYPE_SHARED:
            events.addAll(getSharedEvents(systemSession, username, eventQuery));
            return new EventPageList(events, 10);    
          case Calendar.TYPE_PUBLIC:
            events.addAll(getPublicEvents(systemSession, eventQuery));
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

  public Map<Integer, String > searchHightLightEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception {
    Map<Integer, String > mapData = new HashMap<Integer, String>() ;
    Query query ;
    QueryManager qm ;
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    try {
      CalendarSetting calSetting = getCalendarSetting(sProvider, username)  ;
      // private events
      if(username != null && username.length() > 0) {
        Node calendarHome = getUserCalendarHome(sProvider, username) ;
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
  @SuppressWarnings("deprecation")
  public void shareCalendar(SessionProvider sProvider, String username, String calendarId, List<String> receiverUsers) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome(sProvider) ;
    Node calendarNode = getUserCalendarHome(sProvider, username).getNode(calendarId) ;
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
      CalendarSetting calSetting = getCalendarSetting(sProvider, user) ;
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
      saveCalendarSetting(sProvider, user, calSetting) ;
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

  public GroupCalendarData getSharedCalendars(SessionProvider sProvider, String username, boolean isShowAll) throws Exception {
    if(getSharedCalendarHome(sProvider).hasNode(username)) {
      Node sharedNode = getSharedCalendarHome(sProvider).getNode(username) ;
      List<Calendar> calendars = new ArrayList<Calendar>() ;
      PropertyIterator iter = sharedNode.getReferences() ;
      String[] defaultFilterCalendars =  null  ;
      if(getCalendarSetting(sProvider, username) != null) {
        defaultFilterCalendars = getCalendarSetting(sProvider, username).getFilterSharedCalendars() ;
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
  public void saveSharedCalendar(SessionProvider sProvider, String username, Calendar calendar) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome(sProvider) ;
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
          CalendarSetting usCalSetting = getCalendarSetting(sProvider, username) ;
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
          saveCalendarSetting(sProvider, username, usCalSetting) ;
          calendarNode.save() ;
          break ;
        }
      }
    }      
  }

  public List<CalendarEvent> getSharedEvents(SessionProvider sProvider, String username, EventQuery eventQuery) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    if(getSharedCalendarHome(sProvider).hasNode(username)) {
      PropertyIterator iter = getSharedCalendarHome(sProvider).getNode(username).getReferences() ;
      CalendarEvent calEvent ;
      while(iter.hasNext()) {
        try{
          Node calendar = iter.nextProperty().getParent() ;
          eventQuery.setCalendarPath(calendar.getPath()) ;
          QueryManager qm = calendar.getSession().getWorkspace().getQueryManager() ;
          Query query = qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType()) ;
          NodeIterator it = query.execute().getNodes();
          while(it.hasNext()){
            calEvent = getEvent(sProvider, it.nextNode()) ;
            //if(eventQuery.getFilterCalendarIds()== null || !Arrays.asList(eventQuery.getFilterCalendarIds()).contains(calEvent.getCalendarId())) {
            calEvent.setCalType("1") ;
            events.add(calEvent) ;
            //}
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
          if(calendarIds.contains(calendar.getProperty(Utils.EXO_ID).getString())) {
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



  public void removeSharedCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome(sProvider) ;
    // TODO use try catch
    if(sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username) ;
      String uuid = userNode.getProperty("jcr:uuid").getString() ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendar ;
      CalendarSetting calSetting = getCalendarSetting(sProvider, username) ;
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
          saveCalendarSetting(sProvider, username, calSetting) ;
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
    }
  }

  public void saveEventToSharedCalendar(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  {
    Node sharedCalendarHome = getSharedCalendarHome(sProvider) ;
    //Node eventFolder = getEventFolder(SessionProvider.createSystemProvider(), event.getFromDateTime()) ;
    if(sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendar ;      
      while(iter.hasNext()) {
        calendar = iter.nextProperty().getParent() ;
        if(calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId)) {
          Node reminderFolder = getReminderFolder(sProvider, event.getFromDateTime()) ;
          saveEvent(calendar, event, reminderFolder, isNew) ;
          calendar.save() ;
          break ;
        }
      }      
    }
  } 

  public List<CalendarEvent> getEvents(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    List<String> filterList = new ArrayList<String>() ;
    CalendarSetting calSetting = getCalendarSetting(sProvider, username) ;
    filterList.addAll(Arrays.asList(calSetting.getFilterPrivateCalendars())) ;
    filterList.addAll(Arrays.asList(calSetting.getFilterPublicCalendars())) ;
    filterList.addAll(Arrays.asList(calSetting.getFilterSharedCalendars())) ;
    eventQuery.setFilterCalendarIds(filterList.toArray(new String[]{})) ;
    events.addAll(getUserEvents(sProvider, username, eventQuery)) ;
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    try {
      events.addAll(getSharedEvents(systemSession, username, eventQuery)) ;
      if(publicCalendarIds != null && publicCalendarIds.length > 0) { 
        eventQuery.setCalendarId(publicCalendarIds) ;
        events.addAll(getPublicEvents(systemSession, eventQuery)) ;
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      systemSession.close() ;
    }
    return events ;
  }


  public Map<String, String> checkFreeBusy(SessionProvider sysProvider, EventQuery eventQuery) throws Exception {
    Node eventFolder = getEventFolder(sysProvider, eventQuery.getFromDate().getTime()) ;
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

  public void removeSharedEvent(SessionProvider sessionProvider, String username, String calendarId, String eventId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome(sessionProvider) ;
    if(sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendar ;
      while(iter.hasNext()) {
        calendar = iter.nextProperty().getParent() ;
        if(calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId)) {
          if(calendar.hasNode(eventId)) {
            Node event = calendar.getNode(eventId) ;
            Node eventFolder = getEventFolder(sessionProvider, event.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()) ;
            syncRemoveEvent(eventFolder, eventId) ;
            removeReminder(sessionProvider, event) ;
            event.remove() ;
          }
          calendar.save() ;
          calendar.refresh(true) ;
          break ;
        }
      }      
    }
  }

  public void moveEvent(SessionProvider sProvider, String formCalendar, String toCalendar, String fromType, String toType, List<CalendarEvent> calEvents, String username) throws Exception {
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    try {
      switch (Integer.parseInt(fromType)) {
      case  Calendar.TYPE_PRIVATE :  
        if(getUserCalendarHome(sProvider, username).hasNode(formCalendar)) {
          switch (Integer.parseInt(toType)) {
          case Calendar.TYPE_PRIVATE:
            //move events in side private calendars
            if(getUserCalendarHome(sProvider, username).hasNode(toCalendar)){
              for(CalendarEvent calEvent : calEvents) {
                if(!formCalendar.equals(toCalendar)) {
                  removeUserEvent(sProvider, username, formCalendar, calEvent.getId()) ;
                  calEvent.setCalendarId(toCalendar) ;
                  saveUserEvent(sProvider, username, toCalendar, calEvent, getUserCalendarHome(sProvider, username).getNode(toCalendar).hasNode(calEvent.getId())) ;
                } else {
                  saveUserEvent(sProvider, username, toCalendar, calEvent,  false) ;
                }
              }
            }
            break;
          case Calendar.TYPE_SHARED:
            //move events form private to shared calendar
            if(getSharedCalendarHome(systemSession).hasNode(username)){
              for(CalendarEvent calEvent : calEvents) {
                removeUserEvent(sProvider, username, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                saveEventToSharedCalendar(systemSession, username, toCalendar, calEvent, getSharedCalendarHome(systemSession).getNode(username).hasNode(calEvent.getId()));
              }
            }
            break;
          case Calendar.TYPE_PUBLIC:
            //move events form private to public calendar
            if(getPublicCalendarHome(systemSession).hasNode(toCalendar)){
              for(CalendarEvent calEvent : calEvents) {
                removeUserEvent(systemSession, username, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                savePublicEvent(systemSession, toCalendar, calEvent, getPublicCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId())) ;
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
            if(getUserCalendarHome(sProvider, username).hasNode(toCalendar)) {
              for(CalendarEvent calEvent : calEvents) {
                removeSharedEvent(systemSession, username, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                saveUserEvent(sProvider, username, toCalendar, calEvent, getUserCalendarHome(sProvider, username).getNode(toCalendar).hasNode(calEvent.getId())) ;
              }
            }
            break;
          case Calendar.TYPE_SHARED:
            //   move events in side shared calendars
            if(getSharedCalendarHome(systemSession).hasNode(username)){
              for(CalendarEvent calEvent : calEvents) {
                if(!formCalendar.equals(toCalendar)) {
                  removeSharedEvent(systemSession, username, formCalendar, calEvent.getId()) ;
                  calEvent.setCalendarId(toCalendar) ;
                  saveEventToSharedCalendar(systemSession, username, toCalendar, calEvent, getSharedCalendarHome(systemSession).getNode(username).hasNode(calEvent.getId()));
                } else {
                  saveEventToSharedCalendar(systemSession, username, toCalendar, calEvent, false);
                }
              }
            }
            break;
          case Calendar.TYPE_PUBLIC:
            //move events form share to public calendar
            if(getPublicCalendarHome(systemSession).hasNode(toCalendar)) {
              for(CalendarEvent calEvent : calEvents) {
                removeSharedEvent(systemSession, username, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                savePublicEvent(systemSession, toCalendar, calEvent, getPublicCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId())) ;
              }
            }
            break;
          default:
            break;
          }
        }
        break;
      case Calendar.TYPE_PUBLIC:
        if(getPublicCalendarHome(systemSession).hasNode(formCalendar)) {
          switch (Integer.parseInt(toType)) {
          case Calendar.TYPE_PRIVATE:
            //move events from public to private calendar
            if(getUserCalendarHome(sProvider, username).hasNode(toCalendar)) {
              for(CalendarEvent calEvent : calEvents) {
                removePublicEvent(systemSession, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                saveUserEvent(sProvider, username, toCalendar, calEvent, getUserCalendarHome(sProvider, username).getNode(toCalendar).hasNode(calEvent.getId())) ;
              }
            }
            break;
          case Calendar.TYPE_SHARED:
            //move events from public to shared calendar
            if(getSharedCalendarHome(systemSession).hasNode(username)){
              for(CalendarEvent calEvent : calEvents) {
                removePublicEvent(systemSession, formCalendar, calEvent.getId()) ;
                calEvent.setCalendarId(toCalendar) ;
                saveEventToSharedCalendar(systemSession, username, toCalendar, calEvent, true);
              }
            }
            break;
          case Calendar.TYPE_PUBLIC:
            //move events in side public calendars
            if(getPublicCalendarHome(systemSession).hasNode(toCalendar)){
              for(CalendarEvent calEvent : calEvents) {
                if(!formCalendar.equals(toCalendar)) {
                  removePublicEvent(systemSession, formCalendar, calEvent.getId()) ;
                  calEvent.setCalendarId(toCalendar) ;
                  savePublicEvent(systemSession, toCalendar, calEvent, getPublicCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId())) ;
                } else {
                  savePublicEvent(systemSession, toCalendar, calEvent,  false) ;
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
        event = getUserEvent(session, fromUserId, calendarId, eventId) ;
      } else  if(Calendar.TYPE_SHARED == calType)  {
        List<String> calendarIds = new ArrayList<String>() ;
        calendarIds.add(calendarId) ;
        for(CalendarEvent calEvent : getSharedEventByCalendars(session, fromUserId, calendarIds)) {
          if(calEvent.getId().equals(eventId)) {
            event = calEvent ;
            break ;
          }
        }
      } else  if(Calendar.TYPE_PUBLIC == calType)  {
        event = getGroupEvent(session, calendarId, eventId) ;
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
          saveUserEvent(session, fromUserId, calendarId, event, false) ;
        } else  if(Calendar.TYPE_SHARED == calType)  {
          saveEventToSharedCalendar(session, fromUserId, calendarId, event, false) ;
        } else  if(Calendar.TYPE_PUBLIC == calType)  {
          savePublicEvent(session, calendarId, event, false) ;
        }
      }
    } catch (Exception e) {
      System.out.println("\n\n confirmInvitation " + e.getClass().toString()) ;
      //e.printStackTrace() ;
    } finally {
      session.close() ;
    }
  }
}


