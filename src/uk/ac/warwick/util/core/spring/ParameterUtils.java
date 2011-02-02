package uk.ac.warwick.util.core.spring;

import java.util.Map;

import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;

public abstract class ParameterUtils {

	private static final IntParser INT_PARSER = new IntParser();

	private static final LongParser LONG_PARSER = new LongParser();

	private static final FloatParser FLOAT_PARSER = new FloatParser();

	private static final DoubleParser DOUBLE_PARSER = new DoubleParser();

	private static final BooleanParser BOOLEAN_PARSER = new BooleanParser();

	private static final StringParser STRING_PARSER = new StringParser();


	/**
	 * Get an Integer parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Integer value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Integer getIntParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		if (request.get(name) == null) {
			return null;
		}
		return new Integer(getRequiredIntParameter(request, name));
	}

	/**
	 * Get an int parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static int getIntParameter(Map<String,String[]> request, String name, int defaultVal) {
		if (request.get(name) == null) {
			return defaultVal;
		}
		try {
			return getRequiredIntParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of int parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static int[] getIntParameters(Map<String,String[]> request, String name) {
		try {
			return getRequiredIntParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new int[0];
		}
	}

	/**
	 * Get an int parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static int getRequiredIntParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return INT_PARSER.parseInt(name, getFirst(request,name));
	}
	
	private static String getFirst(Map<String,String[]> request, String name) {
		String[] values = request.get(name);
		if (values != null && values.length > 0) {
			return values[0];
		}
		return null;
	}

	/**
	 * Get an array of int parameters, throwing an exception if not found or one is not a number..
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static int[] getRequiredIntParameters(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return INT_PARSER.parseInts(name, request.get(name));
	}


	/**
	 * Get a Long parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Long value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Long getLongParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		if (request.get(name) == null) {
			return null;
		}
		return new Long(getRequiredLongParameter(request, name));
	}

	/**
	 * Get a long parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static long getLongParameter(Map<String,String[]> request, String name, long defaultVal) {
		if (request.get(name) == null) {
			return defaultVal;
		}
		try {
			return getRequiredLongParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of long parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static long[] getLongParameters(Map<String,String[]> request, String name) {
		try {
			return getRequiredLongParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new long[0];
		}
	}

	/**
	 * Get a long parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static long getRequiredLongParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return LONG_PARSER.parseLong(name, getFirst(request,name));
	}

	/**
	 * Get an array of long parameters, throwing an exception if not found or one is not a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static long[] getRequiredLongParameters(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return LONG_PARSER.parseLongs(name, request.get(name));
	}


	/**
	 * Get a Float parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Float value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Float getFloatParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		if (request.get(name) == null) {
			return null;
		}
		return new Float(getRequiredFloatParameter(request, name));
	}

	/**
	 * Get a float parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static float getFloatParameter(Map<String,String[]> request, String name, float defaultVal) {
		if (request.get(name) == null) {
			return defaultVal;
		}
		try {
			return getRequiredFloatParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of float parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static float[] getFloatParameters(Map<String,String[]> request, String name) {
		try {
			return getRequiredFloatParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new float[0];
		}
	}

	/**
	 * Get a float parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static float getRequiredFloatParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return FLOAT_PARSER.parseFloat(name, getFirst(request,name));
	}

	/**
	 * Get an array of float parameters, throwing an exception if not found or one is not a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static float[] getRequiredFloatParameters(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return FLOAT_PARSER.parseFloats(name, request.get(name));
	}


	/**
	 * Get a Double parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Double value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Double getDoubleParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		if (request.get(name) == null) {
			return null;
		}
		return new Double(getRequiredDoubleParameter(request, name));
	}

	/**
	 * Get a double parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static double getDoubleParameter(Map<String,String[]> request, String name, double defaultVal) {
		if (request.get(name) == null) {
			return defaultVal;
		}
		try {
			return getRequiredDoubleParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of double parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static double[] getDoubleParameters(Map<String,String[]> request, String name) {
		try {
			return getRequiredDoubleParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new double[0];
		}
	}

	/**
	 * Get a double parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static double getRequiredDoubleParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return DOUBLE_PARSER.parseDouble(name, getFirst(request,name));
	}

	/**
	 * Get an array of double parameters, throwing an exception if not found or one is not a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static double[] getRequiredDoubleParameters(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return DOUBLE_PARSER.parseDoubles(name, request.get(name));
	}


	/**
	 * Get a Boolean parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a boolean.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Boolean value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Boolean getBooleanParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		if (request.get(name) == null) {
			return null;
		}
		return (getRequiredBooleanParameter(request, name) ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * Get a boolean parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static boolean getBooleanParameter(Map<String,String[]> request, String name, boolean defaultVal) {
		if (request.get(name) == null) {
			return defaultVal;
		}
		try {
			return getRequiredBooleanParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of boolean parameters, return an empty array if not found.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static boolean[] getBooleanParameters(Map<String,String[]> request, String name) {
		try {
			return getRequiredBooleanParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new boolean[0];
		}
	}

	/**
	 * Get a boolean parameter, throwing an exception if it isn't found
	 * or isn't a boolean.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static boolean getRequiredBooleanParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return BOOLEAN_PARSER.parseBoolean(name, getFirst(request,name));
	}

	/**
	 * Get an array of boolean parameters, throwing an exception if not found
	 * or one isn't a boolean.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static boolean[] getRequiredBooleanParameters(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return BOOLEAN_PARSER.parseBooleans(name, request.get(name));
	}


	/**
	 * Get a String parameter, or <code>null</code> if not present.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the String value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static String getStringParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		if (request.get(name) == null) {
			return null;
		}
		return getRequiredStringParameter(request, name);
	}

	/**
	 * Get a String parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value to default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static String getStringParameter(Map<String,String[]> request, String name, String defaultVal) {
		String val = getFirst(request,name);
		return (val != null ? val : defaultVal);
	}

	/**
	 * Get an array of String parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static String[] getStringParameters(Map<String,String[]> request, String name) {
		try {
			return getRequiredStringParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new String[0];
		}
	}

	/**
	 * Get a String parameter, throwing an exception if it isn't found.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static String getRequiredStringParameter(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return STRING_PARSER.validateRequiredString(name, getFirst(request,name));
	}

	/**
	 * Get an array of String parameters, throwing an exception if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static String[] getRequiredStringParameters(Map<String,String[]> request, String name)
			throws ServletRequestBindingException {

		return STRING_PARSER.validateRequiredStrings(name, request.get(name));
	}


	private abstract static class ParameterParser {

		protected final Object parse(String name, String parameter) throws ServletRequestBindingException {
			validateRequiredParameter(name, parameter);
			try {
				return doParse(parameter);
			}
			catch (NumberFormatException ex) {
				throw new ServletRequestBindingException(
						"Required " + getType() + " parameter '" + name + "' with value of '" +
						parameter + "' is not a valid number", ex);
			}
		}

		protected final void validateRequiredParameter(String name, Object parameter)
				throws ServletRequestBindingException {

			if (parameter == null) {
				throw new MissingServletRequestParameterException(name, getType());
			}
		}

		protected abstract String getType();

		protected abstract Object doParse(String parameter) throws NumberFormatException;
	}


	private static class IntParser extends ParameterParser {

		protected String getType() {
			return "int";
		}

		protected Object doParse(String s) throws NumberFormatException {
			return Integer.valueOf(s);
		}

		public int parseInt(String name, String parameter) throws ServletRequestBindingException {
			return ((Number) parse(name, parameter)).intValue();
		}

		public int[] parseInts(String name, String[] values) throws ServletRequestBindingException {
			validateRequiredParameter(name, values);
			int[] parameters = new int[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseInt(name, values[i]);
			}
			return parameters;
		}
	}


	private static class LongParser extends ParameterParser {

		protected String getType() {
			return "long";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
			return Long.valueOf(parameter);
		}

		public long parseLong(String name, String parameter) throws ServletRequestBindingException {
			return ((Number) parse(name, parameter)).longValue();
		}

		public long[] parseLongs(String name, String[] values) throws ServletRequestBindingException {
			validateRequiredParameter(name, values);
			long[] parameters = new long[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseLong(name, values[i]);
			}
			return parameters;
		}
	}


	private static class FloatParser extends ParameterParser {

		protected String getType() {
			return "float";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
			return Float.valueOf(parameter);
		}

		public float parseFloat(String name, String parameter) throws ServletRequestBindingException {
			return ((Number) parse(name, parameter)).floatValue();
		}

		public float[] parseFloats(String name, String[] values) throws ServletRequestBindingException {
			validateRequiredParameter(name, values);
			float[] parameters = new float[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseFloat(name, values[i]);
			}
			return parameters;
		}
	}


	private static class DoubleParser extends ParameterParser {

	    /**
	     * This value causes the JVM to spin forever if passed to Double.parseDouble().
	     */
	    private static final String BUG_DOUBLE = "2.2250738585072012e-308";
	    
