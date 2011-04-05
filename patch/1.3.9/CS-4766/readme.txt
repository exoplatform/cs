Summary

    * Status: Offline messages are marked with current date
    * CCP Issue: CCP-780, Product Jira Issue: CS-4766.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* Steps to reproduce:
   1. Connect with root, add John to contact
   2. Connect with John. John accepts root's request
   3. John disconnects
   4. Root sends a message when John is disconnected.
   5. Change time and date in John's machine to the day after. Connect again with John.
      -> John receives the message with the new date.

Fix description

How is the problem fixed?

    * Don't re-set the send date property of message bean object when sending message to client
    * When Openfire server pushes messages to client, the sending date is got from the delay date of message.

Patch file: CS-4766.patch

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

    * Function or ClassName change: None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

