/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.impl.ForumServiceImpl;


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
  
  public void testCategory() throws Exception {  
	 Category cat = createCategory("id") ;
    forumService_.createCategory(cat) ;
    // add category
    assertNotNull(forumService_.getCategory("id")) ;
    // get categories
    List<Category> categories = forumService_.getCategories() ;
    assertEquals(categories.size(), 1) ;
    // update category
    cat.setCategoryName("nguyenkequanghung") ;
    forumService_.updateCategory(cat) ;
    Category updatedCat = forumService_.getCategory("id") ;
    assertNotNull(updatedCat) ;
    assertEquals("nguyenkequanghung", updatedCat.getCategoryName()) ;
  }

  public void testForum() throws Exception {
	Category cat = createCategory("id") ;
	Forum forum = createdForum("idfr");
	
	forumService_.createForum(cat.getId(), forum);
	
	Forum forum2 = forumService_.getForum(cat.getId(), forum.getId());
	System.out.print("\n\nTestGetForum:  " + forum2.getForumName()+ "\n\n");
	
	//test getListForum
	List<Forum> Forums = forumService_.getForums(cat.getId());
	assertEquals(Forums.size(), 1);
	//test udateForum
	System.out.print("\n\nName Old Forum:" + forum.getForumName() + "\n\nDescriptionOld:  " + forum.getDescription());
	forum.setForumName("VuDuyTu");
	forum.setDescription("Forum nay dung de test updateForum");
	forumService_.updateForum(cat.getId(), forum);
	Forum forum1 = forumService_.getForum(cat.getId(), forum.getId());
	System.out.print("\n\nName New Forum:" + forum1.getForumName() + "\n\nDescriptionNew:  " + forum1.getDescription()+ "\n\n\n");
	//test move
	Category newCat = createCategory("di1");
	forumService_.createCategory(newCat);
	forumService_.moveForum(cat.getId(), forum.getId(), newCat.getId());
	Forum forum3 = forumService_.getForum(newCat.getId(), forum.getId());
	System.out.print("\n\nTestMoveForum:  " + forum3.getForumName()+ "\n\n");
  }
  
  public void testTopic() throws Exception {
	Category cat = createCategory("id") ;
    Forum forum = createdForum("idfn");
    forumService_.createForum(cat.getId(), forum);
    
	Topic topicnew = createdTopic("idtp");
	
	forumService_.createTopic(cat.getId(), forum.getId(), topicnew);
	
	Topic topic1 = forumService_.getTopic(cat.getId(), forum.getId(), topicnew.getId());
	
	System.out.print("\n\nTestgetTopic TopicName:  " + topic1.getTopicName() + "\nDescription:  " + topic1.getDescription() + "\n\n");
  }
  
  
  
  
  
  
  
  
  private Topic createdTopic( String id) {
	Topic topicNew = new Topic();
		  
	topicNew.setId(id);
	topicNew.setOwner("duytu");
	topicNew.setTopicName("TestTopic");
	topicNew.setCreatedDate(new Date());
	topicNew.setModifiedBy("vuduytu");
	topicNew.setModifiedDate(new Date());
	topicNew.setLastPostBy("tu");
	topicNew.setLastPostDate(new Date());
	topicNew.setDescription(" topic nay dung de test");
	topicNew.setPostCount(0);
	  
	return topicNew;
  }
  
  
  private Forum createdForum(String id) {
	Forum forum = new Forum();
	forum.setId(id);
	forum.setOwner("duytu");
	forum.setForumName("TestForum");
	forum.setForumOrder(1);
	forum.setCreatedDate(new Date());
	forum.setModifiedBy("duytu");
	forum.setModifiedDate(new Date());
	forum.setLastPostBy("duytu");
	forum.setLastPostDate(new Date());
	forum.setDescription("description");
	forum.setPostCount(0);
	forum.setTopicCount(0);
	forum.setViewForumRole(new String[] {});
	forum.setCreateTopicRole(new String[] {});
	forum.setReplyTopicRole(new String[] {});
	forum.setModerators(new String[] {});
	return forum;
  }
  
  
  
  private Category createCategory(String id) {
    Category cat = new Category() ;
    cat.setId(id) ;
    cat.setOwner("nqhung") ;
    cat.setCategoryName("testCategory") ;
    cat.setCategoryOrder(1) ;
    cat.setCreatedDate(new Date()) ;
    cat.setDescription("desciption") ;
    cat.setModifiedBy("nqhung") ;
    cat.setModifiedDate(new Date()) ;    
    return cat ;
  }
  
}