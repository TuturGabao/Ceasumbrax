package com.Ceasumbrax;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {

    public boolean left;
    public boolean right;
    public boolean up;
    public boolean down;

    boolean flipX = Boolean.FALSE;
    boolean flipY = Boolean.FALSE;

    public float x;
    public float y;

    int speed = 300;

    int playerSizeWidth;
    int playerSizeHeight;

    private final Texture playerImage;
    private final Texture playerImageSide;
    private final Texture playerImageUp;
    private final Texture playerImageDown;
    private final Texture playerImageUpDiagonal;
    private final Texture playerImageDownDiagonal;

    /*
    TODO: Change all class to make it random ( -> choose random tile and goes there ; we will start with rnd movement)
          Save each human data when leaving
    */
    Player(float[] poses, Texture texture, Texture textureSide, Texture textureUp, Texture textureDown, Texture textureUpDiagonal, Texture textureDownDiagonal) {
        x = poses[0]; y = poses[1];

        playerSizeWidth = 38;
        playerSizeHeight = 48;

        System.out.println("Heyy");

        playerImage = texture;
        playerImageSide = textureSide;
        playerImageUp = textureUp;
        playerImageDown = textureDown;
        playerImageUpDiagonal = textureUpDiagonal;
        playerImageDownDiagonal = textureDownDiagonal;
    }

    public void drawImage(SpriteBatch batch) {
        Texture showingTexture = playerImage;

        if (left || right) {
            if (up) {
                showingTexture = playerImageUpDiagonal;
            } else if (down) {
                showingTexture = playerImageDownDiagonal;
            } else {
                showingTexture = playerImageSide;
            }
        } else {
            if (up) {
                showingTexture = playerImageUp;
            } else if (down) {
                showingTexture = playerImageDown;
            }
        }

        batch.draw(showingTexture, x, y, playerSizeWidth, playerSizeHeight, 0, 0, playerImage.getWidth(), playerImage.getHeight(), flipX, flipY);

    }

    public void handleMovement() {
        if (left) flipX = Boolean.TRUE;
        if (right) flipX = Boolean.FALSE;

    }

}
