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
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
//import org.exoplatform.ws.frameworks.cometd.ContinuationService;
//import org.exoplatform.ws.frameworks.json.JsonGenerator;
//import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
//import org.exoplatform.ws.frameworks.json.value.JsonValue;
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
      if (log_.isDebugEnabled()) log_.debug("Calendar reminder service");
      java.util.Calendar fromCalendar = org.exoplatform.calendar.service.Utils.getInstanceTempCalendar() ;
      fromCalendar.setLenient(false) ;
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
      path.append("//element(*,exo:reminder)");
      path.append("[@exo:remindDateTime <= xs:dateTime('"	+ ISO8601.format(fromCalendar)
          + "') and @exo:reminderType = 'email' and @exo:isOver = 'false']");
      // and @exo:reminderType = 'email' 
      QueryManager queryManager = calendarHome.getSession().getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(path.toString(), Query.XPATH);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();
      Message message;
      Node reminder;
      while (iter.hasNext()) {
      	try {
      		reminder = iter.nextNode();
          if(Reminder.TYPE_EMAIL.equals(reminder.getProperty("exo:reminderType").getString())){
            String to = reminder.getProperty("exo:email").getString();				
            if (to != null && to.length() > 0) {
              message = new Message();
              message.setContentType(Utils.MIMETYPE_TEXTHTML) ;
              message.setMessageTo(to);
              message.setSubject("[reminder] eXo calendar notify mail !");
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
                    reminder.setProperty("exo:isOver", false) ;
                  }
                }
              }else {
                reminder.setProperty("exo:isOver", true) ;
              }
              messageList.add(message);
              reminder.save() ;
            }
          }
      	}catch(Exception e) {
      		e.printStackTrace() ;
      	}
         
        /*else if(Reminder.TYPE_POPUP.equals(reminder.getProperty("exo:reminderType").getString())){
          Reminder rem = new Reminder(Reminder.TYPE_POPUP) ;
          rem.setSummary(reminder.getProperty("exo:eventSummary").getString());
          rem.setFromDateTime(reminder.getProperty("exo:fromDateTime").getDate().getTime()) ;
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
          reminders.add(rem);
          reminder.save() ;
        }*/
      }
      if(!messageList.isEmpty()) mailService.sendMessages(messageList, config);
    } catch (Exception e) {
      //e.printStackTrace();			
    }
    /*try{
      if(!reminders.isEmpty()) {
        JsonGenerator generatorImpl = new JsonGeneratorImpl();
        JsonValue json = generatorImpl.createJsonObject(new ReminderBean("root", reminders));
        ContinuationService continuation = (ContinuationService) container.getComponentInstanceOfType(ContinuationService.class);      
        if(continuation != null) continuation.sendMessage("root", "/eXo/Application/Calendar/messages", json);
        //System.out.println("\n\n json obj " + json.toString());
      }

    } catch (Exception e) {
      //e.printStackTrace() ;
    }*/
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
