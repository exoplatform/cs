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
import org.apache.commons.logging.LogFactory;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.scheduler.JobInfo;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

public class CheckMailJob implements Job, InterruptableJob {

  
	public static final String CHECKMAIL_GROUP = "CollaborationSuite-webmail";
  public static final String USERNAME = "userName";
  public static final String ACCOUNTID = "acountId";
  public static final String FOLDERID = "folderId";
  private static Log  log = LogFactory.getLog("job.CheckMailJob");
  
  private String username;
  private String accountId;
  private String folderId;
  
  public CheckMailJob() throws Exception {
		
	}

  public void execute(JobExecutionContext context) throws JobExecutionException {
	    
	    MailService mailService = getMailService();
		  
		  JobDetail jobDetail = context.getJobDetail();
		  JobDataMap dataMap = jobDetail.getJobDataMap();
		  
		  username = dataMap.getString(USERNAME);
		  accountId = dataMap.getString(ACCOUNTID);
      folderId = dataMap.getString(FOLDERID);
	    try {
		  if (username!= null && accountId !=null) {
		    mailService.checkNewMessage(username, accountId, folderId) ;
		  }
    } catch (InterruptedException ie) {
      getMailService().stopCheckMail(username, accountId);
	  } catch (Exception e) {
		  log.error("Mail check failed for " + context.getJobDetail().getName(), e);
	  } finally {
  	  if (log.isDebugEnabled()) {
        log.debug("\n\n####  Checking mail of " + context.getJobDetail().getName() + " finished ");
  	  }
	  }
  }

  private MailService getMailService() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();	    
    MailService mailService = (MailService) container.getComponentInstanceOfType(MailService.class);
    //JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
    return mailService;
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

  public void interrupt() throws UnableToInterruptJobException {
    System.out.println("\n\n######### CALLED INTERRUPT!\n\n");
    getMailService().stopCheckMail(username, accountId);
  }
  
}
