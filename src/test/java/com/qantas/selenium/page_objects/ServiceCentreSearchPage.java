package com.qantas.selenium.page_objects;

import com.qantas.selenium.BasePageTemplate;
import com.qantas.selenium.DriverBase;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ServiceCentreSearchPage extends BasePageTemplate {

    private final RemoteWebDriver driver = DriverBase.getDriver();

    @FindBy(id = "locatorTextSearch")
    private WebElement searchTextBox;
    @FindBy(css = "#locatorAutocomplete li:nth-child(1)")
    private WebElement suggestionItem;
    @FindBy(xpath="//a[contains(text(),\"Marrickville Service Centre\"]")
    private WebElement MarrickvilleLink;

    public ServiceCentreSearchPage() throws Exception {

        PageFactory.initElements(driver, this);
        driver.executeScript("return document.readyState").equals("complete");
    }

    public void enterSearchText(String searchText) {

        wait.forElementVisible(searchTextBox,10);
        wait.forElementClickable(searchTextBox,10);
        searchTextBox.clear();
        searchTextBox.sendKeys(searchText);
    }

    public void clickSuggestion() throws Exception{

        int timeout = 30;

        while (timeout > 0) {
            try {
                wait.forElementVisible(suggestionItem, 5);
                suggestionItem.click();
                break;
            } catch (StaleElementReferenceException exception) {
                timeout -= 5;
                continue;
            }
        }
    }

    public Boolean checkForLinkWithText(String text) throws Exception{

        return driver.findElement(By.partialLinkText(text)).isDisplayed();

    }

    public Boolean EnterSearchTextAndExpectSuggestion(String area, String serviceCentre) throws Exception{

        this.enterSearchText(area);

        this.clickSuggestion();

        return this.checkForLinkWithText(serviceCentre);
    }

}
