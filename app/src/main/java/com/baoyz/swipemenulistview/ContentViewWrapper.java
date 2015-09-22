package com.baoyz.swipemenulistview;

import android.view.View;

/**
 * @author yuchentang wrappered with a flag:reusable
 */
public class ContentViewWrapper {
    public View view;
    public boolean ifReUsable;

    public ContentViewWrapper(View view, boolean ifReUsable) {
        this.view = view;
        this.ifReUsable = ifReUsable;
    }
}
