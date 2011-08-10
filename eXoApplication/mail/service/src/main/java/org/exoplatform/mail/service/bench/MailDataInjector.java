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
package org.exoplatform.mail.service.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS
 * @author <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Aug 5, 2011  
 */
public class MailDataInjector extends DataInjector {
  
  private static Log log = ExoLogger.getExoLogger(MailDataInjector.class);
  
  final static public String   FD_INBOX        = "Inbox".intern();

  final static public String   FD_DRAFTS       = "Drafts".intern();

  final static public String   FD_SENT         = "Sent".intern();

  final static public String   FD_SPAM         = "Spam".intern();

  final static public String   FD_TRASH        = "Trash".intern();

  final static public String[] defaultFolders_ = { FD_INBOX, FD_DRAFTS, FD_SENT, FD_SPAM, FD_TRASH };
  
  private MailService mailService;
  
  private SimpleMailServerInitializer mailServerInitializer;
  
  private int maxAccounts = 2;
  
  private int maxMessages = 100;
  
  private boolean randomize = false;
  
  private Random random = new Random();
  
  private Stack<String> accountsStack = new Stack<String>();
  
  public MailDataInjector(MailService mailService, SimpleMailServerInitializer mailServerInitializer, InitParams initParams) {
    this.mailService = mailService;
    this.mailServerInitializer = mailServerInitializer;
    initParams(initParams);
  }
  
  private Account newAccount(Account previous) {
    String username = IdGenerator.generate();
    String email = username + "@example.com";
    Account account = new Account();
    account.setIncomingUser(email);
    account.setLabel(username);
    account.setIncomingPassword("password");
    account.setUseIncomingForAuthentication(true);
    account.setEmailAddress(email);
    account.setIncomingHost(mailServerInitializer.getHost());
    if (previous != null) {
      if (previous.getProtocol().equals("imap")) {
        account.setProtocol("pop3");
        account.setIncomingPort(mailServerInitializer.getPop3Port());
      } else {
        account.setProtocol("imap");
        account.setIncomingPort(mailServerInitializer.getImapPort());
      }
    } else {
      account.setProtocol("pop3");
      account.setIncomingPort(mailServerInitializer.getPop3Port());
    }
    account.setOutgoingHost(mailServerInitializer.getHost());
    account.setOutgoingPort(mailServerInitializer.getSmtpPort());
    account.setUserDisplayName(randomWords(3));
    account.setDescription(randomWords(10));
    account.setIsSavePassword(true);
    account.setAuthMechsIncoming(Utils.STARTTLS);
    account.setAuthMechsOutgoing(Utils.STARTTLS);
    account.setIncomingSsl(true);
    account.setOutgoingSsl(true);
    account.setSecureAuthsIncoming(Utils.STARTTLS);
    account.setSecureAuthsOutgoing(Utils.STARTTLS);
    account.setIncomingFolder(FD_INBOX);
    account.setCheckAll(true);
    return account;
  }
  
  private int maxAccounts() {
    return randomize ? random.nextInt(maxAccounts) : maxAccounts;
  }
  
  private int maxMessages() {
    return randomize ? random.nextInt(maxMessages) : maxMessages;
  }
  
  private List<Folder> generateDefaultFolders() {
    List<Folder> folders = new ArrayList<Folder>();
    for (String folderName : defaultFolders_) {
      Folder folder = new Folder();
      folder.setName(folderName);
      folder.setPersonalFolder(false);
      folders.add(folder);
    }
    return folders;
  }
  
  private List<Account> generateAccounts() {
    List<Account> accounts = new ArrayList<Account>();
    Account previous = null;
    for (int i = 0; i < maxAccounts(); i++) {
      Account account = newAccount(previous);
      accounts.add(account);
      previous = account;
    }
    return accounts;
  }
  
  @Override
  public Log getLog() {
    return log;
  }

  @Override
  public boolean isInitialized() {
    return false;
  }

  @Override
  public void initParams(InitParams initParams) {
    ValueParam param = initParams.getValueParam("mA");
    if (param != null) 
      maxAccounts = Integer.parseInt(param.getValue());
    param = initParams.getValueParam("mM");
    if (param != null)
      maxMessages = Integer.parseInt(param.getValue());
    param = initParams.getValueParam("rand");
    if (param != null) 
     randomize = Boolean.parseBoolean(param.getValue());
  }

  @Override
  public void inject() throws Exception {
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    List<Account> accounts = generateAccounts();
    int accSize = accounts.size();
    for (int i = 0; i < accSize; i++) {
      log.info("\tCreate account " + (i + 1) + "/" + accSize + " ...... ");
      Account account = accounts.get(i);
      mailService.createAccount(username, account);
      mailServerInitializer.addUser(account.getEmailAddress(), account.getIncomingPassword());
      accountsStack.push(account.getId());
      List<Folder> folders = generateDefaultFolders();
      int folderSize = folders.size();
      for (int j = 0; j < folderSize; j++) {
        Folder folder = folders.get(j);
        log.info("\t\tCreate Folder " + folder.getName() + " ....... ");
        folder.setId(Utils.generateFID(account.getId(), folder.getName(), false));
        mailService.saveFolder(username, account.getId(), folder);
        
        if (folder.getName().equalsIgnoreCase(FD_INBOX)) {
          int msgsNum = maxMessages();
          log.info("\t\t\t Pouring " + msgsNum + " messages into INBOX folder .........");
          for (int k = 0; k < msgsNum; k++) {
            log.info("\t\t\t\t Sending message " + k + "/" + msgsNum + " .........");
            mailServerInitializer.sendMailMessage(account.getEmailAddress(),
                                                  account.getEmailAddress(),
                                                  randomWords(100),
                                                  randomParagraphs(4),
                                                  createTextResource(100).getBytes(),
                                                  "filename" + IdGenerator.generate() + ".txt",
                                                  null);
          }
        }
      }
      log.info("\t\t\t Pulling messages from mail server to eXo Mail ........");
      mailService.checkNewMessage(username, account.getId(), FD_INBOX);
      log.info(String.format("\tAccount %1$s has been created successfully!", account.getEmailAddress()));
    }
  }

  @Override
  public void reject() throws Exception {
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    while (!accountsStack.isEmpty()) {
      String accId = accountsStack.pop();
      log.info("\tRemove account " + accId + " ............. ");
      mailService.removeAccount(username, accId);
    }
    log.info("\tAccounts have been removed successfully!");
  }
}
