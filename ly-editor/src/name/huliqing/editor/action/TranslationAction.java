/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.action;

import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.editor.Editor;
import name.huliqing.editor.select.SelectObj;
import name.huliqing.editor.tiles.Axis;
import name.huliqing.editor.tiles.TransformObj;
import name.huliqing.luoying.manager.PickManager;

/**
 * @author huliqing
 */
public class TranslationAction extends ComplexAction {

    private static final Logger LOG = Logger.getLogger(TranslationAction.class.getName());
    private final Ray ray = new Ray();

    // 变换控制物体
    private TransformObj transformObj;
    // 当前操作的轴向
    private Axis actionAxis;
    // 行为操作开始时编辑器中的被选择的物体，以及该物体的位置
    private SelectObj selectObj;
    
    private final Picker picker = new Picker();
    private boolean picking;
    
    // 开始变换时物体的位置(local)
    private final Vector3f startSpatialLoc = new Vector3f();
    
    public TranslationAction(Editor editor) {
        super(editor);
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            if (picking) {
                picker.endPick();
                picking = false;
            }
            if (transformObj != null) {
                transformObj.showDebugLine(actionAxis, false);
                transformObj = null;
            }
            actionAxis = null;
            selectObj = null;
            return;
        }
        
        PickManager.getPickRay(editor.getCamera(), editor.getInputManager().getCursorPosition(), ray);
        transformObj = editor.getForm().getTransformObj();
        actionAxis = transformObj.pickTransformAxis(ray);
        selectObj = editor.getForm().getSelected();
        if (transformObj != null && actionAxis != null && selectObj != null) {
            picking = true;
            Quaternion planRotation = Picker.PLANE_XY;
            switch (actionAxis.getType()) {
                case x:
                    planRotation = Picker.PLANE_XY;
                    break;
                case y:
                    planRotation = Picker.PLANE_YZ;
                    break;
                case z:
                    planRotation = Picker.PLANE_XZ;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            picker.startPick(selectObj.getSelectedSpatial(), editor.getForm().getTransformMode()
                    , editor.getCamera(), editor.getInputManager().getCursorPosition()
                    ,planRotation);
            transformObj.showDebugLine(actionAxis, true);
            startSpatialLoc.set(selectObj.getSelectedSpatial().getLocalTranslation());
//            LOG.log(Level.INFO, "StartSpatialLoc={0}", startSpatialLoc);
        }
    }
    
    @Override
    public void update(float tpf) {
        if (!picking)
            return;
        
        if (!picker.updatePick(editor.getCamera(), editor.getInputManager().getCursorPosition())) {
            return;
        }
        TempVars tv = TempVars.get();
        Vector3f diff = picker.getTranslation(actionAxis.getDirection(tv.vect2));
        
        Spatial parent = selectObj.getSelectedSpatial().getParent();
        if (parent != null) {
            tv.quat1.set(parent.getWorldRotation()).inverseLocal().mult(diff, diff);
            diff.divideLocal(parent.getWorldScale());
        } 
        
        Vector3f finalLocalPos = tv.vect1.set(startSpatialLoc).addLocal(diff);
        selectObj.getSelectedSpatial().setLocalTranslation(finalLocalPos);
        transformObj.setLocalTranslation(selectObj.getSelectedSpatial().getWorldTranslation());
        
        tv.release();
    }
    
}