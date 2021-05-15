function showManifestsFrom(project) {
    $.ajax({
        url: '/iiif/' + project + '/manifests/collection.json',
        type: 'GET',
        success: function(data) {
            showManifests(data);
        },
        error: function(data) {
            console.log('Failed to get manifests collection.json due to ' + data);
            showNoManifests();
        }
    });

}

function showNoManifests() {
    var div = document.getElementById('manifests_div');
    div.innerHTML = "No manifests found. Upload one by clicking the upload button."
}

function findValue(parentnode, key) {
    var response = '';
    if (typeof parentnode[key] === 'object' && !Array.isArray(parentnode[key]) && parentnode[key]["@value"]) {
        response = parentnode[key]["@value"];
    } else if (Array.isArray(parentnode[key]) && parentnode[key].length > 0) {
        if (typeof parentnode[key][0] === 'string') {
            response = parentnode[key][0];
        } else {
            // attribution is a list of objects
            if (parentnode[key][0]["@value"]) {
                response = parentnode[key][0]["@value"];
            }
        }
    } else if (typeof parentnode[key] === 'string') {
        response = parentnode[key];
    }
    return response;
}
function showManifest(manifest) {
    let manifestDiv = document.getElementById('manifests_div');
    let ul = null;
    if (manifestDiv.children.length != 1) {
        manifestDiv.innerHTML = '';
        ul = document.createElement('ul');
        ul.id="manifests";
        manifestDiv.appendChild(ul);
    } else {
        ul = manifestDiv.children[0];
    }
    var li = document.createElement("li");
    li.className = "manifestSummary";

    var thumbnail_img = "";
    if ('thumbnail' in manifest && manifest.thumbnail) {
        if (typeof manifest.thumbnail === 'string' || manifest.thumbnail instanceof String) {
            thumbnail_img = manifest.thumbnail;
        } else if (typeof manifest.thumbnail === 'object' && !Array.isArray(manifest.thumbnail)){
            thumbnail_img = manifest.thumbnail['@id'];
        }
    } else {
        // Get image service from first canvas
        if (manifest.sequences && Array.isArray(manifest.sequences) && manifest.sequences[0].canvases && Array.isArray(manifest.sequences[0].canvases)
                && manifest.sequences[0].canvases[0].images && Array.isArray(manifest.sequences[0].canvases[0].images)
                 && manifest.sequences[0].canvases[0].images[0].resource && typeof manifest.sequences[0].canvases[0].images[0].resource === 'object'
                    && manifest.sequences[0].canvases[0].images[0].resource.service && typeof manifest.sequences[0].canvases[0].images[0].resource.service === 'object'
                        && manifest.sequences[0].canvases[0].images[0].resource.service["@id"] && typeof manifest.sequences[0].canvases[0].images[0].resource.service["@id"] === 'string') {
            var imageId = manifest.sequences[0].canvases[0].images[0].resource.service["@id"];
            thumbnail_img = imageId + '/full/,100/0/default.jpg';
        }
    }
    
    var img = document.createElement("img");
    img.className = "align-self-center mr-3 media-img";
    img.src = thumbnail_img;

    openImg = document.createElement("a");
    //openImg.href = "view.xhtml?collection=" + activeCollection["@id"] + "&manifest=" + manifest["@id"];</option>
    openImg.className = "align-self-center";
    openImg.appendChild(img);


    mediaHeaderDiv= document.createElement("div");
    mediaHeaderDiv.className = "media-header-div";

    mediaBody = document.createElement("div");
    mediaBody.className = "media-body";

    remove = document.createElement("button");
    remove.type = "button";
    remove.className = "btn  btn-secondary mb-2";
    remove.innerHTML = "<i class='far fa-trash-alt'></i>";
    remove.setAttribute('aria-hidden', 'true');
    remove.dataset.mode = 'remove_manifest';
    remove.dataset.manifest = manifest["@id"];
    //remove.onclick = showConfirm;
   // mediaBody.appendChild(remove);

    mediaHeader = document.createElement("div");
    mediaHeader.className = "media-heading";

    heading = document.createElement("h5");
    mediaHeader.appendChild(heading);
    if ('label' in manifest && manifest.label) {
        heading.innerHTML = findValue(manifest, "label");
    } else {
        heading.innerHTML = "Missing Manifest label";
    }
    //mediaBody.appendChild(mediaHeader);

    if ('description' in manifest && manifest.description) {
        mediaContent = document.createElement("p");
        mediaContent.className = "";
        mediaContent.innerHTML = findValue(manifest, "description");
        mediaBody.appendChild(mediaContent);
    }

    if ('attribution' in manifest && manifest.attribution) {
        var attribution = findValue(manifest, "attribution");

        mediaContent = document.createElement("p");
        mediaContent.className = "";
        mediaContent.innerHTML = attribution;
        mediaBody.appendChild(mediaContent);
    }

    actionsBar = document.createElement("div");
    actionsBar.id = "actionBar";
    mediaBody.appendChild(actionsBar);

    actionsBar.appendChild(setupContentState(manifest["@id"], "Link to manifest. Also draggable using IIIF Content State."));

    open = document.createElement("a");
    //open.href = "view.xhtml?collection=" + activeCollection["@id"] + "&manifest=" + manifest["@id"];</option>
    open.className = "btn  btn-secondary mb-2";
    open.innerHTML = '<i class="far fa-edit"></i>';//"Open";
    open.title = "Open manifest for editing";
    actionsBar.appendChild(open);

    move = document.createElement("button");
    move.type = "button";
    move.className = "btn btn-secondary mb-2";
    move.innerHTML = '<i class="far fa-folder-open"></i>';
    move.title = "Move manifest to another collection.";
    //move.onclick = showMoveManifest;
    move.dataset.manifest = manifest["@id"];
    actionsBar.appendChild(move);
    actionsBar.appendChild(remove);

    download = document.createElement("a");
    download.href = "manifest.xhtml?iiif-content=" + manifest["@id"];
    download.className = "btn btn-secondary mb-2";
    download.innerHTML = '<i class="fas fa-cloud-download-alt"></i>';
    download.title = "Export";
    actionsBar.appendChild(download);

    analytics = document.createElement("a");
    analytics.href = "stats/manifest.xhtml?iiif-content=" + manifest["@id"];
    analytics.className = "btn btn-secondary mb-2";
    analytics.innerHTML = '<i class="fas fa-chart-line"></i>';
    analytics.title = "Analytics";
    actionsBar.appendChild(analytics);


    if ('logo' in manifest) {
        var tURL = "";
        if (typeof manifest.logo === 'object' && '@id' in manifest.logo) {
            tURL = manifest.logo['@id'];
        } else if (typeof manifest.logo === 'string') {
            tURL = manifest.logo;
        }
        if (tURL) {
            var logo = document.createElement("img");
            logo.className = "logo";
            logo.src= tURL;
            /*actionsBar.appendChild(logo);
            mediaHeaderDiv.appendChild(logo)*/

           /* logoBar = document.createElement("div");
            logoBar.id = "logoBar";
            mediaBody.appendChild(logoBar);
            logoBar.appendChild(logo);*/
            mediaHeader.insertBefore(logo, mediaHeader.childNodes[0]);
        }

    }
    //mediaHeaderDiv.appendChild(remove)

   // mediaBody.appendChild(remove);
    //mediaBody.appendChild(mediaHeader);

    cardDiv = document.createElement("div");
    cardDiv.className = "media";
    cardDiv.appendChild(openImg);
    cardDiv.appendChild(mediaBody);

    li.appendChild(mediaHeaderDiv);
    li.appendChild(mediaHeader)
    li.appendChild(cardDiv);
    ul.appendChild(li);
}

