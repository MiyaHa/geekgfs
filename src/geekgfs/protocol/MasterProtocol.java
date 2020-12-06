package geekgfs.protocol;

import geekgfs.entity.Chunk;

import java.rmi.Remote;
import java.util.List;
import java.util.Map;

public interface MasterProtocol extends Remote {

    //获取chunk size
    int getDefaultChunkSize() throws Exception;

    //添加文件名称
    void addFileName(String fileName) throws Exception;

    //上传准chunk信息，并返回chunk实体以及对应的chunkserver
    Map<String,Object> chunking(String fileName, int size, int order) throws Exception;

    //删除文件
    void deleteFile(String fileName) throws Exception;

    //下载文件，使用LinkedHashMap返回对应每个chunk的服务器位置
    Map<Chunk, List<String>> getchunks(String fileName) throws Exception;

    //获取最后一个chunk，用于文件追加
    Map<String, Object> getLastChunk(String fileName) throws Exception;

    //更新chunk
    void updateChunk(String fileName, int size, int order) throws Exception;

    boolean exist(String fileName) throws Exception;
}
