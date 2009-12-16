eXo.require("eXo.projects.Module");
eXo.require("eXo.projects.Product");

function getModule(params)
{


   var ws = params.ws;
   var portal = params.portal;
   var module = new Module();

   module.version = "${project.version}"; 
   module.relativeMavenRepo = "org/exoplatform/cs";
   module.relativeSRCRepo = "cs";
   module.name = "cs";
 
  	// COMET (required by CS)
  	// TODO, should be passed in params and have its own module .js definition 
  var cometVersion = "${org.exoplatform.comet.version}";
  module.comet = {};

    
  module.comet.cometd =
	new Project("org.exoplatform.comet", "exo-comet-webapp", "war", cometVersion).
    addDependency(new Project("org.mortbay.jetty", "cometd-bayeux", "jar", "6.1.11")).
	addDependency(new Project("org.mortbay.jetty", "jetty-util", "jar", "6.1.11")).
	addDependency(new Project("org.mortbay.jetty", "cometd-api", "jar", "0.9.20080221")).
	addDependency(new Project("org.exoplatform.comet", "exo-comet-service", "jar", cometVersion));  	
	module.comet.cometd.deployName = "cometd";
  // CS

  module.eXoApplication = {};
  module.eXoApplication.mail = 
    new Project("org.exoplatform.cs", "exo.cs.eXoApplication.mail.webapp", "war", module.version).
    addDependency(new Project("javax.mail", "mail", "jar", "1.4.1")).
    addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.mail.service", "jar",  module.version));
  module.eXoApplication.mail.deployName = "mail";
    
  module.eXoApplication.calendar = 
    new Project("org.exoplatform.cs", "exo.cs.eXoApplication.calendar.webapp", "war", module.version).
      addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.calendar.service", "jar",  module.version)).
	  //addDependency(new Project("org.exoplatform.ws", "exo.ws.frameworks.json", "jar", 1.3)).
	  //addDependency(ws.frameworks.cometd).
	  addDependency(new Project("rome", "rome", "jar", "0.8")).
	  addDependency(new Project("jdom", "jdom", "jar", "1.0")).
      addDependency(new Project("ical4j", "ical4j", "jar", "1.0-beta5")) ;
  module.eXoApplication.calendar.deployName = "calendar";
    
  module.eXoApplication.contact = 
    new Project("org.exoplatform.cs", "exo.cs.eXoApplication.contact.webapp", "war", module.version).
      addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.contact.service", "jar",  module.version)).
      addDependency(new Project("net.wimpi.pim", "jpim-0.1", "jar",  "1.0"));
  module.eXoApplication.contact.deployName = "contact";
  
  module.eXoApplication.content = 
    new Project("org.exoplatform.cs", "exo.cs.eXoApplication.content.webapp", "war", module.version).
      addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.content.service", "jar",  module.version));
  module.eXoApplication.content.deployName = "content";
  
   
  module.eXoApplication.chat = new Project("org.exoplatform.cs", "exo.cs.eXoApplication.chat.webapp", "war", module.version).
  	  addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.chat.service", "jar", module.version).
  	  addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.organization.service", "jar", module.version)).
  	  //addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.organization.webapp", "war", module.version)).
      addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.organization.client.openfire", "jar", module.version)).
  	  //addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.organization.webapp", "war", module.version)).
  	  addDependency(new Project("jabber.smack", "smack", "jar", "3.0.4")).
  	  addDependency(new Project("jabber.smack", "smackx", "jar", "3.0.4")).
  	  addDependency(new Project("org.jcrom", "jcrom", "jar", "1.2")).
      addDependency(new Project("commons-fileupload", "commons-fileupload", "jar", "1.0")).
	  addDependency(new Project("commons-io", "commons-io", "jar", "1.3")).
	  addDependency(new Project("org.slf4j", "slf4j-api", "jar", "1.5.6")).	  
	  addDependency(new Project("org.slf4j", "slf4j-jdk14", "jar", "1.5.6"))
		  	
  	);
  module.eXoApplication.chat.deployName = "chat";
  
  module.eXoApplication.chatbar = new Project("org.exoplatform.cs", "exo.cs.eXoApplication.chatbar.webapp", "war", module.version) ;
  module.eXoApplication.chatbar.deployName = "chatbar";
  
  module.web = {}
  module.web.webservice = 
    new Project("org.exoplatform.cs", "exo.cs.web.webservice", "jar",  module.version);
  module.web.csResources = 
    new Project("org.exoplatform.cs", "exo.cs.web.csResources", "war", module.version) ;
  module.extension = 
    new Project("org.exoplatform.cs", "exo.cs.extension.webapp", "war", module.version).
      addDependency(new Project("org.exoplatform.cs", "exo.cs.extension.config", "jar", module.version));
      module.extension.deployName = "cs";
  
  
  /**
   * Configure and add server path for chat, single sign-on
   */
  module.server = {}
  module.server.tomcat = {}
  module.server.tomcat.patch = 
    new Project("org.exoplatform.cs", "exo.cs.server.tomcat.patch", "jar", module.version);

  module.server.jboss = {}
  module.server.jboss.patch = 
	    new Project("org.exoplatform.cs", "exo.cs.server.jboss.patch", "jar", module.version);
      
  // CS demo 
   module.demo = {};
   // demo portal
   module.demo.portal = 
	   new Project("org.exoplatform.cs", "exo.cs.demo.webapp", "war", module.version).
	   addDependency(new Project("org.exoplatform.cs", "exo.cs.demo.config", "jar", module.version));
	   module.demo.portal.deployName = "csdemo";  
	   
   // demo rest endpoint	   
   module.demo.rest = 
       new Project("org.exoplatform.cs", "exo.cs.demo.rest-war", "war", module.version);
       module.extension.deployName = "rest-csdemo"; 
       
   /**
   * Configure and deploy Openfire
   */
  module.configure = function(tasks, deployServers, serverMap) {
  	if (deployServers != null) {
      var server = serverMap.get("tomcat");
      tasks.add(deployOpenfireServer(server, this));
    }
  };    
   
   return module;
}

