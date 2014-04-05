Introduction
------------

Ingress Dual Map is an app that runs alongside the official Ingress app, and shows notifications when portals are nearby.  These allow the tracking of distance, hack counts, cooldown times and burnout times, all from within the notification.


Portal lists
------------

Portals can be imported from CSV files placed in an *IngressDualMap* folder on the SD card - one portal per line, in the format `portal name,latitude,longitude`.  Start the service, then select import portal lists to load any CSV files in the folder.  Log files will be generated for any erroneous lists.


Android libraries
-----------------

This project requires the following libraries (adjust *project.properties* to set their locations):

* [appcompat and gridlayout](http://developer.android.com/tools/support-library/setup.html) (from the Android v7 support library)
* [DavidWebb](https://github.com/hgoebl/DavidWebb) (a lightweight HTTP request alternative)
* [OpenCSV](http://opencsv.sourceforge.net/) (easier reading of CSV files)
