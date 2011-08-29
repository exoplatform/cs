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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS
 * @author <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Aug 5, 2011  
 */
public class MailDataInjector extends DataInjector {
  
  enum CONSTANTS {
    POP3("pop3"), IMAP("imap");
    
    private final String name; 
    
    CONSTANTS(String name) {
      this.name = name;
    }
    
    public String getName() {
      return name;
    }
    
  }
  
  private static Log log = ExoLogger.getExoLogger(MailDataInjector.class);
  
  public static final String          ARRAY_SPLIT     = ",";
  
  final static public String          FD_INBOX        = "Inbox".intern();

  final static public String          FD_DRAFTS       = "Drafts".intern();

  final static public String          FD_SENT         = "Sent".intern();

  final static public String          FD_SPAM         = "Spam".intern();

  final static public String          FD_TRASH        = "Trash".intern();

  final static public String[] defaultFolders_ = { FD_INBOX, FD_DRAFTS, FD_SENT, FD_SPAM, FD_TRASH };
  
  private MailService mailService;
  
  private SimpleMailServerInitializer mailServerInitializer;
  
  private OrganizationService organizationService;

  public MailDataInjector(MailService mailService, SimpleMailServerInitializer mailServerInitializer, OrganizationService organizationService, InitParams initParams) {
    this.mailService = mailService;
    this.mailServerInitializer = mailServerInitializer;
    this.organizationService = organizationService;
  }
  
  private void printInputParameters(HashMap<String, String> params) {
    StringBuilder sb = new StringBuilder();
    sb.append("PARAMS: \n");
    Iterator<String> keys = params.keySet().iterator();
    while (keys.hasNext()) {
      String key = keys.next();
      sb.append(String.format("%1$10s    :    %2$10s \n", key, params.get(key)));
    }
    log.info(sb.toString());
  }
  
  private String makeAccountId(String prefix, String userId, int order) {
    return prefix + "_" + userId + "_" + order;
  }
  
  private String makeEmailAddress(String username) {
    return username + "@example.com";
  }
  
