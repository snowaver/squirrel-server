require.config
(
	{
		baseUrl: "/",
		paths:
		{
			"jquery": "lib/jquery/jquery-3.3.1", "jquery-md5": "lib/jquery/md5/jquery.md5", "jquery-cookie": "lib/jquery/cookie/jquery.cookie", "jquery-i18n-properties": "lib/jquery/i18n-properties/jquery.i18n.properties", "bootstrap": "lib/bootstrap/bootstrap-3.3.7", "bootstrap-table": "lib/bootstrap/table/bootstrap-table", "sweetalert-2": "lib/sweetalert-2/sweetalert2.all",  
			"monitoring/signin": "javascript/monitoring/signin", "monitoring/main": "javascript/monitoring/main", "user/list": "javascript/user/list", "clustering/list": "javascript/clustering/list"
		}
		,shim:
		{
			"bootstrap": ["jquery"], "jquery-md5": ["jquery"], "jquery-cookie": ["jquery"], "jquery-i18n-properties": ["jquery"], "bootstrap-table": ["bootstrap"], "monitoring/signin": ["bootstrap", "jquery-md5", "sweetalert-2", "jquery-cookie", "jquery-i18n-properties"], "monitoring/main": ["bootstrap"], "user/list": ["bootstrap-table", "jquery-i18n-properties", "jquery-cookie"], "clustering/list": ["bootstrap-table", "jquery-i18n-properties", "jquery-cookie"]
		}
	}
);