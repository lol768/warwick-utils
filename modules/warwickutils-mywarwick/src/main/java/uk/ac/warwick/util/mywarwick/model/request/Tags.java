package uk.ac.warwick.util.mywarwick.model.request;

import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

public class Tags implements ValidTags{
    private Set<Tag> tags;

    public Tags(@NotNull Set<Tag> tags) {
        this.tags = tags;
    }

    public Tags() {
        this.tags = new HashSet<>();
    }

    @Override
    public Set<Tag> getTags() {
        return this.tags;
    }

    public void add(@NotNull Tag tag) {
        this.tags.add(tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Tags tags1 = (Tags) o;

        return new EqualsBuilder()
                .append(getTags(), tags1.getTags())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getTags())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("tags", tags)
                .toString();
    }
}
