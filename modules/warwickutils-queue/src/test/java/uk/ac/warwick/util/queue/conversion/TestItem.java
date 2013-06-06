package uk.ac.warwick.util.queue.conversion;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.beans.factory.annotation.Autowired;

@JsonAutoDetect @ItemType("my-message-type")
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
    
    @JsonSerialize(include=Inclusion.NON_DEFAULT)
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
