==============================================
    Release Notes - exo-cs - Version 2.0.0 Alpha01
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
exo Collaboration Suite 2.0.0 Alpha01

    * General
          o Upgraded to GateIn 3.0 Beta4, http://www.jboss.org/gatein/
          o No WebOS in this version  
         
          
Find the latest release notes here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Release+Notes            
          
=========
 INSTALL
=========

Find the latest install guide here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Install+Guide

- System Requirements
        Web Browser: IE6, IE7, FF2, FF3 (recommended), Safari.
        JVM: version 1.6.0_0 or higher
        Application Server : tomcat-6.0.16 
        Building Tools: Maven 2.2.1 and up
        openfire server version 3.4.5 for more information visit here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Chat+Configuration

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
   
   * Start all servers by one command for Unix/Linux/cygwin environment:
      Go to tomcat/bin and run command:
      ./eXo.sh
   
   * Start tomcat server:
   
     +) On the Windows platform
       Open a DOS prompt command, go to tomcat/bin and type the command:
         eXo.bat run

     +) On Unix/Linux/cygwin
       Open a terminal, go to tomcat/bin and type the command:
         ./eXo.sh run
    
   * Start exo-openfire server:
     +) On the Windows platform
       Open a DOS prompt command, go to exo-openfire/bin and type the command:
         openfired.exe

     +) On Unix/Linux
       Open a terminal, go to exo-openfire/bin and type the command:
         ./openfire start

-) How to access the eXo Collaboration Suite

* Enter one of the following addresses into your browser address bar:
   Classic :
      http://localhost:8080/portal
      http://localhost:8080/portal/public/classic
    

You can log into the portal with the following accounts: root, john, marry, demo.
All those accounts have the default password "".

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

** New Feature
    * [CS-3693] - Initialize contacts based on attributes of UserProfile

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




