package uk.ac.warwick.util.mywarwick.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.ac.warwick.util.mywarwick.model.request.Activity;
import uk.ac.warwick.util.mywarwick.model.request.Recipients;
import uk.ac.warwick.util.mywarwick.model.request.Tag;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ActivityTest {

    ObjectMapper objectMapper = new ObjectMapper();

    private final String expectedRequesJsonString = "{\"type\":\"faketype\",\"title\":\"faketitle\",\"text\":\"faketext\",\"url\":\"fakeurl\",\"tags\":[{\"name\":\"fakename\",\"value\":\"fakevalue\",\"display_value\":\"fakedv\"}],\"recipients\":{\"users\":[\"u1234567\"],\"groups\":[\"ch-students\"]}}";

    @Test
    public void sohuldMakecreateCorrectJsonAgainAPIspec() throws JsonProcessingException {
        Set<String> userIds = new HashSet<>();
        Set<String> groups = new HashSet<>();

        userIds.add("u1234567");
        groups.add("ch-students");

        Recipients recipients = new Recipients(userIds, groups);
        Activity activity = new Activity(userIds,groups,"faketitle","fakeurl","faketext","faketype");

        Tag tag = new Tag();
        tag.setDisplay_value("fakedv");
        tag.setValue("fakevalue");
        tag.setName("fakename");
        activity.setTags(tag);
        assertEquals(expectedRequesJsonString, objectMapper.writeValueAsString(activity));

    }
}