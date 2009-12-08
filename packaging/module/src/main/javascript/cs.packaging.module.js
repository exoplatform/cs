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


      
  // CS demo 
   module.demo = {};
   // demo portal
   module.demo.portal = 
	   new Project("org.exoplatform.cs", "exo.cs.demo.webapp", "war", module.version).
	   addDependency(new Project("org.exoplatform.ks", "exo.cs.demo.config", "jar", module.version));
	   module.demo.portal.deployName = "csdemo";  
	   
   // demo rest endpoint	   
   module.demo.rest = 
       new Project("org.exoplatform.cs", "exo.cs.demo.rest-war", "war", module.version);
       module.extension.deployName = "rest-csdemo"; 
       
       
   
   return module;
}
