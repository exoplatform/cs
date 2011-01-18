Summary

    * Status: Unit Tests for Connectors in CS 1.3.x
    * CCP Issue: N/A, Product Jira Issue: CS-4574.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Connectors are being used in codebase to concentrate mail related operations.
      It is critical that they are well tested and we need unit tests to cover their methods.

Fix description

How is the problem fixed?
    * Implement the mock test class and use 3nd party library to work on
      Now all the mail test will use mock classes instead of using real mail server.
    * Save time and avoid crash test when mail server has problem or change configuration

Patch file: CS-4574.patch

Tests to perform

Reproduction test
* Run build full with test include

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* 

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* Yes, added some 3nd lib

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PM review : patch approved

Support Comment
* 

QA Feedbacks
*

