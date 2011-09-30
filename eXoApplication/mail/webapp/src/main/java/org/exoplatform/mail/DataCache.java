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
package org.exoplatform.mail;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UISelectAccount;

/**
 * Created by The eXo Platform SARL
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Sep 16, 2011 
 */
public class DataCache {
  private Hashtable<String, Hashtable<String, Account>> delegatedAccountCache = new Hashtable<String, Hashtable<String, Account>>();
  private Hashtable<String, Boolean> isAlreadyGotAllDelegatedAccount = new Hashtable<String, Boolean>();
  
  private Hashtable<String, Hashtable<String, Account>> accountCache = new Hashtable<String, Hashtable<String, Account>>();
  private Hashtable<String, Boolean> isAlreadyGotAllAccount = new Hashtable<String, Boolean>();
  
  private Hashtable<String, Hashtable<String, Folder>> folderCache = new Hashtable<String, Hashtable<String,Folder>>();
  private Hashtable<String, List<Folder>> allFolderCache = new Hashtable<String, List<Folder>>();
  private Hashtable<String, Boolean> isAlreadyGotAllFolder = new Hashtable<String, Boolean>();
  
  private Hashtable<String, List<Folder>> subFolderCache = new Hashtable<String, List<Folder>>();
  
  private String selectedAccountId; 
  
  private UIMailPortlet mailPortlet;
  
  public DataCache() {
  }
  
  public void setMailPortlet(UIMailPortlet mailPortlet) {
    this.mailPortlet = mailPortlet;
  }
  
  public void clearAccountCache() {
    delegatedAccountCache.clear();
    isAlreadyGotAllDelegatedAccount.clear();
    accountCache.clear();
    isAlreadyGotAllAccount.clear();
    selectedAccountId = null;
  }
  
  public void clearFolderCache() {
    folderCache.clear();
    allFolderCache.clear();
    isAlreadyGotAllFolder.clear();
    subFolderCache.clear();
  }

  public void clearCache() {
    delegatedAccountCache.clear();
    isAlreadyGotAllDelegatedAccount.clear();
    accountCache.clear();
    isAlreadyGotAllAccount.clear();
    folderCache.clear();
    allFolderCache.clear();
    isAlreadyGotAllFolder.clear();
    subFolderCache.clear();
    selectedAccountId = null;
  }
  
  public String getSelectedAccountId() {
    if (selectedAccountId == null) {
      selectedAccountId = mailPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    }
    return selectedAccountId;
  }
  
  public Account getDelegatedAccount(String username, String accountId) throws Exception {
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(accountId)) {
      return null;
    }
    
    Hashtable<String, Account> delegatedAccountMaps = delegatedAccountCache.get(username);
    if (delegatedAccountMaps == null) {
      delegatedAccountMaps = new Hashtable<String, Account>();
      delegatedAccountCache.put(username, delegatedAccountMaps);
    }
    
