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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.commons.exception.ExoMessageException;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 21, 2004
 * @version $Id: PageList.java,v 1.2 2004/10/25 03:36:58 tuan08 Exp $
 */
abstract public class JCRPageList {
  // final static public PageList EMPTY_LIST = new ObjectPageList(new ArrayList(), 10) ;

  private long                             pageSize_;

  protected long                           available_     = 0;

  protected long                           availablePage_ = 1;

  protected long                           currentPage_   = 1;

  protected LinkedHashMap<String, Message> currentListPage_;

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

  public long getCurrentPage() {
    return currentPage_;
  }

  public long getAvailable() {
    return available_;
  }

  public long getAvailablePage() {
    return availablePage_;
  }

  public List<Message> currentPage(String username) throws Exception {
    if (currentListPage_ == null) {
      populateCurrentPage(currentPage_, username);
    }
    return new ArrayList<Message>(currentListPage_.values());
  }

  abstract protected void populateCurrentPage(long page, String username) throws Exception;

  public List<Message> getPage(long page, String username) throws Exception {
    List<Message> clp = new ArrayList<Message>();
    try {
      checkAndSetPage(page);
      populateCurrentPage(page, username);
      clp = new ArrayList<Message>(currentListPage_.values());
      if (clp == null)
        return new ArrayList<Message>();
    } catch (Exception e) {
      return new ArrayList<Message>();
    }
    return clp;
  }

  abstract public List<Message> getAll() throws Exception;

  public void checkAndSetPage(long page) throws Exception {
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
