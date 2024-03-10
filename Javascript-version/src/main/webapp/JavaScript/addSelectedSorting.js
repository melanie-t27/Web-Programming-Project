// saving the selected sorting
{
	function AddSelectedSorting(e){
		console.log("getting the sorting")
		var songs = document.querySelectorAll("[id='songToSort']");
		var songIds = [];
		songIds[0] = sortingList;
		for(let i = 0; i < songs.length; i++){
			songIds[i+1] = songs[i].getAttribute("value");
		}
		let request = new XMLHttpRequest();
		request.open("POST", "AddSelectedSorting", true); //asynchronous call
		request.send(JSON.stringify(songIds));
		request.onreadystatechange = function(){
			if(request.readyState == 4 && request.status == 200){
				pageOrchestrator.showMainPage();
			} else if (request.readyState == 4 && request.status == 403){
				sessionStorage.removeItem("username");
                window.location.href = "loginPage.html";
			} else {
				document.getElementById("sortingError").textContent = "Something went wrong, please try again";
			}
		};
			
	}
	
}
 