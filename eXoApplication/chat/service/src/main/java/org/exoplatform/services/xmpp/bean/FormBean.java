/**
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
package org.exoplatform.services.xmpp.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class FormBean {

  /**
   * 
   */
  private List<FieldBean> fields;

  /**
   * 
   */
  private String          instructions;

  /**
   * 
   */
  private String          type;

  /**
   * 
   */
  private String          title;

  /**
   * 
   */
  private List<String>    members   = new ArrayList<String>();

  private List<String>    fullNames = new ArrayList<String>();

  /**
   * @return the fields
   */
  public List<FieldBean> getFields() {
    return fields;
  }

  /**
   * @param fields the fields to set
   */
  public void setFields(List<FieldBean> fields) {
    this.fields = fields;
  }

  /**
   * @return the instructions
   */
  public String getInstructions() {
    return instructions;
  }

  /**
   * @param instructions the instructions to set
   */
  public void setInstructions(String instructions) {
    this.instructions = instructions;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the members
   */
  public List<String> getMembers() {
    return members;
  }

  /**
   * @param members the members to set
   */
  public void setMembers(List<String> members) {
    this.members = members;
  }

  /**
   * @return the fullNames
   */
  public List<String> getFullNames() {
    return fullNames;
  }

  /**
   * @param fullNames the members to set
   */
  public void setFullNames(List<String> list) {
    this.fullNames = list;
  }

}
