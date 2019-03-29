package com.krux.stdlib.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StatusBean {

    private final AppState appStatus;

    private final String message;

    private final List<String> warnings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public StatusBean(AppState appStatus, String message) {
        this.appStatus = appStatus;
        this.message = message;
    }

    @Override
    public int hashCode() {
        // only the status and message contribute to this objects "identity"
        return Objects.hash(appStatus, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }

        StatusBean that = (StatusBean) obj;
        return Objects.equals(this.appStatus, that.appStatus)
            && Objects.equals(this.message, that.message);
    }

    @Override
    public String toString() {
        return "StatusBean(" + appStatus + ", " + message + ", warnings: " + warnings + ", errors: " + errors + ")";
    }

    public AppState getAppStatus() {
        return appStatus;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public StatusBean addWarning(String warning) {
        warnings.add(warning);
        return this;
    }

    public StatusBean addError(String error) {
        errors.add(error);
        return this;
    }

}
