/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.models;

import java.util.LinkedHashMap;

/**
 *
 * @author Clive
 */
public class PlanAssertion {

    public static PlanAssertionSenseType getSenseType(String enumTxt) {
        for (PlanAssertionSenseType c : PlanAssertionSenseType.values()) {
            if (c.name().equals(enumTxt)) {
                return c;
            }
        }
        return PlanAssertionSenseType.NONE;
    }

    public static TargetType getTargetType(String enumTxt) {
        for (TargetType c : TargetType.values()) {
            if (c.name().equals(enumTxt)) {
                return c;
            }
        }
        return TargetType.NONE;
    }

    public PlanAssertion() {
        this.preSenseScreenShots = new LinkedHashMap();
        this.userSense = PlanAssertionSenseType.NONE;
        this.status = PlanAssertionStatusType.PENDING;
        id = "";
    }

    private String id;
    private PlanAssertionStatusType status;
    private PlanAssertionSenseType userSense;
    private Target userTarget;
    private String senseWith;
    private LinkedHashMap<String, Object> preSenseScreenShots;

    public Boolean setUserSense(String verb) {
        setUserSense(getSenseType(verb));
        return !(userSense.equals(PlanAssertionSenseType.NONE));
    }
    public Boolean setUserTargetType(String target) {
        userTarget.setTargetType(getTargetType(target)) ;
        return !(userTarget.getTargetType().equals(TargetType.NONE)) ;
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
     * @return the status
     */
    public PlanAssertionStatusType getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(PlanAssertionStatusType status) {
        this.status = status;
    }

    /**
     * @return the userSense
     */
    public PlanAssertionSenseType getUserSense() {
        return userSense;
    }

    /**
     * @param userSense the userSense to set
     */
    public void setUserSense(PlanAssertionSenseType userSense) {
        this.userSense = userSense;
    }

    /**
     * @return the userTarget
     */
    public Target getUserTarget() {
        return userTarget;
    }

    /**
     * @param userTarget the userTarget to set
     */
    public void setUserTarget(Target userTarget) {
        this.userTarget = userTarget;
    }
    
    /**
     * @return the senseWith
     */
    public String getSenseWith() {
        return senseWith;
    }

    /**
     * @param senseWith the senseWith to set
     */
    public void setSenseWith(String senseWith) {
        this.senseWith = senseWith;
    }

    /**
     * @return the preSenseScreenShots
     */
    public LinkedHashMap<String, Object> getPreSenseScreenShots() {
        return preSenseScreenShots;
    }

    /**
     * @param preSenseScreenShots the preSenseScreenShots to set
     */
    public void setPreSenseScreenShots(LinkedHashMap<String, Object> preSenseScreenShots) {
        this.preSenseScreenShots = preSenseScreenShots;
    }
}
