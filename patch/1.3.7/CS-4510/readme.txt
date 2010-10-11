Summary

    * Status: Smacks threads related to user connection
    * CCP Issue: CCP-591, Product Jira Issue: CS-4510.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
When the user connects, connect() (origin of threads Smack Keep Alive, Smack Packet Reader and Smack Packet Writer) and login() methods (origin of Smack Listener Processor) are applied from org.jivesoftware.smack.XMPPConnection. These two methods are used to maintain and authenticate user's connection.

Whenever the user disconnects, disconnect() method of the same class is applied. In that case threads are killed. It is not the case of session expiration (nothing is applied to the disconnect() method), threads are still alive -> Not OK. 

It is necessary to mention that the same threads are used whenever the user connects again. When the user logs out of the chat server, those threads are evacuated.

Fix description

How is the problem fixed?
    * At session time out, the ExoContainer is null that's why logout listener does not work 
    * Now we check and get PortalContainer from RootContainer, then logout listener will be called. The connection to the chat server is now cleaned up.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:

Tests to perform

Reproduction test
* No

Tests performed at DevLevel
* Yes, put the break point in AuthenticationLogoutListener.java to test the function onEvent when session time out

Tests performed at QA/Support Level
* No

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

    * Function or ClassName change : None

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
    * Patch approved

Support Comment
    * Patch validated by Support Team

QA Feedbacks
*

