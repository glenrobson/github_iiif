---
title: OpenSeadragon
description: OpenSeadragon is a zooming IIIF Image Viewer.
---

# OpenSeadragon

OpenSeadragon is a simple zooming viewer which works with IIIF Images. OpenSeadragon is embeded in many of the complex IIIF viewers like Mirador and Universal Viewer but it doesn't support the Presentation API only the Image API.  

<script src="https://cdn.jsdelivr.net/npm/openseadragon@2.4/build/openseadragon/openseadragon.min.js"></script>
<div id="osd" style="height: 500px">
</div>
<script type="text/javascript">
   OpenSeadragon({
                id:            "osd",
                prefixUrl:     "https://cdn.jsdelivr.net/npm/openseadragon@2.4/build/openseadragon/images/",
                sequenceMode:  true,
                tileSources:   [
                    "https://libimages1.princeton.edu/loris/pudl0001%2F4609321%2Fs42%2F00000001.jp2/info.json",
                    "https://libimages1.princeton.edu/loris/pudl0001%2F4609321%2Fs42%2F00000002.jp2/info.json"
                ]
            });
</script>

## Configuration

For configuration options see the documentation on the [OpenSeaDragon site](https://openseadragon.github.io/examples/tilesource-iiif/). To show two images you can
do the following: 

```
<script src="https://cdn.jsdelivr.net/npm/openseadragon@2.4/build/openseadragon/openseadragon.min.js"></script>
<div id="osd" style="height: 500px">
</div>
<script type="text/javascript">
   OpenSeadragon({
                id:            "osd",
                prefixUrl:     "https://cdn.jsdelivr.net/npm/openseadragon@2.4/build/openseadragon/images/",
                sequenceMode:  true,
                tileSources:   [
                    "https://libimages1.princeton.edu/loris/pudl0001%2F4609321%2Fs42%2F00000001.jp2/info.json",
                    "https://libimages1.princeton.edu/loris/pudl0001%2F4609321%2Fs42%2F00000002.jp2/info.json"
                ]
            });
</script>
```

