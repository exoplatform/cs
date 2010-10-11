Summary

    * Status: Impossible to edit mail account details while using auto configuration
    * CCP Issue: CCP-473, Product Jira Issue: CS-4386.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When trying to edit a Mail account that was created automatically, some fields are not stored correctly.

Fix description

How is the problem fixed?

    * The fix will fill those missing properties with the required  values when the account is created.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
There are currently no attachments on this page.

Tests to perform

Reproduction test
* Activate the "mail.new.user.event.listener" component plugin in "war:/portal/WEB-INF/conf/cs/cs-plugins-configuration.xml".
* Run server
* Go to Mail application
* Edit current account, change nothing and click Save button  

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* Run edit account case

Documentation changes:
* No

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?
    * Function or ClassName change 

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
    * PM review: patch 2010-09-27-CS-4386.patch approved

Support Comment
    * Patch validated by support

QA Feedbacks
*

