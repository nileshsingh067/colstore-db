<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%

%>
<?xml version='1.0' encoding='UTF-8' ?> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:p="http://primefaces.org/ui"
      xmlns:f="http://xmlns.jcp.org/jsf/core">

    <h:head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta http-equiv="Cache-Control" content="max-age=0;no-cache,no-store"/>
        <meta http-equiv="X-Frame-OPTIONS" content="SAMEORIGIN"/>
        <h:outputStylesheet name="./css/default.css"/>
        <h:outputStylesheet name="./css/cssLayout.css"/>
        <title>JCMS-WEB</title>
        <script type="text/javascript">
            document.oncontextmenu = function() {
                return false
            };
            
        
        </script>
    </h:head>

    <h:body>
        
        <h:form id="loginform">
            
        <div id="top">
               <p:panelGrid columns="1" style="width:100%;">
                   <f:facet name="header">
                       <table border="0" width="100%">
                            <tr>
                                <td class="bannerhead" width="50%" align="left">JIFFY-CMS</td>
                                <td class="bannertext" width="50%" align="right">Login Role:</td>
                            </tr>
                   </table>
                   </f:facet>
                   
               </p:panelGrid>
            
               <p:panelGrid columns="1" style="width:100%;">
                    <p:menubar id="mainmenu" model=""/>
              </p:panelGrid>
          
        </div>
             <p:spacer height="10px;"/>
        <div id="content" class="center_content">
                <p:growl id="msgOut" />
                <h:panelGrid border="0" columns="5">
                    <p:column class="bannerheadsmall">User Id :</p:column>
                    <p:column>
                        <p:inputText class="bannerheadsmall" id="loginid" value="#{loginBean.userName}" autocomplete="off" style="width:100px;">
                        </p:inputText>
                    </p:column>
                    <p:column class="bannerheadsmall">Password :</p:column>
                    <p:column>
                        <input type="password" name="password_fake" id="password_fake" value="" style="display: none;" />
                        <p:password id="loginpwd" redisplay="true" value="#{loginBean.password}" required="true" maxlength="50" style="width:100px;"/>
                   </p:column>
                        <p:column class="bannerheadsmall"><p:commandButton class="bannerheadsmall" id="loginsubmit" update="msgOut" value="Login" action="#{loginBean.checkLogin()}"  style="width:100px;" />
                    </p:column>
                
                </h:panelGrid>
        </div>
           <!-- <h:outputText id="msgout1" value="#{loginBean.errMsg}"/>-->
  </h:form>
        <p:spacer height="100px;"/>
        <div id="bottom" class="bannerheadsmall">
            Copyright © 2015 by Jiffy Software Solutions Pvt. Ltd.(OPC)
        </div>

    </h:body>

</html>
