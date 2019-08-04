package com.qantas.selenium.core.element;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class JavascriptActions {

    private final static int WEBDRIVER_WAIT_TIMEOUT_SEC = 15;
    private final JavascriptExecutor js;
    private final WebDriver driver;


    public JavascriptActions(WebDriver driver) {
        this.js = (JavascriptExecutor) driver;
        this.driver = driver;
    }

    public void click(WebElement webElement) {
        js.executeScript("arguments[0].click();", webElement);
    }
}
