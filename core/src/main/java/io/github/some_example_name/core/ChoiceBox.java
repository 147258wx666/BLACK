package io.github.some_example_name.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.data.DialogueData.ChoiceOption;

/**
 * 对话选择框：在 DialogueBox 上方弹出选项列表。
 * 上下键切换，空格/回车确认。视觉风格与 PauseMenu 一致（箭头 + 黄色高亮）。
 */
public class ChoiceBox extends Group {

    public interface OnChoiceSelectedListener {
        void onChoiceSelected(ChoiceOption option);
    }

    private Table optionTable;
    private Array<Label> arrows = new Array<>();
    private Array<Label> texts = new Array<>();
    private Array<ChoiceOption> currentOptions = new Array<>();

    private int selectedIndex = 0;
    private boolean active = false;
    private OnChoiceSelectedListener listener;

    private Texture bgTexture;
    private BitmapFont font;

    public ChoiceBox() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.85f));
        pixmap.fill();
        bgTexture = new Texture(pixmap);
        pixmap.dispose();

        font = new BitmapFont();
        font.getData().setScale(2.0f);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        optionTable = new Table();
        optionTable.setBackground(new TextureRegionDrawable(bgTexture));
        optionTable.top().left();
        optionTable.pad(20, 30, 20, 30);
        addActor(optionTable);

        setVisible(false);
    }

    public void setListener(OnChoiceSelectedListener listener) {
        this.listener = listener;
    }

    public void show(Array<ChoiceOption> options) {
        this.currentOptions.clear();
        this.currentOptions.addAll(options);
        this.selectedIndex = 0;
        this.active = true;

        // 清除旧内容
        optionTable.clear();
        arrows.clear();
        texts.clear();

        // 构建选项列表
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        for (int i = 0; i < options.size; i++) {
            Label arrow = new Label(">", style);
            arrow.setColor(Color.YELLOW);
            arrow.setVisible(i == 0);
            arrows.add(arrow);

            Label text = new Label(options.get(i).text, style);
            text.setColor(i == 0 ? Color.YELLOW : Color.WHITE);
            texts.add(text);

            optionTable.add(arrow).width(40).padBottom(10);
            optionTable.add(text).left().padBottom(10).row();
        }

        // 定位：放在对话框上方
        layoutPosition();

        setVisible(true);
    }

    public void hide() {
        this.active = false;
        setVisible(false);
    }

    public boolean isActive() {
        return active;
    }

    public void update(float delta) {
        if (!active) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            changeSelection(-1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            changeSelection(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (listener != null) {
                ChoiceOption chosen = currentOptions.get(selectedIndex);
                listener.onChoiceSelected(chosen);
            }
        }
    }

    private void changeSelection(int delta) {
        selectedIndex += delta;
        if (selectedIndex < 0) selectedIndex = currentOptions.size - 1;
        if (selectedIndex >= currentOptions.size) selectedIndex = 0;

        for (int i = 0; i < arrows.size; i++) {
            arrows.get(i).setVisible(i == selectedIndex);
            texts.get(i).setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
        }
    }

    private void layoutPosition() {
        float boxW = getWidth();
        float boxH = optionTable.getPrefHeight();
        // 放在 Group 的上方区域
        optionTable.setBounds(0, getHeight() - boxH - 10, boxW, boxH);
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        if (getWidth() == 0 || getHeight() == 0) return;
        if (active) layoutPosition();
    }

    public void dispose() {
        bgTexture.dispose();
        font.dispose();
    }
}
