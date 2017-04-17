package com.eldrix.openconsent.model;

import java.time.LocalDateTime;

import com.eldrix.openconsent.model.auto._ConsentForm;

public class ConsentForm extends _ConsentForm {

    private static final long serialVersionUID = 1L;

	@Override
	protected void onPostAdd() {
		setCreatedDateTime(LocalDateTime.now());
		setStatus(ConsentFormStatus.DRAFT);
	} 

}
