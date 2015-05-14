<%@ page import="com.causata.jeeves.JenkinsConfig" %>
<%@ page import="java.util.Set" %>
<%--
  Created by IntelliJ IDEA.
  User: ciccio
  Date: 14/05/15
  Time: 16:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Test</title>
</head>

<body>

<% String baseUrl = request.getParameter("Jenkins Base URL");



  JenkinsConfig jenkinsConfig = new JenkinsConfig(request.getParameter("Jenkins Base URL"));
  jenkinsConfig.fetchCurrentJobsConfig();
  Set<String> allBranchesNames = jenkinsConfig.getAllBranchesNames();
  for (String branchName : allBranchesNames) {
  %>
    <table>
      <tr>
        <td><%= branchName %></td>
      </tr>
    </table>
  <%
  }
%>

</body>
</html>
