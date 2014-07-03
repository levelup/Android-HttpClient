package com.levelup.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONObject;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

public class HttpClientTest extends AndroidTestCase {

	@MediumTest
	public void testUploadStream() throws Exception {
		final String fileFieldName = "media";
		final String uploadData = "Uploaded Stream Data";
		HttpBodyMultiPart body = new HttpBodyMultiPart(1);
		body.addStream("media", new ByteArrayInputStream(uploadData.getBytes()), uploadData.length(), "text/plain");

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
}
