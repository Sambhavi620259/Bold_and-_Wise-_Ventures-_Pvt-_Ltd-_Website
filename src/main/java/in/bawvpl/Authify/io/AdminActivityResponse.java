package in.bawvpl.Authify.io;

public class AdminActivityResponse {

    private String type;
    private String message;
    private String time;

    // GET TYPE
    public String getType() {
        return type;
    }

    // SET TYPE
    public void setType(String type) {
        this.type = type;
    }

    // GET MESSAGE
    public String getMessage() {
        return message;
    }

    // SET MESSAGE
    public void setMessage(String message) {
        this.message = message;
    }

    // GET TIME
    public String getTime() {
        return time;
    }

    // SET TIME
    public void setTime(String time) {
        this.time = time;
    }
}