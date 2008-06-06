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
public class JCRDataStorage{

  final private static String CALENDARS = "calendars".intern() ;

  final private static String SHARED_CALENDAR = "sharedCalendars".intern() ;
  final private static String TASKS = "tasks".intern() ;
  final private static String CALENDAR_CATEGORIES = "categories".intern() ;
  final private static String FEED = "eXoCalendarFeed".intern() ;
  final private static String CALENDAR_GROUPS = "groups".intern() ;
  final private static String CALENDAR_REMINDER = "reminders".intern() ;
  final private static String CALENDAR_EVENT = "events".intern() ;
  final private static String CALENDAR_SETTING = "calendarSetting".intern() ;
  final private static String EVENT_CATEGORIES = "eventCategories".intern() ;
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final private static String CALENDAR_APP = "CalendarApplication".intern() ;
  private static final String SHARED_MIXIN = "exo:calendarShared".intern();
  private static final String SHARED_PROP = "exo:sharedId".intern();
  private final static String VALUE = "value".intern() ; 

  private NodeHierarchyCreator nodeHierarchyCreator_ ;

  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ; 
  }  

  private Node getPublicCalendarServiceHome(SessionProvider sProvider) throws Exception {
    //sProvider = SessionProvider.createSystemProvider() ;
    Node publicApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider)  ;
    try {
      return publicApp.getNode(CALENDAR_APP) ;
    } catch (Exception e) {
      Node calendarApp = publicApp.addNode(CALENDAR_APP, NT_UNSTRUCTURED) ;
      publicApp.getSession().save() ;
      return calendarApp ;
    }
  }

  private Node getSharedCalendarHome(SessionProvider sProvider) throws Exception {
    Node calendarServiceHome = getPublicCalendarServiceHome(sProvider) ;
    try {
      return calendarServiceHome.getNode(SHARED_CALENDAR) ;
    } catch (Exception e) {
      Node sharedCal = calendarServiceHome.addNode(SHARED_CALENDAR, NT_UNSTRUCTURED) ;
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
      return userApp.getNode(CALENDAR_APP) ;
    } catch (Exception e) {
      calendarRoot = userApp.addNode(CALENDAR_APP, NT_UNSTRUCTURED) ;
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
      Node cal = calendarServiceHome.addNode(CALENDARS, NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return cal ; 
    }
  }

  private Node getUserCalendarHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    try {
      return calendarServiceHome.getNode(CALENDARS) ;
    } catch (Exception e) {
      Node calendars = calendarServiceHome.addNode(CALENDARS, NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return calendars ; 
    }
  }

  public Node getRssHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    try {
      return calendarServiceHome.getNode(FEED) ;
    } catch (Exception e) {
      Node feed = calendarServiceHome.addNode(FEED, NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return feed ;
    }
  }

  protected Node getCalendarCategoryHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    try {
      return calendarServiceHome.getNode(CALENDAR_CATEGORIES) ;
    } catch (Exception e) {
      Node calCat = calendarServiceHome.addNode(CALENDAR_CATEGORIES, NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return calCat;
    }
  }

  protected Node getEventCategoryHome(SessionProvider sProvider, String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(sProvider, username) ;
    try {
      return calendarServiceHome.getNode(EVENT_CATEGORIES) ;
    } catch (Exception e) {
      Node eventCat = calendarServiceHome.addNode(EVENT_CATEGORIES, NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return eventCat ; 
    }
  }

  /*  private Node getCalendarGroupHome(SessionProvider sProvider) throws Exception {
    Node calendarServiceHome = getPublicCalendarServiceHome(sProvider) ;

    if(calendarServiceHome.hasNode(CALENDAR_GROUPS)) return calendarServiceHome.getNode(CALENDAR_GROUPS) ;
    else {
      calendarServiceHome.addNode(CALENDAR_GROUPS, NT_UNSTRUCTURED) ;
      getPublicRoot(sProvider).getSession().save() ;
    }
    return calendarServiceHome.getNode(CALENDAR_GROUPS) ; 
  } 
   */
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
        calendarNode = calendarHome.addNode(calendar.getId(), "exo:calendar") ;
        calendarNode.setProperty("exo:id", calendar.getId()) ;
        /*CalendarSetting setting = getCalendarSetting(sProvider, username) ;
        if(setting == null) setting = new CalendarSetting() ;
        List<String> privateCalendars = new ArrayList<String>() ;
        if(setting.getDefaultPrivateCalendars() != null) {
          privateCalendars.addAll(Arrays.asList(setting.getDefaultPrivateCalendars())) ;
        }
        if(!privateCalendars.contains((calendar.getId()))) privateCalendars.add(calendar.getId()) ;
        setting.setDefaultPrivateCalendars(privateCalendars.toArray(new String[privateCalendars.size()])) ;
        saveCalendarSetting(sProvider, username, setting) ;*/
        if(calendar.isDataInit()) reparePermissions(calendarNode, username) ;
      }
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
    calendarNode.setProperty("exo:calendarOwner", calendar.getCalendarOwner()) ;
    //Check to save category
    /*if(calendar.getCategoryId() != null && calendar.getCategoryId().length() > 0) {
      Node calendarCategory = getCalendarCategoryHome(sProvider, username).getNode(calendar.getCategoryId()) ;
      checkToSave(calendarCategory, calendar.getId()) ;
      calendarCategory.save() ;
    }*/
    calendarHome.getSession().save() ;
  }

  public Calendar removeUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    Node calendarHome = getUserCalendarHome(sProvider, username) ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(new String[]{calendarId}, username, calNode, true) ;
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
    Node calendarNode = getPublicCalendarHome(sProvider).getNode(calendarId) ;
    return getCalendar(new String[]{calendarId}, null, calendarNode, true) ;
  }

  public List<GroupCalendarData> getGroupCalendars(SessionProvider sProvider, String[] groupIds, boolean isShowAll, String username) throws Exception {
    Node calendarHome = getPublicCalendarHome(sProvider) ;
    List<Calendar> calendars ;
    QueryManager qm ;
    List<GroupCalendarData> groupCalendars = new ArrayList<GroupCalendarData>();
    String[] defaultCalendars = null ;
    if(getCalendarSetting(sProvider, username) != null) defaultCalendars = getCalendarSetting(sProvider, username).getFilterPublicCalendars() ;
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
      calendarNode = calendarHome.addNode(calendar.getId(), "exo:calendar") ;
      calendarNode.setProperty("exo:id", calendar.getId()) ;   
      if(username != null) {
        //TODO update all user form group that this user belong to.
        /*CalendarSetting calSetting = getCalendarSetting(sProvider, username) ;
        if(calSetting == null) calSetting = new CalendarSetting() ;
        List<String> publicCalendars = new ArrayList<String>() ;
        if(calSetting.getDefaultPublicCalendars() != null) {
          publicCalendars.addAll(Arrays.asList(calSetting.getDefaultPublicCalendars())) ;
        }
        if(!publicCalendars.contains(calendar.getId())) publicCalendars.add(calendar.getId()) ;
        calSetting.setDefaultPublicCalendars(publicCalendars.toArray(new String[publicCalendars.size()])) ;
        saveCalendarSetting(sProvider, username, calSetting) ;*/
      }
    }else {
      calendarNode = calendarHome.getNode(calendar.getId()) ;
    }    
    calendarNode.setProperty("exo:name", calendar.getName()) ;
    calendarNode.setProperty("exo:description", calendar.getDescription()) ;
    calendarNode.setProperty("exo:categoryId", calendar.getCategoryId()) ;
    calendarNode.setProperty("exo:viewPermissions", calendar.getViewPermission()) ;
    calendarNode.setProperty("exo:editPermissions", calendar.getEditPermission()) ;
    calendarNode.setProperty("exo:locale", calendar.getLocale()) ;
    calendarNode.setProperty("exo:timeZone", calendar.getTimeZone()) ;
    calendarNode.setProperty("exo:calendarOwner", calendar.getCalendarOwner()) ;
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
    Node calendarHome = getPublicCalendarHome(sProvider) ;
    if(calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId) ;
      Calendar calendar = getCalendar(new String[]{calendarId}, null, calNode, true) ;
      NodeIterator iter = calNode.getNodes() ;
      while(iter.hasNext()) {
        removeReminder(sProvider, iter.nextNode()) ;
      }
      calNode.remove() ;
      //calendarHome.save() ;
      calendarHome.getSession().save() ;
      return calendar ;
    }
    return null ;
  }
  /*private void checkToSave(Node groupNode, String calendarId) throws Exception{
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

  }*/
  private Calendar getCalendar(String[] defaultFilterCalendars, String username, Node calNode, boolean isShowAll) throws Exception {
    Calendar calendar = null ;
    if(isShowAll) {
      calendar = new Calendar() ;
      if(calNode.hasProperty("exo:id")) calendar.setId(calNode.getProperty("exo:id").getString()) ;
      if(calNode.hasProperty("exo:name")) calendar.setName(calNode.getProperty("exo:name").getString()) ;
      if(calNode.hasProperty("exo:description")) calendar.setDescription(calNode.getProperty("exo:description").getString()) ;
      if(calNode.hasProperty("exo:categoryId")) calendar.setCategoryId(calNode.getProperty("exo:categoryId").getString()) ;
      if(calNode.hasProperty("exo:locale")) calendar.setLocale(calNode.getProperty("exo:locale").getString()) ;
      if(calNode.hasProperty("exo:timeZone")) calendar.setTimeZone(calNode.getProperty("exo:timeZone").getString()) ;
      if(calNode.hasProperty("exo:calendarColor")) calendar.setCalendarColor(calNode.getProperty("exo:calendarColor").getString()) ;
      if(calNode.hasProperty("exo:calendarOwner")) calendar.setCalendarOwner(calNode.getProperty("exo:calendarOwner").getString()) ;
      if(!calendar.isPublic()) {
        if(calNode.hasProperty("exo:groups")){
          Value[] values = calNode.getProperty("exo:groups").getValues() ;
          List<String> groups = new ArrayList<String>() ;
          for(Value v : values) {
            groups.add(v.getString()) ;
          }
          calendar.setGroups(groups.toArray(new String[groups.size()])) ;
        }

        if(calNode.hasProperty("exo:viewPermissions")) {
          Value[] viewValues = calNode.getProperty("exo:viewPermissions").getValues() ;
          List<String> viewPerms = new ArrayList<String>() ;
          for(Value v : viewValues) {
            viewPerms.add(v.getString()) ;
          }
          calendar.setViewPermission(viewPerms.toArray(new String[viewPerms.size()])) ;
        }
        if(calNode.hasProperty("exo:editPermissions")) {
          Value[] editValues = calNode.getProperty("exo:editPermissions").getValues() ;
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
        if(calNode.hasProperty("exo:id")) calendar.setId(calNode.getProperty("exo:id").getString()) ;
        if(calNode.hasProperty("exo:name")) calendar.setName(calNode.getProperty("exo:name").getString()) ;
        if(calNode.hasProperty("exo:description")) calendar.setDescription(calNode.getProperty("exo:description").getString()) ;
        if(calNode.hasProperty("exo:categoryId")) calendar.setCategoryId(calNode.getProperty("exo:categoryId").getString()) ;
        if(calNode.hasProperty("exo:locale")) calendar.setLocale(calNode.getProperty("exo:locale").getString()) ;
        if(calNode.hasProperty("exo:timeZone")) calendar.setTimeZone(calNode.getProperty("exo:timeZone").getString()) ;
        if(calNode.hasProperty("exo:sharedColor")) calendar.setCalendarColor(calNode.getProperty("exo:sharedColor").getString()) ;
        if(calNode.hasProperty("exo:calendarColor")) calendar.setCalendarColor(calNode.getProperty("exo:calendarColor").getString()) ;
        if(calNode.hasProperty("exo:calendarOwner")) calendar.setCalendarOwner(calNode.getProperty("exo:calendarOwner").getString()) ;
        if(!calendar.isPublic()) {
          if(calNode.hasProperty("exo:groups")){
            Value[] values = calNode.getProperty("exo:groups").getValues() ;
            List<String> groups = new ArrayList<String>() ;
            for(Value v : values) {
              groups.add(v.getString()) ;
            }
            calendar.setGroups(groups.toArray(new String[groups.size()])) ;
          }
          if(calNode.hasProperty("exo:viewPermissions")) {
            Value[] viewValues = calNode.getProperty("exo:viewPermissions").getValues() ;
            List<String> viewPerms = new ArrayList<String>() ;
            for(Value v : viewValues) {
              viewPerms.add(v.getString()) ;
            }
            calendar.setViewPermission(viewPerms.toArray(new String[viewPerms.size()])) ;
          }
          if(calNode.hasProperty("exo:editPermissions")) {
            Value[] editValues = calNode.getProperty("exo:editPermissions").getValues() ;
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
          Calendar cal = getCalendar(defaultCalendars, username, it.nextNode(), isShowAll) ;
          if(cal != null) calendars.add(cal) ;
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
      if(calendarCategory.isDataInit()) reparePermissions(calCategoryNode, username) ;
    }else {
      calCategoryNode = calCategoryHome.getNode(calendarCategory.getId()) ;
    }
    calCategoryNode.setProperty("exo:name", calendarCategory.getName()) ;
    calCategoryNode.setProperty("exo:description", calendarCategory.getDescription()) ;
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

  public void saveEventCategory(SessionProvider sProvider, String username, EventCategory eventCategory, String[] values, boolean isNew) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(sProvider, username) ;
    Node eventCategoryNode = null ;
    String name = null ;
    String description = null ;
    if(isNew){
      if(eventCategoryHome.hasNode(eventCategory.getName())) return ;
      eventCategoryNode = eventCategoryHome.addNode(eventCategory.getName(), "exo:eventCategory") ;
      name = eventCategory.getName() ;
      description = eventCategory.getDescription() ;
      if(eventCategory.isDataInit()) reparePermissions(eventCategoryNode, username) ;
    }else {
      eventCategoryNode = eventCategoryHome.getNode(eventCategory.getName()) ;
      if(eventCategory.getName().equals(values[0])) {
        name = eventCategory.getName() ;
        description = values[1] ;
      } else {
        if(eventCategoryHome.hasNode(values[0]))return ; 
        else {
          eventCategoryNode.remove() ;
          eventCategoryHome.addNode(values[0], "exo:eventCategory") ;
          name = values[0] ;
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
              eventNode.setProperty("exo:eventCategoryId", name) ;
            }
          }
          /*for(CalendarEvent ce : getUserEventByCategory(sProvider, username, eventCategory.getName())) {
            ce.setEventCategoryId(newEventCategory.getName()) ;
            saveUserEvent(sProvider, username,  ce.getCalendarId(), ce, false) ;
          }*/
        }
      }
      eventCategoryNode = eventCategoryHome.getNode(name) ;
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
      for(CalendarEvent ce : getSharedEventByCategory(username, eventCategoryName)) {
        removeSharedEvent(SessionProvider.createSystemProvider(), username, ce.getCalendarId(), ce.getId()) ;
      }
      for(CalendarEvent ce : getPublicEventByCategory(username, eventCategoryName)) {
        removePublicEvent(SessionProvider.createSystemProvider(),ce.getCalendarId(), ce.getId()) ;
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
    Node publicCalendarHome = getPublicCalendarHome(SessionProvider.createSystemProvider()) ;
    QueryManager qm = publicCalendarHome.getSession().getWorkspace().getQueryManager();
    List<CalendarEvent> events = new ArrayList<CalendarEvent> () ;
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
        events.add(getEvent(SessionProvider.createSystemProvider(), it.nextNode())) ;
      }
    }
    return events ;
  }
  private List<CalendarEvent> getSharedEventByCategory(String username, String eventCategoryId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome(SessionProvider.createSystemProvider()) ;
    QueryManager qm = sharedCalendarHome.getSession().getWorkspace().getQueryManager();
    List<CalendarEvent> events = new ArrayList<CalendarEvent> () ;
    Query query ;
    QueryResult result ;
    NodeIterator calIter = sharedCalendarHome.getNodes() ;
    while (calIter.hasNext()) {
      StringBuffer queryString = new StringBuffer("/jcr:root" + calIter.nextNode().getPath() 
          + "//element(*,exo:calendarEvent)[@exo:eventCategoryId='").
          append(eventCategoryId).
          append("']");
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      NodeIterator it = result.getNodes();
      while(it.hasNext()){
        events.add(getEvent(SessionProvider.createSystemProvider(), it.nextNode())) ;
      }
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
    Query query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    CalendarEvent calEvent ;
    while(it.hasNext()) {
      calEvent = getEvent(sProvider, it.nextNode()) ;
      // if(eventQuery.getFilterCalendarIds()== null || !Arrays.asList(eventQuery.getFilterCalendarIds()).contains(calEvent.getCalendarId())) {
      calEvent.setCalType("0") ;
      events.add(calEvent) ;
      // }
    }
    return events ;
  }

  public void saveUserEvent(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getUserCalendarHome(sProvider, username).getNode(calendarId);
    //Node eventFolder = getEventFolder(SessionProvider.createSystemProvider(), event.getFromDateTime()) ;
    if(event.getReminders() != null && event.getReminders().size() > 0) {
      //Need to use system session
      Node reminderFolder = getReminderFolder(SessionProvider.createSystemProvider(), event.getFromDateTime()) ;
      saveEvent(calendarNode, event, reminderFolder, isNew) ;
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
      removeReminder(sProvider, eventNode) ;
      eventNode.remove() ;
      calendarNode.save() ;
      calendarNode.getSession().save() ;
      calendarNode.refresh(true) ;
      Node eventFolder = getEventFolder(SessionProvider.createSystemProvider(), event.getFromDateTime()) ;
      syncRemoveEvent(eventFolder, eventId) ;
      return event;
    }
    return null ;
  }

  private void removeReminder(SessionProvider sProvider, Node eventNode)throws Exception {
    // Need to use system session
    if(eventNode.hasProperty("exo:fromDateTime")) {
      Node reminders = getReminderFolder(SessionProvider.createSystemProvider(), eventNode.getProperty("exo:fromDateTime").getDate().getTime()) ;
      if(reminders.hasNode(eventNode.getName())) reminders.getNode(eventNode.getName()).remove() ;
      Node events = reminders.getParent().getNode(CALENDAR_REMINDER) ;
      if(events != null && events.hasNode(eventNode.getName())) events.getNode(eventNode.getName()).remove() ;
      if(!reminders.isNew())reminders.save() ;
    }
  } 

  public CalendarEvent getGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception {
    Node calendarNode = getPublicCalendarHome(sProvider).getNode(calendarId) ;
    CalendarEvent calEvent = getEvent(sProvider, calendarNode.getNode(eventId)) ;
    calEvent.setCalType("2") ;
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
    Query query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    CalendarEvent calEvent ;
    while(it.hasNext()) {
      calEvent = getEvent(sProvider, it.nextNode()) ;
      //if(eventQuery.getFilterCalendarIds()== null || !Arrays.asList(eventQuery.getFilterCalendarIds()).contains(calEvent.getCalendarId())) {
      calEvent.setCalType("2") ;
      events.add(calEvent) ;
      //}
    }
    return events ;
  }
  public void savePublicEvent(SessionProvider sProvider, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getPublicCalendarHome(sProvider).getNode(calendarId) ;
    //Node eventFolder = getEventFolder(SessionProvider.createSystemProvider(), event.getFromDateTime()) ;
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
      Node eventFolder = getEventFolder(SessionProvider.createSystemProvider(), event.getFromDateTime()) ;
      syncRemoveEvent(eventFolder, eventId) ;
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

    event.setReminders(getReminders(SessionProvider.createSystemProvider(), eventNode)) ;
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
      try {
        eventNode = calendarNode.getNode(event.getId()) ;
      } catch (Exception e) {
        CalendarEvent tempEvent = new CalendarEvent() ;
        eventNode = calendarNode.addNode(tempEvent.getId(), "exo:calendarEvent") ;
        eventNode.setProperty("exo:id", tempEvent.getId()) ;
      }
      removeReminder(SessionProvider.createSystemProvider(), eventNode) ;
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
    addEvent(event) ;
  }
  private void addReminder(Node eventNode, Node reminderFolder, Reminder reminder) throws Exception {
    Node reminderNode ;
    Node catNode ;
    /*if(eventNode.hasNode(Utils.REMINDERS_NODE)) {
      reminders = eventNode.getNode(Utils.REMINDERS_NODE) ;
    } else {
      reminders = eventNode.addNode(Utils.REMINDERS_NODE, Utils.NT_UNSTRUCTURED) ;
    }*/
    try {
      catNode = reminderFolder.getNode(eventNode.getName()) ;
    } catch (Exception e) {
      catNode = reminderFolder.addNode(eventNode.getName(), Utils.NT_UNSTRUCTURED) ;
    }
    try {
      reminderNode = catNode.getNode(reminder.getId()) ;
    } catch (Exception e) {
      reminderNode = catNode.addNode(reminder.getId(), "exo:reminder") ;
    }
    reminderNode.setProperty("exo:eventId", eventNode.getName()) ;
    reminderNode.setProperty("exo:alarmBefore", reminder.getAlarmBefore()) ;
    reminderNode.setProperty("exo:repeatInterval", reminder.getRepeatInterval()) ;
    reminderNode.setProperty("exo:reminderType", reminder.getReminderType()) ;
    reminderNode.setProperty("exo:email", reminder.getEmailAddress()) ;
    reminderNode.setProperty("exo:isRepeat", reminder.isRepeat()) ;
    reminderNode.setProperty("exo:isOver", false) ;
    java.util.Calendar cal = new GregorianCalendar() ;
    if(reminder.getFromDateTime() != null) {
      cal.setTime(reminder.getFromDateTime()) ;
      reminderNode.setProperty("exo:fromDateTime", cal) ;
      long time = reminder.getFromDateTime().getTime() - (reminder.getAlarmBefore() * 60 * 1000) ;
      cal = new GregorianCalendar() ;
      cal.setTimeInMillis(time) ;
      reminderNode.setProperty("exo:remindDateTime", cal) ;
    }
    StringBuffer summary = new StringBuffer("Event      : ") ;
    summary.append(eventNode.getProperty("exo:summary").getString()).append("<br>") ;
    summary.append("Description: ") ;
    if(eventNode.hasProperty("exo:description"))
      summary.append(eventNode.getProperty("exo:description").getString());    	
    summary.append("<br>")  ;
    summary.append("Location   : ") ; 
    if(eventNode.hasProperty("exo:location")) 
      summary.append(eventNode.getProperty("exo:location").getString()) ;
    summary.append("<br>") ;
    cal.setTime(eventNode.getProperty("exo:fromDateTime").getDate().getTime()) ;
    summary.append("From       : ").append(cal.get(java.util.Calendar.HOUR_OF_DAY)).append(":") ;
    summary.append(cal.get(java.util.Calendar.MINUTE)).append(" - ") ;
    summary.append(cal.get(java.util.Calendar.DATE)).append("/") ;
    summary.append(cal.get(java.util.Calendar.MONTH)).append("/") ;
    summary.append(cal.get(java.util.Calendar.YEAR)).append("<br>") ;
    cal.setTime(eventNode.getProperty("exo:toDateTime").getDate().getTime()) ;
    summary.append("To         : ").append(cal.get(java.util.Calendar.HOUR_OF_DAY)).append(":") ;
    summary.append(cal.get(java.util.Calendar.MINUTE)).append(" - ") ;
    summary.append(cal.get(java.util.Calendar.DATE)).append("/") ;
    summary.append(cal.get(java.util.Calendar.MONTH) + 1).append("/") ;
    summary.append(cal.get(java.util.Calendar.YEAR)).append("<br>") ;
    reminderNode.setProperty("exo:eventSummary", summary.toString()) ;
    if(!reminderFolder.isNew()) reminderFolder.save() ;
    else reminderFolder.getSession().save() ;
  }

  private void addEvent(CalendarEvent event) throws Exception {
    Node eventFolder = getEventFolder(SessionProvider.createSystemProvider(), event.getFromDateTime()) ;
    Node publicEvent ;
    int fromDate ;
    int toDate ;
    syncRemoveEvent(eventFolder, event.getId()) ;
    CalendarEvent ev = new CalendarEvent() ;
    publicEvent = eventFolder.addNode(ev.getId(), "exo:calendarPublicEvent") ;
    publicEvent.setProperty("exo:id", ev.getId()) ;
    publicEvent.setProperty("exo:rootEventId", event.getId()) ;
    publicEvent.setProperty("exo:eventType", event.getEventType()) ;
    publicEvent.setProperty("exo:calendarId", event.getCalendarId()) ;
    java.util.Calendar dateTime = Utils.getInstanceTempCalendar() ;
    dateTime.setTime(event.getFromDateTime()) ;
    fromDate = dateTime.get(java.util.Calendar.DATE) ;
    publicEvent.setProperty("exo:fromDateTime", dateTime) ;
    publicEvent.setProperty("exo:eventState", event.getEventState()) ;
    dateTime.setTime(event.getToDateTime()) ;
    toDate = dateTime.get(java.util.Calendar.DATE) ;
    if(toDate > fromDate) {
      java.util.Calendar tmpTime = Utils.getInstanceTempCalendar() ;
      tmpTime.setTime(event.getFromDateTime()) ;
      tmpTime.set(java.util.Calendar.HOUR_OF_DAY, 0) ;
      tmpTime.set(java.util.Calendar.MINUTE, 0) ;
      tmpTime.set(java.util.Calendar.SECOND, 0) ;
      tmpTime.set(java.util.Calendar.MILLISECOND, 0) ;
      tmpTime.setTimeInMillis(tmpTime.getTimeInMillis() + (24 * 60 * 60 * 1000) - 1000) ;
      publicEvent.setProperty("exo:toDateTime", tmpTime) ;
    }else {
      publicEvent.setProperty("exo:toDateTime", dateTime) ;
    }

    publicEvent.setProperty("exo:participant", event.getParticipant()) ;
    try{
      if(!eventFolder.isNew()) eventFolder.save() ;
      else eventFolder.getSession().save() ;
    }catch(Exception e) {
      eventFolder.getSession().refresh(true) ;
      eventFolder.getSession().save() ;
      //e.printStackTrace() ;
    }
    if(toDate > fromDate) {
      java.util.Calendar cal = Utils.getInstanceTempCalendar() ;
      cal.setTime(event.getFromDateTime()) ;
      cal.set(java.util.Calendar.HOUR_OF_DAY, 0) ;
      cal.set(java.util.Calendar.MINUTE, 0) ;
      cal.set(java.util.Calendar.SECOND, 0) ;
      cal.set(java.util.Calendar.MILLISECOND, 0) ;

      for(int i = fromDate + 1; i <= toDate ; i++) {
        cal.roll(java.util.Calendar.DATE, true) ;
        Node dateFolder = getEventFolder(SessionProvider.createSystemProvider(), cal.getTime()) ;
        ev = new CalendarEvent() ;
        eventFolder.getSession().getWorkspace().copy(publicEvent.getPath(), dateFolder.getPath() + "/" + ev.getId()) ;
        dateFolder.getSession().save() ;
        if(i <= toDate) {
          Node newEvent = dateFolder.getNode(ev.getId()) ;
          newEvent.setProperty("exo:id", ev.getId()) ;
          newEvent.setProperty("exo:fromDateTime", cal) ;
          java.util.Calendar tmpCal = Utils.getInstanceTempCalendar() ;
          if(i == toDate) tmpCal.setTime(event.getToDateTime()) ;
          else tmpCal.setTimeInMillis(cal.getTimeInMillis() + (24 * 60 * 60 * 1000) - 1000) ;
          newEvent.setProperty("exo:toDateTime", tmpCal) ;
          newEvent.save() ;    			    			
        }    		
      }
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
    if(it.getSize() > 0) {
      while(it.hasNext()) {
        it.nextNode().remove() ;
      }
      eventFolder.getSession().save() ;
    }
  }
  
  private Node getReminderFolder(SessionProvider sysProvider, Date fromDate)throws Exception {
    Node publicApp = getPublicCalendarServiceHome(sysProvider) ;
    Node dateFolder = getDateFolder(publicApp, fromDate) ;
    try {
      return dateFolder.getNode(CALENDAR_REMINDER) ;
    } catch (Exception e) {
      dateFolder.addNode(CALENDAR_REMINDER, NT_UNSTRUCTURED) ;
      getPublicRoot(sysProvider).getSession().save() ;
      return dateFolder.getNode(CALENDAR_REMINDER) ;
    }
  }

  private Node getEventFolder(SessionProvider sysProvider, Date fromDate)throws Exception {
    Node publicApp = getPublicCalendarServiceHome(sysProvider) ;
    Node dateFolder = getDateFolder(publicApp, fromDate) ;
    try {
      return dateFolder.getNode(CALENDAR_EVENT) ;
    } catch (Exception e) {
      dateFolder.addNode(CALENDAR_EVENT, NT_UNSTRUCTURED) ;
      getPublicRoot(sysProvider).getSession().save() ;
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
      yearNode = publicApp.addNode(year, NT_UNSTRUCTURED) ;
    }
    try {
      monthNode = yearNode.getNode(month) ;
    } catch (Exception e) {
      monthNode = yearNode.addNode(month, NT_UNSTRUCTURED) ;
    }
    try {
      return monthNode.getNode(day) ;
    } catch (Exception e) {
      return monthNode.addNode(day, NT_UNSTRUCTURED) ;
    }
  }

  /*private Node getReminderHome(SessionProvider sProvider) throws Exception {
    Node calendarServiceHome  = getPublicCalendarServiceHome(sProvider) ;    
    if(calendarServiceHome.hasNode(CALENDAR_REMINDER)) return calendarServiceHome.getNode(CALENDAR_REMINDER) ;
    return calendarServiceHome.addNode(CALENDAR_REMINDER, NT_UNSTRUCTURED) ;
  }*/

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
    try {
      attachHome = eventNode.getNode(Utils.ATTACHMENT_NODE) ;
    } catch (Exception e) {
      attachHome = eventNode.addNode(Utils.ATTACHMENT_NODE, Utils.NT_UNSTRUCTURED) ;
    }
    String name = attachment.getId() ;
    if(!isNew) name = name.substring(attachment.getId().lastIndexOf("/")+1) ; 
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
          //attachment.setName(attchmentNode.getName()) ;
          Node contentNode = attchmentNode.getNode(Utils.JCR_CONTENT) ; 
          if(contentNode != null) {
            if(contentNode.hasProperty(Utils.JCR_LASTMODIFIED)) attachment.setLastModified(contentNode.getProperty(Utils.JCR_LASTMODIFIED).getDate()) ;
            if(contentNode.hasProperty(Utils.JCR_MIMETYPE)) attachment.setMimeType(contentNode.getProperty(Utils.JCR_MIMETYPE).getString()) ;
            if(contentNode.hasProperty(Utils.JCR_DATA)) {
              InputStream  inputStream = contentNode.getProperty(Utils.JCR_DATA).getStream() ;
              /*attachment.setInputStream(inputStream) ;*/
              attachment.setSize(inputStream.available()) ;
            }
            //attachment.setId(contentNode.getPath()) ;
          }
          attachment.setWorkspace(attchmentNode.getSession().getWorkspace().getName()) ;
          //System.out.println("\n\n attachment " + attachment.getWorkspace());
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
      settingNode = calendarHome.getNode(CALENDAR_SETTING) ; // TODO if node exists why we set properties anyway
    } catch (Exception e) {
      settingNode = calendarHome.addNode(CALENDAR_SETTING, "exo:calendarSetting") ;
    }
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
    settingNode.setProperty("exo:defaultPrivateCalendars", setting.getFilterPrivateCalendars()) ;
    settingNode.setProperty("exo:defaultPublicCalendars", setting.getFilterPublicCalendars()) ;
    settingNode.setProperty("exo:defaultSharedCalendars", setting.getFilterSharedCalendars()) ;
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
      if(settingNode.hasProperty("exo:baseURL")) calendarSetting.setBaseURL(settingNode.getProperty("exo:baseURL").getString()) ;
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
      if(settingNode.hasProperty("exo:defaultPrivateCalendars")){
        Value[] values = settingNode.getProperty("exo:defaultPrivateCalendars").getValues() ;
        String[] calendars = new String[values.length] ;
        for(int i = 0; i < values.length; i++) {
          calendars[i] = values[i].getString() ;
        }
        calendarSetting.setFilterPrivateCalendars(calendars) ;
      }
      if(settingNode.hasProperty("exo:defaultPublicCalendars")){
        Value[] values = settingNode.getProperty("exo:defaultPublicCalendars").getValues() ;
        String[] calendars = new String[values.length] ;
        for(int i = 0; i < values.length; i++) {
          calendars[i] = values[i].getString() ;
        }
        calendarSetting.setFilterPublicCalendars(calendars) ;
      }

      if(settingNode.hasProperty("exo:defaultSharedCalendars")){
        Value[] values = settingNode.getProperty("exo:defaultSharedCalendars").getValues() ;
        String[] calendars = new String[values.length] ;
        for(int i = 0; i < values.length; i++) {
          calendars[i] = values[i].getString() ;
        }
        calendarSetting.setFilterSharedCalendars(calendars) ;
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

  public int generateRss(SessionProvider sProvider ,String username, List<String> calendarIds, RssData rssData, 
      CalendarImportExport importExport) throws Exception {
    Node rssHomeNode = getRssHome(sProvider, username) ;
    Node iCalHome = null ;
    try {
      iCalHome = rssHomeNode.getNode("iCalendars") ;
    } catch (Exception e) {
      iCalHome = rssHomeNode.addNode("iCalendars", NT_UNSTRUCTURED) ;
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
        ids.clear() ;
        ids.add(calendarId) ;
        OutputStream out = importExport.exportCalendar(sProvider, username, ids, "0") ;
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
          description.setType("text/plain");
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
      WebDaveiCalHome = rssHomeNode.addNode("WebDavCalendars", NT_UNSTRUCTURED) ;
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
      List<String> ids = new ArrayList<String>();
      for(String calendarId : calendarIds) {        
        ids.clear() ;
        ids.add(calendarId) ;
        OutputStream out = importExport.exportCalendar(sProvider, username, ids, "0") ;
        if(out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
          try {
            WebDaveiCalHome.getNode(calendarId + ".ics").setProperty("exo:data", is) ;         
          } catch (Exception e) {
            Node ical = WebDaveiCalHome.addNode(calendarId + ".ics", "exo:iCalData") ;
            ical.setProperty("exo:data", is) ;
          }
          String link = rssData.getLink() + WebDaveiCalHome.getPath()+"/"+calendarId + ".ics" ;
          Calendar exoCal = getUserCalendar(sProvider, username, calendarId) ;
          entry = new SyndEntryImpl();
          entry.setTitle(exoCal.getName());                
          entry.setLink(link);     
          description = new SyndContentImpl();
          description.setType("text/plain");
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
    List<CalendarEvent> events = new ArrayList<CalendarEvent>()  ; 
    events.addAll(getUserEvents(sProvider, username, eventQuery)) ;
    events.addAll(getPublicEvents(SessionProvider.createSystemProvider(), eventQuery)) ;
    events.addAll(getSharedEvents(SessionProvider.createSystemProvider(), username, eventQuery)) ; 
    return new EventPageList(events, 10) ;    
  }

  public Map<Integer, String > searchHightLightEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception {
    Map<Integer, String > mapData = new HashMap<Integer, String>() ;
    Query query ;
    QueryManager qm ;
    SessionProvider systemSession = SessionProvider.createSystemProvider() ; 
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
    return mapData ;    
  }


  private Map<Integer, String> updateMap(Map<Integer, String> data, NodeIterator it, java.util.Calendar fromDate, java.util.Calendar toDate, String[] filterCalIds) throws Exception {
    int fromDayOfYear = fromDate.get(java.util.Calendar.DAY_OF_YEAR) ;
    int toDayOfYear = toDate.get(java.util.Calendar.DAY_OF_YEAR) ;
    boolean isVictory = false ;
    while(it.hasNext() && !isVictory) {
      Node eventNode = it.nextNode() ;
      if(filterCalIds == null || !Arrays.asList(filterCalIds).contains(eventNode.getProperty("exo:calendarId").getString())) {
        java.util.Calendar eventFormDate = eventNode.getProperty("exo:fromDateTime").getDate() ;
        java.util.Calendar eventToDate = eventNode.getProperty("exo:toDateTime").getDate() ;
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
  public void shareCalendar(SessionProvider sProvider, String username, String calendarId, List<String> receiverUsers) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome(sProvider) ;
    Node calendarNode = getUserCalendarHome(sProvider, username).getNode(calendarId) ;
    Value[] values = {};
    if (calendarNode.isNodeType(SHARED_MIXIN)) {     
      values = calendarNode.getProperty(SHARED_PROP).getValues();
    } else {
      calendarNode.addMixin(SHARED_MIXIN);     
    }

    SessionProvider spd = SessionProvider.createSystemProvider() ;
    Session systemSession = sharedCalendarHome.getSession() ;
    Node userNode ;
    List<Value> valueList = new ArrayList<Value>() ;
    for(String user : receiverUsers) {
      CalendarSetting calSetting = getCalendarSetting(spd, user) ;

      if(calSetting == null) calSetting = new CalendarSetting() ;
      Map<String, String> map = new HashMap<String, String> () ;
      for(String key : calSetting.getSharedCalendarsColors()) {
        map.put(key.split(":")[0], key.split(":")[1]) ;
      }
      if(map.get(calendarNode.getProperty("exo:id").getString()) == null)
        map.put(calendarNode.getProperty("exo:id").getString(), calendarNode.getProperty("exo:calendarColor").getString()) ;
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
      saveCalendarSetting(spd, user, calSetting) ;
      try {
        userNode = sharedCalendarHome.getNode(user) ;
      } catch (Exception e) {
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
        if(calendarNode.getProperty("exo:id").getString().equals(calendar.getId())) {
          Value[] values = calendarNode.getProperty(SHARED_PROP).getValues() ;
          for(Value value : values){
            if(!value.getString().equals(uuid)) {
              newValues.add(value) ;
            }
          }
          calendarNode.setProperty("exo:name", calendar.getName()) ;
          calendarNode.setProperty("exo:description", calendar.getDescription()) ;
          calendarNode.setProperty("exo:categoryId", calendar.getCategoryId()) ;
          calendarNode.setProperty("exo:viewPermissions", calendar.getViewPermission()) ;
          calendarNode.setProperty("exo:editPermissions", calendar.getEditPermission()) ;
          calendarNode.setProperty("exo:groups", calendar.getGroups()) ;
          calendarNode.setProperty("exo:locale", calendar.getLocale()) ;
          calendarNode.setProperty("exo:timeZone", calendar.getTimeZone()) ;
          //calendarNode.setProperty("exo:calendarColor", calendar.getCalendarColor()) ;
          //calendarNode.setProperty("exo:sharedColor", calendar.getCalendarColor()) ;
          CalendarSetting usCalSetting = getCalendarSetting(sProvider, username) ;
          Map<String, String> map = new HashMap<String, String> () ;
          for(String key : usCalSetting.getSharedCalendarsColors()) {
            map.put(key.split(":")[0], key.split(":")[1]) ;
          }
          map.put(calendar.getId(), calendar.getCalendarColor()) ;
          List<String> calColors = new ArrayList<String>() ;
          for(String key : map.keySet()) {
            calColors.add(key + ":" +map.get(key)) ;
          }
          calColors.add(calendar.getId()+":"+calendar.getCalendarColor());
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
          Query query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH) ;
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



  public void removeSharedCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome(sProvider) ;
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
        if(calendar.getProperty("exo:id").getString().equals(calendarId)) {
          map.remove(calendarId) ;
          Value[] values = calendar.getProperty(SHARED_PROP).getValues() ;
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
          calendar.setProperty(SHARED_PROP, newValues.toArray(new Value[newValues.size()])) ;
          calendar.save() ;
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
        if(calendar.getProperty("exo:id").getString().equals(calendarId)) {
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
    events.addAll(getSharedEvents(SessionProvider.createSystemProvider(), username, eventQuery)) ;
    events.addAll(getPublicEvents(SessionProvider.createSystemProvider(), eventQuery)) ;
    return events ;
  }


  public Map<String, String> checkFreeBusy(SessionProvider sysProvider, EventQuery eventQuery) throws Exception {
    Node eventFolder = getEventFolder(sysProvider, eventQuery.getFromDate().getTime()) ;
    Map<String, String> participantMap = new HashMap<String, String>() ;
    eventQuery.setCalendarPath(eventFolder.getPath()) ;
    eventQuery.setOrderBy(new String[]{"exo:fromDateTime"}) ;
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
        if(event.hasProperty("exo:eventState") && !CalendarEvent.ST_AVAILABLE.equals(event.getProperty("exo:eventState").getValue().getString()))
        {
          java.util.Calendar fromCal = event.getProperty("exo:fromDateTime").getDate() ;
          java.util.Calendar toCal = event.getProperty("exo:toDateTime").getDate() ;
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
        if(calendar.getProperty("exo:id").getString().equals(calendarId)) {
          if(calendar.hasNode(eventId)) {
            Node event = calendar.getNode(eventId) ;
            Node eventFolder = getEventFolder(SessionProvider.createSystemProvider(), event.getProperty("exo:fromDateTime").getDate().getTime()) ;
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

  public boolean hasEditPermission(SessionProvider sProvider, String sharedCalendarId, String username) throws Exception {
    if(getSharedCalendarHome(sProvider).hasNode(username)) {
      Node userNode = getSharedCalendarHome(sProvider).getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node calendar ;
      while(iter.hasNext()) {
        calendar = iter.nextProperty().getParent() ;
        if(calendar.getProperty("exo:id").getString().equals(sharedCalendarId)) {
          Value[] editValues = calendar.getProperty("exo:editPermissions").getValues() ;
          return Arrays.asList(editValues).contains(username) ;
        }
      }
    }  
    return false;
  }

  public void moveEvent(SessionProvider sProvider, String formCalendar, String toCalendar, String fromType, String toType, List<CalendarEvent> calEvents, String username) throws Exception {
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    switch (Integer.parseInt(fromType)) {
    case  Calendar.TYPE_PRIVATE :  
      if(getUserCalendarHome(sProvider, username).hasNode(formCalendar)) {
        switch (Integer.parseInt(toType)) {
        case Calendar.TYPE_PRIVATE:
          //move events in side private calendars
          if(getUserCalendarHome(sProvider, username).hasNode(toCalendar)){
            for(CalendarEvent calEvent : calEvents) {
              if(!formCalendar.equals(toCalendar)) {
                calEvent.setCalendarId(toCalendar) ;
                saveUserEvent(sProvider, username, toCalendar, calEvent, getUserCalendarHome(sProvider, username).getNode(toCalendar).hasNode(calEvent.getId())) ;
                removeUserEvent(sProvider, username, formCalendar, calEvent.getId()) ;
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
              calEvent.setCalendarId(toCalendar) ;
              saveEventToSharedCalendar(systemSession, username, toCalendar, calEvent, getSharedCalendarHome(systemSession).getNode(username).hasNode(calEvent.getId()));
              removeUserEvent(sProvider, username, formCalendar, calEvent.getId()) ;
            }
          }
          break;
        case Calendar.TYPE_PUBLIC:
          //move events form private to public calendar
          if(getPublicCalendarHome(systemSession).hasNode(toCalendar)){
            for(CalendarEvent calEvent : calEvents) {
              calEvent.setCalendarId(toCalendar) ;
              savePublicEvent(systemSession, toCalendar, calEvent, getPublicCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId())) ;
              removeUserEvent(systemSession, username, formCalendar, calEvent.getId()) ;
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
              calEvent.setCalendarId(toCalendar) ;
              saveUserEvent(sProvider, username, toCalendar, calEvent, getUserCalendarHome(sProvider, username).getNode(toCalendar).hasNode(calEvent.getId())) ;
              removeSharedEvent(systemSession, username, formCalendar, calEvent.getId()) ;
            }
          }
          break;
        case Calendar.TYPE_SHARED:
          //   move events in side shared calendars
          if(getSharedCalendarHome(systemSession).hasNode(username)){
            for(CalendarEvent calEvent : calEvents) {
              if(!formCalendar.equals(toCalendar)) {
                calEvent.setCalendarId(toCalendar) ;
                saveEventToSharedCalendar(systemSession, username, toCalendar, calEvent, getSharedCalendarHome(systemSession).getNode(username).hasNode(calEvent.getId()));
                removeSharedEvent(systemSession, username, formCalendar, calEvent.getId()) ;
              } else {
                saveEventToSharedCalendar(systemSession, username, toCalendar, calEvent, false);
              }
            }
          }
          break;
        case Calendar.TYPE_PUBLIC:
          //move events form share to public calendar
          if(getPublicCalendarHome(systemSession).hasNode(formCalendar)) {
            for(CalendarEvent calEvent : calEvents) {
              calEvent.setCalendarId(toCalendar) ;
              savePublicEvent(systemSession, toCalendar, calEvent, getPublicCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId())) ;
              removeSharedEvent(systemSession, username, formCalendar, calEvent.getId()) ;
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
              calEvent.setCalendarId(toCalendar) ;
              saveUserEvent(sProvider, username, toCalendar, calEvent, getUserCalendarHome(sProvider, username).getNode(toCalendar).hasNode(calEvent.getId())) ;
              removePublicEvent(systemSession, formCalendar, calEvent.getId()) ;
            }
          }
          break;
        case Calendar.TYPE_SHARED:
          //move events from public to shared calendar
          if(getSharedCalendarHome(systemSession).hasNode(username)){
            for(CalendarEvent calEvent : calEvents) {
              calEvent.setCalendarId(toCalendar) ;
              saveEventToSharedCalendar(systemSession, username, toCalendar, calEvent, getSharedCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId()));
              removePublicEvent(systemSession, formCalendar, calEvent.getId()) ;
            }
          }
          break;
        case Calendar.TYPE_PUBLIC:
          //move events in side public calendars
          if(getPublicCalendarHome(systemSession).hasNode(toCalendar)){
            for(CalendarEvent calEvent : calEvents) {
              if(!formCalendar.equals(toCalendar)) {
                calEvent.setCalendarId(toCalendar) ;
                savePublicEvent(systemSession, toCalendar, calEvent, getPublicCalendarHome(systemSession).getNode(toCalendar).hasNode(calEvent.getId())) ;
                removePublicEvent(systemSession, formCalendar, calEvent.getId()) ;
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
  }
  public void confirmInvitation(String fromUserId, String toUserId,int calType, String calendarId, String eventId, int answer) throws Exception{
    SessionProvider session = SessionProvider.createSystemProvider() ;
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
      if( Utils.DENY == answer) {
        pars.remove(toUserId) ;
      } if (Utils.ACCEPT == answer || Utils.NOTSURE == answer) {
        pars.put(toUserId, toUserId) ;
      }
      event.setParticipant(pars.values().toArray(new String[pars.values().size()]));
      if( Calendar.TYPE_PRIVATE == calType) {
        saveUserEvent(session, fromUserId, calendarId, event, false) ;
      } else  if(Calendar.TYPE_SHARED == calType)  {
        saveEventToSharedCalendar(session, fromUserId, calendarId, event, false) ;
      } else  if(Calendar.TYPE_PUBLIC == calType)  {
        savePublicEvent(session, calendarId, event, false) ;
      }
    }
  }
}


