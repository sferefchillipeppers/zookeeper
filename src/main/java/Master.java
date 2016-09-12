/*Para recibir notificaciones de zookeeper, necesitaremos implementar watchers
vamos a echar un vistazo a la interfaz watcher.*/

//public interface Watcher {
// void process(WatchedEvent event); ---> indica el tipo de evento
// }
/*** CLIENTE SINCRONO DE ZOOKEEPER ***/

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;

import java.io.IOException;

public class Master implements Watcher{

    ZooKeeper zk;
    String hostPort;

    public  Master(String hostPort){
        this.hostPort=hostPort;
    }

    public void startZK() throws IOException {
        zk = new ZooKeeper(hostPort,15000,this);
        //El objeto zookeeper recibe como parámetro:
        //1. host:puerto
        //2. Timeout de la sesión
        //3. El watcher para las notificaciones
    }

    public void process(WatchedEvent event){
        //simplemente queremos que notifique acerca del evento producido una vez realizada la conexión.
        System.out.println(event);
    }

    public static void main(String[]args) throws Exception{

        Master m = new Master(args[0]);
        m.startZK();
        Thread.sleep(60000);
    }
}
