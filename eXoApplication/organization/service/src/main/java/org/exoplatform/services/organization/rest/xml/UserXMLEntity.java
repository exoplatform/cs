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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Build XML document that content information about user (name , username, email and group that user belong to)
 * 
 * @author <a href="mailto:vitalka_p@ukr.net">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class UserXMLEntity implements SerializableEntity {

  private final User user_;

  public UserXMLEntity(User user) {
    user_ = user;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.SerializableEntity#writeObject(java.io.OutputStream)
   */
  public void writeObject(OutputStream _out) throws IOException {
    try {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
      XMLStreamWriter xsw = outputFactory.createXMLStreamWriter(_out);
      xsw.writeStartDocument();
      xsw.writeStartElement("user");
      xsw.writeDefaultNamespace(XMLContants.EXO_NAMESPACE_URL);
      xsw.writeAttribute("user-name", user_.getUserName());
      xsw.writeStartElement("first-name");
      xsw.writeCharacters((user_.getFirstName() != null) ? user_.getFirstName() : "");
      xsw.writeEndElement();
      xsw.writeStartElement("last-name");
      xsw.writeCharacters((user_.getLastName() != null) ? user_.getLastName() : "");
      xsw.writeEndElement();
      xsw.writeStartElement("email");
      xsw.writeCharacters((user_.getEmail() != null) ? user_.getEmail() : "");
      xsw.writeEndElement();
      xsw.writeStartElement("password");
      xsw.writeCharacters((user_.getPassword() != null) ? user_.getPassword() : "");
      xsw.writeEndElement();
      xsw.writeEndDocument();
      xsw.flush();
      xsw.close();
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }
    
}
