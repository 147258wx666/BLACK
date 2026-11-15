package io.github.some_example_name.core;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.data.SaveData;
import io.github.some_example_name.systems.SaveManager;

public class SaveMenu extends Table {
    private int selectedIndex = 0;
    private final int MAX_SLOTS = 15;
    private Array<Label> arrows = new Array<>();
    private Array<Table> slotTables = new Array<>();
    private ScrollPane scrollPane;
    private boolean isSaveMode = true; // true为存档，false为读档

    private Texture bgTexture;
    private BitmapFont font;
    private Texture fontTexture;
    private Texture slotBgTexture;
    private Texture maleAvatarTexture;
    private Texture femaleAvatarTexture;
    private Texture defaultAvatarTexture; // 无存档时显示的占位头像
    private Array<Image> avatarImages = new Array<>();

    public interface OnSlotSelectedListener {
        void onSlotSelected(int slot);
    }
    private OnSlotSelectedListener listener;

    public SaveMenu() {
        // 背景底板
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.1f, 0.1f, 0.1f, 0.95f));
        pixmap.fill();
        bgTexture = new Texture(pixmap);
        setBackground(new TextureRegionDrawable(bgTexture));
        pixmap.dispose();

        fontTexture = new Texture(Gdx.files.internal("PauseData/zi.png"));
        font = new BitmapFont(Gdx.files.internal("PauseData/zi.fnt"), new TextureRegion(fontTexture));
        font.getData().setScale(1.5f);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        // 每个存档框的背景（共用一张 Texture）
        Pixmap slotBgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        slotBgPixmap.setColor(Color.DARK_GRAY);
        slotBgPixmap.fill();
        slotBgTexture = new Texture(slotBgPixmap);
        slotBgPixmap.dispose();

        // 男女头像纹理，根据存档性别切换显示
        maleAvatarTexture = new Texture(Gdx.files.internal("images/malePlayerHead.jpg"));
        femaleAvatarTexture = new Texture(Gdx.files.internal("images/femalePlayerHead.jpg"));
        defaultAvatarTexture = maleAvatarTexture; // 无存档时的占位头像

        Table listTable = new Table();
        for (int i = 0; i < MAX_SLOTS; i++) {
            Table slotBox = new Table();
            slotBox.setBackground(new TextureRegionDrawable(slotBgTexture));

            Label arrow = new Label(">", style);
            arrow.setColor(Color.YELLOW);
            arrow.setVisible(i == 0);
            arrows.add(arrow);

            Image avatar = new Image(new TextureRegionDrawable(defaultAvatarTexture));
            avatarImages.add(avatar);
            Label text = new Label("empty " + (i + 1), style);
            text.setColor(i == 0 ? Color.YELLOW : Color.WHITE);

            slotBox.add(arrow).width(40);
            slotBox.add(avatar).width(60).height(60).padRight(20);
            slotBox.add(text).left().expandX().padLeft(20);

            slotTables.add(slotBox);
            listTable.add(slotBox).width(800).height(100).padBottom(10).row();
        }

        scrollPane = new ScrollPane(listTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        top();
        add(scrollPane).width(850).height(680).padTop(20);
        setVisible(false);

        refreshSlots();
    }

    // ================= 核心方法 =================

    public void setMode(boolean isSaveMode) {
        this.isSaveMode = isSaveMode;
        refreshSlots();
    }

    public void setOnSlotSelectedListener(OnSlotSelectedListener listener) {
        this.listener = listener;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void triggerSelection(int slot) {
        if (listener != null) {
            listener.onSlotSelected(slot);
        }
    }

    // 👇 新增：自动选中最近的存档
    public void selectLatestSave() {
        int latestSlot = 0;
        long latestTime = 0;
        boolean found = false;

        for (int i = 0; i < MAX_SLOTS; i++) {
            FileHandle file = Gdx.files.local("saves/save_" + (i + 1) + ".json");
            if (file.exists()) {
                long lastMod = file.lastModified();
                if (lastMod > latestTime) {
                    latestTime = lastMod;
                    latestSlot = i;
                    found = true;
                }
            }
        }
        this.selectedIndex = found ? latestSlot : 0;
        updateVisuals();
    }

    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = MAX_SLOTS - 1;
            updateVisuals();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex++;
            if (selectedIndex >= MAX_SLOTS) selectedIndex = 0;
            updateVisuals();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            triggerSelection(selectedIndex + 1);
        }
    }

    public void resetSelection() {
        this.selectedIndex = 0;
        updateVisuals();
    }

    public void refreshSlots() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            Table slotBox = slotTables.get(i);
            Label textLabel = (Label) slotBox.getCells().get(2).getActor();
            SaveData data = SaveManager.loadGame(i + 1);

            if (data != null) {
                textLabel.setText((isSaveMode ? "overwrite: " : "load: ") + data.saveTime);
                // 根据存档中的性别切换头像
                Texture avatarTex = "FEMALE".equals(data.playerGender)
                    ? femaleAvatarTexture
                    : maleAvatarTexture;
                avatarImages.get(i).setDrawable(new TextureRegionDrawable(avatarTex));
            } else {
                textLabel.setText(isSaveMode ? "empty " + (i + 1) : "none " + (i + 1));
                // 无存档时显示占位头像
                avatarImages.get(i).setDrawable(new TextureRegionDrawable(defaultAvatarTexture));
            }
        }
    }

    private void updateVisuals() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            arrows.get(i).setVisible(i == selectedIndex);
            Label text = (Label) slotTables.get(i).getCells().get(2).getActor();
            text.setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
        }
        Table selectedSlot = slotTables.get(selectedIndex);
        scrollPane.scrollTo(0, selectedSlot.getY(), selectedSlot.getWidth(), selectedSlot.getHeight(), false, true);
        scrollPane.updateVisualScroll();
    }

    public void dispose() {
        bgTexture.dispose();
        font.dispose();
        fontTexture.dispose();
        slotBgTexture.dispose();
        maleAvatarTexture.dispose();
        femaleAvatarTexture.dispose();
    }
}
