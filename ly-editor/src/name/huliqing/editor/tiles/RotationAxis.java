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
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Torus;
import name.huliqing.luoying.utils.MaterialUtils;

/**
 * 旋转控制轴
 * @author huliqing
 */
public class RotationAxis extends Node implements AxisObj {
    
    private final AxisNode axisXNode;
    private final AxisNode axisYNode;
    private final AxisNode axisZNode;
    
    public RotationAxis() {
        Spatial rotX = createTorus("rotX", AxisObj.AXIS_COLOR_X);
        rotX.rotate(0, FastMath.PI / 2, 0);
        Axis axisX = new Axis(Axis.Type.x);
        axisX.attachChild(rotX);
        axisXNode = new AxisNode(axisX, AxisObj.AXIS_COLOR_X);
        
        Spatial rotY = createTorus("rotY", AxisObj.AXIS_COLOR_Y);
        rotY.rotate(FastMath.PI / 2, 0, 0);
        Axis axisY = new Axis(Axis.Type.y);
        axisY.attachChild(rotY);
        axisYNode = new AxisNode(axisY, AxisObj.AXIS_COLOR_Y);
        
        Spatial rotZ = createTorus("rotZ", AxisObj.AXIS_COLOR_Z);
        Axis axisZ = new Axis(Axis.Type.z);
        axisZ.attachChild(rotZ);
        axisZNode = new AxisNode(axisZ, AxisObj.AXIS_COLOR_Z);
        
        attachChild(axisXNode);
        attachChild(axisYNode);
        attachChild(axisZNode);
        
        setQueueBucket(RenderQueue.Bucket.Translucent);
    }
    
    private Spatial createTorus(String name, ColorRGBA color) {
        Node torus = new Node(name);
        
        // 可见的旋转圈
        Material mat = MaterialUtils.createUnshaded(color);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mat.getAdditionalRenderState().setDepthTest(false);
        Geometry torusInner = new Geometry(name + "tours", new Torus(80, 6, 0.005f, 1.0f));
        torusInner.setMaterial(mat);
        
        // 用于优化点选
        Geometry torusOuter = new Geometry(name + "picker", new Torus(20, 4, 0.15f, 1.0f));
        torusOuter.setMaterial(MaterialUtils.createUnshaded());
        torusOuter.setCullHint(CullHint.Always);
        
        torus.attachChild(torusInner);
        torus.attachChild(torusOuter);
        
        return torus;
    }

    @Override
    public AxisNode getAxisX() {
        return axisXNode;
    }

    @Override
    public AxisNode getAxisY() {
        return axisYNode;
    }

    @Override
    public AxisNode getAxisZ() {
        return axisZNode;
    }

    
}
