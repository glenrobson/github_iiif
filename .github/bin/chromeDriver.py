import sys
import json
from urllib.request import urlopen
from io import BytesIO
from zipfile import ZipFile
import os

def findPlatform(entries, platform):
    for entry in entries:
        if entry['platform'] == platform:
            return entry['url']

def download_and_unzip(url, extract_to='.'):
    http_response = urlopen(url)
    zipfile = ZipFile(BytesIO(http_response.read()))
    zipfile.extractall(path=extract_to)

if __name__ == "__main__":
    version = "latest"
    if len(sys.args) == 2:
        version = sys.argv[1]


    # store the response of URL
    response = urlopen("https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json")
    
    data = json.loads(response.read())

    chrome_url = ""
    driver_url = ""
    if version == "latest":
        latest = data['versions'][:-1]
        print (f"Using version {latest['version']}")
        chrome_url = findPlatform(latest['chrome'], 'linux64')
        driver_url = findPlatform(latest['chromedriver'], 'linux64')
    else:
        for ver in data['versions']:
            if ver['version'] == version or ver['revision'] == version:    
                chrome_url = findPlatform(ver['chrome'], 'linux64')
                driver_url = findPlatform(ver['chromedriver'], 'linux64')
                break

    app_dir = 'chrome'
    os.mkdir(app_dir)
    print (f"Downloading {chrome_url} to {app_dir}")
    download_and_unzip(chrome_url, app_dir)
    print (f"Downloading {driver_url} to {app_dir}")
    download_and_unzip(driver_url, app_dir)