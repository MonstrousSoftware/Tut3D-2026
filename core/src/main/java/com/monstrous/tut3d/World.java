package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.tut3d.behaviours.CookBehaviour;
import com.monstrous.tut3d.inputs.PlayerController;
import com.monstrous.tut3d.physics.*;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable {

    private final Array<GameObject> gameObjects;
    public GameObject player;
    private final SceneAsset sceneAsset;
    private boolean isDirty;
    private final PhysicsWorld physicsWorld;
    private final PhysicsBodyFactory factory;
    private final PlayerController playerController;
    public final PhysicsRayCaster rayCaster;
    public final GameStats stats;
    public final WeaponState weaponState = new WeaponState();

    public World() {

        gameObjects = new Array<>();
        sceneAsset = Main.assets.sceneAsset;
        isDirty = true;
        physicsWorld = new PhysicsWorld(this);
        rayCaster = new PhysicsRayCaster(physicsWorld);
        factory = new PhysicsBodyFactory(physicsWorld);
        playerController = new PlayerController(this, rayCaster);
        stats = new GameStats();
    }

    public boolean isDirty(){
        return isDirty;
    }

    public void clear() {
        physicsWorld.reset();
        gameObjects.clear();
        player = null;
        isDirty = true;
        stats.reset();
        weaponState.reset();
    }

    public void setPlayer(GameObject go){
        player = go;
        player.body.setCapsuleCharacteristics();
    }

    public PlayerController getPlayerController(){
        return playerController;
    }

    public int getNumGameObjects() {
        return gameObjects.size;
    }

    public GameObject getGameObject(int index) {
        return gameObjects.get(index);
    }

    public GameObject spawnObject(GameObjectType type, String name, String proxyName, CollisionShapeType shapeType, boolean resetPosition, Vector3 position, float mass){
        Scene scene = loadNode( name, resetPosition, position );
        ModelInstance collisionInstance = scene.modelInstance;
        if(proxyName != null) {
            Scene proxyScene = loadNode( proxyName, resetPosition, position );
            collisionInstance = proxyScene.modelInstance;
        }

        PhysicsBody body = factory.createBody(collisionInstance, shapeType, mass, type.isStatic);
        GameObject go = new GameObject(type, scene, body);
        gameObjects.add(go);
        if(go.type == GameObjectType.TYPE_ENEMY)
            stats.numEnemies++;
        if(go.type == GameObjectType.TYPE_PICKUP_COIN)
            stats.numCoins++;
        isDirty = true;         // list of game objects has changed
        return go;
    }

    private Scene loadNode( String nodeName, boolean resetPosition, Vector3 position ) {
        Scene scene = new Scene(sceneAsset.scene, nodeName);
        if(scene.modelInstance.nodes.size == 0)
            throw new RuntimeException("Cannot find node in GLTF file: " + nodeName);
        applyNodeTransform(resetPosition, scene.modelInstance, scene.modelInstance.nodes.first());         // incorporate nodes' transform into model instance transform
        scene.modelInstance.transform.translate(position);
        return scene;
    }

    private void applyNodeTransform(boolean resetPosition, ModelInstance modelInstance, Node node ){
        if(!resetPosition)
            modelInstance.transform.mul(node.globalTransform);
        node.translation.set(0,0,0);
        node.scale.set(1,1,1);
        node.rotation.idt();
        modelInstance.calculateTransforms();
    }

    public void removeObject(GameObject gameObject){
        gameObject.health = 0;
        if(gameObject.type == GameObjectType.TYPE_ENEMY)
            stats.numEnemies--;
        System.out.println("Enemies: "+stats.numEnemies);
        isDirty = true;
    }

    private final Vector3 dir = new Vector3();
    private final Vector3 spawnPos = new Vector3();
    private final Vector3 shootDirection = new Vector3();
    private final Vector3 impulse = new Vector3();

    public void shoot(Vector3 viewingDirection , PhysicsRayCaster.HitPoint hitPoint) {
        if(player.isDead())
            return;
        if(!weaponState.isWeaponReady())  // to give delay between shots
            return;
        weaponState.firing = true;    // set state to firing (triggers gun animation in GameScreen)

        switch(weaponState.currentWeaponType) {
            case BALL:
                dir.set(viewingDirection);
                spawnPos.set(dir);
                spawnPos.add(player.getPosition()); // spawn from 1 unit in front of the player
                GameObject ball = spawnObject(GameObjectType.TYPE_FRIENDLY_BULLET, "ball", null, CollisionShapeType.SPHERE, true, spawnPos, Settings.ballMass);
                shootDirection.set(dir);        // shoot forward
                shootDirection.y += 0.5f;       // and slightly up
                shootDirection.scl(Settings.ballForce);   // scale for speed
                ball.body.applyForce(shootDirection);
                break;
            case GUN:
                Main.assets.sounds.GUN_SHOT.play();
                if(hitPoint.hit) {
                    GameObject victim = hitPoint.refObject;
                    Gdx.app.log("gunshot hit", victim.scene.modelInstance.nodes.first().id);
                    if(victim.type.isEnemy)
                        bulletHit(victim);

                    impulse.set(victim.getPosition()).sub(player.getPosition()).nor().scl(Settings.gunForce);
                    if(victim.body.geom.getBody() != null ) {
                        victim.body.geom.getBody().enable();
                        victim.body.applyForceAtPos(impulse, hitPoint.worldContactPoint);
                    }
                }
                break;
        }
    }

    public void update( float deltaTime ) {
        if(stats.numEnemies > 0 || stats.coinsCollected < stats.numCoins)
            stats.gameTime += deltaTime;
        else {
            if(!stats.levelComplete)
                Main.assets.sounds.GAME_COMPLETED.play();
            stats.levelComplete = true;
        }
        if(player.isDead())
            return;
        weaponState.update(deltaTime);
        playerController.update(player, deltaTime);
        for(GameObject go : gameObjects)
            go.update(this, deltaTime);
        physicsWorld.update(deltaTime);
        for(GameObject go : gameObjects){
            if( go.body.geom.getBody() != null) {
                if(go.type == GameObjectType.TYPE_PLAYER){
                    // use information from the player controller, since the rigid body is not rotated.
                    player.scene.modelInstance.transform.setToRotation(Vector3.Z, playerController.getForwardDirection());
                    player.scene.modelInstance.transform.setTranslation(go.body.getPosition());
                }
                else if(go.type == GameObjectType.TYPE_ENEMY){
                    CookBehaviour cb = (CookBehaviour) go.behaviour;
                    go.scene.modelInstance.transform.setToRotation(Vector3.Z, cb.getDirection());
                    go.scene.modelInstance.transform.setTranslation(go.body.getPosition());
                }
                else
                    go.scene.modelInstance.transform.set(go.body.getPosition(), go.body.getOrientation());
            }
        }
        // remove dead objects
        for(int i = 0; i < gameObjects.size; i++){
            GameObject go = gameObjects.get(i);
            if(go.isDead()){
                gameObjects.removeValue(go, true);
                go.dispose();
            }
        }
    }

    public void onCollision(GameObject go1, GameObject go2){             // called on collision
        // try either order
        handleCollision(go1, go2);
        handleCollision(go2, go1);
    }

    private void handleCollision(GameObject go1, GameObject go2){
        if(go1.type.isStatic || go2.type.isStatic)
            return;
        if(go1.type.isPlayer && go2.type.canPickup){
            pickup(go1, go2);
        }
        if(go1.type.isPlayer && go2.type.isEnemyBullet) {
            removeObject(go2);  // destroy bullet
            bulletHit(go1);
        }
        if(go1.type.isEnemy && go2.type.isFriendlyBullet) {
            removeObject(go2);  // destroy bullet
            bulletHit(go1);
        }
    }

    private void bulletHit(GameObject character) {
        character.health -= 0.25f;      // - 25% health
        Main.assets.sounds.HIT.play();
        if(character.isDead()) {
            removeObject(character);
            if (character.type.isPlayer)
                Main.assets.sounds.GAME_OVER.play();
        }
        System.out.println("Player health: "+100f*player.health+" %");
    }

    private void pickup(GameObject character, GameObject pickup){
        if(pickup.type == GameObjectType.TYPE_PICKUP_COIN) {
            stats.coinsCollected++;
            System.out.println("Coins: "+stats.coinsCollected+"/"+stats.numCoins);
            Main.assets.sounds.COIN.play();
        }
        else if(pickup.type == GameObjectType.TYPE_PICKUP_HEALTH) {
            character.health = Math.min(character.health + 0.5f, 1f);
            System.out.println("Player health: "+100f*player.health+" %");
            Main.assets.sounds.UPGRADE.play();
        } else if(pickup.type == GameObjectType.TYPE_PICKUP_GUN) {
            weaponState.haveGun = true;
            weaponState.currentWeaponType = WeaponType.GUN;
            Main.assets.sounds.UPGRADE.play();
        }
        removeObject(pickup);
    }

    @Override
    public void dispose() {
        physicsWorld.dispose();
        rayCaster.dispose();
    }
}
