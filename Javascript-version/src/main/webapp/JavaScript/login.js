
(function() { // avoid variables ending up in the global scope

  document.getElementById("loginButton").addEventListener('click', (e) => {
	console.log("Login event!");
    var form = e.target.closest("form");
    if (form.checkValidity()) {
      makeCall("POST", 'Login', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            console.log("Making call... status"+ x.status);
            switch (x.status) {
              case 200:
            	sessionStorage.setItem('username', message); 
                window.location.href = "Home.html";
                break;
              case 400: // bad request
                document.getElementById("error").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("error").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("error").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();