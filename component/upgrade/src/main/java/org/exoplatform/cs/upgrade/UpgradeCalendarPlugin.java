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
package org.exoplatform.cs.upgrade;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Sep 12, 2011  
 */
public class UpgradeCalendarPlugin extends UpgradeProductPlugin {

  private static final String  SHARED_CALENDAR = "sharedCalendars";

  private static final String  FEED            = "eXoCalendarFeed";
  
  private static final Log     log             = ExoLogger.getLogger(UpgradeCalendarPlugin.class);

  private RepositoryService    repoService_;

  private NodeHierarchyCreator nodeHierarchy_;

  public UpgradeCalendarPlugin(InitParams initParams) {
    super(initParams);
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    this.repoService_ = ((RepositoryService) container.getComponentInstance(RepositoryService.class));
    this.nodeHierarchy_ = ((NodeHierarchyCreator) container.getComponentInstance(NodeHierarchyCreator.class));
  }

  public void processUpgrade(String oldVersion, String newVersion) {
    // Upgrade from CS 2.1.x to 2.2.3
    try {
      Node oldRssHome = getOldRssHome();
      if (oldRssHome == null) {
        //There is nothing to migrate so return
        log.info("[UpgradeCalendarPlugin] There is nothing to migrate for Calendar.");
        return;
      }
      NodeIterator iterator = oldRssHome.getNodes();
      while (iterator.hasNext()) {
        Node feedNode = iterator.nextNode();
        String url = feedNode.getProperty(Utils.EXO_BASE_URL).getString();
        String username = url.substring(url.indexOf("/cs/calendar/feed/") + 18);
        username = username.substring(0, username.indexOf("/"));
        Node rssHome = getRssHome(username);
        url = url.substring(url.indexOf(":") + 3);
        url = url.substring(url.indexOf("/") + 1);
        url = url.substring(url.indexOf("/"));
        feedNode.setProperty(Utils.EXO_BASE_URL, url);
        feedNode.save();
        Session session = feedNode.getSession();
        session.move(feedNode.getPath(), rssHome.getPath() + "/" + feedNode.getName());
        session.save();
      }
    } catch (Exception e) {
      log.warn("[UpgradeCalendarPlugin] Exception when migrate data from 2.1.x to 2.2.3 for Calendar.", e);
    }
  }

  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    boolean doUpgrade = VersionComparator.isSame("0", previousVersion)
        && ((VersionComparator.isSame("2.2.3-SNAPSHOT", newVersion)) || (VersionComparator.isSame("2.2.3", newVersion)));
    return doUpgrade;
  }

  private Node getOldRssHome() throws Exception {
    SessionProvider sessionProvider = createSystemProvider();
    String oldRssHomePath = this.nodeHierarchy_.getPublicApplicationNode(sessionProvider).getPath()
        + "/" + Utils.CALENDAR_APP + "/" + SHARED_CALENDAR + "/" + FEED;
    try {
      return (Node) getSession(sessionProvider).getItem(oldRssHomePath);
    } catch (Exception e) {
      return null;
    }
  }

  private Node getRssHome(String username) throws Exception {
    Node calendarServiceHome = getUserCalendarAppHomeNode(username);
    Node feed;
    try {
      return calendarServiceHome.getNode(FEED);
    } catch (Exception e) {
      feed = calendarServiceHome.addNode(FEED, Utils.NT_UNSTRUCTURED);
      calendarServiceHome.getSession().save();
    }
    return feed;
  }

  private Node getUserCalendarAppHomeNode(String username) throws Exception {
    Node userNode = this.nodeHierarchy_.getUserApplicationNode(createSystemProvider(), username);
    try {
      return userNode.getNode(Utils.CALENDAR_APP);
    } catch (PathNotFoundException e) {
      return null;
    }
  }

  private Session getSession(SessionProvider sprovider) throws Exception {
    ManageableRepository currentRepo = this.repoService_.getCurrentRepository();
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);
  }

  private SessionProvider createSystemProvider() {
    SessionProviderService sessionProviderService = (SessionProviderService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SessionProviderService.class);
    return sessionProviderService.getSystemSessionProvider(null);
  }

}
