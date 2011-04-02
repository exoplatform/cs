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
package org.exoplatform.contact.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 24, 2009  
 */
public class AddressBook {
  private String   id;

  private String   name;

  private String   description;

  private String[] editPermissionUsers;

  private String[] viewPermissionUsers;

  private String[] editPermissionGroups;

  private String[] viewPermissionGroups;

  public AddressBook() {
    id = "ContactGroup" + IdGenerator.generate();
  }

  public String getId() {
    return id;
  }

  public void setId(String s) {
    id = s;
  }

  public String getName() {
    return name;
  }

  public void setName(String s) {
    name = s;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String s) {
    description = s;
  }

  public String[] getEditPermissionUsers() {
    return editPermissionUsers;
  }

  public void setEditPermissionUsers(String[] editPermission) {
    this.editPermissionUsers = editPermission;
  }

  public String[] getViewPermissionUsers() {
    return viewPermissionUsers;
  }

  public void setViewPermissionUsers(String[] viewPermission) {
    this.viewPermissionUsers = viewPermission;
  }

  // -------------------------------
  public String[] getEditPermissionGroups() {
    return editPermissionGroups;
  }

  public void setEditPermissionGroups(String[] editPermission) {
    this.editPermissionGroups = editPermission;
  }

  public String[] getViewPermissionGroups() {
    return viewPermissionGroups;
  }

  public void setViewPermissionGroups(String[] viewPermission) {
    this.viewPermissionGroups = viewPermission;
  }
}
