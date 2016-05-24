/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.models;

import com.webuseragent.main.App;
import com.webuseragent.helpers.LocatorStrategy;
import com.webuseragent.helpers.StringHelper;
import com.webuseragent.helpers.WebUserReport;
import com.webuseragent.helpers.XpathHelper;
import com.webuseragent.sensors.Vision;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.json.simple.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Clive
 */
public final class WebUser implements Runnable, Vision {

    private static LinkedHashMap<String, Plan> plans = new LinkedHashMap();
    private final LinkedHashMap<String, Thread> parallelActions = new LinkedHashMap();
    private LinkedHashMap<String, WebPage> webPages = new LinkedHashMap();
    private String targetElement;
    private String currentPlan;
    private String currentStep;
    private String currentAssertion;
    private String lastPlan;
    private String lastStep;
    private String lastAssertion;
    private Integer waitTimeOut;
    private boolean planFailed;
    private String role;
    private String screenShotPath;
    private LinkedHashMap<String, String> propertyList;
    private List<String> desires;
    private List<Plan> testPlans;
    private WebDriver driver;
    private Memory memory;
    private UUID webUserId;
    private boolean browserInitialized = false;
    private HashMap<String, Boolean> threadDone = new HashMap<>();

    private void initWebUser() {
        this.webUserId = UUID.randomUUID();
        this.waitTimeOut = 10;
        this.lastStep = "0";
        this.lastPlan = "0";
        this.currentStep = "0";
        this.currentPlan = "0";
        this.currentAssertion = "0";
        this.planFailed = false;
        this.screenShotPath = App.getAPP_PROPS().getProperty("screenShot.localPath");
        this.memory = new Memory();

        //Initialize the browser in a separate thread
        Thread initializeBrowser = new Thread() {
            @Override
            public void run() {
                say("DEBUG", "CONFIGRATION: BROWSER: Initializing....");
                driver = new FirefoxDriver();
                try {
                    driver.manage().timeouts().implicitlyWait(Integer.valueOf(App.getAPP_PROPS().getProperty("driver.timeout")), TimeUnit.SECONDS);
                    driver.manage().timeouts().pageLoadTimeout(Integer.valueOf(App.getAPP_PROPS().getProperty("driver.timeout")), TimeUnit.SECONDS);
                    browserInitialized = true;
                    say("DEBUG", "CONFIGRATION: BROWSER: Initialized!");
                } catch (Exception ex) {
                    driver.manage().timeouts().implicitlyWait(waitTimeOut, TimeUnit.SECONDS);
                    driver.manage().timeouts().pageLoadTimeout(waitTimeOut, TimeUnit.SECONDS);
                    say("WARNING", "CONFIGRATION: BROWSER: A value for 'driver.timeout' was not found; using a default of " + waitTimeOut.toString() + " seconds!");
                }
            }
        };
        parallelActions.put("initializeBrowser", initializeBrowser);
        // start the thread
        (parallelActions.get("initializeBrowser")).start();

        this.propertyList = new LinkedHashMap();
        this.testPlans = Collections.synchronizedList(new ArrayList<Plan>());
        this.desires = Collections.synchronizedList(new ArrayList<String>());
        setCurrentPlan(String.valueOf(Integer.parseInt(getCurrentPlan()) + 1));
    }

    /**
     * @return the plans
     */
    public static LinkedHashMap<String, Plan> getPlans() {
        return plans;
    }

    /**
     * @param aPlans the plans to set
     */
    public static void setPlans(LinkedHashMap<String, Plan> aPlans) {
        plans = aPlans;
    }

    public String currentPlanStep() {
        return getCurrentPlan() + "." + getCurrentStep();
    }

    public String currentPlanAssertion() {
        return getCurrentPlan() + "." + getCurrentAssertion();
    }

    public String lastPlanStep() {
        return getLastPlan() + "." + getLastStep();
    }

    public String lastPlanAssertion() {
        return getLastPlan() + "." + getLastAssertion();
    }
    public WebUserReport wr = new WebUserReport();

    public WebUser(LinkedHashMap mapWU) {
        this.webUserId = UUID.randomUUID();
    }

    public WebUser(String stringWU) {
        this.webUserId = UUID.randomUUID();
    }

    public WebUser(JSONObject jsonWU) {
        initWebUser();
        /**
         * ***************************************
         * Web User: Loads 'MEMORY' from 'JSON' *
         * ***************************************
         */
        
        App.say(jsonWU.toJSONString());
        for (Iterator iterator = jsonWU.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            Object keyValue = jsonWU.get(key);
            if ((keyValue instanceof JSONObject) == false) {
                memory.save(key, jsonWU.get(key));
                say("DEBUG", "MEMORY:" + getCurrentPlan() + " Learn (" + key + "=" + jsonWU.get(key).toString() + ")!");
            }
        }
        
        JSONArray jsonUserData = (JSONArray) jsonWU.get("userData");
        if (jsonUserData == null) {
            say("INFO", "PLAN:" + getCurrentPlan() + " No 'userData' found!");
        } else {
            //say("DEBUG", "PLAN:" + getCurrentPlan() + " Loading (" + String.valueOf(jsonSteps.keySet().size()) + ") STEPS...");
            //SortedSet<String> jsonKeySet = new TreeSet<>(jsonSteps.keySet());
            //for (Object jsonKey : jsonKeySet) {
            say("DEBUG", "PLAN:" + getCurrentPlan() + " Loading (" + String.valueOf(jsonUserData.size()) + ") USER DATA ITEMS...");
            Iterator i = jsonUserData.iterator();            
            while (i.hasNext()) {
                say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + " New User Data Item found ...");
                JSONObject jsonUserDataItem = (JSONObject) i.next();
                String dataKey = (String)jsonUserDataItem.get("dataKey");
                String dataValue = (String)jsonUserDataItem.get("dataValue");
                String dataCaption = (String)jsonUserDataItem.get("dataCaption");                
                memory.save(dataKey, dataValue);
                say("DEBUG", "MEMORY:" + getCurrentPlan() + " Learn (" + dataKey + "=" + dataValue + ")!");
            }
        }

        /**
         * *************************************************
         * Web User: Reports 'INTRODUCTION' from 'MEMORY' *
         * *************************************************
         */
        wr.getIntroduction().add("Hi!");
        if (memory.recall("role") != null) {
            wr.getIntroduction().add("I am a '" + memory.recall("role") + "'");
        }
        wr.getIntroduction().add("My name is "
                + (memory.recall("title") == null ? "" : memory.recall("title") + " ")
                + (memory.recall("firstName") == null ? "" : memory.recall("firstName") + " ")
                + (memory.recall("middleName") == null ? "" : memory.recall("middleName") + " ")
                + (memory.recall("lastName") == null ? "" : memory.recall("lastName") + " ")
        );
        if (memory.recall("desire") != null) {
            wr.getIntroduction().add("I want to " + memory.recall("desire") + ", ");
        }

        if (memory.recall("intent") != null) {
            wr.getIntroduction().add("So I intend to use the '" + memory.recall("intent") + "' feature");
        }
        if (memory.recall("application") != null) {
            wr.getIntroduction().add("of the '" + memory.recall("application") + "' web application!");
        }
        wr.getIntroduction().forEach((String introduction) -> {
            say("INFO", "PLAN: " + getCurrentPlan() + "|INTRODUCTION: " + introduction);
        });

        /**
         * ****************************************
         * Web User: Reports 'DATA' from 'MEMORY' *
         * ****************************************
         */
        memory.recallAll().keySet().stream().forEach((memoryKey) -> {
            //wr.getData().put(memory.camelToTitle(memoryKey.toString()), memory.recall(memoryKey.toString()).toString());
            wr.getData().put(
                    memory.camelToTitle(memoryKey.toString()) + "  (" + memoryKey.toString() + ")",
                    memory.recall(memoryKey.toString()).toString()
            );
        });

