package com.eldrix.openconsent.model;

import java.time.LocalDateTime;

import com.eldrix.openconsent.model.auto._PermissionForm;

public class PermissionForm extends _PermissionForm {
    private static final long serialVersionUID = 1L; 

    @Override
    protected void onPostAdd() {
        setDateTimeCreated(LocalDateTime.now());
    }

}
