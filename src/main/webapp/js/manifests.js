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
    div.innerHTML = "<div id='empty' class='list-group-item list-group-item-action'>No manifests found. Upload one by clicking the upload button.</div>"
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
function showManifest(manifest, retrieved) {
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
    let manifest_id = manifest["@id"];

    var li = document.createElement("li");
    if (document.getElementById(manifest_id) != null) {
        li = document.getElementById(manifest_id);
        li.innerHTML = '';
    } else {
        li.id = manifest["@id"];
        li.className = "manifestSummary";
    }

    var thumbnail_img = "";
    var img = document.createElement("img");
    if ('thumbnail' in manifest && manifest.thumbnail) {
        if (typeof manifest.thumbnail === 'string' || manifest.thumbnail instanceof String) {
            thumbnail_img = manifest.thumbnail;
            img.src = thumbnail_img;
        } else if (typeof manifest.thumbnail === 'object' && !Array.isArray(manifest.thumbnail)){
            thumbnail_img = manifest.thumbnail['@id'];
            img.src = thumbnail_img;
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

            $.ajax({
                url: imageId + '/info.json',
                type: 'GET',
                success: function(data) {
                    let URL = getIIIFImageURL(200,100, data);
                    img.src = URL;
                },
                error: function(data) {
                    console.log('Failed to get image ' + imageId + ' due to ' + data);
                }
            });
        }
    }
    
    img.className = "align-self-center mr-3 media-img";

    openImg = document.createElement("a");
    //openImg.href = "view.xhtml?collection=" + activeCollection["@id"] + "&manifest=" + manifest["@id"];</option>
    openImg.className = "align-self-center";
    openImg.appendChild(img);


    mediaHeaderDiv= document.createElement("div");
    mediaHeaderDiv.className = "media-header-div";

    mediaBody = document.createElement("div");
    mediaBody.className = "media-body";

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

    if (retrieved) {
        actionsBar.appendChild(setupContentState(manifest['@id'], "View manifest JSON"));
    }

    if (manifest["@id"].indexOf("github.io") != -1) {
        let urlSplit = manifest["@id"].split("/");
        let user = urlSplit[2].split(".")[0];
        let repo = urlSplit[3];
        let filename = urlSplit[urlSplit.length - 1];
        let path = urlSplit.splice(4).join('/');

        // https://github.com/iiif-test/test3/edit/main/manifests/manifest2.json
        edit = document.createElement("a");
        edit.href = "https://github.com/" + user + "/" + repo + "/edit/main/" + path;
        edit.className = "btn  btn-secondary mb-2";
        edit.innerHTML = '<i class="far fa-edit"></i>';//"Open";
        edit.title = "Edit manifest in GitHub";
        edit.target = "_blank"
        actionsBar.appendChild(edit);

        replace = document.createElement("a");
        replace.className = "btn btn-secondary mb-2";
        replace.innerHTML = '<i class="fas fa-file-upload"></i>';
        replace.title = "Replace Manifest";
        replace.addEventListener("click", function() {
            let name = document.getElementById('replace_name');
            name.value = filename; 
            $('#manifestReplace').modal('show');
        });
        actionsBar.appendChild(replace);

        remove = document.createElement("a");
        remove.className = "btn  btn-secondary mb-2";
        remove.innerHTML = "<i class='far fa-trash-alt'></i>";
        remove.title = "Remove manifest";
        remove.addEventListener("click", function () {
            showConfirmDelete('Delete?', 'Do you want to delete this manifest?', manifest['@id'], deleteManifest);
        });
        actionsBar.appendChild(remove);

        // https://iiif-test.github.io/test3//manifests/manifest2.json
        // https://github.com/iiif-test/test3/tree/main/manifests
        GitHub = document.createElement("a");
        GitHub.href = "https://github.com/" + user + "/" + repo + "/tree/main/" + path;
        GitHub.className = "btn btn-secondary mb-2";
        GitHub.innerHTML = '<img class="logo_button" src="/images/GitHub-logo-light.png"/>';
        GitHub.title = "View on GitHub";
        GitHub.target = "_blank"
        actionsBar.appendChild(GitHub);
    }
    mirador = document.createElement("a");
    mirador.className = "btn btn-secondary mb-2";
    if (retrieved) {
        mirador.href = "https://projectmirador.org/embed/?iiif-content=" +  manifest["@id"]
        mirador.innerHTML = '<img class="logo_button" src="/images/mirador-logo.svg"/>';
        mirador.title = "View in Mirador";
        mirador.target = "_blank"
    } else {
        mirador.appendChild(createSpinner());
        mirador.title = "Manifest is publishing...";
    }
    actionsBar.appendChild(mirador);

    uv = document.createElement("a");
    uv.className = "btn btn-secondary mb-2";
    if (retrieved) {
        uv.href = "http://universalviewer.io/examples/#?c=&m=&s=&cv=&manifest=" +  manifest["@id"]
        uv.innerHTML = '<img class="logo_button" src="/images/uv-logo.png"/>';
        uv.title = "View in Universal Viewer";
        uv.target = "_blank"
    } else {
        uv.appendChild(createSpinner());
        uv.title = "Manifest is publishing...";
    }
    actionsBar.appendChild(uv);

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

function createSpinner() {
    /* <div class="spinner-border" role="status">
          <span class="sr-only">Loading...</span>
        </div> */
    let div = document.createElement("div");
    div.className = "spinner-border";
    div.role = "status";

    let span = document.createElement("span");
    span.className = "sr-only";
    span.innerHTML = "Loading...";
    div.appendChild(span);

    return div;
}

function addManifest(item, index, manifests, lastmod=null) {
    let url = "";
    if ('id' in item) {
        url = item.id;
    } else {
        url = item["@id"];
    }

    var config = {
      method: 'GET',
      mode: 'cors',
    };

    fetch(url, config)
      .then(function(response) {
            if (!response.ok) {
                if (response.status === 404) {
                    if (url.indexOf('?') != -1) {
                        url = url.split('?')[0];
                    }
                    item.id = url + "?" + performance.now();
                    // wait then try again
                    setTimeout(addManifest, 1000, item, index, manifests);
                    throw new Error('Manifest not deployed yet ' + url);
                } else {
                    throw new Error('Failed to get "' + url + '". Got response ' + response.status);
                }
            }
            if (lastmod != null && lastmod === response.headers.get('last-modified')) {
                console.log('Not updated ' + url);
                if (url.indexOf('?') != -1) {
                    url = url.split('?')[0];
                }
                item.id = url + "?" + performance.now();
                // If etag is the same then request again
                setTimeout(addManifest, 1000, item, index, manifests, lastmod);
                throw new Error('Manifest not updated yet ' + url);
            }
            console.log('Got new a response. Original ' + lastmod + ' current ' + response.headers.get('last-modified') + ' from URL ' + url);
            return response.json();
      }).then(function (data) {
            if (url.indexOf('?') != -1) {
                if ('@id' in item) {
                    delete item.id;
                } else {
                    item.id = url.split('?')[0];
                }
            }
            showManifest(data,true);
      }).catch(function (error) {
            console.log('Failed to get manifest ' + url + ' due to ' + error.message);
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
    //a.onclick = copyClipboard;
    a.target = "_blank";

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

function deleteManifest(event) {
    let manifestURI = event.srcElement.getAttribute('data-url');
    let splitURI = manifestURI.split('/');
    let project = event.srcElement.getAttribute('data-project');
    $.ajax({
        url: '/upload/' + project + '/manifests/' + splitURI[splitURI.length - 1],
        type: 'DELETE',
        success: function(result) {
            let manifestDiv = document.getElementById('manifests');
            let manifest = document.getElementById(result.id);
            manifestDiv.removeChild(manifest);
            $('#confirmdialog').modal('hide')
        }
    });
}
