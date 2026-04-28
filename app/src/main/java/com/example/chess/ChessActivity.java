package com.example.chess;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Stack;

public class ChessActivity extends AppCompatActivity {

    private GridLayout chessBoard;
    private ImageView[][] cells = new ImageView[8][8];
    private int[][] board = new int[8][8];

    private int selectedRow = -1;
    private int selectedCol = -1;

    private boolean whiteTurn = true;
    private boolean gameOver = false;

    private Stack<Move> moveHistory = new Stack<>();

    class Move {
        int fr, fc, tr, tc;
        int moved, captured;

        Move(int fr, int fc, int tr, int tc, int moved, int captured) {
            this.fr = fr;
            this.fc = fc;
            this.tr = tr;
            this.tc = tc;
            this.moved = moved;
            this.captured = captured;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chess);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chessActivityLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        chessBoard = findViewById(R.id.chessBoard);

        int cellSize = getResources().getDisplayMetrics().widthPixels / 8;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ImageView cell = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                cell.setLayoutParams(params);

                if ((r + c) % 2 == 0)
                    cell.setBackgroundColor(0xFFEFEFEF);
                else
                    cell.setBackgroundColor(0xFF7B7B7B);

                final int row = r;
                final int col = c;
                cell.setOnClickListener(v -> handleCellClick(row, col));

