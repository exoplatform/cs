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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.CalendarUpdateEventListener;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.ShareCalendarJob;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.impl.JobSchedulerServiceImpl;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.picocontainer.Startable;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleTrigger;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 11, 2007
 */
public class CalendarServiceImpl implements CalendarService, Startable {

  private ResourceBundle                      rb_;

  private ResourceBundleService               rbs_;

  private JCRDataStorage                      storage_;

  private Map<String, CalendarImportExport>   calendarImportExport_    = new LinkedHashMap<String, CalendarImportExport>();

  protected List<CalendarUpdateEventListener> listeners_               = new ArrayList<CalendarUpdateEventListener>(3);

  protected List<CalendarEventListener>       eventListeners_          = new ArrayList<CalendarEventListener>(3);

  public static final String                  MAX_USER_RUN_JOB_KEY     = "cs.calendar.sharing.max_user_run_job";
  
  public static final String                  MAX_USER_RUN_JOB_DEFAULT = "500";

  public CalendarServiceImpl(InitParams params,NodeHierarchyCreator nodeHierarchyCreator, RepositoryService reposervice, ResourceBundleService rbs) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator, reposervice);
    calendarImportExport_.put(CalendarService.ICALENDAR, new ICalendarImportExport(storage_));
    calendarImportExport_.put(CalendarService.EXPORTEDCSV, new CsvImportExport(storage_));
    rbs_ = rbs;
    ExoProperties props =  params.getPropertiesParam("eventNumber.info").getProperties() ;
    String eventNumber = props.getProperty("eventNumber");
    Utils.EVENT_NUMBER = Integer.parseInt(eventNumber);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarCategory> getCategories(String username) throws Exception {
    return storage_.getCategories(username);
  }

  /**
   * {@inheritDoc}
   */
  public List<GroupCalendarData> getCalendarCategories(String username, boolean isShowAll) throws Exception {
    return storage_.getCalendarCategories(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.getCalendarCategory(username, calendarCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    storage_.saveCalendarCategory(username, calendarCategory, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.removeCalendarCategory(username, calendarCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar getUserCalendar(String username, String calendarId) throws Exception {
    return storage_.getUserCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception {
    try {
      rb_ = rbs_.getResourceBundle(Utils.RESOURCEBUNDLE_NAME, Locale.getDefault()) ;
    } catch (MissingResourceException e) {
      //e.printStackTrace(); 
    }    
    return storage_.getUserCalendars(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.getUserCalendarsByCategory(username, calendarCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    storage_.saveUserCalendar(username, calendar, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception {
    return storage_.removeUserCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar getGroupCalendar(String calendarId) throws Exception {
    return storage_.getGroupCalendar(calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds,
                                                   boolean isShowAll,
                                                   String username) throws Exception {
    return storage_.getGroupCalendars(groupIds, isShowAll, username);
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception {
    storage_.savePublicCalendar(calendar, isNew, username);
  }
  
  public void savePublicCalendar(Calendar calendar, boolean isNew) throws Exception {
    storage_.savePublicCalendar(calendar, isNew, null);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar removePublicCalendar(String calendarId) throws Exception {
    return storage_.removeGroupCalendar(calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<EventCategory> getEventCategories(String username) throws Exception {
    return storage_.getEventCategories(username);
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventCategory(String username,
                                EventCategory eventCategory,
                                boolean isNew) throws Exception {
    EventCategory ev = getEventCategoryByName(username, eventCategory.getName());
    if (ev != null && (isNew || !ev.getId().equals(eventCategory.getId()))) throw new ItemExistsException(); 
    storage_.saveEventCategory(username, eventCategory, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventCategory(String username,
                                EventCategory eventCategory,
                                String[] values,
                                boolean isNew) throws Exception {
    saveEventCategory(username, eventCategory, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public void removeEventCategory(String username, String eventCategoryName) throws Exception {
    storage_.removeEventCategory(username, eventCategoryName);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception {
    return storage_.getUserEventByCalendar(username, calendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception {
    return storage_.getUserEvents(username, eventQuery);
  }

  public CalendarEvent getEvent(String username, String eventId) throws Exception {
    return storage_.getEvent(username, eventId) ;
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.saveUserEvent(username, calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception {
    return storage_.removeUserEvent(username, calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception {
    return storage_.getGroupEvent(calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception {
    return storage_.getGroupEventByCalendar(calendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception {
    return storage_.getPublicEvents(eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.savePublicEvent(calendarId, event, isNew);
    for(CalendarEventListener cel : eventListeners_) {
      if(isNew) cel.savePublicEvent(event, calendarId);
      else cel.updatePublicEvent(event, calendarId);
    }
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception {
    return storage_.removePublicEvent(calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarImportExport getCalendarImportExports(String type) {
    return calendarImportExport_.get(type);
  }

  /**
   * {@inheritDoc}
   */
  public String[] getExportImportType() throws Exception {
    return calendarImportExport_.keySet().toArray(new String[] {});
  }

  /**
   * {@inheritDoc}
   */
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception {
    storage_.saveCalendarSetting(username, setting);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarSetting getCalendarSetting(String username) throws Exception {
    return storage_.getCalendarSetting(username);
  }

  /**
   * {@inheritDoc}
   */
  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception {
    return storage_.generateRss(username,
                                calendars,
                                rssData,
                                calendarImportExport_.get(CalendarService.ICALENDAR));
  }

  /**
   * {@inheritDoc}
   */
  public int generateCalDav(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception {
    return storage_.generateCalDav(username,
                                   calendars,
                                   rssData,
                                   calendarImportExport_.get(CalendarService.ICALENDAR));
  }

  /**
   * {@inheritDoc}
   */
  public List<FeedData> getFeeds(String username) throws Exception {
    return storage_.getFeeds(username);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<FeedData> getUserFeeds(String username) throws Exception {
    return storage_.getUserFeeds(username);
  }

  /**
   * {@inheritDoc}
   */
  public Node getRssHome(String username) throws Exception {
    return storage_.getRssHome(username);
  }

  /**
   * {@inheritDoc}
   */
  public EventPageList searchEvent(String username, EventQuery query, String[] publicCalendarIds) throws Exception {
    return storage_.searchEvent(username, query, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public EventCategory getEventCategory(String username, String eventCategoryId) throws Exception {
    return storage_.getEventCategory(username, eventCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public Map<Integer, String> searchHightLightEvent(String username,
                                                    EventQuery eventQuery,
                                                    String[] publicCalendarIds) throws Exception {
    return storage_.searchHightLightEvent(username, eventQuery, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception {
    JobSchedulerServiceImpl  schedulerService_ = (JobSchedulerServiceImpl)PortalContainer.getInstance().getComponentInstance(JobSchedulerService.class) ;
    JobInfo jobInfo = ShareCalendarJob.getJobInfo(username);
    int maxUserRunJob = getMaxUserRunJob();
    if(findActiveShareClaJob(jobInfo.getJobName(), schedulerService_)){
      ContinuationService continuation = (ContinuationService) PortalContainer.getInstance().getComponentInstanceOfType(ContinuationService.class);
      continuation.sendMessage(username, "/eXo/Application/Calendar/notifySharaCalendar", ShareCalendarJob.STILL_SHARE_ID, ShareCalendarJob.STILL_SHARE_ID);
      return;
    }
    if(receiverUsers.size() > maxUserRunJob){
      SimpleTrigger trigger = new SimpleTrigger(jobInfo.getJobName(), jobInfo.getGroupName(), new Date());
      JobDetail job = new JobDetail(jobInfo.getJobName(), jobInfo.getGroupName(), jobInfo.getJob());
      job.setDescription(jobInfo.getDescription());
      job.getJobDataMap().put(ShareCalendarJob.RECEIVER_USER, receiverUsers);
      job.getJobDataMap().put(ShareCalendarJob.USER_NAME, username);
      job.getJobDataMap().put(ShareCalendarJob.CALENDAR_ID, calendarId);
      job.getJobDataMap().put(ShareCalendarJob.JCR_JATA_STORAGE, storage_);
      schedulerService_.addJob(job, trigger);
    }else{
      storage_.shareCalendar(username, calendarId, receiverUsers);
    }
  }
  
  /**
   * Checking a exist calendar job is running
   * @param userName
   * @param schedulerService_
   * @return boolean
   * @throws Exception
   */
  private boolean findActiveShareClaJob(String userName, JobSchedulerServiceImpl schedulerService_) throws Exception {
    List list = schedulerService_.getAllExcutingJobs();
    for (Object obj : list) {
      JobExecutionContext jec = (JobExecutionContext) obj;
      JobDetail tmp = jec.getJobDetail();
      if (tmp.getName().equals(userName)) {
        return true;
      }
    }
    return false;
  }
  
  private int getMaxUserRunJob(){
    Properties props = new Properties(System.getProperties());
    String maxUserStr = props.getProperty(MAX_USER_RUN_JOB_KEY);
    if(Utils.isEmpty(maxUserStr)){
      maxUserStr = MAX_USER_RUN_JOB_DEFAULT;
    }
    return Integer.parseInt(maxUserStr);
  }

  /**
   * {@inheritDoc}
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception {
    return storage_.getSharedCalendars(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getEvents(String username,
                                       EventQuery eventQuery,
                                       String[] publicCalendarIds) throws Exception {
    return storage_.getEvents(username, eventQuery, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception {
    storage_.removeSharedCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventToSharedCalendar(String username,
                                        String calendarId,
                                        CalendarEvent event,
                                        boolean isNew) throws Exception {
    storage_.saveEventToSharedCalendar(username, calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception {
    return storage_.checkFreeBusy(eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception {
    storage_.saveSharedCalendar(username, calendar);
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception {
    storage_.removeSharedEvent(username, calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public void moveEvent(String fromCalendar,
                        String toCalendar,
                        String fromType,
                        String toType,
                        List<CalendarEvent> calEvents,
                        String username) throws Exception {
    storage_.moveEvent(fromCalendar, toCalendar, fromType, toType, calEvents, username);
    if (fromType.equalsIgnoreCase(toType)
        && toType.equalsIgnoreCase(String.valueOf(Calendar.TYPE_PUBLIC))
        && fromCalendar.equalsIgnoreCase(toCalendar)) {
      for (CalendarEventListener cel : eventListeners_) {
        for (CalendarEvent event : calEvents)
          cel.updatePublicEvent(event, toCalendar);

      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void confirmInvitation(String fromUserId,
                                String toUserId,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception {
    storage_.confirmInvitation(fromUserId, toUserId, calType, calendarId, eventId, answer);
  }

  /**
   * {@inheritDoc}
   */
  public void confirmInvitation(String fromUserId,
                                String confirmingEmail,
                                String confirmingUser,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception {
    storage_.confirmInvitation(fromUserId, confirmingEmail, confirmingUser, calType, calendarId, eventId, answer);
  }

  public void start() {
    for (CalendarUpdateEventListener updateListener : listeners_) {
      updateListener.preUpdate();
    }
  }


  public void stop() {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void addListenerPlugin(CalendarUpdateEventListener listener) throws Exception {
    listeners_.add(listener);
  }

  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp) throws Exception {
    storage_.updateCalDav(usename, calendarId, imp) ;
  }

  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception {
    storage_.updateCalDav(usename, calendarId, imp, number) ;
  }

  public void updateRss(String usename, String calendarId, CalendarImportExport imp) throws Exception {
    storage_.updateRss(usename, calendarId, imp) ;

  }

  public void updateRss(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception {
    storage_.updateRss(usename, calendarId, imp, number) ;
  }

  public int getTypeOfCalendar(String userName, String calendarId) throws Exception {
    return storage_.getTypeOfCalendar(userName, calendarId);
  }

  public int generateCalDav(String username, List<String> calendarIds, RssData rssData) throws Exception {
    return storage_.generateCalDav(username,calendarIds, rssData, calendarImportExport_.get(CalendarService.ICALENDAR));
  }

  public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception {
    return storage_.generateRss(username,calendarIds, rssData, calendarImportExport_.get(CalendarService.ICALENDAR));
  }

  public ResourceBundle getResourceBundle() throws Exception {
    return rb_;
  }

  public EventCategory getEventCategoryByName(String username, String eventCategoryName) throws Exception {
    ResourceBundle rb = null ;
    try { 
      rb = rbs_.getResourceBundle(Utils.RESOURCEBUNDLE_NAME, Locale.getDefault()) ;
    } catch (MissingResourceException e) {
      //TODO the fist time load 
    }
    for (EventCategory ev : storage_.getEventCategories(username)) {
      if(ev.getName().equalsIgnoreCase(eventCategoryName)) {
        return ev ;
      } else if (rb != null) {
        try {
          if (eventCategoryName.equalsIgnoreCase(rb.getString("UICalendarView.label."+ev.getId()))) return ev ;
        } catch (MissingResourceException e) { }
      }
    }
    return null ;
  }
  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception {
    return storage_.getSharedEventByCalendars(username,calendarIds) ;
  }

  public void removeFeedData(String username, String title) {
    storage_.removeFeedData(username, title);
  }

  public void initNewUser(String userName, CalendarSetting defaultCalendarSetting_) throws Exception {
    EventCategory eventCategory = new EventCategory();
    eventCategory.setDataInit(true);
    for (int id = 0; id < NewUserListener.defaultEventCategoryIds.length; id ++) {
      eventCategory.setId(NewUserListener.defaultEventCategoryIds[id]);
      eventCategory.setName(NewUserListener.defaultEventCategoryNames[id]);
      saveEventCategory(userName, eventCategory, true);
    }

    // save default calendar category
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setId(NewUserListener.defaultCalendarCategoryId);
    calCategory.setName(NewUserListener.defaultCalendarCategoryName);
    calCategory.setDataInit(true) ;
    saveCalendarCategory(userName,	calCategory, true);

    // save default calendar
    Calendar cal = new Calendar();
    cal.setId(Utils.getDefaultCalendarId(userName));
    cal.setName(NewUserListener.defaultCalendarName);
    cal.setCategoryId(calCategory.getId());
    cal.setDataInit(true) ;
    cal.setCalendarOwner(userName) ;
    if(defaultCalendarSetting_ != null) {
      if(defaultCalendarSetting_.getLocation() != null)
        cal.setLocale(defaultCalendarSetting_.getLocation()) ;
      if(defaultCalendarSetting_.getTimeZone() != null)
        cal.setTimeZone(defaultCalendarSetting_.getTimeZone()) ;
    }
    saveUserCalendar(userName, cal,	true);

    if(defaultCalendarSetting_ != null) {
      saveCalendarSetting(userName, defaultCalendarSetting_) ;
    }
    OrganizationService organizationService = 
      (OrganizationService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class) ;
    Object[] groupsOfUser = organizationService.getGroupHandler().findGroupsOfUser(userName).toArray() ;
    List<String> groups = new ArrayList<String>();
    for (Object object : groupsOfUser) {
      String groupId = ((Group)object).getId() ;
      groups.add(groupId);
    }
    storage_.autoShareCalendar(groups, userName);
  }

  @Override
  public void addEventListenerPlugin(CalendarEventListener listener) throws Exception {
    eventListeners_.add(listener);    
  }

  @Override
  public void assignGroupTask(String taskId, String calendarId, String assignee) throws Exception {
    storage_.assignGroupTask(taskId, calendarId, assignee);
    
  }

  @Override
  public void setGroupTaskStatus(String taskId, String calendarId, String status) throws Exception {
    storage_.setGroupTaskStatus(taskId, calendarId, status);
  }
}
