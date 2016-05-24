/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.models;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

/**
 *
 * @author Clive
 */
public class WebPage extends Object {
    private String pageHandle;
    private LocalDateTime pageRequested; //LocalDateTime.of(1994, Month.APRIL, 15, 11, 30));
    private LocalDateTime pageArrived; //LocalDateTime.of(1994, Month.APRIL, 15, 11, 30));
    private String html;
    private String url;
    private String title;
    private String windowHandle;

    private LinkedHashMap<String, WebPageElement> pageElements ; 

    public WebPage() {
        this.pageElements = new LinkedHashMap(); 
    }

    /**
     * @return the pageHandle
     */
    public String getPageHandle() {
        return pageHandle;
    }

    /**
     * @param pageHandle the pageHandle to set
     */
    public void setPageHandle(String pageHandle) {
        this.pageHandle = pageHandle;
    }

    /**
     * @return the pageRequested
     */
    public LocalDateTime getPageRequested() {
        return pageRequested;
    }

    /**
     * @param pageRequested the pageRequested to set
     */
    public void setPageRequested(LocalDateTime pageRequested) {
        this.pageRequested = pageRequested;
    }

    /**
     * @return the pageArrived
     */
    public LocalDateTime getPageArrived() {
        return pageArrived;
    }

    /**
     * @param pageArrived the pageArrived to set
     */
    public void setPageArrived(LocalDateTime pageArrived) {
        this.pageArrived = pageArrived;
    }

    /**
     * @return the html
     */
    public String getHtml() {
        return html;
    }

    /**
     * @param html the html to set
     */
    public void setHtml(String html) {
        this.html = html;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the windowHandle
     */
    public String getWindowHandle() {
        return windowHandle;
    }

    /**
     * @param windowHandle the windowHandle to set
     */
    public void setWindowHandle(String windowHandle) {
        this.windowHandle = windowHandle;
    }

    /**
     * @return the pageElements
     */
    public LinkedHashMap<String, WebPageElement> getPageElements() {
        return pageElements;
    }

    /**
     * @param pageElements the pageElements to set
     */
    public void setPageElements(LinkedHashMap<String, WebPageElement> pageElements) {
        this.pageElements = pageElements;
    }
    
}
