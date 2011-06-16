/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.mail.service;

import javax.mail.Store;

/**
 * Created by The eXo Platform SAS
 * Author : Nam Phung
 *          phunghainam@gmail.com
 * Apr 1, 2008  
 */
public class CheckingInfo {
  public static final int    START_CHECKMAIL_STATUS     = 101;

  public static final int    NO_UPDATE_STATUS           = 201;

  public static final int    DOWNLOADING_MAIL_STATUS    = 150;

  public static final int    FINISHED_CHECKMAIL_STATUS  = 200;

  public static final int    REQUEST_STOP_STATUS        = 202;

  public static final int    EXECUTING_FILTER           = 203;

  public static final int    CONNECTION_FAILURE         = 102;

  public static final int    RETRY_PASSWORD             = 103;

  public static final int    COMMON_ERROR               = 104;

  public static final int    START_SYNC_FOLDER          = 301;

  public static final int    FINISH_SYNC_FOLDER         = 302;

  public static final int    FINISHED_SYNC_FOLDER       = 303;

  public static final String START_MSG_KEY              = "starting-status".intern();

  public static final String FETCHING_MSG_KEY           = "fetching-mail".intern();

  public static final String START_SYNC_FOLDER_MSG_KEY  = "start-sync-folder".intern();

  public static final String FINISH_SYNC_FOLDER_MSG_KEY = "finish-sync-folder".intern();

  public static final String FINISH_MSG_KEY             = "finish-check-mail".intern();

  public static final String FINISH_BY_INTERUPTED_KEY   = "finish-check-mail-by-interrupted".intern();

  public static final String CONNECTION_FAILURE_KEY     = "error-connection-fail".intern();

  public static final String RETRY_PASSWORD_KEY         = "msg-retry-password".intern();

  public static final String COMMON_ERROR_KEY           = "error-common".intern();

  private int                totalMsg_                  = 0;

  private int                fetching_                  = 0;

  private int                statusCode_;

  private String             statusMsg_;

  private String             fetchingToFolders_;

  private boolean            hasChanged_;

  private boolean            isRequestStop_             = false;

  private String             requestingForFolder_;

  private String             msgId_;

  private int                syncFolderStatus_          = 300;

  private StatusInfo         status_                    = new StatusInfo();
  
  private Store              mailStore;

  public int getTotalMsg() {
    return totalMsg_;
  };

  public void setTotalMsg(int totalMsg) {
    totalMsg_ = totalMsg;
    hasChanged_ = true;
  }

  /**
   * reset values to default.
   */
  public void resetValues() {
    totalMsg_ = 0;
    fetching_ = 0;
    statusCode_ = 0;
    statusMsg_ = "";
    fetchingToFolders_ = "";
    hasChanged_ = false;
    isRequestStop_ = false;
    requestingForFolder_ = "";
    msgId_ = "";
    syncFolderStatus_ = 300;
    status_ = new StatusInfo();
  }

  public int getFetching() {
    return fetching_;
  }

  public void setFetching(int in) {
    fetching_ = in;
    hasChanged_ = true;
  }

  public int getSyncFolderStatus() {
    return syncFolderStatus_;
  };

  public void setSyncFolderStatus(int syncFolderStatus) {
    syncFolderStatus_ = syncFolderStatus;
    hasChanged_ = true;
  }

  public String getFetchingToFolders() {
    return fetchingToFolders_;
  }

  public void setFetchingToFolders(String fetchingToFolders) {
    fetchingToFolders_ = fetchingToFolders;
  }

  public String getStatusMsg() {
    return statusMsg_;
  }

  public void setStatusMsg(String statusMsg) {
    if (statusMsg_ != null && statusMsg_.equals(statusMsg)) {
      return;
    }
    statusMsg_ = statusMsg;
    status_.setStatusMsg(statusMsg_);
    hasChanged_ = true;

  }

  public int getStatusCode() {
    return statusCode_;
  }

  public void setStatusCode(int code) {
    synchronized (this) {
      if (statusCode_ != code) {

        status_.setPreviousStatus(statusCode_);
        statusCode_ = code;
        status_.setStatus(statusCode_);
        statusMsg_ = generateStatusMsgKey();
        status_.setStatusMsg(statusMsg_);
        hasChanged_ = true;
      }

    }
  }

  /**
   * assign status as interrupted by user.
   */
  public void assignInterruptedStatus() {
    synchronized (this) {
      status_.setPreviousStatus(statusCode_);
      statusCode_ = FINISHED_CHECKMAIL_STATUS;
      status_.setStatus(statusCode_);
      statusMsg_ = FINISH_BY_INTERUPTED_KEY;
      status_.setStatusMsg(statusMsg_);
      hasChanged_ = true;
    }
  }

  /**
   * generate resource bundle following to status code.
   * @return
   */
  private String generateStatusMsgKey() {
    switch (statusCode_) {
    case START_CHECKMAIL_STATUS:
      return START_MSG_KEY;
    case DOWNLOADING_MAIL_STATUS:
      return FETCHING_MSG_KEY;
    case START_SYNC_FOLDER:
      return START_SYNC_FOLDER_MSG_KEY;
    case FINISH_SYNC_FOLDER:
      return FINISH_SYNC_FOLDER_MSG_KEY;
    case FINISHED_CHECKMAIL_STATUS:
      return FINISH_MSG_KEY;
    case CONNECTION_FAILURE:
      return CONNECTION_FAILURE_KEY;
    case RETRY_PASSWORD:
      return RETRY_PASSWORD_KEY;
    case COMMON_ERROR:
      return COMMON_ERROR_KEY;
    default:
      return "";
    }
  }

  public boolean hasChanged() {
    return hasChanged_;
  }

  public void setHasChanged(boolean b) {
    hasChanged_ = b;
  }

  /**
   * this function is involved to ask stopping request of user. if returned value is true, 
   * the checking mail job will try to stop.
   * @return
   */
  public boolean isRequestStop() {
    return isRequestStop_;
  }

  /**
   * this function sees the statuscode to determine the job is running or not.
   * @return true if the job is running, else return false.
   */
  public boolean isCheckingMailJobRunning() {
    return statusCode_ != CheckingInfo.FINISHED_CHECKMAIL_STATUS && statusCode_ != CheckingInfo.CONNECTION_FAILURE && statusCode_ != CheckingInfo.RETRY_PASSWORD && statusCode_ != CheckingInfo.COMMON_ERROR;
  }

  public void setRequestStop(boolean b) {
    synchronized (this) {
      if (b) {
        if (isCheckingMailJobRunning()) {
          // request to stop checking mail, if the job is running.
          isRequestStop_ = b;

          status_.setPreviousStatus(statusCode_);
          statusCode_ = CheckingInfo.REQUEST_STOP_STATUS;
          status_.setStatus(statusCode_);
          hasChanged_ = true;

        }
      } else {
        isRequestStop_ = b;
      }
    }
  }

  public String getMsgId() {
    return msgId_;
  }

  public void setMsgId(String msgId) {
    msgId_ = msgId;
  }

  public String getRequestingForFolder_() {
    return requestingForFolder_;
  }

  public void setRequestingForFolder_(String str) {
    requestingForFolder_ = str;
  }

  public StatusInfo getStatus() {
    return status_;
  }

  public void setAccountId(String accountId) {
    status_.setAccountId(accountId);
  }

  public String getAccountId() {
    return status_.getAccountId();
  }

  public Store getMailStore() {
    return mailStore;
  }

  public void setMailStore(Store mailStore) {
    this.mailStore = mailStore;
  }
  
  
}
