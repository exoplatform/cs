/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.mail.service.test;

import java.util.Date;
import java.util.List;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.services.scheduler.Task;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 8, 2009  
 */
public class FakeJobSchedulerServiceImpl implements JobSchedulerService {

  public void addCronJob(JobInfo jinfo, String exp) throws Exception {

  }

  public void addCronJob(JobInfo jinfo, String exp, JobDataMap jdatamap) throws Exception {

  }

  public void addGlobalJobListener(ComponentPlugin plugin) throws Exception {

  }

  public void addGlobalTriggerListener(ComponentPlugin plugin) throws Exception {

  }

  public void addJob(JobInfo jinfo, Date date) throws Exception {

  }

  public void addJobListener(ComponentPlugin plugin) throws Exception {

  }

  public void addPeriodJob(JobInfo jinfo, PeriodInfo pinfo) throws Exception {

  }

  public void addPeriodJob(JobInfo jinfo, PeriodInfo pinfo, JobDataMap jdatamap) throws Exception {

  }

  public void addTriggerListener(ComponentPlugin plugin) throws Exception {

  }

  public void executeJob(String jname, String jgroup, JobDataMap jdatamap) throws Exception {

  }

  public List getAllExcutingJobs() throws Exception {
    return null;
  }

  public List getAllGlobalJobListener() throws Exception {
    return null;
  }

  public List getAllGlobalTriggerListener() throws Exception {
    return null;
  }

  public List getAllJobListener() throws Exception {
    return null;
  }

  public List getAllJobs() throws Exception {
    return null;
  }

  public List getAllTriggerListener() throws Exception {
    return null;
  }

  public JobListener getGlobalJobListener(String name) throws Exception {
    return null;
  }

  public TriggerListener getGlobalTriggerListener(String name) throws Exception {
    return null;
  }

  public JobDetail getJob(JobInfo jobInfo) throws Exception {
    return null;
  }

  public JobListener getJobListener(String name) throws Exception {
    return null;
  }

  public TriggerListener getTriggerListener(String name) throws Exception {
    return null;
  }

  public int getTriggerState(String triggerName, String triggerGroup) throws Exception {
    return 0;
  }

  public Trigger[] getTriggersOfJob(String jobName, String groupName) throws Exception {
    return null;
  }

  public void pauseJob(String jobName, String groupName) throws Exception {

  }

  public void queueTask(Task task) {

  }

  public boolean removeGlobaTriggerListener(String name) throws Exception {
    return false;
  }

  public boolean removeGlobalJobListener(String name) throws Exception {
    return false;
  }

  public boolean removeJob(JobInfo jinfo) throws Exception {
    return false;
  }

  public boolean removeJobListener(String name) throws Exception {
    return false;
  }

  public boolean removeTriggerListener(String name) throws Exception {
    return false;
  }

  public Date rescheduleJob(String triggerName, String groupName, Trigger newTrigger) throws Exception {
    return null;
  }

  public void resumeJob(String jobName, String groupName) throws Exception {

  }

}
