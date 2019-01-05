package uk.ac.warwick.util.queue.conversion;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.beans.factory.annotation.Autowired;

@JsonAutoDetect
@ItemType("my-message-type")
@JsonPropertyOrder({"name","currentAge"})
public class TestItem {
    private String name;
    private int currentAge;
    
    private transient TestServiceBean testServiceBean;
    
    private List<TestItem> children = new ArrayList<TestItem>();
    
    public TestItem() {
    }
    
    public TestItem(String name, int currentAge) {
        this.name = name;
        this.currentAge = currentAge;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getCurrentAge() {
        return currentAge;
    }
    public void setCurrentAge(int currentAge) {
        this.currentAge = currentAge;
    }
    
    @JsonIgnore
    public TestServiceBean getTestServiceBean() {
        return testServiceBean;
    }
    
    @Autowired
    public void setTestServiceBean(TestServiceBean testServiceBean) {
        this.testServiceBean = testServiceBean;
    }
    
    @JsonSerialize
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public List<TestItem> getChildren() {
        return children;
    }
    
    public void setChildren(List<TestItem> children) {
        this.children = children;
    }
    
    public TestItem addChild(TestItem child) {
        children.add(child);
        return this;
    }
}
