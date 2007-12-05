/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.scheduler.PeriodJob;
import org.quartz.JobDataMap;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * nov 29, 2007
 */
public class ReminderPeriodJob extends  PeriodJob {
  private JobDataMap jdatamap_ ;
  public ReminderPeriodJob(InitParams params) throws Exception {
    super(params) ;
  	/*ExoProperties props =  params.getPropertiesParam("job.info").getProperties() ;
    
    String jobName = props.getProperty("jobName") ;
    String jobGroup= props.getProperty("groupName") ;
    String jobClass = props.getProperty("job") ;
    Class clazz = Class.forName(jobClass) ;
    jinfo_ = new JobInfo(jobName,jobGroup,clazz);
    
    Date startTime = getDate(props.getProperty("startTime")) ;
    Date endTime = getDate(props.getProperty("endTime"));
    int repeatCount = Integer.parseInt(props.getProperty("repeatCount")) ;
    long repeatInterval = Integer.parseInt(props.getProperty("period")) ;
    pjinfo_ = new PeriodInfo(startTime,endTime,repeatCount,repeatInterval) ;   */ 
    
    ExoProperties props =  params.getPropertiesParam("reminder.info").getProperties() ;
    jdatamap_ = new JobDataMap() ;
    String timeZone = props.getProperty("timeZone") ;
    String account = props.getProperty("account") ;
    String password = props.getProperty("password") ;
    String outgoingServer = props.getProperty("outgoing") ;
    String port = props.getProperty("port") ;
    jdatamap_.put("timeZone", timeZone) ;
    jdatamap_.put("account", account) ;
    jdatamap_.put("password", password) ;
    jdatamap_.put("outgoing", outgoingServer) ;
    jdatamap_.put("port", port) ;
 }
  public JobDataMap  getJobDataMap() {  return jdatamap_ ;  }
}