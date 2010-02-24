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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.rest.xml.RESTOrganizationServiceXMLImpl;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.RequestDispatcher;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Created by The eXo Platform SARL Author : Volodymyr Krasnikov
 * volodymyr.krasnikov@exoplatform.com.ua
 */

public class XMLResponseOrgserviceTest extends AbstractResourceTest {

  //StandaloneContainer             container;

  OrganizationService             orgService;

  RESTOrganizationServiceXMLImpl xmlOrgService;

  //ResourceDispatcher              dispatcher;

  static final String             baseURI = "http://localhost:8080/rest/";

  public void setUp() throws Exception {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    super.setUp();
    orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    xmlOrgService = (RESTOrganizationServiceXMLImpl) container.getComponentInstanceOfType(RESTOrganizationServiceXMLImpl.class);

  }

  public void tearDown() throws Exception {
    super.tearDown();
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

    String extURI = "/organization/xml/membership/view-all/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);
    
    MembershipHandler mHandler = orgService.getMembershipHandler();
    Collection<Membership> mms = mHandler.findMembershipsByUser(username);
    
    String username_ = document.getElementsByTagName("user-name").item(0).getTextContent();
    assertEquals(username, username_);
    
    NodeList nodeList = document.getElementsByTagName("membership");
    
    int i = 0;
    for(Iterator<Membership>it = mms.iterator(); it.hasNext(); ){
      
      Membership m = it.next();
      Node node = nodeList.item(i++);
      //gets membership data
      NodeList m_list = node.getChildNodes();
      Node id = m_list.item(0);
      
      Node type = m_list.item(1);
      assertEquals(m.getMembershipType(), type.getTextContent());
      
      Node groupId = m_list.item(2);
      assertEquals(m.getGroupId(), groupId.getTextContent());
      
      Node user_name = m_list.item(3);
      assertEquals(m.getUserName(), user_name.getTextContent());
      
    }
    
  }

//  public void testFindUsers() throws Exception {
//
//    UserHandler hUser = orgService.getUserHandler();
//
//    MultivaluedMetadata mv = new MultivaluedMetadata();
//    MultivaluedMetadata qp = new MultivaluedMetadata();
//    // admin - user from DummyOrganizationService
//    String username = "admin";
//
//    qp.putSingle("username", username);
//
//    String extURI = "/organization/xml/user/find-all/";
//
//    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);
//
//    Response response = null;
//    response = dispatcher.dispatch(request);
//    assertNotNull(response);
//    assertEquals(HTTPStatus.OK, response.getStatus());
//    
//    final SerializableEntity entry = (SerializableEntity)response.getEntity();
//    final PipedOutputStream po = new PipedOutputStream();
//    final PipedInputStream pi = new PipedInputStream(po);
//    new Thread(){
//      @Override
//      public void run() {
//        try {
//          entry.writeObject(po);
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//        try {
//          po.flush();
//          po.close();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//      }
//    }.start();
//    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//    factory.setNamespaceAware(true);        
//    DocumentBuilder builder = factory.newDocumentBuilder();
//    Document document = builder.parse(pi);
//    
//
//    Query query = new Query();
//    query.setUserName(username);
//    Collection<User> list = hUser.findUsers(query).getAll();
//    UserListBean user_list_bean = new UserListBean(list);
//    
//    NodeList nodeList = document.getElementsByTagName("user");
//    
//    int i = 0;
//    for(Iterator<User>it = list.iterator(); it.hasNext(); ){
//      
//      User u = it.next();
//      Node node = nodeList.item(i++);
//      //gets membership data
//      NodeList u_list = node.getChildNodes();
//      
//      Node name = u_list.item(0);
//      assertEquals(u.getUserName(), name.getTextContent() );
//      
//      Node first = u_list.item(1);
//      if(u.getFirstName() == null)
//        assertEquals(first.getTextContent(), "");
//      else assertEquals(first.getTextContent(), u.getFirstName());
//      
//      Node last = u_list.item(2);
//      if(u.getLastName() == null)
//        assertEquals(last.getTextContent(), "");
//      else assertEquals(u.getLastName(), last.getTextContent());
//      
//      Node email = u_list.item(3);
//      if(u.getEmail() == null)
//        assertEquals(email.getTextContent(), "");
//      else assertEquals(u.getEmail(), email.getTextContent());
//      
//    }
//
//
//
//  }

  public void testFindUsersRange() throws Exception {

    UserHandler hUser = orgService.getUserHandler();

    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    // admin - user from DummyOrganizationService
    String username = "admin";

    h.putSingle("username", username);

    Integer from = 0, to = 5;

    String extURI = String.format("/organization/xml/user/view-from-to/%s/%s/", from.toString(),
        to.toString());
    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "POST", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);

    
    Query query = new Query();
    query.setUserName(username);

    Collection<User> list = hUser.findUsers(query).getAll().subList(from, to);
    
    NodeList nd_list = document.getElementsByTagName("user");
    
