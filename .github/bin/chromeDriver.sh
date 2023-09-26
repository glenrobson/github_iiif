#!/bin/bash

version="119.0.6029.0"
echo "Downloading $version"
curl -O "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/$version/linux64/chromedriver-linux64.zip" > chromedriver-linux64.zip
unzip chromedriver-linux64.zip