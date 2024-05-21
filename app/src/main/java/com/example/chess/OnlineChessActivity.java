package com.example.chess;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.Random;

public class OnlineChessActivity extends AppCompatActivity {

    private Socket socket;
    private String playerColor;
    private EditText roomCodeDisplay;
    private EditText roomCodeInput;
    private Button submitRoomCodeButton;
    private Button createRoomButton;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        try {
            socket = IO.socket("http://localhost:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on("roomCreated", onRoomCreated);
        socket.on("roomJoined", onRoomJoined);
        socket.connect();

        createRoomButton = findViewById(R.id.createRoomButton);
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoom();
            }
        });

        roomCodeDisplay = findViewById(R.id.roomCodeDisplay);
        roomCodeInput = findViewById(R.id.roomCodeInput);
        submitRoomCodeButton = findViewById(R.id.submitRoomCodeButton);

        submitRoomCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomCode = roomCodeInput.getText().toString();
                roomCodeDisplay.setText(roomCode); // Aggiorna la casella di testo con il codice della stanza
                sendRoomCode(roomCode);
            }
        });
    }
    private void createRoom() {
        String roomId = generateUniqueRoomId();
        Log.d("MyTag", "posizione: " + roomId);
        sendRoomCode( roomId);
    }

    // Metodo per inviare il codice della stanza al server
    private void sendRoomCode(String roomCode) {
        JSONObject roomData = new JSONObject();
        try {
            roomData.put("roomId", roomCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("joinRoom", roomData); // Invia il codice della stanza al server, ad esempio per unirsi a una stanza esistente
    }

    private Emitter.Listener onConnect = args -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Connesso al server", Toast.LENGTH_SHORT).show());

    private Emitter.Listener onDisconnect = args -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Disconnesso dal server", Toast.LENGTH_SHORT).show());

    private Emitter.Listener onRoomCreated = args -> runOnUiThread(() -> {
        Toast.makeText(getApplicationContext(), "Stanza creata. Attendi il secondo giocatore...", Toast.LENGTH_SHORT).show();
    });

    private Emitter.Listener onRoomJoined = args -> runOnUiThread(() -> {
        Toast.makeText(getApplicationContext(), "Sei entrato nella stanza", Toast.LENGTH_SHORT).show();
    });

    private String generateUniqueRoomId() {
        long timestamp = System.currentTimeMillis();
        int randomNumber = new Random().nextInt(10000);
        return String.valueOf(timestamp) + randomNumber;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off("roomCreated", onRoomCreated);
        socket.off("roomJoined", onRoomJoined);
    }
}
