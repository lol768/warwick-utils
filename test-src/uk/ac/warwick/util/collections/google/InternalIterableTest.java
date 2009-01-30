package uk.ac.warwick.util.collections.google;

import static com.google.common.base.Predicates.isEqualTo;
import static com.google.common.base.Predicates.isNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Comparators;
import com.google.common.collect.Lists;
import com.google.common.collect.PrimitiveArrays;

public class InternalIterableTest extends TestCase {

    private InternalIterable<String> words = InternalIterable.of(newArrayList("ham", "home", "from", "fish", "have", "ham"));
    private static Comparator<String> alpha = Comparators.naturalOrder();


    /*
     * Here are some examples...
     */
    public static void main(String[] args){
        /*
         * Set up a list
         */
        InternalIterable<String> namesAndNulls = InternalIterable.of(newArrayList("chris", "nick", null, "kieran", null, null,
                "hongfeng", "mat", "sarah"));

        /*
         * Filter out the nulls
         */
        for (String name: namesAndNulls.filter(not(isNull()))) {
            System.out.println(name + " has " + name.length() + " letters");
        }
        
        /*
         * Filter out the nulls and reverse the letters in the remainder
         */
        final Function<String, String> reverser = new Function<String, String>(){
            public String apply(String arg0) {
                return new StringBuffer(arg0).reverse().toString();
            }
        };
        
        for (String name: namesAndNulls.filter(not(isNull())).transform(reverser)) {
            System.out.println(name );
        }
        
        /*
         * find the first non-null string from a selection 
         */
        String nonNull = InternalIterable.of(Lists.newArrayList(null, null, "I'm not null!","nor me")).find(not(isNull()));
        System.out.println("First non-null element is "  + nonNull);
        
        
        /* 
         * Filter out the nulls, sort by name and then make an insertion-order sorted Map of the original name and the reversed name
         */
        InternalIterable.Injector<Map<String,String>,String> mapper = new InternalIterable.Injector< Map<String,String>,String>(){
            public  Map<String, String> apply( Map<String, String> memo, String element){
                memo.put(element, reverser.apply(element));
                return memo;
            }
        };
        
        Iterable<Map.Entry<String, String>> entries = namesAndNulls.filter(not(isNull())).sort(alpha).inject(new LinkedHashMap<String, String>(), mapper).entrySet();
        
        for (Map.Entry<String, String> entry: entries) {
            System.out.println(entry.getKey() + " backwards is " + entry.getValue());
        }
        
        /*
         * For comparison, here's the external-iterator way to do the same
         */
        Map<String, String> results = new LinkedHashMap<String, String>();
        List<String> sortedList = new ArrayList<String>();
        for (String name: namesAndNulls) {
           if (name != null) {
               sortedList.add(name);
           }
        }
        Collections.sort(sortedList, alpha);
        for (String name: sortedList) {
            results.put(name, reverser.apply(name));
        }

        for (Map.Entry<String, String> entry: entries) {
            System.out.println(entry.getKey() + " backwards is " + entry.getValue());
        }

        
    }

    public void testFilter() { 

        Predicate<String> startsWithH = new Predicate<String>() {
            public boolean apply(String s) {
                return s.startsWith("h");
            }
        }; 

        Iterable<String> hWords = words.filter(startsWithH);

        /*
         * Verify that we can find a word that doesn't begin with 'h' in the
         * first list...
         */
        assertEquals("fish", find(words, isEqualTo("fish")));

        /*
         * ...and that we can't find a word that doesn't begin with 'h' in the
         * filtered list
         */

        try {
            find(hWords, isEqualTo("fish"));
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /*
     * Transmogrify the list of words into a list of booleans, value depending
     * on whether or not the word begins with "h"
     */
    public void testTransform() {
        Function<String, Boolean> startsWithH = new Function<String, Boolean>() {
            public Boolean apply(String s) {
                return s.startsWith("h");
            }
        };

        Iterable<Boolean> results = words.transform(startsWithH);

        Iterator<Boolean> expectedResults = PrimitiveArrays.asList(new boolean[] { true, true, false, false, true, true })
                .iterator();
        for (Boolean result: results) {
            Boolean e = expectedResults.next();
            assertEquals(e, result);
        }

    }

    public void testAnyAndAll() {
        CallCountingPredicate nonZeroLength = new CallCountingPredicate() {
            public boolean apply(String arg0) {
                incCount();
                return arg0.length() > 0;
            }
        };
        CallCountingPredicate beginsWithH = new CallCountingPredicate() {
            public boolean apply(String arg0) {
                incCount();
                return arg0.startsWith("h");
            }
        };

        assertTrue(words.all(nonZeroLength));
        assertEquals(6, nonZeroLength.getCount());

        assertFalse(words.all(beginsWithH));
        assertEquals(3, beginsWithH.getCount());

        assertTrue(words.any(beginsWithH));
        assertEquals(4, beginsWithH.getCount());

    }

    public void testSort() {
        Iterable<String> sorted = words.sort(alpha);
        assertEquals("fish", sorted.iterator().next());
        Comparator<String> reverse = new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }

        };
        sorted = words.sort(reverse);
        assertEquals("home", sorted.iterator().next());

        /*
         * Check that we haven't modified the source
         */
        assertEquals("ham", words.iterator().next());
    }

    public void testFind() {        
        Predicate<String> startsWithH = new Predicate<String>() {
            public boolean apply(String s) {
                return s.startsWith("h");
            }
        }; 
        assertEquals("ham", words.find(startsWithH));
        try {
            words.find(Predicates.alwaysFalse());
            fail();
        }catch (NoSuchElementException e) {
            assertTrue("expected", true);
        }
    }
    public void testInject() {
        /*
         * Count the frequencies of words in the "words" Iterable.
         */

        InternalIterable.Injector<Map<String, Integer>,String> wordCounter = new InternalIterable.Injector<Map<String, Integer>,String>() {

            /*
             * Called once for each word in the list. If we've already seen this
             * word, increment it's counter in the map; if not, create it and
             * set it to 1
             */
            public Map<String, Integer> apply(Map<String, Integer> memo, String element) {
                if (memo.containsKey(element)) {
                    memo.put(element, memo.get(element) + 1);
                } else {
                    memo.put(element, 1);
                }
                return memo;
            }

        };

        Map<String, Integer> result = words.inject(new HashMap<String, Integer>(), wordCounter);

        assertEquals(5, result.size());
        assertEquals(Integer.valueOf(2), result.get("ham"));
        assertEquals(Integer.valueOf(1), result.get("home"));

    }

    /*
     * A predicate subclass that keeps a count of how many times it's been
     * called
     */
    abstract class CallCountingPredicate implements Predicate<String> {

        private int count = 0;

        protected void incCount() {
            count++;
        }

        int getCount() {
            return count;
        }
    }
}
