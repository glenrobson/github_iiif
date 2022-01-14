function addToGallery(galleryId, infoJsonURL, width, height) {
    console.log('looking for ' + galleryId);
    retrieveInfoJson(infoJsonURL, function (infoJson) {
        let URL = getIIIFImageURL(width, height, infoJson);

        var a = document.createElement("a");
        a.href = "plugins/osd/?iiif-content=" + infoJsonURL;

        var img = document.createElement("img");
        a.appendChild(img);
        img.src = URL;

        let gallery = document.getElementById(galleryId)
        gallery.appendChild(a);
        $("#" + galleryId).justifiedGallery('norewind');
    });
}

function retrieveInfoJson(infoJsonURL, callback) {
    fetch(infoJsonURL)
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not OK');
        }
        return response.json();
      })
      .then(data => {
        callback(data);
      })
      .catch(error => {
        console.error('There has been a problem with your fetch operation:', error);
      });
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

