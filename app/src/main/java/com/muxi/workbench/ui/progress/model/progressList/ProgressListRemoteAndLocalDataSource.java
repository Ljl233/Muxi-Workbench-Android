package com.muxi.workbench.ui.progress.model.progressList;

import androidx.annotation.NonNull;

import com.muxi.workbench.commonUtils.AppExecutors;
import com.muxi.workbench.commonUtils.net.NetUtil;
import com.muxi.workbench.ui.login.model.UserWrapper;
import com.muxi.workbench.ui.progress.model.Progress;
import com.muxi.workbench.ui.progress.model.StickyProgress;
import com.muxi.workbench.ui.progress.model.StickyProgressDao;
import com.muxi.workbench.ui.progress.model.net.GetAStatusResponse;
import com.muxi.workbench.ui.progress.model.net.GetGroupUserListResponse;
import com.muxi.workbench.ui.progress.model.net.GetStatusListResponse;
import com.muxi.workbench.ui.progress.model.net.IfLikeStatusBean;
import com.muxi.workbench.ui.progress.model.net.LikeStatusResponse;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class ProgressListRemoteAndLocalDataSource implements ProgressListDataSource {

    private static ProgressListRemoteAndLocalDataSource INSTANCE;

    private StickyProgressDao mStickyProgressDao;

    private AppExecutors mAppExecutors;

    private final String token = UserWrapper.getInstance().getToken();

    public static ProgressListRemoteAndLocalDataSource getInstance(StickyProgressDao stickyProgressDao, AppExecutors mAppExecutors) {
        if (INSTANCE == null) {
            synchronized (ProgressListRemoteAndLocalDataSource.class) {
                if ( INSTANCE == null ) {
                    INSTANCE = new ProgressListRemoteAndLocalDataSource(stickyProgressDao, mAppExecutors);
                }
            }
        }
        return INSTANCE;
    }

    private ProgressListRemoteAndLocalDataSource(StickyProgressDao stickyProgressDao, AppExecutors appExecutors) {
        mStickyProgressDao = stickyProgressDao;
        mAppExecutors = appExecutors;
    }

    @Override
    public void getProgressList(int page, @NonNull LoadProgressListCallback callback) {

        List<Progress> progressList = new ArrayList<>();

        NetUtil.getInstance().getApi().getStatusList(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( new Observer<GetStatusListResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(GetStatusListResponse getStatusListResponse) {
                        for (GetStatusListResponse.StatuListBean statuListBean : getStatusListResponse.getStatuList() ) {
                            progressList.add(new Progress(statuListBean.getSid(),
                                    statuListBean.getUid(), statuListBean.getAvatar(),
                                    statuListBean.getUsername(), statuListBean.getTime(), statuListBean.getTitle(),
                                    statuListBean.getContent(), statuListBean.isIflike(),
                                    statuListBean.getCommentCount(), statuListBean.getLikeCount()));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        callback.onDataNotAvailable();
                    }

                    @Override
                    public void onComplete() {
                        callback.onProgressListLoaded(progressList);
                    }
                });
    }

        @Override
        public void ifLikeProgress(int sid, boolean iflike, SetLikeProgressCallback callback) {

            NetUtil.getInstance().getApi().ifLikeStatus(sid, new IfLikeStatusBean(iflike?1:0))
                    .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LikeStatusResponse>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(LikeStatusResponse likeStatusResponse) {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        callback.onFail();
                    }

                    @Override
                    public void onComplete() {
                        callback.onSuccessfulSet();
                    }
                });
    }

    @Override
    public void refreshProgressList() {

    }

    @Override
    public void deleteProgress(@NonNull int sid, DeleteProgressCallback callback) {
        NetUtil.getInstance().getApi().deleteStatus(sid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<Void>>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(Response<Void> voidResponse) {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        callback.onFail();
                    }

                    @Override
                    public void onComplete() {
                        callback.onSuccessfulDelete();
                    }
                });
    }

    @Override
    public void setStickyProgress(@NonNull Progress progress) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mStickyProgressDao.addStickyProgress(new StickyProgress(progress));
            }
        };
        mAppExecutors.diskIO().execute(r);

    }

    @Override
    public void getAllStickyProgress(@NonNull LoadStickyProgressCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<StickyProgress> list = mStickyProgressDao.getStickyProgressList();
                List<Progress> progressList = new ArrayList<>();
                for ( int i = 0 ; i < list.size() ; i++ )
                    progressList.add(new Progress(list.get(i)));
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if ( !list.isEmpty() ) {
                            callback.onStickyProgressLoaded(progressList);
                        } else {
                            callback.onDataNotAvailable();
                        }
                    }
                });
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void deleteStickyProgress(int sid) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mStickyProgressDao.deleteStickyProgress(sid);

            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getGroupUserList(int gid, GetGroupUserListCallback callback) {
        List<Integer> UserList = new ArrayList<>();
        NetUtil.getInstance().getApi().getGroupUserList( gid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GetGroupUserListResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(GetGroupUserListResponse getGroupUserListResponse) {
                        for ( int i = 0 ; i < getGroupUserListResponse.getList().size() ; i++ )
                            UserList.add(getGroupUserListResponse.getList().get(i).getUserID());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        callback.onFail();
                    }

                    @Override
                    public void onComplete() {
                        callback.onSuccessfulGet(UserList);
                    }
                });
    }

    @Override
    public void getProgress(int sid, String avatar, String username, int uid, LoadProgressCallback callback) {
        Progress progress = new Progress();
        NetUtil.getInstance().getApi().getAStatus( sid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GetAStatusResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(GetAStatusResponse getAStatusResponse) {
                        progress.setIfLike(getAStatusResponse.getIflike());
                        progress.setLikeCount(getAStatusResponse.getLikeCount());
                        progress.setTime(getAStatusResponse.getTime());
                        progress.setSid(getAStatusResponse.getSid());
                        progress.setContent(getAStatusResponse.getContent());
                        progress.setCommentCount(getAStatusResponse.getCommentList().size());
                        progress.setTitle(getAStatusResponse.getTitle());
                        progress.setAvatar(avatar);
                        progress.setUsername(username);
                        progress.setUid(uid);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onDataNotAvailable();
                    }

                    @Override
                    public void onComplete() {
                        callback.onProgressLoaded(progress);
                    }
                });
    }
}
