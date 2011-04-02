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

import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
/**
 * @author vetal
 *
 */
public class FileTransferRequestBean {

  /**
   * 
   */
  private String uuid;

  /**
   * 
   */
  private String description;

  /**
   * 
   */
  private String filename;

  /**
   * 
   */
  private long   fileSize;

  /**
   * 
   */
  private String mimeType;

  /**
   * 
   */
  private String requestor;

  /**
   * 
   */
  private int    responseTimeout;

  /**
   * @param fileTransferRequest the fileTransferRequestBean
   * @param uuid the id
   */
  public FileTransferRequestBean(FileTransferRequest fileTransferRequest, String uuid) {
    this.uuid = uuid;
    this.description = fileTransferRequest.getDescription();
    this.filename = fileTransferRequest.getFileName();
    this.fileSize = fileTransferRequest.getFileSize();
    this.mimeType = fileTransferRequest.getMimeType();
    this.requestor = fileTransferRequest.getRequestor();
    this.responseTimeout = OutgoingFileTransfer.getResponseTimeout();

  }

  /**
   * 
   */
  public FileTransferRequestBean() {
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
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
   * @return the mimeType
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * @param mimeType the mimeType to set
   */
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * @return the requestor
   */
  public String getRequestor() {
    return requestor;
  }

  /**
   * @param requestor the requestor to set
   */
  public void setRequestor(String requestor) {
    this.requestor = requestor;
  }

  /**
   * @return the uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * @param uuid the id to set
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * @return the responseTimeout
   */
  public int getResponseTimeout() {
    return responseTimeout;
  }

  /**
   * @param responseTimeout the responseTimeout to set
   */
  public void setResponseTimeout(int responseTimeout) {
    this.responseTimeout = responseTimeout;
  }

}
