
// From Getty code: http://www.getty.edu/art/collection/static/viewers/mirador/?manifest=https://data.getty.edu/museum/api/iiif/1895/manifest.json
function getURLParameter(param) {
    if(typeof(param) == "string" && param.length > 0) {
        if(typeof(window.location.search) == "string" && window.location.search.length > 0) {
            var _results = new RegExp(param + "=([^&]*)", "i").exec(window.location.search);
            if(typeof(_results) == "object" && _results !== null && typeof(_results.length) == "number" && _results.length > 0 && _results[1]) {
                if(typeof(_results[1]) == "string" && _results[1].length > 0) {
                    return unescape(_results[1]);
                }
            }
        }
    }
    return null;
}
function ajaxForm(config) {
    var original = setLoading(config.action.button, config.action.verb);
    var form=document.getElementById(config.form);
    $.ajax({
        url: form.action,
        data: $("form").serialize(),
        type: 'POST',
        success: function(data) {
            showMessage("info", data.message);
            clearLoading(config.action.button, original);
            if (config.success) {
                config.success(data);
            }
        },
        error: function(data) {
            if (data.status === 500) {
                showMessage("error", data.statusText);
            } else {
                showMessage("error", data.message);
            }
            clearLoading(config.action.button, original);
        }
    });
    return false;
}

function hideMessage() {
    hideMessage('messages');
}

function hideMessage(messageId) {
    var messages = document.getElementById(messageId);
    messages.textContent = '';
    messages.style.display = 'none';
}

function showMessage(messageType, message) {
    showMessage("messages", messageType, message);
}

function showMessage(messageId, messageType, message) {
    var messages = document.getElementById(messageId);
    messages.textContent = message;
    if (messageType === "info") {
        messages.className = "alert alert-info";
    } else {
        messages.className = "alert alert-danger";
    }
    messages.style.display = 'block';
}

function setLoading(buttonName, verb) {
    var button = document.getElementById(buttonName);
    var orig = button.innerHTML;
    button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>' + verb + '...';
    button.disabled = true;
    return orig;
}

function clearLoading(buttonName, content) {
    var button = document.getElementById(buttonName);
    button.innerHTML = content;
    button.disabled = false;
}

function showProjectForm() {
    hideMessage("createProjectMessage");
    $('#createProject').modal('toggle');
    return false;
}

function createProject() {
    setLoading("createProjectButton", "Creating");
    $.ajax({
        url: '/project/',
        data: $("#project_form").serialize(),
        type: 'POST',
        success: function(data) {
            showMessage("createProjectMessage", "info", "Created project succesfully.");

            $("#scroll").mCustomScrollbar('update');
            clearLoading("createProjectButton", "Create");
            $('#createProject').modal('toggle');
            window.location.href = "/projects/" + data.name + "/images";
        },
        error: function(data) {
            clearLoading("createProjectButton", "Create");
            if (data.responseJSON) {
                if ("reason" in data.responseJSON) {
                    message = "Failed to add project due to: " + data.responseJSON.reason;
                } else if ("message" in data.responseJSON) {
                    message = "Failed to add project due to: " + data.responseJSON.message;
                } else {
                    message = "Failed to add project.";
                }
            } else if ("statusText" in data) {    
                message = "Failed to add project due to: " + data.statusText;
            } else {
                message = "Failed to add project.";
            }
            showMessage("createProjectMessage", "error", message);
            console.log('Failed to load project: ' + data);
        }
    });
}

function fileSelected(name) {
    let fileInput = document.getElementById(name);
    if (fileInput.files.length === 1) {
        let id = "";
        if (fileInput.files[0].name.indexOf(".json") == -1) {
             id = fileInput.files[0].name.replace(/\.[^/.]+$/, "");
        } else {
            id = fileInput.files[0].name;
        }
        // 100 MB
        const mbNum = 1000000
        const maxAllowedSize = 100 * mbNum;
        if (fileInput.files[0].size > maxAllowedSize) {
            showMessage("image_messages", "error", "Image is too big at " + (fileInput.files[0].size / mbNum).toFixed(2) + " MB. Current max size is: " + (maxAllowedSize / mbNum) + " MB.");
        }

        let nameInput = document.getElementById('name');
        nameInput.value = id;

        nameInput.parentElement.parentElement.style.display = "flex";
    }
}

function showConfirmDelete(title, message, url, onClose) {
    let titleEl = document.getElementById('confirm.title');
    titleEl.innerHTML = title;

    let messageEl = document.getElementById('confirm.message');
    messageEl.innerHTML = message;

    let button = document.getElementById('confirm.button');
    button.addEventListener("click", onClose);
    button.setAttribute('data-url', url);
     
    $('#confirmdialog').modal('show')
}

// Return URL to IIIF image which matches or is bigger than supplied width and height
function getIIIFImageURL(width, height, infoJson) {
    let imageUrl = "";
    let version = "2";
    if ('type' in infoJson && infoJson.type === 'ImageService3') {
        imageUrl = infoJson.id;
        version = "3";
    } else {
        imageUrl = infoJson["@id"];
    }

    if ('sizes' in infoJson) {
        let sizes = infoJson.sizes;
        let smallsize = { width: infoJson.width, height: infoJson.height};
        for (let i = 0; i < sizes.length; i++) {
            let size = sizes[i];
            if (size.width > width && size.height > height) { 
                if (size.width < smallsize.width && size.height < smallsize.height) {
                    smallsize.width = size.width;
                    smallsize.height = size.height;
                }
            }
        }
        if (version === "2") {
            return imageUrl + "/full/" + smallsize.width + ",/0/default.jpg";
        } else {
            return imageUrl + "/full/" + smallsize.width + "," + smallsize.height + "/0/default.jpg";
        }
    } else {
        if (Array.isArray(infoJson.profile) && (infoJson.profile.includes('http://iiif.io/api/image/2/level2.json') ||  infoJson.profile.includes('sizeByConfinedWh')) && imageUrl.indexOf('archivelab.org') == -1) {
            // If no sizes but level2 we can use the fit syntax
            return imageUrl + "/full/!" + width + "," + height + "/0/default.jpg"; 
        } else if (infoJson.profile === 'level2') {
            return imageUrl + "/full/!" + width + "," + height + "/0/default.jpg"; 
        }    
        // if level 1 and exact size not supported get best fit by finding smallest dimension 
        if (width > height) {
            return imageUrl + "/full/," + height + "/0/default.jpg"; 
        } else {
            return imageUrl + "/full/" + width + ",/0/default.jpg"; 
        }
    }
}

