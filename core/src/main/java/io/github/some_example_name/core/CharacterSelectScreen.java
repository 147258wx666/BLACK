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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.data.GameState;
import io.github.some_example_name.entities.Player;
import io.github.some_example_name.systems.ScreenFilterSystem;

/**
 * 性别选择界面：左右键切换、空格/回车确认、ESC 返回标题。
 * 两张立绘并排，选中方正常亮度+红色边框，未选中方暗淡。
 */
public class CharacterSelectScreen implements Screen {
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private Texture whitePixel;

    private Texture malePortrait;
    private Texture femalePortrait;

    private int selectedIndex = 0; // 0 = male, 1 = female
    private final String[] labels = {"Male", "Female"};
    private final String[] portraitPaths = {"lihui/malePlayer.png", "lihui/femalePlayer.png"};

    public CharacterSelectScreen() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        batch = new SpriteBatch();

        layout = new GlyphLayout();
        font = new BitmapFont(
            Gdx.files.internal("data/start.fnt"),
            new TextureRegion(new Texture(Gdx.files.internal("data/start.png")))
        );
        font.getData().setScale(0.7f);

        malePortrait = new Texture(Gdx.files.internal("lihui/malePlayer.png"));
        femalePortrait = new Texture(Gdx.files.internal("lihui/femalePlayer.png"));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // 输入处理
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            selectedIndex = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            selectedIndex = 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            io.github.some_example_name.systems.ScreenManager.switchTo(new TitleScreen());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Player.Gender gender = (selectedIndex == 1) ? Player.Gender.FEMALE : Player.Gender.MALE;
            ScreenFilterSystem.resetCorruption();
            GameState.inventory.clear();
            GameState.unlockStory();
            io.github.some_example_name.systems.ScreenManager.switchTo(
                new WorldScreen("maps/forest1/map.tmx", 50, 480, gender));
            return;
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        float vW = viewport.getWorldWidth();
        float vH = viewport.getWorldHeight();

        // 立绘布局参数
        float portraitW = 360;
        float portraitH = 540;
        float gap = 80;
        float totalW = portraitW * 2 + gap;
        float startX = (vW - totalW) / 2f;
        float portraitY = (vH - portraitH) / 2f + 30;

        batch.begin();

        // 标题文字
        font.setColor(Color.WHITE);
        layout.setText(font, "Choose Your Character");
        font.draw(batch, layout, (vW - layout.width) / 2f, vH - 60);

        // 绘制两张立绘
        for (int i = 0; i < 2; i++) {
            Texture tex = (i == 0) ? malePortrait : femalePortrait;
            float x = startX + i * (portraitW + gap);

            if (i == selectedIndex) {
                // 选中：正常亮度
                batch.setColor(1, 1, 1, 1);
            } else {
                // 未选中：暗淡
                batch.setColor(0.4f, 0.4f, 0.4f, 1);
            }
            batch.draw(tex, x, portraitY, portraitW, portraitH);
            batch.setColor(1, 1, 1, 1);

            // 选中方画红色边框
            if (i == selectedIndex) {
                float borderW = 4;
                batch.setColor(0.6f, 0.1f, 0.1f, 1f);
                batch.draw(whitePixel, x - borderW, portraitY - borderW, portraitW + borderW * 2, borderW); // 下
                batch.draw(whitePixel, x - borderW, portraitY + portraitH, portraitW + borderW * 2, borderW); // 上
                batch.draw(whitePixel, x - borderW, portraitY - borderW, borderW, portraitH + borderW * 2); // 左
                batch.draw(whitePixel, x + portraitW, portraitY - borderW, borderW, portraitH + borderW * 2); // 右
                batch.setColor(1, 1, 1, 1);
            }

            // 性别标签
            font.setColor(i == selectedIndex ? Color.YELLOW : Color.LIGHT_GRAY);
            layout.setText(font, labels[i]);
            font.draw(batch, layout, x + (portraitW - layout.width) / 2f, portraitY - 20);
        }

        // 底部提示
        font.setColor(Color.LIGHT_GRAY);
        layout.setText(font, "< Left / Right > to select    SPACE to confirm    ESC to go back");
        font.draw(batch, layout, (vW - layout.width) / 2f, 40);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        whitePixel.dispose();
        malePortrait.dispose();
        femalePortrait.dispose();
    }
}
