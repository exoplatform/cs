/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.organization.rest.html;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.rest.RESTOrganizationServiceAbstractImpl;
import org.exoplatform.services.organization.rest.xml.RESTOrganizationServiceXMLImpl;
import org.exoplatform.services.rest.ContextParam;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.SerializableEntity;
import org.exoplatform.services.rest.transformer.SerializableTransformer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.rest.transformer.XSLT4SourceOutputTransformer;
import org.exoplatform.services.rest.transformer.XSLTConstants;

/**
 * Created by The eXo Platform SAS .</br> Note: we use
 * 
 * @QueryTemplate annotation to simplify XSL
 * @author Gennady Azarenkov
 * @version $Id:$
 */
@URITemplate("/organization/")
public class RESTOrganizationServiceHTMLImpl extends RESTOrganizationServiceAbstractImpl implements
    ResourceContainer {

  private static final Log       LOGGER                      = ExoLogger
                                                                 .getLogger(RESTOrganizationServiceHTMLImpl.class);

  RESTOrganizationServiceXMLImpl xmlImpl;

  protected static final String  GROUP_INFO_SCHEMA           = "group-info";

  protected static final String  GROUP_EDIT_SCHEMA           = "group-edit";

  protected static final String  GROUPS_LIST_SCHEMA          = "groups-list";

  protected static final String  GROUPS_LIST_FRAGMENT_SCHEMA = "groups-list-fragment";

  protected static final String  USER_INFO_SCHEMA            = "user-info";

  protected static final String  USERS_LIST_SCHEMA           = "users-list";

  protected static final String  USERS_LIST_SEARCH_SCHEMA    = "users-list-search";

  protected static final String  MEMBERSHIP_LIST_SCHEMA      = "memberships-list";

  public RESTOrganizationServiceHTMLImpl(OrganizationService organizationService) {
    super(organizationService);

    xmlImpl = new RESTOrganizationServiceXMLImpl(organizationService);
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/group/create/")
  public Response createGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("groupName")
  String groupName, @QueryParam("label")
  String label, @QueryParam("description")
  String description, @QueryParam("parentId")
  String parentId) {

    Response response = xmlImpl.createGroup(baseURI, groupName, label, description, parentId);
    return response;

  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/membership/create/")
  public Response createMembership(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("username")
  String username, @QueryParam("groupId")
  String groupId, @QueryParam("type")
  String type) {

    Response response = xmlImpl.createMembership(baseURI, username, groupId, type);
    return response;

  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/user/create/")
  public Response createUser(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("username")
  String username, @QueryParam("password")
  String password, @QueryParam("firstname")
  String firstname, @QueryParam("lastname")
  String lastname, @QueryParam("email")
  String email) {

    Response response = xmlImpl.createUser(baseURI, username, password, firstname, lastname, email);
    return response;

  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/delete/")
  public Response deleteGroup(@QueryParam("groupId")
  String groupId) {
    Response response = xmlImpl.deleteGroup(groupId);
    return response;
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/delete/{membershipId}/")
  public Response deleteMembership(@URIParam("membershipId")
  String membershipId) {

    Response response = xmlImpl.deleteMembership(membershipId);
    return response;

  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/delete/{username}/")
  public Response deleteUser(@URIParam("username")
  String username) {

    Response response = xmlImpl.deleteUser(username);
    return response;

  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/delete-user/")
  public Response deleteUserFromGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("groupId")
  String groupId, @QueryParam("username")
  String username) {

    Response response = xmlImpl.deleteUserFromGroup(baseURI, groupId, username);
    return response;

  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/info/{membershipId}/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response findMembership(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @URIParam("membershipId")
  String membershipId) {

    Response response = xmlImpl.findMembership(baseURI, membershipId);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, MEMBERSHIP_LIST_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/view-all/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response findMemberships(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("groupId")
  String groupId, @QueryParam("username")
  String username, @QueryParam("type")
  String type) {
    Response response = xmlImpl.findMemberships(baseURI, groupId, username, type);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, MEMBERSHIP_LIST_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/find-all/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response findUsers(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("username")
  String username, @QueryParam("firstname")
  String firstname, @QueryParam("lastname")
  String lastname, @QueryParam("email")
  String email, @QueryParam("fromLoginDate")
  String fromLoginDate, @QueryParam("toLogindate")
  String toLoginDate) {

    Response response = xmlImpl.findUsers(baseURI, username, firstname, lastname, email,
        fromLoginDate, toLoginDate);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, USERS_LIST_SEARCH_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/user/find-from-to/{from}/{to}/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response findUsersRange(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("username")
  String username, @QueryParam("firstname")
  String firstname, @QueryParam("lastname")
  String lastname, @QueryParam("email")
  String email, @QueryParam("fromLoginDate")
  String fromLoginDate, @QueryParam("toLogindate")
  String toLoginDate, @URIParam("from")
  Integer offset, @URIParam("to")
  Integer amount) {
    Response response = xmlImpl.findUsersRange(baseURI, username, firstname, lastname, email,
        fromLoginDate, toLoginDate, offset, amount);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, USERS_LIST_SEARCH_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/filter/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response getAllGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("filter")
  String filter) {

    Response response = xmlImpl.getAllGroup(baseURI, filter);
    if (response.getStatus() != HTTPStatus.OK)
      return response;

    Object entity = response.getEntity();

    ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
    String buff = "";
    try {
      ((SerializableEntity) entity).writeObject(out);
      buff = new String(out.toByteArray());
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Response.Builder.ok(buff).mediaType("text/xml").build();

  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/info/{groupId}/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response getGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @URIParam("groupId")
  String groupId) {

    Response response = xmlImpl.getGroup(baseURI, groupId);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {

      String schemaName = GROUP_INFO_SCHEMA;

      return modifyResponse(response, schemaName);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/edit/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response editGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("groupId")
  String groupId) {
    Response response = xmlImpl.getGroup(baseURI, groupId);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {

      String schemaName = GROUP_EDIT_SCHEMA;

      return modifyResponse(response, schemaName);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/view-all/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response getGroups(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("parentId")
  String parentId) {

    Response response = xmlImpl.getGroups(baseURI, parentId);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {

      String schema_name;
      if (parentId == null)
        schema_name = GROUPS_LIST_SCHEMA;
      else
        schema_name = GROUPS_LIST_FRAGMENT_SCHEMA;

      return modifyResponse(response, schema_name);

    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/count/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response getGroupsCount() {

    Response response = xmlImpl.getGroupsCount();

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, GROUPS_LIST_FRAGMENT_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/groups-for-user/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response getGroupsOfUser(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("username")
  String username) {

    Response response = xmlImpl.getGroupsOfUser(baseURI, username);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, GROUPS_LIST_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/view-from-to/{from}/{to}/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response getGroupsRange(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @URIParam("from")
  Integer offset, @URIParam("to")
  Integer amount, @QueryParam("parentId")
  String parentId) {
    Response response = xmlImpl.getGroupsRange(baseURI, offset, amount, parentId);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, GROUPS_LIST_FRAGMENT_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }
  
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/get-types/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getMembershipTypes() {

    Response response = xmlImpl.getMembershipTypes();
    return response;

  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/info/{username}/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response getUser(@URIParam("username")
  String username) {
    Response response = xmlImpl.getUser(username);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, USER_INFO_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/users/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response getUsers(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI) {

    Response response = xmlImpl.getUsers(baseURI);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, USERS_LIST_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/count/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response getUsersCount() {
    Response response = xmlImpl.getUsersCount();

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, GROUPS_LIST_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/view-range/{from}/{number}/")
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response getUsersRange(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @URIParam("from")
  Integer offset, @URIParam("number")
  Integer amount) {

    Response response = xmlImpl.getUsersRange(baseURI, offset, amount);

    if (response.getStatus() != HTTPStatus.OK)
      return response;
    try {
      return modifyResponse(response, USERS_LIST_SCHEMA);
    } catch (IOException e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/group/update/")
  public Response updateGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("groupId")
  String groupId, @QueryParam("name")
  String name, @QueryParam("label")
  String label, @QueryParam("description")
  String description) {

    Response response = xmlImpl.updateGroup(baseURI, groupId, name, label, description);
    return response;

  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/user/update/")
  public Response updateUser(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
  String baseURI, @QueryParam("username")
  String username, @QueryParam("password")
  String password, @QueryParam("firstname")
  String firstname, @QueryParam("lastname")
  String lastname, @QueryParam("email")
  String email) {

    Response response = xmlImpl.updateUser(baseURI, username, password, firstname, lastname, email);
    return response;

  }

  private Response modifyResponse(final Response response, String xsltSchemaName)
      throws IOException {
    response.setTransformer(new SerializableTransformer());
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread() {
      public void run() {
        try {
          response.writeEntity(po);
        } catch (IOException e) {
          LOGGER.error("Thrown exception : " + e);
        } finally {
          try {
            po.flush();
            po.close();
          } catch (IOException e) {
            ;
          }
        }
      }
    }.start();
    StreamSource s = new StreamSource(pi);
    Map<String, String> p = new HashMap<String, String>();
    p.put(XSLTConstants.XSLT_TEMPLATE, xsltSchemaName);
    return Response.Builder.ok(s, "text/html").setTransformerParameters(p).build();
  }
}
