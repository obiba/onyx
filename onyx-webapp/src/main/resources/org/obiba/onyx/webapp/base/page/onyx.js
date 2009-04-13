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

WindowUtil.pageHeight = function() {
	var pageHeight=window.innerWidth;//Firefox
	if (document.body.clientHeight) {
		pageHeight=document.body.clientHeight;//IE
	}
	
	return pageHeight;
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
	//resize the html according to the size of the window 
	document.getElementById(id).style.height=parseInt(WindowUtil.pageHeight()-offsetBottom-document.getElementById(id).offsetTop)+"px";
}

// Resize the content wrapper to fill the window.
Resizer.resizeContentWrapper = function() {
    var pageHeader = $('.ui-layout-north');
    var pageHeaderHeight = 43;
    if (pageHeader) {
      pageHeaderHeight = pageHeader.height();
    }

    var pageFooter = $('#pageFooter');
    var pageFooterHeight = 40;
    if (pageFooter) {
      pageFooterHeight = pageFooter.height();
    }
    //alert('header='+pageHeaderHeight+' footer='+pageFooterHeight);
	Resizer.resize('contentWrapper', pageHeaderHeight + pageFooterHeight + 7);
}
WindowUtil.attachEvent("load", Resizer.resizeContentWrapper);
WindowUtil.attachEvent("resize", Resizer.resizeContentWrapper);

// Resize the participantAndBarcodeAssignmentWrapper, based on the current size of the content wrapper.
Resizer.resizeParticipantAndBarcodeAssignmentWrapper = function() {
  var participantAndBarcodeAssignmentWrapper = $('#participantAndBarcodeAssignmentWrapper');
  if (participantAndBarcodeAssignmentWrapper) {
    var contentWrapper = $('#contentWrapper');
    if (contentWrapper) {
      participantAndBarcodeAssignmentWrapper.height(contentWrapper.height() - 150);
    }
  }
}
WindowUtil.attachEvent("load", Resizer.resizeParticipantAndBarcodeAssignmentWrapper);
WindowUtil.attachEvent("resize", Resizer.resizeParticipantAndBarcodeAssignmentWrapper);

Resizer.resizeWizard = function() {
	var wizardFooter = $('#wizardFooter');
	var footerHeight = 60;
	if (wizardFooter) {
		if (wizardFooter.outerHeight()) {
			footerHeight = wizardFooter.outerHeight();
		}
	}
	//alert('wizardFooter='+footerHeight);
	
	var wizardContent = $('#wizardContent');
	//alert('offset=' + wizardContent.offset().top + ' wizardFooter='+footerHeight);
	var resizeHeight = parseInt(WindowUtil.pageHeight()-(50 + footerHeight)-wizardContent.offset().top);
	//alert("resizeHeight: " + resizeHeight);
	
    wizardContent.height(resizeHeight + 'px');
	//alert("wizardContent.height: "+wizardContent.height());
}

Resizer.resizeMenu = function() {
    var menuWrapper = $('#menuWrapper');
    if (menuWrapper) {
	    var menuItems = $('#menuWrapper .obiba-menu li');   
	    if (menuItems.length != 0) {	        
	        var menuWidth = 0;	        
	        for (var i=0; i<menuItems.length; i++) {
	            computedWidth = document.defaultView.getComputedStyle(menuItems[i], "").getPropertyValue("width");
	            if (computedWidth.indexOf('px') != -1) {
	                menuWidth += parseFloat(computedWidth.substring(0, computedWidth.length-2));
	            }
	        }
	        menuWrapper.width(Math.ceil(menuWidth) + 'px');
        }
    }
}
WindowUtil.attachEvent("load", Resizer.resizeMenu);

Resizer.resizeConsentFrame = function() {
    Resizer.resize('consentFrame',180);
}

function resizeModalFeedback() {
	var feedback = $('div.wicket-modal div.onyx ul.feedback');
	var modalContent = $('div.wicket-modal div.onyx div.w_content > div');
	modalContent.height(feedback.height() + 120);
}

function resizeNumericPad() {
	var feedback = $('div.wicket-modal div.onyx ul');
	var modalContent = $('div.wicket-modal div.onyx div.w_content > div');
	modalContent.height(feedback.height() + 405);
}

//////////////////////////////////////////////////////////////////////
// Participant search navigation bar styling
//////////////////////////////////////////////////////////////////////
function styleParticipantSearchNavigationBar() {	
	$('#newNavigatorLabel').remove();
	$('#newNavigator').remove();
	$('.entityTable tr.navigation div.navigatorLabel > span').attr( 'id', 'newNavigatorLabel' );
	$('.entityTable tr.navigation div.navigator > span').attr( 'id', 'newNavigator' );		
	$('.entityTable tr.navigation div.navigatorLabel > span').insertBefore('div#participantPageSearchResults h1.results-list');
	$('.entityTable tr.navigation div.navigator > span').insertBefore('div#participantPageSearchResults h1.results-list');
	$('.entityTable tr.navigation').empty();
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
	    minSize: 43,
		size: "auto"
	}
}

//////////////////////////////////////////////////////////////////////
// Content Region Styling
//////////////////////////////////////////////////////////////////////

$(document).ready(function () {
	$('.obiba-content-region').each(function() {
	  $(this).addClass('ui-corner-all');
	});
});

//////////////////////////////////////////////////////////////////////
// Button Styling
//////////////////////////////////////////////////////////////////////

$(document).ready(function () {
	$('.obiba-button').each(function() {
	  $(this).addClass('ui-corner-all');
	});
});
