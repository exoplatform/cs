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

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.services.xmpp.connection.impl.XMPPMessenger;
import org.exoplatform.services.xmpp.history.HistoricalMessage;
import org.exoplatform.services.xmpp.history.Interlocutor;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoricalMessageImpl;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoryImpl;
import org.exoplatform.services.xmpp.history.impl.jcr.InterlocutorImpl;
import org.exoplatform.services.xmpp.util.HistoryUtils;
import org.jivesoftware.smack.packet.Message;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class HistoryTest extends TestCase {

  protected SessionImpl          session;

  protected RepositoryImpl       repository;

  protected RepositoryService    repositoryService;

  protected StandaloneContainer  container;

  private HistoryImpl            historyImpl;

  private SessionProviderService spService;

  private XMPPMessenger          messenger;

  public void setUp() throws Exception {
    StandaloneContainer.addConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
    container = StandaloneContainer.getInstance();
    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config",
                         "src/test/java/conf/standalone/login.conf");
    Identity identity = new Identity("root");
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    repository = (RepositoryImpl) repositoryService.getDefaultRepository();
    messenger = (XMPPMessenger) container.getComponentInstanceOfType(XMPPMessenger.class);
    spService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    spService.setSessionProvider(null, SessionProvider.createAnonimProvider());
    historyImpl = (HistoryImpl) container.getComponentInstanceOfType(HistoryImpl.class);
  }

  public void testSaveMessage() {
    System.out.println("==========================================================");
    System.out.println("Testing save message to history");
    System.out.println("==========================================================");
    assertNotNull(container);
    ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    Message message = new Message("root", Message.Type.chat);
    message.setBody("hello");
    message.setFrom("marry");
    System.out.println("Initial message:");
    System.out.println("----------------------------------------------------------");
    dumpMessage(message);
    System.out.println("----------------------------------------------------------");
    historyImpl.addHistoricalMessage(HistoryUtils.messageToHistoricalMessage(message),
                                     sessionProviderService.getSessionProvider(null));
    message = new Message("marry", Message.Type.chat);
    message.setBody("how are you?");
    message.setFrom("root");
    System.out.println("Initial message:");
    System.out.println("----------------------------------------------------------");
    dumpMessage(message);
    System.out.println("--------------------------------------------------");
    historyImpl.addHistoricalMessage(HistoryUtils.messageToHistoricalMessage(message),
                                     sessionProviderService.getSessionProvider(null));
  }

  public void testGetMessages() {
    System.out.println("==========================================================");
    System.out.println("Testing geting all messages from history");
    System.out.println("==========================================================");
    assertNotNull(container);
    ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    List<HistoricalMessage> list = historyImpl.getHistoricalMessages("root",
                                                                     "marry",
                                                                     false,
                                                                     sessionProviderService.getSessionProvider(null));
    int i = 1;
    for (HistoricalMessage historicalMessage : list) {
      HistoricalMessageImpl historicalMessageImpl = (HistoricalMessageImpl) historicalMessage;
      System.out.println("\nMessage " + i++);
      System.out.println("---------------------------------------------------------");
      dumpMessage(historicalMessageImpl);
    }
  }

  public void testGetMessagesByDate() {
    System.out.println("==========================================================");
    System.out.println("Testing geting messages from history by date ");
    System.out.println("==========================================================");
    assertNotNull(container);
    try {
      ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
      Long long1 = Calendar.getInstance().getTimeInMillis() - 600000; // 10 min
      Date dateFrom = new Date(long1);
      System.out.println("\n           date from: " + dateFrom);
      List<HistoricalMessage> list = historyImpl.getHistoricalMessages("root",
                                                                       "marry",
                                                                       true,
                                                                       dateFrom,
                                                                       sessionProviderService.getSessionProvider(null));
      int i = 1;
      for (HistoricalMessage historicalMessage : list) {
        HistoricalMessageImpl historicalMessageImpl = (HistoricalMessageImpl) historicalMessage;
        System.out.println("\nMessage " + i++);
        System.out.println("---------------------------------------------------------");
        dumpMessage(historicalMessageImpl);
      }
      long1 = Calendar.getInstance().getTimeInMillis() + 600000;
      dateFrom = new Date(long1);
      System.out.println("\n            date from: " + dateFrom);
      list = historyImpl.getHistoricalMessages("root",
                                               "marry",
                                               true,
                                               dateFrom,
                                               sessionProviderService.getSessionProvider(null));
      for (HistoricalMessage historicalMessage : list) {
        HistoricalMessageImpl historicalMessageImpl = (HistoricalMessageImpl) historicalMessage;
        System.out.println("\nMessage " + i++);
        System.out.println("---------------------------------------------------------");
        dumpMessage(historicalMessageImpl);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void testGetInterlocutors() {
    System.out.println("==========================================================");
    System.out.println("Testing geting interlocutors ");
    System.out.println("==========================================================");
    assertNotNull(container);
    ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    List<Interlocutor> list = historyImpl.getInterlocutors("root",
                                                           sessionProviderService.getSessionProvider(null));
    System.out.println("---- User root ");
    for (Interlocutor interlocutor : list) {
      InterlocutorImpl interlocutorImpl = (InterlocutorImpl) interlocutor;
      System.out.println("Name: " + interlocutorImpl.getInterlocutorName());
      System.out.println("Conversation id: " + interlocutorImpl.getConversationId());
      System.out.println("Path in repository: " + interlocutorImpl.getPath());
    }
    list = historyImpl.getInterlocutors("marry", sessionProviderService.getSessionProvider(null));
    System.out.println("---- User marry ");
    for (Interlocutor interlocutor : list) {
      InterlocutorImpl interlocutorImpl = (InterlocutorImpl) interlocutor;
      System.out.println("Name: " + interlocutorImpl.getInterlocutorName());
      System.out.println("Conversation id:" + interlocutorImpl.getConversationId());
      System.out.println("Path in repository:" + interlocutorImpl.getPath());
    }
  }

  private void dumpMessage(HistoricalMessageImpl historicalMessageImpl) {
    System.out.println("From: " + historicalMessageImpl.getFrom());
    System.out.println("To:  " + historicalMessageImpl.getTo());
    System.out.println("Type: " + historicalMessageImpl.getType());
    System.out.println("Body: " + historicalMessageImpl.getBody());
    System.out.println("Date send: " + historicalMessageImpl.getDateSend());
    System.out.println("Recieve: " + historicalMessageImpl.getReceive());
    System.out.println("Path in repository: " + historicalMessageImpl.getPath());
  }

  private void dumpMessage(Message message) {
    System.out.println("From: " + message.getFrom());
    System.out.println("To:  " + message.getTo());
    System.out.println("Type: " + message.getType().name());
    System.out.println("Body: " + message.getBody());
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
    super.tearDown();
  }

}
