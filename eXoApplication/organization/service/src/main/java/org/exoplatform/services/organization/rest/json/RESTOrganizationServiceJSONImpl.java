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

package org.exoplatform.services.organization.rest.json;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.authentication.rest.RESTAuthenticator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.rest.RESTOrganizationServiceAbstractImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id:$
 */

//For chat application 

@Path("/organization/json/")
public class RESTOrganizationServiceJSONImpl extends RESTOrganizationServiceAbstractImpl implements
    ResourceContainer {

  protected final static String JSON_CONTENT_TYPE = "application/json";

  private final static String   ASCENDING         = "ascending";

  private final static String   DESCENDING        = "descending";

  private final static String   USERNAME          = "username";

  private final static String   FIRSTNAME         = "firstname";

  private final static String   LASTNAME          = "lastname";
  
  private static final CacheControl cc;
  static {
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);

  }

  public RESTOrganizationServiceJSONImpl(OrganizationService organizationService) {
    super(organizationService);
  }

  
  
  
  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @GET
  @Path("/user/find-all/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findUsers(@Context UriInfo uriInfo,
                            @QueryParam("username") String username,
                            @QueryParam("firstname") String firstname,
                            @QueryParam("lastname") String lastname,
                            @QueryParam("email") String email,
                            @QueryParam("fromLoginDate") String fromLoginDate,
                            @QueryParam("toLogindate") String toLoginDate) {
    username = RESTAuthenticator.decodeUsername(username);
    try {
    //TODO : now returned all founded user need be carefully then using wildcard (*)
      Query query = new Query();
      query.setUserName(username);
      query.setFirstName(firstname);
      query.setLastName(lastname);
      query.setEmail(email);
      start() ;
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
      PageList pageList = organizationService_.getUserHandler().findUsers(query);
      
      List<User> list = new ArrayList<User>();
      int pages = pageList.getAvailablePage();
      for (int i = 1; i <= pages; i++) {
        list.addAll(pageList.getPage(i));
      }
      List<UserBean> listBean = new ArrayList<UserBean>();
      for (User user : list) {
        if (user != null)
          listBean.add(new UserBean(user));
      }
      UserListBean user_list = new UserListBean(listBean);
      stop();
      return Response.ok(user_list, JSON_CONTENT_TYPE).cacheControl(cc).build();
     
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.status(HTTPStatus.INTERNAL_ERROR)
                             .entity("Thrown exception : " + e)
                             .build();
    }
  }

  

  /**
   * @param baseURI
   * @param question
   * @param from
   * @param to
   * @param sortOrder
   * @param sortField
   * @return
   */
  @SuppressWarnings("unchecked")
  @GET
  @Path("/user/find-user-in-range/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findUsersRange(@Context UriInfo uriInfo,
                                 @QueryParam("question") String question,
                                 @QueryParam("from") Integer from,
                                 @QueryParam("to") Integer to,
                                 @QueryParam("sort-order") String sortOrder,
                                 @QueryParam("sort-field") String sortField) {
    try {      
      /*MultiUserChatManager m = XMPPSessionImpl.multiUserChatManager ;
      for (MultiUserChat multiUserChat : m.getAll()) {
        if (multiUserChat.getRoom().contains(roomName)) {
          try {
            Iterator<String> it = multiUserChat.getConfigurationForm().getField("muc#roomconfig_allowinvites").getValues()  ;
            if (!it.next().equals("1")) {
              it = multiUserChat.getConfigurationForm().getField("muc#roomconfig_roomowners").getValues()  ;
              if (!it.next().contains(username)) {
                if (RESTXMPPService.rb != null) {
                  return Response.status(HTTPStatus.UNAUTHORIZED)
                    .entity(RESTXMPPService.rb.getString("chat.message.unauthorized"))
                    .build();
                }                
              }
            }
            break ;
          } catch (Exception e) {
            return Response.status(HTTPStatus.UNAUTHORIZED)
              .entity(RESTXMPPService.rb.getString("chat.message.unauthorized"))
              .build();  
          }          
        }
      }*/
      List<User> temp = new ArrayList<User>();
      Comparator<User> comparator = getComparator(sortField, sortOrder);
      if (comparator == null) {
        LOGGER.error("You set wrong parameters fo sorting! sort-order = [" + ASCENDING + ", "
            + DESCENDING + "], " + "sort-field = [" + USERNAME + ", " + LASTNAME + ", " + FIRSTNAME
            + "]. You set sort-field = " + sortField + " sort-order = " + sortOrder);
        return Response.status(HTTPStatus.BAD_REQUEST)
                               .entity("You set wrong parameters fo sorting! sort-order = ["
                                   + ASCENDING + ", " + DESCENDING + "], " + "sort-field = ["
                                   + USERNAME + ", " + LASTNAME + ", " + FIRSTNAME
                                   + "]. You set sort-field = " + sortField + " sort-order = "
                                   + sortOrder)
                               .build();
      }
      SortedSet<User> users = new TreeSet<User>(comparator);
      int numResult = to - from;
      if (numResult <= 0) 
        return Response.noContent().cacheControl(cc).build(); 
      Query query = new Query();
      query.setUserName(question);
      start() ;
      PageList pageList = userHandler.findUsers(query);
      int totalUsers = 0;
      Iterator<User> i = null;
      if("*".equals(question.trim())){
        pageList.setPageSize(numResult);
        int page = from / numResult + 1;
        temp = pageList.getPage(page);
        for (User user : temp) {
          if (!users.contains(user))
            users.add(user);
        }
        totalUsers = pageList.getAvailable();
        i = users.iterator();
      }
      else {
      //For CS-2662 and CS-3032
        temp = pageList.getAll();
        for (User user : temp) {
          if (!users.contains(user))
            users.add(user);
        }
        
        query = new Query();
        query.setFirstName(question);
        pageList = userHandler.findUsers(query);
        temp = pageList.getAll();
        for (User user : temp) {
          if (!users.contains(user))
            users.add(user);
        }

        query = new Query();
        query.setLastName(question);
        pageList = userHandler.findUsers(query);
        temp = pageList.getAll();
        for (User user : temp) {
          if (!users.contains(user))
            users.add(user);
        }
        
        totalUsers = users.size();
        
        List<User> list = new ArrayList<User>();
        list.addAll(users);
        int page = from / numResult + 1;
        int f = (page - 1) * numResult;
        int t = page * numResult;
        if(t > totalUsers) t = totalUsers;
        list =  list.subList(f, t);
        
        i = list.iterator();
      }
            
      List<UserBean> uList = new ArrayList<UserBean>();
      while (i.hasNext()) {
        User user = (User) i.next();
        uList.add(new UserBean(user));
      }
      UserListBean user_list = new UserListBean(uList);
      //user_list.setTotalUser(userHandler.getUserPageList(20).getAvailable());
      user_list.setTotalUser(totalUsers);
      stop() ;
      return Response.ok(user_list, JSON_CONTENT_TYPE).cacheControl(cc).build();
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.error("Thrown exception : " + e);
      return Response.status(HTTPStatus.INTERNAL_ERROR)
                             .entity("Thrown exception : " + e)
                             .build();
    }
  }

  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("/user/info/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUser(@PathParam("username") String username) {
    username = RESTAuthenticator.decodeUsername(username);
    try {
      start() ;
      User user = userHandler.findUserByName(username);
      stop() ;
      if (user == null) {
        return Response.status(HTTPStatus.NOT_FOUND).entity("User '" + username
            + "' not found.").build();
      }
      return Response.ok(user).cacheControl(cc).build();

    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.status(HTTPStatus.INTERNAL_ERROR)
                             .entity("Thrown exception : " + e)
                             .build();
    }
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("/users/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUsers(@Context UriInfo uriInfo) {
    return Response.noContent().cacheControl(cc).build();
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("/user/count/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUsersCount() {
    try {
      start() ;
      int number = userHandler.getUserPageList(20).getAvailable();
      stop();
      return Response.ok(new CountBean(number), JSON_CONTENT_TYPE).cacheControl(cc).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.status(HTTPStatus.INTERNAL_ERROR)
                             .entity("Thrown exception : " + e)
                             .build();
    }
  }

//  



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
