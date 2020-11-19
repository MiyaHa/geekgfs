package geekgfs.protocol;

import geekgfs.chunk.Chunk;

import java.rmi.Remote;

/**
 * master to chunkserver protocol
 */

public interface Master2CSProtocol extends Remote {

    //删除chunk
    void deleteChunk(Chunk chunk);

}
