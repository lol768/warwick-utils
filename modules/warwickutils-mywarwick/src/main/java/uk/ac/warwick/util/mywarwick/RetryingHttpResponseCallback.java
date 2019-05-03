package uk.ac.warwick.util.mywarwick;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;

import javax.validation.constraints.NotNull;

public class RetryingHttpResponseCallback implements FutureCallback<HttpResponse> {

	private final HttpClient httpclient;
	private final FutureCallback<HttpResponse> callback;
	private final Logger logger;
	private final int maxAttempts;

	private HttpPost request;
	private int numberOfAttempts = 1;

	RetryingHttpResponseCallback(
			@NotNull HttpClient httpclient,
			@NotNull HttpPost request,
			@NotNull FutureCallback<HttpResponse> callback,
			@NotNull Logger logger,
			int maxAttempts
	) {
		super();
		this.httpclient = httpclient;
		this.request = request;
		this.callback = callback;
		this.maxAttempts = maxAttempts;
		this.logger = logger;
	}

	@Override
	public void completed(HttpResponse httpResponse) {
		callback.completed(httpResponse);
	}

	@Override
	public void failed(Exception e) {
		try {
			if (this.numberOfAttempts < this.maxAttempts) {
				logger.warn("Request to mywarwick API has failed:" +
						"\nURI: " + this.request.getURI() +
						"\nattempt: " + this.numberOfAttempts +
						"\nmax attempts: " + this.maxAttempts +
						"\nerror message:" + e.getMessage(), e);

				RequestConfig requestConfig = this.httpclient.getRequestConfig();
				this.request.setConfig(
						RequestConfig.copy(requestConfig)
								.setConnectTimeout(requestConfig.getConnectTimeout() * Double.valueOf(Math.pow(2, this.numberOfAttempts)).intValue())
								.setSocketTimeout(requestConfig.getSocketTimeout() * Double.valueOf(Math.pow(2, this.numberOfAttempts)).intValue())
								.build()
				);
				this.numberOfAttempts = this.numberOfAttempts + 1;
				httpclient.execute(this.request, this);
			} else {
				callback.failed(e);
			}
		} catch (Exception innerException) {
			callback.failed(e);
		}
	}

	@Override
	public void cancelled() {
		callback.cancelled();
	}
}
