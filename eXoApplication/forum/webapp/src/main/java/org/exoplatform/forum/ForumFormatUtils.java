/***************************************************************************
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
 ***************************************************************************/
/**
 * 
 */

package org.exoplatform.forum;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Dec 21, 2007 5:35:54 PM 
 */

public class ForumFormatUtils {

	@SuppressWarnings("deprecation")
  public static String getFormatTime(String format, Date myDate) {
		 int time = myDate.getHours();
		 int minut = myDate.getMinutes() ;
		 String strMinut;
		 if(minut < 10) strMinut = "0" + minut ;
		 else strMinut = "" + minut ;
		 StringBuffer stringBuffer = new StringBuffer() ;
		 if(time < 10)stringBuffer.append("0");
		 if(format.equals("24h")){
			 stringBuffer.append(time).append(":").append(strMinut);
		 } else {
			 String str = "" ;
			 if(time < 12) str = "AM";
			 else {
				 str = "PM";
				 if(time > 12) time = time - 12 ;
			 }
			 stringBuffer.append(time).append(":").append(strMinut).append(" ").append(str);
		 }
		return stringBuffer.toString();
	}
	
	@SuppressWarnings("deprecation")
  public static String getFormatDate(String format, Date myDate) {
		/* D, DD, DDD, DDDD, M, MM, MMM, MMMM, yy, yyyy
		 * */
		String strCase = "" ;
		int day = myDate.getDay() ;
		switch (day) {
    case 0:
    	strCase = "Sunday" ;
	    break;
    case 1:
    	strCase = "Monday" ;
    	break;
    case 2:
    	strCase = "Tuesday" ;
    	break;
    case 3:
    	strCase = "Webnesday" ;
    	break;
    case 4:
    	strCase = "Thursday" ;
    	break;
    case 5:
    	strCase = "Friday" ;
    	break;
    case 6:
    	strCase = "Saturday" ;
    	break;
    default:
	    break;
    }
		String form = "temp" + format ;
		if(form.indexOf("DDDD") > 0) {
			Format formatter = new SimpleDateFormat(form.substring(form.indexOf("DDDD") + 5));
			return strCase + ", "  + formatter.format(myDate).replaceAll(",", ", ");
		} else if(form.indexOf("DDD") > 0) {
			Format formatter = new SimpleDateFormat(form.substring(form.indexOf("DDD") + 4));
			return strCase.replaceFirst("day", "") + ", " + formatter.format(myDate).replaceAll(",", ", ");
		} else {
			Format formatter = new SimpleDateFormat(format);
			return formatter.format(myDate);
		}
  }
	
	public static String getTimeZoneNumberInString(String string) {
		if(string != null && string.length() > 0) {
			StringBuffer stringBuffer = new StringBuffer();
			for(int i = 0; i <	string.length(); ++i) {
				char c = string.charAt(i) ; 
				if(c == ')') break ;
				if (Character.isDigit(c) || c == '-' || c == '+' || c == ':'){
					if(c == ':') c = '.';
					if(c == '3' && string.charAt(i-1) == ':') c = '5';
					stringBuffer.append(c);
				}
			}
			return stringBuffer.toString() ;
		}
		return null ;
	}
	
	public static String[] getStarNumber(double voteRating) throws Exception {
		int star = (int)voteRating ;
		String[] className = new String[6] ;
		float k = 0;
		for (int i = 0; i < 5; i++) {
			if(i < star) className[i] = "star" ;
			else if(i == star) {
				k = (float) (voteRating - i) ; 
				if(k < 0.25) className[i] = "notStar" ;
				if(k >= 0.25 && k < 0.75) className[i] = "halfStar" ;
				if(k >= 0.75) className[i] = "star" ;
			} else {
				className[i] = "notStar" ;
			}
			
			className[5] = ("" + voteRating) ;
			if(className[5].length() >= 3) className[5] = className[5].substring(0, 3) ;
			if(k == 0) className[5] = "" + star ; 
		}
		return className ;
	}
	
	public static String getStringCleanHtmlCode(String sms) {
		StringBuffer string = new StringBuffer();
		char c; boolean get = true ;
		for (int i = 0; i < sms.length(); i++) {
			c = sms.charAt(i);
			if(c == '<') get = false ;
			if(get) string.append(c);
			if(c == '>') get = true ;
		}
		return string.toString();
	}
	
	public static String[] splitForForum (String str) throws Exception {
		if(str != null && str.length() > 0) {
			if(str.contains(",")) return str.trim().split(",") ;
			else return str.trim().split(";") ;
		} else return new String[] {} ;
	}
	
	public static String unSplitForForum (String[] str) throws Exception {
		StringBuilder rtn = new StringBuilder();
		if(str.length > 0) {
			for (String temp : str) {
				rtn.append(temp).append(",") ; 
			}
		}
		return rtn.toString() ;
	}
	
	public static String convertLinkHTML(String s) {
		int i = 0, j , l;
		StringBuffer buffer ;String str ;
		boolean isWrite = true, Retu = false ;
		List<String> Link = new ArrayList<String>() ; 
		l = s.length() ;
		while (true) {
			j = i ; isWrite = true ;
			i = i + s.substring(i , l).indexOf("ttp://") ;
			if(i <= j || i >= l)break ;
			if(i > 1 && s.charAt(i-1) == '"') continue ;
			buffer = new StringBuffer() ;
			buffer.append("h");
			while (true) {
	     char c = s.charAt(i);
	     	if(c == '"') {isWrite = false ; break ;}
	      if(c == ' ' || c == '<') break ;
	      if(c == '?' || c == '#' || c == '&' || c == '\'') buffer.append("\\") ;
	      buffer.append(c);
	      i++ ;
	      if(i >= (l)) {Retu = true; break ;}
      }
			
			str = buffer.toString() ;
			for (String string : Link) {
	      if(string.equalsIgnoreCase(str)) { 
	      	isWrite = false ;
	      	break ;
	      }
      }
			if(isWrite)	Link.add(str) ;
			if(Retu) break ;
		}
		for (String link : Link) {
			link = link.trim() ;
			s = s.replaceAll(link, "<a target=\"_blank\" href=\"" + link + "\">" + link + "</a>") ;
    }
	  return s ;
  }
}
