package com.github.fujiyamakazan.zabuton.app.rakutenquest;

public interface CreatableAccetFactory {

    //    void download();
    //
    //    List<Journal> createJurnals(List<Journal> existDatas, List<Journal> templates);

    AssetFactory getAssetFactory(String name);

}
