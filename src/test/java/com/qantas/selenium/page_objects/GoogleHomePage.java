package com.qantas.selenium.page_objects;

import com.qantas.selenium.DriverBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class GoogleHomePage {

    private final RemoteWebDriver driver = DriverBase.getDriver();

    @FindBy (name = "q")
    private WebElement searchBar;
    @FindBy (name = "btnK")
    private WebElement googleSearch;
    @FindBy (name = "btnI")
    private WebElement imFeelingLucky;

    public GoogleHomePage() throws Exception {
        PageFactory.initElements(driver, this);
    }

    public GoogleHomePage enterSearchTerm(String searchTerm) {
        searchBar.clear();
        searchBar.sendKeys(searchTerm);

        return this;
    }

    public GoogleHomePage submitSearch() {
        googleSearch.submit();

        return this;
    }

    public void getLucky() {
        imFeelingLucky.click();
    }

}