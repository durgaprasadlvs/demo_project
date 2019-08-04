package com.qantas.selenium;

import com.qantas.selenium.core.element.JavascriptActions;
import com.qantas.selenium.core.element.Wait;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public abstract class BasePageTemplate extends DriverBase {

    protected RemoteWebDriver driver = getDriver();
    protected final Wait wait = new Wait(driver);
    protected JavascriptActions jsActions = new JavascriptActions(driver);

    public BasePageTemplate() throws Exception {
        PageFactory.initElements(driver, this);
    }

    public void openUrl(String str) throws Exception{

        this.driver.get(str);
        DriverBase.clearCookies();
        this.driver.get(str);
        this.driver.manage().window().fullscreen();
        Thread.sleep(5000);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isVisible(WebElement webElement) {
        try {
            wait.forElementVisible(webElement, 10);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isVisible(WebElement webElement, int timeout) {
        try {
            wait.forElementVisible(webElement, timeout);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static class BaseTestTemplate {
    }
}