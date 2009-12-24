/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.xmpp.bean;

import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class FileTransferResponseBean {

  /**
   * 
   */
  private String fileName;

  /**
   * 
   */
  private long   fileSize;

  /**
   * 
   */
  private String sender;

  /**
   * 
   */
  private String status;
  
  /**
   * 
   */
  private String error;
  
  /**
   * 
   */
  private String errorMessage;

  /**
   * 
   */
  private String receiver;

  /**
   * @param transfer the OutgoingFileTransfer
   * @param sender the sender
   */
  public FileTransferResponseBean(OutgoingFileTransfer transfer, String sender) {
    this.fileName = transfer.getFileName();
    this.fileSize = transfer.getFileSize();
    this.status = transfer.getStatus().name();
    this.sender = sender;
    this.receiver = transfer.getPeer();
    if (transfer.getError() != null){
      this.error = transfer.getError().name();
      this.errorMessage = transfer.getError().getMessage();
      
    }
  }

  /**
   * 
   */
  public FileTransferResponseBean() {
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName the fileName to set
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * @return the fileSize
   */
  public long getFileSize() {
    return fileSize;
  }

  /**
   * @param fileSize the fileSize to set
   */
  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  /**
   * @return the sender
   */
  public String getSender() {
    return sender;
  }

  /**
   * @param sender the sender to set
   */
  public void setSender(String sender) {
    this.sender = sender;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @return the receiver
   */
  public String getReceiver() {
    return receiver;
  }

  /**
   * @param receiver the receiver to set
   */
  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  /**
   * @return the error
   */
  public String getError() {
    return error;
  }

  /**
   * @param error the error to set
   */
  public void setError(String error) {
    this.error = error;
  }

  /**
   * @return the errorMessage
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * @param errorMessage the errorMessage to set
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  
  

}
