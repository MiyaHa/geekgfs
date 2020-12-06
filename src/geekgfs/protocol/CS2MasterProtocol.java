package geekgfs.protocol;

import java.rmi.Remote;

/**
 * chunkserver to master protocol
 */

public interface CS2MasterProtocol extends Remote {

    //chunkserver加入集群
    void addChunkServer(String socket) throws Exception;
}
