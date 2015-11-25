package mobilesecurity.ini.cmu.edu.oauthdemo;

/**
 * Created by wind4e on 11/21/15.
 */
public class LoginToken {
    private String token;
    private LoginType type;
    private boolean loggedIn;

    public LoginToken() {
        this("",LoginType.FACEBOOK, false);
    }

    public LoginToken(String token, LoginType type, boolean loggedIn) {
        this.type = type;
        this.token = token;
        this.loggedIn = loggedIn;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setLoginType(LoginType type) {
        this.type = type;
    }

    public void setLoginStatus(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getToken() {
        return this.token;
    }

    public LoginType getLoginType() {
        return this.type;
    }

    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    enum LoginType {
        FACEBOOK("Facebook"), GOOGLE("Google");
        private String text;

        private LoginType(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }
}
