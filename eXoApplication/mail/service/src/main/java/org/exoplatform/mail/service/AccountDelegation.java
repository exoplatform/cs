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
package org.exoplatform.mail.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS
 * Author : nguyen van hoang
 *          hoang.nguyen@exoplatform.com
 * Jan 21, 2011  
 */
public class AccountDelegation {

  private String  accountName;

  private String  accountId;

  private String  delegatedUserName;

  private boolean isFull     = false;

  private boolean isReadOnly = true;

  private String  id;

  public boolean isFull() {
    return isFull;
  }

  public void setFull(boolean isFull) {
    this.isFull = isFull;
  }

  public boolean isReadOnly() {
    return isReadOnly;
  }

  public void setReadOnly(boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
  }

  public AccountDelegation() {
    id = IdGenerator.generate();
  }

  public AccountDelegation(String accountId, String accname, String delegateuser, boolean isFull) {
    id = IdGenerator.generate();
    this.accountId = accountId;
    this.accountName = accname;
    this.delegatedUserName = delegateuser;
    this.isFull = isFull;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public String getDelegatedUserName() {
    return delegatedUserName;
  }

  public void setDelegatedUserName(String delegatedUserName) {
    this.delegatedUserName = delegatedUserName;
  }

  public boolean getIsFull() {
    return isFull;
  }

  public boolean getIsReadOnly() {
    return isReadOnly;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getAccountId() {
    return accountId;
  }
}
