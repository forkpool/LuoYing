/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.tiles;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import name.huliqing.luoying.utils.MaterialUtils;

/**
 *
 * @author huliqing
 */
public class TileScale extends Node {
    
    public TileScale() {
        
        Spatial axisX = createAxis("axisX", new ColorRGBA(1.0f, 0.1f, 0.1f, 1.0f));
        Spatial axisY = createAxis("axisY", new ColorRGBA(0.1f, 1.0f, 0.1f, 1.0f));
        Spatial axisZ = createAxis("axisZ", new ColorRGBA(0.1f, 0.1f, 1.0f, 1.0f));
        
        axisX.rotate(0, 0, -FastMath.PI / 2);
        axisZ.rotate(FastMath.PI / 2, 0, 0);
        
        attachChild(axisX);
        attachChild(axisY);
        attachChild(axisZ);
        
        // 默认放在半透明中桶中，这样可以盖住其它所有物体
        setQueueBucket(RenderQueue.Bucket.Translucent);
    }
    
    private Spatial createAxis(String name, ColorRGBA color) {
        Node node = new Node(name);
        Material mat = MaterialUtils.createUnshaded(color);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mat.getAdditionalRenderState().setDepthTest(false);
        
        // 轴线
        Geometry line = new Geometry("line" + name, new Line(new Vector3f(), new Vector3f(0,1,0)));
        line.setMaterial(mat);
        node.attachChild(line);
        
        // 一个圆锥箭头
        Geometry boxGeo = new Geometry("box", new Box(0.5f, 0.5f, 0.5f));
        boxGeo.setMaterial(mat);
        boxGeo.setLocalTranslation(0, 1, 0);
        boxGeo.setLocalScale(0.1f, 0.1f, 0.1f);
        boxGeo.setQueueBucket(RenderQueue.Bucket.Translucent);
        node.attachChild(boxGeo);
        
        return node;
    }
}
