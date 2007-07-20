/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.*;

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
		Category cat = createCategory("ida") ;
		forumService_.createCategory(cat) ;
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
		Category newCat = createCategory("idb");
		forumService_.createCategory(newCat);
		
		String pathRoot = "/exo:registry/exo:services/ForumService/";
		String scrPath = pathRoot + cat.getId() + "/" + forum.getId();
		String destPath = pathRoot+newCat.getId() + "/" + forum.getId();
		forumService_.moveForum(scrPath, destPath);
		Forum forum3 = forumService_.getForum(newCat.getId(), forum.getId());
		System.out.print("\n\nTestMoveForum:  " + forum3.getForumName()+ "\n\n");
		// test remove Forum xoa forum3
		assertNotNull(forumService_.removeForum(newCat.getId(), forum.getId()));
  }
  
  public void testTopic() throws Exception {
		Category cat = createCategory("id0") ;
		forumService_.createCategory(cat) ;
	    Forum forum = createdForum("idf0");
	    forumService_.createForum(cat.getId(), forum);
	    
		Topic topicnew = createdTopic("idtp0");
		
		forumService_.createTopic(cat.getId(), forum.getId(), topicnew);
		//test getTopic
		Topic topic1 = forumService_.getTopic(cat.getId(), forum.getId(), topicnew.getId());
		System.out.print("\n\nTestgetTopic TopicName:  " + topic1.getTopicName() + "\nDescription:  " + topic1.getDescription() + "\n\n");
		//test updateTopic
		topicnew.setTopicName("New Test Topic");
		topicnew.setDescription("Topic nay de test");
		forumService_.updateTopic(cat.getId(), forum.getId(), topicnew);
		Topic topic2 = forumService_.getTopic(cat.getId(), forum.getId(), topicnew.getId());
		System.out.print("\n\nNewTopic TopicName:  " + topic2.getTopicName() + "\nDescriptionNew:  " + topic2.getDescription() + "\n\n");
		/* test moveTopic test 2 truong hop
		* Ta dang co category(id =id0) co Forum(id = idf0) co Topic(id = idtp0)
		* Va can tao them category(id = di1) co Forum(id = idf1) va ko co Topic
		*/
		Category cate = createCategory("id1");
		forumService_.createCategory(cate) ;
		Forum forum1 = createdForum("idf1");
	    forumService_.createForum(cate.getId(), forum1);
	    //truong hop 1
		//forumService_.moveTopic(forum.getId(), topicnew.getId(), forum1.getId());
	  String pathRoot = "/exo:registry/exo:services/ForumService/";
	  String topicPath = pathRoot + cat.getId() + "/" + forum.getId() + "/" + topicnew.getId();
	  String destForumPath = pathRoot + cate.getId() + "/" + forum1.getId() + "/" + topicnew.getId();
	  forumService_.moveTopic(topicPath, destForumPath);
	  Topic topic3 = forumService_.getTopic(cate.getId(), forum1.getId(), topicnew.getId());
		System.out.print("\n\nTopicMove TopicName:  " + topic3.getTopicName() + "\nDescriptionNew:  " + topic3.getDescription() + "\n\n");
		//truong hop 2 move trong cung 1 category
		Forum forum2 = createdForum("idf2");
		forumService_.createForum(cate.getId(), forum2);
		String newdestForumPath = pathRoot + cate.getId() + "/" + forum2.getId() + "/" + topicnew.getId();
		forumService_.moveTopic(destForumPath, newdestForumPath);
		Topic topic4 = forumService_.getTopic(cate.getId(), forum2.getId(), topicnew.getId());
		System.out.print("\n\nTopicMove trong Cate:  " + topic4.getTopicName() + "\nDescriptionNew:  " + topic4.getDescription() + "\n\n");
		//test removeTopic
		Topic testRmTopic = createdTopic("rmtp");
		forumService_.createTopic("id1", "idf1", testRmTopic);
		assertNotNull(forumService_.getTopic("id1", "idf1", testRmTopic.getId()));
		Topic topicTem = forumService_.removeTopic("id1", "idf1", testRmTopic.getId());
		System.out.print("\n\nTopicRemove trong Cate:  " + topicTem.getTopicName() + "\nDescription:  " + topicTem.getDescription() + "\n\n");
  }
  
  public void testPost() throws Exception {
		Post postNew = createdPost("idp0");
		forumService_.createPost("id1", "idf2", "idtp0", postNew);
		//test getPost
		Post post0 = forumService_.getPost("id1", "idf2", "idtp0", postNew.getId());
		System.out.println("\n\n Noidung Post0:" + post0.getMessage());
		//test getPosts
		List<Post> Posts = forumService_.getPosts("id1", "idf2", "idtp0");
		System.out.println("\n\nSo post:  " + Posts.size()+"\n\n");
		// test updatePost
		postNew.setMessage("Noi dung topic da duoc sua chua");
		postNew.setSubject("Subject cung da dc sua chua");
		forumService_.updatePost("id1", "idf2", "idtp0", postNew);
		Post post1 = forumService_.getPost("id1", "idf2", "idtp0", postNew.getId());
		System.out.println("\n\n Noidung Post1:" + post1.getMessage()+"\n SubjectNew:  " + post1.getSubject() + "\n\n");
		// test movePost
		Topic topic1 = createdTopic("idtp1");
		forumService_.createTopic("id1", "idf2", topic1);
		String pathRoot = "/exo:registry/exo:services/ForumService/";
		String srcPath =  pathRoot + "id1/idf2/idtp0/idp0";
		String destPath = pathRoot + "id1/idf2/idtp1/idp0";
		forumService_.movePost(srcPath, destPath);
		Post post2 = forumService_.getPost("id1", "idf2", "idtp1", postNew.getId());
		System.out.println("\n\n Noidung Move Post2:" + post2.getMessage()+"\n SubjectNew:  " + post2.getSubject() + "\n\n");
		//test removePost
		assertNotNull(forumService_.removePost("id1", "idf2", "idtp1", postNew.getId()));
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