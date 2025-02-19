
/**
 * Main By: Emma Power
 *
 * -<==< CONTROLS >==>-
 * Left Arrow = Move Left
 * Right Arrow = Move Right
 * Up Arrow = Rotate Piece
 * Down Arrow = Drop Piece 1 Block (Hold for 1 second to make it drop even faster)
 * Shift = Hold Piece (Save a piece for later) You can only hold a piece once per new piece, or else you could cheat.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Main extends JPanel implements ActionListener {
	static int screenHeight = 720;
	static int screenWidth = 1280;
	static double posX = 4;
	static double posY = 2;
	static double dirX = -1;
	static double dirY = 0;
	static double planeX = 0;
	static double planeY = 0.66;

	double time = 0;
	double oldTime = 0;

	static boolean left = false;
	static boolean right = false;
	static boolean up = false;
	static boolean down = false;

	static int fps = 0;

	static String[] imgDir = { "Images/deez.png", "Images/d.png", "Images/e.png", "Images/z.png", "Images/n.png",
			"Images/u.png", "Images/t.png", "Images/s.png", "Images/pengu.gif", "Images/crab.gif" };
	static BufferedImage[] imgs = new BufferedImage[imgDir.length];
	static Image[] animatedImgs = new Image[imgDir.length];

	double[][] lines = new double[screenWidth][5];
	static int[][] worldMap = {
        {1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1}
    };

	/*
	 * Paint Method Runs when repaint(); is called Paints Where Pieces are, in the
	 * colour they are supposed to be and important information (Score etc.) This
	 * was by far the hardest part, along with making th JFrame in main
	 */
	public void paint(Graphics g) {
		// Main Paint Loop
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;

		BufferedImage screen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
		g2d = screen.createGraphics();
		g2d.setColor(Color.gray);
		g2d.fillRect(0, 0, screenWidth, screenHeight / 2);
		g2d.setColor(Color.DARK_GRAY);
		g2d.fillRect(0, screenHeight / 2, screenWidth, screenHeight / 2);
		for (int i = 0; i < screenWidth; i++) {

			Color color;
			switch ((int) lines[i][2]) {
			case 1:
				color = Color.red;
				break;
			case 2:
				color = Color.orange;
				break;
			case 3:
				color = Color.yellow;
				break;
			case 4:
				color = Color.green;
				break;
			case 5:
				color = Color.blue;
				break;
			case 6:
				color = new Color(170, 0, 255);
				break;

			default:
				color = Color.white;
			}

			if (lines[i][3] == 1) {
				color = new Color((int) (color.getRed() / 1.5), (int) (color.getGreen() / 1.5),
						(int) (color.getBlue() / 1.5));
			}

			g2d.setColor(color);
			g2d.drawLine(i, (int) lines[i][0], i, (int) lines[i][1]);

			if (lines[i][2] >= 7) {
				int imgNum = (int) lines[i][2] - 7;
				int lineHeight = (int) lines[i][1] - (int) lines[i][0];
				double lineRatioY = ((double) imgs[imgNum].getHeight() / lineHeight);

				for (int j = 0; j < lineHeight; j++) {
					Color temp = new Color(
							imgs[imgNum].getRGB((int) (imgs[imgNum].getWidth() * lines[i][4]), (int) (j * lineRatioY)));
					try {
						screen.setRGB(i, j + (int) lines[i][0], temp.getRGB());
					} catch (Exception e) {
					}
				}
			}
		}

		g2d.setColor(Color.black);
//		for(int i = 0; i < textures.size(); i++) {
//			g2d.drawLine(textures.get(i), 0, textures.get(i), 720);
//		}
//
		Graphics2D paint = (Graphics2D) g;
		paint.drawImage(screen, 0, 0, null);

		fps++;

	}// Paint

	public static void updateGifs() {
		for (int i = 0; i < imgs.length; i++) {
			try {
				MediaTracker mt = new MediaTracker(new JPanel());
				Image image = animatedImgs[i];
				BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null),
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = bi.createGraphics();
				g2d.drawImage(image, 0, 0, null);
				g2d.dispose();
				imgs[i] = bi;
			} catch (Exception e) {
				System.out.println("Still loading imgs");
			}
		}
	}

	private static void runGUI() {
		Main main = new Main();
	}// RunGUI

	public void raycast() {
		for (int x = 0; x < screenWidth; x++) {
			// Current position to next line on grid
			double sideDistX;
			double sideDistY;
			double perpWallDist;
			int stepX;
			int stepY;
			int hit = 0;
			int side = 0;

			// Where is the ray now
			int mapX = (int) posX;
			int mapY = (int) posY;
			// Where is the ray going
			double cameraX = (2 * x) / ((double) screenWidth) - 1;
			double rayDirX = dirX + planeX * cameraX;
			double rayDirY = dirY + planeY * cameraX;

			// Length of ray from one line on grid to the next
			double deltaDistX = Math.abs(1 / rayDirX);
			double deltaDistY = Math.abs(1 / rayDirY);

			// Caculate step direction and sideDist (distance to next line on grid)
			if (rayDirX < 0) {
				stepX = -1;
				sideDistX = (posX - mapX) * deltaDistX;
			} else {
				stepX = 1;
				sideDistX = (mapX + 1.0 - posX) * deltaDistX;
			}
			if (rayDirY < 0) {
				stepY = -1;
				sideDistY = (posY - mapY) * deltaDistY;
			} else {
				stepY = 1;
				sideDistY = (mapY + 1.0 - posY) * deltaDistY;
			}

			// Loop through and jump squares until you hit one
			while (hit == 0) {
				// Go to next line on grid, whichever is closer
				if (sideDistX < sideDistY) {
					sideDistX += deltaDistX;
					mapX += stepX;
					side = 0;
				} else {
					sideDistY += deltaDistY;
					mapY += stepY;
					side = 1;
				}

				// Check if it exists
				if (worldMap[mapX][mapY] > 0) {
					hit = 1;
				}
			}

			// Remove fisheye
			if (side == 0) {
				perpWallDist = (mapX - posX + (1 - stepX) / 2) / rayDirX;
			} else {
				perpWallDist = (mapY - posY + (1 - stepY) / 2) / rayDirY;
			}

			// Make line size to draw on screen
			int lineHeight = (int) (screenHeight / perpWallDist);

			int drawStart = (-lineHeight / 2 + screenHeight / 2) - 10;
			int drawEnd = (lineHeight / 2 + screenHeight / 2) + 10;

			// Max line size
			if (drawStart < 0) {
				drawStart = 0;
			}
			if (drawEnd > screenHeight) {
				drawEnd = screenHeight;
			}
			double wallX;
			if (side == 0)
				wallX = posY + perpWallDist * rayDirY;
			else
				wallX = posX + perpWallDist * rayDirX;

			// Put info in array to draw
			lines[x][0] = drawStart;
			lines[x][1] = drawEnd;
			lines[x][2] = worldMap[mapX][mapY];
			lines[x][3] = side;
			lines[x][4] = wallX % 1;

		}

	}

	public static void controls() {
		double rotSpeed = 0.03;
		double moveSpeed = 0.1;

		if (left) {
			double oldDirX = dirX;
			dirX = dirX * Math.cos(rotSpeed) - dirY * Math.sin(rotSpeed);
			dirY = oldDirX * Math.sin(rotSpeed) + dirY * Math.cos(rotSpeed);
			double oldPlaneX = planeX;
			planeX = planeX * Math.cos(rotSpeed) - planeY * Math.sin(rotSpeed);
			planeY = oldPlaneX * Math.sin(rotSpeed) + planeY * Math.cos(rotSpeed);
		}
		if (right) {
			double oldDirX = dirX;
			dirX = dirX * Math.cos(-rotSpeed) - dirY * Math.sin(-rotSpeed);
			dirY = oldDirX * Math.sin(-rotSpeed) + dirY * Math.cos(-rotSpeed);
			double oldPlaneX = planeX;
			planeX = planeX * Math.cos(-rotSpeed) - planeY * Math.sin(-rotSpeed);
			planeY = oldPlaneX * Math.sin(-rotSpeed) + planeY * Math.cos(-rotSpeed);
		}
		if (up) {
			if (worldMap[(int) (posX + dirX * moveSpeed)][(int) (posY)] == 0) {
				posX += dirX * moveSpeed;
			}
			if (worldMap[(int) (posX)][(int) (posY + dirY * moveSpeed)] == 0) {
				posY += dirY * moveSpeed;
			}
		}
		if (down) {
			if (worldMap[(int) (posX - dirX * moveSpeed)][(int) (posY)] == 0) {
				posX -= dirX * moveSpeed;
			}
			if (worldMap[(int) (posX)][(int) (posY - dirY * moveSpeed)] == 0) {
				posY -= dirY * moveSpeed;
			}
		}
	}

	public static void main(String[] args) {
		// Variables
		for (int i = 0; i < imgs.length; i++) {
			try {
				imgs[i] = ImageIO.read(new File(imgDir[i]));
				animatedImgs[i] = Toolkit.getDefaultToolkit().createImage(imgDir[i]);
			} catch (IOException e) {
			}
		}

		long loopTime = System.currentTimeMillis() + 300;

		// Constructs JFrame
		JFrame frame = new JFrame("Raycaster Redo | By: Emma Power");
		final Main main = new Main();
		frame.add(main);
		frame.setSize(screenWidth + 16, screenHeight + 39);

		// Key Listener Activates Shift/Rotate/Drop Methods on key press
		frame.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				double rotSpeed = 0.1;
				double moveSpeed = 1;
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					left = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					right = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					up = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					down = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {

				}
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					left = false;
				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					right = false;
				}
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					up = false;
				}
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					down = false;
				}
			}
		});

		// Start Game
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		long lastSecond = System.currentTimeMillis();
		// Main Game Loop
		while (true) {
			loopTime = System.currentTimeMillis() + (1000 / 60);

			// Loops until loopTime is over, and forces a drop in piece
			while (System.currentTimeMillis() < loopTime) {

			}
			main.controls();
			main.raycast();
			main.repaint();
			main.updateGifs();
			fps++;
			if (System.currentTimeMillis() > lastSecond + 1000) {
				System.out.println(fps);
				lastSecond = System.currentTimeMillis();
				fps = 0;
			}
		}

	}// Main

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}
}// Main
