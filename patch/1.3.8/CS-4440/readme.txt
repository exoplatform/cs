Summary

    * Status: Mail: exception after a period
    * CCP Issue: N/A, Product Jira Issue: CS-4440.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Using mail a period of time, click a random action >> exception occurs.

The exception:

17:11:32,328 ERROR [portal:UIPortalApplication] Error during the processAction phase
javax.jcr.AccessDeniedException: Access denied /Users/root/ApplicationData for __anonim (get item by
path)
at org.exoplatform.services.jcr.impl.core.SessionDataManager.getItem(SessionDataManager.java
:300)
at org.exoplatform.services.jcr.impl.core.NodeImpl.getNode(NodeImpl.java:193)
at org.exoplatform.services.jcr.ext.hierarchy.impl.NodeHierarchyCreatorImpl.getUserApplicati
onNode(NodeHierarchyCreatorImpl.java:206)
at org.exoplatform.mail.service.impl.JCRDataStorage.getMailHomeNode(JCRDataStorage.java:106)

at org.exoplatform.mail.service.impl.JCRDataStorage.getMessageHome(JCRDataStorage.java:1945)
...

Fix description

How is the problem fixed?
* This problem appears because the session is expired and the condition is created again is incorrect. That condition is to check Identity for current user.

Patch file: CS-4440.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* 

Tests performed at QA/Support Level
* 

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: none

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Patch validated by the Support team

QA Feedbacks
*