		protected String getType() {
			return "double";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
		    if (BUG_DOUBLE.equals(parameter)) {
		        throw new NumberFormatException("Unparseable number");
		    }
			return Double.valueOf(parameter);
		}

		public double parseDouble(String name, String parameter) throws ServletRequestBindingException {
			return ((Number) parse(name, parameter)).doubleValue();
		}

		public double[] parseDoubles(String name, String[] values) throws ServletRequestBindingException {
			validateRequiredParameter(name, values);
			double[] parameters = new double[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseDouble(name, values[i]);
			}
			return parameters;
		}
	}


	private static class BooleanParser extends ParameterParser {

		protected String getType() {
			return "boolean";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
			return (parameter.equalsIgnoreCase("true") || parameter.equalsIgnoreCase("on") ||
					parameter.equalsIgnoreCase("yes") || parameter.equals("1") ? Boolean.TRUE : Boolean.FALSE);
		}

		public boolean parseBoolean(String name, String parameter) throws ServletRequestBindingException {
			return ((Boolean) parse(name, parameter)).booleanValue();
		}

		public boolean[] parseBooleans(String name, String[] values) throws ServletRequestBindingException {
			validateRequiredParameter(name, values);
			boolean[] parameters = new boolean[values.length];
			for (int i = 0; i < values.length; i++) {
				parameters[i] = parseBoolean(name, values[i]);
			}
			return parameters;
		}
	}


	private static class StringParser extends ParameterParser {

		protected String getType() {
			return "string";
		}

		protected Object doParse(String parameter) throws NumberFormatException {
			return parameter;
		}

		public String validateRequiredString(String name, String value) throws ServletRequestBindingException {
			validateRequiredParameter(name, value);
			return value;
		}

		public String[] validateRequiredStrings(String name, String[] values) throws ServletRequestBindingException {
			validateRequiredParameter(name, values);
			for (int i = 0; i < values.length; i++) {
				validateRequiredParameter(name, values[i]);
			}
			return values;
		}
	}

}
