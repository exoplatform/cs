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
package org.exoplatform.calendar.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.ServerConfiguration;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ReminderJob implements Job {


	final private static String CALENDAR_REMINDER = "reminders".intern();
	final private static String CALENDAR_APP = "CalendarApplication".intern() ;

	private static Log log_ = ExoLogger.getLogger("job.RecordsJob");
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<Message> messageList = new ArrayList<Message>();
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		try {
			MailService mailService = 
				(MailService) container.getComponentInstanceOfType(MailService.class);
			ContinuationService continuation = (ContinuationService) container.getComponentInstanceOfType(ContinuationService.class);
			if (log_.isDebugEnabled()) log_.debug("Calendar reminder service");
			java.util.Calendar fromCalendar = Calendar.getInstance() ; //org.exoplatform.calendar.service.Utils.getInstanceTempCalendar() ;
			//fromCalendar.setLenient(false) ;
			JobDataMap jdatamap = context.getJobDetail().getJobDataMap();
			ServerConfiguration config = new ServerConfiguration();
			config.setUserName(jdatamap.getString("account"));
			config.setPassword(jdatamap.getString("password"));
			config.setSsl(true);
			config.setOutgoingHost(jdatamap.getString("outgoing"));
			config.setOutgoingPort(jdatamap.getString("port"));

			Node calendarHome = getPublicServiceHome();
			if(calendarHome == null) return ;
			StringBuffer path = new StringBuffer(getReminderPath(fromCalendar));
			/*path.append("//element(*,exo:reminder)");
      path.append("[@exo:remindDateTime <= xs:dateTime('"	+ ISO8601.format(fromCalendar)
          + "') and @exo:isOver = 'false']");*/
			path.append("//element(*,exo:reminder)");
			path.append("[@exo:isOver = 'false']");
			QueryManager queryManager = calendarHome.getSession().getWorkspace().getQueryManager();
			Query query = queryManager.createQuery(path.toString(), Query.XPATH);
			System.out.println("\n\n query state  " + query.getStatement());
			QueryResult results = query.execute();
			NodeIterator iter = results.getNodes();
			Message message;
			Node reminder;
			List<Reminder> popupReminders = new ArrayList<Reminder>() ;
			while (iter.hasNext()) {
				reminder = iter.nextNode();
				boolean isRepeat = reminder.getProperty("exo:isRepeat").getBoolean() ;
				long fromTime = reminder.getProperty("exo:fromDateTime").getDate().getTimeInMillis() ;
				long remindTime = reminder.getProperty("exo:remindDateTime").getDate().getTimeInMillis() ;
				long interval = reminder.getProperty("exo:repeatInterval").getLong() * 60 * 1000 ;

				if(Reminder.TYPE_EMAIL.equals(reminder.getProperty("exo:reminderType").getString())){
					String to = reminder.getProperty("exo:email").getString();				
					if (to != null && to.length() > 0) {
						message = new Message();
						message.setContentType(Utils.MIMETYPE_TEXTHTML) ;
						message.setMessageTo(to);
						message.setSubject("[reminder] eXo calendar notify mail !");
						message.setMessageBody(reminder.getProperty("exo:eventSummary").getString());
						message.setFrom(jdatamap.getString("account")) ;
						if(isRepeat) {
							if (fromCalendar.getTimeInMillis() >= fromTime) {
								// reminder.setProperty("exo:isOver", true) ;
							}else {
								if((remindTime + interval) > fromTime) {
									// reminder.setProperty("exo:isOver", true) ;
								}else {
									java.util.Calendar cal = new GregorianCalendar() ;
									cal.setTimeInMillis(remindTime + interval) ;
									reminder.setProperty("exo:remindDateTime", cal) ;
									reminder.setProperty("exo:isOver", false) ;
								}
							}
						}else {
							//reminder.setProperty("exo:isOver", true) ;
						}
						messageList.add(message);
						reminder.save() ;
					}
				} else {
					Reminder rmdObj = new Reminder() ;
					rmdObj.setRepeate(isRepeat) ;
					if(reminder.hasProperty("exo:owner")) rmdObj.setReminderOwner(reminder.getProperty("exo:owner").getString()) ;
					if(reminder.hasProperty("exo:eventId")) rmdObj.setId(reminder.getProperty("exo:eventId").getString()) ;
					if(reminder.hasProperty("exo:owner")) rmdObj.setReminderOwner(reminder.getProperty("exo:owner").getString()) ;
					if(reminder.hasProperty("exo:remindDateTime")) {
						Calendar tempCal = reminder.getProperty("exo:remindDateTime").getDate() ;
						rmdObj.setFromDateTime(tempCal.getTime()) ;
					}
          if(reminder.hasProperty("exo:eventSummary")) rmdObj.setSummary(reminder.getProperty("exo:eventSummary").getString()) ;
					rmdObj.setAlarmBefore(remindTime) ; 
          if(reminder.hasProperty("exo:reminderType")) rmdObj.setReminderType(reminder.getProperty("exo:reminderType").getString()) ;
					if(isRepeat) {
						if (fromCalendar.getTimeInMillis() >= fromTime) {
							// reminder.setProperty("exo:isOver", true) ;
						}else {
							if((remindTime + interval) > fromTime) {
								//reminder.setProperty("exo:isOver", true) ;
							}else {
								java.util.Calendar cal = new GregorianCalendar() ;
								cal.setTimeInMillis(remindTime + interval) ;
								reminder.setProperty("exo:remindDateTime", cal) ;
								reminder.setProperty("exo:isOver", false) ;
							}
						}
					}else {
						//reminder.setProperty("exo:isOver", true) ;
					}
					popupReminders.add(rmdObj) ;
					reminder.save() ;
				}
			}
			if(!messageList.isEmpty()) mailService.sendMessages(messageList, config);
			if(!popupReminders.isEmpty()) {
				for(Reminder rmdObj : popupReminders) {
					JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
					JsonValue json = generatorImpl.createJsonObject(rmdObj);
					continuation.sendMessage(rmdObj.getReminderOwner(), "/eXo/Application/Calendar/messages", json);
					System.out.println("\n\n " + rmdObj.getReminderOwner() + " has a message ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		if (log_.isDebugEnabled()) log_.debug("File plan job done");
	}
	private String getReminderPath(java.util.Calendar fromCalendar)
	throws Exception {
		String year = "Y" + String.valueOf(fromCalendar.get(java.util.Calendar.YEAR));
		String month = "M" + String.valueOf(fromCalendar.get(java.util.Calendar.MONTH) + 1);
		String day = "D" + String.valueOf(fromCalendar.get(java.util.Calendar.DATE));
		StringBuffer path = new StringBuffer("/jcr:root");
		path.append(getPublicServiceHome().getPath());
		path.append("/").append(year).append("/").append(month).append("/").append(day);
		path.append("/").append(CALENDAR_REMINDER);
		return path.toString(); 
	}
	private Node getPublicServiceHome() throws Exception {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		NodeHierarchyCreator nodeHierarchyCreator  = (NodeHierarchyCreator) container
		.getComponentInstanceOfType(NodeHierarchyCreator.class);
		Node publicApp = nodeHierarchyCreator.getPublicApplicationNode(SessionProvider.createSystemProvider()) ;
		if(publicApp.hasNode(CALENDAR_APP)) return publicApp.getNode(CALENDAR_APP) ;
		return null ;		
	}
}
