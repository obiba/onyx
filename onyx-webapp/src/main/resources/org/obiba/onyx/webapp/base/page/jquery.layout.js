(function($) {
/*
 * jquery.layout 1.0.0
 *
 * Copyright (c) 2008 
 *   Fabrizio Balliano (http://www.fabrizioballiano.net)
 *   Kevin Dalman (http://allpro.net)
 *
 * Dual licensed under the GPL (http://www.gnu.org/licenses/gpl.html)
 * and MIT (http://www.opensource.org/licenses/mit-license.php) licenses.
 *
 * $Date: 2008-11-15 23:46:22 +0100 (sab, 15 nov 2008) $
 * $Rev: 173 $
 * 
 * NOTE: For best code readability, view this with a fixed-space font and tabs equal to 4-chars
 */
$.fn.layout = function (opts) {

/*
 * ###########################
 *   WIDGET CONFIG & OPTIONS
 * ###########################
 */

	// DEFAULTS for options
	var 
		prefix = "ui-layout-" // prefix for ALL selectors and classNames
	,	defaults = { //	misc default values
			paneClass:				prefix+"pane"		// ui-layout-pane
		,	resizerClass:			prefix+"resizer"	// ui-layout-resizer
		,	togglerClass:			prefix+"toggler"	// ui-layout-toggler
		,	buttonClass:			prefix+"button"		// ui-layout-button
		,	contentSelector:		"."+prefix+"content"// ui-layout-content
		,	contentIgnoreSelector:	"."+prefix+"ignore"	// ui-layout-mask 
		}
	;

	// DEFAULT PANEL OPTIONS - CHANGE IF DESIRED
	var options = {
		name:						""		// FUTURE REFERENCE - not used right now
	,	defaults: { // default options for 'all panes' - will be overridden by 'per-pane settings'
			applyDefaultStyles: 	false	// apply basic styles directly to resizers & buttons? If not, then stylesheet must handle it
		,	closable:				true	// pane can open & close
		,	resizable:				true	// when open, pane can be resized 
		,	slidable:				true	// when closed, pane can 'slide' open over other panes - closes on mouse-out
		,	paneClass:				defaults.paneClass		// border-Pane		- default: 'ui-layout-pane'
		,	contentSelector:		defaults.contentSelector		// INNER div/element to auto-size so only it scrolls, not the entire pane!
		,	contentIgnoreSelector:	defaults.contentIgnoreSelector	// elem(s) to 'ignore' when measuring 'content'
		,	togglerClass:			defaults.togglerClass	// Toggler Button	- default: 'ui-layout-toggler'
		,	buttonClass:			defaults.buttonClass	// CUSTOM Buttons	- default: 'ui-layout-button-toggle/-open/-close/-pin'
		,	resizerClass:			defaults.resizerClass	// Resizer Bar		- default: 'ui-layout-resizer'
		,	resizerDragOpacity:		1		// option for ui.draggable
		,	minSize:				50		// when manually resizing a pane
		,	maxSize:				0		// ditto, 0 = no limit
		,	spacing_open:			6		// space between pane and adjacent panes - when pane is 'open'
		,	spacing_closed:			6		// ditto - when pane is 'closed'
		,	togglerLength_open:		50		// Length = WIDTH of toggler button on north/south edges - HEIGHT on east/west edges
		,	togglerLength_closed: 	50		// 100% OR -1 means 'full height/width of resizer bar' - 0 means 'hidden'
		,	togglerAlign_open:		"center"	// top/left, bottom/right, center, OR...
		,	togglerAlign_closed:	"center"	// 1 => nn = offset from top/left, -1 => -nn == offset from bottom/right
		,	togglerTip_open:		"Close"
		,	togglerTip_closed:		"Open"
		,	resizerTip:				"Resize"
		,	sliderTip:				"Slide Open" // resizer-bar triggers 'sliding' when pane is closed
		,	sliderCursor:			"pointer"	// cursor when resizer-bar will trigger 'sliding'
		,	slideTrigger_open:		"click"		// click, dblclick, mouseover
		,	slideTrigger_close:		"mouseout"	// click, mouseout
		,	hideTogglerOnSlide:		true		// when pane is slid-open, should the toggler show?
		,	raisePaneZindexOnHover:	false		// will bind allowOverflow() utility to pane.onMouseOver
		,	initClosed:				false		// true = init pane as 'closed'
		,	initHidden: 			false 		// true = init pane as 'hidden' - no resizer or spacing
		,	onopen:					""			// CALLBACK when pane is Opened
		,	onclose:				""			// CALLBACK when pane is Closed
		,	onresize:				""			// CALLBACK when pane is Manually Resized
		,	fxName:					"slide" 	// ('none' or blank), slide, drop, scale
		,	fxSpeed:				"normal"	// slow, normal, fast, 200, nnn
		,	fxSettings:				{}			// can be passed, eg: { duration: 500, easing: "bounceInOut" }
		/*	DO NOT set 'default' values for these options - but is OK if user passes them
		,	paneSelector:			""			// MUST be pane-specific!
		,	size:					100			// inital size of pane when layout 'created'
		,	resizerCursor:			""			// cursor when over resizer-bar
		*/
		}
	,	north: {
			paneSelector:			"."+prefix+"north" // default = .ui-layout-north
		,	size:					"auto"
		,	resizerCursor:			"n-resize"
		}
	,	south: {
			paneSelector:			"."+prefix+"south" // default = .ui-layout-south
		,	size:					"auto"
		,	resizerCursor:			"s-resize"
		}
	,	east: {
			paneSelector:			"."+prefix+"east" // default = .ui-layout-east
		,	size:					200
		,	resizerCursor:			"e-resize"
		}
	,	west: {
			paneSelector:			"."+prefix+"west" // default = .ui-layout-west
		,	size:					200
		,	resizerCursor:			"w-resize"
		}
	,	center: {
			paneSelector:			"."+prefix+"center" // default = .ui-layout-center
		}

	};


	// STATIC, INTERNAL CONFIG - DO NOT CHANGE THIS!
	var config = {
		allPanes:		"north,south,east,west,center"
	,	borderPanes:	"north,south,east,west"
	,	zIndex: { // set z-index values here
			resizer_normal:	1		// normal z-index for resizer-bars
		,	pane_normal:	2		// normal z-index for panes
		,	sliding:		100	// applied to both the pane and its resizer when a pane is 'slid open'
		,	resizing:		10000	// applied to the CLONED resizer-bar when being 'dragged'
		,	animation:		10000	// applied to the pane when being animated - not applied to the resizer
		}
	,	fxDefaults: { // LIST *ALL PREDEFINED FX* HERE, even if has no settings
			slide: {
				all:	{}
			,	north:	{ direction: "up"	}
			,	south:	{ direction: "down"	}
			,	east:	{ direction: "right"	}
			,	west:	{ direction: "left"	}
			}
		,	drop: {
				all:	{}
			,	north:	{ direction: "up"	}
			,	south:	{ direction: "down"	}
			,	east:	{ direction: "right"	}
			,	west:	{ direction: "left"	}
			}
		,	scale: {}
		}
	,	resizers: {
			cssReq: {
				position: 	"absolute"
			,	padding: 	0
			,	margin: 	0
			,	fontSize:	"1px"
			,	textAlign:	"left" // to counter-act "center" alignment!
			,	overflow: 	"hidden" // keep toggler button from overflowing
			,	zIndex: 	1
			}
		,	cssDef: { // DEFAULT CSS - applied if: options.PANE.applyDefaultStyles=true
				background: "#DDD"
			,	border:		"none"
			}
		}
	,	togglers: {
			cssReq: {
				position: 	"absolute"
			,	display: 	"block"
			,	padding: 	0
			,	margin: 	0
			,	fontSize:	"1px"
			,	cursor: 	"pointer"
			,	zIndex: 	1
			}
		,	cssDef: { // DEFAULT CSS - applied if: options.PANE.applyDefaultStyles=true
				background: "#AAA"
			,	border: "1px solid #777"
			}
		}
	,	content: {
			cssReq: {
				overflow:	"auto"
			}
		,	cssDef: {}
		}
	,	defaults: { // defaults for ALL panes - overridden by 'per-pane settings' below
			cssReq: {
				position: 	"absolute"
			,	overflow: 	"auto"
			,	margin:		0
			,	zIndex: 	2
			}
		,	cssDef: {
				padding:	"10px"
			,	background:	"#fff"
			,	border:		"1px solid #bbb"
			}
		}
	,	north: {
			edge:			"top"
		,	sizeType:		"height"
		,	dir:			"horz"
		,	cssReq: {
				top: 		0
			,	bottom: 	"auto"
			,	left: 		0
			,	right: 		0
			,	width: 		"auto"
			//	height: 	DYNAMIC
			}
		}
	,	south: {
			edge:			"bottom"
		,	sizeType:		"height"
		,	dir:			"horz"
		,	cssReq: {
				top: 		"auto"
			,	bottom: 	0
			,	left: 		0
			,	right: 		0
			,	width: 		"auto"
			//	height: 	DYNAMIC
			}
		}
	,	east: {
			edge:			"right"
		,	sizeType:		"width"
		,	dir:			"vert"
		,	cssReq: {
				left: 		"auto"
			,	right: 		0
			,	top: 		"auto" // DYNAMIC
			,	bottom: 	"auto" // DYNAMIC
			,	height: 	"auto"
			//	width: 		DYNAMIC
			}
		}
	,	west: {
			edge:			"left"
		,	sizeType:		"width"
		,	dir:			"vert"
		,	cssReq: {
				left: 		0
			,	right: 		"auto"
			,	top: 		"auto" // DYNAMIC
			,	bottom: 	"auto" // DYNAMIC
			,	height: 	"auto"
			//	width: 		DYNAMIC
			}
		}
	,	center: {
			dir:			"center"
		,	cssReq: {
				left: 		"auto" // DYNAMIC
			,	right: 		"auto" // DYNAMIC
			,	top: 		"auto" // DYNAMIC
			,	bottom: 	"auto" // DYNAMIC
			,	height: 	"auto"
			,	width: 		"auto"
			}
		}
	};


/*
 * ###########################
 *  INTERNAL HELPER FUNCTIONS
 * ###########################
 */

	var 
		altEdge = {
			top:	"bottom"
		,	bottom: "top"
		,	left:	"right"
		,	right:	"left"
		}
	,	altSide = {
			north:	"south"
		,	south:	"north"
		,	east: 	"west"
		,	west: 	"east"
		}
	;

	/**
	 * isStr
	 *
	 * Returns true if passed param is EITHER a simple string OR a 'string object' - otherwise returns false
	 */
	var isStr = function (o) {
		if (typeof o == "string")
			return true;
		else if (typeof o == "object") {
			var match = o.constructor.toString().match(/string/i); 
 			return (match !== null);
		}
		else
			return false;
	};

	/**
	 * str
	 *
	 * Returns a simple string if the passed param is EITHER a simple string OR a 'string object',
	 *  else returns the original object
	 */
	var str = function (o) {
		if (typeof o == "string" || isStr(o)) return $.trim(o); // trim converts 'String object' to a simple string
		else return o;
	};

	/**
	 * min / max
	 *
	 * Alias for Math.min/.max to simplify coding
	 */
	var min = function (x,y) { return Math.min(x,y); };
	var max = function (x,y) { return Math.max(x,y); };

	/**
	 * transformData
	 *
	 * Processes the options passed in and transforms them into the format used by layout()
	 * Missing keys are added, and converts the data if passed in 'flat-format' (no sub-keys)
	 * In flat-format, pane-specific-settings are prefixed like: north__optName  (2-underscores)
	 * To update config.fxDefaults, options MUST use nested-keys format, with a fxDefaults key
	 *
	 * @callers  initOptions()
	 * @params  JSON  d  Data/options passed by user - may be a single level or nested levels
	 * @returns JSON  Creates a data struture that perfectly matches 'options', ready to be imported
	 */
	var transformData = function (d) {
		var json = { defaults:{}, north:{}, south:{}, east:{}, west:{}, center:{} };
		d = d || {};
		if (d.fxDefaults || d.defaults || d.north || d.south || d.west || d.east || d.center)
			return $.extend( json, d  ); // already in json format, but add any missing keys
		// convert 'flat' to 'nest-keys' format - also handles 'empty' user-options
		$.each( d, function (key,val) {
			a = key.split("__");
			json[ a[1] ? a[0] : "defaults" ][ a[1] ? a[1] : a[0] ] = val;
		});
		return json;
	};

	/**
	 * execCallback
	 *
	 * Executes a Callback function after a trigger event, like resize, open or close
	 *
	 * @param String  pane   This is passed only so we can pass the 'pane object' to the callback
	 * @param String  v_fn  Accepts a function name, OR a comma-delimited array: [0]=function name, [1]=argument
	 */
	var execCallback = function (pane, v_fn) {
		if (!v_fn) return;
		var fn;
		try {
			if (typeof v_fn == "function")
				fn = v_fn;	
			else if (typeof v_fn != "string")
				return;
			else if (v_fn.indexOf(",") > 0) {
				// function name cannot contain a comma, so must be a function name AND a 'name' parameter
				var 
					args = v_fn.split(",")
				,	fn = eval(args[0])
				;
				if (typeof fn=="function" && args.length > 1) fn(args[1]); // pass the argument parsed from 'list'
				return;
			}
			else // just the name of an external function?
				fn = eval(v_fn);

			if (typeof fn=="function") fn( pane, $Ps[pane], o[pane], o.name ); // pass the pane and 'pane object - may be useful!
		}
		catch (ex) {}
	};

	/**
	 * cssNum
	 *
	 * Returns the 'current CSS value' for an element - returns 0 if property does not exist
	 *
	 * @callers  Called by many methods
	 * @param jQuery  $Elem  Must pass a jQuery object - first element is processed
	 * @param String  property  The name of the CSS property, eg: top, width, etc.
	 * @returns Variant  Usually is used to get an integer value for position (top, left) or size (height, width)
	 */
	var cssNum = function ($E, prop) {
		var
			val = 0
		,	hidden = false
		,	visibility = ""
		;
		if (!$.browser.msie) { // IE CAN read dimensions of 'hidden' elements - FF CANNOT
			if ($.curCSS($E[0], "display", true) == "none") {
				hidden = true;
				visibility = $.curCSS($E[0], "visibility", true); // SAVE current setting
				$E.css({ display: "block", visibility: "hidden" }); // show element 'invisibly' so we can measure it
			}
		}

		val = parseInt($.curCSS($E[0], prop, true), 10) || 0;

		if (hidden) { // WAS hidden, so put back the way it was...
			$E.css({ display: "none" });
			if (visibility && visibility != "hidden")
				$E.css({ visibility: visibility }); // reset 'visibility'
		}

		return val;
	};

	/**
	 * cssW / cssH / cssSize
	 *
	 * Contains logic to check boxModel & browser, and return the correct width/height for the current browser/doctype
	 *
	 * @callers  initPanes(), sizeMidPanes(), initHandles(), sizeHandles()
	 * @param Variant  elem  Can accept a 'pane' (east, west, etc) OR a DOM object OR a jQuery object
	 * @param Integer  outerWidth/outerHeight  (optional) Can pass a width, allowing calculations BEFORE element is resized
	 * @returns Integer  Returns the innerHeight of the elem by subtracting padding and borders
	 *
	 * @TODO  May need to add additional logic to handle more browser/doctype variations - so far tested only under IE7
	 */
	var cssW = function (e, outerWidth) {
		var $E = e;
		if (isStr(e)) {
			e = str(e);
			$E = $Ps[e];
		}

		// a 'calculated' outerHeight can be passed so borders and/or padding are removed if needed
		if (outerWidth <= 0)
			return 0;
		else if (!(outerWidth>0))
			outerWidth = isStr(e) ? getPaneSize(e) : $E.outerWidth();

		if (!$.boxModel)
			return outerWidth;

		else // strip border and padding size from outerWidth to get CSS Width
			return outerWidth
				- cssNum($E, "paddingLeft")		
				- cssNum($E, "paddingRight")
				- ($.curCSS($E[0], "borderLeftStyle", true) == "none" ? 0 : cssNum($E, "borderLeftWidth"))
				- ($.curCSS($E[0], "borderRightStyle", true) == "none" ? 0 : cssNum($E, "borderRightWidth"))
			;
	};
	var cssH = function (e, outerHeight) {
		var $E = e;
		if (isStr(e)) {
			e = str(e);
			$E = $Ps[e];
		}

		// a 'calculated' outerHeight can be passed so borders and/or padding are removed if needed
		if (outerHeight <= 0)
			return 0;
		else if (!(outerHeight>0))
			outerHeight = (isStr(e)) ? getPaneSize(e) : $E.outerHeight();

		// TODO: may need to add additional conditions for Opera or specific doctypes
		if (!$.boxModel)
			return outerHeight;

		else // strip border and padding size from outerHeight to get CSS Height
			return outerHeight
				- cssNum($E, "paddingTop")
				- cssNum($E, "paddingBottom")
				- ($.curCSS($E[0], "borderTopStyle", true) == "none" ? 0 : cssNum($E, "borderTopWidth"))
				- ($.curCSS($E[0], "borderBottomStyle", true) == "none" ? 0 : cssNum($E, "borderBottomWidth"))
			;
	};
	var cssSize = function (pane, outerSize) {
		if (pane=="north" || pane=="south")
			return cssH(pane, outerSize);
		else // pane = east or west
			return cssW(pane, outerSize);
	};

	/**
	 * getPaneSize
	 *
	 * Calculates the current 'size' (width or height) of a border-pane - optionally with 'pane spacing' added
	 *
	 * @returns Integer  Returns EITHER Width for east/west panes OR Height for north/south panes - adjusted for boxModel & browser
	 */
	var getPaneSize = function (pane, inclSpace) {
		var 
			$P	= $Ps[pane]
		,	s	= o[pane]
		,	oSp	= (inclSpace ? s.spacing_open : 0)
		,	cSp	= (inclSpace ? s.spacing_closed : 0)
		;
		if (!$P || s.isHidden)
			return 0;
		else if (s.isClosed || (s.isSliding && inclSpace))
			return cSp;
		else if (c[pane].dir == "horz")
			return $P.outerHeight() + oSp;
		else // dir == "vert"
			return $P.outerWidth() + oSp;
	};

	var getPaneMinMaxSizes = function (pane) {
		var 
			edge			= c[pane].edge
		,	dir				= c[pane].dir
		,	s				= o[pane]
		,	$P				= $Ps[pane]
		,	$altPane		= $Ps[ altSide[pane] ]
		,	paneSpacing		= s.spacing_open
		,	altPaneSpacing	= o[ altSide[pane] ].spacing_open
		,	altPaneSize		= (!$altPane ? 0 : (dir=="horz" ? $altPane.outerHeight() : $altPane.outerWidth()))
		,	containerSize	= (dir=="horz" ? cDims.innerHeight : cDims.innerWidth)
		//	limitSize prevents this pane from 'overlapping' opposite pane - even if opposite pane is currently closed
		,	limitSize		= containerSize - paneSpacing - altPaneSize - altPaneSpacing
		,	minSize			= s.minSize || 0
		,	maxSize			= Math.min(s.maxSize || 9999, limitSize)
		,	minPos, maxPos	// used to set resizing limits
		;
		switch (pane) {
			case "north":	minPos = cDims.offsetTop + minSize;
							maxPos = cDims.offsetTop + maxSize;
							break;
			case "west":	minPos = cDims.offsetLeft + minSize;
							maxPos = cDims.offsetLeft + maxSize;
							break;
			case "south":	minPos = cDims.offsetTop + cDims.innerHeight - maxSize;
							maxPos = cDims.offsetTop + cDims.innerHeight - minSize;
							break;
			case "east":	minPos = cDims.offsetLeft + cDims.innerWidth - maxSize;
							maxPos = cDims.offsetLeft + cDims.innerWidth - minSize;
							break;
		}
		return { minSize: minSize, maxSize: maxSize, minPosition: minPos, maxPosition: maxPos };
	};

	/**
	 * getPaneDims
	 *
	 * Returns data for setting the size/position of center pane. Date is also used to set Height for east/west panes
	 *
	 * @returns JSON  Returns a hash of all dimensions: top, bottom, left, right, (outer) width and (outer) height
	 */
	var getPaneDims = function () {
		var dims = {
			top:	getPaneSize("north", true) // true = include 'spacing' value for p
		,	bottom:	getPaneSize("south", true)
		,	left:	getPaneSize("west", true)
		,	right:	getPaneSize("east", true)
		,	width:	0
		,	height:	0
		}
		,	d = cDims // alias
		;
		with (dims) {
			width 	= d.innerWidth - left - right;
			height 	= d.innerHeight - bottom - top;
			// now add the 'container border/padding' to get final positions - relative to the container
			top		+= d.top;
			bottom	+= d.bottom;
			left	+= d.left;
			right	+= d.right;
		}
		return dims;
	};

	/**
	 * getContainerDims
	 *
	 * Returns data for setting size of container element (normally body or a div). Set only by create() and onWindowResize
	 *
	 * @callers  create(), onWindowResize() -- data is cached in a module-var between updates
	 * @returns JSON  Returns a hash of all dimensions: top, bottom, left, right, outerWidth, outerHeight - PLUS boxModel boolean
	 */
	var getContainerDims = function () {
		// pass the container object - returns INNER width/height - minus BOTH border & padding
		var d = {
			top:	0
		,	bottom:	0
		,	left:	0
		,	right:	0
		,	outerWidth:  $Container.outerWidth()
		,	outerHeight: $Container.outerHeight()
		};

		$.each("Left,Right,Top,Bottom".split(","), function () {
			var side = str(this);
			d["border" +side]	= cssNum($Container, "border"+side+"Width");
			d["padding"+side]	= cssNum($Container, "padding"+side);
			d["offset" +side]	= d["border"+side] + d["padding"+side];
			// if BOX MODEL, then adjust all dimensions accordingly
			if ($.boxModel)
				d[side.toLowerCase()] = d["padding"+side]; // position is less PADDING ONLY - ignore borderWidth
		});

		d.innerWidth  = d.outerWidth; // init
		d.innerHeight = d.outerHeight;
		if ($.boxModel) {
			d.innerWidth  -= (d.offsetLeft + d.offsetRight);
			d.innerHeight -= (d.offsetTop  + d.offsetBottom);
		}

		return d;
	};


	var setTimer = function (pane, action, fn, ms) {
		var
			Layout = window.layout = window.layout || {}
		,	Timers = Layout.timers = Layout.timers || {}
		,	layoutID = "_"+ 999 // TODO: Generate & save a random number in initOptions - and GET IT here
		,	name = layoutID +"_"+ pane +"_"+ action // UNIQUE NAME for every pane/action
		;
		if (Timers[name]) return; // timer already set!
		else Timers[name] = setTimeout(fn, ms);
	};

	var clearTimer = function (pane, action) {
		var
			Layout = window.layout = window.layout || {}
		,	Timers = Layout.timers = Layout.timers || {}
		,	layoutID = "_"+ 999 // TODO: Generate & save a random number in initOptions - and GET IT here
		,	name = layoutID +"_"+ pane +"_"+ action // UNIQUE NAME for every pane/action
		;
		if (Timers[name]) {
			clearTimeout( Timers[name] );
			delete Timers[name];
			return true;
		}
		else
			return false;
	};


/*
 * ###########################
 *   INITIALIZATION METHODS
 * ###########################
 */

	/**
	 * create
	 *
	 * Initialize the layout - called automatically whenever an instance of layout is created
	 *
	 * @callers  NEVER explicity called
	 * @returns  An object pointer to the instance created
	 */
	var create = function () {
		var isBodyContainer = false;
		try { // format html/body if this is a full page layout
			if ($Container[0].tagName == "BODY") {
				isBodyContainer = true;
				$("html").css({
					height:		"100%"
				,	overflow:	"hidden"
				});
				$("body").css({
					position:	"relative"
				,	height:		"100%"
				,	overflow:	"hidden"
				,	margin:		0
				,	padding:	0		// TODO: test whether body-padding could be handled?
				,	border:		"none"	// a body-border creates problems because it cannot be measured!
				});
			}
		} catch (ex) {}

		// initialize config/options
		initOptions();

		// get layout-container dimensions (updated when necessary)
		cDims = getContainerDims();

		// initialize all objects
		initPanes();		// size & position all panes
		initHandles();		// create and position all resize bars & togglers buttons
		initResizable();	// activate resizing on all panes where resizable=true
		sizeContent("all");	// AFTER panes & handles have been initialized, size 'content' divs
	
		// TODO: Update code so EVERY 'layout instance' is bound to window.onresize
		if (isBodyContainer)
			// bind resizeAll() for 'this layout instance' to window.resize event
			$(window).resize(function () {
				if (window.timerLayout) clearTimeout(window.timerLayout);
				window.timerLayout = null;
				if ($.browser.msie) // use a delay for IE because the resize event fires repeatly
					window.timerLayout = setTimeout(resizeAll, 100);
					// TODO: CANNOT use same name, 'timerLayout', for MULTIPLE layout instances!
				else // most other browsers have a built-in delay before firing the resize event
					resizeAll(); // resize all layout elements NOW!
			});
	};

	/**
	 * initOptions
	 *
	 * Build final CONFIG and OPTIONS data
	 *
	 * @callers  create()
	 */
	var initOptions = function () {
		// simplify logic by making sure passed 'opts' var has basic keys
		opts = transformData( opts );

		// see if a 'layout name' was specified
		o.name = opts.name || opts.defaults.name || "";

		// remove default options that should be set 'per-pane' - and remove 'name' if exists
		$.each("paneSelector,resizerCursor,name".split(","),
			function (idx,key) { delete opts.defaults[key]; } // is OK if key does not exist
		);

		// update fxDefaults, if case user passed some
		var fx = c.fxDefaults; // alias
		if (opts.fxDefaults) $.extend( fx, opts.fxDefaults );

		// first merge all config & options for the 'center' pane
		c.center = $.extend( true, {}, c.defaults, c.center );
		$.extend( o.center, opts.center );
		// Most 'default options' do not apply to 'center', so add only those that do
		var o_Center = $.extend( true, {}, o.defaults, opts.defaults, o.center ); // TEMP data
		$.each("paneClass,contentSelector,contentIgnoreSelector,applyDefaultStyles,raisePaneZindexOnHover".split(","),
			function (idx,key) { o.center[key] = o_Center[key]; }
		);

		// loop to create/import a COMPLETE set of options for EACH border-pane
		$.each(c.borderPanes.split(","), function(i,pane) {
			// apply 'pane-defaults' to CONFIG.PANE
			c[pane] = $.extend( true, {}, c.defaults, c[pane] );
			// apply 'pane-defaults' +  user-options to OPTIONS.PANE
			o[pane] = $.extend( true, {}, o.defaults, o[pane], opts.defaults, opts[pane] );

			// make sure we have base-classes
			var s = o[pane]; // alias pane-options
			if (!s.paneClass)		s.paneClass		= defaults.paneClass;
			if (!s.resizerClass)	s.resizerClass	= defaults.resizerClass;
			if (!s.togglerClass)	s.togglerClass	= defaults.togglerClass;

			// create FINAL & UNIQUE fx options for 'each pane', ie: options.PANE.fxName/fxSpeed/fxSettings
			var fxName = s.fxName;
			if (fxName != "none") {
				if (!fxName || !$.effects || !$.effects[fxName] || (!fx[fxName] && !s.fxSettings))
					s.fxName = "none"; // effect not loaded, OR undefined FX AND fxSettings not passed
				else if (fx[fxName])
					// ADD 'missing keys' to s.fxSettings from config.fxSettings
					s.fxSettings = $.extend( {}, fx[fxName].all, fx[fxName][pane], s.fxSettings );
			}
		});

		// LAST, update options.defaults so they are saved and can be read from outside
		$.extend( o.defaults, opts.defaults );
	};

	/**
	 * initPanes
	 *
	 * Initialize module objects, styling, size and position for all panes
	 *
	 * @callers  create()
	 */
	var initPanes = function () {
		// NOTE: do north & south FIRST so we can measure their height - do center LAST
		$.each(c.allPanes.split(","), function() {
			var 
				pane	= str(this)
			,	s		= o[pane]
			,	fx		= s.fx
			,	dir		= c[pane].dir
			//	if s.size is not > 0, then we will use MEASURE the pane and use that as it's 'size'
			,	size	= s.size=="auto" || isNaN(s.size) ? 0 : s.size
			,	minSize	= s.minSize || 1
			,	maxSize	= s.maxSize || 9999
			,	spacing	= s.spacing_open || 0
			,	isIE6	= ($.browser.msie && $.browser.version < 7)
			,	CSS		= {}
			,	$P, $C
			;
			$Cs[pane] = false; // init

			$P = $Ps[pane] = $Container.children(s.paneSelector);
			if (!$P.length) {
				$Ps[pane] = false; // logic
				return true; // SKIP to next
			}

			// add basic classes & attributes
			$P
				.attr("pane", pane) // add pane-identifier
				.addClass( s.paneClass +" "+ s.paneClass+"-"+pane ) // default = "ui-layout-pane ui-layout-pane-west" - may be a dupe of 'paneSelector'
			;

			// init pane-logic vars, etc.
			if (pane != "center") {
				s.isClosed  = false; // true = pane is closed
				s.isSliding = false; // true = pane is currently open by 'sliding' over adjacent panes
				s.isResizing= false; // true = pane is in process of being resized
				s.isHidden	= false; // true = pane is hidden - no spacing, resizer or toggler is visible!
				s.noRoom	= false; // true = pane 'automatically' hidden due to insufficient room - will unhide automatically
				// create special keys for internal use
				c[pane].pins = [];   // used to track and sync 'pin-buttons' for border-panes
			}

			CSS = $.extend({ visibility: "visible", display: "block" }, c.defaults.cssReq, c[pane].cssReq );
			if (s.applyDefaultStyles) $.extend( CSS, c.defaults.cssDef, c[pane].cssDef ); // cosmetic defaults
			$P.css(CSS); // add base-css BEFORE 'measuring' to calc size & position
			CSS = {};	// reset var

			// set css-position to account for container borders & padding
			switch (pane) {
				case "north": 	CSS.top 	= cDims.top;
								CSS.left 	= cDims.left;
								CSS.right	= cDims.right;
								break;
				case "south": 	CSS.bottom	= cDims.bottom;
								CSS.left 	= cDims.left;
								CSS.right 	= cDims.right;
								break;
				case "west": 	CSS.left 	= cDims.left; // top, bottom & height set by sizeMidPanes()
								break;
				case "east": 	CSS.right 	= cDims.right; // ditto
								break;
				case "center":	// top, left, width & height set by sizeMidPanes()
			}

			if (dir == "horz") { // north or south pane
				if (size === 0 || size == "auto") {
					$P.css({ height: "auto" });
					size = $P.outerHeight();
				}
				size = max(size, minSize);
				size = min(size, maxSize);
				size = min(size, cDims.innerHeight - spacing);
				CSS.height = max(1, cssH(pane, size));
				// make sure minSize is sufficient to avoid errors
				s.minSize = max(minSize, size - CSS.height + 1); // = pane.outerHeight when css.height = 1px
				// handle IE6
				//if (isIE6) CSS.width = cssW($P, cDims.innerWidth);
				$P.css(CSS); // apply size & position
			}
			else if (dir == "vert") { // east or west pane
				if (size === 0 || size == "auto") {
					$P.css({ width: "auto", float: "left" }); // float = FORCE pane to auto-size
					size = $P.outerWidth();
					$P.css({ float: "none" }); // RESET
				}
				size = max(size, minSize);
				size = min(size, maxSize);
				size = min(size, cDims.innerWidth - spacing);
				CSS.width = max(1, cssW(pane, size));
				// make sure s.minSize is sufficient to avoid errors
				s.minSize = max(minSize, size - CSS.width + 1); // = pane.outerWidth when css.width = 1px
				$P.css(CSS); // apply size - top, bottom & height set by sizeMidPanes
				sizeMidPanes(pane, null, true); // true = onInit
			}
			else if (pane == "center") {
				$P.css(CSS); // top, left, width & height set by sizeMidPanes...
				sizeMidPanes("center", null, true); // true = onInit
			}

			// close or hide the pane if specified in settings
			if (s.initClosed && !s.isClosable) {
				$P.hide().addClass("closed");
				s.isClosed = true;
			}
			else if (s.initHidden || s.initClosed) {
				hide(pane, true); // will be completely invisible - no resizer or spacing
				s.isHidden = true;
			}
			else
				$P.addClass("open");

			// check option for auto-handling of pop-ups & drop-downs
			if (s.raisePaneZindexOnHover)
				$P.hover( allowOverflow, resetOverflow );

			/*
			 *	see if this pane has a 'content element' that we need to auto-size
			 */
			if (s.contentSelector) {
				$C = $Cs[pane] = $P.children(s.contentSelector+":first"); // match 1-element only
				if (!$C.length) {
					$Cs[pane] = false;
					return true; // SKIP to next
				}
				$C.css( c.content.cssReq );
				if (s.applyDefaultStyles) $C.css( c.content.cssDef ); // cosmetic defaults
				// NO PANE-SCROLLING when there is a content-div
				$P.css({ overflow: "hidden" });
			}
		});
	};

	/**
	 * initHandles
	 *
	 * Initialize module objects, styling, size and position for all resize bars and toggler buttons
	 *
	 * @callers  create()
	 */
	var initHandles = function () {
		// create toggler DIVs for each pane, and set object pointers for them, eg: $R.north = north toggler DIV
		$.each(c.borderPanes.split(","), function() {
			var 
				pane	= str(this)
			,	s		= o[pane]
			,	rClass	= s.resizerClass
			,	tClass	= s.togglerClass
			,	$P		= $Ps[pane]
			;
			$Rs[pane] = false; // INIT
			$Ts[pane] = false;

			if (!$P || (!s.closable && !s.resizable)) return; // pane does not exist - skip

			var 
				edge	= c[pane].edge
			,	isOpen	= $P.is(":visible")
			,	spacing	= (isOpen ? s.spacing_open : s.spacing_closed)
			,	_side	= "-"+ pane // used for classNames
			,	_state	= (isOpen ? "-open" : "-closed") // used for classNames
			,	$R, $T
			;
			// INIT RESIZER BAR
			$R = $Rs[pane] = $("<div></div>");
	
			if (isOpen && s.resizable)
				; // this is handled by initResizable
			else if (!isOpen && s.slidable)
				$R.attr("title", s.sliderTip).css("cursor", s.sliderCursor);
	
			$R
				// if paneSelector is an ID, then create a matching ID for the resizer, eg: "#paneLeft" => "paneLeft-resizer"
				.attr("id", (s.paneSelector.substr(0,1)=="#" ? s.paneSelector.substr(1) + "-resizer" : ""))
				.attr("resizer", pane) // so we can read this from the resizer
				.css(c.resizers.cssReq) // add base/required styles
				// POSITION of resizer bar - allow for container border & padding
				.css(edge, cDims[edge] + getPaneSize(pane))
				// ADD CLASSNAMES - eg: class="resizer resizer-west resizer-open"
				.addClass( rClass +" "+ rClass+_side +" "+ rClass+_state +" "+ rClass+_side+_state )
				.appendTo($Container) // append DIV to container
			;
			 // ADD VISUAL STYLES
			if (s.applyDefaultStyles)
				$R.css(c.resizers.cssDef);

			if (s.closable) {
				// INIT COLLAPSER BUTTON
				$T = $Ts[pane] = $("<span></span>");
				$T
					// if paneSelector is an ID, then create a matching ID for the resizer, eg: "#paneLeft" => "paneLeft-toggler"
					.attr("id", (s.paneSelector.substr(0,1)=="#" ? s.paneSelector.substr(1) + "-toggler" : ""))
					.css(c.togglers.cssReq) // add base/required styles
					.attr("title", (isOpen ? s.togglerTip_open : s.togglerTip_closed))
					.click(function(evt){ toggle(pane); evt.stopPropagation(); })
					.mouseover(function(evt){ evt.stopPropagation(); }) // prevent resizer event
					// ADD CLASSNAMES - eg: class="toggler toggler-west toggler-west-open"
					.addClass( tClass +" "+ tClass+_side +" "+ tClass+_state +" "+ tClass+_side+_state )
					.appendTo($R) // append SPAN to resizer DIV
				;
				 // ADD BASIC VISUAL STYLES
				if (s.applyDefaultStyles)
					$T.css(c.togglers.cssDef);

				if (!isOpen) bindStartSlidingEvent(pane, true); // will enable if options.PANE.isSliding = true
			}

		});

		// SET ALL HANDLE SIZES & LENGTHS
		sizeHandles("all", true); // true = onInit
	};

	/**
	 * initResizable
	 *
	 * Add resize-bars to all panes that specify it in options
	 *
	 * @dependancies  $.fn.resizable - will abort if not found
	 * @callers  create()
	 */
	var initResizable = function () {
		var
			draggingAvailable = (typeof $.fn.draggable == "function")
		,	minPosition, maxPosition, edge // set in start()
		;

		$.each(c.borderPanes.split(","), function() {
			var 
				pane	= str(this)
			,	s		= o[pane]
			;
			if (!draggingAvailable || !$Ps[pane] || !s.resizable) {
				s.resizable = false;
				return true; // skip to next
			}

			var 
				rClass				= s.resizerClass
			//	'drag' classes are applied to the ORIGINAL resizer-bar while dragging is in process
			,	dragClass			= rClass+"-drag"			// resizer-drag
			,	dragPaneClass		= rClass+"-"+pane+"-drag"	// resizer-north-drag
			//	'dragging' class is applied to the CLONED resizer-bar while it is being dragged
			,	draggingClass		= rClass+"-dragging"		// resizer-dragging
			,	draggingPaneClass	= rClass+"-"+pane+"-dragging"// resizer-north-dragging
			,	draggingClassSet	= false 					// logic var
			,	$P 					= $Ps[pane]
			,	$R					= $Rs[pane]
			;

			if (!s.isClosed)
				$R
					.attr("title", s.resizerTip)
					.css("cursor", s.resizerCursor) // n-resize, s-resize, etc
				;

			$R.draggable({
				containment:	$Container[0] // limit resizing to layout container
			,	axis:			(c[pane].dir=="horz" ? "y" : "x") // limit resizing to horz or vert axis
			,	delay:			200
			,	distance:		3
			//	basic format for helper - style it using class: .ui-draggable-dragging
			,	helper:			"clone"
			,	opacity:		s.resizerDragOpacity
			,	zIndex:			c.zIndex.resizing

			,	start: function (e, ui) {
					s.isResizing = true; // prevent pane from closing while resizing
					clearTimer(pane, "closeSlider"); // just in case already triggered
					$R.addClass( dragClass +" "+ dragPaneClass ); // add drag classes
					draggingClassSet = false; // reset logic var - see drag()
					// SET RESIZING LIMITS - used in drag()
					var limits = getPaneMinMaxSizes(pane);
					var resizerWidth = (pane=="east" || pane=="south" ? s.spacing_open : 0);
					minPosition = limits.minPosition - resizerWidth;
					maxPosition = limits.maxPosition - resizerWidth;
					edge = (c[pane].dir=="horz" ? "top" : "left");
				}

			,	drag: function (e, ui) {
					if (!draggingClassSet) { // can only add classes after clone has been added to the DOM
						$(".ui-draggable-dragging")
							.addClass( draggingClass +" "+ draggingPaneClass ) // add dragging classes
							.children().css("visibility","hidden") // hide toggler inside dragged resizer-bar
						;
						draggingClassSet = true;
						// draggable bug!? RE-SET zIndex to prevent E/W resize-bar showing through N/S pane!
						if (o[pane].isSliding) $Ps[pane].css("zIndex", c.zIndex.sliding);
					}
					// CONTAIN RESIZER-BAR TO RESIZING LIMITS
					if		(ui.position[edge] < minPosition) ui.position[edge] = minPosition;
					else if (ui.position[edge] > maxPosition) ui.position[edge] = maxPosition;
				}

			,	stop: function (e, ui) {
					var 
						dragPos	= ui.position
					,	resizerPos
					,	newSize
					;
					$R.removeClass( dragClass +" "+ dragPaneClass ); // remove drag classes
	
					switch (pane) {
						case "north":	resizerPos = dragPos.top; break;
						case "west":	resizerPos = dragPos.left; break;
						case "south":	resizerPos = cDims.outerHeight - dragPos.top - $R.outerHeight(); break;
						case "east":	resizerPos = cDims.outerWidth - dragPos.left - $R.outerWidth(); break;
					}
					// remove container margin from resizer position to get the pane size
					newSize = resizerPos - cDims[ c[pane].edge ];

					sizePane(pane, newSize);

					s.isResizing = false;
				}

			});
		});
	};



/*
 * ###########################
 *       ACTION METHODS
 * ###########################
 */

	/**
	 * hide / show
	 *
	 * Completely 'hides' a pane, including its spacing - as if it does not exist
	 * The pane is not actually 'removed' from the source, so can use 'show' to un-hide it
	 *
	 * @param String  pane   The pane being hidden, ie: north, south, east, or west
	 */
	var hide = function (pane, onInit) {
		var
			s	= o[pane]
		,	$P	= $Ps[pane]
		,	$R	= $Rs[pane]
		;
		if (!$P) return; // pane does not exist
		else if (s.isHidden) return; // already hidden!
		else s.isHidden = true; // set logic var
		s.isSliding = false; // just in case
		// now hide the elements
		if ($R) $R.hide();
		if (onInit) {
			$P.hide(); // no animation when loading page
			s.isClosed = true; // to trigger open-animation on show()
		}
		else close(pane, true); // adjust all panes to fit
	};

	var show = function (pane, openPane) {
		var
			s	= o[pane]
		,	$P	= $Ps[pane]
		,	$R	= $Rs[pane]
		;
		if (!$P) return; // pane does not exist
		else if (!s.isHidden) return; // not hidden!
		else s.isHidden = false; // set logic var
		s.isSliding = false; // just in case
		// now show the elements
		if ($R && s.spacing_open > 0) $R.show();
		if (openPane === false)
			close(pane, true); // true = force
		else
			open(pane); // adjust all panes to fit
	};


	/**
	 * toggle
	 *
	 * Toggles a pane open/closed by calling either open or close
	 *
	 * @param String  pane   The pane being toggled, ie: north, south, east, or west
	 */
	var toggle = function (pane) {
		var s = o[pane];
		if (s.isHidden)
			show(pane); // will call 'open' after unhiding it
		else if (s.isClosed)
			open(pane);
		else
			close(pane);
	};

	/**
	 * close
	 *
	 * Close the specified pane (animation optional), and resize all other panes as needed
	 *
	 * @param String  pane   The pane being closed, ie: north, south, east, or west
	 */
	var close = function (pane, force) {
		var 
			$P		= $Ps[pane]
		,	$R		= $Rs[pane]
		,	$T		= $Ts[pane]
		,	s		= o[pane]
		,	doFX	= (s.fxName != "none") && !s.isClosed
		,	edge	= c[pane].edge
		,	rClass	= s.resizerClass
		,	tClass	= s.togglerClass
		,	_side	= "-"+ pane // used for classNames
		,	_open	= "-open"
		,	_sliding= "-sliding"
		,	_closed	= "-closed"
		;
		if (!$P || (!s.resizable && !s.closable)) return; // invalid request
		else if (!force && s.isClosed) return; // already closed

		s.isClosed = true; // logic

		// sync any 'pin buttons'
		syncPinBtns(pane, false);

		// resize panes adjacent to this one
		if (!s.isSliding) sizeMidPanes(c[pane].dir == "horz" ? "all" : "center");

		// if this pane has a resizer bar, move it now
		if ($R) {
			$R
				.css(edge, cDims[edge]) // move the resizer bar
				.removeClass( rClass+_open +" "+ rClass+_side+_open )
				.removeClass( rClass+_sliding +" "+ rClass+_side+_sliding )
				.addClass( rClass+_closed +" "+ rClass+_side+_closed )
			;
			// DISABLE 'resizing' when closed - do this BEFORE bindStartSlidingEvent
			if (s.resizable)
				$R
					.draggable("disable")
					.css("cursor", "default")
					.attr("title","")
				;
			// if pane has a toggler button, adjust that too
			if ($T) {
				$T
					.removeClass( tClass+_open +" "+ tClass+_side+_open )
					.addClass( tClass+_closed +" "+ tClass+_side+_closed )
					.attr("title", s.togglerTip_closed) // may be blank
				;
			}
			sizeHandles(); // resize 'length' and position togglers for adjacent panes
		}

		// ANIMATE 'CLOSE' - if no animation, then was ALREADY shown above
		if (doFX) {
			lockPaneForFX(pane, true); // need to set left/top so animation will work
			$P.hide( s.fxName, s.fxSettings, s.fxSpeed, function () {
				lockPaneForFX(pane, false); // undo
				if (!s.isClosed) return; // pane was opened before animation finished!
				close_2();
			});
		}
		else {
			$P.hide(); // just hide pane NOW
			close_2();
		}

		// SUBROUTINE
		function close_2 () {
			bindStartSlidingEvent(pane, true); // will enable if options.PANE.isSliding = true
			// see if there is a callback for this pane
			execCallback(pane, s.onclose); // see if there is a callback for this pane
		}
	};

	/**
	 * open
	 *
	 * Open the specified pane (animation optional), and resize all other panes as needed
	 *
	 * @param String  pane   The pane being opened, ie: north, south, east, or west
	 */
	var open = function (pane, slide) {
		var 
			$P		= $Ps[pane]
		,	$R		= $Rs[pane]
		,	$T		= $Ts[pane]
		,	s		= o[pane]
		,	fx		= s.fx
		,	doFX	= (s.fxName != "none") && s.isClosed
		,	edge	= c[pane].edge
		,	rClass	= s.resizerClass
		,	tClass	= s.togglerClass
		,	_side	= "-"+ pane // used for classNames
		,	_open	= "-open"
		,	_closed	= "-closed"
		,	_sliding= "-sliding"
		;
		if (!$P || (!s.resizable && !s.closable)) return; // invalid request
		else if (!s.isClosed && !s.isSliding) return; // already open

		// 'PIN PANE' - stop sliding
		if (s.isSliding && !slide) // !slide = 'open pane normally' - NOT sliding
			bindStopSlidingEvents(pane, false); // will set isSliding=false

		s.isClosed = false; // logic
		s.isHidden = false; // logic

		bindStartSlidingEvent(pane, false); // remove trigger event from resizer-bar

		if (doFX) { // ANIMATE
			lockPaneForFX(pane, true); // need to set left/top so animation will work
			$P.show( s.fxName, s.fxSettings, s.fxSpeed, function() {
				lockPaneForFX(pane, false); // undo
				if (s.isClosed) return; // pane was closed before animation finished!
				open_2(); // continue
			});
		}
		else {// no animation
			$P.show();	// just show pane and...
			open_2();	// continue
		}
		// SUBROUTINE
		function open_2 () {
			// NOTE: if isSliding, then other panes are NOT 'resized'
			if (!s.isSliding) // resize all panes adjacent to this one
				sizeMidPanes(c[pane].dir=="vert" ? "center" : "all");

			// if this pane has a toggler, move it now
			if ($R) {
				$R
					.css(edge, cDims[edge] + getPaneSize(pane)) // move the toggler
					.removeClass( rClass+_closed +" "+ rClass+_side+_closed )
					.addClass( rClass+_open +" "+ rClass+_side+_open )
					.addClass( !s.isSliding ? "" : rClass+_sliding +" "+ rClass+_side+_sliding )
				;
				if (s.resizable)
					$R
						.draggable("enable")
						.css("cursor", s.resizerCursor)
						.attr("title", s.resizerTip)
					;
				else
					$R.css("cursor", "default"); // n-resize, s-resize, etc
				// if pane also has a toggler button, adjust that too
				if ($T) {
					$T
						.removeClass( tClass+_closed +" "+ tClass+_side+_closed )
						.addClass( tClass+_open +" "+ tClass+_side+_open )
						.attr("title", s.togglerTip_open) // may be blank
					;
				}
				sizeHandles("all"); // resize resizer & toggler sizes for all panes
			}

			// resize content every time pane opens - to be sure
			sizeContent(pane);

			// sync any 'pin buttons'
			syncPinBtns(pane, !s.isSliding);

			// see if there is a callback for this pane
			execCallback(pane, s.onopen);
		}
	};


	/**
	 * lockPaneForFX
	 *
	 * Must set left/top on East/South panes so animation will work properly
	 *
	 * @param String  pane  The pane to lock, 'east' or 'south' - any other is ignored!
	 * @param Boolean  doLock  true = set left/top, false = remove
	 */
	var lockPaneForFX = function (pane, doLock) {
		var $P = $Ps[pane];
		if (doLock) {
			$P.css({ zIndex: c.zIndex.animation }); // overlay all elements during animation
			if (pane=="south")
				$P.css({ top: cDims.top + cDims.innerHeight - $P.outerHeight() });
			else if (pane=="east")
				$P.css({ left: cDims.left + cDims.innerWidth - $P.outerWidth() });
		}
		else {
			if (!o[pane].isSliding) $P.css({ zIndex: c.zIndex.pane_normal });
			if (pane=="south")
				$P.css({ top: "auto" });
			else if (pane=="east")
				$P.css({ left: "auto" });
		}
	};


	/**
	 * bindStartSlidingEvent
	 *
	 * Toggle sliding functionality of a specific pane on/off by adding removing 'slide open' trigger
	 *
	 * @callers  open(), close()
	 * @param String  pane  The pane to enable/disable, 'north', 'south', etc.
	 * @param Boolean  enable  Enable or Disable sliding?
	 */
	var bindStartSlidingEvent = function (pane, enable) {
		var 
			s		= o[pane]
		,	$R		= $Rs[pane]
		,	trigger	= s.slideTrigger_open
		;
		if (!$R || !s.slidable) return;
		// make sure we have a valid event
		if (trigger != "click" && trigger != "dblclick" && trigger != "mouseover") trigger = "click";
		$R
			// add or remove trigger event
			[enable ? "bind" : "unbind"](trigger, slideOpen)
			// set the appropriate cursor & title/tip
			.css("cursor", (enable ? s.sliderCursor: "default"))
			.attr("title", (enable ? s.sliderTip : ""))
		;
	};

	/**
	 * bindStopSlidingEvents
	 *
	 * Add or remove 'mouseout' events to 'slide close' when pane is 'sliding' open or closed
	 * Also increases zIndex when pane is sliding open
	 * See bindStartSlidingEvent for code to control 'slide open'
	 *
	 * @callers  slideOpen(), slideClosed()
	 * @param String  pane  The pane to process, 'north', 'south', etc.
	 * @param Boolean  isOpen  Is pane open or closed?
	 */
	var bindStopSlidingEvents = function (pane, enable) {
		var 
			s		= o[pane]
		,	trigger	= s.slideTrigger_close
		,	action	= (enable ? "bind" : "unbind") // can't make 'unbind' work! - see disabled code below
		,	$P		= $Ps[pane]
		,	$R		= $Rs[pane]
		;

		s.isSliding = enable; // logic
		clearTimer(pane, "closeSlider"); // just in case

		// raise z-index when sliding
		$P.css({ zIndex: (enable ? c.zIndex.sliding : c.zIndex.pane_normal) });
		$R.css({ zIndex: (enable ? c.zIndex.sliding : c.zIndex.resizer_normal) });

		// make sure we have a valid event
		if (trigger != "click" && trigger != "mouseout") trigger = "mouseout";

		// when trigger is 'mouseout', must cancel timer when mouse moves between 'pane' and 'resizer'
		if (enable) { // BIND trigger events
			$P.bind(trigger, slideClosed );
			$R.bind(trigger, slideClosed );
			if (trigger = "mouseout") {
				$P.bind("mouseover", cancelMouseOut );
				$R.bind("mouseover", cancelMouseOut );
			}
		}
		else { // UNBIND trigger events
			// TODO: why does unbind of a 'single function' not work reliably?
			//$P[action](trigger, slideClosed );
			$P.unbind(trigger);
			$R.unbind(trigger);
			if (trigger = "mouseout") {
				//$P[action]("mouseover", cancelMouseOut );
				$P.unbind("mouseover");
				$R.unbind("mouseover");
				clearTimer(pane, "closeSlider");
			}
		}

		// SUBROUTINE for mouseout timer clearing
		function cancelMouseOut (evt) {
			clearTimer(pane, "closeSlider");
			evt.stopPropagation();
		}
	};

	var slideOpen = function () {
		var pane = $(this).attr("resizer"); // attr added by initHandles
		if (o[pane].isClosed) { // skip if already open!
			bindStopSlidingEvents(pane, true); // pane is opening, so BIND trigger events to close it
			open(pane, true); // true = slide - ie, called from here!
		}
	};

	var slideClosed = function () {
		var
			$E = $(this)
		,	pane = $E.attr("pane") || $E.attr("resizer")
		,	s = o[pane]
		;
		if (s.isClosed || s.isResizing)
			return; // skip if already closed OR in process of resizing
		else if (s.slideTrigger_close == "click")
			close_NOW(); // close immediately onClick
		else // trigger = mouseout - use a delay
			setTimer(pane, "closeSlider", close_NOW, 300); // .3 sec delay

		// SUBROUTINE for timed close
		function close_NOW () {
			bindStopSlidingEvents(pane, false); // pane is being closed, so UNBIND trigger events
			if (!s.isClosed) close(pane); // skip if already closed!
		}
	};


	/**
	 * sizePane
	 *
	 * @callers  initResizable.stop()
	 * @param String  pane   The pane being resized - usually west or east, but potentially north or south
	 * @param Integer  newSize  The new size for this pane - will be validated
	 */
	var sizePane = function (pane, size) {
		var 
			edge	= c[pane].edge
		,	dir		= c[pane].dir
		,	s		= o[pane]
		,	$P		= $Ps[pane]
		,	$R		= $Rs[pane]
		,	limits	= getPaneMinMaxSizes(pane)
		;
		size = max(size, limits.minSize);
		size = min(size, limits.maxSize);

		// move the resizer bar and resize the pane
		$R.css( edge, size + cDims[edge] );
		$P.css( c[pane].sizeType, max(1, cssSize(pane, size)) );

		// resize all the adjacent panes, and adjust their toggler buttons
		if (!s.isSliding) sizeMidPanes(dir=="horz" ? "all" : "center");
		sizeHandles();
		sizeContent(pane);
		s.size = size; // update options - not necessary, but in case we want to persist it
		execCallback(pane, s.onresize);
	};

	/**
	 * sizeMidPanes
	 *
	 * @callers  create(), open(), close(), onWindowResize()
	 */
	var sizeMidPanes = function (panes, overrideDims, onInit) {
		if (!panes || panes == "all") panes = "east,west,center";

		var dims = getPaneDims();
		if (overrideDims) $.extend( dims, overrideDims );

		$.each(panes.split(","), function() {
			if (!$Ps[this]) return; // NO PANE - skip
			var 
				pane	= str(this)
			,	s		= o[pane]
			,	$P		= $Ps[pane]
			,	$R		= $Rs[pane]
			,	hasRoom	= true
			,	CSS		= {}
			;

			if (pane == "center") {
				dims = getPaneDims(); // REFRESH Dims because may have just 'unhidden' East or West pane after a 'resize'
				CSS = $.extend( {}, dims ); // COPY ALL of the dims
				CSS.width  = max(1, cssW(pane, CSS.width));
				CSS.height = max(1, cssH(pane, CSS.height));
				hasRoom = (CSS.width > 1 && CSS.height > 1);
				/*
				 * Extra CSS for IE6 or IE7 in Quirks-mode - add 'width' to NORTH/SOUTH panes
				 * Normally these panes have only 'left' & 'right' positions so pane auto-sizes
				 */
				if ($.browser.msie && (!$.boxModel || $.browser.version < 7)) {
					$Ps.north.css({ width: cssW($Ps.north, cDims.innerWidth) });
					$Ps.south.css({ width: cssW($Ps.south, cDims.innerWidth) });
				}
			}
			else { // for east and west, set only the height
				CSS.top = dims.top;
				CSS.bottom = dims.bottom;
				CSS.height = max(1, cssH(pane, dims.height));
				hasRoom = (CSS.height > 1);
			}

			if (hasRoom) {

				$P.css(CSS);
				if (s.noRoom) {
					s.noRoom = false;
					if (s.isHidden) return;
					if (!s.isClosed) $P.show(); // in case was previously hidden due to NOT hasRoom
					if ($R) $R.show();
				}
				if (!onInit) {
					sizeContent(pane);
					execCallback(pane, s.onresize);
				}
			}
			else if (!s.noRoom) { // no room for pane, so just hide it (if not already)
				s.noRoom = true;
				if (s.isHidden) return;
				$P.hide();
				if ($R) $R.hide();
			}
		});
	};


	var sizeContent = function (panes) {
		if (!panes || panes == "all") panes = c.allPanes;

		$.each(panes.split(","), function() {
			if (!$Cs[this]) return; // NO CONTENT - skip
			var 
				pane	= str(this)
			,	ignore	= o[pane].contentIgnoreSelector
			,	$P		= $Ps[pane]
			,	$C		= $Cs[pane]
			,	e_C		= $C[0]		// DOM element
			,	height	= cssH($P);	// init to pane.innerHeight
			;
			$P.children().each(function(idx) {
				if (this == e_C) return; // Content elem - skip
				var $E = $(this);
				if (!ignore || !$E.is(ignore))
					height -= $E.outerHeight();
			});
			if (height > 0)
				height = cssH($C, height);
			if (height < 1)
				$C.hide(); // no room for content!
			else
				$C.css({ height: height }).show();
		});
	};


	/**
	 * sizeHandles
	 *
	 * Called every time a pane is opened, closed, or resized to slide the togglers to 'center' and adjust their length if necessary
	 *
	 * @callers  initHandles(), open(), close(), resizeAll()
	 */
	var sizeHandles = function (panes, onInit) {
		if (!panes || panes == "all") panes = c.borderPanes;

		$.each(panes.split(","), function() {
			var 
				pane	= str(this)
			,	s		= o[pane]
			,	$P		= $Ps[pane]
			,	$R		= $Rs[pane]
			,	$T		= $Ts[pane]
			;
			if (!$P || !$R || (!s.resizable && !s.closable)) return; // skip

			var 
				dir			= c[pane].dir
			,	state		= (s.isClosed ? "_closed" : "_open")
			,	spacing		= s["spacing"+ state]
			,	togAlign	= s["togglerAlign"+ state]
			,	togLen		= s["togglerLength"+ state]
			,	paneLen
			,	offset
			,	CSS = {}
			;
			if (spacing == 0) {
				$R.hide();
				return;
			}
			else if (!s.noRoom && !s.isHidden) // skip if resizer was hidden for any reason
				$R.show(); // in case was previously hidden

			// Resizer Bar is ALWAYS same width/height of pane it is attached to
			if (dir == "horz") { // north/south
				paneLen = $P.outerWidth();
				$R.css({
					width:	max(1, cssW($R, paneLen)) // account for borders & padding
				,	height:	max(1, cssH($R, spacing)) // ditto
				,	left:	cssNum($P, "left") // + cDims.left  TODO: FF glitch after resizing window
				});
			}
			else { // east/west
				paneLen = $P.outerHeight();
				$R.css({
					height:	max(1, cssH($R, paneLen)) // account for borders & padding
				,	width:	max(1, cssW($R, spacing)) // ditto
				,	top:	cDims.top + getPaneSize("north", true)
				//,	top:	cssNum($Ps["center"], "top")
				});
				
			}

			if ($T) {
				if (togLen == 0 || (s.isSliding && s.hideTogglerOnSlide)) {
					$T.hide(); // always HIDE the toggler when 'sliding'
					return;
				}
				else
					$T.show(); // in case was previously hidden

				if (!(togLen > 0) || togLen == "100%" || togLen > paneLen) {
					togLen = paneLen;
					offset = 0;
				}
				else { // calculate 'offset' based on options.PANE.togglerAlign_open/closed
					if (typeof togAlign == "string") {
						switch (togAlign) {
							case "top":
							case "left":	offset = 0;
											break;
							case "bottom":
							case "right":	offset = paneLen - togLen;
											break;
							case "middle":
							case "center":
							default:		offset = Math.floor((paneLen - togLen) / 2); // 'default' catches typos
						}
					}
					else { // togAlign = number
						var x = parseInt(togAlign); //
						if (togAlign >= 0) offset = x;
						else offset = paneLen - togLen + x; // NOTE: x is negative!
					}
				}

				if (dir == "horz") // north/south
					$T.css({
						width:	max(0, cssW($T, togLen))  // account for borders & padding
					,	height:	max(1, cssH($T, spacing)) // ditto
					,	left:	offset // 0 // TODO: FIX ME!  POSITION the toggler
					});
				else // east/west
					$T.css({
						height:	max(0, cssH($T, togLen))  // account for borders & padding
					,	width:	max(1, cssW($T, spacing)) // ditto
					,	top:	offset // POSITION the toggler
					});
			}

			// DONE measuring and sizing this resizer/toggler, so can be 'hidden' now
			if (onInit && s.initHidden) {
				$R.hide();
				if ($T) $T.hide();
			}
		});
	};


	/**
	 * resizeAll
	 *
	 * @callers  window.onresize(), resizeNestLayout()
	 * @param String  pane   The pane being resized - usually west or east, but potentially north or south
	 */
	var resizeAll = function () {
		cDims = getContainerDims(); // IMPORTANT - update container size var
		sizeMidPanes("all");
		sizeHandles("all"); // reposition the toggler elements
	};


/*
 * ###########################
 *     UTILITY METHODS
 *   called externally only
 * ###########################
 */

	function allowOverflow (elem) {
		if (this && this.tagName) elem = this; // BOUND to element
		var $P;
		if (typeof elem=="string")
			$P = $Ps[elem];
		else {
			if ($(elem).hasClass("ui-layout-pane")) $P = $(elem);
			else $P = $(elem).parents(".ui-layout-pane:first");
		}
		if (!$P.length) return; // INVALID

		var
			pane	= $P.attr("pane")
		,	s		= o[pane]
		;
		// if pane is already raised, then reset it before doing it again!
		// this would happen if allowOverflow is attached to BOTH the pane and an element 
		if (c[pane].cssSaved)
			resetOverflow(pane); // reset previous CSS before continuing

		// if pane is raised by sliding or resizing, or it's closed, then abort
		if (s.isSliding || s.isResizing || s.isClosed) {
			c[pane].cssSaved = false;
			return;
		}

		var
			newCSS	= { zIndex: (c.zIndex.pane_normal + 1) }
		,	curCSS	= {}
		,	of		= $P.css("overflow")
		,	ofX		= $P.css("overflowX")
		,	ofY		= $P.css("overflowY")
		;
		// determine which, if any, overflow settings need to be changed
		if (of != "visible") {
			curCSS.overflow = of;
			newCSS.overflow = "visible";
		}
		if (ofX && ofX != "visible" && ofX != "auto") {
			curCSS.overflowX = ofX;
			newCSS.overflowX = "visible";
		}
		if (ofY && ofY != "visible" && ofY != "auto") {
			curCSS.overflowY = ofX;
			newCSS.overflowY = "visible";
		}

		// save the current overflow settings - even if blank!
		c[pane].cssSaved = curCSS;

		// apply new CSS to raise zIndex and, if necessary, make overflow 'visible'
		$P.css( newCSS );

		// make sure the zIndex of all other panes is normal
		$.each(c.allPanes.split(","), function(idx, pane2) {
			if (pane2 != pane) resetOverflow (pane2)
		});

	};

	function resetOverflow (elem) {
		if (this && this.tagName) elem = this; // BOUND to element
		var $P;
		if (typeof elem=="string")
			$P = $Ps[elem];
		else {
			if ($(elem).hasClass("ui-layout-pane")) $P = $(elem);
			else $P = $(elem).parents(".ui-layout-pane:first");
		}
		if (!$P.length) return; // INVALID

		var
			pane	= $P.attr("pane")
		,	s		= o[pane]
		,	CSS		= c[pane].cssSaved || {}
		;
		// reset the zIndex
		if (!s.isSliding && !s.isResizing)
			$P.css("zIndex", c.zIndex.pane_normal);

		// reset Overflow - if necessary
		$P.css( CSS );

		// clear var
		c[pane].cssSaved = false;
	};


	/**
	* getBtn
	*
	* Helper function to validate params received by addButton utilities
	*
	* @param String   selector 	jQuery selector for button, eg: ".ui-layout-north .toggle-button"
	* @param String   pane 		Name of the pane the button is for: 'north', 'south', etc.
	* @returns  If both params valid, the element matching 'selector' in a jQuery wrapper - otherwise 'false'
	*/
	function getBtn(selector, pane, suffix) {
		var
			$E = $(selector)
		,	err = "Error Adding Button \n\nInvalid "
		;
		if (!$E.length) // element not found
			alert(err+"selector: "+ selector);
		else if (c.borderPanes.indexOf(pane) == -1) // invalid 'pane' sepecified
			alert(err+"pane: "+ pane);
		else { // VALID
			var pre	= o[pane].buttonClass;
			$E.addClass( pre + suffix +" "+ pre +"-"+ pane + suffix );
			return $E;
		}
		return false;  // INVALID
	};


	/**
	* addToggleBtn
	*
	* Add a custom Toggler button for a pane
	*
	* @param String   selector 	jQuery selector for button, eg: ".ui-layout-north .toggle-button"
	* @param String   pane 		Name of the pane the button is for: 'north', 'south', etc.
	*/
	function addToggleBtn (selector, pane) {
		var $E = getBtn(selector, pane, "-toggle");
		if ($E)
			$E
				.attr("title", o[pane].isClosed ? "Open" : "Close")
				.click(function (evt) {
					toggle(pane);
					evt.stopPropagation();
				})
			;
	};

	/**
	* addOpenBtn
	*
	* Add a custom Open button for a pane
	*
	* @param String   selector 	jQuery selector for button, eg: ".ui-layout-north .open-button"
	* @param String   pane 		Name of the pane the button is for: 'north', 'south', etc.
	*/
	function addOpenBtn (selector, pane) {
		var $E = getBtn(selector, pane, "-open");
		if ($E)
			$E
				.attr("title", "Open")
				.click(function (evt) {
					open(pane);
					evt.stopPropagation();
				})
			;
	};

	/**
	* addCloseBtn
	*
	* Add a custom Close button for a pane
	*
	* @param String   selector 	jQuery selector for button, eg: ".ui-layout-north .close-button"
	* @param String   pane 		Name of the pane the button is for: 'north', 'south', etc.
	*/
	function addCloseBtn (selector, pane) {
		var $E = getBtn(selector, pane, "-close");
		if ($E)
			$E
				.attr("title", "Close")
				.click(function (evt) {
					close(pane);
					evt.stopPropagation();
				})
			;
	};

	/**
	* addPinBtn
	*
	* Add a custom Pin button for a pane
	*
	* Four classes are added to the element, based on the paneClass for the associated pane...
	* Assuming the default paneClass and the pin is 'up', these classes are added for a west-pane pin:
	*  - ui-layout-pane-pin
	*  - ui-layout-pane-west-pin
	*  - ui-layout-pane-pin-up
	*  - ui-layout-pane-west-pin-up
	*
	* @param String   selector 	jQuery selector for button, eg: ".ui-layout-north .ui-layout-pin"
	* @param String   pane 		Name of the pane the pin is for: 'north', 'south', etc.
	*/
	function addPinBtn (selector, pane) {
		var $E = getBtn(selector, pane, "-pin");
		if ($E) {
			var s = o[pane];
			$E.click(function (evt) {
				setPinState($(this), pane, (s.isSliding || s.isClosed));
				if (s.isSliding || s.isClosed) open( pane ); // change from sliding to open
				else close( pane ); // slide-closed
				evt.stopPropagation();
			});
			// add up/down pin attributes and classes
			setPinState ($E, pane, (!s.isClosed && !s.isSliding));
			// add this pin to the pane data so we can 'sync it' automatically
			// PANE.pins key is an array so we can store multiple pins for each pane
			c[pane].pins.push( selector ); // just save the selector string
		}
	};

	/**
	* syncPinBtns
	*
	* INTERNAL function to sync 'pin buttons' when pane is opened or closed
	* Unpinned means the pane is 'sliding' - ie, over-top of the adjacent panes
	*
	* @callers  open(), close()
	* @params  pane   These are the params returned to callbacks by layout()
	* @params  doPin  True means set the pin 'down', False means 'up'
	*/
	function syncPinBtns (pane, doPin) {
		$.each(c[pane].pins, function (idx, selector) {
			setPinState ($(selector), pane, doPin);
		});
	};

	/**
	* setPinState
	*
	* Change the class of the pin button to make it look 'up' or 'down'
	*
	* @callers  addPinBtn(), syncPinBtns()
	* @param Element  $Pin		The pin-span element in a jQuery wrapper
	* @param Boolean  doPin		True = set the pin 'down', False = set it 'up'
	* @param String   pinClass	The root classname for pins - will add '-up' or '-down' suffix
	*/
	function setPinState ($Pin, pane, doPin) {
		var state = $Pin.attr("pin");
		if (state && doPin == (state=="down")) return; // already in correct state
		var
			root	= o[pane].buttonClass
		,	class1	= root +"-pin"
		,	class2	= root +"-"+ pane +"-pin"
		,	UP1		= class1 + "-up"
		,	UP2		= class2 + "-up"
		,	DN1		= class1 + "-down"
		,	DN2		= class2 + "-down"
		;
		$Pin
			.attr("pin", doPin ? "down" : "up") // logic
			.attr("title", doPin ? "Un-Pin" : "Pin")
			.removeClass( doPin ? UP1 : DN1 ) 
			.removeClass( doPin ? UP2 : DN2 ) 
			.addClass( doPin ? DN1 : UP1 ) 
			.addClass( doPin ? DN2 : UP2 ) 
		;
	};



/*
 * ###########################
 * CREATE/RETURN BORDER-LAYOUT
 * ###########################
 */

	// init global vars
	var 
		$Container = $(this).css({ overflow: "hidden" }) // Container elem
	,	$Ps		= {} // Panes x4	- set in initPanes()
	,	$Cs		= {} // Content x4	- set in initPanes()
	,	$Rs		= {} // Resizers x4	- set in initHandles()
	,	$Ts		= {} // Togglers x4	- set in initHandles()
	,	cDims	= {} // Container Dimensions - initialized in create()
	//	create aliases for config & options because they are used so much!
	,	c		= config
	,	o		= options
	;

	// create the border layout NOW
	create();

	// return object pointers to expose data & option Properties, and primary action Methods
	return {
		containerDimensions: cDims		// property - dimensions of layout container
	,	options:		options			// property - options object
	,	panes:			$Ps				// property - object pointers for ALL panes: panes.north, panes.center
	,	toggle:			toggle			// method - pass a 'pane' ("north", "west", etc)
	,	open:			open			// method - ditto
	,	close:			close			// method - ditto
	,	hide:			hide			// method - ditto
	,	show:			show			// method - ditto
	,	resizeContent:	sizeContent		// method - ditto
	,	sizePane:		sizePane		// method - pass a 'pane' AND a 'size' in pixels
	,	resizeAll:		resizeAll		// method - no parameters
	,	addToggleBtn:	addToggleBtn	// utility - pass element selector and 'pane'
	,	addOpenBtn:		addOpenBtn		// utility - ditto
	,	addCloseBtn:	addCloseBtn		// utility - ditto
	,	addPinBtn:		addPinBtn		// utility - ditto
	,	allowOverflow:	allowOverflow	// utility - pass calling element
	,	resetOverflow:	resetOverflow	// utility - ditto
	}

}
})( jQuery );