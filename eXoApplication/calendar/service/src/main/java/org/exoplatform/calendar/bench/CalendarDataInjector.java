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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.bench;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 3, 2011  
 */
public class CalendarDataInjector extends DataInjector {
  private static final Log       log                  = ExoLogger.getLogger(CalendarDataInjector.class);

  private static final String    EMPTY                = "".intern();

  private int                    maxCategories        = 3;

  private int                    maxEventCategories   = 3;

  private int                    maxCalendars         = 3;

  private int                    maxEvents            = 4;

  private int                    maxTasks             = 4;

  private String                 baseURL              = EMPTY;

  private String                 typeOfInject         = "all";

  private boolean                randomize            = false;

  private List<String>           categoryIdAddeds     = new ArrayList<String>();

  private CalendarService        calService;

  private String                 currentUser          = EMPTY;

  private CalendarSetting        setting              = new CalendarSetting();

  private List<EventCategory>    privateEventCategory = new ArrayList<EventCategory>();

  private List<EventCategory>    publicEventCategory  = new ArrayList<EventCategory>();

  private List<CalendarCategory> privateCategory      = new ArrayList<CalendarCategory>();

  private List<CalendarCategory> publicCategory       = new ArrayList<CalendarCategory>();

  private List<String>           name                 = new ArrayList<String>();

  private String[]               groupAdmin           = new String[] { EMPTY };

  private String[]               groupShare           = new String[] { EMPTY };

  public CalendarDataInjector(CalendarService calService, InitParams params) {
    initParams(params);
    this.calService = calService;
  }

  private void initDatas() {
    try {
      Identity identity = ConversationState.getCurrent().getIdentity();
      currentUser = identity.getUserId();
      groupShare = identity.getGroups().toArray(new String[] {});
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      UserACL userACL = (UserACL) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
      groupAdmin = new String[] { userACL.getAdminGroups() };
    } catch (Exception e) {
      log.debug("Failed to get groups admin of gatein.", e);
    }
  }

  @Override
  public Log getLog() {
    return log;
  }

  private int getParam(InitParams initParams, String param, int df) {
    try {
      return Integer.parseInt(initParams.getValueParam(param).getValue());
    } catch (Exception e) {
      return df;
    }
  }

  private boolean getParam(InitParams initParams, String param) {
    try {
      return Boolean.parseBoolean(initParams.getValueParam(param).getValue());
    } catch (Exception e) {
      return false;
    }
  }

  private String getParam(InitParams initParams, String param, String df) {
    try {
      return String.valueOf(initParams.getValueParam(param).getValue());
    } catch (Exception e) {
      return df;
    }
  }

  @Override
  public void initParams(InitParams initParams) {
    maxCategories = getParam(initParams, "mCt", maxCategories);
    maxEventCategories = getParam(initParams, "mEcat", maxEventCategories);
    maxCalendars = getParam(initParams, "mCal", maxCalendars);
    maxEvents = getParam(initParams, "mEv", maxEvents);
    maxTasks = getParam(initParams, "mTa", maxTasks);
    baseURL = getParam(initParams, "baseURL", baseURL);
    typeOfInject = getParam(initParams, "typeOfInject", typeOfInject);
    randomize = getParam(initParams, "rand");
  }

  @Override
  public void inject() throws Exception {
    initDatas();
    if ("all".equals(typeOfInject)) {
      initPublicCalendar();
      if (currentUser.length() > 0) {
        initPrivateCalendar();
      }
    } else if ("public".equals(typeOfInject)) {
      initPublicCalendar();
    } else if (currentUser.length() > 0) {
      initPrivateCalendar();
    }
  }

  @Override
  public boolean isInitialized() {
    return false;
  }

  @Override
  public void reject() throws Exception {

  }

  private void initPublicCalendar() throws Exception {

  }

