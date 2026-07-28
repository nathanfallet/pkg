<#import "../layout.ftl" as l>
<@l.page view.layout>
    <div class="mb-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="/users"><@t key="nav_users" /></a></li>
                <li class="breadcrumb-item active" aria-current="page">${view.item.email}</li>
            </ol>
        </nav>
    </div>

    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                    <h2 class="card-title mb-2">${view.item.email}</h2>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6">
                    <dl class="row">
                        <dt class="col-sm-4">Email</dt>
                        <dd class="col-sm-8">${view.item.email}</dd>

                        <dt class="col-sm-4">User ID</dt>
                        <dd class="col-sm-8">${view.item.id}</dd>

                        <dt class="col-sm-4">Organization</dt>
                        <dd class="col-sm-8">${view.organization.name}</dd>

                        <dt class="col-sm-4">Organization ID</dt>
                        <dd class="col-sm-8">${view.item.organizationId}</dd>
                    </dl>
                </div>
            </div>
        </div>
    </div>
</@l.page>
