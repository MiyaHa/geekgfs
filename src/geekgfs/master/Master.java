package geekgfs.master;

import geekgfs.chunk.Chunk;
import geekgfs.protocol.CS2MasterProtocol;
import geekgfs.protocol.MasterProtocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

public class Master extends UnicastRemoteObject implements MasterProtocol, CS2MasterProtocol {

    //masterIP
    private String socket;
    private FileManager fileManager;
    private static final int CHUNK_SIZE = 64*1024;

    public Master() throws Exception {
        socket = InetAddress.getLocalHost().getHostAddress();
        fileManager = FileManager.getInstance();
    }

    @Override
    public int getDefaultChunkSize() throws Exception {
        return CHUNK_SIZE;
    }

    @Override
    public void addFileName(String fileName) throws Exception {
        fileManager.addFileName(fileName);
        System.out.println(fileName + " has been added.");
    }

    @Override
    public Map<String, Object> chunking(String fileName, int size, int order) throws Exception {
        return fileManager.chunking(fileName, size, order);
    }

    @Override
    public void deleteFile(String fileName) throws Exception {
        fileManager.deleteFile(fileName);
        System.out.println(fileName+" has been deleted.");
    }

    @Override
    public Map<Chunk, List<String>> getchunks(String fileName) throws Exception {
        return fileManager.getChunks(fileName);
    }

    @Override
    public Map getLastChunk(String fileName) throws Exception {
        return fileManager.getLastChunk(fileName);
    }

    @Override
    public void updateChunk(String fileName, int size, int order) throws Exception {
        fileManager.updateChunk(fileName, size, order);
    }

    //CS2Master
    @Override
    public void addChunkServer(String socket) {
        fileManager.addChunkServer(socket);
        System.out.println("chunkserver: " + socket + "has joined.");
    }

    public static void main(String[] args) throws Exception {
        Master master = new Master();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Port num:");
        String port = bufferedReader.readLine();
        master.socket = master.socket + ":" + port;

        LocateRegistry.createRegistry(Integer.parseInt(port));
        Naming.rebind("rmi://" + master.socket + "/master", master);
        System.out.println("master socket: "+master.socket);
    }
}
