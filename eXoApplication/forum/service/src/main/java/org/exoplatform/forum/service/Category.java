/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service ;

import java.util.Date;

/**
 * March 2, 2007  
 */
public class Category {
   private String id_;
   private String owner_;
   private int categoryOrder_;
   private Date createdDate_;
   private String modifiedBy_;
   private Date modifiedDate_;
   private String name_;
   private String description_;   
   
   public Category(){
     
   }

   public void setId(String id){ id_ = id;}
   public String getId(){return id_;}
   
   public void setOwner(String owner){owner_=owner;}
   public String getOwner(){return owner_;}
   
   public void setCategoryOrder(int categoryOrder){categoryOrder_ = categoryOrder;}
   public int getCategoryOrder(){return categoryOrder_;}
   
   public void setCreatedDate(Date createdDate){createdDate_ = createdDate;}
   public Date getCreatedDate(){return createdDate_;}
   
   public void setModifiedBy(String modifiedBy) {modifiedBy_ = modifiedBy;}
   public String getModifiedBy(){return modifiedBy_;}
   
   public void setModifiedDate(Date modifiedDate){modifiedDate_ = modifiedDate;}
   public Date getModifiedDate(){return modifiedDate_;}
   
   public void setCategoryName(String categoryName){name_ = categoryName;}
   public String getCategoryName(){return name_;}
   
   public void setDescription(String description){description_ = description;}
   public String getDescription(){return description_;}   
}
