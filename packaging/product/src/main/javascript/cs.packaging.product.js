eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getProduct(version) {

  var product = new Product();
  product.name = "eXoCS" ;
  product.portalwar = "portal.war" ;
  product.codeRepo = "cs" ;
  product.version = "${project.version}" ;
  product.serverPluginVersion = "${org.exoplatform.portal.version}" ;

  var kernel = Module.GetModule("kernel") ;
  var core = Module.GetModule("core") ;
  var ws = Module.GetModule("ws", {kernel : kernel, core : core});
  var eXoPortletContainer = Module.GetModule("portletcontainer", {kernel : kernel, core : core}) ;    
  var eXoJcr = Module.GetModule("jcr", {kernel : kernel, core : core, ws : ws}) ;
  var portal = Module.GetModule("portal", {kernel : kernel, ws:ws, core : core, eXoPortletContainer : eXoPortletContainer, eXoJcr : eXoJcr}); 
  var webos = Module.GetModule("webos", {kernel : kernel, core : core, eXoPortletContainer : eXoPortletContainer, eXoJcr : eXoJcr });
  var cs = Module.GetModule("cs", {kernel : kernel, ws : ws, core : core, eXoPortletContainer : eXoPortletContainer, eXoJcr : eXoJcr, portal : portal});

  product.addDependencies(cs.eXoApplication.mail) ;
  product.addDependencies(cs.eXoApplication.calendar) ;
  product.addDependencies(cs.eXoApplication.contact) ;
  product.addDependencies(cs.eXoApplication.content) ;

  product.addDependencies(cs.eXoApplication.chat) ;
  product.addDependencies(cs.eXoApplication.chatbar) ;
  
  product.addDependencies(cs.web.webservice) ;
  product.addDependencies(cs.web.csResources) ;
  product.addDependencies(cs.web.csportal) ;

  product.addDependencies(webos.web.webosResources);	
  
  product.addServerPatch("tomcat", cs.server.tomcat.patch) ;
  product.addServerPatch("jboss",  cs.server.jboss.patch) ;
  product.addServerPatch("jbossear",  portal.server.jbossear.patch) ;
  product.addServerPatch("jonas",  portal.server.jonas.patch) ;
  product.addServerPatch("ear",  portal.server.websphere.patch) 
 
  

  product.module = cs ;
  product.dependencyModule = [kernel, core, eXoPortletContainer, ws, eXoJcr, portal ];
 
  product.preDeploy = function() {
	  eXo.System.info("INFO", "Product Pre Deploy phase in cs");
	  this.removeDependency(new Project("javax.mail", "mail", "jar", "1.4"));
	  
  };
  return product ;
}
