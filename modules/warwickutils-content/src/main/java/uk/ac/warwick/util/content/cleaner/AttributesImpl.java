package uk.ac.warwick.util.content.cleaner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * An implementation of Attributes which adds some actually useful methods,
 * like the ability to add and remove things.
 * 
 * Doesn't implement qualified names, but if we need this it can be implemented
 */
public class AttributesImpl implements Attributes {

	private List<Attribute> attributes = new ArrayList<Attribute>();
	
	public AttributesImpl(final Attributes attrs) {
		int l = attrs.getLength();
		for (int i=0; i<l; i++) {
			String name = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			add(name,value);
		}
	}

	// generate an AttributesImpl object from a map - the Attributes map is provided to guarantee consistent ordering
	public AttributesImpl(final Attributes attrs, Map<String, String> map){
		int l = attrs.getLength();
		for (int i=0; i<l; i++) {
			String name = attrs.getLocalName(i);
			String value = map.get(name);
			add(name,value);
		}
	}
	
	public void clear() {
		attributes.clear();
	}
	
	public boolean remove(String name) {
		Iterator<Attribute> it = attributes.iterator();
		while (it.hasNext()) {
			Attribute val = it.next();
			if (val.getLocalName().equals(name)) {
				it.remove();
				return true;
			}
		}
		return false;
	}
	
	public void add(String name, String value) {
		attributes.add(new Attribute(name, value));
	}
	
	public int getIndex(String qName) {
		throw new UnsupportedOperationException("Don't support qnames");
	}

	public int getIndex(String uri, String localName) {
		throw new UnsupportedOperationException("Don't support qnames");
	}

	public int getLength() {
		return attributes.size();
	}

	public String getLocalName(int index) {
		return attributes.get(index).getLocalName();
	}

	public String getQName(int index) {
		throw new UnsupportedOperationException("Don't support qnames");
	}

	public String getType(int index) {
		return "CDATA";
	}

	public String getType(String qName) {
		return null;
	}

	public String getType(String uri, String localName) {
		return null;
	}

	public String getURI(int index) {
		return "";
	}

	public String getValue(int index) {
		return attributes.get(index).getValue();
	}

	public String getValue(String qName) {
		for (Attribute a : attributes) {
			if (qName.equals(a.getLocalName())) {
				return a.getValue();
			}
		}
		return null;
	}

	public String getValue(String uri, String localName) {
		return null;
	}
	
	public static class Attribute {
		private final String localName;
		private final String value;
		public Attribute(String name, String val) {
			localName = name;
			value = val;
		}
		public final String getLocalName() {
			return localName;
		}
		public final String getValue() {
			return value;
		}
	}

}
