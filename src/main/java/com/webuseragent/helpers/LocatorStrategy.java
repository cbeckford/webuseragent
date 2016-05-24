/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.helpers;

import org.openqa.selenium.By;

/**
 *
 * @author Clive
 * Reference: MARCH 29, 2014 BY AMIR GHAHRAI, http://www.testingexcellence.com/webdriver-wait-for-ajax-complete/
 */

public final class LocatorStrategy {
 
    public static By getLocatorMethod(String locatorMethod) {
        String locator = locatorMethod.split("=")[1];
 
        if (locatorMethod.startsWith("id")) {
            return By.id(locator);
        } else if (locatorMethod.startsWith("name")) {
            return By.name(locator);
        } else if (locatorMethod.startsWith("css")) {
            return By.cssSelector(locator);
        } else if (locatorMethod.startsWith("xpath")) {
            return By.xpath(locator);
        } else if (locatorMethod.startsWith("class")) {
            return By.className(locator);
        } else if (locatorMethod.startsWith("linkText")) {
            return By.linkText(locator);
        } else if (locatorMethod.startsWith("partialLinkText")) {
            return By.partialLinkText(locator);
        } else if (locatorMethod.startsWith("tagName")) {
            return By.tagName(locator);
        } else {
            return null;
        }
    }
}