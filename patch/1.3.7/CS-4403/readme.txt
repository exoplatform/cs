Summary

    * Status: eXo Mail: Mails lose their style
    * CCP Issue: CCP-489, Product Jira Issue: CS-4403
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Mail sent from a client mail (e.g. Mozilla Thunderbird) on a mail server (e.g. hMailServer) loses its style when viewing in eXo Mail.

Fix description

How is the problem fixed?

    * Add a supplementing function to check whether a message has attach file/s before saving the message and/or viewing its detail.

Patch information:
Patch files:
CS-4403.patch

Tests to perform

Reproduction test
To reproduce:

    * Install a local mail server like hMailServer
    * Configure your mail server
    * Add two mail addresses (e.g. demo@localhost.com and root@localhost.com)
    * Login as root
    * Configure your mail account at eXo Mail
    * Logout
    * Login as demo
    * Configure your mail account at eXo Mail
    * Configure an email client (e.g. Mozilla Thunderbird) for root@localhost.com
    * Write an email and send it to demo@localhost.com
    * Login to eXo Mail as demo
    * Chekout inbox mail
    * Open the mail you sent from Thunderbird
      --> Mail loses its style.

Tests performed at DevLevel

    * Tested on CS 1.3.x, 1.3.5

Tests performed at QA/Support Level
*


Documentation changes

Documentation Changes:
*


Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*


Risks and impacts

Can this bug fix have an impact on current client projects ?

    * Function or ClassName change?

Is there a performance risk/cost?
*


Validation (PM/Support/QA)

PM Comment

    * Patch review by PM : approved

Support Comment

    * Patch validated by support

QA Feedbacks
*

