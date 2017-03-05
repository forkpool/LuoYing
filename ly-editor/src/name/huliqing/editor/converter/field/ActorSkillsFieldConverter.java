/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.converter.field;

import com.jme3.math.Vector3f;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import name.huliqing.editor.constants.AssetConstants;
import name.huliqing.editor.converter.Converter;
import name.huliqing.editor.ui.utils.JfxUtils;
import name.huliqing.fxswing.Jfx;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.data.SkillData;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.module.ActorModule;
import name.huliqing.luoying.object.module.SkillModule;
import name.huliqing.luoying.object.skill.Skill;
import name.huliqing.luoying.object.skill.WalkSkill;
import name.huliqing.luoying.xml.ObjectData;

/**
 * 角色Actor的Skills字段转换器
 * @author huliqing
 */
public class ActorSkillsFieldConverter extends EntityObjectDatasFieldConverter {
    private static final Logger LOG = Logger.getLogger(ActorSkillsFieldConverter.class.getName());

    public ActorSkillsFieldConverter() {
        super();
        Button playAndStop = new Button("", JfxUtils.createIcon(AssetConstants.INTERFACE_ICON_PLAY));
        toolbar.getItems().add(playAndStop);
        
        playAndStop.setOnAction((e) -> {
            playSkill();
        });
        listView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                playSkill();
            }
        });
    }
    
    private void playSkill() {
        ObjectData od = listView.getSelectionModel().getSelectedItem();
        if (!(od instanceof SkillData)) {
            LOG.log(Level.WARNING, "SelectItem not a SkillData! data={0}", new Object[]{od.getId()});
            return;
        }
        EntityData ed = findEntityData(this);
        if (ed == null) {
            return;
        }
        Jfx.runOnJme(() -> {
            Entity entity = jfxEdit.getJmeEdit().getScene().getEntity(ed.getUniqueId());
            ActorModule am = entity.getModuleManager().getModule(ActorModule.class);
            SkillModule sm = entity.getModuleManager().getModule(SkillModule.class);
            if (sm == null) {
                LOG.log(Level.WARNING, "Could not find SkillModule! entity={0}", new Object[]{entity.getData().getId()});
                return;
            }
            Skill skill = findSkill(sm, (SkillData) od);
            if (skill != null) {
                if (am != null) {
                    Vector3f viewDir = am.getViewDirection();
                    if (skill instanceof WalkSkill) {
                        WalkSkill wSkill = (WalkSkill) skill;
                        wSkill.setWalkDirection(viewDir); // 使用view方法作为walkDir
                        wSkill.setViewDirection(viewDir);
                    }
                }
                sm.playSkill(skill, true);
            }
        });
    }
    
    private Skill findSkill(SkillModule sm, SkillData sd) {
        List<Skill> skills = sm.getSkills();
        if (skills == null || skills.isEmpty())
            return null;
        
        for (Skill s : skills) {
            if (s.getData() == sd) {
                return s;
            }
        }
        return null;
    }
    
    private EntityData findEntityData(Converter c) {
        if (c.getData() instanceof EntityData) {
            return (EntityData) c.getData();
        }
        
        if (c.getParent() != null) {
            return findEntityData(c.getParent());
        }
        return null;
    }

    @Override
    public void initialize() {
        super.initialize();
       
    }
    
    @Override
    protected Node createLayout() {
        return layout;
    }

}