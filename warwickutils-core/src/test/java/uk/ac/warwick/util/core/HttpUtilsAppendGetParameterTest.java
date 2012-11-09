package uk.ac.warwick.util.core;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public final class HttpUtilsAppendGetParameterTest {
	
    @Test
    @Ignore("Not sure why this behaviour was ever expected")
    public void noParamsWithTrailingSlash() {
		String origUrlWithoutSlash = "http://whatever/page";
		String paramName = "param";
		String paramValue = "value";

		String convertedUrl = HttpUtils.appendGetParameter(origUrlWithoutSlash + "/", paramName, paramValue);
		assertEquals(origUrlWithoutSlash + "?" + paramName + "=" + paramValue, convertedUrl);
	}

	@Test
	public void noParamsWithOutTrailingSlash() {
		String origUrlWithoutSlash = "http://whatever/page";
		String paramName = "param";
		String paramValue = "value";

		String convertedUrl = HttpUtils.appendGetParameter(origUrlWithoutSlash, paramName, paramValue);
		assertEquals(origUrlWithoutSlash + "?" + paramName + "=" + paramValue, convertedUrl);

	}

	@Test
	public void existingParam() {
		String origUrl = "http://whatever/page?a=b";
		String paramName = "param";
		String paramValue = "value";

		String convertedUrl = HttpUtils.appendGetParameter(origUrl, paramName, paramValue);
		assertEquals(origUrl + "&" + paramName + "=" + paramValue, convertedUrl);
	}
}