        /**
         * *************************************
         * Web User: Loads 'STEPS' from 'JSON' *
         * *************************************
         */
        //What are the steps of my plan
        //JSONObject jsonSteps = (JSONObject) jsonWU.get("plannedSteps");
        JSONArray jsonSteps = (JSONArray) jsonWU.get("plannedSteps");
        if (jsonSteps == null) {
            say("INFO", "PLAN:" + getCurrentPlan() + " No STEPS found!");
        } else {
            //say("DEBUG", "PLAN:" + getCurrentPlan() + " Loading (" + String.valueOf(jsonSteps.keySet().size()) + ") STEPS...");
            //SortedSet<String> jsonKeySet = new TreeSet<>(jsonSteps.keySet());
            //for (Object jsonKey : jsonKeySet) {
            say("DEBUG", "PLAN:" + getCurrentPlan() + " Loading (" + String.valueOf(jsonSteps.size()) + ") STEPS...");
            Iterator i = jsonSteps.iterator();            
            while (i.hasNext()) {
                say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + " New step found ...");
                //JSONObject jsonStep = (JSONObject) jsonSteps.get(jsonKey);
                JSONObject jsonStep = (JSONObject) i.next();
                setCurrentStep(String.valueOf(Integer.parseInt(getCurrentStep()) + 1));
                Step newPlanStep = new Step();
                newPlanStep.setId(currentPlanStep());

                /**
                 * **************************************
                 * Web User: Loads 'ACTION' from 'JSON' *
                 * **************************************
                 */
                String stepAction = (jsonStep.get("action")).toString().toUpperCase();

                String userAction = deduceUserAction(stepAction);
                if (!newPlanStep.setUserAction(userAction)) {
                    say("WARNING", "'I do not know how to '" + userAction + "'!");
                } else {
                }

                /**
                 * **************************************
                 * Web User: Loads 'TARGET' from 'JSON' *
                 * **************************************
                 */
                String stepTarget = "";
                if (jsonStep.get("targetType") != null) {
                    stepTarget = (jsonStep.get("targetType")).toString().toUpperCase();
                } else {
                    say("WARNING", "Cannot find the target for a step ('the' is missing)!");
                }
                String stepTargetType = deduceUserTarget(stepTarget);
                Target newTarget = new Target();
                if (!newTarget.setTargetType(stepTargetType)) {
                    say("WARNING", "'I do not know what a '" + stepTargetType + "' is!");
                    continue;
                }
                /**
                 * ************************************************
                 * Web User: Loads 'ACTION PARAMETER' from 'JSON' *
                 * ************************************************
                 */
                String stepWith = (jsonStep.get("withValue")).toString();
                newPlanStep.setActionParameter(stepWith);
                newPlanStep.setActionParameterValue(memory.substituteTags(stepWith));
                /**
                 * ****************************************
                 * Web User: Loads 'LOCATORS' from 'JSON' *
                 * ****************************************
                 */
                JSONArray jsonLocators = (JSONArray) jsonStep.get("locators");
                if (jsonLocators == null) {
                    say("INFO", "PLAN:" + getCurrentPlan() + "|STEP:" + " No LOCATORS found!");
                } else {
                    say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + " Loading (" + String.valueOf(jsonLocators.size()) + ") LOCATORS...");
                    Iterator j = jsonLocators.iterator();
                    
                    //for (Object locatorKey : jsonLocators.keySet()) {
                    while (j.hasNext()) { 
                        JSONObject jsonLocator = (JSONObject) j.next();
                        say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + newPlanStep.getId() + "|TARGET|LOCATORS:" + " Loading Locator Type:(" + jsonLocator.get("locatorType") + ")");
                        Locator newTargetLocator = new Locator();
                        /**
                         * **********************************************************
                         * Web User: Loads Locator from 'JSON'
                         * **********************************************************
                         */
                        if (jsonLocator.get("locatorType") != null && !newTargetLocator.setLocatorType(jsonLocator.get("locatorType").toString())) {
                            say("ERROR", "Invalid/missing Locator Type!");
                        }
                        if (jsonLocator.get("locatorRelativePosition") != null && !newTargetLocator.setRelativePosition(jsonLocator.get("locatorRelativePosition").toString()) && jsonLocator.get("locatorType").equals(LocatorType.RELATIVE_LOCATOR.name())) {
                            say("ERROR", "Invalid/missing Locator Relative Position!");
                        }
                        //Overrides the previous default
                        if (jsonLocator.get("locatorTargetType") != null && !newTargetLocator.setLocatorTargetType(jsonLocator.get("locatorTargetType").toString()) && !jsonLocator.get("locatorCriteria").equals(TargetLocatorCriteriaType.ORDINAL_INSTANCE.name())) {
                            say("ERROR", "Invalid/missing Locator Target Type!");
                        }
                        if (jsonLocator.get("locatorCriteria") != null && !newTargetLocator.setCriteria((jsonLocator.get("locatorCriteria").toString()))) {
                            say("ERROR", "Invalid/missing Locator Criteria!");
                        }
                        if (jsonLocator.get("locatorOperator") != null && !newTargetLocator.setOperator((jsonLocator.get("locatorOperator").toString()))) {
                            say("ERROR", "Invalid/missing Locator Operator!");
                        }
                        newTargetLocator.setValue(jsonLocator.get("locatorValue").toString());
                        //Add Locator to Target
                        newTarget.getLocators().add(newTargetLocator);
                    }//Next!
                }
                //Add Target to Step
                newPlanStep.setTarget(newTarget);

                //Say It
                String stepName = " '" + stepAction + "' the '" + stepTarget + "' with '" + stepWith + "'";
                wr.setCurrentStepKey("(" + newPlanStep.getId() + ")" + stepName);
                reportStepKeyValuePair("step", newPlanStep.getId());
                say("PLAN:" + getCurrentPlan() + "|STEP:" + newPlanStep.getId() + "| " + stepName);
                //Do It
                if (!browserInitialized) {
                    try {
                        parallelActions.get("initializeBrowser").join();
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(WebUser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                newPlanStep.setStatus(doStep(newPlanStep));
                setLastPlanStep();

                if (newPlanStep.getStatus().equals(PlanStepStatusType.FAILED)
                        && isPlanFailed()) {
                    return;
                } else {
                }

                //Show It
                waitForLoad(driver); //waitForAjax();
                String screenshot = takeScreenShot();
                reportStepKeyValuePair("status", newPlanStep.getStatus().name());
                reportStepKeyValuePair("screenshot", screenshot);
                say("INFO", "PLAN:" + getCurrentPlan() + "|STEP:" + newPlanStep.getId() + "|STATUS:" + newPlanStep.getStatus().name() + "|SCREENSHOT: " + screenshot);

                //Prove It
                if (newPlanStep.getStatus().equals(PlanStepStatusType.FAILED)
                        && isPlanFailed()) {
                    return;
                }
            } //Next!
        }

        //ASSERTIONS HERE
        JSONArray jsonAssertions = (JSONArray) jsonWU.get("plannedAssertions");
        if (jsonAssertions == null) {
            say("INFO", "PLAN:" + getCurrentPlan() + " No ASSERTIONS found!");
        } else {
            Iterator k = jsonAssertions.iterator();
            say("DEBUG", "PLAN:" + getCurrentPlan() + " Loading (" + String.valueOf(jsonAssertions.size()) + ") ASSERTIONS...");
            while (k.hasNext()) { 
                JSONObject jsonAssertion = (JSONObject) k.next();
                setCurrentAssertion(String.valueOf(Integer.parseInt(getCurrentAssertion()) + 1));
                PlanAssertion newPlanAssertion = new PlanAssertion();
                newPlanAssertion.setId(currentPlanAssertion());
                String assertionSense = (jsonAssertion.get("sensor")).toString().toUpperCase();
                String userSense = deduceUserAction(assertionSense);
                if (!newPlanAssertion.setUserSense(userSense)) {
                    say("WARNING", "ASSERTION: I do not know how to '" + userSense + "'!");
                } else {
                }

                String assertionTarget = (jsonAssertion.get("targetType")).toString().toUpperCase();
                String assertionTargetType = deduceUserTarget(assertionTarget);
                String assertionWith = (jsonAssertion.get("withValue")).toString();
                newPlanAssertion.setSenseWith(assertionWith);
                newPlanAssertion.setUserTarget(new Target());
                
                JSONArray jsonLocators = (JSONArray) jsonAssertion.get("locators");
                if (jsonLocators == null) {
                    say("INFO", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + " No LOCATORS found!");
                    newPlanAssertion.getUserTarget().getLocators().add(new Locator());
                } else {
                    say("INFO", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + " Loading (" + String.valueOf(jsonLocators.size()) + ") LOCATORS...");
                    Iterator j = jsonLocators.iterator();
                    while (j.hasNext()) { 
                        JSONObject jsonLocator = (JSONObject) j.next();
                        Locator newTargetLocator = new Locator();
                        if (newTargetLocator.setCriteria((jsonLocator.get("locatorCriteria").toString())) == true
                                && newTargetLocator.setOperator((jsonLocator.get("locatorOperator").toString())) == true) {
                            newTargetLocator.setValue(jsonLocator.get("locatorValue").toString());
                            newPlanAssertion.getUserTarget().getLocators().add(newTargetLocator);
                        }
                        if (!newPlanAssertion.setUserTargetType(assertionTargetType)) {
                            say("WARNING", "STEP: I do not know what a '" + assertionTargetType + "' is!");
                        }
                    } //Next!
                }

                //Say It
                String assertionKey =  (jsonAssertion.get("assertionNumber")).toString(); 
                String assertionAmount = (jsonAssertion.get("amount")).toString();
                String assertionElement =  (jsonAssertion.get("targetType")).toString();
                StringBuilder assMsg = new StringBuilder();
                assMsg.append("Should '")
                        .append(assertionSense.toUpperCase())
                        .append("' '")
                        .append(assertionAmount)
                        .append("' '")
                        .append(assertionElement)
                        .append("' with '")
                        .append(assertionWith)
                        .append("'");

                String assertionName = " '" + assertionSense + "' the '" + assertionTarget + "' with '" + assertionWith + "'";
                
                wr.setCurrentAssertionKey("(" + getCurrentPlan() + "." + assertionKey + ")" + assMsg.toString());

                reportAssertionKeyValuePair("SENSE", assertionSense);
                reportAssertionKeyValuePair("AMOUNT", assertionAmount);
                reportAssertionKeyValuePair("ELEMENT", assertionElement);
                reportAssertionKeyValuePair("WITH", assertionWith);

                say("PLAN:" + getCurrentPlan() + "|ASSERTION:" + newPlanAssertion.getId() + "| " + assertionName);

                newPlanAssertion.getUserTarget().getLocators().stream().forEach((newTargetLocator) -> {
                    say("INFO", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + newPlanAssertion.getId() + "|TARGET| " + newPlanAssertion.getUserTarget().getTargetType().name() + "' with '" + newTargetLocator.getCriteria().name() + "' that is '" + newTargetLocator.getOperator().name() + "' '" + newTargetLocator.getValue() + "'!");
                    reportAssertionKeyValuePair("status", newPlanAssertion.getStatus().name());
                });

                //Do It
                newPlanAssertion.setStatus(doAssertion(newPlanAssertion));
                setLastPlanAssertion();

                if (newPlanAssertion.getStatus().equals(PlanAssertionStatusType.FAILED)
                        && isPlanFailed()) {
                    return;
                } else {

                }

                //Show It
                waitForLoad(driver);
                waitForAjax();
                String screenshot = takeScreenShot();
                reportAssertionKeyValuePair("status", newPlanAssertion.getStatus().name());
                reportAssertionKeyValuePair("screenshot", screenshot);
                say("INFO", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + newPlanAssertion.getId() + "|STATUS:" + newPlanAssertion.getStatus().name() + "|SCREENSHOT: " + screenshot);

                //Prove It
                if (newPlanAssertion.getStatus().equals(PlanAssertionStatusType.FAILED)
                        && isPlanFailed()) {
                    return;
                }
                //Next
                //i++;
            }
        }
    }

    @Override
    public void run() {

    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setProperty(String key, String val) {
        propertyList.put(key, val);
    }

    public String getProperty(String key) {
        return propertyList.get(key);
    }

    public Object whatIsYour(String wiy) {
        return propertyList.get(wiy);
    }

    public void setDesire(String desire) {
        desires.add(desire);
    }

    public void addPlan(Plan plan) {
        testPlans.add(plan);
    }

    public PlanStepStatusType doStep(Step pendingStep) {
        String msg;
        if (pendingStep.getTarget() == null) {
            say("WARNING", "PLAN:" + getCurrentPlan() + "|STEP:" + pendingStep.getId() + "|TARGET not found!");
            //return PlanStepStatusType.FAILED;
        }
        //Do I need to find a target first?
        boolean targetReadyForAction = (pendingStep.getTarget().isFindableTarget()) ? findTarget(pendingStep) : true;
        if (!targetReadyForAction) {
            msg = "I could not find that " + pendingStep.getTarget().getTargetType().name() + "!";
            reportStepExecutionKeyValuePair("FAILED", msg);
            reportStepKeyValuePair("FAILED", msg);
            say("WARNING", "PLAN:" + getCurrentPlan() + "|STEP:" + pendingStep.getId() + "| " + msg);
            return PlanStepStatusType.FAILED;
        }
        msg = pendingStep.getActionType().name();
        reportStepExecutionKeyValuePair("ACTION", msg);
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + pendingStep.getId() + "|TARGET|ACTION:" + pendingStep.getActionType().name());
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + pendingStep.getId() + "| I Will ["
                + (pendingStep.getActionType().name() != null ? pendingStep.getActionType().name() : "")
                + "] the [" + pendingStep.getTarget().getTargetType().name() + "]: "
                + (pendingStep.getTarget().getWebElement() != null ? "<" + pendingStep.getTarget().getTagName() : "")
                + (pendingStep.getTarget().getWebElement() != null ? " id='" + pendingStep.getTarget().getWebElement().getAttribute("id") + "'" : "")
                + (pendingStep.getTarget().getWebElement() != null ? " class='" + pendingStep.getTarget().getWebElement().getAttribute("class") + "'" : "")
                + (pendingStep.getTarget().getWebElement() != null ? " name='" + pendingStep.getTarget().getWebElement().getAttribute("name") + "'" : "")
                + (pendingStep.getTarget().getWebElement() != null ? " />" : "")
                + (pendingStep.getActionParameter() != null ? " with [" + pendingStep.getActionParameter() + "]" : "")
        );

        if (doStepAction(pendingStep)) {
            say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + pendingStep.getId() + "|TARGET:" + pendingStep.getTarget().getTargetType().name() + "|ACTION:" + pendingStep.getActionType().name() + "|STATUS:Done (" + pendingStep.getActionType().name() + " the " + pendingStep.getTarget().getTargetType().name() + ")!");
        } else {
            msg = "I could not " + pendingStep.getActionType().name() + " the " + pendingStep.getTarget().getTargetType().name() + "!";
            reportStepExecutionKeyValuePair("FAILED", msg);
            reportStepKeyValuePair("FAILED", msg);
            say("WARNING", "PLAN:" + getCurrentPlan() + "|STEP:" + pendingStep.getId() + "| ... I could not " + pendingStep.getActionType().name() + " the " + pendingStep.getTarget().getTargetType().name() + "!");
            return PlanStepStatusType.FAILED;
        }
        return PlanStepStatusType.COMPLETED;
    }

    public PlanAssertionStatusType doAssertion(PlanAssertion pendingAssertion) {
        String msg = "";
        //Do I need to find a target first?
        boolean targetReadyForAction = (pendingAssertion.getUserTarget().isFindableTarget()) ? findAssertionTarget(pendingAssertion) : true;
        if (!targetReadyForAction) {
            msg = "I could not find that " + pendingAssertion.getUserTarget().getTargetType().name() + "!";
            reportAssertionKeyValuePair("FAILED", msg);
            say("WARNING", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + pendingAssertion.getId() + "| " + msg);
            return PlanAssertionStatusType.FAILED;
        }
        msg = pendingAssertion.getUserSense().name();
        reportAssertionKeyValuePair("SENSE", msg);
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + pendingAssertion.getId() + "|TARGET|ACTION:" + pendingAssertion.getUserSense().name());

        return PlanAssertionStatusType.COMPLETED;
    }

    private boolean findTarget(Step targetStep) {
        Target target = targetStep.getTarget();
        String msg;

        /*
        *************************************
        *************************************
        * Find RELATIVE_LOCATOR Coordinates *
        *************************************
        *************************************
         */
        boolean useRelativeFilters = findRelativeLocations(target);

        //Calculate the coordinates of RELATIVE_LOCATOR candidates in a separate thread
        threadDone.put("findRelativeLocations", true);

        Thread findRelativeLocations = new Thread() {
            @Override
            public void run() {
                threadDone.put("findRelativeLocations", false);
                if (App.getLoggingLevel() >= 4 && useRelativeFilters) { //if in DEBUG mode: Show the results of findRelativeLocations()
                    target.getLocators().forEach(locator -> {
                        if (locator.getLocatorType().equals(LocatorType.RELATIVE_LOCATOR)) {
                            say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + targetStep.getId() + "|TARGET|RELATIVE_LOCATOR|POINT_RANGES for elements [" + locator.getRelativePosition().name() + " '" + locator.getValue() + "']");
                            locator.getPointRanges().forEach(pointRange -> {
                                say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + targetStep.getId() + "|TARGET|RELATIVE_LOCATOR|POINT_RANGE: maxX=" + pointRange.maxX.toString() + ", minX=" + pointRange.minX.toString());
                            });
                        }
                    });
                }
                threadDone.put("findRelativeLocations", true);
            }
        };
        parallelActions.put("findRelativeLocations", findRelativeLocations);
        // start the thread
        (parallelActions.get("findRelativeLocations")).start();

        /*
        *****************************************
        *****************************************
        * Generate TARGET_LOCATOR Xpath Filters *
        *****************************************
        *****************************************
         */
        StringBuilder xPfilters = new StringBuilder();
        target.getLocators().stream().filter((thisLocator) -> (thisLocator != null
                && thisLocator.getCriteria() != null
                && thisLocator.getLocatorType().equals(LocatorType.TARGET_LOCATOR)
                && !thisLocator.getCriteria().equals(TargetLocatorCriteriaType.ORDINAL_INSTANCE))).map((thisLocator) -> {
            xPfilters.append(getTargetLocatorFilters(thisLocator));
            return thisLocator;
        });

        /*
        ******************************************************
        ******************************************************
        * Find TARGET_LOCATOR Filtered TargetType Candidates *
        ******************************************************
        ******************************************************
         */
        StringBuilder xP = getTargetXpath(targetStep, xPfilters.toString());
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + targetStep.getId() + "|TARGET|XPATH: " + xP.toString());
        //Wait for the browser state to be READY before searching
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + targetStep.getId() + "|TARGET:" + target.getTargetType().name() + "|BROWSER: Waiting on (STEP:" + lastPlanStep() + ")");

        if (!waitForLoad(driver)) {
            msg = "The BROWSER Timed-Out after waiting " + getWaitTimeOut().toString() + " seconds for STEP(" + lastPlanStep() + ")!";
            reportStepExecutionKeyValuePair("FAILED", msg);
            reportStepKeyValuePair("FAILED", msg);
            say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + targetStep.getId() + "|TARGET:" + target.getTargetType().name() + "|BROWSER: Timed-Out after waiting on (STEP:" + lastPlanStep() + ") for " + getWaitTimeOut().toString() + " seconds!");
            return false;
        }

        setLastPlanStep();
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + targetStep.getId() + "|TARGET:" + target.getTargetType().name() + "|CANDIDATE: Collecting...");

        List<WebElement> targetCandidates = getCandidates(xP);
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|STEP:" + targetStep.getId() + "|TARGET|LOCATOR|CANDIDATES: by TargetType = " + String.valueOf(targetCandidates.size()));
        if (targetCandidates.isEmpty()) {
            return false;
        }

        /*
        ******************************************************************
        ******************************************************************
        * Filter TargetType Candidates with RELATIVE_LOCATOR Coordinates *
        ******************************************************************
        ******************************************************************
         */
        List<WebElement> relativeCandidates = new ArrayList<>();
        if (useRelativeFilters) {
            waitForThread("findRelativeLocations");
            relativeCandidates = filterCandidatesByRelativeCandidates(targetCandidates, target);
            say("DEBUG", "PLAN:"
                    + getCurrentPlan()
                    + "|STEP:"
                    + targetStep.getId()
                    + "|TARGET|RELATIVE_LOCATOR|CANDIDATES: "
                    + String.valueOf(relativeCandidates.size()));
            if (relativeCandidates.isEmpty()) {
                return false;
            }
        } else {
            relativeCandidates = targetCandidates;
            say("DEBUG", "PLAN:"
                    + getCurrentPlan()
                    + "|STEP:"
                    + targetStep.getId()
                    + "|TARGET|RELATIVE_LOCATOR|CANDIDATES: "
                    + String.valueOf(relativeCandidates.size()));
        }
        /*
        ***********************************************************
        ***********************************************************
        * Filter Relative Located Candidates with TARGET_LOCATORs *
        ***********************************************************
        ***********************************************************
         */
        List<WebElement> targetFilteredCandidates;
        if (useRelativeFilters) {
            targetFilteredCandidates = relativeCandidates;
        } else {
            targetFilteredCandidates = targetCandidates;
        }

        /*
        ***************************************
        ***************************************
        * Filter and find by ORDINAL_LOCATORs *
        ***************************************
        ***************************************
         */
        target.setWebElement(findOrdinalCandidate(targetFilteredCandidates, target));
        return target.getWebElement() != null;
    }

    public boolean findRelativeLocations(Target target) {
        boolean foundRelativeLocators = false;
        target.getLocators().forEach(loc -> {
            say("DEBUG", loc.getLocatorType().name());
        });
        List<Locator> locators = target.getLocators();

        say("INFO", "Looking for relative locators...(" + String.valueOf(locators.size()) + ")");
        for (Locator locator : locators) {
            say("DEBUG", "Locator type...(" + locator.getLocatorType().name() + ")");
            if (!locator.getLocatorType().equals(LocatorType.RELATIVE_LOCATOR)) {
                continue;
            }

            if (locator.getLocatorType().equals(LocatorType.RELATIVE_LOCATOR)) {
                foundRelativeLocators = true;
                say("DEBUG", "[" + locator.getLocatorType().name() + "] RELATIVE_LOCATOR found!");
                StringBuilder xP = new StringBuilder();
                StringBuilder xPattr = new StringBuilder();
                String textToFind = memory.substituteTags(locator.getValue());

                switch (locator.getCriteria()) {
                    case TEXT_VALUE:
                        xPattr.setLength(0);
                        if (locator.getOperator().equals(TargetLocatorOperatorType.CONTAINING)) {
                            xPattr.append("[contains(text(),'")
                                    .append(textToFind)
                                    .append("')]");
                        }
                        if (locator.getOperator().equals(TargetLocatorOperatorType.EQUAL)) {
                            xPattr.append("[text()='")
                                    .append(textToFind)
                                    .append("']");
                        }
                        if (locator.getOperator().equals(TargetLocatorOperatorType.BEGINNING_WITH)) {
                            xPattr.append("[starts-with(text(),'")
                                    .append(textToFind)
                                    .append("')]");
                        }
                        if (locator.getOperator().equals(TargetLocatorOperatorType.GREATER_THAN)) {
                            xPattr.append("[text() >'")
                                    .append(textToFind)
                                    .append("']");
                        }
                        if (locator.getOperator().equals(TargetLocatorOperatorType.LESS_THAN)) {
                            xPattr.append("[text() <'")
                                    .append(textToFind)
                                    .append("']");
                        }
                        if (locator.getOperator().equals(TargetLocatorOperatorType.NOT_EQUAL)) {
                            xPattr.append("[text() !='")
                                    .append(textToFind)
                                    .append("']");
                        }
                        if (locator.getOperator().equals(TargetLocatorOperatorType.NOT_CONTAINING)) {
                            xPattr.append("[not(contains(text(),'")
                                    .append(textToFind)
                                    .append("'))]");
                        }
                        break;

                    case XPATH_FILTER:
                        xPattr.setLength(0);

                        xPattr.append("[")
                                .append(textToFind)
                                .append("]");
                        break;
                    default:
                        break;
                }

                switch (locator.getLocatorTargetType()) {
                    case TEXT:
                        xP.setLength(0);
                        xP.append("//*");
                        xP.append(xPattr.toString());
                        break;
                    case TEXTBOX:
                        xP.setLength(0);
                        xP.append("//input[@type='text']|//input[@type='text']|//textarea");
                        xP.append(xPattr.toString());
                        ;
                        break;
                    case TEXT_BOX:
                        xP.setLength(0);
                        xP.append("//input[@type='text']|//input[@type='text']|//textarea");
                        xP.append(xPattr.toString());
                        ;
                        break;
                    case DROPDOWN:
                        xP.setLength(0);
                        xP.append("//select");
                        xP.append(xPattr.toString());
                        ;
                        break;
                    case BUTTON:
                        xP.setLength(0);
                        xP.append("//button|//input[@type='button']");
                        xP.append(xPattr.toString());
                        ;
                        break;
                    case LINK:
                        xP.setLength(0);
                        xP.append("//a");
                        xP.append(xPattr.toString());
                        ;
                        break;
                    case IMAGE:
                        xP.setLength(0);
                        xP.append("//img");
                        xP.append(xPattr.toString());
                        break;
                    case FORM:
                        xP.setLength(0);
                        xP.append("//form");
                        xP.append(xPattr.toString());
                        ;
                        break;
                    case TABLE:
                        xP.setLength(0);
                        xP.append("//table");
                        xP.append(xPattr.toString());
                        ;
                        break;
                    case ROW:
                        xP.setLength(0);
                        xP.append("//tr");
                        xP.append(xPattr.toString());
                        ;
                        break;
                    case COLUMN:
                        xP.setLength(0);
                        xP.append("//td)");
                        xP.append(xPattr.toString());
                        ;
                        break;
                    default:
                        break;
                }

                //Iterate through the Relative Locator candidates
                say("DEBUG", ".........CALCULATING COORDINATES FOR XPATH : " + xP.toString());
                ((List<WebElement>) driver.findElements(By.xpath(xP.toString())).stream()
                        .filter(e -> elementStillThere(e))
                        .collect(Collectors.toList()))
                        .forEach((WebElement candidateElement) -> {
                            //Calclate this candidate's point range for the Locator
                            PointRange pointRange = new PointRange(
                                    candidateElement.getLocation().getX(),
                                    candidateElement.getLocation().getX() + candidateElement.getSize().getWidth(),
                                    candidateElement.getLocation().getY(),
                                    candidateElement.getLocation().getY() + candidateElement.getSize().getHeight()
                            );
                            //Add this candidate's point range to the Locator (for later comparisons)
                            locator.addPointRanges(pointRange);
                            locator.setHasLocatorRange(true);
                        });

            } else {
                say("DEBUG", "[" + locator.getLocatorType().name() + "] Not a RELATIVE_LOCATOR!");
            }

        }
        return foundRelativeLocators;
    }

    private boolean findAssertionTarget(PlanAssertion targetAssertion) {
        String msg = "";

        //1st: Find the Relative Locators
        Target target = targetAssertion.getUserTarget();
        //say("DEBUG", "PLAN:"+getCurrentPlan()+"|ASSERTION:"+targetAssertion.getId()+"| Searching for "+target.getTargetType().name()+"...");
        StringBuilder xP = new StringBuilder();
        List<WebElement> fndLst;
        Integer iCount = 0; //instance counter
        XpathHelper xh = new XpathHelper();
        target.setFound((Boolean) false);
        switch (target.getTargetType()) {
            case URL:
                break;
            case DIALOG:
                break;
            case WINDOW:
                break;
            case BROWSER:
                break;
            case PAGE:
                xP.append("//html");
                break;
            case IMAGE:
                xP.append("//img");
                break;
            case FORM:
                xP.append("//form");
                break;
            case TABLE:
                xP.append("//table");
                break;
            case ROW:
                xP.append("//tr");
                break;
            case COLUMN:
                xP.append("//td");
                break;
            case LINK:
                xP.append("//a");
                break;
            case BUTTON:
                xP.append("//input[@type='submit']|//button");
                break;
            case TEXTBOX:
                xP.append("//input[@type='text']|//textarea");
                break;
            case TEXT:
                //xP.append("//text()[normalize-space()]");
                xP.append("//*");
                break;
            default:
                xP.append("//*");
                break;
        }

        if (!targetAssertion.getSenseWith().trim().isEmpty()) {
            if (targetAssertion.getUserSense().equals(PlanAssertionSenseType.SEE.name())) {
                xP.append("[contains(normalize-space(.), '" + targetAssertion.getSenseWith() + "') | [contains(normalize-space(.), '" + targetAssertion.getSenseWith() + "')] "); //aria-label
            }
            if (xP.toString().contains("//input") || xP.toString().contains("//textarea") || xP.toString().contains("//button")) {
                xP.append("[contains(normalize-space(@value), '" + targetAssertion.getSenseWith() + "')]");
            }
        }

        //Wait for the browser state to be READY before searching
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + targetAssertion.getId() + "|TARGET:" + target.getTargetType().name() + "|BROWSER: Waiting on (ASSERTION:" + lastPlanAssertion() + ")");
        if (!waitForLoad(driver)) {
            msg = "The BROWSER Timed-Out after waiting " + getWaitTimeOut().toString() + " seconds for ASSERTION(" + lastPlanAssertion() + ")!";
            //reportAssertionExecutionKeyValuePair("FAILED", msg);
            reportAssertionKeyValuePair("FAILED", msg);
            say("DEBUG", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + targetAssertion.getId() + "|TARGET:" + target.getTargetType().name() + "|BROWSER: Timed-Out after waiting on (ASSERTION:" + lastPlanAssertion() + ") for " + getWaitTimeOut().toString() + " seconds!");
            return false;
        }

        setLastPlanAssertion();
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + targetAssertion.getId() + "|TARGET:" + target.getTargetType().name() + "|CANDIDATE: Collecting...");

        //fndLst = driver.findElements(By.xpath(xP.toString()));
        fndLst = driver.findElements(By.xpath(xP.toString())).stream()
                .filter((WebElement e) -> {
                    return elementStillThere(e);
                })
                .collect(Collectors.toList());

        //Iterate through target's relativeLocators
        Boolean elementMatch = true;
        for (WebElement candidateElement : fndLst) {
            //if (!candidateElement.isDisplayed()) {  continue; }
            iCount += 1;

            //Iterate through locators (and filter)
            msg = "Filter " + target.getTargetType().name() + " candidates.";
            //reportAssertionExecutionKeyValuePair("TARGET FILTERING", msg);
            say("DEBUG", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + targetAssertion.getId() + "|TARGET:" + target.getTargetType().name() + "|CANDIDATE: Filtering...");
            for (Locator thisLocator : target.getLocators()) {
                switch (thisLocator.getCriteria()) {
                    case ORDINAL_INSTANCE:
                        //The ordinal (1st, 2nd, 3rd, 4th... nth match)
                        switch (thisLocator.getOperator()) {
                            case EQUAL:
                                elementMatch = iCount.equals(Integer.valueOf(thisLocator.getValue()));
                                break;
                            case GREATER_THAN:
                                elementMatch = (iCount > Integer.valueOf(thisLocator.getValue()));
                                break;
                            case LESS_THAN:
                                elementMatch = (iCount < Integer.valueOf(thisLocator.getValue()));
                                break;
                            default:
                                break;
                        }
                        break;
                    case TEXT_VALUE:
                        switch (thisLocator.getOperator()) {
                            case EQUAL:
                                //elementMatch = candidateElement.findElements(By.xpath("*[text() = '"+thisLocator.getValue()+"' and not(text()[2])]")).size()>0 ;
                                elementMatch = candidateElement.findElements(By.xpath(
                                        ".[text() = '" + thisLocator.getValue() + "']"
                                        + "|"
                                        + ".[@value = '" + thisLocator.getValue() + "']"
                                )).size() > 0;
                                break;
                            case CONTAINING:
                                elementMatch = candidateElement.findElements(By.xpath(".[contains(text(),'" + thisLocator.getValue() + "')]")).size() > 0;
                                break;
                            case GREATER_THAN:
                                elementMatch = (iCount > Integer.valueOf(thisLocator.getValue()));
                                break;
                            case LESS_THAN:
                                elementMatch = (iCount < Integer.valueOf(thisLocator.getValue()));
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
                say("DEBUG", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + targetAssertion.getId() + "|TARGET:" + target.getTargetType().name() + "|CANDIDATE|LOCATOR|CRITERIA:" + thisLocator.getCriteria().name() + "|" + thisLocator.getOperator().name() + "|VALUE:" + thisLocator.getValue() + "|RESULT:" + elementMatch.toString());
            }

            if (elementMatch) {
                //Found it!
                target.setFound((Boolean) true);
                say("DEBUG", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + targetAssertion.getId() + "|TARGET:" + target.getTargetType().name() + "|LOCATED:" + elementMatch.toString());
                //target.setLastSeen(targetAssertion.getId());
                //target.setId(candidateElement.getAttribute("id"));
                target.setWebElement(candidateElement);
                //target.setxPath(xh.generateXPATH(candidateElement,""));
                //say("DEBUG", "PLAN:"+getCurrentPlan()+"|ASSERTION:"+targetAssertion.getId()+"| Target xPath: "+target.getxPath());
                return true;
            }
        }
        say("DEBUG", "PLAN:" + getCurrentPlan() + "|ASSERTION:" + targetAssertion.getId() + "|TARGET:" + target.getTargetType().name() + "|LOCATED:" + elementMatch.toString());
        return false;
    }

    public Boolean doStepAction(Step planStep) {
        WebElement thisElement = planStep.getTarget().getWebElement();
        switch (planStep.getActionType()) {
            case GO_TO:
                try {
                    say((driver == null ? "Driver is null!" : ""));
                    say((planStep == null ? "planStep is null!" : ""));
                    say((planStep.getActionParameterValue() == null ? "planStep is null!" : ""));
                    driver.get(planStep.getActionParameterValue());
                } catch (UnreachableBrowserException ex) {
                    return false;
                }
                return true;

            case FILL:
                enterKeys(planStep.getActionParameterValue(), planStep.getTarget().getWebElement());
                return true;

            case CLEAR_AND_FILL:
                planStep.getTarget().getWebElement().clear();
                enterKeys(planStep.getActionParameterValue(), planStep.getTarget().getWebElement());
                return true;

            case CLEAR:
                planStep.getTarget().getWebElement().clear();
                return true;

            case CLEAR_AND_ENTER:
                planStep.getTarget().getWebElement().clear();
                planStep.getTarget().getWebElement().sendKeys(Keys.RETURN);
                return true;

            case CLEAR_FILL_AND_ENTER:
                planStep.getTarget().getWebElement().clear();
                enterKeys(planStep.getActionParameterValue(), planStep.getTarget().getWebElement());
                planStep.getTarget().getWebElement().sendKeys(Keys.RETURN);
                return true;

            case FILL_AND_ENTER:
                enterKeys(planStep.getActionParameterValue(), planStep.getTarget().getWebElement());
                planStep.getTarget().getWebElement().sendKeys(Keys.RETURN);
                return true;

            case CLICK:
                thisElement.click();
                //planStep.getTarget().getWebElement().click();
                return true;

            case LEFT_CLICK:
                thisElement.click();
                //planStep.getTarget().getWebElement().click();
                return true;

            case RIGHT_CLICK:
                planStep.getTarget().getWebElement().click();
                return true;

            default:
                return false;
        }
    }

    public void goTo(String URL) {
        driver.get(URL);
        Thread goToUrl = new Thread() {
            @Override
            public void run() {
                driver.get(URL);
            }
        };
        parallelActions.put("GO_TO", goToUrl);

        // start the thread
        new Thread(parallelActions.get("GO_TO")).start();
    }

    public void assertStep(String step) {
        System.out.print("  " + step);
        //See a button with text
        String regexPat = "(I should|.*) see a '(.*)' (button|input|box)";
        Pattern MY_PATTERN = Pattern.compile(regexPat);
        if (step.matches(regexPat)) {
            //Step action
            Matcher m = MY_PATTERN.matcher(step);
            while (m.find()) {
                String s = m.group(2);
                //System.out.print(s);
                if (see("button", s)) {
                    say(" [PASS]");
                } else {
                    say(" [FAIL]");
                }
            }
        }
    }

    public boolean see(String typ, String txt) {
        List<WebElement> fndLst = driver.findElements(By.xpath("//*[text()='" + txt + "']|//*[@value='" + txt + "']|//*[@title='" + txt + "']"));
        if (fndLst.isEmpty()) {
            System.out.print(", Ooops, I do not see a '" + txt + "' button!");
            return false;
        }
        for (WebElement item : fndLst) {
            if (item.getTagName().equals("input")) {
                return true;
            }
            if (item.getTagName().equals("button")) {
                return true;
            }
        }
        return false;
    }

    private String deduceUserTarget(String target) {
        String newTarget = App.getAPP_PROPS().getProperty("userTarget." + target.replaceAll(" ", "_"));

        if (newTarget == null) {
            say("WARNING", "I do not know what a '" + target + "' is!");
        }
        return (newTarget == null || newTarget.isEmpty()) ? target : newTarget;
    }

    private String deduceUserAction(String verb) {
        String newVerb = App.getAPP_PROPS().getProperty("userVerb." + verb.replaceAll(" ", "_"));
        return (newVerb == null || newVerb.isEmpty()) ? verb : newVerb;
    }

    boolean waitForLoad(WebDriver thisDriver) {
        if (browserInitialized != true) {
            try {
                say("INFO", "|BROWSER| Waiting on browser to initialize...");
                parallelActions.get("initializeBrowser").join();
                return true;
            } catch (InterruptedException ex) {
                //Logger.getLogger(WebUser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            new WebDriverWait(thisDriver, 30).until((ExpectedCondition<Boolean>) wd
                    -> ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
            waitForAjax();
        } catch (WebDriverException ex) {
            say("WARNING", "PLAN:" + getCurrentPlan() + "|STEP:" + currentPlanStep() + "|BROWSER: Failed to wait on AJAX (STEP:" + lastPlanStep() + ") [" + ex.getMessage() + "]!");
            //say("ERROR", "PLAN:" + getCurrentPlan() + "|STEP:" + currentPlanStep() + "|BROWSER: Message: [" + ex.getMessage() + "]");

            setPlanFailed(true);
            return true; //Best effort
        }
        return true;
    }

    boolean waitForAjax() {
        //checkPendingRequests();
        return true;
    }

    boolean waitForAjax2() {
        waitWhileElementHasAttributeValue("css=div.rotating-icon", "style", "block");
        return true;
    }

    boolean waitForAjax1() {
        Boolean isJqueryUsed = (Boolean) ((JavascriptExecutor) driver).executeScript("return (typeof(jQuery) != 'undefined')");
        if (isJqueryUsed) {
            Boolean ajaxIsQueued = false;
            Boolean ajaxIsActive = false;
            while (true) {
                // JavaScript test to verify jQuery is active or not
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                try {
                    ajaxIsQueued = (Boolean) (((JavascriptExecutor) driver).executeScript("return Ajax.activeRequestCount == 0"));
                } catch (Exception ex) {
                    say("WARNING", "PLAN:" + getCurrentPlan() + "|STEP:" + currentPlanStep() + "|BROWSER: Failed to query count of active AJAX requests (STEP:" + lastPlanStep() + ") [" + ex.getMessage() + "]!");
                }
                try {
                    ajaxIsActive = (Boolean) (((JavascriptExecutor) driver).executeScript("return $$.active == 0"));
                } catch (Exception ex) {
                    say("WARNING", "PLAN:" + getCurrentPlan() + "|STEP:" + currentPlanStep() + "|BROWSER: Failed to query if AJAX is active (STEP:" + lastPlanStep() + ") [" + ex.getMessage() + "]!");
                }

                if (!ajaxIsActive && !ajaxIsQueued) {
                    return true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
        return true;
    }

    private boolean elementStillThere(WebElement e) {
        try {
            return e.isDisplayed() && e.isEnabled();
        } catch (StaleElementReferenceException ex) {
            return false;
        }
    }

    /**
     * @return the webPages
     */
    public LinkedHashMap<String, WebPage> getWebPages() {
        return webPages;
    }

    /**
     * @param webPages the webPages to set
     */
    public void setWebPages(LinkedHashMap<String, WebPage> webPages) {
        this.webPages = webPages;
    }

    /**
     * @return the targetElement
     */
    public String getTargetElement() {
        return targetElement;
    }

    /**
     * @param targetElement the targetElement to set
     */
    public void setTargetElement(String targetElement) {
        this.targetElement = targetElement;
    }

    /**
     * @return the currentPlan
     */
    public String getCurrentPlan() {
        return currentPlan;
    }

    /**
     * @param currentPlan the currentPlan to set
     */
    public void setCurrentPlan(String currentPlan) {
        this.currentPlan = currentPlan;
    }

    /**
     * @return the currentStep
     */
    public String getCurrentStep() {
        return currentStep;
    }

    /**
     * @param currentStep the currentStep to set
     */
    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * @return the memory
     */
    public Memory getMemory() {
        return memory;
    }

    /**
     * @param memory the memory to set
     */
    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    /**
     * @return the planFailed
     */
    public boolean isPlanFailed() {
        return planFailed;
    }

    /**
     * @param planFailed the planFailed to set
     */
    public void setPlanFailed(boolean planFailed) {
        this.planFailed = planFailed;
    }

    /**
     * @return the lastPlan
     */
    public String getLastPlan() {
        return lastPlan;
    }

    /**
     * @param lastPlan the lastPlan to set
     */
    public void setLastPlan(String lastPlan) {
        this.lastPlan = lastPlan;
    }

    /**
     * @return the lastStep
     */
    public String getLastStep() {
        return lastStep;
    }

    public String getLastAssertion() {
        return lastAssertion;
    }

    /**
     * @param lastStep the lastStep to set
     */
    public void setLastStep(String lastStep) {
        this.lastStep = lastStep;
    }

    public void setLastAssertion(String lastAssertion) {
        this.lastAssertion = lastAssertion;
    }

    private void setLastPlanStep() {
        setLastPlan(getCurrentPlan());
        setLastStep(getCurrentStep());
    }

    private void setLastPlanAssertion() {
        setLastPlan(getCurrentPlan());
        setLastAssertion(getCurrentAssertion());
    }

    /**
     * @return the waitTimeOut
     */
    public Integer getWaitTimeOut() {
        return waitTimeOut;
    }

    /**
     * @param waitTimeOut the waitTimeOut to set
     */
    public void setWaitTimeOut(Integer waitTimeOut) {
        this.waitTimeOut = waitTimeOut;
    }

    /**
     * @return the screenShotPath
     */
    public String getScreenShotPath() {
        return screenShotPath;
    }

    /**
     * @param screenShotPath the screenShotPath to set
     */
    public void setScreenShotPath(String screenShotPath) {
        this.screenShotPath = screenShotPath;
    }

    private void reportStepKeyValuePair(String key, String value) {
        if (wr.getSteps().get(wr.getCurrentStepKey()) == null) {
            wr.getSteps().put(wr.getCurrentStepKey(), new LinkedHashMap());
        }
        ((LinkedHashMap) wr.getSteps().get(wr.getCurrentStepKey()))
                .put(key, value);
    }

    private void reportAssertionKeyValuePair(String key, String value) {
        if (wr.getAssertions().get(wr.getCurrentAssertionKey()) == null) {
            wr.getAssertions().put(wr.getCurrentAssertionKey(), new LinkedHashMap());
        }
        ((LinkedHashMap) wr.getAssertions().get(wr.getCurrentAssertionKey()))
                .put(key, value);
    }

    private void reportStepExecutionKeyValuePair(String key, String value) {
        //if (wr.getSteps().get(wr.getCurrentStepKey()) == null) {
        //    wr.getSteps().put(wr.getCurrentStepKey(), new LinkedHashMap());
        //}
        if (((LinkedHashMap) wr.getSteps().get(wr.getCurrentStepKey()))
                .get("Execution Details") == null) {
            ((LinkedHashMap) wr.getSteps().get(wr.getCurrentStepKey()))
                    .put("Execution Details", new LinkedHashMap());
        }
        ((LinkedHashMap) ((LinkedHashMap) wr.getSteps().get(wr.getCurrentStepKey()))
                .get("Execution Details"))
                .put(key, value);
    }

    public String takeScreenShot() {
        try {
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String ssFile = getScreenShotPath() + "/screenshot" + App.whenFile() + ".png";
            FileUtils.copyFile(scrFile, new File(ssFile));
            return ssFile;
        } catch (TimeoutException | UnreachableBrowserException | IOException ex) {
            say("ERROR", "Could not take a screen shot! " + ex.getMessage());
        }
        return "";
    }

    public void closeBrowser() {
        if (browserInitialized != true) {
            try {
                parallelActions.get("initializeBrowser").join(); // .wait();
                //wait();
            } catch (InterruptedException ex) {
                //Logger.getLogger(WebUser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!driver.toString().contains("null")) {
            driver.quit();
            browserInitialized = false;
        }
    }

    public void closeCurrentBrowserWindow() {
        if (!driver.toString().contains("null")) {
            driver.close();
        }
    }

    /**
     * @return the webUserId
     */
    public UUID getWebUserId() {
        return webUserId;
    }

    /**
     * @param webUserId the webUserId to set
     */
    public void setWebUserId(UUID webUserId) {
        this.webUserId = webUserId;
    }

    /**
     * @return the currentAssertion
     */
    public String getCurrentAssertion() {
        return currentAssertion;
    }

    /**
     * @param currentAssertion the currentAssertion to set
     */
    public void setCurrentAssertion(String currentAssertion) {
        this.currentAssertion = currentAssertion;
    }

    public void waitWhileElementHasAttributeValue(String locator, String attribute, String value) {
        while (driver.findElement(LocatorStrategy.getLocatorMethod(locator)).getAttribute(attribute).contains(value)) {
            int timeout = 10;
            if (timeout > 0) {
                timeout--;
                try {
                    say("DEBUG", attribute + "\t" + value);
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
            }
        }
    }

    //public static void checkPendingRequests(FirefoxDriver driver) {
    public void checkPendingRequests() {

        int timeoutInSeconds = 5;
        try {
            if (driver instanceof JavascriptExecutor) {
                JavascriptExecutor jsDriver = (JavascriptExecutor) driver;

                for (int i = 0; i < timeoutInSeconds; i++) {
                    Object numberOfAjaxConnections = jsDriver.executeScript("return window.openHTTPs");
                    // return should be a number
                    if (numberOfAjaxConnections instanceof Long) {
                        Long n = (Long) numberOfAjaxConnections;
                        say("DEBUG", "|BROWSER| Number of active AJAX calls: " + n);
                        if (n.longValue() == 0L) {
                            break;
                        }
                    } else {
                        // If it's not a number, the page might have been freshly loaded indicating the monkey
                        // patch is replaced or we haven't yet done the patch.
                        //monkeyPatchXMLHttpRequest(driver);
                        monkeyPatchXMLHttpRequest();
                    }
                    Thread.sleep(1000);
                }
            } else {
                say("DEBUG", "|BROWSER| Web driver: " + driver + " cannot execute javascript");
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    public void monkeyPatchXMLHttpRequest() {
        try {
            if (driver instanceof JavascriptExecutor) {
                JavascriptExecutor jsDriver = (JavascriptExecutor) driver;
                Object numberOfAjaxConnections = jsDriver.executeScript("return window.openHTTPs");
                if (numberOfAjaxConnections instanceof Long) {
                    return;
                }
                String script = "  (function() {"
                        + "var oldOpen = XMLHttpRequest.prototype.open;"
                        + "window.openHTTPs = 0;"
                        + "XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {"
                        + "window.openHTTPs++;"
                        + "this.addEventListener('readystatechange', function() {"
                        + "if(this.readyState == 4) {"
                        + "window.openHTTPs--;"
                        + "}"
                        + "}, false);"
                        + "oldOpen.call(this, method, url, async, user, pass);"
                        + "}"
                        + "})();";
                jsDriver.executeScript(script);
            } else {
                say("DEBUG", "|BROWSER| Web driver: " + driver + " cannot execute javascript");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void enterKeys(String keys, WebElement target) {
        int speed = 1000 / Integer.valueOf("??".equals(memory.recall("keysPerSecond").toString()) ? "4" : memory.recall("keysPerSecond").toString());
        enterKeys(keys, target, speed);
    }

    private void enterKeys(String keys, WebElement target, int speed) {
        for (char ch : keys.toCharArray()) {
            target.sendKeys(String.valueOf(ch));
            try {
                Thread.sleep(speed);
            } catch (Exception ex) {
            }
        }
    }

    private List<WebElement> getCandidates(StringBuilder xP) {
        //Select only DISPLAYED elements by xPath
        return driver.findElements(By.xpath(xP.toString())).stream()
                .filter(e -> elementStillThere(e))
                .collect(Collectors.toList());
    }

    private StringBuilder getTargetXpath(Step targetStep, String filters) {
        Target target = targetStep.getTarget();
        StringBuilder xP = new StringBuilder();
        switch (target.getTargetType()) {
            case LINK:
                xP.append("//a");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP
                            .append("[contains(@title, '").append(targetStep.getActionParameterValue()).append("')]")
                            .append(filters)
                            .append(" | ")
                            .append("//a")
                            .append("[contains(normalize-space(.), '").append(targetStep.getActionParameterValue()).append("')]")
                            .append(filters)
                            .append(" | ")
                            .append("//a")
                            .append("[contains(@alt, '").append(targetStep.getActionParameterValue()).append("')]")
                            .append(filters);
                }
                break;
            case URL:
                break;
            case DIALOG:
                break;
            case WINDOW:
                break;
            case BROWSER:
                break;
            case PAGE:
                xP.append("//html");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(text(), '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]")
                            .append(filters);
                }
                break;
            case IMAGE:
                xP.append("//img");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(., '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]")
                            .append(filters);
                }
                break;
            case FORM:
                xP.append("//form");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(text(), '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]")
                            .append(filters);
                }
                break;
            case TABLE:
                xP.append("//table");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(text(), '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]")
                            .append(filters);
                }
                break;
            case ROW:
                xP.append("//tr");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(text(), '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]")
                            .append(filters);
                }
                break;

            case COLUMN:
                xP.append("//td");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(text(), '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]")
                            .append(filters);
                }
                break;
            case OBJECT:
                xP.append("//*");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(text(), '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]")
                            .append(filters);
                }
                break;
            case BUTTON:
                StringBuilder newFilter = new StringBuilder();
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    newFilter.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(text(), '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]");
                }
                xP
                        .append("//input[@type='submit']")
                        .append(newFilter.toString())
                        .append(filters)
                        .append(" | ")
                        .append("//button")
                        .append(newFilter.toString())
                        .append(filters);
                break;
            case TEXTBOX:
                //xP.append("//input[@type='text']|//input[@type='']|//textarea");
                xP
                        .append("//input[@type='text']")
                        .append(filters)
                        .append(" | ")
                        .append("//input[@type='']")
                        .append(filters)
                        .append(" | ")
                        .append("//textarea")
                        .append(filters);
                break;
            case TEXT:
                xP.append("//*[boolean(text())]");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(., '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]")
                            .append(filters);
                }
                break;
            case DROPDOWN:
                xP
                        .append("//select[contains(./text(),'")
                        .append(targetStep.getActionParameterValue())
                        .append("') or contains(text(),'")
                        .append(targetStep.getActionParameterValue())
                        .append("')]");
                //Select dropdown = new Select(driver.findElement(By.id("designation")));
                //dropdown.selectByVisibleText("Programmer ");
                break;
            default:
                xP.append("//*");
                if (!targetStep.getActionParameterValue().trim().isEmpty()) {
                    xP.append("[contains(@title, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(text(), '")
                            .append(targetStep.getActionParameterValue())
                            .append("') or contains(@alt, '")
                            .append(targetStep.getActionParameterValue())
                            .append("') ]")
                            .append(filters);
                }
                break;
        }
        return xP;
    }

    private List<WebElement> filterCandidatesByRelativeCandidates(List<WebElement> unfilteredCandidates, Target target) {
        say("DEBUG", "Relative candidates count BEFORE= " + String.valueOf(unfilteredCandidates.size()));
        List<Locator> cumulativeLocators = new ArrayList<>();
        ((List<Locator>) target.getLocators().stream().filter((locator) -> (locator.getLocatorType().equals(LocatorType.RELATIVE_LOCATOR))).collect(Collectors.toList())).stream().forEach((locator) -> {
            cumulativeLocators.add(locator);
        });
        say("DEBUG", "Relative locators found= " + String.valueOf(cumulativeLocators.size()));
        List<WebElement> relativeFilterCandidates = new ArrayList<>();

        unfilteredCandidates.stream().forEach((WebElement candidateElement) -> {
            //Calculate the co-ordinates for each unfiltered Candidate: candidatePointRangeA
            PointRange candidatePointRangeA = new PointRange(
                    candidateElement.getLocation().getX(),
                    candidateElement.getLocation().getX() + candidateElement.getSize().getWidth(),
                    candidateElement.getLocation().getY(),
                    candidateElement.getLocation().getY() + candidateElement.getSize().getHeight()
            );

            cumulativeLocators.stream().forEach((Locator locator) -> {
                locator.getPointRanges().stream().forEach((pointRangeB) -> {
                    if (candidatePointRangeA.isPositioned(locator.getRelativePosition(), pointRangeB)) {
                        if (locator.equals(cumulativeLocators.get(0)) || relativeFilterCandidates.contains(candidateElement)) {
                            relativeFilterCandidates.add(candidateElement);
                        }
                    } else if (!locator.equals(cumulativeLocators.get(0)) && relativeFilterCandidates.contains(candidateElement)) {
                        relativeFilterCandidates.remove(candidateElement);
                    }
                });
            });
        });
        say("DEBUG", "Relative candidates AFTER= " + String.valueOf(relativeFilterCandidates.size()));
        return relativeFilterCandidates;
    }

    private WebElement findOrdinalCandidate(List<WebElement> unfilteredCandidates, Target target) {
        say("DEBUG", "Ordinal candidates BEFORE= " + String.valueOf(unfilteredCandidates.size()));
        List<WebElement> ordinalFilterCandidates = new ArrayList<>();
        Integer ordMax = ordinalFilterCandidates.size();
        Integer iCount = 0;
        String instance = null;
        boolean pickFromRange = false;
        for (WebElement candidateElement : unfilteredCandidates) {
            iCount++;
            for (Locator thisLocator : target.getLocators()) {
                if (thisLocator.getCriteria().equals(TargetLocatorCriteriaType.ORDINAL_INSTANCE)) {
                    Integer ordNum = StringHelper.textToOrdinal(thisLocator.getValue().trim().toLowerCase(), ordMax);

                    switch (thisLocator.getOperator()) {
                        case EQUAL:
                            //The ordinal (1st, 2nd, 3rd, 4th... nth match)
                            //save the first EQUAL criteria value > 0 to 'instance' for the subsequent final seletion
                            if (instance == null) {
                                instance = thisLocator.getValue();
                            }
                            break;
                        case GREATER_THAN:
                            if (iCount > ordNum) {
                                ordinalFilterCandidates.add(candidateElement);
                            }
                            ;
                            pickFromRange = true;
                            break;
                        case LESS_THAN:
                            if (iCount < ordNum) {
                                ordinalFilterCandidates.add(candidateElement);
                            }
                            ;
                            pickFromRange = true;
                            break;
                        case DIVISIBLE_BY:
                            if (((iCount % ordNum) == 0)) {
                                ordinalFilterCandidates.add(candidateElement);
                            }
                            ;
                            pickFromRange = true;
                            break;
                        case NOT_DIVISIBLE_BY:
                            if (!((iCount % ordNum) == 0)) {
                                ordinalFilterCandidates.add(candidateElement);
                            }
                            pickFromRange = true;
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }
        }
        if (!pickFromRange) {
            ordinalFilterCandidates = unfilteredCandidates;
        }
        say("DEBUG", "Ordinal candidates AFTER= " + String.valueOf(ordinalFilterCandidates.size())
                + "| Ordinal target INSTANCE= " + instance
        );

        Integer instanceNum = StringHelper.textToOrdinal(instance, ordinalFilterCandidates.size());

        if ((instanceNum == 0 && ordinalFilterCandidates.size() == 1)
                || (instanceNum > 0 && instanceNum <= ordinalFilterCandidates.size())) {
            return ordinalFilterCandidates.get(instanceNum - 1);
        }
        if ((instanceNum == 0 && ordinalFilterCandidates.size() > 1)) {
            say("WARNING", "Ambiguous web page element selection! Using the 1st instance...");
            return ordinalFilterCandidates.get(0);
        }
        say("WARNING", "Could not find that web page element!");
        return null;
    }

    private String getTargetLocatorFilters(Locator thisLocator) {
        StringBuilder thisFilter = new StringBuilder();
        switch (thisLocator.getCriteria()) {
            case XPATH_FILTER:
                switch (thisLocator.getOperator()) {
                    case EQUAL:
                        thisFilter.append("[").append(thisLocator.getValue()).append("]");
                        break;
                    case NOT_EQUAL:
                        thisFilter.append("[not(").append(thisLocator.getValue()).append(")]");
                        break;
                    default:
                        say("WARNING", "XPATH_EXPRESSION only Locatr Operators EQUAL or NOT_EQUAL!");
                        break;
                }
                break;
            case TEXT_VALUE:
                switch (thisLocator.getOperator()) {
                    case EQUAL:
                        thisFilter
                                .append("[text() = '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@title = '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@alt = '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@value = '").append(thisLocator.getValue())
                                .append("']");
                        break;
                    case NOT_EQUAL:
                        thisFilter
                                .append("[not(text() = '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@title = '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@alt = '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@value = '").append(thisLocator.getValue())
                                .append("')]");
                        break;
                    case CONTAINING:
                        thisFilter
                                .append("[contains(text(),'").append(thisLocator.getValue()).append("')")
                                .append(" or ")
                                .append("contains(@title,'").append(thisLocator.getValue()).append("')")
                                .append(" or ")
                                .append("contains(@alt,'").append(thisLocator.getValue()).append("')")
                                .append(" or ")
                                .append("contains(@value,'").append(thisLocator.getValue())
                                .append("')]");
                        break;
                    case NOT_CONTAINING:
                        thisFilter
                                .append("[not(contains(text(),'").append(thisLocator.getValue()).append("')")
                                .append(" or ")
                                .append("contains(@title,'").append(thisLocator.getValue()).append("')")
                                .append(" or ")
                                .append("contains(@alt,'").append(thisLocator.getValue()).append("')")
                                .append(" or ")
                                .append("contains(@value,'").append(thisLocator.getValue())
                                .append("'))]");
                        break;
                    case GREATER_THAN:
                        thisFilter
                                .append("[text() > '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@title > '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@alt > '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@value > '").append(thisLocator.getValue())
                                .append("']");
                        break;
                    case LESS_THAN:
                        thisFilter
                                .append("[text() < '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@title < '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@alt < '").append(thisLocator.getValue()).append("'")
                                .append(" or ")
                                .append("@value < '").append(thisLocator.getValue())
                                .append("']");
                        break;
                    default:
                        say("WARNING", "XPATH_EXPRESSION only Locator Operators EQUAL or NOT_EQUAL!");
                        break;
                }
                break;
            default:
                break;
        }
        return thisFilter.toString();
    }

    public void say(String what) {
        if (!what.contains("PLAN:")) {
            what = "PLAN:" + getCurrentPlan() + "|STEP:" + getCurrentStep() + "|" + what;
        }
        App.say(what);
    }

    public void say(String type, String what) {
        if (!what.contains("PLAN:")) {
            what = "PLAN:" + getCurrentPlan() + "|STEP:" + getCurrentStep() + "|" + what;
        }
        App.say(type, what);
    }

    private void waitForThread(String threadName) {
        if (!threadDone.get(threadName)) {
            try {
                parallelActions.get(threadName).join();
            } catch (InterruptedException ex) {
                say("ERROR", "Failed waiting on thread ["+threadName+"]: "+ex.getMessage());
            }
        }
    }
}
