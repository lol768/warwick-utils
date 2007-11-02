package uk.ac.warwick.util.core;

import junit.framework.TestCase;

public final class HttpUtilsAppendGetParameterTest extends TestCase {
	public void testNoParamsWithTrailingSlash() {
		String origUrlWithoutSlash = "http://whatever/page";
		String paramName = "param";
		String paramValue = "value";

		String convertedUrl = HttpUtils.appendGetParameter(origUrlWithoutSlash + "/", paramName, paramValue);
		assertEquals(origUrlWithoutSlash + "?" + paramName + "=" + paramValue, convertedUrl);
	}

	public void testNoParamsWithOutTrailingSlash() {
		String origUrlWithoutSlash = "http://whatever/page";
		String paramName = "param";
		String paramValue = "value";

		String convertedUrl = HttpUtils.appendGetParameter(origUrlWithoutSlash, paramName, paramValue);
		assertEquals(origUrlWithoutSlash + "?" + paramName + "=" + paramValue, convertedUrl);

	}

	public void testExistingParam() {
		String origUrl = "http://whatever/page?a=b";
		String paramName = "param";
		String paramValue = "value";

		String convertedUrl = HttpUtils.appendGetParameter(origUrl, paramName, paramValue);
		assertEquals(origUrl + "&" + paramName + "=" + paramValue, convertedUrl);
	}
}
