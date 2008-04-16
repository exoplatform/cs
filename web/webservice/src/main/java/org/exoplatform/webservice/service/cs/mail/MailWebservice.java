/**
 * 
 */
package org.exoplatform.webservice.service.cs.mail;

import java.util.List;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.JobDetail;

/**
 * @author Uoc Nguyen
 * 
 */
public class MailWebservice implements ResourceContainer {

  public static final int MIN_SLEEP_TIMEOUT = 500;
  public static final int TOTAL_SLEEP_TIME = 16;
  
  public MailWebservice() {
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/cs/mail/checkmail/{accountId}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response checkMail(@URIParam("accountId")
  String accountId) throws Exception {
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    String userName = this.getUserName();
    boolean isExists = false;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    JobSchedulerService schedulerService = 
      (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
    List allJobs = schedulerService.getAllJobs() ;
    for(Object obj : allJobs) {
      if(((JobDetail)obj).getName().equals(userName + ":" + accountId)) {
         isExists = true;
      }
    }
    
    if (!isExists) {
      mailService.checkMail(userName, accountId);
    }
    
    StringBuffer buffer = new StringBuffer();
    buffer.append("<info>");
    buffer.append("  <checkingmail>");
    buffer.append("    <status>" + CheckingInfo.START_CHECKMAIL_STATUS + "</status>");
    buffer.append("  </checkingmail>");
    buffer.append("</info>");
    
    return Response.Builder.ok(buffer.toString(), "text/xml").build();
  }
  
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/cs/mail/stopcheckmail/{accountId}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response stopCheckMail(@URIParam("accountId")
      String accountId) throws Exception {
    MailService mailService = (MailService) ExoContainerContext
    .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    StringBuffer buffer = new StringBuffer();
    CheckingInfo checkingInfo = mailService.getCheckingInfo(this.getUserName(), accountId);
    if (checkingInfo != null) {
      checkingInfo.setRequestStop(true);
      buffer.append("<info>");
      buffer.append("  <checkingmail>");
      buffer.append("    <status>" + CheckingInfo.REQUEST_STOP_STATUS + "</status>");
      buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
      buffer.append("  </checkingmail>");
      buffer.append("</info>");
    }
    mailService.removeCheckingInfo(this.getUserName(), accountId);
    return Response.Builder.ok(buffer.toString(), "text/xml").build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/cs/mail/checkmailjobinfo/{accountId}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response getCheckMailJobInfo(@URIParam("accountId")
      String accountId) throws Exception {
    MailService mailService = (MailService) ExoContainerContext
                                .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    CheckingInfo checkingInfo = mailService.getCheckingInfo(this.getUserName(), accountId);
    if (checkingInfo == null) {
      Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      checkingInfo = mailService.getCheckingInfo(this.getUserName(), accountId);
    }
    if (checkingInfo == null) {
      return Response.Builder.serverError().build();
    }
    if (!checkingInfo.hasChanged()) {
      Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      for (int i=1; i<MailWebservice.MIN_SLEEP_TIMEOUT * MailWebservice.TOTAL_SLEEP_TIME; i++) {
        if (checkingInfo.hasChanged()) {
          break;
        }
        Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      }
    }
    StringBuffer buffer = new StringBuffer();
    if (checkingInfo != null) {
      if (checkingInfo.hasChanged()) {
        buffer.append("<info>");
        buffer.append("  <checkingmail>");
        buffer.append("    <status>" + CheckingInfo.DOWNLOADING_MAIL_STATUS + "</status>");
        buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
        buffer.append("    <total>" + checkingInfo.getTotalMsg() + "</total>");
        buffer.append("    <completed>" + (checkingInfo.getFetching() - 1) + "</completed>");
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
      if (checkingInfo.getStatusCode() == CheckingInfo.FINISHED_CHECKMAIL_STATUS) {
        buffer.append("<info>");
        buffer.append("  <checkingmail>");
        buffer.append("    <status>" + CheckingInfo.FINISHED_CHECKMAIL_STATUS + "</status>");
        buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
        buffer.append("  </checkingmail>");
        buffer.append("</info>");
        mailService.removeCheckingInfo(this.getUserName(), accountId);
      }
    } else {
      
    }
    return Response.Builder.ok(buffer.toString(), "text/xml").build();
  }

  public String getUserName() throws Exception {
    AuthenticationService authService = (AuthenticationService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(
            AuthenticationService.class);
    return authService.getCurrentIdentity().getUsername();
  }
}
