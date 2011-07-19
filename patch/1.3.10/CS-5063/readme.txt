Summary

    * Status: NPE when sending chat message (Jboss)
    * CCP Issue: N/A, Product Jira Issue: CS-5063.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  NPE when sending chat message. 

Problem analysis
    * roster variable is not initialized by default and then when invoking it, NPE is thrown. 

Fix description

How is the problem fixed?
    * Init roster variable before using it: roster = new ArrayList<ContactBean>();

Patch file: CS-5063.patch

Tests to perform

Reproduction test

    * Steps to reproduce:

John and Marry are online.

John's side:

1. Add Marry to his contact list
2. Send message to Marry.

On Marry's side, the message is still arrived.

However, there's an error on the server console:
17:02:28,892 ERROR [STDERR] java.lang.NullPointerException
17:02:28,892 ERROR [STDERR]     at org.exoplatform.services.xmpp.connection.impl.XMPPSessionImpl$2.processPacket(XMPPSessionImpl.java:309)
17:02:28,893 ERROR [STDERR]     at org.jivesoftware.smack.PacketReader$ListenerWrapper.notifyListener(PacketReader.java:819)
17:02:28,893 ERROR [STDERR]     at org.jivesoftware.smack.PacketReader$ListenerNotification.run(PacketReader.java:799)
17:02:28,893 ERROR [STDERR]     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:417)
17:02:28,893 ERROR [STDERR]     at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:269)
17:02:28,893 ERROR [STDERR]     at java.util.concurrent.FutureTask.run(FutureTask.java:123)
17:02:28,893 ERROR [STDERR]     at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:651)
17:02:28,893 ERROR [STDERR]     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:676)
17:02:28,893 ERROR [STDERR]     at java.lang.Thread.run(Thread.java:595)

This exception doesn't occur if the receiver is offline.

Tests performed at DevLevel

    * No

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

    * Function or ClassName change: No change

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Patch validated.

Support Comment

    * Patch validated.

QA Feedbacks

    *  
