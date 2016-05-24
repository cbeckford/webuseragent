/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.models;

import java.util.LinkedHashMap;
import org.openqa.selenium.WebElement;
//import org.openqa.selenium.Point;

/**
 *
 * @author Clive
 */
public class WebPageElement extends LinkedHashMap {

    /**
     * @return the webElement
     */
    public static WebElement getWebElement() {
        return webElement;
    }

    /**
     * @param aWebElement the webElement to set
     */
    public static void setWebElement(WebElement aWebElement) {
        webElement = aWebElement;
    }

    /**
     * @return the elementProperties
     */
    public static LinkedHashMap<String, Object> getElementProperties() {
        return elementProperties;
    }

    /**
     * @param aElementProperties the elementProperties to set
     */
    public static void setElementProperties(LinkedHashMap<String, Object> aElementProperties) {
        elementProperties = aElementProperties;
    }
    private String itemId ;
    //public Point thisPoint;
    private static WebElement webElement ; 
    private static LinkedHashMap<String, Object> elementProperties = new LinkedHashMap() ;

    /**
     * @return the itemId
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * @param itemId the itemId to set
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
}
