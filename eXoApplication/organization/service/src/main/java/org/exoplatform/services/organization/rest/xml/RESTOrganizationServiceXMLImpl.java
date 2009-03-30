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

package org.exoplatform.services.organization.rest.xml;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.rest.RESTOrganizationServiceAbstractImpl;
import org.exoplatform.services.rest.ContextParam;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.SerializableTransformer;

/**
 * Created by The eXo Platform SAS .
 * @author Gennady Azarenkov
 * @version $Id:$
 */

@URITemplate("/organization/xml/")
public class RESTOrganizationServiceXMLImpl extends
    RESTOrganizationServiceAbstractImpl implements ResourceContainer {
  
  protected final static String XML_CONTENT_TYPE = "text/xml";

  public RESTOrganizationServiceXMLImpl(OrganizationService organizationService) {
    super(organizationService);
  }

  // Group handler methods

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/group/create/")
  public Response createGroup(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("groupName")
      String groupName, @QueryParam("label")
      String label, @QueryParam("description")
      String description, @QueryParam("parentId")
      String parentId) {
    
    Response response = super.createGroup(baseURI, groupName, label, description, parentId); 
    if(response.getStatus() == HTTPStatus.INTERNAL_ERROR)
      return response;
    
    //get group id and return link to new group.
    String id = ((Group)response.getEntity()).getId();
    return Response.Builder.created(
        baseURI + "/organization/xml/group/info" + id).build();
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/membership/create/")
  public Response createMembership(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("username")
      String username, @QueryParam("groupId")
      String groupId, @QueryParam("type")
      String type) {
    Response response = super
        .createMembership(baseURI, username, groupId, type);
    if (response != null)
      return response;

    String id = ((Membership)response.getEntity()).getId();
    return Response.Builder.created(
        baseURI + "/organization/xml/membership/info/" + id).build();
    
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/user/create/")
  public Response createUser(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("username")
      String username, @QueryParam("password")
      String password, @QueryParam("firstname")
      String firstname, @QueryParam("lastname")
      String lastname, @QueryParam("email")
      String email) {
    Response response = super.createUser(baseURI, username, password,
        firstname, lastname, email);
    if (response != null)
      return response;

    return Response.Builder.created(baseURI + "/organization/xml/user/" + username)
        .build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/delete/")
  public Response deleteGroup(@QueryParam("groupId")
  String groupId) {
    Response response = super.deleteGroup(groupId);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/delete/{membershipId}/")
  public Response deleteMembership(@URIParam("membershipId")
  String membershipId) {
    Response response = super.deleteMembership(membershipId);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/delete/{username}/")
  public Response deleteUser(@URIParam("username")
  String username) {
    Response response = super.deleteUser(username);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/delete-user/")
  public Response deleteUserFromGroup(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("groupId")
      String groupId, @QueryParam("username")
      String username) {
    Response response = super.deleteUserFromGroup(baseURI, groupId, username);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();

  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/info/{membershipId}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response findMembership(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @URIParam("membershipId")
      String membershipId) {
    try {
      Membership membership = membershipHandler.findMembership(membershipId);
      if (membership != null)
        return Response.Builder.ok(
            new MembershipXMLEntity(membership, baseURI), XML_CONTENT_TYPE)
            .build();
      return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
          "Membership with id: '" + membershipId + "' not found!").build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/view-all/")
  @OutputTransformer(SerializableTransformer.class)
  public Response findMemberships(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("groupId")
      String groupId, @QueryParam("username")
      String username, @QueryParam("type")
      String type) {
    try {
      Collection<Membership> memberships = null;
      if (groupId != null && username != null && type != null) {
        groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
        Membership membership = membershipHandler
            .findMembershipByUserGroupAndType(username, groupId, type);
        if (membership != null)
          return Response.Builder.ok(
              new MembershipXMLEntity(membership, baseURI), XML_CONTENT_TYPE)
              .build();
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "Membership for groupId: '" + groupId + "', username: '" +
                username + "', membership type: '" + type + "' not found!")
            .build();
      } else if (groupId != null && username != null) {
        groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
        memberships = membershipHandler.findMembershipsByUserAndGroup(username,
            groupId);
      } else if (groupId != null) {
        groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
        Group group = groupHandler.findGroupById(groupId);
        if (group == null) {
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
              .errorMessage("Group '" + groupId + "' not found!").build();
        }
        memberships = membershipHandler.findMembershipsByGroup(group);
      } else if (username != null) {
        memberships = membershipHandler.findMembershipsByUser(username);
      } else {
        // if groupId, username and type is null
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
            .errorMessage(
                "Username, groupId or membership type should be specified!")
            .build();
      }
      if (username != null && userHandler.findUserByName(username) != null)
        return Response.Builder.ok(
            new MembershipListXMLEntity(memberships, username, baseURI),
            XML_CONTENT_TYPE).build();
      return Response.Builder.ok(
          new MembershipListXMLEntity(memberships, baseURI), XML_CONTENT_TYPE)
          .build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/find-all/")
  @OutputTransformer(SerializableTransformer.class)
  public Response findUsers(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("username")
      String username, @QueryParam("firstname")
      String firstname, @QueryParam("lastname")
      String lastname, @QueryParam("email")
      String email, @QueryParam("fromLoginDate")
      String fromLoginDate, @QueryParam("toLogindate")
      String toLoginDate) {
    try {

      Query query = new Query();
      query.setUserName(username);
      query.setFirstName(firstname);
      query.setLastName(lastname);
      query.setEmail(email);
      if (fromLoginDate != null) {
        try {
          query.setFromLoginDate(DateFormat.getDateTimeInstance().parse(
              fromLoginDate));
        } catch (ParseException e) {
          LOGGER.warn("Thrown exception : " + e);
        }
      }
      if (toLoginDate != null) {
        try {
          query.setToLoginDate(DateFormat.getDateTimeInstance().parse(
              toLoginDate));
        } catch (ParseException e) {
          LOGGER.warn("Thrown exception : " + e);
        }
      }
      List<User> list = userHandler.findUsers(query).getAll();
      return Response.Builder.ok(new UserListXMLEntity(list, baseURI),
          XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/user/view-from-to/{from}/{to}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response findUsersRange(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("username")
      String username, @QueryParam("firstname")
      String firstname, @QueryParam("lastname")
      String lastname, @QueryParam("email")
      String email, @QueryParam("fromLoginDate")
      String fromLoginDate, @QueryParam("toLogindate")
      String toLoginDate, @URIParam("from")
      Integer offset, @URIParam("to")
      Integer amount) {
    try {
      Query query = new Query();
      query.setUserName(username);
      query.setFirstName(firstname);
      query.setLastName(lastname);
      query.setEmail(email);
      if (fromLoginDate != null) {
        try {
          query.setFromLoginDate(DateFormat.getDateTimeInstance().parse(
              fromLoginDate));
        } catch (ParseException e) {
          LOGGER.warn("Thrown exception : " + e);
        }
      }
      if (toLoginDate != null) {
        try {
          query.setToLoginDate(DateFormat.getDateTimeInstance().parse(
              toLoginDate));
        } catch (ParseException e) {
          LOGGER.warn("Thrown exception : " + e);
        }
      }
      List<User> list = userHandler.findUsers(query).getAll();
      Integer amount_ = amount;
      if (amount > list.size())
        amount_ = list.size();
      return Response.Builder.ok(
          new UserListXMLEntity(list.subList(offset, amount_), baseURI),
          XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/filter/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getAllGroup(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("filter")
      String filter) {
    try {
      Collection<Group> groups = groupHandler.getAllGroups();
      if (filter != null && filter.length() > 0) {
        Collection<Group> temp = new ArrayList<Group>(groups);
        for (Group g : temp) {
          if (!g.getId().contains(filter))
            groups.remove(g);
        }
      }

      return Response.Builder.ok(new GroupListXMLEntity(groups, baseURI),
          XML_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/info/{groupId}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getGroup(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @URIParam("groupId")
      String groupId) {
    try {
      groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
      Group group = groupHandler.findGroupById(groupId);
      if (group == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "Group '" + groupId + "' not found.").build();
      }
      Collection<User> members = userHandler.findUsersByGroup(groupId).getAll();
      return Response.Builder.ok(new GroupXMLEntity(group, members, baseURI),
          XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/view-all/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getGroups(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("parentId")
      String parentId) {
    try {
      Collection<Group> groups = null;
      if (parentId != null && parentId.length() > 0) {
        parentId = (parentId.startsWith("/")) ? parentId : "/" + parentId;
        Group parent = groupHandler.findGroupById(parentId);
        if (parent == null) {
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
              .errorMessage("Parent '" + parentId + "' not found.").build();
        }
        groups = groupHandler.findGroups(parent);
      } else {
        groups = groupHandler.findGroups(null);
      }
      return Response.Builder.ok(new GroupListXMLEntity(groups, baseURI),
          XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/count/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getGroupsCount() {
    try {
      int number = groupHandler.getAllGroups().size();

      return Response.Builder.ok(new CountXMLEntity(number, "groups"),
          XML_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  // User handler methods

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/groups-for-user/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getGroupsOfUser(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("username")
      String username) {
    try {
      if (userHandler.findUserByName(username) == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "User '" + username + "' not found.").build();
      }
      Collection<Group> groups = groupHandler.findGroupsOfUser(username);

      return Response.Builder.ok(new GroupListXMLEntity(groups, baseURI),
          XML_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/view-from-to/{from}/{to}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getGroupsRange(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @URIParam("from")
      Integer offset, @URIParam("to")
      Integer amount, @QueryParam("parentId")
      String parentId) {
    try {
      Collection<Group> groups = null;
      if (parentId != null && parentId.length() > 0) {
        parentId = (parentId.startsWith("/")) ? parentId : "/" + parentId;
        Group parent = groupHandler.findGroupById(parentId);
        if (parent == null) {
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
              .errorMessage("Parent '" + parentId + "' not found.").build();
        }
        groups = groupHandler.findGroups(parent);
      } else {
        groups = groupHandler.findGroups(null);
      }

      Integer amount_ = amount;
      if (amount > groups.size())
        amount_ = groups.size();
      return Response.Builder.ok(
          new GroupListXMLEntity(new ArrayList<Group>(groups).subList(offset,
              amount_), baseURI), XML_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/get-types/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getMembershipTypes() {
    try {
      Collection<MembershipType> membershipTypes = membershipTypeHandler
          .findMembershipTypes();
      return Response.Builder.ok(
          new MembershipTypesListXMLEntity(membershipTypes), XML_CONTENT_TYPE)
          .build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.organization.rest.RESTOrganizationService#getUser(java.lang.String)
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/info/{username}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getUser(@URIParam("username")
  String username) {
    try {
      User user = userHandler.findUserByName(username);
      if (user == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "User '" + username + "' not found.").build();
      }
      return Response.Builder.ok(new UserXMLEntity(user), XML_CONTENT_TYPE)
          .build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.organization.rest.RESTOrganizationService#getUsers(java.lang.String)
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/users/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getUsers(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI) {
    try {
      List<User> list = userHandler.findUsers(new Query()).getAll();
      return Response.Builder.ok(new UserListXMLEntity(list, baseURI),
          XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/count/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getUsersCount() {
    try {
      int number = userHandler.findUsers(new Query()).getAll().size();
      return Response.Builder.ok(new CountXMLEntity(number, "users"),
          XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/view-range/{from}/{number}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getUsersRange(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @URIParam("from")
      Integer offset, @URIParam("number")
      Integer amount) {
    try {
      List<User> list = userHandler.findUsers(new Query()).getAll();
      int prevFrom = -1;
      if (offset > 0)
        prevFrom = ((offset - amount) > 0) ? offset - amount : 0;
      int nextFrom = ((offset + amount) < list.size()) ? offset + amount : -1;
      int to = (offset + amount < list.size()) ? offset + amount : list.size();
      return Response.Builder.ok(
          new UserListXMLEntity(list.subList(offset, to), baseURI, prevFrom,
              nextFrom, amount), XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
          .errorMessage("Thrown exception : " + e).build();
    }
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/group/update/")
  public Response updateGroup(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("groupId")
      String groupId, @QueryParam("name")
      String name, @QueryParam("label")
      String label, @QueryParam("description")
      String description) {

    Response response = super.updateGroup(baseURI, groupId, name, label,
        description);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();

  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/user/update/")
  public Response updateUser(
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI)
      String baseURI, @QueryParam("username")
      String username, @QueryParam("password")
      String password, @QueryParam("firstname")
      String firstname, @QueryParam("lastname")
      String lastname, @QueryParam("email")
      String email) {
    Response response = super.updateUser(baseURI, username, password,
        firstname, lastname, email);
    if (response != null)
      return response;
    // TODO Check url to real resource
    return Response.Builder.created(baseURI + "user/" + username)
        .build();
  }

}
