package geekgfs.chunk;

import geekgfs.file.FileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录chunk的备份位置
 */

public class ChunkInfo {

    private Chunk chunk;
    //该chunk在文件中的序号
    private int order;
    //该chunk所对应的chunkserver
    private List<String> chunkServers;
    //该chunk所对应的fileInfo，多对一关系
    private FileInfo fileInfo;

    public ChunkInfo(Chunk chunk, int order) {
        this.chunk = chunk;
        this.order = order;
        this.chunkServers = new ArrayList<>();
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void addChunkServer(String chunkServer) {
        this.chunkServers.add(chunkServer);
    }

    public void deleteChunkServer(String chunkServer) {
        this.chunkServers.remove(chunkServer);
    }

    public List<String> getChunkServers() {
        return chunkServers;
    }

    public void setChunkServers(List<String> chunkServers) {
        this.chunkServers = chunkServers;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }
}
