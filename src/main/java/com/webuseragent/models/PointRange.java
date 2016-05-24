/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.models;

/**
 *
 * @author Clive
 */
public class PointRange {
    public Integer minX;
    public Integer maxX;
    public Integer minY;
    public Integer maxY;
    
    PointRange (Integer minX, Integer maxX, Integer minY, Integer maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;        
    }
    
    PointRange (int minX, int maxX, int minY, int maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;        
    }
    
    public String getRelativePosition(PointRange testRange){
        StringBuilder rPos = new StringBuilder();
        String samePlace = "";
        
        //
        if (testRange.maxX <= this.minX) {
            rPos.append("TO_THE_RIGHT_AND");
        } else {
            if (testRange.minX >= this.maxX) {
                rPos.append("TO_THE_LEFT_AND");
            } else {
                rPos.append( "DIRECTLY_");
            }
        }     
        //
        if (testRange.maxY < this.minY) {
            rPos.append("ABOVE");
        } else {
            if (testRange.minY >= this.maxY) {
                rPos.append("BELOW");
            } else {
                rPos.append("DIRECTLY");
            }
        }
        String rPosName= rPos.toString().replaceAll("DIRECTLY_DIRECTLY", "IN_THE_SAME_PLACE");
        rPosName= rPos.toString().replaceAll("TO_THE_LEFT_AND_DIRECTLY", "DIRECTLY_TO_THE_LEFT");
        rPosName= rPos.toString().replaceAll("TO_THE_RIGHT_AND_DIRECTLY", "DIRECTLY_TO_THE_RIGHT");
        
        return rPosName;
    }
    
    // does this PointRange a intersect b?
    public boolean intersects(PointRange b) {
        PointRange a = this;
        if (b.minX  <= a.maxX && b.minX >= a.minX) return true;
        if (a.minX <= b.maxX && a.minX >= b.minX) return true;
        return false;
    }
    
    public boolean isPositioned(RelativePosition position, PointRange b) {
        PointRange a = this;
        switch (position) {
            case ABOVE:
                if (a.maxY <= b.minY) return true;
                break;
            case BELOW:
                if (a.minY >= b.maxY) return true;
                break;
            case TO_THE_LEFT:
                if (a.maxX <= b.minX) return true;
                break;
            case TO_THE_RIGHT:
                if (a.minX >= b.maxX) return true;
                break;
            case BESIDE:
                if ((a.maxX <= b.minX || a.minX >= b.maxX) && (!(a.maxY <= b.minY) && !(a.minY >= b.maxY) )) return true;
                break;
            case NOT_BESIDE:
                if (a.maxY <= b.minY || a.minY >= b.maxY) return true;
                break;
            case DIRECTLY_TO_THE_LEFT:
                if ((a.maxX <= b.minX) && (!(a.maxY <= b.minY) && !(a.maxY >= b.minY) )) return true;
                break;
            case DIRECTLY_TO_THE_RIGHT:
                if ((a.minX >= b.maxX) && (!(a.maxY <= b.minY) && !(a.maxY >= b.minY) )) return true;
                break;
                /*
    CANNOT_BE_FOUND,
    DIRECTLY_ABOVE,
    DIRECTLY_BELOW,
    IN_THE_SAME_COLUMN,
    IN_THE_SAME_ROW,
    OVER_LAPPING,
    TO_THE_LEFT_AND_ABOVE,
    TO_THE_LEFT_AND_BELOW,
    TO_THE_RIGHT_AND_ABOVE,
    TO_THE_RIGHT_AND_BELOW
                */
                
            default:
                return false;
        }
        return false;
    }
    
}
