package com.qantas.selenium.page_objects;

import com.qantas.selenium.BasePageTemplate;
import com.qantas.selenium.DriverBase;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ServiceNswHomePage extends BasePageTemplate {

    private final RemoteWebDriver driver = DriverBase.getDriver();

    @FindBy(css = "#homeAutosuggest input")
    private WebElement searchBar;
    @FindBy(xpath = "//li[contains(text(),\"apply for a number plate\")]")
    private WebElement suggestionItem;

    public ServiceNswHomePage() throws Exception {

        PageFactory.initElements(driver, this);
        driver.executeScript("return document.readyState").equals("complete");
    }

    public void enterSearchTerm(String searchTerm) throws Exception {

        wait.forElementVisible(searchBar,10);
        wait.forElementClickable(searchBar,10);
        searchBar.sendKeys(searchTerm);
    }

    public void clickSuggestion() throws Exception {

        int timeout = 30;

        while (timeout > 0) {
            try {
                new WebDriverWait(driver,5);
                wait.forElementVisible(suggestionItem, 5);
                suggestionItem.click();
                break;
            } catch (StaleElementReferenceException exception) {
                timeout -= 5;
                continue;
            }
        }

    }
}
