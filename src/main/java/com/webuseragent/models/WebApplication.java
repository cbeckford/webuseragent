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
public class WebApplication {
    private static String application;
    private static String domain;
    private static String URL;
    private static  List<String> roles = Collections.synchronizedList(new ArrayList<String>());
    private static List<String> features = Collections.synchronizedList(new ArrayList<String>());    
    private static List<String> feature_prerequisites = Collections.synchronizedList(new ArrayList<String>());    
    private static List<Plan> userstories = Collections.synchronizedList(new ArrayList<Plan>()); //
    private static List<String> feature_assertion = Collections.synchronizedList(new ArrayList<String>()); //confirms feature
    
    public static void setName(String name) {
        application = name;
    }
    public static String getName() {
        return application;
    }    
    public static  void setDomain(String dom) {
        domain = dom;
    }
    public static  String getDomain() {
        return domain;
    }     
    public static  void setURL(String url) {
        URL = url;
    }
    public static  String getURL() {
        return URL;
    }
    public static void addRole(String role) {
        getRoles().add(role);
    }
    public static void removeRole(String role) {
        getRoles().remove(role);
    }
    public static void addFeature(String feature) {
        getFeatures().add(feature);    }
    
    public static void removeFeature(String feature) {
        getFeatures().remove(feature);
    }
    public static void addFeaturePrerequisite(String feature) {
        getFeature_prerequisites().add(feature);    }
    
    public static void removeFeaturePrerequisite(String feature) {
        getFeature_prerequisites().remove(feature);
    }
    public static void addStory(Plan story) {
        getUserstories().add(story);    }
    
    public static void removeStory(Plan story) {
        getUserstories().remove(story);
    
    }

    /**
     * @return the roles
     */
    public static List<String> getRoles() {
        return roles;
    }

    /**
     * @param aRoles the roles to set
     */
    public static void setRoles(List<String> aRoles) {
        roles = aRoles;
    }

    /**
     * @return the features
     */
    public static List<String> getFeatures() {
        return features;
    }

    /**
     * @param aFeatures the features to set
     */
    public static void setFeatures(List<String> aFeatures) {
        features = aFeatures;
    }

    /**
     * @return the feature_prerequisites
     */
    public static List<String> getFeature_prerequisites() {
        return feature_prerequisites;
    }

    /**
     * @param aFeature_prerequisites the feature_prerequisites to set
     */
    public static void setFeature_prerequisites(List<String> aFeature_prerequisites) {
        feature_prerequisites = aFeature_prerequisites;
    }

    /**
     * @return the userstories
     */
    public static List<Plan> getUserstories() {
        return userstories;
    }

    /**
     * @param aUserstories the userstories to set
     */
    public static void setUserstories(List<Plan> aUserstories) {
        userstories = aUserstories;
    }

    /**
     * @return the feature_assertion
     */
    public static List<String> getFeature_assertion() {
        return feature_assertion;
    }

    /**
     * @param aFeature_assertion the feature_assertion to set
     */
    public static void setFeature_assertion(List<String> aFeature_assertion) {
        feature_assertion = aFeature_assertion;
    }
    
}
