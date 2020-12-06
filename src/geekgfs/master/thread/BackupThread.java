package geekgfs.master.thread;

import geekgfs.master.FileManager;

public class BackupThread extends Thread {

    private FileManager fileManager;

    public BackupThread(FileManager fileManager) {
        this.fileManager = fileManager;
        this.start();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(60 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            fileManager.backup();
        }
    }
}
