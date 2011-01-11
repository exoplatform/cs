Summary

    * Status: Chat Room: Messages lost when changing navigation
    * CCP Issue: CCP-500, Product Jira Issue: CS-4411.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* Incoming messages while the receiver is in another session are lost.

Fix description

How is the problem fixed?
    * Before send message from server to Cometd, put it into a Map (in recepient's XMPPSession)
    * After receive message in client, call a REST service to notify and remove this message from message Map
    * Just after reload or changing navigation, re-send all delayed messages from message Map to corresponding Cometd channel

Patch file: CS-4411.patch

Tests to perform

Reproduction test
   * Steps:
   1. Using the same chat room, John starts to send a message to Root
   2. Root changes to classic -> Home. In the same time John continues to send messages
   3. Messages sent by John when Root switches to another session are lost.
   4. These messages are found whenever Root checks the historical messages.

Tests performed at DevLevel
    * Apply the patch, run web server, start chat-server
    * Online and invite other user to chat
    * Send some message and switch page (application), the message should still show after page loaded.


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
*  N/A

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL review : patch approved

Support Comment
* Support Team Review: patch validated

QA Feedbacks
*
