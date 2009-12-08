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
package org.exoplatform.services.xmpp.connection.impl;

import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoryImpl;
import org.exoplatform.services.xmpp.util.HistoryUtils;
import org.jivesoftware.smack.packet.Message;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          vietnt84@gmail.com
 * Sep 30, 2009  
 */
public class HistoryJob implements Job {

  private static Log log = LogFactory.getLog("job.ChatRecordsJob");
  
  public void execute(JobExecutionContext context) throws JobExecutionException {
    SessionProvider provider = SessionProvider.createSystemProvider();
    try {
      JobDataMap jdatamap = context.getJobDetail().getJobDataMap();
      ExoContainer container = RootContainer.getInstance();
      container = ((RootContainer)container).getPortalContainer(jdatamap.getString("portalName"));
      int logBatchSize = Integer.parseInt(jdatamap.getString("logBatchSize"));
      HistoryImpl historyImpl = (HistoryImpl)container.getComponentInstanceOfType(HistoryImpl.class);
      
      Message message;
      boolean success;
      Queue<Message> logQueue = historyImpl.getLogQueue();
      for (int index = 0; index <= logBatchSize && !logQueue.isEmpty(); index++) {
        message = logQueue.poll();
        if (message != null) {
            success = historyImpl.addHistoricalMessage(HistoryUtils.messageToHistoricalMessage(message), provider);
            if (!success) {
                logQueue.add(message);
            }
        }
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    finally {
      provider.close(); // release sessions
    }
    
  }

}
