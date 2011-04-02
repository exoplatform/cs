/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.xmpp.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.FilenameUtils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.xmpp.connection.impl.XMPPMessenger;
import org.exoplatform.services.xmpp.connection.impl.XMPPSessionImpl;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          vietnt84@gmail.com
 * Feb 2, 2010  
 */

@Path("/fileexchange")
public class FileExchangeService implements ResourceContainer {

  private final Log log    = ExoLogger.getLogger(FileExchangeService.class.getName());

  private String    tmpDir = null;

  public FileExchangeService(InitParams initParams) {
    if (initParams != null && initParams.getValueParam("tmpdir") != null)
      tmpDir = initParams.getValueParam("tmpdir").getValue();
    if (tmpDir == null) {
      if (log.isDebugEnabled())
        log.debug("Tmp dir is not set, default " + System.getProperty("java.io.tmpdir"));
      tmpDir = System.getProperty("java.io.tmpdir");
    }
  }

  @POST
  public Response upload() throws IOException {
    EnvironmentContext env = EnvironmentContext.getCurrent();
    HttpServletRequest req = (HttpServletRequest) env.get(HttpServletRequest.class);
    HttpServletResponse resp = (HttpServletResponse) env.get(HttpServletResponse.class);
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
        XMPPMessenger messenger = (XMPPMessenger) PortalContainer.getInstance().getComponentInstanceOfType(XMPPMessenger.class);
        for (FileItem fileItem : items) {
          XMPPSessionImpl session = (XMPPSessionImpl) messenger.getSession(username);
          String fileName = fileItem.getName();
          String fileType = fileItem.getContentType();
          if (session != null) {
            if (fileName != null) {
              // TODO Check this for compatible or not
              // It's necessary because IE posts full path of uploaded files
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
        return Response.status(HTTPStatus.BAD_REQUEST).entity(e.getMessage()).build();
      }
    }
    return Response.ok().build();
  }
}
