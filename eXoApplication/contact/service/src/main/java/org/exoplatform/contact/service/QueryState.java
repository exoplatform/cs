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
 * Created by The eXo Platform SAS
 * Author : Anh-Tu Nguyen
 *          tuna@exoplatform.com
 * Nov 1, 2012  
 */
public class QueryState 
{
  private int relativeOffset;
  
  private Integer lastOffset;
  
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
  
  public QueryState lastOffset(Integer offset)
  {
    this.lastOffset = offset;
    return this;
  }
  
  public int getLastOffset()
  {
    return lastOffset;
  }
}