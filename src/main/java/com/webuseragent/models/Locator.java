/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author Clive
 */
public class Locator {

    private LocatorType locatorType;
    private RelativePosition relativePosition;
    private TargetType locatorTargetType = TargetType.URL;
    private TargetLocatorCriteriaType criteria;
    private TargetLocatorOperatorType operator;
    private String value;
    
    //For Fast ENM Referencing/Validation 
    private HashSet<LocatorType> locatorTypeList = new HashSet();
    private HashSet<RelativePosition> locatorRelativePositionList = new HashSet();
    private HashSet<TargetType> locatorTargetTypeList = new HashSet();
    
    private ArrayList<PointRange> pointRanges = new ArrayList<>();
    private Boolean hasLocatorRange;
    
    public Locator() {
        this.locatorType = LocatorType.TARGET_LOCATOR;
        this.hasLocatorRange = false;
        this.relativePosition = RelativePosition.IN_THE_SAME_ROW;
        
        this.locatorTypeList.addAll(Arrays.asList(LocatorType.values()));
        this.locatorRelativePositionList.addAll(Arrays.asList(RelativePosition.values()));
        this.locatorTargetTypeList.addAll(Arrays.asList(TargetType.values()));
        
    }

    public Boolean setCriteria(String crit) {
        if (crit == null || crit.isEmpty()) {
            return false;
        }
        setCriteria(getCriteriaType(crit));
        return !(criteria == null);
    }

    public static TargetLocatorCriteriaType getCriteriaType(String enumTxt) {
        for (TargetLocatorCriteriaType c : TargetLocatorCriteriaType.values()) {
            if (c.name().equals(enumTxt)) {
                return c;
            }
        }
        return null;
    }

    public Boolean setOperator(String oper) {
        if (oper == null || oper.isEmpty()) {
            return false;
        }
        setOperator(getOperatorType(oper));
        return !(operator == null);
    }

    public static TargetLocatorOperatorType getOperatorType(String enumTxt) {
        for (TargetLocatorOperatorType o : TargetLocatorOperatorType.values()) {
            if (o.name().equals(enumTxt)) {
                return o;
            }
        }
        return TargetLocatorOperatorType.EQUAL;
    }

    /**
     * @return the criteria
     */
    public TargetLocatorCriteriaType getCriteria() {
        return criteria;
    }

    /**
     * @param criteria the criteria to set
     */
    public void setCriteria(TargetLocatorCriteriaType criteria) {
        this.criteria = criteria;
    }

    /**
     * @return the operator
     */
    public TargetLocatorOperatorType getOperator() {
        return operator;
    }

    /**
     * @param operator the operator to set
     */
    public void setOperator(TargetLocatorOperatorType operator) {
        this.operator = operator;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
        
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the locatorType
     */
    public LocatorType getLocatorType() {
        return locatorType;
    }

    /**
     * @param locatorType the locatorType to set
     */
    
    public void setLocatorType(LocatorType locatorType) {
        this.locatorType = locatorType;
    }
    public boolean setLocatorType(String locatorTypeName) {
        if (locatorTypeName==null) {
            return false;
        }
        for (LocatorType locType: locatorTypeList) {
            if (locType.name().equalsIgnoreCase(locatorTypeName)) {
                this.locatorType = LocatorType.valueOf(locatorTypeName);
                return true;
            }
        }
        return false;
    }

    /**
     * @return the relativePosition
     */
    public RelativePosition getRelativePosition() {
        return relativePosition;
    }

    /**
     * @param relativePosition the relativePosition to set
     */
    public void setRelativePosition(RelativePosition relativePosition) {
        this.relativePosition = relativePosition;
    }

    public boolean setRelativePosition(String relativePositionName) {
        if (relativePositionName==null) {
            return false;
        }
        for (RelativePosition relPosType: locatorRelativePositionList) {
            if (relPosType.name().equalsIgnoreCase(relativePositionName)) {
                this.relativePosition = RelativePosition.valueOf(relativePositionName);
                return true;
            }
        }
        return false;
    }
    /**
     * @return the locatorTargetType
     */
    public TargetType getLocatorTargetType() {
        return locatorTargetType;
    }

    /**
     * @param locatorTargetType the locatorTargetType to set
     */
    public void setLocatorTargetType(TargetType locatorTargetType) {
        this.locatorTargetType = locatorTargetType;
    }
    
    public boolean setLocatorTargetType(String locatorTargetTypeName) {
        if (locatorTargetTypeName==null) {
            return false;
        }
        for (TargetType targetType: locatorTargetTypeList) {
            if (targetType.name().equalsIgnoreCase(locatorTargetTypeName)) {
                this.locatorTargetType = TargetType.valueOf(locatorTargetTypeName);
                return true;
            }
        }
        return false;
    }

    /**
     * @return the pointRanges
     */
    public ArrayList<PointRange> getPointRanges() {
        return pointRanges;
    }

    /**
     * @param pointRanges the pointRanges to set
     */
    public void addPointRanges(PointRange pointRanges) {
        this.pointRanges.add(pointRanges);
    }

    /**
     * @return the hasLocatorRange
     */
    public Boolean getHasLocatorRange() {
        return hasLocatorRange;
    }

    /**
     * @param hasLocatorRange the hasLocatorRange to set
     */
    public void setHasLocatorRange(Boolean hasLocatorRange) {
        this.hasLocatorRange = hasLocatorRange;
    }
    
}//