package co.tophe.ion.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.ion.builder.Builders;
import co.tophe.body.HttpBodyJSON;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpBodyJSON extends HttpBodyJSON implements IonBody {

	public IonHttpBodyJSON(HttpBodyJSON sourceBody) {
		super(sourceBody);
	}

	@Override
	public String getContentType() {
		return JSONObjectBody.CONTENT_TYPE;
	}

	@Override
	public void setOutputData(Builders.Any.B requestBuilder) {
		if (jsonElement instanceof JsonObject)
			requestBuilder.setJsonObjectBody((JsonObject) jsonElement);
		else if (jsonElement instanceof JsonArray)
			requestBuilder.setJsonArrayBody((JsonArray) jsonElement);
	}
}
