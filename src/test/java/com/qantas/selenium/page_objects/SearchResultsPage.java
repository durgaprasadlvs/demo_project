package com.qantas.selenium.page_objects;

import com.qantas.selenium.BasePageTemplate;
import com.qantas.selenium.DriverBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SearchResultsPage extends BasePageTemplate {

    private final RemoteWebDriver driver = DriverBase.getDriver();

    @FindBy(xpath = "//a[contains(text(),'Apply for a number plate')]")
    private WebElement numberPlateLink;

    public SearchResultsPage() throws Exception {

        PageFactory.initElements(driver, this);
        driver.executeScript("return document.readyState").equals("complete");
    }

    public void clickNumberPlateLink() {

        wait.forElementVisible(numberPlateLink,10);
        wait.forElementClickable(numberPlateLink,10);
        numberPlateLink.click();
    }

}
