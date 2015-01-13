package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.tophe.parser.XferTransform;

/**
 * Handle the body of the {@link HttpResponse} received from a {@link HttpEngine}.
 *
 * @param <OUTPUT> Type of the Object that the HTTP call will return
 * @param <SE>     Type of the Exception that will be raised when the server reports an error
 * @author Created by robUx4 on 20/08/2014.
 * @see co.tophe.BaseResponseHandler
 */
public class ResponseHandler<OUTPUT, SE extends ServerException> {

	public final XferTransform<HttpResponse, OUTPUT> contentParser;
	public final XferTransform<HttpResponse, SE> errorParser;

	/**
	 * Constructor.
	 *
	 * @param contentParser the transformation used to get the parsed {@link OUTPUT} object.
	 * @param errorParser the transformation used to turn the server generated error into a {@link SE} exception.
	 */
	public ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser, XferTransform<HttpResponse, SE> errorParser) {
		this.contentParser = contentParser;
		this.errorParser = errorParser;
	}

	/**
	 * Called when an HTTP response is received. Does nothing by default.
	 *
	 * @param request  the HTTP request whose response is received.
	 * @param response the HTTP response received.
	 */
	public void onHttpResponse(@NonNull HttpRequest request, @NonNull HttpResponse response) {
	}

	/**
	 * Indicates if the response should follow redirections.
	 *
	 * @return {@code null} if you want to use the default value of the {@link co.tophe.HttpEngine}.
	 */
	@Nullable
	public Boolean followsRedirect() {
		return null;
	}
}
