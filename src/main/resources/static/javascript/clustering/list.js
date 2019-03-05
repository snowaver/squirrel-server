$(document).ready
(
	function()
	{
		var  locale = $.cookie( "org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE" );
		
		jQuery.i18n.properties( {name: "messages", path: "/res/i18n", mode: "map", language: (locale != undefined && locale != null && locale != "" ? locale.replace("-","_") : "zh_CH"), async: false} );
		
		$("#table").bootstrapTable( {columns: [[{field: "ID", title: jQuery.i18n.prop("text.id"), rowspan: 2, width: "320px"}, {field: "ADDRESS", title: jQuery.i18n.prop("text.ip"), rowspan: 2, width: "160px"}, {field: "METRICS.ONLINE_COUNT", title: jQuery.i18n.prop("clustering.node.current.online-count"), rowspan: 2, width: "160px"}, {field: "METRICS.CURRENT_CPU_LOAD", title: jQuery.i18n.prop("text.cpu.load"), rowspan: 2, width: "160px", formatter: (value, row, index) => (value*100).toFixed(2)+"%"}, {title: jQuery.i18n.prop("text.memory"), rowspan: 1, colspan: 2, width: "320px", halign: "center"}, {title: jQuery.i18n.prop("text.thread"), rowspan: 1, colspan: 2, width: "320px", halign: "center"}], [{field: "METRICS.HEAP_MEMORY_MAXIMUM", title: jQuery.i18n.prop("text.memory.heap-maximum"), width: "160px", formatter: (value, row, index) => (value/(1024*1024)).toFixed(2)+"M"}, {field: "METRICS.HEAP_MEMORY_USED", title: jQuery.i18n.prop("text.memory.heap-used"), width: "160px", formatter: (value, row, index) => (value/(1024*1024)).toFixed(2)+"M"}, {field: "METRICS.MAXIMUM_THREAD_COUNT", title: jQuery.i18n.prop("text.thread.peak-count"), width: "160px"}, {field: "METRICS.CURRENT_THREAD_COUNT", title: jQuery.i18n.prop("text.thread.current-count"), width: "160px"}]],
			rowStyle: (row, index) => {return  {classes: "", css: {height: "45px"}};}, url: "/clustering/search?SECRET_KEY="+$("#secret_key").val(),
			onPostBody: () => {$("div.fixed-table-container").css("border-radius", "0px");  $("table  th>div.th-inner,table  td").css("line-height", "29px");  $("th,.fixed-table-header  table").css("border-bottom-style", "none");}} );
	}
);