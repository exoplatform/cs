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

import java.util.List;

import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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
 * Author : Haiddd haidd@exoplatform.com 
 * May 11, 2012
 */

public class ShareCalendarJob implements Job, InterruptableJob {

  public static final String SHARE_CALENDAR_GROUP = "CS-ShareCalenar";

  public static final String RECEIVER_USER        = "receiverUsers";

  public static final String USER_NAME            = "userName";

  public static final String CALENDAR_ID          = "calendarId";

  public static final String JCR_JATA_STORAGE     = "JCRDataStorage";

  public static final String START_SHARE_ID       = "StartToShare";

  public static final String FINISH_SHARE_ID      = "FinishToShare";

  public static final String STILL_SHARE_ID       = "StillToShare";

  public static final String SHARE_CAL_CHANEL     = "/eXo/Application/Calendar/notifySharaCalendar";

  private static Log         log                  = ExoLogger.getLogger("cs.service.job");

  public ShareCalendarJob() throws Exception {

  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    ContinuationService continuation = (ContinuationService) PortalContainer.getInstance()
                                                                            .getComponentInstanceOfType(ContinuationService.class);
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    List<String> receiverUsers = (List<String>) jobDataMap.get(RECEIVER_USER);
    String user = jobDataMap.getString(USER_NAME);
    String calendarId = jobDataMap.getString(CALENDAR_ID);
    JCRDataStorage jcrDataStorage = (JCRDataStorage) jobDataMap.get(JCR_JATA_STORAGE);
    continuation.sendMessage(user, SHARE_CAL_CHANEL, START_SHARE_ID, START_SHARE_ID);
    try {
      jcrDataStorage.shareCalendar(user, calendarId, receiverUsers);
    } catch (Exception e) {
      log.debug("Exception in method:" + e);
    }
    continuation.sendMessage(user, SHARE_CAL_CHANEL, FINISH_SHARE_ID, FINISH_SHARE_ID);
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