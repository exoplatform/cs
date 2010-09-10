/**
 * Copyright (C) 2003-20010 eXo Platform SAS.
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
 **/

========================================== 
    Release Notes - eXo Collaboration - Version 2.1.0
==========================================
===============
1 Introduction
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
2 What's new?
=============


    * General
     - Integrated with ECMs 
     + Select file from DMS
     + Save file to DMS
     - Integrated with Social
     + Automatic initialize data when Space add application in calendar, contact
     + Update activity in Space and customize activity template  
     - Quick application link on chatbar
     - Upcomming events gadget
     - My task gadget
     - Implemented timezone behavior 
                  
    * Find the latest release notes here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Release+Notes            
          
=========
3 INSTALL
=========

Find the latest install guide here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Install+Guide

- System Requirements
        Web Browser: IE6, IE7, FF2, FF3 (recommended), Safari.
        JVM: version 1.6 or higher
        Application Server : jboss-5.1.0.GA
        Building Tools: Maven 2.2.1 and up
        openfire server version 3.6.4 for more information visit here : http://wiki.exoplatform.org/xwiki/bin/view/CS/Chat+Configuration

- Collaboration suite quick start guide
  Collaboration suite have 2 servers need to run at same time to use:
    +) jobs: this is main jobss server include Collaboration web applications and all dependencies.     
    +) eXo-chatserver: a Jabber server used for Chat applications

Need to set the JAVA_HOME variable for run Collaboration suite's servers.
+) How to start Collaboration sute:
   * First thing first you need to give all script files the executable permission if you are in unix family environment.
   Use command: "chmod +x *.sh" (without quote) to have execute permission on these files.
   
   * NOTE for cygwin's user: the JAVA_HOME must be in MS Windows format like: "C:\Program Files\JDK 1.5"
    Example use: export JAVA_HOME=`cygpath -w "$JAVA_HOME"`; to convert unix like format to MS Windows format.
    
   
   * Start jboss server:
   
     +) On the Windows platform
       Open a DOS prompt command, go to jboss/bin and type the command:
         run.bat start

     +) On Unix/Linux/cygwin
       Open a terminal, go to jboss/bin and type the command:
         ./run.sh
  
   * Start eXo-chatserver:
     +) On the Windows platform
       Open a DOS prompt command, go to exo-chatserver/bin and type the command:
         run.bat for using chat in portal
         run_demo.bat for using chat in csdemo

     +) On Unix/Linux
       Open a terminal, go to eXo-chatserver/bin and type the command:
         ./run.sh for using chat in portal
         ./run_demo.sh for using chat in csdemo
         
   * If on provided chat-server have no run*.* file you can reference here http://wiki.exoplatform.org/xwiki/bin/view/CS/Install%20Guide#HLaunchthechatserverforcs2.0
         
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

==============
4 KNOWN ISSUES
==============
 - None
 
===========
5 RESOURCES
===========

     Company site        http://www.exoplatform.com
     Community JIRA      http://jira.exoplatform.org
     Community site      http://www.exoplatform.org
     Community gatein    http://www.jboss.org/gatein/ 
     Developers wiki     http://wiki.exoplatform.org


===========
6 CHANGELOG
===========
- 2.1.0 GA

