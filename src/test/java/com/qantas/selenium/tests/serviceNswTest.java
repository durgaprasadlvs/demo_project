package com.qantas.selenium.tests;

import com.qantas.selenium.DriverBase;
import com.qantas.selenium.page_objects.ApplyNumberPlatePage;
import com.qantas.selenium.page_objects.SearchResultsPage;
import com.qantas.selenium.page_objects.ServiceCentreSearchPage;
import com.qantas.selenium.page_objects.ServiceNswHomePage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class serviceNswTest extends DriverBase {

    @Test
    public void testFindLocation() throws Exception {

        String directory_path = new File("").getAbsolutePath()
                + File.separator + "src"
                + File.separator + "test"
                + File.separator + "java"
                + File.separator + "com"
                + File.separator + "qantas"
                + File.separator + "selenium";

        Reader in = new FileReader(directory_path + File.separator + "data.csv");

        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);

        ServiceNswHomePage serviceNswHomePage = new ServiceNswHomePage();
        SearchResultsPage searchResultsPage = new SearchResultsPage();
        ApplyNumberPlatePage applyNumberPlatePage = new ApplyNumberPlatePage();
        ServiceCentreSearchPage serviceCentreSearchPage = new ServiceCentreSearchPage();


        serviceNswHomePage.openUrl("https://www.service.nsw.gov.au/");

        serviceNswHomePage.enterSearchTerm("Apply for a number plate");

        serviceNswHomePage.clickSuggestion();

        searchResultsPage.clickNumberPlateLink();

        applyNumberPlatePage.verifyUrl();

        applyNumberPlatePage.clickFindLocationsLink();

        for (CSVRecord record : records) {
            String area = record.get("searchterm");
            String expected_value = record.get("expected_value");

            System.out.println(area + "  " + expected_value);

            Assert.assertTrue(serviceCentreSearchPage.EnterSearchTextAndExpectSuggestion
                    (area, expected_value));

        }

    }
}