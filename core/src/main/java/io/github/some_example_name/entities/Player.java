package io.github.some_example_name.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.some_example_name.data.GameState;
import io.github.some_example_name.systems.MapManager;

public class Player extends Actor {

    public enum Gender { MALE, FEMALE }

    private Gender gender;
    public enum State { IDLE, UP, DOWN, LEFT, RIGHT }

    private State currentState = State.IDLE;
    private float stateTime = 0f;
    private float speed = 250;

    // 碰撞箱与状态
    private Rectangle bounds;
    private boolean isLocked = false;

    // 动画对象
    private Animation<TextureRegion> animDown, animLeft, animUp, animRight;
    private Texture texture;

    public Player(float x, float y, Gender gender) {
        this.gender = gender;
        setBounds(x, y, 38, 64);
        bounds = new Rectangle(x, y, 38, 48);

        // 1. 根据性别选择序列帧
        String texturePath = (gender == Gender.FEMALE) ? "images/player2.png" : "images/player.png";
        texture = new Texture(texturePath);
        TextureRegion[][] temp = TextureRegion.split(texture, texture.getWidth() / 4, texture.getHeight() / 4);

        // 2. 下左上右
        animDown = createAnim(temp[0]);
        animLeft = createAnim(temp[1]);
        animUp = createAnim(temp[3]);
        animRight = createAnim(temp[2]);
    }

    /** 兼容旧调用，默认男性 */
    public Player(float x, float y) {
        this(x, y, Gender.MALE);
    }

    public Gender getGender() {
        return gender;
    }

    public void dispose() {
        texture.dispose();
    }

    private Animation<TextureRegion> createAnim(TextureRegion[] frames) {
        Animation<TextureRegion> anim = new Animation<>(0.15f, frames);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        return anim;
    }
    public void lock() {
        this.isLocked = true;
        this.currentState = State.IDLE; // 锁定时强制待机
        this.stateTime = 0;
    }

    public void unlock() {
        this.isLocked = false;
    }

    public boolean isLocked() {
        return isLocked;
    }

    // 供外部直接调用移动（比如 NPC 拉扯玩家、或者剧情强制位移）
    public void moveByRequest(float dx, float dy) {
        if (isLocked) return; // 如果被锁，拒绝移动

        if (dx == 0 && dy == 0) {
            currentState = State.IDLE;
            stateTime = 0;
            return;
        }

        // 判断朝向
        if (Math.abs(dx) > Math.abs(dy)) {
            currentState = dx > 0 ? State.RIGHT : State.LEFT;
        } else {
            currentState = dy > 0 ? State.UP : State.DOWN;
        }

        stateTime += Gdx.graphics.getDeltaTime();
        moveBy(dx, dy);
        bounds.setPosition(getX(), getY());
    }

    // ==========================================
    // 核心更新逻辑 (WorldScreen 每帧调用)
    // ==========================================

    public void update(float delta, float inputX, float inputY, MapManager mapManager) {
        // 如果被剧情锁定，直接返回，不处理任何输入
        if (GameState.isStoryActive) {
            return;
        }

        float dx = 0;
        float dy = 0;

        // 1. 判定状态
        if (inputX != 0 || inputY != 0) {
            if (inputX > 0) currentState = State.RIGHT;
            else if (inputX < 0) currentState = State.LEFT;
            else if (inputY > 0) currentState = State.UP;
            else if (inputY < 0) currentState = State.DOWN;

            // 归一化，防止斜走变快
            float length = (float) Math.sqrt(inputX * inputX + inputY * inputY);
            dx = (inputX / length) * speed * delta;
            dy = (inputY / length) * speed * delta;
        } else {
            currentState = State.IDLE;
        }

        // 2. 分轴碰撞检测
        // X轴
        bounds.x += dx;
        for (Rectangle wall : mapManager.walls) {
            if (bounds.overlaps(wall)) { dx = 0; break; }
        }
        bounds.x -= dx;

        // Y轴
        bounds.y += dy;
        for (Rectangle wall : mapManager.walls) {
            if (bounds.overlaps(wall)) { dy = 0; break; }
        }

        // 3. 真正移动并同步碰撞箱
        moveBy(dx, dy);
        bounds.setPosition(getX(), getY());

        // 4. 动画时间更新
        if (currentState != State.IDLE) {
            stateTime += delta;
        } else {
            stateTime = 0;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion currentFrame;

        if (currentState == State.IDLE) {
            currentFrame = animDown.getKeyFrame(0); // 站立时统一朝下
        } else {
            switch (currentState) {
                case LEFT:  currentFrame = animLeft.getKeyFrame(stateTime); break;
                case UP:    currentFrame = animUp.getKeyFrame(stateTime); break;
                case RIGHT: currentFrame = animRight.getKeyFrame(stateTime); break;
                case DOWN:
                default:    currentFrame = animDown.getKeyFrame(stateTime); break;
            }
        }

        batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
    }
}
