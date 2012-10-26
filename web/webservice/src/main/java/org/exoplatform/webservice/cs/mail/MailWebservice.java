/**
 * 
 */
package org.exoplatform.webservice.cs.mail;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webservice.cs.bean.AccountsData;
import org.exoplatform.webservice.cs.bean.ContactData;
import org.exoplatform.webservice.cs.bean.FoldersTagsData;
import org.exoplatform.webservice.cs.bean.MessageData;

/**
 * @author Uoc Nguyen Modified by : Phung Nam (phunghainam@gmail.com)
 * @anchor CSref.PublicRESTAPIs.MailApplication
 */


@Path("/cs/mail")
public class MailWebservice implements ResourceContainer {

  public static final String TEXT_XML          = "text/xml".intern();

  public final static String JSON              = "application/json".intern();

  public final static String TEXT              = "plain/text".intern();

  public static final int    MIN_SLEEP_TIMEOUT = 100;

  public static final int    MAX_TIMEOUT       = 16;

  private static final Log log = ExoLogger.getLogger("cs.mail.webservice");

  public MailWebservice() {

  } 

  /**
   * Provide communication from the checking-mail job and update the status on the server.
   * This service requires authentication and permission of the _Users_ group only.
   * @param userName The given user Id.
   * @param accountId The Id of an email account. In the current implementation, one user is allowed to have multiple email accounts.
   * @param folderId The given folder Id (name) to check messages inside.
   * @return response in text/xml format
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.checkMail
   */
  @GET
  @RolesAllowed("users")
  @Path("/checkmail/{username}/{accountId}/{folderId}/")
  public Response checkMail(@PathParam("username") String userName,
                            @PathParam("accountId") String accountId,
                            @PathParam("folderId") String folderId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    if(!isAuthorized(userName)) return Response.ok(Status.UNAUTHORIZED).cacheControl(cacheControl).build();
    MailService mailService = (MailService) PortalContainer.getInstance()
        .getComponentInstanceOfType(MailService.class);
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    if (checkingInfo == null || checkingInfo.getStatusCode() == CheckingInfo.FINISHED_CHECKMAIL_STATUS
        || checkingInfo.getStatusCode() == CheckingInfo.CONNECTION_FAILURE || checkingInfo.getStatusCode() == CheckingInfo.RETRY_PASSWORD || 
        checkingInfo.getStatusCode() == CheckingInfo.COMMON_ERROR) {
      mailService.checkMail(userName, accountId, folderId);
    } else if (checkingInfo != null) {
      checkingInfo.setHasChanged(true);
      mailService.updateCheckingMailStatusByCometd(userName, accountId, checkingInfo);   
    }
    StringBuffer buffer = new StringBuffer("");
    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }


  /**
   * Do the synchronization folders of the Mail application.
   * This service requires authentication and permission of the _Users_ group only.
   * @param userName The given username.
   * @param accountId The Id of an email account. In the current implementation, one user is allowed to have multiple accounts.
   * @return text/xml in response 
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.syncFolders
   */
  @GET
  @RolesAllowed("users")
  @Path("/synchfolders/{username}/{accountId}/")
  public Response synchFolders(@PathParam("username") String userName,
                               @PathParam("accountId") String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    if(!isAuthorized(userName)) return Response.ok(Status.UNAUTHORIZED).cacheControl(cacheControl).build();
    MailService mailService = (MailService) ExoContainerContext.getCurrentContainer()
        .getComponentInstanceOfType(MailService.class);

    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    if (checkingInfo == null || checkingInfo.getSyncFolderStatus() == CheckingInfo.FINISH_SYNC_FOLDER) {
      mailService.synchImapFolders(userName, accountId);
      checkingInfo = mailService.getCheckingInfo(userName, accountId);
    }

    StringBuffer buffer = new StringBuffer();
    buffer.append("<info>");
    buffer.append("  <checkingmail>");
    if (checkingInfo != null) {
      buffer.append("    <status>" + checkingInfo.getSyncFolderStatus() + "</status>");
    }
    buffer.append("  </checkingmail>");
    buffer.append("</info>");
    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }

