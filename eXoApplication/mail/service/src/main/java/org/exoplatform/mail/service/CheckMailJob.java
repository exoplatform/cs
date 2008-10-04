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
package org.exoplatform.mail.service;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CheckMailJob implements Job {

  
	public static final String CHECKMAIL_GROUP = "CollaborationSuite-webmail";
  public static final String USERNAME = "userName";
  public static final String ACCOUNTID = "acountId";
  private static Log log = ExoLogger.getLogger("job.CheckMailJob");
  
  public CheckMailJob() throws Exception {
		
	}

  public void execute(JobExecutionContext context) throws JobExecutionException {
	  try {
		  //TODO : khdung
		  // getting service references from ExoContainer is risky ... this one is another thread.
		  // it's better (and of course correct) if we get static references.
		  MailService mailService = Utils.getMailService();
		  JobSchedulerService schedulerService = Utils.getJobSchedulerService();
		  
		  JobDetail jobDetail = context.getJobDetail();
		  JobDataMap dataMap = jobDetail.getJobDataMap();
		  
		  String username = dataMap.getString(USERNAME);
		  String accountId = dataMap.getString(ACCOUNTID);
		  if (username!= null && accountId !=null) {
		    mailService.checkNewMessage(SessionProvider.createSystemProvider(), username.trim(), accountId.trim()) ;
		  }
		  
		  String name = jobDetail.getName();
		  JobInfo info = new JobInfo(name, CHECKMAIL_GROUP, CheckMailJob.class);
		  
		  // Antipattern a job should not unregister himself
		  schedulerService.removeJob(info) ;


	  } catch (Exception e) {
		  log.error("Mail check failed for " + context.getJobDetail().getName(), e);
	  }
	  if (log.isDebugEnabled()) {
      log.debug("\n\n####  Checking mail of " + context.getJobDetail().getName() + " finished ");
	  }
  }
  
  private static String getJobName(String userId, String accountId) {
    return userId + ":" + accountId;
  }
  
  public static JobInfo getJobInfo(String userId, String accountId) {
    String name =  getJobName(userId, accountId);
    JobInfo info = new JobInfo(name, CheckMailJob.CHECKMAIL_GROUP, CheckMailJob.class);
    info.setDescription("Check emails for user " + userId + " on acount " + accountId);
    return info;
  }
  
}
