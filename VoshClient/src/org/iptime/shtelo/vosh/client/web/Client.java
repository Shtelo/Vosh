package org.iptime.shtelo.vosh.client.web;

import org.iptime.shtelo.vosh.client.forms.ChatForm;
import org.iptime.shtelo.vosh.client.utils.Constants;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Client {
    private final String HOST;
    private final int PORT;
    private Socket socket;
    private ChatForm chatForm;

    private PrintStream printStream;
    private Scanner scanner;

    private ClientReceiveThread clientReceiveThread;
    private ClientVoiceReceiveThread clientVoiceReceiveThread;
    private ClientVoiceSendThread clientVoiceSendThread;

    private HashMap<String, double[]> positions;
    private double yaw;

    private boolean connected;

    private String name;

    public HashMap<String, double[]> getPositions() {
        return positions;
    }

    public Client(String name, Socket socket, String host, int port, ChatForm chatForm)
            throws IOException, LineUnavailableException {
        this.name = name;
        this.socket = socket;
        this.HOST = host;
        this.PORT = port;
        this.chatForm = chatForm;

        connected = true;

        printStream = new PrintStream(socket.getOutputStream());
        scanner = new Scanner(socket.getInputStream());

        send("NAME " + name);

        positions = new HashMap<>();
        yaw = 0;

        clientReceiveThread = new ClientReceiveThread(this);
        clientReceiveThread.start();
        clientVoiceReceiveThread = new ClientVoiceReceiveThread(this);
        clientVoiceReceiveThread.start();
        clientVoiceSendThread = new ClientVoiceSendThread(this);
        clientVoiceSendThread.start();
    }

    public void quit() {
        send("QUIT");
        setConnected(false);
    }

    public void send(String string) {
        printStream.println(string);
        if (!string.split(" ")[0].trim().equals(Constants.VOICE_PREFIX)) {
            chatForm.addLog("SERVER", "<-", string);
        }
    }

    @SuppressWarnings("unused")
    public void sendBytes(byte[] bytes) throws IOException {
        socket.getOutputStream().write(bytes);
    }

    public String receive() {
        if (scanner.hasNextLine()) {
            String data = scanner.nextLine();
            if (!data.split(" ")[0].trim().equals(Constants.VOICE_PREFIX)) {
                chatForm.addLog("SERVER", "->", data);
            }
            return data;
        }
        return null;
    }

    @SuppressWarnings("unused")
    public byte[] receiveBytes(int len) {
        try {
            return socket.getInputStream().readNBytes(len);
        } catch (IOException ignored) {
        }
        return null;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        chatForm.getUsernameLabel().setText(name);
        this.name = name;
    }

    public ClientVoiceReceiveThread getClientVoiceReceiveThread() {
        return clientVoiceReceiveThread;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getYaw() {
        return yaw;
    }
}
