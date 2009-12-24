<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:ns="http://exoplatform.org/organization/rest"
	exclude-result-prefixes="xlink ns">

	<xsl:output method="html" encoding="UTF-8" />
	
	<xsl:template match="/ns:user">
	
	 <xsl:variable name="membershipPageLocation">
      <xsl:text>/rest/organization/membership/</xsl:text>
      <xsl:text>view-all/?username=</xsl:text>
      <xsl:value-of select="./@user-name"/>
	 </xsl:variable>
	 
		<html>
			<head>
				<link rel="stylesheet" type="text/css" href="/organization/css/style.css" />
        <script type="text/javascript" src="/organization/js/script.js"></script>
			</head>
			<body>
				<div class="main">
					<form id="updateUser" action="/rest/organization/user/update/"
						method="POST"> <!--onsubmit="return checkForm_createUser(this);"-->
						<table class="user-info">
						  <tr>
                <td class="table-title" colspan="2">User Info</td>
              </tr>
							<tr>
								<td>User Name</td>
								<td>
									<input class="input" type="text" readonly="yes"	name="username" value="{./@user-name}" />
								</td>
							</tr>
							<xsl:apply-templates />
							<tr>
								<td class="action" colspan="2">
									<input class="input" type="button" style="margin-right: 10px"	value="Save" onclick="update_user(document.getElementById('updateUser'));"/>
									<input class="input" type="button" style="margin-right: 10px"	value="Back" onclick="history.go(-1);" />
									<input class="input" type="button" value="Memebership">
									 <xsl:attribute name="onclick">
                    <xsl:text>top.location="</xsl:text>
                    <xsl:value-of select="$membershipPageLocation"/>
                    <xsl:text>"; return true;</xsl:text>
									 </xsl:attribute>
									</input>
								</td>
							</tr>
						</table>
					</form>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="ns:first-name">
		<tr>
			<td>First name:</td>
			<td><input class="input" type="text" name="firstname"	value="{.}" /></td>
		</tr>
	</xsl:template>
	
	<xsl:template match="ns:last-name">
		<tr>
			<td>Last name:</td>
			<td><input class="input" type="text" name="lastname" value="{.}" /></td>
		</tr>
	</xsl:template>
	
	<xsl:template match="ns:email">
		<tr>
			<td>Email:</td>
			<td><input class="input" type="text" name="email"	value="{.}" /></td>
		</tr>
	</xsl:template>
	
	<xsl:template match="ns:password">
		<tr>
			<td>Password:</td>
			<td><input class="input" type="password" name="password" value="{.}" /></td>
		</tr>
		<tr>
			<td>Confirm password:</td>
			<td><input class="input" type="password" name="confirm_password" value="{.}" /></td>
		</tr>
	</xsl:template>

</xsl:stylesheet>
