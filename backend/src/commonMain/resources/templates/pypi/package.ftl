<!DOCTYPE html>
<html>
<head>
    <meta name="pypi:repository-version" content="1.3">
    <title>Links for ${view.name}</title>
</head>
<body>
<h1>Links for ${view.name}</h1>
<#list view.files as file>
    <a href="${file.url}">${file.name}</a><br/>
</#list>
</body>
</html>
