/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.layer.service;

import com.jme3.math.FastMath;
import java.util.List;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.data.StateData;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.xml.DataFactory;
import name.huliqing.luoying.object.module.StateListener;
import name.huliqing.luoying.object.module.StateModule;
import name.huliqing.luoying.object.state.State;

/**
 *
 * @author huliqing
 */
public class StateServiceImpl implements StateService{
//    private static final Logger LOG = Logger.getLogger(StateServiceImpl.class.getName());

    private ResistService resistService;
    
    @Override
    public void inject() {
        resistService = Factory.get(ResistService.class);
    }
    
    @Override
    public float checkAddState(Entity actor, String stateId) {
        // mark20160521,以后不要管是否是死亡都允许添加状态，能否添加状态由各自状态处理器去判断。
        // 角色死亡时不能再添加状态，
        // 1.因为角色在死亡时可能会全部清理身上的状态，这时候如果再允许添状态时可能会发生冲突.may NPE
        // 2.另外一些状态也不能在死亡的时候使用，比如晕眩状态可能让死亡后的角色重新站立(被reset)
        // 3.因此死亡的角色返回抵抗值为1，即完全抵抗
        // remove20160123 test
//        if (actor.isDead()) {
//            return 1;
//        }
   
        // remove20160803
//        // 如果状态不存在，则返回一个绝对抵抗值，以阻止继续添加状态
//        if (!existsState(stateId)) {
//            if (Config.debug) {
//                Logger.getLogger(StateServiceImpl.class.getName()).log(Level.WARNING
//                        , "State not found!!! stateId={0}", stateId);
//            }
//            return 1.0f;
//        }
        
        // 获得角色抗性值
        float resist = resistService.getResist(actor, stateId);
        
        // 1.毫无抗性，直接添加
        if (resist <= 0) {
            return 0;
        }
        
        // 2.完全抗性
        if (resist >= 1.0f) {
            return 1;
        }
        
        // 3.给一个完全抵抗的机会
        if (resist >= FastMath.nextRandomFloat()) {
            return 1;
        }
        
        // 4.抵抗不成功仍有机会根据角色的最高抵抗值随机计算一个最终抵抗值，该
        // 值最高不会超过角色的最高抵抗值．该最终抵抗值可削弱一部分状态的作用．
        float resultResist = resist * FastMath.nextRandomFloat();
        return resultResist;
    }
    
    @Override
    public boolean addState(Entity actor, String stateId, Entity sourceActor) {
        // 如果resist >= 1.0则说明完全抵抗，则不添加
        float resist = checkAddState(actor, stateId);
        if (resist < 1.0f) {
            addStateForce(actor, stateId, resist, sourceActor);
            return true;
        }
        return false;
    }
    
    @Override
    public void addStateForce(Entity actor, String stateId, float resist, Entity sourceActor) {
        // 创建Data
        StateData newStateData = DataFactory.createData(stateId);
        newStateData.setResist(resist);
        
        // 设置状态
        State state = Loader.load(newStateData);
        state.setSourceActor(sourceActor);
        
        StateModule module = actor.getModuleManager().getModule(StateModule.class);
        module.addState(state);
    }

    @Override
    public final boolean removeState(Entity actor, String removeStateId) {
        StateModule module = actor.getModuleManager().getModule(StateModule.class);
        if (module != null) {
            State state = module.getState(removeStateId);
            return state != null ? module.removeState(state) : false;
        }
        return false;
    }

    @Override
    public State findState(Entity actor, String stateId) {
        StateModule module = actor.getModuleManager().getModule(StateModule.class);
        if (module != null) {
            return module.getState(stateId);
        }
        return null;
    }

    @Override
    public void clearStates(Entity actor) {
        StateModule module = actor.getModuleManager().getModule(StateModule.class);
        if (module != null && module.getStates() != null) {
            for (State state : module.getStates()) {
                module.removeState(state);
            }
        }
    }
    
    @Override
    public boolean existsState(Entity actor, String stateId) {
        StateModule module = actor.getModuleManager().getModule(StateModule.class);
        return module != null && module.getState(stateId) != null;
    }
    
    @Override
    public List<State> getStates(Entity actor) {
        StateModule module = actor.getModuleManager().getModule(StateModule.class);
        if (module != null) {
            return module.getStates();
        }
        return null;
    }

    @Override
    public void addListener(Entity actor, StateListener listener) {
        StateModule module = actor.getModuleManager().getModule(StateModule.class);
        if (module != null) {
            module.addStateListener(listener);
        }
    }

    @Override
    public boolean removeListener(Entity actor, StateListener listener) {
        StateModule module = actor.getModuleManager().getModule(StateModule.class);
        return module != null && module.removeStateListener(listener);
    }
    
    
}