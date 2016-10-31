/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.view.actor;

import java.util.ArrayList;
import java.util.List;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.layer.network.ActorNetwork;
import name.huliqing.luoying.layer.network.SkillNetwork;
import name.huliqing.luoying.layer.service.SkillService;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.skill.Skill;
import name.huliqing.luoying.ui.ListView;
import name.huliqing.luoying.ui.Row;
import name.huliqing.luoying.ui.UI;
import name.huliqing.ly.layer.network.GameNetwork;
import name.huliqing.ly.layer.service.GameService;

/**
 *
 * @author huliqing
 */
public class SkillPanel extends ListView<Skill> implements ActorPanel {
    private final SkillService skillService = Factory.get(SkillService.class);
    private final SkillNetwork skillNetwork = Factory.get(SkillNetwork.class);
    private final ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
    private final GameService gameService = Factory.get(GameService.class);
    private final GameNetwork gameNetwork = Factory.get(GameNetwork.class);

    private Entity actor;
    // 最近一次获取技能列表的时间,当角色切换或者技能列表发生变化时应该重新载入
    private final List<Skill> datas = new ArrayList<Skill>();
    
    public SkillPanel(float width, float height) {
        super(width, height);
    }
    
    @Override
    public List<Skill> getDatas() {
        if (actor == null) {
            return datas;
        }
        datas.clear();
        datas.addAll(skillService.getSkills(actor));
        return datas;
    }

    @Override
    protected boolean filter(Skill data) {
        return false;
    }
    
    @Override
    protected Row createEmptyRow() {
        final SkillRow row = new SkillRow(this);
        row.setRowClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (!isPress) {
                    
                    // 一些技能在执行前必须设置目标对象。
                    actorNetwork.setTarget(actor, gameService.getTarget());
            
                    // 执行技能
                    skillNetwork.playSkill(actor, row.getData(), false);
                    
//                    refreshPageData();// skill不会删除
                }
            }
        });
        row.setShortcutListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (!isPress) {
                    gameService.addShortcut(actor, row.getData().getData());
                }
            }
        });
        return row;
    }

    @Override
    public void setPanelVisible(boolean visible) {
        this.setVisible(visible);
    }

    @Override
    public void setPanelUpdate(Entity actor) {
        this.actor = actor;
        refreshPageData();
    }
    
    public Entity getActor() {
        return actor;
    }
}