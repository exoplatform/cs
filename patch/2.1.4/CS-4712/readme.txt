Summary

    * Status: Too many opened connections in XMPP
    * CCP Issue: CCP-803, Product Jira Issue: CS-4712.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  
      it seems that xmpp opens too many connections:

      root@demo-host demo_plf]# lsof -p 22555 | grep xmpp-client | grep CLOSE_WAIT | wc -l
      397
      [root@demo-host demo_plf]# lsof -p 22555 | grep xmpp-client | grep ESTABLISHED | wc -l
      287

      From the stacktrace, we see that when calling a REST service something is happening in xmpp. It may be due to this.

      java.lang.OutOfMemoryError: unable to create new native thread
      	java.lang.Thread.start0(Native Method)
      	java.lang.Thread.start(Thread.java:640)
      	org.jivesoftware.smack.PacketWriter.startup(PacketWriter.java:183)
      	org.jivesoftware.smack.XMPPConnection.initConnection(XMPPConnection.java:942)
      	org.jivesoftware.smack.XMPPConnection.connectUsingConfiguration(XMPPConnection.java:904)
      	org.jivesoftware.smack.XMPPConnection.connect(XMPPConnection.java:1415)
      	org.exoplatform.services.xmpp.connection.impl.XMPPSessionImpl.<init>(XMPPSessionImpl.java:249)
      	org.exoplatform.services.xmpp.connection.impl.XMPPMessenger.login(XMPPMessenger.java:192)
      	org.exoplatform.services.xmpp.connection.impl.AuthenticationLoginListener.onEvent(AuthenticationLoginListener.java:64)
      	org.exoplatform.services.listener.ListenerService.broadcast(ListenerService.java:110)
      	org.exoplatform.services.security.ConversationRegistry.register(ConversationRegistry.java:143)
      	org.exoplatform.services.security.web.SetCurrentIdentityFilter.getCurrentState(SetCurrentIdentityFilter.java:189)
      	org.exoplatform.services.security.web.SetCurrentIdentityFilter.doFilter(SetCurrentIdentityFilter.java:85)

Fix description

How is the problem fixed?

    *  There's case when user login failed to chat server but the XMPP connection is already created and it can't be released when user logout or portal session timeout, because the XMPP session for this user was not saved to sessions map. This situation can occur when the connection to chat server has established but can't login to chat server (authenticate failed). So when catching XMPPException in XMPPSessionImpl constructor, we must check if connection was created then released it by disconnect() function.
    * When logout or session timeout, put the logout() call to finally block to ensure chat session will be always released properly.

Patch information:
Patch files: CS-4712.patch

Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
*

Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*

Risks and impacts
Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*

Validation (PM/Support/QA)
PM Comment
*

Support Comment
*

QA Feedbacks
*

