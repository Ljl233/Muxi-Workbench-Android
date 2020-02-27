package com.muxi.workbench.ui.progress.view.progressDetail;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.muxi.workbench.R;
import com.muxi.workbench.ui.progress.contract.ProgressDetailContract;
import com.muxi.workbench.ui.progress.model.Comment;
import com.muxi.workbench.ui.progress.model.Progress;
import com.muxi.workbench.ui.progress.model.progressDetail.ProgressDetailRemoteDataSource;
import com.muxi.workbench.ui.progress.model.progressDetail.ProgressDetailRepository;
import com.muxi.workbench.ui.progress.presenter.ProgressDetailPresenter;

import java.util.ArrayList;
import java.util.List;

public class ProgressDetailActivity extends AppCompatActivity implements ProgressDetailContract.View, DialogFragmentDataCallback  {

    private ProgressDetailContract.Presenter mPresenter;
    private ProgressDetailListAdapter mAdapter;
    private Toolbar mToolbar;
    private RecyclerView mProgressDetailRv;
    private CommentSendView mCommentSendView;
    private String mAvatar = " ", mUsername = " ", mTitle = " ";
    private boolean mIfComment;
    private int mPosition; //当前进度在ProgressList页的位置
    private int mSid;

    ProgressDetailListAdapter.ProgressDetailListener mProgressDetailListener = new ProgressDetailListAdapter.ProgressDetailListener() {
        @Override
        public void onLikeClick() {
            mPresenter.setLikeProgress();
        }

        @Override
        public void onEditClick() {
            Toast.makeText(ProgressDetailActivity.this, "去编辑进度", Toast.LENGTH_SHORT).show();
            ///todo 去编辑进度页
        }

        @Override
        public void onCommentClick() {
            CommentEditDialogFragment commentEditDialogFragment = CommentEditDialogFragment.newInstance(mCommentSendView.getContent(), mSid);
            commentEditDialogFragment.show(getSupportFragmentManager(), "CommentEditDialogFragment");
        }

        @Override
        public void onUserClick() {
            Toast.makeText(ProgressDetailActivity.this, "去个人主页", Toast.LENGTH_LONG).show();
            ///todo  去个人主页
        }

        @Override
        public void onDeleteCommentClick() {
            mPresenter.deleteComment();
        }
    };

    /**
     * 跳转到详情页intent的构建
     * @param packageContext   跳转来的页面
     * @param sid              当前进度的sid
     * @param username         当前进度所有者的昵称
     * @param avatar           当前进度所有者的头像
     * @param ifComment        是否获取评论框焦点 true-获取  false-不获取
     * @param position         点击位置，等待传回ProgressList做数据更新
     * @return
     */
    public static Intent newIntent(Context packageContext, int sid, String username, String avatar, boolean ifComment, String title, int position) {
        Intent intent = new Intent(packageContext, ProgressDetailActivity.class);
        intent.putExtra("sid", sid);
        intent.putExtra("avatar", avatar);
        intent.putExtra("username", username);
        intent.putExtra("ifComment", ifComment);
        intent.putExtra("position", position);
        intent.putExtra("title", title);
        return intent;
    }


    @Override
    public void onResume() {
        Log.e("TTTTT", this.hashCode()+" onResume()");
        super.onResume();
    }

    @Override
    public void onStart() {
        Log.e("TTTTT", this.hashCode()+" onStart()");
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.e("TTTTT", this.hashCode()+" onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        Log.e("TTTTT", this.hashCode()+" onStop()");
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.e("TTTTT", this.hashCode()+" onPause()");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.e("TTTTT", this.hashCode()+" onDestroy()");
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("TTTTT", this.hashCode()+" onCreate()");
        setContentView(R.layout.activity_progress_detail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mPresenter = new ProgressDetailPresenter(this, ProgressDetailRepository.getInstance(ProgressDetailRemoteDataSource.getInstance()));

        Intent intent = this.getIntent();
        mIfComment = intent.getBooleanExtra("ifComment", false);
        mPosition = intent.getIntExtra("position", -1);
        mSid = intent.getIntExtra("sid", -1);
        mUsername = intent.getStringExtra("username");
        mAvatar = intent.getStringExtra("avatar");
        mTitle = intent.getStringExtra("title");

        mAdapter = new ProgressDetailListAdapter(this, new Progress(), new ArrayList<Comment>(), mProgressDetailListener, mUsername);

        mProgressDetailRv = findViewById(R.id.rv_progressdetail);
        mProgressDetailRv.setLayoutManager(new LinearLayoutManager(this));
        mProgressDetailRv.setAdapter(mAdapter);

        mToolbar = findViewById(R.id.tb_progressdetail);
        mToolbar.setTitle(mTitle);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//添加默认的返回图标
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TTTTT", ProgressDetailActivity.this.hashCode()+ "  is clicked to return");
                Intent backIntent = new Intent();
                backIntent.putExtra("position", mPosition);
                backIntent.putExtra("sid", mSid);
                backIntent.putExtra("avatar", mAvatar);
                backIntent.putExtra("username", mUsername);
                setResult(RESULT_OK, backIntent);
                finish();
            }
        });


        mCommentSendView = findViewById(R.id.csv_progressdetail);
        mCommentSendView.setBackgroundColor(Color.parseColor("#ffffff"));
        mCommentSendView.setElevation(35);
        mCommentSendView.setOnEditClickListener(v -> mProgressDetailListener.onCommentClick());
        mCommentSendView.setOnSendClickListener(v -> {
            if ( mCommentSendView.getContent() == null || mCommentSendView.getContent().length() == 0 ) {
                Toast.makeText(this, "请输入评论", Toast.LENGTH_LONG).show();
            } else {
                mPresenter.submitComment(mSid, mCommentSendView.getContent());
            }
        });

        mPresenter.start(mSid, mAvatar, mUsername);
        mPresenter.loadProgressAndCommentList();

        if ( mIfComment )
            showEditCommentView();
    }
/*
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ( item.getItemId() == android.R.id.home) {
            Intent backIntent = new Intent();
            backIntent.putExtra("position", mPosition);
            backIntent.putExtra("sid", mSid);
            backIntent.putExtra("avatar", mAvatar);
            backIntent.putExtra("username", mUsername);
            setResult(RESULT_OK, backIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void setPresenter(ProgressDetailContract.Presenter presenter) {
        mPresenter = presenter;
        mPresenter.start(mSid, mAvatar, mUsername);
    }

    @Override
    public void refreshLike(int iflike) {
        mAdapter.refreshProgressLike(iflike);
    }

    @Override
    public void showEditCommentView() {
        CommentEditDialogFragment commentEditDialogFragment = CommentEditDialogFragment.newInstance(mCommentSendView.getContent(), mSid);
        commentEditDialogFragment.show(getSupportFragmentManager(), "CommentEditDialogFragment");
    }

    @Override
    public void clearCommentContent() {
        mCommentSendView.setContent("");
    }

    @Override
    public void showProgressDetail(Progress progress, List<Comment> commentList, String username) {
        mAdapter.refresh(progress, commentList, username);
    }

    @Override
    public void showError() {
        Toast.makeText(this, "失败了", Toast.LENGTH_LONG).show();
    }

    @Override
    public String getCommentText() {
        return mCommentSendView.getContent();
    }

    @Override
    public void setCommentText(String commentTextTemp) {
        mCommentSendView.setContent(commentTextTemp);
    }

    @Override
    public void submitComment(String comment) {
        mPresenter.submitComment(mSid, comment);
    }
}