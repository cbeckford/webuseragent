/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import org.openqa.selenium.WebElement;

/**
 *
 * @author Clive
 */
public class Target {

    private TargetType targetType;
    private List<Locator> targetLocators;
    private List<Locator> relativeLocators;
    
    private WebElement webElement;
    private String lastSeen;
    private Boolean found;
    private String id;
    private String xPath;
    private String cssPath;
    private String tagName;
    private LinkedHashMap<String, String> tagProperties = new LinkedHashMap();    
    private HashSet<TargetType> targetTypeList = new HashSet();
    
    public Target() {
        this.targetLocators = new ArrayList<>();
        this.relativeLocators = new ArrayList<>();
        this.targetTypeList.addAll(Arrays.asList(TargetType.values()));
        this.targetType = TargetType.NONE;
        //this.targetLocator = new Locator();
        this.tagName = "";
        this.cssPath = "";
        this.xPath = "";
        this.id = "";
        this.found = false;
        this.lastSeen = "";
    }

    /**
     * @return the targetLocators
     */
    public List<Locator> getLocators() {
        return targetLocators;
    }

    /**
     * @param locators the targetLocators to set
     */
    public void setLocators(ArrayList<Locator> locators) {
        this.targetLocators = locators;
    }

    public boolean isFindableTarget() {
        for (NonFindableTargetType c : NonFindableTargetType.values()) {
            if (c.name().equals(getTargetType().name())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the lastSeen
     */
    public String getLastSeen() {
        return lastSeen;
    }

    /**
     * @param lastSeen the lastSeen to set
     */
    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * @return the found
     */
    public Boolean getFound() {
        return found;
    }

    /**
     * @param found the found to set
     */
    public void setFound(Boolean found) {
        this.found = found;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the xPath
     */
    public String getxPath() {
        return xPath;
    }

    /**
     * @param xPath the xPath to set
     */
    public void setxPath(String xPath) {
        this.xPath = xPath;
    }

    /**
     * @return the cssPath
     */
    public String getCssPath() {
        return cssPath;
    }

    /**
     * @param cssPath the cssPath to set
     */
    public void setCssPath(String cssPath) {
        this.cssPath = cssPath;
    }

    /**
     * @return the tagName
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * @param tagName the tagName to set
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * @return the tagProperties
     */
    public LinkedHashMap<String, String> getTagProperties() {
        return tagProperties;
    }

    /**
     * @param tagProperties the tagProperties to set
     */
    public void setTagProperties(LinkedHashMap<String, String> tagProperties) {
        this.tagProperties = tagProperties;
    }

    /**
     * @return the webElement
     */
    public WebElement getWebElement() {
        return webElement;
    }

    /**
     * @param webElement the webElement to set
     */
    public void setWebElement(WebElement webElement) {
        this.webElement = webElement;
    }

    /**
     * @return the targetType
     */
    public TargetType getTargetType() {
        return targetType;
    }

    /**
     * @param targetType the targetType to set
     */
    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }
    
    public boolean setTargetType(String targetTypeName) {
        if (targetTypeName==null) {
            return false;
        }
        for (TargetType tarType: targetTypeList) {
            if (tarType.name().equalsIgnoreCase(targetTypeName)) {
                this.targetType = TargetType.valueOf(targetTypeName);
                return true;
            }
        }
        return false;
    }

    /**
     * @return the relativeLocators
     */
    public List<Locator> getRelativeLocators() {
        return relativeLocators;
    }

    /**
     * @param relativeLocators the relativeLocators to set
     */
    public void setRelativeLocators(List<Locator> relativeLocators) {
        this.relativeLocators = relativeLocators;
    }

  
    
}
