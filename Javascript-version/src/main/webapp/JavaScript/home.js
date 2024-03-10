{	
    //Page components
    var playlistList;
    var songsList;
    var songsInPlayList;
    var songsNotInPlayList;
    var songDetails;
    var sortingList;  
    var currentPlaylist;
    var currentSection;
    var currentSong;
    var playlistToSort;
    
    let personalMessage;
    let playListMessage;
    var pageOrchestrator = new PageOrchestrator();

	 window.addEventListener("load" , () => {
        if(sessionStorage.getItem("username") == null){
            window.location.href = "loginPage.html";
        }else{
            pageOrchestrator.start(); // initialize the components
            pageOrchestrator.resetErrors();
            pageOrchestrator.showMainPage();
        } // display initial content
    } , false);
    

    //CONSTRUCTOR OF VIEW COMPONENTS
    
    // It contains all the song titles and ids of the current playList needed for the sorting
    function PlayListSongsToOrder(){
        this.playlistId = null;
        this.songs = new Array();

        this.reset = function() {
            this.songs = [];
        }

        this.addSong = function(song) {
            this.songs.push(song);
        }
    }


    //Function that represent a song to be sorted
    function Song(id , name){
        this.id = id;
        this.name = name;
    }

   
    
    //Function that initialize the personal message (the username)
    function PersonalMessage(username , messageContainer) {
        this.username = username;
        this.messageContainer = messageContainer;

        this.show = function() {
            this.messageContainer.innerHTML = this.username;
        }
    }
   
     //Function that shows the name of the current playlist the user is watching 
    function PlaylistMessage(messageContainer){
        this.playlistName = null;
        this.messageContainer = messageContainer;

        this.show = function(playlistName) {
			this.playlistName = playlistName;
            this.messageContainer.textContent = this.playlistName;
            this.messageContainer.style.display = "";
        }

        this.reset = function() {
            this.messageContainer.style.display = "none";
        }
    }
    
    //Function that shows the songs of the playlist in order to be added to a new playlist
    function SongsList(alertContainer, listContainer){
		this.alertContainer = alertContainer;
		this.listContainer = listContainer;
		
		this.reset = function() {
            this.listContainer.innerHTML = "";
            this.alertContainer.textContent = "";
        }
        
        this.show = function(){ 
			let self = this;
			this.reset();
			console.log("reset done");
			//Ask the song list to the server
			makeCall("GET", "GetSongs", null,
	        function(req) {
	          if (req.readyState == 4) {
	            let message = req.responseText;
	            if (req.status == 200) {
	              let songs = JSON.parse(req.responseText);
	              if (songs.length == 0) {
	                self.alertContainer.textContent = "No songs yet!";
	                return;
	              }
	              self.reset();
	              self.update(songs); 
	              
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
              } else {
	            self.alertContainer.textContent = message;
	          }}
	        }
	      );
		}
		
		this.update = function(songs){
			let self = this;
			let checkbox, label;

			let inputTitle = document.createElement("input");
			inputTitle.type = "text";
			inputTitle.id = "titlePlaylist";
			inputTitle.name = "titlePlaylist";
			inputTitle.required = true;
			let lableTitle  = document.createElement("label");
			lableTitle.setAttribute("for", "titlePlaylist");
			lableTitle.innerHTML = "Title of the playlist";
			self.listContainer.appendChild(lableTitle);
			self.listContainer.appendChild(inputTitle);
			self.listContainer.appendChild(document.createElement("br"));
			let par = document.createElement("label")
			par.for = "checkbox"
			par.innerHTML = "Choose one or more song in order to create your new playlist:";
			self.listContainer.appendChild(par);
			self.listContainer.appendChild(document.createElement("br"));
			
			songs.forEach(function(song){
				checkbox = document.createElement("input");
				checkbox.setAttribute("type", "checkbox");
				checkbox.setAttribute("value", song.id);
				checkbox.setAttribute("name", "selectedSong");
				checkbox.setAttribute("id", "selectedSong");
				label = document.createElement("label");
				label.setAttribute("class", "selectedSong");
				label.setAttribute("for", "selectedSong");
				label.innerHTML = song.title + " from " + song.album.title + "(" + song.album.artist + ")";
				self.listContainer.appendChild(checkbox);
				self.listContainer.appendChild(label);
				self.listContainer.appendChild(document.createElement("br"));
			});
			
			let button = document.createElement("input");
			button.type = "button";
			button.value = "submit";
			button.id = "createPlaylistButton";
			button.addEventListener("click", (e) => {handleCreatePlaylist(e);});
			self.listContainer.appendChild(button);
			self.listContainer.appendChild(document.createElement("br"));
		}
        
	}

   
    //Function that take the playlist of the user from the data base 
	function PlaylistList(alertContainer, listContainer){
		this.alertContainer = alertContainer;
        this.listContainer = listContainer;
        let self = this;

        this.reset = function() {
            this.listContainer.innerHTML = "";
            this.alertContainer.textContent = "";
        }
        
        this.show = function(){
			let self = this;
			//Ask the playlist list to the server
			makeCall("GET", "GetPlaylists", null,
	        function(req) {
	          if (req.readyState == 4) {
	            let message = req.responseText;
	            if (req.status == 200) {
	              let pl = JSON.parse(req.responseText);
	              if (pl.length == 0) {
	                self.alertContainer.textContent = "No playlists yet!";
	                return;
	              }
	              self.update(pl); // self visible by closure
	              
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  
              } else {
	            self.alertContainer.textContent = message;
	          }}
	        }
	      );
		}
		
		this.update = function(playlists){
			let self = this;
			//Element of each list item
			let listItem, playlistName, creationDate, anchor, anchorSorting;
			this.listContainer.innerHTML = "";
			
			let p = document.getElementById("playlistListMessage");
			p.innerHTML = "Here are your playlists from the most to the least recently created.";
			
			playlists.forEach(function(playlist){
				//create list item
				playlistName = document.createElement("span");
				playlistName.innerText = playlist.name;
				playlistName.setAttribute("id", "playlistName");
				
				listItem = document.createElement("li");
				anchor = document.createElement("a");
				anchor.appendChild(playlistName);
				anchor.setAttribute("playlistId" , playlist.id);
				
				creationDate = document.createElement("small");
				creationDate.textContent = "Created " + playlist.date;
				
				anchorSorting = document.createElement("button");
				anchorSorting.innerHTML = "Sort";
				anchorSorting.onclick = function(){
					sortingList = playlist.id;
					pageOrchestrator.showSortingPage();
				};
				
				listItem.appendChild(anchor);
				listItem.appendChild(creationDate);
				listItem.appendChild(anchorSorting);
				
				//
				anchor.addEventListener("click" , () => {
					currentPlaylist = playlist;
					currentSection = 1;
	              	pageOrchestrator.showPlaylistPage();
                });
                
                //Disable the href of the anchor
                anchor.href = "#";
                self.listContainer.appendChild(listItem);
				
			});
            
            //Show the list
            this.listContainer.style.visibility = "visible";
		}
	}

	
	//Function that takes the songs in a playlist
	function SongsInPlaylist(alertContainer, tableContainer){
		this.alertContainer = alertContainer;
		this.tableContainer = tableContainer;
		this.songs = null;
		
		this.reset = function(){
			this.alertContainer.textContent = "";
			this.tableContainer.innerHTML = "";
		}
		
		this.show = function(){
			let self = this;
			if(this.songs != null){
				self.update();
			}
			this.reset();
			//Ask the songs in the playlist to the server
			makeCall("GET", "GetSongsInPlaylist?playlistId=" + currentPlaylist.id, null,
	        function(req) {
	          if (req.readyState == 4) {
	            let message = req.responseText;
	            if (req.status == 200) {
	              self.songs = JSON.parse(req.responseText);
	              if (self.songs.length == 0) {
	                self.alertContainer.textContent = "No songs yet!";
	                return;
	              }
          
	              self.update(); // self visible by closure
	              
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  
              } else {
	            self.alertContainer.textContent = message;
	          }}
	        }
	      );
		}
		
		this.update = function(){
			let self = this;
			//Empty the body of the table
            this.tableContainer.innerHTML = "";
            
			let tableRow = document.createElement("tr");
			this.tableContainer.appendChild(tableRow);
			
			let section = currentSection;
			let next = false;
            //Check section and set next
            if (section < 1 || !section) {
                section = 1;
            }
            if (section > (this.songs.length / 5 ) + 1) {
                section = (this.songs.length / 5) + 1;
                //Save just the number before the point 
                section = parseInt(section.toString().split(".")[0]);
            }
            if ((section * 5) < this.songs.length) {
                next = true;
            }
            
            //save the current section
            currentSection = section;
            
            if(section > 1){
				let tableData = document.createElement("td");
				let anchorLeftArrow = document.createElement("a");
				let arrowLeft = document.createElement("i");
				arrowLeft.setAttribute("class", "arrow left");
				let arrowText = document.createElement("b");
				arrowText.innerHTML ="Back";
				
				anchorLeftArrow.addEventListener("click",  () => {
					currentSection = section-1;
					pageOrchestrator.showPlaylistPage();
				});
				
				anchorLeftArrow.appendChild(arrowLeft);
				anchorLeftArrow.appendChild(arrowText);
				tableData.appendChild(anchorLeftArrow);
				tableRow.appendChild(tableData);
			}
			
			//select the right five songs to show
			let songsToShow;
			if (this.songs.length >= currentSection * 5){
            	songsToShow = this.songs.slice(currentSection * 5 - 5, currentSection * 5); // [)
            } else {
            	songsToShow = this.songs.slice(currentSection * 5 - 5, this.songs.length); // [)
            }
			
			songsToShow.forEach(function(song){
				let tableData, spanName, imgTag, anchor;
				imgTag = document.createElement("img");
				imgTag.setAttribute("src", "data:image/jpeg;base64,"+song.album.cover);
				imgTag.setAttribute("width", 100);
				spanName = document.createElement("span");
				spanName.innerHTML = song.title;
				anchor = document.createElement("a");
				anchor.appendChild(spanName);
				anchor.setAttribute("value", song.id);
				
				anchor.addEventListener("click" , () => {
					currentSong = song.id;
					pageOrchestrator.showSongPage();
                });
				
				tableData = document.createElement("td");
				tableData.appendChild(imgTag);
				tableData.appendChild(document.createElement("br"));
				tableData.appendChild(anchor);
				tableRow.appendChild(tableData);
				
			});
			
			 if(next){
				let tableData = document.createElement("td");
				let anchorArrowRight = document.createElement("a");
				let arrowRight = document.createElement("i");
				arrowRight.setAttribute("class", "arrow right");
				let arrowText = document.createElement("b");
				arrowText.innerHTML ="More";
				
				anchorArrowRight.addEventListener("click",  () => {
					currentSection = currentSection +1;
					pageOrchestrator.showPlaylistPage();
				});
				
				anchorArrowRight.appendChild(arrowText);
				anchorArrowRight.appendChild(arrowRight);
				tableData.appendChild(anchorArrowRight);
				tableRow.appendChild(tableData);
			}
		}
	}

	//Function that takes the songs that are not in the playlist
	function SongsNotInPlaylist(alertContainer, messageContainer, selectContainer){
		this.alertContainer = alertContainer;
		this.messageContainer = messageContainer;
		this.selectContainer = selectContainer;
		
		this.reset = function(){
			this.alertContainer.textContent = "";
			this.messageContainer.textContent = "";
			this.selectContainer.innerHTML = "";
		}
		
		this.show = function(){
			this.reset();
			let self = this;
			//Ask the songs not in playlist to the server
			makeCall("GET", "GetSongsNotInPlaylist?playlistId=" + currentPlaylist.id , null,
	        function(req) {
	          if (req.readyState == 4) {
	            let message = req.responseText;
	            if (req.status == 200) {
	              let snp = JSON.parse(req.responseText);
	              if (snp.length == 0) {
	                self.messageContainer.textContent = "This playlist already contains all of your songs.";
	                document.getElementById("addSongToPLayListForm").style.display = "none";
	                return;
	              }
	              self.update(snp); // self visible by closure
	              document.getElementById("addSongToPLayListForm").style.display = "block";
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  
              } else {
	            self.alertContainer.textContent = message;
	          }}
	        }
	      );
		}
		
		this.update = function(songs){
			let self = this;
			let optionSelected = document.createElement("option");
			optionSelected.innerHTML = "---";
			self.selectContainer.appendChild(optionSelected);
			songs.forEach(function(song){
				let option = document.createElement("option");
				option.setAttribute("value", song.id);
				option.id = "addSongToPlayList";
				option.innerHTML = song.title + " from " + song.album.title + "(" + song.album.artist + ")";
				self.selectContainer.appendChild(option);
			});
		}
	}
	
	//Function that gets the song details
	function SongDetails(alertContainer, playerContainer, dataContainer){
		this.alertContainer = alertContainer;
		this.playerContainer = playerContainer;
		this.dataContainer = dataContainer;
		this.song = null;
		
		this.reset = function(){
			this.alertContainer.textContent = "";
			this.playerContainer.innerHTML = "";
			this.dataContainer.innerHTML = "";
		}
		
		this.show = function(){
			this.reset();
			let self = this;
			//Ask the songs not in playlist to the server
			makeCall("GET", "GetSong?songId=" + currentSong + "&playlistId=" + currentPlaylist.id, null,
	        function(req) {
	          if (req.readyState == 4) {
	            let message = req.responseText;
	            if (req.status == 200) {
	              let song = JSON.parse(req.responseText);
	              if (song == null) {
	                self.alertContainer.textContent = "This song is not available.";
	                return;
	              }
	              self.song = song;
	              self.update(); // self visible by closure
	              
	              
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  
              } else {
	            self.alertContainer.textContent = message;
	          }}
	        }
	      );
		}
		
		this.update = function(){
			let self = this;
			let cover = document.createElement("img");
			cover.setAttribute("src", "data:image/jpeg;base64," + this.song.cover);
			let audio = document.createElement("audio");
			audio.controls = "controls";
			let source = document.createElement("source");
			source.setAttribute("src", "data:audio/mpeg;base64," + this.song.audio);
			source.setAttribute("type", "audio/mpeg")
			audio.appendChild(source);
			playerContainer.appendChild(cover);
			playerContainer.appendChild(document.createElement("br"));
			playerContainer.appendChild(audio);
			
			let title, genre, artist, album, year, anchor, parAnchor;
			title = document.createElement("p");
			title.innerHTML = "Title : " + this.song.songTitle;
			genre = document.createElement("p");
			genre.innerHTML = "Genre : " + this.song.genre;
			album = document.createElement("p");
			album.innerHTML = "Album Title : " + this.song.albumTitle;
			artist = document.createElement("p");
			artist.innerHTML = "Artist : " + this.song.singer;
			year = document.createElement("p");
			year.innerHTML = "Year of Publication : " + this.song.publicationYear;
			parAnchor = document.createElement("p");
			anchor= document.createElement("a");
			let arrow = document.createElement("i");
			arrow.setAttribute("class", "arrow left");
			anchor.appendChild(arrow);
			let textAnchor = document.createElement("b");
			textAnchor.innerHTML = "Go Back to Playlist";
			anchor.appendChild(textAnchor);
			parAnchor.appendChild(anchor);
			
			anchor.addEventListener("click" , () => {
					currentSong = -1;
					pageOrchestrator.showPlaylistPage();
                });
			
			this.dataContainer.appendChild(title);
			this.dataContainer.appendChild(genre);
			this.dataContainer.appendChild(artist);
			this.dataContainer.appendChild(album);
			this.dataContainer.appendChild(year);
			this.dataContainer.appendChild(parAnchor);
		}
	}
	
	// Function that gets the list of songs that need to be sorted
	function SortingList(alertContainer, listContainer){
		this.alertContainer = alertContainer;
		this.listContainer = listContainer;
		this.songs = null;
		
		this.reset = function(){
			this.alertContainer.innerHTML = "";
			this.listContainer.innerHTML = "";
		}
		
		this.show = function(){
			this.reset();
			let self = this;
			//Ask the songs in the playlist to the server
			makeCall("GET", "GetSongsInPlaylist?playlistId=" + sortingList, null,
	        function(req) {
	          if (req.readyState == 4) {
	            let message = req.responseText;
	            if (req.status == 200) {
	              self.songs = JSON.parse(req.responseText);
	              if (self.songs.length == 0) {
	                self.alertContainer.textContent = "This playlist doesn't have any songs.";
	                return;
	              }
	              self.update(); // self visible by closure
	              
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  
              } else {
	            self.alertContainer.textContent = message;
	          }}
	        }
	      );
		}
		
		this.update = function(){
			let self = this;
			this.songs.forEach(function(song){
				let listItem = document.createElement("li");
				listItem.value = song.id;
				listItem.id = "songToSort";
				listItem.textContent = song.title;
				self.listContainer.appendChild(listItem);
			});
			handleSorting(self.listContainer);
			document.getElementById("addSortingButton").addEventListener("click", (e) => {AddSelectedSorting(e);});
		}
	}

	// main controller of the single page application
	function PageOrchestrator(){
		let self = this;
	    
	    this.start = function() {
	      this.personalMessage = new PersonalMessage(sessionStorage.getItem('username'),
	        document.getElementById("username"));
	      this.personalMessage.show();

	      // initialize the playList list
	      this.playlistList = new PlaylistList(
	        document.getElementById("playlistListError"),
	        document.getElementById("playlistList"));
	        
	      // initialize the song list
	      this.songsList = new SongsList(
			  document.getElementById("createPlaylistError"),
			  document.getElementById("createPlaylist")
		  );
	        
	      // initialize the songs in the playList
	      this.songsInPlayList = new SongsInPlaylist(
			  document.getElementById("errorSongsInPlaylist"),
			  document.getElementById("SongsInPlaylist")
		  );
		  
		  // initialize the playlist title
		  this.playlistMessage = new PlaylistMessage(
			  document.getElementById("playlistNameMessage")
		  );
		  
		  // initialize the songs not in playlist
		  this.songsNotInPlaylist = new SongsNotInPlaylist(
			  document.getElementById("addSongError"),
			  document.getElementById("addSongMessage"),
			  document.getElementById("addSongToPlayList")
		  );
		  
		  // initialize the song details
		  this.songDetails = new SongDetails(
			  document.getElementById("songPageError"),
			  document.getElementById("player"),
			  document.getElementById("songData")
		  );
		  
		  this.sortingList = new SortingList(
			  document.getElementById("sortingError"),
			  document.getElementById("sortlist")
		  );

	      //Set the event of logout
	      document.querySelector("a[href='Logout']").addEventListener('click', () => {
	          window.sessionStorage.removeItem('username');
	      });
	      
	      //Set the event of homepage reset to the anchor
	      document.querySelector("a[href='Home']").addEventListener('click', (ev) => {
			  ev.preventDefault();
			  self.resetErrors();
			  self.showMainPage();
		  });
		  
	    }
		
		//function that showsthe song page
		this.showSongPage = function(){
			this.resetErrors();
			document.getElementById("songPage").style.display = "block";
			document.getElementById("homePage").style.display = "none";
			document.getElementById("playlistPage").style.display = "none";
			document.getElementById("sortingPage").style.display = "none";
			this.songDetails.show(currentSong, currentPlaylist);
		}
		
		//function that shows the playlist page
		this.showPlaylistPage = function(){
			 this.resetErrors();
			 this.songDetails.reset();
			 document.getElementById("songPage").style.display = "none";
			 document.getElementById("homePage").style.display = "none";
			 document.getElementById("playlistPage").style.display = "block";
			 document.getElementById("sortingPage").style.display = "none";
			 this.playlistMessage.show(currentPlaylist.name);
			 this.songsInPlayList.show(currentPlaylist, currentSection);
			 this.songsNotInPlaylist.show(currentPlaylist.id);
		}
	    
	    //function that resets the errors
	    this.resetErrors = function(){
			document.getElementById("playlistListError").innerHTML = "";
			document.getElementById("createPlaylistError").innerHTML = "";
			document.getElementById("songError").innerHTML = "";
			document.getElementById("errorSongsInPlaylist").innerHTML = "";
			document.getElementById("songPageError").innerHTML = "";
			//TO FINISH
		}
		
		//function that shows the homepage
		this.showMainPage = function(){
			this.resetErrors();
			this.songsInPlayList.reset();
			this.songsNotInPlaylist.reset();
	      	this.songDetails.reset();
	      	this.songsList.reset();
			this.playlistList.show();
	      	this.songsList.show();
	      	console.log("showing main page");
			document.getElementById("playlistPage").style.display = "none";
			document.getElementById("songPage").style.display = "none";
			document.getElementById("homePage").style.display = "block";
			document.getElementById("sortingPage").style.display = "none";
	    }
	    
	    //function that shows the sorting page
	    this.showSortingPage = function(){
			this.resetErrors();
			this.sortingList.show();
			document.getElementById("playlistPage").style.display = "none";
			document.getElementById("songPage").style.display = "none";
			document.getElementById("homePage").style.display = "none";
			document.getElementById("sortingPage").style.display = "block";
		}
	}
}