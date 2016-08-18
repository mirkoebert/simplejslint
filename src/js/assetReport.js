$(document).ready(function() {
	function getQueryVariable(variable) {
		var query = window.location.search.substring(1);
		var vars = query.split("&");
		for (var i=0;i<vars.length;i++) {
			var pair = vars[i].split("=");
			if(pair[0] == variable){return pair[1];}
		}
		return(false);
	}

	function initTableFilter() {
		var numberFormatParameter = getQueryVariable('numberFormat')
		var defaultNumberFormat =  numberFormatParameter ? numberFormatParameter : 'EU';
		var tfBaseConfig = {
			base_path: './modules/tablefilter/',
			filters_row_index: 1,
			auto_filter: true,
			auto_filter_delay: 100
		}
		var cssConfig = {
			col_number_format: [
				// artefact, team, Lines of Code, Size, 
				// Min size, Min gzip size, Warnings, Errors,
				// Media Query rules, Breakpunkt M rules, Breakpunkt L rules, Breakpunkt XL rules,
				// Media Query bytes, Breakpunkt M bytes, Breakpunkt L bytes, Breakpunkt XL bytes
	            null, null, defaultNumberFormat, defaultNumberFormat,
	            defaultNumberFormat, defaultNumberFormat, defaultNumberFormat, defaultNumberFormat,
	            defaultNumberFormat, defaultNumberFormat, defaultNumberFormat, defaultNumberFormat,
	            defaultNumberFormat, defaultNumberFormat, defaultNumberFormat, defaultNumberFormat
	        ],
		}
		var jsConfig = {
			col_number_format: [
				// artefact, team, Lines of Code, Size,
				// Min size, Min gzip size, Count eval, Count new, 
				// Count with, jQuery $( LocatorCalls, jQuery $. FunctionCalls, document.write
				// Count for..in, Count return null
	            null, null, defaultNumberFormat, defaultNumberFormat,
	            defaultNumberFormat, defaultNumberFormat, defaultNumberFormat, defaultNumberFormat,
	            defaultNumberFormat, defaultNumberFormat, defaultNumberFormat, defaultNumberFormat,
	            defaultNumberFormat, defaultNumberFormat
	        ],
		}
		document.querySelectorAll('table.inputFileTable.css').forEach( function(table) {
			var tfConfig = Object.assign({}, tfBaseConfig, cssConfig);
			var tf = new TableFilter(table, tfConfig);
			tf.init();
		});
		document.querySelectorAll('table.inputFileTable.js').forEach( function(table) {
			var tfConfig = Object.assign({}, tfBaseConfig, jsConfig);
			var tf = new TableFilter(table, tfConfig);
			tf.init();
		});
	}

	function initDataTable() {
		// Setup - add a text input to each header cell
	    $('table.inputFileTable thead.tableFilter th').each( function () {
	        var title = $(this).text();
	        $(this).html( '<input type="text" placeholder="Search '+title+'" />');
	    } );

	    var tables = $('table.inputFileTable').DataTable( {
	    	paging: false
	    });

	    // Apply the search
	    var tables2 = $.fn.dataTable.tables()
	    tables2.forEach( function(value) {
	    	var table = $(value.id).DataTable()
	    	var columns = table.columns()
	    	columns.every( function () {
		        var column = this;
		 
		        $( 'input', this.header() ).on( 'keyup change', function () {
		            if ( column.search() !== this.value ) {
		                column
		                    .search( this.value )
		                    .draw();
		            }
		        } );
			} );
	    } );	    
	}

	initTableFilter();
} );