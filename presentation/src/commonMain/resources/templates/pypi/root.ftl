<!DOCTYPE html>
<html>
<head>
    <meta name="pypi:repository-version" content="1.3">
    <title>Simple index</title>
</head>
<body>
<h1>Simple index</h1>
<#list view.packages as package>
    <a href="${package.url}">${package.name}</a><br/>
</#list>
</body>
</html>
