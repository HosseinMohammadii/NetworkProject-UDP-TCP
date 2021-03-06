import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class ChatServer implements Runnable {
    String address;
    int port;
    ServerSocket serverSocket;
    boolean clientConnected;
    private static volatile boolean exit = false;
    private int other_ID;

    public ChatServer() throws IOException {

        port = nextFreePort(3000,62000);
//        System.out.println("port selected is" + port);
        address =  InetAddress.getLocalHost().getHostAddress();
//        System.out.println("system address is " + address);
        serverSocket = new ServerSocket(port);
        System.out.println("Server Socket created successfully on port " + port);

    }

    public void set_other_ID(int i){
        other_ID = i;
    }

    public int getPort(){
        return port;
    }

    public int nextFreePort(int from, int to) {
        Random r = new Random();
        int port = r.nextInt((to-from)) + from;
        while (true) {
            if (isLocalPortFree(port)) {
                return port;
            } else {
                port = r.nextInt((to-from)) + from;
            }
        }
    }

    private boolean isLocalPortFree(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    @Override
    public void run() {
        clientConnected = false;
        Socket socket = null;
        try {
            serverSocket.setSoTimeout(6000);
            socket = serverSocket.accept();
            clientConnected = true;
            System.out.println("Client accepted.\nyou are going to chat with user < " + other_ID  + " > type some thing");
        } catch (IOException e) {
            System.out.println("couldn't to accept client.starting broadcasting and listening");
            Run.startBroadcast();
            Run.startListen();
            clientConnected = false;
        }


        StringBuilder sentence = new StringBuilder();
        String line = "";
        String ans = "";
        boolean flag = true;
        StringBuilder fullText = new StringBuilder();


        BufferedReader inFromUser = null;
        if(clientConnected) {
             inFromUser = new BufferedReader(new InputStreamReader(System.in));
        }

        DataOutputStream outToTaraf = null;
        if(clientConnected) {
            try {
                outToTaraf = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DataInputStream inFromTaraf = null;
        try {
            inFromTaraf = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            System.out.println("Server : Input data stream initialized correct");
        } catch (IOException e) {
            System.out.println("Server : Input data stream not not initialized correct");
//            e.printStackTrace();
        }
        //DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
        String in;
        Recieve recieve = new Recieve(inFromTaraf,"server");
        Thread t1 = new Thread(recieve,"RECEIVED");
        t1.start();


        while (!exit) {
            try {
                line = inFromUser.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line.equals("over") && !exit) {
                line = line + "\n";
                System.out.println("Closing and send close Mes to other person");
                try {
                    outToTaraf.writeBytes(line);
                } catch (IOException e) {
                    System.out.println("connection lost");
//                    e.printStackTrace();
                }
                recieve.stop();
                stop();
                System.out.println("stop receiving message");
                break;
            }

            line = line + "\n";
            if (!exit) {
                try {
                    outToTaraf.writeBytes(line);
                    System.out.print("sent: " + line);
                } catch (IOException e) {
//                e.printStackTrace();
                    System.out.println("connection lost");
                    exit =true;
                }
            }

        }

        System.out.println("finished getting new line.type last word to bye bye :)  (other client will not receive that)");
        try {
            socket.close();
            System.out.println("closed Server socket ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void stop() {
        exit = true;
    }
}
