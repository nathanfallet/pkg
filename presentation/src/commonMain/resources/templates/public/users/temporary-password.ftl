<#import "../layout.ftl" as l>
<@l.page view.layout>
    <h2>Temporary password</h2>
    <p>Give this password to <strong>${view.username}</strong>. It is shown only in this response and cannot be retrieved later.
        They must change it on their first login.</p>
    <div class="alert alert-warning"><code id="temporary-password">${view.password}</code></div>
    <a href="/admin/users" class="btn btn-primary">Return to user management</a>
</@l.page>
