{

   function handleCreatePlaylist(e) {
		
        console.log("Creating a new playList!");

        //Take the closest form
        let form = e.target.closest("form");

        if(form.checkValidity()){
        	 //Check if the title specified is valid
        	 let title = document.getElementById("titlePlaylist").value;
        	 let playlistsList = document.getElementById("playlistList");
        	 //Take the rows of the tables
        	 let playlistNames = playlistsList.getElementsByTagName("li");
        	 let currentName;
        	 
        	 for(let i = 0 ; i < playlistNames.length ; i++){
        	 	//currentName = playlistNames[i].querySelector("playlistName").value; ????
        	 	if(title == currentName){
        	 		document.getElementById("createPlaylistError").textContent = "PlayList name already used.";
        	 		return;
        	 	}
        	 }
        	 
        	 let songsToAdd = [];
        	 let selectedSongs = document.getElementsByClassName("selectedSong");
        	 let checkboxes = document.querySelectorAll('input[type="checkbox"]:checked');
        	 checkboxes.forEach(function(checkbox){
				 console.log(checkbox.value);
				 songsToAdd.push(checkbox.value);
			 })
        	 
        	/* for(var i=0; selectedSongs[i]; ++i){
      			if(selectedSongs[i].checked){
           			songsToAdd[i] = inputElements[i].value;
   				}
			 }*/
        	 
        	 if(songsToAdd === null || songsToAdd.length === 0){
				 document.getElementById("createPlaylistError").textContent = "You have to select at least one song in order to create a new playlist.";
        	 	 return;
			 }
        
            //Make the call to the server
            makeCall("POST" , "CreatePlaylist" , form ,
                function (x) {

                    if(x.readyState == XMLHttpRequest.DONE){
                    pageOrchestrator.resetErrors();
                    
                        switch (x.status){
                            case 200:
                                //Update the playList list
                                pageOrchestrator.showMainPage();
                                break;

                            case 403:
                                sessionStorage.removeItem("username");
                                window.location.href = "loginPage.html";
                                break;

                            default:
                                document.getElementById("createPlaylistError").textContent = x.responseText;
                                break;
                        }
                    }
                }
            );
            form.reset();
        }else{
            form.reportValidity();
        }
   }
 }