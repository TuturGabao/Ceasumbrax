package com.Ceasumbrax;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.HashMap;

public class MapRelated {
    Json json = new Json();

    String pathToGroundRelatedTiles = "GroundRelated/Tiles/";
    FileHandle filePathGroundRelatedTilesTXT = Gdx.files.internal(pathToGroundRelatedTiles + "files.txt");
    String[] pathToGroundRelatedTilesFiles = filePathGroundRelatedTilesTXT.readString().split("\\r?\\n");

    String pathToGroundRelatedMisc = "GroundRelated/Misc/";
    FileHandle filePathGroundRelatedMiscTXT = Gdx.files.internal(pathToGroundRelatedMisc + "files.txt");
    String[] pathToGroundRelatedMiscFiles = filePathGroundRelatedMiscTXT.readString().split("\\r?\\n");

    public boolean left;
    public boolean right;
    public boolean up;
    public boolean down;
    public boolean shifted;

    int[][] tiledVisibleIntMap;

    int wholeMapChunkWidth = 64;
    int wholeMapChunkHeight = 64;

    int chunkWidth = 64;
    int chunkHeight = 64;

    int tileSize = 64;
    int smallTiledSize = 8;

    int resolutionW;
    int resolutionH;

    int tiledMapWidth;
    int tiledMapHeight;

    int startXTilesVisibleOnScreen;
    int startYTilesVisibleOnScreen;

    int startTilesTotalX;
    int startTilesTotalY;

    int speed = 190;
    double speedShiftMultiplier = 2.5;

    Humans[] humans;
    String gameName;

    HashMap<Integer, TextureRegion> tilesDictionary = new HashMap<>();
    HashMap<Integer, TextureRegion> miscDictionary = new HashMap<>();

    int[] activeChunk;
    int centerXCoord;
    int centerYCoord;

    int chunkBaseRow;
    int chunkBaseCol;

    int[][] aroundChunk;

    /*
    TODO: Finish the all map functions with the chunks.
          Save the position of the map.
          When reaching an other chunk, create a new csv chunk containing infos.
          Try not to show every tile, only show visible tiles + 10% of the visible tiles.
            -> if tile > WIDTH + 10% not show, else, show a tile more at the top.
    */

    MapRelated(int resolutionWidth, int resolutionHeight, String GameName, Boolean newGame) {
        centerXCoord = resolutionWidth / 2; //ResWidth = 1920
        centerYCoord = resolutionHeight /2; //ResHeight = 1080


        miscDictionary = initialiseTexture(pathToGroundRelatedMiscFiles, pathToGroundRelatedMisc, miscDictionary);
        tilesDictionary = initialiseTexture(pathToGroundRelatedTilesFiles, pathToGroundRelatedTiles, tilesDictionary);

        json.setOutputType(JsonWriter.OutputType.json);

        resolutionW = resolutionWidth;
        resolutionH = resolutionHeight;

        gameName = GameName;

        if (newGame) {
            createWholeMap(gameName);
        }
        initialiseTiledMap(gameName);
        initialiseData();
    }

    public void createWholeMap(String gameName) {
        String pathToGameSaves = "Saves/"+gameName+"/";

        Gdx.files.local(pathToGameSaves + "Chunks/").mkdirs();

        int[][][] aroundChunks = new int[3][chunkWidth][chunkHeight];
        int[][][][] wholeChunks = new int[2][wholeMapChunkHeight][chunkWidth][chunkHeight];

        for (int i = 0; i < wholeMapChunkWidth; i++) {
            for (int j = 0; j < wholeMapChunkHeight; j++) {
                HashMap<String, Object> rootHashMap = new HashMap<>();

                aroundChunks[2] = createCurrChunkFile(aroundChunks[0], aroundChunks[1]);

                rootHashMap.put("ChunkWidth", chunkWidth);
                rootHashMap.put("ChunkHeight", chunkHeight);
                rootHashMap.put("ChunkData", aroundChunks[2]);

                String prettyJson = json.prettyPrint(rootHashMap);

                String chunkName = "Chunk " + i + "-" + j + ".json";
                Gdx.files.local(pathToGameSaves + "Chunks/" + chunkName).writeString(prettyJson, false);

                aroundChunks[1] = aroundChunks[2];
                wholeChunks[i%2][j] = aroundChunks[2];

                if (i > 0) {
                    aroundChunks[0] = wholeChunks[(i - 1) % 2][j];
                } else {
                    aroundChunks[0] = null;
                }
            }
        }
    }

