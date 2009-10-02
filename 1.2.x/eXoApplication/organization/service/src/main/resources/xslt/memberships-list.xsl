<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ns="http://exoplatform.org/organization/rest"
  exclude-result-prefixes="xlink ns">

  <xsl:output method="html" encoding="UTF-8" />
  
  <xsl:template match="/ns:memberships">
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/organization/css/style.css" />
        <script type="text/javascript" src="/organization/js/script.js"></script>
        <title>
          <xsl:text>Membership Info: </xsl:text>
          <xsl:value-of select="./@user-name" />
        </title>
      </head>
      <body>
      <xsl:attribute name="onload">
        <xsl:text>init_Membership("/rest/organization/group/filter/"</xsl:text>
        <xsl:text>, </xsl:text>
        <xsl:text>"/rest/organization/membership/get-types/"</xsl:text>
        <xsl:text>, "</xsl:text>
        <xsl:value-of select="./@user-name"/>
        <xsl:text>"); </xsl:text>
      </xsl:attribute>
        <div class="main">
          <div class="inner">
            <p class="table-title">
              <xsl:text>Membership Info: </xsl:text>
              <xsl:value-of select="./@user-name" />
            </p>
            <table class="memberships-list">
              <tr>
                <th>Type</th>
                <th>Group</th>
                <th>Actions</th>
              </tr>
              
              <xsl:choose>
                <xsl:when test="count(./ns:membership)=0">
                  <tr>
                    <td class="action" colspan="3">
                      <i>No Items</i>
                    </td>
                  </tr>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:apply-templates select="ns:membership"/>
                </xsl:otherwise>
              </xsl:choose>
            </table>

            <input class="input" type="button" value="Back"
              style="float: left;" onclick="history.go(-1);" />
            <input class="input" type="button" value="Reload"
              style="float: left;" onclick="window.location.reload();" />
            <div style="clear: both; margin-top: 25px;">
            <form id="addMembership" method="POST" 
              action="/rest/organization/membership/create/" onsubmit="window.location.reload();">
            </form>
            </div>
          </div>
        </div>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="ns:membership">

    <xsl:variable name="membershipDeleteSuffix">
      <xsl:value-of select="concat('delete/', ./ns:id )" />
    </xsl:variable>
    
    <xsl:variable name="baseLink">
      <xsl:value-of select = "concat( substring-before(./@xlink:href, 'membership'), 'membership/')" />
    </xsl:variable>

    <tr>
      <td><xsl:value-of select="./ns:type" /></td>
      <td><xsl:value-of select="./ns:group-id"/></td>
      <td class="action">
        <img src="/organization/img/Delete.gif" alt="Delete" title="Delete" style="cursor:pointer;"><xsl:attribute name="onclick">
          <xsl:text>if(confirm("Do you want delete membership?")) remove("</xsl:text>
          <xsl:value-of select="concat($baseLink, $membershipDeleteSuffix)" />
          <xsl:text>");</xsl:text></xsl:attribute></img>
      </td>
    </tr>
  </xsl:template>
  
</xsl:stylesheet>