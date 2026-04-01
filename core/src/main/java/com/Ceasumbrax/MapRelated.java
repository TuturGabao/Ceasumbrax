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

import static java.lang.Math.abs;

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
    int aroundTilesOffset = -1;

    int[][] aroundChunk;

    int[][][] aroundChunks = new int[3][][];

    public boolean leftBuff;
    public boolean rightBuff;
    public boolean upBuff;
    public boolean downBuff;

    public int[][][] bufferedDataChunk;
    boolean changedChunk = false;
    int[][] newChunkVisibleTiles;
    int newStartXForNewChunk;
    int newStartYForNewChunk;

    HashMap<String, int[]> positionsToRelativeCoord = new HashMap<>();

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
        initialisePositionMap();

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

    public void initialisePositionMap() {
        positionsToRelativeCoord.put("UP", new int[]{0, -1});
        positionsToRelativeCoord.put("DOWN", new int[]{0, 1});
        positionsToRelativeCoord.put("RIGHT", new int[]{1, 0});
        positionsToRelativeCoord.put("LEFT", new int[]{-1, 0});

        positionsToRelativeCoord.put("UP-LEFT", new int[]{-1, -1});
        positionsToRelativeCoord.put("UP-RIGHT", new int[]{1, -1});
        positionsToRelativeCoord.put("DOWN-LEFT", new int[]{-1, 1});
        positionsToRelativeCoord.put("DOWN-RIGHT", new int[]{1, 1});
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

    public Object[] getAroundChunk(int[][] chunk, int tilesAroundBeforeLoading /*, int nbTilesUp, int nbTilesDown, int nbTilesLeft, int nbTilesRight*/) {

        boolean up = false;
        boolean down = false;
        boolean right = false;
        boolean left = false;

        int wholeMapLeftBorder = centerXCoord - chunk[0].length/2*tileSize;
        int wholeMapRightBorder = centerXCoord + chunk[0].length/2*tileSize;

        int wholeMapUpBorder = centerYCoord - chunk.length/2*tileSize;
        int wholeMapDownBorder = centerYCoord + chunk.length/2*tileSize;

        if (wholeMapLeftBorder > -tilesAroundBeforeLoading * tileSize) {
            left = true;
        }
        if (wholeMapRightBorder < resolutionW-tilesAroundBeforeLoading * tileSize) {
            right = true;
        }
        if (wholeMapUpBorder > -tilesAroundBeforeLoading * tileSize) {
            up = true;
        }
        if (wholeMapDownBorder < resolutionH-tilesAroundBeforeLoading * tileSize) {
            down = true;
        }

        int i = 0;
        int sum = 0;

        if (up) sum++;
        if (down) sum++;
        if (right) sum++;
        if (left) sum++;

        if (left && up) sum++;
        if (left && down) sum++;
        if (right && up) sum++;
        if (right && down) sum++;

        bufferedDataChunk = aroundChunks.clone();

        aroundChunks = new int[sum][][];
        String[] positions = new String[sum];

        if (up) {
            if (!upBuff) {
                aroundChunks[i] = getChunkData(chunkBaseRow-1, chunkBaseCol, gameName);
            } else {aroundChunks[i] = bufferedDataChunk[i];} positions[i] = "UP"; i++;}

        if (down) {
            if (!downBuff) {
                aroundChunks[i] = getChunkData(chunkBaseRow+1, chunkBaseCol, gameName);
            } else {aroundChunks[i] = bufferedDataChunk[i];} positions[i] = "DOWN"; i++;}

        if (right) {
            if (!rightBuff) {
                aroundChunks[i] = getChunkData(chunkBaseRow, chunkBaseCol+1, gameName);
            } else {aroundChunks[i] = bufferedDataChunk[i];} positions[i] = "RIGHT"; i++;}

        if (left) {
            if (!leftBuff) {
                aroundChunks[i] = getChunkData(chunkBaseRow, chunkBaseCol-1, gameName);
            } else {aroundChunks[i] = bufferedDataChunk[i];} positions[i] = "LEFT"; i++;}

        //Handling of diagonal
        if (left && up) {
            if (!(leftBuff && upBuff)) {
                aroundChunks[i] = getChunkData(chunkBaseRow-1, chunkBaseCol-1, gameName);
            } else {aroundChunks[i] = bufferedDataChunk[i];} positions[i] = "UP-LEFT"; i++;}

        if (left && down) {
            if (!(leftBuff && downBuff)) {
                aroundChunks[i] = getChunkData(chunkBaseRow+1, chunkBaseCol-1, gameName);
            } else {aroundChunks[i] = bufferedDataChunk[i];} positions[i] = "DOWN-LEFT"; i++;}

        if (right && up) {
            if (!(rightBuff && upBuff)) {
                aroundChunks[i] = getChunkData(chunkBaseRow-1, chunkBaseCol+1, gameName);
            } else {aroundChunks[i] = bufferedDataChunk[i];} positions[i] = "UP-RIGHT"; i++;}

        if (right && down) {
            if (!(rightBuff && downBuff)) {
                aroundChunks[i] = getChunkData(chunkBaseRow+1, chunkBaseCol+1, gameName);
            } else {aroundChunks[i] = bufferedDataChunk[i];} positions[i] = "DOWN-RIGHT"; i++;}

        upBuff = up;
        downBuff = down;
        leftBuff = left;
        rightBuff = right;

        return new Object[]{positions, aroundChunks};
    }

    public Object[][] drawAroundChunksBasedOfCenter(int[][][] aroundChunks, String[] positions) {
        /*TODO: Resolve bug when switching chunk, doesn't render properly,
        *   remove a whole chunk.
        *       Debug session to see why not loading. A whole chunk is missing:
        *       see the buffers, "visibleTiles" and "tiledVisibleIntMap".
        */
        changedChunk = false;
        Object[][] objectsToReturn = new Object[positions.length][];

        int i = 0;
        for (String pos: positions) {
            int[][] aroundChunk = aroundChunks[i];

            int[] vector = positionsToRelativeCoord.get(pos);

            int newCenterX = centerXCoord + vector[0] * chunkWidth * tileSize;
            int newCenterY = centerYCoord + vector[1] * chunkHeight * tileSize;

            Object[] returnObjects = getTilesToShow(aroundChunk, newCenterX, newCenterY, false);

            int[][] visibleTiles = (int[][]) returnObjects[0];
            int newStartX = (int) returnObjects[1];
            int newStartY = (int) returnObjects[2];

            objectsToReturn[i] = new Object[]{visibleTiles, newStartX, newStartY};

            if (abs(centerXCoord - resolutionW / 2) > abs(newCenterX - resolutionW / 2)) {
                chunkBaseCol += vector[0];
                centerXCoord = newCenterX;
                changedChunk = true;
                newChunkVisibleTiles = visibleTiles;
                newStartXForNewChunk = newStartX;
                newStartYForNewChunk = newStartY;
            }
            if (abs(centerYCoord - resolutionH / 2) > abs(newCenterY - resolutionH / 2)) {
                chunkBaseRow += vector[1];
                centerYCoord = newCenterY;
                changedChunk = true;
                newChunkVisibleTiles = visibleTiles;
                newStartXForNewChunk = newStartX;
                newStartYForNewChunk = newStartY;
            }

            if (changedChunk) {
                System.out.println("CURR CHUNK: " + chunkBaseRow + "-" + chunkBaseCol);
                System.out.println("LEFT CHUNK CENTER COORD: " + newCenterX + "-" + newStartY);
                System.out.println("CURR CHUNK CENTER COORD: " + centerXCoord + "-" + centerYCoord);
            }
            i++;
        }


        return objectsToReturn;

    }

    public Object[] getTilesToShow(int[][] chunk, int chunkCenterX, int chunkCenterY, boolean invert) {

        int numbTilesLeft = chunkCenterX / tileSize + aroundTilesOffset;
        int numbTilesUp = chunkCenterY / tileSize + aroundTilesOffset;
        int numbTilesRight = (resolutionW - chunkCenterX) / tileSize + aroundTilesOffset;
        int numbTilesDown = (resolutionH - chunkCenterY) / tileSize + aroundTilesOffset;


        int startTilesTotalX = chunkCenterX - numbTilesLeft * tileSize;
        int startTilesTotalY = chunkCenterY - numbTilesUp * tileSize;

        int rows = numbTilesUp + numbTilesDown;
        int cols = numbTilesLeft + numbTilesRight;

        int[][] visibleTiles = new int[rows][cols];

        int chunkCenterRow = chunk.length / 2;
        int chunkCenterCol = chunk[0].length / 2;

        int chunkRow;
        int chunkCol;

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

        return new Object[]{visibleTiles, startTilesTotalX, startTilesTotalY};
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

        Object[] returnObjects = getTilesToShow(tiledVisibleIntMap, centerXCoord, centerYCoord, false);

        int[][] showingRenderedMap = (int[][]) returnObjects[0];

        startTilesTotalX = (int) returnObjects[1];
        startTilesTotalY = (int) returnObjects[2];

        drawChunk(showingRenderedMap, startTilesTotalX, startTilesTotalY, batch);

        Object[] aroundChunkData = getAroundChunk(tiledVisibleIntMap, aroundTilesOffset);

        String[] positions = (String[]) aroundChunkData[0];
        int[][][] aroundChunks = (int[][][]) aroundChunkData[1];

        Object[][] aroundChunksReturnObjects = drawAroundChunksBasedOfCenter(aroundChunks, positions);

        for (Object[] currAroundChunkData: aroundChunksReturnObjects) {
            int[][] aroundChunk = (int[][]) currAroundChunkData[0];
            int currChunkStartX = (int) currAroundChunkData[1];
            int currChunkStartY = (int) currAroundChunkData[2];

            drawChunk(aroundChunk, currChunkStartX, currChunkStartY, batch);
        }

        // drawSmalledTiledPlacingMap(batch);

        System.out.println("BEFORE SWITCH - centerX: " + centerXCoord + " centerY: " + centerYCoord);
        System.out.println("BEFORE SWITCH - chunkBase: " + chunkBaseRow + "-" + chunkBaseCol);
        System.out.println("BEFORE SWITCH - startX: " + startTilesTotalX + " startY: " + startTilesTotalY);

        if (changedChunk) {
            tiledVisibleIntMap = newChunkVisibleTiles;
            startTilesTotalX = newStartXForNewChunk;
            startTilesTotalY = newStartYForNewChunk;

            System.out.println("AFTER SWITCH - centerX: " + centerXCoord + " centerY: " + centerYCoord);
            System.out.println("AFTER SWITCH - startX: " + startTilesTotalX + " startY: " + startTilesTotalY);
            System.out.println("AFTER SWITCH - tiledVisibleIntMap size: " + tiledVisibleIntMap.length + "x" + tiledVisibleIntMap[0].length);
        }

        return tiledVisibleIntMap;
    }

    public void drawChunk(int[][] chunk, int startX, int startY, SpriteBatch batch) {

        for (int i = 0; i < chunk.length; i++) {
            for (int j = 0; j < chunk[i].length; j++) {

                int placeTileX = startX + j * tileSize;
                int placeTileY = startY + i * tileSize;

                TextureRegion tex = tilesDictionary.get(chunk[i][j]);
                if (tex != null) {
                    batch.draw(tex, placeTileX, placeTileY, tileSize, tileSize);
                } else {
                    //System.out.println("INVALID TILE ID: " + showingRenderedMap[i][j]);
                    //batch.draw(dictionary.get(2), placeTileX, placeTileY, tileSize, tileSize);

                }
            }

        }
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
