/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
