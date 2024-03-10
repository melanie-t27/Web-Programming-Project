(function () {
    document.getElementById("createSongButton").addEventListener("click" , (e) => {
       console.log("Creating a new song");

       //Take the closest form
       let form = e.target.closest("form");

       //Reset the error
        pageOrchestrator.resetErrors();

        if(form.checkValidity()){
            //Take the fields of the form and check them
            let title = document.getElementById("title").value;
            let genre = document.getElementById("genre").value;
            let albumTitle = document.getElementById("albumTitle").value;
            let singer = document.getElementById("artist").value;
            let publicationYear = document.getElementById("year").value;

            //Check if the publicationYear is valid
            if(isNaN(publicationYear)){
                document.getElementById("songError").textContent = "Publication year is not a number";
                return;
            }
            if(publicationYear > (new Date().getFullYear())){
                document.getElementById("songError").textContent = "Publication year not valid";
                return;
            }

            //Check if the genre is one of the type allowed
            if(!(genre === "Alternative" || genre === "Pop" || genre ==="Dance" || genre === "Rock" || genre === "Rap")){
                document.getElementById("songError").textContent = "Invalid genre";
                return;
            }

            //Check if some fields are too long
            if(title.length > 45 || albumTitle-length > 45 || singer.length > 45 || genre.length > 45){
                document.getElementById("songError").textContent = "Some values are too long";
                return;
            }

            makeCall("POST" , "CreateSong" , form ,
                function (x) {

                    if(x.readyState = XMLHttpRequest.DONE){
                        switch(x.status){
                            case 200:
                                //Update 
                                pageOrchestrator.showMainPage();
                                //Reset the form if the request was successful
                                form.reset();
                                break;

                            case 403:
                                sessionStorage.removeItem("username");
                                window.location.href = "loginPage.html";
                                break;

                            default:
                                document.getElementById("songError").textContent = x.responseText;
                                break;
                        }
                    }
                }
            );

        }else{
            form.reportValidity();
        }
    });
})();