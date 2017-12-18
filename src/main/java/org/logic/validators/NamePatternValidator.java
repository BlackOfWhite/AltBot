package org.logic.validators;

public class NamePatternValidator {

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
        String s1 = s.replace("-","");
        boolean allLetters = s1.chars().allMatch(Character::isLetter);
        return allLetters;
    }

}
