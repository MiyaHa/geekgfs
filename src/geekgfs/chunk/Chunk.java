package geekgfs.chunk;

import java.io.Serializable;

/**
 * GFS文件的基本单元--chunk实体类
 */

public class Chunk implements Serializable {

    //chunkID
    private long chunkID;
    //chunk 长度
    private int chunkSize;

    public Chunk(long chunkID, int chunkSize) {
        this.chunkID = chunkID;
        this.chunkSize = chunkSize;
    }

    public Chunk(Chunk chunk) {
        this.chunkID = chunk.getChunkID();
        this.chunkSize = chunk.getChunkSize();
    }

    public long getChunkID() {
        return chunkID;
    }

    public void setChunkID(long chunkID) {
        this.chunkID = chunkID;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    //获得chunk对应的文件的文件名
    public String getChunkName(){
        return "chunk_"+chunkID;
    }
}
