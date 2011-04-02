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
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Hoang
 *          hung.hoang@exoplatform.com
 * Mar 16, 2011  
 */
public class MailSettingConfig {
  private String  name;

  private boolean userAllowed;

  private String  defaultValue;

  public String getName() {
    return name;
  }

  public void setName(String name_) {
    this.name = name_;
  }

  public boolean getUserAllowed() {
    return userAllowed;
  }

  public void setUserAllowed(boolean b) {
    userAllowed = b;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String b) {
    defaultValue = b;
  }
}
