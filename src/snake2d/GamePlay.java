/*
 * Copyright (C) 2019 Nishchal Siddharth Pandey
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package snake2d;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author Nishchal Siddharth Pandey
 * @version 1.0
 */
public class GamePlay extends JPanel implements KeyListener, ActionListener {

    private int snakexlength[] = new int[750];
    private int snakeylength[] = new int[750];
    private final int enemyxpos[] = { 0, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400,
            425, 450, 475, 500, 525, 550, 575, 600, 625, 650, 675, 700, 725, 750, 775, 800, 825 };
    private final int enemyypos[] = { 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400, 425, 450,
            475, 500, 525, 550, 575, 600, 625 };

    private int lengthofsnake = 3;
    private int moves = 0;
    private int score = 0;
    private int highscore = 0;

    boolean left = false;
    boolean right = false;
    boolean up = false;
    boolean down = false;
    boolean gameover = false;
    boolean pause = false;

    private String name;
    private final int delay;

    private char prestep = 'r'; // contains l, r, u, d for previous step direction

    Font angry;
    Font fira;
    private final ImageIcon rightmouth = new ImageIcon(
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/rightmouth.png")));
    private final ImageIcon leftmouth = new ImageIcon(
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/leftmouth.png")));
    private final ImageIcon upmouth = new ImageIcon(
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/upmouth.png")));
    private final ImageIcon downmouth = new ImageIcon(
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/downmouth.png")));
    private final ImageIcon snakeimage = new ImageIcon(
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/snakeimage.png")));
    private final ImageIcon enemyimage = new ImageIcon(
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/enemy.png")));

    private final Timer timer;

    private java.util.Timer keyTimer = new java.util.Timer();

    private final Random random = new Random();

    private int xpos = random.nextInt(34);
    private int ypos = random.nextInt(23);
	
	private AudioInputStream eat;
	private AudioInputStream end;
	private Clip clipeat;
	private Clip clipend;

    // constructor
    public GamePlay(int level) throws FontFormatException, IOException {

        loadHighscore();

		// fonts
        angry = Font.createFont(Font.TRUETYPE_FONT, GamePlay.class.getResourceAsStream("font/angrybirds-regular.ttf"))
                .deriveFont(40.0f);
        fira = Font.createFont(Font.TRUETYPE_FONT, GamePlay.class.getResourceAsStream("font/FiraCode-Retina.ttf"))
                .deriveFont(20.0f);

		// audio clips
		try {
			eat=AudioSystem.getAudioInputStream(this.getClass().getResource("wav/mario.wav"));
			end=AudioSystem.getAudioInputStream(this.getClass().getResource("wav/buzzer.wav"));
			clipeat=AudioSystem.getClip();
			clipeat.open(eat);
			clipend=AudioSystem.getClip();
			clipend.open(end);
		} catch (Exception ex) {
			
		}
		
		// others
        this.name = name;
        switch (level) {
        case 0:
            delay = 800;
            break;
        case 1:
            delay = 500;
            break;
        case 2:
            delay = 200;
            break;
        case 3:
            delay = 100;
            break;
        case 4:
            delay = 80;
            break;
        case 5:
            delay = 50;
            break;
        default:
            delay = 100;
        }
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    @Override
    public void paint(Graphics g) {

        // whole background
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, 900, 630);
        // title border
        g.setColor(Color.WHITE);
        g.drawRect(0, 1, 851, 54); // 24, 10

        // title image
        g.setColor(Color.DARK_GRAY);
        g.fillRect(1, 2, 850, 53); // sub 23, 9 was 25, 11

        g.setColor(new Color(0, 153, 0));
        g.setFont(angry);
        g.drawString("RAVENOUS SNAKE", 290, 45);

        // draw the score
        g.setColor(Color.WHITE);
        g.setFont(fira.deriveFont(15.0f));
        g.drawString("Score:     " + score, 720, 22);
        g.drawString("Highscore: " + highscore, 720, 42);

        // border for gameplay
        g.setColor(Color.WHITE);
        g.drawRect(0, 74, 851, 577);

        // background of gameplay
        g.setColor(Color.DARK_GRAY);
        g.fillRect(1, 75, 850, 575);

        // initials
        if (moves == 0) {
            snakexlength[2] = 25;
            snakexlength[1] = 50;
            snakexlength[0] = 75;
            snakeylength[2] = 100;
            snakeylength[1] = 100;
            snakeylength[0] = 100;
            g.setColor(Color.WHITE);
            g.setFont(fira);
            g.drawString("Press Right Arrow Key or 'D' to start.", 213, 340);
        }

        // snake
        for (int i = 0; i < lengthofsnake; i++) {
            if (i == 0) {
                if (right) {
                    rightmouth.paintIcon(this, g, snakexlength[i], snakeylength[i]);
                } else if (left) {
                    leftmouth.paintIcon(this, g, snakexlength[i], snakeylength[i]);
                } else if (up) {
                    upmouth.paintIcon(this, g, snakexlength[i], snakeylength[i]);
                } else if (down) {
                    downmouth.paintIcon(this, g, snakexlength[i], snakeylength[i]);
                } else {
                    switch (prestep) {
                    case 'r':
                        rightmouth.paintIcon(this, g, snakexlength[0], snakeylength[0]);
                        break;
                    case 'l':
                        leftmouth.paintIcon(this, g, snakexlength[0], snakeylength[0]);
                        break;
                    case 'u':
                        upmouth.paintIcon(this, g, snakexlength[0], snakeylength[0]);
                        break;
                    case 'd':
                        downmouth.paintIcon(this, g, snakexlength[0], snakeylength[0]);
                        break;
                    }
                }
            } else {
                snakeimage.paintIcon(this, g, snakexlength[i], snakeylength[i]);
            }
        }

        if (enemyxpos[xpos] == snakexlength[0] && enemyypos[ypos] == snakeylength[0]) {
            lengthofsnake++;
            score++;
			try {
				clipeat.close();
				eat=AudioSystem.getAudioInputStream(this.getClass().getResource("wav/mario.wav"));
				clipeat.open(eat);
			} catch (Exception ex) {
				System.err.println(ex);
			}
			clipeat.start();
            xpos = random.nextInt(34);
            ypos = random.nextInt(23);
        }

        enemyimage.paintIcon(this, g, enemyxpos[xpos], enemyypos[ypos]);

        // check collision
        for (int i = 1; i < lengthofsnake; i++) {
            if (snakexlength[i] == snakexlength[0] && snakeylength[i] == snakeylength[0]) {
                right = false;
                left = false;
                down = false;
                up = false;
                gameover = true;
                g.setColor(Color.WHITE);
                g.setFont(new Font("angrybirds", Font.BOLD, 50));
                g.drawString("Game Over!", 303, 300);
				try {
					clipend.close();
					end=AudioSystem.getAudioInputStream(this.getClass().getResource("wav/buzzer.wav"));
					clipend.open(end);
				} catch (Exception ex) {
					System.err.println(ex);
				}
				clipend.start();
                g.setFont(new Font("fira code", Font.PLAIN, 20));
                g.drawString("Press Spacebar to restart.", 283, 340);
                if (score > highscore) {
                    g.setColor(Color.RED);
                    g.drawString("Congrats! You beated the highscore!", 222, 400);
                    highscore = score;
                    writeHighscore();
                }
            }
        }
        g.dispose();
    }

    @Override
    public void keyTyped(KeyEvent ke) {

    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if ((!gameover && moves != 0) || pause) {
            if ((ke.getKeyCode() == KeyEvent.VK_RIGHT || ke.getKeyCode() == KeyEvent.VK_D) && !left) {
                moves++;
                right = true;
                left = false;
                up = false;
                down = false;
                pause = false;
            } else if ((ke.getKeyCode() == KeyEvent.VK_LEFT || ke.getKeyCode() == KeyEvent.VK_A) && !right) {
                moves++;
                right = false;
                left = true;
                up = false;
                down = false;
                pause = false;
            } else if ((ke.getKeyCode() == KeyEvent.VK_UP || ke.getKeyCode() == KeyEvent.VK_W) && !down) {
                moves++;
                right = false;
                left = false;
                up = true;
                down = false;
                pause = false;
            } else if ((ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_S) && !up) {
                moves++;
                right = false;
                left = false;
                up = false;
                down = true;
                pause = false;
            }
        }
        if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
            right = false;
            left = false;
            down = false;
            up = false;
            if (gameover) {
                gameover = false;
                moves = 0;
                lengthofsnake = 3;
                score = 0;
				prestep='r';
				pause=false;
            } else {
                pause = true;
            }
            repaint();
        } else if ((ke.getKeyCode() == KeyEvent.VK_RIGHT || ke.getKeyCode() == KeyEvent.VK_D) && !left && moves == 0) {
            moves++;
            right = true;
            left = false;
            up = false;
            down = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        // TODO
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        do {
            if (right && prestep != 'l') {
                for (int i = lengthofsnake - 1; i >= 0; i--) {
                    snakeylength[i + 1] = snakeylength[i];
                }
                for (int i = lengthofsnake; i >= 0; i--) {
                    if (i == 0) {
                        snakexlength[i] = snakexlength[i] + 25;
                    } else {
                        snakexlength[i] = snakexlength[i - 1];
                    }
                    if (snakexlength[i] > 825) {
                        snakexlength[i] = 0;
                    }
                }
                prestep = 'r';
                repaint();
                break;
            } else if (left && prestep != 'r') {
                for (int i = lengthofsnake - 1; i >= 0; i--) {
                    snakeylength[i + 1] = snakeylength[i];
                }
                for (int i = lengthofsnake; i >= 0; i--) {
                    if (i == 0) {
                        snakexlength[i] = snakexlength[i] - 25;
                    } else {
                        snakexlength[i] = snakexlength[i - 1];
                    }
                    if (snakexlength[i] < 0) {
                        snakexlength[i] = 825;
                    }
                }
                prestep = 'l';
                repaint();
                break;
            } else if (up && prestep != 'd') {
                for (int i = lengthofsnake - 1; i >= 0; i--) {
                    snakexlength[i + 1] = snakexlength[i];
                }
                for (int i = lengthofsnake; i >= 0; i--) {
                    if (i == 0) {
                        snakeylength[i] = snakeylength[i] - 25;
                    } else {
                        snakeylength[i] = snakeylength[i - 1];
                    }
                    if (snakeylength[i] < 75) {
                        snakeylength[i] = 625;
                    }
                }
                prestep = 'u';
                repaint();
                break;
            } else if (down && prestep != 'u') {
                for (int i = lengthofsnake - 1; i >= 0; i--) {
                    snakexlength[i + 1] = snakexlength[i];
                }
                for (int i = lengthofsnake; i >= 0; i--) {
                    if (i == 0) {
                        snakeylength[i] = snakeylength[i] + 25;
                    } else {
                        snakeylength[i] = snakeylength[i - 1];
                    }
                    if (snakeylength[i] > 625) {
                        snakeylength[i] = 75;
                    }
                }
                prestep = 'd';
                repaint();
                break;
            } else if (!pause && !gameover) {
                switch (prestep) {
                case 'u':
                    up = true;
                    down = false;
                    left = false;
                    right = false;
                    break;
                case 'd':
                    up = false;
                    down = true;
                    left = false;
                    right = false;
                    break;
                case 'l':
                    up = false;
                    down = false;
                    left = true;
                    right = false;
                    break;
                case 'r':
                    up = false;
                    down = false;
                    left = false;
                    right = true;
                    break;
                }
            } else {
                break;
            } // in case of a pause
        } while (true);
    }

    private void loadHighscore() throws IOException {
        FileReader fr = null;
        try {
            fr = new FileReader(System.getProperty("user.home") + File.separator + ".hsSnake.nsp");
            BufferedReader br = new BufferedReader(fr);
            String data = br.readLine();
            int i = data.indexOf("329");
            highscore = Integer.parseInt(data.substring(i + 3));
        } catch (FileNotFoundException ex) {
            BufferedWriter buf = null;
            try {
                buf = new BufferedWriter(
                        new FileWriter(System.getProperty("user.home") + File.separator + ".hsSnake.nsp", false));
                PrintWriter pr = new PrintWriter(buf);
                pr.write("3290");
            } catch (IOException ex1) {
                Logger.getLogger(GamePlay.class.getName()).log(Level.SEVERE, null, ex1);
            } finally {
                try {
                    buf.close();
                } catch (Exception ex1) {
                    Logger.getLogger(GamePlay.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } finally {
            try {
                fr.close();
            } catch (Exception ex) {
                Logger.getLogger(GamePlay.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void writeHighscore() {
        BufferedWriter buf = null;
        try {
            buf = new BufferedWriter(
                    new FileWriter(System.getProperty("user.home") + File.separator + ".hsSnake.nsp", false));
            PrintWriter pr = new PrintWriter(buf);
            pr.write("329" + highscore);
        } catch (IOException ex) {
            Logger.getLogger(GamePlay.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                buf.close();
            } catch (IOException ex) {
                Logger.getLogger(GamePlay.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
