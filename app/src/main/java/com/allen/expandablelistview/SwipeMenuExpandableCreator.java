package com.allen.expandablelistview;

import com.baoyz.swipemenulistview.SwipeMenu;


/**
 * @author yuchentang seperate the group and the child's menu creator
 */
public interface SwipeMenuExpandableCreator {

    void createGroup(SwipeMenu menu);

    void createChild(SwipeMenu menu);
}
