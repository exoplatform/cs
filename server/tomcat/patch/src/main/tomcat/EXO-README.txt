==============================================
    Release Notes - exo-cs - Version 1.2.2
==============================================

===============
 Introduction
===============

** eXo Collaboration Suite provides a rich, Web 2.0 browser-based interface with comprehensive
messaging, shared calendars, address books,  indexing, archival and search capabilities.

*eXo Address Book manages private and public contacts in your organization. A contact
lets you keep precious information such as personal or professional address and other
reaching information such as telephone, fax, email or IM. We support VCard format
import/export so that you can consolidate your existing contacts into a single place.
Additionally, you can refer your contacts from other applications such as eXo Calendar, eXo Mail.

*eXo Calendar helps you to organize and order time in your life more easily and efficiently. It
also manages your shared and public agendas in your organization. eXo Calendar provides
iCal support that you can use to exchange event/task information with other applications.
Searching for events/tasks in calendars also very convenient with full text search and advanced search with many criteria.

*eXo Mail is a mail client that is built with a variety of features designed to make your e-mail
experience more productive. It offers several ways to to view and organize your mails in-box and conversation.


=============
 What's new?
=============
exo Collaboration Suite 1.2.2 features several noteworthy changes:

    * General
          o Upgraded to portal 2.5.3
          o Compatible with webos 1.5          
    * Calendar
          o Bugs fix
    * Mail
          o Bugs fix
    * Address Book
          o Bugs fix
     
          
Find the latest release notes here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Release+Notes            
          
=========
 INSTALL
=========

Find the latest install guide here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Install+Guide

- System Requirements
        Web Browser: IE6, IE7, FF2, FF3 (recommended), Safari.
        JVM: version 1.5.0_09 or higher
        Application Server : Tomcat, jboss
        Building Tools: Maven 2.0.6 and up        

- Collaboration suite quick start guide
  Collaboration suite have 2 servers need to run at same time to use:
    +) exo-tomcat: this is main tomcat server include Collaboration web applications and all dependencies.     
  

Need to set the JAVA_HOME variable for run Collaboration suite's servers.
+) How to start Collaboration sute:
   * First thing first you need to give all script files the executable permission if you are in unix family environment.
   Use command: "chmod +x *.sh" (without quote) to have execute permission on these files.
   
   * NOTE for cygwin's user: the JAVA_HOME must be in MS Windows format like: "C:\Program Files\JDK 1.5"
    Example use: export JAVA_HOME=`cygpath -w "$JAVA_HOME"`; to convert unix like format to MS Windows format.
   
   * Start all servers by one command for Unix/Linux/cygwin environment:
      Go to exo-tomcat/bin and run command:
      ./eXo.sh
   
   * Start exo-tomcat server:
   
     +) On the Windows platform
       Open a DOS prompt command, go to exo-tomcat/bin and type the command:
         eXo.bat run

     +) On Unix/Linux/cygwin
       Open a terminal, go to exo-tomcat/bin and type the command:
         ./eXo.sh run    
   
-) How to access the eXo Collaboration Suite

* Enter one of the following addresses into your browser address bar:
   Classic :
      http://localhost:8080/portal
      http://localhost:8080/portal/public/classic
    WebOS : 
      http://localhost:8080/portal/private/classic/collaboration

You can log into the portal with the following accounts: root, john, marry, demo.
All those accounts have the default password "exo".

* Direct link to access applications in Collaboration suite:
    +) Calendar application: http://localhost:8080/portal/private/classic/calendar     
    +) Mail application: http://localhost:8080/portal/private/classic/mail     
    +) Address Book application: http://localhost:8080/portal/private/classic/contact     
    
  You will get login form if you are not yet logged in to Collaboration Suite.


===========
 RESOURCES
===========

     Company site        http://www.exoplatform.com
     Community JIRA      http://jira.exoplatform.org
     Community site      http://www.exoplatform.org
     Developers wiki     http://wiki.exoplatform.org


===========
 CHANGELOG