** Bug
    * [CS-1519] - Error when sort sender mail
    * [CS-3525] - [SAFARI] Fetching bar is always shown with  the same content : Start...stop
    * [CS-3710] - Bad input for folder name is silently ignored
    * [CS-3711] - Exception when add 1 tag (include special character) for an email
    * [CS-3944] - Exo mail could not connect to the exchange server 
    * [CS-4109] - Js message error after add application to a page
    * [CS-4162] - Mail content is not valid in sepcial case
    * [CS-4266] -  Synchronize folder is invalid in case edit folder in Gmail
    * [CS-4360] - UI broken when select user for [Task delegation]
    * [CS-4373] - Problem of refresh and unread message displaying
    * [CS-4390] - Chat portlet does not work on portal classic 
    * [CS-4394] - Chat - Unread message on chat tab is missed when refresh browser
    * [CS-4400] - Copy/Patse: There are no contacs message display invalidly
    * [CS-4402] - Message counld not be sent
    * [CS-4410] - Can not do some action on the public calendar
    * [CS-4418] - [Canlendar] User do not have edit permission still drag and drop event
    * [CS-4436] - Can not run cs with csdemo 
    * [CS-4442] - initialize application data wrong when adding application to Social Space.
    * [CS-4443] - chatbar user status is always "free to chat" event if user did not yet login 
    * [CS-4450] - Tasks Gadget bug with max height
    * [CS-4451] - Can not load fck editor 
    * [CS-4456] - User auto offline after refresh or login
    * [CS-4461] - SEVERE: Can not record Activity for space when contact updated 
    * [CS-4463] - [eXoMail] Write new mail UI is broken
    * [CS-4465] - event is not shown full when spread in 2 lines in month view.
    * [CS-4474] - Duplicate calendars for space groups
    * [CS-4479] - Can't select file which is create in Site Explorer from server to attach in mail when compose new mail
    * [CS-4480] - Should use default group calendar for space calendar 
    * [CS-4506] - [Chat] Char bar does not work
    * [CS-4516] - Chat works only for predefined users
    * [CS-4517] - [Calendar] Add Event form - calendar list is shown invalid

** Documentation
    * [CS-4503] - Reference Guide

** Improvement
    * [CS-4376] - Attach from DMS
    * [CS-4454] - MyTasks gadget : Hover on the fll title bar
    * [CS-4457] - Update Icon for attach file from DMS
    * [CS-4475] - preselect space calendar when add event/task
    * [CS-4476] - Do not display left panes of Calendar in spaces
    * [CS-4477] - More info about events in space activity stream
    * [CS-4499] - Social Integration : leverage activity plugin

** New Feature
    * [CS-1325] - Implement Timezone behaviour
    * [CS-2450] - Tasks Gadget
    * [CS-3338] - Save to DMS
    * [CS-3643] - Default presence status
    * [CS-3654] - Tags as personal lists
    * [CS-3990] - Share personal calendar to group
    * [CS-4338] - Access to Mail, Contacts and Calendar from ChatBar
    * [CS-4339] - Upcomming Events gadget
    * [CS-4388] - eXo Spaces integration for Calendar
    * [CS-4389] - eXo Spaces integration for AddressBook

** Task
    * [CS-4301] - merge CSCometd.js with Cometd.js from platform
    * [CS-4303] - duplicate  properties file in resource bundle  _en  and .properties
    * [CS-4371] - move all components declared in jars in extension
    * [CS-4377] - Upgrade to Gatein 3.1
    * [CS-4395] -  Add plug-in to initialize default data when space in social created
    * [CS-4427] - Update fisheye URL in pom.xml
    * [CS-4434] - update commons version 
    * [CS-4447] - Organize css following the introduction of gatein 3.1

- 2.1.0 CR03

** Bug
    * [CS-4462] - [eXoMail] Functional exception management is missing.
    * [CS-4483] - Warning Unable to coerce 'Event' into a LONG
    * [CS-4492] -  Chat Conservation form UI is broken in IE 7
    * [CS-4493] - User using IE7 to login can not send file to another user
    * [CS-4502] - remove "Powered By eXo Platform" in chat bar

** Improvement
    * [CS-4482] - Revamp UI composer from in mail app when the header button too long  

** New Feature
    * [CS-4338] - Access to Mail, Contacts and Calendar from ChatBar

** Task
    * [CS-4511] - Package web service in EAR extension
    * [CS-4512] - Remove extension config jar from the ear
    * [CS-4513] - Remove jboss-web.xml from extension war
    * [CS-4520] - Release CS 2.1.0-CR03


- 2.1.0 CR02

** Bug
    * [CS-4360] - UI broken when select user for [Task delegation]
    * [CS-4461] - SEVERE: Can not record Activity for space when contact updated 
    * [CS-4465] - event is not shown full when spread in 2 lines in month view.
    * [CS-4488] - openfire deployment uses the hardcoded value eXo.env.dependenciesDir + "/repository" for the location of the local maven repository
    
