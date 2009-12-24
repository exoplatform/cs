

Upgrade tool for SPFF version 02/10 to version 2.2

This tool migrates data and structures for applications : 

- Calendar (1.1Beta1 -> 1.1Beta3-1) 
- Forum (1.0RC3 -> 1.1RC4-1)



Migration procedure

1) Prepare .sar

1.1) Preparation on prod server before deploying the new version

- replace the  -nodetypes.xml in service jar for forum and calendar
- deploy these jars in replacement to those on server (make a backup copy of the old ones)

- Adjust paths in phase1-configuration.xml 
- Copy it into 02Portal.war/WEB-INF/conf/

- Edit 02Portal.war/WEB-INF/conf/configuration.xml
and add   <import>war:/conf/phase1-configuration.xml</import>
just after   <import>war:/conf/jcr/jcr-configuration.xml</import>  

- Copy spff.migration.tool.jar into exoplatform.sar

1.2) preparation of exoplatform.sar 2.2

- Adjust paths in phase2-configuration.xml 
- Copy it into 02Portal.war/WEB-INF/conf/

- Edit 02Portal.war/WEB-INF/conf/configuration.xml
and add   <import>war:/conf/phase2-configuration.xml</import>
just after  <import>war:/conf/jcr/jcr-configuration.xml</import>  

- Copy spff.migration.tool.jar into exoplatform.sar

2) Phase 1 : export and nodetypes update

- Stop and restart server
- Comment import of phase1-configuration.xml in configuration.xml
- Restart server : 'relaxed' nodetypes are registered


3) Phase 2 : import and migration of data, official nodetypes restoration

- Stop server
- Deploy exoplatform.sar 2.2 prepared in 1.2 : data is corrected
- Stop server
- Comment import of phase2-configuration.xml in configuration.xml
- Restart server : official nodetypes are loaded


