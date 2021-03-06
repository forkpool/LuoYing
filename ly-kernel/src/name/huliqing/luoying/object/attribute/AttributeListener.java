/*
 * LuoYing is a program used to make 3D RPG game.
 * Copyright (c) 2014-2016 Huliqing <31703299@qq.com>
 * 
 * This file is part of LuoYing.
 *
 * LuoYing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LuoYing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LuoYing.  If not, see <http://www.gnu.org/licenses/>.
 */
package name.huliqing.luoying.object.attribute;

/**
 * 属性侦听器，主要用于监听属性的添加、移除
 * @author huliqing
 */
public interface AttributeListener {
    
    /**
     * 当角色添加了一个新属性后该方法被调用。
     * @param am 属性管理器
     * @param attribute 新添加的属性
     */
    void onAttributeAdded(AttributeManager am, Attribute attribute);
    
    /**
     * 当角色移除了一个属性后该方法被调用。
     * @param am 属性管理器
     * @param attribute 
     */
    void onAttributeRemoved(AttributeManager am, Attribute attribute);
}
