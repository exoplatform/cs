/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 				
 * Dec 5, 2007 11:00:12 AM
 */

public class Tag {
  private String id ;
  private String name ;
  private String owner ;
  private String description ;
  private String color ;
  private String []topicIds = new String[] {} ;
  public Tag() {
    id = "Tag" + IdGenerator.generate() ;
  }
  
  public String getId()  { return id ; }
  public void setId(String s) { id = s ; }
  
  public void setName(String name) { this.name = name ; }
  public String getName() { return name ; }
  
  public String getOwner(){return owner;}
  public void setOwner(String owner){this.owner = owner;}
  
  public void setDescription(String description) { this.description = description ; }
  public String getDescription() { return description ; }
    
  public void setColor(String color) { this.color = color ; }
  public String getColor() { return color ; }
  
  public String[] getTags(){return topicIds;}
  public void setTags(String[] topicIds){this.topicIds = topicIds;}
  
}