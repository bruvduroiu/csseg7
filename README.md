# Ad Analytics Dashboard [SEG GROUP 7]

## Overview

This application is part of the Software Engineering Group Project Coursework submission.

The purpose of this application is to provide a clean and useful tool for users that want to
investigate analytics and metrics collected from Ad Auction Campaigns.

The Ad Analytics Dashboard tool provides a graphical view of various metrics, using Line Charts
and Pie Charts.

## Installation & Running

Prerequisites:
    - MongoDB instance on local machine
    (please refer to online documentation for installing MongoDB,
    or refer to the report that can be found in the documentation folder)

Inside the main csseg folder you will find a JAR file named increment1.jar

To run the application, use
```bash
java -cp increment1.jar org.soton.seg7.ad_analytics.view.MainView
```
## Features

    Currently (v1.1.0), the application includes support for:
    * Loading Analytics Data from CSV files using a FileLoader
    * Computing Metrics and Graphs using the data collected above:
        * Cost per Click over time
        * Number of Impressions over time
        * Number of clicks over time
        * Bounce Rate over time
        * Click cost Distribution using Histogram
        * Cost per Thousand Impressions
        * Cost per Acquisition
        * Click through Rate over time
        * Number of conversions over time
        * Total cost over time
    * Filtering the metrics using audience segmentation
        * Age filters
        * Income filters
        * Gender filters
        * Context filters
    * Filtering analytics by date


## Future Work
    - [ ] Using Thread Pools to handle the parsing of large CSV files, for faster parsing
