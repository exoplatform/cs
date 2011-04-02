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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.xmpp.history.HistoricalMessage;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoryImpl;
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

  private static Log log = ExoLogger.getLogger("job.ChatRecordsJob");

  public void execute(JobExecutionContext context) throws JobExecutionException {
    PortalContainer container = getPortalContainer(context);
    if (container == null)
      return;
    ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
    ExoContainerContext.setCurrentContainer(container);
    SessionProvider provider = SessionProvider.createSystemProvider();
    try {
      JobDataMap jdatamap = context.getJobDetail().getJobDataMap();
      int logBatchSize = Integer.parseInt(jdatamap.getString("logBatchSize"));
      HistoryImpl historyImpl = (HistoryImpl) container.getComponentInstanceOfType(HistoryImpl.class);

      HistoricalMessage message;
      boolean success;
      Queue<HistoricalMessage> logQueue = historyImpl.getLogQueue();
      for (int index = 0; index <= logBatchSize && !logQueue.isEmpty(); index++) {
        message = logQueue.poll();
        if (message != null) {
          success = historyImpl.addHistoricalMessage(message, provider);
          if (!success) {
            logQueue.add(message);
          }
        }
      }
    } catch (Exception e) {
      log.error("An exception happened when saving chat message", e);
    } finally {
      provider.close(); // release sessions
      ExoContainerContext.setCurrentContainer(oldContainer);
    }

  }

  private static PortalContainer getPortalContainer(JobExecutionContext context) {
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
