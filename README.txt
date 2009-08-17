- Collaboration suite quick start guide
  Collaboration suite have 2 servers need to run at same time to use:
    +) exo-tomcat: this is main tomcat server include Collaboration web applications and all dependencies.     
    +) exo-openfire: a XMPP server using for Chat application

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
    
   * Start exo-openfire server:
     +) On the Windows platform
       Open a DOS prompt command, go to exo-openfire/bin and type the command:
         openfired.exe

     +) On Unix/Linux
       Open a terminal, go to exo-openfire/bin and type the command:
         ./openfire start

-) How to access the eXo Collaboration Suite

* Enter one of the following addresses into your browser address bar:
   
    http://localhost:8080/portal
    http://localhost:8080/portal/public/classic
    http://localhost:8080/portal/private/classic/collaboration

You can log into the portal with the following accounts: root, john, marry, demo.
All those accounts have the default password "exo".

* Direct link to access applications in Collaboration suite:
    +) Chat application: http://localhost:8080/portal/private/classic/chat
    +) Chat application: http://localhost:8080/portal/private/classic/calendar     
    +) Chat application: http://localhost:8080/portal/private/classic/mail     
    +) Chat application: http://localhost:8080/portal/private/classic/contact         
  You will get login form if you are not yet logged in to Collaboration Suite.


- Other resources and links
     Company site        http://www.exoplatform.com
     Community JIRA      http://jira.exoplatform.org
     Community site      http://www.exoplatform.org
     Developers wiki     http://wiki.exoplatform.org
