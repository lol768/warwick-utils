package uk.ac.warwick.util.concurrency.spring;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Manages all active threads, with the opportunity to stop one suddenly
 * if necessary.
 */
@ManagedResource(description="Manage currently running threads")
public class ThreadManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadManager.class);
    
    Thread thread;
    
    private Set<String> highlightPhrases = new HashSet<String>();
    
    public ThreadManager() {
        
    }
    
    /*
     * Implemented this because I thought JMX Console might expose it as a set of radio buttons -
     * but it doesn't.
     */
    public static enum ThreadSorting {
        none,
        id,
        name
    }
    
    @ManagedOperation(description="Prints an HTML summary of all current threads, sorted by ID")
    public String listAllThreadsById() {
        return listAllThreads(ThreadSorting.id);
    }
    
    @ManagedOperation(description="Prints an HTML summary of all current threads, sorted by Name")
    public String listAllThreadsByName() {
        return listAllThreads(ThreadSorting.name);
    }
    
//    @ManagedOperation(description="Prints an HTML summary of all current threads")
//    @ManagedOperationParameters(value={
//            @ManagedOperationParameter(name="sorting", description="How to sort the threads")
//    })
    public String listAllThreads(ThreadSorting sorting) {
        StringBuilder b = new StringBuilder();
        b.append("<style>table td { border: 1px solid #ccc }</style>\n");
        b.append("<table>")
            .append("<tr><th>ID</th><th>Name</th><th>Stack</th></tr>");
        for (Map.Entry<Thread,StackTraceElement[]> entry : getAllThreads(sorting)) {
            Thread thread = entry.getKey();
            b.append("\n\n<tr>")
                .append("<td class=thread-id>").append(thread.getId()).append("</td>")
                .append("<td class=thread-name>").append(thread.getName()).append("</td>")
                .append("<td class=thread-stack><pre>");
                new StackTracePrinter(entry.getValue()).print(b);
            b.append("</pre></td>")
            .append("</tr>");
        }
        b.append("</table>");
        return b.toString();
    }

    public Set<Entry<Thread, StackTraceElement[]>> getAllThreads(ThreadSorting sorting) {
        return sort(Thread.getAllStackTraces().entrySet(), sorting);
    }

    private Set<Map.Entry<Thread,StackTraceElement[]>> sort(Set<Entry<Thread, StackTraceElement[]>> entrySet, ThreadSorting sorting) {
        Set<Map.Entry<Thread,StackTraceElement[]>> set;
        switch (sorting) {
            case id:
                set = new TreeSet<Entry<Thread,StackTraceElement[]>>(new Comparator<Entry<Thread,StackTraceElement[]>>() {
                    public int compare(Entry<Thread, StackTraceElement[]> o1, Entry<Thread, StackTraceElement[]> o2) {
                        return new Long(o1.getKey().getId()).compareTo(o2.getKey().getId());
                    }
                });
                set.addAll(entrySet);
                break;
            case name:
                set = new TreeSet<Entry<Thread,StackTraceElement[]>>(new Comparator<Entry<Thread,StackTraceElement[]>>() {
                    public int compare(Entry<Thread, StackTraceElement[]> o1, Entry<Thread, StackTraceElement[]> o2) {
                        return o1.getKey().getName().compareTo(o2.getKey().getName());
                    }
                });
                set.addAll(entrySet);
                break;
            default:
                set = entrySet;
                break;
        }
        return set;
    }

    @ManagedOperation(description="Kills a thread with the given ID and name.")
    @ManagedOperationParameters(value={
            @ManagedOperationParameter(name="threadId", description="Thread ID"),
            @ManagedOperationParameter(name="name", description="Thread Name")
    })
    public String kill(long threadId, String name) {
        return doKill(threadId, name, true);
    }

    @SuppressWarnings("deprecation")
    private String doKill(long threadId, String name, boolean manuallyTriggered) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
        for(Thread thread : all.keySet()) {
            if (thread.getId() == threadId) {
                if (name.equals(thread.getName())) {
                    // yes, stop() is very deprecated and bad to use in normal code,
                    // where you should have a running flag and interrupt the thread instead.
                    // but that is no use where a thread is running wild and free, and
                    // needs a swift bop on the head.
                    LOGGER.info("KilledThread manual="+manuallyTriggered+" id="+threadId+" name=\""+name+"\"");
                    thread.stop();
                    return "thread.stop() called";
                }
            }
        }
        throw new IllegalArgumentException("Found no thread matching both ID and name");
    }
    
    class StackTracePrinter {
        private StackTraceElement[] stack;
        public StackTracePrinter(StackTraceElement[] stack) {
            this.stack = stack;
        }
        public void print(StringBuilder b) {
            for (StackTraceElement e : stack) {
                String className = e.getClassName();
                boolean bold = shouldHighlight(className);
                if (bold) {
                    b.append("<b>");
                }
                b.append(className
                        +"."
                        +e.getMethodName()
                        +"("
                        +e.getFileName()
                        +":"
                        +(e.isNativeMethod()? "native" : e.getLineNumber())
                        +")");
                if (bold) {
                    b.append("</b>");
                }
                b.append('\n');
            }
        }
        
        public String print() {
            StringBuilder b = new StringBuilder();
            print(b);
            return b.toString();
        }
    }
    
    private boolean shouldHighlight(String className) {
        if (!highlightPhrases.isEmpty()) {
            for(String phrase : highlightPhrases) {
                if (className.contains(phrase)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setHighlightPhrases(Set<String> highlightPhrases) {
        this.highlightPhrases = highlightPhrases;
    }
    
}
