package geekgfs.protocol;

import geekgfs.entity.Chunk;

import java.rmi.Remote;

/**
 * master to chunkserver protocol
 */

public interface Master2CSProtocol extends Remote {

    //删除chunk
    void deleteChunk(Chunk chunk) throws Exception;

}
