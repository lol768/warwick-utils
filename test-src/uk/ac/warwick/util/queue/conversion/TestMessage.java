package uk.ac.warwick.util.queue.conversion;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;

@JsonAutoDetect @ItemType("my-message-type")
public class TestMessage {
    private String name;
    private int age;
    
    private transient TestServiceBean testServiceBean;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    
    @JsonIgnore
    public TestServiceBean getTestServiceBean() {
        return testServiceBean;
    }
    
    @Autowired
    public void setTestServiceBean(TestServiceBean testServiceBean) {
        this.testServiceBean = testServiceBean;
    }
}
