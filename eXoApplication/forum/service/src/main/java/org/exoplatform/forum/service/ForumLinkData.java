/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service ;


/**
 * March 2, 2007  
 */
public class ForumLinkData {
   private String id;
   private String name;
   private String path;
   private String type;
   

   public String getId(){return id;}
   public void setId(String id){ this.id = id;}
   
   public String getName(){return name;}
   public void setName(String Name){this.name = Name;}
   
   public String getPath(){return path;}
   public void setPath(String path){this.path = path ; }
   
   public String getType(){return type;}
   public void setType(String type){this.type = type ; }
   
}
