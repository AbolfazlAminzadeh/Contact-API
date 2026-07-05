package org.Amin.Contact.Contact;

import org.Amin.Contact.Exceptions.InvalidPhoneNumber;
// TODO optimizations for later
public record Contact(int id, String firstName, String lastName, String phone) {

    public void validate() throws InvalidPhoneNumber, IllegalArgumentException {
        if (id() < 0) throw new IllegalArgumentException("ID Cannot be smaller than 0"); // Maybe change later
        if ( // It's a dirty check, I will change it later
                firstName == null ||
                        firstName.isEmpty() ||
                        lastName == null ||
                        lastName.isEmpty()
        ) throw new IllegalArgumentException("First name and Lastname cannot be null/empty"); // Pass it to user class
        InvalidPhoneNumber.check(phone());
    }
}
