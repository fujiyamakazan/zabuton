package com.github.fujiyamakazan.zabuton.wicketcomponent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.AjaxFileDropBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ファイルドロップでファイルアプロードをします。
 * @author fujiyama
 */
public class FileDropPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(FileDropPanel.class);

    /**
     * アップロードされたファイル。
     * シリアライズの対象外とするために transient修飾子を付与する。
     */
    private transient ArrayList<FileUpload> fileUploads = Generics.newArrayList();

    private boolean single;

    public List<FileUpload> getFiles() {
        return fileUploads;
    }

    public FileDropPanel(String id) {
        this(id, false);
    }

    /**
     * コンストラクタ。
     * @param id wicket:id
     * @param single シングルモード指定
     */
    public FileDropPanel(String id, boolean single) {
        super(id);
        this.single = single;
    }

    private class FileInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String size;

        public FileInfo(FileUpload upload) {
            name = upload.getClientFileName();
            size = Bytes.bytes(upload.getSize()).toString() + " byte";
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        WebMarkupContainer list = new WebMarkupContainer("list");
        add(list);
        list.setOutputMarkupId(true);

        IModel<? extends List<FileInfo>> modelLv = new LoadableDetachableModel<List<FileInfo>>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected List<FileInfo> load() {
                List<FileInfo> fileInfos = Generics.newArrayList();
                for (FileUpload upload : fileUploads) {
                    fileInfos.add(new FileInfo(upload));
                }
                return fileInfos;
            }
        };
        list.add(new ListView<FileInfo>("lv", modelLv) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<FileInfo> item) {

                FileInfo fileInfo = item.getModelObject();
                int index = item.getIndex();
                item.add(new Label("name", fileInfo.name));
                item.add(new Label("size", fileInfo.size));

                item.add(new Link<Void>("delete") {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick() {
                        FileUpload targetUpload = fileUploads.get(index);
                        targetUpload.delete();
                        fileUploads.remove(index);
                    }
                });
            }
        });

        /*
         * ファイルドロップのイベント
         */
        add(new AjaxFileDropBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onFileUpload(AjaxRequestTarget target, List<FileUpload> files) {

                List<FileUpload> objects = FileDropPanel.this.fileUploads;
                objects.clear();

                if (FileDropPanel.this.single && files.isEmpty() == false) {
                    objects.add(files.get(0));
                } else {
                    objects.addAll(files);
                }
                target.add(list);

                FileDropPanel.this.afterFileUpload(target, files);

            }

            @Override
            protected void onError(AjaxRequestTarget target, FileUploadException e) {
                log.error(e.getMessage());
            }
        });
    }

    protected void afterFileUpload(AjaxRequestTarget target, List<FileUpload> files) {
        /* 処理なし */
    }

    /**
     * アップロードされたファイルを１つだけ返却します。
     * @return アップロードされたファイル。ファイルがなければnullを返す。
     */
    public FileUpload getFileSingle() {
        if (FileDropPanel.this.fileUploads.isEmpty()) {
            return null;
        } else {
            return FileDropPanel.this.fileUploads.get(0);
        }
    }

}
