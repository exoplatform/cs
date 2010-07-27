Summary

    * Status: Remove the use of exoservice gmail Account from MailService configuration
    * CCP Issue: CCPID, Product Jira Issue : CS-4197
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * for now, we use a gmail account as default account in mail service. Its configuration is placed in cs-plugins-configuration.xml file. However, this way is inconvenience because we don't want user to use our account in his product.

Fix description

How is the problem fixed?

    * Change two properties: account, password in cs-plugins-configuration.xml file to empty. User must add his account before using the mail service.

Patch information:
Patch files:
File CS-4197.patch 	

Tests to perform

Tests performed at DevLevel ?
* Unit Test case.

Tests performed at QA/Support Level ?
*


Documentation changes

Documentation Changes:
* Need to update document about how to configure mail service.


Configuration changes

Configuration changes:
* Two properties: account, password in cs-plugins-configuration.xml file

Will previous configuration continue to work?
* yes


Risks and impacts

Can this bug fix have an impact on current client projects?

    * Mail service could not run until the above configuration is added by user.

Is there a performance risk/cost?
*


Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