    public int[][] createCurrChunkFile(int[][] upChunk, int[][] leftChunk) {
        int[][] newChunk = new int[chunkWidth][chunkHeight];

        if (upChunk != null) {
            int[] lowerLayerUpChunk = upChunk[upChunk.length - 1];
        }
        for (int i = 0; i < newChunk.length; i++) {
            for (int j = 0; j < newChunk[i].length; j++) {
                // TODO: Add the random and correlation with all the chunks around.
                newChunk[i][j] = 1;
            }
        }

        return newChunk;
    }

    public void initialiseData() {
        startXTilesVisibleOnScreen = (resolutionW - tiledMapWidth * tileSize) / 2 + 1;
        startYTilesVisibleOnScreen = (resolutionH - tiledMapHeight * tileSize) / 2 + 1;
    }

    public HashMap<Integer, TextureRegion> initialiseTexture(String[] path, String pathToFile, HashMap<Integer, TextureRegion> dictionary) {
        int count = 0;
        for (String file : path) {
            Texture tex = new Texture(pathToFile + file);
            TextureRegion region = new TextureRegion(tex);

            region.flip(false, true);

            dictionary.put((count + 1), region);

            count++;
        }

        return dictionary;

    }

    public void initialiseTiledMap(String gameName) {
        chunkBaseCol = wholeMapChunkWidth/2-1;
        chunkBaseRow = wholeMapChunkHeight/2-1;
        int[][] chunkData = getChunkData(chunkBaseRow, chunkBaseCol, gameName);

        int centerX = resolutionW / 2; // - tileSize / 2; //// INFO: We do not remove half the size of a tile because the number of tile is even.
        int centerY = resolutionH / 2; // - tileSize / 2;

        startTilesTotalX = (centerX - chunkWidth / 2 * tileSize);
        startTilesTotalY = (centerY - chunkHeight / 2 * tileSize);

        tiledVisibleIntMap = new int[chunkWidth][chunkHeight];

        for (int i = 0; i < chunkData.length; i++) {
            for (int j = 0; j < chunkData[i].length; j++) {
                tiledVisibleIntMap[i][j] = chunkData[i][j];
            }
        }
    }

    public int[][] getAroundChunk(int[][] chunk, int tilesAroundBeforeLoading /*, int nbTilesUp, int nbTilesDown, int nbTilesLeft, int nbTilesRight*/) {

        boolean up = false;
        boolean down = false;
        boolean right = false;
        boolean left = false;

        int wholeMapLeftBorder = centerXCoord - chunk[0].length/2*tileSize;
        int wholeMapRightBorder = centerXCoord + chunk[0].length/2*tileSize;

        int wholeMapUpBorder = centerYCoord - chunk.length/2*tileSize;
        int wholeMapDownBorder = centerYCoord + chunk.length/2*tileSize;

        if (wholeMapLeftBorder > -tilesAroundBeforeLoading * tileSize) {
            System.out.println("LEFT");
        }
        if (wholeMapRightBorder < resolutionW-tilesAroundBeforeLoading * tileSize) {
            System.out.println("RIGHT");
        }
        if (wholeMapUpBorder > -tilesAroundBeforeLoading * tileSize) {
            System.out.println("UP");
        }
        if (wholeMapDownBorder < resolutionH-tilesAroundBeforeLoading * tileSize) {
            System.out.println("DOWN");
        }


        return new int[1][1];
    }

