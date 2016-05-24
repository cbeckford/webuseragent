/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.helpers;

/**
 *
 * @author Clive
 */
public class StringHelper {

    public static String camelToTitle(String camelCase) {
        StringBuilder titleCase = new StringBuilder();
        for (int i = 0; i <= camelCase.length() - 1; i++) {
            String letter = camelCase.substring(i, i + 1);
            titleCase
                    .append(letter.equals(letter.toUpperCase()) ? " " : "")
                    .append(i == 0 ? letter.toUpperCase() : letter);
        }
        return titleCase.toString();
    }

    public static Integer textToOrdinal(String instance, Integer ordMax) {
        Integer ordNum = 0;
        switch (instance.trim().toLowerCase()) {
            case "first":
                ordNum = 1;
                break;
            case "second":
                ordNum = 2;
                break;
            case "third":
                ordNum = 3;
                break;
            case "fourth":
                ordNum = 4;
                break;
            case "fifth":
                ordNum = 5;
                break;
            case "sixth":
                ordNum = 6;
                break;
            case "seventh":
                ordNum = 7;
                break;
            case "eighth":
                ordNum = 8;
                break;
            case "ninth":
                ordNum = 9;
                break;
            case "tenth":
                ordNum = 10;
                break;
            case "last":
                ordNum = ordMax;
                break;
            default:
                ordNum = Integer.valueOf(instance);
                break;
        }
        return ordNum;
    }

}
