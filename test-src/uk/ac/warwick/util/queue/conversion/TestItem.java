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
@JsonPropertyOrder({"name","age"})
public class TestItem {
    private String name;
    private int age;
    
    private transient TestServiceBean testServiceBean;
    
    private List<TestItem> children = new ArrayList<TestItem>();
    
    public TestItem() {
    }
    
    public TestItem(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
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
