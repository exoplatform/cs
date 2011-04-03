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
 */
package org.exoplatform.services.xmpp.test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.xmpp.history.HistoricalMessage;
import org.exoplatform.services.xmpp.history.Interlocutor;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoricalMessageImpl;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoryImpl;
import org.exoplatform.services.xmpp.history.impl.jcr.InterlocutorImpl;
import org.exoplatform.services.xmpp.util.HistoryUtils;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
@ConfiguredBy( { @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"), @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.organization-configuration.xml"), @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.cs.eXoApplication.chat.service.test-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration2.xml") })
public class HistoryTest extends AbstractKernelTest {
  private Log log = ExoLogger.getLogger(this.getClass());
  protected SessionImpl          session;

  protected RepositoryImpl       repository;

  protected RepositoryService    repositoryService;

  protected PortalContainer      container;

  private HistoryImpl            historyImpl;

  private SessionProviderService spService;

  public void setUp() throws Exception {
    container = PortalContainer.getInstance();
    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/test/java/conf/login.conf");
    Identity identity = new Identity("root");
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    repository = (RepositoryImpl) repositoryService.getCurrentRepository();
    spService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    spService.setSessionProvider(null, SessionProvider.createAnonimProvider());
    historyImpl = (HistoryImpl) container.getComponentInstanceOfType(HistoryImpl.class);
    begin();
  }

  public void testSaveMessage() {
    log.info("==========================================================");
    log.info("Testing save message to history");
    log.info("==========================================================");
    assertNotNull(container);
    ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    Message message = new Message("root@localhost", Message.Type.chat);
    message.setBody("hello");
    message.setFrom("marry@localhost");
    log.info("Initial message:");
    log.info("----------------------------------------------------------");
    dumpMessage(message);
    log.info("----------------------------------------------------------");
    historyImpl.addHistoricalMessage(HistoryUtils.messageToHistoricalMessage(message), sessionProviderService.getSessionProvider(null));
    message = new Message("marry@localhost", Message.Type.chat);
    message.setBody("how are you?");
    message.setFrom("root@localhost");
    log.info("Initial message:");
    log.info("----------------------------------------------------------");
    dumpMessage(message);
    log.info("--------------------------------------------------");
    historyImpl.addHistoricalMessage(HistoryUtils.messageToHistoricalMessage(message), sessionProviderService.getSessionProvider(null));
  }

  public void testGetMessages() {
    log.info("==========================================================");
    log.info("Testing geting all messages from history");
    log.info("==========================================================");
    assertNotNull(container);
    ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    List<HistoricalMessage> list = historyImpl.getHistoricalMessages("root", "marry", false, sessionProviderService.getSessionProvider(null));
    int i = 1;
    for (HistoricalMessage historicalMessage : list) {
      HistoricalMessageImpl historicalMessageImpl = (HistoricalMessageImpl) historicalMessage;
      log.info("\nMessage " + i++);
      log.info("---------------------------------------------------------");
      dumpMessage(historicalMessageImpl);
    }
  }

  public void testGetMessagesByDate() {
    log.info("==========================================================");
    log.info("Testing geting messages from history by date ");
    log.info("==========================================================");
    assertNotNull(container);
    try {
      ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
      Long long1 = Calendar.getInstance().getTimeInMillis() - 600000; // 10 min
      Date dateFrom = new Date(long1);
      log.info("\n           date from: " + dateFrom);
      List<HistoricalMessage> list = historyImpl.getHistoricalMessages("root", "marry", true, dateFrom, sessionProviderService.getSessionProvider(null));
      int i = 1;
      for (HistoricalMessage historicalMessage : list) {
        HistoricalMessageImpl historicalMessageImpl = (HistoricalMessageImpl) historicalMessage;
        log.info("\nMessage " + i++);
        log.info("---------------------------------------------------------");
        dumpMessage(historicalMessageImpl);
      }
      long1 = Calendar.getInstance().getTimeInMillis() + 600000;
      dateFrom = new Date(long1);
      log.info("\n            date from: " + dateFrom);
      list = historyImpl.getHistoricalMessages("root", "marry", true, dateFrom, sessionProviderService.getSessionProvider(null));
      for (HistoricalMessage historicalMessage : list) {
        HistoricalMessageImpl historicalMessageImpl = (HistoricalMessageImpl) historicalMessage;
        log.info("\nMessage " + i++);
        log.info("---------------------------------------------------------");
        dumpMessage(historicalMessageImpl);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void testGetInterlocutors() {
    log.info("==========================================================");
    log.info("Testing geting interlocutors ");
    log.info("==========================================================");
    assertNotNull(container);
    ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    List<Interlocutor> list = historyImpl.getInterlocutors("root", sessionProviderService.getSessionProvider(null));
    log.info("---- User root ");
    for (Interlocutor interlocutor : list) {
      InterlocutorImpl interlocutorImpl = (InterlocutorImpl) interlocutor;
      log.info("Name: " + interlocutorImpl.getInterlocutorName());
      log.info("Conversation id: " + interlocutorImpl.getConversationId());
      log.info("Path in repository: " + interlocutorImpl.getPath());
    }
    list = historyImpl.getInterlocutors("marry", sessionProviderService.getSessionProvider(null));
    log.info("---- User marry ");
    for (Interlocutor interlocutor : list) {
      InterlocutorImpl interlocutorImpl = (InterlocutorImpl) interlocutor;
      log.info("Name: " + interlocutorImpl.getInterlocutorName());
      log.info("Conversation id:" + interlocutorImpl.getConversationId());
      log.info("Path in repository:" + interlocutorImpl.getPath());
    }
  }

  private void dumpMessage(HistoricalMessageImpl historicalMessageImpl) {
    log.info("From: " + historicalMessageImpl.getFrom());
    log.info("To:  " + historicalMessageImpl.getTo());
    log.info("Type: " + historicalMessageImpl.getType());
    log.info("Body: " + historicalMessageImpl.getBody());
    log.info("Date send: " + historicalMessageImpl.getDateSend());
    log.info("Recieve: " + historicalMessageImpl.getReceive());
    log.info("Path in repository: " + historicalMessageImpl.getPath());
  }

  private void dumpMessage(Message message) {
    log.info("From: " + message.getFrom());
    log.info("To:  " + message.getTo());
    log.info("Type: " + message.getType().name());
    log.info("Body: " + message.getBody());
  }

  protected void tearDown() throws Exception {
    if (session != null) {
      try {
        session.refresh(false);
      } catch (Exception e) {
      } finally {
        session.logout();
        container.stopContainer();
      }
    }
    end();
    super.tearDown();
  }

}
