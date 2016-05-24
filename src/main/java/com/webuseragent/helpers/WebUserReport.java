/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.helpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

/**
 *
 * @author Clive
 */
public class WebUserReport {
    private static final StringBuilder builder = new StringBuilder();
    private static final String INDENT = "     ";
    private static final String NEW_LINE = "\n";
    private static final String[] HTML_LIST = new String[]{"<ol>", "</ol>"};
    private static final String[] HTML_LIST_ITEM = new String[]{"<li class='#section#'> <a href='#'>#Item#</a>", "</li>"};
    private static final String[] HTML_SUB_LIST = new String[]{"<li><label class='step' for='#section#'>#Item#</label> <input type='checkbox' /><ol>", "</ol></li>"};
    private static final String[] JSON_LIST = new String[]{"[", "}]"};
    private static final String[] JSON_LIST_ITEM = new String[]{"{", "}"};
    private static String section_prefix = "";
    private static String section_suffix = "";
    private static String item_prefix = "";
    private static String item_suffix = "";
    private static String sub_items_prefix = "";
    private static String sub_items_suffix = "";
    private static String rptType = "";
    private static Integer INDENT_COUNT = 1;
    private ArrayList<String> introduction;
    private LinkedHashMap<String, String> data;
    private LinkedHashMap<String, Object> steps;
    private LinkedHashMap<String, Object> assertions;
    private String currentStepKey;
    private String currentAssertionKey;
    
    public WebUserReport() {
        this.currentStepKey = "";
        this.currentAssertionKey = "";
        this.data = new LinkedHashMap<>();
        this.steps = new LinkedHashMap<>();
        this.assertions = new LinkedHashMap<>();
        this.introduction = new ArrayList<>();
    }

    @Override
    public String toString() {
        listToString("INTRODUCTION", getIntroduction());
        mapToString("DATA", getData());
        mapToString("STEPS", getSteps());
        mapToString("ASSERTIONS", getAssertions());
        return builder.toString();
    }

    public String toString(String requestedRptType) {
        rptType = requestedRptType;
        builder.setLength(0);
        if (requestedRptType.equals("HTML")) {
            builder.append(NEW_LINE).append("<ol class='tree'>");
        }
        listToString("INTRODUCTION", getIntroduction(), rptType);
        mapToString("DATA", "DATA", getData(), rptType);
        mapToString("STEPS", "STEPS", getSteps(), rptType);
        mapToString("ASSERTIONS", "ASSERTIONS", getAssertions(), rptType);
        if (requestedRptType.equals("HTML")) {
            builder.append(NEW_LINE).append("</ol>");
        }
        return builder.toString();
    }

    public void resetTestReport() {
        this.data = new LinkedHashMap<>();
        this.steps = new LinkedHashMap<>();
        this.assertions = new LinkedHashMap<>();
        this.introduction = new ArrayList<>();
    }

    public void loadTestReport() {
        resetTestReport();
        getIntroduction().add("Hi, I am a simulated UAT user. My name is Anna John.");
        getIntroduction().add("I want to 'find the meaning of a word',");
        getIntroduction().add("So I intend to use the 'define:word search prefix' feature");
        getIntroduction().add("of the 'Google Search' web applications!");

        getData().put("webUserId", "1453163");
        getData().put("FirstName", "Clive");
        getData().put("LastName", "Beckford");
        getData().put("Email", "clive.beckford@gmail.com");
        getData().put("Application Name", "Google Search");
        getData().put("URL", "http://www.google.com");

        String step = "I will now 'VISIT' the 'URL' with 'http://www.google.com'";
        LinkedHashMap stepDetail = new LinkedHashMap();
        LinkedHashMap stepExecutionDetails = new LinkedHashMap();

        stepDetail.put("Step", "1.1");
        stepDetail.put("Feature", "define:word search prefix #");
        stepDetail.put("Story", "find the meaning of a word");
        stepDetail.put("Status", "COMPLETED");
        stepDetail.put("Screenshot", "c:/uol/screenShots/screenshot2016-04-26_204623599.png");

        stepExecutionDetails.put("TARGET", "TEXTBOX");
        stepExecutionDetails.put("LOCATOR", "'ORDINAL_INSTANCE' that is 'EQUAL' '1'!");
        stepExecutionDetails.put("ACTION", "FILL");
        stepExecutionDetails.put("WITH", "'define: simulation");

        stepDetail.put("Execution Details", stepExecutionDetails);
        getSteps().put(step, stepDetail);

    }

    private static void mapToString(String section, String outerSection, LinkedHashMap lhm, String rptType) {
        setTags(rptType);
        builder.append(NEW_LINE).append("<li>")
                .append(NEW_LINE).append("<label for='").append(section).append("'>")
                .append(section)
                .append("</label>")
                .append("<input type='checkbox' id='").append(section).append("' />");
        builder.append(NEW_LINE).append(section_prefix.replaceAll("#section", outerSection));
        
        /*
        builder.append(NEW_LINE).append("<li>")
                .append(NEW_LINE).append("<label for='").append(section.toLowerCase()).append("'>")
                .append(section)
                .append("</label>")
                .append("<input type='checkbox' id='").append(section.toLowerCase()).append("' />");
        builder.append(NEW_LINE).append(section_prefix.replaceAll("#section", outerSection));
        */
        
        mapToString(section, lhm, rptType);
        builder.append(NEW_LINE).append(section_suffix);
    }

    private static void mapToString(String section, LinkedHashMap lhm) {
        mapToString(section, section, lhm, rptType);
    }

