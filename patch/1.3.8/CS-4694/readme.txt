Summary

    * Status: [Chat] Buddy list with long user names not correctly displayed
    * CCP Issue: CCP-705, Product Jira Issue: CS-4694.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When user name is too long, there is a display trouble in contact list in the chat bar.

Fix description

How is the problem fixed?

    * Modify the stylesheet of chat bar.

Patch file: CS-4694.patch

Tests to perform

Reproduction test
To reproduce the issue, you should follow these steps:

   1. Register a first user with first name "testuser" and Last name "testusermorethanoneline"
   2. Register a second user with first name "zuser" and Last name "zuser"
   3. Login as another available user (eg. john).
   4. Add "testuser" and "zuser" to contact list.
      The display in contact list is broken.

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
* Function or ClassName change: no

Is there a performance risk/cost?
* N/A

Validation (PM/Support/QA)

PM Comment
* PM review: patch approved

Support Comment
* Patch validated by the Support team

QA Feedbacks
*

