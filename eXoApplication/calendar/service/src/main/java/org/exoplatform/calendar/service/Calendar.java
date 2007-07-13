/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Calendar {
  private String id_ ;
  private String name_ ;
  private String description_ ;
  private String[] viewPermission_ ;
  private String[] editPermission_ ;
  private boolean isPrivate_ = true ;
  private String categoryId_ ;
  private String[] groups_ ;
  
  public Calendar() {}
  
  public void setId(String id_) {
    this.id_ = id_;
  }
  public String getId() {
    return id_;
  }

  public void setName(String name_) {
    this.name_ = name_;
  }

  public String getName() {
    return name_;
  }

  public void setDescription(String description_) {
    this.description_ = description_;
  }

  public String getDescription() {
    return description_;
  }

  public void setEditPermission(String[] editPermission_) {
    this.editPermission_ = editPermission_;
  }

  public String[] getEditPermission() {
    return editPermission_;
  }

  public void setViewPermission(String[] viewPermission_) {
    this.viewPermission_ = viewPermission_;
  }

  public String[] getViewPermission() {
    return viewPermission_;
  }

  public void setGroups(String[] groups_) {
    this.groups_ = groups_;
  }

  public String[] getGroups() {
    return groups_;
  }

  public void setCategoryId(String categoryId_) {
    this.categoryId_ = categoryId_;
  }

  public String getCategoryId() {
    return categoryId_;
  }

  public void setPrivate(boolean isPrivate_) {
    this.isPrivate_ = isPrivate_;
  }

  public boolean isPrivate() {
    return isPrivate_;
  }
  
  
}
