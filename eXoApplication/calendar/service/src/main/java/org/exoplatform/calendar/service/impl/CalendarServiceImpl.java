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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 11, 2007
 */
public class CalendarServiceImpl implements CalendarService {

	final public static String ICALENDAR = "ICalendar(.ics)".intern();
	final public static String EXPORTEDCSV = "ExportedCsv(.csv)".intern();

	private JCRDataStorage storage_;
	private Map<String, CalendarImportExport> calendarImportExport_ = new LinkedHashMap<String, CalendarImportExport>();

	private static final Log log = ExoLogger
			.getLogger(CalendarServiceImpl.class);

	public CalendarServiceImpl(NodeHierarchyCreator nodeHierarchyCreator)
			throws Exception {
		storage_ = new JCRDataStorage(nodeHierarchyCreator);
		calendarImportExport_.put(ICALENDAR,
				new ICalendarImportExport(storage_));
		calendarImportExport_.put(EXPORTEDCSV, new CsvImportExport(storage_));
	}

	public List<CalendarCategory> getCategories(SessionProvider sProvider,
			String username) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getCategories(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<GroupCalendarData> getCalendarCategories(
			SessionProvider sProvider, String username, boolean isShowAll)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getCalendarCategories(sProvider, username,
					isShowAll);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public CalendarCategory getCalendarCategory(SessionProvider sProvider,
			String username, String calendarCategoryId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getCalendarCategory(sProvider, username,
					calendarCategoryId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveCalendarCategory(SessionProvider sProvider,
			String username, CalendarCategory calendarCategory, boolean isNew)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.saveCalendarCategory(sProvider, username,
					calendarCategory, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public CalendarCategory removeCalendarCategory(SessionProvider sProvider,
			String username, String calendarCategoryId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.removeCalendarCategory(sProvider, username,
					calendarCategoryId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Calendar getUserCalendar(SessionProvider sProvider, String username,
			String calendarId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getUserCalendar(sProvider, username, calendarId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Calendar> getUserCalendars(SessionProvider sProvider,
			String username, boolean isShowAll) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getUserCalendars(sProvider, username, isShowAll);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Calendar> getUserCalendarsByCategory(SessionProvider sProvider,
			String username, String calendarCategoryId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getUserCalendarsByCategory(sProvider, username,
					calendarCategoryId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveUserCalendar(SessionProvider sProvider, String username,
			Calendar calendar, boolean isNew) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.saveUserCalendar(sProvider, username, calendar, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Calendar removeUserCalendar(SessionProvider sProvider,
			String username, String calendarId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.removeUserCalendar(sProvider, username, calendarId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Calendar getGroupCalendar(SessionProvider sProvider,
			String calendarId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getGroupCalendar(sProvider, calendarId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<GroupCalendarData> getGroupCalendars(SessionProvider sProvider,
			String[] groupIds, boolean isShowAll, String username)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getGroupCalendars(sProvider, groupIds, isShowAll,
					username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void savePublicCalendar(SessionProvider sProvider,
			Calendar calendar, boolean isNew, String username) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.savePublicCalendar(sProvider, calendar, isNew, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Calendar removePublicCalendar(SessionProvider sProvider,
			String calendarId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.removeGroupCalendar(sProvider, calendarId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<EventCategory> getEventCategories(SessionProvider sProvider,
			String username) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getEventCategories(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<EventCategory> getSysEventCategories(String username) throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = createSystemProvider();
			return storage_.getEventCategories(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	
	
	public void saveEventCategory(SessionProvider sProvider, String username,
			EventCategory eventCategory, String[] values, boolean isNew)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.saveEventCategory(sProvider, username, eventCategory,
					values, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void removeEventCategory(SessionProvider sProvider, String username,
			String eventCategoryName) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_
					.removeEventCategory(sProvider, username, eventCategoryName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<CalendarEvent> getUserEventByCalendar(
			SessionProvider sProvider, String username, List<String> calendarIds)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getUserEventByCalendar(sProvider, username,
					calendarIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<CalendarEvent> getUserEvents(SessionProvider sProvider,
			String username, EventQuery eventQuery) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getUserEvents(sProvider, username, eventQuery);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveUserEvent(SessionProvider sProvider, String username,
			String calendarId, CalendarEvent event, boolean isNew)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.saveUserEvent(sProvider, username, calendarId, event,
					isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}
	public void saveUserEventSys(String username,
			String calendarId, CalendarEvent event, boolean isNew)
			throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = createSystemProvider();
			storage_.saveUserEvent(sProvider, username, calendarId, event,
					isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	

	public CalendarEvent removeUserEvent(SessionProvider sProvider,
			String username, String calendarId, String eventId)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.removeUserEvent(sProvider, username, calendarId,
					eventId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public CalendarEvent getGroupEvent(SessionProvider sProvider,
			String calendarId, String eventId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getGroupEvent(sProvider, calendarId, eventId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<CalendarEvent> getGroupEventByCalendar(
			SessionProvider sProvider, List<String> calendarIds)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getGroupEventByCalendar(sProvider, calendarIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<CalendarEvent> getPublicEvents(SessionProvider sProvider,
			EventQuery eventQuery) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getPublicEvents(sProvider, eventQuery);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void savePublicEvent(SessionProvider sProvider, String calendarId,
			CalendarEvent event, boolean isNew) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.savePublicEvent(sProvider, calendarId, event, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public CalendarEvent removePublicEvent(SessionProvider sProvider,
			String calendarId, String eventId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.removePublicEvent(sProvider, calendarId, eventId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public CalendarImportExport getCalendarImportExports(String type) {
		return calendarImportExport_.get(type);
	}

	public String[] getExportImportType() throws Exception {
		return calendarImportExport_.keySet().toArray(new String[] {});
	}

	public void saveCalendarSetting(SessionProvider sProvider, String username,
			CalendarSetting setting) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.saveCalendarSetting(sProvider, username, setting);
		} finally {
			closeSessionProvider(sProvider);
		}

	}

	public CalendarSetting getCalendarSetting(SessionProvider sProvider,
			String username) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getCalendarSetting(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public CalendarSetting getCalendarSettingSys(String username) throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getCalendarSetting(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	
	
	public int generateRss(SessionProvider sProvider, String username,
			List<String> calendarIds, RssData rssData) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.generateRss(sProvider, username, calendarIds,
					rssData, calendarImportExport_.get(ICALENDAR));
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public int generateCalDav(SessionProvider sProvider, String username,
			List<String> calendarIds, RssData rssData) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.generateCalDav(sProvider, username, calendarIds,
					rssData, calendarImportExport_.get(ICALENDAR));
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<FeedData> getFeeds(SessionProvider sProvider, String username)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getFeeds(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Node getRssHome(SessionProvider sProvider, String username)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getRssHome(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public EventPageList searchEvent(SessionProvider sProvider,
			String username, EventQuery query, String[] publicCalendarIds)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.searchEvent(sProvider, username, query,
					publicCalendarIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public EventCategory getEventCategory(SessionProvider sProvider,
			String username, String eventCategoryId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getEventCategory(sProvider, username,
					eventCategoryId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Map<Integer, String> searchHightLightEvent(
			SessionProvider sProvider, String username, EventQuery eventQuery,
			String[] publicCalendarIds) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.searchHightLightEvent(sProvider, username,
					eventQuery, publicCalendarIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void shareCalendar(SessionProvider sProvider, String username,
			String calendarId, List<String> receiverUsers) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.shareCalendar(sProvider, username, calendarId,
					receiverUsers);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public GroupCalendarData getSharedCalendars(SessionProvider sProvider,
			String username, boolean isShowAll) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getSharedCalendars(sProvider, username, isShowAll);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<CalendarEvent> getEvents(SessionProvider sProvider,
			String username, EventQuery eventQuery, String[] publicCalendarIds)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getEvents(sProvider, username, eventQuery,
					publicCalendarIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<CalendarEvent> getEventsSys(String username, EventQuery eventQuery, String[] publicCalendarIds)
			throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getEvents(sProvider, username, eventQuery,
					publicCalendarIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	
	
	public void removeSharedCalendar(SessionProvider sProvider,
			String username, String calendarId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.removeSharedCalendar(sProvider, username, calendarId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveEventToSharedCalendar(SessionProvider sProvider,
			String username, String calendarId, CalendarEvent event,
			boolean isNew) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveEventToSharedCalendar(sProvider, username, calendarId,
					event, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Map<String, String> checkFreeBusy(SessionProvider sysProvider,
			EventQuery eventQuery) throws Exception {
		try {
			sysProvider = resetSystemProvider(sysProvider);
			return storage_.checkFreeBusy(sysProvider, eventQuery);
		} finally {
			closeSessionProvider(sysProvider);
		}
	}

	public void saveSharedCalendar(SessionProvider sProvider, String username,
			Calendar calendar) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveSharedCalendar(sProvider, username, calendar);
		} finally {
			closeSessionProvider(sProvider);
		}

	}

	public void removeSharedEvent(SessionProvider sessionProvider,
			String username, String calendarId, String eventId)
			throws Exception {
		try {
			sessionProvider = resetSystemProvider(sessionProvider);
			storage_.removeSharedEvent(sessionProvider, username, calendarId,
					eventId);
		} finally {
			closeSessionProvider(sessionProvider);
		}

	}

	public void moveEvent(SessionProvider sProvider, String formCalendar,
			String toCalendar, String fromType, String toType,
			List<CalendarEvent> calEvents, String username) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.moveEvent(sProvider, formCalendar, toCalendar, fromType,
					toType, calEvents, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void confirmInvitation(String fromUserId, String toUserId,
			int calType, String calendarId, String eventId, int answer)
			throws Exception {
		storage_.confirmInvitation(fromUserId, toUserId, calType, calendarId,
				eventId, answer);
	}

	/**
	 * close and create a new SessionProvider
	 * @param provider
	 * @return
	 */
	private SessionProvider resetProvider(SessionProvider provider) {
		closeSessionProvider(provider);
		return createSessionProvider();
	}

	private SessionProvider resetSystemProvider(SessionProvider provider) {
		closeSessionProvider(provider);
		return createSystemProvider();
	}	
	
	private SessionProvider createSystemProvider() {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		SessionProviderService service = (SessionProviderService) container
				.getComponentInstanceOfType(SessionProviderService.class);
		return service.getSystemSessionProvider(null);
	}	

	/**
	 * Create a session provider for current context. The method first try to
	 * get a normal session provider, then attempts to create a system provider
	 * if the first one was not available.
	 * 
	 * @return a SessionProvider initialized by current SessionProviderService
	 * @see SessionProviderService#getSessionProvider(null)
	 */
	private SessionProvider createSessionProvider() {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		SessionProviderService service = (SessionProviderService) container
				.getComponentInstanceOfType(SessionProviderService.class);
		SessionProvider provider = service.getSessionProvider(null);
		if (provider == null) {
			log
					.info("No user session provider was available, trying to use a system session provider");
			provider = service.getSystemSessionProvider(null);
		}
		return provider;
	}

	/**
	 * Safely closes JCR session provider. Call this method in finally to clean
	 * any provider initialized by createSessionProvider()
	 * 
	 * @param sessionProvider
	 *            the sessionProvider to close
	 * @see SessionProvider#close();
	 */
	private void closeSessionProvider(SessionProvider sessionProvider) {
		if (sessionProvider != null) {
			sessionProvider.close();
		}
	}

}
