/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;


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
    forumService_.saveCategory(cat, true) ;
    // add category
    assertNotNull(forumService_.getCategory("id")) ;
    // get categories
    List<Category> categories = forumService_.getCategories() ;
    assertEquals(categories.size(), 1) ;
    // update category
    cat.setCategoryName("1234567890") ;
    forumService_.saveCategory(cat, false) ;
    Category updatedCat = forumService_.getCategory("id") ;
    assertNotNull(updatedCat) ;
    assertEquals("1234567890", updatedCat.getCategoryName()) ;
    // test removeCategory
    assertNotNull(forumService_.removeCategory("id"));
  }

  public void testForum() throws Exception {
  	Category cat = createCategory("idC0");
  	forumService_.saveCategory(cat, true);
  	
  	GregorianCalendar calendar = new GregorianCalendar() ;
		String id = String.valueOf(calendar.getTimeInMillis());
		
  	Forum forum = createdForum(id);
  	//forum la forum khoi tao
  	// add forum
  	forumService_.saveForum(cat.getId(), forum, true);
  	// getForum
  	assertNotNull(forumService_.getForum(cat.getId(), forum.getId()));
  	Forum forumNew  = forumService_.getForum(cat.getId(), forum.getId());
		// getForumByPath
//  	Forum forumN = (Forum)forumService_.getObjectByPath(forumNew.getPath());
//  	assertEquals(forumN.getDescription(),forumNew.getDescription());
		// getList Forum
  	List<Forum> forums = forumService_.getForums(cat.getId());
  	assertEquals(forums.size(), 1);
  	// update Forum
  	forumNew.setForumName("Forum update");
  	forumService_.saveForum(cat.getId(), forumNew, false);
  	assertEquals("Forum update", forumService_.getForum(cat.getId(), forumNew.getId()).getForumName());
  	// test moveForum from cat to cate
  	Category cate = createCategory("idC1");
  	forumService_.saveCategory(cate, true);
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
		forumService_.saveCategory(cat, true);
		Forum forum = createdForum(id);
		forumService_.saveForum(cat.getId(), forum, true);
		Topic topic = createdTopic("1111");
		// add Topic
		forumService_.saveTopic(cat.getId(), forum.getId(), topic, true);
		//get Topic
		assertNotNull(forumService_.getTopic(cat.getId(), forum.getId(), topic.getId()));
		//get PageList Topic
		JCRPageList pagelist = forumService_.getTopics(cat.getId(), forum.getId());
		assertEquals(pagelist.getAvailable(), 1);
    List list = pagelist.getPage(1, session_) ;
    assertEquals(list.size(), 1);
    List page = pagelist.getPage(1, session_) ;
    assertEquals(page.size(), 1);    
    
		// update Topic
		Topic newTopic = forumService_.getTopic(cat.getId(), forum.getId(), topic.getId());
		newTopic.setTopicName("New Name topic");
		forumService_.saveTopic(cat.getId(), forum.getId(), newTopic, false);
		assertEquals("New Name topic", forumService_.getTopic(cat.getId(), forum.getId(), topic.getId()).getTopicName());
		// move Topic
		// move topic from forum to forum 1
		Forum forum1 = createdForum("2222");
		forumService_.saveForum(cat.getId(), forum1, true);
		forum1 = forumService_.getForum(cat.getId(), forum1.getId());
		forumService_.moveTopic(newTopic.getPath(), forum1.getPath() + "/" + newTopic.getId());
		assertNotNull(forumService_.getTopic(cat.getId(), forum1.getId(), newTopic.getId()));
		//test remove Topic return Topic
		assertNotNull(forumService_.removeTopic(cat.getId(), forum1.getId(), newTopic.getId()));
  }
  
  public void testPost() throws Exception {
  	Category cat = createCategory("cate");
		forumService_.saveCategory(cat, true);
		Forum forum = createdForum("111111");
		forumService_.saveForum(cat.getId(), forum, true);
		Topic topic = createdTopic("222222");
		forumService_.saveTopic(cat.getId(), forum.getId(), topic, true);
		List<Post> posts = new ArrayList<Post>();
		Random rand = new Random();
		for (int i = 0; i < 25; ++i) {
		  Post post = createdPost(String.valueOf(rand.nextInt(99999999)));
		  posts.add(post);
		  forumService_.savePost(cat.getId(), forum.getId(), topic.getId(), post, true);
		}
		// getPost
		assertNotNull(forumService_.getPost(cat.getId(), forum.getId(), topic.getId(), posts.get(0).getId()));
		// TopicView
		TopicView topicView = forumService_.getTopicView(cat.getId(), forum.getId(), topic.getId());
		Topic topi = forumService_.getTopic(cat.getId(), forum.getId(), topic.getId());
		//get ListPost
		JCRPageList pagePosts = topicView.getPageList();//forumService_.getPosts(cat.getId(), forum.getId(), topic.getId());
		assertEquals(pagePosts.getAvailable(), posts.size() + 1);// size = 26 (first post and new postList)
    List page1 = pagePosts.getPage(1, session_) ;
    assertEquals(page1.size(), 10);  
    List page2 = pagePosts.getPage(2, session_) ;
    assertEquals(page2.size(), 10);  
    List page3 = pagePosts.getPage(3, session_) ;
    assertEquals(page3.size(), 6);
		// update Post First
		Post newPost = (Post)pagePosts.getPage(1, session_).get(0);
		newPost.setMessage("New messenger");
		forumService_.savePost(cat.getId(), forum.getId(), topic.getId(), newPost, false);
		assertEquals("New messenger", forumService_.getPost(cat.getId(), forum.getId(), topic.getId(), newPost.getId()).getMessage());
//		List<Post> posts1 = topicView.getAllPost(session_);
//		for (int i = 0; i < posts1.size(); i++) {
//			System.out.print("\n" + posts1.get(i).getId() + "\n");
//		}
//		Post testp = (Post)forumService_.getObjectByPath(newPost.getPath());
//		assertEquals(testp.getMessage(), newPost.getMessage());
		//test movePost
		
		Topic topicnew = createdTopic("333334");
		forumService_.saveTopic(cat.getId(), forum.getId(), topicnew, true);
		topicnew = forumService_.getTopic(cat.getId(), forum.getId(), topicnew.getId());
		forumService_.movePost(newPost.getPath(), topicnew.getPath() + "/" + newPost.getId());
		assertNotNull(forumService_.getPost(cat.getId(), forum.getId(), topicnew.getId(), newPost.getId()));
		//test remove Post return post
		assertNotNull(forumService_.removePost(cat.getId(), forum.getId(), topicnew.getId(), newPost.getId()));
//		//getViewPost
//		System.out.print("\n\n" + topicnew.getViewCount() + "\n\n");
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
		post.setIcon("classNameIcon");
		post.setIsApproved(false);
		
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
		topicNew.setIcon("classNameIcon");
		topicNew.setIsApproved(false);  
		topicNew.setViewPermissions(new String[] {});
		topicNew.setEditPermissions(new String[] {});
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
//		forum.setLastPostBy("duytu");
//		forum.setLastPostDate(new Date());
		forum.setLastPostPath("");
		forum.setDescription("description");
		forum.setPostCount(0);
		forum.setTopicCount(0);
		
		forum.setNotifyWhenAddTopic(new String[] {});
		forum.setNotifyWhenAddPost(new String[] {});
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