/**
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
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.Group;
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
 * 
 * @author Gennady Azarenkov
 * @version $Id:$
 */

// For Openfire Service
@URITemplate("/organization/xml/")
public class RESTOrganizationServiceXMLImpl extends RESTOrganizationServiceAbstractImpl implements
    ResourceContainer {

  protected final static String XML_CONTENT_TYPE = "text/xml";

  public RESTOrganizationServiceXMLImpl(OrganizationService organizationService) {
    super(organizationService);
  }

  @SuppressWarnings("unchecked")
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/find-all/")
  @OutputTransformer(SerializableTransformer.class)
  public Response findUsers(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                            @QueryParam("username") String username,
                            @QueryParam("firstname") String firstname,
                            @QueryParam("lastname") String lastname,
                            @QueryParam("email") String email,
                            @QueryParam("fromLoginDate") String fromLoginDate,
                            @QueryParam("toLogindate") String toLoginDate) {
    try {
      // TODO : now returned all founded user need be carefully then using
      // wildcard (*)
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
      List<User> list = new ArrayList<User>();
      PageList pageList = userHandler.findUsers(query);
      int pages = pageList.getAvailablePage();
      for (int i = 1; i <= pages; i++) {
        list.addAll(pageList.getPage(i));
      }
      return Response.Builder.ok(new UserListXMLEntity(list, baseURI), XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @SuppressWarnings("unchecked")
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/find-all/{from}/{num}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response findUsersRange(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                 @QueryParam("username") String username,
                                 @QueryParam("firstname") String firstname,
                                 @QueryParam("lastname") String lastname,
                                 @QueryParam("email") String email,
                                 @QueryParam("fromLoginDate") String fromLoginDate,
                                 @QueryParam("toLogindate") String toLoginDate,
                                 @URIParam("from") Integer from,
                                 @URIParam("num") Integer numResult) {
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
      List<User> list = new ArrayList<User>();
      PageList pageList = userHandler.findUsers(query);
      pageList.setPageSize(numResult);
      int page = from / numResult + 1;
      list = pageList.getPage(page);
      return Response.Builder.ok(new UserListXMLEntity(list, baseURI), XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/view-range/{from}/{num}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getUsersRange(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                @URIParam("from") Integer from,
                                @URIParam("num") Integer numResult) {
    try {
      List<User> list = new ArrayList<User>();
      PageList pageList = userHandler.getUserPageList(numResult);
      int page = from / numResult + 1;
      list = pageList.getPage(page);
      return Response.Builder.ok(new UserListXMLEntity(list, baseURI), XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  /**
   * {@inheritDoc}
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/count/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getUsersCount() {
    try {
      int number = userHandler.getUserPageList(20).getAvailable();
      return Response.Builder.ok(new CountXMLEntity(number, "users"), XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  /**
   * {@inheritDoc}
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/info/{username}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getUser(@URIParam("username") String username) {
    try {
      User user = userHandler.findUserByName(username);
      if (user == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("User '" + username
            + "' not found.").build();
      }
      return Response.Builder.ok(new UserXMLEntity(user), XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @SuppressWarnings("unchecked")
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/info/{groupId}/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getGroup(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                           @URIParam("groupId") String groupId) {
    try {
      groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
      Group group = groupHandler.findGroupById(groupId);
      if (group == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("Group '" + groupId
            + "' not found.").build();
      }

      List<User> members = new ArrayList<User>();
      PageList pageList = userHandler.findUsersByGroup(groupId);
      int pages = pageList.getAvailablePage();
      for (int i = 1; i <= pages; i++) {
        members.addAll(pageList.getPage(i));
      }

      return Response.Builder.ok(new GroupXMLEntity(group, members, baseURI), XML_CONTENT_TYPE)
                             .build();
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @SuppressWarnings("unchecked")
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/view-all/")
  @OutputTransformer(SerializableTransformer.class)
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
      return Response.Builder.ok(new GroupListXMLEntity(groups, baseURI), XML_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/count/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getGroupsCount() {
    try {
      int number = groupHandler.getAllGroups().size();

      return Response.Builder.ok(new CountXMLEntity(number, "groups"), XML_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  // User handler methods

  @SuppressWarnings("unchecked")
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/groups-for-user/")
  @OutputTransformer(SerializableTransformer.class)
  public Response getGroupsOfUser(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
                                  @QueryParam("username") String username) {
    try {
      if (userHandler.findUserByName(username) == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage("User '" + username
            + "' not found.").build();
      }
      Collection<Group> groups = groupHandler.findGroupsOfUser(username);

      return Response.Builder.ok(new GroupListXMLEntity(groups, baseURI), XML_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  @SuppressWarnings("unchecked")
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/group/view-from-to/{from}/{to}/")
  @OutputTransformer(SerializableTransformer.class)
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
      return Response.Builder.ok(new GroupListXMLEntity(new ArrayList<Group>(groups).subList(offset,amount_),
                                                        baseURI),XML_CONTENT_TYPE).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

}
