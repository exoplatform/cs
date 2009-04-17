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

package org.exoplatform.services.organization.rest.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.transformer.SerializableEntity;


/**
 * @author <a href="mailto:vitalka_p@ukr.net">Vitaly Parfonov</a>
 * @version $Id: $
 */
public final class UserListXMLEntity implements SerializableEntity {

  private final List<User> userList_;
  private final String baseURI_;
  private int prevFrom_ = -1;
  private int nextFrom_ = -1;
  private int range_ = -1;
  

  public UserListXMLEntity(List<User> userList, String baseURI) {
    userList_ = userList;
    baseURI_ = baseURI;
  }

  public UserListXMLEntity(List<User> userList, String baseURI,
      int prevFrom, int nextFrom, int range) {
    userList_ = userList;
    baseURI_ = baseURI;
    prevFrom_ = prevFrom;
    nextFrom_ = nextFrom;
    range_ = range;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.rest.transformer.SerializableEntity#writeObject(java.io.OutputStream)
   */
  public void writeObject(OutputStream out) throws IOException {
    try {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
      XMLStreamWriter xsw = outputFactory.createXMLStreamWriter(out);
      xsw.writeStartDocument();
      xsw.writeStartElement("users");
      xsw.writeDefaultNamespace(XMLContants.EXO_NAMESPACE_URL);
      xsw.writeNamespace(XMLContants.XLINK_PREFIX, XMLContants.XLINK_NAMESPACE_URL);
      for (User u : userList_) {
        xsw.writeStartElement("user");
        xsw.writeAttribute(XMLContants.XLINK_NAMESPACE_URL, XMLContants.XLINK_HREF,
            baseURI_ + "/organization/user/" /*+ u.getUserName()*/
            + "?output=xml&command=info");
        xsw.writeStartElement("name");
        xsw.writeCharacters(u.getUserName());
        xsw.writeEndElement();
        xsw.writeStartElement("first-name");
        xsw.writeCharacters((u.getFirstName() != null) ? u.getFirstName() : "");
        xsw.writeEndElement();
        xsw.writeStartElement("last-name");
        xsw.writeCharacters((u.getLastName() != null) ? u.getLastName() : "");
        xsw.writeEndElement();
        xsw.writeStartElement("email");
        xsw.writeCharacters((u.getEmail() != null) ? u.getEmail() : "");
        xsw.writeEndElement();
        xsw.writeEndElement();
      }
      // If requested list with start index and range then prepare 
      // link for previews and next request. 
      if (prevFrom_ >= 0) {
        xsw.writeStartElement("prev-range");
        xsw.writeAttribute(XMLContants.XLINK_NAMESPACE_URL, XMLContants.XLINK_HREF,
            baseURI_ + "/user/" + prevFrom_ + "/"  + range_
            + "/?output=xml&command=view-range");
        xsw.writeEndElement();
      }
      if (nextFrom_ > 0) {
        xsw.writeStartElement("next-range");
        xsw.writeAttribute(XMLContants.XLINK_NAMESPACE_URL, XMLContants.XLINK_HREF,
            baseURI_ + "/user/" + nextFrom_ + "/"  + range_
            + "/?output=xml&command=view-range");
        xsw.writeEndElement();
      }
      xsw.writeEndElement();
      xsw.writeEndDocument();
      xsw.flush();
      xsw.close();
    } catch (XMLStreamException e) {
      throw new IOException(e.getMessage());
    }
  }
  
}