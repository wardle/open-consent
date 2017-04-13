package com.eldrix.openconsent.core;

import java.util.Collections;

import com.nhl.link.rest.annotation.listener.QueryAssembled;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/** 
 * A simple LinkRest listener that intercepts the LinkRest pipeline
 * to return a single object
 *
 * @param <T>
 */
public class SingleObjectListener<T> {
	final T _object;
	public SingleObjectListener(T o) {
		_object = o;
	}

	// called after select parameters applied
	@QueryAssembled
	public ProcessingStage<SelectContext<T>, T> performFetch(SelectContext<T> context) {
		context.setObjects(Collections.singletonList(_object));
		return null;
	}
}