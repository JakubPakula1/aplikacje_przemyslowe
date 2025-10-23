package com.github.jakubpakula1.lab.model;

import java.util.ArrayList;
import java.util.List;

public class ImportSummary {
    private int importedEmployees;
    private  List<ErrorEntry> errors;

    public ImportSummary() {
        this.errors = new ArrayList<>();
    }

    public int getImportedEmployees() {
        return importedEmployees;
    }

    public void setImportedEmployees(int importedEmployees) {
        this.importedEmployees = importedEmployees;
    }

    public List<ErrorEntry> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorEntry> errors) {
        this.errors = errors;
    }

    public void addError(int lineNumber, String errorMessage) {
        errors.add(new ErrorEntry(lineNumber, errorMessage));
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ImportSummary: ");
        sb.append(importedEmployees).append(" employees imported");

        if (errors.isEmpty()) {
            sb.append(", no errors");
        } else {
            sb.append(", ").append(errors.size()).append(" errors:");
            for (ErrorEntry error : errors) {
                sb.append("\n  Line ").append(error.getLineNumber())
                  .append(": ").append(error.getErrorMessage());
            }
        }

        return sb.toString();
    }
    public static class ErrorEntry {
        private int lineNumber;
        private String errorMessage;


        public ErrorEntry(int lineNumber, String errorMessage) {
            this.lineNumber = lineNumber;
            this.errorMessage = errorMessage;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
        @Override
        public String toString() {
            return "Line " + lineNumber + ": " + errorMessage;
        }
    }
}
