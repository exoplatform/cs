/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.test;

import java.util.Date;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.forum.service.Category;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestForumService extends BaseForumTestCase{

  public void testForumService() throws Exception {
    assertNull(null) ;
  }
  
  public void testCreateCategory() throws Exception {
    Category cat = new Category() ;
    cat.setId("id") ;
    cat.setOwner("nqhung") ;
    cat.setCategoryName("testCategory") ;
    cat.setCategoryOrder(1) ;
    cat.setCreatedDate(new Date()) ;
    cat.setDescription("desciption") ;
    cat.setModifiedBy("nqhung") ;
    cat.setModifiedDate(new Date()) ;
    
    assertNotNull(forumService_.createCategory(cat)) ;
    assertNotNull(forumService_.getCategory("id")) ;
    List<Category> categories = forumService_.getCategories() ;
    assertEquals(categories.size(), 1) ;
  }
}