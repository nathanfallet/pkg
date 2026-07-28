<#import "layout.ftl" as l>
<@l.page view.layout>
    <div class="row justify-content-center">
        <div class="col-md-6 text-center">
            <h1 class="display-1">${view.status}</h1>
            <p class="lead">${view.message}</p>
            <a href="/" class="btn btn-primary"><@t key="error_back_home" /></a>
        </div>
    </div>
</@l.page>