  private Account newAccount(String accountId, String incomingProtocol) {
    String email = makeEmailAddress(accountId);
    Account account = new Account();
    account.setId(accountId);
    account.setIncomingUser(email);
    account.setLabel(accountId);
    account.setIncomingPassword("password");
    account.setUseIncomingForAuthentication(true);
    account.setEmailAddress(email);
    account.setIncomingHost(mailServerInitializer.getHost());
    
    if (CONSTANTS.POP3.getName().equals(incomingProtocol)) {
      account.setProtocol("pop3");
      account.setIncomingPort(mailServerInitializer.getPop3Port());
    } else {
      account.setProtocol("imap");
      account.setIncomingPort(mailServerInitializer.getImapPort());
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
  
  private List<String> readUsers(HashMap<String, String> queryParams) {
    List<String> users = new LinkedList<String>();
    String value = queryParams.get("users");
    String[] usersString = value.split(ARRAY_SPLIT);
    for (String user : usersString) {
      users.add(user);
    }
    return users;
  }
  
  private int readNumOfAccounts(HashMap<String, String> queryParams) {
    String value = queryParams.get("accounts");
    String num = value.split(ARRAY_SPLIT)[0];
    return Integer.parseInt(num);
  }
  
  private String readPrefix(HashMap<String, String> queryParams) {
    String value = queryParams.get("accounts");
    return value.split(ARRAY_SPLIT)[1];
  }
  
  private String readInComingProtocol(HashMap<String, String> queryParams) {
    return queryParams.get("inPro");
  }
  
  private boolean readCheckNow(HashMap<String, String> queryParams) {
    return Boolean.parseBoolean(queryParams.get("check"));
  }
  
  private int readNumberOfMessages(HashMap<String, String> queryParams) {
    return Integer.parseInt(queryParams.get("msgs"));
  }
  
  private int readAttachmentSize(HashMap<String, String> queryParams) {
    return Integer.parseInt(queryParams.get("attSize"));
  }
  
  private void validateUsers(List<String> users) throws Exception {
    for (String user : users) {
      if (organizationService.getUserHandler().findUserByName(user) == null) {
        log.info(String.format("\t Validate User: %s does not exist!", user));
        users.remove(user);
      }
    }
  }
  
  private List<Folder> generateDefaultFolders(String accountId) {
    List<Folder> folders = new ArrayList<Folder>();
    for (String folderName : defaultFolders_) {
      String folderId = Utils.generateFID(accountId, folderName, false);
      Folder folder = new Folder();
      folder.setId(folderId);
      folder.setName(folderName);
      folder.setPersonalFolder(false);
      folders.add(folder);
    }
    return folders;
  }
  
  @Override
  public Log getLog() {
    return log;
  }
  
  @Override
  public void inject(HashMap<String, String> queryParams) throws Exception {
    log.info("Start to inject data ................. ");
    printInputParameters(queryParams);
    List<String> users = readUsers(queryParams);
    validateUsers(users);
    int numOfAccounts = readNumOfAccounts(queryParams);
    String accPref = readPrefix(queryParams);
    String incProtocol = readInComingProtocol(queryParams);
    int numOfMsgs = readNumberOfMessages(queryParams);
    int attSize = readAttachmentSize(queryParams);
    boolean checkNow = readCheckNow(queryParams);
    for (String userId : users) {
      log.info("\t Process user: " + userId + " .....................");
      for (int i = 0; i < numOfAccounts; i++) {
        String accId = makeAccountId(accPref, userId, i + 1);
        Account account = mailService.getAccountById(userId, accId);
        if (account == null) {
          account = newAccount(accId, incProtocol);
          mailService.createAccount(userId, account);
          mailServerInitializer.addUser(account.getEmailAddress(), account.getIncomingPassword());
        }
        List<Folder> folders = generateDefaultFolders(accId);
        for (Folder folder : folders) {
          mailService.saveFolder(userId, accId, folder);
        }
        Random rand = new Random();
        byte[] attachment = createTextResource(attSize).getBytes();
        log.info("\t\t\t Pouring " + numOfMsgs + " messages into account: " + accId + " .............. ");
        for (int j = 0; j < numOfMsgs; j++) {
          if (attSize == 0) {
            mailServerInitializer.sendMailMessage(account.getEmailAddress(), makeEmailAddress(makeAccountId(accPref, userId, rand.nextInt())), randomWords(100), randomParagraphs(4));
          } else {
            mailServerInitializer.sendMailMessage(account.getEmailAddress(),
                                                  makeEmailAddress(makeAccountId(accPref, userId, rand.nextInt())),
                                                  randomWords(100),
                                                  randomParagraphs(4),
                                                  attachment,
                                                  "filename" + IdGenerator.generate() + ".txt",
                                                  null);
          }
        }
        
        if (checkNow) {
          log.info("\t\t\t Pulling messages from mail server to eXo Mail ........");
          mailService.checkNewMessage(userId, account.getId(), FD_INBOX);
          log.info(String.format("\tAccount %1$s has been created successfully!", account.getEmailAddress()));
        }
        
      }
    }
    
  }


  @Override
  public void reject(HashMap<String, String> queryParams) throws Exception {
    log.info("Start to reject data ................. ");
    printInputParameters(queryParams);
    List<String> users = readUsers(queryParams);
    validateUsers(users);
    int numOfAccounts = readNumOfAccounts(queryParams);
    String accPref = readPrefix(queryParams);
    for (String user : users) {
      log.info("\t Process user: " + user + " .....................");
      for (int i = 0; i < numOfAccounts; i++) {
        String accId = makeAccountId(accPref, user, i + 1);
        Account acc = mailService.getAccountById(user, accId);
        if (acc != null) {
          log.info(String.format("Remove account: %1$s of %2$s ...........", accId, user));
          long t1 = System.currentTimeMillis();
          mailService.removeAccount(user, accId);
          long t2 = System.currentTimeMillis() - t1;
          log.info("Account has been removed in " + t2 + " (ms)!");
        }
      }
    }
    log.info("Data has been rejected successfully!");
  }

  @Override
  public Object execute(HashMap<String, String> arg0) throws Exception {
    return new Object();
  }
}
