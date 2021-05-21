# GitHub IIIF

This is a WebApp that allows users to store IIIF images, manifests, collections and annotations on their own GitHub repository. Users login with their GitHub account and create a Jekyll Repository automatically configured to deploy to a GitHub pages site.

This can allow users to publish manifests and images which are accessible with the correct CORS headers and https support. The image support is provided by creating a level0 set of tiles that work with most modern IIIF viewers.

This project is heavily inspired by the excellent [AudiAnnotate](https://github.com/hipstas/AudiAnnotate) project which works in a similar way for hosting Audio annotations and [Annonatate](https://github.com/dnoneill/annonatate) which allows you to create Annotations hosted on GitHub.

## User Documentation

This project is being used to support the IIIF training and there are the following guides:

 * [Uploading Images](https://training.iiif.io/iiif-online-workshop/day-two/image-servers/level0-workbench.html)
 * [Uploading Manifests](https://training.iiif.io/iiif-online-workshop/day-three/workbench/)
 * [Uploading Annotations](https://training.iiif.io/iiif-online-workshop/day-four/workbench.html)

## Example Projects

You can see the projects that people have created using this tool by browsing the following topic:
 * [iiif-training-workbench](https://github.com/topics/iiif-training-workbench)
