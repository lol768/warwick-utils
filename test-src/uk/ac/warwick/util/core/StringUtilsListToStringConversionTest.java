package uk.ac.warwick.util.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public final class StringUtilsListToStringConversionTest extends TestCase {
    @SuppressWarnings("unchecked")
    public void testEmptyKeywordToString() {
        String seperator = " ";
        
        String result = StringUtils.convertToString(Collections.EMPTY_LIST, seperator);
        assertEquals("", result);
    }
    
    public void testConvertSingleKeywordToString() {
        String keyword = " the_keyword ";
        String seperator = " ";
        
        String result = StringUtils.convertToString(Collections.singletonList(keyword), seperator);
        assertEquals("trimmed single result", keyword.trim(), result);
    }
    
    public void testConvertMultipleKeywordsToString() {
        String firstKeyword = "firstKeyword";
        String secondKeyword = "secondKeyword";        
        String seperator = ", ";
        
        List<String> keywords = Arrays.asList(new String[] {firstKeyword, secondKeyword});
        
        String result = StringUtils.convertToString(keywords, seperator);
        assertEquals("multiple trimmed result", firstKeyword.trim() + seperator + secondKeyword, result);
    }
    
    public void testConvertEmptyKeywordToList() {
        List<String> results = StringUtils.convertCommaOrSpaceDelimitedStringToList(null);
        assertEquals("number of results", 0, results.size());
    }

    public void testConvertSingleKeywordToList() {
        String keyword = " the_keyword ";
        
        List<String> results = StringUtils.convertCommaOrSpaceDelimitedStringToList(keyword);
        assertEquals("number of results", 1, results.size());
        assertEquals("result", keyword.trim(), results.get(0));
    }
    
    public void testConvertMultipleSpaceSeperatedKeywordToList() {
        String firstKeyword = " the_keyword ";
        String secondKeyword = " the_second_keyword";

        String keywords = firstKeyword + " " + secondKeyword;
        
        List<String> results = StringUtils.convertCommaOrSpaceDelimitedStringToList(keywords);
        assertEquals("number of results", 2, results.size());
        assertEquals("first result", firstKeyword.trim(), results.get(0));
        assertEquals("second result", secondKeyword.trim(), results.get(1));        
    }
    
    public void testConvertMultipleSpaceSeperatedKeywordToListButIgnoreMultipleSpaces() {
        String firstKeyword = " the_keyword ";
        String secondKeyword = " the_second_keyword";

        String keywords = firstKeyword + "      " + secondKeyword;
        
        List<String> results = StringUtils.convertCommaOrSpaceDelimitedStringToList(keywords);
        assertEquals("number of results", 2, results.size());
        assertEquals("first result", firstKeyword.trim(), results.get(0));
        assertEquals("second result", secondKeyword.trim(), results.get(1));        
    }

    
    public void testConvertMultipleCommaSeperatedKeywordToList() {
        String firstKeyword = " the_keyword ";
        String secondKeyword = " the_second_keyword";

        String keywords = firstKeyword + "," + secondKeyword;
        
        List<String> results = StringUtils.convertCommaOrSpaceDelimitedStringToList(keywords);
        assertEquals("number of results", 2, results.size());
        assertEquals("first result", firstKeyword.trim(), results.get(0));
        assertEquals("second result", secondKeyword.trim(), results.get(1));        
    }
    
    public void testConvertMultipleCommaSeperatedKeywordToListButIgnoreMultipleCommas() {
        String firstKeyword = " the_keyword ";
        String secondKeyword = " the_second_keyword";

        String keywords = firstKeyword + ",,,,,," + secondKeyword;
        
        List<String> results = StringUtils.convertCommaOrSpaceDelimitedStringToList(keywords);
        assertEquals("number of results", 2, results.size());
        assertEquals("first result", firstKeyword.trim(), results.get(0));
        assertEquals("second result", secondKeyword.trim(), results.get(1));        
    }
}
