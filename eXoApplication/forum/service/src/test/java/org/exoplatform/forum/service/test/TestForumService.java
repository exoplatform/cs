/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.test;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


import org.exoplatform.commons.utils.PageList;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Post;
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
    // test removeCategory
    assertNotNull(forumService_.removeCategory("id"));
  }

  public void testForum() throws Exception {
  	Category cat = createCategory("idC0");
  	forumService_.createCategory(cat);
  	
  	GregorianCalendar calendar = new GregorianCalendar() ;
		String id = String.valueOf(calendar.getTimeInMillis());
		
  	Forum forum = createdForum(id);
  	//forum la forum khoi tao
  	// add forum
  	forumService_.createForum(cat.getId(), forum);
  	// getForum
  	assertNotNull(forumService_.getForum(cat.getId(), forum.getId()));
  	Forum forumNew  = forumService_.getForum(cat.getId(), forum.getId());
  	// getList Forum
  	List<Forum> forums = forumService_.getForums(cat.getId());
  	assertEquals(forums.size(), 1);
  	// update Forum
  	forumNew.setForumName("Forum update");
  	forumService_.updateForum(cat.getId(), forumNew);
  	assertEquals("Forum update", forumService_.getForum(cat.getId(), forumNew.getId()).getForumName());
  	// test moveForum from cat to cate
  	Category cate = createCategory("idC1");
  	forumService_.createCategory(cate);
  	Category cateNew = forumService_.getCategory("idC1");
  	forumService_.moveForum(forumNew.getPath(), cateNew.getPath() + "/" + forumNew.getId());
  	assertNotNull(forumService_.getForum("idC1", forumNew.getId()));
  	// remove Forum return Forum
  	assertNotNull(forumService_.removeForum("idC1", forumNew.getId()));
  }
  
  public void testTopic() throws Exception {
  	GregorianCalendar calendar = new GregorianCalendar() ;
		String id = String.valueOf(calendar.getTimeInMillis());
		Category cat = createCategory("Cat");
		forumService_.createCategory(cat);
		Forum forum = createdForum(id);
		forumService_.createForum(cat.getId(), forum);
		Topic topic = createdTopic("1111");
		// add Topic
		forumService_.createTopic(cat.getId(), forum.getId(), topic);
		//get Topic
		assertNotNull(forumService_.getTopic(cat.getId(), forum.getId(), topic.getId()));
		//get PageList Topic
		PageList pagelits = forumService_.getTopics(cat.getId(), forum.getId());
		assertEquals(pagelits.getAvailable(), 1);
		// update Topic
		Topic newTopic = forumService_.getTopic(cat.getId(), forum.getId(), topic.getId());
		newTopic.setTopicName("New Name topic");
		forumService_.updateTopic(cat.getId(), forum.getId(), newTopic);
		assertEquals("New Name topic", forumService_.getTopic(cat.getId(), forum.getId(), topic.getId()).getTopicName());
		// move Topic
		// move topic from forum to forum 1
		Forum forum1 = createdForum("2222");
		forumService_.createForum(cat.getId(), forum1);
		forum1 = forumService_.getForum(cat.getId(), forum1.getId());
		forumService_.moveTopic(newTopic.getPath(), forum1.getPath() + "/" + newTopic.getId());
		assertNotNull(forumService_.getTopic(cat.getId(), forum1.getId(), newTopic.getId()));
		//test remove Topic return Topic
		assertNotNull(forumService_.removeTopic(cat.getId(), forum1.getId(), newTopic.getId()));
  }
  
  public void testPost() throws Exception {
  	Category cat = createCategory("cate");
		forumService_.createCategory(cat);
		Forum forum = createdForum("111111");
		forumService_.createForum(cat.getId(), forum);
		Topic topic = createdTopic("222222");
		forumService_.createTopic(cat.getId(), forum.getId(), topic);
		Post post = createdPost("333333");
		//add Post
		forumService_.createPost(cat.getId(), forum.getId(), topic.getId(), post);
		// getPost
		assertNotNull(forumService_.getPost(cat.getId(), forum.getId(), topic.getId(), post.getId()));
		//get ListPost
		List<Post> posts = forumService_.getPosts(cat.getId(), forum.getId(), topic.getId());
		assertEquals(posts.size(), 1);
		// update Post
		Post newPost = forumService_.getPost(cat.getId(), forum.getId(), topic.getId(), post.getId());
		newPost.setMessage("New messenger");
		forumService_.updatePost(cat.getId(), forum.getId(), topic.getId(), newPost);
		assertEquals("New messenger", forumService_.getPost(cat.getId(), forum.getId(), topic.getId(), newPost.getId()).getMessage());
		//test movePost
		Topic topicnew = createdTopic("333334");
		forumService_.createTopic(cat.getId(), forum.getId(), topicnew);
		topicnew = forumService_.getTopic(cat.getId(), forum.getId(), topicnew.getId());
		forumService_.movePost(newPost.getPath(), topicnew.getPath() + "/" + newPost.getId());
		assertNotNull(forumService_.getPost(cat.getId(), forum.getId(), topicnew.getId(), newPost.getId()));
		//test remove Post return post
		assertNotNull(forumService_.removePost(cat.getId(), forum.getId(), topicnew.getId(), newPost.getId()));
		//getViewPost
		System.out.print("\n\n" + topicnew.getViewCount() + "\n\n");
  }
  
  
  
  private Post createdPost( String id) {
		Post post = new Post();
		
		post.setId(id);
		post.setOwner("duytu");
		post.setCreatedDate(new Date());
		post.setModifiedBy("duytu");
		post.setModifiedDate(new Date());
		post.setSubject("SubJect");
		post.setMessage("Noi dung topic test chang co j ");
		post.setRemoteAddr("khongbiet");
		
		return post;
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
		topicNew.setDescription("TopicDescription");
		topicNew.setPostCount(0);
		topicNew.setViewCount(0);
		topicNew.setIsNotifyWhenAddPost(false);
		topicNew.setIsModeratePost(false);
		topicNew.setIsClosed(false);
		topicNew.setIsLock(false);
		  
		return topicNew;
  }
  
  private Forum createdForum(String idf) {
		Forum forum = new Forum();
		forum.setId(idf);
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
		
		forum.setIsNotifyWhenAddTopic(false);
		forum.setIsNotifyWhenAddPost(false);
		forum.setIsModeratePost(false);
		forum.setIsModerateTopic(false);
		forum.setIsClosed(false);
		forum.setIsLock(false);
	  
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