package com.levelup.http;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.json.JSONObject;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class HttpClientTest extends AndroidTestCase {

	@MediumTest
	public void testUploadInputStream() throws Exception {
		final String fileFieldName = "media";
		final String uploadData = "Uploaded Streamé Data";
		HttpBodyMultiPart body = new HttpBodyMultiPart(1);
		body.addStream("media", new ByteArrayInputStream(uploadData.getBytes()), uploadData.getBytes().length, "text/plain");

		BaseHttpRequest<JSONObject> request = new BaseHttpRequest.Builder<JSONObject>(getContext()).
				setUrl("http://httpbin.org/post?test=stream").
				setBody(body).
				setStreamParser(InputStreamJSONObjectParser.instance).
				build();

		JSONObject result = HttpClient.parseRequest(request);
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

			BaseHttpRequest<JSONObject> request = new BaseHttpRequest.Builder<JSONObject>(getContext()).
					setUrl("http://httpbin.org/post?test=file").
					setBody(body).
					setStreamParser(InputStreamJSONObjectParser.instance).
					build();

			JSONObject result = HttpClient.parseRequest(request);
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

		BaseHttpRequest<JSONObject> request = new BaseHttpRequest.Builder<JSONObject>(getContext()).
				setUrl("http://httpbin.org/post?test=multitext").
				setBody(body).
				setStreamParser(InputStreamJSONObjectParser.instance).
				build();

		JSONObject result = HttpClient.parseRequest(request);
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

		BaseHttpRequest<JSONObject> request = new BaseHttpRequest.Builder<JSONObject>(getContext()).
				setUrl("http://httpbin.org/post?test=urlencoded").
				setBody(body).
				setStreamParser(InputStreamJSONObjectParser.instance).
				build();

		JSONObject result = HttpClient.parseRequest(request);
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
		BaseHttpRequest<JSONObject> request = new BaseHttpRequest.Builder<JSONObject>(getContext()).
				setUrl("http://httpbin.org/post?test=jsonBody").
				setBody(body).
				setStreamParser(InputStreamJSONObjectParser.instance).
				build();

		JSONObject result = HttpClient.parseRequest(request);
		assertNotNull(result);
		assertFalse(result.isNull("json"));
		JSONObject json = result.optJSONObject("json");
		assertFalse(json.isNull(fieldName1));
		assertEquals(uploadData1, json.optString(fieldName1));
		assertFalse(json.isNull(fieldName2));
		assertEquals(uploadData2, json.optString(fieldName2));
	}

	@MediumTest
	public void testStreaming() throws Exception {
		BaseHttpRequest<HttpStream> request = new BaseHttpRequest.Builder<HttpStream>(getContext()).
				setUrl("http://httpbin.org/drip?numbytes=5&duration=5").
				setStreaming().
				build();

		HttpStream stream = HttpClient.parseRequest(request);
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

			assertEquals(-1 ,streamIn.read(buffer));
		} finally {
			stream.disconnect();
		}
	}

	@MediumTest
	public void testStreamingLine() throws Exception {
		BaseHttpRequest<HttpStream> request = new BaseHttpRequest.Builder<HttpStream>(getContext()).
				setUrl("http://httpbin.org/stream/2").
				setStreaming().
				build();

		HttpStream stream = HttpClient.parseRequest(request);
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
		BaseHttpRequest<HttpStream> request = new BaseHttpRequest.Builder<HttpStream>(getContext()).
				setUrl("http://httpbin.org/drip?numbytes=5&duration=2&delay=8").
				setStreaming().
				build();
		request.setHttpConfig(new HttpConfig() {
			@Override
			public int getReadTimeout(HttpRequest request) {
				return 5000; // 5s
			}
		});

		try {
			HttpStream stream = HttpClient.parseRequest(request);
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
		} catch (HttpException e) {
			if (e.getErrorCode() != HttpException.ERROR_TIMEOUT)
				throw e;
		}
	}

	@MediumTest
	public void testStreamingDisconnect() throws Exception {
		BaseHttpRequest<HttpStream> request = new BaseHttpRequest.Builder<HttpStream>(getContext()).
				setUrl("http://httpbin.org/drip?numbytes=5&duration=200&delay=2").
				setStreaming().
				build();

		HttpStream stream = HttpClient.parseRequest(request);
		try {
			InputStream streamIn = stream.getInputStream();
			byte[] buffer = new byte[1];
			int read = streamIn.read(buffer);
			if (read == -1) throw new EOFException("could not read more");
			assertEquals('*', buffer[0]);
		} finally {
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
	}
}
