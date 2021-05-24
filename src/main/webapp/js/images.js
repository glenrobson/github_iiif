
function showImageJson(project) {
    $.ajax({
        url: '/iiif/' + project + '/images/manifest.json',
        type: 'GET',
        success: function(data) {
            showImages(project, data);
        },
        error: function(data) {
            console.log('Failed to get images manifest.json due to ' + data);
            showNoImages();
        }
    });
}

function showNoImages() {
    var div = document.getElementById('images');
    div.innerHTML = "No images found. Upload one by clicking the upload button."
}

// Return URL to IIIF image which matches or is bigger than supplied width and height
function getImageURL(width, height, canvas) {
    return getIIIFImageURL(width, height, canvas.items[0].items[0].body.service[0]);
}

function monitorImage(details) {
    /*
        details.proccess_id 
        details.status_div
        details.project
    */
    $.ajax({
        url: '/status/images/' + details.project + '/images/manifest.json',
        type: 'GET',
        data: {
            id: details.proccess_id
        },
        success: function(data) {
            let div = document.getElementById(details.status_div);
            let finished = false;
            if ("status" in data) {
                if (data.status === "INIT") {
                    div.innerHTML = "Processing...";
                } else if (data.status === "TILE_GENERATION") {
                    div.innerHTML = "Generating tiles...";
                } else if (data.status === "GIT_UPLOAD") {
                    div.innerHTML = "Uploading images to GitHub...";
                } else if (data.status === "UPDATING_IMG_LIST") {
                    div.innerHTML = "Updating list of images...";
                } else if (data.status === "PAGES_UPDATING") {
                    div.innerHTML = "Publishing files to the web...";
                } else if (data.status === "FINISHED") {
                    finished = true;
                } else if (data.status === "FAILED") {
                    finished = true;
                }
            } else {
                console.log("Process " + details.proccess_id + " seems to have finished");
                finished = true;
            }

            if (!finished) {
                setTimeout(monitorImage, 4000, details);
            } else {
                showImageJson(details.project);
            }
        },
        error: function(data) {
            console.log('Failed to get images status due to ' + data);
        }
    });
    
}

function addImage(item, project) {
    var parentDiv = document.getElementById('images');
    /*
        <div class="card" style="width: 18rem;">
          <img class="card-img-top" src="..." alt="Card image cap">
          <div class="card-body">
            <h5 class="card-title">Card title</h5>
            <p class="card-text">Some quick example text to build on the card title and make up the bulk of the card's content.</p>
            <a href="#" class="btn btn-primary">Go somewhere</a>
          </div>
        </div>
     */

    let inProgress = ('proccess_id' in item);

    let update = false;
    let cardDiv = null;
    
    //cardDiv.style.width = "18rem";
    if (document.getElementById(item.id) != null) {
        // already added this image so return
        cardDiv = document.getElementById(item.id);
        update = true;
        if (!cardDiv.hasAttribute("data-inprogress") || inProgress) {
            return;
        }
        cardDiv.innerHTML = "";
        cardDiv.removeAttribute("data-inProgress");
    } else {
        cardDiv = document.createElement("div");
        cardDiv.id = item.id;
        cardDiv.className = "card mb-4";
        parentDiv.insertBefore(cardDiv,parentDiv.children[0]);
    }

    if (inProgress) {
        cardDiv.setAttribute("data-inProgress", true);
        let statusDiv = document.createElement("p");
        statusDiv.id = "image-status-" + item.proccess_id;
        statusDiv.innerHTML = "Processing...";
        statusDiv.className = "image-status-text";
        let details = {
            proccess_id: item.proccess_id,
            status_div: statusDiv.id,
            project: project
        }
        setTimeout(monitorImage, 4000, details);
        /* <div class="spinner-border" role="status">
             <span class="sr-only">Loading...</span>
           </div> */
        let container = document.createElement("div");
        container.className = "card-img-top"
        cardDiv.appendChild(container);

        let spinner = document.createElement("div");
        spinner.className = "spinner-border image-spinner";
        spinner.role = "status";
        container.appendChild(spinner);
        let spinnerText = document.createElement("span");
        spinnerText.className = "sr-only";
        spinnerText.innerHTML = "Loading...";
        spinner.appendChild(spinnerText);
        
        container.appendChild(statusDiv);
    } else {
        let img = document.createElement("img");
        let url = getImageURL(286, 180, item);
        img.addEventListener('error', updateFailedImage);
        img.className = "card-img-top";
        img.src = url;
        //img.style.width = "286px";</option>
        cardDiv.appendChild(img);
    }

    let cardBody = document.createElement("div");
    cardBody.className = "card-body";
    cardDiv.appendChild(cardBody);

    let cardTitle = document.createElement('h5');
    cardTitle.className = "card-title";
    cardTitle.innerHTML = item.label.none[0];
    cardBody.appendChild(cardTitle);

    let cardText = document.createElement('p');
    cardText.className = "card-text";
    cardBody.appendChild(cardText);
    if (inProgress) {
        //cardText.innerHTML = "In Progress";
    } else {
        let infoJsonURL = "";
        let fullImageURL = "";
        let version = "";
        if ('type' in item.items[0].items[0].body.service[0] && item.items[0].items[0].body.service[0].type === 'ImageService3') {
            infoJsonURL = item.items[0].items[0].body.service[0].id + "/info.json";
            fullImageURL = item.items[0].items[0].body.service[0].id + "/full/max/0/default.jpg";
            version = "3.0";
        } else {
            infoJsonURL = item.items[0].items[0].body.service[0]["@id"] + "/info.json";
            fullImageURL = item.items[0].items[0].body.service[0]["@id"] + "/full/full/0/default.jpg";
            version = "2.0";
        }

        let ul = document.createElement('ul');
        cardText.appendChild(ul);

        let liInfo = document.createElement('li');
        ul.appendChild(liInfo);
        liInfo.innerHTML = "<a href='" + infoJsonURL + "' target='_blank'>info.json</a>"

        let liFull = document.createElement('li');
        ul.appendChild(liFull);
        liFull.innerHTML = "<a href='" + fullImageURL + "' target='_blank''>Full image</a>"

        let versionNote  = document.createElement('p');
        versionNote.innerHTML = "Version " + version;
        cardText.appendChild(versionNote);
    }
}

function updateFailedImage(event) {
    let url = event.srcElement.src;
    if (url.indexOf('?') != -1) {
        url = url.split('?')[0];
    }
    setTimeout(function () {
        event.srcElement.src = url + "?" + performance.now();
    }, 500);
}

function showImages(project, data) {
    if (data.items.length === 0) {
        showNoImages();
    } else {
        data.items.forEach(function(item, index) {
            addImage(item, project); 
        });
    }
}
