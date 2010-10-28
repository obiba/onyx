if (typeof (Wicket) == "undefined") {
	Wicket = {};
}

Wicket.Sortable = {}

Wicket.Sortable.create = function(listId) {
	$("#"+ listId).sortable({
		placeholder : 'ui-state-highlight',
		update: function(event, ui) {
			Wicket.Sortable.toStringArray($("#"+ listId).sortable('toArray').toString());
		}
	}).disableSelection();
}

