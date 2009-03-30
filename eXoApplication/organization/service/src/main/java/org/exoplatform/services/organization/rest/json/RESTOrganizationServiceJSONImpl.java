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

package org.exoplatform.services.organization.rest.json;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.exoplatform.ws.frameworks.json.transformer.Bean2JsonOutputTransformer;

import com.sun.mail.util.QEncoderStream;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id:$
 */
@URITemplate("/organization/json/")
public class RESTOrganizationServiceJSONImpl extends RESTOrganizationServiceAbstractImpl implements
    ResourceContainer {

  protected final static String JSON_CONTENT_TYPE = "application/json";

  private final static String   ASCENDING         = "ascending";

  private final static String   DESCENDING        = "descending";

  private final static String   USERNAME          = "username";

  private final static String   FIRSTNAME         = "firstname";

  private final static String   LASTNAME          = "lastname";

  public RESTOrganizationServiceJSONImpl(OrganizationService organizationService) {
    super(organizationService);
  }

  // ok
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/group/create/")
  public Response createGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                              @QueryParam("groupName") String groupName,
                              @QueryParam("label") String label,
                              @QueryParam("description") String description,
                              @QueryParam("parentId") String parentId) {

    Response response = super.createGroup(baseURI, groupName, label, description, parentId);
    if (response != null)
      return response;

    String id = ((Group) response.getEntity()).getId();
    return Response.Builder.created(baseURI + "/organization/json/group/info" + id).build();
  }

  // fix
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/membership/create")
  public Response createMembership(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                   @QueryParam("username") String username,
                                   @QueryParam("groupId") String groupId,
                                   @QueryParam("type") String type) {
    Response response = super.createMembership(baseURI, username, groupId, type);
    if (response != null)
      return response;

    String id = ((Membership) response.getEntity()).getId();
    return Response.Builder.created(baseURI + "/organization/json/membership/info/" + id).build();
  }

  // ok
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/user/create/")
  public Response createUser(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                             @QueryParam("username") String username,
                             @QueryParam("password") String password,
                             @QueryParam("firstname") String firstname,
                             @QueryParam("lastname") String lastname,
                             @QueryParam("email") String email) {
    Response response = super.createUser(baseURI, username, password, firstname, lastname, email);
    if (response != null)
      return response;

    return Response.Builder.created(baseURI + "/organization/json/user/" + username).build();
  }

  // ok
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/delete/")
  public Response deleteGroup(@QueryParam("groupId") String groupId) {
    Response response = super.deleteGroup(groupId);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/delete/{membershipId}/")
  public Response deleteMembership(@URIParam("membershipId") String membershipId) {
    Response response = super.deleteMembership(membershipId);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/delete/{username}/")
  public Response deleteUser(@URIParam("username") String username) {
    Response response = super.deleteUser(username);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/delete-user/")
  public Response deleteUserFromGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                      @QueryParam("groupId") String groupId,
                                      @QueryParam("username") String username) {
    Response response = super.deleteUserFromGroup(baseURI, groupId, username);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/info/{membershipId}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response findMembership(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                 @URIParam("membershipId") String membershipId) {
    Membership membership = null;
    try {
      membership = membershipHandler.findMembership(membershipId);
      if (membership != null) {
        return Response.Builder.ok(membership, JSON_CONTENT_TYPE).build();
      }
      return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("Membership with id: '"
          + membershipId + "' not found!").build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/view-all/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response findMemberships(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                  @QueryParam("groupId") String groupId,
                                  @QueryParam("username") String username,
                                  @QueryParam("type") String type) {
    try {
      Collection<Membership> memberships = null;
      if (groupId != null && username != null && type != null) {
        groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
        Membership membership = membershipHandler.findMembershipByUserGroupAndType(username,
                                                                                   groupId,
                                                                                   type);
        if (membership != null) {
          return Response.Builder.ok(membership, JSON_CONTENT_TYPE).build();
        } else {
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
                                 .errorMessage("Membership for groupId: '" + groupId
                                     + "', username: '" + username + "', membership type: '" + type
                                     + "' not found!")
                                 .build();
        }
      } else if (groupId != null && username != null) {
        groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
        memberships = membershipHandler.findMembershipsByUserAndGroup(username, groupId);

      } else if (groupId != null) {
        groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
        Group group = groupHandler.findGroupById(groupId);

        if (group == null) {
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("Group '" + groupId
              + "' not found!").build();
        }
        memberships = membershipHandler.findMembershipsByGroup(group);
      } else if (username != null) {
        memberships = membershipHandler.findMembershipsByUser(username);
      } else {
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                               .errorMessage("Username, groupId or membership type should be specified!")
                               .build();
      }
      if (username != null && userHandler.findUserByName(username) != null) {
        MembershipListBean membership_list = new MembershipListBean(username, memberships);
        return Response.Builder.ok(membership_list, JSON_CONTENT_TYPE).build();
      }
      MembershipListBean membership_list = new MembershipListBean(null, memberships);
      return Response.Builder.ok(membership_list, JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/find-all/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response findUsers(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                            @QueryParam("username") String username,
                            @QueryParam("firstname") String firstname,
                            @QueryParam("lastname") String lastname,
                            @QueryParam("email") String email,
                            @QueryParam("fromLoginDate") String fromLoginDate,
                            @QueryParam("toLogindate") String toLoginDate) {
    try {
      Query query = new Query();
      query.setUserName(username);
      query.setFirstName(firstname);
      query.setLastName(lastname);
      query.setEmail(email);
      if (fromLoginDate != null) {
        try {
          query.setFromLoginDate(DateFormat.getDateTimeInstance().parse(fromLoginDate));
        } catch (ParseException e) {
          LOGGER.warn("Thrown exception : " + e);
        }
      }
      if (toLoginDate != null) {
        try {
          query.setToLoginDate(DateFormat.getDateTimeInstance().parse(toLoginDate));
        } catch (ParseException e) {
          LOGGER.warn("Thrown exception : " + e);
        }
      }
      List<User> list = userHandler.findUsers(query).getAll();
      List<UserBean> listBean = new ArrayList<UserBean>();
      for (User user : list) {
        if (user != null)
          listBean.add(new UserBean(user));
      }
      UserListBean user_list = new UserListBean(listBean);
      return Response.Builder.ok(user_list, JSON_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/user/view-from-to/{from}/{to}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response findUsersRange(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                 @QueryParam("username") String username,
                                 @QueryParam("firstname") String firstname,
                                 @QueryParam("lastname") String lastname,
                                 @QueryParam("email") String email,
                                 @QueryParam("fromLoginDate") String fromLoginDate,
                                 @QueryParam("toLogindate") String toLoginDate,
                                 @URIParam("from") Integer offset,
                                 @URIParam("to") Integer amount) {
    try {
      Query query = new Query();
      query.setUserName(username);
      query.setFirstName(firstname);
      query.setLastName(lastname);
      query.setEmail(email);
      if (fromLoginDate != null) {
        try {
          query.setFromLoginDate(DateFormat.getDateTimeInstance().parse(fromLoginDate));
        } catch (ParseException e) {
          LOGGER.warn("Thrown exception : " + e);
        }
      }
      if (toLoginDate != null) {
        try {
          query.setToLoginDate(DateFormat.getDateTimeInstance().parse(toLoginDate));
        } catch (ParseException e) {
          LOGGER.warn("Thrown exception : " + e);
        }
      }
      List<User> list = userHandler.findUsers(query).getAll();
      Integer amount_ = amount;
      if (amount > list.size())
        amount_ = list.size();
      List<User> listSub = list.subList(offset, amount_);
      List<UserBean> listBean = new ArrayList<UserBean>();
      for (User user : listSub) {
        if (user != null)
          listBean.add(new UserBean(user));
      }
      UserListBean user_list = new UserListBean(listBean);
      return Response.Builder.ok(user_list, JSON_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/find-user-in-range/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response findUsersRange2(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                  @QueryParam("question") String question,
                                  @QueryParam("from") Integer from,
                                  @QueryParam("to") Integer to,
                                  @QueryParam("sort-order") String sortOrder,
                                  @QueryParam("sort-field") String sortField) {
    try {
      List<User> temp = new ArrayList<User>();
      Comparator<User> comparator = getComparator(sortField, sortOrder);
      if (comparator == null) {
        LOGGER.error("You set wrong parameters fo sorting! sort-order = [" + ASCENDING + ", "
            + DESCENDING + "], " + "sort-field = [" + USERNAME + ", " + LASTNAME + ", " + FIRSTNAME
            + "]. You set sort-field = " + sortField + " sort-order = " + sortOrder);
        return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                               .errorMessage("You set wrong parameters fo sorting! sort-order = ["
                                   + ASCENDING + ", " + DESCENDING + "], " + "sort-field = ["
                                   + USERNAME + ", " + LASTNAME + ", " + FIRSTNAME
                                   + "]. You set sort-field = " + sortField + " sort-order = "
                                   + sortOrder)
                               .build();
      }
      SortedSet<User> users = new TreeSet<User>(comparator);

      Query query = new Query();
      query.setUserName(question);
      temp = userHandler.findUsers(query).getAll();
      for (User user : temp) {
        if (!users.contains(user))
          users.add(user);
      }
      query = new Query();
      query.setFirstName(question);
      temp = userHandler.findUsers(query).getAll();
      for (User user : temp) {
        if (!users.contains(user))
          users.add(user);
      }
      query = new Query();
      query.setLastName(question);
      temp = userHandler.findUsers(query).getAll();
      for (User user : temp) {
        if (!users.contains(user))
          users.add(user);
      }

      if (to > users.size())
        to = users.size();
      // UserListBean user_list = new UserListBean(users.subList(from, to));

      List<User> uList = new ArrayList<User>();
      Iterator<User> i = users.iterator();
      while (i.hasNext()) {
        User user = (User) i.next();
        uList.add(user);
      }
      List<User> listSub = uList.subList(from, to);
      List<UserBean> listBean = new ArrayList<UserBean>();
      for (User user : listSub) {
        if (user != null)
          listBean.add(new UserBean(user));
      }
      UserListBean user_list = new UserListBean(listBean);
      user_list.setTotalUser(users.size());
      return Response.Builder.ok(user_list, JSON_CONTENT_TYPE).build();
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/filter/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getAllGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                              @QueryParam("filter") String filter) {
    try {
      Collection<Group> groups = groupHandler.getAllGroups();
      if (filter != null && filter.length() > 0) {
        Collection<Group> temp = new ArrayList<Group>(groups);
        for (Group g : temp) {
          if (!g.getId().contains(filter))
            groups.remove(g);
        }
      }
      GroupListBean group_list = new GroupListBean(groups);
      return Response.Builder.ok(group_list, JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/info/{groupId}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                           @URIParam("groupId") String groupId) {
    try {
      groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
      Group group = groupHandler.findGroupById(groupId);
      if (group == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("Group '" + groupId
            + "' not found.").build();
      }
      Collection<User> members = userHandler.findUsersByGroup(groupId).getAll();
      return Response.Builder.ok(new GroupMembersBean(group, members), JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/view-all/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getGroups(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                            @QueryParam("parentId") String parentId) {
    try {
      Collection<Group> groups = null;
      if (parentId != null && parentId.length() > 0) {
        parentId = (parentId.startsWith("/")) ? parentId : "/" + parentId;
        Group parent = groupHandler.findGroupById(parentId);
        if (parent == null) {
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("Parent '"
              + parentId + "' not found.").build();
        }
        groups = groupHandler.findGroups(parent);
      } else {
        groups = groupHandler.findGroups(null);
      }
      GroupListBean group_list = new GroupListBean(groups);

      return Response.Builder.ok(group_list, JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/count/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getGroupsCount() {
    try {
      int number = groupHandler.getAllGroups().size();

      return Response.Builder.ok(new CountBean(number), JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/groups-for-user/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getGroupsOfUser(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                  @QueryParam("username") String username) {
    try {
      if (userHandler.findUserByName(username) == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("User '" + username
            + "' not found.").build();
      }
      Collection<Group> groups = groupHandler.findGroupsOfUser(username);

      GroupListBean group_list = new GroupListBean(groups);

      return Response.Builder.ok(group_list, JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/view-from-to/{from}/{to}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getGroupsRange(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                 @URIParam("from") Integer offset,
                                 @URIParam("to") Integer amount,
                                 @QueryParam("parentId") String parentId) {
    try {
      Collection<Group> groups = null;
      if (parentId != null && parentId.length() > 0) {
        parentId = (parentId.startsWith("/")) ? parentId : "/" + parentId;
        Group parent = groupHandler.findGroupById(parentId);
        if (parent == null) {
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("Parent '"
              + parentId + "' not found.").build();
        }
        groups = groupHandler.findGroups(parent);
      } else {
        groups = groupHandler.findGroups(null);
      }
      Integer amount_ = amount;
      if (amount > groups.size())
        amount_ = groups.size();

      GroupListBean group_list = new GroupListBean(new ArrayList<Group>(groups).subList(offset,
                                                                                        amount_));

      return Response.Builder.ok(group_list, JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/membership/get-types/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getMembershipTypes() {
    try {
      Collection<MembershipType> membershipTypes = membershipTypeHandler.findMembershipTypes();

      MembershipTypesListBean membership_types_list = new MembershipTypesListBean(membershipTypes);

      return Response.Builder.ok(membership_types_list, JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/info/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getUser(@URIParam("username") String username) {
    try {
      User user = userHandler.findUserByName(username);
      if (user == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("User '" + username
            + "' not found.").build();
      }
      return Response.Builder.ok(user).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/users/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getUsers(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI) {
    try {
      List<User> list = userHandler.findUsers(new Query()).getAll();
      // UserListBean user_list = new UserListBean(list);
      List<UserBean> listBean = new ArrayList<UserBean>();
      for (User user : list) {
        if (user != null)
          listBean.add(new UserBean(user));
      }
      UserListBean user_list = new UserListBean(listBean);
      return Response.Builder.ok(user_list, JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/count/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getUsersCount() {
    try {
      int number = userHandler.findUsers(new Query()).getAll().size();

      return Response.Builder.ok(new CountBean(number), JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/view-range/{from}/{number}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getUsersRange(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                @URIParam("from") Integer offset,
                                @URIParam("number") Integer amount) {
    try {
      List<User> list = userHandler.findUsers(new Query()).getAll();
      int prevFrom = -1;
      if (offset > 0)
        prevFrom = ((offset - amount) > 0) ? offset - amount : 0;
      int nextFrom = ((offset + amount) < list.size()) ? offset + amount : -1;
      int to = (offset + amount < list.size()) ? offset + amount : list.size();

      // UserListBean user_list = new UserListBean(list.subList(offset, to));
      List<User> listSub = list.subList(offset, to);
      List<UserBean> listBean = new ArrayList<UserBean>();
      for (User user : listSub) {
        if (user != null)
          listBean.add(new UserBean(user));
      }
      UserListBean user_list = new UserListBean(listBean);

      return Response.Builder.ok(user_list, JSON_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/group/update/")
  public Response updateGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                              @QueryParam("groupId") String groupId,
                              @QueryParam("name") String name,
                              @QueryParam("label") String label,
                              @QueryParam("description") String description) {
    Response response = super.updateGroup(baseURI, groupId, name, label, description);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/user/update/")
  public Response updateUser(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                             @QueryParam("username") String username,
                             @QueryParam("password") String password,
                             @QueryParam("firstname") String firstname,
                             @QueryParam("lastname") String lastname,
                             @QueryParam("email") String email) {
    Response response = super.updateUser(baseURI, username, password, firstname, lastname, email);
    if (response != null)
      return response;
    return Response.Builder.noContent().build();
  }

  /**
   * @author vetal
   */
  private class UserNameComporatorAsc implements Comparator<User> {
    public int compare(User u1, User u2) {
      return u1.getUserName().compareTo(u2.getUserName());
    }
  }

  private class UserNameComporatorDesc implements Comparator<User> {
    public int compare(User u1, User u2) {
      return -(u1.getUserName().compareTo(u2.getUserName()));
    }
  }

  /**
   * @author vetal
   */
  private class FirstNameComporatorAsc implements Comparator<User> {
    public int compare(User u1, User u2) {
      if (u1.getFirstName() == null)
        return 1;
      if (u2.getFirstName() == null)
        return -1;
      return u1.getFirstName().compareTo(u2.getFirstName());
    }
  }

  private class FirstNameComporatorDesc implements Comparator<User> {
    public int compare(User u1, User u2) {
      if (u1.getFirstName() == null)
        return -1;
      if (u2.getFirstName() == null)
        return 1;
      return -(u1.getFirstName().compareTo(u2.getFirstName()));
    }
  }

  /**
   * @author vetal
   */
  private class LastNameComporatorAsc implements Comparator<User> {
    public int compare(User u1, User u2) {
      if (u1.getLastName() == null)
        return 1;
      if (u2.getLastName() == null)
        return -1;
      return u1.getLastName().compareTo(u2.getLastName());
    }
  }

  private class LastNameComporatorDesc implements Comparator<User> {
    public int compare(User u1, User u2) {
      if (u1.getLastName() == null)
        return -1;
      if (u2.getLastName() == null)
        return 1;
      return -(u1.getLastName().compareTo(u2.getLastName()));
    }
  }

  /**
   * @param sortField
   * @return
   */
  private Comparator<User> getComparator(String sortField, String sortOrder) {
    if (sortOrder == null || sortOrder.length() == 0)
      sortOrder = ASCENDING;
    if (sortField == null || sortField.length() == 0)
      sortField = USERNAME;
    if (sortOrder.equalsIgnoreCase(ASCENDING)) {
      if (sortField.equalsIgnoreCase(FIRSTNAME))
        return new FirstNameComporatorAsc();
      else if (sortField.equalsIgnoreCase(LASTNAME))
        return new LastNameComporatorAsc();
      else if (sortField.equalsIgnoreCase(USERNAME))
        return new UserNameComporatorAsc();
    } else if (sortOrder.equalsIgnoreCase(DESCENDING)) {
      if (sortField.equalsIgnoreCase(FIRSTNAME))
        return new FirstNameComporatorDesc();
      else if (sortField.equalsIgnoreCase(LASTNAME))
        return new LastNameComporatorDesc();
      else if (sortField.equalsIgnoreCase(USERNAME))
        return new UserNameComporatorDesc();
    }
    return null;
  }

}
