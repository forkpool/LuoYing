/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.view.talk;

import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import name.huliqing.ly.Factory;
import name.huliqing.ly.layer.network.ActorNetwork;
import name.huliqing.ly.layer.service.ActorService;
import name.huliqing.ly.layer.service.SkillService;
import name.huliqing.ly.object.entity.Entity; 
import name.huliqing.ly.object.skill.Skill;

/**
 * 处理谈话逻辑：朝向, 使用朝向角色或是朝向位置只能二选一。
 * @author huliqing
 */
public class TalkLogicFace extends AbstractTalkLogic {
    private final ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
    private final ActorService actorService = Factory.get(ActorService.class);
    private final SkillService skillService = Factory.get(SkillService.class);
    
    // remove20160920
//    // 当角色在执行这些技能的时候不能进行“朝向”，除非强制执行（force=true)
//    private final long UNABLE_FACE_SKILL_STATE = SkillType.createSkillStates(
//              SkillType.attack, SkillType.dance, SkillType.dead, SkillType.defend
//            , SkillType.duck, SkillType.hurt, SkillType.jump, SkillType.magic
//            , SkillType.reset, SkillType.run, SkillType.sit, SkillType.trick
//            , SkillType.walk
//            );
    
    // 朝向的源角色
    private Entity actor;
    // 朝向的目标
    private Entity target;
    // 朝向的位置
    private Vector3f position;
    // 是否强制朝向
    private boolean force;
    
    public TalkLogicFace(Entity actor, Entity target, boolean force) {
        this.actor = actor;
        this.target = target;
        this.force = force;
        this.useTime = 0; // 转向目标不需要时间。
    }
    
    public TalkLogicFace(Entity actor, Vector3f position, boolean force) {
        this.actor = actor;
        if (this.position == null) {
            this.position = new Vector3f();
        }
        this.position.set(position);
        this.force = force;
        this.useTime = 0; // 转向目标不需要时间。
    }

    @Override
    protected void doInit() {
        // 质量小于0的角色不能进行朝向，一般为静物
        if (actorService.getMass(actor) <= 0 || actorService.isDead(actor))
            return;
        
        TempVars tv = TempVars.get();
        Vector3f targetPos = tv.vect1;
        if (target != null) {
            targetPos.set(target.getSpatial().getWorldTranslation());
        } else if (position != null) {
            targetPos.set(position);
        } else {
            tv.release();
            return;
        }
        
        // remove20160324
//        boolean canFace = force || 
//                (   actor.getMass() > 0
//                && !actor.isDead() 
//                && !actor.isAttacking()
//                && !actor.isDefending()
//                && !actor.isDucking()
//                && !actor.isRunning()
//                );

        Skill waitSkill = skillService.getSkillWaitDefault(actor);
        boolean canFace = force || (waitSkill != null && skillService.isPlayingSkill(actor, waitSkill.getData().getTags()));
        
        if (canFace) {
            Vector3f viewDirection = targetPos.subtract(actor.getSpatial().getWorldTranslation());
            if (network) {
                actorNetwork.setViewDirection(actor, viewDirection);
            } else {
                actorService.setViewDirection(actor, viewDirection);
            }
        }
        tv.release();
    }

    @Override
    protected void doTalkLogic(float tpf) {
        // ignore
    }
    
}