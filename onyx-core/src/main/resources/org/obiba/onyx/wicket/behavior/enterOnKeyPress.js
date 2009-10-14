/*
* Click on component when specific key is pressed on keyboard.
*/

function clickOnComponentWhenKeyPressed(event, elementId, keyPressed){
  var code;
  if(!event){
    event = window.event;
  }
  if(event.keyCode) { //IE
    code = event.keyCode;
  } else if(event.which) { //FF
    code = event.which;
  }
  
  if (code == keyPressed){
    $('#' + elementId).click();
    event.preventDefault();
    return false;
  } else {
    return true;
  }
}