  private void initPrivateCalendar() throws Exception {
    // save setting
    log.info("save setting for user " + currentUser + " .....");
    try {
      setting = calService.getCalendarSetting(currentUser);
    } catch (Exception e) {
      setting = newCalendarSetting();
      calService.saveCalendarSetting(currentUser, setting);
    }
    // save category
    log.info("save category .....");
    for (CalendarCategory cate : findCalendarCategorys()) {
      categoryIdAddeds.add(cate.getId());
      calService.saveCalendarCategory(currentUser, cate, true);
      privateCategory.add(cate);
    }
    // save EventCategoy
    log.info("save EventCategory ......");
    for (EventCategory evCat : findEventCategorys()) {
      calService.saveEventCategory(currentUser, evCat, true);
      privateEventCategory.add(evCat);
    }
    // save calendar
    log.info("save calendar ......");
    for (Calendar calendar : findCalendars()) {
      calService.saveUserCalendar(currentUser, calendar, true);
      // save Event
      log.info("save evets in calendar" + calendar.getId() + " ......");
      for (CalendarEvent event : findCalendarEvent(calendar.getId(), "0", CalendarEvent.TYPE_EVENT, false)) {
        calService.saveUserEvent(currentUser, calendar.getId(), event, true);
      }
      // save Task
      log.info("save tasks in calendar" + calendar.getId() + " ......");
      for (CalendarEvent event : findCalendarEvent(calendar.getId(), "0", CalendarEvent.TYPE_TASK, false)) {
        calService.saveUserEvent(currentUser, calendar.getId(), event, true);
      }
    }
  }

  private List<EventCategory> findEventCategorys() throws Exception {
    List<EventCategory> categories = new ArrayList<EventCategory>();
    int mCat = getMaxItem(maxEventCategories);
    name.clear();
    for (int i = 0; i < mCat; i++) {
      categories.add(newEventCategory());
    }
    return categories;
  }

  private List<CalendarCategory> findCalendarCategorys() throws Exception {
    List<CalendarCategory> categories = new ArrayList<CalendarCategory>();
    int mCat = getMaxItem(maxCategories);
    name.clear();
    for (int i = 0; i < mCat; i++) {
      categories.add(newCalendarCategory());
    }
    return categories;
  }

  private List<Calendar> findCalendars() throws Exception {
    List<Calendar> calendars = new ArrayList<Calendar>();
    int mCal = getMaxItem(maxCalendars);
    name.clear();
    for (int i = 0; i < mCal; i++) {
      calendars.add(newPrivateCalendar());
    }
    return calendars;
  }

  private List<CalendarEvent> findCalendarEvent(String calendarId, String CalType, String type, boolean isPublic) throws Exception {
    List<CalendarEvent> calendars = new ArrayList<CalendarEvent>();
    int mCe = (type.equals(CalendarEvent.TYPE_EVENT)) ? getMaxItem(maxEvents) : getMaxItem(maxTasks);
    name.clear();
    for (int i = 0; i < mCe; i++) {
      calendars.add(newCalendarEvent(calendarId, CalType, type, isPublic));
    }
    return calendars;
  }

  private int getMaxItem(int maxType) {
    Random rand = new Random(maxType);
    return (randomize) ? (rand.nextInt(maxType) + 1) : maxType;
  }

  private CalendarCategory newCalendarCategory() {
    CalendarCategory category = new CalendarCategory();
    category.setName(calRandomWords(10));
    category.setDescription(randomWords(20));
    return category;
  }

  private CalendarSetting newCalendarSetting() {
    CalendarSetting setting = new CalendarSetting();
    setting.setViewType("1");
    setting.setBaseURL(baseURL);
    setting.setWeekStartOn(String.valueOf(java.util.Calendar.MONDAY));
    setting.setWorkingTimeBegin("08:00");
    setting.setWorkingTimeEnd("18:00");
    setting.setShowWorkingTime(false);
    setting.setLocation("VNM");
    setting.setTimeZone("Asia/Bangkok");
    return setting;
  }

  private Calendar newPrivateCalendar() {
    Calendar calendar = new Calendar();
    calendar.setCalendarOwner(currentUser);
    calendar.setCategoryId(randomCategory().getId());
    calendar.setDataInit(true);
    calendar.setName(calRandomWords(10));
    calendar.setDescription(randomWords(20));
    calendar.setCalendarColor(getRandomColor());
    calendar.setEditPermission(new String[] { currentUser });
    calendar.setGroups(new String[] { EMPTY });
    calendar.setViewPermission(new String[] { currentUser });
    calendar.setPrivateUrl(EMPTY);
    calendar.setPublicUrl(EMPTY);
    calendar.setPublic(false);
    calendar.setLocale(setting.getLocation());
    calendar.setTimeZone(setting.getTimeZone());
    return calendar;
  }

