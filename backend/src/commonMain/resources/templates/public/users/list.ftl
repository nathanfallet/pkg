<#import "../layout.ftl" as l>
<@l.page view.layout>
    <div class="mb-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item active" aria-current="page"><@t key="nav_users" /></li>
            </ol>
        </nav>
    </div>

    <#if view.users?size gt 0>
        <div class="card shadow-sm border-0">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
                <h4 class="mb-0">Organization Users</h4>
            </div>
            <div class="list-group list-group-flush">
                <#list view.users as user>
                    <div class="list-group-item">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h5 class="mb-1">${user.email}</h5>
                                <p class="mb-0 text-muted small">User ID: ${user.id}</p>
                            </div>
                            <div>
                                <a href="${user.url}" class="btn btn-sm btn-outline-primary">Details</a>
                            </div>
                        </div>
                    </div>
                </#list>
            </div>
        </div>
    <#else>
        <div class="alert alert-info">No users in this organization yet.</div>
    </#if>
</@l.page>
