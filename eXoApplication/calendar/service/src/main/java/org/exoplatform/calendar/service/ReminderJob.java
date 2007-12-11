package org.exoplatform.calendar.service;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.ServerConfiguration;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ReminderJob implements Job {
  

	final private static String CALENDAR_REMINDER = "reminders".intern();
	final private static String CALENDAR_APP = "CalendarApplication".intern() ;

	private static Log log_ = ExoLogger.getLogger("job.RecordsJob");
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// Session session = null ;
		try {
			ExoContainer container = ExoContainerContext.getCurrentContainer();
			MailService mailService = (MailService) container
					.getComponentInstanceOfType(MailService.class);
			if (log_.isDebugEnabled())
				log_.debug("Calendar reminder service");
			java.util.Calendar fromCalendar = new GregorianCalendar();
			JobDataMap jdatamap = context.getJobDetail().getJobDataMap();

			ServerConfiguration config = new ServerConfiguration();
			String timeZone = jdatamap.getString("timeZone");
			config.setUserName(jdatamap.getString("account"));
			config.setPassword(jdatamap.getString("password"));
			config.setSsl(true);
			config.setOutgoingHost(jdatamap.getString("outgoing"));
			config.setOutgoingPort(jdatamap.getString("port"));
			
			Node calendarHome = getPublicServiceHome();
			if(calendarHome == null) return ;
			StringBuffer path = new StringBuffer(getReminderPath(fromCalendar));
			path.append("//element(*,exo:reminder)");
			path.append("[@exo:remindDateTime <= xs:dateTime('"	+ ISO8601.format(fromCalendar)
					+ "') and @exo:reminderType = 'email' and @exo:isOver = 'true']");			
			QueryManager queryManager = calendarHome.getSession().getWorkspace().getQueryManager();
			Query query = queryManager.createQuery(path.toString(), Query.XPATH);
			QueryResult results = query.execute();
			NodeIterator iter = results.getNodes();
			System.out.println("\n\n\n >>>>>" + iter.getSize());
			Message message;
			Node reminder;
			List<Message> messageList = new ArrayList<Message>();
			while (iter.hasNext()) {
				reminder = iter.nextNode();
				String to = reminder.getProperty("exo:email").getString();				
				if (to != null && to.length() > 0) {
					message = new Message();
					message.setMessageTo(to);
					message.setSubject("eXo calendar reminder!");
					message.setMessageBody(reminder.getProperty("exo:eventSummary").getString());
					message.setFrom(jdatamap.getString("account")) ;
					if(reminder.getProperty("exo:isRepeat").getBoolean()) {
						long fromTime = reminder.getProperty("exo:fromDateTime").getDate().getTimeInMillis() ;
						long remindTime = reminder.getProperty("exo:remindDateTime").getDate().getTimeInMillis() ;
						long interval = reminder.getProperty("exo:repeatInterval").getLong() * 60 * 1000 ;
						if (fromCalendar.getTimeInMillis() >= fromTime) {
							reminder.setProperty("exo:isOver", true) ;
						}else {
							if((remindTime + interval) > fromTime) {
								reminder.setProperty("exo:isOver", true) ;
							}else {
								java.util.Calendar cal = new GregorianCalendar() ;
								cal.setTimeInMillis(remindTime + interval) ;
								reminder.setProperty("exo:remindDateTime", cal) ;
							}
						}
					}else {
						reminder.setProperty("exo:isOver", true) ;
					}
					messageList.add(message);
					reminder.save() ;
				}
			}
			if(messageList.size() > 0) mailService.sendMessages(messageList, config);
		} catch (Exception e) {
			e.printStackTrace();
			/*
			 * if(session != null) { session.logout(); }
			 */
		}
		if (log_.isDebugEnabled())
			log_.debug("File plan job done");
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
