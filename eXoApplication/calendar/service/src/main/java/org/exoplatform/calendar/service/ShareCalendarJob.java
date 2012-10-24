/**
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
import java.util.List;

import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

/**
 * Created by The eXo Platform SARL 
 * Author : Haiddd 
 *          haidd@exoplatform.com 
 * May 11, 2012
 */

public class ShareCalendarJob implements Job, InterruptableJob {

  public static final String SHARE_CALENDAR_GROUP = "CS-ShareCalenar";

  public static final String RECEIVED_GROUPS        = "receivedGroups";

  public static final String USER_NAME            = "userName";

  public static final String CALENDAR_ID          = "calendarId";

  public static final String JCR_DATA_STORAGE     = "JCRDataStorage";

  public static final String START_SHARE_ID       = "StartSharing";

  public static final String FINISH_SHARE_ID      = "FinishSharing";

  public static final String STILL_SHARE_ID       = "StillSharing";

  public static final String SHARE_CAL_CHANEL     = "/eXo/Application/Calendar/notifyShareCalendar";

  private static Log         log                  = ExoLogger.getLogger("cs.service.job");

  public ShareCalendarJob() throws Exception {

  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Starting share calendar for group job.....");
    
    ContinuationService continuation = (ContinuationService) PortalContainer.getInstance()
        .getComponentInstanceOfType(ContinuationService.class);
    OrganizationService oService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    List<String> users = new ArrayList<String>();

    List<String> receivedGroups = (List<String>) jobDataMap.get(RECEIVED_GROUPS);
    String username = jobDataMap.getString(USER_NAME);
    String calendarId = jobDataMap.getString(CALENDAR_ID);
    JCRDataStorage jcrDataStorage = (JCRDataStorage) jobDataMap.get(JCR_DATA_STORAGE);
    continuation.sendMessage(username, SHARE_CAL_CHANEL, START_SHARE_ID, START_SHARE_ID);
    
    try {
      for(String groupName : receivedGroups) {
        for(User  user : oService.getUserHandler().findUsersByGroup(groupName).getAll()) {
          String sharedUser = user.getUserName();
          if(!sharedUser.equals(username)) {
            users.add(sharedUser);  
          }
        }
      }
      jcrDataStorage.shareCalendar(username, calendarId, users);
    } catch (Exception e) {
      log.debug("Exception in method:" + e);
    }
    continuation.sendMessage(username, SHARE_CAL_CHANEL, FINISH_SHARE_ID, FINISH_SHARE_ID);
    log.info("finish sharing calendar for group");
  }

  public static JobInfo getJobInfo(String userId) {
    JobInfo info = new JobInfo(userId,
                               ShareCalendarJob.SHARE_CALENDAR_GROUP,
                               ShareCalendarJob.class);

    info.setDescription("There are too many users");
    return info;
  }

  public void interrupt() throws UnableToInterruptJobException {
    log.debug("\n\n######### CALLED INTERRUPT!\n\n");
  }
}