(function(){
    document.getElementById("addSongButton").addEventListener("click" , (e) => {

        //Take the closest form
        let form = e.target.closest("form");

        //Reset the error
        document.getElementById("addSongMessage").textContent = "";
        
        if(form.checkValidity()){
            makeCall("POST" , "AddSong?playlistId=" + currentPlaylist.id , form ,
                function(request) {
					let self = this;
                    if(request.readyState == XMLHttpRequest.DONE){
                    	pageOrchestrator.resetErrors();
                    	
                        switch(request.status){
                            case 200:
                                //Update the view
                                pageOrchestrator.showPlaylistPage();
                                break;

                            case 403:
                                sessionStorage.removeItem("username");
                                window.location.href = "loginPage.html";
                                break;

                            default:
                                document.getElementById("addSongMessage").textContent = request.responseText;
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