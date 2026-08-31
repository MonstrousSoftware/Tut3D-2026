package com.monstrous.tut3d;

import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.physics.CollisionShapeType;

public class Populator {

    public static void populate(World world) {
        world.clear();
        world.spawnObject(true, "brickcube", CollisionShapeType.BOX, false, Vector3.Zero, 1);
        world.spawnObject(true, "groundbox", CollisionShapeType.BOX, false,Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.001", CollisionShapeType.BOX,false,Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.002", CollisionShapeType.BOX,false,Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.003", CollisionShapeType.BOX,false,Vector3.Zero, 1f);
        world.spawnObject(true, "wall", CollisionShapeType.BOX,false,Vector3.Zero, 1f);
        world.spawnObject(false, "ball", CollisionShapeType.SPHERE, true, new Vector3(0,4,-2), 1f);
        world.spawnObject(false, "ball", CollisionShapeType.SPHERE,true, new Vector3(-1,5,-2), 1f);
        world.spawnObject(false, "ball", CollisionShapeType.SPHERE, true, new Vector3(-2,6,-2), 1f);

        GameObject player = world.spawnObject(false, "ducky",CollisionShapeType.CAPSULE, true, new Vector3(0,1,0), Settings.playerMass);
        world.setPlayer(player);
    }
}
