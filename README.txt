Actually, no branch exists for Collaboration Suite.
So if you want to use CS trunk for a 2.0 version of portal, ecm ..., you have to modify some thinks for update CS dependancies.

1) In cs/trunk/pom.xml :
Change component version in "profiles" part, for example :
<profiles>
    <profile>
      <id>default</id>  
      <activation>
        <property><name>default</name></property>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <org.exoplatform.kernel.version>2.0</org.exoplatform.kernel.version>
        <org.exoplatform.core.version>2.0</org.exoplatform.core.version>
        <org.exoplatform.jcr.version>1.8</org.exoplatform.jcr.version>
        <org.exoplatform.pc.version>trunk</org.exoplatform.pc.version>
        <org.exoplatform.portal.version>2.0</org.exoplatform.portal.version>
        <org.exoplatform.cs.version>trunk</org.exoplatform.cs.version>

        <test.classes>Test</test.classes>
        <test.skip>true</test.skip>

      </properties>
    </profile>
  </profiles>


2) in cs/trunk/web/csportal/pom.xml,
Change each time ".../portal/trunk/..." by ".../portal/branches/2.0/..." if you use portal 2.0 version.


