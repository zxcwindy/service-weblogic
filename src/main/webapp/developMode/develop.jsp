<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <script type="text/javascript" src="<%=request.getContextPath()%>/developMode/org/cometd.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/developMode/org/cometd/AckExtension.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/developMode/org/cometd/ReloadExtension.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/developMode/jquery/jquery-1.9.1.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/developMode/jquery/jquery.cookie.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/developMode/jquery/jquery.cometd.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/developMode/jquery/jquery.cometd-reload.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/developMode/develop.js"></script>
    <title><decorator:title/></title>
    <decorator:head/>
  </head>
  <body>
    <decorator:body/>
  </body>
</html>