  private Calendar newPublicCalendar() {
    Calendar calendar = new Calendar();
    calendar.setCalendarOwner(currentUser);
    calendar.setCategoryId(randomPublicCategory().getId());
    calendar.setDataInit(true);
    calendar.setName(calRandomWords(10));
    calendar.setDescription(randomWords(20));
    calendar.setCalendarColor(getRandomColor());
    calendar.setEditPermission(groupAdmin);
    calendar.setGroups(groupShare);
    calendar.setViewPermission(new String[] { "*.*" });
    calendar.setPrivateUrl(EMPTY);
    calendar.setPublicUrl(EMPTY);
    calendar.setPublic(true);
    calendar.setLocale("VNM");
    calendar.setTimeZone("Asia/Bangkok");
    return calendar;
  }

  private EventCategory newEventCategory() {
    EventCategory eventCategory = new EventCategory();
    eventCategory.setDataInit(true);
    eventCategory.setDescription(randomWords(20));
    eventCategory.setName(calRandomWords(10));
    return eventCategory;
  }

  private CalendarEvent newCalendarEvent(String calendarId, String CalType, String type, boolean isPublic) {
    CalendarEvent categoryEvent = new CalendarEvent();
    categoryEvent.setCalendarId(calendarId);
    categoryEvent.setCalType(CalType);
    categoryEvent.setDescription(randomWords(20));
    EventCategory eventCategory = (isPublic) ? randomEventCategory() : randomPrivateEventCategory();
    categoryEvent.setEventCategoryId(eventCategory.getId());
    categoryEvent.setEventCategoryName(eventCategory.getName());
    categoryEvent.setEventState(CalendarEvent.ST_BUSY);
    categoryEvent.setEventType(type);

    Date fromDate = randomDateTime(new Random().nextInt(360), 0);
    categoryEvent.setFromDateTime(fromDate);
    categoryEvent.setToDateTime(randomDateTime(new Random().nextInt(3), getOldTime(fromDate)));

    categoryEvent.setLocation(setting.getLocation());
    categoryEvent.setMessage(randomWords(30));

    categoryEvent.setExcludeId(new String[] { EMPTY });
    categoryEvent.setInvitation(new String[] { EMPTY });
    categoryEvent.setIsExceptionOccurrence(false);
    categoryEvent.setOriginalReference(EMPTY);
    categoryEvent.setParticipant(new String[] { EMPTY });
    categoryEvent.setParticipantStatus(new String[] { EMPTY });
    categoryEvent.setPriority(EMPTY);
    categoryEvent.setSendOption(EMPTY);
    categoryEvent.setStatus(EMPTY);
    categoryEvent.setTaskDelegator(EMPTY);

    categoryEvent.setSummary(calRandomWords(10));
    categoryEvent.setPrivate(!isPublic);
    return categoryEvent;
  }

  private String calRandomWords(int i) {
    String s = "qwertyuiopasdfghjkzxcvbnm";
    s = randomWords(i) + String.valueOf(s.charAt(new Random().nextInt(s.length())));
    if(name.contains(s)) return calRandomWords(i+1);
    else name.add(s);
    return s;
  }

  private String getRandomColor() {
    int l = Calendar.COLORS.length;
    return Calendar.COLORS[new Random().nextInt(l)];
  }

  private Date randomDateTime(int days, long oldTime) {
    java.util.Calendar currentCal = Utils.getGreenwichMeanTime();
    long time = 0;
    if (days == 0) {
      time = new Random(86400).nextLong() * 1000;
    } else {
      time = new Random(days).nextInt(days) * 86400000;
    }
    if (oldTime > 0) {
      time += oldTime;
    }
    currentCal.setTimeInMillis(currentCal.getTimeInMillis() + time);
    return currentCal.getTime();
  }

  private long getOldTime(Date date) {
    java.util.Calendar currentCal = Utils.getGreenwichMeanTime();
    return date.getTime() - currentCal.getTimeInMillis();
  }

  private CalendarCategory randomPublicCategory() {
    int i = publicCategory.size();
    return publicCategory.get(new Random().nextInt(i));
  }

  private CalendarCategory randomCategory() {
    int i = privateCategory.size();
    return privateCategory.get(new Random().nextInt(i));
  }

  private EventCategory randomPrivateEventCategory() {
    int i = privateEventCategory.size();
    return privateEventCategory.get(new Random().nextInt(i));
  }

  private EventCategory randomEventCategory() {
    int i = publicEventCategory.size();
    return publicEventCategory.get(new Random().nextInt(i));
  }

}
