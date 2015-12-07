package mobilesecurity.ini.cmu.edu.oauthdemo;

public class DoorTaskParameter {
    public final String url;
    public final LoginToken token;

    public DoorTaskParameter(String url, LoginToken token) {
        this.url = url;
        this.token = token;
    }
}
