

#Automation Framework

#####Java + Selenium Webdriver + Maven + TestNG

- Java JDK 1.8 is used

- Selenium Webdriver 3.12.0 is used

- Maven is used as Build automation and dependency management tool, pom.xml has all the dependencies, plugins and build setup required

- TestNG 6.14.3 testing framework is used for test coverage


#####PageFactoryModel

- Classes are created for all the required pages to be traversed in the journey

- All the locators and corresponding functionalities in the page are defined in the individual class pages

- Test class import all the required pages and perform the actions accordingly 

#####Webdriver manager 

- Webdriver manager helps to install the required drivers as specified in the _**DriverType.java**_ file


#####Browser and Driver Requirements

Below browser versions and driver versions are tested 

- Chrome 

Browser | Driver
:---: |: ---:
75.0.3770 | 2.37
"| 2.38
"| 75.0.3770.140
"| 76.0.3809.68


- Firefox (Gecko driver) 

Browser | Driver
:---: |: ---:
68.0.1 | 0.18.0


#### How to specify testcases to run

- Testcases to be run are specified in _**TestNG.xml**_

#### Data driven Testing

- All the required inputs (Area name with postcode) and expected values are provided in _**data.csv**_
- Testcase in _**serviceNswTest.java**_ iterates through all the inputs and validates them against the **expected_value** and then passes the testscript

#### Reporting

- A customised report generated can be seen in the **Report** folder in **target** directory
- A surefire report is generated once the testcases are run
- surefire report is stored in the target folder


#### How to run in local browsers

- Dbrowser param is command lines helps to specify the browser

```bash
mvn clean install -Dbrowser=chrome
```

```bash
mvn clean install -Dbrowser=firefox
```

```bash
mvn clean install -Dbrowser=ie
```

#### How to run headless in chrome and firefox browsers

```bash
mvn clean install -Dbrowser=chrome -Dheadless=true
```

```bash
mvn clean install -Dbrowser=firefox -Dheadless=true
```


#### How to run in Selenium Grid

- Selenium server jar 3.3.0 is placed inside the project
- Run the server using the below lines in the command prompt
- Then Selenium Hub starts running in the local port 4444

```bash
> cd src/test/java/com/qantas/selenium/server_jar/

> java -jar selenium-server-standalone-3.3.0.jar -role hub
```

- Create a node on any port (in below example, created node on port 5555)

```bash
> cd src/test/java/com/qantas/selenium/

> java -Dwebdriver.chrome.driver="drivers//chromedriver" -jar server_jar/selenium-server-standalone-3.3.0.jar -role node -port 5555 -nodeConfig node_config.json
```

- Once node is created, then we can run the tests using the below params

```
clean install -Dremote=true  -DgridURL=http://localhost:5555/wd/hub  -Dplatform=xp  -Dbrowser=chrome  -DbrowserVersion=75.0.3770
```