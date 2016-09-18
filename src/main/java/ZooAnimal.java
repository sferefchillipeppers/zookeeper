import java.io.IOException;
import java.util.Random.*;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import com.sun.corba.se.impl.transport.CorbaContactInfoBase;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/* All animals must enter the zoo first, before they all can start running.
 * The animals are programs/processes that will be represented by nodes.
 * As each animal enters the zoo, they will check if there are the required 
 * number of animals before they can run. If the number has not been reached,
 * they will set a watcher and will wait until there are enough.
 * Once there are enough animals, every animal will get notify and they will run
 * at the same time.
 */

//1 animal = 1 proceso
//requisito que tengan un número de animal
//Si el número no ha sido alcanzado.
//pondrán un watcher y esperarán hasta que haya sufiecientes animales.


public class ZooAnimal implements Watcher {

	static ZooKeeper zk = null;
	Integer mutex;
	String animal;
	String root;
	
	public ZooAnimal(String animal, String address)throws IOException{
		if(zk == null){
				System.out.println("Starting ZooKeeperAnimal");
				// creating a new ZooKeeper instance + setting itself as the watcher
				zk = new ZooKeeper(address,15000,this);
				// setting up a mutex to be used for waiting on the Watches 
				this.mutex=0;
				this.animal = animal;
				System.out.println("An animal has entered the zoo: " + animal);		
		}
	}

	/*This is the process method of the Watch interface. Its task is to
	 * process the notifications triggered due to watches.
	 */

	//@Override
	synchronized public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		synchronized (mutex){
			System.out.println("Process: " + event.getType());
			//notifies the mutex waiting on it
			mutex.notify();//SOLUTION

		}
	}

	/* The Animal class, representing a node in the ZooKeeper system.*/
	static public class Animal extends ZooAnimal {
		int size;
		String nodeName;
		String animal;

		// animal constructor
		Animal(String animal, String address, String root, int size) throws Exception {
			super(animal, address);
			this.animal = animal;
			this.root = root;
			this.size = size;

			if (zk != null) {
				/* checking if the root node exists. Note: this is not the  ZooKeeper root, but the root of our application. */
				//Need to construct my path : /root + /animal

				String container_size = Integer.toString(size);

				if (zk.exists(this.root, false) == null) {
					System.out.println("Creando znodo raíz....");
					try {
						zk.create(this.root, container_size.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					} catch (KeeperException.NodeExistsException e) {
						e.printStackTrace();
					} catch (KeeperException.ConnectionLossException e) {
						e.printStackTrace();
					}
				} else {
					Stat stat = new Stat();
					byte data[] = zk.getData(root, false, stat);
					String info = data.toString();
					System.out.println("Animal que existe en el zoologico " + info);
				}
			}
					/* If the Stat object returned by the exists method is null (no zoo)
					 * it has to be created. Create the zoo (application's root node).
					 */
			// Get the node name
		}

		boolean enter() throws KeeperException, InterruptedException {
			//create the child node to represent the animal.

			this.nodeName = this.root + this.animal;

			System.out.println(nodeName);

			if (zk.exists(nodeName, false) == null) {
				System.out.println("Creando znodo animal....");
				try {
					String value = Integer.toString(1);
					zk.create(nodeName, value.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				} catch (KeeperException.NodeExistsException e) {
					e.printStackTrace();
				} catch (KeeperException.ConnectionLossException e) {
					e.printStackTrace();
				}
			}

			while (true) {
				synchronized (mutex) {
					/* Get the list of children + setting a watcher on the root
					* to notify where there is a change to the root node */

					Watcher nodeRootChanged = new Watcher() {
						public void process(WatchedEvent ev) {
							if (ev.getType() == Event.EventType.NodeChildrenChanged) {
								System.out.println(ev.getState());
							}
						}
					};

					List<String> lista = zk.getChildren(root, nodeRootChanged);
					mutex = lista.size();
					if(mutex == size){
						System.out.println("Zoologico completo");
						System.out.println("Animals running");
						leave();
						zk.close();
					}
					else{
						Thread.sleep(1000);
					}
				}
			}
		}
					/* if the number of animals (nodes) has not been reached
					 * keep waiting for the notification from the watch
					 */


		boolean leave() throws KeeperException, InterruptedException{
			// delete the animal node

			zk.delete(nodeName,0);
			
			while(true){
				synchronized (mutex){
					List<String> list = zk.getChildren(root, true);
					if(list.size() > 0){
						mutex.wait();
					}
					else{
						return true;
					}
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Starting ZooAnimal Runner");
		String animal = args[0];
		String host_port = args[1];
		int size = new Integer(args[2]);
        
		// create the Animal
		Animal a = new Animal(animal,host_port,"/zooAnimal",size);
		
        // have the animal enter the zoo
		a.enter();
            
        System.out.println(animal + " entered zoo");
            
        // if flag is false, print an error message to alert the user
       
        
        // Generate random integer to run the program. The program will only reach this stage when the Watcher
        // has notified that there are enough animals in the zoo
        Random rand = new Random();
        int r = rand.nextInt(100);
 
        for (int i = 0; i < r; i++) {
            try {
                Thread.sleep(1000);
                // print the name of the animal to see it run in the console
                
            } catch (InterruptedException e) {
            	System.out.println("Running interrupted exception e: " + e.getMessage());
            }
        }
        // when the animal is done running, have it leave the zoo
                  
        // Print a message saying the animal left the zoo.
        }
}
