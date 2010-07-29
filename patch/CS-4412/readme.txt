Summary

    * Status: CS Memory Leak caused by unclosed JCR sessions
    * CCP Issue: CCP-502, Product Jira Issue : CS-4412
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Unclose session rise memory leak  

Fix description

How is the problem fixed?

    *  Retrive to close session after using it

Patch information:
Patches files:
CS-4412.patch

Tests to perform

Tests performed at DevLevel ?
* Yes, Run unit test, jcr session detector (http://wiki.exoplatform.org/xwiki/bin/view/JCR/Session+leak+detector) and selenium automation script test 

Tests performed at QA/Support Level?
* Yes


Documentation changes

Documentation Changes:
* No


Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes


Risks and impacts

Can this bug fix have an impact on current client projects?

    * Function or ClassName change ? Yes

Is there a performance risk/cost?
* Yes, it might get session close problem because some inner functions call and transaction will be call 


Validation (PM/Support/QA)

PM Comment
*

Support Comment
* Patch validated by Support

QA Feedbacks
*

