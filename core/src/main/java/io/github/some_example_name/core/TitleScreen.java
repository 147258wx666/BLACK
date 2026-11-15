package io.github.some_example_name.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.data.GameState;
import io.github.some_example_name.data.SaveData;
import io.github.some_example_name.systems.SaveManager;
import io.github.some_example_name.systems.ScreenFilterSystem;

public class TitleScreen implements Screen {
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private Texture background;
    private BitmapFont font;
    private GlyphLayout layout;
    private Texture whitePixel;

    private boolean isMenuVisible = false;
    private int selectedIndex = 0;
    private final String[] options = {"Start Game", "Load Game", "Quit"};

    private Stage uiStage;
    private SaveMenu loadMenu;
    private boolean isShowingLoad = false;

    public TitleScreen() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        batch = new SpriteBatch();

        layout = new GlyphLayout();
        font = new BitmapFont(
            Gdx.files.internal("data/start.fnt"),
            new TextureRegion(new Texture(Gdx.files.internal("data/start.png")))
        );
        font.getData().setScale(0.7f);
        background = new Texture(Gdx.files.internal("images/start.jpg"));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        uiStage = new Stage(new FitViewport(1280, 720));
        loadMenu = new SaveMenu();
        loadMenu.setMode(false);
        loadMenu.setFillParent(true);
        loadMenu.setVisible(false);
        uiStage.addActor(loadMenu);

        loadMenu.setOnSlotSelectedListener(slot -> {
            SaveData data = SaveManager.loadGame(slot);
            if (data != null) {
                GameState.inventory.clear();
                if (data.inventory != null) GameState.inventory.addAll(data.inventory);
                // 读档时必须清除剧情锁，否则锁死状态会被带进新游戏
                GameState.unlockStory();
                ScreenFilterSystem.setCorruption(data.corruptionLevel);
                io.github.some_example_name.entities.Player.Gender gender =
                    "FEMALE".equals(data.playerGender)
                        ? io.github.some_example_name.entities.Player.Gender.FEMALE
                        : io.github.some_example_name.entities.Player.Gender.MALE;
                io.github.some_example_name.systems.ScreenManager.switchTo(
                    new WorldScreen(data.mapPath, data.playerX, data.playerY, gender));
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        if (isShowingLoad) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                isShowingLoad = false;
                loadMenu.setVisible(false);
                isMenuVisible = true;
                return;
            }

            loadMenu.update(delta);

            uiStage.act(delta);
            uiStage.draw();
            return;
        }
        if (!isMenuVisible) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                isMenuVisible = true;
                return;
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                selectedIndex--;
                if (selectedIndex < 0) selectedIndex = options.length - 1;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                selectedIndex++;
                if (selectedIndex >= options.length) selectedIndex = 0;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                String choice = options[selectedIndex];

                if (choice.equals("Start Game")) {
                    io.github.some_example_name.systems.ScreenManager.switchTo(new CharacterSelectScreen());

                } else if (choice.equals("Load Game")) {
                    isMenuVisible = false;
                    isShowingLoad = true;
                    loadMenu.setVisible(true);
                    loadMenu.refreshSlots();
                    loadMenu.selectLatestSave();
                } else if (choice.equals("Quit")) {
                    Gdx.app.exit();
                }
            }
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        float vW = viewport.getWorldWidth();
        float vH = viewport.getWorldHeight();

        batch.begin();
        batch.draw(background, 0, 0, vW, vH);
        batch.end();

        if (isMenuVisible) {
            drawMenu(vW, vH);
        } else {
            drawPressAnyKey(vW, vH);
        }
    }

    private void drawPressAnyKey(float vW, float vH) {
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "Press Any Key...");
        float textX = (vW - layout.width) / 2f;
        float textY = vH / 4f;
        font.draw(batch, layout, textX, textY);
        batch.end();
    }

    private void drawMenu(float vW, float vH) {
        float menuWidth = 400;
        float menuHeight = 300;
        float menuX = (vW - menuWidth) / 2f;
        float menuY = (vH - menuHeight) / 2f;

        // 黑底
        batch.begin();
        batch.setColor(0, 0, 0, 0.8f);
        batch.draw(whitePixel, menuX, menuY, menuWidth, menuHeight);
        batch.setColor(1, 1, 1, 1);
        batch.end();

        // 文字和红框
        batch.begin();
        for (int i = 0; i < options.length; i++) {
            layout.setText(font, options[i]);
            float textX = menuX + (menuWidth - layout.width) / 2f;
            float textY = menuY + 220 - (i * 70);

            if (i == selectedIndex) {
//                font.setColor(Color.YELLOW); // 选中变黄

                float boxW = 240;
                float boxH = 45;
                float boxX = menuX + (menuWidth - boxW) / 2f;
                float boxY = textY - 32;

                batch.setColor(0.6f, 0.1f, 0.1f, 1f); // 红框
                batch.draw(whitePixel, boxX, boxY + boxH, boxW, 2); // 上
                batch.draw(whitePixel, boxX, boxY, boxW, 2);        // 下
                batch.draw(whitePixel, boxX, boxY, 2, boxH);        // 左
                batch.draw(whitePixel, boxX + boxW, boxY, 2, boxH); // 右
                batch.setColor(1, 1, 1, 1);
            } else {
                font.setColor(Color.LIGHT_GRAY);
            }
            font.draw(batch, layout, textX, textY);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        background.dispose();
        whitePixel.dispose();
        loadMenu.dispose();
        uiStage.dispose();
    }
}
