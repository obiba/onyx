if (typeof (Wicket) == "undefined") {
	Wicket = {};
}

Wicket.QTree = {}

function buildQTree(id, options) {
	Wicket.QTree[id] = this;
}

Wicket.QTree.buildTree = function(treeId) {
	$("#" + treeId).jstree({
		"themes" : {
			"theme" : "apple",
			"url": "resources/org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior/themes/apple/style.css",
			"dots" : false,
			"icons" : true
		},		
		"types" : {
			"types" : {
				"Questionnaire" : {
					"valid_children" : [ "Section", "Page" ]
				},
				"Section" : {
					"valid_children" : [ "Section", "Page" ]
				},
				"Page" : {
					"valid_children" : "Question"
				},
				"Question" : {
					"valid_children" : "QuestionCategory"
				},
				"QuestionCategory" : {
					"valid_children" : [ "Category", "OpenAnswerDefinition" ]
				},
				"Category" : {
					"valid_children" : "none"
				},
				"OpenAnswerDefinition" : {
					"valid_children" : "none"
				}
			}
		},
		"plugins" : [ "themes", "html_data", "types", "ui", "dnd" ]
	})
	.bind("move_node.jstree", function (e, data) {
		data.rslt.o.each(function (i) {
			// alert("move "+ i +" - id: "+ $(this).attr("name") +" ["+ $(this).attr("id") +"], parent: "+  data.rslt.op.attr("id") +" --> "+ data.rslt.np.attr("id") +", position: "+ data.rslt.cp);
			// see http://groups.google.com/group/jstree/browse_thread/thread/72c504f1212f7258/ff44f4f7d8a57422?lnk=gst&q=move_node#ff44f4f7d8a57422
			Wicket.QTree.moveCallback($(this).attr("id"), data.rslt.np.attr("id"), data.rslt.cp);
		});
	});
}
