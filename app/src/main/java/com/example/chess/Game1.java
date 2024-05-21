package com.example.chess;

import static com.example.chess.chess.Color.WHITE;
import static com.example.chess.chess.Color.BLACK;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.chess.ChessMatch;
import com.example.chess.chess.ChessPiece;
import com.example.chess.chess.ChessPosition;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;


public class Game1 extends AppCompatActivity {
    private boolean WhiteTurn = true;
    private static int row;
    private static int col;
    private int colonna;
    private int riga;
    private String[][] chessBoard = {
            {"a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"},
            {"a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7"},
            {"a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6"},
            {"a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5"},
            {"a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4"},
            {"a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3"},
            {"a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2"},
            {"a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"}
    };
    private String[][] pedine = {
            {"br", "bn", "bb", "bq", "bk", "bb", "bn", "br"},
            {"bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"},
            {"wr", "wn", "wb", "wq", "wk", "wb", "wn", "wr"},
    };
    private String[][] pedineBlack = {
            {"wr", "wn", "wb", "wq", "wk", "wb", "wn", "wr"},
            {"wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"},
            {"br", "bn", "bb", "bq", "bk", "bb", "bn", "br"},
    };
    private String[][] chessBoardBlack = {
            {"a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"},
            {"a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2"},
            {"a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3"},
            {"a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4"},
            {"a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5"},
            {"a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6"},
            {"a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7"},
            {"a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"}

    };
    private ChessMatch chessMatch = new ChessMatch();
    private TextView textViewGame;
    private TextView textViewRoomCode;
    private SocketManager socketManager;
    private String idRoom;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ImageButton buttonBack = findViewById(R.id.button3);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Game1.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });

        ImageButton buttonHome = findViewById(R.id.button);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Game1.this, Login.class);
                startActivity(intent);
                finish();
            }
        });



        moves = new ArrayList<>();
        textViewGame = findViewById(R.id.TypeGame);
        textViewRoomCode = findViewById(R.id.RoomCode);
        Intent intent = getIntent();
        String typeGame = intent.getStringExtra("TypeGame");
        String roomCode = intent.getStringExtra("RoomCode");
        idRoom = roomCode;

        socketManager = SocketManager.getInstance(this);
        socketManager.getSocket().on("opponentMove", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject moveData = (JSONObject) args[0];
                String from = null;
                String to = null;
                try {
                    from = moveData.getString("from");
                    to = moveData.getString("to");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                executeMove(from, to);
            }
        });


        Log.d("MyTag", "posizione: " + typeGame);
        Log.d("MyTag", "posizione: " + roomCode);

        if(typeGame.equals("Online1")||typeGame.equals("Online2")){
            textViewGame.setText("Tipo di partita: " + typeGame);
            textViewRoomCode.setText("Codice stanza: " + roomCode);
        }
        else{
            textViewGame.setText("Tipo di partita: " + typeGame);
        }

        for (riga = 0; riga < chessBoardBlack.length; riga++) {
            for (colonna = 0; colonna < chessBoardBlack[row].length; colonna++) {
                String buttonId = "button_" + chessBoardBlack[riga][colonna];
                int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
                ImageButton button = findViewById(resId);
                button.setTag(pedineBlack[riga][colonna]);
                Log.d("MyTag", "posizione: " + pedineBlack[riga][colonna]);
                button.setOnClickListener(v -> handleButtonClick(button));
            }
        }
    }

    private boolean[][] possibleMoves;
    private ImageButton selectedButton;
    private String tag="";
    private List <String> moves;
    private ChessPosition source;
    private ChessPosition target;
    private int posizioneRigaPartenza;
    private int posizioneColonnaPartenza;
    private String posizioneIniziale;
    private ImageButton selectedButton1;
    private List<ChessPiece> capturedBlack = new ArrayList<>();
    private List<ChessPiece> capturedWhite = new ArrayList<>();
    private boolean check ;
    private boolean checkMate;
    private boolean canMove = false;
    private void handleButtonClick(ImageButton button) {
        if (selectedButton == null) {
            selectedButton = button;
            tag = button.getTag().toString();
            int posizione = button.getId();
            String posizionescacchi = translateNumber(posizione);

            if(tag!="") {
                if (TurnChecker.canMove(tag, WhiteTurn)&&canMove) {
                    posizione = button.getId();
                    posizionescacchi = translateNumber(posizione);
                    posizioneIniziale = posizionescacchi;

                    char colona = posizionescacchi.charAt(0);
                    int rig = Integer.parseInt(posizionescacchi.substring(1));
                    source = new ChessPosition((char) (colona), rig);
                    possibleMoves = chessMatch.possibleMoves(source);

                    posizioneColonnaPartenza = col;
                    posizioneRigaPartenza = row;
                    selectedButton1 = button;
                }
                else {
                    selectedButton = null;
                    tag= null;
                }
            }
            else {
                selectedButton = null;
                tag= null;
            }
        } else {

            List<int[]> possibleMoveCoordinates = getPossibleMovesCoordinates(possibleMoves);
            for (int[] coordinates : possibleMoveCoordinates) {
                int i = 0;
                Log.d("MyTag", "posizione: " + coordinates[i] + " " + coordinates[i+1]);
                i++;
            }
            int posizione1 = button.getId();
            String posizionescacchi = translateNumber(posizione1);
            char colona = posizionescacchi.charAt(0);
            int rig = Integer.parseInt(posizionescacchi.substring(1));
            target = new ChessPosition((char) (colona), rig);
            ChessPiece capturedPiece = null;
            //controllo la colonna e la riga
            boolean validMove = false;
            for (int[] coordinates : possibleMoveCoordinates) {
                int i = 0;
                Log.d("MyTag", "mossa0 " + coordinates[i] + " " + coordinates[i+1]);
                if (coordinates[i] == col && coordinates[i+1] == row) {
                    validMove = true;
                    break;
                }
                i++;
            }
            Log.d("MyTag", "mossa1 " + validMove);
            //controllo validità in caso di scacco
            if(!WhiteTurn){
                check = chessMatch.testCheck(BLACK);
            }
            else{
                check = chessMatch.testCheck(WHITE);
            }
            if(check){
                validMove = false;
            }


            if (selectedButton.getBackground() != null && !(selectedButton.getBackground() instanceof ColorDrawable && ((ColorDrawable) selectedButton.getBackground()).getColor() == Color.TRANSPARENT)  && validMove) {
                Drawable backgroundImage = selectedButton.getBackground();
                selectedButton.setBackground(null);
                button.setBackground(backgroundImage);

                button.setTag(tag);

                pedine[col][row] = tag;
                moves.add(posizioneIniziale + " " + tag);
                moves.add(posizionescacchi + " " + tag);
                String from = posizioneIniziale+ " " + tag;
                String to = posizionescacchi + " " + tag;
                socketManager.sendMoveToServer(from, to,idRoom);

                Log.d("MyTag", "Mosse: " + moves);
                selectedButton = null;
                tag = null;

                selectedButton1.setTag("");
                pedine[posizioneColonnaPartenza][posizioneRigaPartenza] = "";
                capturedPiece = chessMatch.performChessMove(source, target);

                if (capturedPiece != null) {
                    if (WhiteTurn) {
                        capturedBlack.add(capturedPiece);
                    } else {
                        capturedWhite.add(capturedPiece);
                    }
                } else {
                    selectedButton = null;
                    tag = null;
                }
                Log.d("MyTag", "Pedine Prese dai Bianchi: " + capturedBlack);
                Log.d("MyTag", "Pedine Prese dai Neri: " + capturedWhite);


                if(!WhiteTurn) {
                    check = chessMatch.testCheck(WHITE);
                    if (check) {
                        checkMate = chessMatch.testCheckMate(WHITE);
                        if (checkMate) {
                            Log.d("MyTag", "Scacco Matto Re Bianco");
                            Log.d("MyTag", "Vittoria Neri");
                            Toast.makeText(this, "Scacco Matto Re Bianco", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.d("MyTag", "Scacco Re Bianco");
                            Toast.makeText(this, "Scacco Re Bianco", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    check = chessMatch.testCheck(BLACK);
                    if (check) {
                        checkMate = chessMatch.testCheckMate(BLACK);
                        if (checkMate) {
                            Log.d("MyTag", "Scacco Matto Re Nero");
                            Log.d("MyTag", "Vittoria Bianchi");
                            Toast.makeText(this, "Scacco Matto Re Nero", Toast.LENGTH_SHORT).show();

                        }
                        else {
                            Log.d("MyTag", "Scacco Re Nero");
                            Toast.makeText(this, "Scacco Re Nero", Toast.LENGTH_SHORT).show();
                        }

                    }
                    Log.d("MyTag", "Check: " + check);
                    Log.d("MyTag", "Check Mate: " + checkMate);
                }

                WhiteTurn = !WhiteTurn;
                canMove = false;
                for (int i = 0; i < 8; i++) {
                    String riga = "";
                    for (int j = 0; j < 8; j++) {
                        riga = riga + pedine[i][j] + " ";
                        if (pedine[i][j] == "") {
                            riga = riga + "  ";
                        }
                    }
                    Log.d("MyTag", "pedina: " + riga);
                }
                Log.d("MyTag", "Scacco " + check);
            }
            else{
                selectedButton = null;
                tag= null;
            }
        }
    }


    private void executeMove(String from, String to) {
        runOnUiThread(() -> {
            String buttonFrom = "button_" + from.substring(0, 2);
            String buttonTo = "button_" + to.substring(0, 2);
            String tagN = from.substring(3);

            int resId = getResources().getIdentifier(buttonFrom, "id", getPackageName());
            ImageButton button = findViewById(resId);
            Drawable backgroundImage = button.getBackground();
            button.setBackground(null);
            button.setTag("");

            resId = getResources().getIdentifier(buttonTo, "id", getPackageName());
            button = findViewById(resId);
            button.setBackground(backgroundImage);
            button.setTag(tagN);

            char colona0 = from.charAt(0);
            int rig0 = from.charAt(1)- '0';
            Log.d( "MyTag", "colona0: " + colona0 + " rig0: " + rig0);
            source = new ChessPosition(colona0, rig0);

            char colona = to.charAt(0);
            int rig = to.charAt(1)- '0';
            Log.d( "MyTag", "colona: " + colona + " rig: " + rig);
            target = new ChessPosition(colona, rig);
            ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
            canMove=true;
            WhiteTurn = !WhiteTurn;
        });
    }

    public List<int[]> getPossibleMovesCoordinates(boolean[][] possibleMoves) {
        List<int[]> coordinates = new ArrayList<>();

        // Scorrere la matrice possibleMoves
        for (int row1 = 0; row1 < possibleMoves.length; row1++) {
            for (int col1 = 0; col1 < possibleMoves[row1].length; col1++) {
                // Se la posizione (row, col) è una mossa possibile, aggiungi le coordinate alla lista
                if (possibleMoves[row1][col1]) {
                    int[] coordinate = {row1, col1};
                    coordinates.add(coordinate);
                }
            }
        }

        return coordinates;
    }


    public static String translateNumber(int id) {
        String idString = Integer.toString(id);
        String lastTwoDigits = idString.substring(idString.length() - 3);
        Log.d("MyTag", "Last Two Digits: " + lastTwoDigits);
        int number = Integer.parseInt(lastTwoDigits);
        int translatedNumber = number - 827;
        String suffix;
        switch (translatedNumber) {
            case 1:
                col=7;
                suffix = "a";
                row=0;
                break;
            case 2:
                col=6;
                suffix = "a";
                row=0;
                break;
            case 3:
                col=5;
                suffix = "a";
                row=0;
                break;
            case 4:
                col=4;
                suffix = "a";
                row=0;
                break;
            case 5:
                col=3;
                suffix = "a";
                row=0;
                break;
            case 6:
                col=2;
                suffix = "a";
                row=0;
                break;
            case 7:
                col=1;
                suffix = "a";
                row=0;
                break;
            case 8:
                col=0;
                suffix = "a";
                row=0;
                break;
            case 9:
                col=7;
                suffix = "b";
                translatedNumber = translatedNumber - 8;
                row=1;
                break;
            case 10:
                col=6;
                suffix = "b";
                translatedNumber = translatedNumber - 8;
                row=1;
                break;
            case 11:
                col=5;
                suffix = "b";
                translatedNumber = translatedNumber - 8;
                row=1;
                break;
            case 12:
                col=4;
                suffix = "b";
                translatedNumber = translatedNumber - 8;
                row=1;
                break;
            case 13:
                col=3;
                suffix = "b";
                translatedNumber = translatedNumber - 8;
                row=1;
                break;
            case 14:
                col=2;
                suffix = "b";
                translatedNumber = translatedNumber - 8;
                row=1;
                break;
            case 15:
                col=1;
                suffix = "b";
                translatedNumber = translatedNumber - 8;
                row=1;
                break;
            case 16:
                col=0;
                suffix = "b";
                translatedNumber = translatedNumber - 8;
                row=1;
                break;
            case 17:
                col=7;
                suffix = "c";
                translatedNumber = translatedNumber - 16;
                row=2;
                break;
            case 18:
                col=6;
                suffix = "c";
                translatedNumber = translatedNumber - 16;
                row=2;
                break;
            case 19:
                col=5;
                suffix = "c";
                translatedNumber = translatedNumber - 16;
                row=2;
                break;
            case 20:
                col=4;
                suffix = "c";
                translatedNumber = translatedNumber - 16;
                row=2;
                break;
            case 21:
                col=3;
                suffix = "c";
                translatedNumber = translatedNumber - 16;
                row=2;
                break;
            case 22:
                col=2;
                suffix = "c";
                translatedNumber = translatedNumber - 16;
                row=2;
                break;
            case 23:
                col=1;
                suffix = "c";
                translatedNumber = translatedNumber - 16;
                row=2;
                break;
            case 24:
                col=0;
                suffix = "c";
                translatedNumber = translatedNumber - 16;
                row=2;
                break;
            case 25:
                col=7;
                suffix = "d";
                translatedNumber = translatedNumber - 24;
                row=3;
                break;
            case 26:
                col=6;
                suffix = "d";
                translatedNumber = translatedNumber - 24;
                row=3;
                break;
            case 27:
                col=5;
                suffix = "d";
                translatedNumber = translatedNumber - 24;
                row=3;
                break;
            case 28:
                col=4;
                suffix = "d";
                translatedNumber = translatedNumber - 24;
                row=3;
                break;
            case 29:
                col=3;
                suffix = "d";
                translatedNumber = translatedNumber - 24;
                row=3;
                break;
            case 30:
                col=2;
                suffix = "d";
                translatedNumber = translatedNumber - 24;
                row=3;
                break;
            case 31:
                col=1;
                suffix = "d";
                translatedNumber = translatedNumber - 24;
                row=3;
                break;
            case 32:
                col=0;
                suffix = "d";
                translatedNumber = translatedNumber - 24;
                row=3;
                break;
            case 33:
                col= 7;
                suffix = "e";
                translatedNumber = translatedNumber - 32;
                row=4;
                break;
            case 34:
                col= 6;
                suffix = "e";
                translatedNumber = translatedNumber - 32;
                row=4;
                break;
            case 35:
                col= 5;
                suffix = "e";
                translatedNumber = translatedNumber - 32;
                row=4;
                break;
            case 36:
                col= 4;
                suffix = "e";
                translatedNumber = translatedNumber - 32;
                row=4;
                break;
            case 37:
                col= 3;
                suffix = "e";
                translatedNumber = translatedNumber - 32;
                row=4;
                break;
            case 38:
                col= 2;
                suffix = "e";
                translatedNumber = translatedNumber - 32;
                row=4;
                break;
            case 39:
                col= 1;
                suffix = "e";
                translatedNumber = translatedNumber - 32;
                row=4;
                break;
            case 40:
                col= 0;
                suffix = "e";
                translatedNumber = translatedNumber - 32;
                row=4;
                break;
            case 41:
                col=7;
                suffix = "f";
                translatedNumber = translatedNumber - 40;
                row=5;
                break;
            case 42:
                col=6;
                suffix = "f";
                translatedNumber = translatedNumber - 40;
                row=5;
                break;
            case 43:
                col=5;
                suffix = "f";
                translatedNumber = translatedNumber - 40;
                row=5;
                break;
            case 44:
                col=4;
                suffix = "f";
                translatedNumber = translatedNumber - 40;
                row=5;
                break;
            case 45:
                col=3;
                suffix = "f";
                translatedNumber = translatedNumber - 40;
                row=5;
                break;
            case 46:
                col=2;
                suffix = "f";
                translatedNumber = translatedNumber - 40;
                row=5;
                break;
            case 47:
                col=1;
                suffix = "f";
                translatedNumber = translatedNumber - 40;
                row=5;
                break;
            case 48:
                col=0;
                suffix = "f";
                translatedNumber = translatedNumber - 40;
                row=5;
                break;
            case 49:
                col=7;
                suffix = "g";
                translatedNumber = translatedNumber - 48;
                row=6;
                break;
            case 50:
                col=6;
                suffix = "g";
                translatedNumber = translatedNumber - 48;
                row=6;
                break;
            case 51:
                col=5;
                suffix = "g";
                translatedNumber = translatedNumber - 48;
                row=6;
                break;
            case 52:
                col=4;
                suffix = "g";
                translatedNumber = translatedNumber - 48;
                row=6;
                break;
            case 53:
                col=3;
                suffix = "g";
                translatedNumber = translatedNumber - 48;
                row=6;
                break;
            case 54:
                col=2;
                suffix = "g";
                translatedNumber = translatedNumber - 48;
                row=6;
                break;
            case 55:
                col=1;
                suffix = "g";
                translatedNumber = translatedNumber - 48;
                row=6;
                break;
            case 56:
                col=0;
                suffix = "g";
                translatedNumber = translatedNumber - 48;
                row=6;
                break;
            case 57:
                col=7;
                suffix = "h";
                translatedNumber = translatedNumber - 56;
                row=7;
                break;
            case 58:
                col=6;
                suffix = "h";
                translatedNumber = translatedNumber - 56;
                row=7;
                break;
            case 59:
                col=5;
                suffix = "h";
                translatedNumber = translatedNumber - 56;
                row=7;
                break;
            case 60:
                col=4;
                suffix = "h";
                translatedNumber = translatedNumber - 56;
                row=7;
                break;
            case 61:
                col=3;
                suffix = "h";
                translatedNumber = translatedNumber - 56;
                row=7;
                break;
            case 62:
                col=2;
                suffix = "h";
                translatedNumber = translatedNumber - 56;
                row=7;
                break;
            case 63:
                col=1;
                suffix = "h";
                translatedNumber = translatedNumber - 56;
                row=7;
                break;
            case 64:
                col=0;
                suffix = "h";
                translatedNumber = translatedNumber - 56;
                row=7;
                break;
            default:
                return null;
        }

        Log.d("MyTag", "Translated Number: " + translatedNumber);
        Log.d("MyTag", "Col: " + col);
        Log.d("MyTag", "Row: " + row);

        // Return the translated number with the suffix.
        return suffix+translatedNumber ;
    }
}
