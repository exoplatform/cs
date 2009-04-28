/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.content.service;

import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.content.model.ContentItem;
import org.exoplatform.content.model.ContentNode;
import org.exoplatform.services.rss.parser.DefaultRSSChannel;
import org.exoplatform.services.rss.parser.DefaultRSSItem;
import org.exoplatform.services.rss.parser.RSSDocument;
import org.exoplatform.services.rss.parser.RSSParser;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Jul 21, 2006  
 */
public class RSSContentPlugin extends ContentPlugin {

  private RSSParser service_;

  public RSSContentPlugin(RSSParser service){
    super();
    type ="rss";
    service_ = service;
  }

  @SuppressWarnings("unchecked")
  public PageList loadContentMeta(ContentNode node) throws Exception {
    //TODO: tuan.pham CS-2531 get encode from rss file
    DocumentBuilder  docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder() ;
    Document doc = docbuilder.parse(node.getUrl()) ;
    String encode = doc.getXmlEncoding() ;
    if (encode == null || encode.trim().length() == 0) encode = "utf-8" ;
    URL uri = new URL(node.getUrl()); 
    RSSDocument<DefaultRSSChannel, RSSItem> document = 
      service_.createDocument(uri, encode, DefaultRSSChannel.class, RSSItem.class);
    List<RSSItem> list = document.getItems();     
    return new ContentPageList(list);
  } 

  static public class RSSItem extends DefaultRSSItem implements ContentItem {

    public RSSItem(){
    }

    //@SuppressWarnings("unused")
    //TODO: dang.tung -> set creator of content
    public void setCreator(String creator){ super.setCreator(creator) ;}
    public String getCreator(){return super.getCreator() ;
    }

  }

}
