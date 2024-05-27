package com.example.chess;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Random;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {
    private static final String SERVER_URL = "http://192.168.178.147:3000";
    //private static final String SERVER_URL = "https://scacchi.5cimarcopiovesan.barsanti.edu.it/";
    private static SocketManager instance;
    private Socket socket;
    private Context context;
    private Handler handler;

    private SocketManager(Context context) {
        this.context = context.getApplicationContext();
        this.handler = new Handler(Looper.getMainLooper());
        initializeSocket();
    }

    public static synchronized SocketManager getInstance(Context context) {
        if (instance == null) {
            instance = new SocketManager(context);
        }
        return instance;
    }

    private void initializeSocket() {
        try {
            socket = IO.socket(SERVER_URL);
            registerSocketEvents();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e("SocketManager", "Error connecting to server: " + e.getMessage());
            handler.post(() -> Toast.makeText(context, "Unable to connect to the server", Toast.LENGTH_SHORT).show());
        }
    }

    private void registerSocketEvents() {
        if (socket != null) {
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on("roomCreated", onRoomCreated);
            socket.on("roomJoined", onRoomJoined);
            socket.on("opponentMove", onOpponentMove);
            socket.on("playerJoined", onPlayerJoined);
            socket.on("gameStart", onGameStart); // Aggiungi questo

        }
    }

    public void connect() {
        if (socket != null && !socket.connected()) {
            socket.connect();
        }
    }

    public static String idRoom;
    public static String getIDRoom(){
        return idRoom;
    }
    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }
    public void addPlayerLeftListener(Emitter.Listener listener) {
        socket.on("playerLeft", listener);
    }

    public boolean isConnected() {
        return socket != null && socket.connected();
    }

    public void createRoom() {
        String roomId = generateUniqueRoomId();
        String last5Chars = roomId.substring(roomId.length() - 5);
        sendRoomCode(last5Chars);
        idRoom = last5Chars;
    }

    public void joinRoom(String roomCode) {
        sendRoomCode(roomCode);
    }

    public void sendMoveToServer(String from, String to,String roomId) {
        JSONObject moveData = new JSONObject();
        try {
            moveData.put("from", from);
            moveData.put("to", to);
            moveData.put("roomId", roomId); // Aggiungi l'ID della stanza
            socket.emit("move", moveData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String generateUniqueRoomId() {
        long timestamp = System.currentTimeMillis();
        Random random = new Random();
        int randomNumber = random.nextInt(10000);
        return String.valueOf(timestamp) + randomNumber;
    }

    private void sendRoomCode(String roomCode) {
        if (isConnected()) {
            JSONObject roomData = new JSONObject();
            try {
                roomData.put("roomId", roomCode);
                socket.emit("joinRoom", roomData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            handler.post(() -> Toast.makeText(context, "Connection to server not active", Toast.LENGTH_SHORT).show());
        }
    }

    private Emitter.Listener onConnect = args -> {
        Log.d("SocketManager", "Connected to server");
        handler.post(() -> Toast.makeText(context, "Connected to server", Toast.LENGTH_SHORT).show());
    };

    private Emitter.Listener onDisconnect = args -> {
        Log.d("SocketManager", "Disconnected from server");
        handler.post(() -> Toast.makeText(context, "Disconnected from server", Toast.LENGTH_SHORT).show());
    };

    private Emitter.Listener onRoomCreated = args -> {
        Log.d("SocketManager", "Room created. Waiting for the second player...");
        handler.post(() -> Toast.makeText(context, "Room created. Waiting for the second player...", Toast.LENGTH_SHORT).show());
    };

    private Emitter.Listener onRoomJoined = args -> {
        Log.d("SocketManager", "Joined the room");
        handler.post(() -> Toast.makeText(context, "Joined the room", Toast.LENGTH_SHORT).show());
    };

    private Emitter.Listener onPlayerJoined = args -> {
        Log.d("SocketManager", "Another player has joined the room");
        handler.post(() -> Toast.makeText(context, "Another player has joined the room", Toast.LENGTH_SHORT).show());
    };
    public Socket getSocket() {
        return socket;
    }

    public Emitter.Listener onOpponentMove = args -> {
        Log.d("SocketManager", "Received opponent's move: " + args[0]);
        JSONObject moveData = (JSONObject) args[0];
        String from, to;
        try {
            from = moveData.getString("from");
            to = moveData.getString("to");
            // Notify the UI about the opponent's move.
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };
    public Emitter.Listener onGameStart = args -> {
        Log.d("SocketManager", "Game started");
        handler.post(() -> Toast.makeText(context, "Game started", Toast.LENGTH_SHORT).show());
    };
}
