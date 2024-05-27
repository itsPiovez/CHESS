package com.example.chess;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Dashboard extends AppCompatActivity {
    private EditText roomCodeDisplay;
    private EditText roomCodeInput;
    private Button submitRoomCodeButton;
    private Button createRoomButton;
    private SocketManager socketManager;

    private String TypeGame = "";
    private String RoomCode = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inizializza SocketManager
        socketManager = SocketManager.getInstance(this);
        socketManager.connect(); // Connessione al server

        Button myButton = findViewById(R.id.GAME);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypeGame = "Offline";
                Intent intent = new Intent(Dashboard.this, Game.class);
                intent.putExtra("TypeGame", TypeGame);
                startActivity(intent);
            }
        });

        createRoomButton = findViewById(R.id.createRoomButton);
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypeGame = "Bianco";
                if (socketManager.isConnected()) {
                    socketManager.createRoom(); // Crea la stanza
                    String roomCode = socketManager.getIDRoom();
                    Intent intent = new Intent(Dashboard.this, Game.class);
                    intent.putExtra("TypeGame", TypeGame);
                    intent.putExtra("RoomCode", roomCode); // Passa il codice della stanza all'Activity Game
                    startActivity(intent);
                } else {
                    Toast.makeText(Dashboard.this, "Connection to server not active", Toast.LENGTH_SHORT).show();
                }
            }
        });


        roomCodeDisplay = findViewById(R.id.roomCodeDisplay);
        roomCodeInput = findViewById(R.id.roomCodeInput);
        submitRoomCodeButton = findViewById(R.id.submitRoomCodeButton);
        submitRoomCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomCode = roomCodeInput.getText().toString();
                if(roomCode.isEmpty()) {
                    Toast.makeText(Dashboard.this, "Room code cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    roomCodeDisplay.setText(roomCode);
                    TypeGame = "Nero";
                    if (socketManager.isConnected()) {
                        socketManager.joinRoom(roomCode); // Unisciti alla stanza
                        Intent intent = new Intent(Dashboard.this, Game1.class);
                        intent.putExtra("TypeGame", TypeGame);
                        intent.putExtra("RoomCode", roomCode); // Passa il codice della stanza all'Activity Game
                        startActivity(intent);
                    } else {
                        Toast.makeText(Dashboard.this, "Connection to server not active", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketManager.disconnect(); // Disconnessione dal server
    }
}
