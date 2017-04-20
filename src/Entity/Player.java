package Entity;

import GFX.Colors;
import GFX.Font;
import GFX.Screen;
import Game.InputHandler;
import Level.Level;

public class Player extends Mob {

    private InputHandler input;
    
    private String username;
    
    private int colour = Colors.get(-1, 111, 145, 543);
    private int playerScale = 1;
    private int updateCount = 0;
    
    protected boolean isSwimming = false;

    public Player(Level level, int x, int y, InputHandler input, String username) {
        super(level, "Player", x, y, 1);
        
        this.input = input;
        this.username = username;
    }

    @Override
    public void update() {
        int xa = 0;
        int ya = 0;
        
        if (input != null) {
            if (input.up.isPressed())
                ya--;
            if (input.down.isPressed())
                ya++;
            if (input.left.isPressed())
                xa--;
            if (input.right.isPressed())
                xa++;
        }
        
        if (xa != 0 || ya != 0) {
            move(xa, ya);
            isMoving = true;
        } else {
            isMoving = false;
        }
        
        if (level.getTile(this.x >> 3, this.y >> 3).getId() == 3) {
            isSwimming = true;
        }
        
        if (isSwimming && level.getTile(this.x >> 3, this.y >> 3).getId() != 3) {
            isSwimming = false;
        }
        
        updateCount++;
    }

    @Override
    public void render(Screen screen) {
        int xTile = 0;
        int yTile = 28;
        int walkingSpeed = 4;
        int flipTop = (numSteps >> walkingSpeed) & 1;
        int flipBottom = (numSteps >> walkingSpeed) & 1;

        if (movingDir == 1) {
            xTile += 2;
        } else if (movingDir > 1) {
            xTile += 4 + ((numSteps >> walkingSpeed) & 1) * 2;
            flipTop = (movingDir - 1) % 2;
        }

        int modifier = 8 * playerScale;
        int xOffset = x - modifier / 2;
        int yOffset = y - modifier / 2 - 4;
        
        if (isSwimming) {
            int waterColor;
            yOffset += 4;
            
            if (updateCount % 60 < 15) {
                waterColor = Colors.get(-1, -1, 225, -1);
            } else if (15 <= updateCount % 60 && updateCount % 60 < 30) {
                yOffset -= 1;
                waterColor = Colors.get(-1, 225, 115, -1);
            } else if (30 <= updateCount % 60 && updateCount % 60 < 45) {
                waterColor = Colors.get(-1, 115, -1, 225);
            } else {
                yOffset -= 1;
                waterColor = Colors.get(-1, 225, 115, -1);
            }
            
            screen.render(xOffset, yOffset + 3, 0 + 27 * 32, waterColor, 0x00, 1);
            screen.render(xOffset + 8, yOffset + 3, 0 + 27 * 32, waterColor, 0x01, 1);
        }
        
        screen.render(xOffset + (modifier * flipTop), yOffset, xTile + yTile * 32, colour, flipTop, playerScale);
        screen.render(xOffset + modifier - (modifier * flipTop), yOffset, (xTile + 1) + yTile * 32, colour, flipTop, playerScale);

        if (!isSwimming) {
            screen.render(xOffset + (modifier * flipBottom), yOffset + modifier, xTile + (yTile + 1) * 32, colour, flipBottom, playerScale);
            screen.render(xOffset + modifier - (modifier * flipBottom), yOffset + modifier, (xTile + 1) + (yTile + 1) * 32, colour, flipBottom, playerScale);
        }
        
        if (username != null) {
            Font.render(username, screen, xOffset - ((username.length() - 1) / 2 * 8), yOffset - 10, Colors.get(-1, -1, -1, 555), 1);
        }
    }

    @Override
    public boolean hasCollided(int xa, int ya) {
        int xMin = 0;
        int xMax = 7;
        int yMin = 3;
        int yMax = 7;
        
        for (int xx = xMin; xx < xMax; xx++)
            if (isSolidTile(xa, ya, xx, yMin))
                return true;
        
        for (int xx = xMin; xx < xMax; xx++)
            if (isSolidTile(xa, ya, xx, yMax))
                return true;
                
        for (int yy = yMin; yy < yMax; yy++)
            if (isSolidTile(xa, ya, xMin, yy))
                return true;
                
        for (int yy = yMin; yy < yMax; yy++)
            if (isSolidTile(xa, ya, xMax, yy))
                return true;
                
        return false;
    }

    public String getUsername() {
        return this.username;
    }
}
