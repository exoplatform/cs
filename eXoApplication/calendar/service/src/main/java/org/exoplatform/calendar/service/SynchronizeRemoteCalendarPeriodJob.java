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

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.scheduler.PeriodJob;
import org.quartz.JobDataMap;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 10, 2011  
 */
public class SynchronizeRemoteCalendarPeriodJob extends PeriodJob {
  private JobDataMap jdatamap_ ;
  
  public SynchronizeRemoteCalendarPeriodJob(InitParams params) throws Exception {
    super(params);
    jdatamap_ = new JobDataMap() ;
  }
  
  public JobDataMap  getJobDataMap() {  
    return jdatamap_ ;  
  }
}
