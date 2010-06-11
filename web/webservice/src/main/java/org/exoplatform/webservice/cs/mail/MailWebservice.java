
/**
 * 
 */
package org.exoplatform.webservice.cs.mail;

import java.util.Properties;

import javax.jcr.AccessDeniedException;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webservice.cs.bean.ContactData;

import com.sun.mail.imap.IMAPStore;

/**
 * @author Uoc Nguyen Modified by : Phung Nam (phunghainam@gmail.com)
 */
@Path("/cs/mail")
public class MailWebservice implements ResourceContainer {

  public static final String TEXT_XML          = "text/xml".intern();

  public final static String JSON              = "application/json".intern();

  public final static String TEXT              = "plain/text".intern();

  public static final int    MIN_SLEEP_TIMEOUT = 100;

  public static final int    MAX_TIMEOUT       = 16;

  private static final Log log = LogFactory.getLog(MailWebservice.class);
  // TODO need to organize code, don't keep html content here !
  public MailWebservice() {
  }

  @GET
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
    
    //checkingInfo = null;
    if (checkingInfo == null || checkingInfo.getStatusCode() == CheckingInfo.FINISHED_CHECKMAIL_STATUS
        || checkingInfo.getStatusCode() == CheckingInfo.CONNECTION_FAILURE || checkingInfo.getStatusCode() == CheckingInfo.RETRY_PASSWORD || 
        checkingInfo.getStatusCode() == CheckingInfo.COMMON_ERROR) {
      mailService.checkMail(userName, accountId, folderId);
    } else if (checkingInfo != null) {
      checkingInfo.setHasChanged(true);
      mailService.updateCheckingMailStatusByCometd(userName, accountId, checkingInfo);
      
    }
    /*if (folderId != null && folderId.trim().length() > 0
        && !folderId.equalsIgnoreCase("checkall")) {
      checkingInfo.setRequestingForFolder_(folderId);
    }*/
    StringBuffer buffer = new StringBuffer("");
    
    /*
    buffer.append("<info>");
    buffer.append("  <checkingmail>");
    int stt = CheckingInfo.START_CHECKMAIL_STATUS;
    String sttMsg = "";
    if (checkingInfo != null) sttMsg = checkingInfo.getStatusMsg();
    
    /////////////////////////////////
    try {
      Account account = mailService.getAccountById(userName, accountId);
      Properties props = System.getProperties();
      // this line fix for base64 encode problem with corrupted
      // attachments
      props.setProperty("mail.mime.base64.ignoreerrors", "true");

      String socketFactoryClass = "javax.net.SocketFactory";
      if (account.isIncomingSsl()) {
        socketFactoryClass = Utils.SSL_FACTORY;
      }
      props.setProperty("mail.imap.socketFactory.fallback", "false");
      props.setProperty("mail.imap.socketFactory.class", socketFactoryClass);

      Session session = Session.getDefaultInstance(props, null);
      IMAPStore imapStore = (IMAPStore) session.getStore("imap");
      try {
        if (Utils.isEmptyField(account.getIncomingPassword()))
          account.setIncomingPassword(IdGenerator.generate());
        imapStore.connect(account.getIncomingHost(),
                          Integer.valueOf(account.getIncomingPort()),
                          account.getIncomingUser(),
                          account.getIncomingPassword());
      } catch (AuthenticationFailedException e) {
          sttMsg = "The userName or password may be wrong.";
          stt = CheckingInfo.RETRY_PASSWORD;
      } catch (MessagingException e) {
          sttMsg = "Connecting failed. Please check server configuration.";
          stt = CheckingInfo.CONNECTION_FAILURE;
      } catch (IllegalStateException e) {
        log.error("cannot connect to server", e);
      } catch (Exception e) {
        log.error("cannot connect to server", e);
        sttMsg ="There was an unexpected error. Connecting failed.";
        stt = CheckingInfo.CONNECTION_FAILURE;
      }
    } catch (AccessDeniedException e) {
      //log.error("cannot connect to server", e);
    } catch (Exception ex) {
      log.error("cannot connect to server", ex);
    }
    ///////////////////////////////////
    
    buffer.append("    <status>" + stt + "</status>");
    buffer.append("    <statusmsg>" + sttMsg + "</statusmsg>");
    buffer.append("  </checkingmail>");
    buffer.append("</info>");
    */
    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }

  @GET
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
    if (checkingInfo == null) {
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
//    mailService.removeCheckingInfo(userName, accountId);
    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }

  @GET
  @Path("/stopcheckmail/{username}/{accountId}/")
  public Response stopCheckMail(@PathParam("username") String userName,
                                @PathParam("accountId") String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
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

      checkingInfo.setRequestStop(true);
//      mailService.removeCheckingInfo(userName, accountId);
      mailService.stopCheckMail(userName, accountId);
    } else {
      Response.status(HTTPStatus.INTERNAL_ERROR);
      return Response.ok().build();
    }

    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }

  @GET
  @Path("/checkmailjobinfo/{username}/{accountId}/")
  public Response getCheckMailJobInfo(@PathParam("username") String userName,
                                      @PathParam("accountId") String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
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
        if(checkingInfo.getStatusCode() == CheckingInfo.FINISHED_CHECKMAIL_STATUS){
//          mailService.removeCheckingInfo(userName, accountId);
        }
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
   * Get all email from contacts data base, the security level will take from
   * ConversationState
   * 
   * @param keywords : the text to compare with data base
   * @return application/json content type
   */
  @GET
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
  
  private boolean isAuthorized(String usename) {
    return (ConversationState.getCurrent() != null && ConversationState.getCurrent().getIdentity() != null && 
        ConversationState.getCurrent().getIdentity().getUserId() != null && ConversationState.getCurrent().getIdentity().getUserId().equals(usename)  
    );
  }
}
