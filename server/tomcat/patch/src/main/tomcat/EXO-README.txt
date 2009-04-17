FOR ALL THE TECHNICAL DOCUMENTATION :
http://docs.exoplatform.org


WARNING: eXo Enterprise Liveroom is still under development. The HSQLDB database is currently
used to persist information. You actually have to remove the \tmp\hsql (in Windows) or
/tmp/hsql (in Unix) directory when upgrading from one pioneer build to another. Indeed,
database schemas are not stabilized yet. You will no longer need to do this operation as
soon as we switch to Derby (very soon, hopefully :-) ).

-) Introduction
    eXo Enterprise Liveroom is project included applications: Chat, Whiteboard, Video Conference take advantage of advanced collaboration suite to user.
    These are main features completed release in this version:

    * Chat:
      - XMPP with high interactive with Cometd and REST technical give to user feel comfortable as like as using native application and many advantage features.
      - Authenticated between portal and XMPP server which allow all user registered with portal can login to XMPP server by default.
      - File exchange.
        + Room chat which allow user to manage room using room configuration dialog
          + Public/Private room with password protected.
          + Manage room's owners/admins.
          + Presence broadcast.
          + Manage invite.
        + Easy to use UI:
          + Conversation window with tabs.
          + Auto filter user name in add contact popup.
          + Auto save/restore UI state session.
          + Unread message marker and unread message counter.
          + Animation of minimized chat box to notify user about new messages is coming.
          + Large message with input box allow create multiple line using Shift/Ctrl + Enter.
      - Message history handle give to user function to see old messages.
      - Export history of old messages to plain text file.
    * Whiteboard and Video conference are using Flex technology to give to user advantage of team working and remote broadcasting video conference:
      - Whiteboard: Give to user advanced whiteboard to take advantage of team working.
      - Video conference: A remote broadcast video conference to give to user to access online webcam and broadcast it for online video conferencing.

-) System Requirements
   +) Web Browser: IE6, IE7, FF2, FF3, Safari. Best for Frirefox 2 and Firefox 3, with flash plugin version 9+, recommends: flash plugin version 10.
   +) JVM: version 1.5.0_xx only


-) Liveroom quickstart
  Liveroom have 3 servers need to run at same time to use full features of Liveroom:
    +) exo-tomcat: this is main tomcat server include Liveroom web applications and all dependencies.
    +) exo-red5: a streaming flash/flex server provide services for Whiteboard and Video Conference.
    +) exo-openfire: a XMPP server using for Chat application

 Need to set the JAVA_HOME variable for run Liveroom's servers.
 +) How to start Liveroom:
   * First thing first you need to give all script files the executable permission if you are in unix family environment.
   Use command: "chmod +x *.sh" (without qoute) to have execute permission on these files.
   
   * NOTE for cygwin's user: the JAVA_HOME must be in MS Windows format like: "C:\Program Files\JDK 1.5"
    Example use: export JAVA_HOME=`cygpath -w "$JAVA_HOME"`; to convert unix like format to MS Windows format.
   
   * Start all servers by one command for Unix/Linux/cygwin environment:
      Go to exo-tomcat/bin and run command:
      ./eXo-Liveroom.sh run
   
   * Start exo-tomcat server:
   
     +) On the Windows platform
       Open a DOS prompt command, go to exo-tomcat/bin and type the command:
         eXo.bat run

     +) On Unix/Linux/cygwin
       Open a terminal, go to exo-tomcat/bin and type the command:
         ./eXo.sh run
   * Start exo-red5 server:
     +) On the Windows platform
       Open a DOS prompt command, go to exo-red5 and type the command:
         red5.bat

     +) On Unix/Linux
       Open a terminal, go to exo-red5 and type the command:
         ./red5.sh
   * Start exo-openfire server:
     +) On the Windows platform
       Open a DOS prompt command, go to exo-openfire/bin and type the command:
         openfired.exe

     +) On Unix/Linux
       Open a terminal, go to exo-openfire/bin and type the command:
         ./openfire start

-) How to access the eXo Liveroom

 * Enter one of the following addresses into your browser address bar:
   
    http://localhost:8080/portal
    http://localhost:8080/portal/public/classic
    http://localhost:8080/portal/private/classic/liveroom

 You can log into the portal with the following accounts: root, john, marry, demo. 
 All those accounts have the default password "exo".
 
 * Direct link to access applications in Liveroom:
    +) Chat application: http://localhost:8080/portal/private/classic/liveroom/chat
    +) Whiteboard application: http://localhost:8080/portal/private/classic/liveroom/whiteboard
    +) Video Conference application: http://localhost:8080/portal/private/classic/liveroom/videoconf
  You will get login form if you are not yet logged in to Liveroom.

For more documentation and latest updated news, please visit our website www.exoplatform.com.
If you have questions, please send a mail to the list exoplatform@objectweb.org.

Thank your for using eXo Platform products !
The eXo Platform team.