/**
 * Configure and deploy Openfire for integrated chat on cs 
 */

function deployOpenfireServer(mainServer, module) {
	var deployServerTask = new TaskDescriptor("Release Dependency Task", eXo.env.dependenciesDir) ;
  var server = {};
  
  server.openfireJarPath = eXo.env.dependenciesDir + "/repository";  
  for (var i=0; i < module.eXoApplication.chat.dependencies.size(); i++) {
	  var tmpObj = module.eXoApplication.chat.dependencies.get(i).dependencies; 
	  for (var j=0; j<tmpObj.size(); j++) {
		  if (tmpObj.get(j).artifactId == "exo.cs.eXoApplication.organization.client.openfire") {
			  server.openfireJarPath += "/" + tmpObj.get(j).relativePath;
			  break;
		  }
	  }
  }
   
  server.cleanServer = "openfire-3.4.5";
  server.name = "exo-openfire";
  server.serverHome = eXo.env.workingDir + "/" + server.name;
  server.deployLibDir = server.serverHome + "/lib";
  server.openfireJar = "exo.cs.eXoApplication.organization.client.openfire-" + module.version + ".jar" ;
  deployServerTask.description = "Deploy " + server.name + " ";
	deployServerTask.execute = function() {
    eXo.System.info("DELETE", "Delete " + server.serverHome);
    eXo.core.IOUtil.remove(server.serverHome);
		eXo.System.info("COPY", "Copy a clean server " + server.name);
		eXo.core.IOUtil.cp(eXo.env.dependenciesDir + "/" + server.cleanServer, server.serverHome);
    eXo.System.info("Gets the configuration file -in a buffer - of openfire (openfire.xml) from the library jar file");
		//var configBuffer = eXo.core.IOUtil.getJarEntryContent(mainServer.deployLibDir+"/"+server.openfireJar, "openfire/openfire.xml") ;
		var configBuffer = eXo.core.IOUtil.getJarEntryContent(server.openfireJarPath, "openfire/openfire.xml") ;
		if (configBuffer == null) { eXo.System.info("ERROR", "Error retrieving config file from jar !"); return; }
		// writes the buffer into the configuration file (openfire/conf/openfire.xml)
		eXo.System.info("INFO", "Creating config file from buffer...");
		eXo.core.IOUtil.createFile(server.serverHome+"/conf/openfire.xml", configBuffer);
		// copies the exo openfire library to openfire server
		eXo.System.info("INFO", "Copying exo openfire library file...");
		eXo.core.IOUtil.cp(server.openfireJarPath, 
						           server.deployLibDir + "/" + server.openfireJar);
	}
	return deployServerTask ;
}