package com.monstrous.tut3d;


import com.badlogic.gdx.math.Vector3;

public class Settings {
    static public final String GLTF_FILE = "models/step12.gltf";

    static public float eyeHeight = 1.5f;   // meters

    static public float walkSpeed = 5f;    // m/s
    static public float runFactor = 3f;    // m/s
    static public float turnSpeed = 120f;   // degrees/s
    static public float jumpForce = 10f;

    static public boolean invertLook = false;
    static public boolean freeLook = true;
    static public float headBobDuration = 0.6f; // s
    static public float headBobHeight = 0.04f;  // m
    static public float degreesPerPixel = 0.1f; // mouse sensitivity

    static public float groundRayLength = 1.2f;

    static public float gravity = -30f;

    static public float ballMass = 0.2f;
    static public float ballForce = 300f;

    static public float panMass = 0.05f;
    static public float panForce = 40f;

    static public float gunForce = 200f;

    static public float playerMass = 1f;
    static public float playerLinearDamping = 0.05f;
    static public float playerAngularDamping = 0.5f;

    static public final int shadowMapSize = 4096;

    static public Vector3 gunPosition = new Vector3(-1.1f, 1.1f, 1.8f); // gun position in gun camera view
    static public float gunScale = 3.0f;

}
