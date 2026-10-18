<#import "../layout.ftl" as l>
<@l.page view.layout>
    <h2>Change password</h2>
    <p>Change your temporary password before using PKG. Use at least 14 characters (at most 72 UTF-8 bytes).
        Changing it signs out all your sessions and revokes your registry tokens.</p>
    <form method="post">
        <input type="hidden" name="csrf" value="${view.csrf}">
        <div class="mb-3"><label class="form-label" for="current">Current password</label>
            <input class="form-control" id="current" name="current" type="password" autocomplete="current-password" required></div>
        <div class="mb-3"><label class="form-label" for="replacement">New password</label>
            <input class="form-control" id="replacement" name="replacement" type="password" autocomplete="new-password" minlength="14" maxlength="72" required></div>
        <button class="btn btn-primary">Change password and sign out</button>
    </form>
</@l.page>
