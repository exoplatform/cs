====
    Copyright (C) 2009 eXo Platform SAS.
    
    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.
    
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.
    
    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.
====

==============================================
    Release Notes - exo-cs - Version 2.0.0 Beta01
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

*eXo Chat is a live web-based Instant Messaging system. It is a Jabber chat implemented over the open xmpp protocol. With it, you can send instant message, exchange files real-time, store, export your conversation's history and sort by date, week, month. Group conversation very useful for co-working and group meeting online. eXo Chat comes in 2 flavours, the Chat application an webOS friendly windowed application and Chat Bar, an ubiquitous tiny chat bar that you can put on classic pages so that portal users can chat from wherever they are.


=============
 What's new?
=============


    * General
          o Upgraded to GateIn 3.0 RC1 and dependencies http://www.jboss.org/gatein/
          
          
         
          
    * Find the latest release notes here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Release+Notes            
          
=========
 INSTALL
=========

Find the latest install guide here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Install+Guide

- System Requirements
        Web Browser: IE6, IE7, FF2, FF3 (recommended), Safari.
        JVM: version 1.6.0_0 or higher
        Application Server : tomcat-6.0 and up 
        Building Tools: Maven 2.2.1 and up
        openfire server version 3.6.4 for more information visit here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Chat+Configuration

- Collaboration suite quick start guide
  Collaboration suite have 2 servers need to run at same time to use:
    +) tomcat: this is main tomcat server include Collaboration web applications and all dependencies.     
    +) exo-openfire: a Jabber server used for Chat applications

Need to set the JAVA_HOME variable for run Collaboration suite's servers.
+) How to start Collaboration sute:
   * First thing first you need to give all script files the executable permission if you are in unix family environment.
   Use command: "chmod +x *.sh" (without quote) to have execute permission on these files.
   
   * NOTE for cygwin's user: the JAVA_HOME must be in MS Windows format like: "C:\Program Files\JDK 1.5"
    Example use: export JAVA_HOME=`cygpath -w "$JAVA_HOME"`; to convert unix like format to MS Windows format.
   
   
   
   * Start tomcat server
   
     +) On the Windows platform
       Open a DOS prompt command, go to tomcat/bin and type the command:
        "gatein.bat run" for production
        "gatein-dev.bat run" for development 


     +) On Unix/Linux/cygwin
       Open a terminal, go to tomcat/bin and type the command:
         "./gatein.sh run" for production
         "./gatein-dev.sh run" for development
    
   * Start exo-openfire server:
     +) On the Windows platform
       Open a DOS prompt command, go to exo-openfire/bin and type the command:
         run.bat

     +) On Unix/Linux
       Open a terminal, go to exo-openfire/bin and type the command:
         "./openfire start" to start
         "./openfire stop" to shutdown 

-) How to access the eXo Collaboration Suite

* Enter one of the following addresses into your browser address bar:
   Classic :
      http://localhost:8080/portal
      http://localhost:8080/portal/public/classic
   CS demo portal  
      http://localhost:8080/csdemo

You can log into the portal with the following accounts: root, john, marry, demo.
All those accounts have the default password "gtn".

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
     Community gatein    http://www.jboss.org/gatein/ 
     Developers wiki     http://wiki.exoplatform.org


===========
 CHANGELOG
===========
- 2.0.0 Beta01

- 2.0.0 Alpha02


** Bug
    * [CS-3539] - lost last message when show message from "today" in special case
    * [CS-3606] - Chat room loss when refreshing the browser page or even changing menu
    * [CS-3708] - Some mails do not display message's content
    * [CS-3720] - ContinuationService getUserToken NPE exception when run both csdemo and extension
    * [CS-3768] - Can not save event/task in special case 
    * [CS-3769] - Can not send remind email for more than 1 user
    * [CS-3775] - Unknown error when send an email with attachment
    * [CS-3776] - Do not show event/task of imported calendar
    * [CS-3782] - csdemo and rest-csdemo webapps are not deployed
    * [CS-3785] - Can not move message when it is not original message
    * [CS-3789] - After reading 1 imported email, the number unread email of folder is not discounted
    * [CS-3821] - Can not get mail from pop3
    * [CS-3837] - Set align for attributes of UserProfile  
    * [CS-3838] - Show popup message when create or delete account
    * [CS-3877] - NPE on PopupReminder at startup
    * [CS-3907] - Impossible to send event invitation
    * [CS-3913] - Cannot make Chat work
    * [CS-3920] - JBoss patch doesn't contain the specific server.xml with SSO



