package com.wast3dmynd.tillr.boundary.views;

import android.support.annotation.AnimRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;
import com.wast3dmynd.tillr.R;


/**
 * Created by sizwe on 2017/01/28 @ 1:35 PM.
 */
public class ContentViewHolder {

    public View contentViewRoot;
    public View contentRecycler;
    public View contentLoader;
    public AVLoadingIndicatorView contentLoaderProgress;
    public TextView contentLoaderInfo;

    public ContentViewHolder(View view) {
        contentViewRoot = view.findViewById(R.id.content_view_root);
        contentRecycler = view.findViewById(R.id.content_recycler);
        initialize(view);
    }


    private void initialize(View view) {
        contentLoader = view.findViewById(R.id.content_loader);
        contentLoaderProgress =  view.findViewById(R.id.content_loader_progress);
        contentLoaderInfo = view.findViewById(R.id.content_loader_info);
        startContentLoaderProgress();
    }


    //region Start and Stop contentLoaderProgress
    public void startContentLoaderProgress() {
        contentLoaderProgress.smoothToShow();
    }

    public void overrideContentRecylerLayoutAnimation(@AnimRes int animLayoutAnimationRes) {
        //override recyclerView animation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(contentRecycler.getContext(), animLayoutAnimationRes);
        ((RecyclerView) contentRecycler).setLayoutAnimation(animation);
    }

    public void stopContentLoaderProgress() {
        contentLoaderProgress.smoothToHide();
    }
    //endregion
}
