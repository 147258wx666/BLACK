package io.github.some_example_name.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;

public class DialogueBox extends Group {
    private Table contentTable;    // 左下角的黑色文字框
    private Image portraitImage;   // 右侧的大半身立绘

    private Label nameLabel;
    private Label textLabel;

    private Texture bgTexture;
    private BitmapFont font;
    private Texture defaultPortrait;
    private Texture currentPortrait;

    /** 立绘缓存：路径 → Texture，避免每次 setAvatar 都 new Texture */
    private ObjectMap<String, Texture> portraitCache = new ObjectMap<>();

    private String fullText = "";
    private float charTimer = 0f;
    private final float CHAR_INTERVAL = 0.04f;
    private int currentCharIndex = 0;

    private boolean isTyping = false;
    private boolean isFinished = false;

    public DialogueBox() {
        // ==========================================
        // 1. 构建左侧文字对话框 (名字在上，文本在下)
        // ==========================================
        contentTable = new Table();
        bgTexture = new Texture(Gdx.files.internal("ui/Dialogbox.PNG"));
        NinePatch patch = new NinePatch(bgTexture, 30, 30, 30, 30);
        contentTable.setBackground(new NinePatchDrawable(patch));

        font = new BitmapFont();
        font.getData().setScale(2.0f);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        nameLabel = new Label("", style);
        nameLabel.setColor(Color.YELLOW);

        textLabel = new Label("", style);
        textLabel.setWrap(true); // 自动换行
        textLabel.setAlignment(Align.topLeft);

        contentTable.top().left();
        contentTable.pad(40, 60, 30, 60); // 内边距，防止文字贴边
        contentTable.add(nameLabel).left().padBottom(15).row();
        contentTable.add(textLabel).expand().fill().top().left();

        addActor(contentTable);

        // ==========================================
        // 2. 构建右侧大半身立绘
        // ==========================================
        defaultPortrait = new Texture(Gdx.files.internal("images/head.jpg"));
        currentPortrait = defaultPortrait;
        portraitImage = new Image(new TextureRegionDrawable(currentPortrait));
        addActor(portraitImage);

        setVisible(false);
    }

    /**
     * 动态更换右侧立绘（在 NPC 的 triggerStory 里调用）
     * @param imagePath 例如 "characters/red_hood_big.png"
     */
    public void setAvatar(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            // 无立绘，使用默认占位图
            currentPortrait = defaultPortrait;
            portraitImage.setDrawable(new TextureRegionDrawable(defaultPortrait));
            return;
        }

        // 从缓存取，没有则加载并缓存
        Texture tex = portraitCache.get(imagePath);
        if (tex == null) {
            tex = new Texture(Gdx.files.internal(imagePath));
            portraitCache.put(imagePath, tex);
        }
        currentPortrait = tex;
        portraitImage.setDrawable(new TextureRegionDrawable(tex));
    }

    /**
     * 【核心】：当 DialogueBox 尺寸改变时（通常外部会设它为全屏），自动计算排版
     */
    @Override
    public void sizeChanged() {
        super.sizeChanged();
        if (getWidth() == 0 || getHeight() == 0) return;

        float screenW = getWidth();
        float screenH = getHeight();

        // 1. 对话框：占据屏幕左侧 65% 的宽度，高度 240，贴底
        float boxW = screenW * 0.80f;
        float boxH = 240f;
        contentTable.setBounds(screenW * 0.05f, 30f, boxW, boxH);

        // 2. 右侧立绘：尺寸设得很大（比如 450x650），自然超出对话框的 240 高度
        float portraitW = 240f;
        float portraitH = 608f;
        portraitImage.setBounds(
            screenW - portraitW - 20f,
            -30f,
            portraitW,
            portraitH
        );
    }

    public void setText(String speaker, String text) {
        this.nameLabel.setText(speaker);
        this.fullText = text;
        this.textLabel.setText("");

        this.currentCharIndex = 0;
        this.charTimer = 0f;
        this.isTyping = true;
        this.isFinished = false;

        setVisible(true);
    }

    public void updateTyping(float delta) {
        if (!isTyping) return;

        charTimer += delta;
        if (charTimer >= CHAR_INTERVAL) {
            charTimer = 0f;
            currentCharIndex++;

            if (currentCharIndex >= fullText.length()) {
                currentCharIndex = fullText.length();
                isTyping = false;
                isFinished = true;
            }
            textLabel.setText(fullText.substring(0, currentCharIndex));
        }
    }

    public void skipTyping() {
        if (isTyping) {
            currentCharIndex = fullText.length();
            textLabel.setText(fullText);
            isTyping = false;
            isFinished = true;
        }
    }

    public boolean isFinished() { return isFinished; }
    public boolean isTyping() { return isTyping; }

    public void dispose() {
        bgTexture.dispose();
        font.dispose();
        // 释放所有缓存的立绘
        for (Texture tex : portraitCache.values()) {
            tex.dispose();
        }
        portraitCache.clear();
        defaultPortrait.dispose();
    }
}