** Improvement
    * [CS-3333] - Share a temporary selection of contacts
    * [CS-3528] - Perform XMPP authentication based on current user credentials
    * [CS-3544] - Better support for move messages in IMAP
    * [CS-3771] - remove wasted space
    * [CS-3825] - Put a cleaner message in Chat application when the chat server is not available / remove stack trace
    * [CS-3921] - Auto-complete should provide easy way to slect email when user has multipe email

** New Feature
    * [CS-382] - Autocomplete in To: CC: and BCC:
    * [CS-3693] - public contacts attributes based on portal's user profile

** Task
    * [CS-1138] - Refactoring : JCRDataStorage does not implement DataStorage
    * [CS-2429] - add xsd to the xml configurations
    * [CS-3422] - Migrate webservices to WS 2
    * [CS-3427] - Upgrade openfre dependency
    * [CS-3770] - Enable csdemo portal
    * [CS-3794] - Find a way to configure 2 openfire
    * [CS-3795] - Cleanup dependencies
    * [CS-3796] - Upgrade to gatein beta5
    * [CS-3858] - Chat service don't support multi-portal mode
    * [CS-3866] - Edit the background in [Delete account] form
    * [CS-3867] - Edit the background in [Attach] form while creating event or task

 

- 2.0.0 Alpha01


** Bug
    * [CS-3473] - after report messages are spam, can't do these messages are not spam 
    * [CS-3560] - should disable chat room when  user offline 
    * [CS-3672] - Don't show warning message when user create new room without permission 
    * [CS-3674] - Calendar porlet loss action bar button when run on AIL project
    * [CS-3676] - WebOS, IE7, Chat: Can not send file 
    * [CS-3686] - AddressBook Picker gets all addresses
    * [CS-3688] - All-in-one :Mail:  Message pane: invalid display
    * [CS-3719] - java script loading order error
    * [CS-3749] - Event/Task is automatic returned to the created date/time after drag and drop
    * [CS-3750] - Unknown error when generate RSS
    * [CS-3753] - Error in Mail pop-up

** Improvement
    * [CS-3328] - Make categories translatable
    * [CS-3528] - Chat Login : Get password from DB or LDAP

** Task
    * [CS-1991] - Remove hard coded references to /portal from web\csportal\src\main\webapp\templates\home.gtmpl
    * [CS-1992] - Remove hard coded references to /portal from web\csportal\src\main\webapp\templates\sidebar.gtmpl
    * [CS-2271] - Deprecate shared UI components
    * [CS-2272] - Remove deprecated components
    * [CS-2389] - Guard permission management in service
    * [CS-2616] - Check compatible when kernel clean up configuration.xml
    * [CS-2649] - Update Validator class from Portal to replace static Utils check function
    * [CS-3173] - Cometd study and update for mail service
    * [CS-3635] - Create source code subtree
    * [CS-3636] - Split csportal between extension and demo
    * [CS-3666] - Remove <testFailureIgnore>false</testFailureIgnore> from parent pom of CS projects
    * [CS-3717] - upgrade to Gatein beta4
    * [CS-3718] - Incorrect inheritance in pkg/pom.xml


** Sub-task
    * [CS-2454] - [DEV] occur some fails when check an email with big attachment
    * [CS-3382] - DEV:  if add star for message can't resize Detail message pane when view in Vertical layout
    * [CS-3456] - DEV: IMAP folders containing '/' not handled properly
    * [CS-3752] - [DEV] Chat: failure to send file 
    * [CS-3760] - [DEV] should disable chat room when user offline
    * [CS-3762] - [DEV]  Don't show warning message when user create new room without permission 




