///////////////////////////////////////////////////////////////////////////////
// WindowUtil class
///////////////////////////////////////////////////////////////////////////////

WindowUtil = {};

WindowUtil.attachEvent = function(event, callback) {
	if (window.addEventListener) {
    	window.addEventListener(event, callback, true);
	} else {
    	if (window.attachEvent) {
        	window.attachEvent("on" + event, callback);
    	}
	}
}


///////////////////////////////////////////////////////////////////////////////
// Focus class that set the focus to the first input at page load
///////////////////////////////////////////////////////////////////////////////

Focus = {};

Focus.setInitialFocus = function() {

    // look for inputs with CSS class "invalid" first
    var inputs = document.getElementsByTagName("input");
    for (var i = 0; i < inputs.length; i++) {
        var input = inputs.item(i);
        if (input.className.indexOf("invalid") >= 0) {
            input.focus();
            input.select();
            return;
        }
    }

    for (var i = 0; i < inputs.length; i++) {
        var input = inputs.item(i);
        if (input.type == "text"
            || input.type == "file"
            || input.type == "password"
            || input.type == "radio"
            || input.type == "checkbox") {

            // skip it if it's disabled or has the nofocus CSS class
            if (!input.disabled && input.className.indexOf("nofocus") == -1) {
                input.focus();
                return;
            }
        }
    }
}

WindowUtil.attachEvent("load", Focus.setInitialFocus);


///////////////////////////////////////////////////////////////////////////////
// Resizer class to resize an element
///////////////////////////////////////////////////////////////////////////////

Resizer = {}

Resizer.resize  = function(id, offsetBottom) {
	var height=window.innerWidth;//Firefox
	if (document.body.clientHeight)
		{
		height=document.body.clientHeight;//IE
	}
	//resize the html according to the size of the window 
	document.getElementById(id).style.height=parseInt(height-offsetBottom-document.getElementById(id).offsetTop)+"px";
}

function resizeWizardContent() {
	var table = $('#wizardFormData .wizard-content table:first-child');
	var wizardContent = $('#wizardFormData .wizard-content');
	var resizeHeight = parseInt(document.body.clientHeight-170-wizardContent.get()[0].offsetTop);
	
	if ( table == null || table.height() < resizeHeight ) {
		wizardContent.height(resizeHeight + 'px');
	} else {
		wizardContent.removeAttr('style');
	}
}

function resizeModalFeedback() {
	var feedback = $('div.wicket-modal div.onyx ul.feedback');
	var modalContent = $('div.wicket-modal div.onyx div.w_content > div');
	modalContent.height(feedback.height() + 80);
}

//////////////////////////////////////////////////////////////////////
// JQuery Layout
//////////////////////////////////////////////////////////////////////
var outerLayout, innerLayout; // a var is required because this page utilizes: myLayout.allowOverflow() method

//
// ON PAGE LOAD
//
$(document).ready(function () {
	outerLayout = $('body').layout( baseLayoutSettings );
	//innerLayout = $('body > div.ui-layout-center > div#contentWrapper > wicket:child > wicket:extend > div.stage').layout( stageLayoutSettings );
});



//
// Base Layout Settings
//
var baseLayoutSettings = {
	name: "baseLayout",
	defaults: {
		spacing_open: 0,
		spacing_closed:0
	},
	north: {
	    minSize: 0,
		size: "auto"
	},
	center: {
		onresize: "innerLayout.resizeAll"	// resize INNER LAYOUT when center pane resizes
	}
}

//
// Stage Layout Settings
//
var stageLayoutSettings = {
	name: "stageLayout",
	defaults: {
		spacing_open: 0,
		spacing_closed:0
	}
}