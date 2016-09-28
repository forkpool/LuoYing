/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.define;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.core.data.DefineData;
import name.huliqing.core.object.skill.SkillTag;

/**
 * 技能类型的定义
 * @author huliqing
 */
public class SkillTagDefine extends Define {
    private static final Logger LOG = Logger.getLogger(SkillTagDefine.class.getName());
    
    /**
     * 技能标记列表,这个列表是有序的,并且在载入后不在运行时改变
     */
    private final List<SkillTag> tagList = new ArrayList<SkillTag>();

    @Override
    public void setData(DefineData data) {
        super.setData(data);
        String[] tags = data.getAsArray("skillTags");
        if (tags != null && tags.length > 0) {
            for (String tag : tags) {
                registerSkillTag(tag);
            }
        } else {
            LOG.log(Level.WARNING, "skillTags not defined.");
        }
    }
    
    /**
     * 查询当前已经注册的tag的数量
     * @return 
     */
    public final int size() {
        return tagList.size();
    }
    
    /**
     * 通过技能标记获取SkillTag
     * @param skillTag
     * @return 
     */
    public SkillTag getSkillTag(String skillTag) {
        SkillTag st = getSkillTagInner(skillTag);
        if (st != null) {
            return st;
        }
        return null;
    }
    
    /**
     * 转化所有tag为一个整型，所有整型中每个二进制(1)位代表一个tag类型, 如果tags为null则返回0.
     * @param tags
     * @return 
     */
    public long convert(String... tags) {
        long result = 0;
        if (tags != null) {
            SkillTag st;
            for (String tag : tags) {
                st = getSkillTagInner(tag);
                if (st == null) {
                    continue;
                }
                result |= st.indexAsBinary();
            }            
        }
        return result;
    }
 
    /**
     * 注册、登记一个技能类型标记,这个注册应该放在系统初始化的时候进行。
     * @param skillTag 
     */
    public synchronized void registerSkillTag(String skillTag) {
        SkillTag st = getSkillTagInner(skillTag);
        if (st != null) {
            LOG.log(Level.WARNING, "Could not register skill tag,  skillTag already exists! skillTag={0}", st);
            return;
        }
        if (size() >= 64) {
            LOG.log(Level.WARNING
                    , "Could not register skill tag, the size of skillTag could not more than 64! skillTag={0}, current size={1}"
                    , new Object[] {skillTag, size()});
            return;
        }
        tagList.add(new SkillTag(tagList.size(), skillTag));
    }
    
    /**
     * 清理所有技能标记
     */
    public synchronized void clear() {
        tagList.clear();
    }
    
    private SkillTag getSkillTagInner(String skillTag) {
        for (SkillTag st : tagList) {
            if (st.name().equals(skillTag)) {
                return st;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return tagList.toString();
    }
    
    /**
     * @param tags
     * @return 
     */
    public String toString(long tags) {
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < size(); i++) {
            if ((tags & 1 << i) != 0) {
                temp.add(tagList.get(i).name());
            }
        }
        return temp.toString();
    }
}