    public int[][] getTilesToShowChangingStartPoint(int[][] chunk) {
        //TODO: Add the loading of around chunks when leaving a new one, have to handle 2 cases, when on 2 chunk (either Y axis or X axis) or 4 chunks (center, Y, X and the center of Y and X)
        int aroundTilesOffset = 2;

        int centerX = centerXCoord;
        int centerY = centerYCoord;

        int numbTilesLeft  = centerX / tileSize + aroundTilesOffset;
        int numbTilesUp    = centerY / tileSize + aroundTilesOffset;
        int numbTilesRight = (resolutionW  - centerX) / tileSize + aroundTilesOffset;
        int numbTilesDown  = (resolutionH - centerY) / tileSize + aroundTilesOffset;

        startTilesTotalX = centerX - numbTilesLeft * tileSize;
        startTilesTotalY = centerY - numbTilesUp * tileSize;

        getAroundChunk(chunk, aroundTilesOffset);

        int rows = numbTilesUp + numbTilesDown;
        int cols = numbTilesLeft + numbTilesRight;

        int[][] visibleTiles = new int[rows][cols];

        int chunkCenterRow = chunk.length / 2;
        int chunkCenterCol = chunk[0].length / 2;

        int chunkRow = 32;
        int chunkCol = 32;

        for (int i = 0 ; i < rows ; i++ ) {
            for (int j = 0 ; j < cols ; j++) {
                chunkRow = chunkCenterRow - numbTilesUp + i;
                chunkCol = chunkCenterCol - numbTilesLeft + j;

                if (chunkRow >= 0 && chunkRow < chunk.length &&
                    chunkCol >= 0 && chunkCol < chunk[chunkRow].length) {

                    visibleTiles[i][j] = chunk[chunkRow][chunkCol];
                } else {
                    visibleTiles[i][j] = 0;

                }
            }
        }

        return visibleTiles;
    }

    public int[][] getChunkData(int i, int j, String gameName) {
        System.out.println(i + "-" + j);
        String pathToGameSaves = "Saves/"+gameName+"/";

        activeChunk = new int[]{i, j};

        FileHandle file = Gdx.files.local(pathToGameSaves + "Chunks/Chunk " + i + "-" + j + ".json");
        JsonValue root = json.fromJson(null, file);

        JsonValue chunkData = root.get("ChunkData");
        int nbRows = chunkData.size;
        int[][] dataChunk = new int[nbRows][];
        for (int k = 0; k < nbRows; k++) {
            int nbCols = chunkData.get(k).size;
            dataChunk[k] = new int[nbCols];
            for (int l = 0; l < nbCols; l++) {
                dataChunk[k][l] = chunkData.get(k).getInt(l);
            }
        }

        return dataChunk;
    }

    public int[][] drawTiledMapReturningIntMap(SpriteBatch batch) {

        int[][] showingRenderedMap = getTilesToShowChangingStartPoint(tiledVisibleIntMap);

        for (int i = 0; i < showingRenderedMap.length; i++) {
            for (int j = 0; j < showingRenderedMap[i].length; j++) {

                int placeTileX = startTilesTotalX + j * tileSize;
                int placeTileY = startTilesTotalY + i * tileSize;

                TextureRegion tex = tilesDictionary.get(showingRenderedMap[i][j]);
                if (tex != null) {
                    batch.draw(tex, placeTileX, placeTileY, tileSize, tileSize);
                } else {
                    //System.out.println("INVALID TILE ID: " + showingRenderedMap[i][j]);
                    //batch.draw(dictionary.get(2), placeTileX, placeTileY, tileSize, tileSize);

                }
            }
        }

        // drawSmalledTiledPlacingMap(batch);

        return tiledVisibleIntMap;
    }

    public void drawSmalledTiledPlacingMap(SpriteBatch batch) {    //// This will be only used for debug purposes
        for (int i = 0; i < tiledVisibleIntMap.length; i++) {
            for (int j = 0; j < tiledVisibleIntMap[i].length; j++) {
                for (int k = 0; k < (tileSize / smallTiledSize)*(tileSize/smallTiledSize); k++) {

                    int tileXPos = k % (tileSize / smallTiledSize) * smallTiledSize + j * tileSize;
                    int tileYPos = k / (tileSize / smallTiledSize) * smallTiledSize + i * tileSize;
                    batch.draw(tilesDictionary.get(1), tileXPos, tileYPos, smallTiledSize, smallTiledSize);

                }

            }
        }
    }

    public void handleMapMovement(float delta) {
        int currSpeed = speed;

        float dx = 0;
        float dy = 0;

        if (left) dx += 1;
        if (right) dx -= 1;
        if (up) dy += 1;
        if (down) dy -= 1;
        if (shifted) currSpeed = (int) (currSpeed * speedShiftMultiplier);

        float length = (float)Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            dx /= length;
            dy /= length;
        }

        centerXCoord += (int) (dx * currSpeed * delta);
        centerYCoord += (int) (dy * currSpeed * delta);
    }
}
