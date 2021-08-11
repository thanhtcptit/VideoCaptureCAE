package com.androidwave.camera2video;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.androidwave.camera2video.camera.AutoFitTextureView;
import com.androidwave.camera2video.camera.CameraVideoFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class CameraFragment extends CameraVideoFragment {

    @BindView(R.id.mTextureView)
    AutoFitTextureView mTextureView;
    @BindView(R.id.mRecordVideo)
    ImageView mRecordVideo;
    @BindView(R.id.mVideoView)
    VideoView mVideoView;
    @BindView(R.id.mPlayVideo)
    ImageView mPlayVideo;
    Unbinder unbinder;
    private String mOutputFilePath;

    public CameraFragment() {}

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public int getTextureResource() {
        return R.id.mTextureView;
    }

    @Override
    protected void setUp(View view) {}

    @OnClick({R.id.mRecordVideo, R.id.mPlayVideo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mRecordVideo:
                if (mIsRecordingVideo) {
                    try {
                        stopRecordingVideo();
//                        prepareViews();
                        mRecordVideo.setImageResource(R.drawable.ic_record);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    startRecordingVideo();
                    mRecordVideo.setImageResource(R.drawable.ic_stop);
                    mOutputFilePath = getCurrentFile().getAbsolutePath();
                }
                break;
            case R.id.mPlayVideo:
                mVideoView.start();
                mPlayVideo.setVisibility(View.GONE);
                break;
        }
    }

    private void prepareViews() {
        if (mVideoView.getVisibility() == View.GONE) {
            mVideoView.setVisibility(View.VISIBLE);
            mPlayVideo.setVisibility(View.VISIBLE);
            mTextureView.setVisibility(View.GONE);
            setMediaForRecordVideo();
        }
    }

    private void setMediaForRecordVideo() {
        mVideoView.setMediaController(new MediaController(getActivity()));
        mVideoView.requestFocus();
        mVideoView.setVideoPath(mOutputFilePath);
        mVideoView.seekTo(100);
        mVideoView.setOnCompletionListener(mp -> {
            // Reset player
            mVideoView.setVisibility(View.GONE);
            mTextureView.setVisibility(View.VISIBLE);
            mPlayVideo.setVisibility(View.GONE);
            mRecordVideo.setImageResource(R.drawable.ic_record);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}