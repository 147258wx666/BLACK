package io.github.some_example_name.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.some_example_name.data.GameState;
import io.github.some_example_name.data.SaveData;
import io.github.some_example_name.entities.BaseNPC;
import io.github.some_example_name.entities.EntityFactory;
import io.github.some_example_name.entities.Player;
import io.github.some_example_name.systems.DialogueSystem;
import io.github.some_example_name.systems.MapManager;
import io.github.some_example_name.systems.SaveManager;
import io.github.some_example_name.systems.ScreenFilterSystem;
import io.github.some_example_name.systems.ScreenManager;

import java.util.Arrays;
import java.util.LinkedList;

public class WorldScreen implements Screen {
    private OrthographicCamera camera;
    private FitViewport viewport;
    private Stage worldStage;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapManager mapManager;
    private ScreenFilterSystem filterSystem;
    private DialogueSystem dialogueSystem;

    private Player player;
    private Array<BaseNPC> npcList = new Array<>();
    private Vector3 cameraTarget = new Vector3();

    private String currentMapPath;

    private Stage uiStage;
    private DialogueBox dialogueBox;
    private ChoiceBox choiceBox;

    //菜单
    private PauseMenu pauseMenu;
    private SaveMenu saveMenu;

    /** 输入冷却：screen 切换后前几帧跳过输入，防止按键残留 */
    private float inputCooldown = 0;

    /** 兼容旧调用，默认男性 */
    public WorldScreen(String mapPath, float spawnX, float spawnY) {
        this(mapPath, spawnX, spawnY, io.github.some_example_name.entities.Player.Gender.MALE);
    }

    public WorldScreen(String mapPath, float spawnX, float spawnY, io.github.some_example_name.entities.Player.Gender gender) {
        // 1. 初始化环境
        this.currentMapPath = mapPath;
        this.map = new TmxMapLoader().load(mapPath);
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);
        this.mapManager = new MapManager();
        this.mapManager.extractData(map);

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(640, 480, camera);
        this.worldStage = new Stage(viewport);
        this.filterSystem = new ScreenFilterSystem();
        //初始化玩家
        this.player = new Player(spawnX, spawnY, gender);
        worldStage.addActor(player);
        camera.position.set(spawnX, spawnY, 0);

        setupEntities(mapPath);

        //对话框初始化
        uiStage = new Stage(new FitViewport(1280, 720));
        dialogueBox = new DialogueBox();
        dialogueBox.setBounds(90, 40, 1100, 250);
        choiceBox = new ChoiceBox();
        choiceBox.setBounds(90, 300, 1100, 400);
        uiStage.addActor(dialogueBox);
        uiStage.addActor(choiceBox);
        this.dialogueSystem = new DialogueSystem(uiStage, dialogueBox, choiceBox);
        //初始化菜单
        pauseMenu = new PauseMenu(gender);
        pauseMenu.setBounds(0, 0, 1280, 720);
        uiStage.addActor(pauseMenu);

        //存档界面
        saveMenu = new SaveMenu();
        saveMenu.setBounds(0, 0, 1280, 720);
        uiStage.addActor(saveMenu);

