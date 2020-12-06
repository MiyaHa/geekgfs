package geekgfs.master;

import geekgfs.entity.Chunk;
import geekgfs.entity.ChunkInfo;
import geekgfs.entity.FileInfo;
import geekgfs.protocol.Master2CSProtocol;
import geekgfs.util.MyIDUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 由master运行的文件名称管理系统
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

    //备份路径
    private String backupPath = "";
    //备份版本
    private int baackupVer;

    private static FileManager instance = new FileManager();

     FileManager() {
        fileInfos = new ConcurrentHashMap<>();
        servers = new ArrayList<>();
        serverInfo = new LinkedHashMap<>();
        baackupVer = 0;
    }

     static FileManager getInstance() {
        return instance;
    }

     void addFileName(String fileName) {
        fileInfos.put(fileName, new FileInfo(fileName));
    }

     Map<String, Object> chunking(String fileName, int size, int order) {
        Map<String, Object> map = new HashMap<>();
        //使用generate()生成chunkID
        Chunk chunk = new Chunk(myIDUtil.generate(), size);
        ChunkInfo chunkInfo = new ChunkInfo(chunk, order);

        FileInfo fileInfo = fileInfos.get(fileName);
        fileInfo.addChunkInfo(chunkInfo);
        chunkInfo.setFileInfo(fileInfo);

        setChunkReplica(chunkInfo);
        map.put("chunk", chunk);
        map.put("chunkservers", chunkInfo.getChunkServers());
        return map;
    }

     void setChunkReplica(ChunkInfo chunkInfo){
        //为chunk分配chunkserver，此处简化处理，可做均衡负载
        if (servers.size() > 0){
            for (int i = 0; i < servers.size(); i++) {
                chunkInfo.addReplica(servers.get(i));
                if (i == 1)
                    break;
            }
        }
    }

     void deleteFile(String fileName) {
        FileInfo fileInfo = fileInfos.get(fileName);
        List<ChunkInfo> chunkInfos = fileInfo.getChunkInfos();
        for (ChunkInfo cki : chunkInfos) {
            List<String> list = cki.getChunkServers();
            for (String socket : list) {
                new Thread(() -> {
                    try {
                        Master2CSProtocol m2CS = (Master2CSProtocol) Naming.lookup("rmi://" + socket + "/chunkserver");
                        m2CS.deleteChunk(cki.getChunk());
                        List<ChunkInfo> list1 = serverInfo.get(socket);
                        synchronized (list1) {
                            list1.remove(cki);
                        }
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        fileInfos.remove(fileName);
    }

     Map<Chunk, List<String>> getChunks(String fileName) {
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
        for (ChunkInfo cki : chunkInfos) {
            map.put(cki.getChunk(), cki.getChunkServers());
        }
        return map;
    }

     Map getLastChunk(String fileName) {
        FileInfo fileInfo = fileInfos.get(fileName);
        List<ChunkInfo> chunkInfos = fileInfo.getChunkInfos();

        //获取order最大的chunkInfo
        ChunkInfo chunkInfo = chunkInfos.get(0);
        if (chunkInfos.size() > 1) {
            for (int i = 1; i < chunkInfos.size(); i++) {
                if (chunkInfos.get(i).getOrder() > chunkInfo.getOrder())
                    chunkInfo = chunkInfos.get(i);
            }
        }
//        Collections.sort(chunkInfos, new Comparator<ChunkInfo>() {
//            @Override
//             int compare(ChunkInfo o1, ChunkInfo o2) {
//                return o1.getOrder() > o2.getOrder() ? 1 : -1;
//            }
//        });
//        ChunkInfo chunkInfo = chunkInfos.get(chunkInfos.size() - 1);
        Map<String, Object> map = new HashMap<>();
        map.put("chunk", chunkInfo.getChunk());
        map.put("chunkservers", chunkInfo.getChunkServers());
        map.put("order", chunkInfo.getOrder());
        return map;
    }

     void addChunkServer(String socket) {
        servers.add(socket);
    }

     void updateChunk(String fileName, int size, int order) {
        FileInfo fileInfo = fileInfos.get(fileName);
        List<ChunkInfo> chunkInfos = fileInfo.getChunkInfos();
        for (ChunkInfo chunkInfo : chunkInfos) {
            if (chunkInfo.getOrder() == order) {
                chunkInfo.getChunk().setChunkSize(size);
            }
        }
    }

     boolean exist(String fileName){
        return fileInfos.containsKey(fileName);
    }

    //备份
    public void backup() {
        File file = new File(backupPath + "\\ver_" + ++baackupVer + ".backup");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(fileInfos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
