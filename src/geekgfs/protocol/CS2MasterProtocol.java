package geekgfs.protocol;

import java.rmi.Remote;

public interface CS2MasterProtocol extends Remote {

    //chunkserver加入集群
    void addChunkServer(String socket);
}
