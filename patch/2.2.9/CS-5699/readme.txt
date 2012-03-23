CS-5699: [Mail] Account is auto removed after logout
What is the problem to fix?
- Go to mail application
- Create an account
- Get all mails
- Log out and log in again
Problem: All folders of created account are deleted. A message shows: there is no email account.

Problem analysis
Because of CS-5649 Remove all Blocker, Critical, Major code violations in CS 2.2.x (eXo Quality 1.2), defaultAccount is changed from null to be StringUtils.EMPTY. So it violated the old checking block:
if (defaultAcc == null && accounts.size() > 0) defaultAcc = accounts.get(0).getId();
Default account is not set as the first account in the list, it's empty. So, no account and mails is displayed.

How is the problem fixed?
Revert this assignment to 
private String              defaultAccount_     ;
