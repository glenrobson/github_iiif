<script src="{{"/plugins/js/content-state.js" | absolute_url }}"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/openseadragon/3.0.0/openseadragon.min.js" integrity="sha512-Dq5iZeGNxm7Ql/Ix10sugr98niMRyuObKlIzKN1SzUysEXBxti479CTsCiTV00gFlDeDO3zhBsyCOO+v6QVwJw==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>

## OpenSeadragon

This is a demo of the [OpenSeadragon](https://openseadragon.github.io/) viewer. This viewer works with IIIF Image API images and is included in many of the more advanced viewers like Mirador and the UV.

Use your mouse wheel to zoom in or use the + or - minus buttons


<div id="osd" style="width: 100%; height: 586px"></div>

<script type="text/javascript">
    var infoJson = getContentState();
    OpenSeadragon({
            id:                 "osd",
            prefixUrl:          "https://cdnjs.cloudflare.com/ajax/libs/openseadragon/3.0.0/images/",
            preserveViewport:   true,
            visibilityRatio:    1,
            minZoomLevel:       1,
            defaultZoomLevel:   1,
            sequenceMode:       true,
            tileSources:   [infoJson]
        });
</script>