                chessBoard.addView(cell);
                cells[r][c] = cell;
            }
        }

        boolean continueGame = getIntent().getBooleanExtra("continueGame", false);

        if (continueGame)
            loadGame();
        else
            setupInitialPieces();
    }

    private void handleCellClick(int row, int col) {
        if (gameOver) return;

        if (selectedRow == -1) {
            int piece = board[row][col];
            if (piece == 0) return;
            if (whiteTurn && piece < 0) return;
            if (!whiteTurn && piece > 0) return;

            selectedRow = row;
            selectedCol = col;
        } else {
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                int captured = board[row][col];

                moveHistory.push(
                        new Move(selectedRow, selectedCol, row, col, board[selectedRow][selectedCol], captured)
                );

                setPiece(row, col, board[selectedRow][selectedCol]);
                setPiece(selectedRow, selectedCol, 0);

                if (Math.abs(captured) == 6) {
                    gameOver = true;
                    String msg = captured > 0 ? "White loses!" : "Black loses!";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                    chessBoard.postDelayed(() -> {
                        Intent intent = new Intent(ChessActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }, 500);
                } else {
                    whiteTurn = !whiteTurn;
                }
            }
            selectedRow = -1;
            selectedCol = -1;
        }
    }

    private boolean isValidMove(int fr, int fc, int tr, int tc) {
        int piece = board[fr][fc];
        int target = board[tr][tc];

        if (piece == 0) return false;
        if (target != 0 && Integer.signum(piece) == Integer.signum(target)) return false;

        int dr = tr - fr;
        int dc = tc - fc;

        switch (Math.abs(piece)) {
            case 1: // pawn
                if (piece > 0) { // white
                    if (dc == 0 && dr == -1 && target == 0) return true;
                    if (dc == 0 && dr == -2 && fr == 6 && target == 0 && board[5][fc] == 0) return true;
                    if (Math.abs(dc) == 1 && dr == -1 && target < 0) return true;
                } else { // black
                    if (dc == 0 && dr == 1 && target == 0) return true;
                    if (dc == 0 && dr == 2 && fr == 1 && target == 0 && board[2][fc] == 0) return true;
                    if (Math.abs(dc) == 1 && dr == 1 && target > 0) return true;
                }
                return false;
            case 2: // rook
                if (dr == 0 || dc == 0) return clearPath(fr, fc, tr, tc);
                return false;
            case 3: // knight
                return (Math.abs(dr) == 2 && Math.abs(dc) == 1) || (Math.abs(dr) == 1 && Math.abs(dc) == 2);
            case 4: // bishop
                if (Math.abs(dr) == Math.abs(dc)) return clearPath(fr, fc, tr, tc);
                return false;
            case 5: // queen
                if (dr == 0 || dc == 0) return clearPath(fr, fc, tr, tc);
                if (Math.abs(dr) == Math.abs(dc)) return clearPath(fr, fc, tr, tc);
                return false;
            case 6: // king
                return Math.abs(dr) <= 1 && Math.abs(dc) <= 1;
        }
        return false;
    }

    private boolean clearPath(int fr, int fc, int tr, int tc) {
        int dr = Integer.signum(tr - fr);
        int dc = Integer.signum(tc - fc);

        int r = fr + dr;
        int c = fc + dc;

        while (r != tr || c != tc) {
            if (board[r][c] != 0) return false;
            r += dr;
            c += dc;
        }
        return true;
    }

    private void setPiece(int row, int col, int piece) {
        board[row][col] = piece;
        ImageView cell = cells[row][col];

        switch (piece) {
            case 1:
                cell.setImageResource(R.drawable.white_pawn);
                break;
            case 2:
                cell.setImageResource(R.drawable.white_rook);
                break;
            case 3:
                cell.setImageResource(R.drawable.white_knight);
                break;
            case 4:
                cell.setImageResource(R.drawable.white_bishop);
                break;
            case 5:
                cell.setImageResource(R.drawable.white_queen);
                break;
            case 6:
                cell.setImageResource(R.drawable.white_king);
                break;
            case -1:
                cell.setImageResource(R.drawable.black_pawn);
                break;
            case -2:
                cell.setImageResource(R.drawable.black_rook);
                break;
            case -3:
                cell.setImageResource(R.drawable.black_knight);
                break;
            case -4:
                cell.setImageResource(R.drawable.black_bishop);
                break;
            case -5:
                cell.setImageResource(R.drawable.black_queen);
                break;
            case -6:
                cell.setImageResource(R.drawable.black_king);
                break;
            default:
                cell.setImageDrawable(null);
        }
    }

    private void setupInitialPieces() {
        // Бели
        setPiece(7, 0, 2);
        setPiece(7, 1, 3);
        setPiece(7, 2, 4);
        setPiece(7, 3, 5);
        setPiece(7, 4, 6);
        setPiece(7, 5, 4);
        setPiece(7, 6, 3);
        setPiece(7, 7, 2);
        for (int i = 0; i < 8; i++) setPiece(6, i, 1);

        // Черни
        setPiece(0, 0, -2);
        setPiece(0, 1, -3);
        setPiece(0, 2, -4);
        setPiece(0, 3, -5);
        setPiece(0, 4, -6);
        setPiece(0, 5, -4);
        setPiece(0, 6, -3);
        setPiece(0, 7, -2);
        for (int i = 0; i < 8; i++) setPiece(1, i, -1);
    }

    private void loadGame() {
        SharedPreferences prefs = getSharedPreferences("ChessSave", MODE_PRIVATE);
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                int piece = prefs.getInt("cell_" + r + "_" + c, 0);
                board[r][c] = piece;
                setPiece(r, c, piece);
            }
        whiteTurn = prefs.getBoolean("whiteTurn", true);
    }

    public void onUndoClick(View v) {
        if (moveHistory.empty() || gameOver) return;
        Move last = moveHistory.pop();
        setPiece(last.fr, last.fc, last.moved);
        setPiece(last.tr, last.tc, last.captured);
        whiteTurn = !whiteTurn;
    }

    public void onSaveClick(View v) {
        if(gameOver) return;
        SharedPreferences prefs = getSharedPreferences("ChessSave", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                editor.putInt("cell_" + r + "_" + c, board[r][c]);

        editor.putBoolean("whiteTurn", whiteTurn);
        editor.apply();

        Intent intent = new Intent(ChessActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onBtnMenuClick(View v) {
        Intent intent = new Intent(ChessActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}