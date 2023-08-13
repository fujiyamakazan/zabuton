package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.io.Serializable;
import java.util.Map;

public abstract class AssetFactory implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AssetFactory.class);
    private final String assetName;

    public AssetFactory(String assetName) {
        this.assetName = assetName;
    }

    public abstract Integer getAssetText();

    /**
     * 記録されている資産の金額を返します。現在の金額と差があれば、その情報を付与します。
     */
    public String[] getSummary(Map<String, Integer> existAssets) {
        Integer asset = getAssetText();
        if (asset == null) {
            return null;
        }
        //assetText = assetText.replaceAll(",", ""); // カンマ除去
        //if (StringUtils.isEmpty(nowAsset)) {
        //    return new String[] { "", "" };
        //}
        //for (String assetName : this.assetNames) {
        //String existAsset = String.valueOf(existAssets.get(assetName));
        int existAsset = existAssets.get(assetName);
        String assetNote = assetName;
        //if (assetText.contains(existAsset) == false) {
        if (asset != existAsset) {
            assetNote += "★更新前の値:[" + existAsset + "]★";
        }
        //}
        return new String[] {String.valueOf(asset), assetNote};
    }

    public abstract Exception getDownloadException();

}
