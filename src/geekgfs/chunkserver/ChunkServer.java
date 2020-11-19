package geekgfs.chunkserver;

import geekgfs.chunk.Chunk;
import geekgfs.protocol.CS2MasterProtocol;
import geekgfs.protocol.ChunkServerProtocol;
import geekgfs.protocol.Master2CSProtocol;

import java.io.*;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ChunkServer extends UnicastRemoteObject implements ChunkServerProtocol, Master2CSProtocol {

    private String masterSocket;
    private String serverSocket;

    private List<Long> chunkIDs;
    //记录每个chunk的hash
//    private Map<Long, String> chunkHash;

    //默认路径
    private String defaultFilePath = "";

    public ChunkServer(String masterSocket) throws Exception {
        serverSocket = InetAddress.getLocalHost().getHostAddress();
        this.masterSocket = masterSocket;
//        chunkHash = new ConcurrentHashMap<>();
        chunkIDs = new ArrayList<>();
    }

    @Override
    public void addChunk(Chunk chunk, byte[] stream) throws Exception{
        synchronized (chunkIDs){
            chunkIDs.add(chunk.getChunkID());
        }

        File file = new File(defaultFilePath+chunk.getChunkName());
        synchronized(ChunkServer.class){
            try(FileOutputStream fos = new FileOutputStream(file,true)){
                fos.write(stream);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("chunk: "+chunk.getChunkID()+"写入失败");
            }
        }
    }

    @Override
    public byte[] getChunk(Chunk chunk) throws Exception {
        File file = new File(defaultFilePath + chunk.getChunkName());
        byte[] buffer = new byte[chunk.getChunkSize()];
        try(FileInputStream fis = new FileInputStream(file)){
            fis.read(buffer);
        }catch (Exception e){
            e.printStackTrace();
        }
        return buffer;
    }

    @Override
    public void updateChunk(Chunk chunk, byte[] stream) throws Exception {
        File file = new File(defaultFilePath + chunk.getChunkName());
        byte[] buffer = new byte[chunk.getChunkSize() + stream.length];
        try(FileInputStream fis = new FileInputStream(file)){
            fis.read(buffer);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.arraycopy(stream, 0, buffer, chunk.getChunkSize(), stream.length);
        try(FileOutputStream fos = new FileOutputStream(file)){
            fos.write(buffer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //Master2CS
    @Override
    public void deleteChunk(Chunk chunk) {
        File file = new File(defaultFilePath+chunk.getChunkName());
        file.delete();
        synchronized (chunkIDs){
            try{
                chunkIDs.remove(chunk.getChunkID());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("masterSocket is: ");
        String masterSocket = bufferedReader.readLine();

        ChunkServer chunkServer = new ChunkServer(masterSocket);

        System.out.println("Port num:");
        String port = bufferedReader.readLine();
        chunkServer.serverSocket = chunkServer.serverSocket + ":" + port;

        LocateRegistry.createRegistry(Integer.parseInt(port));
        Naming.rebind("rmi://" + chunkServer.serverSocket + "/chunkserver", chunkServer);

        CS2MasterProtocol master = (CS2MasterProtocol) Naming.lookup("rmi://" + chunkServer.masterSocket + "/master");
        master.addChunkServer(chunkServer.serverSocket);
    }
}