    private static void listToString(String section, ArrayList al) {
        listToString(section, al, rptType);
    }

    private static void setTags(String rptType) {
        switch (rptType) {
            case "HTML":
                section_prefix = HTML_LIST[0];
                section_suffix = HTML_LIST[1];
                item_prefix = HTML_LIST_ITEM[0];
                item_suffix = HTML_LIST_ITEM[1];
                sub_items_prefix = HTML_SUB_LIST[0];
                sub_items_suffix = HTML_SUB_LIST[1];
                break;

            case "JSON":
                section_prefix = JSON_LIST[0];
                section_suffix = JSON_LIST[1];
                item_prefix = JSON_LIST_ITEM[0];
                item_suffix = JSON_LIST_ITEM[1];
                sub_items_prefix = JSON_LIST_ITEM[0];
                sub_items_suffix = JSON_LIST_ITEM[1];
                break;

            default:
                section_prefix = "";
                section_suffix = "";
                item_prefix = "";
                item_suffix = "";
        }
    }

    private static void listToString(String section, ArrayList al, String rptType) {
        setTags(rptType);

        builder.append(NEW_LINE).append("<li>")
                .append(NEW_LINE).append("<label for='").append(section.toLowerCase()).append("'>")
                .append(section)
                .append("</label>")
                .append("<input type='checkbox' checked id='").append(section.toLowerCase()).append("' />");
        builder.append(NEW_LINE).append(section_prefix);

        al.stream().forEach((Object thisLine) -> {
            builder.
                    append(NEW_LINE)
                    .append(indented())
                    .append(item_prefix.replaceAll("#section#", section.toLowerCase()).replaceAll("#Item#", thisLine.toString())).append(item_suffix);
        });
        builder.append(NEW_LINE)
                .append(section_suffix)
                .append("</li>");
    }

    private static void mapToString(String section, LinkedHashMap lhm, String rptType) {
        setTags(rptType);
        lhm.keySet().stream().map((detailKey) -> {
            return detailKey;
        }).forEach(new Consumer() {
            @Override
            public void accept(Object detailKey) {
                INDENT_COUNT += 1;
                switch (lhm.get(detailKey.toString()).getClass().getName()) {
                    case "java.lang.String":
                        String[] fileParts;
                        String itemValue = "";
                        if (detailKey.toString().toLowerCase().equals("screenshot")) {
                            fileParts = lhm.get(detailKey.toString()).toString().split("/");
                            itemValue = fileParts[fileParts.length - 1];
                        } else {
                            itemValue = lhm.get(detailKey.toString()).toString();
                        }
                        
                        builder.append(NEW_LINE)
                                .append(indented())
                                .append(item_prefix.replaceAll("#section#", "file").replaceAll("#Item#", detailKey.toString() + " = " + itemValue));
                        if (detailKey.toString().toLowerCase().equals("screenshot")) {
                            builder
                                    .append(NEW_LINE).append("<img style='padding-top:1px' width='100%' border='1px' src='/images/")
                                    .append(itemValue)
                                    .append("' />")
                                    .append(NEW_LINE);
                        }
                        break;
                        
                    case "java.util.LinkedHashMap":
                        builder.append(NEW_LINE)
                                .append(sub_items_prefix.replaceAll("#section#", section).replaceAll("#Item#", detailKey.toString()));
                        
                        mapToString(section, (LinkedHashMap) lhm.get(detailKey.toString()), rptType);
                        
                        builder.append(NEW_LINE)
                                .append(sub_items_suffix)
                                ;
                                //.append(item_suffix)
                                
                        
                        break;
                        
                    default:
                        builder.append(indented())
                                .append("ERROR: Unexpected value ")
                                .append(lhm.get(detailKey.toString()).getClass().getName());
                }
                INDENT_COUNT -= 1;
            }
        });
    }

    private static String indented() {
        return String.format(String.format("%%%ds", INDENT_COUNT), " ").replace(" ", INDENT);
    }

    /**
     * @return the introduction
     */
    public ArrayList<String> getIntroduction() {
        return introduction;
    }

    /**
     * @param introduction the introduction to set
     */
    public void setIntroduction(ArrayList<String> introduction) {
        this.introduction = introduction;
    }

    /**
     * @return the data
     */
    public LinkedHashMap<String, String> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(LinkedHashMap<String, String> data) {
        this.data = data;
    }

    /**
     * @return the steps
     */
    public LinkedHashMap<String, Object> getSteps() {
        return steps;
    }

    /**
     * @param steps the steps to set
     */
    public void setSteps(LinkedHashMap<String, Object> steps) {
        this.steps = steps;
    }

    /**
     * @return the assertions
     */
    public LinkedHashMap<String, Object> getAssertions() {
        return assertions;
    }

    /**
     * @param assertions the assertions to set
     */
    public void setAssertions(LinkedHashMap<String, Object> assertions) {
        this.assertions = assertions;
    }

    /**
     * @return the currentStepKey
     */
    public String getCurrentStepKey() {
        return currentStepKey;
    }

    /**
     * @param currentStepKey the currentStepKey to set
     */
    public void setCurrentStepKey(String currentStepKey) {
        this.currentStepKey = currentStepKey;
    }

    /**
     * @return the currentAssertionKey
     */
    public String getCurrentAssertionKey() {
        return currentAssertionKey;
    }

    /**
     * @param currentStepExectionKey the currentAssertionKey to set
     */
    public void setCurrentAssertionKey(String currentAssertKey) {
        this.currentAssertionKey = currentAssertKey;
    }

}
