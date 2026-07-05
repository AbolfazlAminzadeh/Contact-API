package org.Amin.Contact.Exceptions;

import java.util.regex.Pattern;

public class InvalidPhoneNumber extends RuntimeException {
    public InvalidPhoneNumber(String message) {
        super(message);
    }
    public static void check(String phone) throws InvalidPhoneNumber {
        // Check using api Later
        if (!phone.startsWith("09")) phone = "09" + phone.replaceFirst("^(09|0|9)?", "");
        if (!phone.matches("\\d+")) throw new InvalidPhoneNumber("Characters in phone number? are you awake?");
        if (phone.length() != 11) throw new InvalidPhoneNumber("The Phone Number length is invalid");
    }
}
