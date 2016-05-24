/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Clive
 */
public class Plan {
    private ArrayList<Step> stepsPASSED = new ArrayList<>();
    private ArrayList<Step> stepsFAILED = new ArrayList<>();
    
    private ArrayList<PlanAssertion> assertionsPASSED;
    private ArrayList<PlanAssertion> assertionsFAILED;
    
    //ALPHA Version, to be deprecated
    private String role = "Visitor"; //As a <role>
    private String desire = "Find the meaning of a word"; ///I want to "Find the meaning of a word"
    ///I want to "Find the meaning of a word"
    private List<String[]> testSteps = Collections.synchronizedList(new ArrayList<String[]>()); // "Go to <URL>"
    private List<String> steps_assertion; 

    public Plan() {
        this.steps_assertion = Collections.synchronizedList(new ArrayList<String>());
        this.assertionsFAILED = new ArrayList<>();
        this.assertionsPASSED = new ArrayList<>();
    }

    /**
     * @return the stepsPASSED
     */
    public ArrayList<Step> getStepsPASSED() {
        return stepsPASSED;
    }

    /**
     * @param stepsPASSED the stepsPASSED to set
     */
    public void setStepsPASSED(ArrayList<Step> stepsPASSED) {
        this.stepsPASSED = stepsPASSED;
    }

    /**
     * @return the stepsFAILED
     */
    public ArrayList<Step> getStepsFAILED() {
        return stepsFAILED;
    }

    /**
     * @param stepsFAILED the stepsFAILED to set
     */
    public void setStepsFAILED(ArrayList<Step> stepsFAILED) {
        this.stepsFAILED = stepsFAILED;
    }

    /**
     * @return the assertionsPASSED
     */
    public ArrayList<PlanAssertion> getAssertionsPASSED() {
        return assertionsPASSED;
    }

    /**
     * @param assertionsPASSED the assertionsPASSED to set
     */
    public void setAssertionsPASSED(ArrayList<PlanAssertion> assertionsPASSED) {
        this.assertionsPASSED = assertionsPASSED;
    }

    /**
     * @return the assertionsFAILED
     */
    public ArrayList<PlanAssertion> getAssertionsFAILED() {
        return assertionsFAILED;
    }

    /**
     * @param assertionsFAILED the assertionsFAILED to set
     */
    public void setAssertionsFAILED(ArrayList<PlanAssertion> assertionsFAILED) {
        this.assertionsFAILED = assertionsFAILED;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the desire
     */
    public String getDesire() {
        return desire;
    }

    /**
     * @param desire the desire to set
     */
    public void setDesire(String desire) {
        this.desire = desire;
    }

    /**
     * @return the testSteps
     */
    public List<String[]> getTestSteps() {
        return testSteps;
    }

    /**
     * @param testSteps the testSteps to set
     */
    public void setTestSteps(List<String[]> testSteps) {
        this.testSteps = testSteps;
    }

    /**
     * @return the steps_assertion
     */
    public List<String> getSteps_assertion() {
        return steps_assertion;
    }

    /**
     * @param steps_assertion the steps_assertion to set
     */
    public void setSteps_assertion(List<String> steps_assertion) {
        this.steps_assertion = steps_assertion;
    }

}
