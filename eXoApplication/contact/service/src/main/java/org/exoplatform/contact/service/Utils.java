package org.exoplatform.contact.service ;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */

public class Utils {
  
  public static String formatDate(String format, Date date) {
    Format formatter = new SimpleDateFormat(format);
    return formatter.format(date);
  }
}