    assertEquals( nd_list.getLength(), list.size() );
    

  }
  

  public void testGetAllGroup() throws Exception {

    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();

    String group_exclude = "";
    h.putSingle("filter", group_exclude);

    String extURI = "/organization/xml/group/filter/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);

    GroupHandler hGroup = orgService.getGroupHandler();
    Collection<Group> groups = hGroup.getAllGroups();
    
    NodeList nd_list = document.getElementsByTagName("group");
    
    // is quantity of nodes equals ?? 
    assertEquals(groups.size(), nd_list.getLength() );
    
    // test content
    int i = 0;
    for(Iterator<Group> iterator = groups.iterator(); iterator.hasNext(); ){
      String groupName = iterator.next().getGroupName();
      assertEquals(groupName, nd_list.item(i++).getTextContent());
    }
    

  }

  public void testGetGroup() throws Exception {
    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();

    String group_id = "/admin";
    String extURI = "/organization/xml/group/info/" + "admin";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);

    GroupHandler hGroup = orgService.getGroupHandler();
    UserHandler hUser = orgService.getUserHandler();
    
    Collection<User> members = hUser.findUsersByGroup(group_id).getAll();
    Group group = hGroup.findGroupById(group_id);
    
    NodeList nd_list = document.getElementsByTagName("name");
    assertEquals( nd_list.item(0).getTextContent(), group.getGroupName() );
    
    nd_list = document.getElementsByTagName("membership");
    
    assertEquals(members.size(), nd_list.getLength());
    
    
  }

  public void testGetGroupsCount() throws Exception {
    //MultivaluedMetadata mv = new MultivaluedMetadata();
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String extURI = "/organization/xml/group/count/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);

    GroupHandler hGroup = orgService.getGroupHandler();
    int quantity = hGroup.getAllGroups().size();

    String number = document.getElementsByTagName("number").item(0).getTextContent();

    assertEquals(quantity, Integer.parseInt(number));
  }

  public void testGetGroupsOfUser() throws Exception {
    /*MultivaluedMetadata mv = new MultivaluedMetadata();
    MultivaluedMetadata qp = new MultivaluedMetadata();*/
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "admin";
    h.putSingle("username", username);

    String extURI = "/organization/xml/group/groups-for-user/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, qp);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);

    GroupHandler hGroup = orgService.getGroupHandler();
    Collection<Group> groups = hGroup.findGroupsOfUser(username);
    
    int size = document.getElementsByTagName("group").getLength();
    
    assertEquals(size, groups.size() );
    
    
  }

  public void testGetUser() throws Exception {
    //MultivaluedMetadata mv = new MultivaluedMetadata();
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "admin";

    String extURI = String.format("/organization/xml/user/info/%s/", username);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);

    UserHandler hUser = orgService.getUserHandler();
    User user = hUser.findUserByName(username);
    
    NamedNodeMap map = document.getElementsByTagName("user").item(0).getAttributes();
    
    String _username = map.getNamedItem("user-name").getTextContent();
    
    assertEquals( _username, user.getUserName());
    
  }
  

  public void testGetUsers() throws Exception {
    //MultivaluedMetadata mv = new MultivaluedMetadata();
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String extURI = "/organization/xml/users/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);

    UserHandler hUser = orgService.getUserHandler();
    Collection<User> user_list = hUser.findUsers(new Query()).getAll();
    
    int size = document.getElementsByTagName("user").getLength();
    
    assertEquals(size, user_list.size() );
    
  }

  public void testGetUsersCount() throws Exception {
    //MultivaluedMetadata mv = new MultivaluedMetadata();
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String extURI = "/organization/xml/user/count/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);

    UserHandler hUser = orgService.getUserHandler();
    int quantity = hUser.findUsers(new Query()).getAll().size();
    
    String number = document.getElementsByTagName("number").item(0).getTextContent();

    assertEquals(quantity, Integer.parseInt(number));

  }

  public void testUsersRange() throws Exception {
    //MultivaluedMetadata mv = new MultivaluedMetadata();
	MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    Integer offset = 0, amount = 5;
    String extURI = String.format("/organization/xml/user/view-range/%s/%s/", offset, amount);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    /*Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);

    Response response = null;
    response = dispatcher.dispatch(request);*/
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    final StreamingOutput entry = (StreamingOutput)response.getEntity();
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread(){
      @Override
      public void run() {
        try {
          entry.write(po);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          po.flush();
          po.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(pi);

    UserHandler userHandler = orgService.getUserHandler();
    
    List<User> list = userHandler.findUsers(new Query()).getAll();
    int prevFrom = -1;
    if (offset > 0)
      prevFrom = ((offset - amount) > 0) ? offset - amount : 0;
    int nextFrom = ((offset + amount) < list.size()) ? offset + amount : -1;
    int to = (offset + amount < list.size()) ? offset + amount : list.size();
    
    int size = list.subList(offset, to).size();
    
    int user_list_size = document.getElementsByTagName("user").getLength();
    
    assertEquals(size, user_list_size);
        
  }

}
