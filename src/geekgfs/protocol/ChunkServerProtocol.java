package geekgfs.protocol;

import geekgfs.entity.Chunk;

import java.rmi.Remote;

public interface ChunkServerProtocol extends Remote {

    //上传chunk及数据流
    void addChunk(Chunk chunk, byte[] stream) throws Exception;

    byte[] getChunk(Chunk chunk) throws Exception;

    //追加未达到defChunkSize的chunk
    void updateChunk(Chunk chunk, byte[] stream) throws Exception;
}