** Task
    * [CS-4371] - move all components declared in jars in extension
    * [CS-4447] - Organize css following the introduction of gatein 3.1
    * [CS-4505] - Release CS 2.1.0-CR02

- 2.1.0 CR01

** Bug
    * [CS-4410] - Can not do some action on the public calendar
    * [CS-4436] - Can not run cs with csdemo 
    * [CS-4442] - initialize application data wrong when adding application to Social Space.
    * [CS-4450] - Tasks Gadget bug with max height
    * [CS-4451] - Can not load fck editor 
    * [CS-4458] -  Scrolling the weeks doesn't change the central view
    * [CS-4463] - [eXoMail] Write new mail UI is broken
    * [CS-4486] - German translation instead of french for Calendar

** Improvement
    * [CS-4457] - Update Icon for attach file from DMS

** New Feature
    * [CS-2450] - Tasks Gadget

** Task
    * [CS-4395] -  Add plugin to initialize default data when space in social created
    * [CS-4469] - base structure for eXo Collaboration Reference Guide
    * [CS-4485] - Release CS 2.1.0-CR01

- 2.0.0 GA
** Bug
    * [CS-4043] - Attachment file is not shown when view messages in Sent folder
    * [CS-4244] - Calendar - Show error message when all event categories are deteted
    * [CS-4252] - Time field is not shown in some cases
    * [CS-4260] - Problem in mail without remember password
    * [CS-4264] - Don't update fetched message in Message pane while checking mail
    * [CS-4265] - Imap- Can't check mail any more after stopped check mail
    * [CS-4266] -  Synchronize folder is invalid in case edit folder in Gmail
    * [CS-4308] - Calendar - Error message is showed when all Event categories are deleted
    * [CS-4309] - [MAIL] - Add contact form does not close by click to close button 
    * [CS-4313] - Chat: Chat app is blocked with new created user
    * [CS-4315] -  [Contact] Unknown error when click one tag of share contact
    * [CS-4316] - [Contact] Can't Drag and drop contacts from a shared address book without edit right to personal address book
    * [CS-4327] - Can't Maximize reading pane
    * [CS-4330] - problem when select event category of shared events.
    * [CS-4332] - exception when starting server 
    * [CS-4333] - Cannot get mail by pop3 account
** Feedback
    * [CS-4218] - Contact auto-complete blocks input on slow server
** Task
    * [CS-4297] - ChangeLanguage link on csdemo is not nice
    * [CS-4314] - Do not delete events when delete event category contains these events.

    
- 2.0.0 CR01
** Bug
    * [CS-2975] - Calendar RSS feed does not generate links for more than one calendar
    * [CS-3559] - Chat bar can not be displayed in Vista & Mac Skin
    * [CS-3725] - Webmail: Date format error (wrong pattern used)
    * [CS-3729] - [DEV] Can not load FCKEditor when send an email 
    * [CS-3823] - Display name of created event when open form to create new event
    * [CS-4102] - Can not see default navigation on cs extension in tomcat with some pc
    * [CS-4165] - Calendar: parameter "first day of the week" not taken into account
    * [CS-4170] - Home page of eXo Mail is only in English
    * [CS-4180] - chat on jboss does not work
    * [CS-4181] - Error when print contact with extension
    * [CS-4211] - Error in displaying right-click menu (in Extension)
    * [CS-4225] -  CSdemo: Return no message when try to view sent messages by Today | This week | Last 30 days
    * [CS-4226] - Reschedule event with mouse on "Schedule" tab does not work
    * [CS-4241] - IE7: ICAL icon is not shown when Enable public access calendar
    * [CS-4247] - Event - There is no Confirmation Link in Invatation email
    * [CS-4252] - Time field is not shown in some cases
    * [CS-4253] - Check mail inexactly when limit time to check 
    * [CS-4259] - Exception when send mail without enable SMTP authentication
    * [CS-4263] - Alert message is not shown to inform user that invalid username/password
    * [CS-4264] - Don't update fetched message in Message pane while checking mail
    * [CS-4269] - Calendar: some typo in French
    * [CS-4270] - Chat - the user's id is displayed instead of the user name
    * [CS-4271] - Adressbook refreshes to empty state after mouse move
    * [CS-4280] - Auto apply server locale to UI event user not setting 
    * [CS-4291] - Only eXo Chat IM is displayed
    * [CS-4294] - Chat room loss when refreshing the browser page or even changing menu
    * [CS-4299] - Remove the use of exoservice gmail Account from MailService configuration

