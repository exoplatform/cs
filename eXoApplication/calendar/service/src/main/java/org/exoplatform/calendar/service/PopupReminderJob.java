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
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
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

public class PopupReminderJob implements Job {
  private static Log log_ = ExoLogger.getLogger("job.PopupRecordsJob");
  public void execute(JobExecutionContext context) throws JobExecutionException {
    SessionProvider provider = SessionProvider.createSystemProvider();
    try {
      if (log_.isDebugEnabled()) log_.debug("Calendar popup reminder service");
      java.util.Calendar fromCalendar = GregorianCalendar.getInstance() ;  
      JobDataMap jdatamap = context.getJobDetail().getJobDataMap();
      ExoContainer container = RootContainer.getInstance();
      container = ((RootContainer)container).getPortalContainer(jdatamap.getString("portalName"));
      ContinuationService continuation = (ContinuationService) container.getComponentInstanceOfType(ContinuationService.class);


      Node calendarHome = getPublicServiceHome(provider);
      if(calendarHome == null) return ;
      StringBuffer path = new StringBuffer(getReminderPath(fromCalendar, provider));
      path.append("//element(*,exo:reminder)");
      path.append("[@exo:remindDateTime <= xs:dateTime('"	+ ISO8601.format(fromCalendar)
          + "') and @exo:isOver = 'false' and @exo:reminderType = 'popup' ]"); 
      QueryManager queryManager = calendarHome.getSession().getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(path.toString(), Query.XPATH);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();
      Node reminder;
      List<Reminder> popupReminders = new ArrayList<Reminder>() ;
      while (iter.hasNext()) {
        reminder = iter.nextNode();
        boolean isRepeat = reminder.getProperty(Utils.EXO_IS_REPEAT).getBoolean() ;
        long fromTime = reminder.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTimeInMillis() ;
        long remindTime = reminder.getProperty(Utils.EXO_REMINDER_DATE).getDate().getTimeInMillis() ;
        long interval = reminder.getProperty(Utils.EXO_TIME_INTERVAL).getLong() * 60 * 1000 ;
          Reminder rmdObj = new Reminder() ;
          rmdObj.setRepeate(isRepeat) ;
          if(reminder.hasProperty(Utils.EXO_OWNER)) rmdObj.setReminderOwner(reminder.getProperty(Utils.EXO_OWNER).getString()) ;
          if(reminder.hasProperty(Utils.EXO_EVENT_ID)) rmdObj.setId(reminder.getProperty(Utils.EXO_EVENT_ID).getString()) ;
          if(reminder.hasProperty(Utils.EXO_FROM_DATE_TIME)) {
            Calendar tempCal = reminder.getProperty(Utils.EXO_FROM_DATE_TIME).getDate() ;
            rmdObj.setFromDateTime(tempCal.getTime()) ;
          }
          if(reminder.hasProperty(Utils.EXO_SUMMARY)) rmdObj.setSummary(reminder.getProperty(Utils.EXO_SUMMARY).getString()) ;
          rmdObj.setAlarmBefore(remindTime) ; 
          if(reminder.hasProperty(Utils.EXO_REMINDER_TYPE)) rmdObj.setReminderType(reminder.getProperty(Utils.EXO_REMINDER_TYPE).getString()) ;
          if(isRepeat) {
            if (fromCalendar.getTimeInMillis() >= fromTime) {
              reminder.setProperty(Utils.EXO_IS_OVER, true) ;
            }else {
              if((remindTime + interval) > fromTime) {
                reminder.setProperty(Utils.EXO_IS_OVER, true) ;
              }else {
                java.util.Calendar cal = new GregorianCalendar() ;
                cal.setTimeInMillis(remindTime + interval) ;
                reminder.setProperty(Utils.EXO_REMINDER_DATE, cal) ;
                reminder.setProperty(Utils.EXO_IS_OVER, false) ;
              }
            }
          }else {
            reminder.setProperty(Utils.EXO_IS_OVER, true) ;
          }
          popupReminders.add(rmdObj) ;
          reminder.save() ;
      }
      if(!popupReminders.isEmpty()) {
        for(Reminder rmdObj : popupReminders) {
          for(String user : rmdObj.getReminderOwner().split(Utils.COMMA)) {
            JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
            JsonValue json = generatorImpl.createJsonObject(rmdObj);
            continuation.sendMessage(user, "/eXo/Application/Calendar/messages", json, rmdObj.toString());
            System.out.println("\n\n " + user + " has a notify !");
          }
        }
      }
    } catch (Exception e) {
      System.out.println("\n\n Error when run popup reminder job !");
      //e.printStackTrace();			
    }  finally {
      provider.close(); // release sessions
    }
    if (log_.isDebugEnabled()) log_.debug("File plan job done");
  }
  private String getReminderPath(java.util.Calendar fromCalendar, SessionProvider provider)
  throws Exception {
    String year = "Y" + String.valueOf(fromCalendar.get(java.util.Calendar.YEAR));
    String month = "M" + String.valueOf(fromCalendar.get(java.util.Calendar.MONTH) + 1);
    String day = "D" + String.valueOf(fromCalendar.get(java.util.Calendar.DATE));
    StringBuffer path = new StringBuffer("/jcr:root");
    path.append(getPublicServiceHome(provider).getPath());
    path.append(Utils.SLASH).append(year).append(Utils.SLASH).append(month).append(Utils.SLASH).append(day);
    path.append(Utils.SLASH).append(Utils.CALENDAR_REMINDER);
    return path.toString(); 
  }
  private Node getPublicServiceHome(SessionProvider provider) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    NodeHierarchyCreator nodeHierarchyCreator  = (NodeHierarchyCreator) container
    .getComponentInstanceOfType(NodeHierarchyCreator.class);
    Node publicApp = nodeHierarchyCreator.getPublicApplicationNode(provider) ;
    if(publicApp != null && publicApp.hasNode(Utils.CALENDAR_APP)) return publicApp.getNode(Utils.CALENDAR_APP) ;
    return null ;		
  }
}
