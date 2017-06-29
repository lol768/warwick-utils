package uk.ac.warwick.util.mywarwick.model.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ActivityTest {

    ObjectMapper objectMapper = new ObjectMapper();

    private final String expectedRequestJsonString = "{\"type\":\"faketype\",\"title\":\"faketitle\",\"text\":\"faketext\",\"url\":\"fakeurl\",\"tags\":[{\"name\":\"fakename\",\"value\":\"fakevalue\",\"display_value\":\"fakedv\"}],\"recipients\":{\"users\":[\"u1234567\"],\"groups\":[\"ch-students\"]},\"send_email\":null}";

    @Test
    public void shouldMakeCorrectJsonAgainAPISpec() throws JsonProcessingException {
        Set<String> userIds = new HashSet<>();
        Set<String> groups = new HashSet<>();

        userIds.add("u1234567");
        groups.add("ch-students");

        Activity activity = new Activity(userIds,groups,"faketitle","fakeurl","faketext","faketype");

        Tag tag = new Tag();
        tag.setDisplay_value("fakedv");
        tag.setValue("fakevalue");
        tag.setName("fakename");
        activity.setTags(tag);
        assertEquals(expectedRequestJsonString, objectMapper.writeValueAsString(activity));

    }
}