/**
 * AJAX call management
 */

function makeCall(method, url, formElement, callBack , objectToSend , reset = true) {
    let request = new XMLHttpRequest();
    request.onreadystatechange = function() {
        callBack(request);
    };

    request.open(method, url);

    if (formElement == null && objectToSend == null) {
        request.send();
    } else if(formElement != null){
    	//Send the form
        request.send(new FormData(formElement));
    } else {
		//Send the object
        let toSend = JSON.stringify(objectToSend);
        request.send(toSend);
    }

    //If there is a form and reset is true -> reset the fields of the form
    if (formElement !== null && reset === true) {
        formElement.reset();
    }
}