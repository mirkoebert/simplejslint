$(document).ready(function() {
	function initTableFilter() {
		var tf;
		var tfConfig = {
			base_path: './modules/tablefilter/',
			filters_row_index: 1,
			auto_filter: true,
			auto_filter_delay: 100
		}
		document.querySelectorAll('table.inputFileTable').forEach( function(table) {
			tf = new TableFilter(table, tfConfig);
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