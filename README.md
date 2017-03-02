# Ad Analytics Dashboard [SEG GROUP 7]

## Overview

This application is part of the Software Engineering Group Project Coursework submission.

The purpose of this application is to provide a clean and useful tool for users that want to
investigate analytics and metrics collected from Ad Auction Campaigns.

The Ad Analytics Dashboard tool provides a graphical view of various metrics, using Line Charts
and Pie Charts.

## Installation

Download the ZIP file provided in the repository under Release folder.
Inside you will find a JAR file named csseg7.jar

To run the application, use
```bash
java -cp /out/artifacts/analyticscsseg7.jar org.soton.seg7.ad_analytics.view.MainView
```

## Features

Currently (v1.0.0), the application includes support for:
* Loading Analytics Data from CSV files using a FileLoader
* Computing Metrics and Graphs using the data collected above:
    * Cost per Click over time
    * Number of Impressions over time
    * Number of clicks over time
    * Click through Rate over time
    * Number of conversions over time
    * Total cost over time

## Future Work

- [ ] Using Thread Pools to handle the parsing of large CSV files, for faster parsing
- [ ] Adding filtering capabilities
