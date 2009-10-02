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

import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS .<br>
 * An interface for managing an Organization Service using REST framework
 * @author Gennady Azarenkov
 * @version $Id:$
 */

public interface RESTOrganizationService {

  /**
   * @param groupName the group name
   * @param label the label
   * @param description the description
   * @param parentId Id of the parent group
   * @return Response object with HTTP status.
   */
  Response createGroup(String baseURI, String groupName, String label,
      String description, String parentId);

  /**
   * @param username the user name
   * @param groupId the group ID
   * @param type the membership type
   * @return Response object with HTTP status.
   */
  Response createMembership(String baseURI, String username, String groupId,
      String type);

  /**
   * @param username the user name.
   * @param password the user's password.
   * @param firstname the first name.
   * @param lastname the last name.
   * @param email the email address.
   * @return Response object with HTTP status.
   */
  Response createUser(String baseURI, String username, String password,
      String firstname, String lastname, String email);

  /**
   * @param groupId the ID of the group to delete
   * @return Response object with HTTP status.
   */
  Response deleteGroup(String groupId);

  /**
   * @param membershipId the ID of the membership to delete
   * @return Response object with HTTP status.
   */
  Response deleteMembership(String membershipId);

  /**
   * @param username the user name to remove.
   * @return Response object with HTTP status.
   */
  Response deleteUser(String username);
  
  /**
   * @param groupId the ID of the group
   * @param username the user's name
   * @return Response object with HTTP status.
   */
  Response deleteUserFromGroup(String baseURI, String groupId, String username);

  /**
   * @param membershipId
   * @return
   */
  Response findMembership(String baseURI, String membershipId);

  /**
   * @param groupId
   * @param username
   * @param type
   * @return
   */
  Response findMemberships(String baseURI, String groupId, String username,
      String type);

  /**
   * @param username the username.
   * @param firstname the first name.
   * @param lastname the last name.
   * @param email the email address.
   * @param fromLoginDate
   * @param toLoginDate
   * @return Returns list of users in the system.
   */
  Response findUsers(String baseURI, String username, String firstname,
      String lastname, String email, String fromLoginDate, String toLoginDate);

  /**
   * @param username the username.
   * @param firstname the first name.
   * @param lastname the last name.
   * @param email the email address.
   * @param fromLoginDate
   * @param toLoginDate
   * @param from the beginning index to start the results at.
   * @param number the number of results from start index to return.
   * @return Returns list of users in the system within the specified range.
   */
  Response findUsersRange(String baseURI, String username, String firstname,
      String lastname, String email, String fromLoginDate, String toLoginDate,
      Integer offset, Integer amount);

  /**
   * @param filter
   * @return list of the groups except filters 
   */
  Response getAllGroup(String baseURI, String filter);

  /**
   * @param groupId
   * @param action
   * @return
   */
  Response getGroup(String baseURI, String groupId);
  
  /**
   * @param parentId
   * @return list of the groups which are related to parent group
   */
  Response getGroups(String baseURI, String parentId);

  /**
   * @return count of the groups
   */  
  Response getGroupsCount();

  /**
   * @param username the name of the user
   * @return all groups where user participate
   */
  Response getGroupsOfUser(String baseURI, String username);

  /**
   * @param offset the start range
   * @param amount - quantity of groups
   * @param parentId the parent id
   * @return list of the groups in special range
   */
  Response getGroupsRange(String baseURI, Integer offset, Integer amount,
      String parentId);

  /**
   * @return list of membership types
   */
  Response getMembershipTypes();

  /**
   * @param username the user name
   * @return document that content information about user based on it's name.
   */
  Response getUser(String username);

  /**
   * @return document that content list of all usernames in system.
   */
  Response getUsers(String baseURI);

  /**
   * @return XML document that content information about number of user in
   *         system.
   */
  Response getUsersCount();

  /**
   * Returns XML document that content list of users in the system within the specified range.
   * 
   * @param from the beginning index to start the results at.
   * @param number the number of results from start index.
   * @return Returns XML document that content list of users in the system within the specified range.
   */
  Response getUsersRange(String baseURI, Integer offset, Integer amount);

  /**
   * Update group data
   * 
   * @param groupId the group id
   * @param name the name of the group
   * @param label the label of the group
   * @param description of the group
   * @return Response object with HTTP status. It points on result of operation.
   *         No useful content is returned.
   */
  Response updateGroup(String baseURI, String groupId, String name, String label,
      String description);

  /**
   * Update user data
   * 
   * @param username the username.
   * @param password the password.
   * @param firstname the first name.
   * @param lastname the last name.
   * @param email the email address.
   * @return Response object with HTTP status. It points on result of operation.
   *         No useful content is returned.
   */
  Response updateUser(String baseURI, String username, String password,
      String firstname, String lastname, String email);

}
