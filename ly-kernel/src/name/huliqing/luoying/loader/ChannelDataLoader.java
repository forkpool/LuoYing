/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.loader;

import name.huliqing.luoying.data.ChannelData;
import name.huliqing.luoying.xml.Proto;
import name.huliqing.luoying.xml.DataLoader;

/**
 *
 * @author huliqing
 */
public class ChannelDataLoader implements DataLoader<ChannelData> {

    @Override
    public void load(Proto proto, ChannelData data) {
        // remove20160930
//        data.setFromRootBones(proto.getAsArray("fromRootBones"));
//        data.setToRootBones(proto.getAsArray("toRootBones"));
//        data.setBones(proto.getAsArray("bones"));
    }
    
}