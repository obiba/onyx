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

if (window.addEventListener) {
    window.addEventListener("load", Focus.setInitialFocus, true);
} else {
    if (window.attachEvent) {
        window.attachEvent("onload", Focus.setInitialFocus);
    }
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
		size: 63
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