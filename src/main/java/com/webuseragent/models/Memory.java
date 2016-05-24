/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.models;

import com.webuseragent.main.App;
import com.google.common.base.CaseFormat;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Clive
 */
public class Memory {

    private final LinkedHashMap<String, Object> my = new LinkedHashMap();
    private final LinkedHashMap<String, LinkedHashMap<String, Object>> pages = new LinkedHashMap();

    public Object recall(String key) {
        Object memoryValue;
        try {
            memoryValue = my.get(asMemoryKey(key));
            if (memoryValue == null) {
                memoryValue = "??";
            }
        } catch (Exception ex) {
            memoryValue = key;
        }
        return memoryValue;
    }

    public ArrayList recallAsList(String handle) {
        return (ArrayList) my.get(handle);
    }

    public void save(String handle, Object thing) {
        my.put(handle, thing);
    }

    public void saveLastPage(String handle, LinkedHashMap<String, Object> thing) {
        pages.put(handle, thing);
    }

    public void recallLastPage(String handle) {
        pages.get(handle);
    }

    public LinkedHashMap recallAll() {
        return my;
    }

    public String asMemoryKey(String regularCase) {
        String memKey = regularCase.trim();
        
        if (memKey.contains(" ")
                || memKey.contains("_")
                || memKey.startsWith(memKey.substring(0, 1).toUpperCase())) {
            
            if (memKey.startsWith(memKey.substring(0, 1).toUpperCase())) {
                memKey = memKey.replaceFirst(memKey.substring(0, 1).toUpperCase(), memKey.substring(0, 1).toLowerCase());
            }
            memKey = CaseFormat.LOWER_UNDERSCORE.to(LOWER_CAMEL, memKey.trim().toLowerCase().replaceAll(" ", "_"));
            App.say("DEBUG", "MEMKEY........: " + camelToTitle(memKey));
        }
        return memKey;
    }

    public String camelToTitle(String camelCase) {
        StringBuilder titleCase = new StringBuilder();
        for (int i = 0; i <= camelCase.length() - 1; i++) {
            String letter = camelCase.substring(i, i + 1);
            if (i == 0) {
                letter = letter.toUpperCase();
            } else {
                letter = (letter.equals(letter.toUpperCase()) ? " " : "") + letter;
            }
            titleCase.append(letter);
        }
        return titleCase.toString();
    }

    public String substituteTags(String preEval) {
        if (!preEval.contains("%{")) {
            return preEval;
        }
        
        String postEval = preEval;
        Matcher m = Pattern.compile("[%][{][a-zA-Z0-9_ ]*[}][%]")
                .matcher(preEval);
        while (m.find()) {
            String tag = m.group();
            String tagKey = tag.substring(2, tag.length() - 2);
            String tagValue = recall(tagKey).toString();
            postEval = postEval.replace(
                    tag,
                    tagValue);
        }
        return postEval;
    }
}
