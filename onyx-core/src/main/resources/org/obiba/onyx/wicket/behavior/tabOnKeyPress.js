/*
* OnKeyPress "Tab" submit form with right action
*/

function tabSubmitForm(event, elementId){
  var code;

  if(!event){
    event = window.event;
  }
  if(event.keyCode) { //IE
    code = event.keyCode;
  } else if(event.which) { //FF
    code = event.which;
  }

  if (code == 9){
    document.getElementById(elementId).click();
    return false;
  } else {
    return true;
  }
}