    Account account = delegatedAccountMaps.get(accountId);
    if (account == null) {
      account = MailUtils.getMailService().getDelegatedAccount(username, accountId);
      if (account != null) {
        delegatedAccountMaps.put(accountId, account);
      }
    }
    return account;
  }
  
  public List<Account> getDelegatedAccounts(String userId) throws Exception {
    if (StringUtils.isEmpty(userId)) {
      return new ArrayList<Account>();
    }
    
    List<Account> delegatedAccounts = null;
    
    Boolean isGotAllDelegatedAccounts = isAlreadyGotAllDelegatedAccount.get(userId);
    if (isGotAllDelegatedAccounts == null) {
      delegatedAccounts = MailUtils.getMailService().getDelegatedAccounts(userId);
      
      // Get cache
      Hashtable<String, Account> delegatedAccountMaps = delegatedAccountCache.get(userId);
      if (delegatedAccountMaps == null) {
        delegatedAccountMaps = new Hashtable<String, Account>();
        delegatedAccountCache.put(userId, delegatedAccountMaps);
      }
      
      // Put all delegated accounts to cache
      for (Account account : delegatedAccounts) {
        delegatedAccountMaps.put(account.getId(), account);
      }
      
      // Mark that we were already got all delegated accounts
      isAlreadyGotAllDelegatedAccount.put(userId, Boolean.TRUE);
    } else {
      Hashtable<String, Account> delegatedAccountMaps = delegatedAccountCache.get(userId);
      delegatedAccounts = new ArrayList<Account>();
      delegatedAccounts.addAll(delegatedAccountMaps.values());
    }
    return delegatedAccounts;
  }
  
  public Account getAccountById(String username, String accountId) throws Exception {
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(accountId)) {
      return null;
    }
    
    Hashtable<String, Account> accountMaps = accountCache.get(username);
    if (accountMaps == null) {
      accountMaps = new Hashtable<String, Account>();
      accountCache.put(username, accountMaps);
    }
    
    Account account = accountMaps.get(accountId);
    if (account == null) {
      account = MailUtils.getMailService().getAccountById(username, accountId);
      if (account != null) {
        accountMaps.put(accountId, account);
      }
    }
    return account;
  }
  
  public List<Account> getAccounts(String username) throws Exception {
    if (StringUtils.isEmpty(username)) {
      return null;
    }
    
    List<Account> accounts = null;
    
    Boolean isGotAllAccounts = isAlreadyGotAllAccount.get(username);
    if (isGotAllAccounts == null) {
      accounts = MailUtils.getMailService().getAccounts(username);
      
      // Get cache
      Hashtable<String, Account> accountMaps = accountCache.get(username);
      if (accountMaps == null) {
        accountMaps = new Hashtable<String, Account>();
        accountCache.put(username, accountMaps);
      }
      
      // Put all accounts to cache
      for (Account account : accounts) {
        accountMaps.put(account.getId(), account);
      }
      
      // Mark that we were already got all accounts
      isAlreadyGotAllAccount.put(username, Boolean.TRUE);
    } else {
      Hashtable<String, Account> accountMaps = accountCache.get(username);
      accounts = new ArrayList<Account>();
      accounts.addAll(accountMaps.values());
    }
    return accounts;
  }
  
  public Folder getFolderById(String username, String accountId, String folderId) throws Exception {
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(accountId) || StringUtils.isEmpty(folderId)) {
      return null;
    }
    
    String cacheKey = username + "_" + accountId;
    
    Hashtable<String, Folder> folderMaps = folderCache.get(cacheKey);
    if (folderMaps == null) {
      folderMaps = new Hashtable<String, Folder>();
      folderCache.put(cacheKey, folderMaps);
    }
    
    Folder folder = folderMaps.get(folderId);
    if (folder == null) {
      folder = MailUtils.getMailService().getFolderById(username, accountId, folderId);
      if (folder != null) {
        folderMaps.put(folderId, folder);
      }
    }
    return folder;
  }
   
  public List<Folder> getFolders(String username, String accountId) throws Exception {
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(accountId)) {
      return new ArrayList<Folder>();
    }
    
    String cacheKey = username + "_" + accountId;
    List<Folder> folders = null;
    
    Boolean isGotAllFolder = isAlreadyGotAllFolder.get(cacheKey);
    if (isGotAllFolder == null) {
      folders = MailUtils.getMailService().getFolders(username, accountId);
      
      // Get cache
      Hashtable<String, Folder> folderMaps = folderCache.get(cacheKey);
      if (folderMaps == null) {
        folderMaps = new Hashtable<String, Folder>();
        folderCache.put(cacheKey, folderMaps);
      }
      
      // Store all folder to cache
      for (Folder folder : folders) {
        folderMaps.put(folder.getId(), folder);
      }
      
      // Get all folders cache
      List<Folder> allFolders = allFolderCache.get(cacheKey);
      if (allFolders == null) {
        allFolders = new ArrayList<Folder>();
        allFolderCache.put(cacheKey, allFolders);
      }
      
      // Store to all folder cache
      allFolders.clear();
      allFolders.addAll(folders);
      
      // Mark that we were already got all folders
      isAlreadyGotAllFolder.put(cacheKey, Boolean.TRUE);
    } else {
      List<Folder> allFolders = allFolderCache.get(cacheKey);
      folders = new ArrayList<Folder>();
      folders.addAll(allFolders);
    }
    return folders;
  }
  
  public List<Folder> getFolders(String username, String accountId, boolean isPersonal) throws Exception {
    List<Folder> folders = getFolders(username, accountId);
    List<Folder> resultList = new ArrayList<Folder>();
    
    for (Folder folder : folders) {
      if (folder.isPersonalFolder() == isPersonal) {
        resultList.add(folder);
      }
    }
    return resultList;
  }
  
  public List<Folder> getSubFolders(String userName, String accountId, String parentPath) throws Exception {
    if (parentPath == null) {
      return new ArrayList<Folder>();
    }
    String key = userName + "_" + accountId + "_" + parentPath;
    
    List<Folder> subFolders = subFolderCache.get(key);
    if (subFolders == null) {
      subFolders = MailUtils.getMailService().getSubFolders(userName, accountId, parentPath);
      if (subFolders != null) {
        subFolderCache.put(key, subFolders);
      }
    }
    return subFolders;
  }
}
