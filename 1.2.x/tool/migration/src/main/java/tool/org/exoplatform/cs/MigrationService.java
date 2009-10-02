package tool.org.exoplatform.cs;

import org.exoplatform.container.component.ComponentPlugin;


public interface MigrationService {
	
	public void initService() throws Exception;
  
  public void addListenerPlugin(ComponentPlugin listener) throws Exception ;
	
}
