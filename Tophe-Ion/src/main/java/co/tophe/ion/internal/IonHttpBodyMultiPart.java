package co.tophe.ion.internal;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.builder.Builders;
import co.tophe.body.HttpBodyMultiPart;

/**
 * @author Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpBodyMultiPart extends HttpBodyMultiPart implements IonBody {

	public IonHttpBodyMultiPart(HttpBodyMultiPart sourceBody) {
		super(sourceBody);
	}

	@Override
	public String getContentType() {
		return MultipartFormDataBody.CONTENT_TYPE;
	}

	@Override
	public void setOutputData(Builders.Any.B requestBuilder) {
		for (HttpParam param : mParams) {
			if (param.value instanceof File) {
				FilePart part = new FilePart(param.name, (File) param.value);
				if (!TextUtils.isEmpty(param.contentType))
					part.setContentType(param.contentType);
				part.getRawHeaders().add("Content-Transfer-Encoding", "binary");
				List<Part> partList = new ArrayList<Part>(1);
				partList.add(part);
				requestBuilder.addMultipartParts(partList);
				/*if (!TextUtils.isEmpty(param.contentType))
					requestBuilder.setMultipartFile(param.name, param.contentType, (File) param.value);
				else
					requestBuilder.setMultipartFile(param.name, (File) param.value);*/
			} else if (param.value instanceof InputStream) {
				InputStreamPart part = new InputStreamPart(param.name, (InputStream) param.value, param.length);
				if (!TextUtils.isEmpty(param.contentType))
					part.setContentType(param.contentType);
				part.getRawHeaders().add("Content-Transfer-Encoding", "binary");
				List<Part> partList = new ArrayList<Part>(1);
				partList.add(part);
				requestBuilder.addMultipartParts(partList);
			}
		}
		for (HttpParam param : mParams) {
			if (param.value instanceof String) {
				requestBuilder.setMultipartParameter(param.name, (String) param.value);
			}
		}
	}
}
