/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.contact.service;

/** 
 * stores the state of the current JCR query
 * used for the next JCR query
 * @see {@link QueryState#relativeOffset}, {@link QueryState#currentQuery}
 * 
 * @author Created by The eXo Platform SAS
 * <br/>Anh-Tu Nguyen
 * <br/><a href="mailto:tuna@exoplatform.com">tuna@exoplatform.com<a/>
 * <br/>Nov 1, 2012  
 */
public class QueryState 
{
  /**
   * stores the relative position of offset in the current JCR query
   * 
   */
  private int relativeOffset;
   
  /**
   * stores the type of contacts where the JCR query is on 
   * currentQuery can be "public contacts" or "personal contacts" or "shared contacts"
   *
   */
  private String currentQuery;
    
  public QueryState on(String currentQuery)
  {
    this.currentQuery = currentQuery;
    return this;
  }
  
  public QueryState withRelativeOffset(int offset)
  {
    this.relativeOffset = offset;
    return this;
  }
  
  public int getRelativeOffset()
  {
    return relativeOffset;
  }
 
  public boolean isOn(String query)
  {
    return query.equals(currentQuery);
  }
  
  public String getQuery()
  {
    return currentQuery;
  }
}