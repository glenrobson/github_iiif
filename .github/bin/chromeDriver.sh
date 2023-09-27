#!/bin/bash

if [ $# -eq 0 ]; then
    version="119.0.6029.0"
else
    version="$1"
fi
echo "Downloading $version: https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/$version/linux64/chromedriver-linux64.zip"
curl -O "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/$version/linux64/chromedriver-linux64.zip" > chromedriver-linux64.zip
ls
unzip chromedriver-linux64.zip