===========
- 1.2.2
** Bug
    * [CS-1397] - multiple reminders at the same time
    * [CS-2505] - Typo on home page
    * [CS-2510] - RSS Portlet : Portlet does not manage accent correctly when the feed contains accent
    * [CS-2610] - MAC OS: Little error when show add new contact pop up in mail application
    * [CS-2621] - Error when run email reminder job and  popup reminder job       
    * [CS-2669] - Overloaded methods in UICalendarPortlet.js
    * [CS-2716] - Don't change label on menu of custom layout when change custom layout
    * [CS-2847] - WYSIWYG in compose dialog does not support i18n   
    * [CS-2867] - impossible to change time by mouse
    * [CS-2868] - UNknown error while exporting an email
    * [CS-2883] - UI error when open User workspace
    * [CS-2888] - ics import - duration is one day later
    * [CS-2890] - Show blank message when try to view Contact search result in Vcard
    * [CS-2893] - IE7-WeOS: Can not do anything after logout/login while checking mail
    * [CS-2894] - shared calendar is shown in many times in special case
    * [CS-2896] -  IE 7: while standing at search form : Can't open add/edit event/task form,Can't show menu when right click on calendar 
    * [CS-2910] - Don't show pop up reminder
    * [CS-2913] - UI error when try to some actions on email of account which was deleted in special case
    * [CS-2914] - IE7: Error in displaying Compose mail form when there are many email addresses
    * [CS-2915] - Replace Save button in Password required popup by Cancel
    * [CS-2916] - wrong default date time of event when create on month view
    * [CS-2917] - Error in showing email address of receivers when "Reply to all" from Sent folder (auto put to cc field)
    * [CS-2918] - Always auto change priority of draft mail to "Normal" when edit a saved draft with high or low priority
    * [CS-2923] - Don't show event when use Calendar Sunbird programmer to subscribe calendar with Caldav
    * [CS-2930] - Can't drag and drop mail 
    * [CS-2986] - Show wrong event when add event from 13/07/2009 00:00 to 14/07/2009 23:59
    * [CS-2998] - Field label is missing in Generate RSS popup
    * [CS-3001] - Error in showing email address of receivers when review Sent folder
    * [CS-3025] - has an unexpected icon at Add/edit new contact form when add sender to contacts
    * [CS-3075] - Choose hours does not work when creating/editing event/task
    * [CS-3082] - unknown error when importing an address book after trying to upload a file larger than sizelimit
    * [CS-3097] - JCR session leak in AuthenticationLogoutListener
    * [CS-3396] - can't not view  message detail
    * [CS-3399] - FF :Can't import contact in special case
    * [CS-3403] - UI at Compose form 

** Improvement
    * [CS-2566] - Make AddressBook application vertically elastic
    * [CS-2895] - use buttons in message view   

** Task
    * [CS-2615] - Resource Bundles and FR translation review
    * [CS-2891] - Remove duplicate Cometd.js on cs, use only one form portal
    * [CS-2892] - fix calendar porlet id to avoid duplicate id on hole products
 
- 1.2.1
** Bug
    * [CS-2477] - Safari: Wrong UI of Manage account Icon
    * [CS-2479] - Priority Icon and Outside Icon overlay together
    * [CS-2480] - Safari: UI error at Export contacts form
    * [CS-2531] - In RSS content plug-in, char-set used to read xml rss is always utf-8   
    * [CS-2667] - Error with page iterator in  some forms of CS

** Task
    * [CS-2426] - Upgrade to portal 2.5.3
    * [CS-2624] - Check new parent pom 1.1.1 to be used in trunks and new releases
    * [CS-2642] - Back port ui bugs when update portal 2.5.3 from 1.3 to 1.2.x
    * [CS-2697] - rename source folder to resource folder for deploy context
- 1.2
** Bug
    * [CS-2334] - Small typo in home page (at least on cd demo site)
    * [CS-2393] - Message list is not updated in special case
    * [CS-2399] - after sending a message , can't know who is at BCC field
    * [CS-2403] - lost the path of uploaded file when click on Import to new calendar Icon or  import to the existing calendar Icon at the Import calendar form
    * [CS-2407] - UI error when have many address mail in participants to invite field  when view invitation mail
    * [CS-2487] - event time: rework the minute treatment
    * [CS-2489] - Event Schedule: Status of participants is always empty
    * [CS-2493] - Unknow error when execute function when standing at last page (page number > 1)
    * [CS-2499] - Can't delete calendar which belong to calendar group in case the calendar has event(s)

** Doc
    * [CS-2400] - Apply wiki homepage guidelines
    * [CS-2406] - Add wiki about the org.exoplatform.mail.service.AuthenticationLogoutListener plugin
    * [CS-2423] - Integration HOW TO
    * [CS-2424] - Install Guide


** Improvement
    * [CS-293] - Do not display image in Preview an Event/Task when click ''Download'' button
    * [CS-521] - Warning on empty subject email
    * [CS-784] - Conversation column in List View
    * [CS-1267] - Show All addressbooks option
    * [CS-1385] - Calendar is checked again after reload browser although unchecked 
    * [CS-1421] - Tooltips on minicalendar
 
** Task
    
    * [CS-2405] - Remove duplicate class SelectOptionGroup, SelectItem,SelectOption (on Contact porlet) and use it form portal
    * [CS-2418] - Backport menu from 1.1
    * [CS-2442] - Change bundle keys to be compliant with xml format


