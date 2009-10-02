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

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.PageList;
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


/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id:$
 */

//For chat application 

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

  
  
  
  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
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
    //TODO : now returned all founded user need be carefully then using wildcard (*)        
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
      PageList pageList = userHandler.findUsers(query);
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
      return Response.Builder.ok(user_list, JSON_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
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
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/find-user-in-range/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response findUsersRange(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
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
      int numResult = to - from;
      if (numResult <= 0) 
        return Response.Builder.noContent().build(); 
      Query query = new Query();
      query.setUserName(question);
      PageList pageList = userHandler.findUsers(query);
      pageList.setPageSize(numResult);
      int page = from / numResult + 1;
      temp = pageList.getPage(page);
      for (User user : temp) {
        if (!users.contains(user))
          users.add(user);
      }
      List<UserBean> uList = new ArrayList<UserBean>();
      Iterator<User> i = users.iterator();
      while (i.hasNext()) {
        User user = (User) i.next();
        uList.add(new UserBean(user));
      }
      UserListBean user_list = new UserListBean(uList);
      user_list.setTotalUser(userHandler.getUserPageList(20).getAvailable());
      return Response.Builder.ok(user_list, JSON_CONTENT_TYPE).build();
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

  /**
   * {@inheritDoc}
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/users/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getUsers(@ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI) {
    return Response.Builder.noContent().build();
  }

  /**
   * {@inheritDoc}
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/user/count/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getUsersCount() {
    try {
      int number = userHandler.getUserPageList(20).getAvailable();
      return Response.Builder.ok(new CountBean(number), JSON_CONTENT_TYPE).build();
    } catch (Exception e) {
      LOGGER.error("Thrown exception : " + e);
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
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
