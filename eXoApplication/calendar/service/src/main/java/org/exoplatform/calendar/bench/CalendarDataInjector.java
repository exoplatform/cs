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
import java.util.Arrays;
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
  private static final Log       log                = ExoLogger.getLogger(CalendarDataInjector.class);

  private static final String    EMPTY              = "".intern();

  private int                    maxCategories      = 3;

  private int                    maxEventCategories = 3;

  private int                    maxCalendars       = 3;

  private int                    maxEvents          = 4;

  private int                    maxTasks           = 4;

  private String                 baseURL            = EMPTY;

  private String                 typeOfInject       = "all";

  private boolean                randomize          = true;

  private CalendarService        calService;

  private String                 currentUser        = EMPTY;

  private CalendarSetting        setting            = new CalendarSetting();

  private List<EventCategory>    eventCategory      = new ArrayList<EventCategory>();

  private List<CalendarCategory> categories         = new ArrayList<CalendarCategory>();

  private List<String>           publicCalendar     = new ArrayList<String>();

  private List<String>           privateCalendar    = new ArrayList<String>();

  private List<String>           name               = new ArrayList<String>();

  private List<Integer>          ints               = new ArrayList<Integer>();

  private String[]               groupAdmin         = new String[] { EMPTY };

  private String[]               groupShare         = new String[] { EMPTY };

  private Random                 rand               = new Random(1000);

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
      if (currentUser.length() > 0) {
        initPrivateCalendar();
      }
      initPublicCalendar();
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

  private void removePrivateData() throws Exception {
    try {
      log.info(String.format("removing private datas..... \n  removing %s calendars.....", privateCalendar.size()));
      for (String calId : privateCalendar) {
        calService.removeUserCalendar(currentUser, calId);
      }
      log.info(String.format("removing %s event catetories.....", eventCategory.size()));
      for (EventCategory evCat : eventCategory) {
        calService.removeEventCategory(currentUser, evCat.getId());
      }
      log.info(String.format("removing %s catetories.....", categories.size()));
      for (CalendarCategory cat : categories) {
        calService.removeCalendarCategory(currentUser, cat.getId());
      }
    } catch (Exception e) {
      log.debug("Failed to remove private injecter datas", e);
    }
  }

  private void removePublicData() throws Exception {
    try {
      log.info(String.format("remove public datas..... \n  removing %s calendars.....", publicCalendar.size()));
      for (String calId : publicCalendar) {
        calService.removePublicCalendar(calId);
      }
    } catch (Exception e) {
      log.debug("Failed to remove public injecter datas", e);
    }
  }

  @Override
  public void reject() throws Exception {
    setHistoryInject();
    if ("all".equals(typeOfInject)) {
      // remove public
      removePublicData();
      if (currentUser.length() > 0) {
        // remove private
        removePrivateData();
      }
    } else if ("public".equals(typeOfInject)) {
      // remove public
      removePublicData();
    } else if (currentUser.length() > 0) {
      // remove private
      removePrivateData();
    }
  }

  private void initPublicCalendar() throws Exception {
    // save public calendar
    for (Calendar calendar : findCalendars(true)) {
      calService.savePublicCalendar(calendar, true);
      publicCalendar.add(calendar.getId());
      // save event in public calendar
      for (CalendarEvent event : findCalendarEvent(calendar.getId(), "2", CalendarEvent.TYPE_EVENT, true)) {
        calService.savePublicEvent(calendar.getId(), event, true);
      }
      // save task in public calendar
      for (CalendarEvent event : findCalendarEvent(calendar.getId(), "2", CalendarEvent.TYPE_TASK, true)) {
        calService.savePublicEvent(calendar.getId(), event, true);
      }
    }
  }

  private void initPrivateCalendar() throws Exception {
    // save setting
    try {
      setting = calService.getCalendarSetting(currentUser);
    } catch (Exception e) {
      setting = newCalendarSetting();
      calService.saveCalendarSetting(currentUser, setting);
    }
    // save category
    for (CalendarCategory cate : findCalendarCategorys()) {
      calService.saveCalendarCategory(currentUser, cate, true);
      categories.add(cate);
    }
    // save EventCategoy
    for (EventCategory evCat : findEventCategorys()) {
      calService.saveEventCategory(currentUser, evCat, true);
      eventCategory.add(evCat);
    }
    // save calendar
    for (Calendar calendar : findCalendars(false)) {
      calService.saveUserCalendar(currentUser, calendar, true);
      privateCalendar.add(calendar.getId());
      // save Event
      for (CalendarEvent event : findCalendarEvent(calendar.getId(), "0", CalendarEvent.TYPE_EVENT, false)) {
        calService.saveUserEvent(currentUser, calendar.getId(), event, true);
      }
      // save Task
      for (CalendarEvent event : findCalendarEvent(calendar.getId(), "0", CalendarEvent.TYPE_TASK, false)) {
        calService.saveUserEvent(currentUser, calendar.getId(), event, true);
      }
    }
    saveHistoryInject();
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

  private List<Calendar> findCalendars(boolean isPublic) throws Exception {
    List<Calendar> calendars = new ArrayList<Calendar>();
    int mCal = getMaxItem(maxCalendars);
    name.clear();
    for (int i = 0; i < mCal; i++) {
      calendars.add((isPublic) ? newPublicCalendar() : newPrivateCalendar());
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
    return (randomize) ? (new Random(maxType + 1).nextInt(maxType) + 1) : maxType;
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
    setting.setTimeZone("Asia/Ho_Chi_Minh");
    return setting;
  }

  private Calendar newPrivateCalendar() {
    Calendar calendar = new Calendar();
    calendar.setCalendarOwner(currentUser);
    calendar.setCategoryId(randomCategory().getId());
    calendar.setDataInit(true);
    calendar.setName(calRandomWords(5));
    calendar.setDescription(randomWords(20));
    calendar.setCalendarColor(getRandomColor());
    calendar.setEditPermission(new String[] {});
    calendar.setGroups(new String[] {});
    calendar.setViewPermission(new String[] {});
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
    calendar.setDataInit(true);
    calendar.setName(calRandomWords(5));
    calendar.setDescription(randomWords(20));
    calendar.setCalendarColor(getRandomColor());
    calendar.setEditPermission(groupAdmin);
    calendar.setGroups(groupShare);
    calendar.setViewPermission(new String[] { "*.*" });
    calendar.setPrivateUrl(EMPTY);
    calendar.setPublicUrl(EMPTY);
    calendar.setPublic(true);
    calendar.setLocale("VNM");
    calendar.setTimeZone("Asia/Ho_Chi_Minh");
    return calendar;
  }

  private EventCategory newEventCategory() {
    EventCategory eventCategory = new EventCategory();
    eventCategory.setDataInit(true);
    eventCategory.setDescription(randomWords(20));
    eventCategory.setName(calRandomWords(5));
    return eventCategory;
  }

  private CalendarEvent newCalendarEvent(String calendarId, String CalType, String type, boolean isPublic) {
    CalendarEvent categoryEvent = new CalendarEvent();
    categoryEvent.setCalendarId(calendarId);
    categoryEvent.setCalType(CalType);
    categoryEvent.setDescription(randomWords(20));
    if (!isPublic) {
      EventCategory eventCategory = randomEventCategory();
      categoryEvent.setEventCategoryId(eventCategory.getId());
      categoryEvent.setEventCategoryName(eventCategory.getName());
    }
    categoryEvent.setEventState(CalendarEvent.ST_BUSY);
    categoryEvent.setEventType(type);

    Date fromDate = randomDateTime(new Random().nextInt(360), 0);
    categoryEvent.setFromDateTime(fromDate);
    categoryEvent.setToDateTime(randomDateTime(new Random().nextInt(4), getOldTime(fromDate)));

    categoryEvent.setLocation(setting.getLocation());
    categoryEvent.setMessage(randomWords(30));

    categoryEvent.setExcludeId(new String[] { EMPTY });
    categoryEvent.setInvitation(new String[] { EMPTY });
    categoryEvent.setIsExceptionOccurrence(false);
    categoryEvent.setOriginalReference(EMPTY);
    categoryEvent.setParticipant(new String[] { currentUser });
    categoryEvent.setParticipantStatus(new String[] { currentUser+":"});
    categoryEvent.setPriority(CalendarEvent.PRIORITY_NORMAL);
    categoryEvent.setSendOption(EMPTY);
    categoryEvent.setStatus(EMPTY);
    categoryEvent.setTaskDelegator(EMPTY);
    categoryEvent.setRepeatType(CalendarEvent.RP_NOREPEAT);
    
    categoryEvent.setSummary(calRandomWords(5));
    categoryEvent.setPrivate(!isPublic);
    return categoryEvent;
  }

  private String calRandomWords(int i) {
    String s = "qwertyuiopasdfghjkzxcvbnm";
    s = randomWords(i) + String.valueOf(s.charAt(new Random().nextInt(s.length())));
    if (name.contains(s)) {
      return calRandomWords(i + 1);
    } else {
      name.add(s);
    }
    return s;
  }

  private String getRandomColor() {
    int l = Calendar.COLORS.length;
    if (l == (ints.size() - 5))
      ints.clear();
    l = rand.nextInt(l);
    if (ints.contains(l)) {
      return getRandomColor();
    } else {
      ints.add(l);
    }
    return Calendar.COLORS[l];
  }

  private Date randomDateTime(int days, long oldTime) {
    java.util.Calendar currentCal = Utils.getGreenwichMeanTime();
    long time = 0;
    if (days == 0) {
      time = new Random(86400).nextLong() * 1000;
    } else {
      time = new Random().nextInt(days) * 86400000;
    }
    if (time <= 0){
      time = 720000;
    }
    if (oldTime > 0) {
      time += oldTime;
    }
    currentCal.setTimeInMillis(currentCal.getTimeInMillis() + time);
    return currentCal.getTime();
  }

  private long getOldTime(Date date) {
    return date.getTime() - Utils.getGreenwichMeanTime().getTimeInMillis();
  }

  private CalendarCategory randomCategory() {
    int i = categories.size();
    return categories.get(new Random().nextInt(i));
  }

  private EventCategory randomEventCategory() {
    int i = eventCategory.size();
    return eventCategory.get(new Random().nextInt(i));
  }

  private void saveHistoryInject() throws Exception {
    if (baseURL == null || baseURL.trim().length() == 0) {
      baseURL = publicCalendar.toString();
      baseURL += ";" + privateCalendar.toString();
      baseURL += ";[";
      for (CalendarCategory cat : categories) {
        baseURL += cat.getId() + ",";
      }
      baseURL += " ]";
      setting.setBaseURL(baseURL);
      calService.saveCalendarSetting(currentUser, setting);
    }
  }

  private void setHistoryInject() {
    if (privateCalendar.size() == 0) {
      initDatas();
      try {
        String s = calService.getCalendarSetting(currentUser).getBaseURL();
        String[] strs = s.split(";");
        publicCalendar.addAll(convertStringToList(strs[0]));
        privateCalendar.addAll(convertStringToList(strs[1]));
        CalendarCategory category = new CalendarCategory();
        for (String string : convertStringToList(strs[2])) {
          category.setId(string);
          categories.add(category);
        }
      } catch (Exception e) {
      }
    }
  }

  private List<String> convertStringToList(String s) {
    s = s.replace("[", "").replace("]", "");
    s = s.trim().replaceAll("(,\\s*)", ",").replaceAll("(\\s*,)", ",");
    String[] strs = s.split(",");
    return new ArrayList<String>(Arrays.asList(strs));
  }
}
