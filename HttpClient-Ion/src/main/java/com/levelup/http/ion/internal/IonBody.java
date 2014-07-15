package com.levelup.http.ion.internal;

import com.koushikdutta.ion.builder.Builders;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public interface IonBody {
	void setOutputData(Builders.Any.B requestBuilder);
}
