package com.eldrix.openconsent.model;

import java.time.LocalDateTime;

import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.validation.SimpleValidationFailure;
import org.apache.cayenne.validation.ValidationResult;

import com.eldrix.openconsent.model.auto._ConsentForm;

public class ConsentForm extends _ConsentForm {

	private static final long serialVersionUID = 1L;

	@Override
	protected void onPostAdd() {
		setCreatedDateTime(LocalDateTime.now());
		setStatus(ConsentFormStatus.DRAFT);
	}

	@Override
	protected void validateForSave(ValidationResult validationResult) {
		super.validateForSave(validationResult);
		ConsentFormStatus committed = getCommittedStatus();
		if (ConsentFormStatus.FINAL == committed && ConsentFormStatus.DRAFT == getStatus()) {
			validationResult.addFailure(new SimpleValidationFailure(this, "Cannot change status to DRAFT once it is FINAL."));
		}
		if (ConsentFormStatus.FINAL == getStatus() && getFinalDateTime() == null) {
			validationResult.addFailure(new SimpleValidationFailure(this, "Date/time must be set when consent form finalised."));
		}
	}

	@Override
	public void setStatus(ConsentFormStatus status) {
		ConsentFormStatus committed = getCommittedStatus();
		if (ConsentFormStatus.FINAL != committed && ConsentFormStatus.FINAL == status) {
			setFinalDateTime(LocalDateTime.now());
		}
		super.setStatus(status);
	}

	/**
	 * Return the committed value for the status.
	 * TODO: mark: make this less of a hack, and make generic and place into a utilities class.
	 * @return
	 */
	protected ConsentFormStatus getCommittedStatus() {
		if (getPersistenceState() != PersistenceState.NEW) {
			ObjectContext context = getObjectContext();
			if (context instanceof DataContext) {
				DataContext dContext = (DataContext) context;
				DataRow row = dContext.getObjectStore().getSnapshot(getObjectId());
				String name = getObjectContext().getEntityResolver().getObjEntity(this).getAttribute(ConsentForm.STATUS.getName()).getDbAttributeName();
				Object value = row.get(name);
				if (value instanceof ConsentFormStatus) {
					return (ConsentFormStatus) value;
				} else {
					throw new IllegalStateException("Value was not mapped to a consent form status.");
				}
			} else {
				throw new IllegalStateException("Expect object context to be an instance of data context.");
			}
		}
		return null;
	}
}
