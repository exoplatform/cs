eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getModule(params) {

  var kernel = params.kernel;
  var core = params.core;
  var eXoPortletContainer = params.eXoPortletContainer;
  var jcr = params.eXoJcr;
  var portal = params.portal;

  var module = new Module();

  module.version =  "trunk" ;
  module.relativeMavenRepo =  "org/exoplatform/cs" ;
  module.relativeSRCRepo =  "cs/trunk" ;
  module.name = "cs" ;
    
  module.eXoApplication = {};
  module.eXoApplication.mail = 
    new Project("org.exoplatform.cs", "exo.cs.eXoApplication.mail.webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.mail.service", "jar",  module.version));
  module.eXoApplication.mail.deployName = "mail";
    
  module.eXoApplication.forum = 
    new Project("org.exoplatform.cs", "exo.cs.eXoApplication.forum.webapp", "war", module.version).
      addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.forum.service", "jar",  module.version));
  module.eXoApplication.forum.deployName = "forum";
    
  module.eXoApplication.calendar = 
    new Project("org.exoplatform.cs", "exo.cs.eXoApplication.calendar.webapp", "war", module.version).
      addDependency(new Project("org.exoplatform.cs", "exo.cs.eXoApplication.calendar.service", "jar",  module.version)).
	  addDependency(new Project("rome", "rome", "jar", "0.8")).
      addDependency(new Project("ical4j", "ical4j", "jar", "0.9.20")) ;
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
    
  module.web = {}
  module.web.csportal = 
    new Project("org.exoplatform.cs", "exo.cs.web.portal", "exo-portal", module.version).
      addDependency(portal.web.eXoResources) .
      addDependency(portal.web.eXoMacSkin) .
      addDependency(portal.web.eXoVistaSkin) .
			addDependency(new Project("org.exoplatform.portal", "exo.portal.component.jcrext", "jar", module.version)).
      addDependency(portal.webui.portal) .
      addDependency(jcr.frameworks.command) .
      addDependency(jcr.frameworks.web) ;
      
  return module;
}
