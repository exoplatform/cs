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

package org.exoplatform.services.organization.rest;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS        .
 * @author Gennady Azarenkov
 * @version $Id:$
 */

public abstract class RESTOrganizationServiceAbstractImpl implements RESTOrganizationService {

  protected static final Log            LOGGER = ExoLogger.getLogger("ws.RESTOrganizationService");

  protected final GroupHandler          groupHandler;

  protected final UserHandler           userHandler;

  protected final MembershipHandler     membershipHandler;

  protected final MembershipTypeHandler membershipTypeHandler;

  protected RESTOrganizationServiceAbstractImpl(OrganizationService organizationService) {
    groupHandler = organizationService.getGroupHandler();
    userHandler = organizationService.getUserHandler();
    membershipHandler = organizationService.getMembershipHandler();
    membershipTypeHandler = organizationService.getMembershipTypeHandler();
  }

  public Response createGroup(String baseURI, String groupName, String label, String description,
      String parentId) {
    Group parent = null;
    Group group = null;
    try {
      if (parentId != null && parentId.length() > 0) {
        parentId = (parentId.startsWith("/")) ? parentId : "/" + parentId;
        parent = groupHandler.findGroupById(parentId);
        if (parent == null) {
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
              "Parent group '" + parentId + "' not found!").build();
        }
      }
      group = groupHandler.createGroupInstance();
      group.setGroupName(groupName);
      group.setLabel(label);
      group.setDescription(description);
      groupHandler.addChild(parent, group, true);
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
    //Group Id holder
    return Response.Builder.ok(group).build();
  }

  public Response createMembership(String baseURI, String username, String groupId, String type) {
    
    Membership m = null;
    try {
  
      User user = userHandler.findUserByName(username);
      if (user == null)
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "User : '" + username + "' not found!").build();
      groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
      Group group = groupHandler.findGroupById(groupId);
      if (group == null)
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "Group : '" + groupId + "' not found!").build();
      
      MembershipType membershipType = membershipTypeHandler.findMembershipType(type);
      
      m = membershipHandler.findMembershipByUserGroupAndType(username, groupId, type);
      
      if (membershipType == null)
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "MembershipType : '" + type + "' not found!").build();
      membershipHandler.linkMembership(user, group, membershipType, true);
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
    
    return Response.Builder.ok(m).build();

  }

  public Response createUser(String baseURI, String username, String password, String firstname,
      String lastname, String email) {
    try {
      User user = userHandler.createUserInstance(username);
      if (user == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "User '" + username + "' not found.").build();
      }
      user.setPassword(password);
      user.setFirstName(firstname);
      user.setLastName(lastname);
      user.setEmail(email);
      userHandler.createUser(user, true);

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
    
    return null;
  }

  public Response deleteGroup(String groupId) {
    try {
      
      groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
      
      Group group = groupHandler.findGroupById(groupId);
      
      if (group != null) {
        groupHandler.removeGroup(group, true);
      } else
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "Group '" + groupId + "' not found!").build();
      
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
    return null;
  }

  public Response deleteMembership(String membershipId) {
    try {
      membershipHandler.removeMembership(membershipId, true);
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
    return null;
  }

  public Response deleteUser(String username) {
    try {
      User user = userHandler.findUserByName(username);
      if (user == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "User '" + username + "' not found.").build();
      }
      userHandler.removeUser(username, true);
      
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
    return null;
  }

  public Response deleteUserFromGroup(String baseURI, String groupId, String username) {
    try {
      groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
      if (userHandler.findUserByName(username) == null
          || groupHandler.findGroupById(groupId) == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "Group or user not found!").build();
      }
      Collection<Membership> memberships = membershipHandler.findMembershipsByUserAndGroup(
          username, groupId);
      for (Membership m : memberships)
        membershipHandler.removeMembership(m.getId(), true);
      
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
    return null;
  }

  public Response updateGroup(String baseURI, String groupId, String name, String label,
      String description) {
    try {
      groupId = (groupId.startsWith("/")) ? groupId : "/" + groupId;
      Group group = groupHandler.findGroupById(groupId);
      if (group == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).errorMessage(
            "Group '" + groupId + "' not found!").build();
      }
      group.setGroupName(name);
      group.setLabel(label);
      group.setDescription(description);
      groupHandler.saveGroup(group, true);
      
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
    return null;
  }

  public Response updateUser(String baseURI, String username, String password, String firstname,
      String lastname, String email) {
    try {
      User user = userHandler.findUserByName(username);
      user.setPassword(password);
      user.setFirstName(firstname);
      user.setLastName(lastname);
      user.setEmail(email);

      userHandler.saveUser(user, true);

    } catch (Exception e) {
      LOGGER.error("Cannot manage user : ", e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).errorMessage(
          "Thrown exception : " + e).build();
    }
    return null;
  }

}
