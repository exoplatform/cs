function createXMLHTTPRequest() {
  var xmlHttp=false;
  try {
    xmlHttp=new XMLHttpRequest();
  } catch(ex) {
    try {
      xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
    } catch(ex1) {
      try {
        xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
      } catch(ex2) {
        xmlHttp=false;
	    }
    }
  }
  if(!xmlHttp)
    alert("Can't create XMLHttpRequest/Microsoft.XMLHTTP");
  return xmlHttp;
}

function remove(url) {
  var xmlHttp = createXMLHTTPRequest();
  if (!xmlHttp)
    return;
  xmlHttp.open("GET", url, true);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.readyState == 4){
      if (xmlHttp.status == 204)
        window.location.reload();
      else if(typeof ActiveXObject != "undefined" && xmlHttp.status == 1223) //IE mangles HTTP response status code 204 to 1223
        window.location.reload();
      else 
        alert("Error : HTTP status " + xmlHttp.status + ".");
    } 
  }
  xmlHttp.send(null);
}

function groups(url, image){
  if(image.alt == "+") {
    var xmlHttp = createXMLHTTPRequest();
    if (!xmlHttp)
      return;
    xmlHttp.open("GET", url, true);
    xmlHttp.onreadystatechange=function() {
	      if (xmlHttp.readyState == 4) {
	        var newDIV = document.createElement("div");
	        newDIV.style.marginLeft="10px"
	        var resTxt = xmlHttp.responseText.replace(/(^\s*)|(\s*$)/g, "");
	        if (resTxt != "")
	          newDIV.innerHTML = resTxt;
	        else
		        newDIV.innerHTML="<i>No Items</i>"
        	image.alt="-";
	        image.src="/organization/img/SmallGrayMinus.gif";
	        image.parentNode.appendChild(newDIV);
	      }
	    }
    xmlHttp.send(null);
  } else {
    var parentDIV = image.parentNode;
    parentDIV.removeChild(image.parentNode.lastChild);
    image.alt="+";
    image.src="/organization/img/SmallGrayPlus.gif";
  }
}

function checkForm_createUser(form) {
  var username = form.username.value.replace(/(^\s*)|(\s*$)/g, "");
  if (username == "") {
    alert("Error : User Name is not defined!");
    return false;
  }
  var password = form.password.value.replace(/(^\s*)|(\s*$)/g, "");
  if (password == "") {
    alert("Error : Password is not defined!");
    return false;
  }
  var confirm = form.confirm_password.value.replace(/(^\s*)|(\s*$)/g, "");
  if (password != confirm) {
    alert("Error : Password Confirmation failed!");
    return false;
  }
  var email = form.email.value.replace(/(^\s*)|(\s*$)/g, "");
  if (email != "" && email.match(/^([\w\-]+)@([\w\-\.]+)$/)==null) {
    alert("Error : Wrong email address: " + email);
    return false;
  }
  return true;
}

function checkForm_createGroup(form) {
  var groupname = form.groupName.value.replace(/(^\s*)|(\s*$)/g, "");
  if (groupname == "") {
    alert("Error : Group Name is not defined!");
    return false;
  }
  return true;
}

function init_Membership(groupsURL, membershipTypes, username) {
  var xmlHttp = createXMLHTTPRequest();

  if (!xmlHttp)
    return;
    
  var form = "";
  form += "<input type='hidden' name='username' value='" + username + "'/>";
  xmlHttp.open("GET", membershipTypes, false);
  xmlHttp.onreadystatechange=function() {
      if (xmlHttp.readyState == 4) {
        if (xmlHttp.status != 200)
          return;
        var types = xmlHttp.responseXML.getElementsByTagName("membership-type");
        form += "<table class='membership-add' style='margin-top: 20px;'>";
        form += "<tr><td class='table-title' colspan='2'>Add membership</td></tr>";
        form += "</tr><tr><td>Membership type:</td><td><select name='type' class='input'>";
        for (i = 0; i < types.length; i++)
          form += "<option>" + types[i].getAttribute("name") + "</option>";
        form += "</select></td>";
      }
    }
  xmlHttp.send(null);
  
  // first connection is free - use it again
  xmlHttp.open("GET", groupsURL, false);
  xmlHttp.onreadystatechange = function() {
  	if (xmlHttp.readyState == 4) {
	    if (xmlHttp.status != 200)
	       return;
	    var groups = xmlHttp.responseXML.getElementsByTagName("group");
	    form += "<tr><td>Group: </td><td><select name='groupId' class='input'>";
	    for (i = 0; i < groups.length; i++)
	    	form += "<option>" + groups[i].getAttribute("groupId") + "</option>";
	    form += "</select></td></tr>";
	    form += "<tr><td class='action' colspan='2'><input class='input' type='button' value='Save' onclick='submit_form();'/>";
	    form += "</td></tr></table>";
  	}
  }
  xmlHttp.send(null);
       
  document.getElementById("addMembership").innerHTML = form;
    
}

function submit_form(){
	var xmlHttp = createXMLHTTPRequest();
  	if (!xmlHttp)
    	return;
	var form = document.getElementById("addMembership");
	var url = form.action + "?";
	for(var i = 0; i< form.length-1; i++){
		url += form.elements[i].name + "=" + form.elements[i].value + "&";
	}
	url = url.substring(0, url.length - 1);
	xmlHttp.open("POST", url, false);
  	xmlHttp.onreadystatechange = function() {
  		if (xmlHttp.readyState == 4) {
		    if (xmlHttp.status != 201)
		       	return;
		    window.location.reload();
  		}
  	}
  	xmlHttp.send(null);
}

function update_user(form){
	//var form = document.getElementById("updateUser");
	if( !checkForm_createUser(form) )
		return false;
	
	var xmlHttp = createXMLHTTPRequest();
  	if (!xmlHttp)
    	return;
	
	var url = form.action + "?";
	for(var i = 0; i< form.length-1; i++){
		url += form.elements[i].name + "=" + form.elements[i].value + "&";
	}
	url = url.substring(0, url.length - 1);
	xmlHttp.open("POST", url, false);
  	xmlHttp.onreadystatechange = function() {
  		if (xmlHttp.readyState == 4) {
		    if (xmlHttp.status != 201)
		       	return;
		    window.location.reload();
		    window.back();
  		}
  	}
  	xmlHttp.send(null);
}

function createGroup(){
	var form = document.getElementById("createGroup");
	if( !checkForm_createGroup(form) )
		return false;
		
	var xmlHttp = createXMLHTTPRequest();
  	if (!xmlHttp)
    	return;
	
	var url = form.action + "?";
	for(var i = 0; i< form.length-1; i++){
		url += form.elements[i].name + "=" + form.elements[i].value + "&";
	}
	url = url.substring(0, url.length - 1);
	xmlHttp.open("POST", url, false);
  	xmlHttp.onreadystatechange = function() {
  		if (xmlHttp.readyState == 4) {
		    if (xmlHttp.status != 201)
		       	return;
		    window.location.reload();
		    window.back();
  		}
  	}
  	xmlHttp.send(null);
}

function groupSelected(group) {
  document.getElementById("selected-group").value = group;
  document.getElementById("output-list").style.border = "none";
  document.getElementById("output-list").style.display = "none";
}

