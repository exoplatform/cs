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

import javax.jcr.RepositoryException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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

  public static final String USERNAME        = "userName";

  public static final String ACCOUNTID       = "acountId";

  public static final String FOLDERID        = "folderId";

  public static final String REPO_NAME       = "repository";
  
  private static Log         log             = ExoLogger.getLogger("cs.service.job");

  private String             username;

  private String             accountId;

  private String             folderId;

  public CheckMailJob() throws Exception {
  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    PortalContainer container = getPortalContainer(context);
    MailService mailService = (MailService) container.getComponentInstanceOfType(MailService.class);
    RepositoryService repoService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    String currentRepo = null;
    JobDetail jobDetail = context.getJobDetail();
    JobDataMap dataMap = jobDetail.getJobDataMap();
    
    //Using SessionProviderService to avoid JCR sessions leak
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    sessionProviderService.getSystemSessionProvider(null);
    
    try {
      currentRepo = repoService.getCurrentRepository().getConfiguration().getName();
    } catch (RepositoryException e) {
      log.warn("Can't get current repository name", e);
    }    
    username = dataMap.getString(USERNAME);
    accountId = dataMap.getString(ACCOUNTID);
    folderId = dataMap.getString(FOLDERID);
    
    String repoName = dataMap.getString(REPO_NAME);
    if (repoName != null) {
      try {
        repoService.setCurrentRepositoryName(repoName);
      } catch (RepositoryConfigurationException ex) {
        log.error(String.format("Can't set current repository name as %s", repoName), ex);
      }
    }
    try {
      if (username != null && accountId != null) {
        mailService.checkNewMessage(username, accountId, folderId);
      }
    } catch (InterruptedException ie) {
      log.warn("checking new mail message failed", ie);
      getMailService().stopCheckMail(username, accountId);
    } catch (Exception e) {
      log.error("checking new mail message failed", e);
      CheckingInfo info = null;
      try {
        info = mailService.getCheckingInfo(username, accountId);
      } catch (Exception e1) {
        log.error(e1);
      }
      if (info != null) {
        info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
        mailService.updateCheckingMailStatusByCometd(username, accountId, info);
      }
    } finally {
      if (currentRepo != null) {
        try {
          repoService.setCurrentRepositoryName(currentRepo);
        } catch (RepositoryConfigurationException e) {
          log.error(String.format("Can't set current repository name as %s", currentRepo), e);
        }
      }
      if (log.isDebugEnabled()) {
        log.debug("\n\n####  Checking mail of " + context.getJobDetail().getName() + " finished ");
      }
    }
    
    try {
      // remove SessionProvider
      sessionProviderService.removeSessionProvider(null);
    } catch (Exception e) {
      log.warn("An error occured while cleaning the ThreadLocal", e);
    }
    
  }

  private MailService getMailService() {
    MailService mailService = (MailService) PortalContainer.getInstance().getComponentInstanceOfType(MailService.class);
    return mailService;
  }

  private static String getJobName(String userId, String accountId) {
    return userId + ":" + accountId;
  }

  public static JobInfo getJobInfo(String userId, String accountId) {
    String name = getJobName(userId, accountId);
    JobInfo info = new JobInfo(name, CheckMailJob.CHECKMAIL_GROUP, CheckMailJob.class);
    info.setDescription("Check emails for user " + userId + " on acount " + accountId);
    return info;
  }

  public void interrupt() throws UnableToInterruptJobException {
    getMailService().stopCheckMail(username, accountId);
  }

  public static PortalContainer getPortalContainer(JobExecutionContext context) {
    if (context == null)
      return null;
    String portalName = context.getJobDetail().getGroup();
    if (portalName == null)
      return null;
    if (portalName.indexOf(":") > 0)
      portalName = portalName.substring(0, portalName.indexOf(":"));
    return RootContainer.getInstance().getPortalContainer(portalName);
  }
}
