This is new branches of cs version 1.0.x, we use to fix all bug of released version 1.0 and 1.0.1.
1) We use this for depenencies :

<properties>
        <org.exoplatform.kernel.version>2.0.1</org.exoplatform.kernel.version>
        <org.exoplatform.core.version>2.0.2</org.exoplatform.core.version>
	    <org.exoplatform.ws.version>1.1.2</org.exoplatform.ws.version>
        <org.exoplatform.jcr.version>1.8.3</org.exoplatform.jcr.version>
        <org.exoplatform.pc.version>2.0</org.exoplatform.pc.version>
        <org.exoplatform.portal.version>2.1</org.exoplatform.portal.version>
        <org.exoplatform.cs.version>1.0.x-SNAPSHOT</org.exoplatform.cs.version>
        <test.classes>Test</test.classes>
        <test.skip>true</test.skip>
</properties>

2) We use the commands like these: 
 exobuild --product=cs --version=1.0.x-SNAPSHOT --build 
 exobuild --product=cs --version=1.0.x-SNAPSHOT --deploy
 
 to build and deploy this version.
 
3) Make sure you have tool up to date to build successful.





