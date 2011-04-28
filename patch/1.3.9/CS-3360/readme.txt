Summary

    * Status: Can't check mail any more when edit date to check in special case
    * CCP Issue: N/A, Product Jira Issue: CS-3360.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

   1. Create an account mail, and choose date to check mail.
      Check mail -->ok, mails will be checked.
   2. Edit date to check, this date less than the above date, --> check mail ---> no mail is checked although there are mails on server --> NOK
      Check all mails --> can't check mail.

Fix description

How is the problem fixed?

    * refine logic of getting mail message: remove unnecessary variable and repair time check conditions.

Patch files: CS-3360.patch
 
Tests to perform

Reproduction test
*

   1. Create an account mail, and choose date to check mail.
      Check mail -->ok, mails will be checked.
   2. Edit date to check, this date less than the above date, --> check mail ---> no mail is checked although there are mails on server --> NOK
      Check all mails --> can't check mail.

Tests performed at DevLevel
* reproduction test.

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*None
Configuration changes

Configuration changes:
*None

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change : org.exoplatform.mail.service.impl.MailServiceImpl

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

