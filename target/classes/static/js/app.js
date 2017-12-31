$(document).ready(function(){
    $('.dropdown').each(function (key, dropdown) {
            var $dropdown = $(dropdown);
            $dropdown.find('.dropdown-menu a').on('click', function () {
                $dropdown.find('button').text($(this).text()).append(' <span class="caret"></span>');

                var spanElement = $dropdown.parent().parent().find('key[post_value]');
                if(spanElement.length = 1)
                    spanElement.attr('post_value', $(this).text());
            });
    });

    $('.dropdown').each(function (key, dropdown) {
            var $dropdown = $(dropdown);
           $dropdown.find('show').on('click', function () {
               $dropdown.find('button').text($(this).text()).append(' <span class="caret"></span>');

               var spanElement = $dropdown.parent().parent().find('key[post_value]');
               if(spanElement.length = 1)
                   spanElement.attr('post_value', $(this).text());
           });

           $dropdown.find('show-message').on('click', function () {
              $dropdown.find('button').text($(this).attr('code') + ' - ' + $(this).text()).append(' <span class="caret"></span>');

              var spanElement = $dropdown.parent().parent().find('key[post_value]');
              if(spanElement.length = 1)
                  spanElement.attr('post_value', $(this).attr('code'));
          });
    });
})

var respCode = {
    Success : "90000",
    NotFound : "10404",
    NotPermitted : "10405",
}

var routeHelper = {
    allRoutesBasePath : "http://localhost:8092/isw-mock/administration/routes",
    isValid : function(routeId){
        return true;
    },
    buildDisableRouteBtnId : function(routeId){
        return "#disable_".concat(routeId);
    },
    buildEnableRouteBtnId : function(routeId){
     return "#enable_".concat(routeId);
    },

    updateRoute : function(mRouteId, mMethod, mStatus, mBody, mRouteType){
    var disableRoutePath = routeHelper.allRoutesBasePath;
        var requestData = {
            requestId : mRouteId,
            verb : mMethod,
            responseStatus : mStatus,
            responseBody : mBody,
            routeType : mRouteType
        }

        $.ajax({
            url: disableRoutePath,
            type: "put",
            contentType : "application/json",
            data: JSON.stringify(requestData),
            dataType : "json",
            success : function(data) {
                if(data.responseCode == "90000"){
                    $('#edit_route_' + mRouteId).modal('hide')
                }else{
                    alert("fail")
                }
            },
            error : function(e){

            }
        })
    }
}

function showPageMessage(){

}

function showRouteStateDialog(routeId){
    var textMsg = $('#btn_route_status_' + routeId).text().trim();
    if(textMsg == "Disable"){
        $(routeHelper.buildDisableRouteBtnId(routeId)).modal("show")
    }else{
        $(routeHelper.buildEnableRouteBtnId(routeId)).modal("show")
    }
}

function submitRouteUpdate(mRouteId){
    //get fields
    var verb = $('#detail_view_verb_' + mRouteId).attr("post_value");
    var status = $('#detail_view_status_code_' + mRouteId).attr("post_value");
    var template = $('#detail_view_template_' + mRouteId).attr("post_value");
    var environment = $('#detail_view_environment_' + mRouteId).attr("post_value");
    var textarea = $('#detail_view_textarea_' + mRouteId).attr("post_value");

    routeHelper.updateRoute(mRouteId, verb, status, textarea, template);

    //suspend view
    $('#view_route_' + mRouteId).modal('hide');
}

function suspend(routeId){
    var textMsg = $('#btn_route_status_' + routeId).text();

    $.get(routeHelper.allRoutesBasePath + '/disable?id=' + routeId, function (data) {
        var modalId = '#disable_' + routeId;
        if(data.responseCode = respCode.Success){
            $('#btn_route_status_' + routeId).text("Enable route")
            $(modalId).modal('hide');
        }else{
            alert("could not process route request! try again later")
        }
    })
}

function resume(routeId){
    var textMsg = $('#btn_route_status_' + routeId).text();

    $.get(routeHelper.allRoutesBasePath + '/enable?id=' + routeId, function (data) {
        var modalId = '#enable_' + routeId;
        if(data.responseCode = respCode.Success){
            $('#btn_route_status_' + routeId).text("Disable")
            $(modalId).modal('hide');
        }else{
            alert("could not process route request! try again later")
        }
    });
}

function buildDetailsModelViewDescriptor(routeId){
    return "view_route_" + routeId;
}

function buildEditDetailsModelViewDescriptor(routeId){
    return "edit_route_" + routeId;
}

function showEditDialogFromDetailView(routeId){
    var mRouteId = buildDetailsModelViewDescriptor(routeId)
    $("#" + mRouteId).modal('hide');

    var mEditRouteId = buildEditDetailsModelViewDescriptor(routeId)
    $("#" + mEditRouteId).modal('show');
}

