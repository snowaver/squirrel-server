$(document).ready
(
	function()
	{
		var  locale = $.cookie( "org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE" );
		
		jQuery.i18n.properties( {name: "messages", path: "/res/i18n", mode: "map", language: (locale != undefined && locale != null && locale != "" ? locale.replace("-","_") : "zh_CH"), async: false} );
		
		$("#table").bootstrapTable( {columns: [{field: "ID", title: jQuery.i18n.prop("text.id"), width: "25%"}, {field: "USERNAME", title: jQuery.i18n.prop("text.username"), width: "25%"}, {field: "NAME", title: jQuery.i18n.prop("text.name"), width: "25%"}, {field: "NICKNAME", title: jQuery.i18n.prop("text.nickname"), width: "25%"}],
			height: $(document).height(), rowStyle: (row, index) => {return  {classes: "", css: {height: "45px"}};}, url: "/user/search?SECRET_KEY="+$("#secret_key").val()+"&action=1&keyword=&extras="+encodeURI("{}"), 
			onPostBody: () => {$("div.fixed-table-container").css("border-radius", "0px");  $("table  th>div.th-inner,table  td").css("line-height", "29px");  $("th,.fixed-table-header  table").css("border-bottom-style", "none");}} );
	}
);