function addManifest(item, index, manifests) {
    let url = "";
    if ('id' in item) {
        url = item.id;
    } else {
        url = item["@id"];
    }
    $.ajax({
        url: url,
        type: 'GET',
        success: function(data) {
            showManifest(data);
        },
        error: function(data) {
            console.log('Failed to get manifest ' + url + ' due to ' + data);
        }
    });

}

function showManifests(data) {
    if (data.items.length === 0) {
        showNoManifests();
    } else {
        data.items.forEach(addManifest); 
    }
}


function setupContentState(uri, description) {
    var a = document.createElement("a");
    a.href = uri;
    a.title = description;

    var img = document.createElement("img");
    img.src = "https://iiif.io/img/logo-iiif-34x30.png";
    img.draggable = true;
    img.ondragstart = drag;
    img.dataset.link = uri;

    a.appendChild(img);
    a.onclick = copyClipboard;

    return a;
}

function drag(ev) {
    ev.dataTransfer.setData("text/plain", ev.srcElement.dataset.link);
} 

function copyClipboard(event) {
    var data = document.createElement("input");
    data.style = "position: absolute; left: -1000px; top: -1000px";
    document.body.appendChild(data);
    data.value = event.srcElement.dataset.link;
    data.select();
    try {
        document.execCommand("copy");
        event.preventDefault();
        document.body.removeChild(data);
        document.getElementById("copyTitle").innerHTML = "Copied to Clipboard!";
        document.getElementById("copyURI").innerHTML = event.srcElement.dataset.link;
        $('#copy').modal('toggle');
        return true;
    } catch (error) {
        console.log("Failed to copy url due to: " + error);
    }
    document.body.removeChild(data);
    return true;
}


