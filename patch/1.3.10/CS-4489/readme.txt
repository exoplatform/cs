Summary

    * Status: *_en.properties are missing in Mail, Calendar and contact portlets
    * CCP Issue: N/A, Product Jira Issue: CS-4489.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * _en.properties are missing in Mail, Calendar and contact portlets

Fix description

How is the problem fixed?

    * Add 3 Resource Bundle files for English

Patch file: CS-4489.patch

Tests to perform

Reproduction test
In tomcat bundle: 
   1. Add -Duser.language=fr -Duser.country=FR into JAVA_OPTS in eXo.sh
   2. Start portal and login
   3. Change language to EN
   4. Go to Mail portlet or Contact portlet or Calendar portlet
   5. The labels are in French

Tests performed at DevLevel

Tests performed at QA/Support Level

Documentation changes

Documentation changes:

    * N/A

Configuration changes

Configuration changes:

    * N/A

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * N/A

Is there a performance risk/cost?

    * Low

Validation (PM/Support/QA)

PM Comment

    * PL Review : Patch validated

Support Comment

    * Patch validated

QA Feedbacks
*
