/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.anim;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author huliqing
 */
public class AnimationControl extends AbstractControl {
    
    private Anim anim;

    public AnimationControl() {}
    
    public AnimationControl(Anim anim) {
        this.anim = anim;
    }
    
    public Anim getAnim() {
        return anim;
    }

    public void setAnim(Anim anim) {
        this.anim = anim;
    }

    @Override
    protected void controlUpdate(float tpf) {
        anim.update(tpf);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
}