  /**
   * Stop the checking-mail job in case the user does not want to run the checking mail.
   * This service requires authentication and permission of the _Users_ group only.
   * @param userName The given user Id.
   * @param accountId The Id of an email account. In the current implementation, one user is allowed to have multiple accounts for a single user, so this should be a specific account Id.
   * @return text/xml in response
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.stopCheckMail
   */
  @GET
  @RolesAllowed("users")
  @Path("/stopcheckmail/{username}/{accountId}/")
  public Response stopCheckMail(@PathParam("username") String userName,
                                @PathParam("accountId") String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    if(!isAuthorized(userName)) return Response.ok(Status.UNAUTHORIZED).cacheControl(cacheControl).build();
    MailService mailService = (MailService) ExoContainerContext.getCurrentContainer()
        .getComponentInstanceOfType(MailService.class);
    StringBuffer buffer = new StringBuffer();
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    if (checkingInfo != null) {
      buffer.append("<info>");
      buffer.append("  <checkingmail>");
      buffer.append("    <status>" + checkingInfo.getStatusCode() + "</status>");
      buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
      buffer.append("  </checkingmail>");
      buffer.append("</info>");
      mailService.stopCheckMail(userName, accountId);
    } else {
      Response.status(HTTPStatus.INTERNAL_ERROR);
      return Response.ok().build();
    }

    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }

  /**
   * Get more information of the mail-checking job.
   * This service requests for authentication and authorization only for the _Users_ group.
   * @param userName The given user Id.
   * @param accountId The given user account. By implementation, there are multiple accounts for a single user.
   * @return text/xml in the response
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.getCheckMailJobInfo
   */
  @GET
  @RolesAllowed("users")
  @Path("/checkmailjobinfo/{username}/{accountId}/")
  public Response getCheckMailJobInfo(@PathParam("username") String userName,
                                      @PathParam("accountId") String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    if(!isAuthorized(userName)) return Response.ok(Status.UNAUTHORIZED).cacheControl(cacheControl).build();
    MailService mailService = (MailService) ExoContainerContext.getCurrentContainer()
        .getComponentInstanceOfType(MailService.class);
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    StringBuffer buffer = new StringBuffer();
    if (checkingInfo == null) {
      Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      checkingInfo = mailService.getCheckingInfo(userName, accountId);
      buffer.append("<info>");
      buffer.append("  <checkingmail>");
      buffer.append("    <status>1000</status>");
      buffer.append("  </checkingmail>");
      buffer.append("</info>");
      return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
    }
    if (checkingInfo != null && !checkingInfo.hasChanged()) {
      Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      for (int i = 1; i < MailWebservice.MIN_SLEEP_TIMEOUT * MailWebservice.MAX_TIMEOUT; i++) {
        if (checkingInfo.hasChanged())
          break;
        Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      }
    }
    if (checkingInfo != null) {
      if (checkingInfo.getStatusCode() == CheckingInfo.FINISHED_CHECKMAIL_STATUS
          || checkingInfo.getStatusCode() == CheckingInfo.CONNECTION_FAILURE
          || checkingInfo.getStatusCode() == CheckingInfo.RETRY_PASSWORD
          || checkingInfo.getStatusCode() == CheckingInfo.START_CHECKMAIL_STATUS){
        buffer.append("<info>");
        buffer.append("  <checkingmail>");
        buffer.append("    <status>" + checkingInfo.getStatusCode() + "</status>");
        buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
        buffer.append("  </checkingmail>");
        buffer.append("</info>");
        return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
      } else if (checkingInfo.hasChanged()) {
        buffer.append("<info>");
        buffer.append("  <checkingmail>");
        buffer.append("    <status>" + CheckingInfo.DOWNLOADING_MAIL_STATUS + "</status>");
        buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
        buffer.append("    <total>" + checkingInfo.getTotalMsg() + "</total>");
        buffer.append("    <syncFolderStatus>" + checkingInfo.getSyncFolderStatus()
                      + "</syncFolderStatus>");
        buffer.append("    <completed>" + checkingInfo.getFetching() + "</completed>");
        buffer.append("    <fetchingtofolders>" + checkingInfo.getFetchingToFolders()
                      + "</fetchingtofolders>");
        if (checkingInfo.getMsgId() != null && !checkingInfo.getMsgId().equals("")) {
          buffer.append("    <messageid>"
              + checkingInfo.getMsgId().replace("<", "&lt;").replace(">", "&gt;") + "</messageid>");
        }
        buffer.append("  </checkingmail>");
        buffer.append("</info>");
        checkingInfo.setHasChanged(false);
      } else {
        buffer.append("<info>");
        buffer.append("  <checkingmail>");
        buffer.append("    <status>" + CheckingInfo.NO_UPDATE_STATUS + "</status>");
        buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
        buffer.append("  </checkingmail>");
        buffer.append("</info>");
      }
    }

    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }

  /**
   * Look up all contacts' emails by the input keywords.
   * This service requires authentication and permission of the _Users_ group only.
   * @param keywords The given keywords.
   * @return list of found email in JSon object in response 
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.searchemail
   */
  @GET
  @RolesAllowed("users")
  @Path("/searchemail/{keywords}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchemail(@PathParam("keywords") String keywords) throws Exception {
    ContactService contactSvr = (ContactService) PortalContainer.getInstance()
        .getComponentInstanceOfType(ContactService.class);
    ContactData fullData = new ContactData();
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      if (ConversationState.getCurrent().getIdentity() == null)
        return Response.ok(Status.UNAUTHORIZED).cacheControl(cacheControl).build();
      String username = ConversationState.getCurrent().getIdentity().getUserId();
      if (username == null)
        return Response.ok(Status.UNAUTHORIZED).cacheControl(cacheControl).build();
      ContactFilter filter = new ContactFilter();
      filter.setType(org.exoplatform.contact.service.DataStorage.PERSONAL);
      filter.searchByAnd(false);
      filter.setFullName(keywords);
      filter.setNickName(keywords);
      filter.setLimit(10);
      filter.setEmailAddress(keywords);
      fullData.setInfo(contactSvr.searchEmailsByFilter(username, filter));
    } catch (Exception e) {
      log.error("error search email", e);
      return Response.ok(Status.INTERNAL_SERVER_ERROR).cacheControl(cacheControl).build();
    }
    return Response.ok(fullData, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  /**
   * List the header of unread mails.
   * This service requires authentication and permission of the _Users_ group only.
   * 
   * @param accountId The Id of an email account.
   * @param folderId The Id of an email folder.
   * @param tagId The Id of a tag which is used to highlight for email messages.
   * @param limit The maximum number of returned emails.
   * @return application/json content type
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.unreadMail
   */
  @GET
  @RolesAllowed("users")
  @Path("/unreadMail/{accountId}/{folderId}/{tagId}/{limit}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unreadMail(@PathParam("accountId") String accountId,
                             @PathParam("folderId") String folderId,
                             @PathParam("tagId") String tagId,
                             @PathParam("limit") int limit) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    MessageFilter filter = new MessageFilter("Folder");
    MessageData data = new MessageData();
    if (Utils.isEmptyField(accountId) || accountId.equals("_")) 
      return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    filter.setAccountId(accountId);
    if (!Utils.isEmptyField(folderId) && !folderId.equals("_")) filter.setFolder(new String[] { folderId });
    if (!Utils.isEmptyField(tagId) && !tagId.equals("_")) filter.setTag(new String[] { tagId });
    filter.setViewQuery("@" + Utils.EXO_ISUNREAD + "='true'");
    filter.setOrderBy(Utils.EXO_LAST_UPDATE_TIME);
    List<Message> messList;
    try {
      messList = mailService.getMessages(username, filter);      
    } catch (Exception e) {
      return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    }
    try {
      data.setInfo(messList.subList(0, limit));
    } catch (IndexOutOfBoundsException e) {
      data.setInfo(messList.subList(0, messList.size()));
    }
    return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  /**
   * List the accounts of the current user.
   * This service requires authentication and permission of the _Users_ group only.
   * @return application/json content type
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.getAccounts
   */
  @GET
  @RolesAllowed("users")
  @Path("/getAccounts/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAccounts() throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    List<Account> accounts = mailService.getAccounts(username);
    AccountsData data = new AccountsData();
    if (accounts.size() > 0) data.setInfo(accounts);
    return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  /**
   * List folders and tags of the current accounts.
   * This service requires authentication and permission of the _Users_ group only.
   * @param accountId The Id of an email account.
   * @return application/json content type
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.getFoldersTags
   */
  @GET
  @RolesAllowed("users")
  @Path("/getFoldersTags/{accountId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFoldersTags(@PathParam("accountId") String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    FoldersTagsData data = new FoldersTagsData();
    data.setFolders(mailService.getFolders(username, accountId));
    data.setTags(mailService.getTags(username, accountId));
    return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  private boolean isAuthorized(String usename) {
    return (ConversationState.getCurrent() != null && ConversationState.getCurrent().getIdentity() != null && 
        ConversationState.getCurrent().getIdentity().getUserId() != null && ConversationState.getCurrent().getIdentity().getUserId().equals(usename)  
        );
  }

  /**
   * Help checking supported types from any given mail server by host name or IP.
   * @param mechanisms The name of mechanisms to check.
   * @param username The given username.
   * @param proto The type of protocol to check.
   * @param host The host name or the mail server IP.
   * @return JSon object in response
   * @throws Exception
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.checkForSupportedTypes
   */
  @GET
  @RolesAllowed("users")
  @Path("/checkforsupportedtypes/{mechs}/{username}/{protocol}/{host}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response checkForSupportedTypes(@PathParam("mechs") String mechanisms, @PathParam("username") String username,
                                         @PathParam("protocol") String proto, @PathParam("host") String host) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    StringBuilder data = new StringBuilder();
    String[] mechs = mechanisms.split(",");
    int i = 0;
    for(String m : mechs){
      if(m.equalsIgnoreCase("kerberos-gssapi")) mechs[i] = m.substring(m.indexOf("-"));
      mechs[i] = m;
      i =+ 1;
    }
    SaslClient sasl = Sasl.createSaslClient(mechs, username, proto, host, null, null);
    if(sasl != null)
      log.info(sasl.getMechanismName());
    if(mechs != null && mechs.length>0){
      for(String mech : mechs){
        data.append(mech);
      }
    }
    return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  /**
   * Get all users from the user database of the portal. 
   * This service is currently deprecated.
   * @param keywords The text which is used to search for a specific username.
   * @return application/json content type
   * 
   * @anchor CSref.PublicRESTAPIs.MailApplication.searchUser
   */
  @SuppressWarnings({"deprecation" })
  @GET
  @RolesAllowed("users")
  @Path("/searchuser/{keywords}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchUser(@PathParam("keywords") String keywords) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    ContactData fullData = new ContactData();

    try {
      OrganizationService service = (OrganizationService)PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
      List<User> user = service.getUserHandler().findUsers(new Query()).getAll();
      List<String> userList = new ArrayList<String>();
      if(user != null && user.size() > 0)
        for(User u : user){
          String username = u.getUserName();
          if(username.contains(keywords)){
            username =  "<div class='AutoCompleteItem'>" + username.replace(keywords,"<b>" + keywords + "</b>") + "</div>";
            userList.add(username);
          }
        }
      fullData.setInfo(userList);
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(cacheControl).build();
    }
    return Response.ok(fullData, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

}