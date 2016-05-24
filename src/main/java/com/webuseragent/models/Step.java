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
public class Step {
    
    private String id;
    private PlanStepStatusType status;    
    private ActionType actionType;
    private Target target;
    private String actionParameter;
    private String actionParameterValue;
    private LinkedHashMap<String, Object> preActionScreenShots;
    
    public static ActionType getActionType(String enumTxt) {
        for (ActionType c : ActionType.values()) {
            if (c.name().equals(enumTxt)) {
                return c;
            }
        }
        return ActionType.NONE;
    }
    public static TargetType getTargetType(String enumTxt) {
        for (TargetType c : TargetType.values()) {
            if (c.name().equals(enumTxt)) {
                return c;
            }
        }
        return TargetType.NONE;
    }
    
    public Step (){
        this.preActionScreenShots = new LinkedHashMap();
        this.actionType = ActionType.NONE;
        this.status = PlanStepStatusType.PENDING;
        id = "";
    }
    
    public Boolean setUserAction(String verb) {
        setActionType(Step.this.getActionType(verb)) ;
        return !(actionType.equals(ActionType.NONE)) ;
    }    
    public Boolean setUserTargetType(String target) {
        this.target.setTargetType(getTargetType(target)) ;
        return !(this.target.getTargetType().equals(TargetType.NONE)) ;
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
    public PlanStepStatusType getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(PlanStepStatusType status) {
        this.status = status;
    }

    /**
     * @return the actionType
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * @param actionType the actionType to set
     */
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * @return the target
     */
    public Target getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * @return the actionParameter
     */
    public String getActionParameter() {
        return actionParameter;
    }

    /**
     * @param actionParameter the actionParameter to set
     */
    public void setActionParameter(String actionParameter) {
        this.actionParameter = actionParameter;
    }

    /**
     * @return the preActionScreenShots
     */
    public LinkedHashMap<String, Object> getPreActionScreenShots() {
        return preActionScreenShots;
    }

    /**
     * @param preActionScreenShots the preActionScreenShots to set
     */
    public void setPreActionScreenShots(LinkedHashMap<String, Object> preActionScreenShots) {
        this.preActionScreenShots = preActionScreenShots;
    }

    /**
     * @return the actionParameterValue
     */
    public String getActionParameterValue() {
        return actionParameterValue;
    }

    /**
     * @param actionParameterValue the actionParameterValue to set
     */
    public void setActionParameterValue(String actionParameterValue) {
        this.actionParameterValue = actionParameterValue;
    }
    
}
