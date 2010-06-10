package org.exoplatform.mail.service;

public class StatusInfo {
  private int status_;
  private String accountId_;
  private int previousStatus_;
  private String statusMsg_;
  
  public StatusInfo() {}
  
  protected void setStatus(int status) {
    status_ = status;
  }

  protected void setPreviousStatus(int previousStatus) {
    previousStatus_ = previousStatus;
  }

  protected void setStatusMsg(String statusMsg) {
    statusMsg_ = statusMsg;
  }

  public int getPreviousStatus() {
    return previousStatus_;
  }

  public int getStatus() {
    return status_;
  }
  public String getStatusMsg() {
    return statusMsg_;
  }

  public String getAccountId() {
    return accountId_;
  }

  public void setAccountId(String accountId) {
    this.accountId_ = accountId;
  }

}
