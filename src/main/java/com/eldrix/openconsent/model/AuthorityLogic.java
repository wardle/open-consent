package com.eldrix.openconsent.model;

import com.eldrix.openconsent.nhs.NhsNumber;

public enum AuthorityLogic {
	UK_NHS {
		@Override
		public boolean isValidIdentifier(Authority authority, String identifier) {
			return NhsNumber.validate(identifier);
		}
	};
	public abstract boolean isValidIdentifier(Authority authority, String identifier);
}
