Summary

    * Status: Mail: Cannot send/receive email with incoming/outgoing SSL protocol
    * CCP Issue: N/A, Product Jira Issue: CS-4681.
    * Complexity: N/A

The Proposal

Problem description

What is the problem to fix?

    * Using SSL protocol for incomming/outgoing server is not successful.

Fix description

How is the problem fixed?

    * We provide additional function to trust the certs that presented by server. To know detail information, show ExoMailSSLSocketFactory.java file.

Patch file: CS-4681.patch

Tests to perform

Reproduction test

    * Yes

Tests performed at DevLevel

    * Yes

Tests performed at QA/Support Level

    * Yes

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:

    * There is no configuration change

Will previous configuration continue to work?

    * No need

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

      eXoApplication/mail/service/src/test/java/org/exoplatform/mail/service/test/TestConnectorService.java
      eXoApplication/mail/service/src/main/java/org/exoplatform/mail/connection/impl/ImapConnector.java
      eXoApplication/mail/service/src/main/java/org/exoplatform/mail/service/Utils.java
      eXoApplication/mail/service/src/main/java/org/exoplatform/mail/service/impl/ExoMailSSLSocketFactory.java
      eXoApplication/mail/service/src/main/java/org/exoplatform/mail/service/impl/MailServiceImpl.java

      Is there a performance risk/cost?
    * No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*

