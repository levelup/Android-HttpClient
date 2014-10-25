package co.tophe;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import co.tophe.body.HttpBodyJSON;
import co.tophe.body.HttpBodyMultiPart;
import co.tophe.body.HttpBodyParameters;
import co.tophe.body.HttpBodyUrlEncoded;
import co.tophe.internal.HttpEngineUrlConnection;
import co.tophe.parser.BodyToHttpStream;
import co.tophe.parser.BodyToJSONObject;
import co.tophe.parser.BodyToString;
import okio.BufferedSource;
import okio.Okio;

public class TopheClientTest extends AndroidTestCase {

	private static final Class<?> ENGINE_CLASS = HttpEngineUrlConnection.class;
	private static final Class<?> STREAM_ENGINE_CLASS = HttpEngineUrlConnection.class;

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		TopheClient.setup(context);
	}

	public void testNoSSLv3() throws Exception{
		BaseHttpRequest<String, ServerException> request = new BaseHttpRequest.Builder<String, ServerException>()
				//.setUrl("https://userstream.twitter.com/1.1/user.json")
				.setUrl("https://github.com/robUx4")
				.setResponseHandler(BodyToString.RESPONSE_HANDLER)
				.build();
		try {
			TopheClient.parseRequest(request);
		} catch (ServerException e) {
			assertEquals(401, e.getStatusCode());
		}
	}

	@MediumTest
	public void testUploadInputStream() throws Exception {
		final String fileFieldName = "media";
		final String uploadData = "Uploaded Streamé Data";
		HttpBodyMultiPart body = new HttpBodyMultiPart(1);
		body.addStream("media", new ByteArrayInputStream(uploadData.getBytes()), uploadData.getBytes().length, "text/plain");

		BaseHttpRequest<JSONObject, ServerException> request = new BaseHttpRequest.Builder<JSONObject, ServerException>().
				setUrl("http://httpbin.org/post?test=stream").
				setBody(body).
				setResponseHandler(BodyToJSONObject.RESPONSE_HANDLER).
				build();
		// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());

		JSONObject result = TopheClient.parseRequest(request);
		assertNotNull(result);
		assertFalse(result.isNull("files"));
		JSONObject files = result.optJSONObject("files");
		assertFalse(files.isNull(fileFieldName));
		assertEquals(uploadData, files.optString(fileFieldName));
	}

	@MediumTest
	public void testUploadFile() throws Exception {
		final String fileFieldName = "media";
		final String uploadData = "Uploaded File Data";
		File tempFile = new File(getContext().getCacheDir(), "upload.txt");
		FileOutputStream outStream = new FileOutputStream(tempFile);
		outStream.write(uploadData.getBytes());
		outStream.close();

		try {
			HttpBodyMultiPart body = new HttpBodyMultiPart(1);
			body.addFile(fileFieldName, tempFile, "text/plain");

			BaseHttpRequest<JSONObject, ServerException> request = new BaseHttpRequest.Builder<JSONObject, ServerException>().
					setUrl("http://httpbin.org/post?test=file").
					setBody(body).
					setResponseHandler(BodyToJSONObject.RESPONSE_HANDLER).
					build();
		// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());

			JSONObject result = TopheClient.parseRequest(request);
			assertNotNull(result);
			assertFalse(result.isNull("files"));
			JSONObject files = result.optJSONObject("files");
			assertFalse(files.isNull(fileFieldName));
			assertEquals(uploadData, files.optString(fileFieldName));
		} finally {
			tempFile.delete();
		}
	}

	public void testUploadMultipartText() throws Exception {
		final String fieldName1 = "field1";
		final String uploadData1 = "First Field Data";
		final String fieldName2 = "field2";
		final String uploadData2 = "Second Field Data";

		HttpBodyMultiPart body = new HttpBodyMultiPart(2);
		body.add(fieldName1, uploadData1);
		body.add(fieldName2, uploadData2);

		BaseHttpRequest<JSONObject, ServerException> request = new BaseHttpRequest.Builder<JSONObject, ServerException>().
				setUrl("http://httpbin.org/post?test=multitext").
				setBody(body).
				setResponseHandler(BodyToJSONObject.RESPONSE_HANDLER).
				build();
		// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());

		JSONObject result = TopheClient.parseRequest(request);
		assertNotNull(result);
		assertFalse(result.isNull("form"));
		JSONObject form = result.optJSONObject("form");
		assertFalse(form.isNull(fieldName1));
		assertEquals(uploadData1, form.optString(fieldName1));
		assertFalse(form.isNull(fieldName2));
		assertEquals(uploadData2, form.optString(fieldName2));
	}

	public void testUploadUrlEncoded() throws Exception {
		final String fieldName = "fieldName";
		final String uploadData = "Uploaded Post Data URL encoded";

		HttpBodyParameters body = new HttpBodyUrlEncoded();
		body.add(fieldName, uploadData);

		BaseHttpRequest<JSONObject, ServerException> request = new BaseHttpRequest.Builder<JSONObject, ServerException>().
				setUrl("http://httpbin.org/post?test=urlencoded").
				setBody(body).
				setResponseHandler(BodyToJSONObject.RESPONSE_HANDLER).
				build();
		// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());

		JSONObject result = TopheClient.parseRequest(request);
		assertNotNull(result);
		assertFalse(result.isNull("form"));
		JSONObject form = result.optJSONObject("form");
		assertFalse(form.isNull(fieldName));
		assertEquals(uploadData, form.optString(fieldName));
	}

	public void testUploadJson() throws Exception {
		final String fieldName1 = "name";
		final String uploadData1 = "Steve Lhomme";
		final String fieldName2 = "screenName";
		final String uploadData2 = "robUx4";

		JSONObject object = new JSONObject();
		object.put(fieldName1, uploadData1);
		object.put(fieldName2, uploadData2);

		HttpBodyJSON body = new HttpBodyJSON(object);
		BaseHttpRequest<JSONObject, ServerException> request = new BaseHttpRequest.Builder<JSONObject, ServerException>().
				setUrl("http://httpbin.org/post?test=jsonBody").
				setBody(body).
				setResponseHandler(BodyToJSONObject.RESPONSE_HANDLER).
				build();
		// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());

		JSONObject result = TopheClient.parseRequest(request);
		assertNotNull(result);
		assertFalse(result.isNull("json"));
		JSONObject json = result.optJSONObject("json");
		assertFalse(json.isNull(fieldName1));
		assertEquals(uploadData1, json.optString(fieldName1));
		assertFalse(json.isNull(fieldName2));
		assertEquals(uploadData2, json.optString(fieldName2));
	}

	public void testTimeout() throws Exception {
		BaseHttpRequest<JSONObject, ServerException> request = new BaseHttpRequest.Builder<JSONObject, ServerException>().
				setUrl("http://httpbin.org/delay/10").
				setResponseHandler(BodyToJSONObject.RESPONSE_HANDLER).
				build();
		request.setHttpConfig(new HttpConfig() {
			@Override
			public int getReadTimeout(HttpRequestInfo request) {
				return 3000; // 3s
			}
		});
		// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());

		try {
			JSONObject result = TopheClient.parseRequest(request);
			fail("we should have timed out after 3s");
		} catch (HttpTimeoutException e) {
			// ok
		}
	}

	private void testError(int errorCode) throws Exception {
		BaseHttpRequest<String, ServerException> request = new BaseHttpRequest.Builder<String, ServerException>().
				setUrl("http://httpbin.org/status/" + errorCode).
				setResponseHandler(BodyToString.RESPONSE_HANDLER).
				build();
		// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());

		try {
			String result = TopheClient.parseRequest(request);
			fail("we should have an HTTP error " + errorCode);
		} catch (ServerException e) {
			assertEquals(errorCode, e.getStatusCode());
		}
	}

	private void testStreamingError(int errorCode) throws Exception {
		BaseHttpRequest<HttpStream, ServerException> request = new BaseHttpRequest.Builder<HttpStream, ServerException>()
				.setUrl("http://httpbin.org/status/" + errorCode)
				.setResponseHandler(BodyToHttpStream.RESPONSE_HANDLER)
				.build();

		try {
			HttpStream result = TopheClient.parseRequest(request);
			fail("we should have an HTTP error " + errorCode + ", not a stream");
		} catch (ServerException e) {
			assertEquals(errorCode, e.getStatusCode());
		}
	}

	public void testError401() throws Exception {
		testError(401);
	}

	public void testError500() throws Exception {
		testError(500);
	}

	public void testStreamingError401() throws Exception {
		testStreamingError(401);
	}

	@MediumTest
	public void testString() throws Exception {
		BaseHttpRequest<String, ServerException> request = new BaseHttpRequest.Builder<String, ServerException>()
				.setUrl("http://httpbin.org/ip")
				.setResponseHandler(BodyToString.RESPONSE_HANDLER)
				.build();
		// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());

		String result = TopheClient.parseRequest(request);
		assertNotNull(result);
		assertTrue(result.contains("\"origin\""));
	}

	@MediumTest
	public void testMime() throws Exception {
		BaseHttpRequest<String, ServerException> request = new BaseHttpRequest.Builder<String, ServerException>()
				.setUrl("http://httpbin.org/html")
				.setResponseHandler(BodyToString.RESPONSE_HANDLER)
				.build();
		// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());
		request.setHeader(HttpRequest.HEADER_ACCEPT, "application/json");

		try {
			String result = TopheClient.parseRequest(request);
			fail("we should not be here");
		} catch (HttpMimeException e) {
			// ok
		}
	}

	@MediumTest
	public void testStreaming() throws Exception {
		BaseHttpRequest<HttpStream, ServerException> request = new BaseHttpRequest.Builder<HttpStream, ServerException>()
				.setUrl("http://httpbin.org/drip?numbytes=5&duration=5")
				.setResponseHandler(BodyToHttpStream.RESPONSE_HANDLER)
				.build();
		// TODO assertEquals(STREAM_ENGINE_CLASS, request.getHttpEngine().getClass());

		HttpStream stream = TopheClient.parseRequest(request);
		try {
			InputStream streamIn = stream.getInputStream();
			byte[] buffer = new byte[1];
			int read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			assertEquals(-1, streamIn.read(buffer));
		} finally {
			stream.disconnect();
		}
	}

	@MediumTest
	public void testStreamingCompressed() throws Exception {
		BaseHttpRequest<HttpStream, ServerException> request = new BaseHttpRequest.Builder<HttpStream, ServerException>()
				.setUrl("http://httpbin.org/drip?numbytes=5&duration=5")
				.setResponseHandler(BodyToHttpStream.RESPONSE_HANDLER)
				.build();
		request.setHeader(HttpRequest.HEADER_ACCEPT_ENCODING, "gzip,deflate");
		// TODO assertEquals(STREAM_ENGINE_CLASS, request.getHttpEngine().getClass());

		HttpStream stream = TopheClient.parseRequest(request);
		try {
			InputStream streamIn = stream.getInputStream();
			byte[] buffer = new byte[1];
			int read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);

			assertEquals(-1, streamIn.read(buffer));
		} finally {
			stream.disconnect();
		}
	}

	@MediumTest
	public void testStreamingLine() throws Exception {
		BaseHttpRequest<HttpStream, ServerException> request = new BaseHttpRequest.Builder<HttpStream, ServerException>()
				.setUrl("http://httpbin.org/stream/2")
				.setResponseHandler(BodyToHttpStream.RESPONSE_HANDLER)
				.build();
		// TODO assertEquals(STREAM_ENGINE_CLASS, request.getHttpEngine().getClass());

		HttpStream stream = TopheClient.parseRequest(request);
		try {
			BufferedSource lineReader = Okio.buffer(Okio.source(stream.getInputStream()));
			String line = lineReader.readUtf8LineStrict();
			assertNotNull(line);
			assertTrue(line.contains("\"headers\":"));

			line = lineReader.readUtf8LineStrict();
			assertNotNull(line);
			assertTrue(line.contains("\"headers\":"));

			try {
				line = lineReader.readUtf8LineStrict();
				fail("we should throw after the 2 lines are received");
			} catch (EOFException ok) {
				// all good
			}
		} finally {
			stream.disconnect();
		}
	}

	@MediumTest
	public void testStreamingTimeout() throws Exception {
		BaseHttpRequest<HttpStream, ServerException> request = new BaseHttpRequest.Builder<HttpStream, ServerException>()
				.setUrl("http://httpbin.org/drip?numbytes=5&duration=2&delay=8")
				.setResponseHandler(BodyToHttpStream.RESPONSE_HANDLER)
				.build();
		request.setHttpConfig(new HttpConfig() {
			@Override
			public int getReadTimeout(HttpRequestInfo request) {
				return 5000; // 5s
			}
		});
		// TODO assertEquals(STREAM_ENGINE_CLASS, request.getHttpEngine().getClass());

		try {
			HttpStream stream = TopheClient.parseRequest(request);
			try {
				BufferedSource lineReader = Okio.buffer(Okio.source(stream.getInputStream()));
				String line = lineReader.readUtf8();
				assertNotNull(line);
				assertTrue(line.startsWith("*****"));
				fail("we should not read 1 item");
			} finally {
				stream.disconnect();
			}
			fail("we should have been in timeout");
		} catch (HttpTimeoutException e) {
			// ok
		}
	}

	@MediumTest
	public void testStreamingDisconnect() throws Exception {
		BaseHttpRequest<HttpStream, ServerException> request = new BaseHttpRequest.Builder<HttpStream, ServerException>()
				.setUrl("http://httpbin.org/drip?numbytes=5&duration=200&delay=2")
				.setResponseHandler(BodyToHttpStream.RESPONSE_HANDLER)
				.build();
		// TODO assertEquals(STREAM_ENGINE_CLASS, request.getHttpEngine().getClass());

		HttpStream stream = TopheClient.parseRequest(request);
		InputStream streamIn = stream.getInputStream();
		byte[] buffer = new byte[1];
		int read = streamIn.read(buffer);
		if (read == -1) throw new EOFException("could not read more");
		assertEquals('*', buffer[0]);

		stream.disconnect();

		try {
			stream.getInputStream().read(new byte[1]);
			fail("we shouldn't be able to read after disconnection");
		} catch (EOFException ok) {
			// all good
		} catch (IOException ok) {
			assertTrue(ok.getMessage().contains("closed"));
		}
	}

	@MediumTest
	public void testStreamingDisconnectAsync() throws Exception {
		BaseHttpRequest<HttpStream, ServerException> request = new BaseHttpRequest.Builder<HttpStream, ServerException>()
				.setUrl("http://httpbin.org/drip?numbytes=5&duration=200&delay=2")
				.setResponseHandler(BodyToHttpStream.RESPONSE_HANDLER)
				.build();
		// TODO assertEquals(STREAM_ENGINE_CLASS, request.getHttpEngine().getClass());

		final HttpStream stream = TopheClient.parseRequest(request);
		InputStream streamIn = stream.getInputStream();
		byte[] buffer = new byte[1];
		int read = streamIn.read(buffer);
		if (read == -1) throw new EOFException("could not read more");
		assertEquals('*', buffer[0]);

		final CountDownLatch latch = new CountDownLatch(1);
		new Thread() {
			@Override
			public void run() {
				stream.disconnect();
				latch.countDown();
			}
		}.start();
		latch.await(40, TimeUnit.SECONDS);

		try {
			stream.getInputStream().read(new byte[1]);
			fail("we shouldn't be able to read after disconnection");
		} catch (EOFException ok) {
			// all good
		} catch (IOException ok) {
			assertTrue(ok.getMessage().contains("closed"));
		}
	}

	public void testNullContext() throws Exception {
		try {
			TopheClient.setup(null);
			BaseHttpRequest<String, ServerException> request = new BaseHttpRequest.Builder<String, ServerException>()
					.setUrl("http://httpbin.org/drip?numbytes=5&duration=200&delay=2")
					.setResponseHandler(BodyToString.RESPONSE_HANDLER)
					.build();
			// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());
			//when not using Ion, we don't need a Context fail("A query with no context is invalid");
		} catch (NullPointerException e) {
			// all good
		}
	}

	public void testNullContextStreaming() throws Exception {
		TopheClient.setup(null);
		BaseHttpRequest<String, ServerException> request = new BaseHttpRequest.Builder<String, ServerException>()
				.setUrl("http://httpbin.org/drip?numbytes=5&duration=200&delay=2")
				.setResponseHandler(BodyToString.RESPONSE_HANDLER)
				.build();
			// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());
	}

	public void testSetupContext() throws Exception {
		TopheClient.setup(getContext());
		BaseHttpRequest<String, ServerException> request = new BaseHttpRequest.Builder<String, ServerException>()
				.setUrl("http://httpbin.org/drip?numbytes=5&duration=200&delay=2")
				.setResponseHandler(BodyToString.RESPONSE_HANDLER)
				.build();
			// TODO assertEquals(ENGINE_CLASS, request.getHttpEngine().getClass());
		TopheClient.setup(null);
	}
}
