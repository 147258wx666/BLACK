package io.github.some_example_name.core;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.entities.Player;

public class PauseMenu extends Table {
    private int selectedIndex = 0;
    private final String[] options = {"Backpack", "Save", "Settings", "Exit"};
    private Array<Label> arrows = new Array<>();
    private Array<Label> texts = new Array<>();

    private Texture bgTexture;
    private BitmapFont font;
    private Texture fontTexture;
    private Texture avatarTexture;

    // 未实现功能的提示信息
    private Label hintLabel;
    private float hintTimer = 0;

    public PauseMenu(Player.Gender gender) {
        setFillParent(true);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.85f)); // 85%不透明度的黑底
        pixmap.fill();
        bgTexture = new Texture(pixmap);
        setBackground(new TextureRegionDrawable(bgTexture));
        pixmap.dispose();

        fontTexture = new Texture(Gdx.files.internal("PauseData/zi.png"));
        font = new BitmapFont(Gdx.files.internal("PauseData/zi.fnt"), new TextureRegion(fontTexture));
        font.getData().setScale(1.5f);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        String avatarPath = (gender == Player.Gender.FEMALE)
            ? "images/femalePlayerHead.jpg"
            : "images/malePlayerHead.jpg";
        avatarTexture = new Texture(avatarPath);
        Image avatar = new Image(avatarTexture);

        Table leftTable = new Table();
        leftTable.add(avatar).size(200).padBottom(60).row();

        Table menuTable = new Table();
        for (int i = 0; i < options.length; i++) {
            Label arrow = new Label(">", style);
            arrow.setColor(Color.YELLOW);
            arrow.setVisible(i == 0);
            arrows.add(arrow);

            Label text = new Label(options[i], style);
            text.setColor(i == 0 ? Color.YELLOW : Color.WHITE); // 选中黄色，未选中白色
            texts.add(text);

            menuTable.add(arrow).width(50);
            menuTable.add(text).left().padBottom(25).row();
        }

        hintLabel = new Label("", style);
        hintLabel.setColor(Color.ORANGE);
        hintLabel.setVisible(false);
        menuTable.add().width(50);
        menuTable.add(hintLabel).left().padTop(10).row();

        leftTable.add(menuTable).left();
        add(leftTable).expand().fill().left().padLeft(150);
        setVisible(false);
    }

    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            changeSelection(-1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            changeSelection(1);
        }
        // 提示信息超时自动隐藏
        if (hintTimer > 0) {
            hintTimer -= delta;
            if (hintTimer <= 0) hintLabel.setVisible(false);
        }
    }

    /** 显示一条临时提示，1.5 秒后自动消失 */
    public void showHint(String message) {
        hintLabel.setText(message);
        hintLabel.setVisible(true);
        hintTimer = 1.5f;
    }

    private void changeSelection(int delta) {
        selectedIndex += delta;
        if (selectedIndex < 0) selectedIndex = options.length - 1;
        if (selectedIndex >= options.length) selectedIndex = 0;

        for (int i = 0; i < arrows.size; i++) {
            arrows.get(i).setVisible(i == selectedIndex);
            texts.get(i).setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void dispose() {
        bgTexture.dispose();
        font.dispose();
        fontTexture.dispose();
        avatarTexture.dispose();
    }
}
