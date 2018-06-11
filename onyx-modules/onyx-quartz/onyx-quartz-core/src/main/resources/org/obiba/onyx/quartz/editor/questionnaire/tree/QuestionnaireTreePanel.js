if (typeof (Wicket) == "undefined") {
	Wicket = {};
}

Wicket.QTree = {}

function buildQTree(id, options) {
	Wicket.QTree[id] = this;
}

Wicket.QTree.refreshTree = function(treeId) {
	$("#" + treeId).jstree('refresh', -1);	
}

Wicket.QTree.buildTree = function(treeId, jsonUrl) {
	$("#" + treeId).jstree({		
		"json_data" : {
			"ajax" : {
				"url" : jsonUrl
			}
		},		
		"themes" : {
			"theme" : "apple",
			"url": "resources/org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior/themes/apple/style.css",
			"dots" : false,
			"icons" : true
		},		
		"types" : {
			"valid_children" : [ "Questionnaire" ],
			"types" : {
				"Questionnaire" : {
					"valid_children" : [ "Section", "Variables" ]
				},
				"Section" : {
					"valid_children" : [ "Section", "Page" ]
				},
				"Page" : {
					"valid_children" : [ "Question", "Questionboilerplate" ]
				},
				"Question" : {
					"valid_children" : [ "none" ]
				},
				"Questionboilerplate" : {
					"valid_children" : [ "Questionboilerplate" ]
				},
				"Variables" : {
					"valid_children" : [ "Variable" ]
				},
				"Variable" : {
					"valid_children" : "none"
				}				
			}
		},
//		"contextmenu" : {
//			"select_node": true,
//			"items": 	
//				function (node) { 
//					var elementId = node.attr("id");
//					var elementType = node.attr("rel");
//					var obj = { 
//						"create_section": (elementType != "Questionnaire" && elementType != "Section") ? false : { 
//							label: "Add new Section", 
//							action: function (obj) { Wicket.QTree.addChild(elementId, 'section'); }, 
//							seperator_after : false, 
//							seperator_before : false 
//						}, 
//						"create_page": (elementType != "Section") ? false : { 
//							label: "Add new Page", 
//							action: function (obj) { Wicket.QTree.addChild(elementId, 'page'); },
//							seperator_after : false, 
//							seperator_before : false 
//						}, 
//						"create_question": (elementType != "Page") ? false :{ 
//							label: "Add new Question", 
//							action: function (obj) { Wicket.QTree.addChild(elementId, 'question'); },
//							seperator_after : false, 
//							seperator_before : false 
//						},
//						"create_variable": (elementType != "Variables") ? false :{ 
//							label: "Add new Variable", 
//							action: function (obj) { Wicket.QTree.addChild(elementId, 'variable'); },
//							seperator_after : false, 
//							seperator_before : false 
//						},
//						"edit": elementType == "Variables" ? false : { 
//							label: "Edit", 
//							action: function (obj) { Wicket.QTree.editElement(elementId); },
//							seperator_after : false, 
//							seperator_before : true 
//						},
//						"delete": (elementType == "Variables" || elementType == "Questionnaire") ? false : { 
//							label: "Delete", 
//							action: function (obj) { Wicket.QTree.deleteElement(elementId); },
//							seperator_after : false, 
//							seperator_before : true 
//						}
//					}
//					return obj; 
//				}
//		 }, 
		"ui" : {
			"select_limit" : 1
		},
		"crrm" : {
			"move" : {
				"check_move" : function (m) {
					if (m.o[0].getAttribute("rel") == "Variable" || m.o[0].getAttribute("rel") == "Variables") {
						return false;
					}
					return true;
				}
			}
		},		
		"plugins" : [ "themes", "json_data", "types", "ui", "dnd","crrm" /*, "contextmenu" */] // don't use contextmenu because menu does not hide on mouse out
	})
	.bind("move_node.jstree", function (e, data) {
		data.rslt.o.each(function (i) {
			// see http://groups.google.com/group/jstree/browse_thread/thread/72c504f1212f7258/ff44f4f7d8a57422?lnk=gst&q=move_node#ff44f4f7d8a57422
			Wicket.QTree.moveNode($(this).attr("id"), data.rslt.np.attr("id"), data.rslt.cp, data.rslt.op.attr("id"));
		});
	})
	.bind("select_node.jstree", function (e, data) {		
		Wicket.QTree.previewNode(data.rslt.obj.attr("id"));
	});	
}
