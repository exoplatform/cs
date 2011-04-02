/*
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
package org.exoplatform.contact.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class SharedAddressBook {
  private String   name;

  private String   id;

  private String   sharedUserId;

  // add to show/hide actions in rightClick popup
  private String[] editPermissionUsers;

  private String[] editPermissionGroups;

  public SharedAddressBook(String name, String id, String sharedUserId) throws Exception {
    this.name = name;
    this.id = id;
    this.sharedUserId = sharedUserId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setSharedUserId(String sharedUserId) {
    this.sharedUserId = sharedUserId;
  }

  public String getSharedUserId() {
    return sharedUserId;
  }

  public String[] getEditPermissionUsers() {
    return editPermissionUsers;
  }

  public void setEditPermissionUsers(String[] editPermission) {
    this.editPermissionUsers = editPermission;
  }

  public String[] getEditPermissionGroups() {
    return editPermissionGroups;
  }

  public void setEditPermissionGroups(String[] editPermission) {
    this.editPermissionGroups = editPermission;
  }

}
