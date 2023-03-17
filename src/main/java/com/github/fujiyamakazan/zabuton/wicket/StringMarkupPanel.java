package com.github.fujiyamakazan.zabuton.wicket;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文字列をマークアップとして使用します。
 * wicket:id を埋め込んだ cmsdb.contentsitem.contentstext などに使用します。
 */
public class StringMarkupPanel extends Panel implements IComponentResolver {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(StringMarkupPanel.class);

    private final String str;

    public StringMarkupPanel(String id, String str) {
        super(id);
        this.str = str;
    }

    /**
     * マークアップが指定のwicket:idを含むかを判定します。
     */
    public boolean has(String id) {
        if (StringUtils.isEmpty(id)) {
            return false;
        }
        String tmp = str.replaceAll(" ", "");
        return StringUtils.contains(tmp, "wicket:id=\"" + id + "\"");
    }

    /**
     * 文字列をPanelのマークアップリソースとして使用します。
     */
    @Override
    public Markup getAssociatedMarkup() {
        return Markup.of("<wicket:panel>" + str + "</wicket:panel>");
    }

    @Override
    protected void onRender() {
        super.onRender();
    }

    @Override
    protected boolean renderNext(MarkupStream markupStream) {
        /*
         * マークアップに未知のwicket:idが含まれているとき、
         * エクセプションはログ出力にとどめる。
         */
        try {
            return super.renderNext(markupStream);
        } catch (MarkupException e) {
            LOGGER.error("renderNextでエラーが発生しました。", e);
            return true;
        }
    }

    /**
     * マークアップと対応できなかったときの処理です。
     */
    @Override
    public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag) {

        // 処理なし。

        return null;
    }

}
