package com.krux.stdlib.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class StatusBean {

    private final AppState state;

    private final String message;

    private final List<String> warnings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public StatusBean(AppState appStatus, String message) {
        this.state = appStatus;
        this.message = message;
    }

    @Override
    public int hashCode() {
        // only the status and message contribute to this objects "identity"
        return Objects.hash(state, message);
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
        return Objects.equals(this.state, that.state)
            && Objects.equals(this.message, that.message);
    }

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("state", state)
                .append("message", message)
                .append("warnings", warnings)
                .append("errors", errors);
    }

    @Override
    public String toString() {
        return toStringBuilder().build();
    }

    public AppState getAppStatus() {
        return state;
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
