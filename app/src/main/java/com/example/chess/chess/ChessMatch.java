package com.example.chess.chess;

import static com.example.chess.chess.Color.BLACK;
import static com.example.chess.chess.Color.WHITE;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.chess.boardgame.*;
import com.example.chess.chess.pieces.*;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check;
	private boolean checkMate;
	private ChessPiece enPassantVulnerable;
	private ChessPiece promoted;
	
	private List<Piece> piecesOnTheBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();
	private Context context;

	public ChessMatch(Context context) {
		board = new Board(8, 8);
		this.context = context;
		turn = 1;
		currentPlayer = WHITE;
		initialSetup();
	}

	public int getTurn() {
		return turn;
	}

	public Color getCurrentPlayer() {
		return currentPlayer;
	}

	public boolean getCheck() {
		return check;
	}

	public boolean getCheckMate() {
		return checkMate;
	}

	public ChessPiece getEnPassantVulnerable() {
		return enPassantVulnerable;
	}

	public ChessPiece getPromoted() {
		return promoted;
	}
	
	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for (int i=0; i<board.getRows(); i++) {
			for (int j=0; j<board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}
		return mat;
	}
	
	public boolean[][] possibleMoves(ChessPosition sourcePosition) {
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}

	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
		Position source = sourcePosition.toPosition();
		Position target = targetPosition.toPosition();
		validateSourcePosition(source);
		validateTargetPosition(source, target);
		Piece capturedPiece = makeMove(source, target);

		if (testCheck(currentPlayer)) {
			undoMove(source, target, capturedPiece);
			return null;
		}
		
		ChessPiece movedPiece = (ChessPiece)board.piece(target);
		
		// #specialmove promotion
		promoted = null;
		if (movedPiece instanceof Pawn) {
			if ((movedPiece.getColor() == WHITE && target.getRow() == 0) || (movedPiece.getColor() == BLACK && target.getRow() == 7)) {
				promoted = (ChessPiece)board.piece(target);
				promoted = replacePromotedPiece("Q");
			}
		}
		
		check = (testCheck(opponent(currentPlayer))) ? true : false;

		if (testCheckMate(opponent(currentPlayer))) {
			checkMate = true;
		}
		else {
			nextTurn();
		}
		
		// #specialmove en passant
		if (movedPiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)) {
			enPassantVulnerable = movedPiece;
		}
		else {
			enPassantVulnerable = null;
		}
		
		return (ChessPiece)capturedPiece;
	}

	public ChessPiece replacePromotedPiece(String type) {
		if (promoted == null) {
			throw new IllegalStateException("There is no piece to be promoted");
		}
		if (!type.equals("B") && !type.equals("N") && !type.equals("R") & !type.equals("Q")) {
			return promoted;
		}
		
		Position pos = promoted.getChessPosition().toPosition();
		Piece p = board.removePiece(pos);
		piecesOnTheBoard.remove(p);
		
		ChessPiece newPiece = newPiece(type, promoted.getColor());
		board.placePiece(newPiece, pos);
		piecesOnTheBoard.add(newPiece);
		
		return newPiece;
	}
	
	private ChessPiece newPiece(String type, Color color) {
		if (type.equals("B")) return new Bishop(board, color);
		if (type.equals("N")) return new Knight(board, color);
		if (type.equals("Q")) return new Queen(board, color);
		return new Rook(board, color);
	}
	
	private Piece makeMove(Position source, Position target) {
		ChessPiece p = (ChessPiece)board.removePiece(source);
		p.increaseMoveCount();
		Piece capturedPiece = board.removePiece(target);
		board.placePiece(p, target);
		
		if (capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		
		// #specialmove castling kingside rook
		if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
			board.placePiece(rook, targetT);
			rook.increaseMoveCount();
		}

		// #specialmove castling queenside rook
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
			board.placePiece(rook, targetT);
			rook.increaseMoveCount();
		}		
		
		// #specialmove en passant
		if (p instanceof Pawn) {
			if (source.getColumn() != target.getColumn() && capturedPiece == null) {
				Position pawnPosition;
				if (p.getColor() == WHITE) {
					pawnPosition = new Position(target.getRow() + 1, target.getColumn());
				}
				else {
					pawnPosition = new Position(target.getRow() - 1, target.getColumn());
				}
				capturedPiece = board.removePiece(pawnPosition);
				capturedPieces.add(capturedPiece);
				piecesOnTheBoard.remove(capturedPiece);
			}
		}
		
		return capturedPiece;
	}
	
	public void undoMove(Position source, Position target, Piece capturedPiece) {
		ChessPiece p = (ChessPiece)board.removePiece(target);
		p.decreaseMoveCount();
		board.placePiece(p, source);
		
		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}

		// #specialmove castling kingside rook
		if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			ChessPiece rook = (ChessPiece)board.removePiece(targetT);
			board.placePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}

		// #specialmove castling queenside rook
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece)board.removePiece(targetT);
			board.placePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}
		
		// #specialmove en passant
		if (p instanceof Pawn) {
			if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
				ChessPiece pawn = (ChessPiece)board.removePiece(target);
				Position pawnPosition;
				if (p.getColor() == WHITE) {
					pawnPosition = new Position(3, target.getColumn());
				}
				else {
					pawnPosition = new Position(4, target.getColumn());
				}
				board.placePiece(pawn, pawnPosition);
			}
		}
	}

	private void validateSourcePosition(Position position) {
		if(!board.thereIsAPiece(position)) {
			//throw new ChessException("There is no piece on source position");
		}
		ChessPiece p = (ChessPiece)board.piece(position);
		if (p == null || currentPlayer != p.getColor()) {
			//throw new ChessException("The chosen piece is not yours");
		}
		if(p != null && !p.isThereAnyPossibleMove()) {
			//throw new ChessException("There is no possible moves for the chosen piece");
		}
	}

	private void validateTargetPosition(Position source, Position target) {
		if (!board.piece(source).possibleMove(target)) {
			Toast.makeText(context, "The chosen piece can't move to target position", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void nextTurn() {
		turn++;
		currentPlayer = (currentPlayer == WHITE) ? BLACK : WHITE;
	}
	
	private Color opponent(Color color) {
		return (color == WHITE) ? BLACK : WHITE;
	}
	
	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for (Piece p : list) {
			if (p instanceof King) {
				return (ChessPiece)p;
			}
		}
		throw new IllegalStateException("There is no " + color + " king on the board");
	}

	public boolean testCheck(Color color) {
		Position kingPosition = king(color).getChessPosition().toPosition();
		List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
		for (Piece p : opponentPieces) {
			boolean[][] mat = p.possibleMoves();
			if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
	}

	public boolean testCheckMate(Color color) {
		if (!testCheck(color)) {
			return false;
		}
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for (Piece p : list) {
			boolean[][] mat = p.possibleMoves();
			for (int i=0; i<board.getRows(); i++) {
				for (int j=0; j<board.getColumns(); j++) {
					if (mat[i][j]) {
						Position source = ((ChessPiece)p).getChessPosition().toPosition();
						Position target = new Position(i, j);
						Piece capturedPiece = makeMove(source, target);
						boolean testCheck = testCheck(color);
						undoMove(source, target, capturedPiece);
						if (!testCheck) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition());
		piecesOnTheBoard.add(piece);
	}
	public List<Position> findDefensiveMoves(Position opponentPiecePosition, Color myColor) {
		List<Position> defensiveMoves = new ArrayList<>();

		// Trova tutte le tue pedine sul tabellone
		List<Piece> myPieces = piecesOnTheBoard.stream()
				.filter(x -> ((ChessPiece)x).getColor() == myColor)
				.collect(Collectors.toList());

		// Per ogni tua pedina, calcola le mosse possibili
		for (Piece p : myPieces) {
			boolean[][] mat = p.possibleMoves();
			for (int i=0; i<board.getRows(); i++) {
				for (int j=0; j<board.getColumns(); j++) {
					// Controlla se una delle mosse possibili può uccidere o mettersi in mezzo al pezzo avversario
					if (mat[i][j] && (new Position(i, j).equals(opponentPiecePosition) || isBetween(new Position(i, j), opponentPiecePosition, king(myColor).getChessPosition().toPosition()))) {
						defensiveMoves.add(new Position(i, j));
					}
				}
			}
		}

		// Se non trovi nessuna mossa che può uccidere o mettersi in mezzo al pezzo avversario, trova tutte le mosse possibili del re
		if (defensiveMoves.isEmpty()) {
			boolean[][] kingMoves = king(myColor).possibleMoves();
			for (int i=0; i<board.getRows(); i++) {
				for (int j=0; j<board.getColumns(); j++) {
					if (kingMoves[i][j]) {
						defensiveMoves.add(new Position(i, j));
					}
				}
			}
		}

		return defensiveMoves;
	}
	private boolean isBetween(Position middle, Position start, Position end) {
		// Controlla se le posizioni sono sulla stessa riga
		if (middle.getRow() == start.getRow() && middle.getRow() == end.getRow()) {
			// Controlla se la posizione "middle" è tra "start" e "end"
			if (start.getColumn() < end.getColumn()) {
				return middle.getColumn() > start.getColumn() && middle.getColumn() < end.getColumn();
			} else {
				return middle.getColumn() < start.getColumn() && middle.getColumn() > end.getColumn();
			}
		}

		// Controlla se le posizioni sono sulla stessa colonna
		if (middle.getColumn() == start.getColumn() && middle.getColumn() == end.getColumn()) {
			// Controlla se la posizione "middle" è tra "start" e "end"
			if (start.getRow() < end.getRow()) {
				return middle.getRow() > start.getRow() && middle.getRow() < end.getRow();
			} else {
				return middle.getRow() < start.getRow() && middle.getRow() > end.getRow();
			}
		}

		// Controlla se le posizioni sono sulla stessa diagonale
		if (Math.abs(middle.getRow() - start.getRow()) == Math.abs(middle.getColumn() - start.getColumn()) &&
				Math.abs(middle.getRow() - end.getRow()) == Math.abs(middle.getColumn() - end.getColumn())) {
			// Controlla se la posizione "middle" è tra "start" e "end"
			if (start.getRow() < end.getRow()) {
				return middle.getRow() > start.getRow() && middle.getRow() < end.getRow();
			} else {
				return middle.getRow() < start.getRow() && middle.getRow() > end.getRow();
			}
		}

		// Se le posizioni non sono né sulla stessa riga né sulla stessa colonna né sulla stessa diagonale, non sono allineate
		return false;
	}
	private void initialSetup() {
        placeNewPiece('a', 1, new Rook(board, WHITE));
        placeNewPiece('b', 1, new Knight(board, WHITE));
        placeNewPiece('c', 1, new Bishop(board, WHITE));
        placeNewPiece('d', 1, new Queen(board, WHITE));
        placeNewPiece('e', 1, new King(board, WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, WHITE));
        placeNewPiece('g', 1, new Knight(board, WHITE));
        placeNewPiece('h', 1, new Rook(board, WHITE));
        placeNewPiece('a', 2, new Pawn(board, WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, WHITE, this));

        placeNewPiece('a', 8, new Rook(board, BLACK));
        placeNewPiece('b', 8, new Knight(board, BLACK));
        placeNewPiece('c', 8, new Bishop(board, BLACK));
        placeNewPiece('d', 8, new Queen(board, BLACK));
        placeNewPiece('e', 8, new King(board, BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, BLACK));
        placeNewPiece('g', 8, new Knight(board, BLACK));
        placeNewPiece('h', 8, new Rook(board, BLACK));
        placeNewPiece('a', 7, new Pawn(board, BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, BLACK, this));
	}
}
