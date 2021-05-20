function showAnnosFrom(project) {
    $.ajax({
        url: '/iiif/' + project + '/annotations/collection.json',
        type: 'GET',
        success: function(data) {
            if (data.otherContent.length === 0) {
                showNoAnnos();
            } else {
                data.otherContent.forEach(function (item) {
                    showAnnotation(item, true);
                }); 
            }
        },
        error: function(data) {
            console.log('Failed to get annotations collection.json due to ' + data);
            showNoAnnos();
        }
    });
}
function showNoAnnos() {
    var ul = document.getElementById('anno_list');
    ul.innerHTML = "<li>No Annotations found. Upload one by clicking the upload button.</li>"
}

function showAnnotation(url, retrieved) {
    var ul = document.getElementById('anno_list');
    let li = document.createElement('li');
    if (document.getElementById(url) != null) {
        li = document.getElementById(url);
        li.innerHTML = '';
    } else {    
        ul.appendChild(li);
        li.id = url;
        li.className = "list-group-item list-group-item-action";
    }

    let splitURL = url.split('/');
    let name = splitURL[splitURL.length -1];

    li.innerHTML = "<i class='fa fa-file-o fa-lg'> </i> <span class='filename'>"  + name + "</span>";

    actionsBar = document.createElement("span");
    actionsBar.id = "actionBar";
    actionsBar.className = "miniActionBar";
    li.appendChild(actionsBar);

    let iiifLogo = document.createElement("a");
    iiifLogo.href = url;
    iiifLogo.innerHTML = '<img src="https://iiif.io/img/logo-iiif-34x30.png" draggable="true" dataset-link="' + url + '"/>';//"Open";
    iiifLogo.title = "View annotations";
    iiifLogo.target = "_blank"
    actionsBar.appendChild(iiifLogo);

    if (url.indexOf("github.io") != -1) {
        let urlSplit = url.split("/");
        let user = urlSplit[2].split(".")[0];
        let repo = urlSplit[3];
        let path = urlSplit.splice(4).join('/');

        // https://github.com/iiif-test/test3/edit/main/manifests/manifest2.json
        edit = document.createElement("a");
        edit.href = "https://github.com/" + user + "/" + repo + "/edit/main/" + path;
        edit.className = "btn btn-secondary mb-2";
        edit.innerHTML = '<i class="far fa-edit"></i>';//"Open";
        edit.title = "Edit manifest";
        edit.target = "_blank"
        actionsBar.appendChild(edit);

        remove = document.createElement("a");
        remove.className = "btn  btn-secondary mb-2";
        remove.innerHTML = "<i class='far fa-trash-alt'></i>";
        remove.title = "Remove manifest";
        remove.addEventListener("click", function () {
            showConfirmDelete('Delete?', 'Do you want to delete these annotations?', url, deleteAnnos);
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
}

function deleteAnnos(event) {
    let annoURI = event.srcElement.getAttribute('data-url');
    let splitURI = annoURI.split('/');
    let project = event.srcElement.getAttribute('data-project');
    $.ajax({
        url: '/upload/' + project + '/annotations/' + splitURI[splitURI.length - 1],
        type: 'DELETE',
        success: function(result) {
            let ul = document.getElementById('anno_list');
            let li = document.getElementById(result.id);
            ul.removeChild(li);
            $('#confirmdialog').modal('hide')
        }
    });
}
