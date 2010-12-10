if (typeof (Wicket) == "undefined") {
	Wicket = {};
}

Wicket.Sortable = {}

Wicket.Sortable.create${listMarkupId} = function(listId) {
	$("#"+ listId).sortable({
		placeholder : 'ui-state-highlight',
		update: function(event, ui) {
			Wicket.Sortable.toStringArray${listMarkupId}($("#"+ listId).sortable('toArray').toString());
		}
	}).disableSelection();
}
	
Wicket.Sortable.toStringArray${listMarkupId} = function(items) {
	wicketAjaxGet('${callbackUrl} + &items='+ items, function() { }, function() { alert('Cannot communicate with server...'); });
}

