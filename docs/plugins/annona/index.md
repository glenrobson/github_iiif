<script src="{{"/plugins/js/content-state.js" | absolute_url }}"></script>
<script src="https://ncsu-libraries.github.io/annona/dist/annona.js"></script>
<link rel="stylesheet" type="text/css" href="https://ncsu-libraries.github.io/annona/dist/annona.css">

## Annona Storyboard viewer

This is a demo of the [Annona Storyboard viewer](https://ncsu-libraries.github.io/annona/) by Niqui O'Neill from NC State University Libraries. Annona allows you to navigate through your annotations and can be a useful way of sharing them with others.

Use the left and right arrow keys below to navigate through your annotations.

<div id="storyboard"></div>
        

<script type="text/javascript">
    var annotationList = getContentState();
    if(typeof(annotationList) == "string" && annotationList.length > 0) {
        var div = document.getElementById("storyboard");
        div.innerHTML = "<iiif-storyboard annotationlist='" + annotationList + "'></iiif-storyboard>"
    }
</script>
