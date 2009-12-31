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
package org.exoplatform.calendar.service;

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.scheduler.PeriodJob;
import org.quartz.JobDataMap;

/**
 * Created by The eXo Platform SAS
 * Author : tuan pham
 *          tuan.pham@exoplatform.com
 * May 14, 2009  
 */
public class AutoGeneratePeriodJob extends PeriodJob {
  private JobDataMap jdatamap_ ;
  public AutoGeneratePeriodJob(InitParams params, CalendarService calSvr) throws Exception {
    super(params) ;
    ExoProperties props =  params.getPropertiesParam("autogenerate.info").getProperties() ;
    jdatamap_ = new JobDataMap() ;
    String portalName = props.getProperty("portalName") ;
    jdatamap_.put("portalName", portalName) ;
    jdatamap_.put("event_number", props.getProperty("event_number")) ;
    jdatamap_.put("calendarservice", calSvr);
  }
  public JobDataMap  getJobDataMap() {  return jdatamap_ ;  }
}
