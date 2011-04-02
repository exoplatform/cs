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

import java.util.List;

import org.exoplatform.commons.exception.ExoMessageException;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 21, 2004
 * @version $Id: PageList.java,v 1.2 2004/10/25 03:36:58 tuan08 Exp $
 */
abstract public class JCRPageList {
  // final static public PageList EMPTY_LIST = new ObjectPageList(new ArrayList(), 10) ;

  private long            pageSize_;

  protected long          available_     = 0;

  protected long          availablePage_ = 1;

  protected long          currentPage_   = 1;

  protected List<Contact> currentListPage_;

  public JCRPageList(long pageSize) {
    pageSize_ = pageSize;
  }

  public long getPageSize() {
    return pageSize_;
  }

  public void setPageSize(long pageSize) {
    pageSize_ = pageSize;
    setAvailablePage(available_);
  }

  public void setCurrentPage(long page) {
    currentPage_ = page;
  }

  public long getCurrentPage() {
    return currentPage_;
  }

  public long getAvailable() {
    return available_;
  }

  public long getAvailablePage() {
    return availablePage_;
  }

  public List<Contact> currentPage(String username) throws Exception {
    if (currentListPage_ == null) {
      populateCurrentPage(currentPage_, username);
    }
    return currentListPage_;
  }

  public void setContact(List<Contact> contacts, boolean isUpdate) throws Exception {
    if (currentListPage_ == null)
      return;
    for (Contact ct : contacts) {
      for (int i = 0; i < currentListPage_.size(); i++) {
        if (currentListPage_.get(i).getId().endsWith(ct.getId())) {
          if (isUpdate) {
            currentListPage_.set(i, ct);
          } else {
            currentListPage_.remove(i);
          }
          break;
        }
      }
    }
  }

  abstract protected void populateCurrentPage(long page, String username) throws Exception;

  public List<Contact> getPage(long page, String username) throws Exception {
    checkAndSetPage(page);
    populateCurrentPage(page, username);
    return currentListPage_;
  }

  abstract public List<Contact> getAll() throws Exception;

  abstract public void setList(List<Contact> contacts);

  protected void checkAndSetPage(long page) throws Exception {
    if (page < 1 || page > availablePage_) {
      Object[] args = { Long.toString(page), Long.toString(availablePage_) };
      throw new ExoMessageException("PageList.page-out-of-range", args);
    }
    currentPage_ = page;
  }

  protected void setAvailablePage(long available) {
    available_ = available;
    if (available == 0) {
      availablePage_ = 1;
      currentPage_ = 1;
    } else {
      long pages = available / pageSize_;
      if (available % pageSize_ > 0)
        pages++;
      availablePage_ = pages;
      // currentPage_ = 1 ;
    }
  }

  /*
   * public long getFrom() { return (currentPage_ - 1) * pageSize_ ; } public long getTo() { long to = currentPage_ * pageSize_ ; if (to > available_ ) to = available_ ; return to ; }
   */
}
