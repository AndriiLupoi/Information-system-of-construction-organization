public class User {
    private int id;
    public String login;
    public String password;
    public String possition;

    public User(int id, String login, String password, String possition) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.possition = possition;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getPossition() {
        return possition;
    }
}
