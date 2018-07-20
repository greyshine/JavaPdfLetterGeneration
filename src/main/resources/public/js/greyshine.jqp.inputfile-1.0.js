(function ( $ ) {
	
	var inputFileChanged = function(evt) {
		
		var theFiles = evt.currentTarget.files;
		
		for(var i=0, l=theFiles.length; i < l ; i++ ) {
			
			var theFile = theFiles[i];
			
			var theReader = new FileReader();
			theReader.onload = function(inEvt) {
            	
           	 evt.callbackFunction( inEvt.currentTarget.result, this );
				
            }.bind( theFile );
            
            // Read in the image file as a data URL.
            theReader.readAsDataURL( theFile );
		}
	
	};
	
	/**
	 * http://stackoverflow.com/a/15809374/845117
	 * 
	 * function( text:base64, file:file )
	 */
	$.fn.fileDropped = function( inCallbackFunction ) {
	
		for( var i=0, l=this.length; i < l; i++ ) {
    		
			$(this[i]).bind('dragover dragleave', function(event) {
				event.stopPropagation()
				event.preventDefault()
			});
			
    		$(this[i]).bind('drop', function(evt) { 
    			
    			evt.stopPropagation()
                evt.preventDefault()

                // Get all files that are dropped
                var theFiles = evt.originalEvent.target.files || evt.originalEvent.dataTransfer.files;
                
                for(var i=0, l=theFiles.length; i < l ; i++ ) {
        			
        			var theFile = theFiles[i];
        			
        			var theReader = new FileReader();
        			theReader.onload = function(inEvt) {
                    	
        				inCallbackFunction( inEvt.currentTarget.result, this );

        			}.bind( theFile );
                    
                    // Read in the image file as a data URL.
                    theReader.readAsDataURL( theFile );
        		}
                
    		} );
    	}
	};
	
	/**
	 * function( text:base64, file:file )
	 */
    $.fn.fileSelected = function( inFunction ) {
    	
    	if ( typeof(inFunction) != 'function' ) { return; }
    	
    	for( var i=0, l=this.length; i < l; i++ ) {
    		
    		$(this[i]).change( function(evt) { evt.callbackFunction = inFunction; inputFileChanged(evt); } )	
    	}

        return this;
    };
 
}( jQuery ));