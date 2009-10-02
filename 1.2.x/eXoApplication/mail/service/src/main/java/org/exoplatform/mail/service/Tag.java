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

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Philippe Aristote
 *          philippe.aristote@gmail.com
 * Jul 10, 2007  
 */
public class Tag {
  
  private String id;
  private String name;
  private String desc;
  private String color;
  
  public Tag() {
    id = Utils.KEY_TAGS + IdGenerator.generate() ;
  } 
  
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  
  public String getName() { return name ; }
  public void setName(String value) { this.name = value ; } 
  
  public String getDescription() {return desc ;}
  public void setDescription(String desc) {this.desc = desc ;}
  
  public String getColor() { return color; }
  public void setColor(String color) { this.color = color; }
}
