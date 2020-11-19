package geekgfs.master;

import geekgfs.chunk.Chunk;
import geekgfs.chunk.ChunkInfo;
import geekgfs.file.FileInfo;
import geekgfs.protocol.Master2CSProtocol;
import geekgfs.util.MyIDUtil;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 由master运行的文件管理系统
 */

public class FileManager {

    //所有文件信息，只保证文件名唯一，后续可以进行优化
    private Map<String, FileInfo> fileInfos;
    //chunkserver列表
    private List<String> servers;
    //每一个chunkserver对应的chunk列表
    private Map<String, List<ChunkInfo>> serverInfo;
    //IDUtil实例
    private MyIDUtil myIDUtil = MyIDUtil.getInstance();

    private static FileManager instance = new FileManager();

    public FileManager() {
        fileInfos = new ConcurrentHashMap<>();
        servers = new ArrayList<>();
        serverInfo = new LinkedHashMap<>();
    }

    public static FileManager getInstance() {
        return instance;
    }

    public void addFileName(String fileName) {
        fileInfos.put(fileName, new FileInfo(fileName));
    }

    public Map<String, Object> chunking(String fileName, int size, int order) {
        Map<String, Object> map = new HashMap<>();
        //使用generate()生成chunkID
        Chunk chunk = new Chunk(myIDUtil.generate(), size);
        ChunkInfo chunkInfo = new ChunkInfo(chunk, order);

        FileInfo fileInfo = fileInfos.get(fileName);
        fileInfo.addChunkInfo(chunkInfo);
        chunkInfo.setFileInfo(fileInfo);

        //返回chunk和chunkserver到client，可做服务器负载均衡，这里直接返回chunkserver列表中的第一个
        map.put("chunk", chunk);
        map.put("chunkserver", servers.get(0));
        chunkInfo.addChunkServer(servers.get(0));
        return map;
    }

    public void deleteFile(String fileName) {
        FileInfo fileInfo = fileInfos.get(fileName);
        List<ChunkInfo> chunkInfos = fileInfo.getChunkInfos();
        for (ChunkInfo ci : chunkInfos) {
            List<String> list = ci.getChunkServers();
            for (String socket : list) {
                new Thread(() -> {
                    try {
                        Master2CSProtocol m2CS = (Master2CSProtocol) Naming.lookup("rmi://" + socket + "/chunkserver");
                        m2CS.deleteChunk(ci.getChunk());
                        List<ChunkInfo> list1 = serverInfo.get(socket);
                        synchronized (list1) {
                            list1.remove(ci);
                        }
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        fileInfos.remove(fileName);
    }

    public Map<Chunk, List<String>> getChunks(String fileName) {
        Map<Chunk, List<String>> map = new LinkedHashMap<>();
        FileInfo fileInfo = fileInfos.get(fileName);
        List<ChunkInfo> chunkInfos = fileInfo.getChunkInfos();
        //按order进行排序
        Collections.sort(chunkInfos, new Comparator<ChunkInfo>() {
            @Override
            public int compare(ChunkInfo o1, ChunkInfo o2) {
                return o1.getOrder() > o2.getOrder() ? 1 : -1;
            }
        });
        for (ChunkInfo cki : chunkInfos){
            map.put(cki.getChunk(),cki.getChunkServers());
        }
        return map;
    }

    public Map getLastChunk(String fileName){
        FileInfo fileInfo = fileInfos.get(fileName);
        List<ChunkInfo> chunkInfos = fileInfo.getChunkInfos();

        //按order进行排序
        Collections.sort(chunkInfos, new Comparator<ChunkInfo>() {
            @Override
            public int compare(ChunkInfo o1, ChunkInfo o2) {
                return o1.getOrder() > o2.getOrder() ? 1 : -1;
            }
        });

        ChunkInfo chunkInfo = chunkInfos.get(chunkInfos.size() - 1);
        Map<String, Object> map = new HashMap<>();
        map.put("chunk", chunkInfo.getChunk());
        map.put("chunkservers", chunkInfo.getChunkServers());
        map.put("order", chunkInfo.getOrder());
        return map;
    }

    public void addChunkServer(String socket){
        servers.add(socket);
    }

    public void updateChunk(String fileName, int size, int order){
        System.out.println("  ");
    }
}
