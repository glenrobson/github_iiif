
function getElementById(parent, id) {
    for (const child of parent.children) {
        if (child.id === id) {
            return child;
        } else if (child.children) {
            result = getElementById(child, id);
            if (result != null) {
                return result;
            }
        }
    }

    return null;
}

function getElementsByClassName(parent, className) {
    for (const child of parent.children) {
        if (child.className === className) {
            return child;
        } else if (child.children) {
            result = getElementsByClassName(child, className);
            if (result != null) {
                return result;
            }
        }
    }

    return null;
}

function getElementsByTagName(parent, tagName) {
    for (const child of parent.children) {
        if (child.tagName === tagName.toUpperCase()) {
            return child;
        } else if (child.children) {
            result = getElementsByTagName(child, tagName);
            if (result != null) {
                return result;
            }
        }
    }

    return null;
}

function getElementsByName(parent, name) {
    for (const child of parent.children) {
        if (child.name === name) {
            return child;
        } else if (child.children) {
            result = getElementsByName(child, name);
            if (result != null) {
                return result;
            }
        }
    }

    return null;
}

function getElementsByTitle(parent, title) {
    for (const child of parent.children) {
        if (child.title === title) {
            return child;
        } else if (child.children) {
            result = getElementsByTitle(child, title);
            if (result != null) {
                return result;
            }
        }
    }

    return null;
}

function getId(manifest){
    if ('@id' in manifest) {
        return manifest['@id']; 
    } else {
        return manifest.id;
    }
}

function setupDOM() {
    let manifest_div = document.createElement('div');
    manifest_div.id = 'manifests_div';
    document.getElementById = jasmine.createSpy('HTML Element').and.callFake(function(ID) {
        if(ID === 'manifests_div') {
           return manifest_div;
        } else {
            return null;
        }
     });
     return manifest_div;
}

describe("V2 Manifest", function() {
    jasmine.getFixtures().fixturesPath = '../src/test/resources/json-ld'
    let manifest = JSON.parse(jasmine.getFixtures().read('v2_manifest.json'));
    let manifest_id = getId(manifest);
    it("Finding manifest id v2", function() {
        expect(manifest_id).toBe("https://iiif-test.github.io/Presentation2023/manifests/manifest-combined.json");
    });

    let manifest_div = setupDOM();
    showManifest(manifest, true);

    it("Finding manifest div", function() {
        expect(getElementById(manifest_div, manifest_id).className).toBe("manifestSummary");
    });
    
    let thumb = getElementsByClassName(manifest_div, 'align-self-center mr-3 media-img');
    if (thumb.src) {
        it("Checking thumbnail", function() {
            expect(thumb.src).toBe("https://iiif-test.github.io/Presentation2023/images/vdc_100121318680.0x000001/full/200,/0/default.jpg");
        });
    }

    let label = getElementsByTagName(manifest_div, "h5");
    it("Checking label", function() {
        expect(label.innerHTML).toBe("[Click to edit label]");
    });

    let description = getElementsByName(manifest_div, "description");
    it("Checking description", function() {
        expect(description.innerHTML).toBe("[Click to edit description]");
    });

    let attribution = getElementsByName(manifest_div, "attribution");
    it("Checking attribution", function() {
        expect(attribution.innerHTML).toBe("[Click to edit attribution]");
    });

    let edit = getElementsByTitle(manifest_div, "Edit manifest in GitHub");
    it("Checking Mirador link", function() {
        expect(edit.href).toBe("https://github.com/iiif-test/Presentation2023/edit/main/manifests/manifest-combined.json");
    });

    let mirador = getElementsByTitle(manifest_div, "View in Mirador");
    it("Checking Mirador link", function() {
        expect(mirador.href).toBe("https://projectmirador.org/embed/?iiif-content=https://iiif-test.github.io/Presentation2023/manifests/manifest-combined.json");
    });

    let uv = getElementsByTitle(manifest_div, "View in Universal Viewer");
    it("Checking UV link", function() {
        expect(uv.href).toBe("http://universalviewer.io/examples/#?c=&m=&s=&cv=&manifest=https://iiif-test.github.io/Presentation2023/manifests/manifest-combined.json");
    });

    let logo = getElementsByClassName(manifest_div, "logo");
    it("Checking logo", function() {
        expect(logo.src).toBe("http://example.org/logos/institution1.jpg");
    });

});

describe("V3 Manifest", function() {
    jasmine.getFixtures().fixturesPath = '../src/test/resources/json-ld'
    let manifest = JSON.parse(jasmine.getFixtures().read('v3_manifest.json'));
    let manifest_id = getId(manifest);
    
    it("Finding manifest id v3", function() {
        expect(manifest_id).toBe("https://iiif-test.github.io/test2/images/manifest_v3.json");
    });

    let manifest_div = setupDOM();
    showManifest(manifest, true);

    it("Finding manifest div", function() {
        expect(getElementById(manifest_div, manifest_id).className).toBe("manifestSummary");
    });


    let thumb = getElementsByClassName(manifest_div, 'align-self-center mr-3 media-img');
    if (thumb.src) {
        it("Checking thumbnail", function() {
            expect(thumb.src).toBe("https://iiif-test.github.io/test2/images/IMG_8669/full/252,/0/default.jpg");
        });
    }

    let label = getElementsByTagName(manifest_div, "h5");
    it("Checking label", function() {
        expect(label.innerHTML).toBe("All images loaded in iiif-test/test2 project");
    });

    let description = getElementsByName(manifest_div, "description");
    it("Checking description", function() {
        expect(description.innerHTML).toBe("This is a summary of the object.");
    });

    let attribution = getElementsByName(manifest_div, "attribution");
    it("Checking attribution", function() {
        expect(attribution.innerHTML).toBe("Provided courtesy of Example Institution");
    });

    let edit = getElementsByTitle(manifest_div, "Edit manifest in GitHub");
    it("Checking Mirador link", function() {
        expect(edit.href).toBe("https://github.com/iiif-test/test2/edit/main/images/manifest_v3.json");
    });

    let mirador = getElementsByTitle(manifest_div, "View in Mirador");
    it("Checking Mirador link", function() {
        expect(mirador.href).toBe("https://projectmirador.org/embed/?iiif-content=https://iiif-test.github.io/test2/images/manifest_v3.json");
    });

    let uv = getElementsByTitle(manifest_div, "View in Universal Viewer");
    it("Checking UV link", function() {
        expect(uv.href).toBe("http://universalviewer.io/examples/#?c=&m=&s=&cv=&manifest=https://iiif-test.github.io/test2/images/manifest_v3.json");
    });

    let logo = getElementsByClassName(manifest_div, "logo");
    it("Checking logo", function() {
        expect(logo.src).toBe("https://example.org/images/logo.png");
    });
});