package com.qantas.selenium.page_objects;

import com.qantas.selenium.BasePageTemplate;
import com.qantas.selenium.DriverBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

public class ApplyNumberPlatePage extends BasePageTemplate {

    private final RemoteWebDriver driver = DriverBase.getDriver();

    @FindBy(xpath = "//a[contains(text(),\"Find locations\")]")
    private WebElement findLocationsLink;

    public ApplyNumberPlatePage() throws Exception {

        PageFactory.initElements(driver, this);
        driver.executeScript("return document.readyState").equals("complete");
    }

    public void verifyUrl() {

        Assert.assertEquals(this.driver.getCurrentUrl(),"https://www.service.nsw.gov.au/transaction/apply-number-plate");
    }

    public void clickFindLocationsLink() {

        wait.forElementClickable(findLocationsLink, 5);

        findLocationsLink.click();
    }

}
