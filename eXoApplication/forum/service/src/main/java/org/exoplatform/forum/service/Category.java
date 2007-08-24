/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service ;

import java.util.Date;

import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * March 2, 2007  
 */
public class Category {
   private String id;
   private String owner;
   private String path;
   private long categoryOrder;
   private Date createdDate;
   private String modifiedBy;
   private Date modifiedDate;
   private String name;
   private String description;   
   
   public Category(){
     id = ("category" + IdGenerator.generate()).toUpperCase() ;
   }

   public String getId(){return id;}
   public void setId(String id){ this.id = id;}
   
   public String getOwner(){return owner;}
   public void setOwner(String owner){this.owner=owner;}

   public String getPath() {return path; }
   public void setPath( String path) { this.path = path;}
   
   public long getCategoryOrder(){return categoryOrder;}
   public void setCategoryOrder(long categoryOrder){this.categoryOrder = categoryOrder;}
   
   public Date getCreatedDate(){return createdDate;}
   public void setCreatedDate(Date createdDate){this.createdDate = createdDate;}
   
   public String getModifiedBy(){return modifiedBy;}
   public void setModifiedBy(String modifiedBy) {this.modifiedBy = modifiedBy;}
   
   public Date getModifiedDate(){return modifiedDate;}
   public void setModifiedDate(Date modifiedDate){this.modifiedDate = modifiedDate;}
   
   public String getCategoryName(){return name;}
   public void setCategoryName(String categoryName){this.name = categoryName;}
   
   public String getDescription(){return description;}   
   public void setDescription(String description){this.description = description;}
}
