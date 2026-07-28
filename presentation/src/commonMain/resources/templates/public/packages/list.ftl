<#import "../layout.ftl" as l>
<#import "../../components/packages/card.ftl" as card>
<@l.page view.layout>
    <div class="mb-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item active" aria-current="page"><@t key="nav_packages" /></li>
            </ol>
        </nav>
    </div>

    <#if view.packages?size gt 0>
        <#list view.packages as package>
            <@card.card package />
        </#list>
    <#else>
        <div class="alert alert-info">No packages published yet.</div>
    </#if>
</@l.page>
