package org.exoplatform.calendar.service;


import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ReminderJob implements Job {

  final private static String CALENDAR_REMINDER = "reminders".intern() ;
  private static Log log_ = ExoLogger.getLogger("job.RecordsJob");

  private RepositoryService repositoryService_ ;
  private JCRRegistryService jcrRegistryService_ ;
  
  public void execute(JobExecutionContext context) throws JobExecutionException {
    //Session session = null ;
    try {
    	System.out.println("\n\n\n Start send mail >>>>>") ;
    	ExoContainer container = ExoContainerContext.getCurrentContainer();
    	jcrRegistryService_ = (JCRRegistryService)container.getComponentInstanceOfType(JCRRegistryService.class);
      repositoryService_ = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    	if(log_.isDebugEnabled())
        log_.debug("Calendar reminder service");
      java.util.Calendar fromCalendar = new GregorianCalendar() ;
      //fromCalendar.setTimeZone(arg0) ;
      JobDataMap jdatamap = context.getJobDetail().getJobDataMap() ;
      String timeZone = jdatamap.getString("timeZone") ;
      System.out.println("\n\n\n== Time Zone =====>" + timeZone) ;
      /*String srcWorkspace = jdatamap.getString("srcWorkspace") ;
      String srcPath = jdatamap.getString("srcPath") ;
      String actionName = jdatamap.getString("actionName") ;
      String executable = jdatamap.getString("executable") ;
      String repository = jdatamap.getString("repository") ;*/
      
      StringBuffer path = new StringBuffer("/jcr:root/exo:portal-registry/exo:services/CalendarService/reminders");
      path.append("//element(*,exo:reminder)") ;
      path.append("[@exo:fromDateTime > xs:dateTime('"+ISO8601.format(fromCalendar)+"')]") ;
      System.out.println("\n\n\n=======>" + path.toString()) ;
      //session = getJCRSession() ;
      Node calendarHome = getCalendarServiceHome() ;
      QueryManager queryManager = calendarHome.getSession().getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(path.toString(), Query.XPATH);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();
      System.out.println("\n\n\n >>>>>" + iter.getSize()) ;
      System.out.println("\n\n\n") ;
      
    } catch (Exception e) {
    	e.printStackTrace() ;
      /*if(session != null) {
        session.logout(); 
      } */     
    } 
    if(log_.isDebugEnabled())
      log_.debug("File plan job done");
  }
  
  /*private Node getReminderHome() throws Exception {
    Node calendarServiceHome  = getCalendarServiceHome() ;    
    if(calendarServiceHome.hasNode(CALENDAR_REMINDER)) return calendarServiceHome.getNode(CALENDAR_REMINDER) ;
    return null ;
  }*/
  
  private String getReminderPath(java.util.Calendar fromCalendar)throws Exception {
  	String year = String.valueOf(fromCalendar.get(java.util.Calendar.YEAR)) ;
  	String month = String.valueOf(fromCalendar.get(java.util.Calendar.MONTH) + 1) ;
  	String day = String.valueOf(fromCalendar.get(java.util.Calendar.DATE)) ;
  	StringBuffer path = new StringBuffer("/jcr:root") ;
  	path.append(getCalendarServiceHome().getPath()) ;
  	path.append("/").append(CALENDAR_REMINDER) ;
  	path.append("/").append(year).append("/").append(month).append("/").append(day) ;
  	return path.toString() ;  	
  }
  private Session getJCRSession() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ; 
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return sessionProvider.getSession(defaultWS, repositoryService_.getCurrentRepository()) ;    
  }
  
  
  private Node getCalendarServiceHome() throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("CalendarService") ;
    Session session = getJCRSession() ;
    jcrRegistryService_.createServiceRegistry(serviceRegistry, false) ;
    Node node = jcrRegistryService_.getServiceRegistryNode(session, serviceRegistry.getName()) ;
    session.logout() ;
    return node ;
  }
}
