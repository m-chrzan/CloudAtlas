package pl.edu.mimuw.cloudatlas.client;

public class ContactsString {
    private String string;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return "ContactString{" +
                "string='" + string + '\'' +
                '}';
    }
}
