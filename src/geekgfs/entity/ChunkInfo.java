package geekgfs.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 记录chunk的相关信息
 */

public class ChunkInfo implements Serializable {

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

    public List<String> getChunkServers() {
        return chunkServers;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public void setPrimaryReplica(String socket){
        //主副本服务器存储在index = 0处
        chunkServers.add(0, socket);
    }

    public String getPrimaryReplica(){
        return chunkServers.get(0);
    }

    public void addReplica(String socket){
        chunkServers.add(socket);
    }

    public void removeReplica(String socket){
        //若移除主副本服务器，且size > 1, 则把index = 1的二级副本服务器变更为主副本服务器
        chunkServers.remove(socket);
    }
}
