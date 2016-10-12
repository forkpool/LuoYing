/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.layer.service;

import name.huliqing.ly.data.MagicData;
import name.huliqing.ly.object.Loader;
import name.huliqing.ly.xml.DataFactory;
import name.huliqing.ly.object.magic.Magic;

/**
 *
 * @author huliqing
 */
public class MagicServiceImpl implements MagicService {

    @Override
    public void inject() {
        // ignore
    }
    
    @Override
    public MagicData loadMagic(String magicId) {
        return DataFactory.createData(magicId);
    }

    @Override
    public Magic loadMagic(MagicData magicData) {
        return DataFactory.createProcessor(magicData);
    }
 
}