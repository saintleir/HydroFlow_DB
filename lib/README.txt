this project needs TWO jar files. add both the same way:

  right-click the project > Build Path > Configure Build Path > Libraries tab
  > Classpath > Add External JARs... > pick the jar > Apply and Close

1. MySQL Connector/J   (example name: mysql-connector-j-8.x.x.jar)
   needed to talk to MySQL. without it the login shows "Cannot connect to MySQL"
   Download: https://dev.mysql.com/downloads/connector/j/  (choose "Platform Independent", unzip it)

2. JFreeChart         (example name: jfreechart-1.5.4.jar)
   needed ONLY by the chart tabs of Reports (ReportCharts.java)
   (the old 1.0.19 version also needs jcommon-1.0.23.jar)
