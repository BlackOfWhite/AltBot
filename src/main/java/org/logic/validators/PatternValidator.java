package org.logic.validators;

import org.apache.commons.validator.routines.EmailValidator;

public class PatternValidator {

    public boolean isMarketNameValid(final String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        if (s.length() > 13) {
            return false;
        }
        int count = s.length() - s.replace("-", "").length();
        if (count != 1) {
            return false;
        }
        String s1 = s.replace("-", "");
        boolean allLetters = s1.chars().allMatch(Character::isLetter);
        return allLetters;
    }

    public boolean isEmailValid(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        EmailValidator validator = EmailValidator.getInstance();
        return validator.isValid(email);
    }
}
