<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ns="http://exoplatform.org/organization/rest"
  exclude-result-prefixes="xlink ns">

  <xsl:output method="html" encoding="UTF-8" />
  <xsl:template match="/ns:group">
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/organization/css/style.css" />
        <script type="text/javascript" src="/organization/js/script.js"></script>
        <title>
          <xsl:text>Edit Group: </xsl:text>
          <xsl:value-of select="./ns:name" />
        </title>
      </head>
      <body>
        <div class="main">
          
          <xsl:variable name = "groupUpdateSuffix" >
            <xsl:text>update/</xsl:text>
          </xsl:variable>
            
          <form method="post" onsubmit="return checkForm_updateGroup(this);">
            <xsl:attribute name="action">
              <xsl:value-of select="concat(substring-before(./@xlink:href, '?'), $groupUpdateSuffix)" />
            </xsl:attribute>
            <table class="edit-group">
              <tr>
                <td class="table-title" colspan="2">Edit Group</td>
              </tr>
              <xsl:apply-templates select="./ns:name"/>
              <xsl:apply-templates select="./ns:id"/>
              <xsl:apply-templates select="./ns:label"/>
              <xsl:apply-templates select="./ns:description"/>
              <tr>
                <td class="action" colspan="2">
                  <input class="input" type="submit" style="margin-right: 10px" value="Save" />
                  <input class="input" type="button" style="margin-right: 10px" value="Back"
                  onclick="history.go(-1);" />
                </td>
              </tr>
            </table>
          </form>
        </div>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="ns:name">
    <tr>
      <td>Group Name:</td>
      <td><input class="input" type="text" name="name" value="{.}" readonly="yes" /></td>
    </tr>
  </xsl:template>
  <xsl:template match="ns:id">
    <tr>
      <td>Group ID name:</td>
      <td><input class="input" type="text" name="groupId" value="{.}" readonly="yes"/></td>
    </tr>
  </xsl:template>
  <xsl:template match="ns:label">
    <tr>
      <td>Label:</td>
      <td><input class="input" type="text" name="label" value="{.}" /></td>
    </tr>
  </xsl:template>
  <xsl:template match="ns:description">
    <tr>
      <td>Description:</td>
      <td><input class="input" type="text" name="description" value="{.}" /></td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
