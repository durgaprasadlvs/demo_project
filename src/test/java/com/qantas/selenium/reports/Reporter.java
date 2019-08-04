
package com.qantas.selenium.reports;

import com.qantas.selenium.DriverBase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class Reporter extends TestListenerAdapter {

    ITestResult testResult;

    private Properties userProperties;

    WebDriver driver;

    private int passed = 0, skipped = 0, totalTests = 0, unknownFails = 0, testExecutionCount = 0, totalNumOfTestsToRun = 0;

    private static long overallDuration = 0;

    private static long startTime = 0;

    private static long endTime = 0;

    private final ArrayList<String> failedScripts = new ArrayList<String>();

    private final ConcurrentHashMap<String, List<String>> passedScripts = new ConcurrentHashMap<String, List<String>>();
    private final ConcurrentHashMap<String, List<String>> testfailures = new ConcurrentHashMap<String, List<String>>();
    private final ConcurrentHashMap<String, List<String>> skippedScripts = new ConcurrentHashMap<String, List<String>>();

    private static String SuiteName;
    private static String ResultValue;

    public Reporter() throws IOException {
        userProperties = new Properties();
        userProperties.load(DriverBase.class.getResourceAsStream("user.properties"));
    }

    /**
     * Overrides TestNG method for any tests which throw the TestNG Skipped
     * exception. Method adds the test script name, reason for skipping and test
     * duration to the skippedScripts hashmap
     */
    @Override
    public synchronized void onTestSkipped(final ITestResult testResult) {

        this.testResult = testResult;

        skipped += 1;

        String testScriptName = getTestName();

        final String testDescription = testResult.getMethod().getDescription();

        final String reasonForSkipping = testResult.getThrowable().getMessage();

        skippedScripts.put(testScriptName, Arrays.asList(reasonForSkipping, testDescription));

    }

    /**
     * Calculates the total number of scripts which will be run in the test
     * suite and splits them up into Individual Scripts and Data Driven scripts.
     * The counts are then outputted in the Console window
     */
    @Override
    public void onStart(final ITestContext testContext) {

        setSuiteName(testContext.getSuite().getName().toString());

        final Date date = new Date();

        startTime = date.getTime();

        totalNumOfTestsToRun = testContext.getAllTestMethods().length;

        System.out.println("Total number of scripts in the Test Suite : " + totalNumOfTestsToRun);
    }

    /**
     * Outputs the Test Script name and number into the Console window
     */
    @Override
    public void onTestStart(final ITestResult testResult) {
        testExecutionCount++;
        System.out.println("Script [" + testExecutionCount + " of " + totalNumOfTestsToRun + "]");
    }

    /**
     * Overrides TestNG method for a successful test. Method adds test script
     * name, the duration and test description to the PassedScripts hashmap
     */
    @Override
    public synchronized void onTestSuccess(final ITestResult testResult) {

        this.testResult = testResult;
        passed += 1;
        String testScriptName = getTestName();
        String fileName = getFilename(testResult);
        final String testDescription = testResult.getMethod().getDescription();
        final String testDuration = calcTestDuration(testResult.getStartMillis(), testResult.getEndMillis());
        passedScripts.put(testScriptName, Arrays.asList(testDuration, testDescription, fileName, getResultValue()));
        super.onTestSuccess(testResult);
    }

    /**
     * Overrides TestNG method for a failed test. Method determines the type of
     * failure i.e. Unknown or Environment
     */
    @Override
    public synchronized void onTestFailure(final ITestResult testResult) {
        this.testResult = testResult;
        genScreenShot(testResult);
        final String thrownExceptionMsg = testResult.getThrowable().toString();
        final String testDuration = calcTestDuration(testResult.getStartMillis(), testResult.getEndMillis());
        final Date date = new Date();
        final String timeOfFailure = date.toString();
        final String testDescription = testResult.getMethod().getDescription();
        final String thrownException = testResult.getThrowable().toString();
        final String stackTrace = highlightStackTrace();
        final String pageObjectFailure = getPageObject();
        String formattedTestName = getTestName();
        String testName = getFilename(testResult);
        addToFailedScriptList();

        unknownFails += 1;

        testfailures.put(formattedTestName,
                Arrays.asList(testDuration, pageObjectFailure, stackTrace, testDescription, timeOfFailure, testName, thrownException));

        genStackTrace(stackTrace, testName, timeOfFailure, thrownException);

        super.onTestFailure(testResult);
    }

    @Override
    public void onFinish(final ITestContext testContext) {

        final Date date = new Date();
        endTime = date.getTime();
        totalTests = passed + unknownFails + skipped;
        generateHTMLReport(testContext);
        outputFailuresToTextFile();
        File srcDir = new File(System.getProperty("user.dir") + "/target");
        File destDir = new File(System.getProperty("user.dir") + "/target_" + System.currentTimeMillis());
        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        super.onFinish(testContext);
    }

    /**
     * Calculates the Percentage ran as an overall
     *
     * @param v
     * @param t
     * @return
     */

    private static String value(final int v, final int t) {
        double a = 0.00;
        double p = 0.00;

        if (v > 0) {
            p = percent(v, t);
            a = round(p, 0, BigDecimal.ROUND_HALF_UP);
        }

        final DecimalFormat df = new DecimalFormat("#");
        final String percentage = df.format(a);

        return percentage;
    }

    private static double percent(final int total, final int fail) {
        final double p = ((double) fail / total) * 100;

        return p;
    }

    private static double round(final double unrounded, final int precision, final int roundingMode) {
        final BigDecimal bd = new BigDecimal(unrounded);
        final BigDecimal rounded = bd.setScale(precision, roundingMode);

        return rounded.doubleValue();
    }

    /**
     * Calculates test duration
     *
     * @param s
     * @param e
     * @return
     */
    private static String calcTestDuration(final long s, final long e) {

        Double duration = (double) (e - s) / 1000;

        duration = round(duration, 2, BigDecimal.ROUND_HALF_UP);

        final String convertedDuration = convertSecondsToHrsMinsSecs(duration);

        overallDuration += duration;

        return convertedDuration;
    }

    /**
     * Converts seconds into hrs mins and seconds
     *
     * @param seconds
     * @return String
     */
    private static String convertSecondsToHrsMinsSecs(final double seconds) {

        String convertedTime;

        if (seconds > 3599) {
            // Add hours
            double hrs = seconds / 3600;
            double secs = seconds % 3600;
            double mins = secs / 60;

            if (mins > 59) {
                hrs = hrs + 1;
                mins = 0;
            }

            secs = secs % 60;

            convertedTime = (int) hrs + " hr " + (int) mins + " min " + (int) secs + " sec";
        } else {
            final double mins = seconds / 60;
            final double remainder = seconds % 60;
            convertedTime = (int) mins + " min " + (int) remainder + " sec";
        }

        return convertedTime;
    }

    /**
     * Generates the full HTML report
     *
     * @param testContext
     * @throws IOException
     */
    private void generateHTMLReport(final ITestContext testContext) {

        final StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder
                .append("<!doctype html><html lang=\"en\"><head><link rel=\"stylesheet\" type=\"text/css\" href=\"");

        htmlBuilder.append(userProperties.getProperty("stylesheetCSSPath"));


        htmlBuilder.append("\"><meta charset=\"utf-8\"><title>Report Output</title>");


        htmlBuilder.append("<meta name=\"description\" content=\"Test Output\">");

        htmlBuilder.append("<script type=\"text/javascript\" src=\"");

        htmlBuilder.append(userProperties.getProperty("jqueryPath1"));

        htmlBuilder.append("\"></script>");

        htmlBuilder.append("<script type=\"text/javascript\" src=\"");

        htmlBuilder.append(userProperties.getProperty("jqueryPath2"));

        htmlBuilder.append("\"></script>");

        htmlBuilder.append("<script> $(function(){$(\"#testfailures\").tablesorter(); $(\"#Passed\").tablesorter();});</script>");

        htmlBuilder.append("</head><body class=\"test\">");

        htmlBuilder.append("<div class=\"mainPage\"><p>QA Automation Test Results</p><br>");

        htmlBuilder.append("<p id=\"suiteTitle\">" + testContext.getSuite().getName() + "</p><br>");

        htmlBuilder.append("<h5>" + getExecutionDateAndTime() + "</h5>");

        htmlBuilder.append("<h1 class=\"ir\" id=\"logo\"><img class=\"logo\" src=\"");

        htmlBuilder.append(userProperties.getProperty("LogoPath"));

        htmlBuilder.append("\" alt=\"logo\"></h1></div>");

        htmlBuilder.append("<div id=\"container\"><div class=\"panels\"><div class=\"top\">");

        htmlBuilder.append("<h2 id=\"RanTotal\">Ran</h2></div><h1>" + totalTests + "</h1><hr><p>" + convertSecondsToHrsMinsSecs(totalTimeTaken())
                + "</p><p>100<sup>%</sup></div>");

        htmlBuilder.append("<div class=\"panels\"><div class=\"top\"><h2 id=\"PassedTotal\">Passed</h2></div><h1>" + passed + "</h1><hr><p>"
                + value(totalTests, passed) + "<sup>%</sup></div>");

        htmlBuilder.append("<div class=\"panels\"><div class=\"top\"><h2 id=\"FailedTotal\">Failed</h2></div><h1>" + unknownFails + "</h1><hr><p>"
                + value(totalTests, unknownFails) + "<sup>%</sup></div>");

        htmlBuilder.append("<div class=\"panels\"><div class=\"top\"><h2 id=\"SkippedTotal\">Skipped</h2></div><h1>" + skipped + "</h1><hr><p>"
                + value(totalTests, skipped) + "<sup>%</sup></div></div>");

        outputPassedScripts(htmlBuilder);

        outputTestFailures(htmlBuilder);

        outputSkippedScripts(htmlBuilder);

        htmlBuilder.append("</body></html>");

        FileWriter fstream;
        try {
            final File file = new File(System.getProperty("user.dir") + "/target/Report");
            file.mkdirs();
            fstream = new FileWriter(System.getProperty("user.dir") + "/target/Report/index.html");
            final BufferedWriter out = new BufferedWriter(fstream);
            out.write(htmlBuilder.toString());
            out.close();

        } catch (final IOException e) {
            System.out.println("***************REPORT Didn't Work ATENTION*********************");
        }
    }

    /**
     * Builds HTML code for the skipped test scripts
     *
     * @param htmlBuilder
     */
    private void outputSkippedScripts(final StringBuilder htmlBuilder) {
        if (!skippedScripts.isEmpty()) {
            htmlBuilder.append("<p>");
            htmlBuilder.append("<p>");

            htmlBuilder.append("<table id=\"Skipped\">");
            htmlBuilder.append("<caption><b>Skipped</b></caption>");
            htmlBuilder.append("<tr><th>Test Case</th><th>Reason</th></tr>");

            for (final Entry<String, List<String>> entry : skippedScripts.entrySet()) {

                htmlBuilder.append("<tr><td title=\" " + entry.getValue().get(1) + " align=\"left\">" + entry.getKey() + "</td><td align=\"center\">"
                        + entry.getValue().get(0) + "</td></tr>");
            }

            htmlBuilder.append("</table>");
        }
    }

    /**
     * Builds the HTML code for the Test Failures
     *
     * @param htmlBuilder
     */
    private void outputTestFailures(final StringBuilder htmlBuilder) {
        if (!testfailures.isEmpty()) {
            htmlBuilder.append("<p>");
            htmlBuilder.append("<p>");
            htmlBuilder.append("<table id=\"UnrecognisedFailures\" class=\"tablesorter\">");
            htmlBuilder.append("<caption><b>Test Failures</b></caption>");
            htmlBuilder.append("<thead>");
            htmlBuilder
                    .append("<tr><th>Test Case</th><th class=\"sorter-false\">Test Duration</th><th>Page Object</th><th class=\"sorter-false\">ScreenShot</th><th class=\"sorter-false\">Stack Trace</th></tr>");
            htmlBuilder.append("</thead><tbody>");

            final Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(testfailures);

            for (final Entry<String, List<String>> entry : treeMap.entrySet()) {

                final String htmlScreenShot = "<a href=\"" + entry.getValue().get(5) + ".png\" target=\"_blank\">Click To View</a>";

                final String htmlStackTrace = "<a href=\"" + entry.getValue().get(5) + ".html\" target=\"_blank\">Click To View</a>";


                htmlBuilder.append("<tr><td title=\"" + entry.getValue().get(3) + "\">" + entry.getKey() + "</td><td align=\"center\">"
                        + entry.getValue().get(0) + "</td><td align=\"center\">" + entry.getValue().get(1) + "</td><td align=\"center\">" + htmlScreenShot
                        + "</td><td align=\"center\">" + htmlStackTrace + "</td></tr>");

            }
            htmlBuilder.append("</tbody></table>");

        }
    }

    /**
     * Builds the HTML code for the Passed test scripts
     *
     * @param htmlBuilder
     */
    private void outputPassedScripts(final StringBuilder htmlBuilder) {
        if (!passedScripts.isEmpty())

        {
            htmlBuilder.append("<p>");
            htmlBuilder.append("<p>");
            htmlBuilder.append("<table id=\"Passed\" class=\"tablesorter\">");
            htmlBuilder.append("<caption><b>Passed</b></caption>");
            htmlBuilder.append("<thead><tr><th>Test Case</th><th>Test Duration</th><th>Result</th></tr></thead><tbody>");

            final Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(passedScripts);

            for (final Entry<String, List<String>> entry : treeMap.entrySet()) {

                htmlBuilder.append("<tr>" +
                             "<td align=\"center\">" + entry.getKey() +          //Test Script Name
                        "</td><td align=\"center\">" + entry.getValue().get(0) + // Test Duration
                        "</td><td align=\"center\">" + entry.getValue().get(3) + // Result output value - Account number, Order number etc
                        "</td></tr>");
            }

            htmlBuilder.append("</tbody></table>");
        }
    }

    /**
     * Highlights and links the stack trace lines which relate to the framework
     * to the actual code on the GIT repository
     *
     * @return
     */
    private String highlightStackTrace() {

        final StackTraceElement[] stackTrace = testResult.getThrowable().getStackTrace();

        String errText = null;

        for (final StackTraceElement err : stackTrace) {
            if (err.toString().contains("com.dplvs.selenium")) {

                final String tmpName = err.getClassName().replace(".", "/");

                if (err.toString().contains(testResult.getName())) {
                    errText += "<font id=\"output\"><a href=\"http://svn.internal.com.au/au/com/Automation/src/test/java/" + tmpName
                            + ".java\" target=\"_blank\">" + err + "</a></font><br>";
                } else

                {

                    errText += "<font id=\"output\"><a href=\"http://svn.internal.com.au/au/com/Automation/src/main/java/" + tmpName
                            + ".java\" target=\"_blank\">" + err + "</a></font><br>";
                }

            } else

            {
                errText += err.toString() + "<br>";
            }

        }

        return errText;
    }

    /**
     * Generates a HTML page off the full stack trace
     *
     * @param stackTrace
     * @param testName
     * @param timeOfFailure
     * @param thrownException
     */
    private void genStackTrace(final String stackTrace, final String testName, final String timeOfFailure, final String thrownException) {


        final StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder
                .append("<!doctype html><html lang=\"en\"><head><link rel=\"stylesheet\" type=\"text/css\" href=\"");

        htmlBuilder.append(userProperties.getProperty("stackTraceCSSPath"));

        htmlBuilder.append("\"><meta charset=\"utf-8\"><title>StackTrace Output</title></head><body>");

        htmlBuilder.append("<div id=\"stackTraceTitle\"><h1>An Error Occurred:</h1></div>");
        htmlBuilder.append("<div id=\"timeOfFailureDiv\"><h2 id=\"timeOfFailureDate\"><strong>Time of failure </strong>" + timeOfFailure + "</h2></div>");
        htmlBuilder.append("<div id=\"stackTraceExceptionDiv\"><h3 id=\"stackTraceException\">" + org.apache.commons.lang3.StringEscapeUtils.escapeHtml3(thrownException) + "</h3></div>");
        htmlBuilder.append("<pre>" + stackTrace + "</pre>");
        htmlBuilder.append("</body></html>");

        FileWriter fstream;
        try {
            final File file = new File(System.getProperty("user.dir") + "/target/Report");
            file.mkdirs();
            fstream = new FileWriter(System.getProperty("user.dir") + "/target/Report/" + testName + ".html");
            final BufferedWriter out = new BufferedWriter(fstream);
            out.write(htmlBuilder.toString());
            out.close();

        } catch (final IOException e) {
            System.out.println("***************StackTrace REPORT Didn't Work ATENTION*********************");
        }
    }

    /**
     * Returns the Page object which has the code which caused the failure. If
     * the point of failure was within the script itself, then the script name
     * is returned.
     *
     * @return
     */
    private String getPageObject() {

        final StackTraceElement[] stackTrace = testResult.getThrowable().getStackTrace();

        String pageObjectName = null;

        for (final StackTraceElement err : stackTrace) {
            if (err.toString().contains("com.dplvs.selenium")) {

                final String methodName = err.getMethodName();

                String fileName = err.getFileName();

                fileName = fileName.replace(".java", "");

                if (methodName.replace('_', ' ').contains(fileName.replace('_', ' '))) {
                    pageObjectName = fileName;
                } else {
                    pageObjectName = fileName + "." + methodName;
                }

                break;
            }
        }

        return pageObjectName;
    }

    /**
     * Returns the last run test script name formatted using HTML. The Package
     * name is Bold and the remaining name set to italics
     *
     * @return
     */
    private String getTestName() {

        final String classNameS = testResult.getTestClass().getName();
        final String classNameBroken[] = classNameS.split("\\.");

        String testName;

        if (classNameBroken.length > 6) {
            testName = "<b>" + classNameBroken[classNameBroken.length - 3] + "." + classNameBroken[classNameBroken.length - 2] + ".</b><i>"
                    + classNameBroken[classNameBroken.length - 1] + "</i>";
        } else {
            testName = "<b>" + classNameBroken[classNameBroken.length - 2] + ".</b><i>" + classNameBroken[classNameBroken.length - 1] + "</i>";

        }
        return testName;
    }

    /**
     * Returns the Test Class name removing the package details
     *
     * @return
     */
    private String getTestClassName() {
        final String classNameS = testResult.getTestClass().getName();
        final String classNameBroken[] = classNameS.split("\\.");

        final String testName = classNameBroken[classNameBroken.length - 1];

        return testName;
    }

    /**
     * Returns the filename to use for the log file, stack trace and failure
     * screenshot. The filename is made up of the package name followed by '_'
     * and the script name i.e. residential_scriptname
     *
     * @param testResult
     * @return filename
     */
    private String getFilename(final ITestResult testResult) {
        final String fullPath = testResult.getInstanceName();
        final String tempFilename[] = fullPath.split("\\.");

        return String.format("%s_%s", tempFilename[tempFilename.length - 2], tempFilename[tempFilename.length - 1]);

    }

    /**
     * Stores a screenshot at the point of failure in the Report.
     * Screenshot name is saved as the current test script name followed by
     * _Screenshot.png. If the test script is a data driven script then the
     * dataDrivenFailure int value is prefixed to the name
     *
     * @param testResult
     */
    private void genScreenShot(final ITestResult testResult) {

        String fileName = getFilename(testResult);

        FileOutputStream outputStream = null;

        try {
            driver = ((DriverBase) testResult.getInstance()).getDriver();

            zoomOutOfBrowser(driver);

            // Take ScreenShot
            File file = new File(System.getProperty("user.dir") + "/target/Report");
            file.mkdirs();

            file = new File(System.getProperty("user.dir") + "/target/Report/" + fileName + ".png");

            outputStream = new FileOutputStream(file);

            byte[] screenShotData = null;

            screenShotData = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            outputStream.write(screenShotData);
        } catch (final Exception e) {
            System.out.println(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (final IOException e) {
                    // Already closed.
                }
            }
        }
    }

    /**
     * Get the time and date of the test execution run in the following format
     * Name of day, Month day, year at hours:minutes:seconds i.e. Saturday,
     * September 14, 2013 at 18:00:00
     *
     * @return String
     */
    private String getExecutionDateAndTime() {
        final Date d = new Date();

        final DateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' HH:mm:ss");

        return dateFormat.format(d);
    }

    /**
     * Stores all failures including Test Failures and Known into an array list
     * in the format required to run the scripts using TestNG
     */
    private void addToFailedScriptList() {
        // failuresXML.add("<class name=\"" + tr.getTestClass().getName() +
        // "\" />");
        final String fullName = testResult.getTestClass().getName();
        final String splitScriptName[] = fullName.split("\\.");

        String testName;

        if (splitScriptName.length > 6) {
            testName = splitScriptName[splitScriptName.length - 3] + "." + splitScriptName[splitScriptName.length - 2] + "."
                    + splitScriptName[splitScriptName.length - 1];
        } else {
            testName = splitScriptName[splitScriptName.length - 2] + "." + splitScriptName[splitScriptName.length - 1];

        }

        failedScripts.add(testName);
    }

    /**
     * Creates a file with the correctly formatted failures ready to use in a
     * TestNG XML file. Output file type is text and location is within the
     * Reports folder.
     */
    private void outputFailuresToTextFile() {

        try {
            final BufferedWriter out = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/target/Report/failures.txt"));

            // for (String script : failuresXML)
            // {
            // out.write(script + ",");
            // }

            out.write(StringUtils.join(failedScripts, ","));

            out.close();
        } catch (final IOException e) {

            e.printStackTrace();
        }

    }

    /**
     * Get the groups which were included in the test run
     *
     * @param testContext
     */
    private void getRanGroups(final ITestContext testContext) {
        final String[] groupsRan = testContext.getIncludedGroups();

    }

    /**
     * Used as a way to get around the issue with screenshots within workplace.
     * Workplace is made up of frames so when webDriver takes a screenshot it
     * does so of the frame and not the full screen. The only way around this at
     * the minute is to use the zoom out functionality of a browser before
     * taking a screenshot.
     */
    private void zoomOutOfBrowser(final WebDriver driver) {
        try {
            driver.switchTo().defaultContent();

            driver.manage().timeouts().implicitlyWait(12, TimeUnit.MILLISECONDS);

            if (driver.findElements(By.cssSelector("frame[name='topFrame']")).size() > 0) {
                // Leaving this in for future use, code removes the top-bar
                // frame
                // from the web page.
                // ((JavascriptExecutor)
                // driver).executeScript("var x=document.getElementsByName('topFrame'); x[0].parentNode.removeChild(x[0]);",
                // "");

                // ((JavascriptExecutor)
                // driver).executeScript("var x=document.getElementsByName('main'); x[0].parentNode.setAttribute('rows','');",
                // "");

                final WebElement browser = driver.findElement(By.tagName("html"));

                int counter = 1;
                while (counter < 6) {
                    browser.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
                    counter++;
                }
            }

        } catch (final Exception ex) {
            System.out.println(ex);
        }
    }

    private long totalTimeTaken() {

        final long duration = endTime - startTime;

        final long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);

        return diffSeconds;

    }

    /**
     * Set the name of the test suite which is being ran.
     */
    private void setSuiteName(final String suiteName) {
        SuiteName = suiteName;
    }

    public static String getSuiteName() {
        return SuiteName;
    }

    public static void setResultValue(String result) {
        ResultValue = result;
    }

    public static String getResultValue() {
        return ResultValue;
    }

}
