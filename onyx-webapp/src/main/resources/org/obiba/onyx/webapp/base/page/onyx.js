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
	var elementToResize = document.getElementById(id);
	if(elementToResize != null) {
		elementToResize.style.height=parseInt(WindowUtil.pageHeight()-offsetBottom-elementToResize.offsetTop)+"px";
	}
}

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

Resizer.resizeConsentFrame = function() {
    Resizer.resize('consentFrame',180);
}

Resizer.resizeAll = function() {
  Resizer.resizeParticipantAndBarcodeAssignmentWrapper();
}

WindowUtil.attachEvent('load', Resizer.resizeAll);
WindowUtil.attachEvent('resize', Resizer.resizeAll);

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
// NavigationUtil class
//////////////////////////////////////////////////////////////////////

NavigationUtil = {}

NavigationUtil.previous = function() {
  var previousButton = $('input.obiba-nav-prev');
  if (previousButton) {
    previousButton.click();
  }
}
	    
NavigationUtil.admin = function() {
  var adminButton = $('input.obiba-quartz-nav-admin');
  if (adminButton) {
    adminButton.click();
  }
}

NavigationUtil.next = function() {
  var nextButton = $('input.obiba-nav-next');
  if (nextButton) {
    nextButton.click();
  }
}

NavigationUtil.begin = function() {
  var beginButton = $('input.obiba-button.begin, #beginButton');
  if (beginButton) {
    beginButton.click();
  }
}

NavigationUtil.end = function() {
  var endButton = $('input.obiba-button.end, #endButton');
  if (endButton) {
    endButton.click();
  }
}

NavigationUtil.finish = function() {
  var finishButton = $('input.obiba-nav-finish');
  if (finishButton) {
    finishButton.click();
  }
}

NavigationUtil.closeWindow = function() {
  var closeButton = $('input.obiba-button-close');
  if (closeButton) {
    closeButton.click();
  }
}
NavigationUtil.closeWindow = function() {
  var cancelButton = $('input.obiba-button-cancel');
  var closeButton = $('input.obiba-button-close');  
  if(closeButton.size()>0) {
    closeButton.click();
  } else if(cancelButton.size()>0) {
    cancelButton.click();
  }
}

//////////////////////////////////////////////////////////////////////
// Participant search navigation bar styling
//////////////////////////////////////////////////////////////////////
function styleParticipantSearchNavigationBar() {	
	$('#newNavigatorLabel').remove();
	$('#newNavigator').remove();
	$('.entityTable tr.navigation div.navigatorLabel > span').attr( 'id', 'newNavigatorLabel' );
	$('.entityTable tr.navigation div.navigator > span').attr( 'id', 'newNavigator' );
	$('.entityTable tr.navigation div.navigator > span').insertBefore('div#participantPageSearchResults h1.results-list');
	$('.entityTable tr.navigation div.navigatorLabel > span').insertBefore('div#participantPageSearchResults h1.results-list');
	$('.entityTable tr.navigation').empty();
}

//////////////////////////////////////////////////////////////////////
// Workstation Instruments list navigation bar styling
//////////////////////////////////////////////////////////////////////
function styleWorkstationNavigationBar() {
	$('#newNavigatorLabel').remove();
	$('#newNavigator').remove();
	$('.entityTable tr.navigation div.navigatorLabel > span').attr( 'id', 'newNavigatorLabel' );
	$('.entityTable tr.navigation div.navigator > span').attr( 'id', 'newNavigator' );
	$('.entityTable tr.navigation div.navigator > span').insertBefore('div#workstationInstrument h1.results-list');
	$('.entityTable tr.navigation div.navigatorLabel > span').insertBefore('div#workstationInstrument h1.results-list');
	$('.entityTable tr.navigation').empty();
}

