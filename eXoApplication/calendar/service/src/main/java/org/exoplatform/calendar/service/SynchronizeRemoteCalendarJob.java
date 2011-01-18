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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 10, 2011  
 */
public class SynchronizeRemoteCalendarJob implements Job {
  
  private static Log log_ = ExoLogger.getLogger("cs.calendar.job");
  
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    PortalContainer container = Utils.getPortalContainer(context);
    if(container == null) return;
    ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
    ExoContainerContext.setCurrentContainer(container);
    SessionProvider provider = SessionProvider.createSystemProvider();
    CalendarService calService = (CalendarService) container.getComponentInstanceOfType(CalendarService.class);
    int total = 0;
    int success = 0;
    int failed = 0;
    long start = System.currentTimeMillis();
    try {
      if (log_.isDebugEnabled()) log_.debug("Remote calendar synchronization service");
      RemoteCalendarService remoteCalendarService = calService.getRemoteCalendarService();
      
      // get list of remote calendar
      StringBuffer path = new StringBuffer("/jcr:root");
      path.append("//element(*,exo:remoteCalendar)");
      QueryManager queryManager = getSession(provider).getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(path.toString(), Query.XPATH);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();
      
      Node remoteCalendar;
      InputStream icalInputStream = null;
      
      // iterate over each calendar in this list, two case:
      while (iter.hasNext()) {
        total++;
        remoteCalendar = iter.nextNode();
        String remoteCalendarId = remoteCalendar.getProperty(Utils.EXO_ID).getString();
        String username = remoteCalendar.getProperty(Utils.EXO_CALENDAR_OWNER).getString();
        String remoteUrl = remoteCalendar.getProperty(Utils.EXO_REMOTE_URL).getString();
        String remoteType = remoteCalendar.getProperty(Utils.EXO_REMOTE_TYPE).getString();
        String remoteUser = remoteCalendar.getProperty(Utils.EXO_REMOTE_USERNAME).getString();
        String remotePassword = remoteCalendar.getProperty(Utils.EXO_REMOTE_PASSWORD).getString();
        String syncPeriod = remoteCalendar.getProperty(Utils.EXO_REMOTE_SYNC_PERIOD).getString();
        
        try {
          
          // case 1: if auto refresh calendar, do refresh this calendar
          if (syncPeriod.equals(Utils.SYNC_AUTO)) {          
            
            icalInputStream = remoteCalendarService.connectToRemoteServer(remoteUrl, remoteType, remoteUser, remotePassword);
            
            /*if (!remoteCalendarService.isValidate(icalInputStream)) {
              log_.debug("Error when parsing input stream of remote calendar " + remoteCalendarId +  ".The input stream is not in iCalendar format.");
              failed++;
              break;
            }*/
            
            // remove all components in local calendar
            List<String> calendarIds = new ArrayList<String>();
            calendarIds.add(remoteCalendarId);
            List<CalendarEvent> events = calService.getUserEventByCalendar(username, calendarIds);
            for (CalendarEvent event : events) {
              calService.removeUserEvent(username, remoteCalendarId, event.getId());
            }
            
            remoteCalendarService.importRemoteCalendar(username, remoteCalendarId, icalInputStream);
            calService.setRemoteCalendarLastUpdated(username, remoteCalendarId, Utils.getGreenwichMeanTime());
            success++;
          }
          else {
            long lastUpdate = remoteCalendar.getProperty(Utils.EXO_REMOTE_LAST_UPDATED).getDate().getTimeInMillis();           
            long now = Utils.getGreenwichMeanTime().getTimeInMillis();
            long interval = 0;
            if (Utils.SYNC_5MINS.equals(syncPeriod)) interval = 5 * 60 * 1000;
            if (Utils.SYNC_10MINS.equals(syncPeriod)) interval = 10 * 60 * 1000;
            if (Utils.SYNC_15MINS.equals(syncPeriod)) interval = 15 * 60 * 1000;
            if (Utils.SYNC_1HOUR.equals(syncPeriod)) interval = 60 * 60 * 1000;
            if (Utils.SYNC_1DAY.equals(syncPeriod)) interval = 24 * 60 * 60 * 1000;
            if (Utils.SYNC_1WEEK.equals(syncPeriod)) interval = 7 * 24 * 60 * 60 * 1000;
            if (Utils.SYNC_1YEAR.equals(syncPeriod)) interval = 365 * 7 * 24 * 60 * 60 * 1000;
            
            // if this remote calendar has expired
            if (lastUpdate + interval < now) {
              icalInputStream = remoteCalendarService.connectToRemoteServer(remoteUrl, remoteType, remoteUser, remotePassword);
              
              /*if (!remoteCalendarService.isValidate(icalInputStream)) {
                log_.debug("Error when parsing input stream of remote calendar " + remoteCalendarId +  ".The input stream is not in iCalendar format.");
                failed++;
                break;
              }*/
              
              // remove all components in local calendar
              List<String> calendarIds = new ArrayList<String>();
              calendarIds.add(remoteCalendarId);
              List<CalendarEvent> events = calService.getUserEventByCalendar(username, calendarIds);
              for (CalendarEvent event : events) {
                calService.removeUserEvent(username, remoteCalendarId, event.getId());
              }
              
              remoteCalendarService.importRemoteCalendar(username, remoteCalendarId, icalInputStream);
              calService.setRemoteCalendarLastUpdated(username, remoteCalendarId, Utils.getGreenwichMeanTime());
              success++;
            }
          }
        }
        catch (Exception e) {
          log_.debug("Skip this calendar, error when reload remote calendar " + remoteCalendarId +  ". Error message: " + e.getMessage());
          failed++;
          continue;
        }
      }      
    } 
    catch (RepositoryException e) {
      if (log_.isDebugEnabled()) log_.debug("Data base not ready!");
    } catch (Exception e) {
      e.printStackTrace() ;
      if (log_.isDebugEnabled()) log_.debug("Exception when synchronize remote calendar. " + e.toString());
    } finally {
      provider.close(); // release sessions
      ExoContainerContext.setCurrentContainer(oldContainer);
    }
    long finish = System.currentTimeMillis();
    long spent = (finish - start); 
    log_.info("Reload remote calendar completed. Total: " + total + ", Success: " + success + ", Failed: " + failed + ", Skip: " + (total-success-failed) + ". Time spent: " + spent + " ms.");
    
  }
  
  private Session getSession(SessionProvider sprovider) throws Exception{
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository currentRepo = repositoryService.getCurrentRepository() ;
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo) ;
  }

}
