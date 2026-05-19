package p01;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Huusenkamone extends JPanel implements ActionListener, KeyListener {

    // フィールドサイズ
    private final int ROWS = 20, COLS = 10;

    // 1ブロックのピクセルサイズ（30x30）
    private final int BLOCK = 30;

    // タイマー（定期的に actionPerformed を呼び出す）
    private Timer timer;

    // ゲームボードの状態を表す2次元配列（0: 空, 1: 固定ブロック）
    private int[][] board = new int[ROWS][COLS];

    // 現在操作中のブロック形状（2次元配列）
    private int[][] currentPiece;

    // 現在操作中ブロックの座標（左上基準）
    private int px = 3, py = 0;

    // ランダムブロック生成用
    private Random rand = new Random();

    // ブロックパターン（I, O, T, S, Z, J, L）
    private final List<int[][]> pieces = Arrays.asList(
        new int[][] { // I
            {0, 0, 0, 0},
            {1, 1, 1, 1},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        },
        new int[][] { // O
            {1, 1},
            {1, 1}
        },
        new int[][] { // T
            {0, 1, 0},
            {1, 1, 1},
            {0, 0, 0}
        },
        new int[][] { // S
            {0, 1, 1},
            {1, 1, 0},
            {0, 0, 0}
        },
        new int[][] { // Z
            {1, 1, 0},
            {0, 1, 1},
            {0, 0, 0}
        },
        new int[][] { // J
            {1, 0, 0},
            {1, 1, 1},
            {0, 0, 0}
        },
        new int[][] { // L
            {0, 0, 1},
            {1, 1, 1},
            {0, 0, 0}
        }
    );

    // コンストラクタ（初期化処理）
    public Huusenkamone() {
        setPreferredSize(new Dimension(COLS * BLOCK, ROWS * BLOCK)); // パネルサイズ指定
        setBackground(Color.BLACK); // 背景黒
        setFocusable(true); // キー入力受け付け
        addKeyListener(this); // キーイベント登録
        spawnPiece(); // 最初のブロック出現
        timer = new Timer(500, this); // 0.5秒ごとに落下処理
        timer.start(); // タイマースタート
    }

    // ランダムにブロックを出現させる
    private void spawnPiece() {
        currentPiece = deepCopy(pieces.get(rand.nextInt(pieces.size()))); // パターンからランダム取得
        px = 3;
        py = 0;

        // 初期位置で置けない場合はゲームオーバー
        if (!canMove(0, 0)) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Game Over!");
        }
    }

    // 2次元配列のディープコピー
    private int[][] deepCopy(int[][] shape) {
        int[][] copy = new int[shape.length][];
        for (int i = 0; i < shape.length; i++) {
            copy[i] = Arrays.copyOf(shape[i], shape[i].length);
        }
        return copy;
    }

    // 指定方向に移動できるかどうかチェック
    private boolean canMove(int dx, int dy) {
        for (int y = 0; y < currentPiece.length; y++) {
            for (int x = 0; x < currentPiece[y].length; x++) {
                if (currentPiece[y][x] == 1) {
                    int nx = px + x + dx;
                    int ny = py + y + dy;
                    // 画面外または既存ブロックと衝突していないか
                    if (nx < 0 || nx >= COLS || ny >= ROWS || (ny >= 0 && board[ny][nx] == 1))
                        return false;
                }
            }
        }
        return true;
    }

    // 回転後の形状で配置可能かを判定
    private boolean canRotate(int[][] rotated) {
        for (int y = 0; y < rotated.length; y++) {
            for (int x = 0; x < rotated[y].length; x++) {
                if (rotated[y][x] == 1) {
                    int nx = px + x;
                    int ny = py + y;
                    if (nx < 0 || nx >= COLS || ny >= ROWS || (ny >= 0 && board[ny][nx] == 1))
                        return false;
                }
            }
        }
        return true;
    }

    // ブロックの回転処理（90度右回転）
    private void rotatePiece() {
        int size = currentPiece.length;
        int[][] rotated = new int[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                rotated[x][size - 1 - y] = currentPiece[y][x];
            }
        }
        if (canRotate(rotated)) {
            currentPiece = rotated;
        }
    }

    // ブロックを盤面に固定（マージ）
    private void mergePiece() {
        for (int y = 0; y < currentPiece.length; y++) {
            for (int x = 0; x < currentPiece[y].length; x++) {
                if (currentPiece[y][x] == 1) {
                    board[py + y][px + x] = 1;
                }
            }
        }
        clearLines();  // ライン消去
        spawnPiece();  // 次のブロックを出現
    }

    // ラインが揃っていたら消去
    private void clearLines() {
        for (int y = ROWS - 1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < COLS; x++) {
                if (board[y][x] == 0) full = false;
            }
            if (full) {
                // 上の行を下にずらす
                for (int row = y; row > 0; row--) {
                    board[row] = board[row - 1].clone();
                }
                board[0] = new int[COLS]; // 一番上は空に
                y++; // 同じ行をもう一度確認（複数ライン消し対応）
            }
        }
    }

    // 描画処理（盤面と現在のブロック）
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 固定されたブロックを描画
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (board[y][x] == 1) {
                    g.setColor(Color.CYAN);
                    g.fillRect(x * BLOCK, y * BLOCK, BLOCK, BLOCK);
                }
            }
        }

        // 現在の操作中ブロックを描画
        g.setColor(Color.GREEN);
        for (int y = 0; y < currentPiece.length; y++) {
            for (int x = 0; x < currentPiece[y].length; x++) {
                if (currentPiece[y][x] == 1) {
                    g.fillRect((px + x) * BLOCK, (py + y) * BLOCK, BLOCK, BLOCK);
                }
            }
        }
    }

    // タイマーから呼ばれる落下処理
    public void actionPerformed(ActionEvent e) {
        if (canMove(0, 1)) {
            py++; // 1マス下に移動
        } else {
            mergePiece(); // 着地 → 固定化
        }
        repaint(); // 画面更新
    }

    // キー入力処理
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (canMove(-1, 0)) px--;
                break;
            case KeyEvent.VK_RIGHT:
                if (canMove(1, 0)) px++;
                break;
            case KeyEvent.VK_DOWN:
                if (canMove(0, 1)) py++;
                break;
            case KeyEvent.VK_UP:
                rotatePiece(); // 回転
                break;
        }
        repaint();
    }

    // 他のキーイベントは使用しない
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // メインメソッド：ゲーム起動処理
    public static void main(String[] args) {
        JFrame frame = new JFrame("Mini Tetris");
        Huusenkamone game = new Huusenkamone();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}