/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.organization.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.rest.json.CountBean;
import org.exoplatform.services.organization.rest.json.GroupListBean;
import org.exoplatform.services.organization.rest.json.GroupMembersBean;
import org.exoplatform.services.organization.rest.json.MembershipListBean;
import org.exoplatform.services.organization.rest.json.RESTOrganizationServiceJSONImpl;
import org.exoplatform.services.organization.rest.json.UserBean;
import org.exoplatform.services.organization.rest.json.UserListBean;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;

/**
 * Created by The eXo Platform SARL Author : Volodymyr Krasnikov
 * volodymyr.krasnikov@exoplatform.com.ua
 */

public class JsonResponseOrgserviceTest extends AbstractResourceTest {

  //StandaloneContainer             container;

  OrganizationService             orgService;

  RESTOrganizationServiceJSONImpl jsonOrgService;

  //ResourceDispatcher              dispatcher;

  static final String             baseURI = "http://localhost:8080/rest/";

  public void setUp() throws Exception {
    super.setUp();
    orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    jsonOrgService = (RESTOrganizationServiceJSONImpl) container.getComponentInstanceOfType(RESTOrganizationServiceJSONImpl.class);

  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  // not implemented by DummyOrganizationService
  public void testCreateGroup() throws Exception {
  }

  // not implemented by DummyOrganizationServiceOrganizationService
  public void testCreateMembership() throws Exception {
  }

  // not implemented by DummyOrganizationService
  public void testCreateUser() throws Exception {
  }

  // not implemented by DummyOrganizationService
  public void testDeleteGroup() throws Exception {
  }

  // not implemented by DummyOrganizationService
  public void testDeleteMembership() throws Exception {
  }

  // not implemented by DummyOrganizationService
  public void testDeleteUser() throws Exception {
  }

  // not implemented by DummyOrganizationService
  public void testDeleteUserFromGroup() throws Exception {
  }

  // not supported by DummyOrganizationServiceOrganizationService
  public void testFindMembership() throws Exception {
  }

  // not implemented by DummyOrganizationService
  public void testGetGroups() throws Exception {
  }
  
  //not implemented by DummyOrganizationService
  public void testUpdateGroup() throws Exception {
  }
  
  //not implemented by DummyOrganizationService
  public void testUpdateUser() throws Exception {
  }

  /**
   * However methods findMembershipsByUserAndGroup,
   * findMembershipByUserGroupAndType, findMembershipsByGroup are not 
   * implemented by DummyOrganizationService,
   * we should pass "username" parameter only by QueryParam!
   * others parameters are groupId and type 
   */
  public void testFindMemberships() throws Exception {

    MembershipHandler hMembership = orgService.getMembershipHandler();

    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    // admin - user from DummyOrganizationService
    String username = "admin";

    h.putSingle("username", username);
    String extURI = "/organization/json/membership/view-all/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    Collection<Membership> list = hMembership.findMembershipsByUser(username);
    MembershipListBean wrapper = new MembershipListBean(username, list);

    MembershipListBean entity = (MembershipListBean) response.getEntity();

    //See overrided method "equals" in MembershipListBean
    assertEquals(wrapper, entity);

  }

  public void testFindUsers() throws Exception {

    UserHandler hUser = orgService.getUserHandler();

    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    // admin - user from DummyOrganizationService
    String username = "admin";

    h.putSingle("username", username);
    String extURI = "/organization/json/user/find-all/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    UserListBean entity = (UserListBean) response.getEntity();

    Query query = new Query();
    query.setUserName(username);

    Collection<User> list = hUser.findUsers(query).getAll();
    List<UserBean> listBean = new ArrayList<UserBean>();
    for (User user : list) {
      if (user != null)
        listBean.add(new UserBean(user));
    }
    UserListBean user_list_bean = new UserListBean(listBean);
    
//    UserListBean user_list_bean = new UserListBean(list);

    //See overrided method "equals" in UserListBean
    assertEquals(user_list_bean, entity);

  }

  public void testFindUsersRange() throws Exception {

    UserHandler hUser = orgService.getUserHandler();

    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    // admin - user from DummyOrganizationService
    String username = "admin";

    h.putSingle("username", username);

    Integer from = 0, to = 5;

    String extURI = String.format("/organization/json/user/view-from-to/%s/%s/", from.toString(),
        to.toString());
    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "POST", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    UserListBean entity = (UserListBean) response.getEntity();

    Query query = new Query();
    query.setUserName(username);

    Collection<User> list = hUser.findUsers(query).getAll().subList(from, to);
    
    List<UserBean> listBean = new ArrayList<UserBean>();
    for (User user : list) {
      if (user != null)
        listBean.add(new UserBean(user));
    }
    UserListBean user_list_bean = new UserListBean(listBean);
    
//    UserListBean user_list_bean = new UserListBean(list);

    // See overrided method "equals" in UserListBean
    assertEquals(user_list_bean, entity);

  }
  
  
  public void testFindUsersRange2() throws Exception {

    UserHandler hUser = orgService.getUserHandler();

    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    // admin - user from DummyOrganizationService
    String username = "admin";

    h.putSingle("username", username);

    Integer from = 0, to = 10;

    String extURI = "/organization/json/user/find-user-in-range/";
    h.putSingle("question", "*");
    h.putSingle("from", "0");
    h.putSingle("to", "10");
    h.putSingle("sort-field", "lastname");
    h.putSingle("sort-order", "descending");
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    UserListBean entity = (UserListBean) response.getEntity();
     
    Collection<UserBean> collection = entity.getUsers();
    for (UserBean userBean : collection) {
      System.out.println("JsonResponseOrgserviceTest.testFindUsersRange2()" + userBean.getUserName() + " : " + userBean.getFirstName() + " : " + userBean.getLastName());
    }
    
    Query query = new Query();
    query.setUserName(username);

    Collection<User> list = hUser.findUsers(query).getAll().subList(from, to);
    
    List<UserBean> listBean = new ArrayList<UserBean>();
    for (User user : list) {
      if (user != null)
        listBean.add(new UserBean(user));
    }
    UserListBean user_list_bean = new UserListBean(listBean);
    
//    UserListBean user_list_bean = new UserListBean(list);

    // See overrided method "equals" in UserListBean
    assertEquals(user_list_bean, entity);

  }

  public void testGetAllGroup() throws Exception {

    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String group_exclude = "";
    h.putSingle("filter", group_exclude);

    String extURI = "/organization/json/group/filter/";

    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    GroupListBean entity = (GroupListBean) response.getEntity();

    GroupHandler hGroup = orgService.getGroupHandler();
    Collection<Group> groups = hGroup.getAllGroups();

    GroupListBean groupsBean = new GroupListBean(groups);
    
    // See overrided method "equals" in GroupListBean
    assertEquals(entity, groupsBean);

  }

  public void testGetGroup() throws Exception {
    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String group_id = "/admin";
    
    String extURI = "/organization/json/group/info/" + "admin";

    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    
   /* Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    GroupMembersBean entity = (GroupMembersBean) response.getEntity();

    GroupHandler hGroup = orgService.getGroupHandler();
    UserHandler hUser = orgService.getUserHandler();
    
    Collection<User> members = hUser.findUsersByGroup(group_id).getAll();
    Group group = hGroup.findGroupById(group_id);
    
    GroupMembersBean groupMembersBean = new GroupMembersBean(group, members);

    // See overrided method "equals" in GroupMembersBean
    assertEquals(entity, groupMembersBean );
  }

  public void testGetGroupsCount() throws Exception {
    //MultivaluedMetadata mv = new MultivaluedMetadata();
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String extURI = "/organization/json/group/count/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    CountBean entity = (CountBean) response.getEntity();

    GroupHandler hGroup = orgService.getGroupHandler();
    int quantity = hGroup.getAllGroups().size();

    CountBean groupsBean = new CountBean(quantity);

  //See overrided method "equals" in CountBean
    assertEquals(entity, groupsBean);
  }

  public void testGetGroupsOfUser() throws Exception {
    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
	MultivaluedMap<String, String> h = new MultivaluedMapImpl(); 

    String username = "admin";
    h.putSingle("username", username);

    String extURI = "/organization/json/group/groups-for-user/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    GroupListBean entity = (GroupListBean) response.getEntity();

    GroupHandler hGroup = orgService.getGroupHandler();
    Collection<Group> groups = hGroup.findGroupsOfUser(username);

    GroupListBean groupsBean = new GroupListBean(groups);
    
  //See overrided method "equals" in GroupListBean
    assertEquals(entity, groupsBean);
  }

//  Find group is not supported by DummyOrganizationService
//
//  public void testGetGroupsRange_NullParentID() throws Exception {
//    MultivaluedMetadata mv = new MultivaluedMetadata();
//    MultivaluedMetadata qp = new MultivaluedMetadata();
//
//    Integer from = 0, to = 5;
//    String extURI = String.format("/organization/json/group/view-from-to/%s/%s/", from, to);
//
//    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);
//
//    Response response = null;
//    response = dispatcher.dispatch(request);
//    assertNotNull(response);
//    assertEquals(HTTPStatus.OK, response.getStatus());
//
//    GroupListBean entity = (GroupListBean) response.getEntity();
//
//    GroupHandler hGroup = orgService.getGroupHandler();
//    Collection<Group> groups = hGroup.findGroups(null);
//
//    GroupListBean groupsBean = new GroupListBean(groups);
//
//    assertEquals(entity, groupsBean);
//  }
//
//  public void testGetGroupsRange_NotNullParentID() throws Exception {
//    MultivaluedMetadata mv = new MultivaluedMetadata();
//    MultivaluedMetadata qp = new MultivaluedMetadata();
//
//    String parentId = "admin";
//    qp.putSingle("parentId", parentId);
//
//    Integer from = 0, to = 10;
//    String extURI = String.format("/organization/json/group/view-from-to/%s/%s/", from, to);
//
//    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);
//
//    Response response = null;
//    response = dispatcher.dispatch(request);
//    assertNotNull(response);
//    assertEquals(HTTPStatus.OK, response.getStatus());
//
//    GroupListBean entity = (GroupListBean) response.getEntity();
//
//    GroupHandler hGroup = orgService.getGroupHandler();
//    Group parent = hGroup.findGroupById(parentId);
//
//    assertNotNull(parent);
//
//    Collection<Group> groups = hGroup.findGroups(parent);
//
//    GroupListBean groupsBean = new GroupListBean(groups);
//
//    assertEquals(entity, groupsBean);
//  }

//  MembershipTypes is not supported by DummyOrganizationService
  
//  public void testGetMembershipsTypes() throws Exception {
//    MultivaluedMetadata mv = new MultivaluedMetadata();
//
//    String extURI = "/organization/json/membership/types/";
//
//    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
//
//    Response response = null;
//    response = dispatcher.dispatch(request);
//    assertNotNull(response);
//    assertEquals(HTTPStatus.OK, response.getStatus());
//
//    MembershipTypesListBean entity = (MembershipTypesListBean) response.getEntity();
//
//    MembershipTypeHandler hMembershipType = orgService.getMembershipTypeHandler();
//
//    Collection<MembershipType> membershipTypes = hMembershipType.findMembershipTypes();
//
//    MembershipTypesListBean membership_types_list_bean = new MembershipTypesListBean(
//        membershipTypes);
//
//    assertEquals(entity, membership_types_list_bean);
//  }

  public void testGetUser() throws Exception {
    //MultivaluedMetadata mv = new MultivaluedMetadata();
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "admin";

    String extURI = String.format("/organization/json/user/info/%s/", username);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    User entity = (User) response.getEntity();

    UserHandler hUser = orgService.getUserHandler();
    User user = hUser.findUserByName(username);

    assertEquals(entity, user);

  }

  public void testGetUsers() throws Exception {
    //MultivaluedMetadata mv = new MultivaluedMetadata();
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String extURI = "/organization/json/users/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    UserListBean entity = (UserListBean) response.getEntity();

    UserHandler hUser = orgService.getUserHandler();
    Collection<User> user_list = hUser.findUsers(new Query()).getAll();
    
    List<UserBean> listBean = new ArrayList<UserBean>();
    for (User user : user_list) {
      if (user != null)
        listBean.add(new UserBean(user));
    }
    UserListBean user_list_bean = new UserListBean(listBean);
    
//    UserListBean user_list_bean = new UserListBean(user_list);

    assertEquals(entity, user_list_bean);
  }

  public void testGetUsersCount() throws Exception {
    //MultivaluedMetadata mv = new MultivaluedMetadata();
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String extURI = "/organization/json/user/count/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    CountBean entity = (CountBean) response.getEntity();

    UserHandler hUser = orgService.getUserHandler();
    int quantity = hUser.findUsers(new Query()).getAll().size();

    CountBean usersBean = new CountBean(quantity);

    assertEquals(entity, usersBean);
  }

//  public void testUsersRange() throws Exception {
//    MultivaluedMetadata mv = new MultivaluedMetadata();
//
//    Integer offset = 0, amount = 5;
//    String extURI = String.format("/organization/json/user/view-range/%s/%s/", offset, amount);
//
//    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
//
//    Response response = null;
//    response = dispatcher.dispatch(request);
//    assertNotNull(response);
//    assertEquals(HTTPStatus.OK, response.getStatus());
//
//    UserListBean entity = (UserListBean) response.getEntity();
//
//    UserHandler userHandler = orgService.getUserHandler();
//    
//    List<User> list = userHandler.findUsers(new Query()).getAll();
//    int prevFrom = -1;
//    if (offset > 0)
//      prevFrom = ((offset - amount) > 0) ? offset - amount : 0;
//    int nextFrom = ((offset + amount) < list.size()) ? offset + amount : -1;
//    int to = (offset + amount < list.size()) ? offset + amount : list.size();
//
//    UserListBean user_list_bean = new UserListBean(list.subList(offset, to));
//    
//
//    assertEquals(entity, user_list_bean );
//  }

}
