if (typeof (Wicket) == "undefined") {
	Wicket = {};
}

Wicket.Sortable = {}

Wicket.Sortable.create = function(listId) {
	$("#"+ listId).sortable({
		placeholder : 'ui-state-highlight' /*,
		update: function(event, ui) {
			alert(ui.item[0].id);
			//Wicket.Sortable.update(elementId, 'section');
		}*/
	}).disableSelection();
}

Wicket.Sortable.toArray = function(listId) {
	Wicket.Sortable.toStringArray($("#"+ listId).sortable('toArray').toString());
}