        // 绑定存档触发逻辑
        saveMenu.setOnSlotSelectedListener(slot -> {
            // 剧情锁/对话进行中禁止保存，防止把锁死状态写进存档
            if (GameState.isStoryActive || dialogueSystem.isTalking()) {
                System.out.println("【系统】剧情进行中，无法保存");
                return;
            }
            // 1. 打包当前游戏数据
            SaveData data = new SaveData();
            data.mapPath = currentMapPath;
            data.playerX = player.getX();
            data.playerY = player.getY();
            data.playerGender = (player.getGender() == Player.Gender.FEMALE) ? "FEMALE" : "MALE";
            data.corruptionLevel = ScreenFilterSystem.getCorruptionLevel();
            data.inventory.addAll(io.github.some_example_name.data.GameState.inventory);

            // 2. 交给独立管家保存
            SaveManager.saveGame(slot, data);

            // 3. 刷新一下 UI
            saveMenu.refreshSlots();

            System.out.println("【系统】已保存至槽位 " + slot);
        });
    }


    //加载实体
    private void setupEntities(String mapPath) {
        // 1. 获取地图中的 Interactables 图层
        com.badlogic.gdx.maps.MapLayer layer = map.getLayers().get("Interactables");
        if (layer == null) return;

        // 2. 遍历你在 Tiled 里画的所有对象，丢给工厂去处理
        for (com.badlogic.gdx.maps.MapObject obj : layer.getObjects()) {
            BaseNPC npc = EntityFactory.createNPC(obj);
            if (npc != null) {
                worldStage.addActor(npc); // 加入舞台渲染
                npcList.add(npc);         // 加入统一更新列表
            }
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        inputCooldown = 0.2f; // 切换到此 screen 后 0.2 秒内不处理输入
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1);

        // 输入冷却：切换 screen 后的前几帧跳过所有输入处理
        boolean inputLocked = false;
        if (inputCooldown > 0) {
            inputCooldown -= delta;
            inputLocked = true;
        }

        if (!inputLocked) handleMenuInput(delta);
        boolean isPaused = pauseMenu.isVisible() || saveMenu.isVisible();
       if(!isPaused){
           if (!inputLocked) {
           dialogueSystem.update(delta);
           // 1. 收集输入
           float inputX = 0, inputY = 0;
           if (Gdx.input.isKeyPressed(Input.Keys.W)) inputY += 1;
           if (Gdx.input.isKeyPressed(Input.Keys.S)) inputY -= 1;
           if (Gdx.input.isKeyPressed(Input.Keys.A)) inputX -= 1;
           if (Gdx.input.isKeyPressed(Input.Keys.D)) inputX += 1;
           // 2. 更新玩家
           player.update(delta, inputX, inputY, mapManager);
           // 3. 盲调所有 NPC 的逻辑（完全泛型化）
           for (BaseNPC npc : npcList) {
               npc.updateLogic(delta, player, dialogueSystem);
           }
           // 4. 检查传送门（只查全局状态，不含任何 NPC 具体逻辑）
           checkPortals();
           }
           // 5. 渲染
           worldStage.act(delta);

           cameraTarget.set(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, 0);
           camera.position.lerp(cameraTarget, 0.1f);
           camera.update();

       }


        mapRenderer.setView(camera);
        mapRenderer.render();
        worldStage.draw();
        filterSystem.render();
        uiStage.act(delta);
        uiStage.draw();
    }
    //检测传送触发器
    private void checkPortals() {
        if (dialogueSystem.isTalking() || GameState.isStoryActive) {
            return;
        }

        for (BaseNPC npc : npcList) {
            if (npc.isBlockingPortal()) {
                return;
            }
        }
        Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        for (MapObject portal : mapManager.portals) {
            Rectangle rect = ((RectangleMapObject) portal).getRectangle();
            if (playerRect.overlaps(rect)) {
                String targetMap = portal.getProperties().get("target_map", String.class);
                if (targetMap != null) {
                    Float sx = portal.getProperties().get("spawn_x", Float.class);
                    Float sy = portal.getProperties().get("spawn_y", Float.class);
                    float spawnX = (sx != null) ? sx : 100f;
                    float spawnY = (sy != null) ? sy : 100f;

                    ScreenFilterSystem.addCorruption();
                    Player.Gender gender = player.getGender();
                    Gdx.app.postRunnable(() -> {
                        ScreenManager.switchTo(
                            new WorldScreen(targetMap, spawnX, spawnY, gender));
                    });
                    return;
                }
            }
        }
    }
    //处理菜单输入
    private void handleMenuInput(float delta) {
        // 剧情锁/对话进行中禁止打开菜单，防止把锁死状态存进档
        if (GameState.isStoryActive || dialogueSystem.isTalking()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (saveMenu.isVisible()) {
                saveMenu.setVisible(false);
                pauseMenu.setVisible(true);
            } else if (pauseMenu.isVisible()) {
                pauseMenu.setVisible(false);
            } else {
                pauseMenu.setVisible(true);
                saveMenu.setVisible(false);
                saveMenu.refreshSlots();
            }
            return;
        }

        if (pauseMenu.isVisible()) {
            pauseMenu.update(delta);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                int idx = pauseMenu.getSelectedIndex();
                if (idx == 1) { // 存档
                    pauseMenu.setVisible(false);
                    saveMenu.setVisible(true);
                    saveMenu.resetSelection();
                } else if (idx == 3) { // 结束游戏：返回主菜单
                    ScreenManager.switchTo(new TitleScreen());
                } else {
                    // Backpack(0) / Settings(2) 暂未实现，给出提示
                    pauseMenu.showHint("This feature is coming soon...");
                }
            }
        } else if (saveMenu.isVisible()) {
            saveMenu.update(delta);
        }
    }
    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        player.dispose();
        for (BaseNPC npc : npcList) {
            npc.dispose();
        }
        dialogueBox.dispose();
        choiceBox.dispose();
        pauseMenu.dispose();
        saveMenu.dispose();
        map.dispose();
        mapRenderer.dispose();
        filterSystem.dispose();
        worldStage.dispose();
        uiStage.dispose();
    }
}
