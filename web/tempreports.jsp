<%-- 
    Document   : tempreports
    Created on : 7 Mar, 2015, 3:40:09 PM
    Author     : Rishi Tyagi
--%>

<%@page import="net.rocg.util.DBConnection"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
DBConnection dbConn=new DBConnection();
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>MobilArt Stats</title>
    </head>
    <body>
        <table>
            <tr>
                <td>Date</td>
                <td>Campaign Id</td>
                <td>Campaign Name</td>
                <td>Price Point (QAR)</td>
                <td>EventName</td>
                <td>Status</td>
                <td>Count</td>
            </tr>
            
        </table>
    </body>
</html>