** Feedback
    * [CS-4164] - csdemo and cs-extention doesn't use the same workspace
    * [CS-4272] - demo homepage illustration is UGLY
    * [CS-4273] - quicksearch should run on hit ENTER

** Improvement
    * [CS-2762] - Limit size of .ics files generated for feeds
    * [CS-3003] - Allow Whitespace in chat room name
    * [CS-3276] - Safari 4 support
    * [CS-3333] - Share a temporary selection of contacts
    * [CS-3528] - Perform XMPP authentication based on current user credentials
    * [CS-3544] - Better support for move messages in IMAP
    * [CS-3682] - highlight links in chat room
    * [CS-3835] - More concise contact sheet
    * [CS-3882] - Do not display empty group of calendars
    * [CS-3885] - Calendar UI glitches
    * [CS-3887] - Mail ui glitches
    * [CS-3945] - Configurable exclusions for group adressbooks
    * [CS-4008] - Public feed have add more function 
    * [CS-4298] - Preserve Chat bar state when browsing

** New Feature
    * [CS-1316] - Send mail to group of users
    * [CS-2813] - Messages Sync
    * [CS-3655] - Select All in AddressBook
    * [CS-3693] - public contacts attributes based on portal's user profile
    * [CS-3908] - Autocomplete in To: CC: and BCC contact app: 
- 2.0.0 CR01

** Bug
    * [CS-3817] - "Time" field is not disabled when check "All day" while creating Event/Task
    * [CS-4045] - Mistakes in french internationalization in Chat popups
    * [CS-4060] - JBoss cannot start
    * [CS-4161] - vertical scrollbar
    * [CS-4170] - Home page of eXo Mail is only in English
    * [CS-4174] - Agenda: it lacks the French translations 
    * [CS-4175] - unknown error when create feed navigation on rss reader porlet
    * [CS-4178] -  Mails : Cannot add a folder 
    * [CS-4183] - Address book : missing french traduction in contacts
    * [CS-4193] - Calendar: the "date format" (jj/mm/aaaa) setting is only partially taken into account

** Improvement
    * [CS-3834] - Distinctive icon for shared contacts
    * [CS-3955] - Create user profiles lazily
   
** Task
    * [CS-3634] - Apply gatein migration prerequisites
    * [CS-4096] - move components declaration to src/main/resources

- 2.0.0 Beta02

