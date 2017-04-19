package com.eldrix.openconsent.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
		setDateTimeCreated(LocalDateTime.now());
		setStatus(ConsentFormStatus.DRAFT);
	}

	@Override
	protected void validateForSave(ValidationResult validationResult) {
		super.validateForSave(validationResult);
		ConsentFormStatus committed = getCommittedStatus();
		if (ConsentFormStatus.FINAL == committed && ConsentFormStatus.DRAFT == getStatus()) {
			validationResult.addFailure(new SimpleValidationFailure(this, "Once a form is FINAL, it cannot be changed to DRAFT."));
		}
		if (ConsentFormStatus.DEPRECATED == committed && ConsentFormStatus.DEPRECATED != getStatus()) {
			validationResult.addFailure(new SimpleValidationFailure(this, "Once a form is DEPRECATED, it cannot be changed."));
		}
		if (ConsentFormStatus.FINAL == getStatus() && getDateTimeFinalised() == null) {
			validationResult.addFailure(new SimpleValidationFailure(this, "Date/time must be set when consent form finalised."));
		}
	}

	@Override
	public void setStatus(ConsentFormStatus status) {
		ConsentFormStatus committed = getCommittedStatus();
		if (ConsentFormStatus.FINAL != committed && ConsentFormStatus.FINAL == status) {
			setDateTimeFinalised(LocalDateTime.now());
		}
		super.setStatus(status);
	}
	
	

    public Optional<ConsentItem> getItemWithBehaviour(String behaviour) {
    	return getConsentItems().stream()
    			.filter(item -> item.getBehaviour().equalsIgnoreCase(behaviour))
    			.findFirst();
    }

	/**
	 * Return an ordered list of the consent items.
	 * @return
	 */
	public List<ConsentItem> getOrderedConsentItems() {
		ArrayList<ConsentItem> ordered = new ArrayList<>(getConsentItems());
		Collections.sort(ordered);
		return Collections.unmodifiableList(ordered);
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
