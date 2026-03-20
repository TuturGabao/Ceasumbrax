package com.Ceasumbrax;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class DataReader {
    String gameName;
    String pathToPlayerData;

    DataReader(String game) {
        gameName = game;

        pathToPlayerData = "MapRelated/" + game + "/PlayerData.json";
    }

    public int getChunckPlayer() {
        JsonValue root = new JsonReader().parse(Gdx.files.internal(pathToPlayerData));

        return root.get("Position").getInt("PlayerChunkPose");
    }

    public float[] getPosesPlayer() {
        JsonValue root = new JsonReader().parse(Gdx.files.internal(pathToPlayerData));

        float xPose;
        float yPose;

        xPose = root.get("Position").getFloat("PlayerCoordX");
        yPose = root.get("Position").getFloat("PlayerCoordY");

        return new float[]{xPose, yPose};
    }


}
