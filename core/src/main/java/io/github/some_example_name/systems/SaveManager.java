package io.github.some_example_name.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import io.github.some_example_name.data.SaveData;

public class SaveManager {
    private static final String SAVE_DIR = "saves/";
    private static final Json json = new Json();

    public static void saveGame(int slot, SaveData data) {
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();

        data.saveTime = getCurrentTime();
        FileHandle file = Gdx.files.local(SAVE_DIR + "save_" + slot + ".json");
        file.writeString(json.prettyPrint(data), false);
    }

    public static SaveData loadGame(int slot) {
        FileHandle file = Gdx.files.local(SAVE_DIR + "save_" + slot + ".json");
        if (file.exists()) {
            return json.fromJson(SaveData.class, file.readString());
        }
        return null;
    }

    public static boolean hasSave(int slot) {
        return Gdx.files.local(SAVE_DIR + "save_" + slot + ".json").exists();
    }

    private static String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date());
    }
}
