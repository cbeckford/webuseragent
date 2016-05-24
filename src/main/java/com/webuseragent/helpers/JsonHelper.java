/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.helpers;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.JSONParser;

/**
 *
 * @author Clive
 */
public class JsonHelper {

    public static final JSONParser JSON_PARSER = new JSONParser();

    public JSONObject stringToJson(String jsonString) {
        try {
            Object obj = JSON_PARSER.parse(jsonString);
            JSONObject jObject = (JSONObject) obj;
            return jObject;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject jsonObjectToJson(JSONObject jObject, String item) {
        try {
            return (JSONObject) jObject.get(item);
        } catch (Exception e) {
            return null;
        }
    }



}
