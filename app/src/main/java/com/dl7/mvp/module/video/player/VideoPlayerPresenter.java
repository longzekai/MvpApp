package com.dl7.mvp.module.video.player;

import com.dl7.downloaderlib.model.DownloadStatus;
import com.dl7.mvp.local.table.VideoInfo;
import com.dl7.mvp.local.table.VideoInfoDao;
import com.dl7.mvp.module.base.ILoadDataView;
import com.dl7.mvp.module.base.ILocalPresenter;
import com.dl7.mvp.rxbus.RxBus;
import com.dl7.mvp.rxbus.event.VideoEvent;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by long on 2016/11/30.
 * Video Presenter
 */
public class VideoPlayerPresenter implements ILocalPresenter<VideoInfo> {

    private final ILoadDataView mView;
    private final VideoInfoDao mDbDao;
    private final RxBus mRxBus;
    private final VideoInfo mVideoData;
    // 是否数据库有记录
    private boolean mIsContains = false;

    public VideoPlayerPresenter(ILoadDataView view, VideoInfoDao dbDao, RxBus rxBus, VideoInfo videoData) {
        mView = view;
        mDbDao = dbDao;
        mRxBus = rxBus;
        mVideoData = videoData;
        mIsContains = mDbDao.queryBuilder().list().contains(videoData);
    }

    @Override
    public void getData() {
        mDbDao.queryBuilder().rx()
                .oneByOne()
                .filter(new Func1<VideoInfo, Boolean>() {
                    @Override
                    public Boolean call(VideoInfo videoBean) {
                        return mVideoData.equals(videoBean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<VideoInfo>() {
                    @Override
                    public void call(VideoInfo videoBean) {
                        mIsContains = true;
                        mView.loadData(videoBean);
                    }
                });
    }

    @Override
    public void getMoreData() {

    }

    @Override
    public void insert(VideoInfo data) {
        if (mIsContains) {
            mDbDao.update(data);
        } else {
            mDbDao.insert(data);
        }
        mRxBus.post(new VideoEvent());
    }

    @Override
    public void delete(VideoInfo data) {
        if (!data.isCollect() && data.getDownloadStatus() == DownloadStatus.NORMAL) {
            mDbDao.delete(data);
            mIsContains = false;
        } else {
            mDbDao.update(data);
        }
        mRxBus.post(new VideoEvent());
    }

    @Override
    public void update(List<VideoInfo> list) {
    }
}