//////////////////////////////////////////////////////////////////////
// Experimental Condition and Instrument Calibration Table navigation bar styling. Moves the paging navigation from the
// line below the table title to the far right of the table title.
// parentId = id of parent container element. A div for example.
//////////////////////////////////////////////////////////////////////
function styleOnyxEntityListNavigationBar(parentId) {
    $('#newNavigatorLabel' + parentId).remove();
    $('#newNavigator' + parentId).remove();
    $('#' + parentId + ' .entityTable tr.navigation div.navigatorLabel > span').attr( 'id', 'newNavigatorLabel' + parentId );
    $('#' + parentId + ' .entityTable tr.navigation div.navigatorLabel > span').addClass( 'newNavigatorLabel' );
    $('#' + parentId + ' .entityTable tr.navigation div.navigator > span').attr( 'id', 'newNavigator' + parentId );
    $('#' + parentId + ' .entityTable tr.navigation div.navigator > span').addClass( 'newNavigator' );
    $('#' + parentId + ' .entityTable tr.navigation div.navigatorLabel > span').appendTo('#' + parentId + ' div .command');
    $('#' + parentId + ' .entityTable tr.navigation div.navigator > span').appendTo('#' + parentId + ' div .command');
    $('#' + parentId + ' .entityTable tr.navigation').empty();
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
    minSize: 44,
		size: 44
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

//////////////////////////////////////////////////////////////////////
// Edit Sample Dialog Styling
//////////////////////////////////////////////////////////////////////
function styleSelectedTubeRemark() { 
	$(".editSamplePanel select").children('not(option:selected)').removeClass('ui-state-highlight');
	$(".editSamplePanel select").children('option:selected').addClass('ui-state-highlight');
}

function addOnyxWizardBehavior() {
  /* Navigation hotkeys */
  $(document).bind('keydown', {combi:'-', disableInInput: true}, 
					function(evt) {
          				NavigationUtil.previous();
        			}, 
        			function(evt, jTarget, elem) {
          				if (jTarget.is("input[type=text]") || jTarget.is("textarea")
   			  				|| elem.is("input[type=text]") || elem.is("textarea")||
   			  				Wicket.Window.get() != null) {
                			return true;
          				}        
        			});
		
  $(document).bind('keydown', {combi:'+', disableInInput: true}, 
  					function(evt) { 
  						NavigationUtil.next();
        			}, 
        			function(evt, jTarget, elem) {
          				if (jTarget.is("input[type=text]") || jTarget.is("textarea")
   			  				|| elem.is("input[type=text]") || elem.is("textarea")||
   			  				Wicket.Window.get() != null) {
                			return true;
          				}        
        			});
        			
  $(document).bind('keydown', 'home',
                    function (evt){ 
                      NavigationUtil.begin(); 
                      return false; 
                    }
                  );

        			
  $(document).bind('keydown', 'end',
                    function (evt){ 
                      NavigationUtil.end(); 
                      return false; 
                    }
                  );
        			
  $(document).bind('keydown', {combi:'f', disableInInput: true}, 
  					function(evt) { 
  						NavigationUtil.finish();
        			}, 
        			function(evt, jTarget, elem) {
          				if (jTarget.is("input[type=text]") || jTarget.is("textarea")
   			  				|| elem.is("input[type=text]") || elem.is("textarea")||
   			  				Wicket.Window.get() != null) {
                			return true;
          				}        
        			});
        			
  $(document).bind('keydown', {combi:'a', disableInInput: true}, 
  					function(evt) { 
  						NavigationUtil.admin();
        			}, 
        			function(evt, jTarget, elem) {
          				if (jTarget.is("input[type=text]") || jTarget.is("textarea")
   			  				||  elem.is("input[type=text]") || elem.is("textarea")||
   			  				Wicket.Window.get() != null ) {
                			return true;
          				}        
        			}); 
        			
  $(document).bind('keydown', {combi:'esc', disableInInput: true}, 
  					function(evt) { 
  						NavigationUtil.closeWindow();
        			},
        			function(evt, jTarget, elem) {
          				return false;        
        			});        			       			
		
  WindowUtil.attachEvent("load", Resizer.resizeWizard);
  WindowUtil.attachEvent("resize", Resizer.resizeWizard);
}


