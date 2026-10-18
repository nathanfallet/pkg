<#import "../layout.ftl" as l>
<@l.page view.layout>
    <h2>Manage users</h2>
    <p>Create accounts directly. Give each person their temporary password; no email is sent.</p>
    <div class="table-responsive"><table class="table align-middle">
        <thead><tr><th>Username</th><th>Organization</th><th>Access</th><th>Password</th></tr></thead>
        <tbody><#list view.users as user><tr>
            <td>${user.email}<#if user.passwordChangeRequired><br><small>Password change required</small></#if></td>
            <td><#list view.organizations as organization><#if organization.id == user.organizationId>${organization.name}</#if></#list></td>
            <td><form method="post" action="/admin/users/${user.id}/update">
                <input type="hidden" name="csrf" value="${view.csrf}">
                <label class="me-2"><input type="checkbox" name="admin" <#if user.admin>checked</#if>> Administrator</label>
                <label class="me-2"><input type="checkbox" name="disabled" <#if user.disabled>checked</#if>> Disabled</label>
                <button class="btn btn-sm btn-outline-primary">Save</button>
            </form></td>
            <td><form method="post" action="/admin/users/${user.id}/reset-password">
                <input type="hidden" name="csrf" value="${view.csrf}">
                <button class="btn btn-sm btn-outline-secondary">Reset password</button>
            </form></td>
        </tr></#list></tbody>
    </table></div>
    <h3>Add a user</h3>
    <form method="post">
        <input type="hidden" name="csrf" value="${view.csrf}">
        <div class="mb-3"><label class="form-label" for="username">Username</label>
            <input class="form-control" id="username" name="username" minlength="3" maxlength="255" pattern="[a-zA-Z0-9][a-zA-Z0-9._@+\-]{2,254}" autocomplete="off" required></div>
        <div class="mb-3"><label class="form-label" for="organization">Organization</label>
            <select class="form-select" id="organization" name="organization" required>
                <#list view.organizations as organization><option value="${organization.id}" <#if organization.id == view.layout.user.organizationId>selected</#if>>${organization.name}</option></#list>
            </select></div>
        <div class="mb-3"><label><input type="checkbox" name="admin"> Administrator (can manage all instance users)</label></div>
        <button class="btn btn-primary">Create user</button>
    </form>
</@l.page>
