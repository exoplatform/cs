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
package org.exoplatform.services.xmpp.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.rest.Connector;
import org.exoplatform.services.xmpp.connection.impl.XMPPMessenger;
import org.exoplatform.services.xmpp.connection.impl.XMPPSessionImpl;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class FileExchangeServlet extends HttpServlet implements Connector {
  /**
   * Class logger.
   */
  private final Log log = LogFactory.getLog("ws.FileExchangeServlet");

  /**
   * 
   */
  private String    tmpDir;

  @Override
  public void init() throws ServletException {
    final String tmpdir = this.getInitParameter("tmpdir");
    if (tmpdir != null)
      tmpDir = tmpdir;
    else {
      if (log.isDebugEnabled())
        log.debug("Tmp dir is not set, default " + System.getProperty("java.io.tmpdir"));
      tmpDir = System.getProperty("java.io.tmpdir");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
                                                                         IOException {
    String description = req.getParameter("description");
    String username = req.getParameter("username");
    String requestor = req.getParameter("requestor");
    String isRoom = req.getParameter("isroom");
    boolean isMultipart = FileUploadBase.isMultipartContent(req);
    if (isMultipart) {
      // FileItemFactory factory = new DiskFileUpload();
      // Create a new file upload handler
      // ServletFileUpload upload = new ServletFileUpload(factory);
      DiskFileUpload upload = new DiskFileUpload();
      // Parse the request
      try {
        List<FileItem> items = upload.parseRequest(req);
        ExoContainer container = RootContainer.getInstance();
        container = ((RootContainer) container).getPortalContainer("portal");
        XMPPMessenger messenger = (XMPPMessenger) container.getComponentInstanceOfType(XMPPMessenger.class);
        for (FileItem fileItem : items) {
          XMPPSessionImpl session = (XMPPSessionImpl) messenger.getSession(username);
          String fileName = fileItem.getName();
          String fileType = fileItem.getContentType();
          if (session != null) {
            if (fileName != null) {
              fileName = FilenameUtils.getName(fileName);
              fileType = FilenameUtils.getExtension(fileName);
              File file = new File(tmpDir);
              if (file.isDirectory()) {
                String uuid = UUID.randomUUID().toString();
                boolean success = (new File(tmpDir + "/" + uuid)).mkdir();
                if (success) {
                  String path = tmpDir + "/" + uuid + "/" + fileName;
                  File f = new File(path);
                  success = f.createNewFile();
                  if (success) {
                    // File did not exist and was created
                    InputStream inputStream = fileItem.getInputStream();
                    OutputStream out = new FileOutputStream(f);
                    byte buf[] = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0)
                      out.write(buf, 0, len);
                    out.close();
                    inputStream.close();
                    if (log.isDebugEnabled())
                      log.debug("File " + path + "is created");
                    session.sendFile(requestor, path, description, Boolean.parseBoolean(isRoom));
                  }
                } else {
                  if (log.isDebugEnabled())
                    log.debug("File already exists");
                }
              }
            }
          } else {
            if (log.isDebugEnabled())
              log.debug("XMPPSession for user " + username + " is null!");
          }
        }
      } catch (Exception e) {
        if (log.isDebugEnabled())
         e.printStackTrace();
        resp.sendError(HTTPStatus.BAD_REQUEST, e.getMessage());
      }
    }
  }
}
