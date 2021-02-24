import net.NetConstants;
import net.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(NetConstants.PORT);
        new Thread(server).start();
    }
}
