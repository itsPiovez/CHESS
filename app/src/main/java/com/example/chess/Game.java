package com.example.chess;

import static com.example.chess.chess.Color.WHITE;
import static com.example.chess.chess.Color.BLACK;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import androidx.gridlayout.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.boardgame.Position;
import com.example.chess.chess.ChessMatch;
import com.example.chess.chess.ChessPiece;
import com.example.chess.chess.ChessPosition;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;


public class Game extends AppCompatActivity {
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
    private ChessMatch chessMatch = new ChessMatch(this);
    private TextView textViewGame;
    private TextView textViewRoomCode;
    private SocketManager socketManager;
    private String idRoom;
    private String tipoPartita;

    private Boolean butt=true;
    private Button turnIndicator;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        turnIndicator = findViewById(R.id.button6);
        ImageButton buttonBack = findViewById(R.id.button5);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //devo impostare che lascio la partita
                SocketManager.getInstance(Game.this).disconnect();

                Intent intent = new Intent(Game.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });

        ImageButton buttonHome = findViewById(R.id.button);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //devo impostare che lascio la partita
                SocketManager.getInstance(Game.this).disconnect();

                Intent intent = new Intent(Game.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        ImageButton buttonSettings = findViewById(R.id.button3);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(butt){
                    buttonHome.setVisibility(View.VISIBLE);
                    buttonBack.setVisibility(View.VISIBLE);
                    butt=false;
                }else{
                    buttonHome.setVisibility(View.INVISIBLE);
                    buttonBack.setVisibility(View.INVISIBLE);
                    butt=true;
                }
            }
        });

        moves = new ArrayList<>();
        textViewGame = findViewById(R.id.TypeGame);
        textViewRoomCode = findViewById(R.id.RoomCode);
        Intent intent = getIntent();
        String typeGame = intent.getStringExtra("TypeGame");
        String roomCode = intent.getStringExtra("RoomCode");
        idRoom = roomCode;
        tipoPartita= typeGame;

        socketManager = SocketManager.getInstance(this);

        socketManager.getSocket().on("gameStart", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        canMove = true;
                        Toast.makeText(Game.this, "Game started", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        SocketManager.getInstance(this).addPlayerLeftListener(new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // The other player has left the game
                        // Update the UI accordingly
                        Toast.makeText(Game.this, "Game WIN", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
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

        // Get screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Calculate button size
        int buttonSize = screenWidth / 8;

        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            ImageButton button = (ImageButton) gridLayout.getChildAt(i);
            ViewGroup.LayoutParams params = button.getLayoutParams();
            params.width = buttonSize;
            params.height = buttonSize;
            button.setLayoutParams(params);
        }

        Log.d("MyTag", "posizione: " + typeGame);
        Log.d("MyTag", "posizione: " + roomCode);

        if(typeGame.equals("Bianco")){
            textViewGame.setText("Utente: " + typeGame);
            textViewRoomCode.setText("Codice stanza: " + roomCode);
        }
        else{
            textViewGame.setText("Tipo di partita: " + typeGame);
            canMove = true;
        }

        for (riga = 0; riga < chessBoard.length; riga++) {
            for (colonna = 0; colonna < chessBoard[row].length; colonna++) {
                String buttonId = "button_" + chessBoard[riga][colonna];
                int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
                ImageButton button = findViewById(resId);
                button.setTag(pedine[riga][colonna]);
                Log.d("MyTag", "posizione: " + pedine[riga][colonna]);
                button.setOnClickListener(v -> {
                    if(v instanceof ImageButton) {
                        buttonID=getResources().getResourceEntryName(v.getId());
                        handleButtonClick((ImageButton) v);
                    }
                });
            }
        }
    }
    private String buttonID;
    public int[] chessToMatrixPosition(String chessPosition) {
        int[] matrixPosition = new int[2];
        matrixPosition[0] = chessPosition.charAt(0) - 'a'; // a = 7, b = 6, c = 5, d = 4, e = 3, f = 2, g = 1, h = 0
        matrixPosition[1] = '8'-chessPosition.charAt(1) ; // 1 = 0, 2 = 1, 3 = 2, 4 = 3, 5 = 4, 6 = 5, 7 = 6, 8 = 7
        Log.d("DCCC", "posizione: " + matrixPosition[0] + " " + matrixPosition[1]);
        col = matrixPosition[1];
        row = matrixPosition[0];
        return matrixPosition;
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
    private ChessPiece capturedPiece = null;

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
            if(tag!="") {
                if (TurnChecker.canMove(tag, WhiteTurn)&&canMove) {
                    String posizionescacchi = "";
                    if(buttonID!=null){
                    posizionescacchi = buttonID;
                    posizionescacchi = posizionescacchi.substring(7);
                    Log.d("MyTag", "posizione: " + posizionescacchi);
                    chessToMatrixPosition(posizionescacchi);
                    }
                    posizioneIniziale = posizionescacchi;

                    char colona = posizionescacchi.charAt(0);
                    int rig = Integer.parseInt(posizionescacchi.substring(1));
                    source = new ChessPosition((char) (colona), rig);
                    possibleMoves = chessMatch.possibleMoves(source);

                    posizioneColonnaPartenza = col;
                    posizioneRigaPartenza = row;
                    selectedButton1 = button;
                    highlightPossibleMoves(possibleMoves);
                }
                else {
                    removeHighlightFromPossibleMoves();
                    selectedButton = null;
                    tag= null;
                }
            }
            else {
                removeHighlightFromPossibleMoves();
                selectedButton = null;
                tag= null;
            }
        }
        else {

            List<int[]> possibleMoveCoordinates = getPossibleMovesCoordinates(possibleMoves);
            for (int[] coordinates : possibleMoveCoordinates) {
                int i = 0;
                Log.d("MyTag", "posizione: " + coordinates[i] + " " + coordinates[i+1]);
                i++;
            }
            String posizionescacchi = buttonID;
            posizionescacchi = posizionescacchi.substring(7);
            chessToMatrixPosition(posizionescacchi);

            char colona = posizionescacchi.charAt(0);
            int rig = Integer.parseInt(posizionescacchi.substring(1));
            target = new ChessPosition((char) (colona), rig);
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
            if (!validMove) {
                selectedButton = null;
                tag = null;
                removeHighlightFromPossibleMoves();
                Toast.makeText(this, "Invalid move. Please select a valid location.", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("MyTag", "mossa1 " + validMove);
            //controllo validità in caso di scacco
            if (!WhiteTurn) {
                capturedPiece = chessMatch.performChessMove(source, target);
                check = chessMatch.testCheck(BLACK);

                Position opponentPiecePosition = new Position(row, col); // sostituisci row e column con le coordinate del pezzo avversario
                List<Position> defensiveMoves = chessMatch.findDefensiveMoves(opponentPiecePosition, BLACK);
            }
            else{
                capturedPiece = chessMatch.performChessMove(source, target);
                check = chessMatch.testCheck(WHITE);

                Position opponentPiecePosition = new Position(row, col); // sostituisci row e column con le coordinate del pezzo avversario
                List<Position> defensiveMoves = chessMatch.findDefensiveMoves(opponentPiecePosition, WHITE);
            }
            if(check){
                validMove = false;
            }
            Toast.makeText(this, "Scacco " + check, Toast.LENGTH_SHORT).show();

            if (selectedButton.getForeground() != null && validMove ==true ) {
                Drawable backgroundImage = selectedButton.getForeground();
                selectedButton.setForeground(null);
                button.setForeground(backgroundImage);
                removeHighlightFromPossibleMoves();

                button.setTag(tag);

                pedine[col][row] = tag;
                moves.add(posizioneIniziale + " " + tag);
                moves.add(posizionescacchi + " " + tag);
                String from = posizioneIniziale+ " " + tag;
                String to = posizionescacchi + " " + tag;
                handleArrocco(from,to);
                socketManager.sendMoveToServer(from, to,idRoom);

                Log.d("MyTag", "Mosse: " + moves);
                selectedButton = null;
                tag = null;

                selectedButton1.setTag("");
                pedine[posizioneColonnaPartenza][posizioneRigaPartenza] = "";

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


                chessHandle();
                if(tipoPartita.equals("Bianco")){
                    canMove = false;
                }

                WhiteTurn = !WhiteTurn;
                updateTurnIndicator();
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
        }
    }


    private void executeMove(String from, String to) {
        runOnUiThread(() -> {
            String buttonFrom = "button_" + from.substring(0, 2);
            String buttonTo = "button_" + to.substring(0, 2);
            String tagN = from.substring(3);

            int resId = getResources().getIdentifier(buttonFrom, "id", getPackageName());
            ImageButton button = findViewById(resId);
            Drawable backgroundImage = button.getForeground();
            button.setForeground(null);
            button.setTag("");

            resId = getResources().getIdentifier(buttonTo, "id", getPackageName());
            button = findViewById(resId);
            button.setForeground(backgroundImage);
            button.setTag(tagN);

            char colona0 = from.charAt(0);
            int rig0 = from.charAt(1)- '0';
            Log.d( "MyTag", "colona0: " + colona0 + " rig0: " + rig0);
            source = new ChessPosition(colona0, rig0);

            char colona = to.charAt(0);
            int rig = to.charAt(1)- '0';
            Log.d( "MyTag", "colona: " + colona + " rig: " + rig);
            target = new ChessPosition(colona, rig);
            handleArrocco(from,to);
            chessMatch.performChessMove(source, target);
            canMove=true;
            chessHandle();
            WhiteTurn = !WhiteTurn;
            updateTurnIndicator();
            chessHandle();

        });
    }

    private void highlightPossibleMoves(boolean[][] possibleMoves) {
        for (int i = 0; i < possibleMoves.length; i++) {
            for (int j = 0; j < possibleMoves[i].length; j++) {
                if (possibleMoves[i][j]) {
                    String buttonId = "button_" + chessBoard[i][j];
                    int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
                    ImageButton button = findViewById(resId);
                    ColorDrawable buttonColor = (ColorDrawable) button.getBackground();
                    int colorId = buttonColor.getColor();
                    GradientDrawable border = new GradientDrawable();
                    border.setColor(colorId);
                    border.setStroke(6, Color.WHITE); // Imposta lo spessore del bordo a 2 pixel e il colore a giallo
                    button.setBackground(border);
                }
            }
        }
    }


    private void removeHighlightFromPossibleMoves() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[i].length; j++) {
                    String buttonId = "button_" + chessBoard[i][j];
                    int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
                    ImageButton button = findViewById(resId);
                    if ((i + j) % 2 == 0) {
                        button.setBackgroundColor(Color.parseColor("#EED7B3"));
                    } else {
                        button.setBackgroundColor(Color.parseColor("#B38663"));
                    }
                }
        }
    }
    private void updateTurnIndicator() {
        if (WhiteTurn) {
            turnIndicator.setText("White Turn");
            turnIndicator.setBackgroundColor(Color.WHITE); // Imposta lo sfondo bianco
            turnIndicator.setTextColor(Color.BLACK); // Imposta il colore del testo nero
        } else {
            turnIndicator.setText("Black Turn");
            turnIndicator.setBackgroundColor(Color.BLACK); // Imposta lo sfondo nero
            turnIndicator.setTextColor(Color.WHITE); // Imposta il colore del testo bianco
        }
    }
    public void handleArrocco(String from, String to) {
        if (from.equals("e1 wk") && to.equals("g1 wk")) {
            String buttonFrom = "button_" + "h1";
            String buttonTo = "button_" + "f1";
            int resId = getResources().getIdentifier(buttonFrom, "id", getPackageName());
            ImageButton button = findViewById(resId);
            Drawable backgroundImage = button.getForeground();
            button.setForeground(null);
            button.setTag("");

            resId = getResources().getIdentifier(buttonTo, "id", getPackageName());
            button = findViewById(resId);
            button.setForeground(backgroundImage);
            button.setTag("wr");
        }
        if (from.equals("e1 wk") && to.equals("c1 wk")) {
            String buttonFrom = "button_" + "a1";
            String buttonTo = "button_" + "d1";
            int resId = getResources().getIdentifier(buttonFrom, "id", getPackageName());
            ImageButton button = findViewById(resId);
            Drawable backgroundImage = button.getForeground();
            button.setForeground(null);
            button.setTag("");

            resId = getResources().getIdentifier(buttonTo, "id", getPackageName());
            button = findViewById(resId);
            button.setForeground(backgroundImage);
            button.setTag("wr");
        }
        if (from.equals("e8 bk") && to.equals("g8 bk")) {
            String buttonFrom = "button_" + "h8";
            String buttonTo = "button_" + "f8";
            int resId = getResources().getIdentifier(buttonFrom, "id", getPackageName());
            ImageButton button = findViewById(resId);
            Drawable backgroundImage = button.getForeground();
            button.setForeground(null);
            button.setTag("");

            resId = getResources().getIdentifier(buttonTo, "id", getPackageName());
            button = findViewById(resId);
            button.setForeground(backgroundImage);
            button.setTag("br");
        }
        if (from.equals("e8 bk") && to.equals("c8 bk")) {
            String buttonFrom = "button_" + "a8";
            String buttonTo = "button_" + "d8";
            int resId = getResources().getIdentifier(buttonFrom, "id", getPackageName());
            ImageButton button = findViewById(resId);
            Drawable backgroundImage = button.getForeground();
            button.setForeground(null);
            button.setTag("");

            resId = getResources().getIdentifier(buttonTo, "id", getPackageName());
            button = findViewById(resId);
            button.setForeground(backgroundImage);
            button.setTag("br");
        }
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
    private void chessHandle(){
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
    }
}
