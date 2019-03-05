$(document).ready
(
	function()
	{
		var  locale = $.cookie( "org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE" );
		
		jQuery.i18n.properties( {name: "messages", path: "/res/i18n", mode: "map", language: (locale != undefined && locale != null && locale != "" ? locale.replace("-","_") : "zh_CH"), async: false} );
		
		$("#signin_button").click
		(
			function()
			{
				$.ajax( {url: "/user/signin", type: "POST", data: {username: $("#username").val(), password: $.md5($("#password").val()).toUpperCase(), roletype: 1}, complete: (response, status) => 
					response.status != "200" ? Swal.mixin({toast: true, animation: false, position: "center", showConfirmButton: false, timer: 3000}).fire({type: "error", title: jQuery.i18n.prop("text.username-or-password-error")}) : (window.location.href = "/monitoring/tomain?SECRET_KEY="+JSON.parse(response.responseText).SECRET_KEY)} );
			}
		);
		
		$("a.langsel_branch_item[href='?locale="+(locale != undefined && locale != null && locale != "" ? locale.replace("-","_") : "zh_CH")+"']").addClass( "selected" );
		
		$("a.langsel_branch_item.selected").attr( "href","javascript:void(0)" );
		
		$("a.langsel_parent_item").text( $("a.langsel_branch_item.selected").text() );
	}
);