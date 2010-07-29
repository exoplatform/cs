Summary

    * Status: eXo Mail : Advanced search is case sensitive
    * CCP Issue: CCP-485, Product Jira Issue : CS-4399
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  the advanced search function in Mail is case sensitive. The issue requires to change this function to be case insensitive.

Fix description

How is the problem fixed?

    *  change JCR query. Using fn:upper-case() functions to convert keywords to upper case before querying.

Patch information:
Patch files:
File CS-4399.patch

Tests to perform

Tests performed at DevLevel?
* Unit Test cases.

* Sniff test for advanced search function in mail.

Tests performed at QA/Support Level?
*


Documentation changes

Documentation Changes:
*


Configuration changes

Configuration changes:
* No Configuration changed.

Will previous configuration continue to work?
*


Risks and impacts

Can this bug fix have an impact on current client projects?

    * Function or ClassName change?
          o Function: org.exoplatform.mail.service.MessageFilter.getStatement()

Is there a performance risk/cost?
*  Not detected yet.


Validation (PM/Support/QA)

PM Comment
*

Support Comment
* Support Patch review : validated

QA Feedbacks
*

