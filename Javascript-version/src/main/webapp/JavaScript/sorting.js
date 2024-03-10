/**
 * Handle the sorting of a playList
 */
{ 
    function handleSorting (target) {
 	 //set css and get all list items
  	target.classList.add("slist");
 	let items = target.getElementsByTagName("li"); 
  	let current = null;

  	// make items draggable and sortable
  	for (let i of items) {
    	i.draggable = true;

    	//drag start - grey highlights in dropzones
    	i.ondragstart = e => {
      		current = i;
      		for (let it of items) {
        		if (it != current) { it.classList.add("hint"); }
      		}
    	};

    	// drag enter - pink highlight dropzone
    	i.ondragenter = e => {
      		if (i != current) { i.classList.add("active"); }
    	};

    	// drag leave - remove pink highlight
    	i.ondragleave = () => i.classList.remove("active");

    	// drag end - remove all highlight
    	i.ondragend = () => { for (let it of items) {
      		it.classList.remove("hint");
      		it.classList.remove("active");
    	}};

    	// drag over - prevent the default 'drop' so we can do our own 
    	i.ondragover = e => e.preventDefault();

    	// on drop 
    	i.ondrop = e => {
      		e.preventDefault();
      		if (i != current) {
        		let currentpos = 0, droppedpos = 0;
        		for (let it=0; it<items.length; it++) {
          			if (current == items[it]) { 
						 currentpos = it; 
					}
         			if (i == items[it]) { 
						 droppedpos = it; 
					}
        		}
        		if (currentpos < droppedpos) {
          			i.parentNode.insertBefore(current, i.nextSibling);
        		} else {
          			i.parentNode.insertBefore(current, i);
        		}
      		}
    	};
  	}
  }
}
