/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.env;

import com.jme3.app.Application;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import name.huliqing.core.data.EnvData;
import name.huliqing.core.object.scene.Scene;
import name.huliqing.core.processor.VerySimpleWaterProcessor;

/**
 * 轻量级的水体效果，可支持移动设置、手机等。特别针对Opengl es应用。
 * @author huliqing
 * @param <T>
 */
public class WaterSimpleEnv<T extends EnvData> extends AbstractEnv<T> implements WaterEnv<T> {
//    private final PlayService playService = Factory.get(PlayService.class);
    
    private String waterModelFile;
    private Vector3f location;
    private Vector3f rotation;
    private Vector3f scale;
    
    private ColorRGBA waterColor;
    private float texScale = 1;
    private float waveSpeed = 0.05f;
    private float distortionMix = 0.5f;
    private float distortionScale = 0.2f;
    
    private String foamMap;
    private Vector2f foamScale;
    private String foamMaskMap;
    private Vector2f foamMaskScale;
    
    // ----
    private Application app;
    private Spatial waterModel;
    private VerySimpleWaterProcessor water;
    
    @Override
    public void setData(T data) {
        super.setData(data);
        waterModelFile = data.getAttribute("waterModel");
        location = data.getAsVector3f("location");
        rotation = data.getAsVector3f("rotation");
        scale = data.getAsVector3f("scale");
        waterColor = data.getAsColor("waterColor");
        texScale = data.getAsFloat("texScale", texScale);
        waveSpeed = data.getAsFloat("waveSpeed", waveSpeed);
        distortionMix = data.getAsFloat("distortionMix", distortionMix);
        distortionScale = data.getAsFloat("distortionScale", distortionScale);
        
        foamMap = data.getAttribute("foamMap");
        foamScale = data.getAsVector2f("foamScale");
        foamMaskMap = data.getAttribute("foamMaskMap");
        foamMaskScale = data.getAsVector2f("foamMaskScale");
    }
    
    @Override
    public void initialize(Application app, Scene scene) {
        super.initialize(app, scene);
        this.app = app;
        
        waterModel = app.getAssetManager().loadModel(waterModelFile);
        if (location != null) {
            waterModel.setLocalTranslation(location);
        }
        if (rotation != null) {
            Quaternion rot = waterModel.getLocalRotation();
            rot.fromAngles(rotation.x, rotation.y, rotation.z);
            waterModel.setLocalRotation(rot);
        }
        if (scale != null) {
            waterModel.setLocalScale(scale);
        }
        
        water = new VerySimpleWaterProcessor(app.getAssetManager(), waterModel);
        water.addReflectionScene(scene.getSceneRoot());
        water.setTexScale(texScale);
        water.setWaveSpeed(waveSpeed);
        water.setDistortionMix(distortionMix);
        water.setDistortionScale(distortionScale);
        if (waterColor != null) {
            water.setWaterColor(waterColor);
        }
        if (foamMap != null) {
            water.setFoamMap(foamMap);
        }
        if (foamScale != null) {
            water.setFoamScale(foamScale.x, foamScale.y);
        }
        if (foamMaskMap != null) {
            water.setFoamMaskMap(foamMaskMap);
        }
        if (foamMaskScale != null) {
            water.setFoamMaskScale(foamMaskScale.x, foamMaskScale.y);
        }
        app.getViewPort().addProcessor(water);
        
        scene.addSceneObject(waterModel);
    }
    
    @Override
    public void cleanup() {
        if (waterModel != null) {
            scene.removeSceneObject(waterModel);
        }
        if (water != null && app != null) {
            if (app.getViewPort().getProcessors().contains(water)) {
                app.getViewPort().removeProcessor(water);
            }
        }
        super.cleanup(); 
    }
    
    @Override
    public boolean isUnderWater(Vector3f point) {
        if (!isInitialized()) {
            return false;
        }
        if (point.y < waterModel.getWorldTranslation().y) {
            BoundingBox bb = (BoundingBox) waterModel.getWorldBound();
            bb.setYExtent(Float.MAX_VALUE);
            return bb.contains(point);
        }
        return false;
    }
    
    
}