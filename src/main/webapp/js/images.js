
function showImageJson(project) {
    $.ajax({
        url: '/iiif/' + project + '/images/manifest.json',
        type: 'GET',
        success: function(data) {
            showImages(data);
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
    let sizes = canvas.items[0].items[0].body.service[0].sizes;
    let smallsize = { width: canvas.width, height: canvas.height};
    for (let i = 0; i < sizes.length; i++) {
        let size = sizes[i];
        if (size.width > width && size.height > height) { 
            if (size.width < smallsize.width && size.height < smallsize.height) {
                smallsize.width = size.width;
                smallsize.height = size.height;
            }
        }
    }

    if ('type' in canvas.items[0].items[0].body.service[0] && canvas.items[0].items[0].body.service[0].type === 'ImageService3') {
        let imageUrl = canvas.items[0].items[0].body.service[0].id;
        return imageUrl + "/full/" + smallsize.width + "," + smallsize.height + "/0/default.jpg";
    } else {
        let imageUrl = canvas.items[0].items[0].body.service[0]["@id"];
        return imageUrl + "/full/" + smallsize.width + ",/0/default.jpg";
    }
}

function addImage(item, index, images) {
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

    let cardDiv = document.createElement("div");
    cardDiv.id = item.id;
    cardDiv.className = "card mb-4";
    //cardDiv.style.width = "18rem";
    if (document.getElementById(item.id) != null) {
        // already added this image so return
        return;
    }
    parentDiv.insertBefore(cardDiv,parentDiv.children[0]);

    if (inProgress) {
        /* <div class="spinner-border" role="status">
             <span class="sr-only">Loading...</span>
           </div> */
        let spinner = document.createElement("div");
        spinner.className = "card-img-top spinner-border";
        spinner.role = "status";
        cardDiv.appendChild(spinner);
        let spinnerText = document.createElement("span");
        spinnerText.className = "sr-only";
        spinnerText.innerHTML = "Loading...";
        spinner.appendChild(spinnerText);
    } else {
        let img = document.createElement("img");
        img.className = "card-img-top";
        img.src = getImageURL(286, 180, item);
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
        cardText.innerHTML = "In Progress";
    } else {
        let infoJsonURL = "";
        if ('type' in item.items[0].items[0].body.service[0] && item.items[0].items[0].body.service[0].type === 'ImageService3') {
            infoJsonURL = item.items[0].items[0].body.service[0].id + "/info.json";
        } else {
            infoJsonURL = item.items[0].items[0].body.service[0]["@id"] + "/info.json";
        }
        cardText.innerHTML = "IIIF image url: <a href='" + infoJsonURL + "'>info.json</a>";

        let viewButton = document.createElement('a');
        viewButton.className = "btn btn-primary";
        viewButton.href = "#";
        viewButton.innerHTML = "View Image";
        cardBody.appendChild(viewButton);
    }
}

function showImages(data) {
    if (data.items.length === 0) {
        showNoImages();
    } else {
        data.items.forEach(addImage); 
    }
}
