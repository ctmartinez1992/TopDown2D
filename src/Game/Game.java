package Game;

import Entity.Player;
import Entity.PlayerMP;
import GFX.Screen;
import GFX.SpriteSheet;
import Level.Level;
import Network.GameClient;
import Network.GameServer;
import Network.Packet.Packet00Login;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Game extends Canvas implements Runnable {

    private static final long serialVersionUID = 1L;

    public static final int GAME_WIDTH = 300;
    public static final int GAME_HEIGHT = GAME_WIDTH / 12 * 9;
    public static final int SCALE = 3;
    
    public static final String NAME = "ROTMG - V0.01";

    public JFrame frame;

    public boolean running = false;
    public int updateCount = 0;

    private BufferedImage image = new BufferedImage(GAME_WIDTH, GAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
    
    private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    private int[] colors = new int[6 * 6 * 6];

    private Screen screen;
    public InputHandler input;
    public WindowHandler windowHandler;
    public Level level;
    public Player player;

    public GameClient socketClient;
    public GameServer socketServer;

    public Game() {
        setMinimumSize(new Dimension(GAME_WIDTH * SCALE, GAME_HEIGHT * SCALE));
        setMaximumSize(new Dimension(GAME_WIDTH * SCALE, GAME_HEIGHT * SCALE));
        setPreferredSize(new Dimension(GAME_WIDTH * SCALE, GAME_HEIGHT * SCALE));

        frame = new JFrame(NAME);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.add(this, BorderLayout.CENTER);
        frame.pack();

        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void init() {
        int index = 0;
        for (int r = 0; r < 6; r++) {
            for (int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                    int rr = (r * 255 / 5);
                    int gg = (g * 255 / 5);
                    int bb = (b * 255 / 5);

                    colors[index++] = rr << 16 | gg << 8 | bb;
                }
            }
        }
        
        screen = new Screen(GAME_WIDTH, GAME_HEIGHT, new SpriteSheet("/Texture/sprite_sheet.png"));
        input = new InputHandler(this);
        windowHandler = new WindowHandler(this);
        level = new Level("/Level/water_test_level.png");
        player = new PlayerMP(level, 100, 100, input, JOptionPane.showInputDialog(this, "Please enter a username"), null, -1);
        
        level.addEntity(player);
        
        Packet00Login loginPacket = new Packet00Login(player.getUsername());
        
        if (socketServer != null)
            socketServer.addConnection((PlayerMP) player, loginPacket);
        loginPacket.writeData(socketClient);
    }

    public synchronized void start() {
        running = true;
        
        new Thread(this).start();

        if (JOptionPane.showConfirmDialog(this, "Do you want to run the server") == 0) {
            socketServer = new GameServer(this);
            socketServer.start();
        }

        socketClient = new GameClient(this, "127.0.0.1");
        socketClient.start();
    }

    public synchronized void stop() {
        running = false;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerupdate = 1000000000D / 60D;

        int updates = 0, frames = 0;

        long lastTimer = System.currentTimeMillis();
        double delta = 0;

        init();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerupdate;
            lastTime = now;
            boolean shouldRender = true;

            while (delta >= 1) {
                updates++;
                
                update();
                
                delta -= 1;
                shouldRender = true;
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (shouldRender) {
                frames++;
                
                render();
            }

            if (System.currentTimeMillis() - lastTimer >= 1000) {
                frame.setTitle(updates + " updates, " + frames + " frames");
                
                lastTimer += 1000;
                frames = updates = 0;
            }
        }
    }

    public void update() {
        updateCount++;
        level.update();
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        int xOffset = player.x - (screen.width / 2);
        int yOffset = player.y - (screen.height / 2);

        level.renderTiles(screen, xOffset, yOffset);
        level.renderEntities(screen);

        for (int y = 0; y < screen.height; y++) {
            for (int x = 0; x < screen.width; x++) {
                int colorCode = screen.pixels[x + y * screen.width];
                if (colorCode < 255)
                    pixels[x + y * GAME_WIDTH] = colors[colorCode];
            }
        }

        Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        
        g.dispose();
        
        bs.show();
    }

    public static void main(String[] args) {
        new Game().start();
    }
}
