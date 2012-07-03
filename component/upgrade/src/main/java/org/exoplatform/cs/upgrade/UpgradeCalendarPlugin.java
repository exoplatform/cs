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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Sep 12, 2011  
 */
public class UpgradeCalendarPlugin extends UpgradeProductPlugin {

  private static final String  SHARED_CALENDAR = "sharedCalendars";

  private static final String  FEED            = "eXoCalendarFeed";

  private static final String  SPACE_GROUP_ID  = "/spaces";

  private static final String  SPACE_CALENDAR_ID_SUFFIX = "_space_calendar";

  private static final Log     log             = ExoLogger.getLogger(UpgradeCalendarPlugin.class);

  private final RepositoryService    repoService_;

  private final NodeHierarchyCreator nodeHierarchy_;

  private final CalendarServiceImpl calendarService_;

  private final SessionProviderService sessionProviderService_;

  private final OrganizationService organizationService_;

  private SocialChromatticLifeCycle socialLifeCycle_;

  public UpgradeCalendarPlugin(InitParams initParams, RepositoryService repoService, NodeHierarchyCreator nodeHierarchy, 
                               CalendarService calendarService, SessionProviderService sessionProviderService, OrganizationService organizationService, ChromatticManager chromatticMan) {
    super(initParams);
    this.repoService_ = repoService;
    this.nodeHierarchy_ = nodeHierarchy;
    this.calendarService_ = (CalendarServiceImpl) calendarService;
    this.sessionProviderService_ = sessionProviderService;
    this.organizationService_ = organizationService;
    this.socialLifeCycle_ = (SocialChromatticLifeCycle) chromatticMan.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME) ;
  }
  public void processUpgrade(String oldVersion, String newVersion) {
    // Upgrade from CS 2.1.x to 2.2.x
    try {
      // Migrate calendar RSS
      migrateCalendarRSS();
      // Migrate space calendars
      migrateSpaceCalendars();
    } catch (Exception e) {
      log.warn("[UpgradeCalendarPlugin] Exception when migrate data from 2.1.x to 2.2.x for Calendar.", e);
    }
  }

  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isBefore(previousVersion, newVersion);
  }

  private Node getOldRssHome() throws Exception {
    SessionProvider sessionProvider = getSystemSessionProvider();
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

  private Node getPublicCalendarHome() throws Exception {
    return calendarService_.getDataStorage().getPublicCalendarHome();
  }

  private Node getUserCalendarAppHomeNode(String username) throws Exception {
    Node userNode = this.nodeHierarchy_.getUserApplicationNode(getSystemSessionProvider(), username);
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

  private SessionProvider getSystemSessionProvider() {
    return sessionProviderService_.getSystemSessionProvider(null);
  }

  private void migrateCalendarRSS() throws Exception {
    log.info("[UpgradeCalendarPlugin] Migrating calendar RSS ...");
    Node oldRssHome = getOldRssHome();
    if (oldRssHome != null) {
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
    } else {
      // There is not any calendar RSS to migrate
      log.info("[UpgradeCalendarPlugin] There is not any calendar RSS to migrate.");
    }
    log.info("[UpgradeCalendarPlugin] Finished migrate calendar RSS.");
  }

  private void migrateSpaceCalendars() throws Exception {
    log.info("[UpgradeCalendarPlugin] Migrating space calendars ...");
    Node publicCalendarHome = getPublicCalendarHome();
    NodeIterator iter = getSpaceCalendars();
    if (iter.getSize() > 0) {
      Map<String, String> spaceGroupIds = getSpaceGroups();
      while (iter.hasNext()) {

        Node calendarNode = iter.nextNode();
        List<String> spaceGroup = valuesToList(calendarNode.getProperty(Utils.EXO_GROUPS).getValues());
        String groupId = getGroupId(spaceGroup);

        String newId = groupId + SPACE_CALENDAR_ID_SUFFIX;
        String oldId = calendarNode.getProperty(Utils.EXO_ID).getString();
        migrateActivityCalendarId(oldId, newId);

        if (spaceGroupIds.containsKey(groupId) && !newId.equals(calendarNode.getName())) {
          calendarNode.setProperty(Utils.EXO_ID, newId);
          calendarNode.setProperty(Utils.EXO_NAME, spaceGroupIds.get(groupId));
          calendarNode.save();
          NodeIterator events = calendarNode.getNodes();
          if (events.getSize() > 0) {
            while (events.hasNext()) {
              Node event = events.nextNode();
              event.setProperty(Utils.EXO_CALENDAR_ID, newId);
              event.save();
            }
          }
          Session session = publicCalendarHome.getSession();
          session.move(calendarNode.getPath(), publicCalendarHome.getPath() + "/" + newId);
          session.save();
        }
      }
    } else {
      log.info("[UpgradeCalendarPlugin] There is not any space calendars to migrate.");
    }
    log.info("[UpgradeCalendarPlugin] Finished migrating space calendars. " + iter.getSize() + " calendars are migrated.");
  }

  private void migrateActivityCalendarId(String oldId, String newId) {
    try {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      Session jcrSession = socialLifeCycle_.getSession().getJCRSession();
      QueryManager qm = jcrSession.getWorkspace().getQueryManager() ;
      String sql = "select * from soc:activityparam where CalendarID='" + oldId + "'";
      Query q = qm.createQuery(sql, Query.SQL);
      QueryResult result = q.execute();
      NodeIterator nodeIterator = result.getNodes();
      while(nodeIterator.hasNext()) {
        Node activityNode = nodeIterator.nextNode();
        activityNode.setProperty("CalendarID", newId);
        activityNode.save();
      }
      jcrSession.save();
      RequestLifeCycle.end();
      log.info("Succesfully migrated " + nodeIterator.getSize() + " nodes");
    } catch (NullPointerException ne) {
      log.error("Failed to get social session data !",ne);
    } catch (Exception e) {
      log.error("Failed to migrate Activity CalendarID",e);
    }
  }

  private NodeIterator getSpaceCalendars() throws Exception {
    Node node = getPublicCalendarHome();
    QueryManager qm = node.getSession().getWorkspace().getQueryManager();
    StringBuilder strQuery = new StringBuilder("[jcr:like(@").append(Utils.EXO_ID).append(", 'CalendarInSpace%')]");
    StringBuilder pathQuery = new StringBuilder("/jcr:root").append(node.getPath())
        .append("//element(*,")
        .append(Utils.EXO_CALENDAR)
        .append(")")
        .append(strQuery)
        .append(" order by @")
        .append(Utils.EXO_ID)
        .append(" descending");
    Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  private Map<String, String> getSpaceGroups() throws Exception {
    Map<String, String> groupIds = new HashMap<String, String>();
    boolean started = false;
    try {
      RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
      started = true;
      Group group = organizationService_.getGroupHandler().findGroupById(SPACE_GROUP_ID);
      if (group != null) {
        Collection<Group> groups = organizationService_.getGroupHandler().findGroups(group);
        for (Group gr : groups) {
          groupIds.put(gr.getGroupName(), gr.getLabel());
        }
      }
    } catch (Exception e) {
      log.warn("\nFailed to get all space groups.", e);
    } finally {
      if (started) {
        RequestLifeCycle.end();
      }
    }
    return groupIds;
  }

  private String getGroupId(List<String> spaceGroups) throws Exception {
    for (String spaceGroup : spaceGroups) {
      if (spaceGroup.indexOf("/spaces/") >= 0) {
        spaceGroup = spaceGroup.substring(spaceGroup.lastIndexOf("/") + 1);
        if (!Utils.isEmpty(spaceGroup)) {
          return spaceGroup;
        }
      }
    }
    return "";
  }

  private List<String> valuesToList(Value[] values) throws Exception {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < values.length; i++) {
      String s = values[i].getString();
      if (!Utils.isEmpty(s)) {
        list.add(s);
      }
    }
    return list;
  }

}