** Bug
    * [CS-3561] - Missing upload icon in import calendar form
    * [CS-3706] - Can not quick add event after setting time is AM/PM
    * [CS-3732] - Drag and drop of a contact with no edit permission produces an unknown error
    * [CS-3784] - Can not open sub-folder has sub-folder
    * [CS-3833] - Unknown error when delete permission of share address book/contact
    * [CS-3851] - ChatBar does'nt work with Internet Explorer 7
    * [CS-3854] - Unknown error when delete permission of share calendar
    * [CS-3855] - Can not add multi users at the same time for shared calendar
    * [CS-3857] - [Unplanned] AddressBook Portlet is using OrganizationService instead of ContactService for Importing Groups
    * [CS-3865] - password prompted even after save
    * [CS-3868] - Can not send mail for user has multi email
    * [CS-3874] - Can not move some message at the same time
    * [CS-3924] - Always show message in Inforbar 
    * [CS-3975] - Can not add IM Contact when creating new contact
    * [CS-3976] - Can not send multi email at the same time
    * [CS-3977] - Exception when share contact for owner
    * [CS-3981] - Can not delete or move the sent messages in Sent folder to others
    * [CS-3982] - User is not prompted for password
    * [CS-3985] - Messages are disappeared after editing filter without changing
    * [CS-3991] - Unknown error when edit 1 event in List view
    * [CS-3994] - Unknown error when double click on event when see it in List view
    * [CS-3996] - Exception when importe 1 message at the second time
    * [CS-3999] - Unknown error when edit  share permission of contact
    * [CS-4014] - Contact's image is not show
    * [CS-4018] - Error in displaying buttons in Add Tag form
    * [CS-4021] - Can not get mail with csdemo
    * [CS-4026] - Hiding calendars with duplicate named does not work
    * [CS-4028] - Can not add group user to send remind when create an event in Mail application
    * [CS-4031] - [unplanned] Can not view image attachment  of event/task with csdemo
    * [CS-4037] - Can not get mail without select folder
    * [CS-4038] - Right click menu still display event when action is done
    * [CS-4039] - UI error with forms has 2 tabs or more (in csdemo)
    * [CS-4040] - Error message pop-up (with csdemo)
    * [CS-4060] - JBoss cannot start
    * [CS-4070] - The first check-box is not checked when open form to add tag, send mail...
    * [CS-4084] - Unknown error when add group has edit permission for group calendar
    * [CS-4089] - IE7: Error in upload form 
    * [CS-4095] - IE7: Show  error message when drag and drop event/task in Month view
    * [CS-4108] - The check-box is not unchecked 
    * [CS-4124] - Can not get mail using pop3
    * [CS-4127] - [Unplanned] French accentuated characters are corrupted
    * [CS-4133] - Exception when a user is deleted
    * [CS-4168] -  Problem in RSS Portlet Refresh





** Improvement
    * [CS-3276] - Safari 4 support
    * [CS-3883] - Improve RSS feeds content
    * [CS-3945] - Configurable exclusions for group adressbooks

** New Feature
    * [CS-842] - Revamp Feeds management
    * [CS-3655] - Select All in AddressBook
    * [CS-3888] - Public iCal URL for calendars
    * [CS-3889] - Multi-calendar RSS Feeds
    * [CS-3906] - revamp demo portal 

    
- 2.0.0 Beta01
  ** Bug
    * [CS-3772] - Do not display tag's name in [Tag Message] form
    * [CS-3783] - Little error when folder's name is long
    * [CS-3790] - Still show message's content when an email is removed from tag
    * [CS-3791] - Unknown error when select user to send reminder while creating 1 event in Mail application
    * [CS-3849] - Can not join un-public room when user put right password 
    * [CS-3907] - Impossible to send event invitation
    * [CS-3913] - Chat does not work :   "Keystore was tampered" 
    * [CS-3953] -  Fix for labels i18n
    * [CS-3989] - Users can chat to each other but can not send file when user cancel add contact invitation
    * [CS-4010] - Little error in Contact form when email address is long

** Documentation
    * [CS-3788] - Update documentation auto-complete in To: CC: and BCC:

** Improvement
    * [CS-3673] - Room of administrative messages should be assigned a name 
    * [CS-3682] - highlight links in chat room
    * [CS-3835] - More concise contact sheet
    * [CS-3882] - Do not display empty group of calendars
    * [CS-3937] - Chat server packaging improvements

** New Feature
    * [CS-1316] - Send mail to group of users
    * [CS-3888] - Public iCal URL for calendars
    * [CS-3908] - Auto-complete in To: CC: and BCC contact app

** Task
    * [CS-2731] - Optimize calendar queries
    * [CS-3700] - Replace text "Remove" by icon in forms to add/edit event or task
    * [CS-3797] - Release CS 2.0 - Alpha2
    * [CS-3884] - Using "HashSet<String>" instead of "HashMap<String, String>", if  values==keys
    * [CS-3917] - Rename eXoLiveroom.sh
    * [CS-3918] - "eXo undefined" when starting with gatein.sh
    * [CS-3927] - Remove scroll bar in [Change image] form
    * [CS-3948] - rename the folder inside eXoChatServer.2.0.0-XXX.zip
    * [CS-3951] - Update the build to deploy binary bundles on release
    * [CS-3952] - chat peristence in erroneous ppath
    * [CS-3959] - Upgrade to GateIn GA
  
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




