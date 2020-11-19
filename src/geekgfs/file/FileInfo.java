package geekgfs.file;

import geekgfs.chunk.ChunkInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 每个file对应一个List<ChunkInfo>
 */

public class FileInfo {

    private String fileName;
    private List<ChunkInfo> chunkInfos;

    public FileInfo(String fileName) {
        this.fileName = fileName;
        this.chunkInfos = new ArrayList<>();
    }

    public FileInfo(String fileName, List<ChunkInfo> chunkInfos) {
        this.fileName = fileName;
        this.chunkInfos = chunkInfos;
    }

    public void addChunkInfo(ChunkInfo chunkInfo) {
        this.chunkInfos.add(chunkInfo);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<ChunkInfo> getChunkInfos() {
        return chunkInfos;
    }

    public void setChunkInfos(List<ChunkInfo> chunkInfos) {
        this.chunkInfos = chunkInfos;
    }
}
