Summary

    * Status: XML parsing error caused by wrong Encoding at org.exoplatform.rest.client.openfire.Utils.doGet
    * CCP Issue: CCP-570, Product Jira Issue: CS-4468.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * To reproduce the problem :
      1- login as root
      2- Go to community management portlet
      3- Add a new group of users : invités (the group name must contain an accented character )
      4- Add user marry into it
      5- Logout and login with user Marry.
      An exception is raised in OpenFire

      [Fatal Error] :1:226: Invalid byte 2 of 3-byte UTF-8 sequence.
      org.apache.commons.httpclient.HttpException: XML parsing error : org.xml.sax.SAXParseException: Invalid byte 2 of 3-byte UTF-8 sequence.
      	at org.exoplatform.rest.client.openfire.Utils.doGet(Utils.java:120)
      	at org.exoplatform.rest.client.openfire.ExoGroupProvider.getGroupNames(ExoGroupProvider.java:292)
      	at org.exoplatform.rest.client.openfire.ExoGroupProvider.getGroupNames(ExoGroupProvider.java:247)
      	at org.jivesoftware.openfire.group.GroupManager.getGroups(GroupManager.java:363)
      	at org.jivesoftware.openfire.roster.Roster.<init>(Roster.java:105)
      	at org.jivesoftware.openfire.roster.RosterManager.getRoster(RosterManager.java:85)
      	at org.jivesoftware.openfire.user.User.getRoster(User.java:302)

      This exception is caused by the fact that in function org.exoplatform.rest.client.openfire.Utils.doGet(URL, HashMap<String,String>), we don't specify the encoding of the InputStream which MUST be UTF-8 at:

      resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(get.getResponseBodyAsStream());

Fix description

How is the problem fixed?

    * Use java.net.URLEncoder to encodes querystring of GET method URL to UTF-8

Patch file: CS-4468.patch

Tests to perform

Reproduction test
* Steps:
1- login as root
2- Go to community management portlet
3- Add a new group of users : invités (the group name must contain an accented character )
4- Add user marry into it 
5- Logout and login with user Marry.

Tests performed at DevLevel
* Yes

Tests performed at QA/Support Level
* No

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change : None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PM review: patch approved

Support Comment
* Support review: patch validated

QA Feedbacks
*
