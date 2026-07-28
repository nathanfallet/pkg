<#import "../../layout.ftl" as l>
<@l.page view.layout>
    <div class="mb-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="/packages"><@t key="nav_packages" /></a></li>
                <li class="breadcrumb-item">
                    <a href="${view.packageItem.url}">${view.packageItem.name}</a>
                </li>
                <li class="breadcrumb-item active" aria-current="page">${view.item.version}</li>
            </ol>
        </nav>
    </div>

    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                    <h2 class="card-title mb-2">
                        ${view.packageItem.name} <span class="text-muted">v${view.item.version}</span>
                    </h2>
                    <div class="mb-2">
                        <span class="badge ${view.packageItem.visibilityBadge} me-2">${view.packageItem.visibility}</span>
                        <span class="badge bg-light text-dark">${view.packageItem.format}</span>
                        <#if view.item.yanked>
                            <span class="badge bg-warning text-dark">Yanked</span>
                        </#if>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6">
                    <dl class="row">
                        <dt class="col-sm-4">Version</dt>
                        <dd class="col-sm-8">${view.item.version}</dd>

                        <dt class="col-sm-4">Published</dt>
                        <dd class="col-sm-8">${view.item.publishedAt}</dd>

                        <#if view.item.metadata??>
                            <dt class="col-sm-4">Metadata</dt>
                            <dd class="col-sm-8">${view.item.metadata}</dd>
                        </#if>
                    </dl>
                </div>
            </div>
        </div>
    </div>

    <#if view.files?size gt 0>
        <div class="card shadow-sm border-0">
            <div class="card-header bg-white">
                <h4 class="mb-0">Files</h4>
            </div>
            <div class="list-group list-group-flush">
                <#list view.files as file>
                    <div class="list-group-item">
                        <div class="d-flex justify-content-between align-items-center">
                            <div class="flex-grow-1">
                                <h5 class="mb-1">${file.name}</h5>
                                <p class="mb-0 text-muted small">${file.contentType} • ${file.size}</p>
                            </div>
                            <div>
                                <a href="${file.url}" class="btn btn-sm btn-outline-primary" download>Download</a>
                            </div>
                        </div>
                    </div>
                </#list>
            </div>
        </div>
    <#else>
        <div class="alert alert-info">No files available for this version.</div>
    </#if>
</@l.page>
