
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
    it("Checking thumbnail", function() {
        expect(thumb.src).toBe("https://iiif-test.github.io/Presentation2023/images/vdc_100121318680.0x000001/full/200,/0/default.jpg");
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
    it("Checking thumbnail", function() {
        expect(thumb.src).toBe("https://iiif-test.github.io/test2/images/IMG_8669/full/252,/0/default.jpg");
    });
});