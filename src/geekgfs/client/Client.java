package geekgfs.client;

import geekgfs.entity.Chunk;
import geekgfs.protocol.ChunkServerProtocol;
import geekgfs.protocol.MasterProtocol;

import java.io.*;
import java.rmi.Naming;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 采用JAVA RMI进行远程通信
 */

public class Client {

    private int CHUNK_SIZE;
    //默认文件路径
    private String defaultFilePath = "";
    //master远程对象
    private MasterProtocol master;

    public Client(String socket) throws Exception {
        //获取master远程对象
        master = (MasterProtocol) Naming.lookup("rmi://" + socket + "/master");
        System.out.println("master connect");
        //获取chunk size
        CHUNK_SIZE = master.getDefaultChunkSize();
    }

    public void uploadFile(String fileName, String filePath) throws Exception {

        //上传文件名
        master.addFileName(fileName);

        int nowSize = 0, order = 0;
        byte[] buffer = new byte[CHUNK_SIZE];

        try (FileInputStream fis = new FileInputStream(filePath + fileName)) {
            while ((nowSize = fis.read(buffer, 0, CHUNK_SIZE)) != -1) {
                if (nowSize == CHUNK_SIZE) {
                    uploadChunk(fileName, CHUNK_SIZE, order++, buffer);
                } else {
                    byte[] bytes = new byte[nowSize];
                    System.arraycopy(buffer, 0, bytes, 0, nowSize);
                    uploadChunk(fileName, nowSize, order++, bytes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(String fileName) throws Exception {
        uploadFile(fileName, defaultFilePath);
    }

    public void uploadChunk(String fileName, int size, int order, byte[] stream) throws Exception {

        Map<String, Object> map = master.chunking(fileName, size, order);
        List<String> list = (List<String>) map.get("chunkservers");
        for (String socket:
             list) {
            ChunkServerProtocol chunkServer = (ChunkServerProtocol) Naming.lookup("rmi://" + socket + "/chunkserver");
            chunkServer.addChunk((Chunk) map.get("chunk"), stream);
        }
    }

    public void append(String fileName, byte[] stream) throws Exception {
        Map<String, Object> map = master.getLastChunk(fileName);
        Chunk chunk = (Chunk) map.get("chunk");
        List<String> list = (List<String>) map.get("chunkservers");
        int order = (int) map.get("order");

        if (chunk.getChunkSize() + stream.length <= CHUNK_SIZE) {
            for (String socket:
                 list) {
                ChunkServerProtocol chunkServer = (ChunkServerProtocol) Naming.lookup("rmi://" + socket + "/chunkserver");
                chunkServer.updateChunk(chunk, stream);
            }
            master.updateChunk(fileName, chunk.getChunkSize() + stream.length, order);
        } else {
            int len = CHUNK_SIZE - chunk.getChunkSize();
            byte[] appendBytes = new byte[len];
            System.arraycopy(stream, 0, appendBytes, 0, len);
            for (String socket:
                    list) {
                byte[] finalAppendBytes = appendBytes;
                ChunkServerProtocol chunkServer = (ChunkServerProtocol) Naming.lookup("rmi://" + socket + "/chunkserver");
                chunkServer.updateChunk(chunk, finalAppendBytes);
            }
            master.updateChunk(fileName, CHUNK_SIZE, order);

            while (len + CHUNK_SIZE < stream.length) {
                appendBytes = new byte[CHUNK_SIZE];
                System.arraycopy(stream, len, appendBytes, 0, CHUNK_SIZE);
                uploadChunk(fileName, CHUNK_SIZE, ++order, appendBytes);
                len += CHUNK_SIZE;
            }

            if (len != CHUNK_SIZE){
                int newLen = CHUNK_SIZE - len;
                appendBytes = new byte[newLen];
                System.arraycopy(stream, len, appendBytes, 0, newLen);
                uploadChunk(fileName, newLen, ++order, appendBytes);
            }
        }
    }

    public void deleteFile(String fileName, String filePath) throws Exception {
        master.deleteFile(filePath + fileName);
    }

    public void deleteFile(String fileName) throws Exception {
        deleteFile(fileName, defaultFilePath);
    }

    public void downloadFile(String fileName, String filePath) throws Exception {
        File file = new File(filePath + fileName);

        LinkedHashMap<Chunk, List<String>> map = (LinkedHashMap) master.getchunks(fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (Map.Entry<Chunk, List<String>> m : map.entrySet()) {
                //默认从主副本服务器下载
                ChunkServerProtocol chunkServer = (ChunkServerProtocol) Naming.lookup("rmi://" + m.getValue().get(0) + "/chunkserver");
                fos.write(chunkServer.getChunk(m.getKey()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String fileName) throws Exception {
        downloadFile(fileName, defaultFilePath);
    }

    public boolean exist(String fileName) throws Exception {
        return master.exist(fileName);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("master socket is: ");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String socket = bufferedReader.readLine();
        Client client = new Client(socket);
        while (true) {
            System.out.println("1: uploadFile, 2: downloadFile");
            String num = bufferedReader.readLine();
            System.out.println("fileName is:");
            String fileName = bufferedReader.readLine();
            switch (Integer.parseInt(num)) {
                case 1:
                    client.uploadFile(fileName);
                    System.out.println("upload success!");
                    break;
                case 2:
                    client.downloadFile(fileName);
                    System.out.println("download success!");
            }
        }
    }
}
