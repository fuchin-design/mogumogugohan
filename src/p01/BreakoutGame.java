package p01;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class BreakoutGame extends JPanel implements ActionListener {
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static final int PADDLE_WIDTH = 100;
	private static final int PADDLE_HEIGHT = 20;
	private static final int BALL_SIZE = 20;
	private static final int BLOCK_WIDTH = 60;
	private static final int BLOCK_HEIGHT = 30;
	private static final int BLOCK_ROWS = 5;
	private static final int BLOCK_COLUMNS = 10;
	private static final int BLOCK_PADDING = 10;

	private Timer timer;
	private int paddleX;
	private int ballX, ballY, ballDX, ballDY;

	private List<Rectangle> blocks;

	public BreakoutGame() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.BLACK);
		setFocusable(true);
		addKeyListener(new PaddleMover());

		initGame();
		timer = new Timer(5, this);
		timer.start();
	}

	private void initGame() {
		paddleX = WIDTH / 2 - PADDLE_WIDTH / 2;
		ballX = WIDTH / 2 - BALL_SIZE / 2;
		ballY = HEIGHT / 2 - BALL_SIZE / 2;
		ballDX = 2;
		ballDY = -2;

		blocks = new ArrayList<>();
		for (int row = 0; row < BLOCK_ROWS; row++) {
			for (int col = 0; col < BLOCK_COLUMNS; col++) {
				int x = col * (BLOCK_WIDTH + BLOCK_PADDING) + BLOCK_PADDING;
				int y = row * (BLOCK_HEIGHT + BLOCK_PADDING) + BLOCK_PADDING;
				blocks.add(new Rectangle(x, y, BLOCK_WIDTH, BLOCK_HEIGHT));
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(Color.WHITE);
		g.fillRect(paddleX, HEIGHT - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);
		g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

		g.setColor(Color.RED);
		for (Rectangle block : blocks) {
			g.fillRect(block.x, block.y, block.width, block.height);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ballX += ballDX;
		ballY += ballDY;

		if (ballX <= 0 || ballX >= WIDTH - BALL_SIZE) {
			ballDX = -ballDX;
		}
		if (ballY <= 0) {
			ballDY = -ballDY;
		}
		if (ballY >= HEIGHT - BALL_SIZE) {
			initGame();
		}

		Rectangle ballRect = new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE);
		Rectangle paddleRect = new Rectangle(paddleX, HEIGHT - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);

		if (ballRect.intersects(paddleRect)) {
			ballDY = -ballDY;
		}

		List<Rectangle> blocksToRemove = new ArrayList<>();
		for (Rectangle block : blocks) {
			if (ballRect.intersects(block)) {
				ballDY = -ballDY;
				blocksToRemove.add(block);
			}
		}
		blocks.removeAll(blocksToRemove);

		if (blocks.isEmpty()) {
			initGame();
		}

		repaint();
	}

	private class PaddleMover extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				paddleX -= 10;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				paddleX += 10;
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Breakout Game");
		BreakoutGame game = new BreakoutGame();
		frame.add(game);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
